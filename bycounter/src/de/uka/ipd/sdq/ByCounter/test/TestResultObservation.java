package de.uka.ipd.sdq.ByCounter.test;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.asm.Opcodes;

import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultCompleteMethodExecutionUpdate;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultSectionExecutionUpdate;
import de.uka.ipd.sdq.ByCounter.instrumentation.EntityToInstrument;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedCodeArea;
import de.uka.ipd.sdq.ByCounter.parsing.LineNumberRange;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;
import de.uka.ipd.sdq.ByCounter.results.RequestResult;
import de.uka.ipd.sdq.ByCounter.results.ResultCollection;
import de.uka.ipd.sdq.ByCounter.test.framework.expectations.Expectation;
import de.uka.ipd.sdq.ByCounter.test.helpers.TestSubjectLineNumbers;
import de.uka.ipd.sdq.ByCounter.test.helpers.TestSubjectResultObservation;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * This test suite tests observing of {@link CountingResultCollector} for
 * ByCounter.
 *
 * @since 0.1
 * @version 2.0
 * @author Martin Krogmann
 */
@RunWith(Parameterized.class)
public class TestResultObservation extends AbstractByCounterTest {

	/** Signature of the method that is used to test in {@link #testCallTreeObservation()} */
    private static final String SIGNATURE_METHOD1 = "public void method1(boolean firstLevel) {";

	/** Signature of the method that is used to test in {@link #testRangeBlockOrderedCounting()} */
    private static final String SIGNATURE_METHOD_CALLS = "public void testNestedNormalisedLoopsWithExternalCalls(int i)";

	/**
	 * see http://en.wikipedia.org/wiki/Data_log
	 */
	private static Logger log = Logger.getLogger(TestResultObservation.class.getCanonicalName());

    /**
     * This constructor is used by the Parametrized runner for running tests with different
     * parameters.
     *
     * @param params
     *            {@link InstrumentationParameters} template for the counting setup.
     */
    public TestResultObservation(final InstrumentationParameters params) {
        super(params);
    }

    /**
     * Cleans up results after every test.
     */
    @After
    @Override
    public void cleanResults() {
        // clear all collected results
        super.cleanResults();
        // delete all observers
        CountingResultCollector.getInstance().deleteObservers();
    }

    /**
     * Tests the counting of user defined line number ranges while recording the order of execution.
     */
    @Test
    public void testRangeBlockOrderedCounting() {
        // define expectations
        Expectation e = new Expectation(true);
        e.add(51, 53).add(Opcodes.ICONST_0, 3)
                     .add(Opcodes.ISTORE, 3);
        e.add(54, 54).add(Opcodes.BIPUSH, 1)
                     .add(Opcodes.GOTO, 1)
                     .add(Opcodes.IF_ICMPLT, 1)
                     .add(Opcodes.ILOAD, 1);
        e.add(55, 55).add(Opcodes.IINC, 1);
        e.add(57, 57).add(Opcodes.IINC, 1);
        e.add(58, 58).add(Opcodes.BIPUSH, 1)
                     .add(Opcodes.GOTO, 1)
                     .add(Opcodes.IF_ICMPLT, 1)
                     .add(Opcodes.ILOAD, 1);
        for (int i = 0; i < 12; i++) {
            e.add(59, 59).add(Opcodes.ICONST_2, 1)
                         .add(Opcodes.ILOAD, 1)
                         .add(Opcodes.IMUL, 1)
                         .add(Opcodes.ISTORE, 1);
            e.add(61, 61).add(Opcodes.IINC, 1);
            e.add(58, 58).add(Opcodes.BIPUSH, 1)
                         .add(Opcodes.IF_ICMPLT, 1)
                         .add(Opcodes.ILOAD, 1);
        }
        e.add(63, 63).add(Opcodes.IINC, 1);
        e.add(54, 54).add(Opcodes.BIPUSH, 1)
                     .add(Opcodes.IF_ICMPLT, 1)
                     .add(Opcodes.ILOAD, 1);

        CountingResultCollector.getInstance().addObserver(new Observer() {
			public void update(Observable crc, Object updateData) {
				log.info("Notification received: " + updateData);
			}
        });

        // run ByCounter
        RequestResult[] rResults = this.instrumentAndExecute(e.getRanges()).getRequestResults().toArray(new RequestResult[0]);
        Assert.assertEquals(1, rResults.length);
        CountingResult[] results = rResults[0].getCountingResults().toArray(new CountingResult[0]);
        for (CountingResult r : results) {
        	r.logResult(false, true);
        }
        // compare
        e.compare(results);
    }

