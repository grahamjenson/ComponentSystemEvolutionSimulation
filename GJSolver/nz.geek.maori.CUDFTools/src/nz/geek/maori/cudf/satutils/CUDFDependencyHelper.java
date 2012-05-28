package nz.geek.maori.cudf.satutils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import nz.geek.maori.cudf.Keep;
import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.PackageList;
import nz.geek.maori.cudf.PackageVersionConstraint;
import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.Request;
import nz.geek.maori.cudf.satutils.constraints.AbstractConflictSet;
import nz.geek.maori.cudf.satutils.constraints.AbstractConstraint;
import nz.geek.maori.cudf.satutils.constraints.AbstractSATConstraint;
import nz.geek.maori.cudf.satutils.constraints.Literal;
import nz.geek.maori.cudf.satutils.criteria.Criteria;

public class CUDFDependencyHelper {

	public ProfileChangeRequest getProfileChangeRequest() {
		return this.pcr;
	}



	private Criteria lex;

	int count = 1;

	private ProfileChangeRequest pcr;

	private Collection<AbstractConstraint> criteriaConstraints;
	
	public CUDFDependencyHelper(ProfileChangeRequest fullpcr, Criteria criteria) {
		this.pcr = fullpcr.slice();
		Logger.getLogger(getClass().getName()).log(Level.INFO, "Sliced from " + fullpcr.getUniverse().size() + " to " + pcr.getUniverse().size());
		
		this.lex = criteria;
		this.lex.setDependencyHelper(this);
		this.criteriaConstraints = lex.init();

	}

	
	public Criteria getCriteria()
	{
		return lex;
	}


	public Collection<AbstractConstraint> generateCriteriaConstraints() {
		Logger.getLogger(getClass().getName()).log(Level.INFO,"generated " + criteriaConstraints.size() + " critera constraints");
		return criteriaConstraints;
	}
	
	

	// return the variable associated with the name of the package, if it is tru
	// then there is a package with that name in the model

	

	// This is the also very strong set of constraints
	public Collection<AbstractConstraint> generateKeepConstraints() {
		ArrayList<AbstractConstraint> cons = new ArrayList<AbstractConstraint>();
		for (Package p : this.pcr.getInstalledPackages()) {
			if (p.getKeep() != Keep.NONE) {
				if (p.getKeep() == Keep.VERSION) {
					// A unit constraint that the package must remain in the
					// solution
					AbstractSATConstraint keepVersionConstraint = new AbstractSATConstraint();
					keepVersionConstraint.getLits().add(new Literal(true,p));
					cons.add(keepVersionConstraint);
					
				} else if (p.getKeep() == Keep.PACKAGE) {
					// Constraint such that at least one component must be in
					// the solution
					AbstractSATConstraint keepPackageConstraint = new AbstractSATConstraint();
					
					for (Package c : this.pcr.getPackageVersions(p.getName())) {
						keepPackageConstraint.getLits().add( new Literal(true,c));
					}

					cons.add(keepPackageConstraint);

				} else if (p.getKeep() == Keep.FEATURE) {
					for (PackageVersionConstraint pvc : p.getProvides()
							.getList()) {
						AbstractSATConstraint keepFeatureConstraint = new AbstractSATConstraint();
						for (Package c : this.pcr.getPackagesThatSatisfy(pvc)) {
							keepFeatureConstraint.getLits().add(new Literal(true,c));
						}
						cons.add(keepFeatureConstraint);
					}
				}
			}
		}
		Logger.getLogger(getClass().getName()).log(Level.INFO,"generate " + cons.size() + " keep constraints");
		return cons;
	}

