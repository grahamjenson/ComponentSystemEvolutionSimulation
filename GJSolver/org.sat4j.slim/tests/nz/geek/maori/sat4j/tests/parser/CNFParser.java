package nz.geek.maori.sat4j.tests.parser;

import java.io.BufferedReader;
import java.io.IOException;

public class CNFParser {

	public static void parse(BufferedReader reader, Visitor[] visitors)
			throws IOException {
		String str = reader.readLine();
		while ((str = reader.readLine()) != null) {
			str = str.trim();
			if ((str.length() == 0) || str.startsWith("c")
					|| str.startsWith("p") || str.startsWith("%")
					|| str.startsWith("0")) {
			} else {
				String[] split = str.split(" ");
				// System.out.println(str);
				int[] v = new int[split.length - 1];
				for (int i = 0; i < v.length; i++) {
					v[i] = Integer.valueOf(split[i]);
				}
				for (Visitor vis : visitors) {
					vis.addClause(v);
				}
			}
		}
		reader.close();
	}

}
