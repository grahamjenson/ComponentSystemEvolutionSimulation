package nz.geek.maori.cudftests;

import java.io.File;

import junit.framework.TestCase;
import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.Type;
import nz.geek.maori.cudf.parser.Parser;

public class ParserTest extends TestCase {

	@Override
	public void setUp() {

	}

	// Basically parse a bunch of examples for errors
	// public void testAllExamples() throws Exception
	// {
	// File root = new File("./testData/");
	//
	// ArrayList<File> files = new ArrayList<File>();
	// for(File f : root.listFiles())
	// {
	// if(f.getName().endsWith(".cudf"))
	// {
	// files.add(f);
	// }
	// }
	//
	// for(File f : files)
	// {
	// ProfileChangeRequest pcr = Parser.parse(f);
	// pcr.toCUDF();
	// }
	//
	// }

	public void testSpecificExamples() throws Exception {
		File f = new File("./testData/parsingTest.cudf");

		ProfileChangeRequest pcr = Parser.parse(f);
		// Three Pacakges, 1 installed, 1 explicitly uninstalled, 1 default not
		// installed

		System.out.println(pcr.toCUDF());
		testPreamble(pcr);
		testRequest(pcr);
		testPackages(pcr);
	}

	private void testPackages(ProfileChangeRequest pcr) {
		assertTrue(pcr.getInstalledPackages().size() == 1);
		assertTrue(pcr.getNotInstalledPackages().size() == 2);

	}

	private void testRequest(ProfileChangeRequest pcr) {

		assertTrue(pcr.getRequest().getInstall().getList().size() == 1);
		assertTrue(pcr.getRequest().getUpgrade().getList().size() == 2);
		assertTrue(pcr.getRequest().getRemove().toCUDF().startsWith("c"));
	}

	private void testPreamble(ProfileChangeRequest pcr) {
		assertTrue(pcr.getPreamble().getTypes().size() == 13);
		assertTrue(pcr.getPreamble().getTypes().get("tbool") == Type.BOOL);
		assertTrue(pcr.getPreamble().getDefaults().get("tint").equals(0));
	}
}
