package nz.geek.maori.cudftests;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import nz.geek.maori.cudf.parser.Parser;

public class ParserTimerTest extends TestCase {

	public void testTime() throws IOException {
		System.out.println();
		File f = new File("./testData/rand35afbf.cudf");

		long t1 = System.currentTimeMillis();
		Parser.parse(f);
		long t2 = System.currentTimeMillis();
		System.out.println(t2 - t1);
	}

}
