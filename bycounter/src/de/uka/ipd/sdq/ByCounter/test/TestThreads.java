package de.uka.ipd.sdq.ByCounter.test;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.asm.Opcodes;

import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector;
import de.uka.ipd.sdq.ByCounter.instrumentation.EntityToInstrument;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedCodeArea;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedMethod;
import de.uka.ipd.sdq.ByCounter.parsing.LineNumberRange;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;
import de.uka.ipd.sdq.ByCounter.test.framework.expectations.Expectation;
import de.uka.ipd.sdq.ByCounter.test.framework.expectations.SectionExpectation;
import de.uka.ipd.sdq.ByCounter.test.helpers.RunnableForThreading;
import de.uka.ipd.sdq.ByCounter.test.helpers.RunnableIinc;
import de.uka.ipd.sdq.ByCounter.test.helpers.ThreadedTestSubject;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * This test suite tests ByCounter when instrumenting multi-threaded 
 * applications.
 *
 * @since 0.1
 * @version 2.0
 * @author Martin Krogmann
 */
@RunWith(Parameterized.class)
public class TestThreads extends AbstractByCounterTest {
	/** Refers to {@link RunnableForThreading#run()}. */
	private static MethodDescriptor methodRunnableForThreadingRun;
	/** Refers to {@link RunnableIinc#run()}. */
	private static MethodDescriptor methodRunnableIincRun;
	/** Refers to {@link ThreadedTestSubject#runThreads()}. */
	private static MethodDescriptor methodRun;
	
	static {
		methodRunnableForThreadingRun = new MethodDescriptor(
				RunnableForThreading.class.getCanonicalName(),
				"public void run()");
		methodRunnableIincRun = new MethodDescriptor(
				RunnableIinc.class.getCanonicalName(),
				"public void run()");
		methodRun = new MethodDescriptor(
				ThreadedTestSubject.class.getCanonicalName(),
				"public void runThreads()");
	}

    /**
     * This constructor is used by the {@link Parameterized} runner for running tests with different
     * parameters.
     *
     * @param params
     *            {@link InstrumentationParameters} template for the counting setup.
     */
    public TestThreads(final InstrumentationParameters params) {
        super(params);
    }

    /**
     * Tests the counting of user defined line number ranges while recording 
     * the order of execution.
     * This test case does not instrument the method that spawns the threads
     * (using Thread.start()). Therefore results are not expected in a thread 
     * structure.
     */
    @Test
    public void testInstrumentRunnable() {
        // define expectations
        Expectation e = new Expectation(true);
        // the thread is executed four times
        for(int i = 0 ; i < 4+1; i++) {
	        e.add(createExpectationsRunnableFTRun());
        }
		// initialize ByCounter
		BytecodeCounter counter = this.setupByCounter();
		
		counter.addEntityToInstrument(methodRunnableForThreadingRun);
		counter.instrument();
		
		// execute with ()
		Object[] executionParameters = new Object[0];
		counter.execute(methodRun, executionParameters);
		
		SortedSet<CountingResult> countingResults = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults();
		removeMethodCallsWithFrequency0(countingResults);
        
        // print ByCounter results
		CountingResult[] results = countingResults.toArray(new CountingResult[0]);
        for (CountingResult r : results) {
        	r.logResult(false, true);
        }
        // compare
        e.compare(results);
    }
    
    /**
     * Create the expectations for {@link #methodRunnableForThreadingRun}.
     */
    private SectionExpectation createExpectationsRunnableFTRun() {
    	return new SectionExpectation().add(Opcodes.LDC, 5)
        .add(Opcodes.ALOAD, 5)
        .add(Opcodes.ASTORE, 2)
        .add(Opcodes.DUP, 3)
        .add(Opcodes.LREM, 1)
        .add(Opcodes.GOTO, 1)
        .add(Opcodes.RETURN, 1)
        .add(Opcodes.GETSTATIC, 3)
        .add(Opcodes.INVOKEVIRTUAL, 13)
        .add(Opcodes.INVOKESPECIAL, 3)
        .add(Opcodes.INVOKESTATIC, 3)
        .add(Opcodes.NEW, 3)
        .add(Opcodes.LADD, 1)
        .add(Opcodes.GETFIELD, 1)
        .add("java.lang.Math", "public long abs(long l)", 1)
//        .add("java.lang.RuntimeException", "public void RuntimeException(java.lang.Throwable t)", 0) // This exception is part of the method, but never thrown. ExpectationsFramework does however not permit expectation 0 at this point.
        .add("java.lang.StringBuilder", "public StringBuilder(java.lang.String s)", 3)
        .add("java.lang.StringBuilder", "public java.lang.StringBuilder append(long l)", 1)
        .add("java.lang.StringBuilder", "public java.lang.StringBuilder append(java.lang.String s)", 4)
        .add("java.lang.StringBuilder", "java.lang.String toString()", 3)
        .add("java.lang.Thread", "public java.lang.Thread currentThread()", 1)
        .add("java.lang.Thread", "public long getId()", 1)
        .add("java.lang.Thread", "public java.lang.String getName()", 1)
        .add("java.lang.Thread", "public void sleep(long t)", 1)
        .add("java.util.Random", "public long nextLong()", 1)
        .add("java.util.logging.Logger" , "public void info(java.lang.String s)", 2)
        ;
	}

