package locoGP.experiments;

import java.util.logging.LogManager;

import locoGP.locoGP;
import locoGP.util.Logger;

public class GPConfig {
	
	boolean debug = true;

	int populationSize , 
			numGenerations , 
			tournamentSize = 2,
			threadPoolSize  ;

	private float overProvisioningRatio = 1.5f; // at least 1/3 of programs fail to compile
	
	boolean singlePointEnforced = true; // true - always true until multipoint is implemented
	boolean pickBestLocation = false; // do we select nodes to apply operators to?
	boolean updateLocationBias = true; // should we update the bias at locations which have been changed? 
	
	public boolean isUpdateLocationBias() { 
		return updateLocationBias;
	}

	public int getThreadPoolSize(){
		return threadPoolSize;
	}

	private void setUpdateLocationBias(boolean updateLocationBias) {
		this.updateLocationBias = updateLocationBias;
	}

	String experimentDescription ="";
		
	public String getExperimentDescription() {
		// TODO Improve logging of experiment config settings
		experimentDescription +="Tournament size: " +tournamentSize +"\n";
		
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
		
		return experimentDescription;
	}

	public GPConfig(int populationSize, int numGenerations, int tournamentSize, boolean pickBestLocation, boolean updateLocationBias, boolean debugFlag) {
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
	}
		
	public boolean isPickBestLocation() { // should we always tournament for the best node to pick?
		return pickBestLocation;
	}

	private void setPickBestLocation(boolean pickBestLocation) {
		this.pickBestLocation = pickBestLocation;
	}

	public static void turnOffLogging(){
		LogManager.getLogManager().reset();
		java.util.logging.Logger globalLogger = java.util.logging.Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
		globalLogger.setLevel(java.util.logging.Level.SEVERE); //SEVERE OFF);
		Logger.setLoggingEnabled(false);
	}
	
	public boolean isSinglePointEnforced() {
		return singlePointEnforced;
	}
	
	public int getTournamentSize() {
		return tournamentSize;
	}

	private void setTournamentSize(int tournamentSize) {
		this.tournamentSize = tournamentSize;
	}

	public int getNumGenerations() {
		return numGenerations;
	}

	private void setNumGenerations(int numGenerations) {
		this.numGenerations = numGenerations;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	private void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	public float getOverProvisioningRatio() {
		return this.overProvisioningRatio;
	}

	public void setOverProvisioning(float newRatio) {
		if(newRatio > 5)
			newRatio =5;
		else
			newRatio=newRatio*1.1f;
		this.overProvisioningRatio = newRatio;		
	}
}
