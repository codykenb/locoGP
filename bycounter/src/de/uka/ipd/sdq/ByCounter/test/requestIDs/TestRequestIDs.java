package de.uka.ipd.sdq.ByCounter.test.requestIDs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector;
import de.uka.ipd.sdq.ByCounter.instrumentation.EntityToInstrument;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedMethod;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;
import de.uka.ipd.sdq.ByCounter.results.RequestResult;
import de.uka.ipd.sdq.ByCounter.results.ResultCollection;
import de.uka.ipd.sdq.ByCounter.test.AbstractByCounterTest;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * JUnit Test for the tracking of request Ids.
 * Uses {@link A#main(String[])} as the instrumentation subject.
 */
@RunWith(Parameterized.class)
public class TestRequestIDs extends AbstractByCounterTest {	

	private static final Object[] EXECUTION_PARAMETERS_NONE = new Object[]{new String[]{}};

	private static Logger log = Logger.getLogger(TestRequestIDs.class.getCanonicalName());
	
	/**
	 * This constructor is used by the Parametrized runner 
	 * for running tests with different parameters.
	 * @param params {@link InstrumentationParameters} for the counting setup.
	 */
	public TestRequestIDs(InstrumentationParameters params) {
		super(params);
	}

	private static final MethodDescriptor METHOD_A_MAIN = 
		new MethodDescriptor(A.class.getCanonicalName(), "public static void main(java.lang.String argv[]) {");

	private BytecodeCounter counter;
	private CountingResultCollector resultColl;
	private String testClassName;
	private MethodDescriptor methodToExecute;

	private Object[] executionParameters;

	@Test
	public void testRequestIDInstrumentation() {
		this.testClassName 			= A.class.getCanonicalName();
		this.methodToExecute		= METHOD_A_MAIN;
		this.executionParameters	= EXECUTION_PARAMETERS_NONE;
		init();
		this.instrumentationParameters.setWriteClassesToDisk(true);
		count();
	}
	
	
	/**
	 * Default constructor, see source
	 */
	public void init(){
		this.resultColl = CountingResultCollector.getInstance();
		this.counter = setupByCounter();
	}
	
	/**
	 * Performs the actual counting by calling BytecodeCounter.count(...)
	 */
	private void count(){
		MethodDescriptor methDescMethodA = new MethodDescriptor(this.testClassName, "public void methodA(int reqID)");
		MethodDescriptor methDescMethodB = new MethodDescriptor(this.testClassName, "public void methodB(int reqID)");
		MethodDescriptor methDescDoSth = new MethodDescriptor(this.testClassName, "public java.lang.String doSth()");
		MethodDescriptor methDescDoSthElse = new MethodDescriptor(this.testClassName, "public char[] doSthElse()");
		MethodDescriptor methDescDoSthDifferent = new MethodDescriptor(this.testClassName, "public static boolean doSthDifferent(short s)");
		MethodDescriptor methDescDoSthStatic = new MethodDescriptor(this.testClassName, "public static byte doSthStatic()");
		MethodDescriptor methDesc7 = new MethodDescriptor(this.testClassName, "public boolean parameterTest(int i, float f, java.lang.String s)");
		MethodDescriptor methDesc8 = new MethodDescriptor(de.uka.ipd.sdq.ByCounter.test.requestIDs.A.class.getCanonicalName(), "public A(int param)");
		
		counter.setInstrumentationParams(this.instrumentationParameters);
		counter.getInstrumentationParams().setTraceAndIdentifyRequests(true);
//		log.fine("Method descriptor: "+methDesc);
		
		long start = System.nanoTime();
		log.fine("(NOT INITIALISED)" + this.counter.getInstrumentationParams().toString());
		List<EntityToInstrument> entitiesToInstrument = new ArrayList<EntityToInstrument>();
		// the order of adding the methods is used further down for checking the results
		entitiesToInstrument.add(new InstrumentedMethod(methDescMethodA));
		entitiesToInstrument.add(new InstrumentedMethod(methDescMethodB));
		entitiesToInstrument.add(new InstrumentedMethod(methDescDoSth));
		entitiesToInstrument.add(new InstrumentedMethod(methDescDoSthElse));
		entitiesToInstrument.add(new InstrumentedMethod(methDescDoSthDifferent));
		entitiesToInstrument.add(new InstrumentedMethod(methDescDoSthStatic));
		entitiesToInstrument.add(new InstrumentedMethod(methDesc7));
		entitiesToInstrument.add(new InstrumentedMethod(methDesc8));

        counter.addEntityToInstrument(entitiesToInstrument);
        counter.instrument();
		this.counter.execute(this.methodToExecute, 
				this.executionParameters);
		long stop = System.nanoTime();
		long counting = stop-start;
		
		log.fine(this.counter.getInstrumentationParams().toString());
		log.info(counting+    "ns to count (aka \t"+
				Math.round((double) counting/1000)+"us aka \t"+
				Math.round((double) counting/1000000)+"ms aka \t"+
				Math.round((double) counting/1000000000)+"s)");
		// The executed main method spawns 2 threads and runs them.
		
		// There should be 2 results that are not request results because 
		// ReqRunnable.run constructs A. Constructors cannot be tracked with 
		// request ids.
		ResultCollection retrieveAllCountingResults = this.resultColl.retrieveAllCountingResults();
		SortedSet<CountingResult> finalResults = retrieveAllCountingResults.getCountingResults();
		Assert.assertSame("Number of results must be 2.", 2, finalResults.size());
		log.info(finalResults.size()+" counting results found, logging them: ");
		for(CountingResult r : finalResults) {
			r.logResult(false, true);
		}
		// There should also be 2 request results (one for each thread).
		SortedSet<RequestResult> requestResults = retrieveAllCountingResults.getRequestResults();
		Assert.assertSame("Number of request results must be 2.", 2, requestResults.size());
		log.info(requestResults.size()+" request results found, logging them: ");
		for(RequestResult rq : requestResults) {
			log.info(rq.toString());
			Assert.assertNotNull(rq.getCountingResults());
			// A.methodA, A.methodB, A.doSth, A.doSthElse, A.doSthDifferent
			Assert.assertSame("There should be 5 counting results per request.", 5, rq.getCountingResults().size());
			UUID rid = rq.getRequestId();
			Iterator<CountingResult> iter = rq.getCountingResults().iterator();
			for(CountingResult r: rq.getCountingResults()) {
				Assert.assertEquals(rid, r.getRequestID());
				CountingResult expectedR = iter.next();
				Assert.assertEquals(expectedR.getQualifiedMethodName(), r.getQualifiedMethodName());
			}
		}
		// clear all collected results
		this.resultColl.clearResults();
	}
}
