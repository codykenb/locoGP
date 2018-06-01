package de.uka.ipd.sdq.ByCounter.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;

import de.uka.ipd.sdq.ByCounter.results.CountingResult;

/**
 * Indexing for the thread hierarchy of counting results.
 * @author Martin Krogmann
 */
public class CountingResultThreadIndexing {
	/**
	 * Class used to correctly hash incomplete results.
	 * @author Martin Krogmann
	 *
	 */
	private static class ResultHash {
		private UUID ownID;
		private UUID observedID;

		public ResultHash(CountingResult r) {
			this.ownID = r.getMethodExecutionID();
			this.observedID = r.getObservedElement().getId();
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime
					* result
					+ ((this.observedID == null) ? 0 : this.observedID
							.hashCode());
			result = prime * result
					+ ((this.ownID == null) ? 0 : this.ownID.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ResultHash other = (ResultHash) obj;
			if (this.observedID == null) {
				if (other.observedID != null)
					return false;
			} else if (!this.observedID.equals(other.observedID))
				return false;
			if (this.ownID == null) {
				if (other.ownID != null)
					return false;
			} else if (!this.ownID.equals(other.ownID))
				return false;
			return true;
		}
		
	}
	
	/**
	 * For each {@link CountingResult} a list of thread ids for spawned threads.
	 */
	Map<CountingResult, ArrayList<Long>> resultsSpawnedThreads;
	
	/**
	 * For a thread <i>t</i> with the key == id, this map contains the counting result for 
	 * the thread that spawned <i>t</i> if such a result exists.
	 */
	Map<Long, CountingResult> spawningThreadForThreadId;
	
	/**
	 * For threads <i>t</i> with key == t.id, this map contains the entry
	 * (key, list_of_ts) if the thread could not be linked to a spawning thread.
	 */
	Map<Long, List<CountingResult>> rootThreadsByThreadId;
	
	/**
	 * All incomplete results reported when spawning threads are gathered here.
	 */
	Map<ResultHash, CountingResult> incompleteResults;
	
	/**
	 * Construct the empty infrastructure.
	 */
	public CountingResultThreadIndexing() {
		this.resultsSpawnedThreads = new HashMap<CountingResult, ArrayList<Long>>();
		this.spawningThreadForThreadId = new HashMap<Long, CountingResult>();
		this.rootThreadsByThreadId = new HashMap<Long, List<CountingResult>>();
		this.incompleteResults = new HashMap<ResultHash, CountingResult>();
	}

	/**
	 * @param res Partial counting result.
	 * @param spawnedThreadsIds Threads spawned by the result elements thread.
	 */
	public CountingResult apply(final CountingResult res, final ArrayList<Long> spawnedThreadsIds) {
		CountingResult result = res;
		boolean existingResultStubFound = false;
		
		// use existing stubs if possible to ensure object equality
		CountingResult resultStub = this.incompleteResults.get(new ResultHash(result));
		if(resultStub != null) {
			existingResultStubFound = true;
			result = resultStub;
			if(res.getFinal() == true) {
				// copy from complete result
				SortedSet<CountingResult> savedSpawns = resultStub.getSpawnedThreadedCountingResults();
				CountingResult savedSource = resultStub.getThreadedCountingResultSource();
				resultStub.set(res);	// use res instead of result because result == resultStaub!
				resultStub.setSpawnedThreadedCountingResults(savedSpawns);
				resultStub.setThreadedCountingResultSource(savedSource);
			}
		} else if(res.getFinal() == false) {
			this.incompleteResults.put(new ResultHash(result), result);
		}

		// handle child threads
		if(spawnedThreadsIds != null && !spawnedThreadsIds.isEmpty()) {
			final ArrayList<Long> threadSpawns = interpretSpawnedList(spawnedThreadsIds,
					result);
			// for each spawned thread id, save this result as the spawning thread
			for(final long id : threadSpawns) {
				if(!this.spawningThreadForThreadId.containsKey(id)) {
					this.spawningThreadForThreadId.put(id, result);
				}
			}
		}

		// handle parent threads
		CountingResult spawningThread = spawningThreadForThreadId.get(result.getThreadId());
		if(spawningThread != null) {
			result.setThreadedCountingResultSource(spawningThread);
			spawningThread.getSpawnedThreadedCountingResults().add(result);
		} else if(!existingResultStubFound) {
			// add result to root nodes; maybe a parent is found later
			long threadId = result.getThreadId();
			List<CountingResult> resultList = this.rootThreadsByThreadId.get(threadId);
			if(resultList == null) {
				resultList = new LinkedList<CountingResult>();
				this.rootThreadsByThreadId.put(threadId, resultList);
			}
			resultList.add(result);
		}
		return result;
	}

	/**
	 * Since the list of spawned threads can also contain information on the 
	 * source section, this method correctly interprets the available 
	 * information.
	 * @param spawnedThreadsIds List with thread spawning information.
	 * @param result The relevant counting result.
	 * @return List that only contains thread ids.
	 */
	private ArrayList<Long> interpretSpawnedList(
			final ArrayList<Long> spawnedThreadsIds,
			final CountingResult result) {
		final int indexOfRangeBlock = result.getIndexOfRangeBlock();
		final ArrayList<Long> threadSpawns = new ArrayList<Long>();
		if(indexOfRangeBlock >= 0) {
			// using range blocks
			// the list contains threadId, srcSectionNumber, ... (alternating)
			for(int i = 0; i < spawnedThreadsIds.size()/2; i++) {
				long threadId = spawnedThreadsIds.get(2*i);
				long srcSectionNumber = spawnedThreadsIds.get(2*i+1);
				if(result.getIndexOfRangeBlock() == srcSectionNumber) {
					threadSpawns.add(threadId);
				}
			}
		} else {
			// not using sections
			// only threadIds in the list
			for(long threadId : spawnedThreadsIds) {
				threadSpawns.add(threadId);
			}
		}
		return threadSpawns;
	}
	
	/**
	 * Remove all results from the indexing infrastructure.
	 */
	public void clearResults() {
		this.resultsSpawnedThreads.clear();
		this.spawningThreadForThreadId.clear();
		this.rootThreadsByThreadId.clear();
		this.incompleteResults.clear();
	}
}
