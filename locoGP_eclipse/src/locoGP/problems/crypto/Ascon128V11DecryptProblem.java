package locoGP.problems.crypto;

import huffmanCodeTable.*;
import iaik.ascon128v11.Ascon128v11;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;

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

public class Ascon128V11DecryptProblem extends Problem{                        
	
	protected String problemName; // Should w get this from the file?
	protected String className;
	protected String methodName;
	    
	protected CompilationSet problemStrings;
	
	protected TestCase[] testData;
	
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
	
	
	public Ascon128V11DecryptProblem(){
		setNames("");
		problemStrings = loadFiles("");
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

	public Ascon128V11DecryptProblem(String variantName){
		setNames(variantName);
		problemStrings = loadFiles(variantName);
	}
	
	protected CompilationSet loadFiles(String name){
		CompilationDetail[] fileSet = new CompilationDetail[1];
		
			String fileContents = StringFromFile.getStringFromFile("/locoGP/problems/crypto/Ascon128v11"+name+".txt");
			fileSet[0]= new CompilationDetail(fileContents);
			
		CompilationSet cS = new CompilationSet(fileSet);
	
		return cS;
	}
	public int getMissingTestCaseValue(int numMissing){
		return numMissing *5;
	}
	
	// static int crypto_aead_decrypt(byte m[], int mlen, byte nsec[], 
	//                                byte c[], int clen, byte ad[],
	//								  int adlen, byte npub[], byte k[]) 
	@Override
	public String getMethodSignature() {
		
		return  "static int crypto_aead_decrypt(byte[] m, int mlen, byte[] nsec,"+
				"byte[] c, int clen, byte[] ad,"+
				"int adlen, byte[] npub, byte[] k)";
				//"public static java.lang.String[] getCodeBook(java.lang.Byte[] bytes)";
	}
	
	@Override
	public Class[] getMethodParameterTypes() {
		// TODO Auto-generated method stub
		return new Class[]{byte[].class, int.class,byte[].class,
		                 byte[].class, int.class, byte[].class,
		                 int.class, byte[].class, byte[].class};
	}
	
/*	protected String getStringFromFile(String fileName){
		StringWriter writer = new StringWriter();
		InputStream iStream = getClass().getResourceAsStream(fileName);
		
		try {
			//IOUtils.copy(iStream, writer, null);
			IOUtils.copy(iStream, writer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return writer.toString();
	}*/

	

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
	public int getSeedFunctionalityScore() {
		/*int seedBest = 0;
		for(int i = 0 ; i< testData.length ; i++)
			seedBest += 1 ;//(((asconTestCase)testData[i]).getFunctionalityScore());
		return seedBest;*/
		
		return 0;  
	}
	
	/*private int getSeedCodeBookLength(){
		int differentSymbols = 0 ;  how many different symbols should there be?
		If there are this many missing, we can reckon the code does nothing.  
		
		for(int i = 0 ; i< testData.length ; i++)
			differentSymbols = ((String[])testData[i].getAnswer()).length;
		return differentSymbols;
	}*/
	
	//private Integer worstFunctionalityScore = null;
	public int getWorstFunctionalityScore(){
		/*if (worstFunctionalityScore == null){
				worstFunctionalityScore =getSeedFunctionalityScore(); 
				//worstFunctionalityScore += getSeedCodeBookLength(); // always empty codebook
		}*/
		return 3*testData.length;//worstFunctionalityScore;
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
	public void setStaticOptimalBias(Individual individual, int optimisationType) {
		// yeuck!
		System.out.println("Ascon setStaticOptimalBias called, exiting");
		System.exit(555);
	}

	@Override
	public ArrayList<String> getClassNames() {
		return problemStrings.getClassNames();
	}






}

/*
package iaik.ascon128v11;

import iaik.ascon128v11.Ascon128v11;

public class Ascon128v11Main {
  public final static int MAXLEN = 65536;

  public static void main(String[] args) {

    int i;
    int MLEN = 1;
    if (args.length == 1)
      MLEN = Integer.decode(args[0]);

    int alen = MAXLEN;
    int mlen = MAXLEN;
    int clen = MAXLEN + Ascon128v11.CRYPTO_ABYTES;
    byte a[] = new byte[alen];
    byte m[] = new byte[mlen];
    byte c[] = new byte[m.length + Ascon128v11.CRYPTO_ABYTES];
    byte nsec[] = new byte[Ascon128v11.CRYPTO_NSECBYTES];
    byte npub[] =
        {(byte) 0x7c, (byte) 0xc2, (byte) 0x54, (byte) 0xf8, (byte) 0x1b, (byte) 0xe8, (byte) 0xe7,
            (byte) 0x8d, (byte) 0x76, (byte) 0x5a, (byte) 0x2e, (byte) 0x63, (byte) 0x33,
            (byte) 0x9f, (byte) 0xc9, (byte) 0x9a};
    byte k[] =
        {0x67, (byte) 0xc6, 0x69, 0x73, 0x51, (byte) 0xff, 0x4a, (byte) 0xec, 0x29, (byte) 0xcd,
            (byte) 0xba, (byte) 0xab, (byte) 0xf2, (byte) 0xfb, (byte) 0xe3, 0x46};

    for (i = 0; i < MLEN; ++i)
      a[i] = (byte) ('A' + i % 26);
    for (i = 0; i < MLEN; ++i)
      m[i] = (byte) ('a' + i % 26);

    for (alen = 0; alen <= MLEN; ++alen)
      for (mlen = 0; mlen <= MLEN; ++mlen) {
        Ascon128v11.print("k", k, Ascon128v11.CRYPTO_KEYBYTES, 0);
        Ascon128v11.print("n", npub, Ascon128v11.CRYPTO_NPUBBYTES, 0);
        Ascon128v11.print("a", a, alen, 0);
        Ascon128v11.print("m", m, mlen, 0);
        clen = Ascon128v11.crypto_aead_encrypt(c, clen, m, mlen, a, alen, nsec, npub, k);
        Ascon128v11.print("c", c, clen - Ascon128v11.CRYPTO_ABYTES, 0);
        Ascon128v11.print("t", c, Ascon128v11.CRYPTO_ABYTES, clen - Ascon128v11.CRYPTO_ABYTES);
        mlen = Ascon128v11.crypto_aead_decrypt(m, mlen, nsec, c, clen, a, alen, npub, k);
        if (mlen != -1) {
          Ascon128v11.print("p", m, mlen, 0);
        } else
          System.out.printf("verification failed\n");
        System.out.printf("\n");
      }
    return;
  }

}
*/