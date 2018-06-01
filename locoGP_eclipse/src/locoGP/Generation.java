package locoGP;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

import locoGP.experiments.GPConfig;
import locoGP.fitness.IndividualEvaluator;
import locoGP.fitness.bytecodeCount.ByteCodeIndividualEvaluator;
import locoGP.individual.Individual;
import locoGP.operators.GPASTNodeData;
import locoGP.operators.Mutator;
import locoGP.operators.NodeOperators;
import locoGP.operators.OperatorPipeline;
import locoGP.problems.Problem;
import locoGP.problems.crypto.Ascon128V11COptDecryptProblem;
import locoGP.problems.crypto.Ascon128V11COptEncryptProblem;
import locoGP.problems.crypto.Ascon128V11DecryptProblem;
import locoGP.problems.crypto.Ascon128V11EncryptProblem;
import locoGP.util.Logger;
import locoGP.util.gpDataSetterVisitor;

public class Generation implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5101375138958967830L;
	public Vector<Individual> individuals = new Vector<Individual>(101);
	private Random ranNumGenerator = new Random();
	
	private static String logID = "";
	public static Individual originalIndividual = null;
	private static int generationCount = 0;
	//public static GPConfig gpConfig;
	private static ExecutorService executor; //= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*5);
	

	
	public Generation(Generation currentGen) {
		incrementGenerationCount();
		createGeneration(currentGen);
		if(originalIndividual.ourProblem.gpConfig.useDiverseElitism())
			addDiverseElite(currentGen);
		else
			addElite(currentGen.getElite()); // helps diversity
		writeCurrentPopulationDetails();

		/*
		 * Elitism options 
		 * addElite(currentGen.getSingleElite());
		 * addElite(currentGen.getFlatElite()); 
		 * addElite(currentGen.getElite());
		 * addElite(currentGen.getFlatElite()); 
		 * addDiverseElite(currentGen);
		 */
		currentGen=null;
	}
	
	private void incrementGenerationCount() {
		Generation.generationCount++;
		Logger.log("Generation: " + Generation.generationCount);
		System.out.print("Generation: " + Generation.generationCount + "\n");
	}
	
	public int getGenerationCount(){
		return Generation.generationCount;
	}
	

	
	private void createGeneration(Generation currentGen) {
		ArrayList<Future<OperatorPipeline>> results = null;
		Collection<Callable<OperatorPipeline>> allTasks;
		int programsNeeded = 0;
		while (this.individuals.size() < Problem.gpConfig.getPopulationSize()) {
			if (executor != null && !executor.isShutdown()) {
				executor.shutdownNow();
			}

			allTasks = new Vector<Callable<OperatorPipeline>>();
			executor = Executors.newFixedThreadPool((int) (Runtime.getRuntime()
					.availableProcessors() - 1)); // *2 -1);
			programsNeeded = Problem.gpConfig.getPopulationSize()
					- this.individuals.size();
			for (int i = (int) (programsNeeded * Problem.gpConfig
					.getOverProvisioningRatio()); i > 0; i--) {
				allTasks.add(new OperatorPipeline(currentGen, this.individuals,
						this.ranNumGenerator, Problem.gpConfig.getEvaluator()));
			}
			try {
				results = new ArrayList<Future<OperatorPipeline>>(
						executor.invokeAll(allTasks, 30, TimeUnit.MINUTES));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			executor.shutdownNow();
			try {
				while (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
					executor.shutdownNow();
				}
				// Are threads locking on an object, and then being halted, no
				// unlocking is performed..
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// we needed x, we put on y, we got z, y/z shows the percentage we
			// can expect to yield
			// * (how many we put on) / (how many we got) = multiplier to hit
			// what we need
			Problem.gpConfig.setOverProvisioning(((float) allTasks.size())
					/ (getSuccess(results)));
		}
	}
		
	public Generation(Individual originalIndividual, GPConfig gpConfig,
			String logID) {
		// Create initial generation
		//Problem.gpConfig = gpConfig;
		Generation.originalIndividual = originalIndividual;
		Generation.logID = logID;
		Problem.gpConfig.getEvaluator().evaluateIndNoTimeLimit(originalIndividual);
		originalIndividual.ourProblem.setBaselineRuntimeAvg(originalIndividual.getRuntimeAvg());
		Logger.logDebugConsole(" - - Seed Individual: " + originalIndividual.getClassName()
		//System.out.println(" - - Seed Individual: " + originalIndividual.getClassName()
				+ " Time: " + originalIndividual.getRunningTime()
				+ " RuntimeAvg: " + originalIndividual.getRuntimeAvg()
				+ " Fit:" + originalIndividual.getFitness() 
				+ " TestError:" + originalIndividual.getFunctionalityErrorCount() 
				+ " ASTNodes: " + originalIndividual.getNumNodes() 
				+ " GPNodes: " 	+ originalIndividual.getNumGPNodes()
				+ " xovers: " 	+ originalIndividual.getCrossoverAttempts()
				+ " mutations: " 	+ originalIndividual.getMutationAttempts());
		Logger.logTrash("\nSeed Individual\n" + originalIndividual.ourProblem.getStrings().getCodeListing() + "\n");
		
		
		
		/*Individual newInd = originalIndividual.clone();
		getOurIndEval().evaluateIndNoTimeLimit(newInd);
		System.out.println(" - - Clone Individual: " + newInd.getClassName()
				+ " Time: " + newInd.getRunningTime()
				+ " RuntimeAvg: " + newInd.getRuntimeAvg()
				+ " Fit:" + newInd.getFitness() 
				+ " TestError:" + newInd.getFunctionalityErrorCount() 
				+ " ASTNodes: " + newInd.getNumNodes() 
				+ " GPNodes: " 	+ newInd.getNumGPNodes()
				+ " xovers: " 	+ newInd.getCrossoverAttempts()
				+ " mutations: " 	+ newInd.getMutationAttempts());*/
		Logger.flushLog();
		//System.exit(0);
		
		
		
		
		if(gpConfig.issetSeedBiasFromDeletionAnalysis()){
			this.setSeedBiasFromDeletionAnalysis(originalIndividual);
		}
		if(gpConfig.issetSeedBiasFromPerfRedFreqOverCompFreq()){
			createGenFromPerfRedFreqOverCompFreq(originalIndividual);
		}else if(gpConfig.isFirstGenCreatedFromExhaustive()){
			this.createGenFromExhaustiveMutation(originalIndividual);
		}else {
			this.createGenfromRandomMutation(originalIndividual);
		}
	}

	private void setSeedBiasFromDeletionAnalysis(Individual originalIndividual) {
		/*
		 * As an alternative to using a profiler, or sensitivity analysis, 
		 * we delete each statement in the program (in order of appearance),
		 * an measure how much of an effect this has on the execution cost
		 * execution cost reduction amount is used to set the bias for all elements
		 * down the AST from that statement
		 */
		List<Statement> cloneStmts, seedStmts = originalIndividual.gpMaterial.getStatements();
		Individual indClone = null;
		gpDataSetterVisitor biasSetterVisitor = null;
		Statement tmpStmt;
		//ourIndEval.evaluateIndNoTimeLimit(originalIndividual); // This is our reference individual
		
		System.out.print("Deleting each line of seed to set location bias");
		
		originalIndividual.setAllGPDataNodesTo((float).1); // why?
		for(int i = 0 ; i< seedStmts.size(); i++){
			// clone the program
			indClone=originalIndividual.clone();
			cloneStmts = indClone.gpMaterial.getStatements();
			//delete a statement
			NodeOperators.deleteNode(cloneStmts.get(i));
			tmpStmt= seedStmts.get(i);
			if(Problem.gpConfig.getEvaluator().evaluateInd(indClone)){
				if(indClone.getRuntimeAvg() < originalIndividual.getRuntimeAvg()){
					// set bias in proportion to the reduction in execution
					if(indClone.getFunctionalityErrorCount() > originalIndividual.getFunctionalityErrorCount()){
					  biasSetterVisitor = new gpDataSetterVisitor(1-(indClone.getRuntimeAvg()/originalIndividual.getRuntimeAvg()));
					  tmpStmt.accept(biasSetterVisitor);
					}else { // if we reduce performance without introducing any error, then we've already found an improvement!
						biasSetterVisitor = new gpDataSetterVisitor(1);
						tmpStmt.accept(biasSetterVisitor);
						this.individuals.add(indClone);
					}
				}else{ // we've deleted a statement, with no reduction in performance, 
					// so it's dead code, leave it in the program so it can be reused/cloned into executed sections  
					biasSetterVisitor = new gpDataSetterVisitor(0);
					tmpStmt.accept(biasSetterVisitor);
				}
				
			}
			if(tmpStmt.getNodeType() == 8 ){ // instanceof BlockStatement)
				GPASTNodeData tempData = (GPASTNodeData) tmpStmt.getProperty("gpdata");
				if(tempData != null)
					tempData.setProbabilityVal(1); // encourage cloning into blocks
			}
			Logger.log("Deletion index: " + i + " " + indClone.getClassName()
					+ " Time: " + indClone.getRunningTime() 
					+ " Fit: " + indClone.getFitness() 
					+ " TestError: " + indClone.getFunctionalityErrorCount() 
					+ " ASTNodes: " + indClone.getNumNodes() 
					+ " Compiled: " + indClone.compiled() 
					+ " testResults:" + indClone.getTestCaseResultsText()
					+ " bias: "+ (1-(indClone.getRuntimeAvg()/originalIndividual.getRuntimeAvg())));
			System.out.print(".");
		}
		Logger.logBiasFile("Seed-"+originalIndividual.getClassName()+"-Deletion",0 , originalIndividual.getCodeProbabilities() );
		Logger.flushLog();
		System.out.println("Done\n");
	}
	
	private void createGenFromPerfRedFreqOverCompFreq(Individual originalIndividual) {
		/* for each node, replace with all other nodes
		 * count how many replacements compile, and how many variant programs have reduced execution
		 * bias of node is = reduced execution frequency / compilation frequency 
		 	any program that has reduced execution cost (when compared with the seed) is put into the generation
		 */
		/* Sensitivity analysis:
		 * for changes which create a program with reduced bias, 
		 *   set the bias low in the child location and
		 *   set the bias for that location high in all other programs
		 *   
		 * Implementation:
		 *   set all bias to 0 in the seed program
		 *   set all bias objects in cloned programs to be the same object as corresponding one in the seed
		 *   when a change produces a reduced execution variant
		 *     for the changed node in seed, set bias proportional to execution cost saving
		 *     for new node in variant, replace the bias object with a new one, with bias value proportional to execution cost saving (divided by 2)
		 */
	
		// TODO refactoring needed - the following code was copied from locoGP.experiments.ExhaustiveChange 

		List<ASTNode> seedNodes = originalIndividual.gpMaterial.getAllAllowedNodes();

		Individual indClone = null;
		ASTNode seedNodeToReplace, nodeToReplace,replacementNode = null;		
		
		//originalIndividual.setAllGPDataNodesTo((float)0);
		
		int compileCount =0, redExeCount=0;
		
		// for each node in the program // TODO reduce this nesting, in the name of bejaykers
		for(int i=0 ; i<seedNodes.size() ; i++){
			seedNodeToReplace = seedNodes.get(i); // pick a node from the seed
			compileCount =redExeCount=0;
			//long bestFunctionalityForNode = 2* originalIndividual.ourProblem.getWorstFunctionalityScore();
			GPASTNodeData tmpData = (GPASTNodeData)seedNodeToReplace.getProperty("gpdata");
			for(int j=0 ; j<seedNodes.size() ; j ++){ // go through clone, and try that node at every location
				if(i!=j){
					indClone = originalIndividual.clone(); // we should only do this once we know the nodes are compatible, would speed things up a bit..
					indClone.getRefsToOriginalBiasObjects(originalIndividual);
					nodeToReplace = indClone.gpMaterial.getAllAllowedNodes().get(i); // keep replacing the same node
					replacementNode = indClone.gpMaterial.getAllAllowedNodes().get(j);
					
					if(nodeToReplace.toString().compareTo(replacementNode.toString())!=0){ // skip nodes that are the same
						ASTNode newlyReplacedNode = NodeOperators.replaceNode(nodeToReplace, replacementNode);
						if(newlyReplacedNode!=null){
							// Eval the new ind 
							if(Problem.gpConfig.getEvaluator().evaluateInd(indClone)){
								compileCount++;
								if(indClone.getRuntimeAvg() < originalIndividual.getRuntimeAvg()){	
									redExeCount++;
									// add the individual to the generation
									
									// TODO why check fun score?
									if(indClone.getFunctionalityErrorCount() >0 && indClone.getFunctionalityErrorCount() < originalIndividual.ourProblem.getWorstFunctionalityScore()){
										// provided we don't already have a program representing the same error count
										Iterator<Individual> iter = this.individuals.iterator();
										boolean interestingIndForGen = true;
										while(iter.hasNext()){
											Individual current = iter.next();
											if(current.getFunctionalityErrorCount() == indClone.getFunctionalityErrorCount())
												interestingIndForGen = false;
										}
										if( interestingIndForGen )
											this.individuals.add(indClone);
									}
									/*if(indClone.getFunctionalityScore() < bestFunctionalityForNode){
										bestFunctionalityForNode = indClone.getFunctionalityScore();
									}*/
									
									GPASTNodeData newData = new GPASTNodeData();
									
//								 	if the variant has reduced execution cost, new gpdata of value 0 for this node, unlikely to be changed during GP, less likely to get back to seed
									newData.setProbabilityVal(0); 
									newData.setParentIndividualNodeData(tmpData); // for what its worth, every program has a reference to this object
									newlyReplacedNode.setProperty("gpdata", newData);
									//j=seedNodes.size(); // we found out that this node is interesting, move on
								}
							}
						}
					}
				}
			}
			/* all program variants have a ref to this same object,
			 * so set it proportional to the best functionality found when exhaustively modifying that node
			 * deletion analysis determines what the max bias nodes in a statement should receive
			 * the best functionality is used to differentiate nodes  
			 */
			if (compileCount > 0) { // if we didn't get a compile, we know
				// very little about the node, hopefully
				// will have already been set by
				// deletion

				if (Problem.gpConfig.isupdateSeedBiasFromPerfRedFreqOverCompFreq()) {
					// by multiplying we either leave the value as is, or reduce it
					// if deletion was used, we just update the values, instead of resetting them
					tmpData.setProbabilityVal(tmpData.getProbabilityVal()
							* ((double) redExeCount / (double) compileCount));
				} else {
					tmpData.setProbabilityVal((double) redExeCount
							/ (double) compileCount);
				}
			}
		}	
	}

	private void createGenFromExhaustiveMutation(Individual originalIndividual) {

		// take all nodes, and replace them by all other nodes
		// any program that compiles is put into the generation
		/* Sensitivity analysis:
		 * for changes which create a program with reduced bias, 
		 *   set the bias low in the child location and
		 *   set the bias for that location high in all other programs
		 *   
		 * Implementation:
		 *   set all bias to 0 in the seed program
		 *   set all bias objects in cloned programs to be the same object as corresponding one in the seed
		 *   when a change produces a reduced execution variant
		 *     for the changed node in seed, set bias proportional to execution cost saving
		 *     for new node in variant, replace the bias object with a new one, with bias value proportional to execution cost saving (divided by 2)
		 */
	
		// TODO refactoring needed - the following code was copied from locoGP.experiments.ExhaustiveChange 

		List<ASTNode> seedNodes = originalIndividual.gpMaterial.getAllAllowedNodes();

		Individual indClone = null;
		ASTNode seedNodeToReplace, nodeToReplace,replacementNode = null;		
		
		//ourIndEval.evaluateIndNoTimeLimit(originalIndividual); // This is our reference individual
		//originalIndividual.ourProblem.setBaselineRuntimeAvg(originalIndividual.getRuntimeAvg());
		//originalIndividual.setAllGPDataNodesTo((float)0);
		
		// for each node in the program // TODO reduce this nesting
		for(int i=0 ; i<seedNodes.size() ; i++){
			seedNodeToReplace = seedNodes.get(i); // pick a node from the seed
			long bestFunctionalityForNode = 2* originalIndividual.ourProblem.getWorstFunctionalityScore();
			GPASTNodeData tmpData = (GPASTNodeData)seedNodeToReplace.getProperty("gpdata");
			for(int j=0 ; j<seedNodes.size() ; j ++){ // go through clone, and try that node at every location
				if(i!=j){
					indClone = originalIndividual.clone(); // we should only do this once we know the nodes are compatible, would speed things up a bit..
					indClone.getRefsToOriginalBiasObjects(originalIndividual);
					nodeToReplace = indClone.gpMaterial.getAllAllowedNodes().get(i); // keep replacing the same node
					replacementNode = indClone.gpMaterial.getAllAllowedNodes().get(j);
					
					if(nodeToReplace.toString().compareTo(replacementNode.toString())!=0){ // skip nodes that are the same
						ASTNode newlyReplacedNode = NodeOperators.replaceNode(nodeToReplace, replacementNode);
						if(newlyReplacedNode!=null){
							// Eval the new ind 
							if(Problem.gpConfig.getEvaluator().evaluateInd(indClone)){
								if(indClone.getRuntimeAvg() < originalIndividual.getRuntimeAvg()){	
									
									// add the individual to the generation
									if(indClone.getFunctionalityErrorCount() >0 && indClone.getFunctionalityErrorCount() < originalIndividual.ourProblem.getWorstFunctionalityScore()){
										// provided we don't already have a program representing the same error count
										Iterator<Individual> iter = this.individuals.iterator();
										boolean interestingIndForGen = true;
										while(iter.hasNext()){
											Individual current = iter.next();
											if(current.getFunctionalityErrorCount() == indClone.getFunctionalityErrorCount())
												interestingIndForGen = false;
										}
										if( interestingIndForGen )
											this.individuals.add(indClone);
									}
									if(indClone.getFunctionalityErrorCount() < bestFunctionalityForNode){
									}
									
									GPASTNodeData newData = new GPASTNodeData();
									
//								 	if the variant has reduced execution cost, new gpdata of value 0 for this node, unlikely to be changed during GP, less likely to get back to seed
									newData.setProbabilityVal(0); 
									newData.setParentIndividualNodeData(tmpData); // for what its worth
									newlyReplacedNode.setProperty("gpdata", newData);
									//j=seedNodes.size(); // we found out that this node is interesting, move on
									
									
								}
							}
						}
					}
				}
			}
			/* all program variants have a ref to this same object,
			 * so set it proportional to the best functionality found when exhaustively modifying that node
			 * deletion analysis determines what the max bias nodes in a statement should receive
			 * the best functionality is used to differentiate nodes  
			 */
			
			tmpData.setProbabilityVal(
					tmpData.getProbabilityVal() // which was originally set by deletion analysis
							* originalIndividual.getFunctionalityErrorCount()/bestFunctionalityForNode); 
		}	
	}
	
	private void fillGenFromRandomMut() {
		Collection<Callable<Mutator>> allTasks = new Vector<Callable<Mutator>>();
		System.out.println("Generation: " + Generation.generationCount);
		while (this.individuals.size() < 
				Problem.gpConfig.getPopulationSize()-
				(Problem.gpConfig.getPopulationSize() * (Problem.gpConfig.getInitialPopulationSeedRatio()))) {
			allTasks = new Vector<Callable<Mutator>>();
			// some programs don't compile, so to reduce the number of
			// iterations, we overprovision
			for (int i = (int) ((Problem.gpConfig.getPopulationSize() - this.individuals
					.size()) * Problem.gpConfig.getOverProvisioningRatio()); i > 0; i--) {
				allTasks.add(new Mutator(originalIndividual, this.individuals,
						Problem.gpConfig.getEvaluator(), Problem.gpConfig));
			}

			Logger.logDebugConsole("New executor about to start: "
					+ allTasks.size());
			executor = Executors.newFixedThreadPool(Problem.gpConfig
					.getThreadPoolSize()); // *2);
			List<Future<Mutator>> executionFutureList = null;
			try {
				executionFutureList = executor.invokeAll(allTasks,
						(Problem.gpConfig.getPopulationSize() / 4) + 5,
						TimeUnit.MINUTES);
				if (Logger.debugLoggingEnabled()) {
					for (Future<Mutator> futureSpent : executionFutureList) {
						try {
							futureSpent.get();
						} catch (Exception e) {
							Logger.logDebugConsole(e.getMessage());
							e.printStackTrace();
							/* Why is this suddenly a problem?
							 * Logger.logDebugConsole(e.getCause().getMessage());
							 * e.getCause().printStackTrace();
							 */ 
						}
					}
				}
			} catch (InterruptedException e) {
				Logger.logDebugConsole("Executor bork");
				e.printStackTrace();
			}
			executor.shutdown();
			if (!executor.isTerminated()) {
				try {
					while (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
						System.out.println("Executor still not shutdown");
						executor.shutdownNow();
					}
					// Are threads locking on an object, and then being halted,
					// no unlocking is performed..
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		for (int i = 0; 
				i < (Problem.gpConfig.getPopulationSize() * 
						(Problem.gpConfig.getInitialPopulationSeedRatio()))
						; i++) {
			// fill the sucker up with cloned seeds (after bias has been accumulated in the seed!)
			this.individuals.add(originalIndividual.cloneWithFitness());
		}
	}
	
	private void createGenfromRandomMutation(Individual originalIndividual){
		
		// gather primitives from original program
		//Individual.initialise(originalIndividual.ourProblem.getStrings());
		
		// Evaluate our reference individual (assumption is it halts)
		 
		//originalIndividual.ourProblem.setBaselineRuntimeAvg(originalIndividual.getRuntimeAvg()); 
		/*if (originalIndividual.ourProblem instanceof Ascon128V11COptDecryptProblem
				|| originalIndividual.ourProblem instanceof Ascon128V11COptEncryptProblem
				|| originalIndividual.ourProblem instanceof Ascon128V11EncryptProblem
				|| originalIndividual.ourProblem instanceof Ascon128V11DecryptProblem)
			originalIndividual.setFunctionalityErrorCount(0); // yet another clutch
*/		
		//Logger.log("Seed Individual: " + originalIndividual.getClassName()
		
		// the fitness should be normalised so the seed is 1
		if (originalIndividual.getFitness() != 1 && !(originalIndividual.ourProblem instanceof Ascon128V11COptDecryptProblem
				|| originalIndividual.ourProblem instanceof Ascon128V11COptEncryptProblem
				|| originalIndividual.ourProblem instanceof Ascon128V11EncryptProblem
				|| originalIndividual.ourProblem instanceof Ascon128V11DecryptProblem)) { 
			throw new IllegalStateException("Seed fitness is not 1");
		}
		Logger.flushLog();
		// parralellzone --------------------------------------\
		fillGenFromRandomMut();									//  |
		// parralellzone --------------------------------------/
		
	}

	
	private float getSuccess(ArrayList<Future<OperatorPipeline>> results) {
		int successCount = 0;
		for (Future<OperatorPipeline> f : results) {
			try {
				f.get();
				successCount++;
			} catch (Exception e) {
			}
		}
		if (successCount < 1)
			successCount = 1;
		return successCount;
	}

	
	private void addDiverseElite(Generation previousGen) {
		// This form of elitism preserves the rarities. and then those that are the most fit		
		ArrayList<Individual> allIndividuals = new ArrayList<Individual>();
		allIndividuals.addAll(this.individuals);
		allIndividuals.addAll(previousGen.individuals);

		Vector<Individual> uniqueIndividuals = uniqueIndividualsOnly(allIndividuals);
		
		while(uniqueIndividuals.size() < Problem.gpConfig.getPopulationSize() )
		{
			allIndividuals.removeAll(uniqueIndividuals);
			Vector<Individual> flattenLeftovers = uniqueIndividualsOnly(allIndividuals);
			while(flattenLeftovers.size()> 0 && uniqueIndividuals.size() < Problem.gpConfig.getPopulationSize() )
				uniqueIndividuals.add(flattenLeftovers.remove(0));
		} 
		while (uniqueIndividuals.size() > Problem.gpConfig.getPopulationSize()) {
			uniqueIndividuals.remove(uniqueIndividuals.size()-1); // trim the end ( or calculate how many to take off the end? )
		}		
		this.individuals = uniqueIndividuals;		
	}

	private Vector<Individual> uniqueIndividualsOnly(
			ArrayList<Individual> allIndividuals) {
		Vector<Individual> uniqueIndividuals = new Vector<Individual>();
		Collections.sort(allIndividuals);
		Iterator<Individual> iter = allIndividuals.iterator();
		Individual curInd ;
		while(iter.hasNext()){
			curInd = (Individual) iter.next();
			if (uniqueIndividuals.size() == 0 || Math.round(curInd.getFitness()) != Math
					.round(uniqueIndividuals.get(uniqueIndividuals.size() - 1).getFitness()))
				uniqueIndividuals.add(curInd);
		}
		return uniqueIndividuals;
	}

	private void addElite(List<Individual> elite) {
		replaceWorstWithElite(elite);
	}
	
	private void replaceWorstWithElite(List<Individual> elite) {
		Collections.sort(this.individuals);
		for (int i = 0; i< elite.size(); i++  ){
			Logger.log("Replacing " + individuals.get(((this.individuals.size() - 1 )-i)).getClassName() + " with " +elite.get(i).getClassName());
			this.individuals.set((this.individuals.size() - 1 )-i , elite.get(i));
		}
	}
	
	private void addEliteByNegTourny(List<Individual> elite) {
		List<Integer> indexToReplace = new ArrayList<Integer>();
		
		Integer temp ;
		
		while(indexToReplace.size() < elite.size()) {
			temp = getNegativeTourneyWinnerIndex(); // TODO replace this with sorting and removing the x number of worst programs
			if(!indexToReplace.contains(temp)){
				indexToReplace.add(temp);
			}else{
				System.out.println("Duplicate bad program ");
			}
		}		
		// Pick the locations first, them replace the whole lot.
		// This is better as we dont want items that we just inserted, being themselves replaced.
		for (int i = 0; i < elite.size(); i++) {
			Logger.log("Replacing " + individuals.get(indexToReplace.get(i)).getClassName() + " with " +elite.get(i).getClassName());
			this.individuals.set(indexToReplace.get(i), elite.get(i));
		}
	}

	public void writeCurrentPopulationDetails(){
		// TODO write Java/Logs asynchronously (in a separate thread)
		Iterator<Individual> iter = this.individuals.iterator();
		Individual tempInd;
		while( iter.hasNext()){
			tempInd = iter.next();
			Logger.logGenInfo(tempInd.getClassName() + " 100 mult "
					+ tempInd.getCorrectness() + " + "
					+ tempInd.getTimeFitnessRatio() + " = "
					+ tempInd.getFitness() + " ASTNodes: "
					+ tempInd.getNumNodes() + " GPNodes: "
					+ tempInd.getNumGPNodes()
					+ " xoverApplied: " 	+ tempInd.crossoverApplied()
					+ " xovers: " 	+ tempInd.getCrossoverAttempts()
					+ " mutationApplied: " 	+ tempInd.mutationApplied()
					+ " mutations: " 	+ tempInd.getMutationAttempts()
					+ " testResults: "+ tempInd.getTestCaseResultsText());
			Logger.logJavaFile(tempInd.getClassName(), tempInd.ASTSet.getCodeListing() );
			Logger.logBiasFile(tempInd.getClassName(),this.getGenerationCount() , tempInd.getCodeProbabilities() );
			// + "\n\n " + tempInd.getCodeProbabilitiesComment()
		}
	}
	
	private List<Individual> getUniqueEliteWholeNumbers(){
		// Elitism which picks the best individuals, but only one for each whole number fitness value (decimal values are truncated)
		List<Individual> elite = new ArrayList<Individual>();
		Collections.sort(this.individuals);
		int i = 0, indFit=0, eliteFit=0 ;
		float indFitFull = 0, eliteFitFull=0;
		elite.add(this.individuals.get(i));
		Logger.logTrash("Elitism size: "+ (Problem.gpConfig.getPopulationSize()*Problem.gpConfig.getElitismRate()));
		while(elite.size() < 
				(Problem.gpConfig.getPopulationSize()*Problem.gpConfig.getElitismRate()) 
				&& i<this.individuals.size()-1){
			i++;
			indFitFull = (this.individuals.get(i).getFitness());
			indFit = (int)indFitFull;
			eliteFitFull = elite.get(elite.size()-1).getFitness();
			eliteFit = (int) eliteFitFull;
			if(indFit != eliteFit)
				elite.add(this.individuals.get(i));
		}
		Logger.logTrash("Elite individuals selected: "+elite.size());
		return elite;
	}
	
	private List<Individual> getUniqueEliteFine(){
		List<Individual> elite = new ArrayList<Individual>();
		
		Collections.sort(this.individuals);
		int i = 0 ;
		elite.add(this.individuals.get(i));
		while(elite.size() < (Problem.gpConfig.getPopulationSize()*Problem.gpConfig.getElitismRate()) 
				&& i<this.individuals.size()-1){
			i++;
			if(this.individuals.get(i).getFitness() != elite.get(elite.size()-1).getFitness())
			{
				elite.add(this.individuals.get(i));
			}
		}
		return elite;
	}

	private List<Individual> getElite() {
		//List<Individual> elite = new ArrayList<Individual>();
		if( Problem.gpConfig.getUseDiverseElitismFine())
			return getUniqueEliteFine();
		else
			return getUniqueEliteWholeNumbers();
		
		/*
		Collections.sort(this.individuals);
		for (int i = 0; i < (gpConfig.getPopulationSize() * elitismRate); i++)
			elite.add(this.individuals.get(i)); // this allows duplicates
		return elite;*/
	}
	
	public List<Individual> getSingleElite() {
		List<Individual> elite = new ArrayList<Individual>();
		Collections.sort(this.individuals);
		elite.add(this.individuals.get(0));
		return elite;
	}
	
	
	private void addIndividual(Individual newInd){
		this.individuals.add(newInd); // vector is thread safe
	}

	private int getNegativeTourneyWinnerIndex() {
		List<Individual> candidateInds = new ArrayList<Individual>();
		for (int i = 0; i < Problem.gpConfig.getTournamentSize(); i++) {
			candidateInds.add(individuals.get(ranNumGenerator
					.nextInt(individuals.size())));
		}
		int worstIndIndex = individuals.indexOf(candidateInds.get(0));
		float worstFitness = candidateInds.get(0).getFitness();
		for (int i = 0; i < Problem.gpConfig.getTournamentSize(); i++) {
			if (candidateInds.get(i).getFitness() > worstFitness) {
				worstFitness = candidateInds.get(i).getFitness();
				worstIndIndex = individuals.indexOf(candidateInds.get(i));
			}
		}
		return worstIndIndex;
	}

/*	private void setIndividualIDCount(long individualIDCount) {
		this.individualID = individualIDCount;
	}*/

	/*public long getIndividualIDCount() {
		return individualID;
	}*/

	public boolean foundBetterThanSeed() {
		Iterator<Individual> iter = this.individuals.iterator();
		boolean betterIndividualThanSeedFound = false;
		while(iter.hasNext()){
			if (iter.next().getFitness() <1 )
				betterIndividualThanSeedFound = true;
		}
		return betterIndividualThanSeedFound;
	}

/*	public static IndividualEvaluator getOurIndEval() {
		return ourIndEval;
	}

	public static void setOurIndEval(IndividualEvaluator ourIndEval) {
		Generation.ourIndEval = ourIndEval;
	}*/

	public void clearBacklinks(Generation nextGen) {
		/* For individuals which are in this generation only,
		 * remove all references to parent objects
		 * added due to allow old objects to be garbage collected 
		 */
		for( Individual oldInd: individuals ){
			if(!nextGen.individuals.contains(oldInd)){
				oldInd.setNullRefs();
			}
		}
		
	}
	
}

