package de.uka.ipd.sdq.ByCounter.test.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.SortedSet;

import junit.framework.Assert;
import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector;
import de.uka.ipd.sdq.ByCounter.execution.IFullCountingResult;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedMethod;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * A collection of tool functions that are used by more than one unit test. 
* @author Martin Krogmann
* @author Michael Kuperberg
* @since 0.9
* @version 0.9
*/
public class Utils {

	/**
	 * Gets the results from CountingResultCollector, assures (asserts) 
	 * that there is only one and then returns that. 
	 */
	public static CountingResult getAssertedResult() {
		SortedSet<CountingResult> r = CountingResultCollector.getInstance().retrieveAllCountingResults().getCountingResults();
//		Assert.assertEquals(1, r.length);
		return r.first();
	}

	/**
	 * Instruments the test method with name methodToInstrument, runs it and returns the
	 * opcode counts.
	 * @param c BytecodeCounter to use for counting.
	 * @param methodToInstrument The method to instrument.
	 * @return The de.uka.ipd.sdq.ByCount result.
	 */
	public static CountingResult getCountingResultForTest(BytecodeCounter c, 
			MethodDescriptor methodToInstrument) {
		return getCountingResultForTest(c, methodToInstrument, methodToInstrument);
	}

	/**
	 * Instruments the test method with name methodToInstrument, runs the
	 * method with the name methodToExecute and returns the
	 * opcode counts.
	 * @param c BytecodeCounter to use for counting.
	 * @param methodToInstrument The method to instrument.
	 * @param methodToExecute The method to execute. 
	 * @return The de.uka.ipd.sdq.ByCount result.
	 */
	public static CountingResult getCountingResultForTest(BytecodeCounter c, 
			MethodDescriptor methodToInstrument,
			MethodDescriptor methodToExecute) {
		c.getInstrumentationParams().getEntitiesToInstrument().add(new InstrumentedMethod(methodToInstrument));
		c.instrument();
		c.execute(methodToExecute, new Object[0]);
	
		CountingResult r = getAssertedResult();
		//resultCollector.logResult(r);
		return r;
	}

	/**
	 * Gets the count for the given opcode in the given result.
	 * Has asserts for the result not being null.
	 * @param r Result containing opcode counts.
	 * @param opcode Opcode to get the count for.
	 * @return Count for the given opcode as defined in the result.
	 */
	public static long getOpcCount(IFullCountingResult r, int opcode) {
		Assert.assertNotNull(r);
		Assert.assertNotNull(r.getOpcodeCounts());
		Assert.assertNotNull(r.getOpcodeCounts()[opcode]);
		
		return r.getOpcodeCounts()[opcode];
	}

	/**
	 * Writes the given byte[] to the given file
	 * @param outputFile The {@link File} to write the byte[] to.
	 * @param outputClass The byte[] to write.
	 * @throws IOException Thrown when no file outputFile exists and no new 
	 * file can be created. Also thrown if writing to the file fails.
	 */
	public static void writeBytesToFile(
			File outputFile, byte[] outputClass) throws IOException {
		if(!outputFile.exists()) {
			outputFile.createNewFile();
		}
		FileOutputStream os = new FileOutputStream(outputFile);
		os.write(outputClass);
		os.close();
	}

}
