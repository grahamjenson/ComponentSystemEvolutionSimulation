package nz.geek.maori.cudf.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import nz.geek.maori.cudf.CUDFFactory;
import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.PackageVersionConstraint;
import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.Relation;
import nz.geek.maori.cudf.Request;
import nz.geek.maori.cudf.parser.Parser;

public class UtilMain {

	public static void usage() {
		System.out.println("cudftool onlyInstalled [cudfFile]");
		System.out.println("cudftool merge-request [requestcudf] [cudffile]");
		System.out
				.println("cudftool generate-delta-set [cudffile] [packagelistfile] [number of deltas] [numberof packages per delta] [outputdir]");
		System.out.println("cudftool copyInstalled [cudfIn] [cudfOut]");
		System.out
				.println("cudftool merge-RequestANDcopyInstalled [requestcudf] [cudfIn] [cudfOut]");
	}

	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			usage();
			return;
		}

		if (args[0].equals("generate-delta-set")) {

			File cudfFile = new File(args[1]);
			if (!cudfFile.exists()) {
				throw new FileNotFoundException("CUDF File does not Exist");
			}
			ProfileChangeRequest cudf = Parser.parse(cudfFile);

			File packalistfile = new File(args[2]);
			if (!packalistfile.exists()) {
				throw new FileNotFoundException(
						"PackageList File does not Exist");
			}
			ArrayList<String> packageList = new ArrayList<String>();
			BufferedReader br = new BufferedReader(
					new FileReader(packalistfile));
			String line = br.readLine();
			while (line != null) {
				packageList.add(line.trim());
				line = br.readLine();
			}

			int numberOfDeltas = Integer.valueOf(args[3]);
			int numberOfPackagesPerDelta = Integer.valueOf(args[4]);

			String outputDirectory = args[5];

			int g = 0;
			ArrayList<String> delta = new ArrayList<String>();
			for (; g < numberOfDeltas; g++) {
				System.out.println("Generating delta " + g);
				Random r = new Random();

				for (int i = 0; i < numberOfPackagesPerDelta; i++) {
					int sel = r.nextInt(packageList.size());
					delta.add(packageList.get(sel));
					packageList.remove(sel);

				}

				Request re = CUDFFactory.eINSTANCE.createRequest();
				re.setInstall(CUDFFactory.eINSTANCE.createPackageList());

				for (String packageName : delta) {
					Collection<Package> gp = cudf
							.getGreatestVersion(packageName);
					PackageVersionConstraint pvc = CUDFFactory.eINSTANCE
							.createPackageVersionConstraint();
					Package p;
					if (gp.size() == 1) {
						p = gp.iterator().next();
					} else {
						System.out.println(" no " + packageName);
						return;
					}
					pvc.setPackage(p.getName());
					pvc.setRel(Relation.EQUALS);
					pvc.setVersion(p.getVersion());

					re.getInstall().getList().add(pvc);
				}

				File f = new File(outputDirectory + "/g" + g + ".request");
				f.createNewFile();

				OutputStreamWriter bwriter = new OutputStreamWriter(
						new FileOutputStream(f), Charset.forName("US-ASCII"));
				bwriter.write(re.toCUDF());
				bwriter.close();
			}

			return;
		} else if (args[0].equals("merge-request")) {
			File requestFile = new File(args[1]);
			if (!requestFile.exists()) {
				throw new FileNotFoundException("Request File does not Exist");
			}

			File cudfFile = new File(args[2]);
			if (!cudfFile.exists()) {
				throw new FileNotFoundException("Cudf File does not Exist");
			}

			ProfileChangeRequest request = Parser.parse(requestFile);
			ProfileChangeRequest cudf = Parser.parse(cudfFile);

			cudf.setRequest(request.getRequest());

			System.out.println(cudf.toCUDF());
			return;
		} else if (args[0].equals("copyInstalled")) {
			File inFile = new File(args[1]);
			if (!inFile.exists()) {
				throw new FileNotFoundException("Cudf In Does Not exist");
			}

			File outFile = new File(args[2]);
			if (!outFile.exists()) {
				throw new FileNotFoundException("Cudf Out does not Exist");
			}

			ProfileChangeRequest in = Parser.parse(inFile);
			ProfileChangeRequest out = Parser.parse(outFile);

			for (Package p : in.getInstalledPackages()) {
				out.getPackage(p.getName(), p.getVersion()).setInstalled(true);
			}

			ProfileChangeRequest newPCR = CUDFFactory.eINSTANCE
					.createProfileChangeRequest();

			for (Package p : out.getUniverse()) {
				newPCR.addPackage(p);
			}
			newPCR.setPreamble(out.getPreamble());
			newPCR.setRequest(out.getRequest());

			System.out.println(newPCR.toCUDF());
			return;
		} else if (args[0].equals("onlyInstalled")) {
			File inFile = new File(args[1]);
			if (!inFile.exists()) {
				throw new FileNotFoundException("Cudf In Does Not exist");
			}

			ProfileChangeRequest in = Parser.parse(inFile);
			ProfileChangeRequest newPCR = CUDFFactory.eINSTANCE
					.createProfileChangeRequest();

			for (Package p : in.getInstalledPackages()) {
				p.setInstalled(true);
				newPCR.addPackage(p);
			}

			System.out.println(newPCR.toCUDF());
			return;
		} else if (args[0].equals("merge-RequestANDcopyInstalled")) {
			File requestFile = new File(args[1]);
			if (!requestFile.exists()) {
				throw new FileNotFoundException("Request File does not Exist");
			}

			File inFile = new File(args[2]);
			if (!inFile.exists()) {
				throw new FileNotFoundException("Cudf In Does Not exist");
			}

			File outFile = new File(args[3]);
			if (!outFile.exists()) {
				throw new FileNotFoundException("Cudf Out does not Exist");
			}

			ProfileChangeRequest request = Parser.parse(requestFile);

			ProfileChangeRequest in = Parser.parse(inFile);
			ProfileChangeRequest repository = Parser.parse(outFile);

			for (Package p : in.getInstalledPackages()) {
				repository.getPackage(p.getName(), p.getVersion())
						.setInstalled(true);
			}

			ProfileChangeRequest newPCR = CUDFFactory.eINSTANCE
					.createProfileChangeRequest();

			for (Package p : repository.getUniverse()) {
				newPCR.addPackage(p);
			}
			newPCR.setPreamble(repository.getPreamble());
			newPCR.setRequest(request.getRequest());

			System.out.println(newPCR.toCUDF());
			System.out.println();
			return;
		}
	}
}
