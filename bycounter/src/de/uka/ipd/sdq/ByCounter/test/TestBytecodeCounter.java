package de.uka.ipd.sdq.ByCounter.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector;
import de.uka.ipd.sdq.ByCounter.instrumentation.AlreadyInstrumentedException;
import de.uka.ipd.sdq.ByCounter.instrumentation.EntityToInstrument;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationScopeModeEnum;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedClass;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedMethod;
import de.uka.ipd.sdq.ByCounter.instrumentation.Instrumenter;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;
import de.uka.ipd.sdq.ByCounter.test.helpers.ASMBytecodeOccurences;
import de.uka.ipd.sdq.ByCounter.test.helpers.TestSubject;
import de.uka.ipd.sdq.ByCounter.test.helpers.TestSubjectInterfaceMethods;
import de.uka.ipd.sdq.ByCounter.test.helpers.Utils;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * This test suite tests several different usage patterns for ByCounter.
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
@RunWith(Parameterized.class)
public class TestBytecodeCounter extends AbstractByCounterTest {
	
	private static final String CLASS_DIR = "bin" + File.separatorChar + 
						File.separatorChar + "de" + 
						File.separatorChar + "uka" +
						File.separatorChar + "ipd" +
						File.separatorChar + "sdq" +
						File.separatorChar + "ByCounter" +
						File.separatorChar + "test" +
						File.separatorChar + "helpers" + 
						File.separatorChar;

	private static final String METHOD_SIGNATURE_ARRAY_INSTUCTIONS = "public static void arrayInstructions()";
	
	private static String nestedClassMethodSig1 = "public TestSubject$InnerClass$InnerClassLevel2()";
	
	private static String nestedClassMethodSig2 = "public boolean isWorking(int a)";

	private static String nestedClassRunMethodSig = "public void useInnerClassLevel2()";

	private static final String resultLogFileName = "output" + File.separatorChar +"tmpLogFile.log";
	
	private static String testClassMethodCallTest = "public void methodCallTest()";
	
	/**
	 * This constructor is used by the Parametrized runner 
	 * for running tests with different parameters.
	 * @param params {@link InstrumentationParameters} template for the counting setup.
	 */
	public TestBytecodeCounter(InstrumentationParameters params) {
		super(params);
	}
	
