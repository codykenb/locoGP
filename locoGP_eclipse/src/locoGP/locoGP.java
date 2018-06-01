package locoGP;

// Brendan Cody-Kenny <codykenny@gmail.com>

import locoGP.experiments.GPConfig;
import locoGP.fitness.IndividualEvaluator;
import locoGP.fitness.bytecodeCount.ByteCodeIndividualEvaluator;
import locoGP.fitness.runTimer.RunTimerIndividualEvaluator;
import locoGP.fitness.stmtCount.StmtCountIndividualEvaluator;
import locoGP.individual.Individual;
import locoGP.operators.BiasedNodeSelector;
import locoGP.operators.GPMaterialVisitor;
import locoGP.operators.StatementOnlyCrossoverOperator;
import locoGP.operators.NodeSelector;
import locoGP.problems.Problem;

import locoGP.problems.Sort1Problem; // bubblesort
import locoGP.problems.Sort1CocktailProblem; // depending on the problem you select
import locoGP.problems.Sort1HeapProblem;
import locoGP.problems.Sort1InsertionProblem;
import locoGP.problems.Sort1MergeProblem;
import locoGP.problems.Sort1ProblemVariantSeed;
import locoGP.problems.Sort1QuickProblem;
import locoGP.problems.Sort1RadixProblem;
import locoGP.problems.Sort1Selection2Problem;
import locoGP.problems.Sort1SelectionProblem;
import locoGP.problems.Sort1ShellProblem;
import locoGP.problems.crypto.Ascon128V11COptDecryptProblem;
import locoGP.problems.crypto.Ascon128V11COptEncryptProblem;
import locoGP.problems.crypto.Ascon128V11DecryptProblem;
import locoGP.problems.crypto.Ascon128V11EncryptProblem;
import locoGP.problems.crypto.Ascon128V11NoDepsEncryptProblem;
import locoGP.problems.huffmancodebook.HuffmanCodeBookProblem;
import locoGP.problems.scalablesort.Sort1CloneProblem;
import locoGP.problems.scalablesort.Sort1LoopCloneProblem;
import locoGP.problems.scalablesort.Sort1LoopsProblem;
import locoGP.problems.scalablesort.Sort1SegIncProblem;
import locoGP.problems.scalablesort.Sort1SegmentedCloneProblem;

import locoGP.util.Logger;

public class locoGP {

	public static void main(String args[]) {
		try{
			realMain(args);
		}catch (Exception e){
			e.printStackTrace();
			Logger.flushLog();
		}
	}
	
	public static Generation createFirstGeneration(GPConfig gpConfig){
		Problem aProblem;
		/* Fischer-yates shuffle
		 * Timsort, Runsort
		 * Dual-pivot quicksort
		 */
		
		//aProblem = new Sort1RadixProblem(); // Hardest sort to improve on
		//aProblem = new Sort1Selection2Problem();
		//aProblem = new Sort1SelectionProblem();
		//aProblem = new Sort1ShellProblem();
		//aProblem = new Sort1MergeProblem();
		//aProblem = new Sort1InsertionProblem();
		
		//aProblem = new Sort1HeapProblem();
		//aProblem = new Sort1QuickProblem();
		//aProblem = new Sort1CocktailProblem();		
		
		//aProblem = new Sort1Problem();
		
		//aProblem = new Sort1CloneProblem(1);
		//aProblem = new Sort1SegmentedCloneProblem(2);
		//aProblem = new Sort1SegIncProblem(2);
		//aProblem = new Sort1LoopCloneProblem(1);
		//aProblem = new Sort1LoopsProblem(1);
		
		//aProblem = new Sort1ProblemVariantSeed();
		//aProblem = new HuffmanCodeBookProblem();
		
		aProblem = new Ascon128V11DecryptProblem(); // crypto! new hardest problem
		//aProblem = new Ascon128V11COptDecryptProblem();
		//aProblem = new Ascon128V11COptEncryptProblem();
		//aProblem = new Ascon128V11EncryptProblem(); 

		//aProblem = new Ascon128V11NoDepsEncryptProblem(); // Not working 
		
		Individual originalIndividual = new Individual(aProblem);
		Individual.ourProblem.setConfig(gpConfig); 
		
		//aProblem.setStaticOptimalBias(originalIndividual, 2);
		//aProblem.setStaticOptimalBias(originalIndividual, 3); // profiler bias
		//aProblem.setStaticOptimalBias(originalIndividual, 4); // 4 is R15tiny accumulated bias
			
		return new Generation(originalIndividual, gpConfig, Logger.getLogID());
		
	}

