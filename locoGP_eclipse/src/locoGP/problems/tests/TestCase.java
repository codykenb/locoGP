package locoGP.problems.tests;

import java.io.Serializable;

public interface TestCase extends Serializable{

	
	public Object[] getTest(); // array of parameters to be passed to the problem method 
	
	public Object getAnswer(); // TODO move whatever depends on this into the test case!
	
	public int checkAnswer(Object ans);

}
