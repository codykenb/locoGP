package de.uka.ipd.sdq.ByCounter.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import de.uka.ipd.sdq.ByCounter.instrumentation.BlockCountingMode;
import de.uka.ipd.sdq.ByCounter.instrumentation.EntityToInstrument;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;

/**
 * A container used for all information that is reported by instrumented 
 * methods to the {@link CountingResultCollector}.
 * @author Martin Krogmann
 * 
 * <p>
 * Changing or adding fields requires 
 * additional changes in ByCounter.
 * 1) In insertResultCollectorCall, make sure that the stack is filled
 * correctly before the visitMethodInsn calls to match the changed signature.
 * 2) The directResultWriting method that is inserted into instrumented
 * classes if chosen may need to be adapted as it mostly shares the parameter
 * list of the protocol methods. For this, adapt the methods template in
 * MethodCountClassAdapter and follow the instructions in the javadoc of the
 * template that instruct you to change the asm code that is generated from
 * the template.
 * </p>
 *
 */
public class ProtocolCountStructure {
	/** Signature of constructor.*/
	public static final String SIGNATURE_CONSTRUCTOR_INT = "(JLjava/lang/String;[I[I[Ljava/lang/String;[ILjava/util/UUID;Ljava/util/UUID;Ljava/util/UUID;ZI)V";
	/** Signature of constructor.*/
	public static final String SIGNATURE_CONSTRUCTOR_LONG = "(JLjava/lang/String;[J[J[Ljava/lang/String;[JLjava/util/UUID;Ljava/util/UUID;Ljava/util/UUID;ZI)V";
	/**
	 * Use integer variables as counters.
	 */
	public static final boolean COUNTER_PRECISION_INT = false;
	/**
	 * Use long variables as counters.
	 */
	public static final boolean COUNTER_PRECISION_LONG = true;
	/** Time at which the reporting method started the execution */
	public long executionStart;
	/** Fully qualified method name. */
	public String qualifyingMethodName;
	/** 
	 * Counter precision
	 * @see InstrumentationParameters#getCounterPrecision()
	 */
	public boolean counterPrecisionLong;
	/**
	 * An array of integers where each bytecode instruction is
	 * the index for which the value represents the number of calls to a
	 * specific instruction.
	 */
	public int[] opcodeCountsInt;
	/** @see #opcodeCountsInt */
	public long[] opcodeCounts;
	/**
	 * An array of integers where each element represents
	 * the number of times, the method was called.
	 * The index is the same as for calledMethods.
	 */
	public int[] methodCallCountsInt;
	/** @see #methodCallCountsInt */
	public long[] methodCallCounts;
	/**
	 * An array of strings where each element is a
	 * method signature.
	 * The index is the same as for methodCallCounts.
	 */
	public String[] calledMethods;
	/**
	 * The counts for the specific *newarray call.
	 * The index is the same as for newArrayTypeOrDim and newArrayDesc.
	 */
	public int[] newArrayCountsInt;
	/** @see #newArrayCountsInt */
	public long[] newArrayCounts;
	/** The time at which this result arrives in the result collector. */
	public long reportingStart;
	/**
	 * This is a unique identifier that allows to group
	 * CountingResults that result from a common request.
	 */
	public UUID requestID;
	/** This ID is a reference passed to the methods called by the method. */
	public UUID ownID;
	/** This ID is a reference to the calling method. */
	public UUID callerID;
	/** This ID is the ID of the {@link EntityToInstrument} that produced this 
	 * result. */
	public UUID observedEntityID;
	/** When execution order recording is enabled, this is a list of integers
	 * in which each item represents the execution of a block with the index of 
	 * that number. */
	public ArrayList<Integer> blockExecutionSequence;
	/** When execution order recording is enabled, this is a list of integers
	 * in which each item represents the execution of a range block with the 
	 * index of that number. */
	public ArrayList<Integer> rangeBlockExecutionSequence;
	/**
	 * Mode in which the instructions have been grouped for counting.
	 */
	public BlockCountingMode blockCountingMode;
	/** Inlining of the method was specified when true. */
	public boolean inliningSpecified;
	/** Threads (by id) spawned in the executed method. 
	 * If range blocks are used, every second value is the number of the 
	 * range block from which the thread was spawned. */
	public ArrayList<Long> spawnedThreads;
	
	/**
	 * Constructor that only nulls all fields.
	 */
	public ProtocolCountStructure() {
		this.executionStart = 0L;
		this.qualifyingMethodName = null;
		this.opcodeCountsInt = null;
		this.opcodeCounts = null;
		this.methodCallCountsInt = null;
		this.methodCallCounts = null;
		this.calledMethods = null;
		this.newArrayCountsInt = null;
		this.newArrayCounts = null;
		this.reportingStart = 0L;
		this.requestID = null;
		this.ownID = null;
		this.callerID = null;
		this.observedEntityID = null;
		this.blockCountingMode = null;
		this.counterPrecisionLong = false;
		this.inliningSpecified = false;
		this.spawnedThreads = null;
	}
	