	public static void realMain(String args[]) {
		GPConfig gpConfig = new GPConfig();
		//gpConfig.setElitismRate(.02); // default is .3 (30% are replaced)
		gpConfig.xoverOperator = new StatementOnlyCrossoverOperator(); // TODO is defining operators as objects a good idea?
		
		gpConfig.setPopulationSize(100);
		gpConfig.setNumGenerations(1000);
		gpConfig.setTournamentSize(2);
		gpConfig.setEvaluator(new ByteCodeIndividualEvaluator());
		//gpConfig.setEvaluator(new StmtCountIndividualEvaluator());
		//gpConfig.setEvaluator(new RunTimerIndividualEvaluator());
		
		gpConfig.setPickBestLocation(true);
		GPMaterialVisitor.nodeSelector = new BiasedNodeSelector();
		
		//gpConfig.biasUpdater = new BiasUpdaterR45();
		gpConfig.setUpdateLocationBias(true);
		gpConfig.setThreadPoolSize(7);
		gpConfig.turnOffDebugLogging(); //gpConfig.setThreadPoolSize(4);
		//gpConfig.setThreadPoolSize(1);
		//gpConfig.setSeedBiasFromPerfRedFreqOverCompFreq();
		gpConfig.setSeedBiasFromDeletionAnalysis();
		gpConfig.setLinkNewChildBiasDataWithParent();
		 
		//gpConfig.setReferenceChildBiasToParentData(); // affects how bias data is referenced when cloning individuals
		//gpConfig.setUseDiverseElitismFine(); // which elitism is best for ascon? we have some programs which are partially correct on different input!
		gpConfig.setUseDiverseElitism();
		GPConfig.setFineGranularityChange(true);
		
		Logger.logAll(gpConfig.getExperimentDescription());
		gpConfig.checkDebug();
		Logger.flushLog();
		Generation currentGen = createFirstGeneration(gpConfig);
		
		Logger.logJavaFile("Seed-"+currentGen.originalIndividual.getClassName(), currentGen.originalIndividual.ASTSet.getCodeListing() );
		Logger.logBiasFile("Seed-"+currentGen.originalIndividual.getClassName(),0 , currentGen.originalIndividual.getCodeProbabilities() );
		
		Logger.logDebugConsole(currentGen.originalIndividual.getCodeProbabilitiesLogString());
		//Logger.logDebugConsole(currentGen.individuals.get(currentGen.individuals.size()-1).getCodeProbabilitiesLogString());
		Logger.logAll("Our problem is " +currentGen.originalIndividual.ourProblem.getProblemName());
		Logger.logAll("Starting GP ------------------------- GP_Start "+gpConfig.getConfigDetailsString()+ " numTests: "
				+ currentGen.originalIndividual.ourProblem.getNumTests());
		Logger.logAll("Generation: " + currentGen.getGenerationCount());
		
		currentGen.writeCurrentPopulationDetails();  
		
		Generation nextGen;
		for (int i = currentGen.getGenerationCount(); i < gpConfig.getNumGenerations()  ; i++) {
			Logger.flushLog();
			Logger.logAll("Generation: " + currentGen.getGenerationCount());
			nextGen = new Generation(currentGen);		
			//currentGen.clearBacklinks(nextGen);
			currentGen = nextGen;
		}
		
		//currentGen.getOurIndEval().shutdownAllThreads();
		Logger.logAll("End GP ------------------------- GP_End pop: "
				+ gpConfig.getPopulationSize() + " Gens: " + gpConfig.getNumGenerations() + " Tourney: "
				+ gpConfig.getTournamentSize());
		Logger.flushLog();
		System.exit(0);
	}	
}







































































































































































































































































































































































































































































































































































































































































































































































































































































































