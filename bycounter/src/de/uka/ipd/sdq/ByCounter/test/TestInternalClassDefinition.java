package de.uka.ipd.sdq.ByCounter.test;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.asm.Opcodes;

import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector;
import de.uka.ipd.sdq.ByCounter.execution.ExecutionSettings;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;
import de.uka.ipd.sdq.ByCounter.test.framework.expectations.Expectation;
import de.uka.ipd.sdq.ByCounter.test.framework.expectations.SectionExpectation;
import de.uka.ipd.sdq.ByCounter.test.helpers.ClassZ;
import de.uka.ipd.sdq.ByCounter.test.helpers.InterfaceX;
import de.uka.ipd.sdq.ByCounter.test.helpers.TestSubjectInterfaceMethods;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * This test suite tests the internal class definition of ByCounter.
 * 
 * @author ?
 * @author Florian Schreier
 * @see <a href="https://sdqweb.ipd.kit.edu/bugzilla/show_bug.cgi?id=726">Bugzilla Bug 726</a>
 */
@RunWith(Parameterized.class)
public class TestInternalClassDefinition extends AbstractByCounterTest {
	
	public TestInternalClassDefinition(InstrumentationParameters params) {
		super(params);
	}

	private static final String SIGNATURE_METHODA1 = "public void methodA1()";
	
	/** This logger is used to log all kinds of messages of this test suite. */
	private static final Logger LOG = Logger.getLogger(TestInternalClassDefinition.class.getCanonicalName());

	/**
	 * Tests if pattern matching on internal classes works correctly.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testInternalClassPatternMatcher() {
		String[] definitions = {"de.uka*", "de.uka.Test"};
		HashMap<String, Boolean>[] expectations = new HashMap[2];
		
		expectations[0] = new HashMap<String, Boolean>();
		expectations[0].put("de.ukap.Test", true);
		expectations[0].put("de.uka.ipd", true);
		
		expectations[1] = new HashMap<String, Boolean>();
		expectations[1].put("de.uka.Test", true);
		expectations[1].put("de.uka.Test$XXX$YYY", true);
		expectations[1].put("de.uka.Test.{ENUM Y}", false);

		// iterate through different input definitions
		for (int i = 0; i < 2; i++) {
			HashSet<String> internalClassesDef = new HashSet<String>();
			internalClassesDef.add(definitions[i]);
			ExecutionSettings execParameters = new ExecutionSettings();
			execParameters.setInternalClassesDefinition(internalClassesDef);
			// iterates through all expectations and compares them with reality
			for (String test : expectations[i].keySet()) {
				String message = definitions[i] + " matcht " + test;
				boolean actual = execParameters.isInternalClass(test);
				if (expectations[i].get(test)) {
					Assert.assertTrue(message + " nicht", actual);
					LOG.info(message);
				} else {
					Assert.assertFalse(message, actual);
					LOG.info(message + " nicht");
				}
			}
		}
	}
	

	/**
	 * This unit test instruments {@link TestSubjectInterfaceMethods}.
	 * First {@link TestSubjectInterfaceMethods#methodA1()} is executed without
	 * specifying internal classes. Then, in a second run, internal classes are
	 * specified and the class method is executed again. The classes defined 
	 * as internal are {@link TestSubjectInterfaceMethods} and {@link ClassZ}.
	 */
	@Test
	public void testRetrieveInternalResults() {
		// define expectations (the external call to ClassY should not be inlined)
		// the comments behind add() state where the opcode comes from
		Expectation e = constructMethodA1Expectations(false);
		
		//1. Set up a BytecodeCounter instance to use ByCounter, using a parameterless constructor. 
		BytecodeCounter counter = setupByCounter();

		//2. Specify the method to be instrumented (several methods are supported as well)
		MethodDescriptor methodA1Descriptor = new MethodDescriptor(TestSubjectInterfaceMethods.class.getCanonicalName(), SIGNATURE_METHODA1);
		
		counter.getInstrumentationParams().setInstrumentRecursively(true);
		
		//3. now tell ByCounter to instrument the specified method
		counter.addEntityToInstrument(methodA1Descriptor);
		counter.instrument();

		counter.getExecutionSettings().setAddUpResultsRecursively(true);
		// define internal classes
		{
		Set<String> internalClassesDefinition = new HashSet<String>();
		internalClassesDefinition.add(TestSubjectInterfaceMethods.class.getCanonicalName());
		counter.getExecutionSettings().setInternalClassesDefinition(internalClassesDefinition);
		}

		counter.execute(methodA1Descriptor, new Object[0]);

		// retrieve results
		CountingResult[] results = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults().toArray(new CountingResult[0]);
		e.compare(results);


		CountingResultCollector.getInstance().clearResults();
		// define internal classes
		Set<String> internalClassesDefinition = new HashSet<String>();
		internalClassesDefinition.add(TestSubjectInterfaceMethods.class.getCanonicalName());
		internalClassesDefinition.add(ClassZ.class.getCanonicalName());
		counter.getExecutionSettings().setInternalClassesDefinition(internalClassesDefinition);

		counter.execute(methodA1Descriptor, new Object[0]);
		
		// try retrieving results again and make sure they still match
		results = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults().toArray(new CountingResult[0]);
		e = constructMethodA1Expectations(true);
		e.compare(results);

		Assert.assertNotNull(results);
		Assert.assertTrue(results.length >= 1);
		for(CountingResult newResult: results) {
			newResult.logResult(false, true);
		}
	}


	/**
	 * Helper method for {@link #testRetrieveInternalResults()}.
	 * @param internalClassesDefinition2 When true, use the second definition.
	 * @return expectations.
	 */
	private Expectation constructMethodA1Expectations(boolean internalClassesDefinition2) {
		Expectation e = new Expectation(true);
		SectionExpectation foo = e.add().add(Opcodes.ALOAD, 4) // 1x ClassZ(), 3x methodA1()
			   .add(Opcodes.DUP, 1) // 1x methodA1()
			   .add(Opcodes.GETFIELD, 2) // 2x methodA1()
			   .add(Opcodes.GETSTATIC, 2) // 1x ClassY.mexthodX1(), 1x ClassZ.mexthodX1()
			   .add(Opcodes.INVOKEINTERFACE, 2) // 2x methodA1()
			   .add(Opcodes.INVOKESPECIAL, 2) // 1x ClassZ(), 1x methodA1()
			   .add(Opcodes.INVOKEVIRTUAL, 2) // 1x ClassY.mexthodX1(), 1x ClassZ.mexthodX1()
			   .add(Opcodes.LDC, 2) // 1x ClassZ.mexthodX1(), 1x ClassY.methodX1()
			   .add(Opcodes.NEW, 1) // 1x methodA1()
			   .add(Opcodes.PUTFIELD, 1) // 1x mexthodA1()
			   .add(Opcodes.RETURN, 4) // 1x ClassY.mexthodX1(), 1x ClassZ.mexthodX1(), 1x ClassZ(), 1x methodA1()
			   .add(InterfaceX.class.getCanonicalName(), "void methodX1()", 2) // 2x methodA1()
			   .add(PrintStream.class.getCanonicalName(), "public void println(java.lang.String x)", 2) // 1x ClassY.methodX1(), 1x ClassZ.methodX1()
			   .add("java.lang.Object.Object()V", 1) // 1x ClassZ()
			   ;
		if(!internalClassesDefinition2) {
			   foo.add("de.uka.ipd.sdq.ByCounter.test.helpers.ClassZ.ClassZ()V", 1) // 1x methodA1()
			   ;
		}
		return e;
	}

}
