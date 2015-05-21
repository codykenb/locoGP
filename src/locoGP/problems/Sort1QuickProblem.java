package locoGP.problems;

public class Sort1QuickProblem extends Sort1Problem{

	/**
	 * 
	 */
	
public Sort1QuickProblem(){
		problemName = "Sort1Quick";//this.className;//+"Test";
		className = problemName;
		methodName = "sort";

		//http://codereview.stackexchange.com/questions/4022/java-implementation-of-quick-sort
		
		problemString = "public class "+ problemName +" { \n" +
				"  public static Integer[] sort(Integer []a , Integer length) { \n" +
		//"		void quick_sort (int *a, int n) { \n" +
	    //public static void quickSort(int[] a, int p, int r)
		" return sort(a,0,length-1); \n"+
	    "} \n" +
	    "public static Integer[] sort(Integer []a , Integer p, Integer r){ \n"+
	    "    if(p<r) \n" +
	    "    { \n" +
//	    "        int q=partition(a,p,r); \n" +
	    "int q =0 ; \n"+	
	    "        int x = a[p]; \n" +
		"        int i = p-1 ; \n" +
		"        int j = r+1 ; \n" +

		        "while (true) { \n" +
		        "    i++; \n" +
		        "    while ( i< r && a[i] < x) \n" +
		        "        i++; \n" +
		        "    j--; \n" +
		        "    while (j>p && a[j] > x) \n" +
		        "        j--; \n" +

		            "if (i < j){ \n" +
		            "    //swap(a, i, j); \n" +
		            "    int temp = a[i]; \n" +
		    	    "    a[i] = a[j]; \n" +
		    	    "    a[j] = temp; \n" +
		            "} \n" +
		            "else{ \n" +
		            "    q= j; \n" +
		            "    break; \n" +
		            "} \n" +
		        "} \n" +
		        
	            "sort(a,p,q); \n" +
	            "sort(a,q+1,r); \n" +
	        "} \n" +
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
