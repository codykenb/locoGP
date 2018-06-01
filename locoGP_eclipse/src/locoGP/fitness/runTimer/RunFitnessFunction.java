package locoGP.fitness.runTimer;

import locoGP.fitness.PerformanceMeasure;
import locoGP.individual.Individual;
import locoGP.util.Logger;

public class RunFitnessFunction extends Thread{

	PerformanceMeasure perfCounter ; 
	
	private boolean running = false;
	boolean stayAlive = true;
	
	public RunFitnessFunction( Individual ind){
		setIndividualToEvaluate(ind);
	}
	
	public void setIndividualToEvaluate( Individual ind){
		running = true;
		/*if(Individual.ourProblem.gpConfig.useByteCodeCounter())
			perfCounter = new OpCodeCounter(ind);
		else*/
		perfCounter = new RunTimer(ind);

	}
	
	public  void runBasicCounter(){
		try {
			perfCounter.measureAllTests();
		} catch (Exception e) {
			Logger.logDebugConsole("Thread died (possibly infinite loop?): ");
			e.printStackTrace();
		}
	}
	
	public void run(){
		//runFancy();
		runBasicCounter();
	}
	/*public void runFancy(){
		
		while(stayAlive){
			// Reimplementation of the fitness measure. Use ByCounter instead of
			// timing the function.
			if (opCounter != null) {
				runBasicCounter();
			}
			
			opCounter=null;
			
						
			try {
				synchronized (this) {
					this.notify();
					running = false;
					while(opCounter==null && stayAlive){ // wait some more if theres no work to do
						running = false;
						this.wait();
					}
					running = true;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}*/
	
	public boolean isRunning(){
		return running;
	}	
}
