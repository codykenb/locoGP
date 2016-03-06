package locoGP.problems;

public class Sort1CocktailProblem extends Sort1Problem{

	/**
	 * 
	 */
	
public Sort1CocktailProblem(){
		problemName = "Sort1Cocktail";//this.className;//+"Test";
		className = problemName;
		methodName = "sort";

		//http://rosettacode.org/wiki/Cocktail_Sort#Java
		
		problemString = "public class "+ problemName +" { \n" +
				"	public static Integer[] sort(Integer []a , Integer length) { \n" +
				" boolean swapped; \n" +
				" do { \n" +
			" swapped = false; \n" +
			" 		for (int i =0; i<=  length  - 2;i++) { \n" +
			" if (a[ i ] > a[ i + 1 ]) { \n" +
					//test whether the two elements are in the wrong order
					" int temp = a[i]; \n" +
					" a[i] = a[i+1]; \n" +
					" a[i+1]=temp; \n" +
					" swapped = true; \n" +
					" } \n" +
					" } \n" +
					" if (!swapped) { \n" +
				//we can exit the outer loop here if no swaps occurred.
				" break; \n" +
				" } \n" +
				" swapped = false; \n" +
				" for (int i= length - 2;i>=0;i--) { \n" +
				" if (a[ i ] > a[ i + 1 ]) { \n" +
				" int temp = a[i]; \n" +
				" a[i] = a[i+1]; \n" +
				" a[i+1]=temp; \n" +
				" swapped = true; \n" +
				" } \n" +
				" } \n" +
			//if no elements have been swapped, then the list is sorted
			" } while (swapped); \n" +
"				return a; \n" +
"			} \n" +
						"} \n" ;
	}

	
}
