package locoGP.roughwork;

import locoGP.fitness.IndividualEvaluator;
import locoGP.individual.Individual;
import locoGP.problems.Problem;
import locoGP.problems.huffmancodebook.HuffmanCodeBookProblem;
import locoGP.problems.huffmancodebook.HuffmanCodeBookVariant;

public class VariantTester {
	
	/*
	 * Variants of huffmancode book have been generated, we want to test how they are operating
	 * Good for debugging yaw
	 */

	static IndividualEvaluator indEval = new IndividualEvaluator();
	
	public static void main(String args[]){
		compareSeedWithVariant(new HuffmanCodeBookProblem(), new HuffmanCodeBookVariant(218038));
	}
	
	public static void compareSeedWithVariant(Problem aProblem, Problem variantProblem){
		
		// create the original seed individual, evaluate
		Individual seedInd = new Individual(aProblem);
		indEval.evaluateIndNoTimeLimit(seedInd);
		seedInd.ourProblem.setBaselineRuntimeAvg(seedInd.getRuntimeAvg());
		
		System.out.println("Seed " +  seedInd.getFunctionalityScore()+" "+  seedInd.getRuntimeAvg());
		System.out.println("Seed " +  seedInd.getFitness()+"\n");
		
		checkVariant(seedInd, variantProblem);
		//checkVariant(seedInd, 226);
		//checkVariant(seedInd, 5223);
		//checkVariant(seedInd, 165);
		//checkVariant(seedInd, 86);
		
		// GP112-huffbook-noBias-rare50Elite-30Xo/rep50-vm-63-0-41-20140812215602
		//checkVariant(seedInd, 58912);
		
		///home/bck/GP/archive/GP116-huffbook-noBias/rep50-lg12l10-20140830022522/huffmanCodeTable.BasicHuffman58856.java
		//checkVariant(seedInd, 58856);
		
		///home/bck/GP/archive/GP117-huffbook-noBias/rep50-lg12l5-20140901221118/huffmanCodeTable.BasicHuffman20907.java
		//checkVariant(seedInd,20907);
		
		
	}
	
	public static void checkVariant(Individual seedInd, Problem variantProblem){
		// Load the variant into an individual, evaluate
				
				Individual varInd = new Individual(seedInd, variantProblem, null);
				
				indEval.evaluateIndNoTimeLimit(varInd);
				
				System.out.println("Variant "+variantProblem.getProblemName() +" "+  varInd.getFunctionalityScore()+" "+  varInd.getRuntimeAvg());
				System.out.println("timeratio: " + varInd.getTimeFitnessRatio()+ " correctness "+ varInd.getCorrectness());
				System.out.println("Fitness " + varInd.getFitness()+"\n");
	}
	
}
