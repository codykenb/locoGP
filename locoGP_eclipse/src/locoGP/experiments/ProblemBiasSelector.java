package locoGP.experiments;

// Brendan Cody-Kenny <codykenny@gmail.com>
 

import locoGP.Generation;
import locoGP.experiments.GPConfig;
import locoGP.individual.Individual;
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
import locoGP.problems.huffmancodebook.HuffmanCodeBookProblem;
import locoGP.problems.scalablesort.Sort1CloneProblem;
import locoGP.problems.scalablesort.Sort1LoopCloneProblem;
import locoGP.problems.scalablesort.Sort1LoopsProblem;
import locoGP.problems.scalablesort.Sort1SegIncProblem;
import locoGP.problems.scalablesort.Sort1SegmentedCloneProblem;

import locoGP.util.Logger;

public class ProblemBiasSelector {

	public static void main(String args[]) {
		try{
			realMain(args);
		}catch (Exception e){
			e.printStackTrace();
			Logger.flushLog();
		}
	}
	
	public static Generation createFirstGeneration(GPConfig gpConfig, String arg){
		
		int expNum = Integer.parseInt(arg);
		
		Problem[] problems = {new Sort1Problem(), new Sort1HeapProblem(),
				new Sort1ShellProblem(),new Sort1QuickProblem(),
				new Sort1MergeProblem(),new Sort1CocktailProblem(),
				new Sort1RadixProblem(),new Sort1InsertionProblem(),
				new Sort1SelectionProblem(),new Sort1Selection2Problem(),
				new Sort1LoopsProblem(1),new HuffmanCodeBookProblem()};
		
		//Problem aProblem = new Sort1RadixProblem();
		//Problem aProblem = new Sort1Selection2Problem();
		//Problem aProblem = new Sort1SelectionProblem();
		//Problem aProblem = new Sort1ShellProblem();
		//Problem aProblem = new Sort1MergeProblem();
		//Problem aProblem = new Sort1InsertionProblem();
		
		//Problem aProblem = new Sort1HeapProblem();
		//Problem aProblem = new Sort1QuickProblem();
		//Problem aProblem = new Sort1CocktailProblem();		
		
		//Problem aProblem = new Sort1Problem();
		
		//Problem aProblem = new Sort1CloneProblem(1);
		//Problem aProblem = new Sort1SegmentedCloneProblem(2);
		//Problem aProblem = new Sort1SegIncProblem(2);
		//Problem aProblem = new Sort1LoopCloneProblem(1);
		//Problem aProblem = new Sort1LoopsProblem(1);
		
		//Problem aProblem = new Sort1ProblemVariantSeed();
		//Problem aProblem = new HuffmanCodeBookProblem();
		Individual originalIndividual = new Individual(problems[expNum]);
		
		//aProblem.setStaticOptimalBias(originalIndividual, 2);
		//aProblem.setStaticOptimalBias(originalIndividual, 3);
		//aProblem.setStaticOptimalBias(originalIndividual, 4); // 4 is R15tiny accumulated bias
		
		return new Generation(originalIndividual, gpConfig, Logger.getLogID());
		
	}

	public static void realMain(String args[]) {
		//             popSize, numGens, tournSize, pickBestLocation, updateLocationBias, debug
		GPConfig gpConfig = new GPConfig(); // 1, 0, 2, true,true, false);
		gpConfig.setPopulationSize(1);
		gpConfig.setNumGenerations(0);
		gpConfig.setTournamentSize(2);
		gpConfig.setPickBestLocation(true);
		gpConfig.setUpdateLocationBias(true);
		gpConfig.turnOffDebugLogging();
		int delNum = Integer.parseInt(args[1]); // can be 0, 1 
		int biasNum = Integer.parseInt(args[2]); // can be 0, 1, 2
		if (delNum == 1)
			gpConfig.setSeedBiasFromDeletionAnalysis();
		if (biasNum == 1 )
			gpConfig.setExhaustiveMutateFirstGen(); // the initial generation will be created from sensitivity analysis
		else if(biasNum == 2)
			gpConfig.setSeedBiasFromPerfRedFreqOverCompFreq(); // initial gen created from exeRedFreq / compileFreq
		else if(biasNum == 3)
			gpConfig.updateSeedBiasFromPerfRedFreqOverCompFreq();
		
		Logger.logAll(gpConfig.getExperimentDescription());
		
		Generation currentGen = createFirstGeneration(gpConfig, args[0]);
		
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