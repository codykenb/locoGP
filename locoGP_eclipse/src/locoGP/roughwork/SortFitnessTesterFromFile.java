package locoGP.roughwork;

import locoGP.experiments.GPConfig;
import locoGP.fitness.IndividualEvaluator;
import locoGP.fitness.bytecodeCount.ByteCodeIndividualEvaluator;
import locoGP.individual.Individual;
import locoGP.problems.Problem;
import locoGP.problems.Sort1CocktailProblem;
import locoGP.problems.Sort1InsertionProblem;
import locoGP.problems.Sort1MergeProblem;
import locoGP.problems.Sort1Quick22387;
import locoGP.problems.Sort1QuickProblem;
import locoGP.problems.Sort1RadixProblem;
import locoGP.problems.Sort1Selection2Problem;
import locoGP.problems.Sort1SelectionProblem;
import locoGP.problems.SortVariant;
import locoGP.util.Logger;

public class SortFitnessTesterFromFile {

	/**
	 * @param args
	 */
	
	private static IndividualEvaluator ourIndEval = new ByteCodeIndividualEvaluator();
	
	public static void main(String[] args) {
		
		GPConfig.turnOffDebugLogging();
		
		testFile(new Sort1MergeProblem(),
				new SortVariant("/locoGP/problems/Sort1Merge5281.txt","Sort1Merge5281"));

		
		/*testFile(new Sort1RadixProblem(),
				new SortVariant("/locoGP/problems/Sort1Radix27404.txt","Sort1Radix27404"));
*/
		
/*		testFile(new Sort1SelectionProblem(),
				new SortVariant("/locoGP/problems/Sort1SelectionProblem132.txt","Sort1SelectionProblem132"));
*/
		
		//../optimisedFiles/Sort1Insertion26118.java  ../optimisedFiles/Sort1Insertion30129.java  ../optimisedFiles/Sort1Insertion33991.java

		/*testFile(new Sort1InsertionProblem(),
				new SortVariant("/locoGP/problems/Sort1Insertion30129.txt","Sort1Insertion30129"));
*/
		
		/*testFile(new Sort1CocktailProblem(),
				new SortVariant("/locoGP/problems/Sort1Cocktail4687.txt","Sort1Cocktail4687"));
*/
/*		testFile(new Sort1Selection2Problem(),
				new SortVariant("/locoGP/problems/Sort1Selection2Problem19584.txt","Sort1Selection2Problem19584"));
*/
/*		testFile(new Sort1QuickProblem(),
				new SortVariant("/locoGP/problems/Sort1Quick22387.variant.txt","Sort1Quick22387"));
*/
	}
	
	public static void testFile(Problem seedProg, Problem variantProg){
	// eval seed
		Individual originalIndividual = new Individual(seedProg);
		ourIndEval.evaluateIndNoTimeLimit(originalIndividual);
		originalIndividual.ourProblem.setBaselineRuntimeAvg(originalIndividual
			.getRuntimeAvg());
	
	// eval variant 
		Individual variantInd = new Individual(variantProg);
	ourIndEval.evaluateInd(variantInd);
		System.out.println( variantInd.getClassName() 
				+ " Time: "+variantInd.getRunningTime()+" Fit: " + variantInd.getFitness()
				+ " TestError: " + variantInd.getFunctionalityErrorCount()+ " ASTNodes: " + variantInd.getNumNodes() 
				+ " Replaced: 1" + " Compiled: 1"
				);
	
	
	}
}
