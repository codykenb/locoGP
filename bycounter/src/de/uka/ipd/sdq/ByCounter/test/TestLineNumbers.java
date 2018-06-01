package de.uka.ipd.sdq.ByCounter.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
import de.uka.ipd.sdq.ByCounter.parsing.LineNumberRange;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;
import de.uka.ipd.sdq.ByCounter.test.framework.expectations.Expectation;
import de.uka.ipd.sdq.ByCounter.test.helpers.TestSubjectLineNumbers;
import de.uka.ipd.sdq.ByCounter.test.helpers.Utils;
import de.uka.ipd.sdq.ByCounter.test.helpers.subjects.Branch;
import de.uka.ipd.sdq.ByCounter.test.helpers.subjects.ExecutionOrder;
import de.uka.ipd.sdq.ByCounter.test.helpers.subjects.LoopExternalActionNoDependency;
import de.uka.ipd.sdq.ByCounter.test.helpers.subjects.LoopExternalActionStackOverflow;
import de.uka.ipd.sdq.ByCounter.test.helpers.subjects.UncommonFormatting;
import de.uka.ipd.sdq.ByCounter.utils.ASMOpcodesMapper;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * This test suite tests several different usage patterns for ByCounter.
 * 
 * @since 0.1
 * @version 2.0
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @author Florian Schreier
 */
@RunWith(Parameterized.class)
public class TestLineNumbers extends AbstractByCounterTest {

    /** The canonical name of the test subject's class. */
    private static final String DEFAULT_SUBJECT_CANONICAL = TestSubjectLineNumbers.class.getCanonicalName();

    /** Signature of the method that is used to test in testRangeBlocksForeach(). */
    private static final String SIGNATURE_FOREACH = "public int testForeach()";

    /** Signature of the method that is used to test in testBasicBlockCounting(). */
    private static final String SIGNATURE_BASIC_BLOCK = "public void testNestedNormalisedLoops(int i)";

    /** Signature of the method that is used to test in both testRangeBlock{|Ordered}Counting(). */
    private static final String SIGNATURE_RANGE_BLOCK = "public void testNestedNormalisedLoopsWithExternalCalls(int i)";

    /** Signature of the method that is used to test in testMethodCallOrderedCounting(). */
    private static final String SIGNATURE_METHOD_CALLS = SIGNATURE_RANGE_BLOCK;

    /** Signature of the method that is used to test in testLabelAndLineNumbers(). */
    private static final String SIGNATURE_LINE_NUMBERS = "public void testLabelAndLineNumbers()";

    /**
     * This constructor is used by the Parametrized runner for running tests with different
     * parameters.
     * 
     * @param params
     *            {@link InstrumentationParameters} template for the counting setup.
     */
    public TestLineNumbers(final InstrumentationParameters params) {
        super(params);
    }
    
	/**
	 * Tests if instrumentation of a single line works correctly.
	 */
	@Test
	public void testSingleLineInstrumentation() {
		// define expectations
		Expectation e = new Expectation();
		e.add(15, 15).add(Opcodes.ICONST_0, 1)
					 .add(Opcodes.ISTORE, 1);
        // run ByCounter
		String canonicalClassName = ExecutionOrder.class.getCanonicalName();
		String methodSignature = "void process()";
        CountingResult[] results = this.instrumentAndExecute(e.getRanges(), canonicalClassName, methodSignature, new Object[0]);
        for (CountingResult r : results) {
        	r.logResult(false, true);
        }
        // compare
        e.compare(results);
	}

	/**
	 * Test that ByCounter does not fail due to an Invocation Target Exception (Stack Overflow) although the problem is
	 * not due to the implementation or structure of it. If an interface instead of a direct class is used ByCounter
	 * should not fail due to an Invocation Target Exception.
	 */
	@Test
	public void callToMethodsViaInterface() {
		// ensure direct execution works and no exceptions are thrown
		LoopExternalActionStackOverflow cut = new LoopExternalActionStackOverflow();
		cut.process();
		
		// define line number ranges
		LineNumberRange[] lnrs = new LineNumberRange[2];
		lnrs[0] = new LineNumberRange(28, 30); // loop
		lnrs[1] = new LineNumberRange(31, 31); // external call within loop
		// run ByCounter
		String className = LoopExternalActionStackOverflow.class.getCanonicalName();
		String methodSignature = "void process()";
		CountingResult[] results = this.instrumentAndExecute(lnrs, className, methodSignature, new Object[0]);
		for (CountingResult result : results) {
			result.logResult(false, true);
		}
		// compare TODO usage of test framework
		assertNotNull("Results must not be null.", results);
		assertTrue("Number of results for LNRs must be bigger than 0.", results.length > 0);
	}

