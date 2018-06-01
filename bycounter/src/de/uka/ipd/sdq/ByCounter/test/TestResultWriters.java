package de.uka.ipd.sdq.ByCounter.test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.SortedSet;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;
import de.uka.ipd.sdq.ByCounter.reporting.ChartResultWriter;
import de.uka.ipd.sdq.ByCounter.reporting.CountingResultCSVWriter;
import de.uka.ipd.sdq.ByCounter.reporting.PdfReport;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;
import de.uka.ipd.sdq.ByCounter.test.helpers.TestSubject;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * This test suite tests several different usage patterns for ByCounter.
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
@RunWith(Parameterized.class)
public class TestResultWriters extends AbstractByCounterTest {

	private static Logger log = Logger.getLogger(TestResultWriters.class.toString());

	private static final String resultLogFileName = "output" + File.separatorChar +"tmpLogFile.log";

	private static final String testChartName = "testMyChart";

	private static final String testChartsDirectory = "testCharts";
	
	private static final String TEST_CSV_DIRECTORY = "outputCsv";

	private static final String CLASS_TEST_SUBJECT = TestSubject.class.getCanonicalName();

	private static final String METHOD_METHOD_CALL_TEST = "public void methodCallTest()";

	private static final String METHOD_PARAMETER_TEST = "public boolean parameterTest(int i, float f, java.lang.String s)";

	/**
	 * This constructor is used by the Parametrized runner
	 * for running tests with different parameters.
	 * @param params {@link InstrumentationParameters} for the counting setup.
	 */
	public TestResultWriters(InstrumentationParameters params) {
		// save the template
		super(params);
	}
	
	/**
	 * Test the usage of the {@link ChartResultWriter}.
	 */
	@Test
	public void testChartWriter() {


		// create a BytecodeCounter
		BytecodeCounter counter = setupByCounter();
		Assert.assertNotNull(counter);

		CountingResultCollector resultColl = CountingResultCollector.getInstance();
		ChartResultWriter resultWriter =
			new ChartResultWriter(testChartsDirectory, testChartName);
		resultColl.registerWriter(
				resultWriter
			);

		// test with void method
		MethodDescriptor methodDescriptor = new MethodDescriptor(
				CLASS_TEST_SUBJECT, METHOD_METHOD_CALL_TEST);
		counter.addEntityToInstrument(methodDescriptor);
		counter.instrument();
		counter.execute(methodDescriptor, new Object[]{});



		SortedSet<CountingResult> finalResults = resultColl.retrieveAllCountingResults().getCountingResults();
		Assert.assertEquals(1, finalResults.size());
		log.info(finalResults.size()+" counting results found, logging them: ");
		for(CountingResult r : finalResults) {
			r.logResult(true, true); //including delegation to registered writers
		}
		// clear all collected results - they should not be output during the next run
		resultColl.clearResults();


		// check whether a file was written
		File f = new File(testChartsDirectory + File.separator + testChartName + ".pdf");
		Assert.assertTrue("No chart file was created.", f.exists());
		Assert.assertTrue("Created file is empty.", f.length() > 0);

		// remove file again
		if(f.exists()) {
			f.delete();
		}
	}

