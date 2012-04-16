package nz.geek.maori.cudf.satutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.PackageList;
import nz.geek.maori.cudf.PackageVersionConstraint;
import nz.geek.maori.cudf.ProfileChangeRequest;

public class PredictiveUse {

	public static class Graph {
		ArrayList<PackageWrapper> verticies = new ArrayList<PackageWrapper>();
		HashMap<Package, PackageWrapper> map = new HashMap<Package, PackageWrapper>();

		public HashMap<Package, PackageWrapper> getMap() {
			return this.map;
		}

		public void addVerties(PackageWrapper pw) {
			this.verticies.add(pw);
		}

		public ArrayList<PackageWrapper> getVerticies() {
			return this.verticies;
		}

	}

	public static class PackageWrapper {
		public Package p;
		// Weights are maintained in other nodes
		HashSet<PackageWrapper> inEdges = new HashSet<PackageWrapper>();

		HashMap<PackageWrapper, Double> outEdges = new HashMap<PackageWrapper, Double>();

		HashSet<PackageWrapper> conflicts = new HashSet<PackageWrapper>();

		double value = 0;
		boolean dirty = true;
		double oldvalue = 0;
		private Map<PackageWrapper, Double> prob = new HashMap<PackageWrapper, Double>();

		public Collection<PackageWrapper> getProbabilitiesGivenKeySet() {
			return this.prob.keySet();
		}

		public double getProbabilityGiven(PackageWrapper x) {
			Double double1 = this.prob.get(x);
			if (double1 == null) {
				return 0.0;
			}
			return double1;
		}

		public void setProbabilityGiven(PackageWrapper x, double value) {
			this.prob.put(x, value);
		}

		public PackageWrapper(Package p) {
			this.p = p;
		}

		@Override
		public int hashCode() {
			return this.p.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof PackageWrapper) {
				return this.p.equals(((PackageWrapper) obj).p);
			}
			return false;

		}

		@Override
		public String toString() {
			return this.p.toString();
		}

		public void addConflict(PackageWrapper confl) {
			this.conflicts.add(confl);
		}

		public void addOutEdge(PackageWrapper out, double weight) {
			this.outEdges.put(out, weight);
		}

		public void addInEdge(PackageWrapper in) {
			this.inEdges.add(in);
		}

		public HashSet<PackageWrapper> getInEdges() {
			return this.inEdges;
		}

		public HashMap<PackageWrapper, Double> getOutEdges() {
			return this.outEdges;
		}