	/**
	 * Tests if the order of execution during runtime is preserved in the results. This test case is different from
	 * {@link #preserveExecutionOrderOfMeasurements_21()} in the order of the defined line number ranges.
	 */
	@Test
	public void preserveExecutionOrderOfMeasurements_12() {
		// define expectations
		Expectation e = new Expectation(true);
		e.add(15, 16).add(Opcodes.ICONST_0, 1) // first two lines
				.add(Opcodes.IINC, 1)
				.add(Opcodes.ISTORE, 1);
		e.add(17, 17).add(Opcodes.ICONST_1, 1) // third line
				.add(Opcodes.ILOAD, 1)
				.add(Opcodes.IMUL, 1)
				.add(Opcodes.ISTORE, 1);
		// run ByCounter
		String canonicalClassName = ExecutionOrder.class.getCanonicalName();
		String methodSignature = "void process()";
		CountingResult[] results = this.instrumentAndExecute(e.getRanges(), canonicalClassName, methodSignature, new Object[0]);
        for (CountingResult r : results) {
        	r.logResult(false, true);
        }
        // compare
        e.compare(results);
	}

	/**
	 * Tests if the order of execution during runtime is preserved in the results. This test case is different from
	 * {@link #preserveExecutionOrderOfMeasurements_12()} in the order of the defined line number ranges.
	 */
	@Test
	public void preserveExecutionOrderOfMeasurements_21() {
		Expectation e = new Expectation(true);
		e.add(15, 16).add(Opcodes.ICONST_0, 1) // first two lines
				.add(Opcodes.IINC, 1)
				.add(Opcodes.ISTORE, 1);
		e.add(17, 17).add(Opcodes.ICONST_1, 1) // third line
				.add(Opcodes.ILOAD, 1)
				.add(Opcodes.IMUL, 1)
				.add(Opcodes.ISTORE, 1);
		// run ByCounter
		String canonicalClassName = ExecutionOrder.class.getCanonicalName();
		String methodSignature = "void process()";
		CountingResult[] results = this.instrumentAndExecute(e.getRanges(), canonicalClassName, methodSignature, new Object[0]);
        for (CountingResult r : results) {
        	r.logResult(false, true);
        }
        // compare
        e.compare(results);
	}

	@Test
	public void preserveExecutionOrderOfMeasurements_Loop() {
		Expectation e = new Expectation();
		e.add(28, 30).add(Opcodes.GOTO, 1)
					 .add(Opcodes.ICONST_0, 1)
					 .add(Opcodes.ICONST_5, 1)
					 .add(Opcodes.IF_ICMPLT, 1)
					 .add(Opcodes.ILOAD, 1)
					 .add(Opcodes.ISTORE, 1);
		for (int i = 0; i < 5; i++) {
			e.add(31, 31).add(Opcodes.DUP, 1)
						 .add(Opcodes.GETSTATIC, 1)
						 .add(Opcodes.ILOAD, 1)
						 .add(Opcodes.INVOKESPECIAL, 1)
						 .add(Opcodes.INVOKEVIRTUAL, 3)
						 .add(Opcodes.LDC, 1)
						 .add(Opcodes.NEW, 1)
						 .add("java.io.PrintStream", "public void println(java.lang.String obj)", 1)
						 .add("java.lang.StringBuilder", "public StringBuilder(java.lang.String obj)", 1)
						 .add("java.lang.StringBuilder", "public java.lang.StringBuilder append(int obj)", 1)
						 .add("java.lang.StringBuilder", "public java.lang.String toString()", 1);
			e.add(28, 30).add(Opcodes.IINC, 1)
						 .add(Opcodes.ICONST_5, 1)
						 .add(Opcodes.IF_ICMPLT, 1)
						 .add(Opcodes.ILOAD, 1);
		}

		LineNumberRange[] lnrs = new LineNumberRange[2];
		lnrs[0] = new LineNumberRange(28, 30); // loop
		lnrs[1] = new LineNumberRange(31, 31); // body | external call within loop
		
		String canonicalClassName = LoopExternalActionNoDependency.class.getCanonicalName();
		String methodSignature = "void process()";
		CountingResult[] results = this.instrumentAndExecute(lnrs, canonicalClassName, methodSignature, new Object[0]);
        for (CountingResult r : results) {
        	r.logResult(false, true);
        }
        // compare
        e.compare(results);
	}