    /**
     * Create the expectations for {@link #methodRunnableIincRun}.
     */
    private SectionExpectation createExpectationsRunnableIincRun() {
    	SectionExpectation expectation = new SectionExpectation();
    	return expectation.add(Opcodes.ICONST_0, 1)
        .add(Opcodes.ISTORE, 1)
        .add(Opcodes.IINC, 1)
        .add(Opcodes.GETSTATIC, 1)
        .add(Opcodes.ILOAD, 1)
        .add(Opcodes.INVOKEVIRTUAL, 1)
        .add("java.io.PrintStream", "public void println(int i)", 1)
        .add(Opcodes.RETURN, 1)
        ;
	}
    

    /**
     * Create the expectations for {@link #methodRun}.
     * @param sectionNumber SectionNumber of the created SectionExpectation.
     */
    private SectionExpectation createExpectationsRunThreads(int sectionNumber) {
    	SectionExpectation e = new SectionExpectation(sectionNumber);
    	return e.add(Opcodes.GETSTATIC, 4)
		   	   	.add(Opcodes.LDC, 8)
		   	   	.add(Opcodes.INVOKEVIRTUAL, 22)
//		        .add("java.util.logging.Logger", "public long info(java.lang.String m)", 5)
		        .add(Opcodes.ICONST_0, 3)
		        .add(Opcodes.ANEWARRAY, 1)
		        .add(Opcodes.DUP, 20)
		        .add(Opcodes.NEW, 14)
		        .add(Opcodes.INVOKESPECIAL, 14)
		        .add("de.uka.ipd.sdq.ByCounter.test.helpers.RunnableForThreading", "public RunnableForThreading()", 5)
		        .add("java.lang.Thread", "public Thread(java.lang.Runnable r, java.lang.String s)", 2)
		        .add("java.lang.Thread", "public Thread(java.lang.Runnable r)", 4)
		        .add(Opcodes.AASTORE, 4)
		        .add(Opcodes.ICONST_1, 1)
		        .add(Opcodes.ICONST_2, 1)
		        .add(Opcodes.ICONST_3, 1)
		        .add(Opcodes.ICONST_4, 1)
		        .add(Opcodes.ASTORE, 13)
		        .add(Opcodes.ALOAD, 24)
		        .add(Opcodes.ARRAYLENGTH, 4)
		        .add(Opcodes.ISTORE, 4)
		        .add(Opcodes.GOTO, 2)
		        .add(Opcodes.ILOAD, 28)
		        .add(Opcodes.AALOAD, 8)
		        .add("java.lang.Thread", "public void start()", 6)
				.add(Opcodes.IINC, 8)
				.add(Opcodes.IF_ICMPLT, 10)
		        .add("java.lang.StringBuilder", "public StringBuilder(java.lang.String s)", 2)
		        .add("java.lang.Thread", "public void join()", 6)
		        .add("java.util.logging.Logger", "public void info(java.lang.String s)", 4)
		        .add("de.uka.ipd.sdq.ByCounter.test.helpers.RunnableIinc", "public RunnableIinc()", 1)
		        .add("java.lang.StringBuilder", "public java.lang.StringBuilder append(int i)", 2)
		        .add("java.lang.StringBuilder", "public java.lang.StringBuilder append(java.lang.String s)", 2)
		        .add("java.lang.StringBuilder", "public java.lang.String toString()", 2)
		        .add(Opcodes.RETURN, 1)
		        .addParallel(createExpectationsRunnableFTRun(),
		        		createExpectationsRunnableFTRun(),
		        		createExpectationsRunnableFTRun(),
		        		createExpectationsRunnableFTRun(),
		        		createExpectationsRunnableFTRun(),
							createExpectationsRunnableIincRun());
	}
    
