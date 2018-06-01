package locoGP.problems.tests;

import java.util.Arrays;

public class SortTestCase implements TestCase {

	private Object[] test = null; // this should be an array containing an array, and its length
	private Integer[] oracleAnswer = null;
	
	public SortTestCase(Object[] testParameters, Object ansArray) {
		this.setTest(testParameters);
		this.oracleAnswer = (Integer[])ansArray;
	}
	
	private void setTest(Object[] test) {
		this.test = test;
	}

	public Object[] getTest() {
		return new Object[]{ ((Integer[])test[0]).clone(), ((Integer) test[1])};
	}

	public Integer[] getAnswer() {
		return oracleAnswer;
	}
	
	public int checkAnswer(Object ans){
		return checkAnswer((Integer[]) ans );
	}
	
	private int checkAnswer(Integer[] returnedAns){
		Integer[] originalUnsortedTest = (Integer[]) test[0];
		int errorCount = 0;
		for (int i = 0; i < oracleAnswer.length; i++) {
			/*if(returnedAns.length -1 < i)
				errorCount += 5;
			else */
			if (oracleAnswer[i].equals(returnedAns[i])) // 
				errorCount++; // 1 for correct
			//else if (!oracleAnswer[i].equals(originalUnsortedTest[i]))
			else if (!originalUnsortedTest[i].equals(returnedAns[i]))
				errorCount += 2; // not correct, but has been moved 
			else
				errorCount += 10; // highest cost for no change
		}
		return errorCount;
		//}
	}
	
	/*public int checkAnswer(Integer[] ans){
		boolean same = false;
		if( Arrays.equals((Integer[])test[0],(ans)))
			return -(answer.length*5);// needed in case the array is exactly the same, if so, penalise the result
		else {
			int sameCount = 0;
			for (int i = 0; i < answer.length; i++) { // number goes up, when array is sorted.. wrong!
				if (answer[i].equals(ans[i]))
					sameCount++;
			}
			return sameCount;
		}
	}*/

}
