package de.uka.ipd.sdq.ByCounter.results;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A collection of individual request results and general counting results. 
 * If a {@link CountingResult} has a request id it is found in a 
 * {@link RequestResult} in 
 * {@link #getRequestResults()}, otherwise it is found in 
 * {@link #getCountingResults()}.
 * <p>
 * Counting results ({@link #getCountingResults()}) include instances of the 
 * base class {@link CountingResult} for results of methods, section or regions.
 * They also contain {@link ThreadedCountingResult}s. These have a specific 
 * thread id {@link ThreadedCountingResult#getThreadId()} and can contain 
 * more {@link ThreadedCountingResult}s from threads that were spawned in the 
 * element observed in the result 
 * ({@link ThreadedCountingResult#getSpawnedThreadedCountingResults()}), i.e. 
 * when the executed code contains {@link Thread#start()} calls.
 * </p>
 * <p>
 * Request results ({@link #getRequestResults()}) is a collection holding 
 * instances of {@link RequestResult} that are identified by their request id 
 * ({@link RequestResult#getRequestId()}). {@link RequestResult}s are containers 
 * for the standard types of {@link CountingResult}, accessible via 
 * {@link RequestResult#getCountingResults()}.
 * </p>
 * @author Martin Krogmann
 *
 */
public class ResultCollection {
	/**
	 * Results of requests, i.e. results with a specific request id.
	 * See {@link ResultCollection} for a destinction between
	 * {@link #getRequestResults()} and {@link #getCountingResults()}.
	 */
	private SortedSet<RequestResult> requestResults;
	
	/**
	 * {@link CountingResult} objects or objects of inheriting classes 
	 * (for instance {@link ThreadedCountingResult}).
	 * See {@link ResultCollection} for a distinction between
	 * {@link #getRequestResults()} and {@link #getCountingResults()}.
	 */
	private SortedSet<CountingResult> countingResults;
	
	/**
	 * Default constructor: initialises empty lists.
	 */
	public ResultCollection() {
		this.requestResults = new TreeSet<RequestResult>();
		this.countingResults = new TreeSet<CountingResult>();
	}

	/**
	 * @return Results of requests, i.e. results with a specific request id.
	 * See {@link ResultCollection} for a distinction between
	 * {@link #getRequestResults()} and {@link #getCountingResults()}.
	 */
	public SortedSet<RequestResult> getRequestResults() {
		return this.requestResults;
	}
	
	/**
	 * @return {@link CountingResult} objects or objects of inheriting classes 
	 * (for instance {@link ThreadedCountingResult}).
	 * See {@link ResultCollection} for a destinction between
	 * {@link #getRequestResults()} and {@link #getCountingResults()}.
	 */
	public SortedSet<CountingResult> getCountingResults() {
		return this.countingResults;
	}
	
	/**
	 * Adds another result to this result.
	 * @param lr {@link ResultCollection} to add.
	 */
	public void add(ResultCollection lr) {
		this.requestResults.addAll(lr.requestResults);
		this.countingResults.addAll(lr.countingResults);
	}
}