	/**
     * Instrument the run method that spawns threads recursively and check 
     * for results. 
     * <p>
     * This currently demonstrates a shortcoming of recursive instrumentation.
     * ByCounter is unable to find that it needs to instrument implementations 
     * of {@link Runnable#run()}.
     * </p>
     */
    @Test
    public void testInstrumentRunRecursivly() {
		// initialize ByCounter
		BytecodeCounter counter = setupByCounter();
		counter.getInstrumentationParams().setInstrumentRecursively(true);
		counter.getExecutionSettings().setAddUpResultsRecursively(true);
		counter.addEntityToInstrument(methodRun);
		counter.instrument();
		
		Object[] executionParameters = new Object[0];
		counter.execute(methodRun, executionParameters);
		
		SortedSet<CountingResult> countingResults = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults();
		removeMethodCallsWithFrequency0(countingResults);

        // define expectations
        Expectation e = new Expectation(true);
        e.add(createExpectationsRunThreads(0));
        
        // print ByCounter results
        CountingResult[] results = countingResults.toArray(new CountingResult[0]);

        for (CountingResult r : results) {
        	r.logResult(false, true);
        }
        
        // This will fail because currently the threads run methods are not
        // being instrumented. Recursive instrumentation is unable to make the
        // connection between thread.start() and the triggered run method.
        e.compare(results);
    }
    
    /**
     * Test ByCounters ability to wait for spawned threads to finish. 
     * @throws InterruptedException 
     */
    @Test
    public void testJoinThreads() throws InterruptedException {
    	// initialize ByCounter
		BytecodeCounter counter = setupByCounter();
		counter.getInstrumentationParams().setProvideJoinThreadsAbility(true);
		MethodDescriptor runThreadsNoJoinMD = new MethodDescriptor(
				ThreadedTestSubject.class.getCanonicalName(), 
				"public void runThreadsNoJoin(long sleepBase)");
		counter.addEntityToInstrument(runThreadsNoJoinMD);
		counter.addEntityToInstrument(methodRunnableForThreadingRun);
		counter.instrument();
		
        // define expectations
        Expectation e = new Expectation(true);
        e.add()
        .add(Opcodes.LLOAD, 1)
        .add(Opcodes.ALOAD, 3)
        .add(Opcodes.ASTORE, 2)
        .add(Opcodes.DUP, 2)
        .add(Opcodes.RETURN, 1)
        .add(Opcodes.PUTFIELD, 1)
        .add(Opcodes.INVOKEVIRTUAL, 1)
        .add(Opcodes.INVOKESPECIAL, 2)
        .add(Opcodes.NEW, 2)
        .add(RunnableForThreading.class.getCanonicalName(), "public RunnableForThreading()", 1)
        .add(Thread.class.getCanonicalName(), "public Thread(java.lang.Runnable r)", 1)
        .add(Thread.class.getCanonicalName(), "public void start()", 1)
        .addParallel(this.createExpectationsRunnableFTRun());
        ;
		
		counter.getExecutionSettings().setWaitForThreadsToFinnish(false);
		long sleepTime = 0;
		Object[] executionParameters;
		long t;
		// make sure the execute call executes faster than the spawned thread
		// if joining does not work
		do {
			CountingResultCollector.getInstance().clearResults();
			sleepTime += 1000;
			executionParameters = new Object[] {sleepTime};
			t = System.currentTimeMillis();
			counter.execute(runThreadsNoJoinMD, executionParameters);
			t = Math.abs(t - System.currentTimeMillis());
		} while(t > sleepTime);
		CountingResultCollector.getInstance().joinSpawnedThreads();

        // compare results
        SortedSet<CountingResult> countingResults = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults();
        removeMethodCallsWithFrequency0(countingResults);
        CountingResult[] results = countingResults.toArray(new CountingResult[0]);
        e.compare(results);

		CountingResultCollector.getInstance().clearResults();
		counter.getExecutionSettings().setWaitForThreadsToFinnish(true);
		t = System.currentTimeMillis();
		counter.execute(runThreadsNoJoinMD, executionParameters);
		t = Math.abs(t - System.currentTimeMillis());
		Assert.assertTrue("Counter.execute must not return before spawned threads finished.", 
				t > sleepTime);
		
        // compare results
        countingResults = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults();
        removeMethodCallsWithFrequency0(countingResults);
        results = countingResults.toArray(new CountingResult[0]);
        e.compare(results);
    }
    
