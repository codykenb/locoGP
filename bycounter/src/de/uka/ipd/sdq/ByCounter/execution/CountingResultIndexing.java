package de.uka.ipd.sdq.ByCounter.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import de.uka.ipd.sdq.ByCounter.results.CountingResult;


/**
 * Indexing infrastructure for {@link CountingResultBase}s.
 * @author Martin Krogmann
 * @author Michael Kuperberg
 *
 */
public class CountingResultIndexing {
	/** Logging for this class. */
	private Logger log;

	/**
	 * retrieve the full counting artefact information by the beginning time
	 */
	private HashMap<Long, List<CountingArtefactInformation>> countingInformationsByBeginning; //later, use SortedSet (after defining a comparator...)

	/**
	 * Retrieve all invocations of a method by its signature
	 */
	private HashMap<String, List<CountingArtefactInformation>> countingInformationsByMethodname; //later, use SortedSet (after defining a comparator...)

	/**
	 * Database replacement ;-)
	 * TODO does not consider forced inlining
	 */
	private HashMap<CountingArtefactInformation, CountingResult> countingResultsByArtefactInformation;
	
	/**
	 *	A {@link SortedSet} that holds the results.
	 */
	private SortedSet<CountingResult> countingResults;

	public CountingResultIndexing() {
		this.log = Logger.getLogger(getClass().getCanonicalName());
		this.countingResults = new TreeSet<CountingResult>();
		this.countingInformationsByBeginning = new HashMap<Long, List<CountingArtefactInformation>>();
		this.countingInformationsByMethodname = new HashMap<String, List<CountingArtefactInformation>>();//TODO consider removing this...
		this.countingResultsByArtefactInformation = new HashMap<CountingArtefactInformation, CountingResult>();
	}
	
	public void clearResults() {
		this.log.fine("Used to have "+this.countingResults.size()+" results before clearing");
		this.countingResults.clear();
		this.countingInformationsByBeginning.clear();
		this.countingInformationsByMethodname.clear();
		this.countingResultsByArtefactInformation.clear();
	}

	/**
	 * Add to indexing infrastructure.
	 * @param res
	 * @param reportingStart
	 */
	public void add(CountingResult res,
			long reportingStart) {

		this.countingResults.add(res);
		
		int nrOfCountingResults=this.countingResults.size();
		if(nrOfCountingResults%10000==0){
			log.warning(nrOfCountingResults+" results in ByCounter");
		}
		
		CountingArtefactInformation artefact;//does THIS create too much overhead? it requires ALL elements to be in memory?
		final String qualifyingMethodName = res.getQualifiedMethodName();
		final Long executionStart = res.getMethodInvocationBeginning();
		artefact = new CountingArtefactInformation(
				this,
				qualifyingMethodName,
				executionStart,
				null,//input parameters
				reportingStart,
				null //output parameters
				);

		List<CountingArtefactInformation> ciAtExecutionStart = 
				this.countingInformationsByBeginning.get(executionStart);
		if(ciAtExecutionStart == null) {
			ciAtExecutionStart = new LinkedList<CountingArtefactInformation>();
			this.countingInformationsByBeginning.put(executionStart, ciAtExecutionStart);
		}
		ciAtExecutionStart.add(artefact);
		
		Set<String> keys = this.countingInformationsByMethodname.keySet();
		if(keys.contains(qualifyingMethodName)){
			this.countingInformationsByMethodname.get(qualifyingMethodName).add(artefact);
		}else{
			List<CountingArtefactInformation> list = new ArrayList<CountingArtefactInformation>();
			list.add(artefact);
			this.countingInformationsByMethodname.put(qualifyingMethodName, list);
		}
		this.countingResultsByArtefactInformation.put(artefact, res);
	}

	/**
	 * Gets the mapping of {@link CountingArtefactInformation} to {@link CountingResultBase}s.
	 * @return The mapping as {@link HashMap}.
	 */
	public HashMap<CountingArtefactInformation, CountingResult> getAllCountingResultsByArtefacts() {
		log.warning("getAllCountingResultsByArtefacts disregards inlined and force-inlined methods, " +
				"use retrieveAllCountingResults instead");
		return this.countingResultsByArtefactInformation;
	}

	/**
	 * Gets the {@link CountingArtefactInformation} by the time of
	 * method execution beginning.
	 * TODO does not consider forced inlining?!
	 * @return A {@link HashMap}. The long value is the time as
	 * returned by System.nanoTime().
	 */
	public HashMap<Long, List<CountingArtefactInformation>> getCountingArtefactsByBeginning() {
		log.warning("getCountingArtefactsByBeginning disregards inlined and force-inlined methods, " +
			"use retrieveAllCountingResults instead");
		return this.countingInformationsByBeginning;
	}

