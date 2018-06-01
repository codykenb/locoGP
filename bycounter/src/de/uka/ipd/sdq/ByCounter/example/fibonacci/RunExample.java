package de.uka.ipd.sdq.ByCounter.example.fibonacci;

import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultBase;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedCodeArea;
import de.uka.ipd.sdq.ByCounter.parsing.LineNumberRange;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**Runs ByCounter on the Fibonacci algorithm and stores the counting results.
 *
 * This example shows how to use ByCounter to instrument a specific method and
 * execute it not by itself, but in its regular context, i.e. as in the method
 * {@link #countFibonacciOneIteration()}.
 * @author groenda
 *
 */
public class RunExample {
	/** Logger of this class.*/
	public static Logger logger = Logger.getLogger(RunExample.class.getCanonicalName());

	/**Measures the issued Bytecode instructions for the FibonacciAlgorithm class under test.
	 * A static method is used for the test.
	 */
	public void measureBytecodeInstructionsStaticMethod() {
		// prepare classes for counting
		BytecodeCounter counter = new BytecodeCounter();
		String className = FibonacciAlgorithm.class.getCanonicalName();
		MethodDescriptor fibonacciAlgorithmMD = new MethodDescriptor(
				className,
				"public long fibonacci(long rounds)"); //$NON-NLS-1$
		counter.addEntityToInstrument(fibonacciAlgorithmMD);
		counter.instrument();

		// execute method
		// executing the test method supports setting initialization parameters
		MethodDescriptor fibonacciTestRunner = new MethodDescriptor(
				this.getClass().getCanonicalName(),
				"public static void countFibonacciOneIteration()");
		Object[] executionParameters = new Object[] {};
		counter.execute(fibonacciTestRunner, executionParameters);

		// Output
		SortedSet<CountingResult> results =
			CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults();
		// output the results to the console/log
		for(CountingResultBase r : results) {
			r.logResult(false, true);
		}

		// clear the results as we do not need them anymore
		CountingResultCollector.getInstance().clearResults();
	}

	/**Measures the issued Bytecode instructions for the FibonacciAlgorithm class under test.
	 * A class instance's method is used for the test.
	 * @param rounds Execution parameter of algorithm under test.
	 */
	public void measureBytecodeInstructionsInstanceMethod(long rounds) {
		// prepare classes for counting
		BytecodeCounter counter = new BytecodeCounter();
		String className = FibonacciAlgorithm.class.getCanonicalName();
		MethodDescriptor fibonacciAlgorithmMD = new MethodDescriptor(
				className,
				"public long fibonacci(long rounds)"); //$NON-NLS-1$
		counter.addEntityToInstrument(fibonacciAlgorithmMD);
		counter.instrument();

		// execute method
		// executing the test method supports setting initialization parameters
		MethodDescriptor fibonacciTestRunner = new MethodDescriptor(
				FibonacciAlgorithm.class.getCanonicalName(),
				"public long fibonacci(long rounds)");
		Object[] executionParameters = new Object[] {rounds};
		counter.execute(fibonacciTestRunner, executionParameters);

		// Output
		SortedSet<CountingResult> results =
				CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults();
		// output the results to the console/log
		for(CountingResultBase r : results) {
			r.logResult(false, true);
		}

		// clear the results as we do not need them anymore
		CountingResultCollector.getInstance().clearResults();
	}

	/**Measures the issued Bytecode instructions for the FibonacciAlgorithm class under test.
	 * A class instance's method is used for the test. Just the content and not the method itself is measured.
	 * @param rounds Execution parameter of algorithm under test.
	 */
	public void measureBytecodeInstructionsInstanceMethodContent(long rounds) {
		// prepare classes for counting
		BytecodeCounter counter = new BytecodeCounter();
		String className = FibonacciAlgorithm.class.getCanonicalName();
		MethodDescriptor fibonacciAlgorithmMD = new MethodDescriptor(
				className,
				"public long fibonacci(long rounds)"); //$NON-NLS-1$
		LineNumberRange codeAreasToInstrument = new LineNumberRange(20,	32);
		counter.getInstrumentationParams().setUseBasicBlocks(true);
		counter.getInstrumentationParams().getEntitiesToInstrument().add(
				new InstrumentedCodeArea(fibonacciAlgorithmMD, codeAreasToInstrument));
		counter.instrument();

		// execute method
		// executing the test method supports setting initialization parameters
		MethodDescriptor fibonacciTestRunner = new MethodDescriptor(
				FibonacciAlgorithm.class.getCanonicalName(),
				"public long fibonacci(long rounds)");
		Object[] executionParameters = new Object[] {rounds};
		counter.execute(fibonacciTestRunner, executionParameters);

		// Output
		SortedSet<CountingResult> results =
				CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults();
		// output the results to the console/log
		for(CountingResultBase r : results) {
			r.logResult(false, true);
		}

		// clear the results as we do not need them anymore
		CountingResultCollector.getInstance().clearResults();
	}

	/**Testcase for which the Bytecode instructions should be counted.
	 * Allows to initialize the class(es) under test. Method is static
	 * to allow ByCounter execution without initializing an instance of the
	 * RunExample class.
	 */
	public static void countFibonacciOneIteration() {
		FibonacciAlgorithm algorithm = new FibonacciAlgorithm();
		algorithm.fibonacci(1);
	}

	/**Wrapper to start the Bytecode counting.
	 * @param args Not used.
	 */
	public static void main(String[] args) {
		RunExample example = new RunExample();
		long rounds;
		// static method
		logger.log(Level.INFO, "Execution using a static method.");
		example.measureBytecodeInstructionsStaticMethod();

		// instance method (whole method)
		rounds = 10;
		logger.log(Level.INFO, "Execution using a dynamic method with parameter rounds=" + rounds + ".");
		example.measureBytecodeInstructionsInstanceMethod(rounds);

		// instance method (only content of method)
		rounds = 10;
		logger.log(Level.INFO, "Execution using a dynamic method with parameter rounds=" + rounds + " and measuring the contents of the method.");
		example.measureBytecodeInstructionsInstanceMethodContent(rounds);
	}

}