	/**
	 * Tests reading back a {@link CountingResult} instance from a written csv.
	 */
	@Test
	public void testCSVReading() {
		File csvDirectory = new File(TEST_CSV_DIRECTORY);
		if(!csvDirectory.exists()) {
			csvDirectory.mkdir();
		}

		// create a BytecodeCounter
		BytecodeCounter counter = setupByCounter();
		Assert.assertNotNull(counter);

		String resultFileNameSpecifier = "";

		CountingResultCollector resultColl = CountingResultCollector.getInstance();
		CountingResultCSVWriter resultWriter = new CountingResultCSVWriter(
				true, 	// append grand total at the end
				',',  	// char (character) for separating entries in the file
				true, 	// listInvokeOpcodes
				true, 	// performIntegrityCheckOnInvokeOpcodes
				"Test."+resultFileNameSpecifier, // CSV file name "core"
				"csv", 	// CSV file name "suffix" (file extension)
				TEST_CSV_DIRECTORY, 	// path to CSV file
				true, 	// write booleans as integers
				true, 	// write opcodes as integers (not as String pseudonames)
				true, 	// write unused opcodes, too (i.e. those with count 0)
				true,	// truncate undefined opcodes (i.e. those with >199)
				true	// write array details to a separate file
		);
		resultColl.registerWriter(resultWriter);


		// test with void method
		MethodDescriptor methodDescriptor = new MethodDescriptor(
				CLASS_TEST_SUBJECT, METHOD_METHOD_CALL_TEST);
		counter.addEntityToInstrument(methodDescriptor);
		counter.instrument();
		counter.execute(methodDescriptor, new Object[]{});




		SortedSet<CountingResult> finalResults = resultColl.retrieveAllCountingResults().getCountingResults();
		Assert.assertEquals(1, finalResults.size());
		log.info(finalResults.size()+" counting results found, logging them: ");
		for(CountingResult r : finalResults) {
			r.logResult(true, true); //including delegation to registered writers
		}
		// clear all collected results - they should not be output during the next run
		resultColl.clearResults();


		// check whether a file was written
		File f = resultWriter.getLastWrittenFile();
		Assert.assertTrue("No csv file was created.", f.exists());
		Assert.assertTrue("Created file is empty.", f.length() > 0);

		try {
			File csvFile = f;
			Assert.assertTrue(csvFile.exists());
			CountingResult cr =
				CountingResultCSVWriter.readCountingResultFromCSV(
						csvFile);
			cr.logResult(false, true);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		} finally {
			// remove file again
			if(f.exists()) {
				f.delete();
			}
		}
	}

	/**
	 * Test the usage of the {@link CountingResultCSVWriter}.
	 */
	@Test
	public void testCSVWriter() {
		File csvDirectory = new File(TEST_CSV_DIRECTORY);
		if(!csvDirectory.exists()) {
			csvDirectory.mkdir();
		}

		// create a BytecodeCounter
		BytecodeCounter counter = setupByCounter();
		Assert.assertNotNull(counter);

		String resultFileNameSpecifier = "";

		CountingResultCollector resultColl = CountingResultCollector.getInstance();
		CountingResultCSVWriter resultWriter = new CountingResultCSVWriter(
				true, 	// append grand total at the end
				',',  	// char (character) for separating entries in the file
				true, 	// listInvokeOpcodes
				true, 	// performIntegrityCheckOnInvokeOpcodes
				"Test."+resultFileNameSpecifier, // CSV file name "core"
				"csv", 	// CSV file name "suffix" (file extension)
				TEST_CSV_DIRECTORY, 	// path to CSV file
				true, 	// write booleans as integers
				true, 	// write opcodes as integers (not as String pseudonames)
				true, 	// write unused opcodes, too (i.e. those with count 0)
				true,	// truncate undefined opcodes (i.e. those with >199)
				true	// write array details to a separate file
		);
		resultColl.registerWriter(resultWriter);


		// test with void method
		MethodDescriptor methodDescriptor = new MethodDescriptor(
				CLASS_TEST_SUBJECT, METHOD_METHOD_CALL_TEST);
		counter.addEntityToInstrument(methodDescriptor);
		counter.instrument();
		counter.execute(methodDescriptor, new Object[]{});




		SortedSet<CountingResult> finalResults = resultColl.retrieveAllCountingResults().getCountingResults();
		Assert.assertEquals(1, finalResults.size());
		log.info(finalResults.size()+" counting results found, logging them: ");
		for(CountingResult r : finalResults) {
			r.logResult(true, true); //including delegation to registered writers
		}
		// clear all collected results - they should not be output during the next run
		resultColl.clearResults();


		// check whether a file was written
		File f = resultWriter.getLastWrittenFile();
		Assert.assertTrue("No csv file was created.", f.exists());
		Assert.assertTrue("Created file is empty.", f.length() > 0);

		// remove file again
		if(f.exists()) {
			f.delete();
		}
	}