	/**
	 * Gets the {@link CountingArtefactInformation} by method name.
	 * TODO does not consider forced inlining?
	 * @return A {@link HashMap}. The {@link String} is the method name.
	 */
	public HashMap<String, List<CountingArtefactInformation>> getCountingArtefactsByMethodname() {
		log.warning("getCountingArtefactsByMethodname disregards inlined and force-inlined methods, " +
			"use retrieveAllCountingResults instead");
		return this.countingInformationsByMethodname;
	}

	/**
	 * Gets the {@link CountingArtefactInformation} by method name.
	 * TODO does not consider forced inlining?
	 * @param name The method name used to select the
	 * {@link CountingArtefactInformation} that is returned.
	 * @return The specified list of {@link CountingArtefactInformation}.
	 */
	public List<CountingArtefactInformation> getCountingArtefactsByName(String name){
		log.warning("getCountingArtefactsByName disregards inlined and force-inlined methods, " +
			"use retrieveAllCountingResults instead");
		return this.countingInformationsByMethodname.get(name);
	}

	/**
	 * Gets the {@link CountingArtefactInformation} by the time of
	 * method execution beginning.
	 * TODO does not consider forced inlining?
	 * @param time A time as returned by System.nanoTime().
	 * @return The specified {@link CountingResult}.
	 */
	public synchronized List<CountingResult> retrieveCountingResultByMethodStartTime(long time){
		List<CountingArtefactInformation> cai = this.countingInformationsByBeginning.get(time);
		if(cai==null){
			this.log.severe("No counting artefact information for starting time "+time);
			return null;
		}
		List<CountingResult> results = new LinkedList<CountingResult>();
		for(CountingArtefactInformation c : cai) {
			results.add(this.countingResultsByArtefactInformation.get(c));
		}
		return results;
	}

	/**
	 * Gets a {@link CountingResultBase} that is the accumulation of all reported 
	 * results of the calling tree specified by the callerStartTime. A method
	 * that reported a result at callerStartTime is the root node of the calling 
	 * tree. All methods invoked from that method, and also reported results, 
	 * are accumulated for the returned result.
	 *
	 * @param callerStartTime Start time of the method invocation for the method 
	 * that is the root node of the calling tree.
	 * @param suppressDebugMessages Debug messages require additional 
	 * computations. When true, these calculations will be stopped.
	 * @return The calculated {@link CountingResultBase}.
	 */
	public synchronized CountingResult retrieveCountingResultByStartTime_evaluateCallingTree(
			long callerStartTime,
			boolean suppressDebugMessages){
		this.log.info("Evaluating calling tree for method start time "+callerStartTime);
		CountingResult candidateCountingResult;		// The currently considered counting result
		long candidateStartTime;					// the start time of the considered result
		long candidateReportingTime;				// the reporting time of the considered result

		List<CountingArtefactInformation> countingInformationAtCallerStartTime = 
				this.countingInformationsByBeginning.get(callerStartTime);
		long callerReportingTime = 0;
		if(countingInformationAtCallerStartTime != null && !countingInformationAtCallerStartTime.isEmpty()) {
			callerReportingTime = countingInformationAtCallerStartTime.get(0).getResultsReceivedByCollectorTime();
		}
		this.log.fine("Corresponding caller reporting time: "+callerReportingTime);

		Set<Long> allKeys = this.countingInformationsByBeginning.keySet();
		// create a list of results sorted by start time
		ArrayList<Long> keysCopy = new ArrayList<Long>(allKeys);
		Collections.sort(keysCopy);
		// skip all result of methods executed before callerStartTime
		Iterator<Long> iter = keysCopy.iterator();
		candidateStartTime = iter.next();
		List<CountingResult> countingResultAtStartTime = this.retrieveCountingResultByMethodStartTime(callerStartTime);
		CountingResult totalCountingResult = new CountingResult();
		if(countingResultAtStartTime != null && !countingResultAtStartTime.isEmpty()) {
			totalCountingResult = (CountingResult) countingResultAtStartTime.get(0).clone();
		}
		this.log.fine("Counting result before Type2 addition: "+totalCountingResult);

		while(candidateStartTime<callerStartTime){//ECHT kleiner!
			if(!suppressDebugMessages) this.log.fine("Ignoring time "+candidateStartTime+" because <"+callerStartTime);
			candidateStartTime=iter.next();
		}

		boolean firstIteration = true;
		// now add the results of the methods executed before the current
		// result was reported.
		do {
			if(!firstIteration) {
				candidateStartTime = iter.next();
			}
			firstIteration = false;
			for(CountingArtefactInformation canditateCAI : this.countingInformationsByBeginning.get(candidateStartTime)) {	// corresponding CAI
				candidateReportingTime = canditateCAI.getResultsReceivedByCollectorTime();
				if(!suppressDebugMessages) this.log.fine("Considering for addition: "+canditateCAI+"");
				if(!suppressDebugMessages){
					this.log.fine("Just for the record: trying to add " +
						"["+candidateStartTime+","+candidateReportingTime+"] to " +
						"["+callerStartTime+","+callerReportingTime+"].");
				}
				// candidate results were reported before the caller was
				// assume that the caller has called the candidate and add it
				if(candidateReportingTime < callerReportingTime) {
					if(!suppressDebugMessages){
						this.log.fine("Adding callee counts of time "+candidateStartTime+
							" because its start >"+callerStartTime+
							" and because its reporting time " +
							"("+canditateCAI.getResultsReceivedByCollectorTime()+")< " +
							"caller reporting time ("+callerReportingTime+").");
					}
					candidateCountingResult = canditateCAI.getCountingResult();
					if(!suppressDebugMessages) this.log.fine("Added counting result: "+candidateCountingResult);
					totalCountingResult.add(candidateCountingResult);
					if(!suppressDebugMessages) this.log.fine("Intermediate total counting result: "+totalCountingResult);
				} else if(candidateReportingTime > callerReportingTime) {
					if(!suppressDebugMessages) this.log.fine("Skipping callee counts of time "+candidateStartTime+
							" because, while its start time >"+callerStartTime+
							", its reporting time " +
							"("+canditateCAI.getResultsReceivedByCollectorTime()+")> " +
							"caller reporting time ("+callerReportingTime+").");
				} else if(candidateReportingTime == callerReportingTime) {
					if(candidateStartTime == callerStartTime){
						if(!suppressDebugMessages) this.log.fine("Potential callee is the caller herself -> skipping");
					} else{
						if(!suppressDebugMessages) this.log.fine("A real callee that ends at the same instant " +
								"that the caller --> SKIPPING");
					}
				}
			}
		} while (candidateStartTime<callerReportingTime && iter.hasNext());
		if(!suppressDebugMessages) this.log.fine("Finished the active part");

		if(!suppressDebugMessages) {
			while(iter.hasNext()) {
				this.log.fine("Skipping callers with time "+iter.next());
			}
		}
		return totalCountingResult;
	}

