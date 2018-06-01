package locoGP.problems.crypto;

import java.util.Random;

import locoGP.problems.tests.AsconEncryptTestCase;
import locoGP.problems.tests.TestCase;


public class Ascon128V11EncryptProblem extends Ascon128V11DecryptProblem{
	
	/*protected void setCases(){
		testData = new TestCase[]{ 
				//new asconTestCase("A string of text with various characters"),
				new AsconEncryptTestCase("",""), // empty
				
				// message only
				new AsconEncryptTestCase("a",""), // test single iteration
				new AsconEncryptTestCase("abcd",""), // test looping
				new AsconEncryptTestCase("abcdefgha",""), // test looping above 8 chars
				new AsconEncryptTestCase("The quick brownasdfasdfasdfasdfasdfasdfasdf",""), // above 40 chars
				new AsconEncryptTestCase("The quick brownasdfasdfasdfasdfasdfasdfasdfThe quick brownasdfasdfasdfasdfasdfasdfasdf",""), //above 80 chars
				
				// associated data only
				new AsconEncryptTestCase("", "a"),
				new AsconEncryptTestCase("", "abcd"),
				new AsconEncryptTestCase("", "abcdefgha"),
				new AsconEncryptTestCase("", "The quick brownasdfasdfasdfasdfasdfasdfasdf"), // above 40 chars
				new AsconEncryptTestCase("", "The quick brownasdfasdfasdfasdfasdfasdfasdfThe quick brownasdfasdfasdfasdfasdfasdfasdf"), //above 80 chars
				
				// message and associated data
				new AsconEncryptTestCase("a", "a"),
				new AsconEncryptTestCase("abcd", "abcd"),
				new AsconEncryptTestCase("abcdefgha", "abcdefgha"),
				new AsconEncryptTestCase("The quick brownasdfasdfasdfasdfasdfasdfasdf", "The quick brownasdfasdfasdfasdfasdfasdfasdf"), // above 40 chars
				new AsconEncryptTestCase("The quick brownasdfasdfasdfasdfasdfasdfasdfThe quick brownasdfasdfasdfasdfasdfasdfasdf", "The quick brownasdfasdfasdfasdfasdfasdfasdfThe quick brownasdfasdfasdfasdfasdfasdfasdf") //above 80 chars
				
				
				new AsconEncryptTestCase("The quick brown"),
				new AsconEncryptTestCase(""),
				new AsconEncryptTestCase("0"),
				new AsconEncryptTestCase("01"),
				new AsconEncryptTestCase("001"),
				new AsconEncryptTestCase("asdflkjsdflkjasdfljkeebcekekjceiueouiasknelaohuaybltnzlmaxghde" +
								"219056810287wjhanslkdhfkl23gho9a8yhzxhlfasjkdglkdnlqnw;oafdfncue;mjhnsklfhg" +
								"219056810287wjhanslkdhfkl23gho9a8yhzxhlfasjkdglkdnlqnwdfgh;oafncue;mjhnsklfhg" +
								"219056810287wjhanslkdhfkl23gho9a8yhzxhlfasjkdglkdndfhglqnw;oafncue;mjhnsklfhg" +
								"219056810287wjhanslkdhfkl23gho9a8yhzxhlfasjkdgfdghlkdnlqnw;oafncue;mjhnsklfhg" +
								"219056810287wjhanslkdhfkl23gho9a8yhzxhlfasdfghjkdglkdnlqnw;oafncue;mjhnsklfhg" +
								"219056810287wjhanslkdhfkl23gho9a8yhzxhldfhfasjkdglkdnlqnw;oafncue;mjhnsklfhg" +
								"219056810287wjhanslkdhfkl23gho9a8yhzxhdlfasjkdglkdnlqnw;oafncue;mjhnsklfhg" +
								"219056810287wjhanslkdhfkl23gho9a8sfdydhzxhlfasjkdglkdnlqnw;oafncue;mjhnsklfhg" +
								"219056810287wjhanslkdhfkl23ghasdfo9a8yhzxhlfasjkdglkdnlqnw;oafncue;mjhnsklfhg" +
								"sadjflashtlnyrwmsdhfmawioexfmur489757q2382opqi10`kjmlkmjsufzwo;eijmzsm")
								
				new AsconEncryptTestCase("asdflkjsd")
				new AsconEncryptTestCase("asdflkjsdflkjasdfljkeebcekekjceiueouiasknelaohuaybltnzlmaxe" +
						"219056810287wjhanslkdhfkl23gho9a8yhzxhlfasjkdglkdnlqnw;oafncue;mjhnsklfhg" +
						"sadjflashtlnyrwmsdhfmawioexfmur489757q2382opqi10`kjmlkmjsufzwo;eijmzsm"),
				
			};
	}*/
	
	@Override
	public int getSeedFunctionalityScore() {
		/*int seedBest = 0;
		for(int i = 0 ; i< testData.length ; i++)
			seedBest += 1 ;//(((asconTestCase)testData[i]).getFunctionalityScore());
		return seedBest;*/
		
		return 0;  
	}
	public Ascon128V11EncryptProblem(){
		setNames("");
		problemStrings = loadFiles("");
	}
	
	protected void setMethodName() {
		super.methodName = "crypto_aead_encrypt";
	}
	
	public Ascon128V11EncryptProblem(String variantName){
		setNames(variantName);
		problemStrings = loadFiles(variantName);
	}
	
	@Override
	public String getMethodSignature() {
		
		return  "static int crypto_aead_encrypt(byte[] c, int clen, "+
				"byte[] m, int mlen, " +
				"byte[] ad, int adlen, byte[] nsec, "+
				"byte[] npub, byte[] k)";
	}

	public void setTestCaseLength(int mLen, int aLen) {
		String mInput ="",aInput=""; 
		Random r = new Random();
		
		for (int i = 0; i < mLen; ++i)
		      mInput += (char)(r.nextInt(26) + 'a');
		for (int i = 0; i < aLen; ++i)
		      aInput += (char)(r.nextInt(26) + 'a');
		super.testData = new TestCase[]{
				new AsconEncryptTestCase(mInput,aInput)
		};
	}
	
}
