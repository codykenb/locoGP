package locoGP.problems.tests;

import java.util.Arrays;

public class SortTestCase implements TestCase {

	private Object[] test = null; // this should be an array containing an array, and its length
	private Integer[] answer = null;
	
	public SortTestCase(Object[] testParameters, Object ansArray) {
		this.setTest(testParameters);
		this.answer = (Integer[])ansArray;
	}
	
	private void setTest(Object[] test) {
		this.test = test;
	}

	public Object[] getTest() {
		return new Object[]{ ((Integer[])test[0]).clone(), ((Integer) test[1])};
	}

	public Integer[] getAnswer() {
		return answer;
	}
	
	public int checkAnswer(Object ans){
		return checkAnswer((Integer[]) ans );
	}
	
	public int checkAnswer(Integer[] ans){
		Integer[] originalTest = (Integer[]) test[0];
			int errorCount = 0;
			for (int i = 0; i < answer.length; i++) { 
				if (answer[i].equals(ans[i]))
					errorCount++; // 1 for correct
				else if(!answer[i].equals(originalTest[i]))
					errorCount +=2; // 2 for moved, but not correct
				else
					errorCount += 3; // highest cost for no change 
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
