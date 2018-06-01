package de.uka.ipd.sdq.ByCounter.execution;

import java.util.HashMap;
import java.util.SortedMap;
import java.util.UUID;

public interface IFullCountingResult extends ISimpleCountingResult{

	/** 
	 * Simple getter for method call counts.
	 * @return A {@link HashMap} were the method name is mapped to the number 
	 * of calls of that method.
	 */
	public SortedMap<String, Long> getMethodCallCounts();
	
	/**
	 * TODO
	 * @param methodName
	 * @return Method execution count.
	 */
	public Long getMethodCountByString(String methodName);

	/**
	 * TODO
	 * @return Beginning time of method invocation.
	 */
	public long getMethodInvocationBeginning();
	
	/**
	 * TODO
	 * @return Time of result reporting for the method.
	 */
	public long getReportingTime();
	
	/**
	 * TODO
	 * @return Opcode execution count
	 */
	public Long getOpcodeCountByInteger(int opcode);

	/**
	 * TODO
	 * @return Opcode execution count
	 */
	public Long getOpcodeCountByString(String opcode);

	/** 
	 * Simple getter for the opcode counts as a HashMap integers 
	 * where each bytecode instruction is the key for which 
	 * the value represents the number of calls to a specific instruction.
	 * @return The counts.
	 */
	public long[] getOpcodeCounts();

	/**
	 * Simple getter
	 * @return the qualifyingMethodName
	 */
	public String getQualifiedMethodName();
	
	/**
	 * 
	 * @return A {@link UUID} that is linked to a request. This is used to keep track 
	 * of execution sequences when dealing with parallel execution.
	 */
	public UUID getRequestID();

}