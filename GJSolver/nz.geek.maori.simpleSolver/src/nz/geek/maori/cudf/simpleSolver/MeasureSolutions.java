package nz.geek.maori.cudf.simpleSolver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import nz.geek.maori.cudf.ProfileChangeRequest;
import nz.geek.maori.cudf.parser.Parser;
import nz.geek.maori.cudf.satutils.criteria.OptimisationFunctions;

public class MeasureSolutions {

	static String paranoid = "-removed,-changed";
	static String trendy = "-removed,-notuptodate,-unsat_recommends,-new";

	public static void main(String[] args) throws IOException
	{

		System.out.println("Press a key to begin");
		System.in.read();
		String[] cudfs = new String[]
		                            {
				//cudf set
				"small3.cudf",
				"small2.cudf",
				"small1.cudf",
				//"large3.cudf",
				//"large2.cudf",
				//"large1.cudf",
		                            };
		String[] easy = new String[]
		                           {
				//Easy
				"randf7a7e4.cudf",
				"randeb343c.cudf",
				"randea6106.cudf",
				"randc192b7.cudf",
				"randbe9acf.cudf",};

		String[] diff = new String[]
		                           {
				//difficult
				//"randf61f65.cudf",
				//"randf406d5.cudf",
				"randf17677.cudf",
				"rande4fcd8.cudf",
				"randd8bf6a.cudf",
				"randc10393.cudf",
				"randa870aa.cudf",};
		String[] impos = new String[]
		                            {		
				//impossible
				"randfa4522.cudf",
				"randec88d5.cudf",
				"randaafcce.cudf",
				"randa223c3.cudf",
				"rand8cc693.cudf",
				"rand878003.cudf",};
		String[] selss = new String[]
		                            {	
				//sarge-etch-lenny-squeeze-sid
				"rand986.sarge-etch-lenny-squeeze-sid.cudf",
				"rand915.sarge-etch-lenny-squeeze-sid.cudf",
				"rand815.sarge-etch-lenny-squeeze-sid.cudf",
				"rand753.sarge-etch-lenny-squeeze-sid.cudf",
				"rand550.sarge-etch-lenny-squeeze-sid.cudf",};

		String[] sels = new String[]
		                           {	
				//sarge-etch-lenny-squeeze
				"rand986.sarge-etch-lenny-squeeze.cudf",
				"rand915.sarge-etch-lenny-squeeze.cudf",
				"rand815.sarge-etch-lenny-squeeze.cudf",
				"rand753.sarge-etch-lenny-squeeze.cudf",

		                           };

		//cudfs = new String[] {"rand550.sarge-etch-lenny-squeeze-sid.cudf",};

		FileWriter fw = new FileWriter(new File("out"));
		long st =0;
		double mean = 0;
		fw.write("Paranoid Track\n");


//		fw.write("CUDFS \n");		
//		st = System.currentTimeMillis();
//		runTests("-uptodatedistance.-removed", diff, fw);
//		fw.write("Mean time " + mean + "\n"+ "\n");

//		fw.write("EASY \n");		
//		st = System.currentTimeMillis();
//		runTests(paranoid, easy, fw);
//		mean = (System.currentTimeMillis() - st)/easy.length;
//		fw.write("Mean time " + mean + "\n"+ "\n");

		fw.write("Difficult \n");		
		st = System.currentTimeMillis();
		runTests(paranoid, diff, fw);
		mean = (System.currentTimeMillis() - st)/diff.length;
		fw.write("Mean time " + mean + "\n"+ "\n");

		fw.write("Impossible \n");		
		st = System.currentTimeMillis();
		runTests(paranoid, impos, fw);
		mean = (System.currentTimeMillis() - st)/impos.length;
		fw.write("Mean time " + mean + "\n"+ "\n");

		fw.write("sarge-etch-lenny-squeeze-sid \n");		
		st = System.currentTimeMillis();
		runTests(paranoid, selss, fw);
		mean = (System.currentTimeMillis() - st)/selss.length;
		fw.write("Mean time " + mean + "\n"+ "\n");


		fw.write("sarge-etch-lenny-squeeze \n");		
		st = System.currentTimeMillis();
		runTests(paranoid, sels, fw);
		mean = (System.currentTimeMillis() - st)/sels.length;
		fw.write("Mean time " + mean + "\n"+ "\n");

		fw.write("\n\n\nTrendy Track\n");

//		fw.write("CUDFS \n");		
//		st = System.currentTimeMillis();
//		runTests(trendy, cudfs, fw);
//		mean = (System.currentTimeMillis() - st)/cudfs.length;
//		fw.write("Mean time " + mean + "\n"+ "\n");
//
//		fw.write("EASY \n");		
//		st = System.currentTimeMillis();
//		runTests(trendy, easy, fw);
//		mean = (System.currentTimeMillis() - st)/easy.length;
//		fw.write("Mean time " + mean + "\n"+ "\n");

		fw.write("Difficult \n");		
		st = System.currentTimeMillis();
		runTests(trendy, diff, fw);
		mean = (System.currentTimeMillis() - st)/diff.length;
		fw.write("Mean time " + mean + "\n"+ "\n");

		fw.write("Impossible \n");		
		st = System.currentTimeMillis();
		runTests(trendy, impos, fw);
		mean = (System.currentTimeMillis() - st)/impos.length;
		fw.write("Mean time " + mean + "\n"+ "\n");

		fw.write("sarge-etch-lenny-squeeze-sid \n");		
		st = System.currentTimeMillis();
		runTests(trendy, selss, fw);
		mean = (System.currentTimeMillis() - st)/selss.length;
		fw.write("Mean time " + mean + "\n"+ "\n");


		fw.write("sarge-etch-lenny-squeeze \n");		
		st = System.currentTimeMillis();
		runTests(trendy, sels, fw);
		mean = (System.currentTimeMillis() - st)/sels.length;
		fw.write("Mean time " + mean + "\n"+ "\n");



		fw.close();


	}

	private static void runTests(String criteria, String[] cudfs, FileWriter fw)
	throws IOException {

		for(String cudf : cudfs)
		{
			System.out.println(cudf);
			long time = -1;
			fw.write(cudf + " : \t\t\t");
			fw.flush();
			try {
				long t1 = System.currentTimeMillis();
				SimpleSolver.main(new String[] {"input/" + cudf,"output/"+ cudf,criteria,"60000"});
				time = (System.currentTimeMillis() - t1);


				ProfileChangeRequest inpcr = Parser.parse(new File("input/" + cudf));
				ProfileChangeRequest outpcr = Parser.parse(new File("output/"+ cudf));
				if(criteria == paranoid)
				{
					fw.write(OptimisationFunctions.removed(inpcr, outpcr) + " , " + OptimisationFunctions.changedPacakges(inpcr, outpcr) );
				}
				else if(criteria == trendy)
				{
					fw.write(OptimisationFunctions.removed(inpcr, outpcr) + " , " + OptimisationFunctions.notUpToDatePacakges(inpcr, outpcr) + " , " + OptimisationFunctions.unsatisfiedReccommends(inpcr, outpcr)+ " , " + OptimisationFunctions.newPacakges(inpcr, outpcr));
				}

			} catch (Exception e) {
				e.printStackTrace();
				fw.write("FAIL");
				System.out.println("FAIL");
			}
			fw.write("\t : in " + time +  " ms\n");
			fw.flush();
		}
	}
}
