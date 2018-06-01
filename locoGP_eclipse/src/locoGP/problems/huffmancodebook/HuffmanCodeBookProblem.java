package locoGP.problems.huffmancodebook;

import huffmanCodeTable.*;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;

import locoGP.individual.Individual;
import locoGP.problems.CompilationDetail;
import locoGP.problems.CompilationSet;
import locoGP.problems.Problem;
import locoGP.problems.tests.TestCase;
import locoGP.problems.tests.prefixCodeTestCase;
import locoGP.util.StringFromFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class HuffmanCodeBookProblem extends Problem{                        
	
	protected String problemName = "huffmanCodeTable.BasicHuffman"; // Should w get this from the file?
	protected String className = "huffmanCodeTable.BasicHuffman";
	private String methodName = "getCodeBook";
	
	protected CompilationSet problemStrings = loadFiles("");
	

	
	private TestCase[] testData = { 
			createCase("A string of text with various characters"),
			createCase("The quick brown fox jumps over the lazy dog"),
			createCase("His face was the true index of his mind."),
			createCase("asdflkjsdflkjasdfljkeebcekekjceiueouiasknelaohuaybltnzlmaxe" +
					"219056810287wjhanslkdhfkl23gho9a8yhzxhlfasjkdglkdnlqnw;oafncue;mjhnsklfhg" +
					"sadjflashtlnyrwmsdhfmawioexfmur489757q2382opqi10`kjmlkmjsufzwo;eijmzsm"),
			createCase("z y x w v u t s r q p o n m l k j i h g f e d c b a ` _ ^ ]  [ Z Y X W V" +
					" U T S R Q P O N M L K J I H G F E D C B A")
		};
	
	protected CompilationSet loadFiles(String variantNum){
		return new CompilationSet(Arrays.asList(
				"/locoGP/problems/huffmancodebook/BasicHuffman"+variantNum+".txt",
				"/locoGP/problems/huffmancodebook/BubbleSort"+variantNum+".txt",
				"/locoGP/problems/huffmancodebook/huffmanNode"+variantNum+".txt"));
	}
	
	private TestCase createCase(String string) {
		Byte[] testInput = ArrayUtils.toObject(string.getBytes());
		String[] sampleResult = BasicHuffman.getCodeBook(testInput);
		/*	getCodeBook sorts the array in place :/ */
		Byte[] testInputOrig = ArrayUtils.toObject(string.getBytes());
		return new prefixCodeTestCase(new Object[]{testInputOrig}, sampleResult);
	}

	@Override
	public String getEntryClassName() {
		return className;
	}
	
	public int getMissingTestCaseValue(int numMissing){
		int missingError=0;
		for(int i = testData.length-1 ; i>numMissing ; i--)
			missingError += ((String[])testData[i].getAnswer()).length;
		return missingError;
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
	public int getSeedFunctionalityScore() {
		int seedBest = 0;
		for(int i = 0 ; i< testData.length ; i++)
			seedBest += (((prefixCodeTestCase)testData[i]).getFunctionalityScore());
		return seedBest;
	}
	
	private int getSeedCodeBookLength(){
		int differentSymbols = 0 ; /* how many different symbols should there be?
		If there are this many missing, we can reckon the code does nothing.  
		*/
		for(int i = 0 ; i< testData.length ; i++)
			differentSymbols = ((String[])testData[i].getAnswer()).length;
		return differentSymbols;
	}
	
	private Integer worstFunctionalityScore = null;
	public int getWorstFunctionalityScore(){
		if (worstFunctionalityScore == null){
				worstFunctionalityScore =getSeedFunctionalityScore(); 
				worstFunctionalityScore += getSeedCodeBookLength(); // always empty codebook
		}
		return worstFunctionalityScore;
	}

	/*@Override
	int sampleCall(Object[] args) {
		sampleCall((Byte[])args);
		return 0;
	}*/
	
	/*public int sampleCall(byte[] args) {
		BasicHuffman.getCodeBook(args);
		return 0;
	}*/

	@Override
	public void setProblemName(String problemName) {
		this.problemName = problemName;
	}

	@Override
	public String getProblemName() {
		return problemName;
	}

	@Override
	public Class[] getMethodParameterTypes() {
		return new Class[] {Byte[].class};
	}

	@Override
	public void setStaticOptimalBias(Individual individual, int optimisationType) {
		
	}

	@Override
	public String getMethodSignature() {
		return "public static java.lang.String[] getCodeBook(java.lang.Byte[] bytes)";
	}

	@Override
	public ArrayList<String> getClassNames() {
		return problemStrings.getClassNames();
	}



	



}