    /**
     * Tests for the correct structure of threaded counting results.
     */
    @Test
    public void testThreadStructure() {
    	// initialize ByCounter
		BytecodeCounter counter = setupByCounter();
		counter.addEntityToInstrument(methodRun);
		counter.addEntityToInstrument(methodRunnableForThreadingRun);
		counter.addEntityToInstrument(methodRunnableIincRun);
		counter.instrument();
		
		Object[] executionParameters = new Object[0];
		counter.execute(methodRun, executionParameters);
		
		SortedSet<CountingResult> countingResults = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults();
		removeMethodCallsWithFrequency0(countingResults);
        
        // check ByCounter results against expectations
        CountingResult[] results = countingResults.toArray(new CountingResult[0]);
        // we expect 1 result with 4 child thread results
		Expectation e = new Expectation(true);
		e.add(createExpectationsRunThreads(-1));
		e.compare(results);
		
		// output for debugging purposes
        for (CountingResult r : results) {
        	r.logResult(false, true);
        }
    }
    

    /**
     * Test for retrieving results when the executed method ends before all 
     * instrumented threads end.
     */
    @Test
    public void testThreadOverlap() {
    	// initialize ByCounter
		BytecodeCounter counter = setupByCounter();
		counter.getInstrumentationParams().setUseBasicBlocks(true);
		
		// specify the part "sequential" part of the runThreads method
		MethodDescriptor myMethodRun = new MethodDescriptor(methodRun);
        List<EntityToInstrument> entitiesToInstrument = new LinkedList<EntityToInstrument>();
        entitiesToInstrument.add(new InstrumentedCodeArea(myMethodRun, new LineNumberRange(53, 58)));
        entitiesToInstrument.add(new InstrumentedMethod(methodRunnableForThreadingRun));
        entitiesToInstrument.add(new InstrumentedMethod(methodRunnableIincRun));
        counter.addEntityToInstrument(entitiesToInstrument);
		counter.instrument();
		
		Object[] executionParameters = new Object[0];
		counter.execute(myMethodRun, executionParameters);
		
		SortedSet<CountingResult> countingResults = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults();
		removeMethodCallsWithFrequency0(countingResults);
        
        // check ByCounter results against expectations
        CountingResult[] results = countingResults.toArray(new CountingResult[0]);
        // we expect 1 result with 2 child thread results
		Expectation e = new Expectation(true);
		e.add(53, 58).add(Opcodes.NEW, 4)
		   	   	.add(Opcodes.DUP, 4)
		        .add(Opcodes.INVOKESPECIAL, 4)
		        .add("de.uka.ipd.sdq.ByCounter.test.helpers.RunnableIinc", "public RunnableIinc()", 1)
		        .add("java.lang.Thread", "public Thread(java.lang.Runnable r)", 2)
		        .add(Opcodes.ASTORE, 2)
		        .add("de.uka.ipd.sdq.ByCounter.test.helpers.RunnableForThreading", "public RunnableForThreading()", 1)
		        .add(Opcodes.ALOAD, 4)
		        .add(Opcodes.INVOKEVIRTUAL, 4)
		        .add("java.lang.Thread", "public void start()", 2)
		        .add("java.lang.Thread", "public void join()", 2)
		        .addParallel(createExpectationsRunnableFTRun(),
							createExpectationsRunnableIincRun());
		        ;
        // the thread is executed four times before the specified code area
        for(int i = 0 ; i < 4; i++) {
	        e.add(createExpectationsRunnableFTRun());
        }
		// output for debugging purposes
        for (CountingResult r : results) {
        	r.logResult(false, true);
        }
		e.compare(results);
		
    }

    /**
     * Remove method calls with frequency 0.
     * @param countingResults Counting results to change.
     */
	private static void removeMethodCallsWithFrequency0(final SortedSet<CountingResult> countingResults) {
		final Queue<SortedSet<CountingResult>> resultsQueue = new LinkedList<SortedSet<CountingResult>>();
		resultsQueue.add(countingResults);
		do {
			SortedSet<CountingResult> currentCountingResults = resultsQueue.poll();
			for(CountingResult cr : currentCountingResults) {
				List<String> methodsToDiscard = new LinkedList<String>();
				for(String m : cr.getMethodCallCounts().keySet()) {
					if(cr.getMethodCallCounts().get(m) == 0) {
						// mark for removal
						methodsToDiscard.add(m);
					}
				}
				// remove
				for(String m : methodsToDiscard) {
					cr.getMethodCallCounts().remove(m);
				}
				// add lists of spawned results to the queue
				SortedSet<CountingResult> spawnedThreadedCountingResults = cr.getSpawnedThreadedCountingResults();
				if(!spawnedThreadedCountingResults.isEmpty()) {
					TreeSet<CountingResult> spawned = new TreeSet<CountingResult>(spawnedThreadedCountingResults);
					resultsQueue.add(spawned);
				}
			}
		} while(!resultsQueue.isEmpty());
	}
}
