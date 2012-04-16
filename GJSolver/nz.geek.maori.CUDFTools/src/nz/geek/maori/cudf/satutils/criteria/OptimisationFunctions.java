package nz.geek.maori.cudf.satutils.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import nz.geek.maori.cudf.CUDFFactory;
import nz.geek.maori.cudf.Keep;
import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.PackageFormula;
import nz.geek.maori.cudf.PackageList;
import nz.geek.maori.cudf.PackageVersionConstraint;
import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.Request;
import nz.geek.maori.cudf.satutils.constraints.Model;

/*
 * Suppose that a solver tool outputs for the initial universe I a proposed solution S. We write V(X,name) 
 * the set of versions in which name (the name of a package) is installed in X, where X may be I or S. 
 * That set may be empty (name is not installed), contain one element (name is installed in exactly that version), 
 * or even contain multiple elements in case a package is installed in multiple versions.
 */
public class OptimisationFunctions {

	public static String report(ProfileChangeRequest pcrFrom,
			ProfileChangeRequest pcrTo) {
		String delim = "|";
		StringBuffer sb = new StringBuffer();
		sb.append("From size: ");
		sb.append(pcrFrom.getInstalledPackages().size());
		sb.append(delim);

		sb.append("To size: ");
		sb.append(pcrTo.getInstalledPackages().size());
		sb.append(delim);

		sb.append("removed: ");
		sb.append(removed(pcrFrom, pcrTo));
		sb.append(delim);

		sb.append("changed: ");
		sb.append(changedPacakges(pcrFrom, pcrTo));
		sb.append(delim);

		sb.append("new: ");
		sb.append(newPacakges(pcrFrom, pcrTo));
		sb.append(delim);

		sb.append("not up to date: ");
		sb.append(notUpToDatePacakges(pcrFrom, pcrTo));
		sb.append(delim);

		sb.append("unmet reccommends: ");
		sb.append(unsatisfiedReccommends(pcrFrom, pcrTo));

		return sb.toString();
	}

	/*
	 * #removed(I,S) is the number of packages removed in the proposed solution
	 * S w.r.t. the original installation I:
	 * 
	 * #removed(I,S) = #{name | V(I,name) nonempty and V(S,name) empty}
	 */
	public static int removed(ProfileChangeRequest pcrFrom,
			ProfileChangeRequest pcrTo) {
		int ri = 0;
		for (nz.geek.maori.cudf.Package package1 : pcrFrom
				.getInstalledPackages()) {
			Collection<Package> packageVersions = pcrTo
			.getInstalledPackageVersions(package1.getName());
			if ((packageVersions == null) || (packageVersions.size() == 0)) {
				ri++;
			}
		}
		return ri;
	}

	/*
	 * #new(I,S)is the number of new packages in the proposed solution S w.r.t.
	 * the original installation I:
	 * 
	 * #new(I,S) = #{name| V(I,name) empty and V(S,name) nonempty}
	 */
	public static int newPacakges(ProfileChangeRequest pcrFrom,
			ProfileChangeRequest pcrTo) {
		int ri = 0;
		for (String name : pcrTo.getPackageNames(true)) {

			if (pcrFrom.getInstalledPackageVersions(name).size() == 0) {
				ri++;
			}
		}
		return ri;
	}

	/*
	 * #changed(I,S) is the number of packages with a modified (set of)
	 * version(s) in the proposed solution S w.r.t. the original installation I
	 * :
	 * 
	 * #changed(I,S) = #{name | V(I,name) different to V(S,name) }
	 */
	public static int changedPacakges(ProfileChangeRequest pcrFrom,
			ProfileChangeRequest pcrTo) {
		int ci = 0;

		for (String pn : pcrFrom.getPackageNames(false)) {
			// If we have already determined that the package has changed we do
			// not need to look into it


			Set<Package> pFrom = new HashSet(pcrFrom.getPackageVersions(pn,true));
			Set<Package> pTo = new HashSet(pcrTo.getPackageVersions(pn,true));


			if (!pFrom.equals(pTo)) {
				ci++;
			}

		}

		return ci;
	}

