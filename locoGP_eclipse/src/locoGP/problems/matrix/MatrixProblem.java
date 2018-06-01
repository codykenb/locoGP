package locoGP.problems.matrix;

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
import locoGP.problems.tests.AsconEncryptTestCase;
import locoGP.problems.tests.TestCase;
import locoGP.problems.tests.AsconDecryptTestCase;
import locoGP.problems.tests.prefixCodeTestCase;
import locoGP.util.StringFromFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class MatrixProblem extends ProblemFromFile{
	
	public MatrixProblem(){
		super(Arrays.asList("/locoGP/problems/matrix/Matrix.java.txt"));
		setNames("");
		
	}
	
	protected void setCases(){
		testData = new TestCase[]{ 
				//new asconTestCase("A string of text with various characters"),
				new AsconDecryptTestCase("",""), // empty
				
				// message only
				new AsconDecryptTestCase("a",""), // test single iteration
				new AsconDecryptTestCase("abcd",""), // test looping
				new AsconDecryptTestCase("abcdefgha",""), // test looping above 8 chars
				new AsconDecryptTestCase("dfasdfasdfasdfasdfasdfThe quick brownasdfas",""), // above 40 chars
				new AsconDecryptTestCase("asdfasdfasdfThe quick brownaThe quick brownasdfasdfasdfasdfsdfasdfasdfasdfasdfasdfasdf",""), //above 80 chars
				
				// associated data only (results in failed verification when m is empty, whether rightly or wrongly) 
				new AsconDecryptTestCase("", "a"),
				new AsconDecryptTestCase("", "abcd"),
				new AsconDecryptTestCase("", "abcdefgha"),
				new AsconDecryptTestCase("", "ck brownasdfasdfasdfasdfaThe quisdfasdfasdf"), // above 40 chars
				new AsconDecryptTestCase("", "sdfasdfasdfasdfThe quiThe quick brownasdfasdfasdfack brownasdfasdfasdfasdfasdfasdfasdf"), //above 80 chars
				
				// message and associated data
				new AsconDecryptTestCase("a", "a"),
				new AsconDecryptTestCase("abcd", "cdab"),
				new AsconDecryptTestCase("abcdefgha", "efghaabcd"),
				new AsconDecryptTestCase("fasdfasdfasdfasdfThe quick brownasdfasdfasd", "The quick brownasdfasdfasdfasdfasdfasdfasdf"), // above 40 chars
				new AsconDecryptTestCase("he quick broThe quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf", "The quick brownasdfasdfasdfasdfasdfasdfasdfThe quick brownasdfasdfasdfasdfasdfasdfasdf"), //above 80 chars
				new AsconDecryptTestCase("he quick broThe quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdfquick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdfquick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdfquick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdfquick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdfquick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdfquick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf"
						, "The quick brownasdfasdfasdfasdfasdfasdfasdfThe quick brownasdfasdfasdfasdfasdfasdfaquick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"he quick broThe quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdfThe quick brownasdfasdfasdfasdfasdfasdfasdfThe quick brownasdfasdfasdquick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdf" +
						"quick brownasdfasdfasdfasdfasdfasdfasdfTwnasdfasdfasdfasdfasdfasdfasdffasdfasdfasdfadfasdfafdsfsfsfsdfsdf") //above 80 chars
				
				/*
				new AsconDecryptTestCase("The quick brown"),
				new AsconDecryptTestCase(""),
				new AsconDecryptTestCase("0"),
				new AsconDecryptTestCase("01"),
				new AsconDecryptTestCase("001")*//*,
				new AsconDecryptTestCase("asdflkjsdflkjasdfljkeebcekekjceiueouiasknelaohuaybltnzlmaxghde" +
								"219056810287wjhanslkdhfkl23gho9a8yhzxhlfasjkdglkdnlqnw;oafdfncue;mjhnsklfhg" +
								"219056810287wjhanslkdhfkl23gho9a8yhzxhlfasjkdglkdnlqnwdfgh;oafncue;mjhnsklfhg" +
								"219056810287wjhanslkdhfkl23gho9a8yhzxhlfasjkdglkdndfhglqnw;oafncue;mjhnsklfhg" +
								"219056810287wjhanslkdhfkl23gho9a8yhzxhlfasjkdgfdghlkdnlqnw;oafncue;mjhnsklfhg" +
								"219056810287wjhanslkdhfkl23gho9a8yhzxhlfasdfghjkdglkdnlqnw;oafncue;mjhnsklfhg" +
								"219056810287wjhanslkdhfkl23gho9a8yhzxhldfhfasjkdglkdnlqnw;oafncue;mjhnsklfhg" +
								"219056810287wjhanslkdhfkl23gho9a8yhzxhdlfasjkdglkdnlqnw;oafncue;mjhnsklfhg" +
								"219056810287wjhanslkdhfkl23gho9a8sfdydhzxhlfasjkdglkdnlqnw;oafncue;mjhnsklfhg" +
								"219056810287wjhanslkdhfkl23ghasdfo9a8yhzxhlfasjkdglkdnlqnw;oafncue;mjhnsklfhg" +
								"sadjflashtlnyrwmsdhfmawioexfmur489757q2382opqi10`kjmlkmjsufzwo;eijmzsm")*/
								
				/*new AsconDecryptTestCase("asdflkjsd")
				new AsconDecryptTestCase("asdflkjsdflkjasdfljkeebcekekjceiueouiasknelaohuaybltnzlmaxe" +
						"219056810287wjhanslkdhfkl23gho9a8yhzxhlfasjkdglkdnlqnw;oafncue;mjhnsklfhg" +
						"sadjflashtlnyrwmsdhfmawioexfmur489757q2382opqi10`kjmlkmjsufzwo;eijmzsm"),*/
				
			};
	}
	
	
	
	protected void setNames(String name){
		problemName = "iaik.ascon128v11.Ascon128v11"+name; // Should w get this from the file?
		className = "iaik.ascon128v11.Ascon128v11"+name;
		setMethodName();
		setCases();
	}
	
	protected void setMethodName() {
		this.methodName = "crypto_aead_decrypt";
	}

	/*public MatrixProblem (String variantName){
		setNames(variantName);
		problemStrings = loadFiles(variantName);
	}*/
	
	

	
	// static int crypto_aead_decrypt(byte m[], int mlen, byte nsec[], 
	//                                byte c[], int clen, byte ad[],
	//								  int adlen, byte npub[], byte k[]) 
	
	
	@Override
	public Class[] getMethodParameterTypes() {
		// TODO Auto-generated method stub
		return new Class[]{byte[].class, int.class,byte[].class,
		                 byte[].class, int.class, byte[].class,
		                 int.class, byte[].class, byte[].class};
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

	

	

	






}