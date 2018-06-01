package locoGP.problems.matrix;
import java.util.ArrayList;
import java.util.List;

import locoGP.problems.CompilationSet;
import locoGP.problems.Problem;
import locoGP.problems.tests.TestCase;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;

import locoGP.individual.Individual;
import locoGP.problems.CompilationDetail;
/*import locoGP.problems.CompilationSet;
import locoGP.problems.Problem;
import locoGP.problems.tests.AsconEncryptTestCase;
import locoGP.problems.tests.TestCase;
import locoGP.problems.tests.AsconDecryptTestCase;
import locoGP.problems.tests.prefixCodeTestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;*/

import locoGP.util.StringFromFile;

public class ProblemFromFile extends Problem{

	protected String problemName; // get this from the file?
	protected String className;
	protected String methodName;
	protected CompilationSet problemStrings;
	protected TestCase[] testData;
	
	ProblemFromFile(List<String> fileNames){ // what about multiple files?
		problemStrings = new CompilationSet(fileNames);
	}
	
	@Override
	public ArrayList<String> getClassNames() {
		return problemStrings.getClassNames();
	}
	
	@Override
	public void setProblemName(String problemName) {
		this.problemName = problemName;
	}

	@Override
	public String getProblemName() {
		return problemName;
	}
	
	@Override
	public int getSeedFunctionalityScore() {
		/*int seedBest = 0;
		for(int i = 0 ; i< testData.length ; i++)
			seedBest += 1 ;//(((asconTestCase)testData[i]).getFunctionalityScore());
		return seedBest;*/
		
		return 0;  
	}
	
	@Override
	public String getEntryClassName() {
		return className;
	}

	@Override
	public String getMethodName() {
		return methodName;
	}

	@Override
	public CompilationSet getStrings() {
		return problemStrings;
	}

	@Override
	public TestCase[] getTestData() {
		return testData;
	}

	@Override
	public int getNumTests() {
		return testData.length;
	}
	
	@Override
	public Class[] getMethodParameterTypes() {
		// TODO Auto-generated method stub
		return new Class[]{byte[].class, int.class,byte[].class,
		                 byte[].class, int.class, byte[].class,
		                 int.class, byte[].class, byte[].class};
	}
	
	@Override
	public void setStaticOptimalBias(Individual individual, int optimisationType) {
		// yeuck! this is problem specific
		System.out.println("setStaticOptimalBias called, exiting");
		System.exit(555);
	}
	
	public int getMissingTestCaseValue(int numMissing){
		return numMissing *5;
	}
	
	public int getWorstFunctionalityScore(){
		/*if (worstFunctionalityScore == null){
				worstFunctionalityScore =getSeedFunctionalityScore(); 
				//worstFunctionalityScore += getSeedCodeBookLength(); // always empty codebook
		}*/
		return 3*testData.length;//worstFunctionalityScore;
	}
	
	@Override
	public String getMethodSignature() {
		
		return  "static int crypto_aead_decrypt(byte[] m, int mlen, byte[] nsec,"+
				"byte[] c, int clen, byte[] ad,"+
				"int adlen, byte[] npub, byte[] k)";
				//"public static java.lang.String[] getCodeBook(java.lang.Byte[] bytes)";
	}
	
}
