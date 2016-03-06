package locoGP.fitness;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.SortedSet;

import locoGP.individual.Individual;
import locoGP.util.Logger;

import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultBase;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector;
import de.uka.ipd.sdq.ByCounter.execution.ExecutionSettings;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;
import de.uka.ipd.sdq.ByCounter.utils.InvocationResultData;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;


public class OpCodeCounter {

	Individual ind = null;
	
	public OpCodeCounter( Individual ind){
		this.ind = ind;
	}
	
	public synchronized void run(){
		// Reimplementation of the fitness measure. Use ByCounter instead of timing the function.
		BytecodeCounter counter = new BytecodeCounter();
		String className = ind.getClassName();
		ind.setRunninTimeToZero();

		MethodDescriptor ourMD =new MethodDescriptor( className, ind.ourProblem.getMethodSignature()); 
		//"public static void sort(java.lang.Integer[] a,java.lang.Integer length)");
				
		counter.addEntityToInstrument(ourMD);
		counter.setClassesToInstrument(ind.getClassByteMap());
		counter.getInstrumentationParams().setInstrumentRecursively(true);
		
		Logger.logDebugConsole(" ----- About to instrument "+ind.getClassName());
		counter.instrument(); //
		Logger.logDebugConsole(" ----- Just instrumented "+ind.getClassName());			
		
		Object[] _args = null ;
		long instructionCount=0;

			int functionalityScore = 0;
			for (int testIndex = 0; testIndex < ind.ourProblem.getNumTests(); testIndex++) {

				instructionCount=0;
					CountingResultCollector.getInstance().clearResults(); // if this thread was stopped previously, then results will not have been cleared
					
					_args = ind.ourProblem.getTestData()[testIndex].getTest(); 
					//System.gc();
					InvocationResultData exeResult = null;
					//System.out.println("Before: "+arrayPrinter((Integer[])_args[0]));
					try{
						Logger.logDebugConsole(" ----- About to execute instrumented version of "+ind.getClassName());
						exeResult =counter.execute(ourMD, _args); 
						Logger.logDebugConsole(" ----- Successful execution of instrumented version of "+ind.getClassName());
						//exeResult = counter.execute(ourMD, targetClass, _args); // this is for classes we want bycounter to instatiate (not static)
						//result = (Integer) m.invoke(null, _args); // null is only for static methods			
						//ExecutionSettings eSettings = counter.getExecutionSettings();					
					} catch (Exception e){ // what do we do here? if the program has runtime issues then we cannot gather instruction count!
						// Infinite loop, or broken runtime
						Logger.logDebugConsole(" ---- Compiled code ("+ind.getClassName()+") threw an error at index "+testIndex);
						estimateFitness(testIndex,functionalityScore);
						return; // make sure this thread stops if this happens, otherwise we get orphaned threads!
						// Should we use a flag for this? is there anything left below that we want to run? no.
					}
					
					SortedSet<CountingResult> results =
						CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults();

					for(CountingResultBase r : results) {
						instructionCount +=r.getTotalCount(true); //r.getTotalCountInclInvokes();
					}
					CountingResultCollector.getInstance().clearResults();
					//CountingResultCollector.
					ind.addBestTime(instructionCount); // put the instruction count into the time
					functionalityScore += ind.ourProblem.getTestData()[testIndex].checkAnswer(exeResult.returnValue);
					results = null;
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
