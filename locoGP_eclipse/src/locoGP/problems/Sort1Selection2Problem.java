package locoGP.problems;

public class Sort1Selection2Problem extends Sort1Problem{

	/**
	 * 
	 */
	
public Sort1Selection2Problem(){
		problemName = "Sort1Selection2Problem"; //this.className; //+"Test";
		className = problemName;
		methodName = "sort";

		
		problemString = "public class " + problemName + " { \n"
				+ "	public static Integer[] sort(Integer []a, Integer length){ \n"
				+ "		double p = 0;  \n" 
				+ "		int k = 0; \n" 
				+ "		for (int i = 0; i < length - 1; i++) \n" + "{ \n"
				+ "   		k = i; \n" 
				+ "			for (int j = i + 1; j < length; j++) \n"+ "{  \n" 
				+ "				if (a[j] < a[k])  \n" + "k = j; \n" + "} \n" 
				+ "				p = a[i]; \n" 
				+ " 			a[i] = a[k]; \n" 
				+ "				a[k] = (int) p;  \n"
				+ "			} \n" 
				+ "			return a;" 
				+ "		} \n" 
				+ "} \n";
	}

/*
 * 
 * Selection sort apparently, taken from here:
http://stackoverflow.com/questions/21334739/what-algorithm-is-this/21334774#21334774

public static void sort(double[] tal)
{
    double p = 0;
    int k = 0;

    for (int i = 0; i < tal.length - 1; i++)
    {
        k = i;
        for (int j = i + 1; j < tal.length; j++)
        {
            if (tal[j] < tal[k])
                k = j;
        }

        p = tal[i];
        tal[i] = tal[k];
        tal[k] = p;
    }
} 

*/
	
}