	/**
	 * Test for writing to log file instead of or in addition to 
	 * using CountingResultCollector.
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
				CLASS_TEST_SUBJECT, METHOD_METHOD_CALL_TEST);
		counter.addEntityToInstrument(methodDescriptor);
		counter.instrument();
		counter.execute(methodDescriptor, new Object[]{});

		// check whether a file was written
		checkAndDeleteFile(resultLogFileName);
		cleanResults();

		// test with boolean method
		methodDescriptor = new MethodDescriptor(CLASS_TEST_SUBJECT,
			METHOD_PARAMETER_TEST);
		counter.addEntityToInstrument(methodDescriptor);
		counter.instrument();
		counter.execute(methodDescriptor,
			new Object[]{2, 2, CLASS_TEST_SUBJECT});

		// check whether a file was written
		checkAndDeleteFile(resultLogFileName);
		
		// enable CountingResultCollector additionally
		counter.getInstrumentationParams().setUseResultCollector(true);

		// test with boolean method
		methodDescriptor = new MethodDescriptor(CLASS_TEST_SUBJECT,
			METHOD_PARAMETER_TEST);
		counter.addEntityToInstrument(methodDescriptor);
		counter.instrument();
		counter.execute(methodDescriptor,
			new Object[]{2, 2, CLASS_TEST_SUBJECT});

		// check whether a file was written
		checkAndDeleteFile(resultLogFileName);
	}


	/**
	 * Test the usage of the {@link PdfReport}.
	 */
	@Test
	public void testPdfReport() {


		// create a BytecodeCounter
		BytecodeCounter counter = setupByCounter();
		Assert.assertNotNull(counter);

		CountingResultCollector resultColl = CountingResultCollector.getInstance();
		PdfReport pdfReport = new PdfReport();
		resultColl.registerWriter(pdfReport);

		// test with void method
		MethodDescriptor methodDescriptor = new MethodDescriptor(
				CLASS_TEST_SUBJECT, METHOD_METHOD_CALL_TEST);
		counter.addEntityToInstrument(methodDescriptor);
		counter.instrument();
		counter.execute(methodDescriptor, new Object[]{});




		SortedSet<CountingResult> finalResults = resultColl.retrieveAllCountingResults().getCountingResults();
		Assert.assertEquals(1, finalResults.size());
		log.info(finalResults.size()+" counting results found, logging them: ");
		for(CountingResult r : finalResults) {
			r.logResult(true, true); //including delegation to registered writers
		}
		// clear all collected results - they should not be output during the next run
		resultColl.clearResults();


		// check whether a file was written
		File f = pdfReport.getLastWrittenFile();
		Assert.assertTrue("No pdf file was created.", f.exists());
		Assert.assertTrue("Created file is empty.", f.length() > 0);

		// remove file again
		if(f.exists()) {
			f.delete();
		}
	}

	/**
	 * Checks whether a file was created from the file name template.
	 * @param resultLogFileName Log filename template
	 */
	private void checkAndDeleteFile(String resultLogFileName) {
		// check whether a file was written
		final File f = new File(resultLogFileName);
		// The true result log file name has attached time stamps!
		String[] files = f.getParentFile().list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if(name.startsWith(f.getName())) {
					return true;
				}
				return false;
			}
		});
		Assert.assertNotSame("No log file was created.", 0, files.length);
		File realF = new File(f.getParentFile(), files[0]);
		Assert.assertTrue("Created file is empty.", realF.length() > 0);

		// remove file again
		if(realF.exists()) {
			realF.delete();
		}
	}

}
