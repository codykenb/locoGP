package locoGP.operators;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Callable;

import locoGP.experiments.GPConfig;
import locoGP.fitness.IndividualEvaluator;
import locoGP.individual.Individual;
import locoGP.util.Logger;

public class Mutator implements Callable<Mutator>{

	Individual anIndividual;
	Vector<Individual> individuals;

	IndividualEvaluator ourIndEval;

	private GPConfig gpConfig;
	public Mutator(Individual anIndividual, Vector<Individual> individuals, IndividualEvaluator ourIndEval, GPConfig gpConfig){
		this.anIndividual = anIndividual;
		this.individuals = individuals;
		this.ourIndEval = ourIndEval;
		this.gpConfig = gpConfig;
	}

	public void run() {

		Individual newInd = null;
		try{
			newInd =successfullMutate(anIndividual);
		}catch (Exception e){
			e.printStackTrace();
		}
		individuals.add(newInd);
		System.out.println("New individual added: " + individuals.size());
	}
	
	@Override
	public Mutator call() throws Exception {
		run();
		return this;
	}
	
	public Individual successfullMutate(Individual parentInd){ // force a successful mutation
		boolean success = false;
		boolean mutateSuccess = false;
		Random ranNumGenerator = new Random();
		int choice = ranNumGenerator.nextInt(10); 
		
		// mutateFail is to guard against getting stuck trying to mutate an empty program. (What if all programs are empty?)
		// failCount is to measure modifications which do not compile
		int mutateFail = 0 ,failCount = 0 ;
		Logger.logTrash(" Mutation Choice - " + choice);
		
		Individual returnInd = null; 
		
		int numChanges = 0 ;
		
		while (!success) {
			
			// clones gpdata and links the new object to the old.
			returnInd = parentInd.clone(); 

			
			numChanges = 1;
			try{
			NodeOperators.mutate(returnInd, choice, numChanges, gpConfig);
			mutateSuccess = true;
			}catch(Exception e){
				Logger.logDebugConsole("Problem mutating " );
				e.printStackTrace();
				mutateFail++;
			}
			
			if (mutateSuccess){
				// TODO optimisation, if no changes, no need to reevaluate ind
				if(ourIndEval.evaluateInd(returnInd)) { 
					Logger.logDebugConsole(" success eval" );
				success = true;
				
				returnInd.setMutationSucceeded();
				returnInd.addMutationAttempts(1); 

				if(numChanges >0 ){ // only log if an actual mutation has occurred
					Logger.log("Mutating " + parentInd.getClassName() + " = "
						+ returnInd.getClassName() + " Time: "+returnInd.getRunningTime()+" Fit:" + returnInd.getFitness()
						+ " TestError:" + returnInd.getFunctionalityScore()+ " ASTNodes: " + returnInd.getNumNodes() 
						+ " GPNodes: " 	+ returnInd.getNumGPNodes()
						+ " xoverApplied: " 	+ returnInd.crossoverApplied()
						+ " xovers: " 	+ returnInd.getCrossoverAttempts()
						+ " mutationApplied: " 	+ returnInd.mutationApplied()
						+ " mutations: " 	+ returnInd.getMutationAttempts());
				}else{
					Logger.log("Mutation skipped " + parentInd.getClassName() + " = "
							+ returnInd.getClassName() + " Time: "+returnInd.getRunningTime()+" Fit:" + returnInd.getFitness()
							+ " TestError:" + returnInd.getFunctionalityScore()+ " ASTNodes: " + returnInd.getNumNodes() 
							+ " GPNodes: " 	+ returnInd.getNumGPNodes()
							+ " xoverApplied: " 	+ returnInd.crossoverApplied()
							+ " xovers: " 	+ returnInd.getCrossoverAttempts()
							+ " mutationApplied: " 	+ returnInd.mutationApplied()
							+ " mutations: " 	+ returnInd.getMutationAttempts());
				}
				
				
				returnInd.refreshGPMaterial();
				if(gpConfig.isPickBestLocation() && gpConfig.isUpdateLocationBias()){
					// This function back-propagates:
					//returnInd.updateProbabilitiesWithGaussianNoise();
					returnInd.updateProbabilities(parentInd); // results are in, update any gpDataproperties accordingly, using the difference between parent and child
					returnInd.clearChangedFlags();
				}
				
				
				Logger.logTrash("\n\n" + returnInd.ASTSet.getCodeListing()+ "\n");
				Logger.logTrash("\n\n" + returnInd.getCodeProbabilitiesLogString());

				}else{ // fail to compile
					failCount++;
					System.out.println("Failed Mutation - " + failCount);
					if(gpConfig.isPickBestLocation() && gpConfig.isUpdateLocationBias()){
						//parentInd.updateProbabilitiesDecay();
						//parentInd.updateProbabilitiesWithGaussianNoise();
						//returnInd.reduceBiasForInitialChangeAttempt();
						parentInd.updateProbabilitiesDecayLarge();
						//returnInd.updateProbabilities(parentInd); // results are in, update any gpDataproperties accordingly, using the difference between parent and child
						parentInd.clearChangedFlags();
					}
				}
			}else{ // on no replace
				//if(gpConfig.isPickBestLocation() && gpConfig.isUpdateLocationBias()){
					parentInd.updateProbabilitiesDecayLarge();
					parentInd.clearChangedFlags();
				//}
			}
			
			/*if(gpConfig.isPickBestLocation() && gpConfig.isUpdateLocationBias()){
				// if problem mutating, and first change, reduce bias value drastically, can only happen once
				returnInd.reduceBiasForInitialChangeAttempt();
			}*/
			
			if(failCount > 100 || mutateFail > 500){
				success = true;
				returnInd = parentInd.clone();
			}
		}
		returnInd.addMutationAttempts(failCount); 
		return returnInd;
	}
}
