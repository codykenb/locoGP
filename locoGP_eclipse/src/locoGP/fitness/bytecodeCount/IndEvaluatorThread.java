package locoGP.fitness.bytecodeCount;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Callable;

import locoGP.experiments.GPConfig;
import locoGP.fitness.IndividualEvaluator;
import locoGP.individual.Individual;
import locoGP.util.Logger;

public class IndEvaluatorThread implements Callable<IndEvaluatorThread>, Runnable{

	private Individual anIndividual;
	private IndividualEvaluator ourIndEval;

	public IndEvaluatorThread(Individual anIndividual,
			IndividualEvaluator ourIndEval) {
		this.anIndividual = anIndividual;
		this.ourIndEval = ourIndEval;
	}

	public void run() {
		evalInd();
	}

	public void evalInd() {
		// TODO optimisation, if no changes, no need to reevaluate ind
		if (ourIndEval.evaluateInd(anIndividual)) { 
			Logger.logDebugConsole(" success eval");
			Logger.log("Eval success " + anIndividual.getClassName() + " = "
					+ anIndividual.getClassName() + " Time: "+anIndividual.getRunningTime()+" Fit:" + anIndividual.getFitness()
					+ " TestError:" + anIndividual.getFunctionalityErrorCount()+ " ASTNodes: " + anIndividual.getNumNodes() 
					+ " GPNodes: " 	+ anIndividual.getNumGPNodes()
					+ " xoverApplied: " 	+ anIndividual.crossoverApplied()
					+ " xovers: " 	+ anIndividual.getCrossoverAttempts()
					+ " mutationApplied: " 	+ anIndividual.mutationApplied()
					+ " mutations: " 	+ anIndividual.getMutationAttempts());
		} else {
			Logger.log("Eval failed " + anIndividual.getClassName());
		}
				
	}

	@Override
	public IndEvaluatorThread call() throws Exception {
		run();
		return this;
	}
	
}
