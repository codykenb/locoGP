package locoGP.fitness;


import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import locoGP.individual.Individual;

// Old code to get wall-clock time of method execution
public class RunTimer{
	Individual ind = null;
	RunTimer( Individual ind){
		this.ind = ind;
	}
	
	public void run(){
		/*
		 * set the timer // start the thread // if the thread runs for longer
		 * than 1 second, stop it. // Do we want to test for an infinite loop
		 * for each test? or 1 test for the lot?
		 */
		long startTime = System.nanoTime();
		long stopTime = System.nanoTime();
		
		int runNums = 3; // call the function this many times, 1 of them should be close to best performance.
		
		Method m = ind.getEntryMethod();
		
		
		Object[] _args = null ;
		Integer[] result=null;
		
		ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
		if(!bean.isCurrentThreadCpuTimeSupported())
			System.out.println("Thread CPU time not supported! No fitness!");
		try {
			long bestTime = (long)999999999*10;
			int functionalityScore = 0;
			/*
			 * Warmup needed?
			 * for (int testIndex = 0; testIndex < Triangle2Example.getNumTests(); testIndex++) {
				for (int i = 0; i < runNums; i++) {
					_args = Triangle2Example.testData[testIndex].getTest(); // new
																			// Object[]
																			// {
																			// testArray
																			// };
					m.invoke(null, _args);
				}
			}*/
			
			for (int testIndex = 0; testIndex < ind.ourProblem.getNumTests(); testIndex++) {
				for (int i = 0; i < runNums; i++) {
					_args = ind.ourProblem.getTestData()[testIndex].getTest(); //new Object[] { testArray };
					System.gc();
					
					//System.out.println("Before: "+arrayPrinter((Integer[])_args[0]));
					try{
					//startTime = System.nanoTime();
					startTime = bean.getCurrentThreadUserTime( ) ;
						
					// __________________________Timezone_______________________________________________________
					m.invoke(null, _args); // sort doesnt return! // null is only for static methods !!!!!!		|
					// ________________________________________________________________________________________/
					stopTime = bean.getCurrentThreadUserTime( ) ;
					//stopTime = System.nanoTime();
					/*
					 * These are exceptions that the code is allowed to throw.
					 * e.g. array out of bounds exception
					 * An exception that is caused by ThreadDeath should not be allowed. 
					 */
					/*} catch (IllegalArgumentException e) { 
						System.out.println("Compiled code threw an error ");
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						System.out.println("Compiled code threw an error ");
						e.printStackTrace();*/
					} catch (Exception e){
						System.out.println("Compiled code threw an error ");
						//e.printStackTrace();
						stopTime = System.nanoTime();
						if(e.getCause() instanceof java.lang.ThreadDeath)
							throw e; // this will break out to the catch clause below.
					}
					
					//System.out.println("After : "+arrayPrinter((Integer[])_args[0]));
					if ((stopTime - startTime) < bestTime && (stopTime - startTime) >0)
						/* discard any value that returns 0, this is caused when using getCurrentThreadUserTime
						 * http://www-01.ibm.com/support/docview.wss?uid=swg1IV21775
						 */
						bestTime = (stopTime - startTime);
				}

				// we have best time, and the result, how do we combine these to
				// give fitness?
				ind.addBestTime(bestTime); //broken here
				/*if(ind.ourProblem.getTestData()[testIndex].checkAnswer(result))
					functionalityScore ++;*/
				functionalityScore += ind.ourProblem.getTestData()[testIndex].checkAnswer((Integer[])_args[0]);
				
			}
			
			ind.setFunctionalityScore((long)functionalityScore);
			
		}catch (Exception e){
			System.out.println("Thread killed due to infinite loop: ");
			e.printStackTrace();
		}	
	}
}
