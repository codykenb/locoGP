/**
 * This package contains result structures for ByCounter instrumentation runs.
 * <p>
 * Here is an overview of the classes used to represent results.
 * </p>
 * <p>
 * <img src="doc-files/ByCounter_OutputModelOverview.png" alt="ByCounter Output Model Overview" />
 * </p>
 * <p>
 * Results of ByCounter instrumentation runs are all retrieved using
 * {@link de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector}.
 * The method {@link de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector#retrieveAllCountingResults()}
 * returns a {@link de.uka.ipd.sdq.ByCounter.results.ResultCollection} which is 
 * the container class for all results.
 * </p>
 * <ul>
 * <li>
 * Results that are associated with a request are represented as 
 * {@link de.uka.ipd.sdq.ByCounter.results.RequestResult}s and are available via
 * {@link de.uka.ipd.sdq.ByCounter.results.ResultCollection#getRequestResults()}.
 * {@link de.uka.ipd.sdq.ByCounter.results.RequestResult}s contain regular results
 * but are grouped by their id ({@link de.uka.ipd.sdq.ByCounter.results.RequestResult#getRequestId()}).
 * </li>
 * <li>
 * Results that are not part of a request are avaiable via
 * {@link de.uka.ipd.sdq.ByCounter.results.ResultCollection#getCountingResults()}.
 * </li>
 * <li>
 * {@link de.uka.ipd.sdq.ByCounter.results.CountingResult}s contain all the 
 * information collected by executing code that was instrumented by ByCounter.
 * If the executed instrumented code is multithreaded, the results have the sub
 * type {@link de.uka.ipd.sdq.ByCounter.results.ThreadedCountingResult}. These 
 * results have a thread id and contain a list of spawned results 
 * ( {@link de.uka.ipd.sdq.ByCounter.results.ThreadedCountingResult#getSpawnedThreadedCountingResults()}).
 * These are results from threads that were spawned from the original thread.
 * </li>
 * </ul>
 */
package de.uka.ipd.sdq.ByCounter.results;