	/**
	 * Constructor for integer typed counters.
	 * @param executionStart {@link #executionStart}
	 * @param qualifyingMethodName {@link #qualifyingMethodName}
	 * @param opcodeCounts {@link #opcodeCountsInt}
	 * @param methodCallCounts {@link #methodCallCountsInt}
	 * @param calledMethods {@link #calledMethods}
	 * @param newArrayCounts {@link #newArrayCountsInt}
	 * @param requestID {@link #requestID}
	 * @param ownID {@link #ownID}
	 * @param callerID {@link #callerID}
	 * @param inliningSpecified {@link #inliningSpecified}
	 * @param blockCountingMode {@link #blockCountingMode}
	 */
	public ProtocolCountStructure(
			final long executionStart,
			final String qualifyingMethodName,
			final int[] opcodeCounts,
			final int[] methodCallCounts,
			final String[] calledMethods,
			final int[] newArrayCounts,
			final UUID requestID,
			final UUID ownID,
			final UUID callerID,
			final boolean inliningSpecified,
			final int blockCountingMode) {
		this.executionStart = executionStart;
		this.qualifyingMethodName = qualifyingMethodName;
		this.opcodeCountsInt = opcodeCounts;
		this.methodCallCountsInt = methodCallCounts;
		this.calledMethods = calledMethods;
		this.newArrayCountsInt = newArrayCounts;
		this.requestID = requestID;
		this.ownID = ownID;
		this.callerID = callerID;
		this.inliningSpecified = inliningSpecified;
		this.counterPrecisionLong = COUNTER_PRECISION_INT;
		this.blockCountingMode = BlockCountingMode.values[blockCountingMode];
		
		this.convertIntToLong();
	}
	

	/**
	 * Constructor for long typed counters.
	 * @param executionStart {@link #executionStart}
	 * @param qualifyingMethodName {@link #qualifyingMethodName}
	 * @param opcodeCounts {@link #opcodeCountsInt}
	 * @param methodCallCounts {@link #methodCallCountsInt}
	 * @param calledMethods {@link #calledMethods}
	 * @param newArrayCounts {@link #newArrayCountsInt}
	 * @param requestID {@link #requestID}
	 * @param ownID {@link #ownID}
	 * @param callerID {@link #callerID}
	 * @param inliningSpecified {@link #inliningSpecified}
	 * @param blockCountingMode {@link #blockCountingMode} 
	 */
	public ProtocolCountStructure(
			final long executionStart,
			final String qualifyingMethodName,
			final long[] opcodeCounts,
			final long[] methodCallCounts,
			final String[] calledMethods,
			final long[] newArrayCounts,
			final UUID requestID,
			final UUID ownID,
			final UUID callerID, 
			final boolean inliningSpecified,
			final int blockCountingMode) {
		this.executionStart = executionStart;
		this.qualifyingMethodName = qualifyingMethodName;
		this.opcodeCounts = opcodeCounts;
		this.methodCallCounts = methodCallCounts;
		this.calledMethods = calledMethods;
		this.newArrayCounts = newArrayCounts;
		this.requestID = requestID;
		this.ownID = ownID;
		this.callerID = callerID;
		this.inliningSpecified = inliningSpecified;
		this.counterPrecisionLong = COUNTER_PRECISION_LONG;
		this.blockCountingMode = BlockCountingMode.values[blockCountingMode];
	}
	
	/**
	 * Converts {@link #opcodeCountsInt}, {@link #methodCallCountsInt} and
	 * {@link #newArrayCountsInt} to the respective long variants and nulls the 
	 * *Int fields.
	 */
	private void convertIntToLong() {
		if(opcodeCountsInt != null) {
			this.opcodeCounts = convertIntArrayToLongArray(this.opcodeCountsInt);
			this.opcodeCountsInt = null;
		}
		
		if(methodCallCountsInt != null) {
			this.methodCallCounts = convertIntArrayToLongArray(this.methodCallCountsInt);
			this.methodCallCountsInt = null;
		}
		
		if(newArrayCountsInt != null) {
			this.newArrayCounts = convertIntArrayToLongArray(this.newArrayCountsInt);
			this.newArrayCountsInt = null;
		}
		
	}
	

	/**
	 * Copies an int array into a new long array.
	 * @param array Array to copy.
	 * @return All int values from array in a new long array.
	 */
	private static final long[] convertIntArrayToLongArray(int[] array) {
		if(array == null) return null;
		long[] result = new long[array.length];
		for(int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProtocolCountStructure [executionStart=");
		builder.append(this.executionStart);
		builder.append(", qualifyingMethodName=");
		builder.append(this.qualifyingMethodName);
		builder.append(", blockCountingMode=");
		builder.append(this.blockCountingMode);
		builder.append(", opcodeCountsInt=");
		builder.append(Arrays.toString(this.opcodeCountsInt));
		builder.append(", opcodeCounts=");
		builder.append(Arrays.toString(this.opcodeCounts));
		builder.append(", methodCallCountsInt=");
		builder.append(Arrays.toString(this.methodCallCountsInt));
		builder.append(", methodCallCounts=");
		builder.append(Arrays.toString(this.methodCallCounts));
		builder.append(", calledMethods=");
		builder.append(Arrays.toString(this.calledMethods));
		builder.append(", newArrayCountsInt=");
		builder.append(Arrays.toString(this.newArrayCountsInt));
		builder.append(", newArrayCounts=");
		builder.append(Arrays.toString(this.newArrayCounts));
		builder.append(", requestID=");
		builder.append(this.requestID);
		builder.append(", ownID=");
		builder.append(this.ownID);
		builder.append(", callerID=");
		builder.append(this.callerID);
		builder.append(", blockExecutionSequence=");
		builder.append(this.blockExecutionSequence);
		builder.append(", rangeBlockExecutionSequence=");
		builder.append(this.rangeBlockExecutionSequence);
		builder.append(", spawnedThreads=");
		builder.append(this.spawnedThreads);
		builder.append(", observedEntityID=");
		builder.append(this.observedEntityID);
		builder.append("]");
		return builder.toString();
	}
	
	
}
