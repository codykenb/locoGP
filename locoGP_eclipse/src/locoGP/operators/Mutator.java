package locoGP.operators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Callable;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.Statement;

import locoGP.Generation;
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
		Logger.log("I: "+newInd.getClassName() +" fit: " + newInd.getFitness() + " - Total: " + individuals.size()  );
		System.out.println(newInd.getClassName() +" fit: " + newInd.getFitness());
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
		int choice = ranNumGenerator.nextInt(100); 
		
		// mutateFail is to guard against getting stuck trying to mutate an empty program. (What if all programs are empty?)
		// failCount is to measure modifications which do not compile
		int mutateFailCount = 0 ,mutateCompileFailCount = 0 ;
		Logger.logTrash(" Mutation Choice - " + choice);
		
		Individual returnInd = null; 
		
		int numChanges = 0 ;
		
		while (!success) {
			
			// clones gpdata and links the new object to the old.
			returnInd = parentInd.clone(); 

			
			numChanges = 1;
			try{
				mutate(returnInd, choice, numChanges, gpConfig);
				mutateSuccess = true;
			}catch(Exception e){
				Logger.logDebugConsole("Problem mutating " );
				e.printStackTrace();
				mutateFailCount++;
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
						+ " TestError:" + returnInd.getFunctionalityErrorCount()+ " ASTNodes: " + returnInd.getNumNodes() 
						+ " GPNodes: " 	+ returnInd.getNumGPNodes()
						+ " xoverApplied: " 	+ returnInd.crossoverApplied()
						+ " xovers: " 	+ returnInd.getCrossoverAttempts()
						+ " mutationApplied: " 	+ returnInd.mutationApplied()
						+ " mutations: " 	+ returnInd.getMutationAttempts());
				}else{
					Logger.log("Mutation skipped " + parentInd.getClassName() + " = "
							+ returnInd.getClassName() + " Time: "+returnInd.getRunningTime()+" Fit:" + returnInd.getFitness()
							+ " TestError:" + returnInd.getFunctionalityErrorCount()+ " ASTNodes: " + returnInd.getNumNodes() 
							+ " GPNodes: " 	+ returnInd.getNumGPNodes()
							+ " xoverApplied: " 	+ returnInd.crossoverApplied()
							+ " xovers: " 	+ returnInd.getCrossoverAttempts()
							+ " mutationApplied: " 	+ returnInd.mutationApplied()
							+ " mutations: " 	+ returnInd.getMutationAttempts());
				}
				
				
				returnInd.refreshGlobalGPMaterial();
				if(gpConfig.isPickBestLocation() && gpConfig.isUpdateLocationBias()){
					// This function back-propagates:
					//returnInd.updateProbabilitiesWithGaussianNoise();
					returnInd.updateProbabilities(parentInd); // results are in, update any gpDataproperties accordingly, using the difference between parent and child
					returnInd.clearChangedFlags();
				}
				
				
				Logger.logTrash("\n\n" + returnInd.ASTSet.getCodeListing()+ "\n");
				Logger.logTrash("\n\n" + returnInd.getCodeProbabilitiesLogString());

				}else{ // fail to compile
					mutateCompileFailCount++;
					Logger.logTrash(" - Failed Mutation - " + mutateCompileFailCount);
					if(gpConfig.isPickBestLocation() && gpConfig.isUpdateLocationBias()){
						//parentInd.updateProbabilitiesDecay();
						//parentInd.updateProbabilitiesWithGaussianNoise();
						//returnInd.reduceBiasForInitialChangeAttempt();
						parentInd.updateProbabilitiesDecaySmall();
						//returnInd.updateProbabilities(parentInd); // results are in, update any gpDataproperties accordingly, using the difference between parent and child
					}
				}
			}else{ // on no replace
				if(gpConfig.isPickBestLocation() && gpConfig.isUpdateLocationBias()){
					parentInd.updateProbabilitiesDecaySmall();
					
				}
			}
			
			/*if(gpConfig.isPickBestLocation() && gpConfig.isUpdateLocationBias()){
				// if problem mutating, and first change, reduce bias value drastically, can only happen once
				returnInd.reduceBiasForInitialChangeAttempt();
			}*/
			
			if(mutateCompileFailCount > 100 || mutateFailCount > 500){
				success = true;
				returnInd = parentInd.clone();
				System.out.println(returnInd.getClassName() +" mutate limit reached: Mutation Count: "+mutateFailCount +" Compile count: "+mutateCompileFailCount);
			}
			parentInd.clearChangedFlags();
		}
		returnInd.addMutationAttempts(mutateCompileFailCount); 
		return returnInd;
	}
	
	private static int mutate(Individual newInd, int choice, int numChanges,
			GPConfig gpConfig) {
		ASTNode mutNode = newInd.gpMaterial.selectANodeForModification();// implement num changes as different mutator, implement bias/noBias as different gpMaterial objects
				
		NodeOperators.setChangedFlag(mutNode);
		if (mutNode != null) {
			// choice: 0 = delete, 1-9 = modify
			if (choice ==0) {
				/* TODO a hoist operator to remove a method invocation from around arguments
				 */
				NodeOperators.deleteNode(mutNode);
				
			}else if (choice > 5 && mutNode instanceof Block){ // this happens too rarely 
				// 1/9 change of replacing a block with something else
				/* insert:
				 *   statement in block
				 *   TODO allow method call around expression **
				 *   expression around expression
				 */
				// System.out.println("Inserting statmement clone in block (mut)");
				NodeOperators.insertRandomStmtInBlock((Block) mutNode, newInd);
			} else{
				modifyNode(mutNode,newInd);
			}
		} else {
			Logger.logTrash("Nothing to mutate (empty program?");
		}
		return numChanges;
	}
	
	private static void modifyNode(ASTNode node, Individual ind){ //GPMaterialVisitor gpMaterial) {
		/*	depending on the node type, we should go in and mess with it?
		 *  or should we replace the node with some other type?
		 *  the node can be a primitive in an expression, or statement 
		 *  (this is crossover with itself?)
		 *  and operator messing
		 */
		
		/* change operators in assignment, postfix or infix expression
		 * otherwise
		 * replace primitives with any other primitive 
		 * replace statements with statements
		 * 
		 */

		if (node instanceof InfixExpression) {
			NodeOperators.modifyInfixExpressionOperator((InfixExpression) node, ind.gpMaterial);
		} else if (node instanceof Assignment) {
			NodeOperators.modifyAssignmentOperator((Assignment) node, ind.gpMaterial);
		} else if (node instanceof PostfixExpression) {
			NodeOperators.modifypostFixOperator((PostfixExpression) node, ind.gpMaterial);
		}else {
			NodeOperators.replaceStatementOrExpression(node, ind, false);
		}
	}
	
}
