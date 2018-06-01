package de.uka.ipd.sdq.ByCounter.execution;

import java.util.logging.Logger;

import de.uka.ipd.sdq.ByCounter.results.ResultCollection;

/**
 * Abstract class implementing {@link ICollectionStrategy} that keeps track
 * of the {@link CountingResultCollector} and logging.
 * @author Martin Krogmann
 *
 */
public abstract class AbstractCollectionStrategy implements ICollectionStrategy {

	/** The {@link CountingResultCollector} that uses this strategy. */
	protected CountingResultCollector parentResultCollector;
	
	/** The {@link ResultCollection} to fill. */
	protected ResultCollection currentResultCollection;

	/** see http://en.wikipedia.org/wiki/Data_log */
	protected Logger log;

	/**
	 * Construction of the strategy object.
	 * @param parent CountingResultCollector that makes use of this strategy.
	 */
	public AbstractCollectionStrategy(CountingResultCollector parent) {
		this.log = Logger.getLogger(this.getClass().getCanonicalName());
		this.parentResultCollector = parent;
	}
	
	@Override
	public void setResultCollection(ResultCollection resultCollection) {
		this.currentResultCollection = resultCollection;
	}

	/**
	 * Default implementation does nothing.
	 */
	@Override
	public void protocolFutureCount(ProtocolFutureCountStructure futureCount) {
	}
}