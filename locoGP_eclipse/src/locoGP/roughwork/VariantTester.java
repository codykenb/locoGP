package locoGP.roughwork;

import locoGP.fitness.IndividualEvaluator;
import locoGP.fitness.bytecodeCount.ByteCodeIndividualEvaluator;
import locoGP.fitness.stmtCount.StmtCountIndividualEvaluator;
import locoGP.individual.Individual;
import locoGP.problems.Problem;
import locoGP.problems.Sort1Problem;
import locoGP.problems.crypto.Ascon128V11DecryptProblem;
import locoGP.problems.crypto.Ascon128V11EncryptProblem;
import locoGP.problems.crypto.Ascon128V11NoDepsEncryptProblem;
import locoGP.problems.huffmancodebook.HuffmanCodeBookProblem;
import locoGP.problems.huffmancodebook.HuffmanCodeBookVariant;

public class VariantTester {
	
	/*
	 * Variants of huffmancode book have been generated, we want to test how they are operating
	 * Good for debugging yaw
	 */

	//static IndividualEvaluator indEval = new ByteCodeIndividualEvaluator();
	
	
	/* TODO we should be able to add a execution count statement between every statement of every block
	 * For some reason lines are not being inserted properly. 
	 */
	static IndividualEvaluator indEval = new StmtCountIndividualEvaluator();
	
	public static void main(String args[]){
		
		//testVariant(new HuffmanCodeBookProblem(), new HuffmanCodeBookVariant(218038));
		//compareSeedWithVariant(new Ascon128V11DecryptProblem(), new Ascon128V11DecryptProblem("238276"));
		//compareSeedWithVariant(new Ascon128V11DecryptProblem(), new Ascon128V11DecryptProblem("COpt"));
		//compareSeedWithVariant(new Ascon128V11DecryptProblem(), new Ascon128V11DecryptProblem("_146"));
		
		//compareSeedWithVariant(new Ascon128V11EncryptProblem(), new Ascon128V11EncryptProblem("164610"));
		//compareSeedWithVariant(new Ascon128V11EncryptProblem(), new Ascon128V11EncryptProblem("74304"));
		//compareSeedWithVariant(new Ascon128V11EncryptProblem(), new Ascon128V11EncryptProblem("COpt"));
		
		
		//compareSeedWithVariant(new Ascon128V11EncryptProblem(),new Ascon128V11NoDepsEncryptProblem()); // just testing
		
		//compareSeedWithVariant(new Sort1Problem(), new Sort1Problem("_2575"));
		//compareSeedWithVariant(new Sort1Problem(), new Sort1Problem("_107"));
		//compareSeedWithVariant(new Sort1Problem(), new Sort1Problem("_24"));
		//compareSeedWithVariant(new Sort1Problem(), new Sort1Problem("_74"));
		//compareSeedWithVariant(new Sort1Problem(), new Sort1Problem("129"));
		//compareSeedWithVariant(new Sort1Problem(), new Sort1Problem("Optimised"));
		/*for(int i = 0 ; i< 10 ; i++)
			compareSeedWithVariant(new Sort1Problem(), new Sort1Problem("Bloat"));*/
		compareSeedWithVariant(new Sort1Problem(), new Sort1Problem("_4"));
		//compareSeedWithVariant(new Sort1Problem(), new Sort1Problem("_259"));
	}
	
	public static void compareSeedWithVariant(Problem aProblem, Problem variantProblem){
		
		// create the original seed individual, evaluate
		Individual seedInd = new Individual(aProblem);
		indEval.evaluateIndNoTimeLimit(seedInd);
		seedInd.ourProblem.setBaselineRuntimeAvg(seedInd.getRuntimeAvg());
		
		System.out.println("Seed functionality: " +  seedInd.getFunctionalityErrorCount()+" runtimeAvg: "+  seedInd.getRuntimeAvg());
		System.out.println("Seed Fitness: " +  seedInd.getFitness()+"\n");
		System.out.println("Seed " +  seedInd.ourProblem.getSeedFunctionalityScore()+"\n");
		
		//for(int i = 0 ; i < 10 ; i++)
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
				//indEval.evaluateInd(varInd);
				
				System.out.println("Variant "+variantProblem.getProblemName() +" functionalityScore: "+  varInd.getFunctionalityErrorCount()+" runtimeAvg: "+  varInd.getRuntimeAvg());
				System.out.println("timeratio: " + varInd.getTimeFitnessRatio()+ " correctness "+ varInd.getCorrectness());
				System.out.println("Fitness " + varInd.getFitness()+"\n\n");
				System.out.println("Test Cases: " + varInd.getTestCaseResultsText()+"\n");
				
				
				/*Individual newInd = varInd.clone();
				indEval.evaluateIndNoTimeLimit(newInd);
				
				System.out.println("Variant "+newInd.getClassName() +" functionalityScore: "+  newInd.getFunctionalityErrorCount()+" runtimeAvg: "+  newInd.getRuntimeAvg());
				System.out.println("timeratio: " + newInd.getTimeFitnessRatio()+ " correctness "+ newInd.getCorrectness());
				System.out.println("Fitness " + newInd.getFitness()+"\n\n");
				System.out.println("Test Cases: " + newInd.getTestCaseResultsText()+"\n");
				System.out.println("Test Cases: " + newInd.ASTSet.getCodeListing()+"\n");*/
				
				
	}
	
}