	/**
	 * Reads the class file and returns its contents.
	 * @param file A {@link File} pointing to a class
	 * @return The contents of the file as byte[].
	 */
	private static byte[] readClassFromFile(File file) {
		FileInputStream fs = null;
		
		// create a file stream for the .class data
		try {
			fs = new FileInputStream(file);

			// get the data into a Byte[] array.
			ArrayList<Byte> bytelist = new ArrayList<Byte>();
			try {
				while(fs.available() > 0) {
					bytelist.add((byte)fs.read());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			Byte[] bytesBig = bytelist.toArray(new Byte[bytelist.size()]);
			
			// get the data from the Byte[] array into the byte[] array.
			byte[] bytes = new byte[bytelist.size()];
			for(int i = 0; i < bytesBig.length; i++) {
				bytes[i] = bytesBig[i].byteValue();
			}
			
			return bytes;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				fs.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * This unit test tries to instrument a class that is given as byte[].
	 * In this case the .class file of ASMBytecodeOccurences is used.
	 *
	 */
	@Test
	public void testByteClass() {
		File file = new File(CLASS_DIR + ASMBytecodeOccurences.class.getSimpleName() + ".class");
		byte[] bytes = readClassFromFile(file);
		
		// early CountingResultCollector construction; initialize the singleton
		CountingResultCollector resultColl = CountingResultCollector.getInstance();
		
		Assert.assertNotNull(resultColl);
		
		// create a BytecodeCounter
		BytecodeCounter counter = setupByCounter();
		Assert.assertNotNull(counter);
		counter.setClassToInstrument(bytes);
		
		// do the de.uka.ipd.sdq.ByCount
		// let the counter do its work on a method
		MethodDescriptor methodToInstrument = new MethodDescriptor(ASMBytecodeOccurences.class.getCanonicalName(), METHOD_SIGNATURE_ARRAY_INSTUCTIONS);
		counter.addEntityToInstrument(methodToInstrument);
		counter.instrument();

		counter.execute(methodToInstrument, new Object[0]);
		
		Assert.assertNotNull(resultColl.retrieveAllCountingResults());
		// print the results into the log
		for(CountingResult r : resultColl.retrieveAllCountingResults().getCountingResults()) {
			Assert.assertNotSame(r.getOpcodeCounts().length, 0);
			r.logResult(false, true);
		}

	}
	
	/**TODO: This test does no tests yet
	 * This test uses the {@link Instrumenter} on the method methodTest(),
	 * which is a method that calls other methods of the TestSubject class.
	 * It is then tested whether the results calculated by 
	 * {@link CountingResultCollector} are sane.
	 */
	@Test
	public void testCallingTreeResults() {
		BytecodeCounter counter = setupByCounter();
		// instrument the methodCallTest
		// flat results
		MethodDescriptor methodDescriptorMCT = 
			new MethodDescriptor(TestSubject.class.getCanonicalName(), 
				testClassMethodCallTest);
		MethodDescriptor methodDescriptorLT = 
			new MethodDescriptor(TestSubject.class.getCanonicalName(), 
					"public int loopTest()");
		MethodDescriptor methodDescriptorPT =  
			new MethodDescriptor(TestSubject.class.getCanonicalName(), 
			"public void printTest()");
		
		ArrayList<InstrumentedMethod> methodsToInstrument = new ArrayList<InstrumentedMethod>();
		methodsToInstrument.add(new InstrumentedMethod(methodDescriptorMCT));
		methodsToInstrument.add(new InstrumentedMethod(methodDescriptorLT));
		methodsToInstrument.add(new InstrumentedMethod(methodDescriptorPT));
		this.instrumentationParameters.getEntitiesToInstrument().addAll(methodsToInstrument);
		counter.setInstrumentationParams(this.instrumentationParameters);
		counter.instrument();
		counter.execute(methodDescriptorMCT, new Object[0]);

		CountingResult[] countingResults = 
			CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults().toArray(new CountingResult[0]);
		Assert.assertTrue("Could not get any counting results.", countingResults.length > 0);
		CountingResult r1 = countingResults[countingResults.length-1];	// last is methodCallTest
		r1.logResult(false, true);
		CountingResultCollector.getInstance().clearResults();
		// calling tree results
		counter.getExecutionSettings().setAddUpResultsRecursively(true);
		counter.execute(methodDescriptorMCT, new Object[0]);
		
		countingResults = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults().toArray(new CountingResult[0]);
		Assert.assertTrue("Could not get any counting results.", countingResults.length > 0);
		CountingResult r2 = countingResults[countingResults.length-1];
		r2.logResult(false, true);
	}

	/**
	 * Test for writing to log file instead of using CountingResultCollector.
	 */
	@Test
	public void testDirectResultWriting() {
		// create a BytecodeCounter
		BytecodeCounter counter = setupByCounter();
		Assert.assertNotNull(counter);
		
		// disable usage of result collector
		Assert.assertNotNull(counter.getInstrumentationParams());
		counter.getInstrumentationParams().setUseResultCollector(false);
		Assert.assertEquals(false, counter.getInstrumentationParams().getUseResultCollector());
		
		// set the file name for the result log
		counter.getInstrumentationParams().enableResultLogWriter(resultLogFileName);
		counter.getInstrumentationParams().setUseArrayParameterRecording(true);
		Assert.assertEquals(resultLogFileName, counter.getInstrumentationParams().getResultLogFileName());

		// test with void method
		MethodDescriptor methodDescriptor = new MethodDescriptor(
				TestSubject.class.getCanonicalName(), "public void methodCallTest()");
		counter.addEntityToInstrument(methodDescriptor);
		counter.instrument();
		counter.execute(methodDescriptor, new Object[]{});
		
		// The log file name is dynamic and cannot be checked
		cleanResults();

		// test with boolean method
		methodDescriptor = new MethodDescriptor(TestSubject.class.getCanonicalName(), 
			"public boolean parameterTest(int i, float f, java.lang.String s)");
		counter.addEntityToInstrument(methodDescriptor);
		counter.instrument();
		counter.execute(methodDescriptor, 
			new Object[]{2, 2, TestSubject.class.getCanonicalName()});
	}
	
	/**
	 * This unit test tries to instrument a class that is given as byte[].
	 * In this case the .class file of ASMBytecodeOccurences is used.
	 *
	 */
	@Test
	public void testGenerateCallTree() {
		//1. Set up a BytecodeCounter instance to use ByCounter, using a parameterless constructor. 
		BytecodeCounter counter = setupByCounter();

		//2. Specify the method to be instrumented (several methods are supported as well)
		MethodDescriptor myMethod = new MethodDescriptor(
				TestSubject.class.getCanonicalName(),
				testClassMethodCallTest); //$NON-NLS-1$
		
		counter.getInstrumentationParams().setInstrumentRecursively(true);
		
		//3. now tell ByCounter to instrument the specified method
		counter.addEntityToInstrument(myMethod);
		counter.instrument();
		// enable recursive result counting
		counter.getExecutionSettings().setAddUpResultsRecursively(true);
		counter.execute(myMethod, new Object[0]);

		SortedSet<CountingResult> countingResults = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults();
		Assert.assertNotNull(countingResults);
		Assert.assertTrue(countingResults.size() == 1);
		for(CountingResult newResult : countingResults) {
			newResult.logResult(false, true);
		}
	}

	/**
	 * This unit test tries to instrument a class using instrumentAll.
	 */
	@Test
	public void testInstrumentAll() {
		//1. Set up a BytecodeCounter instance to use ByCounter, using a parameterless constructor. 
		BytecodeCounter counter = setupByCounter();
		
		//2. now tell ByCounter to instrument all methods
		counter.getInstrumentationParams().setInstrumentationScopeOverrideClassLevel(InstrumentationScopeModeEnum.InstrumentEverything);
		List<EntityToInstrument> entitiesToInstrument = new LinkedList<EntityToInstrument>();
		entitiesToInstrument.add(new InstrumentedClass(TestSubject.class.getCanonicalName()));
		counter.addEntityToInstrument(entitiesToInstrument);
		counter.instrument();

		//3. Specify the method to be executed
		MethodDescriptor myMethod = new MethodDescriptor(
				TestSubject.class.getCanonicalName(),
				testClassMethodCallTest);
		counter.getExecutionSettings().setAddUpResultsRecursively(false);
		counter.execute(myMethod, new Object[0]);

		SortedSet<CountingResult> countingResults = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults();
		Assert.assertNotNull(countingResults);
		Assert.assertTrue(countingResults.size() > 1);
		for(CountingResult newResult: countingResults) {
			newResult.logResult(false, true);
		}
	}
	
	
	/**
	 * This unit test tries to instrument a class that is given as byte[].
	 * In this case the .class file of ASMBytecodeOccurences is used.
	 *
	 */
	@Test
	public void testInstrumetingTwice() {
		File file = new File(CLASS_DIR + ((Class<?>) ASMBytecodeOccurences.class).getSimpleName() + ".class");
		byte[] bytes = readClassFromFile(file);
		
		Assert.assertNotNull(bytes);
		
		if(bytes == null) {
			return;
		}
		
		// CountingResultCollector construction; initialize the singleton
		CountingResultCollector resultColl = CountingResultCollector.getInstance();
		
		Assert.assertNotNull(resultColl);
		
		// create a BytecodeCounter
		BytecodeCounter counter = new BytecodeCounter();
		Assert.assertNotNull(counter);
		counter.setClassToInstrument(bytes);
		counter.setInstrumentationParams(this.instrumentationParameters);
		
		// do the de.uka.ipd.sdq.ByCount
		// let the counter do its work on a method
		counter.addEntityToInstrument(
				new MethodDescriptor(((Class<?>) ASMBytecodeOccurences.class).getCanonicalName(), METHOD_SIGNATURE_ARRAY_INSTUCTIONS));
		counter.instrument();
		
		byte[] instrumentedBytes = counter.getInstrumentedBytes();
		Assert.assertNotNull(instrumentedBytes);
		
		counter.setClassToInstrument(instrumentedBytes);
	
		boolean exceptionThrown = false;
		try {
		counter.addEntityToInstrument(
				new MethodDescriptor(((Class<?>) ASMBytecodeOccurences.class).getCanonicalName(), METHOD_SIGNATURE_ARRAY_INSTUCTIONS));
		counter.instrument();
		} catch(AlreadyInstrumentedException e) {
			exceptionThrown = true;
		}
		Assert.assertTrue("Expected an exception to be thrown.", exceptionThrown);
//		
//		Assert.assertNotNull(resultColl.getAllCountingResults_nonRecursively());
//		// print the results into the log
//		for(CountingResult r : resultColl.getAllCountingResults_nonRecursively()) {
//			Assert.assertNotSame(r.getOpcodeCounts().size(), 0);
//			resultColl.logResult(r, true, true);
//		}

	}
	

	/**
	 * This unit test tries to instruments {@link TestSubject}
	 * and instantiates it. Then it instruments differently and tries to 
	 * instantiate again.
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	@Test
	public void testInstantiateTwice() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, SecurityException {
		// create a BytecodeCounter
		BytecodeCounter counter = setupByCounter();
		Assert.assertNotNull(counter);
		
		// instrument a method
		counter.addEntityToInstrument(
				new MethodDescriptor(TestSubject.class.getCanonicalName(), "public void methodCallTest()"));
		counter.instrument();
		MethodDescriptor methodToExecute = new MethodDescriptor(TestSubject.class.getCanonicalName(), "public void printTest()");
		Object instObject = counter.instantiate(methodToExecute);
		Object[] params = new String[0];
		instObject.getClass().getDeclaredMethods();
		counter.execute(methodToExecute, params);
		
		
//		counter.addEntityToInstrument(
//				new MethodDescriptor(TestSubject.class.getCanonicalName(), "public int loopTest()"));
//		counter.instrument();
		methodToExecute = new MethodDescriptor(TestSubject.class.getCanonicalName(), "public void printTest()");
		instObject.getClass().getDeclaredMethods();
		instObject = counter.instantiate(methodToExecute);
		instObject.getClass().getDeclaredMethods();
		counter.execute(methodToExecute, params);
	}
	
	/**
	 * This test uses the instrumenter on inner classes using the classfile 
	 * overwriting option which is important as 
	 * nested classes follow a special .class file naming scheme.
	 */
	@Test
	public void testNestedClassInstrumentation() {
		BytecodeCounter counter = setupByCounter();

		String classNameInnerClassLevel2 = TestSubject.class.getCanonicalName() 
			+"$" + TestSubject.InnerClass.class.getSimpleName() 
			+"$" + TestSubject.InnerClass.InnerClassLevel2.class.getSimpleName();
		
		// instrument the constructor of the nested class		
		CountingResult r = Utils.getCountingResultForTest(
				counter,
				new MethodDescriptor(classNameInnerClassLevel2, 
						nestedClassMethodSig1),
				new MethodDescriptor(TestSubject.class.getCanonicalName(), 
						nestedClassRunMethodSig));

		r.logResult(false, true);
		
		cleanResults();
		
		// instrument the method of the nested class
		r = Utils.getCountingResultForTest(
				counter,
				new MethodDescriptor(classNameInnerClassLevel2, 
						nestedClassMethodSig2),
				new MethodDescriptor(TestSubject.class.getCanonicalName(), 
						nestedClassRunMethodSig));

		r.logResult(false, true);
	}
	

	/**
	 * This unit test tries to recursively instrument a class that makes calls 
	 * to an interface.
	 *
	 */
	@Test
	public void testInterfaceMethodRecursiveInstrumentation() {
		//1. Set up a BytecodeCounter instance to use ByCounter, using a parameterless constructor. 
		BytecodeCounter counter = setupByCounter();

		//2. Specify the method to be instrumented (several methods are supported as well)
		MethodDescriptor myMethod = new MethodDescriptor(
				TestSubjectInterfaceMethods.class.getCanonicalName(),
				"void methodA1()");
		
		counter.getInstrumentationParams().setInstrumentRecursively(true);
		
		//3. now tell ByCounter to instrument the specified method
		counter.addEntityToInstrument(myMethod);
		counter.instrument();
		counter.getExecutionSettings().setAddUpResultsRecursively(true);
		counter.execute(myMethod, new Object[0]);

		SortedSet<CountingResult> allCountingResultsRecursively = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults();
		Assert.assertNotNull(allCountingResultsRecursively);
		allCountingResultsRecursively.first().logResult(false, false);
		Assert.assertTrue("One counting result is expected.", allCountingResultsRecursively.size() == 1);
		for(CountingResult newResult: allCountingResultsRecursively) {
			newResult.logResult(false, true);
		}
		
		counter.getInstrumentationParams().setInstrumentRecursively(false);
	}
	

	/**
	 * This unit test tries to recursively instrument a class that makes calls 
	 * to an interface.
	 *
	 */
	@Test
	public void testInstrumentationNoPackageName() {
		//1. Set up a BytecodeCounter instance to use ByCounter, using a parameterless constructor. 
		BytecodeCounter counter = setupByCounter();

		//2. Specify the method to be instrumented (several methods are supported as well)
		MethodDescriptor myMethod = new MethodDescriptor(
				"ClassInDefaultPackage",
				"public static void main(java.lang.String[] args)");
		
		//3. now tell ByCounter to instrument the specified method
		counter.addEntityToInstrument(myMethod);
		counter.instrument();
		counter.getExecutionSettings().setAddUpResultsRecursively(true);
		counter.execute(myMethod, new Object[]{new String[0]});

		SortedSet<CountingResult> allCountingResultsRecursively = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults();
		Assert.assertNotNull(allCountingResultsRecursively);
		for(CountingResult newResult: allCountingResultsRecursively) {
			newResult.logResult(false, true);
		}
	}
	
	/**
	 * This instruments a method in {@link TestSubject} and writes the 
	 * instrumented .class file. 
	 * @throws IOException When writing fails.
	 */
	@Test
	public void testWriteClass() throws IOException {
		//1. Set up a BytecodeCounter instance to use ByCounter, using a parameterless constructor. 
		BytecodeCounter counter = setupByCounter();

		//2. Specify the method to be instrumented (several methods are supported as well)
		String className = TestSubject.class.getCanonicalName();
		MethodDescriptor myMethod = new MethodDescriptor(
				className,
				"public void methodCallTest()"); //$NON-NLS-1$
		
		
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
}
