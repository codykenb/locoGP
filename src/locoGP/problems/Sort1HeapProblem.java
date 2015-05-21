package locoGP.problems;

public class Sort1HeapProblem extends Sort1Problem{

	/**
	 * 
	 */
	
public Sort1HeapProblem(){
		problemName = "Sort1HeapProblem"; 
		className = problemName;
		methodName = "sort";

		
		problemString = "public class "+ problemName +" { \n" +
						"  public static Integer[] sort(Integer []a , Integer array_size) { \n" +
						"		int i; \n" +
						"		for (i = (array_size / 2 - 1); i >= 0; --i) { \n" +
						"			int maxchild, temp, child, root = i, bottom = array_size - 1; \n" +
						"			while (root * 2 < bottom) { \n" +
						"				child = root * 2 + 1; \n" +
						"				if (child == bottom) { \n" +
						"					maxchild = child; \n" +
						"				} else { \n" +
						"					if (a[child] > a[child + 1]) { \n" +
						"						maxchild = child; \n" +
						"					} else { \n" +
						"						maxchild = child + 1; \n" +
						"					} \n" +
						"				} \n" +
						"				if (a[root] < a[maxchild]) { \n" +
						"					temp = a[root]; \n" +
						"					a[root] = a[maxchild]; \n" +
						"					a[maxchild] = temp; \n" +
						"				} else { \n" +
						"					break; \n" +
						"				} \n" +
						"				root = maxchild; \n" +
						"			} \n" +
						"		} \n" +
						"		for (i = array_size - 1; i >= 0; --i) { \n" +
						"			int temp; \n" +
						"			temp = a[i]; \n" +
						"			a[i] = a[0]; \n" +
						"			a[0] = temp; \n" +
						"			int maxchild, child, root = 0, bottom = i - 1; \n" +
						"			while (root * 2 < bottom) { \n" +
						"				child = root * 2 + 1; \n" +
						"				if (child == bottom) { \n" +
						"					maxchild = child; \n" +
						"				} else { \n" +
						"					if (a[child] > a[child + 1]) { \n" +
						"						maxchild = child; \n" +
						"					} else { \n" +
						"						maxchild = child + 1; \n" +
						"					} \n" +
						"				} \n" +
						"				if (a[root] < a[maxchild]) { \n" +
						"					temp = a[root]; \n" +
						"					a[root] = a[maxchild]; \n" +
						"					a[maxchild] = temp; \n" +
						"				} else { \n" +
						"					break; \n" + // this is heapsort with function calls replaced with contents of functions. surely optimisable ;)
						"				} \n" +
						"				root = maxchild; \n" +
						"			} \n" +
						"		} \n" +
						"	return a; \n" +
						"  }\n" +
						"} \n" ;
	}

}
