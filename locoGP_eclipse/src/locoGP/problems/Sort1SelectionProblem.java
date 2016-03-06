package locoGP.problems;

public class Sort1SelectionProblem extends Sort1Problem{

	/**
	 * 
	 */
	
public Sort1SelectionProblem(){
		problemName = "Sort1SelectionProblem"; //this.className; //+"Test";
		className = problemName;
		methodName = "sort";

		
		problemString = "public class "+ problemName +" { \n" +
			"	public static Integer[] sort(Integer []a, Integer length){ \n" +
			"for(int currentPlace = 0;currentPlace<length-1;currentPlace++){ \n" +
		"		int smallest = Integer.MAX_VALUE; \n" +
	"			int smallestAt = currentPlace+1; \n" +
	"			for(int check = currentPlace; check<length;check++){ \n" +
	"				if(a[check]<smallest){ \n" +
	"					smallestAt = check; \n" +
	"					smallest = a[check]; \n" +
	"				} \n" +
	"			} \n" +
	"			int temp = a[currentPlace]; \n" +
	"			a[currentPlace] = a[smallestAt]; \n" +
	"			a[smallestAt] = temp; \n" +
	"		} \n" +
	"		return a;"+
	"	} \n" +
						"} \n" ;
	}

/*
 * public static void sort(int[] nums){
	for(int currentPlace = 0;currentPlace<nums.length-1;currentPlace++){
		int smallest = Integer.MAX_VALUE;
		int smallestAt = currentPlace+1;
		for(int check = currentPlace; check<nums.length;check++){
			if(nums[check]<smallest){
				smallestAt = check;
				smallest = nums[check];
			}
		}
		int temp = nums[currentPlace];
		nums[currentPlace] = nums[smallestAt];
		nums[smallestAt] = temp;
	}
}
 * 
 */


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
