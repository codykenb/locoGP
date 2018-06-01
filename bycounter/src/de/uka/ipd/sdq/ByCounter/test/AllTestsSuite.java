package de.uka.ipd.sdq.ByCounter.test;

//import evaluation.TestApplications;
//import evaluation.TestCBSE;
//import evaluation.TestCompress;
import de.uka.ipd.sdq.ByCounter.test.requestIDs.TestRequestIDs;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;



/**
 * Test suite that contains all tests for ByCounter.
 * This makes running all tests at once simpler.
 * NOTE: When some tests fail due to heap space issues, try to add 
 * the VM option "-Xmx192M". Also, the option "-Xss1M" for more stack 
 * size might be necessary for some tests.
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
public class AllTestsSuite {

	public static Test suite() {
		
		TestSuite suite = new TestSuite(
				"Test for de.uka.ipd.sdq.ByCounter.test");
		//$JUnit-BEGIN$
		
		// Manually added tests:
		suite.addTest(new JUnit4TestAdapter(TestASMBytecodes.class));
		suite.addTest(new JUnit4TestAdapter(TestBytecodeCounter.class));
		suite.addTest(new JUnit4TestAdapter(TestMethodDescriptor.class));
		suite.addTest(new JUnit4TestAdapter(TestResultWriters.class));
		suite.addTest(new JUnit4TestAdapter(TestLineNumbers.class));
		suite.addTest(new JUnit4TestAdapter(TestInternalClassDefinition.class));
		suite.addTest(new JUnit4TestAdapter(TestResultObservation.class));
		suite.addTest(new JUnit4TestAdapter(TestInstrumentationRegions.class));
		suite.addTest(new JUnit4TestAdapter(TestRequestIDs.class));
		suite.addTest(new JUnit4TestAdapter(TestQueryUpdates.class));
		suite.addTest(new JUnit4TestAdapter(TestThreads.class));
//		suite.addTest(new JUnit4TestAdapter(TestCBSE.class));
//		suite.addTest(new JUnit4TestAdapter(TestCompress.class));
//		suite.addTest(new JUnit4TestAdapter(TestApplications.class));
		//$JUnit-END$
		
		return suite;
	}

}
