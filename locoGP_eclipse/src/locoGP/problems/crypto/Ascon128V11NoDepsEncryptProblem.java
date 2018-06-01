package locoGP.problems.crypto;

import java.util.Random;

import locoGP.problems.CompilationDetail;
import locoGP.problems.CompilationSet;
import locoGP.problems.tests.AsconEncryptTestCase;
import locoGP.problems.tests.TestCase;
import locoGP.util.StringFromFile;


public class Ascon128V11NoDepsEncryptProblem extends Ascon128V11DecryptProblem{
	
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
	
	/*@Override
	public int getSeedFunctionalityScore() {
		int seedBest = 0;
		for(int i = 0 ; i< testData.length ; i++)
			seedBest += 1 ;//(((asconTestCase)testData[i]).getFunctionalityScore());
		return seedBest;
		
		return 0;  
	}*/
	/**
	 * (This doesn't work!) Attempt to include all code that Ascon is dependent
	 * on. Major problems. When cloning the AST's and updating class names
	 * having difficulty updating static method call names for some reason the
	 * simpleNames have null parents
	 * 
	 * List of problems: Bits Buffer BufferOverflowException
	 * BufferUnderflowException ByteBuffer ByteBufferAsCharBufferB
	 * ByteBufferAsCharBufferL ByteBufferAsCharBufferRB ByteBufferAsCharBufferRL
	 * ByteBufferAsDoubleBufferB ByteBufferAsDoubleBufferL
	 * ByteBufferAsDoubleBufferRB ByteBufferAsDoubleBufferRL
	 * ByteBufferAsFloatBufferB ByteBufferAsFloatBufferL
	 * ByteBufferAsFloatBufferRB ByteBufferAsFloatBufferRL
	 * ByteBufferAsIntBufferB ByteBufferAsIntBufferL ByteBufferAsIntBufferRB
	 * ByteBufferAsIntBufferRL ByteBufferAsLongBufferB ByteBufferAsLongBufferL
	 * ByteBufferAsLongBufferRB ByteBufferAsLongBufferRL
	 * ByteBufferAsShortBufferB ByteBufferAsShortBufferL
	 * ByteBufferAsShortBufferRB ByteBufferAsShortBufferRL ByteOrder CharBuffer
	 * DirectByteBuffer DirectByteBufferR DirectCharBufferRS DirectCharBufferRU
	 * DirectCharBufferS DirectCharBufferU DirectDoubleBufferRS
	 * DirectDoubleBufferRU DirectDoubleBufferS DirectDoubleBufferU
	 * DirectFloatBufferRS DirectFloatBufferRU DirectFloatBufferS
	 * DirectFloatBufferU DirectIntBufferRS DirectIntBufferRU DirectIntBufferS
	 * DirectIntBufferU DirectLongBufferRS DirectLongBufferRU DirectLongBufferS
	 * DirectLongBufferU DirectShortBufferRS DirectShortBufferRU
	 * DirectShortBufferS DirectShortBufferU DoubleBuffer FloatBuffer
	 * HeapByteBuffer HeapByteBufferR HeapCharBuffer HeapCharBufferR
	 * HeapDoubleBuffer HeapDoubleBufferR HeapFloatBuffer HeapFloatBufferR
	 * HeapIntBuffer HeapIntBufferR HeapLongBuffer HeapLongBufferR
	 * HeapShortBuffer HeapShortBufferR IntBuffer InvalidMarkException
	 * LongBuffer MappedByteBuffer ReadOnlyBufferException ShortBuffer
	 * StringCharBuffer Unsafe
	 */
	public Ascon128V11NoDepsEncryptProblem(){
		setNames("NoDependencies");
		problemStrings = loadFiles("NoDependencies");
	}
	
