package de.uka.ipd.sdq.ByCounter.results;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultBase;
import de.uka.ipd.sdq.ByCounter.instrumentation.EntityToInstrument;

/**
 * This class is a container for a result obtained by executing bytecode 
 * instrumented using {@link BytecodeCounter}. The element in the code that 
 * lead to the production of this result can be a specific method, a range of 
 * line numbers in a method, a region specified by start and stop points, etc. 
 * It is referenced in {@link #getObservedElement()}. 
 * <p>
 * The execution counts for specific bytecodes is available as 
 * {@link #getOpcodeCounts()}. Method invocations are saved in 
 * {@link #getMethodCallCounts()}.
 * Depending on the type of instrumentation, more information is available. 
 * Refer to the documentation on the get methods of this class for details. 
 * </p> 
 * @see CountingResultBase
 * @see de.uka.ipd.sdq.ByCounter.results
 * @author Martin Krogmann
 *
 */
public class CountingResult extends CountingResultBase implements Cloneable {

	/**
	 * Version for {@link Serializable} interface.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * When true, all fields in the result have complete values.
	 * When false, some values have not yet been set.
	 */
	private boolean isFinal;
	
	/**
	 * The parent {@link ResultCollection} that this {@link CountingResult} 
	 * is a part of.
	 */
	private ResultCollection resultCollection;
	
	/**
	 * A reference to the parent if this result has a parent {@link RequestResult}.
	 */
	private RequestResult requestResult;

	/**
	 * The specification of instrumentation that lead to the observation of 
	 * this result.
	 */
	private EntityToInstrument observedElement;

	/**
	 * A {@link SortedSet} of {@link CountingResult} from threads that
	 * where spawned from the thread of this result.
	 */
	protected SortedSet<CountingResult> spawnedThreadedCountingResults;

	/**
	 * The {@link CountingResult} that contains this 
	 * {@link CountingResult} as part of 
	 * {@link CountingResult#getSpawnedThreadedCountingResults()}.
	 */
	protected CountingResult threadedCountingResultSource;

	/**
	 * Set fields to null.
	 */
	public CountingResult() {
		this.observedElement = null;
		this.resultCollection = null;
		this.requestResult = null;
		this.isFinal = false;
		this.spawnedThreadedCountingResults = new TreeSet<CountingResult>();
		this.threadedCountingResultSource = null;
		this.threadId = -1;
	}
	
	/**
	 * Copy constructor.
	 * @param src Source to copy attributes from.
	 */
	public CountingResult(final CountingResult src) {
		this.set(src);
	}
	
	/**
	 * Constructs the result and initialises the fields with null.
	 * @param parent The parent {@link ResultCollection} that this {@link CountingResult} 
	 * is a part of.
	 */
	public CountingResult(final ResultCollection parent) {
		this();
		this.resultCollection = parent;
	}
	
