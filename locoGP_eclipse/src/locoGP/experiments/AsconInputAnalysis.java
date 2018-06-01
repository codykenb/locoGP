package locoGP.experiments;

import locoGP.fitness.IndividualEvaluator;
import locoGP.fitness.bytecodeCount.ByteCodeIndividualEvaluator;
import locoGP.individual.Individual;
import locoGP.problems.Problem;
import locoGP.problems.crypto.Ascon128V11EncryptProblem;
import locoGP.util.Logger;

public class AsconInputAnalysis {

	/**
	 * @param args
	 */
	private static IndividualEvaluator ourIndEval = new ByteCodeIndividualEvaluator();
	
	public static void main(String[] args) {
		
		// get a measure of execution cost for a range of input sizes
		
		/* show larger input sizes actually enable different execution paths
		 * 1. by observing jumps in execution cost, 
		 *    the rate of execution cost increase changes as input size increases.
		 *    e.g. 1 char message and 0 char data is lower than with 1 char data
		 * 2. by taking an execution trace through the program with different sized
		 *    input. How many different paths are there?   
		 */
		GPConfig gpConfig = new GPConfig();
		gpConfig.turnOffDebugLogging();
		Ascon128V11EncryptProblem asconProblem = new Ascon128V11EncryptProblem();
		asconProblem.setConfig(gpConfig);
		Individual originalIndividual ; //= new Individual(asconProblem);
		originalIndividual = new Individual(asconProblem);
		
		for(int messageSize=0; messageSize<Integer.MAX_VALUE; messageSize++){
			for(int dataSize=0; dataSize<=messageSize; dataSize++){
				asconProblem.setTestCaseLength(messageSize,dataSize);	
				ourIndEval.evaluateIndNoTimeLimit(originalIndividual);
				Logger.log("MessDataCost " + messageSize + " " + dataSize + " " + originalIndividual.getRunningTime());
				asconProblem.setTestCaseLength(dataSize,messageSize);	
				ourIndEval.evaluateIndNoTimeLimit(originalIndividual);
				Logger.log("MessDataCost " + dataSize + " " + messageSize + " " + originalIndividual.getRunningTime());
				Logger.flushLog();
			}
		}

	}

}