    /**
     * Tests for counting a method using the detected invariant sections aka basic blocks.
     */
    @Test
    public void testBasicBlockCounting() {
        // start standard counting
        BytecodeCounter counter = new BytecodeCounter();
        counter.setInstrumentationParams(this.instrumentationParameters);
        counter.getInstrumentationParams().setUseBasicBlocks(false);
        MethodDescriptor methodNormalise = new MethodDescriptor(DEFAULT_SUBJECT_CANONICAL, SIGNATURE_BASIC_BLOCK);
        counter.addEntityToInstrument(methodNormalise);
		counter.instrument();
        Object[] executionParameters = new Object[] { 5 };
        counter.execute(methodNormalise, executionParameters);

        CountingResult originalResult = Utils.getAssertedResult();
        originalResult.logResult(false, true);
        CountingResultCollector.getInstance().clearResults();

        // enable usage of basic blocks and count again
        counter.getInstrumentationParams().setUseBasicBlocks(true);
        counter.getInstrumentationParams().setRecordBlockExecutionOrder(false);
        counter.getInstrumentationParams().setWriteClassesToDisk(true);
        counter.addEntityToInstrument(methodNormalise);
		counter.instrument();
        counter.execute(methodNormalise, executionParameters);

        CountingResult newResult = Utils.getAssertedResult();
        newResult.logResult(false, true);

        // now assert that the results are equal
        Assert.assertEquals(originalResult.getMethodCallCounts().size(), newResult.getMethodCallCounts().size());
        for (String methodName : originalResult.getMethodCallCounts().keySet()) {
            Long expected = originalResult.getMethodCallCounts().get(methodName);
            Long actual = newResult.getMethodCallCounts().get(methodName);
            Assert.assertEquals(expected, actual);
        }
        Assert.assertEquals(originalResult.getOpcodeCounts().length, newResult.getOpcodeCounts().length);
        for (int opcode = 0; opcode < originalResult.getOpcodeCounts().length; opcode++) {
            StringBuilder message = new StringBuilder();
            message.append("Counts for the ");
            message.append(ASMOpcodesMapper.getInstance().getOpcodeString(opcode));
            message.append(" instruction do not match.");
            long expected = originalResult.getOpcodeCounts()[opcode];
            long actual = newResult.getOpcodeCounts()[opcode];
            Assert.assertEquals(message.toString(), expected, actual);
        }

    }

