package locoGP.roughwork;

import locoGP.fitness.IndividualEvaluator;
import locoGP.fitness.bytecodeCount.ByteCodeIndividualEvaluator;
import locoGP.individual.Individual;
import locoGP.problems.Problem;
import locoGP.problems.Sort1Optimised;
import locoGP.problems.Sort1Problem;
import locoGP.problems.Sort1QuickProblem;

public class SortFitnessTester {

	/**
	 * Run some variant of sort, see what fitness it is given. 
	 * Written to test whether the system can detect an optimised program.
	 * @param args
	 */
	public static void main(String[] args) {
		// Set up our seed
		//testAProblem( new Sort1Problem(),new Sort1Optimised());
		
		testAProblem(new Sort1QuickProblem(),null);
	}
	
	static void testAProblem(Problem aProblem,Problem optimisedSortProblem){
		// Set up our seed
				//Problem aProblem = new Sort1Problem();
				Individual originalIndividual = new Individual(aProblem);
				//Individual.initialise(originalIndividual.ourProblem.getStrings());
				originalIndividual.gpMaterial.printAll();
				IndividualEvaluator indEval = new ByteCodeIndividualEvaluator();
				indEval.evaluateIndNoTimeLimit(originalIndividual); // This is our reference individual
				originalIndividual.ourProblem.setBaselineRuntimeAvg(originalIndividual.getRuntimeAvg());
				
				// create a variant to compare against
				//Problem optimisedSortProblem = new Sort1Optimised();
				Individual sortVariant = new Individual(originalIndividual, optimisedSortProblem,null);
				indEval.evaluateIndNoTimeLimit(sortVariant);
				System.out.println("Variant "+sortVariant +" "+  sortVariant.getFunctionalityErrorCount()+" "+  sortVariant.getRuntimeAvg());
				System.out.println("timeratio: " + sortVariant.getTimeFitnessRatio()+ " correctness "+ sortVariant.getCorrectness());
				System.out.println("Fitness " + sortVariant.getFitness()+"\n");
				
				/*
				 *	9 May 2014 (commented out 15 Jan 2015)
				 * Added to test multithreaded evaluation,
				 * create a generation of individuals, then evaluate the lot.   
				 * (not needed, bug found in multi-threaded eval, due to programs accessing static methods when collecting results
				 */
				/*for ( int i = 1 ; i < 100 ; i++){
					optimisedSortProblem = new Sort1Optimised();
					sortVariant = new Individual(originalIndividual, optimisedSortProblem,null);
					indEval.evaluateIndNoTimeLimit(sortVariant);
					System.out.println("Variant "+sortVariant +" "+  sortVariant.getFunctionalityScore()+" "+  sortVariant.getRuntimeAvg());
					System.out.println("timeratio: " + sortVariant.getTimeFitnessRatio()+ " correctness "+ sortVariant.getCorrectness());
					System.out.println("Fitness " + sortVariant.getFitness()+"\n");
				}*/
	}

}
