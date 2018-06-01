package locoGP.fitness.runTimer;


import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import locoGP.fitness.PerformanceMeasure;
import locoGP.individual.Individual;
import locoGP.problems.tests.TestCase;
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
		
		int runNums = 100000; // call the function this many times, 1 of them should be close to best performance.
		
		Method m = ind.getEntryMethod();
		
		Object[] _args = null ;
		
		//ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
		//if(!bean.isCurrentThreadCpuTimeSupported())
		//	System.out.println("Thread CPU time not supported! No fitness!");
		long bestTime = (long)999999999*10;
		//int functionalityScore = 0;
		Object returnVal = null;
		boolean runtimeError = false;
		for (int testIndex = 0; testIndex < ind.ourProblem.getNumTests(); testIndex++) {
			bestTime = (long)999999999*10;	
			runtimeError = false;
			TestCase aTestCase = ind.ourProblem.getTestData()[testIndex];
			_args = aTestCase.getTest(); //new Object[] { testArray };
			for (int i = 0; i < runNums; i++) {
				//System.out.println("Before: "+arrayPrinter((Integer[])_args[0]));
				try{
				startTime = System.nanoTime();
				//startTime = bean.getCurrentThreadUserTime( ) ;
					
				// __________________________Timezone_______________________________________________________
				returnVal = m.invoke(null, _args); // sort doesnt return! // null is for static methods !	|
				// ________________________________________________________________________________________/
				stopTime = System.nanoTime();

				} catch (RuntimeException e){
					Logger.logDebugConsole(" ---- Continuing with tests: Compiled code ("+ind.getClassName()+") threw runtime error at index "+testIndex+":\n"+e.getMessage());
					runtimeError = true;
					return;
					
				} catch (Exception e){
					Logger.logDebugConsole(" ---- Skipping remaining tests - Compiled code ("+ind.getClassName()+") threw an error at index "+testIndex+":\n"+e.getMessage());
					// estimateFitness(testIndex,functionalityScore); this never runs!
					// estimateFitness(testIndex,functionalityScore);
					//e.printStackTrace();
					stopTime = System.nanoTime();
					return; // make sure this thread stops if this happens, otherwise we get orphaned threads!
				}
				
				//System.out.println("After : "+arrayPrinter((Integer[])_args[0]));
				/*if ((stopTime - startTime) < bestTime && (stopTime - startTime) >0)
					 // discard any value that returns 0, this is caused when using getCurrentThreadUserTime
					 // http://www-01.ibm.com/support/docview.wss?uid=swg1IV21775
					bestTime = (stopTime - startTime); 
				*/
				ind.addBestTime(stopTime - startTime);	
				if ( aTestCase.checkAnswer(returnVal) > 0) // if it's got error, no point benchmarking
					runNums = 1;
			}

			//ind.addBestTime(bestTime);
			int errorCount = aTestCase.checkAnswer(returnVal);
			ind.setTestResult(testIndex, errorCount,runtimeError);
			
		}
					
	}
	
/*	private void estimateFitness(int testIndex, int functionalityScore) {
		long instructionCount=0;
		int numRunsMissing = ind.ourProblem.getNumTests()-testIndex ;
		if(testIndex >0){ // If the program ran for an input, but broke on subsequent, estimate the rest
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
			 
		// if we are missing runs, just assume they are twice as bad as the seed..
		instructionCount += (long) (ind.ourProblem.getBaselineRuntimeAvg()*2)*numRunsMissing; // bit arbitrary	
		functionalityScore += (ind.ourProblem.getSeedFunctionalityScore()*2)*numRunsMissing;
		//}
		ind.addBestTime(instructionCount); // Try to just put the instruction count into the time
		ind.setFunctionalityScore(functionalityScore);
	}*/
}