    /**
     * Tests the counting of user defined line number ranges.
     */
    @Test
    public void testRangeBlockCounting() {
        // define expectations
        Expectation e = new Expectation(false);
        e.add(51, 53).add(Opcodes.ICONST_0, 3)
                     .add(Opcodes.ISTORE, 3);
        e.add(54, 54).add(Opcodes.BIPUSH, 2)
                     .add(Opcodes.GOTO, 1)
                     .add(Opcodes.IF_ICMPLT, 2)
                     .add(Opcodes.ILOAD, 2);
        e.add(55, 55).add(Opcodes.IINC, 1);
        e.add(57, 57).add(Opcodes.IINC, 1);
        e.add(58, 58).add(Opcodes.BIPUSH, 13)
                     .add(Opcodes.GOTO, 1)
                     .add(Opcodes.IF_ICMPLT, 13)
                     .add(Opcodes.ILOAD, 13);
        e.add(59, 59).add(Opcodes.ICONST_2, 12)
                     .add(Opcodes.ILOAD, 12)
                     .add(Opcodes.IMUL, 12)
                     .add(Opcodes.ISTORE, 12);
        e.add(61, 61).add(Opcodes.IINC, 12);
        e.add(63, 63).add(Opcodes.IINC, 1);
        // initialize ByCounter
        BytecodeCounter counter = new BytecodeCounter();
        counter.setInstrumentationParams(this.instrumentationParameters);
        counter.getInstrumentationParams().setUseBasicBlocks(true);
        counter.getInstrumentationParams().setRecordBlockExecutionOrder(false);
        MethodDescriptor methodRanged = new MethodDescriptor(DEFAULT_SUBJECT_CANONICAL, SIGNATURE_RANGE_BLOCK);
        List<EntityToInstrument> entitiesToInstrument = new LinkedList<EntityToInstrument>();
        for(LineNumberRange r : e.getRanges()) {
        	entitiesToInstrument.add(new InstrumentedCodeArea(methodRanged, r));
        }
        counter.addEntityToInstrument(entitiesToInstrument);
        counter.instrument();
        // execute with (10)
        Object[] executionParameters = new Object[] { 10 };
        counter.execute(methodRanged, executionParameters);

        CountingResult[] results = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults().toArray(new CountingResult[0]);
        Assert.assertTrue("No or not enough results counted", results.length > 1);
        for (CountingResult r : results) {
        	r.logResult(false, true);
        }
        // compare
        e.compare(results);
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
        // run ByCounter
        CountingResult[] results = this.instrumentAndExecute(e.getRanges());
        for (CountingResult r : results) {
        	r.logResult(false, true);
        }
        // compare
        e.compare(results);
    }

    /**
     * Tests the method {@link TestSubjectLineNumbers#testForeach()} that contains a "foreach" loop.
     */
    @Test
    public void testRangeBlocksForeach() {
        // define expectations
        Expectation e = new Expectation(true);
        e.add(102, 108).add(Opcodes.AALOAD, 3)
                       .add(Opcodes.AASTORE, 3)
                       .add(Opcodes.ARRAYLENGTH, 1)
                       .add(Opcodes.ALOAD, 7)
                       .add(Opcodes.ASTORE, 8)
                       .add(Opcodes.ANEWARRAY, 1)
                       .add(Opcodes.DUP, 7)
                       .add(Opcodes.GOTO, 1)
                       .add(Opcodes.ICONST_0, 3)
                       .add(Opcodes.ICONST_1, 1)
                       .add(Opcodes.ICONST_2, 1)
                       .add(Opcodes.ICONST_3, 1)
                       .add(Opcodes.IF_ICMPLT, 4)
                       .add(Opcodes.IINC, 6)
                       .add(Opcodes.ILOAD, 12)
                       .add(Opcodes.INVOKESPECIAL, 3)
                       .add(Opcodes.INVOKESTATIC, 3)
                       .add(Opcodes.INVOKEVIRTUAL, 3)
                       .add(Opcodes.IRETURN, 1)
                       .add(Opcodes.ISTORE, 3)
                       .add(Opcodes.LDC, 3)
                       .add(Opcodes.NEW, 3)
                       .add(String.class.getCanonicalName(), "public static java.lang.String valueOf(java.lang.Object obj)", 3)
                       .add(StringBuilder.class.getCanonicalName(), "public StringBuilder(java.lang.String str)", 3)
                       .add(StringBuilder.class.getCanonicalName(), "public java.lang.String toString()", 3);
        // initialize ByCounter
        BytecodeCounter counter = new BytecodeCounter();
        counter.setInstrumentationParams(this.instrumentationParameters);
        counter.getInstrumentationParams().setUseBasicBlocks(true);
        MethodDescriptor methodForeach = new MethodDescriptor(DEFAULT_SUBJECT_CANONICAL, SIGNATURE_FOREACH);
        List<EntityToInstrument> entitiesToInstrument = new LinkedList<EntityToInstrument>();
        for(LineNumberRange r : e.getRanges()) {
        	entitiesToInstrument.add(new InstrumentedCodeArea(methodForeach, r));
        }
        counter.addEntityToInstrument(entitiesToInstrument);
        counter.instrument();
        // execute
        Object[] executionParameters = new Object[] {};
        counter.execute(methodForeach, executionParameters);

        CountingResult[] results = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults().toArray(new CountingResult[0]);
        Assert.assertTrue("No or too many results counted", results.length == 1);
        for (CountingResult r : results) {
        	r.logResult(false, true);
        }
        // compare
        e.compare(results);
    }

