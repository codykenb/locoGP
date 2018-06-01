package de.uka.ipd.sdq.ByCounter.execution;

public interface ISimpleCountingResult {
	final int NO_COUNT_AVAILABLE = -1;
	
	/**
	 * @param methodName Fully qualifying method name (signature) including 
	 * parameter and return types in bytecode notation. For example:
	 * <code>java.lang.Math.abs(J)J</code>
	 * @return Method execution count for the given method. When no count was 
	 * recorded, {@link #NO_COUNT_AVAILABLE} ({@value #NO_COUNT_AVAILABLE}) will 
	 * be returned.
	 */
	public Long getMethodCount(String methodName);
	
	/**
	 * TODO
	 * @return Opcode execution count
	 */
	public Long getOpcodeCount(int opcode);
	
	/**
	 * TODO
	 * @return Opcode execution count
	 */
	public Long getOpcodeCount(String opcode);
	
	/**
	 * TODO
	 * @param includeInvokeOpcodes
	 * @return Total number of executed opcodes.
	 */
	public Long getTotalCount(boolean includeInvokeOpcodes);

	/**
	 * Set counted BCs for a opcode-specified BC
	 * @param opcode
	 * @param count
	 */
	public void setOpcodeCount(int opcode, Long count);

}
