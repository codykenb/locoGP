package de.uka.ipd.sdq.ByCounter.execution;

import de.uka.ipd.sdq.ByCounter.results.ResultCollection;

/**
 * Interface used by {@link CountingResultCollector} to control different 
 * methods of result collection and storage.
 * @author Martin Krogmann
 */
public interface ICollectionStrategy {

	/** Clear the result storage fields. */
	public abstract void clearResults();

	/**
	 * Protocol the count to the collection strategy.
	 * @param result The result to protocol.
	 * @return True, if the result was accepted.
	 */
	public abstract boolean protocolCount(final ProtocolCountStructure result);
	
	/**
	 * @param resultCollection The {@link ResultCollection} that the 
	 * {@link ICollectionStrategy} 
	 * will fill with results.
	 */
	public void setResultCollection(final ResultCollection resultCollection);

	/**
	 * This is called before retrieving results to allow any late calculations 
	 * to take place.
	 */
	void prepareCountingResults();

	/**
	 * Preallocate a CountingResult in the result collector so that the 
	 * structure of ThreadedCountingResults can be correctly constructed.
	 * @param futureCount {@link ProtocolFutureCountStructure}.
	 */
	void protocolFutureCount(final ProtocolFutureCountStructure futureCount);
}