    /**
     * This test tests ByCounter's method call counting ability.
     */
    @Test
    public void testMethodCallOrderedCounting() {
        /*
         * expect three different sections
         * [51, 52] is part of a basic block that contains no external calls
         * [55, 56] itself contains an external call
         * [57, 57] itself contains no external call, but is part of a basic block that contains an external call
         */
        Expectation e = new Expectation(true);
        e.add(51, 52).add(Opcodes.ICONST_0, 2)
                     .add(Opcodes.ISTORE, 2);
        e.add(55, 56).add(Opcodes.ALOAD, 1)
                     .add(Opcodes.IINC, 1)
                     .add(Opcodes.INVOKESPECIAL, 1)
                     .add(DEFAULT_SUBJECT_CANONICAL + ".extCall1()V", 1);
        e.add(57, 57).add(Opcodes.IINC, 1);
        // run ByCounter
        CountingResult[] results = this.instrumentAndExecute(e.getRanges());
        for (CountingResult r : results) {
        	r.logResult(false, true);
        }
        // compare
        e.compare(results);
    }

    /**
     * A test.
     */
    @Test
    public void testLabelAndLineNumbers() {
        BytecodeCounter counter = new BytecodeCounter();
        MethodDescriptor d = new MethodDescriptor(DEFAULT_SUBJECT_CANONICAL, SIGNATURE_LINE_NUMBERS);
        counter.addEntityToInstrument(d);
		counter.instrument();
        counter.execute(d, new Object[0]);
    }

    @Test
    public void testUncommonFormatting() {
        // define expectations
        Expectation e = new Expectation(true);
        e.add(7, 8).add(Opcodes.ICONST_3, 1)
        		   .add(Opcodes.ISTORE, 1);
        e.add(9, 11).add(Opcodes.GOTO, 1)
					.add(Opcodes.ICONST_4, 2)
					.add(Opcodes.IF_ICMPLT, 2)
					.add(Opcodes.IINC, 1)
        			.add(Opcodes.ILOAD, 2);
        // initialize ByCounter
        BytecodeCounter counter = new BytecodeCounter();
        counter.setInstrumentationParams(this.instrumentationParameters);
        counter.getInstrumentationParams().setUseBasicBlocks(true);
        counter.getInstrumentationParams().setRecordBlockExecutionOrder(true);
        counter.getInstrumentationParams().setWriteClassesToDisk(true);
        MethodDescriptor methodRanged = new MethodDescriptor(UncommonFormatting.class.getCanonicalName(), "public void process()");
        List<EntityToInstrument> entitiesToInstrument = new LinkedList<EntityToInstrument>();
        for(LineNumberRange r : e.getRanges()) {
        	entitiesToInstrument.add(new InstrumentedCodeArea(methodRanged, r));
        }
        counter.addEntityToInstrument(entitiesToInstrument);
        counter.instrument();
        // execute with ()
        Object[] executionParameters = new Object[] {  };
        counter.execute(methodRanged, executionParameters);

        CountingResult[] results = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults().toArray(new CountingResult[0]);
        Assert.assertTrue("No or not enough results counted", results.length > 1);
        for (CountingResult r : results) {
        	r.logResult(false, true);
        }
		// compare
		e.compare(results);
    }

	@Test
	public void measureMultipleLNRForOneMethod_NoResults() {
		Expectation e = new Expectation();
		// expect no sections at all

		CountingResult[] results = runTestBranch(-1);
		e.compare(results);
	}

	@Test
	public void measureMultipleLNRForOneMethod_ResultsLNR1() {
		Expectation e = new Expectation();
		e.add(14, 14).add(Opcodes.IINC, 1);

		CountingResult[] results = runTestBranch(1);
		e.compare(results);
	}

	@Test
	public void measureMultipleLNRForOneMethod_ResultsLNR2() {
		Expectation e = new Expectation();
		e.add(1).add(Opcodes.ICONST_1, 1)
				.add(Opcodes.ILOAD, 1)
				.add(Opcodes.ISTORE, 1)
				.add(Opcodes.IMUL, 1);
		
		CountingResult[] results = runTestBranch(10);
		e.compare(results);
	}

