package de.uka.ipd.sdq.ByCounter.execution;

import java.util.List;

import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * This class holds method execution details, i.e. how 
 * (with which parameters etc.) was which method executed.
 */
public class MethodExecutionRecord {
	/**
	 * Canonical name of the executed class.
	 */
	public String canonicalClassName;
	
	/**
	 * Methods called in the executed class.
	 */
	public List<MethodDescriptor> methodsCalled;
	
	/**
	 * Execution parameters for the executed methods ({@link #methodsCalled}.
	 */
	public List<Object[]> methodCallParams;
	
	/** Settings of the execution */
	public ExecutionSettings executionSettings;
}