	// This is the second strongest form of constraint
	public Collection<AbstractConstraint> generteComponentConstraints(){
		ArrayList<AbstractConstraint> cons = new ArrayList<AbstractConstraint>();
		for (Package p : this.pcr.getUniverse()) {
			if (p.getDepends() != null) {
				for (PackageList packList : p.getDepends().getAnd()) {
					Collection<Package> orPackages = new ArrayList<Package>();

					for (PackageVersionConstraint pvc : packList.getList()) {
						Collection<Package> packagesThatSatisfy = this.pcr
						.getPackagesThatSatisfy(pvc);
						orPackages.addAll(packagesThatSatisfy);
					}

					AbstractSATConstraint dependsOn = new AbstractSATConstraint();
					// p -> dependsSet
					dependsOn.getLits().add(new Literal(false,p));
					for (Package c : orPackages) {
						dependsOn.getLits().add(new Literal(true,c));
					}

					cons.add(dependsOn);
				}
			}

			if (p.getConflicts() != null) {

				// Conflicts
				Literal notp = new Literal(false,p);
				Collection<Package> conflictingPacakges = this.pcr
				.getPackagesThatSatisfy(p.getConflicts());
				for (Package c : conflictingPacakges) {

					if (p == c) {
						continue;
					}

					AbstractSATConstraint conflictWith = new AbstractSATConstraint();
					conflictWith.getLits().add(notp);
					// conflictSet
					Literal notc = new Literal(false,c);
					conflictWith.getLits().add(notc);

					cons.add(conflictWith);
				}
			}

		}
		Logger.getLogger(getClass().getName()).log(Level.INFO, "generated " + cons.size() + " component constraints");
		return cons;
	}

	// P the strongest form of constraints this represents P
	//TODO Make sure that upgrading\installing, or removing virtual-pacakges will work as well.
	public Collection<AbstractConstraint> generteRequestConstraints() {
		ArrayList<AbstractConstraint> cons = new ArrayList<AbstractConstraint>();
		Request r = this.pcr.getRequest();
		if (r != null) {
			PackageList install;
			if ((install = r.getInstall()) != null) {
				for (PackageVersionConstraint pvc : install.getList()) {
					// Install Ensures that at least one of these packages is
					// installed
					AbstractSATConstraint toInstall = new AbstractSATConstraint();
					for (Package p : this.pcr.getPackagesThatSatisfy(pvc)) {
						toInstall.getLits().add(new Literal(true,p));
					}
					if(toInstall.getLits().size() > 0)
					{
						cons.add(toInstall);
					}
					else
					{
						Logger.getLogger(getClass().getName()).log(Level.WARNING,"Installed request " + pvc.toCUDF() + " not existent and ignores");
					}
				}
			}

			PackageList remove  = r.getRemove();
			if (remove != null) {
				for (PackageVersionConstraint pvc : remove.getList()) {
					// Remove ensures that all these packages are not in the
					// solution
					// This means that all these constraints are unit
					
					for (Package p : this.pcr.getPackagesThatSatisfy(pvc)) {
						AbstractSATConstraint toRemove = new AbstractSATConstraint();
						toRemove.getLits().add(new Literal(false, p));
						cons.add(toRemove);
					}

				}
			}

			PackageList upgrade = r.getUpgrade();
			if (upgrade  != null) {
				for (PackageVersionConstraint pvc : upgrade.getList()) {
					
					// Upgrade ensures that
					// 1) there is only one package of this type in the next solution
					// 2) The next packge has a greater than or equal version

					// So the first add a cardinality constraint, ensuring only
					// one of these packages is installed
					AbstractConflictSet conflictSet = new AbstractConflictSet();
					for (Package c : this.pcr.getPackageVersions(pvc.getPackage())) {
						conflictSet.getLits().add(new Literal(true,c));
					}

					cons.add(conflictSet);

					// Must remove versions less than the greatest currently
					// installed version
					// So find the greatest installed Version
					Collection<Package> installedPacakges = this.pcr.getInstalledPackageVersions(pvc.getPackage());
					int maxVersion = -1;
					for (Package posMax : installedPacakges) {
						if (maxVersion < posMax.getVersion()) {
							maxVersion = posMax.getVersion();
						}
					}

					// Filter all packages less than maxVersion
					AbstractSATConstraint greaterThanPackages = new AbstractSATConstraint();
					for (Package p : this.pcr.getPackagesThatSatisfy(pvc)) {
						if (maxVersion <= p.getVersion()) {
							greaterThanPackages.getLits().add(new Literal(true,p));
						}
					}
					// Add Constraint that one of the better than or equal to
					// maxVersion of the installed Packages must be installed
					//cons.add(greaterThanPackages);
				}
			}
		}
		Logger.getLogger(getClass().getName()).log(Level.INFO,"generated " + cons.size() + " request constraints");
		return cons;
	}



}
