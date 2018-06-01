package locoGP.fitness;

import locoGP.individual.Individual;

public interface IndividualEvaluator {

	public abstract boolean evaluateInd(Individual ind);

	public abstract boolean evaluateIndNoTimeLimit(Individual ind);

}