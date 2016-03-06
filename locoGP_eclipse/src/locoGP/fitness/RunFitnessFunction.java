package locoGP.fitness;

import locoGP.individual.Individual;
import locoGP.util.Logger;

public class RunFitnessFunction extends Thread{

	//RunTimer opCounter ; // old code to get wall-clock time
	OpCodeCounter opCounter ;
	private boolean running = false;
	boolean stayAlive = true;
	
	public RunFitnessFunction( Individual ind){
		setIndividualToEvaluate(ind);
		//opCounter = new RunTimer(ind);
	}
	
	public void setIndividualToEvaluate( Individual ind){
		running = true;
		opCounter = new OpCodeCounter(ind);
	}
	
	public  void runBasicCounter(){
		try {
			opCounter.run();
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