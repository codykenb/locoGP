package locoGP.operators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Callable;

import locoGP.Generation;
import locoGP.fitness.IndividualEvaluator;
import locoGP.individual.Individual;
import locoGP.util.Logger;

public class OperatorPipeline implements Callable<OperatorPipeline>{
	Generation currentGen;
	Vector<Individual> nextgenIndividuals;
	Random generator;
	IndividualEvaluator ourIndEval;
	int mutationRate= 90;
	int xoverRate = 30;
	
	public OperatorPipeline(Generation currentGen, Vector<Individual> individuals, Random generator, IndividualEvaluator ourIndEval) {
		this.currentGen = currentGen;
		this.nextgenIndividuals = individuals; 
		this.generator = generator;
		this.ourIndEval= ourIndEval; 
	}

	public void run() {
			applyOperators(getTourneyWinner(), getTourneyWinner());
	}
	
	@Override
	public OperatorPipeline call() throws Exception {
		run();		
		return this;
	}

	
	
	private boolean applyOperators(Individual parentOne, Individual parentTwo){
		Individual newInd = null ;
		
		int maxCrossoverTries = 100; 
		int crossoverAttempts = 0;		
		boolean crossoverSuccess = false;
		
		System.out.println(parentOne.getCodeProbabilitiesLogString());
		
		if(generator.nextInt(100) < xoverRate){
			while(crossoverAttempts < maxCrossoverTries && !crossoverSuccess ){
				newInd = parentOne.crossover(parentTwo, currentGen.gpConfig);
				
				if ( ourIndEval.evaluateInd(newInd)) {
					crossoverSuccess = true;
					if(newInd.hasChanged()){
						newInd.setCrossoverSucceeded();
						Logger.log(parentOne.getClassName() + " + "
							+ parentTwo.getClassName() + " = "
							+ newInd.getClassName() + " Time: "
							+ newInd.getRunningTime() + " Fit:"
							+ newInd.getFitness() + " TestError:"
							+ newInd.getFunctionalityScore()
							+ " ASTNodes: " + newInd.getNumNodes() 
							+ " GPNodes: " + newInd.getNumGPNodes()
							+ " xoverApplied: " 	+ newInd.crossoverApplied()
							+ " xovers: " 	+ newInd.getCrossoverAttempts()
							+ " mutationApplied: " 	+ newInd.mutationApplied()
							+ " mutations: " 	+ newInd.getMutationAttempts());
						newInd.setUnChanged();
					}
				}else{
					if (currentGen.gpConfig.isPickBestLocation() && currentGen.gpConfig.isUpdateLocationBias()) {
						//parentOne.updateProbabilitiesDecay();
						//parentOne.updateProbabilitiesWithGaussianNoise();
					}
					parentOne.clearChangedFlags();
				}
				crossoverAttempts ++;
				System.out.println("Crossover Attempts - "+crossoverAttempts);
			}
			//newInd.addCrossoverAttempts(crossoverAttempts);
		}
		
		if(!crossoverSuccess){ // either crossover was not picked, or 100 attempts failed
			newInd = parentOne.clone();
			
			Logger.logTrash("Skipped crossover "+parentOne.getClassName() + " + "
					+ parentTwo.getClassName() + " = " + newInd.getClassName() 
					+ " Time: "	+ newInd.getRunningTime() 
					+ " Fit:" + newInd.getFitness() 
					+ " TestError:" + newInd.getFunctionalityScore()
					+ " ASTNodes: " + newInd.getNumNodes() 
					+ " GPNodes: " + newInd.getNumGPNodes()
					+ " xoverApplied: " 	+ newInd.crossoverApplied()
					+ " xovers: " 	+ newInd.getCrossoverAttempts()
					+ " mutationApplied: " 	+ newInd.mutationApplied()
					+ " mutations: " 	+ newInd.getMutationAttempts());
			Logger.log("Skipped crossover "+parentOne.getClassName() + " + "
					+ parentTwo.getClassName() + " = " + newInd.getClassName() 
					+ " Time: "	+ newInd.getRunningTime() 
					+ " Fit:" + newInd.getFitness() 
					+ " TestError:" + newInd.getFunctionalityScore()
					+ " ASTNodes: " + newInd.getNumNodes() 
					+ " GPNodes: " + newInd.getNumGPNodes()
					+ " xoverApplied: " 	+ newInd.crossoverApplied()
					+ " xovers: " 	+ newInd.getCrossoverAttempts()
					+ " mutationApplied: " 	+ newInd.mutationApplied()
					+ " mutations: " 	+ newInd.getMutationAttempts());
		}
		
			
			newInd.refreshGPMaterial();
		if (currentGen.gpConfig.isPickBestLocation() && currentGen.gpConfig.isUpdateLocationBias()) {
			//newInd.updateProbabilitiesWithGaussianNoise();
			newInd.updateProbabilities(parentOne, parentTwo); 
			newInd.clearChangedFlags();
		}
			
			
			if (generator.nextInt(100) < mutationRate){ 
				Mutator ourMut = new Mutator(newInd, nextgenIndividuals, ourIndEval, currentGen.gpConfig);
					newInd = ourMut.successfullMutate(newInd); 
			}else
			Logger.log("Skipping Mutate = " +newInd.getClassName() +
					" Time: "+newInd.getRunningTime()+" Fit:"+newInd.getFitness()
					+ " TestError:"+newInd.getFunctionalityScore()
					+ " ASTNodes: " + newInd.getNumNodes() 
					+ " GPNodes: " + newInd.getNumGPNodes()
					+ " xoverApplied: " 	+ newInd.crossoverApplied()
					+ " xovers: " 	+ newInd.getCrossoverAttempts()
					+ " mutationApplied: " 	+ newInd.mutationApplied()
					+ " mutations: " 	+ newInd.getMutationAttempts());
			
			newInd.refreshGPMaterial();
			
			newInd.addCrossoverAttempts(crossoverAttempts); // mutation attempts have already been added in "successfullMutate"
			if(crossoverSuccess)
				newInd.setCrossoverSucceeded();
			
			
			System.out.println(newInd.getCodeProbabilitiesLogString());
			if(nextgenIndividuals.size() < currentGen.gpConfig.getPopulationSize()){ // dont go over
				nextgenIndividuals.add(newInd);
				Logger.logTrash("\n\n" + newInd.ASTSet.getCodeListing()+ "\n");
				Logger.logTrash("\n\n" + newInd.getCodeProbabilitiesLogString() + "\n");
			}


		return crossoverSuccess;
	}
	
	
	
private Individual getTourneyWinner() {
		
		List<Individual>  candidateInds = new ArrayList<Individual>();
		
		for (int i = 0 ; i<currentGen.gpConfig.getTournamentSize() ; i++){
			candidateInds.add(currentGen.individuals.get(generator.nextInt(currentGen.individuals.size())));
		}
		
		int bestIndIndex = 0 ;
		
		float bestFitness = candidateInds.get(0).getFitness();
		for (int i = 0; i < currentGen.gpConfig.getTournamentSize(); i++) {
			if (candidateInds.get(i).getFitness() < bestFitness) {
				bestIndIndex = i;
			}
		}
		
		/*if ( useFunctionality(candidateInds) ) {
			long bestFitness = candidateInds.get(0).getFunctionalityScore();
			for (int i = 0; i < tournamentSize; i++) {
				if (candidateInds.get(i).getFunctionalityScore() < bestFitness) {
					bestIndIndex = i;
				}
			}
		}else{
			float bestFitness = candidateInds.get(0).getRuntimeAvg();
			for (int i = 0; i < tournamentSize; i++) {
				if (candidateInds.get(i).getRuntimeAvg() < bestFitness) {
					bestIndIndex = i;
				}
			}
		}*/
		
		return candidateInds.get(bestIndIndex);
	}





}
