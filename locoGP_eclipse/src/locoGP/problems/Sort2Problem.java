package locoGP.problems;

import java.util.ArrayList;
import java.util.Random;

import locoGP.individual.Individual;
import locoGP.problems.tests.SortTestCase;
import locoGP.problems.tests.TestCase;

public class Sort2Problem extends Sort1Problem{

	private String problemName = "Sort1Test";
	private String className = "Sort1Test";
	private String methodName = "sort";

	public String getEntryClassName() {
		return className;
	}
	public String getCodeListing(){
			return problemString;
	}

	public String getMethodName() {
		return methodName;
	}

	public CompilationSet getStrings() {
		CompilationDetail[] fileSet = new CompilationDetail[1];
		fileSet[0].setCodeString(problemString);
		fileSet[0].setClassName(className,"");
		return new CompilationSet(fileSet);
	}

	public TestCase[] getTestData() {
		return testData;
	}
	public int getMissingTestCaseValue(int numMissing){
		int missingError=0;
		for(int i = testData.length-1 ; i>numMissing ; i--)
			missingError += ((Integer[]) testData[i].getAnswer()).length * 2;
		return missingError;
	}
	public void setTestData(TestCase[] testData){
		this.testData = testData;
	}
	
	// TODO we need to extend our system to handle for loops, and their conditionals!
	private String problemString = "class Sort1Test { \n" +
	"   public static void sort(  Integer[] a,  Integer length){\n" +
    "for (int i=0; i < length; i++) {\n" +
      "i++;\n" +
      "for (; i < length; i++) {\n" +
        "for (int j=0; j < length - 1; j++) {\n" +
          "if (a[j] > a[j + 1]) {\n" +
            "int k=a[j];\n" +
            "a[j]=a[j + 1];\n" +
            "a[j + 1]=k;\n" +
          "}\n" +
        "}\n" +
      "}\n" +
      "for (int j=0; j < length - 1; j--) {\n" +
        "if (a[j] > a[j + 1]) {\n" +
          "i++;\n" +
          "int k=a[j];\n" +
          "a[j]+=a[j + 1];\n" +
          "a[j + 1]=k;\n" +
        "}\n" +
      "}\n" +
    "}\n" +
  "}\n" +

	"} \n" ;
		
	private TestCase[] testData = { 
			createCase(0),
			createCase(0),
			createCase(0),
			createCase(1),
			createCase(2)
			
		}; // an array of test cases
	
	public int getNumTests(){
		return testData.length;
	}
	
	public int sampleCall ( Object[] args )
	{
		sampleCall( (Integer[])args[0], (Integer)args[1] );
		return 0;
	}

	public void sampleCall(Integer[] a, Integer length) {
		for (; 0 < (length - 1); length--) {
			for (int j = 0; j < (length - 1); j++) {
				if (a[j] > a[1 + j]) {
					int k = a[j];
					a[j] = a[j + 1];
					a[1 + j] = k;
				}
			}
		}
	}
	
	private Integer worstFunctionalityScore = null;
	public int getWorstFunctionalityScore(){
		if (worstFunctionalityScore == null)
				worstFunctionalityScore =getSeedFunctionalityScore() *3; 
		return worstFunctionalityScore;
	}
	
	private Integer[] createRandomIntArray(){
		Random ran = new Random();
		Integer[] randomArray = new Integer[100 + ran.nextInt(900)];
		for(int i = 0 ; i< randomArray.length; i++)
			randomArray[i] = ran.nextInt();
		return randomArray;
	}

	private Integer[] createAlmostSortedArray(){
		Random ran = new Random();
		Integer[] randomArray = new Integer[100 + ran.nextInt(900)];
		for(int i = 0 ; i< randomArray.length; i++)
			randomArray[i] = ran.nextInt( (100+(i) * 1000) );
		return randomArray;
	}
	
	private Integer[] createReverseSortedArray(){
		Random ran = new Random();
		Integer[] randomArray = new Integer[100 + ran.nextInt(900)];
		for(int i = 0 ; i< randomArray.length; i++)
			randomArray[i] = 1000000 - (1000 *i) + ran.nextInt( 1000 );
		return randomArray;
	}
	
	private SortTestCase createCase(int caseType) { // 0 = random, 1 = almost sorted, 2 = reverse
		Integer[] testArr; 
		if(caseType ==0)
			testArr = createRandomIntArray();
		else if(caseType ==1 )
			testArr = createAlmostSortedArray();
		else
			testArr = createReverseSortedArray();
		
		//System.out.println("Creating test " + arrayPrinter(testArr));
		Integer[] ansArray = testArr.clone();
		sampleCall(ansArray, ansArray.length); // sort the sucka
		//System.out.println("Sorted " + arrayPrinter(ansArray));
		SortTestCase aCase = new SortTestCase(new Object[]{ testArr, (Integer)testArr.length}, ansArray);
		return aCase;
	}

	
	String arrayPrinter(Integer[] iA){
		String ret = " ";
		for (int i = 0 ; i< iA.length; i++){
			ret+= " "+ iA[i];
		}
		return ret;
	}
	
	public void setProblemName(String problemName) {
		this.problemName = problemName;
	}

	public String getProblemName() {
		return problemName;
	}

	public int getSeedFunctionalityScore() {
		int max = 0;
		for(int i = 0 ; i< testData.length ; i++)
			max += ((Integer[]) testData[i].getAnswer()).length;
		return max;
	}

	public Class[] getMethodParameterTypes() {
		return new Class[] {Integer[].class, Integer.class};
	}

	@Override
	public void setStaticOptimalBias(Individual individual, int optimisationType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getMethodSignature() {
		return "public static void sort(java.lang.Integer[] a,java.lang.Integer length)";
	}
	
	@Override
	public ArrayList<String> getClassNames() {
		return getStrings().getClassNames();
	}
}
