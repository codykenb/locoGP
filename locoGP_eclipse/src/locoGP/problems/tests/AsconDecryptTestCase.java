package locoGP.problems.tests;

import java.util.Arrays;

import iaik.ascon128v11.Ascon128v11;

import org.apache.commons.lang3.ArrayUtils;

public class AsconDecryptTestCase implements TestCase {

	// the test input (empty C in case of encrypt, empty a & d in case of decrypt
	//protected Object[] originalTestInput = null; // should not change
	// result of values after encrypt or decrypt
	/*protected byte[] oracleM = null;
	protected byte[] oracleA = null;
	protected byte[] oracleD = null;*/
	
	// a copy of originalTestInput which can be are modified inplace and compared to oracle
	protected Object[] testVals = null; 
	
	//int MAXLEN = 65536;
	protected int inputalen ;
	protected int inputclen ; //= MAXLEN + Ascon128v11.CRYPTO_ABYTES;
	protected int inputmlen;
	protected byte[] inputm;
	protected byte[] inputa;
	protected byte[] inputnsec;
	protected byte[] inputnpub=
        {(byte) 0x7c, (byte) 0xc2, (byte) 0x54, (byte) 0xf8, (byte) 0x1b, (byte) 0xe8, (byte) 0xe7,
            (byte) 0x8d, (byte) 0x76, (byte) 0x5a, (byte) 0x2e, (byte) 0x63, (byte) 0x33,
            (byte) 0x9f, (byte) 0xc9, (byte) 0x9a};
    
	protected byte[] inputk=
        {0x67, (byte) 0xc6, 0x69, 0x73, 0x51, (byte) 0xff, 0x4a, (byte) 0xec, 0x29, (byte) 0xcd,
            (byte) 0xba, (byte) 0xab, (byte) 0xf2, (byte) 0xfb, (byte) 0xe3, 0x46};
    
	// the oracle code sets these values
	protected byte[] encryptOracleC; 
	protected int encryptOracleReturnValclen;
	protected byte[] decryptOracleM;
	protected int decryptOracleReturnValmlen;

	
	/*public asconTestCase(Object[] testParameters, byte[] expectedAnswer) {
		this.setTest(testParameters);
		this.oracleAnswer = expectedAnswer;
		//this.codebookBitLength = countBitLength(oracleAnswer);
	}*/
	
	public AsconDecryptTestCase(String mString, String aString){
		setupCase(mString, aString);
	}
	
	/* oh the humanity!
	 * public asconTestCase(String string, int weakSecurity) {
		setupCase(string);
		if(weakSecurity ==1){
			npub=new byte[]{(byte) 0x0c};
			k=new byte[]{0x0};
		}else if (weakSecurity ==2){
			npub=new byte[]{(byte) 0x0c, (byte) 0xc2};
			k=new byte[]{(byte)0x0, (byte) 0xc2};
		}
	}*/

