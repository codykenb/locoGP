package de.uka.ipd.sdq.ByCounter.instrumentation;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * A collection of properties that determine the way the instrumentation
 * is done.
 * 
 * <p>
 * Some combination of parameters only make sense in combination with others.
 * The following list summarizes these dependencies.
 * <ul>
 * <li>{@link #setWriteClassesToDiskDirectory(File)} only applies if {@link #getWriteClassesToDisk()} == true</li>
 * <li>{@link #setRecordBlockExecutionOrder(boolean)} only applies if either range blocks or basic blocks are used, i.e. if {@link #getUseBasicBlocks()} == true</li>
 * <li>{@link #setUseArrayParameterRecording(boolean)} is currently only supported when not using basic/range blocks.</li>
 * <li>Instrumentation regions ({@link InstrumentedRegion}) only work with {@link #getUseBasicBlocks()} == true and {@link #getProvideOnlineSectionExecutionUpdates()} == true</li>
 * </ul>
 * </p>
 * 
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
public final class InstrumentationParameters implements Cloneable {

	/** Default value for {@link #getIgnoredPackagePrefixes()}. */
	public static final String[] IGNORED_PACKAGES_DEFAULT = {
			"java/",
			"javax/",
			"sun/",
			"org/w3c/dom/"
		};

	/** Default value for {@link #getCounterPrecision()}:
	 * {@value #COUNTER_PRECISION_DEFAULT} */
	private static final InstrumentationCounterPrecision COUNTER_PRECISION_DEFAULT = InstrumentationCounterPrecision.Long;

	/** Default value for {@link #getUseBasicBlocks()}:
	 * {@value #USE_BASIC_BLOCKS_DEFAULT} */
	public static final boolean USE_BASIC_BLOCKS_DEFAULT = false;

	/** 
	 * Default value for {@link #getRecordBlockExecutionOrder()}:
	 * {@value #RECORD_BLOCK_EXECUTION_ORDER_DEFAULT}
	 */
	public static final boolean RECORD_BLOCK_EXECUTION_ORDER_DEFAULT = true;

	/** Default value for {@link #getInstrumentationScopeOverrideClassLevel()}. */
	public static final InstrumentationScopeModeEnum INSTRUMENTATION_SCOPE_OVERRIDE_CLASS_LEVEL_DEFAULT = InstrumentationScopeModeEnum.InstrumentAsSpecified;
	
	/** Default value for {@link #getInstrumentationScopeOverrideMethodLevel()}. */
	public static final InstrumentationScopeModeEnum INSTRUMENTATION_SCOPE_OVERRIDE_METHOD_LEVEL_DEFAULT = InstrumentationScopeModeEnum.InstrumentAsSpecified;

	/** Default value for {@link #getWriteClassesToDiskDirectory()}. */
	public static final File WRITE_CLASSES_TO_DISK_DIRECTORY_DEFAULT = new File("bin_instrumented");

	/** Default value for {@link #getTraceAndIdentifyRequests()}. */
	public static final boolean TRACE_AND_IDENTIFY_REQUESTS_DEFAULT = false;

	/** Default value for {@link #getWriteClassesToDisk()}. */
	public static final boolean WRITE_CLASSES_TO_DISK_DEFAULT = false;

	/** Default value for {@link #getInstrumentRecursively()}. */
	public static final boolean INSTRUMENT_RECURSIVELY_DEFAULT = false;

	/** Default value for {@link #getUseHighRegistersForCounting()}. */
	public static final boolean USE_HIGH_REGISTERS_FOR_COUNTING_DEFAULT = true;

	/** Default value for {@link #getUseResultCollector()}. */
	public static final boolean USE_RESULT_COLLECTOR_DEFAULT = true;

	/** Default value for {@link #getUseResultLogWriter()}. */
	public static final boolean USE_RESULT_LOG_WRITER_DEFAULT = false;

	/** Default value for {@link #getUseArrayParameterRecording()}. */
	public static final boolean USE_ARRAY_PARAMETER_RECORDING = false;

	/** Default value for {@link #getCountStatically()}. */
	public static final boolean COUNT_STATICALLY_DEFAULT = false;

	/** Default value for {@link #getProvideOnlineSectionExecutionUpdates()}. */
	private static final boolean PROVIDE_ONLINE_SECTION_EXECUTION_UPDATES_DEFAULT = false;

	/** Default value for {@link #getProvideOnlineSectionActiveUpdates()}. */
	private static final boolean PROVIDE_ONLINE_SECTION_ACTIVE_UPDATES_DEFAULT = false;
	
	/** Default value for #{@link #getProvideJoinThreadsAbility()}. */
	private static final boolean PROVIDE_JOIN_THREADS_ABILITY_DEFAULT = true;
	
	/**
	 * Directory in which result log files are written by default.
	 * This only applies if 
	 * <code>{@link #getUseResultCollector()} == true</code>.
	 * @see #RESULT_LOG_DEFAULT_PREFIX
	 */
	public static final String RESULT_LOG_DEFAULT_DIRECTORY = 
		"ByCounter_logged_counting_results";

	/**
	 * Default file name prefix of the result log files.
	 * This only applies if 
	 * <code>{@link #getUseResultCollector()} == true</code>.
	 * @see #RESULT_LOG_DEFAULT_DIRECTORY
	 */
	public static final String RESULT_LOG_DEFAULT_PREFIX = 
		RESULT_LOG_DEFAULT_DIRECTORY + 
		File.separatorChar;

	/**
	 * A list of strings that cause a class to be ignored in the parsing 
	 * when found at the start of a package name.
	 */
	protected String[] ignoredPackagePrefixes = IGNORED_PACKAGES_DEFAULT;

	/** Decides on the precision of the variables used for counting. For 
	 * very high counts integers might not be enough and you want to use 
	 * long variables instead. On the other hand, integer counting offers
	 * better performance. */
	private InstrumentationCounterPrecision counterPrecision;

	/** When true, ByCounter makes a static analysis of the specified code. */
	@Deprecated
	private boolean countStatically;
	
	/**
	 * The entities to instrument in the instrumentation run.
	 */
	private List<EntityToInstrument> entitiesToInstrument;
	
	/**
	 * @see #setInstrumentRecursivly(boolean, int)
	 */
	private boolean instrumentRecursively;
	
	/** The filename of the log containing the results, that is used if useResultCollector == false. */
	private String resultLogFileName;

	/** Decides whether instrumentation for the recording of parameters of array construction takes place. 
	 * Causes some additional overhead.
	 * When false, results are written to disk directly.  */
	private boolean useArrayParameterRecording;
	
	/**
	 * When true, bytecode instructions will be counted in groups made up of 
	 * identified basic blocks. The execution numbers of single instructions 
	 * are calculated after the execution.
	 * When false, every single bytecode instruction will be counted 
	 * by an individual counter.
	 */
	private boolean useBasicBlocks;
	
	/** Decides whether to preallocate registers near max_locals instead of using LocalVariablesSorter. */
	private boolean useHighRegistersForCounting;
	
	/** Decides whether to use the CountingResultCollector framework. */
	private boolean useResultCollector;
	
	/** Decides whether to write the instrumentation results into log files. */
	private boolean useResultLogWriter;
	
	/**
	 * When true, ByCounter will write the instrumented class files
	 * to the "bin_instrumented" directory.
	 */
	private boolean writeClassesToDisk;
	
	/**
	 * @see #getRecordBlockExecutionOrder()
	 */
	private boolean recordBlockExecutionOrder;
	
	/**
	 * @see #getProvideJoinThreadsAbility()
	 */
	private boolean provideJoinThreadsAbility;

	/**
	 * @see #getProvideOnlineSectionExecutionUpdates()
	 */
	private boolean provideOnlineSectionExecutionUpdates;
	
	/**
	 * @see #getProvideOnlineSectionActiveUpdates()
	 */
	private boolean provideOnlineSectionActiveUpdates;
	
	/**
	 * @see #getTraceAndIdentifyRequests()
	 */
	private boolean traceAndIdentifyRequests;

	/**
	 * If {@link #getWriteClassesToDisk()} is enabled, classes will be written 
	 * to the here specified directory.
	 * @see {@link #setWriteClassesToDisk(boolean)}
	 */
	private File writeClassesToDiskDirectory;

	/**
	 * Specifies if all or no methods shall be instrumented instead of the 
	 * otherwise specified methods.
	 */
	private InstrumentationScopeModeEnum instrumentationScopeOverrideClassLevel;
	
	/**
	 * Overrides the instrumentation behavior in a method.
	 */
	private InstrumentationScopeModeEnum instrumentationScopeOverrideMethodLevel;
	
	/**
	 * This is intended only for construction in multiple steps.
	 * Methods to instrument are NOT set - you must do so manually! 
	 * Assumes dynamic analysis and usage of the CountingResultCollector.
	 * Uses high registers for counting and the CountingResultCollector 
	 * framework.
	 */
	@SuppressWarnings("dep-ann")
	public InstrumentationParameters() {
		this(	new LinkedList<EntityToInstrument>(),
				USE_HIGH_REGISTERS_FOR_COUNTING_DEFAULT,
				USE_RESULT_COLLECTOR_DEFAULT,	// use CountingResultCollector instead of result log
				USE_ARRAY_PARAMETER_RECORDING,
				COUNT_STATICALLY_DEFAULT,
				COUNTER_PRECISION_DEFAULT
			);
	}
	
	/**
	 * Assumes dynamic analysis and usage of the CountingResultCollector. Array 
	 * construction parameters will not be recorded.
	 * Uses high registers for counting.
	 * @param pEntitesToInstrument Entities that shall be instrumented. 
	 */
	@SuppressWarnings("dep-ann")
	public InstrumentationParameters(final List<EntityToInstrument> pEntitesToInstrument) {
		this(pEntitesToInstrument, 
				USE_HIGH_REGISTERS_FOR_COUNTING_DEFAULT,
				USE_RESULT_COLLECTOR_DEFAULT,	// use CountingResultCollector instead of result log
				USE_ARRAY_PARAMETER_RECORDING,
				COUNT_STATICALLY_DEFAULT,
				COUNTER_PRECISION_DEFAULT
				);
	}
	
	/**
	 * @param pEntitesToInstrument Entities that shall be instrumented.
	 * @param pUseHighRegistersForCounting Decides whether to preallocate registers near max_locals instead of using LocalVariablesSorter.
	 * @param pUseResultCollector Decides whether to use the CountingResultCollector framework.
	 * @param pUseArrayParameterRecording Decides whether instrumentation for the recording of parameters of array construction takes place. Causes some additional overhead.
	 * @param countStatically When true, ByCounter makes a static analysis of the specified code.
	 * @param counterPrecision Decides on the precision of the variables used 
	 * for counting. See the COUNTER_PRECISION_ constants. 
	 */
	public InstrumentationParameters(
			final List<EntityToInstrument> pEntitesToInstrument,
			boolean pUseHighRegistersForCounting, 
			boolean pUseResultCollector,
			boolean pUseArrayParameterRecording,
			boolean countStatically,
			InstrumentationCounterPrecision counterPrecision) {
		this.setUseBasicBlocks(USE_BASIC_BLOCKS_DEFAULT);
		this.setUseHighRegistersForCounting(pUseHighRegistersForCounting);
		this.setUseResultCollector(pUseResultCollector);
		this.useResultLogWriter = USE_RESULT_LOG_WRITER_DEFAULT;
		this.resultLogFileName = RESULT_LOG_DEFAULT_PREFIX;
		this.setCountStatically(countStatically);
		this.setUseArrayParameterRecording(pUseArrayParameterRecording);
		this.counterPrecision = counterPrecision;
		this.writeClassesToDisk = WRITE_CLASSES_TO_DISK_DEFAULT;
		this.traceAndIdentifyRequests = TRACE_AND_IDENTIFY_REQUESTS_DEFAULT;
		this.writeClassesToDiskDirectory = WRITE_CLASSES_TO_DISK_DIRECTORY_DEFAULT;
		this.instrumentationScopeOverrideClassLevel = INSTRUMENTATION_SCOPE_OVERRIDE_CLASS_LEVEL_DEFAULT;
		this.instrumentationScopeOverrideMethodLevel = INSTRUMENTATION_SCOPE_OVERRIDE_METHOD_LEVEL_DEFAULT;
		this.recordBlockExecutionOrder = RECORD_BLOCK_EXECUTION_ORDER_DEFAULT;
		this.instrumentRecursively = INSTRUMENT_RECURSIVELY_DEFAULT;
		this.provideOnlineSectionExecutionUpdates = PROVIDE_ONLINE_SECTION_EXECUTION_UPDATES_DEFAULT;
		this.provideOnlineSectionActiveUpdates = PROVIDE_ONLINE_SECTION_ACTIVE_UPDATES_DEFAULT;
		this.provideJoinThreadsAbility = PROVIDE_JOIN_THREADS_ABILITY_DEFAULT;
		this.entitiesToInstrument = pEntitesToInstrument;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public InstrumentationParameters clone() {
		InstrumentationParameters copy = null;
		
		try {
			copy = (InstrumentationParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			// object.clone() cannot fail
			return null;
		}
		
		// copy all fields
		copy.counterPrecision = this.counterPrecision;
		copy.countStatically = this.countStatically;
		copy.entitiesToInstrument = new LinkedList<EntityToInstrument>();
		copy.entitiesToInstrument.addAll(this.entitiesToInstrument);
		copy.ignoredPackagePrefixes = this.ignoredPackagePrefixes.clone();
		copy.instrumentationScopeOverrideClassLevel = this.instrumentationScopeOverrideClassLevel;
		copy.instrumentationScopeOverrideMethodLevel = this.instrumentationScopeOverrideMethodLevel;
		copy.instrumentRecursively = this.instrumentRecursively;
		copy.provideJoinThreadsAbility = this.provideJoinThreadsAbility;
		copy.provideOnlineSectionActiveUpdates = this.provideOnlineSectionActiveUpdates;
		copy.provideOnlineSectionExecutionUpdates = this.provideOnlineSectionExecutionUpdates;
		copy.recordBlockExecutionOrder = this.recordBlockExecutionOrder;
		copy.resultLogFileName = this.resultLogFileName;
		copy.traceAndIdentifyRequests = this.traceAndIdentifyRequests;
		copy.useArrayParameterRecording = this.useArrayParameterRecording;
		copy.useBasicBlocks = this.useBasicBlocks;
		copy.useHighRegistersForCounting = this.useHighRegistersForCounting;
		copy.useResultCollector = this.useResultCollector;
		copy.useResultLogWriter = this.useResultLogWriter;
		copy.writeClassesToDisk = this.writeClassesToDisk;
		copy.writeClassesToDiskDirectory = this.writeClassesToDiskDirectory;
		
		return copy;
	}

	/**
	 * @deprecated Static counting is implemented only in it's basics (opcode and method call counts). 
	 * It is not a focus of ByCounter.
	 * @return Reflects, whether dynamic or static method analysis is employed.
	 */
	@SuppressWarnings("dep-ann")
	public boolean getCountStatically() {
		return this.countStatically;
	}
	
	/**
	 * @return The entities to instrument in the instrumentation run.
	 */
	public List<EntityToInstrument> getEntitiesToInstrument() {
		return this.entitiesToInstrument;
	}
	
	/**
	 * @return @see {@link #setInstrumentRecursively(boolean)}.
	 */
	public boolean getInstrumentRecursively() {
		return this.instrumentRecursively;
	}

	/**
	 * @return The result log filename used if 
	 * {@link #getUseResultLogWriter()} == true.
	 * The given filename is a prefix to the generated filename that includes 
	 * the method descriptor and a timestamp.
	 * @see #enableResultLogWriter(String)
	 * @see #getUseResultLogWriter()
	 */
	public String getResultLogFileName() {
		return this.resultLogFileName;
	}

	/**
	 * Decides whether instrumentation for the recording of parameters of array construction takes place. Causes some additional overhead. 
	 * @return True if recording is inserted, false otherwise.
	 */
	public boolean getUseArrayParameterRecording() {
		return this.useArrayParameterRecording;
	}

	/**
	 * @return useHighRegistersForCounting
	 */
	public boolean getUseHighRegistersForCounting() {
		return this.useHighRegistersForCounting;
	}

	/**
	 * @return When true, results will be collected using 
	 * {@link CountingResultCollector}.
	 */
	public boolean getUseResultCollector() {
		return this.useResultCollector;
	}
	
	/**
	 * @return When true, a log file will be written when instrumented methods 
	 * are executed.
	 */
	public boolean getUseResultLogWriter() {
		return this.useResultLogWriter;
	}

	/**
	 * When true, ByCounter will write the instrumented class files
	 * to the "bin_instrumented" directory.
	 * @return The value of writeClassesToDisk.
	 */
	public boolean getWriteClassesToDisk() {
		return this.writeClassesToDisk;
	}

	/**
	 * @deprecated Static counting is implemented only in it's basics (opcode and method call counts). 
	 * It is not a focus of ByCounter.
	 * @param countStatically When true, no runtime analysis is done. Instead the
	 * method is statically analysed.
	 */
	@SuppressWarnings("dep-ann")
	public void setCountStatically(boolean countStatically) {
		this.countStatically = countStatically;
	}

	/**
	 * Enable writing of result logs and
	 * set the filename for the log that is created.
	 * Use this if you want to override the default file name 
	 * {@link #RESULT_LOG_DEFAULT_PREFIX}
	 * that consists of a time stamp and the class and method name,
	 * and will be written to
	 * the {@link InstrumentationParameters#RESULT_LOG_DEFAULT_DIRECTORY} directory.
	 * The given filename is a prefix to the generated filename that includes 
	 * the method descriptor and a timestamp.
	 * 
	 * @param resultLogFileName The prefix of written log files.
	 */
	public void enableResultLogWriter(String resultLogFileName) {
		this.useResultLogWriter = true;
		this.resultLogFileName = resultLogFileName;
	}
	
	/**
	 * Disable result log writing.
	 * @see #enableResultLogWriter(String)
	 * @see #setUseResultCollector(boolean)
	 */
	public void disableResultLogWriter() {
		this.useResultLogWriter = false;
	}


	/**
	 * Decides whether instrumentation for the recording of parameters of array construction takes place. Causes some additional overhead.
	 * @param useArrayParameterRecording Set to true if recording is to be inserted, false otherwise.
	 */
	public void setUseArrayParameterRecording(boolean useArrayParameterRecording) {
		this.useArrayParameterRecording = useArrayParameterRecording;
	}

	/**
	 * Decides whether to preallocate registers near max_locals instead of using LocalVariablesSorter.
	 * The default (when not called) is false.
	 * When true, the instrumented bytecode remains closer to the original bytecode in that the register numbers stay the same.
	 * Setting this to true might cause problems if the instrumented code uses very high register numbers (near 65000).
	 * @param useHighRegistersForCounting Defaults to false.
	 */
	public void setUseHighRegistersForCounting(boolean useHighRegistersForCounting) {
		this.useHighRegistersForCounting = useHighRegistersForCounting;
	}

	/**
	 * @param useResultCollector Set whether to use the 
	 * <code>CountingResultCollector</code>.
	 * @see #getUseResultCollector()
	 * @see #getUseResultLogWriter()
	 * @see #enableResultLogWriter(String)
	 * @see #disableResultLogWriter()
	 */
	public void setUseResultCollector(boolean useResultCollector) {
		this.useResultCollector = useResultCollector;
	}

	/**
	 * Sets the value of writeClassesToDisk.
	 * When true, ByCounter will write the instrumented class files
	 * to the "bin_instrumented" directory.
	 * @param write The new value for writeClassesToDisk.
	 */
	public void setWriteClassesToDisk(boolean write) {
		this.writeClassesToDisk = write;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder("InstrumentationParameters {\n");
		b.append("counterPrecision:             	  " + this.counterPrecision + ", \n");
		b.append("countStatically:                    " + this.countStatically + ", \n");
		b.append("instrumentRecursively:              " + this.instrumentRecursively + ", \n");
		b.append("entitesToInstrument:                " + this.entitiesToInstrument + ", \n");
		b.append("provideJoinThreadsAbility:          " + this.provideJoinThreadsAbility + ", \n");
		b.append("provideOnlineActiveEntityUpdates:   " + this.provideOnlineSectionActiveUpdates + ", \n");
		b.append("provideOnlineSectionExecutionUpdates:" + this.provideOnlineSectionExecutionUpdates + ", \n");
		b.append("recordBlockExecutionOrder:          " + this.recordBlockExecutionOrder + ", \n");
		b.append("resultLogFileName:                  " + this.resultLogFileName + ", \n");
		b.append("traceAndIdentifyRequests:           " + this.traceAndIdentifyRequests + ", \n");
		b.append("useArrayParameterRecording:         " + this.useArrayParameterRecording + ", \n");
		b.append("useBasicBlocks:                     " + this.useBasicBlocks + ", \n");
		b.append("useHighRegistersForCounting:        " + this.useHighRegistersForCounting + ", \n");
		b.append("useResultCollector:                 " + this.useResultCollector + ", \n");
		b.append("useResultLogWriter:                 " + this.useResultLogWriter + ", \n");
		b.append("writeClassesToDisk:                 " + this.writeClassesToDisk + ", \n");
		b.append("writeClassesToDiskDirectory:        " + this.writeClassesToDiskDirectory + "\n");
		b.append("}");
		return b.toString();
	}

	/**
	 * When true, bytecode instructions will be counted in groups made up of 
	 * identified basic blocks. The execution numbers of single instructions 
	 * are calculated after the execution.
	 * When false, every single bytecode instruction will be counted 
	 * by an individual counter.
	 * @param useBasicBlocks the useBasicBlocks to set
	 */
	public void setUseBasicBlocks(boolean useBasicBlocks) {
		this.useBasicBlocks = useBasicBlocks;
	}

	/**
	 * When true, bytecode instructions will be counted in groups made up of 
	 * identified basic blocks. The execution numbers of single instructions 
	 * are calculated after the execution.
	 * When false, every single bytecode instruction will be counted
	 * by an individual counter.
	 * @return the useBasicBlocks
	 */
	public boolean getUseBasicBlocks() {
		return useBasicBlocks;
	}

	/**
	 * RequestIDs are UUIDs used to track the method call graph across threads.
	 * They are created in a root method (a method called with no request ID) 
	 * and then passed recursively on to all methods it calls.
	 * Note: Currently, constructors cannot be tracked.
	 * @param traceAndIdentifyRequests True, when request IDs are to be used. 
	 * False otherwise.
	 * Default is false.
	 */
	public void setTraceAndIdentifyRequests(boolean traceAndIdentifyRequests) {
		this.traceAndIdentifyRequests = traceAndIdentifyRequests;
	}

	/**
	 * RequestIDs are UUIDs used to track the method call graph across threads.
	 * They are created in a root method (a method called with no request ID) 
	 * and then passed recursively on to all methods it calls.
	 * Note: Currently, constructors cannot be tracked.
	 * @return True, when request IDs are used. False otherwise.
	 */
	public boolean getTraceAndIdentifyRequests() {
		return traceAndIdentifyRequests;
	}

	/**
	 * If {@link #getWriteClassesToDisk()} is enabled, classes will be written 
	 * to the here specified directory.
	 * @param outputClassDirectory The directory where instrumented class files 
	 * will be written to.
	 */
	public void setWriteClassesToDiskDirectory(File outputClassDirectory) {
		this.writeClassesToDiskDirectory = outputClassDirectory;
		
	}

	/**
	 * @return If {@link #getWriteClassesToDisk()} is enabled, classes will be written 
	 * to the here specified directory.
	 */
	public File getWriteClassesToDiskDirectory() {
		return this.writeClassesToDiskDirectory;
	}

	/**
	 * @param instrumentationScopeOverrideClassLevel the instrumentationScopeOverrideClassLevel to set
	 */
	public void setInstrumentationScopeOverrideClassLevel(
			InstrumentationScopeModeEnum instrumentationScopeOverrideClassLevel) {
		this.instrumentationScopeOverrideClassLevel = instrumentationScopeOverrideClassLevel;
	}

	/**
	 * @return the instrumentationScopeOverrideClassLevel
	 */
	public InstrumentationScopeModeEnum getInstrumentationScopeOverrideClassLevel() {
		return instrumentationScopeOverrideClassLevel;
	}

	/**
	 * @param instrumentationScopeOverrideMethodLevel the instrumentationScopeOverrideMethodLevel to set
	 */
	public void setInstrumentationScopeOverrideMethodLevel(
			InstrumentationScopeModeEnum instrumentationScopeOverrideMethodLevel) {
		this.instrumentationScopeOverrideMethodLevel = instrumentationScopeOverrideMethodLevel;
	}

	/**
	 * @return the instrumentationScopeOverrideMethodLevel
	 */
	public InstrumentationScopeModeEnum getInstrumentationScopeOverrideMethodLevel() {
		return instrumentationScopeOverrideMethodLevel;
	}
	
	/**
	 * @return
	 * A list of strings that cause a class to be ignored in the parsing 
	 * when found at the start of a package name.
	 */
	public String[] getIgnoredPackagePrefixes() {
		return ignoredPackagePrefixes;
	}

	/**
	 * @see InstrumentationCounterPrecision
	 * @return The precision currently set.
	 */
	public InstrumentationCounterPrecision getCounterPrecision() {
		return this.counterPrecision;
	}

	/**
	 * @see InstrumentationCounterPrecision
	 * @param counterPrecision The precision to set.
	 */
	public void setCounterPrecision(InstrumentationCounterPrecision counterPrecision) {
		this.counterPrecision = counterPrecision;
	}

	/**
	 * When set, instruments methods called from the methods specified in 
	 * {@link #getEntitiesToInstrument()} that 
	 * are not Java API methods (packages java.*, javax.* sun.*) and not 
	 * native methods.
	 * @param instrumentRecursively When true, the above applies.
	 */
	public void setInstrumentRecursively(boolean instrumentRecursively) {
		this.instrumentRecursively = instrumentRecursively;
	}

	/**
	 * @see #getIgnoredPackagePrefixes()
	 * @param ignoredPackagePrefixes Prefixes of packages that are ignored.
	 */
	public void setIgnoredPackagePrefixes(String[] ignoredPackagePrefixes) {
		this.ignoredPackagePrefixes = ignoredPackagePrefixes;
	}
	
	/**
	 * @see #getRecordBlockExecutionOrder()
	 * @param recordBlockExecutionOrder the recordBlockExecutionOrder to set
	 */
	public void setRecordBlockExecutionOrder(boolean recordBlockExecutionOrder) {
		this.recordBlockExecutionOrder = recordBlockExecutionOrder;
	}

	/**
	 * When true, record the exact order in which blocks are executed. This 
	 * applies to range blocks and to basic blocks.
	 * The order of the results as returned by the 
	 * {@link CountingResultCollector} then reflects the execution order.
	 * If this flag is set to false, the execution counts for each section are 
	 * aggregated.
	 * 
	 * <p>
	 * Note that enabling this option may result in memory and processing 
	 * overhead if the instrumented code contains sections that are executed 
	 * extremely often.
	 * </p>
	 * @return the recordBlockExecutionOrder
	 */
	public boolean getRecordBlockExecutionOrder() {
		return recordBlockExecutionOrder;
	}

	/**
	 * @return When true, allows ByCounter to wait for all threads spawned 
	 * from instrumented methods to complete.
	 * @see CountingResultCollector#joinSpawnedThreads()
	 */
	public boolean getProvideJoinThreadsAbility() {
		return this.provideJoinThreadsAbility;
	}
	
	/**
	 * @param provideJoinThreadsAbility When true, allows ByCounter to wait for all threads spawned 
	 * from instrumented methods to complete.
	 * @see CountingResultCollector#joinSpawnedThreads()
	 */
	public void setProvideJoinThreadsAbility(boolean provideJoinThreadsAbility) {
		this.provideJoinThreadsAbility = provideJoinThreadsAbility;
	}

	/**
	 * @return When true, the instrumented entity that is currently being 
	 * executed can be queried from the {@link CountingResultCollector}
	 * using the method {@link CountingResultCollector#queryActiveSection(long)}
	 */
	public boolean getProvideOnlineSectionActiveUpdates() {
		return provideOnlineSectionActiveUpdates;
	}

	/**
	 * {@link CountingResultCollector} provides a mechanism for monitoring 
	 * online updates on incoming results.
	 * @see CountingResultCollector#addObserver(java.util.Observer)
	 * @return When true, the instrumentation for providing updates on the 
	 * execution of user specified code sections is inserted. This only applies 
	 * when {@link #getRecordBlockExecutionOrder()} is also set to true.
	 */
	public boolean getProvideOnlineSectionExecutionUpdates() {
		return provideOnlineSectionExecutionUpdates;
	}

	/**
	 * 
	 * @param provideOnlineSectionEnteredUpdates
	 * When true, the instrumented section that is currently being 
	 * executed can be queried from the {@link CountingResultCollector}
	 * using the method {@link CountingResultCollector#queryActiveSection(long)}.
	 */
	public void setProvideOnlineSectionActiveUpdates(
			boolean provideOnlineSectionEnteredUpdates) {
		this.provideOnlineSectionActiveUpdates = provideOnlineSectionEnteredUpdates;
	}

	/**
	 * @see #getRecordBlockExecutionOrder()
	 * @see #getProvideOnlineSectionExecutionUpdates()
	 * @param provideOnlineSectionExecutionUpdates When true, instrumentation 
	 * for providing updates is added.
	 */
	public void setProvideOnlineSectionExecutionUpdates(
			boolean provideOnlineSectionExecutionUpdates) {
		if(provideOnlineSectionExecutionUpdates && !this.recordBlockExecutionOrder) {
			throw new IllegalArgumentException("Cannot provide online section execution updates with recordBlockExecutionOrder set to false.");
		}
		this.provideOnlineSectionExecutionUpdates = provideOnlineSectionExecutionUpdates;
	}
	
	/**
	 * @see #getEntitiesToInstrument()
	 * @param md A method that will be searched for {@link InstrumentedRegion}s.
	 * @return True if an {@link InstrumentedRegion} exists that either has 
	 * a start or a stop for the given method.
	 */
	public boolean hasInstrumentationRegionForMethod(MethodDescriptor md) {
		for(EntityToInstrument e : this.entitiesToInstrument) {
			if (e instanceof InstrumentedRegion) {
				InstrumentedRegion r = (InstrumentedRegion) e;
				if(r.getStartMethod().equals(md) || r.getStopMethod().equals(md)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @see #getEntitiesToInstrument()
	 * @return True if an {@link InstrumentedRegion} exists in {@link #getEntitiesToInstrument()}.
	 */
	public boolean hasInstrumentationRegions() {
		for(EntityToInstrument e : this.entitiesToInstrument) {
			if(e instanceof InstrumentedRegion) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see #getEntitiesToInstrument()
	 * @see #hasInstrumentationRegionForMethod(MethodDescriptor)
	 * @param md A method that will be searched for {@link InstrumentedRegion}s 
	 * that end here.
	 * @return True if an {@link InstrumentedRegion} exists that has 
	 * stop for the given method.
	 */
	public boolean hasInstrumentationRegionEndForMethod(MethodDescriptor md) {
		for(EntityToInstrument e : this.entitiesToInstrument) {
			if (e instanceof InstrumentedRegion) {
				InstrumentedRegion r = (InstrumentedRegion) e;
				if(r.getStopMethod().equals(md)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @return True, if there is an {@link EntityToInstrument} that is 
	 * an {@link InstrumentedCodeArea}.
	 */
	public boolean hasMethodsWithCodeAreas() {
		if(getEntitiesToInstrument() == null) {
			return false;
		}
		for(EntityToInstrument e : getEntitiesToInstrument()) {
			if(e instanceof InstrumentedCodeArea) {
				return true;
			}
		}
		return false;
	}
	/**
	 * @param m Method to look for.
	 * @return True, if there is an {@link EntityToInstrument} that is 
	 * an {@link InstrumentedCodeArea} defined for the given method.
	 */
	public List<InstrumentedCodeArea> findCodeAreasForMethod(MethodDescriptor m) {
		List<InstrumentedCodeArea> result = new LinkedList<InstrumentedCodeArea>();
		for(EntityToInstrument e : getEntitiesToInstrument()) {
			if(e instanceof InstrumentedCodeArea) {
				InstrumentedCodeArea instrumentedCodeArea = (InstrumentedCodeArea) e;
				if(e instanceof InstrumentedCodeArea
						&& instrumentedCodeArea.getMethod().equals(m)) {
					result.add(instrumentedCodeArea);
				}
			}
		}
		return result;
	}

	/**
	 * @param canonicalClassName Canonical class name.
	 * @return True, when the given class name is excluded from instrumentation.
	 * @see InstrumentationParameters#getIgnoredPackagePrefixes()
	 */
	public boolean isClassExcluded(String canonicalClassName) {
		// check if the method is from a class in an excluded package
		for(String p : getIgnoredPackagePrefixes()) {
			if(canonicalClassName.startsWith(p.replace('/', '.'))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check the different parameters for consistency.
	 * @throws IllegalArgumentException if two or more parameters are 
	 * conflicting.
	 */
	public void verify() throws IllegalArgumentException {
		if(this.hasInstrumentationRegions()) {
			// dealing with instrumentation regions
			if(this.useBasicBlocks == false) {
				throw new IllegalArgumentException("useBasicBlocks must be true when specifying instrumentation regions.");
			}
			if(this.provideOnlineSectionExecutionUpdates == false) {
				throw new IllegalArgumentException("provideOnlineSectionExecutionUpdates must be true when specifying instrumentation regions.");
			}
			if(this.hasMethodsWithCodeAreas()) {
				// LineNumberRanges where specified
				throw new IllegalArgumentException("When using instrumentation regions, you must not also specify code areas (LineNumberRanges) for methods.");
			}
		}
		if(this.provideOnlineSectionExecutionUpdates) {
			if(!this.useBasicBlocks) {
				throw new IllegalArgumentException("Online section execution updates are only supported with useBasicBlocks=true.");
			}
		}
		if(this.useArrayParameterRecording) {
			if(this.useBasicBlocks) {
				throw new IllegalArgumentException("Array parameter recording is only supported with useBasicBlocks=false.");
			}
		}
		if(this.provideOnlineSectionActiveUpdates) {
			if(!this.useBasicBlocks) {
				throw new IllegalArgumentException("Online active entity updates can only be provided when instrumenting ranges (useBasicBlocks=true)");
			}
		}
	}
}
