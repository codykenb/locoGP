package de.uka.ipd.sdq.ByCounter.execution;

import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * The numerical results calculated from the reports of executed code that 
 * was instrumented by ByCounter. These only include counts for opcode 
 * executions and method calls.
 * @author Martin Krogmann
 *
 */
public class CalculatedCounts {
	public void init() {
		this.opcodeCounts = new long[CountingResultBase.MAX_OPCODE];
		methodCounts = new TreeMap<String, Long>();
		indexOfRangeBlock = -1;
	}
	
	long[] opcodeCounts;
	SortedMap<String, Long> methodCounts;
	public int indexOfRangeBlock;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CalculatedCounts[");
		sb.append("opcodeCounts: " + Arrays.toString(opcodeCounts));
		sb.append("; methodCounts: " + methodCounts);
		sb.append("; indexOfRangeBlock: " + indexOfRangeBlock);
		sb.append("]");
		return sb.toString();
	}
	
	/**
	 * Adds the counts opcodeCountsInBB to the counts.
	 * @param opcodeCountsInBB opcodeCounst in the basic block
	 * @param bbCount nr of executions of the basic block
	 */
	public void addOpcodeCounts( 
			int[] opcodeCountsInBB,
			long bbCount) {
		for(int opc = 0; opc < opcodeCountsInBB.length; opc++) {
			int opcodeCountInBB = opcodeCountsInBB[opc];
			long prevValue = this.opcodeCounts[opc];
			this.opcodeCounts[opc] = prevValue + bbCount*opcodeCountInBB;
		}
	}

	/**
	 * Adds the counts in methodCallCounts to the counts.
	 * @param methodCallCounts methodCallCounts in the basic block
	 * @param bbCount nr of executions of the basic block
	 */
	public void addMethodCallCounts(
			Map<String, Integer> methodCallCounts, 
			long bbCount) {
		for(String methodKey : methodCallCounts.keySet()) {
			int methodCountInBB = methodCallCounts.get(methodKey);
			if(methodCountInBB == 0) {
				continue;
			}
			Long prevValue = this.methodCounts.get(methodKey);
			if(prevValue == null) {
				prevValue = 0L;
			}
			this.methodCounts.put(methodKey, prevValue + bbCount*methodCountInBB);
		}
	}

}
