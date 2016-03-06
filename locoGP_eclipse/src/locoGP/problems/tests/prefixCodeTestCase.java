package locoGP.problems.tests;

import org.apache.commons.lang3.ArrayUtils;

public class prefixCodeTestCase implements TestCase {

	private Object[] testInput = null; // this should be an array containing an array
	private String[] oracleAnswer = null;
	private int codebookBitLength;
	
	public prefixCodeTestCase(Object[] testParameters, String[] sampleResult) {
		this.setTest(testParameters);
		this.oracleAnswer = sampleResult;
		this.codebookBitLength = countBitLength(oracleAnswer);
	}

	
	private int countBitLength(String[] codebook) {
		int tempCodebookBitLength =0 ;
		for(int i =0; i<codebook.length; i++){
			tempCodebookBitLength += codebook[i].length();
		}
		return tempCodebookBitLength;
	}


	private void setTest(Object[] test) {
		this.testInput = test;
	}
	
	@Override
	public Object[] getTest() {
		return new Object[]{ ((Byte[])testInput[0]).clone()};
	}

	@Override
	public Object getAnswer() {
		return oracleAnswer;
	}

	@Override
	public int checkAnswer(Object ans) {
			return checkAnswer((String[])ans) ;
	}

	public int getFunctionalityScore(){
		return codebookBitLength;
	}
	
	public int checkAnswer(String[] ans){
		/*
		 * Compare an array of strings
		 * Gradient of prefix-code-eness
		 * Count the number of characters that are the same
		 * Higher the number, the better the answer
		 */
		int errorCount = 0 ;
		
		/* TODO Figure out if an empty code-book can have better functionality than one
		 * will the right number of programs, but many prefix violations? (it can't)
		 * Whats the "worst" functionality value? (This is needed for R3)
		 */
		errorCount += countIncorrectCodebookLength(ans);		
 
		errorCount += countPrefixViolations(ans); 
		
		if(errorCount >0){
			errorCount += codebookBitLength; // individual with functional errors can never have smaller error count 
		}/*else{ // if there's no error, we have a prefix code, just measure the bit length... 
			errorCount = countBitLength(ans);
		}*/
		
		errorCount += countBitLength(ans);
		
		/*if(errorCount >0){
			errorCount = errorCount*codebookBitLength+countBitLength(ans); // individual with functional errors can never have smaller error count 
		}else{ // if there's no error, we have a prefix code, just measure the bit length... 
			errorCount = countBitLength(ans);
		}*/
		return errorCount;
	}


	private int countPrefixViolations(String[] ans) {
		int violations = 0 ;
		// there is a prefix violation if one code is contained at the start of another 
		for (int i =0 ; i< ans.length ;  i++){
			for (int j =0 ; j< ans.length ;  j++){
				if(ans[i]==null || ans[j]==null){
					violations+=1; // not exactly a prefix violation, but its not good
				}else if(i == j ){
					// theyre the same, so skip it
				}else if( (ans[i].length() >= ans[j].length()) ){ // 
					violations += checkPrefix(ans[i], ans[j]);
				}
			}
		}
		return violations; 
	}


	private int checkPrefix(String aCode, String anotherSameLengthOrLongerCode) {
		for(int i=0 ; i <aCode.length() ; i++ ){
			if(aCode.charAt(i)!= anotherSameLengthOrLongerCode.charAt(i)){
				return 0;
			}
		}
		return 1;
	}


	private int countIncorrectCodebookLength(String[] ans) {
		int incorrectCodeBookLength = ans.length - oracleAnswer.length;
		if(incorrectCodeBookLength > oracleAnswer.length){
			incorrectCodeBookLength = oracleAnswer.length;
		}else if (incorrectCodeBookLength <0 ){
			incorrectCodeBookLength = -incorrectCodeBookLength;
		}
		
		/*if (incorrectCodeBookLength > oracleAnswer.length)// why?
			incorrectCodeBookLength = oracleAnswer.length;*/
		return incorrectCodeBookLength;
	}
	
}
