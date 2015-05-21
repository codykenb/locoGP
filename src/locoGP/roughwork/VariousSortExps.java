package locoGP.roughwork;

import locoGP.problems.Sort1Insertion30129;
import locoGP.problems.Sort1Quick22387;
import locoGP.problems.Sort1Radix27404;
import locoGP.problems.Sort1SelectionProblem132;

public class VariousSortExps {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Integer[] testArr = {987,36,45,56,3,2,3464,45,2,6,45,2,65,76,2,1,57,5};
		//Integer[] newArr= Sort1Quick22387.sort(testArr, testArr.length);
		//Integer[] newArr= Sort1Insertion30129.sort(testArr, testArr.length);
		//Integer[] newArr= Sort1SelectionProblem132.sort(testArr, testArr.length);
		Integer[] newArr= Sort1Radix27404.sort(testArr, testArr.length);
		
		printArr(testArr);
		printArr(newArr);
		
	}
	
	static void printArr(Integer[] arr){
		System.out.print("\nVals: " );
		for(Integer anInt:arr)
			System.out.print(anInt + " ");
	}

	public static Integer[] sort(Integer[] a, Integer length) {
		
		
		for (int currentPlace = 0; currentPlace < length - 1; currentPlace++) {
			int smallest = Integer.MAX_VALUE;
			int smallestAt = currentPlace;
			for (int check = currentPlace; check < length; check++) {
				{
					if (a[check] < smallest) {
						smallestAt = check;
						smallest = a[smallestAt];
					}
				}
			}
			int temp = a[currentPlace];
			a[currentPlace] = a[smallestAt];
			a[smallestAt] = temp;
		}
		return a;
	}
	  

}
