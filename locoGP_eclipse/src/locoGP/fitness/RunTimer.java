package locoGP.fitness;


import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import locoGP.individual.Individual;
import locoGP.util.Logger;

// get wall-clock time of method execution
public class RunTimer implements PerformanceMeasure{
	Individual ind = null;
	
	public RunTimer( Individual ind){
		this.ind = ind;
	}
	
	/* (non-Javadoc)
	 * @see locoGP.fitness.PerformanceMeasure#measureAllTests()
	 */
	@Override
	public void measureAllTests(){
		
		long startTime = System.nanoTime();
		long stopTime = System.nanoTime();
		
		int runNums = 3; // call the function this many times, 1 of them should be close to best performance.
		
		Method m = ind.getEntryMethod();
		
		Object[] _args = null ;
		
		ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
		if(!bean.isCurrentThreadCpuTimeSupported())
			System.out.println("Thread CPU time not supported! No fitness!");
		long bestTime = (long)999999999*10;
		int functionalityScore = 0;
		
		for (int testIndex = 0; testIndex < ind.ourProblem.getNumTests(); testIndex++) {
			for (int i = 0; i < runNums; i++) {
				_args = ind.ourProblem.getTestData()[testIndex].getTest(); //new Object[] { testArray };

				
				//System.out.println("Before: "+arrayPrinter((Integer[])_args[0]));
				try{
				//startTime = System.nanoTime();
				startTime = bean.getCurrentThreadUserTime( ) ;
					
				// __________________________Timezone_______________________________________________________
				m.invoke(null, _args); // sort doesnt return! // null is only for static methods !!!!!!		|
				// ________________________________________________________________________________________/
				stopTime = bean.getCurrentThreadUserTime( ) ;

				} catch (Exception e){
					
					Logger.logDebugConsole(" ---- Compiled code ("+ind.getClassName()+") threw an error at index "+testIndex);
					estimateFitness(testIndex,functionalityScore);
					//e.printStackTrace();
					stopTime = System.nanoTime();
					return;
				}
				
				//System.out.println("After : "+arrayPrinter((Integer[])_args[0]));
				if ((stopTime - startTime) < bestTime && (stopTime - startTime) >0)
					/* discard any value that returns 0, this is caused when using getCurrentThreadUserTime
					 * http://www-01.ibm.com/support/docview.wss?uid=swg1IV21775
					 */
					bestTime = (stopTime - startTime);
			}

			ind.addBestTime(bestTime);
			functionalityScore += ind.ourProblem.getTestData()[testIndex].checkAnswer((Integer[])_args[0]);
			
		}
		
		ind.setFunctionalityScore((long)functionalityScore);
			
	}
	
	private void estimateFitness(int testIndex, int functionalityScore) {
		long instructionCount=0;
		int numRunsMissing = ind.ourProblem.getNumTests()-testIndex ;
		/*if(testIndex >0){ // If the program ran for an input, but broke on subsequent, estimate the rest
			SortedSet<CountingResult> results =
				CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults();
		
			for(CountingResultBase r : results) {
				//r.logResult(false, true);
				instructionCount +=r.getTotalCount(true); //r.getTotalCountInclInvokes();
			}
			// the numbers we have are only partial, maybe 3/5ths of what it should be?
			// so multiply the values by 2/5ths
			instructionCount += instructionCount*((ind.ourProblem.getNumTests()-testIndex)/ind.ourProblem.getNumTests());
			functionalityScore += functionalityScore*((ind.ourProblem.getNumTests()-testIndex)/ind.ourProblem.getNumTests());
		}else{ // if the program compiled, but didnt run at all, give it a score
			
			 * Use the original individual for guidance, it shouldnt be better than original. 
			 what should this be really?
			 */
		// if we are missing runs, just assume they are twice as bad as the seed..
		instructionCount += (long) (ind.ourProblem.getBaselineRuntimeAvg()*2)*numRunsMissing; // bit arbitrary	
		functionalityScore += (ind.ourProblem.getSeedFunctionalityScore()*2)*numRunsMissing;
		//}
		ind.addBestTime(instructionCount); // Try to just put the instruction count into the time
		ind.setFunctionalityScore(functionalityScore);
	}
}
