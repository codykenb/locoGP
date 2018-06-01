package locoGP.problems;

import java.util.ArrayList;

import locoGP.experiments.GPConfig;
import locoGP.individual.Individual;
import locoGP.problems.tests.TestCase;



public abstract class Problem implements java.io.Serializable{
	/*
	 * TODO Structure this properly so we can slot in a range of new problems
	 * easily.
	 */

	private static float baselineRuntimeAvg;
	public static GPConfig gpConfig = new GPConfig(); // default
	
	public abstract String getEntryClassName();

	public abstract String getMethodName();

	public abstract CompilationSet getStrings();

	public abstract TestCase[] getTestData();

	public abstract int getNumTests();
	
	public abstract int getSeedFunctionalityScore();

	public abstract int getWorstFunctionalityScore();

	public abstract void setProblemName(String problemName);

	public abstract String getProblemName();

	//getMethodParameterTypes // public abstract Class[] getClassParams(); ?
	public abstract Class[] getMethodParameterTypes();

	public abstract void setStaticOptimalBias(Individual individual, int optimisationType);

	public abstract String getMethodSignature();
	
	public float getBaselineRuntimeAvg(){
		return baselineRuntimeAvg;
	}

	public void setBaselineRuntimeAvg(float bRA){
		Problem.baselineRuntimeAvg = bRA;
	}

	public abstract ArrayList<String> getClassNames();

	public void setConfig(GPConfig gpConfig) {
		Problem.gpConfig = gpConfig;
	}

	public abstract int getMissingTestCaseValue(int i);
}
