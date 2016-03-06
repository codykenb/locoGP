package locoGP.problems.tests;

import java.io.Serializable;

public interface TestCase extends Serializable{

	
	public Object[] getTest(); // array of parameters to be passed to the problem method 
	
	public Object getAnswer();
	
	public int checkAnswer(Object ans);

}
