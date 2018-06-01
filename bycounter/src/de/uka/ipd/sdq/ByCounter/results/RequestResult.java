package de.uka.ipd.sdq.ByCounter.results;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;


/**
 * Holds results produced by a single request.
 * @author Martin Krogmann
 * @see de.uka.ipd.sdq.ByCounter.results
 *
 */
public class RequestResult implements Comparable<RequestResult> {
	/**
	 * {@link UUID} of the request.
	 */
	private UUID requestId;
	/**
	 * CountingResults for this request.
	 */
	private SortedSet<CountingResult> countingResults;
	
	/**
	 * Construct the {@link RequestResult}.
	 */
	public RequestResult() {
		this.requestId = null;
		this.countingResults = new TreeSet<CountingResult>();
	}

	/**
	 * 
	 * @return {@link UUID} of the request.
	 */
	public UUID getRequestId() {
		return requestId;
	}

	/**
	 * 
	 * @param requestId {@link UUID} of the request.
	 */
	public void setRequestId(UUID requestId) {
		this.requestId = requestId;
	}

	/**
	 * 
	 * @return CountingResults for this request.
	 */
	public SortedSet<CountingResult> getCountingResults() {
		return countingResults;
	}

	/**
	 * 
	 * @param countingResults CountingResults for this request.
	 */
	public void setCountingResults(SortedSet<CountingResult> countingResults) {
		this.countingResults = countingResults;
	}

	@Override
	public int compareTo(RequestResult o) {
		return this.requestId.compareTo(o.requestId);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RequestResult [requestId=");
		builder.append(this.requestId);
		builder.append(", countingResults=");
		builder.append(this.countingResults);
		builder.append("]");
		return builder.toString();
	}
}
