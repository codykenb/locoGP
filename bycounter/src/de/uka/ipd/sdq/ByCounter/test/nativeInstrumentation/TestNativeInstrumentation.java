package de.uka.ipd.sdq.ByCounter.test.nativeInstrumentation;

import java.util.Collection;
import java.util.SortedSet;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultBase;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationCounterPrecision;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;
import de.uka.ipd.sdq.ByCounter.test.TestASMBytecodes;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * JUnit Test for the instrumentation of compress.
 *
 */
@RunWith(Parameterized.class)
public class TestNativeInstrumentation {	

	private static final Object[] EXECUTION_PARAMETERS_NONE = new Object[]{new String[]{}};

	private static Logger log = Logger.getLogger(TestNativeInstrumentation.class.getCanonicalName());
	
	private static boolean EXECUTE = false;
	
	/**
	 * Generates the different parameters with which all tests are run.
	 * This reuses the parameters from TestASMBytecodes.parameterSetup().
	 * @return The parameter collection for calling the test constructor.
	 * @see #TestASMBytecodes.parameterSetup()
	 */
	@SuppressWarnings({"rawtypes"})
	@Parameters
	public static Collection parameterSetup() {
		return TestASMBytecodes.parameterSetup();
	}

	private InstrumentationParameters instrumentationParameters;

	/**
	 * This constructor is used by the Parametrized runner 
	 * for running tests with different parameters.
	 * @param params {@link InstrumentationParameters} for the counting setup.
	 */
	public TestNativeInstrumentation(InstrumentationParameters params) {
		// create a BytecodeCounter
		this.instrumentationParameters = params;
	}

	private static final MethodDescriptor METHOD_TO_EXECUTE = 
		new MethodDescriptor(Caller.class.getCanonicalName(), "public static void main(java.lang.String argv[]) {");

	private BytecodeCounter counter;
	private CountingResultCollector resultColl;
	private String testClassName;
	private String testMethodSignature;
	private MethodDescriptor methodToExecute;

	private Object[] executionParameters;

	@Test
	public void testNativeInstrumentation() {
		this.testClassName 			= Caller.class.getCanonicalName();
		this.testMethodSignature 	= "public void methToInstr() {";
		this.methodToExecute		= METHOD_TO_EXECUTE;
		this.executionParameters	= EXECUTION_PARAMETERS_NONE;
		init();
		
		// setup recursive instrumentation
		this.instrumentationParameters.setInstrumentRecursively(true);
		
		count();
	}
	
	
	/**
	 * Default constructor, see source
	 */
	public void init(){
		this.resultColl = CountingResultCollector.getInstance();
		this.counter = new BytecodeCounter();
		log.fine("Using class "+this.testClassName+" " +
				"(instrumented: "+this.testMethodSignature+", " +
				"called: "+ this.methodToExecute +").");
	}
	
	/**
	 * Performs the actual counting by calling BytecodeCounter.count(...)
	 */
	private void count(){
		MethodDescriptor methDesc = new MethodDescriptor(this.testClassName, this.testMethodSignature);
		counter.setInstrumentationParams(this.instrumentationParameters);
//		log.fine("Method descriptor: "+methDesc);
		
		long start = System.nanoTime();
		log.fine("(NOT INITIALISED)" + this.counter.getInstrumentationParams().toString());
		this.counter.getInstrumentationParams().setCounterPrecision(InstrumentationCounterPrecision.Integer);
		this.counter.addEntityToInstrument(methDesc);
		counter.instrument();
		if(EXECUTE) {
			this.counter.execute(this.methodToExecute, 
					this.executionParameters);
		}
		long stop = System.nanoTime();
		long counting = stop-start;
		
		log.fine(this.counter.getInstrumentationParams().toString());
		log.info(counting+    "ns to count (aka \t"+
				Math.round((double) counting/1000)+"us aka \t"+
				Math.round((double) counting/1000000)+"ms aka \t"+
				Math.round((double) counting/1000000000)+"s)");
		if(EXECUTE) {
			SortedSet<CountingResult> finalResults = this.resultColl.retrieveAllCountingResults().getCountingResults();
			Assert.assertNotSame("Number of results must be != 0.", 0, finalResults.size());
			log.info(finalResults.size()+" counting results found, logging them: ");
			for(CountingResultBase r : finalResults) {
				r.logResult(true, true); //from Martin
			}
			// clear all collected results
			this.resultColl.clearResults();
		}
	}
}