	protected void setNames(String name){ // this sucks TODO fix it!
		problemName = "iaik.ascon128v11NoDeps.Ascon128v11"+name; // Should w get this from the file?
		// these will be loaded from eclipse project
		className = "iaik.ascon128v11NoDeps.Ascon128v11"+name;  
		setMethodName();
		setCases();
	}
	
	/*protected void setMethodName() {
		super.methodName = "crypto_aead_encrypt";
	}*/
	
	public Ascon128V11NoDepsEncryptProblem(String variantName){
		setNames(variantName);
		problemStrings = loadFiles(variantName);
	}
	
	protected CompilationSet loadFiles(String name){
		String[] allFiles = { "Arrays", "Ascon128v11NoDependencies",
				"Bits", "BufferOverflowException", "Buffer",
				"BufferUnderflowException", "ByteBufferAsCharBufferB",
				"ByteBufferAsCharBufferL", "ByteBufferAsCharBufferRB",
				"ByteBufferAsCharBufferRL",
				"ByteBufferAsDoubleBufferB",
				"ByteBufferAsDoubleBufferL",
				"ByteBufferAsDoubleBufferRB",
				"ByteBufferAsDoubleBufferRL",
				"ByteBufferAsFloatBufferB", "ByteBufferAsFloatBufferL",
				"ByteBufferAsFloatBufferRB",
				"ByteBufferAsFloatBufferRL", "ByteBufferAsIntBufferB",
				"ByteBufferAsIntBufferL", "ByteBufferAsIntBufferRB",
				"ByteBufferAsIntBufferRL", "ByteBufferAsLongBufferB",
				"ByteBufferAsLongBufferL", "ByteBufferAsLongBufferRB",
				"ByteBufferAsLongBufferRL", "ByteBufferAsShortBufferB",
				"ByteBufferAsShortBufferL",
				"ByteBufferAsShortBufferRB",
				"ByteBufferAsShortBufferRL", "ByteBuffer",
				"ByteOrder", "CharBuffer", "DirectByteBufferR",
				"DirectByteBuffer", "DirectCharBufferRS",
				"DirectCharBufferRU", "DirectCharBufferS",
				"DirectCharBufferU", "DirectDoubleBufferRS",
				"DirectDoubleBufferRU", "DirectDoubleBufferS",
				"DirectDoubleBufferU", "DirectFloatBufferRS",
				"DirectFloatBufferRU", "DirectFloatBufferS",
				"DirectFloatBufferU", "DirectIntBufferRS",
				"DirectIntBufferRU", "DirectIntBufferS",
				"DirectIntBufferU", "DirectLongBufferRS",
				"DirectLongBufferRU", "DirectLongBufferS",
				"DirectLongBufferU", "DirectShortBufferRS",
				"DirectShortBufferRU", "DirectShortBufferS",
				"DirectShortBufferU", "DoubleBuffer",
				"FloatBuffer", "HeapByteBufferR", "HeapByteBuffer",
				"HeapCharBufferR", "HeapCharBuffer",
				"HeapDoubleBufferR", "HeapDoubleBuffer",
				"HeapFloatBufferR", "HeapFloatBuffer",
				"HeapIntBufferR", "HeapIntBuffer",
				"HeapLongBufferR", "HeapLongBuffer",
				"HeapShortBufferR", "HeapShortBuffer", "IntBuffer",
				"InvalidMarkException", "LongBuffer",
				"MappedByteBuffer", "ReadOnlyBufferException",
				"ShortBuffer", "StringCharBuffer", "Unsafe" }; 
		
		
		
		CompilationDetail[] fileSet = new CompilationDetail[allFiles.length];
		int i =0;
		for( String aFileName : allFiles){
			String fileContents = StringFromFile.getStringFromFile("/locoGP/problems/crypto/asconNoDeps/"+aFileName+".txt");
			fileSet[i]= new CompilationDetail(fileContents);
			i++;
		}
		CompilationSet cS = new CompilationSet(fileSet);
		return cS;
	}
	
	/*@Override
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
	}*/
	
}
