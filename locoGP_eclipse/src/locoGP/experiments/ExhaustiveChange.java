package locoGP.experiments;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

import locoGP.*;
import locoGP.fitness.IndividualEvaluator;
import locoGP.fitness.bytecodeCount.ByteCodeIndividualEvaluator;
import locoGP.fitness.bytecodeCount.IndEvaluatorThread;
import locoGP.individual.Individual;
import locoGP.operators.GPMaterialVisitor;
import locoGP.operators.NodeOperators;
import locoGP.problems.*;
import locoGP.problems.crypto.Ascon128V11DecryptProblem;
import locoGP.problems.crypto.Ascon128V11EncryptProblem;
import locoGP.problems.huffmancodebook.HuffmanCodeBookProblem;
import locoGP.problems.scalablesort.Sort1LoopsProblem;
import locoGP.util.Logger;

public class ExhaustiveChange {
	/*
	 * For every location, and every possible replacement, go through all
	 * combinations see what the range of effects are on the fitness,
	 * performance and functionality
	 */
	private static IndividualEvaluator ourIndEval = new ByteCodeIndividualEvaluator();

	public static void main(String[] args) {
		
		/*mutateSelectedElements();*/
		
		if(args[0] != null ){
			bruteForceMutate(args[0]);
		}else{
			System.out.println("Missing experiment argument (0-12)");
		}		
	}
	

	private static Individual replaceNodeAtIndex(Individual originalIndividual, int indexToReplace, int replacementIndex) {
		Individual indClone =originalIndividual.clone(); 
		List<ASTNode> seedNodes = originalIndividual.gpMaterial.getAllAllowedNodes();
		ASTNode nodeToReplace,replacementNode = null;
		replacementNode = seedNodes.get(replacementIndex); // "i" 
		nodeToReplace = indClone.gpMaterial.getAllAllowedNodes().get(indexToReplace); // "1"
		
		NodeOperators.replaceNode(nodeToReplace, replacementNode);
		
		System.out.println(indClone.ASTSet.getCodeListing());
		
		if(ourIndEval.evaluateInd(indClone)){
			System.out.println("Replacing " +indexToReplace+" with " +replacementIndex + " "+ indClone.getClassName() 
					+ " Time: "+indClone.getRunningTime()+" Fit: " + indClone.getFitness()
					+ " TestError: " + indClone.getFunctionalityErrorCount()+ " ASTNodes: " + indClone.getNumNodes() 
					+ " Replaced: 1" + " Compiled: 1");
		}else{
/*			Logger.log("Replacing " +indexToReplace+" with " +replacementIndex + " "+ indClone.getClassName() 
					+ " Time: "+indClone.getRunningTime()+" Fit: " + indClone.getFitness()
					+ " TestError: " + indClone.getFunctionalityScore()+ " ASTNodes: " + indClone.getNumNodes() 
					+ " Replaced: 1" + " Compiled: 0");*/
			System.out.println("Replacing " +indexToReplace+" with " +replacementIndex + " "+ indClone.getClassName() 
					+ " Time: "+indClone.getRunningTime()+" Fit: " + indClone.getFitness()
					+ " TestError: " + indClone.getFunctionalityErrorCount()+ " ASTNodes: " + indClone.getNumNodes() 
					+ " Replaced: 1" + " Compiled: 0");
		}
		return indClone;
	}

	private static Problem getProblem(int i) {
		Problem[] problems = {new Sort1Problem(), new Sort1HeapProblem(),
				new Sort1ShellProblem(),new Sort1QuickProblem(),
				new Sort1MergeProblem(),new Sort1CocktailProblem(),
				new Sort1RadixProblem(),new Sort1InsertionProblem(),
				new Sort1SelectionProblem(),new Sort1Selection2Problem(),
				new Sort1LoopsProblem(1),new HuffmanCodeBookProblem(),
				new Ascon128V11DecryptProblem(),new Ascon128V11EncryptProblem()};
		return problems[i];
	}
	
	private static Individual getIndividual(int i){
		Individual originalIndividual = new Individual(getProblem(i));
		// taking the two locations in bubblesort which we know have to change
		List<ASTNode> seedNodes = originalIndividual.gpMaterial.getAllAllowedNodes();

		Individual indClone = null;
		ASTNode nodeToReplace, replacementNode = null;
		ourIndEval.evaluateIndNoTimeLimit(originalIndividual); // This is our
																// reference
																// individual
		originalIndividual.ourProblem.setBaselineRuntimeAvg(originalIndividual
				.getRuntimeAvg());
		return originalIndividual;
	}

