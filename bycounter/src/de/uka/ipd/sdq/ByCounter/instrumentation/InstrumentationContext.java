package de.uka.ipd.sdq.ByCounter.instrumentation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.execution.CountingMode;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector;
import de.uka.ipd.sdq.ByCounter.parsing.ArrayCreation;
import de.uka.ipd.sdq.ByCounter.parsing.InstructionBlockSerialisation;

/**
 * This class serialises all information produced by {@link BytecodeCounter} 
 * during the instrumentation phase that is necessary for execution and result 
 * evaluation.
 * 
 * @author Martin Krogmann
 *
 */
public class InstrumentationContext implements Serializable {

	/**
	 * Default filename for the {@link InstrumentationContext}.
	 */
	public static final String FILE_SERIALISATION_DEFAULT_NAME ="instrumentation_context.bcic";

	/**
	 * Version of the serialisation.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Version that the object was constructed with.
	 * @see #serialVersionUID
	 */
	private long version;

	/**
	 * Basic block definitions.
	 */
	private InstructionBlockSerialisation basicBlocks;
	
	/**
	 * Label block definitions.
	 */
	private InstructionBlockSerialisation labelBlocks;

	/**
	 * Range block definitions.
	 */
	private InstructionBlockSerialisation rangeBlocks;
	
	/**
	 * {@link InstrumentedCodeArea}s defined for each method.
	 */
	private Map<String, InstrumentedCodeArea[]> rangesByMethod;
	
	/**
	 * Instrumentation region definitions.
	 */
	private Set<InstrumentedRegion> instrumentationRegions;
	
	/**
	 * The mode of counting when using block based counting by method.
	 */
	private Map<String, BlockCountingMode> blockCountingMode;
	
	/**
	 * Global {@link CountingMode}
	 */
	private CountingMode countingMode;
	
	/**
	 * When true, the currently executed entity can be queried using 
	 * {@link CountingResultCollector#queryActiveSection()}.
	 */
	private boolean queryActiveEntitySupported;

	/**
	 * Array creation types by method.
	 */
	private Map<String, List<ArrayCreation>> arrayCreations;

	/**
	 * Entities specified for instrumentation by id.
	 */
	private Map<UUID, EntityToInstrument> entitiesToInstrument;
	
	/**
	 * Construct the instrumentation context.
	 */
	public InstrumentationContext() {
		this.version = serialVersionUID;
		this.basicBlocks = new InstructionBlockSerialisation();
		this.rangeBlocks = new InstructionBlockSerialisation();
		this.labelBlocks = new InstructionBlockSerialisation();
		this.instrumentationRegions = new HashSet<InstrumentedRegion>();
		this.arrayCreations = new HashMap<String, List<ArrayCreation>>();
		this.blockCountingMode = new HashMap<String, BlockCountingMode>();
		this.countingMode = CountingMode.Default;
		this.rangesByMethod = new HashMap<String, InstrumentedCodeArea[]>();
		this.queryActiveEntitySupported = false;
		this.entitiesToInstrument = new HashMap<UUID, EntityToInstrument>();
	}
	
	private static void checkVersion(final long version) {
		if(version != serialVersionUID) {
			throw new RuntimeException("Wrong version of " 
				+ InstrumentationContext.class.getClass().getCanonicalName() 
				+ ". Was " + version + " but expected " + serialVersionUID +".");
		}
	}

	/**
	 * Serialise the context. You need to create an instance of 
	 * {@link InstrumentationContext} and set all properties.
	 * @param ic An instance of {@link InstrumentationContext}.
	 * @param file The file to write to.
	 * @throws IOException Thrown when writing to disk fails.
	 */
	public static void serialise(
			InstrumentationContext ic,
			File file) throws IOException {
		
		ObjectOutputStream outStream = null;
		try {
			outStream = new ObjectOutputStream(new FileOutputStream(file));
			outStream.writeObject(ic);
		} finally {
			if(outStream != null) {
				outStream.close();
			}
		}
	}

	/**
	 * Read a serialisation file written using the 
	 * {@link #serialise(InstrumentationContext, File)} method.
	 * @param file The file to read the serialisation from.
	 * @return An instance of {@link InstrumentationContext}
	 * @throws FileNotFoundException Thrown when the specified file cannot be 
	 * found.
	 * @throws IOException Thrown when reading fails.
	 * @throws ClassNotFoundException Thrown when the format of the serialised 
	 * file is wrong.
	 */
	public static InstrumentationContext deserialise(File file) throws FileNotFoundException, IOException, ClassNotFoundException {
		InstrumentationContext ic = new InstrumentationContext();
		ObjectInputStream outStream = null;
		try {
			outStream = new ObjectInputStream(new FileInputStream(file));
			ic = (InstrumentationContext)outStream.readObject();
			checkVersion(ic.version);
		} finally {
			if(outStream != null) {
				outStream.close();
			}
		}
		return ic;
	}