	/**
	 * Constructs the result and initialises the fields with null.
	 * @param parent The parent {@link RequestResult} that this {@link CountingResult} 
	 * is a part of.
	 */
	public CountingResult(final RequestResult parent) {
		this();
		this.requestResult = parent;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public CountingResult clone(){
		CountingResult copy = null;

		copy = (CountingResult) super.clone();
		copy.setResultCollection(this.resultCollection);
		copy.setRequestResult(this.requestResult);
		copy.setObservedElement(this.observedElement);
		copy.setFinal(this.isFinal);
		copy.setThreadId(this.threadId);
		copy.setSpawnedThreadedCountingResults(
				new TreeSet<CountingResult>(this.spawnedThreadedCountingResults));
		copy.setThreadedCountingResultSource(this.threadedCountingResultSource);

		return copy;
	}
	
	/** {@inheritDoc} */
	@Override
	public void set(CountingResultBase src) {
		super.set(src);
		if(src instanceof CountingResult) {
			CountingResult crSrc = (CountingResult) src;
			this.observedElement = crSrc.observedElement;
			this.resultCollection = crSrc.resultCollection;
			this.requestResult = crSrc.requestResult;
			this.isFinal = crSrc.isFinal;
			this.spawnedThreadedCountingResults = new TreeSet<CountingResult>(crSrc.getSpawnedThreadedCountingResults());
			this.threadedCountingResultSource = crSrc.threadedCountingResultSource;
		} else {
			this.observedElement = null;
			this.resultCollection = null;
			this.requestResult = null;
			this.isFinal = false;
		}
	}

	/**
	 * @return The parent {@link ResultCollection} that this {@link CountingResult} 
	 * is a part of.
	 */
	public ResultCollection getResultCollection() {
		return resultCollection;
	}

	/**
	 * 
	 * @param resultCollection The parent {@link ResultCollection} that this {@link CountingResult} 
	 * is a part of.
	 */
	public void setResultCollection(ResultCollection resultCollection) {
		this.resultCollection = resultCollection;
	}

	/**
	 * @return A reference to the parent if this result has a parent {@link RequestResult}.
	 */
	public RequestResult getRequestResult() {
		return requestResult;
	}

	/**
	 * @param requestResult A reference to the parent if this result has a parent {@link RequestResult}.
	 */
	public void setRequestResult(RequestResult requestResult) {
		this.requestResult = requestResult;
	}

	/**
	 * @return The specification of instrumentation that lead to the observation of 
	 * this result.
	 */
	public EntityToInstrument getObservedElement() {
		return observedElement;
	}

	/**
	 * @param observedElement The specification of instrumentation that lead to the observation of 
	 * this result.
	 */
	public void setObservedElement(EntityToInstrument observedElement) {
		this.observedElement = observedElement;
	}
	
	/**
	 * @return True, when all fields in the result have complete values.
	 * False, when some values have not yet been set.
	 */
	public boolean getFinal() {
		return this.isFinal;
	}
	
	/**
	 * @param isFinal When true, all fields in the result have complete values.
	 * When false, some values have not yet been set.
	 */
	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	/* Don't consider spawned results in the hash code creation so that the 
	 * stack doesn't blow up. */
	/*
	 * (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.execution.CountingResultBase#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (this.isFinal ? 1231 : 1237);
		result = prime
				* result
				+ ((this.observedElement == null) ? 0 : this.observedElement
						.hashCode());
		result = prime
				* result
				+ ((this.requestResult == null) ? 0 : this.requestResult
						.hashCode());
		result = prime
				* result
				+ ((this.resultCollection == null) ? 0 : this.resultCollection
						.hashCode());
		result = prime
				* result
				+ ((this.threadedCountingResultSource == null) ? 0
						: this.threadedCountingResultSource.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CountingResult other = (CountingResult) obj;
		if (this.isFinal != other.isFinal)
			return false;
		if (this.observedElement == null) {
			if (other.observedElement != null)
				return false;
		} else if (!this.observedElement.equals(other.observedElement))
			return false;
		if (this.requestResult == null) {
			if (other.requestResult != null)
				return false;
		} else if (!this.requestResult.equals(other.requestResult))
			return false;
		if (this.resultCollection == null) {
			if (other.resultCollection != null)
				return false;
		} else if (!this.resultCollection.equals(other.resultCollection))
			return false;
		if (this.spawnedThreadedCountingResults == null) {
			if (other.spawnedThreadedCountingResults != null)
				return false;
		} else if (!this.spawnedThreadedCountingResults
				.equals(other.spawnedThreadedCountingResults))
			return false;
		if (this.threadedCountingResultSource == null) {
			if (other.threadedCountingResultSource != null)
				return false;
		} else if (!this.threadedCountingResultSource
				.equals(other.threadedCountingResultSource))
			return false;
		return true;
	}

	/**
	 * 
	 * @return Id of the thread from which the result was reported.
	 * @see Thread#getId()
	 */
	public long getThreadId() {
		return threadId;
	}

	/**
	 * 
	 * @param threadId Id of the thread from which the result was reported.
	 * @see Thread#getId()
	 */
	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	/**
	 * @return A {@link SortedSet} of {@link CountingResult} from threads that
	 * where spawned from the thread of this result.
	 */
	public SortedSet<CountingResult> getSpawnedThreadedCountingResults() {
		return spawnedThreadedCountingResults;
	}

	/**
	 * @param spawnedThreadedCountingResults A {@link SortedSet} of {@link CountingResult} from threads that
	 * where spawned from the thread of this result.
	 */
	public void setSpawnedThreadedCountingResults(SortedSet<CountingResult> spawnedThreadedCountingResults) {
		this.spawnedThreadedCountingResults = spawnedThreadedCountingResults;
	}

	/**
	 * @return The {@link CountingResult} that contains this 
	 * {@link CountingResult} as part of 
	 * {@link CountingResult#getSpawnedThreadedCountingResults()}.
	 */
	public CountingResult getThreadedCountingResultSource() {
		return threadedCountingResultSource;
	}

	/**
	 * @param threadedCountingResultSource The {@link CountingResult} that contains this 
	 * {@link CountingResult} as part of 
	 * {@link CountingResult#getSpawnedThreadedCountingResults()}.
	 */
	public void setThreadedCountingResultSource(
			CountingResult threadedCountingResultSource) {
		this.threadedCountingResultSource = threadedCountingResultSource;
	}
}
