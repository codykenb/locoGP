package locoGP.problems;

public class Sort1ShellProblem extends Sort1Problem{

	/**
	 * 
	 */
	
public Sort1ShellProblem(){
		problemName = "Sort1Shell";//this.className;//+"Test";
		className = problemName;
		methodName = "sort";

		
		problemString = "public class "+ problemName +" { \n" +
		"		public static Integer[] sort(Integer []a , Integer length) { \n" +
		"	int increment = length / 2; \n" +
		"	while (increment > 0) { \n" +
		"		for (int i = increment; i < length; i++) { \n" +
		"			int j = i; \n" +
		"			int temp = a[i]; \n" +
		"			while (j >= increment && a[j - increment] > temp) { \n" +
		"				a[j] = a[j - increment]; \n" +
		"				j = j - increment; \n" +
		"			} \n" +
		"			a[j] = temp; \n" +
		"		} \n" +
		"		if (increment == 2) { \n" +
		"			increment = 1; \n" +
		"		} else { \n" +
		"			increment *= (5.0 / 11); \n" +
		"		} \n" +
		"	} \n" +
		"	return a; \n" +
		"} \n" +
						"} \n" ;
	}
	/*
	void SelectionSort(int a[], int array_size)
	{
	     int i;
	     for (i = 0; i < array_size - 1; ++i)
	     {
	          int j, min, temp;
	          min = i;
	          for (j = i+1; j < array_size; ++j)
	          {
	               if (a[j] < a[min])
	                    min = j;
	          }

	          temp = a[i];
	          a[i] = a[min];
	          a[min] = temp;
	     }
	}
	*/
	
}