	@Test
	public void measureMultipleLNRForOneMethod_ResultsLNR12() {
		Expectation e = new Expectation();
		e.add(14, 14).add(Opcodes.IINC, 1);
		e.add(17, 17).add(Opcodes.ICONST_1, 1)
				.add(Opcodes.ILOAD, 1)
				.add(Opcodes.ISTORE, 1)
				.add(Opcodes.IMUL, 1);

		CountingResult[] results = runTestBranch(9);
		e.compare(results);
	}
	
	/**
	 * 
	 * @param codeAreasToInstrument
	 * @return The result of ByCounter.
	 */
	private CountingResult[] instrumentAndExecute(LineNumberRange[] codeAreasToInstrument) {
		return this.instrumentAndExecute(codeAreasToInstrument, DEFAULT_SUBJECT_CANONICAL, SIGNATURE_METHOD_CALLS);
	}
	
	/**
	 * Instruments the given method and lets ByCounter evaluate it.
	 * 
	 * The method is executed with parameter 10.
	 * 
	 * @param codeAreasToInstrument
	 * @param canonicalClassName
	 * @param methodSignature
	 * @return The result of ByCounter.
	 */
	private CountingResult[] instrumentAndExecute(LineNumberRange[] codeAreasToInstrument, String canonicalClassName, String methodSignature) {
        return this.instrumentAndExecute(codeAreasToInstrument, canonicalClassName, methodSignature, new Object[] { 10 });
	}
	
	/**
	 * Instruments the given method and lets ByCounter evaluate it.
	 * 
	 * @param codeAreasToInstrument
	 * @param canonicalClassName
	 * @param methodSignature
	 * @param executionParameters
	 * @return The result of ByCounter.
	 */
	private CountingResult[] instrumentAndExecute(LineNumberRange[] codeAreasToInstrument, String canonicalClassName, String methodSignature, Object[] executionParameters) {
		// initialize ByCounter
        BytecodeCounter counter = new BytecodeCounter();
        counter.setInstrumentationParams(this.instrumentationParameters);
        counter.getInstrumentationParams().setUseBasicBlocks(true);
        counter.getInstrumentationParams().setRecordBlockExecutionOrder(true);
        MethodDescriptor methodRanged = new MethodDescriptor(canonicalClassName, methodSignature);
        List<EntityToInstrument> entitiesToInstrument = new LinkedList<EntityToInstrument>();
        for(LineNumberRange r : codeAreasToInstrument) {
        	entitiesToInstrument.add(new InstrumentedCodeArea(methodRanged, r));
        }
        counter.addEntityToInstrument(entitiesToInstrument);
        counter.instrument();
        counter.execute(methodRanged, executionParameters);

        return CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults().toArray(new CountingResult[0]);
	}

	/**
	 * Runs the method {@link Branch#process(int)} with the given parameter.
	 * 
	 * @param inputValue
	 *            Input parameter.
	 * @return Counting results after measurement.
	 */
	private CountingResult[] runTestBranch(int inputValue) {
		ArrayList<LineNumberRange> lnrs = new ArrayList<LineNumberRange>();
		lnrs.add(new LineNumberRange(14, 14)); // first branch
		lnrs.add(new LineNumberRange(17, 17)); // second branch
		// initialize ByCounter
		BytecodeCounter counter = new BytecodeCounter();
		counter.setInstrumentationParams(this.instrumentationParameters);
		counter.getInstrumentationParams().setInstrumentRecursively(true);
		counter.getInstrumentationParams().setUseBasicBlocks(true);
		MethodDescriptor methodRanged = new MethodDescriptor(Branch.class.getCanonicalName(), "public int process(int input)");
        List<EntityToInstrument> entitiesToInstrument = new LinkedList<EntityToInstrument>();
        for(LineNumberRange r : lnrs.toArray(new LineNumberRange[0])) {
        	entitiesToInstrument.add(new InstrumentedCodeArea(methodRanged, r));
        }
        counter.addEntityToInstrument(entitiesToInstrument);
        counter.instrument();
		// execute
		Object[] executionParameters = new Object[1];
		executionParameters[0] = new Integer(inputValue);
		counter.execute(methodRanged, executionParameters);

		return CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults().toArray(new CountingResult[0]);
	}

}