	/*
	 * # #notuptodate(I,S) is the number of installed packages but not in the
	 * latest available version:
	 * 
	 * #notuptodate(I,S) = #{name| V(S,name) nonempty and does not contain the
	 * most recent version of name in S}
	 */
	public static int notUpToDatePacakges(ProfileChangeRequest pcrFrom, ProfileChangeRequest pcrTo) {
		int notuptoDate = 0;
		for (String name : pcrTo.getPackageNames(true)) {
			// If we have already searched this package name
			// Create a constraint to search for the versions better than this

			Package greatestVerion = pcrFrom.getGreatestVersion(name).iterator().next();

			// This should only return one answer and that answer should be the
			// same name, as it is not virtual
			if(pcrTo.getPackage(greatestVerion.getName(), greatestVerion.getVersion()) == null)
			{
				notuptoDate++;
			}


		}
		return notuptoDate;
	}

	public static int upToDateDistance(ProfileChangeRequest pcrFrom, ProfileChangeRequest pcrTo) {
		int distance = 0;
		for (String name : pcrTo.getPackageNames(true)) {
			ArrayList<Package> packs = new ArrayList<Package>(pcrFrom.getPackageVersions(name));

			Collections.sort(packs, new Comparator<Package>() {

				@Override
				public int compare(Package o1, Package o2) {
					return o2.getVersion() - o1.getVersion();
				}
			});

			for(Package p : pcrTo.getPackageVersions(name,true))
			{
				distance += packs.indexOf(p);
			}

		}
		return distance;
	}

	/*
	 * #unsatisfied-recommends(I,S) counts the number of disjunctions in
	 * Recommends-fields of installed packages that are not satisfied by S:
	 * 
	 * #unsatisfied-recommends(I,S) = { (name,v,c) | v is an element of
	 * V(S,name) and (name,v) recommends ..., c, ... and c is not satisfied by S
	 * }For instance, if package a recommends
	 * 
	 * b, c|d|e, e|f|g, b|g, h
	 * 
	 * and if S installs a, e, f, and h, but neither of b, c, d, or g, then one
	 * would obtain for the package a alone a value of 2 for
	 * #unsatisfied-recommends since the 2nd, 3rd and 5th disjunct of the
	 * recommendation are satisfied,and the others are not. If no other package
	 * contains recommendations that means that, in that case,
	 * #unsatisfied-recommends(I,S)=2.Note that in any case the value of
	 * #unsatisfied-recommends(I,S) only depends on S but not on I.
	 */
	public static int unsatisfiedReccommends(ProfileChangeRequest pcrFrom, ProfileChangeRequest pcrTo) {
		int ur = 0;
		for (nz.geek.maori.cudf.Package p : pcrTo.getInstalledPackages()) {
			// Get all installed PAckages reccommends
			p = pcrFrom.getPackage(p.getName(), p.getVersion());
			PackageFormula pf = (PackageFormula) p.getProperties().get("recommends");
			if (pf == null) {
				// No recomends
				continue;
			}
			// For each reccommends calculate the
			for (PackageList pl : pf.getAnd()) {
				// Get OR, one of the packages must be installed for the
				// recommends to be TRUE

				if(!atLeastOneInstalled(pcrFrom.getPackagesThatSatisfy(pl), pcrTo))
				{
					ur++;
				}

			}
		}

		return ur;
	}


	public static int hamming(ProfileChangeRequest pcrFrom, ProfileChangeRequest pcrTo) {
		int hamming = 0;
		for (nz.geek.maori.cudf.Package p : pcrTo.getInstalledPackages()) {
			Package pf = pcrFrom.getPackage(p.getName(), p.getVersion());
			if(!pf.isInstalled())
			{
				hamming++;
			}
		}
		for (nz.geek.maori.cudf.Package p : pcrTo.getNotInstalledPackages()) {
			Package pf = pcrFrom.getPackage(p.getName(), p.getVersion());
			if(pf.isInstalled())
			{
				hamming++;
			}
		}
		return hamming;
	}