    /**
     * This test instruments and executes a mildly complex call tree to verify
     * that the online results are not mixed up.
     */
    @Test
    public void testCallTreeObservation() {
		// initialize ByCounter
        BytecodeCounter counter = setupOnlineUpdateByCounter();

        MethodDescriptor method1 = new MethodDescriptor(
        		TestSubjectResultObservation.class.getCanonicalName(),
        		SIGNATURE_METHOD1);

        Expectation eInit = new Expectation(true);	// false because the execution sequence is specified manually down below
        eInit.add(0).add(Opcodes.ICONST_2, 1)
        			  .add(Opcodes.ISTORE, 1);
        LineNumberRange eInitLnr = new LineNumberRange(29, 29);
        Expectation eInt = new Expectation(true);
        eInt.add(1).add(Opcodes.IINC, 1)
        			.add(Opcodes.GOTO, 1);
        LineNumberRange eIntLnr = new LineNumberRange(31, 31);
        Expectation eDouble = new Expectation(true);
        eDouble.add(2).add(Opcodes.ILOAD, 1)
        				   .add(Opcodes.I2D, 1)
        				   .add(Opcodes.LDC, 1)
        				   .add(Opcodes.DADD, 1)
        				   .add(Opcodes.D2I, 1)
        				   .add(Opcodes.ISTORE, 1);
        LineNumberRange eDoubleLnr = new LineNumberRange(35, 35);

        final Expectation[] expectations = new Expectation[] {
        		eInit,					// method1
        		eInit, eInt, eDouble, 	// method1->method2->method1
        		eDouble 				// method1
        };

        // instrument all ranges
        LinkedList<LineNumberRange> ranges = new LinkedList<LineNumberRange>();
        ranges.add(eInitLnr);
        ranges.add(eIntLnr);
        ranges.add(eDoubleLnr);

        List<EntityToInstrument> entitiesToInstrument = new LinkedList<EntityToInstrument>();
        for(LineNumberRange r : ranges) {
        	entitiesToInstrument.add(new InstrumentedCodeArea(method1, r));
        }
        counter.addEntityToInstrument(entitiesToInstrument);
        counter.instrument();

        // setup the observer
        CountingResultCollector.getInstance().addObserver(new Observer() {
        	private int observationCounter;
        	{
        		observationCounter = 0;
        	}
			public void update(Observable crc, Object updateData) {
				if(updateData instanceof CountingResultSectionExecutionUpdate) {
					log.info("Notification received: " + updateData);
					// compare the observation with the expectation
					CountingResult observation = ((CountingResultSectionExecutionUpdate)updateData).sectionResult;
					expectations[observationCounter].compare(new CountingResult[] {observation});
					observationCounter++;
				} else if(updateData instanceof CountingResultCompleteMethodExecutionUpdate) {
					// skip complete result
				}else {
					Assert.fail("Test case is missing the correct updateData type.");
				}
			}
        });


        // execute with (true)
        Object[] executionParameters = new Object[] { true };
        counter.execute(method1, executionParameters);

        int i = 0;
        for(CountingResult cr : CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults()) {
        	cr.logResult(false, true);
        	System.out.println(cr.getMethodInvocationBeginning());
//        	Expectation ea = expectations[i];
        	expectations[i].compare(new CountingResult[] {cr});
        	i++;
        }
    }

	private ResultCollection instrumentAndExecute(LineNumberRange[] codeAreasToInstrument) {
		// initialize ByCounter
        BytecodeCounter counter = setupOnlineUpdateByCounter();

        MethodDescriptor methodRanged = new MethodDescriptor(
        		TestSubjectLineNumbers.class.getCanonicalName(),
        		SIGNATURE_METHOD_CALLS);
        List<EntityToInstrument> entitiesToInstrument = new LinkedList<EntityToInstrument>();
        for(LineNumberRange r : codeAreasToInstrument) {
        	entitiesToInstrument.add(new InstrumentedCodeArea(methodRanged, r));
        }

        counter.addEntityToInstrument(entitiesToInstrument);
        counter.instrument();
        // execute with (10)
        Object[] executionParameters = new Object[] { 10 };
        counter.execute(methodRanged, executionParameters);

        return CountingResultCollector.getInstance().retrieveAllCountingResults();
	}

	/**
	 * @return A {@link BytecodeCounter} instance setup with online section
	 * execution updates.
	 */
	private BytecodeCounter setupOnlineUpdateByCounter() {
		BytecodeCounter counter = this.setupByCounter();
        counter.getInstrumentationParams().setUseBasicBlocks(true);
        counter.getInstrumentationParams().setRecordBlockExecutionOrder(true);
        counter.getInstrumentationParams().setProvideOnlineSectionExecutionUpdates(true);
        counter.getInstrumentationParams().setTraceAndIdentifyRequests(true);
		return counter;
	}
}
