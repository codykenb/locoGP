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
import locoGP.experiments.GPConfig;
import locoGP.fitness.IndividualEvaluator;
import locoGP.individual.Individual;
import locoGP.operators.Mutator;
import locoGP.operators.NodeOperators;
import locoGP.operators.OperatorPipeline;
import locoGP.util.Logger;

public class Generation implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5101375138958967830L;
	public Vector<Individual> individuals = new Vector<Individual>(101);
	private Random ranNumGenerator = new Random();
	private static IndividualEvaluator ourIndEval = new IndividualEvaluator();
	private static String logID = "";
	public static Individual originalIndividual = null;
	private long individualID = 0 ;
	private static int generationCount = 0;
	public static GPConfig gpConfig;
	private static ExecutorService executor; //= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*5);
	
	private  double elitismRate = .30; // TODO move these to config
	private static double initialPopulationSeedRatio = .1;
	
	public Generation(Individual originalIndividual, int tournamentSize, String logID,  GPConfig gpConfig){
		// generate initial population, this constructor is expected to be called only once
		Generation.gpConfig = gpConfig;
		Generation.originalIndividual = originalIndividual;
		Generation.logID = logID;
		
		// gather primitives from original program
		NodeOperators.initialise(originalIndividual.ourProblem.getStrings());
		// Evaluate our reference individual (assumption is it halts)
		getOurIndEval().evaluateIndNoTimeLimit(originalIndividual); 
		originalIndividual.ourProblem.setBaselineRuntimeAvg(originalIndividual.getRuntimeAvg()); 
		
		//Logger.log("Seed Individual: " + originalIndividual.getClassName()
		Logger.logDebugConsole(" - - Seed Individual: " + originalIndividual.getClassName()
				+ " Time: " + originalIndividual.getRunningTime()
				+ " RuntimeAvg: " + originalIndividual.getRuntimeAvg()
				+ " Fit:" + originalIndividual.getFitness() 
				+ " TestError:" + originalIndividual.getFunctionalityScore() 
				+ " ASTNodes: " + originalIndividual.getNumNodes() 
				+ " GPNodes: " 	+ originalIndividual.getNumGPNodes()
				+ " xovers: " 	+ originalIndividual.getCrossoverAttempts()
				+ " mutations: " 	+ originalIndividual.getMutationAttempts());
		Logger.logTrash("\nSeed Individual\n" + originalIndividual.ourProblem.getStrings().getCodeListing() + "\n");
		
		// the fitness should be normalised so the seed is 1
		if (originalIndividual.getFitness() != 1) { 
			throw new IllegalStateException("Seed fitness is not 1");
		}
		
		// parralellzone --------------------------------------\
		fillGeneration();									//  |
		// parralellzone --------------------------------------/
		
		setIndividualIDCount(Individual.getID());
	}
	
	public Generation(Generation currentGen) {
		incrementGenerationCount();
		createGeneration(currentGen);
		addElite(currentGen.getRareElite()); // helps diversity
		writeCurrentPopulationDetails();

		/*
		 * Elitism options 
		 * addElite(currentGen.getSingleElite());
		 * addElite(currentGen.getFlatElite()); 
		 * addElite(currentGen.getElite());
		 * addElite(currentGen.getFlatElite()); 
		 * addDiverseElite(currentGen);
		 */
	}
	
	private void incrementGenerationCount() {
		Generation.generationCount++;
	}
	
	public int getGenerationCount(){
		return Generation.generationCount;
	}
	
	private void fillGeneration() {
		Collection<Callable<Mutator>> allTasks = new Vector<Callable<Mutator>>();
		for (int i = 0; i < (gpConfig.getPopulationSize() * (initialPopulationSeedRatio)); i++) {
			// fill the sucker up with cloned seeds
			this.individuals.add(originalIndividual.cloneWithFitness());
		}

		while (this.individuals.size() < gpConfig.getPopulationSize()) {
			allTasks = new Vector<Callable<Mutator>>();
			// some programs don't compile, so to reduce the number of
			// iterations, we overprovision
			for (int i = (int) ((gpConfig.getPopulationSize() - this.individuals
					.size()) * gpConfig.getOverProvisioningRatio()); i > 0; i--) {
				allTasks.add(new Mutator(originalIndividual, this.individuals,
						getOurIndEval(), gpConfig));
			}

			Logger.logDebugConsole("New executor about to start: "
					+ allTasks.size());
			executor = Executors.newFixedThreadPool(gpConfig
					.getThreadPoolSize()); // *2);
			List<Future<Mutator>> executionFutureList = null;
			try {
				executionFutureList = executor.invokeAll(allTasks,
						(gpConfig.getPopulationSize() / 4) + 5,
						TimeUnit.MINUTES);
				if (Logger.isLoggingEnabled()) {
					for (Future<Mutator> futureSpent : executionFutureList) {
						try {
							futureSpent.get();
						} catch (Exception e) {
							Logger.logDebugConsole(e.getMessage());
							e.printStackTrace();
							Logger.logDebugConsole(e.getCause().getMessage());
							e.getCause().printStackTrace();
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
	}
	
	private void createGeneration(Generation currentGen) {
		// TODO refactor this into fillGeneration
		ArrayList<Future<OperatorPipeline>> results = null;
		Collection<Callable<OperatorPipeline>> allTasks;
		int programsNeeded = 0;
		while (this.individuals.size() < gpConfig.getPopulationSize()) {
			if (executor != null && !executor.isShutdown()) {
				executor.shutdownNow();
			}

			allTasks = new Vector<Callable<OperatorPipeline>>();
			executor = Executors.newFixedThreadPool((int) (Runtime.getRuntime()
					.availableProcessors() - 1)); // *2 -1);
			programsNeeded = gpConfig.getPopulationSize()
					- this.individuals.size();
			for (int i = (int) (programsNeeded * gpConfig
					.getOverProvisioningRatio()); i > 0; i--) {
				allTasks.add(new OperatorPipeline(currentGen, this.individuals,
						this.ranNumGenerator, getOurIndEval()));
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
			gpConfig.setOverProvisioning(((float) allTasks.size())
					/ (getSuccess(results)));
		}
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


	public Generation(Individual originalIndividual, GPConfig gpConfig,
			String logID) {
		this(originalIndividual, gpConfig.getTournamentSize(),logID, gpConfig);
	}

	private void addDiverseElite(Generation previousGen) {
		// This form of elitism preserves the rarities. and then those that are the most fit		
		ArrayList<Individual> allIndividuals = new ArrayList<Individual>();
		allIndividuals.addAll(this.individuals);
		allIndividuals.addAll(previousGen.individuals);

		Vector<Individual> uniqueIndividuals = uniqueIndividualsOnly(allIndividuals);
		
		while(uniqueIndividuals.size() < gpConfig.getPopulationSize() )
		{
			allIndividuals.removeAll(uniqueIndividuals);
			Vector<Individual> flattenLeftovers = uniqueIndividualsOnly(allIndividuals);
			while(flattenLeftovers.size()> 0 && uniqueIndividuals.size() < gpConfig.getPopulationSize() )
				uniqueIndividuals.add(flattenLeftovers.remove(0));
		} 
		while (uniqueIndividuals.size() > gpConfig.getPopulationSize()) {
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
			if(uniqueIndividuals.size() ==0 || curInd.getFitness() != uniqueIndividuals.get(uniqueIndividuals.size()-1).getFitness() )
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
		// TODO write Java/Logs in a separate thread
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
					+ " mutations: " 	+ tempInd.getMutationAttempts());
			Logger.logJavaFile(tempInd.getClassName(), tempInd.ASTSet.getCodeListing() );
			Logger.logBiasFile(tempInd.getClassName(),this.getGenerationCount() , tempInd.getCodeProbabilities() );
			// + "\n\n " + tempInd.getCodeProbabilitiesComment()
		}
	}
	
	private List<Individual> getRareElite(){
		List<Individual> elite = new ArrayList<Individual>();
		Collections.sort(this.individuals);
		int i = 0 ;
		elite.add(this.individuals.get(i));
		Logger.logTrash("Elitism size: "+ (gpConfig.getPopulationSize()*elitismRate));
		while(elite.size() < (gpConfig.getPopulationSize()*elitismRate) && i<this.individuals.size()-1){
			i++;
			if(((int)this.individuals.get(i).getFitness()) != ((int)elite.get(elite.size()-1).getFitness()))
			{
				elite.add(this.individuals.get(i));
			}
		}
		Logger.logTrash("Elite individuals selected: "+elite.size());
		return elite;
	}
	
	private List<Individual> getFlatElite(){
		List<Individual> elite = new ArrayList<Individual>();
		
		Collections.sort(this.individuals);
		int i = 0 ;
		elite.add(this.individuals.get(i));
		while(elite.size() < (gpConfig.getPopulationSize()*elitismRate) && i<this.individuals.size()-1){
			i++;
			if(this.individuals.get(i).getFitness() != elite.get(elite.size()-1).getFitness())
			{
				elite.add(this.individuals.get(i));
			}
		}
		return elite;
	}

	private List<Individual> getElite() {
		List<Individual> elite = new ArrayList<Individual>();
		Collections.sort(this.individuals);
		for (int i = 0; i < (gpConfig.getPopulationSize() * elitismRate); i++)
			elite.add(this.individuals.get(i)); // this allows duplicates
		return elite;
	}
	
	public List<Individual> getSingleElite() {
		List<Individual> elite = new ArrayList<Individual>();
		Collections.sort(this.individuals);
		elite.add(this.individuals.get(0));
		return elite;
	}
	
	
	private synchronized void addIndividual(Individual newInd){
		this.individuals.add(newInd);
	}

	private int getNegativeTourneyWinnerIndex() {
		List<Individual> candidateInds = new ArrayList<Individual>();
		for (int i = 0; i < gpConfig.getTournamentSize(); i++) {
			candidateInds.add(individuals.get(ranNumGenerator
					.nextInt(individuals.size())));
		}
		int worstIndIndex = individuals.indexOf(candidateInds.get(0));
		float worstFitness = candidateInds.get(0).getFitness();
		for (int i = 0; i < gpConfig.getTournamentSize(); i++) {
			if (candidateInds.get(i).getFitness() > worstFitness) {
				worstFitness = candidateInds.get(i).getFitness();
				worstIndIndex = individuals.indexOf(candidateInds.get(i));
			}
		}
		return worstIndIndex;
	}

	private void setIndividualIDCount(long individualIDCount) {
		this.individualID = individualIDCount;
	}

	public long getIndividualIDCount() {
		return individualID;
	}

	public boolean foundBetterThanSeed() {
		Iterator<Individual> iter = this.individuals.iterator();
		boolean betterIndividualThanSeedFound = false;
		while(iter.hasNext()){
			if (iter.next().getFitness() <1 )
				betterIndividualThanSeedFound = true;
		}
		return betterIndividualThanSeedFound;
	}

	public static IndividualEvaluator getOurIndEval() {
		return ourIndEval;
	}

	public static void setOurIndEval(IndividualEvaluator ourIndEval) {
		Generation.ourIndEval = ourIndEval;
	}

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

