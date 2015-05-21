package locoGP.fitness;

import java.lang.reflect.Method;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import locoGP.individual.Individual;
import locoGP.roughwork.SortTester;
import locoGP.util.Logger;
import locoGP.util.StringCompiler;

 
public class IndividualEvaluator implements java.io.Serializable{
	StringCompiler ourJCompiler = new StringCompiler();
	int[] testArray = SortTester.getTestArray();

	public boolean evaluateInd(Individual ind){
		if( ourJCompiler.compileClass( ind ) ){
			evaluate(  ind ); // this has a timeout
			return true;
		}else{
			System.out.println("Debug: no compile" );
			return false;
		}
	}
	
	public boolean evaluateIndNoTimeLimit(Individual ind){
		if( ourJCompiler.compileClass( ind ) ){
			evaluateNoTimeLimit(  ind );
			return true;
		}else
			return false;
	}
	
	private void evaluateNoTimeLimit(Individual ind){
		// no thread used...		
		RunFitnessFunction rTimer = new RunFitnessFunction(ind);
		rTimer.runBasicCounter();
	}
	
	private synchronized void evaluate(Individual ind){  // This needs to be executed in a thread
		Logger.logTrash("\nRun Candidate-------------\n" +ind.ASTSet.getCodeListing());
		RunFitnessFunction rTimer = new RunFitnessFunction(ind);
		rTimer.start();
		try {
			rTimer.join(15000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ind.addBestTime(9999999999999L);
		}
		if(rTimer.isAlive()){
			rTimer.stop();
			try {
				rTimer.join(500);
				rTimer.destroy();
				//Thread.sleep(500); // give it a second to die
			} catch (Throwable e) { // edited 
				// really not cool if we get here
				// actually ok, destroy is not implemented
				Logger.logSevere("thread stopped, destroyed then threw error (increase join timeout?)");
				e.printStackTrace();
			}
			//rTimer.destroy();
			//System.out.println("Infinite loop stopped");
			Logger.logTrash("Infinite loop stopped");
			ind.addBestTime(999999999*10); 
		}
		//System.gc();
	}
	
	String arrayPrinter(Integer[] iA){
		String ret = " ";
		for (int i = 0 ; i< iA.length; i++){
			ret+= " "+ iA[i];
		}
		return ret;
	}
}