	/**
	 * @param lastMethodExecutionDetails {@link MethodExecutionRecord} with 
	 * information on internal classes. 
	 * @return Results added up recursively.
	 */
	public List<CountingResult> retrieveRecursiveSum(final MethodExecutionRecord lastMethodExecutionDetails) {
		// calculate the sums for all results
		long callerStartTime;
		long callerReportTime;
		long prevCallerReportTime = Long.MIN_VALUE;
		List<CountingResult> ret = new LinkedList<CountingResult>();
		for(CountingResultBase cr : this.countingResults) {
			// countingResults are ordered by callerStartTime!
			callerStartTime = cr.getMethodInvocationBeginning();
			callerReportTime = cr.getReportingTime();
			if(prevCallerReportTime > callerReportTime) {
				// do not return results that have been added up into a previous result already
				continue;
			}
			CountingResult crSum = this.retrieveCountingResultByStartTime_evaluateCallingTree(callerStartTime, true);
			if(lastMethodExecutionDetails == null
					|| lastMethodExecutionDetails.executionSettings.isInternalClass(
					crSum.getQualifiedMethodName())) {
				removeInternalCalls(lastMethodExecutionDetails, crSum);
				// add the calculated recursive result to the list of results
				ret.add(crSum);
				prevCallerReportTime = callerReportTime;
			}
		}
		
		return ret;
	}

	/**
	 * Remove listing of calls to methods that are defined as internal 
	 * (the invoke* opcodes are still counted!). 
	 * @param lastMethodExecutionDetails {@link MethodExecutionRecord} used to
	 * find internal class definitions.
	 * @param countingResult Result that is modified to exclude internal calls.
	 */
	protected static void removeInternalCalls(
			final MethodExecutionRecord lastMethodExecutionDetails,
			CountingResult countingResult) {
		List<String> methodCallsToRemove = new LinkedList<String>();
		for(String methodCall : countingResult.getMethodCallCounts().keySet()) {
			if(lastMethodExecutionDetails.executionSettings.isInternalClass(methodCall)) {
				// found internal call
				methodCallsToRemove.add(methodCall);
			}
		}
		for(String methodCall : methodCallsToRemove) {
			// remove call
			countingResult.getMethodCallCounts().remove(methodCall);
		}
	}
}
