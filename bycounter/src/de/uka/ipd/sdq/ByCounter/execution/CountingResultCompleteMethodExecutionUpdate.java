package de.uka.ipd.sdq.ByCounter.execution;

import de.uka.ipd.sdq.ByCounter.results.ResultCollection;


/**
 * This class is used to update observers registered to 
 * {@link CountingResultCollector} when a complete method has been executed,
 * i.e. reached a return statement or a throw statement.
 * @author Martin Krogmann
 */
public class CountingResultCompleteMethodExecutionUpdate implements CountingResultUpdate {
	/**
	 * All results of the completely executed method.
	 */
	public ResultCollection methodResults;

	/**
	 * Construct the update.
	 * @param methodResults All results of the completely executed method.
	 */
	public CountingResultCompleteMethodExecutionUpdate(ResultCollection methodResults) {
		this.methodResults = methodResults;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CountingResultCompleteMethodExecutionUpdate [methodResults=");
		builder.append(this.methodResults);
		builder.append("]");
		return builder.toString();
	}
}