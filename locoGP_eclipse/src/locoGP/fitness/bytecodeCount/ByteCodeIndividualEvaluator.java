package locoGP.fitness.bytecodeCount;

import java.lang.reflect.Method;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import locoGP.fitness.IndividualEvaluator;
import locoGP.individual.Individual;
import locoGP.roughwork.SortTester;
import locoGP.util.Logger;
import locoGP.util.StringCompiler;

 
public class ByteCodeIndividualEvaluator implements java.io.Serializable, IndividualEvaluator{
	/*StringCompiler ourJCompiler = new StringCompiler();
	int[] testArray = SortTester.getTestArray();*/

	/* (non-Javadoc)
	 * @see locoGP.fitness.IndividualEvaluatorI#evaluateInd(locoGP.individual.Individual)
	 */
	@Override
	public boolean evaluateInd(Individual ind){
		StringCompiler ourJCompiler = new StringCompiler();
		if( ourJCompiler.compileClass( ind ) ){
			ind.setCompiled();
			evaluate(  ind ); // this has a timeout
			return true;
		}else{
			//System.out.print(ind.getClassName() + ": no compile" );
			return false;
		}
	}
	
	/* (non-Javadoc)
	 * @see locoGP.fitness.IndividualEvaluatorI#evaluateIndNoTimeLimit(locoGP.individual.Individual)
	 */
	@Override
	public boolean evaluateIndNoTimeLimit(Individual ind){
		StringCompiler ourJCompiler = new StringCompiler();
		/*Logger.logDebugConsole(" ----- About to instrument "
				+ ind.getClassName());
		ASTInstrumenter.instrumentForOverallExecutionCount(ind);
		Logger.logDebugConsole(" ----- Just instrumented " + ind.getClassName());*/
		if( ourJCompiler.compileClass( ind ) ){
			evaluateNoTimeLimit(  ind );
			//ASTInstrumenter.deInstrumentForOverallExecutionCount(ind);
			return true;
		}else
			return false;
	}
	
	private void evaluateNoTimeLimit(Individual ind){
		// no thread used...		
		OpCodeCounter rTimer = new OpCodeCounter(ind);
		rTimer.runBasicCounter();
	}
	
	private void evaluate(Individual ind){  // This needs to be executed in a thread
		Logger.logTrash("\nRun Candidate-------------\n" +ind.ASTSet.getCodeListing());
		OpCodeCounter rTimer = new OpCodeCounter(ind);
		rTimer.start();
		try {
			// TODO Evaluation thread timeout should be set to ?double? the seed value
			//rTimer.join(30000); // 7.5 minutes needed to evaluate ascon on 65k chars on an i7 :/
			rTimer.join(30000); 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ind.addBestTime(9999999999999L);
		}
		while(rTimer != null && rTimer.isAlive()){ // unbelievable
			
			try {
				rTimer.stop();
				/*rTimer.join(500);
				//rTimer.destroy();
				rTimer.currentThread().destroy();
				//Thread.sleep(500); // give it a second to die
*/			} catch (Exception e) { // edited 
				// really not cool if we get here
				// actually ok, destroy is not implemented
				rTimer.interrupt();
				Logger.logSevere("Error trying to stop thread");
				e.printStackTrace();
			}
			rTimer=null;
			//rTimer.destroy();
			//System.out.println("Infinite loop stopped");
			Logger.logTrash("Attempted to stop infinite loop");
			//ind.addBestTime(999999999*10); 
		}
		ind = null;
		System.gc();
	}
	
	String arrayPrinter(Integer[] iA){
		String ret = " ";
		for (int i = 0 ; i< iA.length; i++){
			ret+= " "+ iA[i];
		}
		return ret;
	}
}
