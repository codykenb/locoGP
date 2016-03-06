package locoGP.problems;

public class Sort1InsertionProblem extends Sort1Problem{

	/**
	 * 
	 */
	
public Sort1InsertionProblem(){
		problemName = "Sort1Insertion";//this.className;//+"Test";
		className = problemName;
		methodName = "sort";

		
		problemString = "public class "+ problemName +" { \n" +
						"  public static Integer[] sort(Integer []a , Integer array_size) { \n" +
						"	int i, j, index; \n" +
						"	for (i = 1; i < array_size; ++i) \n" +
						"	{ \n" +
						"	     index = a[i]; \n" +
						"	     for (j = i; j > 0 && a[j-1] > index; j--){ \n" +
						"	          a[j] = a[j-1]; \n" +
						"			}" +
						"	     a[j] = index; \n" +
						"	} \n" +
						"	return a; \n" +
						"  }\n" +
						"} \n" ;
	}

	
}
