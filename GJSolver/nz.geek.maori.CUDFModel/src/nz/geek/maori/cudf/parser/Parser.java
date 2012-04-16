/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM Corporation - initial implementation and ideas 
 ******************************************************************************/
package nz.geek.maori.cudf.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import nz.geek.maori.cudf.CUDFFactory;
import nz.geek.maori.cudf.Keep;
import nz.geek.maori.cudf.Package;
import nz.geek.maori.cudf.PackageFormula;
import nz.geek.maori.cudf.PackageList;
import nz.geek.maori.cudf.PackageVersionConstraint;
import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.Relation;
import nz.geek.maori.cudf.Type;

public class Parser {

	private Package currentPackage = null;
	private ProfileChangeRequest query = CUDFFactory.eINSTANCE
			.createProfileChangeRequest();

	private CUDFFactory cudf = CUDFFactory.eINSTANCE;

	private Parser() {

	}

	public static ProfileChangeRequest parse(File file) {
		try {
			Parser p = new Parser();
			p.parse(new FileInputStream(file));
			return p.query;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	private boolean handleExtraProperties = true;

	private void parse(InputStream stream) {
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			String next = reader.readLine();
			while (true) {

				// look-ahead to check for line continuation
				String line = next;
				for (next = reader.readLine(); (next != null)
						&& (next.length() > 1) && (next.charAt(0) == ' '); next = reader
						.readLine()) {
					line = line + next.substring(1);
				}

				// terminating condition of the loop... reached the end of the
				// file
				if (line == null) {
					validateAndAddPackage();
					break;
				}
				line = line.toLowerCase();
				// end of stanza
				if (line.trim().length() == 0) {
					validateAndAddPackage();
					continue;
				}

				// process line
				if (line.startsWith("#")) {
					// ignore
				} else if (line.startsWith("preamble: ")) {
					this.query.setPreamble(CUDFFactory.eINSTANCE
							.createPreamble());
				} else if (this.handleExtraProperties
						&& line.startsWith("property: ")) {
					handleTypeDeclaration(line);
				} else if (line.startsWith("univ-checksum: ")) {
					this.query.getPreamble().setUchecksum(
							line.substring("univ-checksum: ".length()));
				} else if (line.startsWith("status-checksum: ")) {
					this.query.getPreamble().setSchecksum(
							line.substring("status-checksum: ".length()));
				} else if (line.startsWith("req-checksum: ")) {
					this.query.getPreamble().setRchecksum(
							line.substring("req-checksum: ".length()));
				}

				else if (line.startsWith("package: ")) {
					handlePackage(line);
				} else if (line.startsWith("version: ")) {
					handleVersion(line);
				} else if (line.startsWith("installed: ")) {
					handleInstalled(line);
				} else if (line.startsWith("was-installed: ")) {
					handleWasInstalled(line);
				} else if (line.startsWith("depends: ")) {
					handleDepends(line);
				} else if (line.startsWith("conflicts: ")) {
					handleConflicts(line);
				} else if (line.startsWith("provides: ")) {
					handleProvides(line);
				} else if (line.startsWith("keep: ")) {
					handleKeep(line);
				} else if (line.startsWith("request:")) {

					handleRequest(line);
				} else if (line.startsWith("install: ")) {
					handleInstall(line);
				} else if (line.startsWith("upgrade: ")) {
					handleUpgrade(line);
				} else if (line.startsWith("remove: ")) {
					handleRemove(line);
				} else if (this.handleExtraProperties) {
					// Handle Properties
					int i = line.indexOf(":");
					String id = line.substring(0, i);
					String val = line.substring(i + 2); // minus the ": "
					Type t = null;
					if (this.query.getPreamble() != null) {
						t = this.query.getPreamble().getTypes().get(id);
					}

					if (t == null) {
						// Logger.getLogger(this.getClass().toString()).log(Level.WARNING,
						// "Type " + id + " is used but not defined");
					}
					Object processType = processType(t, val);
					Map<String, Object> properties = this.currentPackage.getProperties();
					properties.put(id, processType);

				}

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	private void handleWasInstalled(String line) {
		String value = line.substring("was-installed: ".length());
		if (value.length() != 0) {
			boolean bool = Boolean.valueOf(value);
			this.currentPackage.setWasInstalled(bool);
		}

	}

	private void handleTypeDeclaration(String typeDecl) {
		typeDecl = typeDecl.substring("property: ".length());
		ArrayList<String> typeList = new ArrayList<String>();
		String type = "";
		boolean bracket = false;
		boolean quote = false;
		boolean negate = false;
		for (char c : typeDecl.toCharArray()) {
			if ((c == '"') && !negate) {
				quote = !quote;
			} else if ((c == '\\') && quote) {
				negate = true;
				continue;
			} else if ((c == '[') && !quote) {
				if (bracket) {
					throw new RuntimeException(
							"The declaration of types is incorrect");
				}
				bracket = true;
			} else if ((c == ']') && !quote) {
				if (!bracket) {
					throw new RuntimeException(
							"The declaration of types is incorrect");
				}
				bracket = false;
			}

			if ((c == ',') && !quote && !bracket) {
				typeList.add(type);
				type = "";

			} else {
				type += c;
			}
			if (negate) {
				negate = false;
			}
		}
		if (negate || quote || bracket) {
			throw new RuntimeException("The declaration of types is incorrect");
		}
		typeList.add(type);
		Map<String, Type> types = this.query.getPreamble().getTypes();
		Map<String, Object> defaults = this.query.getPreamble().getDefaults();

		for (String next : typeList) {
			// Look ahead to the next element has ":" in it
			int idIndex = next.indexOf(":");

			String id = next.substring(0, idIndex).trim();
			String definition = next.substring(idIndex + 1).trim();
			;
			Type t = getType(definition);
			types.put(id, t);

			int defaultIndex = definition.indexOf("=");
			if (defaultIndex != -1) {
				// Default value minus the equals sing
				String defaultValue = definition.substring(defaultIndex + 1)
						.trim();
				// Remove the begining and end charaters as they are the
				// brackets
				defaultValue = defaultValue.substring(1,
						defaultValue.length() - 1);
				defaults.put(id, processType(t, defaultValue));
			}

		}
	}

	private Object processType(Type t, String def) {
		if (t == null) {
			return def;
		} else if (t.equals(Type.STRING)) {
			return def;
		} else if (t.equals(Type.ENUM)) {
			return def;
		} else if (t.equals(Type.INT)) {
			return Double.valueOf(def);
		} else if (t.equals(Type.BOOL)) {
			return Boolean.valueOf(def);
		} else if (t.equals(Type.PACKAGE)) {
			return createPackageVersionConstraint(def);
		} else if (t.equals(Type.PACKAGEFORMULA)) {
			return createPackageFormula(def);
		} else if (t.equals(Type.PACKAGELIST)) {
			return createPackageList(new StringTokenizer(def, ","));
		}

		return null;
	}

	private Type getType(String def) {
		if (def.startsWith("bool")) {
			return Type.BOOL;
		} else if (def.startsWith("int")) {
			return Type.INT;
		} else if (def.startsWith("nat")) {
			return Type.INT;
		} else if (def.startsWith("posint")) {
			return Type.INT;
		} else if (def.startsWith("string")) {
			return Type.STRING;
		} else if (def.startsWith("pkgname")) {
			return Type.PACKAGE;
		} else if (def.startsWith("ident")) {
			return Type.PACKAGE;
		} else if (def.startsWith("vpkgformula")) {
			return Type.PACKAGEFORMULA;
		} else if (def.startsWith("vpkglist")) {
			return Type.PACKAGELIST;
		} else if (def.startsWith("veqpkglist")) {
			return Type.PACKAGELIST;
		}
		// Have to have these down here because it could start with this and
		// another ; e.g. vpkglist
		else if (def.startsWith("vpkg")) {
			return Type.PACKAGE;
		} else if (def.startsWith("veqpkg")) {
			return Type.PACKAGE;
		} else if (def.startsWith("enum")) {
			return Type.ENUM;
		} else {
			throw new UnsupportedOperationException("Unknown Type");
		}
	}

	private void handleKeep(String line) {
		line = line.substring("keep: ".length());
		if (line.contains("version")) {
			this.currentPackage.setKeep(Keep.VERSION);
			return;
		}
		if (line.contains("package")) {
			this.currentPackage.setKeep(Keep.PACKAGE);
			return;
		}
		if (line.contains("none")) {
			return;
		}
		if (line.contains("feature")) {
			this.currentPackage.setKeep(Keep.FEATURE);
		}

	}

	/*
	 * Ensure that the current IU that we have been building is validate and if
	 * so, then add it to our collected list of all converted IUs from the file.
	 */
	private void validateAndAddPackage() {
		if (this.currentPackage == null) {
			return;
		}
		// For a package stanza, the id and version are the only mandatory
		// elements
		if (this.currentPackage.getName() == null) {
			throw new IllegalStateException(
					"Malformed \'package\' stanza. No package element found.");
		}
		if (this.currentPackage.getVersion() == -1) {
			throw new IllegalStateException(
					"Malformed \'package\' stanza. Package "
							+ this.currentPackage.getName()
							+ " does not have a version.");
		}
		// If it is installed add it to the begining of the list
		this.query.addPackage(this.currentPackage);
		// reset to be ready for the next stanza
		this.currentPackage = null;
	}

	private void handleInstalled(String line) {
		String value = line.substring("installed: ".length());
		if (value.length() != 0) {
			boolean bool = Boolean.valueOf(value);
			this.currentPackage.setInstalled(bool);
		}
	}

	private void handleInstall(String line) {
		line = line.substring("install: ".length());
		StringTokenizer tokenizer = new StringTokenizer(line, ",");
		this.query.getRequest().setInstall(createPackageList(tokenizer));
	}

	private void handleRequest(String line) {
		this.query.setRequest(this.cudf.createRequest());
	}

	private void handleRemove(String line) {
		line = line.substring("remove: ".length());
		StringTokenizer tokenizer = new StringTokenizer(line, ",");
		this.query.getRequest().setRemove(createPackageList(tokenizer));
	}

	private void handleUpgrade(String line) {
		line = line.substring("upgrade: ".length());
		if(line.startsWith("*"))
		{
			//This is an addition meaning upgrade everything
			//By this time all packages have been added
			PackageList packageList = this.cudf.createPackageList();
			for(Package p : this.query.getInstalledPackages())
			{
				PackageVersionConstraint pvc = this.cudf.createPackageVersionConstraint();
				pvc.setPackage(p.getName());
				packageList.getList().add(pvc);
			}
			this.query.getRequest().setUpgrade(packageList);
			
		}
		else
		{
			StringTokenizer tokenizer = new StringTokenizer(line, ",");
			this.query.getRequest().setUpgrade(createPackageList(tokenizer));
		}
	}

	/*
	 * Convert the version string to a version object and set it on the IU
	 */
	private void handleVersion(String line) {
		this.currentPackage.setVersion(Integer.valueOf(line.substring(
				"version: ".length()).trim()));
	}

	private void handleDepends(String line) {
		line = line.substring("depends: ".length());
		this.currentPackage.setDepends(createPackageFormula(line));
	}

	private void handleConflicts(String line) {
		line = line.substring("conflicts: ".length());
		StringTokenizer tokenizer = new StringTokenizer(line, ",");
		this.currentPackage.setConflicts(createPackageList(tokenizer));
	}

	private PackageFormula createPackageFormula(String line) {
		PackageFormula packageFormula = CUDFFactory.eINSTANCE
				.createPackageFormula();

		StringTokenizer s = new StringTokenizer(line, ",");
		String subtoken;
		while (s.hasMoreElements()) {
			subtoken = s.nextToken();
			if ("true!".equals(subtoken)) {
				return new PackageFormula.TRUE();

			}
			if ("false!".equals(subtoken)) {
				return new PackageFormula.FALSE();

			}
			StringTokenizer subTokenizer = new StringTokenizer(subtoken, "|");
			PackageList o = createPackageList(subTokenizer);
			packageFormula.getAnd().add(o);
		}

		return packageFormula;
	}

	private PackageList createPackageList(StringTokenizer tokenizer) {
		// >, >=, =, <, <=, !=
		PackageList packageList = this.cudf.createPackageList();
		while (tokenizer.hasMoreElements()) {
			String nextToken = tokenizer.nextToken().trim();

			PackageVersionConstraint pvc = createPackageVersionConstraint(nextToken);
			packageList.getList().add(pvc);
		}

		return packageList;
	}

	private PackageVersionConstraint createPackageVersionConstraint(String def) {
		StringTokenizer expressionTokens = new StringTokenizer(def, ">=!<",
				true);
		int tokenCount = expressionTokens.countTokens();
		String id = expressionTokens.nextToken().trim();
		PackageVersionConstraint pvc = this.cudf
				.createPackageVersionConstraint();
		pvc.setPackage(id);

		if (tokenCount == 1)// a
		{

		} else if (tokenCount == 3) // a > 2, a < 2, a = 2
		{
			String signFirstChar = expressionTokens.nextToken();
			if (signFirstChar.equals("=")) {
				pvc.setRel(Relation.EQUALS);
			} else if (signFirstChar.equals("<")) {
				pvc.setRel(Relation.L);
			} else if (signFirstChar.equals(">")) {
				pvc.setRel(Relation.G);
			}

			String version = expressionTokens.nextToken().trim();
			pvc.setVersion(Integer.valueOf(version));
		} else if (tokenCount == 4) // a >= 2, a <=2, a != 2
		{

			String signFirstChar = expressionTokens.nextToken();
			expressionTokens.nextToken();// skip second char of the sign

			if (signFirstChar.equals("!")) {
				pvc.setRel(Relation.NEQ);
			} else if (signFirstChar.equals("<")) {
				pvc.setRel(Relation.LEQ);
			} else if (signFirstChar.equals(">")) {
				pvc.setRel(Relation.GEQ);
			}

			String version = expressionTokens.nextToken().trim();
			pvc.setVersion(Integer.valueOf(version));

		}
		return pvc;
	}

	// package name matches: "^[a-zA-Z0-9+./@()%-]+$"
	private void handlePackage(String readLine) {
		this.currentPackage = this.cudf.createPackage();
		this.currentPackage.setName(readLine.substring("package: ".length())
				.trim());
	}

	private void handleProvides(String line) {
		line = line.substring("provides: ".length());
		StringTokenizer tokenizer = new StringTokenizer(line, ",");
		this.currentPackage.setProvides(createPackageList(tokenizer));
	}

}