	public static boolean checkSolution(ProfileChangeRequest pcrFrom, ProfileChangeRequest pcrTo)
	{
		//CheckRequest
		{
			Request req = pcrFrom.getRequest();
			//Check Installed
			{
				if(req.getInstall() != null)
				{
					for(PackageVersionConstraint pf : req.getInstall().getList())
					{
						Collection<Package> packagesThatSatisfy = pcrFrom.getPackagesThatSatisfy(pf);
						if(!atLeastOneInstalled(packagesThatSatisfy, pcrTo))
						{
							return false;
						}
					}
				}
			}
			//Check Remove			
			{
				if(req.getRemove() != null)
				{
					for(PackageVersionConstraint pf : req.getRemove().getList())
					{
						if(pcrTo.getPackagesThatSatisfy(pf).size() > 0)
						{
							return false;
						}
					}
				}
			}
			//Check Upgrade
			{
				if(req.getUpgrade() != null)
				{
					for(PackageVersionConstraint pf : req.getUpgrade().getList())
					{

						Collection<Package> packagesThatSatisfy = pcrTo.getPackagesThatSatisfy(pf);
						Package next = packagesThatSatisfy.iterator().next();
						Collection<Package> versions = pcrFrom.getInstalledPackageVersions(next.getName());
						int max = -1;

						for(Package p : versions)
						{
							if(p.getVersion() > max)
							{
								max = p.getVersion();
							}
						}

						Package greatestVersion = pcrTo.getGreatestVersion(next.getName()).iterator().next();

						if(packagesThatSatisfy.size() != 1 || !(greatestVersion.getVersion() <= next.getVersion()))
						{
							return false;
						}
					}
				}
			}
		}

		//Check All package Dependencies are satisfied
		{
			//PCRTo may not hae all the required dependencies
			for(Package pTo : pcrTo.getInstalledPackages())
			{

				Package p = pcrFrom.getPackage(pTo.getName(), pTo.getVersion());
				if(p.getDepends() != null)
				{
					//Check Depdends
					for(PackageList pl : p.getDepends().getAnd())
					{
						if(!atLeastOneInstalled(pcrFrom.getPackagesThatSatisfy(pl),pcrTo))
						{
							return false;
						}
					}
				}

				//Check Conflicts
				if(p.getConflicts() != null)
				{
					Collection<Package> packagesThatSatisfy = pcrFrom.getPackagesThatSatisfy(p.getConflicts());
					packagesThatSatisfy.remove(p);
					if(atLeastOneInstalled(packagesThatSatisfy,pcrTo))
					{
						return false;
					}
				}
			}

		}

		//Check All Keep Constraints from pcrFrom
		{
			for(Package p : pcrFrom.getInstalledPackages())
			{				
				if(p.getKeep() != null && p.getKeep() != Keep.NONE)
				{
					if(p.getKeep() == Keep.VERSION)
					{
						if(pcrTo.getInstalledPackageVersion(p.getName(), p.getVersion()) == null)
						{
							return false;
						}

					}
					else if(p.getKeep() == Keep.PACKAGE)
					{
						if(pcrTo.getInstalledPackageVersions(p.getName()).size() == 0)
						{
							return false;
						}
					}
					else if(p.getKeep() == Keep.FEATURE)
					{
						return false;
					}
					else
					{
						return false;
					}
				}
			}
		}
		return true;
	}

	private static boolean atLeastOneInstalled(Collection<Package> ps, ProfileChangeRequest pcrTo)
	{
		for(Package p : ps)
		{
			if(pcrTo.getInstalledPackageVersion(p.getName(), p.getVersion()) != null)
			{
				return true;
			}
		}
		return false;
	}


}
