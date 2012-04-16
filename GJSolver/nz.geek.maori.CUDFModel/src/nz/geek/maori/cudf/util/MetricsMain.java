package nz.geek.maori.cudf.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;

import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.parser.Parser;

public class MetricsMain {

	public static void usage() {
		System.out
				.println("cudfMetrics dynamic [cudfPrev] [cudfNow] -> removed,added");
		System.out.println("cudfMetrics static [cudfFile] - > size,");
	}

	public static void main(String[] args) throws FileNotFoundException {
		if (args.length == 0) {
			usage();
			return;
		}

		if (args[0].equals("static")) {
			File cudfFile = new File(args[1]);
			if (!cudfFile.exists()) {
				throw new FileNotFoundException("Request File does not Exist");
			}

			ProfileChangeRequest cudf = Parser.parse(cudfFile);
			int size = cudf.getInstalledPackages().size();

			// output
			System.out.println(size);
			return;

		} else if (args[0].equals("dynamic")) {
			// generation n file
			File gnFile = new File(args[1]);
			if (!gnFile.exists()) {
				throw new FileNotFoundException("Request File does not Exist");
			}
			// generation n + 1 file
			File gnp1File = new File(args[2]);
			if (!gnp1File.exists()) {
				throw new FileNotFoundException("Cudf File does not Exist");
			}

			ProfileChangeRequest gn = Parser.parse(gnFile);
			ProfileChangeRequest gnp1 = Parser.parse(gnp1File);

			int removed = complimentCardinality(gnp1, gn).size();
			// added
			int added = complimentCardinality(gn, gnp1).size();

			System.out.println(removed + "," + added);
			return;
		}

	}

	public static Collection<Package> complimentCardinality(
			ProfileChangeRequest gn, ProfileChangeRequest gnp1) {
		ArrayList<Package> col = new ArrayList<Package>();
		for (Package p : gnp1.getInstalledPackages()) {
			Package package1 = gn.getPackage(p.getName(), p.getVersion());
			if ((package1 == null) || !package1.isInstalled()) {
				col.add(p);
			}
		}
		return col;
	}

}
