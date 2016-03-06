package locoGP.problems;

public class Sort1MergeProblem extends Sort1Problem{

	/**
	 * 
	 */
	
public Sort1MergeProblem(){
		problemName = "Sort1Merge";//this.className;//+"Test";
		className = problemName;
		methodName = "sort";

		//http://rosettacode.org/wiki/Merge_sort#C
		
		problemString = "public class "+ problemName +" { \n" +
			"  public static Integer[] sort(Integer []a , Integer length) { \n" +
			" 	mergesort_r(0, length, a); \n"+
			"	return a; \n" +
			"} \n" +
			
			"  public static Integer[] merge(Integer []a, int left_start, int left_end, int right_start, int right_end){ \n" +
			" 		int left_length = left_end - left_start; \n" +
			" 		int right_length = right_end - right_start; \n" +
			" int[] left_half = new int[left_length]; \n" +
			" int[] right_half= new int[right_length]; \n" +
			" 	int r = 0;  \n" +
			" 	int l = 0;  \n" +
			" 	int i = 0;  \n" +
			" 	for (i = left_start; i < left_end; i++, l++){ \n" +
			" 		left_half[l] = a[i]; \n" +
			" } \n" +
			" 	for (i = right_start; i < right_end; i++, r++){ \n" +
			" 		right_half[r] = a[i]; \n" +
			" } \n" +
			" for ( i = left_start, r = 0, l = 0; l < left_length && r < right_length; i++){ \n" +
			" 		if ( left_half[l] < right_half[r] ) { a[i] = left_half[l++]; } \n" +
			" 		else { a[i] = right_half[r++]; } \n" +
			" } \n" +
			" for ( ; l < left_length; i++, l++) { a[i] = left_half[l]; } \n" +
			" for ( ; r < right_length; i++, r++) { a[i] = right_half[r];}  \n" +
			"	return a; \n" +
			" } \n" +
			" public static Integer[] mergesort_r(int left, int right, Integer []a){ \n" +
			" if (right - left <= 1){ \n" +
			" 		return a; \n" +
			" }else{} \n" +
			" int left_start  = left; \n" +
			" int left_end    = (left+right)/2; \n" +
			" int right_start = left_end; \n" +
			" int right_end   = right; \n" +
			" mergesort_r( left_start, left_end, a); \n" +
			" mergesort_r( right_start, right_end, a); \n" +
			" merge(a, left_start, left_end, right_start, right_end); \n" +
			"	return a; \n" +
			" }\n" +
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
