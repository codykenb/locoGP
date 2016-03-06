package locoGP.experiments;




import java.net.URI;
import javax.tools.SimpleJavaFileObject;

import locoGP.individual.Individual;
import locoGP.problems.Problem;
import locoGP.problems.Sort1Problem;
import locoGP.util.Logger;
import locoGP.*;


/* author: Brendan Cody-Kenny
 Date created:;
 Lines Of Code + Objects + Genetic Programming = locoGP

 This is a re-implementation of a hacked java + bash monster started
 in April 2011
 */
/*	30 July 2013
 *  Experiment to test a static bias overlay with GP
 *  
 *  Create a new version of the seed with a pre-existing (hand generated) bias overlay
 *  Use tournament selection across this bias, inherit bias but do not update it.
 */

class StaticBiasTester {

	public static void main(String args[]) {
		try{
			realMain(args);
		}catch (Exception e){
			e.printStackTrace();
			Logger.flushLog();
		}
	}

	public static void realMain(String args[]) { 
		//TODO finish refactoring gp config, and this main method should be merged with locoGP
		// Config our GP (gen size, num gens, tournament size, pickbestlocation, updateBestLocation)
		//GPConfig gpConfig = new GPConfig( 100, 100, 2, true, false, false );
		GPConfig gpConfig = new GPConfig(); // 1, 0, 2, true,true, false);
		gpConfig.setPopulationSize(100);
		gpConfig.setNumGenerations(100);
		gpConfig.setTournamentSize(2);
		gpConfig.setPickBestLocation(true);
		gpConfig.setUpdateLocationBias(true);
		gpConfig.turnOffDebugLogging();
		Logger.logAll(gpConfig.getExperimentDescription());
			
		Problem aProblem = new Sort1Problem(); 
		
		Individual originalIndividual = new Individual(aProblem);
		
		// change the node probabilities, before the gen is seeded
		originalIndividual.setStaticOptimalBias(2);
		
		Generation currentGen = new Generation(originalIndividual,
				gpConfig, Logger.getLogID());
		
		
		System.out.println(currentGen.originalIndividual.getCodeProbabilitiesLogString());
		// the last individual should have similar biases
		System.out.println(currentGen.individuals.get(currentGen.individuals.size()-1).getCodeProbabilitiesLogString());
				
		Logger.logAll("Our problem is " +currentGen.originalIndividual.ourProblem.getProblemName());
		Logger.logAll("Starting GP ------------------------- GP_Start pop: "
				+ gpConfig.getPopulationSize() + " Gens: " + gpConfig.getNumGenerations() + " Tourney: "
				+ gpConfig.getTournamentSize() + " numTests: "
				+ currentGen.originalIndividual.ourProblem.getNumTests());
		Logger.logAll("Generation: " + currentGen.getGenerationCount());
		currentGen.writeCurrentPopulationDetails();
		
		Generation nextGen;
		for (int i = currentGen.getGenerationCount(); i <= gpConfig.getNumGenerations() ; i++) {
			Logger.flushLog();
			Logger.logAll("Generation: " + currentGen.getGenerationCount());
			nextGen = new Generation(currentGen);			
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


