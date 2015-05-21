package locoGP.problems;

public class Sort1ProblemVariantSeed extends Sort1Problem{

	
	public Sort1ProblemVariantSeed(){
		/*problemString = "public class Sort1ProblemVariantTest {  \n" +
		  "public static Integer[] sort(  Integer[] a,  Integer length){\n" +
		  "  for (int i=0; i < length; i--) {\n" +
		  "    for (int j=0; j < length - i++; j++) {\n" +
		  "    }\n" +
		  "  }\n" +
		  "  for (int i=0; i < length; i--) {\n" +
		  "    for (int j=0; j < length - i++; j++) {\n" +
		  "      if (a[j] > a[j + 1]) {\n" +
		  "        int k=a[j];\n" +
		  "        a[j]=a[j + 1];\n" +
		  "        a[j + 1]=k;\n" +
		  "      }\n" +
		  "    }\n" +
		  "  }\n" +
		  "  return a;\n" +
		  "}\n" +
		"}\n" ;*/
		
		problemName = className = "Sort1ProblemVariantTest";
	
	
	
	
	
	problemString = "public class "+problemName +"{\n" +
			"public static Integer[] sort( Integer[] a, Integer length){\n" +
			"{\n" +
			"{\n" +
			"{\n" +
			"{\n" +
			"}\n" +
			"{\n" +
			"for (int j=0; j < length - a[j]; j++) {\n" +
			"if (a[j] > a[length - j + 1 + 1]) {\n" +
			"int k=a[j];\n" +
			"a[j]=a[j + 1];\n" +
			"a[j + 1]=k;\n" +
			"}\n" +
			"}\n" +
			"}\n" +
			"for (int j=0; j < length - j + 1; j++) {\n" +
			"if (a[j + 1] > a[j + 1]) {\n" +
			"int k=a[j];\n" +
			"a[j]-=a[j++ + 1];\n" +
			"j=k;\n" +
			"}\n" +
			"}\n" +
			"}\n" +
			"for (int j=0; j < length - a[j]; j++) {\n" +
			"if (a[j] > a[j + 1]) {\n" +
			"int k=a[j];\n" +
			"a[j]=a[j + 1];\n" +
			"a[j + 1]=k;\n" +
			"}\n" +
			"}\n" +
			"}\n" +
			"{\n" +
			"for (int j=0; j < length - a[j]; j++) {\n" +
			"{\n" +
			"int k=a[j];\n" +
			"a[j]=a[j + 1];\n" +
			"a[j + 1]=k;\n" +
			"}\n" +
			"if (a[j] > a[j + 1]) {\n" +
			"int k=a[j];\n" +
			"a[j]=a[j + 1];\n" +
			"a[j + 1]=k;\n" +
			"}\n" +
			"}\n" +
			"}\n" +
			"for (int j=0; j < length - j + 1; j++) {\n" +
			"if (a[j] > a[j + 1]) {\n" +
			"int k=a[j];\n" +
			"a[j]=a[j++ + 1];\n" +
			"a[j + 1]=k;\n" +
			"}\n" +
			"}\n" +
			"}\n" +
			"for (int j=0; j < length - a[j]; j++) {\n" +
			"if (a[j] > a[j + 1]) {\n" +
			"int k=a[j];\n" +
			"a[j]=a[j + 1];\n" +
			"a[j + 1]=k;\n" +
			"}\n" +
			"}\n" +
			"return a;\n" +
			"}\n" +
			"}\n" ;
	}
	
}
