package de.uka.ipd.sdq.ByCounter.utils;

import de.uka.ipd.sdq.ByCounter.execution.MethodInvocationHelper;

/**
 * Results from the execution of a method, i.e. using 
 * {@link MethodInvocationHelper}.
 *
 */
public class InvocationResultData {
	
	/**
	 * Duration of execution (wall clock time) in ns. 
	 * <code>-1</code> if nothing was executed.
	 */
	public long duration;
	
	/**
	 * Return value of the invoked operation.
	 * <code>null</code> if nothing was executed or no value was returned.
	 */
	public Object returnValue;


	/**
	 * Construct the structure with default values.
	 * @see #duration
	 * @see #returnValue
	 */
	public InvocationResultData() {
		this.duration = -1;
		this.returnValue = null;
	}
}
