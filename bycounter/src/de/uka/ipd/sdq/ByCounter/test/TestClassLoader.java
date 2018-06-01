package de.uka.ipd.sdq.ByCounter.test;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.logging.Logger;

import org.junit.Test;

import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector;
import de.uka.ipd.sdq.ByCounter.instrumentation.EntityToInstrument;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedCodeArea;
import de.uka.ipd.sdq.ByCounter.parsing.LineNumberRange;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;
import de.uka.ipd.sdq.ByCounter.test.helpers.ClassLoadTime2;
import de.uka.ipd.sdq.ByCounter.test.helpers.subjects.DoNothing;
import de.uka.ipd.sdq.ByCounter.test.helpers.subjects.LoopExternalAction;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

public class TestClassLoader {
	
	public static void main(String[] args) {
		try {
			new TestClassLoader().testClassLoadTime();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This instruments a method and writes the instrumented .class file.
	 * 
	 * @throws IOException
	 *             When writing fails.
	 */
	@Test
	public void testClassLoadTime() throws IOException {
		//1. Set up a BytecodeCounter instance to use ByCounter, using a parameterless constructor. 
		BytecodeCounter counter = new BytecodeCounter();

		//2. Specify the method to be instrumented (several methods are supported as well)
		String className = ClassLoadTime2.class.getCanonicalName();
		MethodDescriptor myMethod = new MethodDescriptor(
				className,
				"public static void main(java.lang.String[] args)"); //$NON-NLS-1$
		
		
		//3. now tell ByCounter to instrument the specified method
		counter.addEntityToInstrument(myMethod);
		counter.instrument();
		

		byte[] bytes = counter.getInstrumentedBytes();
		
		File file = new File("instrumented_" + className + ".class");
		FileOutputStream fos = new FileOutputStream(
				file);
		fos.write(bytes);
		Logger.getAnonymousLogger().info("Wrote " + file.getAbsolutePath());
		fos.close();
	}

	/**
	 * Tests if dependencies of a Class Under Test (CUT) can successfully be resolved after instantiation and before
	 * execution.
	 */
	@Test
	public void testDependencyResolutionOfClassUnderTest() {
		BytecodeCounter counter = new BytecodeCounter();
		
		String methodSignature = "void process()";
		MethodDescriptor descriptor = new MethodDescriptor(LoopExternalAction.class.getCanonicalName(), methodSignature);
		
		// instrument
		InstrumentationParameters instrumentationParams = new InstrumentationParameters();
		instrumentationParams.setInstrumentRecursively(true);
		instrumentationParams.setUseBasicBlocks(true);
		List<EntityToInstrument> entitiesToInstrument = new ArrayList<EntityToInstrument>();
		entitiesToInstrument.add(
				new InstrumentedCodeArea(descriptor, new LineNumberRange(28, 30))); // loop
		entitiesToInstrument.add(
				new InstrumentedCodeArea(descriptor, new LineNumberRange(31, 31))); // external call within loop
		instrumentationParams.getEntitiesToInstrument().addAll(entitiesToInstrument);
		instrumentationParams.setWriteClassesToDisk(false);
		counter.setInstrumentationParams(instrumentationParams);
		counter.instrument();
		// instantiate
		Object targetObject = counter.instantiate(descriptor);
		// resolve dependencies
		DoNothing doNothing = new DoNothing();
		LoopExternalAction target = (LoopExternalAction) targetObject;
		target.setRequiredComponent(doNothing);
		// execute
		counter.execute(descriptor, target, new Object[0]);
		// show non-null results
		SortedSet<CountingResult> results = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults();
		for (CountingResult result : results) {
			result.logResult(false, true);
		}
		assertNotNull("Results must not be null.", results);
	}
}
