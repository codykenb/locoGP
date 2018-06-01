package locoGP.operators;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Callable;

import locoGP.Generation;
import locoGP.fitness.IndividualEvaluator;
import locoGP.individual.Individual;
import locoGP.problems.Problem;
import locoGP.util.Logger;

public class OperatorPipeline implements Callable<OperatorPipeline>{
	Generation currentGen;
	Vector<Individual> nextgenIndividuals;
	static Random generator;
	IndividualEvaluator ourIndEval;
	int mutationRate= 99; //50;
	int xoverRate = 30; //50; // 30
	
	public OperatorPipeline(Generation currentGen, Vector<Individual> individuals, Random generator, IndividualEvaluator ourIndEval) {
		this.currentGen = currentGen;
		this.nextgenIndividuals = individuals; 
		OperatorPipeline.generator = generator;
		this.ourIndEval= ourIndEval; 
	}

	public void run() {
		Individual firstParent=getTourneyWinner(), secondParent=getTourneyWinner();
		/*Logger.logTrash("Entering Pipeline with "+firstParent.getClassName() +" and " + secondParent.getClassName());*/
		applyOperators(getTourneyWinner(), getTourneyWinner());
		/*Logger.logTrash("Entering Pipeline with "+firstParent.getClassName() +" and " + secondParent.getClassName());
		Logger.flushLog();*/
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
		
		//System.out.println(parentOne.getCodeProbabilitiesLogString());
		
		// TODO this is like brood selection! change so only one modification made at a time.		
		if(generator.nextInt(100) < xoverRate){
			while(crossoverAttempts < maxCrossoverTries && !crossoverSuccess ){
				newInd = parentOne.crossover(parentTwo, Problem.gpConfig);
				
				if ( ourIndEval.evaluateInd(newInd)) {
					crossoverSuccess = true;
					if(newInd.hasChanged()){
						newInd.setCrossoverSucceeded();
						Logger.log(parentOne.getClassName() + " + "
							+ parentTwo.getClassName() + " = "
							+ newInd.getClassName() + " Time: "
							+ newInd.getRunningTime() + " Fit:"
							+ newInd.getFitness() + " TestError:"
							+ newInd.getFunctionalityErrorCount()
							+ " ASTNodes: " + newInd.getNumNodes() 
							+ " GPNodes: " + newInd.getNumGPNodes()
							+ " xoverApplied: " 	+ newInd.crossoverApplied()
							+ " xovers: " 	+ newInd.getCrossoverAttempts()
							+ " mutationApplied: " 	+ newInd.mutationApplied()
							+ " mutations: " 	+ newInd.getMutationAttempts());
						newInd.setUnChanged();
					}
				}else{
					if (Problem.gpConfig.isPickBestLocation() && Problem.gpConfig.isUpdateLocationBias()) {
						//parentOne.updateProbabilitiesDecay();
						//parentOne.updateProbabilitiesWithGaussianNoise();
					}
					parentOne.clearChangedFlags();
				}
				crossoverAttempts ++;
				Logger.logDebugConsole("Crossover Attempts - "+crossoverAttempts);
			}
			//newInd.addCrossoverAttempts(crossoverAttempts);
		}
		
		if(!crossoverSuccess){ // either crossover was not picked, or 100 attempts failed
			newInd = parentOne.clone();
			
			Logger.logTrash("Skipped crossover "+parentOne.getClassName() + " + "
					+ parentTwo.getClassName() + " = " + newInd.getClassName() 
					+ " Time: "	+ newInd.getRunningTime() 
					+ " Fit:" + newInd.getFitness() 
					+ " TestError:" + newInd.getFunctionalityErrorCount()
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
					+ " TestError:" + newInd.getFunctionalityErrorCount()
					+ " ASTNodes: " + newInd.getNumNodes() 
					+ " GPNodes: " + newInd.getNumGPNodes()
					+ " xoverApplied: " 	+ newInd.crossoverApplied()
					+ " xovers: " 	+ newInd.getCrossoverAttempts()
					+ " mutationApplied: " 	+ newInd.mutationApplied()
					+ " mutations: " 	+ newInd.getMutationAttempts());
		}
		
			
			newInd.refreshGlobalGPMaterial();
		if (Problem.gpConfig.isPickBestLocation() && Problem.gpConfig.isUpdateLocationBias()) {
			//newInd.updateProbabilitiesWithGaussianNoise();
			//newInd.updateProbabilities(parentOne, parentTwo); 
			newInd.clearChangedFlags();
			
		}
			
			
			if (generator.nextInt(100) < mutationRate){ 
				Mutator ourMut = new Mutator(newInd, nextgenIndividuals, ourIndEval, Problem.gpConfig);
					newInd = ourMut.successfullMutate(newInd); 
			}else
			Logger.log("Skipping Mutate = " +newInd.getClassName() +
					" Time: "+newInd.getRunningTime()+" Fit:"+newInd.getFitness()
					+ " TestError:"+newInd.getFunctionalityErrorCount()
					+ " ASTNodes: " + newInd.getNumNodes() 
					+ " GPNodes: " + newInd.getNumGPNodes()
					+ " xoverApplied: " 	+ newInd.crossoverApplied()
					+ " xovers: " 	+ newInd.getCrossoverAttempts()
					+ " mutationApplied: " 	+ newInd.mutationApplied()
					+ " mutations: " 	+ newInd.getMutationAttempts());
			
			newInd.refreshGlobalGPMaterial();
			
			newInd.addCrossoverAttempts(crossoverAttempts); // mutation attempts have already been added in "successfullMutate"
			if(crossoverSuccess)
				newInd.setCrossoverSucceeded();
			
			
			//System.out.println(newInd.getCodeProbabilitiesLogString());
			if(nextgenIndividuals.size() < Problem.gpConfig.getPopulationSize()){ // dont go over
				nextgenIndividuals.add(newInd); // doing the work of a generation here
				Logger.logTrash("\n\n" + newInd.ASTSet.getCodeListing()+ "\n");
				Logger.logTrash("\n\n" + newInd.getCodeProbabilitiesLogString() + "\n");
				System.out.println(newInd.getClassName() +" fit: " + newInd.getFitness());
			}


		return crossoverSuccess;
	}
	
	
	
private Individual getTourneyWinner() {
		
		List<Individual>  candidateInds = new ArrayList<Individual>();
		
		for (int i = 0 ; i<Problem.gpConfig.getTournamentSize() ; i++){
			candidateInds.add(currentGen.individuals.get(generator.nextInt(currentGen.individuals.size())));
		}
		
		int bestIndIndex = 0 ;
		
		float bestFitness = candidateInds.get(0).getFitness();
		for (int i = 0; i < Problem.gpConfig.getTournamentSize(); i++) {
			if (candidateInds.get(i).getFitness() < bestFitness) {
				bestIndIndex = i;
			}
		}
		
		return candidateInds.get(bestIndIndex);
	}





}
