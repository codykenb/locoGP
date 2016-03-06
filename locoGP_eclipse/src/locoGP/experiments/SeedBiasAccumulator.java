package locoGP.experiments;

import locoGP.*;
import locoGP.individual.Individual;
import locoGP.operators.GPASTNodeData;
import locoGP.problems.*;
import locoGP.problems.huffmancodebook.HuffmanCodeBookProblem;
import locoGP.problems.scalablesort.Sort1LoopsProblem;
import locoGP.util.Logger;

public class SeedBiasAccumulator {
/*
 * test how bias emerges on the seed program only
 * 
 */
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		SeedBiasAccumulator();
		
		//SeedPrinter();
		
	}
	
	public static void SeedPrinter(){
		Problem[] ourProblems = { new Sort1HeapProblem(),
				new Sort1ShellProblem(), new Sort1QuickProblem(),
				new Sort1MergeProblem(), new Sort1CocktailProblem(),
				new Sort1RadixProblem(), new Sort1InsertionProblem(), new Sort1InsertionProblem(),
				new Sort1Selection2Problem(),new Sort1SelectionProblem(),
				new Sort1Problem(),new Sort1LoopsProblem(1) , new HuffmanCodeBookProblem()};
		for (Problem aProblem : ourProblems){
			Individual originalIndividual = new Individual(aProblem);
			Logger.logJavaFile("Seed-"+originalIndividual.getClassName(), originalIndividual.ASTSet.getCodeListing() );
		}

	}
	
	public static void SeedBiasAccumulator(){
		
		// int populationSize, int numGenerations, int tournamentSize, boolean pickBestLocation, boolean updateLocationBias
		//GPConfig gpConfig = new GPConfig( 5, 250, 2, true,true, false );
		GPConfig gpConfig = new GPConfig(); // 1, 0, 2, true,true, false);
		gpConfig.setPopulationSize(5);
		gpConfig.setNumGenerations(250);
		gpConfig.setTournamentSize(2);
		gpConfig.setPickBestLocation(true);
		gpConfig.setUpdateLocationBias(true);
		gpConfig.turnOffDebugLogging();
		
		//Problem aProblem = new Sort1HeapProblem();
		//Problem aProblem = new Sort1ShellProblem();
		//Problem aProblem = new Sort1QuickProblem();
		//Problem aProblem = new Sort1MergeProblem();
		 //Problem aProblem = new Sort1CocktailProblem();

		//Problem aProblem = new Sort1RadixProblem();
		//Problem aProblem = new Sort1InsertionProblem();
		// Problem aProblem = new Sort1SelectionProblem();
		//Problem aProblem = new Sort1Selection2Problem();

		Problem aProblem = new Sort1Problem();

		//Problem aProblem = new Sort1CloneProblem(1);
		// Problem aProblem = new Sort1SegmentedCloneProblem(2);
		// Problem aProblem = new Sort1SegIncProblem(2);
		// Problem aProblem = new Sort1LoopCloneProblem(1);
		// Problem aProblem = new Sort1LoopsProblem(1);

		// Problem aProblem = new Sort1ProblemVariantSeed();
		//Problem aProblem = new HuffmanCodeBookProblem();

		//Problem aProblem = new Sort1LoopsProblem(1);
		
		//Problem aProblem = new HuffmanCodeBookProblem();
		Individual originalIndividual = new Individual(aProblem);
		//aProblem.setStaticOptimalBias(originalIndividual, 2);
		
		Generation currentGen;
		for (int i = 0; i < gpConfig.getNumGenerations(); i++) {
			// mutate it over and over
			originalIndividual.clearRuntime();
			Logger.logJavaFile("Seed-"+originalIndividual.getClassName()+(i*gpConfig.getPopulationSize()),
					originalIndividual.ASTSet.getCodeListing() );
			Logger.logAll("Generation: " + i*gpConfig.getPopulationSize());
			currentGen = new Generation(originalIndividual, gpConfig, Logger.getLogID());
			currentGen.writeCurrentPopulationDetails();
			//Logger.logJavaFile(tempInd.getClassName(), tempInd.ASTSet.getCodeListing() );
			Logger.logBiasFile("Seed-"+originalIndividual.getClassName()+(i*gpConfig.getPopulationSize()),
					i, originalIndividual.getCodeProbabilities() );
			Logger.flushLog();
		}
	}

}