		public HashSet<PackageWrapper> getConflicts() {
			return this.conflicts;
		}

	}

	Graph graph;

	public Map<Package, Double> getPackageUse(Package p) {
		HashMap<Package, Double> hm = new HashMap<Package, Double>();
		PackageWrapper pp = this.graph.getMap().get(p);
		for (PackageWrapper pw : this.graph.verticies) {
			Double value = pw.prob.get(pp);
			if (value != null) {
				hm.put(pw.p, value);
			}
		}
		return hm;
	}

	public double getPackagePredictiveUse(Package p) {
		PackageWrapper packageWrapper = this.graph.getMap().get(p);
		return packageWrapper.value;
	}

	private void writeCache(File f) {
		try {
			f.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			for (PackageWrapper p : this.graph.getVerticies()) {
				bw.write(p.p.getName() + "@" + p.p.getVersion());
				bw.write("=");
				// bw.write("{");
				String delim = "";
				for (PackageWrapper pm : p.getProbabilitiesGivenKeySet()) {
					bw.write(delim);
					delim = ",";
					bw.write(pm.p.getName() + "@" + pm.p.getVersion());
					bw.write(":");
					bw.write("" + p.prob.get(pm));

				}
				// bw.write("}");
				bw.write("\n");
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Pass name@version string to this
	private PackageWrapper getPackageFromCache(String pnv,
			ProfileChangeRequest cx) {
		String[] nv = pnv.split("@");
		return this.graph.map.get(cx.getPackage(nv[0], Integer.valueOf(nv[1])));
	}

	private HashMap<PackageWrapper, Map<PackageWrapper, Double>> readCahceFile(
			File f, ProfileChangeRequest cx) {
		HashMap<PackageWrapper, Map<PackageWrapper, Double>> ret = new HashMap<PackageWrapper, Map<PackageWrapper, Double>>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(f));

			String line = br.readLine();
			while (line != null) {
				String[] p = line.split("=");
				PackageWrapper pw = getPackageFromCache(p[0], cx);
				HashMap<PackageWrapper, Double> localMap = new HashMap<PackageWrapper, Double>();
				ret.put(pw, localMap);
				for (String s : p[1].split(",")) {
					String[] packageValue = s.split(":");
					PackageWrapper mpw = getPackageFromCache(packageValue[0],
							cx);
					localMap.put(mpw, Double.valueOf(packageValue[1]));
				}

				// Last
				line = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ret;
	}

	private boolean cache = true;

	public PredictiveUse(ProfileChangeRequest cx, Collection<Package> xAndDelta) {
		this.graph = createGraph(cx);

		ArrayList<PackageWrapper> xd = new ArrayList<PackageWrapper>();
		for (Package pp : xAndDelta) {
			xd.add(this.graph.getMap().get(pp));
		}

		boolean needToWrite = true;
		if (this.cache
				&& (cx.getPreamble() != null)
				&& (cx.getPreamble().getUchecksum() != null)
				&& (new File(cx.getPreamble().getUchecksum() + ".cache"))
						.exists()) {
			needToWrite = false;
			File cachedFile = new File(cx.getPreamble().getUchecksum()
					+ ".cache");
			HashMap<PackageWrapper, Map<PackageWrapper, Double>> cachedMap = readCahceFile(
					cachedFile, cx);
			for (PackageWrapper p : this.graph.getVerticies()) {
				p.prob = cachedMap.get(p);
				predictiveUseForPackage(xd, p);

			}

		} else {
			iterDependsValues();
		}

		if (this.cache && (cx.getPreamble() != null)
				&& (cx.getPreamble().getUchecksum() != null) && needToWrite) {
			File cachedFile = new File(cx.getPreamble().getUchecksum()
					+ ".cache");

			writeCache(cachedFile);
		}

	}

	private Graph createGraph(ProfileChangeRequest cx) {
		Graph graph = new Graph();

		for (Package p : cx.getUniverse()) {
			PackageWrapper pw = new PackageWrapper(p);
			graph.getMap().put(p, pw);
			graph.addVerties(pw);
		}

		for (Package p : cx.getUniverse()) {
			PackageWrapper pw = graph.getMap().get(p);

			if (p.getDepends() != null) {

				for (PackageList packList : p.getDepends().getAnd()) {
					HashSet<Package> orPackages = new HashSet<Package>();

					for (PackageVersionConstraint pvc : packList.getList()) {
						Collection<Package> packagesThatSatisfy = cx
								.getPackagesThatSatisfy(pvc);

						orPackages.addAll(packagesThatSatisfy);
					}

					for (Package c : orPackages) {
						PackageWrapper pwc = graph.getMap().get(c);
						double weight = 1. / orPackages.size();
						Double edgeW = pw.getOutEdges().get(pwc);
						if (edgeW != null) {
							pw.addOutEdge(pwc, edgeW + weight - (edgeW * weight));
						} else {
							pw.addOutEdge(pwc, weight);
							pwc.addInEdge(pw);
						}

					}
				}
			}

			if (p.getConflicts() != null) {
				// Conflicts
				Collection<Package> conflictingPacakges = cx
						.getPackagesThatSatisfy(p.getConflicts());
				for (Package c : conflictingPacakges) {
					if (p == c) {
						continue;
					}
					PackageWrapper pwc = graph.getMap().get(c);
					pwc.addConflict(pw);
					pw.addConflict(pwc);
				}
			}

		}
		return graph;
	}

	private void predictiveUseForPackage(Collection<PackageWrapper> xAndDelta,
			PackageWrapper a) {
		a.value = 0;
		double xValue = 0.0;
		for (PackageWrapper b : xAndDelta) {
			double aGivenb = aGivenb(a, b);
			xValue = xValue + aGivenb - (xValue * aGivenb);
		}

		double fValue = 0.0;
		for (PackageWrapper b : a.getProbabilitiesGivenKeySet()) {
			double aGivenb = aGivenb(a, b);
			fValue += (aGivenb + fValue - (fValue * aGivenb))
					/ this.graph.getVerticies().size();
		}

		double finalValue = xValue + fValue - (xValue * fValue);
		a.value = finalValue;
	}

	private double aGivenb(PackageWrapper a, PackageWrapper b) {
		double depends = a.getProbabilityGiven(b);
		if (depends == 0) {
			return 0;
		}
		double confl = 0.0;
		for (PackageWrapper c : a.getConflicts()) {
			double probabilityGiven = c.getProbabilityGiven(b);
			confl = confl + probabilityGiven - (probabilityGiven * confl);
		}

		return depends * (1 - confl);
	}

	private void iterDependsValues() {

		int iter = 0;
		int maxIter = 100;
		double diff = 1;
		double stoppingDiff = 0.015; // Should be half the average error
		double localStoppingDiff = 1.0 / this.graph.verticies.size();
		while (((diff > stoppingDiff) && (iter < maxIter))) {
			long t1 = System.currentTimeMillis();
			diff = 0;
			iter++;
			System.out.println("iter " + iter);
			int c = 0;
			for (PackageWrapper a : this.graph.verticies) {
				if (!a.dirty) {
					continue;
				}

				c++;
				double oldvalue = a.value;
				pullAssuming(a);
				predictiveUseForPackage(new ArrayList<PackageWrapper>(), a);
				double newValue = a.value;
				double localdiff = Math.abs(oldvalue - newValue);
				diff = Math.max(diff, localdiff);

				if (localdiff < localStoppingDiff) {
					a.dirty = false;
				} else {
					for (PackageWrapper pw : a.getOutEdges().keySet()) {
						pw.dirty = true;
					}
				}

			}
			System.out.println("Processed " + c + " in time "
					+ (System.currentTimeMillis() - t1));
			System.out.println(diff);
		}
	}

	private void pullAssuming(PackageWrapper x) {

		HashMap<PackageWrapper, Double> vals = new HashMap<PackageWrapper, Double>();
		vals.put(x, 1.0);
		Collection<PackageWrapper> ins = x.getInEdges();
		for (PackageWrapper in : ins) {
			// if edge is dirty it has some new value to be pulled
			// if(!in.dirty) continue;

			double inw = in.outEdges.get(x);
			for (PackageWrapper p : in.getProbabilitiesGivenKeySet()) {
				Double weight = vals.get(p);

				if (weight == null) {
					weight = 0.0;
				}

				double val = in.getProbabilityGiven(p) * inw;

				vals.put(p, weight + val - (weight * val));
			}
		}
		double minweight = 0.1;
		x.prob = new HashMap<PackageWrapper, Double>();
		for (PackageWrapper p : vals.keySet()) {
			Double weight = vals.get(p);
			if (weight > minweight) {
				x.prob.put(p, weight);
			}
		}
	}

	public double getPredictiveUse(ProfileChangeRequest cx,
			ProfileChangeRequest mn) {
		HashSet<Package> mncx = new HashSet<Package>();
		HashSet<Package> cxmn = new HashSet<Package>();

		cxmn.addAll(cx.getInstalledPackages());
		cxmn.removeAll(mn.getInstalledPackages());

		mncx.addAll(mn.getInstalledPackages());
		mncx.removeAll(cx.getInstalledPackages());

		double total = 0.0;

		for (Package p : cxmn) {
			total += this.graph.getMap().get(p).value;
		}

		for (Package p : mncx) {
			total += (1 - this.graph.getMap().get(p).value);
		}

		return total;
	}

	public void printValues() {
		Collections.sort(this.graph.verticies,
				new Comparator<PackageWrapper>() {

					@Override
					public int compare(PackageWrapper o1, PackageWrapper o2) {
						if (o1.value > o2.value) {
							return 1;
						} else if (o1.value < o2.value) {
							return -1;
						} else {
							return 0;
						}
					}

				});
		for (PackageWrapper p : this.graph.verticies) {
			System.out.println(p + " , " + p.prob.size());
		}
		System.out.println("Finished Predictive use");
	}

	public void dotPrint(Writer os, double minvalue, double minedgeWeight) {
		try {

			os.write("digraph g {\n");
			for (PackageWrapper pw : this.graph.getVerticies()) {
				if (pw.value < minvalue) {
					continue;
				}
				os.write("\"" + pw + "\" ["
						+ (pw.p.isInstalled() ? "color=blue" : "") + "] \n");
				System.out.println(pw + " : " + pw.value);
				for (PackageWrapper d : pw.getOutEdges().keySet()) {
					double weight = pw.getOutEdges().get(d);
					if ((d.value < minvalue) || (weight < minedgeWeight)) {
						continue;
					}
					os.write("\"" + pw + "\" -> \"" + d + "\" [arrowsize="
							+ weight + "] \n");
				}
				for (PackageWrapper c : pw.getConflicts()) {
					if (c.value < minvalue) {
						continue;
					}
					os.write("\"" + pw + "\" -> \"" + c
							+ "\" [color=red,arrowsize=0,constraint=false] \n");
				}
			}
			os.write("}\n");
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
