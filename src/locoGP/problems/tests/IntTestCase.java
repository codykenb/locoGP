package locoGP.problems.tests;

public class IntTestCase implements TestCase{

	private Integer[] test = null;
	private Integer answer = 0;
	
	public IntTestCase(Object[] testParameters, Object answer) {
		this.setTest(testParameters);
		this.setAnswer((Integer)answer);
	}

	private void setTest(Object[] test) {
		this.test = (Integer[])test;
	}

	public Object[] getTest() {
		return test;
	}

	private void setAnswer(int answer) {
		this.answer = answer;
	}

	public Object getAnswer() {
		return answer;
	}
	
	public int checkAnswer(Object ans){
		if(answer.equals((Integer)ans))
			return 0;
		else
			return 1;
	}

}
