package locoGP.problems.tests;

import java.util.Arrays;

import iaik.ascon128v11.Ascon128v11;

public class AsconEncryptTestCase extends AsconDecryptTestCase{

	public AsconEncryptTestCase(String mString, String aString) {
		super(mString,aString);
	}
	
	@Override
	public Object[] getTest() {
		return cloneEncryptionInput(); // these a brand new values, we have no ref to them when it returns
	}
	
	protected Object[] cloneEncryptionInput(){
		//public static int crypto_aead_encrypt(byte c[], int clen, byte m[], int mlen, byte ad[], int adlen, byte nsec[], byte npub[], byte k[]) {
		testVals = new Object[9];//originalTestInput.length];
		testVals[0]=new byte[inputclen];// do not give the mutant the answer! 
		testVals[1]=this.inputclen;
		testVals[2]=this.inputm.clone();
		testVals[3]=this.inputmlen; 
		testVals[4]=this.inputa.clone();
		testVals[5]=this.inputalen; // integers are copied in Java
		testVals[6]= this.inputnsec.clone(); 
		testVals[7]=this.inputnpub.clone();
		testVals[8]=this.inputk.clone();
		return testVals;
	}
	

	@Override
	public int checkAnswer(Object ans) {
		return checkEncryptAnswer(ans);
	}
	
	public int checkEncryptAnswer(Object ans) {
		int errorCount = 0 ;
		if( ! Arrays.equals(this.encryptOracleC,(byte[])this.testVals[0])){ // reference the cloned array which was given out for testing 
			errorCount+=1;
		}
		if(((Integer)ans != this.encryptOracleReturnValclen) ) // We give the seed program an empty array
			errorCount+=1;
		
		return errorCount;
}
	


}
