package locoGP.experiments;

import java.lang.reflect.Field;
import java.util.logging.LogManager;

import locoGP.locoGP;
import locoGP.fitness.IndividualEvaluator;
import locoGP.fitness.bytecodeCount.ByteCodeIndividualEvaluator;
import locoGP.operators.StatementOnlyCrossoverOperator;
import locoGP.util.Logger;

public class GPConfig {
	
	private boolean UseDiverseElitismFine  = false;

	private static boolean debug = false;

	private boolean singlePointEnforced = true;

	private boolean pickBestLocation = true;

	private boolean updateLocationBias = true;

	private boolean createFirstGenFromExhaustivelyMutatingSeed=false;

	private boolean setSeedBiasFromDeletionAnalysis=false;

	private boolean setSeedBiasFromPerfRedFreqOverCompFreq=false;

	private boolean updateSeedBiasFromPerfRedFreqOverCompFreq=false;

	private static boolean fineGranularityChange=true;

	private static boolean linkNewChildBiasDataWithParent = false;

	private static boolean referenceChildBiasToParentData = false;
	
	private int populationSize =100, 
			numGenerations =100, 
			tournamentSize = 2 ;

	private static int threadPoolSize = 10;
	
	private double elitismRate = .30,
			initialPopulationSeedRatio = .1,
			overProvisioningRatio = 1.1; //1.5; // at least 1/3 of programs fail to compile

	private boolean useDiverseElitism = false;

	public StatementOnlyCrossoverOperator xoverOperator = null;

	private IndividualEvaluator individualEvaluator;
	
	public static boolean useFineGranularityChange(){ // TODO make all static?
		return fineGranularityChange;
	}
	
	public static void setFineGranularityChange(boolean fGC){
		 fineGranularityChange = fGC;
	}
	
	public double getInitialPopulationSeedRatio(){
		return this.initialPopulationSeedRatio;
	}
	
	public boolean isUpdateLocationBias() { 
		return updateLocationBias;
	}

	public int getThreadPoolSize(){
		return threadPoolSize;
	}
	
	public void setThreadPoolSize(int threadPS){
		threadPoolSize = threadPS;
	}

	public void setUpdateLocationBias(boolean updateLocationBias) {
		this.updateLocationBias = updateLocationBias;
	}