	private void setupCase(String mString, String aString) {
		byte[] initialM = mString.getBytes();
		byte[] initialA = aString.getBytes();

		this.inputmlen = initialM.length;
		this.inputm = initialM; //new byte[mlen]; // thats a bug!  
		inputalen=initialA.length; // for 65k characters, it takes over 7.5 minutes to evaluate
		this.inputa = initialA; //new byte[alen]; 
		inputclen = inputalen + Ascon128v11.CRYPTO_ABYTES;
		
		/*for (int i = 0; i < a.length; ++i)
		      a[i] = (byte) ('A' + i % 26);*/
	    this.encryptOracleC = new byte[inputm.length + Ascon128v11.CRYPTO_ABYTES];
	    inputclen = this.encryptOracleC.length;
	    this.inputnsec = new byte[Ascon128v11.CRYPTO_NSECBYTES];
	    
		//String[] sampleResult = Ascon128v11.crypto_aead_encrypt(testInput);
	    
	    encryptOracleReturnValclen = Ascon128v11.crypto_aead_encrypt(encryptOracleC, inputclen, inputm, inputmlen, inputa, inputalen, inputnsec, inputnpub, inputk);
	  //crypto_aead_decrypt(byte m[], int mlen, byte nsec[], byte c[], int clen, byte ad[], int adlen, byte npub[], byte k[])
	    //oraclemlen = Ascon128v11.crypto_aead_decrypt(oraclem, oraclemlen, oraclensec, oraclec, oracleclen,   oraclea, oraclealen,  oraclenpub, oraclek);
	    // Why does the reference demo for ascon give the decrypt function the answers???
	    decryptOracleM=new byte[this.inputmlen];

	    decryptOracleReturnValmlen = Ascon128v11.crypto_aead_decrypt(decryptOracleM, inputmlen, inputnsec, encryptOracleC, encryptOracleReturnValclen, inputa, inputalen,  inputnpub, inputk);
	    //decryptReturnVal = Ascon128v11.crypto_aead_decrypt(new byte[this.oraclemlen], oraclemlen, oraclensec, oraclec, oracleclen, new byte[this.oraclealen], oraclealen,  oraclenpub, oraclek);
	    
		// all oracle values should be set correctly now!
	    
		// decryption/decryption has different ordering
		// this is fine for Decrypt:
		//this.setTest(m.clone(), mlen, nsec, c, clen, a.clone(), alen,npub,k);
		
		//setReturnVals(); // why?
		
	}
	
/*	protected void setReturnVals() { // why is this needed?
		//crypto_aead_decrypt(byte m[], int mlen, byte nsec[], byte c[], int clen, byte ad[], int adlen, byte npub[], byte k[])
		Object[] tempInput = cloneDecryptionInput();
		int tempMLen = (Integer)tempInput[1];
		int tempCLen= (Integer)tempInput[4];
		int tempALen = (Integer)tempInput[6]; 
		this.returnVal = Ascon128v11.crypto_aead_decrypt(
				(byte[])tempInput[0], 
				tempMLen, 
				(byte[])tempInput[2],
				(byte[])tempInput[3],
				tempCLen, 
				(byte[])tempInput[5], 
				tempALen, 
				(byte[])tempInput[7], 
				(byte[])tempInput[8]);
		this.oracleM = (byte[])tempInput[0]; // this is what we should get back after decryption
	}*/

	/*protected void setTest(byte[] m2, int mlen2, byte[] nsec2, byte[] c2, int clen2, byte[] a2, int alen2, byte[] npub2, byte[] k2) {
		this.originalTestInput = new Object[]{m2, mlen2, nsec2, c2, clen2, a2, alen2,npub2,k2};
	}*/
	
	@Override
	public Object[] getTest() { // data to be passed to a mutant program
		return cloneDecryptionInput(); // these a brand new values, we have no ref to them when it returns
	}
	
	/**
	 * Create a set of variables which will be passed to a mutant program.
	 * The program may modify these variables in place.
	 * These variables are subsequently compared to the oracle values. 
	 * @return
	 */
	protected Object[] cloneDecryptionInput(){
		//this.setTest(m.clone(), mlen, nsec, c, clen, a.clone(), alen,npub,k);
		this.testVals = new Object[9] ; 
		testVals[0]=new byte[this.inputmlen]; // do not set the answer!
		//int newmlen = this.oraclemlen;
		testVals[1]=this.inputmlen; //newmlen; 
		testVals[2]=this.inputnsec.clone(); // clone arrays so that the oracle values can not be modified by a mutant program
		testVals[3]=this.encryptOracleC.clone();
		//int tempclen =clen; 
		testVals[4]=this.encryptOracleReturnValclen; //oracleclen;
		testVals[5]=this.inputa.clone(); // needed for verification
		//int tempalen = alen;
		testVals[6]=this.inputalen;
		//testVals[7]=nsec.clone();
		testVals[7]=this.inputnpub.clone();
		testVals[8]=this.inputk.clone();
		return testVals;
	}

	@Override
	public Object getAnswer() {
		new Exception().printStackTrace(System.out); // yuch! TODO
		System.out.println();
		System.exit(1);
		return null;
	}

	@Override
	public int checkAnswer(Object ans) {
		return checkDecryptAnswer(ans);
	}
	
	public int checkDecryptAnswer(Object ans) {
			//System.exit(1);
			
 			int errorCount = 0 ;
			if( ! Arrays.equals(this.decryptOracleM,(byte[])this.testVals[0])){ // reference the cloned array which was given out for testing 
				errorCount+=1;
			}
			// ad = associated data (it's not encrypted, just authenticated)
			/*if( ! Arrays.equals(this.decryptOracleA,(byte[])this.testVals[5])){ // reference the cloned array which was given out for testing 
				errorCount+=1;
			}*/
			if(((Integer)ans != this.decryptOracleReturnValmlen) ) 
				errorCount+=1;
			
			return errorCount;
	}

	/*public int getFunctionalityScore(){
		return checkAnswer("Seed!");//codebookBitLength;
	}*/
/*	
	private int checkAnswer(String[] ans){
		
		
	}*/
	
}
