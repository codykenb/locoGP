package de.uka.ipd.sdq.ByCounter.test;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationCounterPrecision;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;

public abstract class AbstractByCounterTest {

	/** These parameters are used by all tests. Revert potential modifications in &#064;After-method. */
	protected InstrumentationParameters instrumentationParameters;
	protected final InstrumentationParameters instrumentationParametersTemplate;

	/**
	 * Generates the different parameters with which all tests are run.
	 *
	 * @return The parameter collection for calling the test constructor.
	 */
	@Parameters
	public static Collection<?> parameterSetup() {
		InstrumentationParameters p1 = new InstrumentationParameters();
		InstrumentationParameters p2 = new InstrumentationParameters();
		InstrumentationParameters p3 = new InstrumentationParameters();
		InstrumentationParameters p4 = new InstrumentationParameters();
		p1.setCounterPrecision(InstrumentationCounterPrecision.Integer);
		p1.setUseHighRegistersForCounting(true);
		p2.setCounterPrecision(InstrumentationCounterPrecision.Integer);
		p2.setUseHighRegistersForCounting(false);
		p3.setCounterPrecision(InstrumentationCounterPrecision.Long);
		p3.setUseHighRegistersForCounting(true);
		p4.setCounterPrecision(InstrumentationCounterPrecision.Long);
		p4.setUseHighRegistersForCounting(false);

		return Arrays.asList(new Object[][] { {p1}, {p2}, {p3}, {p4} });
	}

    /**
     * This constructor is used by the {@link Parameterized} runner for running tests with different
     * parameters.
     *
     * @param params
     *            {@link InstrumentationParameters} template for the counting setup.
     */
	public AbstractByCounterTest(final InstrumentationParameters params) {
		// save the template
		this.instrumentationParametersTemplate = params;
	}

	/**
	 * Clones an instance of {@link InstrumentationParameters} from the template.
	 */
	@Before
	public void setupInstrumentationParameters() {
		this.instrumentationParameters = this.instrumentationParametersTemplate.clone();
	}

	/**
	 * Cleans up results after every test.
	 */
	@After
	public void cleanResults() {
		if(!this.instrumentationParametersTemplate.getEntitiesToInstrument().isEmpty()) {
			throw new RuntimeException("EMAPDM");
		}
	    // clear all collected results
	    CountingResultCollector.getInstance().clearResults();
	}

	/**
	 * @return A {@link BytecodeCounter} instance with parameters selected 
	 * by the test runner.
	 */
	protected BytecodeCounter setupByCounter() {
		BytecodeCounter counter = new BytecodeCounter();
        counter.setInstrumentationParams(this.instrumentationParameters);
		return counter;
	}
}