package de.uka.ipd.sdq.ByCounter.test;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.asm.Opcodes;

import de.uka.ipd.sdq.ByCounter.example.fibonacci.FibonacciAlgorithm;
import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector;
import de.uka.ipd.sdq.ByCounter.instrumentation.EntityToInstrument;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedMethod;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;
import de.uka.ipd.sdq.ByCounter.test.framework.expectations.Expectation;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * This test suite tests for the correct counting results when 
 * instrumenting the fibonacci algorithm as implemented in 
 * {@link FibonacciAlgorithm}.
 *
 * @since 0.1
 * @version 2.0
 * @author Martin Krogmann
 */
@RunWith(Parameterized.class)
public class TestFibonacciAlgorithm extends AbstractByCounterTest {
	
    /**
     * This constructor is used by the Parametrized runner for running tests with different
     * parameters.
     *
     * @param params
     *            {@link InstrumentationParameters} template for the counting setup.
     */
    public TestFibonacciAlgorithm(final InstrumentationParameters params) {
        super(params);
    }

    /**
     * Instrument and execute the fibonacci algorithm and compare the results 
     * to the expectations.
     */
    @Test
    public void testFibonacci() {
        MethodDescriptor methodFib = new MethodDescriptor(
        		FibonacciAlgorithm.class.getCanonicalName(), 
        		"public long fibonacci(long rounds)");
        EntityToInstrument entitiyToInstrument = new InstrumentedMethod(methodFib);
        
        // initialize ByCounter
        BytecodeCounter counter = setupByCounter();
        counter.getInstrumentationParams().getEntitiesToInstrument().add(entitiyToInstrument);
        counter.instrument();
        
        // execute with (13)
        int rounds = 13;
        Object[] executionParameters = new Object[] {rounds};
        counter.execute(methodFib, executionParameters);


        // define expectations
        Expectation e = new Expectation(true);
        e.add()	   .add(Opcodes.LCONST_0, 3)
                       .add(Opcodes.LCONST_1, 	1 + 1*rounds)
                       .add(Opcodes.LSTORE, 	4 + 4*rounds)
                       .add(Opcodes.GOTO, 		1)
                       .add(Opcodes.LLOAD, 		3 + 7*rounds)
                       .add(Opcodes.LCMP, 		1 + 1*rounds)
                       .add(Opcodes.IFLT, 		1 + 1*rounds)
                       .add(Opcodes.LADD, 		0 + 2*rounds)
                       .add(Opcodes.LRETURN, 	1)
                       ;

        CountingResult[] results = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults().toArray(new CountingResult[0]);
        Assert.assertTrue("No or too many results counted", results.length == 1);
        for (CountingResult r : results) {
        	r.logResult(false, true);
        }
        // compare
        e.compare(results);
    }
}
