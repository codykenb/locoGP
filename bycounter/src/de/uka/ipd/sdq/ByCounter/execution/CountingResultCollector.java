package de.uka.ipd.sdq.ByCounter.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uka.ipd.sdq.ByCounter.instrumentation.EntityToInstrument;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationContext;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;
import de.uka.ipd.sdq.ByCounter.reporting.ICountingResultWriter;
import de.uka.ipd.sdq.ByCounter.results.ResultCollection;

/**
 * Class used to collect statistics about an instrumented method.
 * <p>
 * This class is observable ({@link #addObserver(java.util.Observer)}) and can 
 * provide online updates on the collection of results. Updates consist of 
 * update objects with types implementing the {@link CountingResultUpdate} interface.
 * The following update 
 * types are currently available:
 * <ul>
 * <li>{@link CountingResultSectionExecutionUpdate}</li>
 * <li>{@link CountingResultCompleteMethodExecutionUpdate}</li>
 * </ul>
 * </p>
 * TODO implement an "adaptation-oriented inlining", where after a certain (threshold) number of invocations, a method is inlined (callees independently, too)
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
public final class CountingResultCollector extends Observable {
	
	/** Default value for {@link #getMode()}. */
	public static final CountingResultCollectorMode MODE_DEFAULT = CountingResultCollectorMode.UseReportingMethodChoiceByInstrumentedMethods;

	/**
	 * Think about the singleton pattern here :-)
	 */
	private static CountingResultCollector instance = null;

	/**
	 * The bytecode parameter descriptor for
	 * {@link #protocolCount(ProtocolCountStructure)}.
	 */
	public static final String SIGNATURE_protocolCount = "(Lde/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure;)Z";

	/**
	 * The bytecode parameter descriptor for 
	 * {@link #protocolActiveEntity(String)}.
	 */
	public static final String SIGNATURE_protocolActiveEntity = "(Ljava/lang/String;)V";

	/**
	 * The bytecode parameter descriptor for 
	 * {@link #protocolSpawnedThread(Thread)}.
	 */
	public static final String SIGNATURE_protocolSpawnedThread = "(Ljava/lang/Thread;)V";

	/**
	 * The bytecode parameter descriptor for 
	 * {@link #protocolFutureCount(ProtocolFutureCountStructure)}.
	 */
	public static final String SIGNATURE_protocolFutureCount = "(Lde/uka/ipd/sdq/ByCounter/execution/ProtocolFutureCountStructure;)V";

	/**
	 * Public singleton accessor. Use this to get a reference
	 * to the singleton instance.
	 * @return The singleton instance of {@link CountingResultCollector}.
	 */
	public synchronized static CountingResultCollector getInstance() {
		if (instance == null) {
			instance = new CountingResultCollector();
		}
		return instance;
	}

	/**
	 * see http://en.wikipedia.org/wiki/Data_log
	 */
	private Logger log;
	
	/**
	 * The current result collector mode which switches forced inlining etc. on.
	 */
	private CountingResultCollectorMode mode;

	/**
	 * For usage related to the class CountingResultCollectorMonitor...
	 * TODO refactor this.
	 */
	private boolean monitorShouldStop;

	/**
	 * When a {@link CountingResultBase} is logged, all known writers will be
	 * asked to log (write) it as well. This mechanism is introduced to
	 * decouple {@link CountingResultCollector} from specific mechanisms and
	 * frameworks, such as CSV writing, JFreeChart creation etc.
	 */
	private List<ICountingResultWriter> resultWriters;

	/** Instrumentation context with basic block and range block definitions. */
	public InstrumentationContext instrumentationContext;
	
	/**
	 * Method execution details on how BytecodeCounters execute method was 
	 * last called.
	 */
	private MethodExecutionRecord lastMethodExecutionDetails;

	/** Forced inlining result collection strategy. */
	private ICollectionStrategy inliningStrategyForced;

	/** Inlining result collection strategy for methods requesting inlining. */
	private ICollectionStrategy inliningStrategyWished;
	
	/** Spawned threads as reported from instrumented methods. */
	private List<Thread> spawnedThreads;

	/** Default result collection Strategy */
	private ICollectionStrategy strategyDefault;
	
	/** List of all used result collection strategies */
	private List<ICollectionStrategy> collectionStrategies;

	/**
	 * When {@link InstrumentationParameters#getProvideOnlineSectionActiveUpdates()}
	 * is true, this field hold the instrumented entity last entered.
	 */
	private Map<Long, EntityToInstrument> activeEntity;

	private ResultCollection currentResultCollection;

	/**
	 * Private constructor that is invoked to create the singleton instance
	 */
	private CountingResultCollector() {
		this.log = Logger.getLogger(this.getClass().getCanonicalName());
		this.mode = MODE_DEFAULT;
		this.resultWriters = new ArrayList<ICountingResultWriter>();
		this.instrumentationContext = null;
		
		this.collectionStrategies = new LinkedList<ICollectionStrategy>();
		this.strategyDefault = new CollectionStrategyDefault(this);
		this.inliningStrategyForced = new CollectionStrategyForceInlining(this);
		this.inliningStrategyWished = new CollectionStrategyWishedInlining(this);
		this.collectionStrategies.add(this.strategyDefault);
		this.collectionStrategies.add(this.inliningStrategyWished);
		this.collectionStrategies.add(this.inliningStrategyForced);
		this.activeEntity = new HashMap<Long, EntityToInstrument>();
		this.spawnedThreads = new LinkedList<Thread>();
		this.resetResultCollection();
	}

	/**
	 * Clear all results in the internal list.
	 * Also resets the currently active section of execution and
	 * the list of threads spawned from instrumented code.
	 */
	public synchronized void clearResults() {
		for(ICollectionStrategy s : this.collectionStrategies) {
			s.clearResults();
		}
		this.resetResultCollection();
		this.activeEntity.clear();
		this.spawnedThreads.clear();
	}

	/**
	 * Reconstruct the {@link #currentResultCollection} and provide a 
	 * reference to each {@link ICollectionStrategy}.
	 */
	private void resetResultCollection() {
		this.currentResultCollection = new ResultCollection();
		for(ICollectionStrategy s : this.collectionStrategies) {
			s.setResultCollection(this.currentResultCollection);
		}	
	}
	
	/**
	 * Gets all result writers registered to the collector.
	 * @return A list of {@link ICountingResultWriter}s.
	 */
	public List<ICountingResultWriter> getAllResultWriters() {
		return this.resultWriters;
	}

	/** 
	 * @see BytecodeCounter#setExecutionSettings(ExecutionSettings)
	 * @see CountingResultCollectorMode
	 * @return Current Counting mode.
	 */
	public CountingResultCollectorMode getMode() {
		return this.mode;
	}

	/**
	 * @deprecated because only GUI-used but the GUI is outdated
	 */
	public boolean getMonitorShouldStop() {
		return this.monitorShouldStop;
	}

	/**
	 * @deprecated because only GUI-used but the GUI is outdated
	 */
	public void monitorShouldStop() {
		this.setMonitorShouldStop(true);
	}
	
	/**
	 * Join with all threads that were spawned from instrumented methods.
	 * Instrumented methods register spawned threads with 
	 * {@link #protocolSpawnedThread(Thread)}.
	 * <p>
	 * After successfully joining all threads, the threads are removed from the
	 * internal list of spawned threads.
	 * </p>
	 * @throws InterruptedException Thrown when {@link Thread#join()} was 
	 * interrupted.
	 */
	public void joinSpawnedThreads() throws InterruptedException {
		for(Thread t : this.spawnedThreads) {
			t.join();
		}
		this.spawnedThreads.clear();
	}
	
	/**
	 * An instrumented class calls this method to report the instruction and method call counts.
	 * TODO: how far is "synchronized" problematic in multi-threading?
	 * @param result The result reported by an instrumented method.
	 * @return True when the result was accepted, false when it is ignored.
	 */
	public synchronized boolean protocolCount(final ProtocolCountStructure result) {
		result.reportingStart = System.nanoTime();//TODO make this configurable and clear, move to an interface/class that is accessed
		boolean handledResult = false;
		if(this.mode==CountingResultCollectorMode.DiscardAllIncomingCountingResults){
			log.fine("Discarding counting result of method "+result.qualifyingMethodName+", which started execution " +
					"at "+result.executionStart);
			handledResult = true;
		} else if(this.mode.getForceInliningPossible()){
			handledResult = this.inliningStrategyForced.protocolCount(result);
		}
		
		if(!handledResult) {
			// the result was not accepted by a strategy yet
			if(result.inliningSpecified) {
				handledResult = this.inliningStrategyWished.protocolCount(result);
			} else {
				handledResult = this.strategyDefault.protocolCount(result);
			}
		}

		if(!handledResult) {
			// Result was not added by any of the strategies. This most often means
			// that it is not needed (for instance out of region).
			log.info("Protocolled count at " + result.executionStart + " not added.");
			return false;
		} else if(this.countObservers() > 0) {
			
			// notify observers
			this.setChanged();
			if(!(result instanceof ProtocolCountUpdateStructure)) {
				((CollectionStrategyDefault)this.strategyDefault).getCountingResultIndexing().retrieveCountingResultByMethodStartTime(result.executionStart);
				ResultCollection allResults = this.retrieveAllCountingResults();
				// filter results to only contain those for the current method.
				ResultCollection relevantResults = new ResultCollection();
				for(de.uka.ipd.sdq.ByCounter.results.CountingResult r : allResults.getCountingResults()) {
					if(r.getMethodInvocationBeginning() == result.executionStart) {
						relevantResults.getCountingResults().add(r);
					}
				}
				this.notifyObservers(
						new CountingResultCompleteMethodExecutionUpdate(relevantResults));
			}
		}
		return true;
	}
	
	/**
	 * Called by an instrumented method if 
	 * {@link InstrumentationParameters#getProvideOnlineSectionActiveUpdates()}
	 * is true.
	 * @param activeEntityUUID {@link UUID#toString()} of the instrumented entity of the reporting method.
	 */
	public void protocolActiveEntity(final String activeEntityUUID) {
		EntityToInstrument entity = null;
		if(activeEntityUUID != null) {
			entity = instrumentationContext.getEntitiesToInstrument().get(UUID.fromString(activeEntityUUID));
		}
		this.activeEntity.put(Thread.currentThread().getId(), entity);
	}
	
	/**
	 * Preallocate a CountingResult in the result collector so that the 
	 * structure of ThreadedCountingResults can be correctly constructed.
	 * @param futureCount {@link ProtocolFutureCountStructure}.
	 */
	public void protocolFutureCount(final ProtocolFutureCountStructure futureCount) {
		this.strategyDefault.protocolFutureCount(futureCount);
	}
	
	/**
	 * Called by an instrumented method if a {@link Thread} is spawned.
	 * @param thread Thread object for a started thread.
	 */
	public void protocolSpawnedThread(final Thread thread) {
		this.spawnedThreads.add(thread);
	}
	
	/**
	 * When an {@link EntityToInstrument} is currently being 
	 * executed and {@link InstrumentationParameters#getProvideOnlineSectionActiveUpdates()}
	 * is true, this entity is returned for it's thread id.
	 * @return A map from  thread id to the currently active 
	 * {@link EntityToInstrument} for that thread. The entity can be null.
	 * @throws InvalidQueryException Thrown when the instrumentation does not
	 * support the query. 
	 */
	public EntityToInstrument queryActiveSection(long threadId) throws InvalidQueryException {
		if(!this.instrumentationContext.getQueryActiveEntitySupported()) {
			throw new InvalidQueryException("The instrumentation does not provide support for querying active sections.");
		}
		return this.activeEntity.get(threadId);
	}
	
	/**
	 * Adds an additional result writer used in {@link CountingResultBase#logResult(boolean, boolean, Level)}.
	 * @param resultWriter {@link ICountingResultWriter} used when logging result.
	 */
	public synchronized void registerWriter(ICountingResultWriter resultWriter){
		if(resultWriter==null){
			log.severe("Passed resultWriter is null, adding nonetheless");
		}
		this.resultWriters.add(resultWriter);
	}
		
	/**
	 * Get all results the {@link CountingResultCollector} holds.
	 * This does not clear the {@link CountingResultCollector} contents.
	 * You have to explicitly
	 * call <code>clearResults()</code> if that is your intention.
	 * @return A {@link ResultCollection}.
	 */
	public synchronized ResultCollection retrieveAllCountingResults() {
		for(ICollectionStrategy s : this.collectionStrategies) {
			s.prepareCountingResults();
		}
		return currentResultCollection;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void setChanged() {
		super.setChanged();
	}

	/**
	 * @deprecated because only GUI-used but the GUI is outdated
	 * @param monitorShouldStop
	 */
	public void setMonitorShouldStop(boolean monitorShouldStop) {
		this.monitorShouldStop = monitorShouldStop;
	}
	
	/**
	 * This is called by {@link BytecodeCounter} when an execute method is 
	 * executed to provide the details of the execution to 
	 * {@link CountingResultCollector}.
	 * <p>
	 * Do not call; instead use {@link BytecodeCounter#setExecutionSettings(ExecutionSettings)}.
	 * </p>
	 * @param lastMethodExecutionDetails Method execution details.
	 */
	public void setLastMethodExecutionDetails(MethodExecutionRecord lastMethodExecutionDetails) {
		this.lastMethodExecutionDetails = lastMethodExecutionDetails;
		// set the counting mode
		this.mode = lastMethodExecutionDetails.executionSettings.getCountingResultCollectorMode();
	}

	/**
	 * The settings used for the last execution relevant to the 
	 * {@link CountingResultCollector}.
	 * <p>Do not call to change these settings.
	 * Instead, use {@link BytecodeCounter#setExecutionSettings(ExecutionSettings)}.
	 * </p>
	 * @see #setLastMethodExecutionDetails(MethodExecutionRecord)
	 * @return the lastMethodExecutionDetails
	 */
	public MethodExecutionRecord getLastMethodExecutionDetails() {
		return lastMethodExecutionDetails;
	}
}