	public String getExperimentDescription() {
		// TODO Improve logging of experiment config settings
		/*experimentDescription +="Tournament size: " +tournamentSize +"\n";
		
		if(singlePointEnforced)
			experimentDescription+="Single point (node) selection is enforced, per individual modification decision\n"
				+ "Rates are set for Crossover (20%) and Mutation (80%)\n" 
				;
		else
			experimentDescription+="Multi-point (node) selection is enabled, per line modification decision \n"
				+ "each line has 58/1000 by default chance of being selected for crossover and mutation.\n"
				+ "Default probability multiplier per line is (1)\n (broken!)\n"
				+ " Mutation and Crossover rates can vary randomly \n";
		
		if(pickBestLocation){
			experimentDescription+="We pick a location (node) to apply operator to by picking the node with the highest value (location probability is considered)\n";
			experimentDescription+="Bias propogates back into the parent!\n";
			experimentDescription += "\nUsing Gaussian noise to update nodes. (No rules)\n";
			
		}else
			experimentDescription+="We pick a location (node) to apply operator to randomly, static bias has been applied by hand\n";

		experimentDescription+="setSeedBiasFromDeletionAnalysis "+setSeedBiasFromDeletionAnalysis+"\n";
		experimentDescription+="setSeedBiasFromPerfRedFreqOverCompFreq "+setSeedBiasFromPerfRedFreqOverCompFreq+"\n";
		experimentDescription+="createFirstGenFromExhaustivelyMutatingSeed "+createFirstGenFromExhaustivelyMutatingSeed+"\n";*/
		String experimentDescription ="locoGP experiment settings: \n";
		Object value = null;
		for (Field field : this.getClass().getDeclaredFields()) {
		    field.setAccessible(true);
		    String name = field.getName();
		    
			try {
				value = field.get(this);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    experimentDescription += name +": "+ value.toString()+"\n";
		}
		
		return experimentDescription;
	}

	/*public GPConfig(int populationSize, int numGenerations, int tournamentSize, boolean pickBestLocation, boolean updateLocationBias, boolean debugFlag) {
		this.debug = debugFlag;
		if( debug ){
			threadPoolSize = 1 ; 
			Logger.logDebugConsole(" Debug Logging is on! ---------------------------------------------------------------");
			Logger.logAll("Debug on! ---------------------------------------------------------------");
		}else{
			turnOffLogging();
			threadPoolSize = (int) Runtime.getRuntime().availableProcessors()/2; 
		}
		setPopulationSize(populationSize);
		setNumGenerations(numGenerations);
		setTournamentSize(tournamentSize);
		setPickBestLocation(pickBestLocation);
		setUpdateLocationBias(updateLocationBias);
	}*/
	
	public GPConfig() {
		// Defaults
	}

    	public void setDebug(){
		threadPoolSize = 1 ; 
		Logger.logDebugConsole(" Debug Logging is on! ---------------------------------------------------------------");
		Logger.logAll("Debug on! ---------------------------------------------------------------");
		this.debug = true;
	}
    
	public void checkDebug(){
		if (debug) {
			Logger.logDebugConsole(" Debug on! ---------------------------------------------------------------");
			Logger.logAll("Debug on! ---------------------------------------------------------------");
		}
	}
	
	public boolean debug(){
		return this.debug;
	}
		
	public boolean isPickBestLocation() { // should we always tournament for the best node to pick?
		return pickBestLocation;
	}

	public void setPickBestLocation(boolean pickBestLocation) {
		this.pickBestLocation = pickBestLocation;
	}

	public static void turnOffDebugLogging(){
		LogManager.getLogManager().reset();
		java.util.logging.Logger globalLogger = java.util.logging.Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
		globalLogger.setLevel(java.util.logging.Level.SEVERE); //SEVERE OFF);
		Logger.disableDebugLogging();
		threadPoolSize = 1;
		debug = false;
	}
	
	public boolean isSinglePointEnforced() {
		return singlePointEnforced;
	}
	
	public int getTournamentSize() {
		return tournamentSize;
	}

	public void setTournamentSize(int tournamentSize) {
		this.tournamentSize = tournamentSize;
	}

	public int getNumGenerations() {
		return numGenerations;
	}

	public void setNumGenerations(int numGenerations) {
		this.numGenerations = numGenerations;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	public double getOverProvisioningRatio() {
		return this.overProvisioningRatio;
	}

	public void setOverProvisioning(float newRatio) {
		if(newRatio > 5)
			newRatio =5;
		else
			newRatio=newRatio*1.1f;
		this.overProvisioningRatio = newRatio;		
	}

	public void setExhaustiveMutateFirstGen() {
		this.createFirstGenFromExhaustivelyMutatingSeed=true;
	}

	public boolean isFirstGenCreatedFromExhaustive() {
		return this.createFirstGenFromExhaustivelyMutatingSeed;
	}

	public String getConfigDetailsString() {
		return "pop: " + getPopulationSize() + 
				" Gens: " + getNumGenerations() + 
				" Tourney: " + getTournamentSize() + 
				" FirstGenCreatedFromExhaustiveChange: " + createFirstGenFromExhaustivelyMutatingSeed +
				" PickBestLocationPerBias: " +pickBestLocation +
				" UpdateBias: "+ this.updateLocationBias;
	}

	public void setSeedBiasFromDeletionAnalysis() {
		this.setSeedBiasFromDeletionAnalysis=true;
	}
	public boolean issetSeedBiasFromDeletionAnalysis(){
		return this.setSeedBiasFromDeletionAnalysis;
	}

	public void setSeedBiasFromPerfRedFreqOverCompFreq() {
		this.setSeedBiasFromPerfRedFreqOverCompFreq=true;
	}
	public boolean issetSeedBiasFromPerfRedFreqOverCompFreq() {
		return this.setSeedBiasFromPerfRedFreqOverCompFreq;
	}
	
	public boolean isupdateSeedBiasFromPerfRedFreqOverCompFreq() {
		return this.updateSeedBiasFromPerfRedFreqOverCompFreq;
	}

	public void updateSeedBiasFromPerfRedFreqOverCompFreq() {
		this.setSeedBiasFromPerfRedFreqOverCompFreq=true;
		this.updateSeedBiasFromPerfRedFreqOverCompFreq=true;
	}

	public void setLinkNewChildBiasDataWithParent() {
		GPConfig.linkNewChildBiasDataWithParent =true;// TODO Auto-generated method stub
	}
	
	public static boolean getLinkNewChildBiasDataWithParent() {
		return linkNewChildBiasDataWithParent;// TODO Auto-generated method stub
	}

	public void setUseDiverseElitismFine() {
		// Elitism which takes only one of each program for each of the top fitness values
		// This works at a fine level, so any decimal difference in fitness is distinct and 
		this.UseDiverseElitismFine = true;	
		if (this.useDiverseElitism) // surely a better way TODO replace with single Elitism selector variable
			throw new IllegalStateException("Multiple Elitism Settings used");	
	}
	public boolean getUseDiverseElitismFine() {
		// Elitism which takes only one of each program for each of the top fitness values
		// This works at a fine level, so any decimal difference in fitness is distinct and 
		return this.UseDiverseElitismFine;	
	}

	public void setReferenceChildBiasToParentData() {
		this.referenceChildBiasToParentData  = true;
	}
	public static boolean getReferenceChildBiasToParentData() {
		return referenceChildBiasToParentData;
	}

	public double getElitismRate() {
		// TODO Auto-generated method stub
		return this.elitismRate;
	}
	
	public void setElitismRate(double rate) {
		this.elitismRate = rate;
	}
	
	public void setUseDiverseElitism() {
		this.useDiverseElitism = true;
		if (this.UseDiverseElitismFine)
			throw new IllegalStateException("Multiple Elitism Settings used");	
	}
	public boolean useDiverseElitism() {
		return this.useDiverseElitism ;
	}

	public void setEvaluator(
			IndividualEvaluator individualEvaluator) {
		this.individualEvaluator = individualEvaluator;
	}
	public IndividualEvaluator getEvaluator() {
		return this.individualEvaluator ;
	}
}
