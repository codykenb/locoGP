package locoGP.problems;

public class Sort1RadixProblem extends Sort1Problem{

	/**
	 * 
	 */
	
public Sort1RadixProblem(){
		problemName = "Sort1Radix";//this.className;//+"Test";
		className = problemName;
		methodName = "sort";

		//http://rosettacode.org/wiki/Radix_sort#Java
		
		problemString = "public class "+ problemName +" { \n" +
				"	public static Integer[] sort(Integer []a , Integer length) { \n" +
				"	for(int shift = Integer.SIZE-1; shift > -1; shift--) {  \n" +	 
				"		Integer[] tmp = new Integer[a.length];  \n" +
				"		int j = 0;  			  \n" +
				"	for(int i = 0; i < length; i++) {   \n" +
"		boolean move = a[i] << shift >= 0;     \n" +
"						if(shift == 0 ? !move : move) {   \n" +
"							tmp[j] = a[i]; \n" +
"							j++; \n" +
"							} else {   \n" +
"							a[i-j] = a[i];  \n" +
"						} \n" +
"					} \n" +
"					for(int i = j; i < tmp.length; i++) {   \n" +
"						tmp[i] = a[i-j];	 \n" +
"					} \n" +
"					a = tmp;   \n" +
"				} \n" +
"				return a; \n" +
"			} \n" +
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