	private static void bruteForceMutate(String arg) {
		int expNum = Integer.parseInt(arg);
		/*
		Problem[] problems = {new Sort1Problem(), new Sort1HeapProblem(),
				new Sort1ShellProblem(),new Sort1QuickProblem(),
				new Sort1MergeProblem(),new Sort1CocktailProblem(),
				new Sort1RadixProblem(),new Sort1InsertionProblem(),
				new Sort1SelectionProblem(),new Sort1Selection2Problem(),
				new Sort1LoopsProblem(1),new HuffmanCodeBookProblem(),
				new Ascon128V11Problem()};*/
		
		Problem ourProblem = getProblem(expNum);
				
		Logger.newLogFile(ourProblem.getProblemName());
		
		bruteForceMutation(ourProblem);
		
	}

	public static void bruteForceMutation(Problem aProblem){
		/*
		 * Create a variant
		 * Record replacement
		 * Evaluate variant
		 * Log vals
		 */
		
		GPConfig.turnOffDebugLogging();
		GPConfig.setFineGranularityChange(true);

		Individual originalIndividual = new Individual(aProblem);

		// taking the two locations in bubblesort which we know have to change
		List<ASTNode> seedNodes = originalIndividual.gpMaterial.getAllAllowedNodes();

		Individual indClone = null;
		ASTNode nodeToReplace,replacementNode = null;
		
		
		ourIndEval.evaluateIndNoTimeLimit(originalIndividual); // This is our reference individual
		originalIndividual.ourProblem.setBaselineRuntimeAvg(originalIndividual.getRuntimeAvg());
		/*
		originalIndividual = null;
		System.gc();*/
		
		// for each node in the program
		for (int i = 0; i < seedNodes.size(); i++) {
			replacementNode = seedNodes.get(i); // pick a node from the seed
			for (int j = 0; j < seedNodes.size(); j++) { // go through clone,
															// and try that node
															// at every location
				// exclude replacement of nodes at the same index, and nodes
				// which are identical
				if ((j != i && !seedNodes.get(j).toString()
						.equals(seedNodes.get(i)))
						|| (j == 0 && i == 0)) { // replace the first node with
													// itself, to ensure
													// modification and
													// compilation are working
					indClone = new Individual(originalIndividual); // .clone();
																	// // we
																	// should
																	// only do
																	// this once
																	// we know
																	// the nodes
																	// are
																	// compatible,
																	// would
																	// speed
																	// things up
																	// a bit..
					// indClone = null;
					// System.out.println("indCone: "+indClone);
					GPMaterialVisitor gpM = indClone.gpMaterial;
					List<ASTNode> gpMAN = gpM.getAllAllowedNodes();
					nodeToReplace = gpMAN.get(j);

					if (NodeOperators.replaceNode(nodeToReplace,
							replacementNode) != null) {

						if (ourIndEval.evaluateInd(indClone)) {
							Logger.log("Replacing " + j + " with " + i + " "
									+ indClone.getClassName() + " Time: "
									+ indClone.getRunningTime() + " Fit: "
									+ indClone.getFitness() + " TestError: "
									+ indClone.getFunctionalityErrorCount()
									+ " ASTNodes: " + indClone.getNumNodes()
									+ " Replaced: 1" + " Compiled: 1"
									+ " testResults:"+indClone.getTestCaseResultsText());
						} else {
							Logger.log("Replacing " + j + " with " + i + " "
									+ indClone.getClassName() + " Time: "
									+ indClone.getRunningTime() + " Fit: "
									+ indClone.getFitness() + " TestError: "
									+ indClone.getFunctionalityErrorCount()
									+ " ASTNodes: " + indClone.getNumNodes()
									+ " Replaced: 1" + " Compiled: 0"
									+ " testResults:"+indClone.getTestCaseResultsText());
						}
					} else {

						Logger.log("Replacing " + j + " with " + i + " "
								+ indClone.getClassName() + " Time: "
								+ indClone.getRunningTime() + " Fit: "
								+ indClone.getFitness() + " TestError: "
								+ indClone.getFunctionalityErrorCount()
								+ " ASTNodes: " + indClone.getNumNodes()
								+ " Replaced: 0"+ " Compiled: 0"
								+ " testResults:"+indClone.getTestCaseResultsText());
					}
					/*+ " GPNodes: " 	+ indClone.getNumGPNodes()
					+ " xoverApplied: " 	+ indClone.crossoverApplied()
					+ " xovers: " 	+ indClone.getCrossoverAttempts()
					+ " mutationApplied: " 	+ indClone.mutationApplied()
					+ " mutations: " 	+ indClone.getMutationAttempts()*/
					indClone.setNullRefs();
					indClone = null; // GC doesn't appear to be getting rid of these :/
					Logger.flushLog();
					System.gc();
				}
			}
			//Logger.flushLog();
		}
		
		
		// For each location 
			// for each possible node replacement
			// generate a program and evaluate
		
		/*Generation currentGen;
		for (int i = 0; i < gpConfig.numGenerations; i++) {
			// mutate it over and over
			originalIndividual.clearRuntime();
			Logger.logAll("Generation: " + i*gpConfig.populationSize);
			currentGen = new Generation(originalIndividual, gpConfig, Logger.getLogID());
			currentGen.saveCurrentPopulationDetails();
			Logger.logJavaFile("Seed-"+originalIndividual.getClassName()+(i*gpConfig.populationSize),
					originalIndividual.ASTSet.getCodeListing() );
			//Logger.logJavaFile(tempInd.getClassName(), tempInd.ASTSet.getCodeListing() );
			Logger.logBiasFile("Seed-"+originalIndividual.getClassName()+(i*gpConfig.populationSize),
					i, originalIndividual.getCodeProbabilities() );
			Logger.flushLog();
		}*/
		
		
		/*private static void mutateSelectedElements() {
		Integer[] sortArr = {12,4,6,2,3,5,6,45,5};
		blarghSort(sortArr, sortArr.length);		
		Individual originalIndividual = getIndividual(12);
		Individual returned = replaceNodeAtIndex(originalIndividual,5,21);
		replaceNodeAtIndex(returned,21,10);
	}
	
	

	private static Integer[]  blarghSort(Integer[] a, Integer length) {
	    for (int i=0; i < length; i++) {
	      for (int j=0; j < length - i; j++) {
	        if (a[j] > a[j + 1]) {
	          int k=a[j];
	          a[j]=a[j + 1];
	          a[j + 1]=k;
	        }
	      }
	    }
	    return a;
	}

	private static Individual replaceNodeAtIndex(Individual originalIndividual, int indexToReplace, int replacementIndex) {
		Individual indClone =originalIndividual.clone(); 
		List<ASTNode> seedNodes = originalIndividual.gpMaterial.getAllAllowedNodes();
		ASTNode nodeToReplace,replacementNode = null;
		replacementNode = seedNodes.get(replacementIndex); // "i" 
		nodeToReplace = indClone.gpMaterial.getAllAllowedNodes().get(indexToReplace); // "1"
		
		NodeModifierUtil.replaceNode(nodeToReplace, replacementNode);
		
		System.out.println(indClone.ASTSet.getCodeListing());
		
		if(ourIndEval.evaluateInd(indClone)){
			System.out.println("Replacing " +indexToReplace+" with " +replacementIndex + " "+ indClone.getClassName() 
					+ " Time: "+indClone.getRunningTime()+" Fit: " + indClone.getFitness()
					+ " TestError: " + indClone.getFunctionalityErrorCount()+ " ASTNodes: " + indClone.getNumNodes() 
					+ " Compiled: 1" 
					+ " testResults:"+indClone.getTestCaseResultsText());
		}else{
			Logger.log("Replacing " +indexToReplace+" with " +replacementIndex + " "+ indClone.getClassName() 
					+ " Time: "+indClone.getRunningTime()+" Fit: " + indClone.getFitness()
					+ " TestError: " + indClone.getFunctionalityScore()+ " ASTNodes: " + indClone.getNumNodes() 
					+ " Replaced: 1" + " Compiled: 0");
			System.out.println("Replacing " +indexToReplace+" with " +replacementIndex + " "+ indClone.getClassName() 
					+ " Time: "+indClone.getRunningTime()+" Fit: " + indClone.getFitness()
					+ " TestError: " + indClone.getFunctionalityErrorCount()+ " ASTNodes: " + indClone.getNumNodes() 
					+ " Compiled: 0" 
					+ " testResults:"+indClone.getTestCaseResultsText());
		}
		return indClone;
	}*/
		
	}

}