	/**
	 * @return Basic block definitions when set. Null otherwise.
	 */
	public InstructionBlockSerialisation getBasicBlocks() {
		return this.basicBlocks;
	}
	
	/**
	 * @return Range block definitions when set. Null otherwise.
	 */
	public InstructionBlockSerialisation getRangeBlocks() {
		return this.rangeBlocks;
	}

	/**
	 * @return Label block definitions when set. Null otherwise.
	 */
	public InstructionBlockSerialisation getLabelBlocks() {
		return this.labelBlocks;
	}
	
	/**
	 * @return Instrumentation region definitions.
	 */
	public Set<InstrumentedRegion> getInstrumentationRegions() {
		return this.instrumentationRegions;
	}
	
	/**
	 * @return True unless basicBlocks is null or empty.
	 */
	public boolean isBasicBlocksLoaded() {
		if(this.basicBlocks == null
				|| this.basicBlocks.getInstructionBlocksByMethod().isEmpty()) {
			return false;
		}
		return true;
	}
	
	/**
	 * @return True unless rangeBlocks is null or empty.
	 */
	public boolean isRangeBlocksLoaded() {
		if(this.rangeBlocks == null
				|| this.rangeBlocks.getInstructionBlocksByMethod().isEmpty()) {
			return false;
		}
		return true;
	}
	
	/**
	 * @return True unless labelBlocks is null or empty.
	 */
	public boolean isLabelBlocksLoaded() {
		if(this.labelBlocks == null
				|| this.labelBlocks.getInstructionBlocksByMethod().isEmpty()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Try to load the context from {@link #FILE_SERIALISATION_DEFAULT_NAME}.
	 * @return The loaded {@link InstrumentationContext}.
	 */
	public static InstrumentationContext loadFromDefaultPath() {
		File iContextFile = new File(InstrumentationContext.FILE_SERIALISATION_DEFAULT_NAME);
		try {
			return InstrumentationContext.deserialise(iContextFile);
		} catch (Exception e) {
			throw new RuntimeException("Could not load instrumentation context from '" + iContextFile + "'.", e);
		}
	}

	/**
	 * @return The mode of counting when using block based counting by method.
	 */
	public Map<String, BlockCountingMode> getBlockCountingMode() {
		return this.blockCountingMode;
	}
	
	/**
	 * @param method Canonical method name.
	 * @param blockCountingMode The mode of counting when using block based counting by method.
	 */
	public void setBlockCountingMode(final String method, BlockCountingMode blockCountingMode) {
		this.blockCountingMode.put(method, blockCountingMode);
	}
	
	/**
	 * @param countingMode Global {@link CountingMode}.
	 */
	public void setCountingMode(CountingMode countingMode) {
		this.countingMode = countingMode;
	}
	
	/**
	 * @return Global {@link CountingMode}.
	 */
	public CountingMode getCountingMode() {
		return this.countingMode;
	}

	/**
	 * @return {@link InstrumentedCodeArea}s defined by each method.
	 */
	public Map<String, InstrumentedCodeArea[]> getRangesByMethod() {
		return this.rangesByMethod;
	}

	/**
	 * @return When true, the currently executed entity can be queried using 
	 * {@link CountingResultCollector#queryActiveSection(long)}.
	 */
	public boolean getQueryActiveEntitySupported() {
		return queryActiveEntitySupported;
	}

	/**
	 * @param queryActiveEntitySupported When true, the currently executed entity can be queried using 
	 * {@link CountingResultCollector#queryActiveSection(long)}.
	 */
	public void setQueryActiveEntitySupported(boolean queryActiveEntitySupported) {
		this.queryActiveEntitySupported = queryActiveEntitySupported;
	}

	/**
	 * @return Array creation types by method.
	 */
	public Map<String, List<ArrayCreation>> getArrayCreations() {
		return this.arrayCreations;
	}

	/** 
	 * @return Entities specified for instrumentation by id.
	 */
	public Map<UUID, EntityToInstrument> getEntitiesToInstrument() {
		return this.entitiesToInstrument;
	}
}
