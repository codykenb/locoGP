package de.uka.ipd.sdq.ByCounter.execution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uka.ipd.sdq.ByCounter.parsing.ArrayCreation;
import de.uka.ipd.sdq.ByCounter.reporting.ICountingResultWriter;
import de.uka.ipd.sdq.ByCounter.utils.FullOpcodeMapper;
import de.uka.ipd.sdq.ByCounter.utils.IAllJavaOpcodes;

/**
 * Class that holds the results of an (instrumented) method run.
 * TODO test serialisation? test O/R-mapper? test XML serialisation?
 * TODO in the long term, "section counts" should accept entire method as sections,
 * and method call counts as well as opcodeCounts should become redundant
 * TODO all setter method should return the old value of the set/overwritten variable
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
public class CountingResultBase
implements Serializable, Cloneable, IFullCountingResult, Comparable<IFullCountingResult>{

	/**
	 * Class used in {@link #logResult(boolean, boolean)} with
	 * statistics about {@link CountingResultBase#getMethodCallCounts()}.
	 */
	private class MethodLogInfo {
		public long totalCountOfAllMethods;
		public SortedSet<String> classesContainingMethodSigs;
		
		/** Initialise values with 0. */
		public MethodLogInfo() {
			this.classesContainingMethodSigs = new TreeSet<String>();
			this.totalCountOfAllMethods = 0; //you need longs for that...
		}

	}

	/**
	 * Class used in {@link #logResult(boolean, boolean)} with
	 * statistics about {@link CountingResultBase#getOpcodeCounts()}.
	 */
	private class OpcodeLogInfo {
		public long totalCountOfAllOpcodes;
		public int numberOfOpcodesWithNonzeroFrequencies;
		
		/** Initialise values with 0. */
		public OpcodeLogInfo() {
			this.totalCountOfAllOpcodes = 0; //you need longs for that...
			this.numberOfOpcodesWithNonzeroFrequencies = 0;
		}
	}

	/**
	 * Version for {@link Serializable} interface.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The highest possible number of a Java bytecode opcode.
	 */
	public static final int MAX_OPCODE = FullOpcodeMapper.mnemonics.length;
	
	/** Newline character for log output. */
	private static final String NEWLINE = System.getProperty("line.separator");

	/**
	 * see http://en.wikipedia.org/wiki/Data_log
	 */
	private static Logger log = Logger.getLogger(CountingResultBase.class.getCanonicalName());


	/**
	 * The returned CountingResult is completely different from the summands
	 * w.r.t. the method name, etc. Hence, it is not initialised and only
	 * holds the sum of the two added {@link CountingResultBase}s.
	 * Calling this method is quite expensive.
	 * @param left the first summand
	 * @param right the second summand
	 * @return an instance of {@link CountingResultBase} where only counts are initialised
	 */
	@SuppressWarnings("dep-ann")
	public synchronized static CountingResultBase add(CountingResultBase left, CountingResultBase right){
		long[] resultOpcodeCounts = new long[MAX_OPCODE];
		SortedMap<String,Long>  resultMethodCallCounts = new TreeMap<String, Long>();
		Map<ArrayCreation, Long> resultArrayCreationCounts;

		// add up all opcode counts
		resultOpcodeCounts = addOpcodeCounts(left.opcodeCounts, right.opcodeCounts);
		// add up all method call counts
		resultMethodCallCounts = addMethodCallCounts(left.methodCallCounts, right.methodCallCounts);
		// add up all array creation parameters
		resultArrayCreationCounts = addArrayCreationCounts(left.arrayCreationCounts, right.arrayCreationCounts);

		CountingResultBase cr;
		cr = new CountingResultBase(
				null,
				null,
				null,
				new String(""),
				-1L,
				-1L,
				resultOpcodeCounts,
				resultMethodCallCounts,
				resultArrayCreationCounts);

		return cr;
	}

	/**
	 * Add up method call counts.
	 * @param leftMethodCallCounts left operand
	 * @param rightMethodCallCounts right operand
	 * @return Sum of counts.
	 */
	private static SortedMap<String, Long> addMethodCallCounts(
			SortedMap<String, Long> leftMethodCallCounts,
			SortedMap<String, Long> rightMethodCallCounts) {
		TreeMap<String, Long> resultMethodCallCounts = new TreeMap<String, Long>();
		String keyString;
		Long resultValue;
		String rightKey_String;
		Long rightValue;

		// set all method call counts for which 'left' has keys:
		Iterator<String> iteratorMethods = leftMethodCallCounts.keySet().iterator();
		while (iteratorMethods.hasNext()) {
			keyString = iteratorMethods.next();
			rightValue = rightMethodCallCounts.get(keyString);
			resultValue = leftMethodCallCounts.get(keyString);
			if(rightValue != null) {
				resultValue += rightValue;
			}
			resultMethodCallCounts.put(
					new String(keyString),
					new Long(resultValue));
		}
		// set all method call counts for which only 'right' has keys:
		Iterator<String> methodsKeysetRight = rightMethodCallCounts.keySet().iterator();
		while (methodsKeysetRight.hasNext()) {
			rightKey_String = methodsKeysetRight.next();
			rightValue = rightMethodCallCounts.get(rightKey_String);
			resultValue = resultMethodCallCounts.get(rightKey_String);
			if(resultValue == null) {
				resultMethodCallCounts.put(rightKey_String, rightValue);
			}
		}
		return resultMethodCallCounts;
	}

	/**
	 * Add up the opcode count arrays
	 * @param leftOpcodeCounts Opcode counts of first summand.
	 * @param rightOpcodeCounts Opcode counts of second summand.
	 * @return The sum array.
	 */
	private static long[] addOpcodeCounts(long[] leftOpcodeCounts, long[] rightOpcodeCounts) {
		long[] resultOpcodeCounts = new long[MAX_OPCODE];
		// add up all opcode counts
		for(int i = 0; i < MAX_OPCODE; i++) {
			resultOpcodeCounts[i] = leftOpcodeCounts[i] + rightOpcodeCounts[i];
		}
		return resultOpcodeCounts;
	}

	/**
	 * Add up array creation counts..
	 * @param arrayCreationCountsLeft Left operand.
	 * @param arrayCreationCountsRight Right operand.
	 * @return The sum.
	 */
	private static Map<ArrayCreation, Long> addArrayCreationCounts(
			Map<ArrayCreation, Long> arrayCreationCountsLeft,
			Map<ArrayCreation, Long> arrayCreationCountsRight) {
		Map<ArrayCreation, Long> resultArrayCreationCounts = new HashMap<ArrayCreation, Long>();
		ArrayCreation keyACreation;
		Long resultValue;
		ArrayCreation rightKeyACreation;
		Long rightValue;

		if(arrayCreationCountsLeft == null
				|| arrayCreationCountsLeft.isEmpty()) {
			return arrayCreationCountsRight;
		}
		if(arrayCreationCountsRight == null
				|| arrayCreationCountsRight.isEmpty()) {
			return arrayCreationCountsLeft;
		}

		// set all array creation counts for which 'left' has keys:
		Iterator<ArrayCreation> iteratorMethods = arrayCreationCountsLeft.keySet().iterator();
		while (iteratorMethods.hasNext()) {
			keyACreation = iteratorMethods.next();
			rightValue = arrayCreationCountsRight.get(keyACreation);
			resultValue = arrayCreationCountsLeft.get(keyACreation);
			if(rightValue != null) {
				resultValue += rightValue;
			}
			resultArrayCreationCounts.put(
					keyACreation,
					new Long(resultValue));
		}
		// set all method call counts for which only 'right' has keys:
		Iterator<ArrayCreation> methodsKeysetRight = arrayCreationCountsRight.keySet().iterator();
		while (methodsKeysetRight.hasNext()) {
			rightKeyACreation = methodsKeysetRight.next();
			rightValue = arrayCreationCountsRight.get(rightKeyACreation);
			resultValue = resultArrayCreationCounts.get(rightKeyACreation);
			if(resultValue == null) {
				resultArrayCreationCounts.put(rightKeyACreation, rightValue);
			}
		}
		return resultArrayCreationCounts;
	}

	/**
	 * The returned CountingResult is completely different from the summands
	 * w.r.t. the method name, etc. Hence, it is not initialised and only
	 * holds the sum of the two added {@link CountingResultBase}s.
	 * Calling this method is quite expensive.
	 * @param left the first summand
	 * @param right the second summand
	 * @return an instance of {@link CountingResultBase} where only counts are initialised
	 */
	@SuppressWarnings("dep-ann")
	public synchronized static CountingResultBase addMethodsAndInstructionsOnly(
			CountingResultBase left, CountingResultBase right){
		long[] resultOpcodeCounts = new long[MAX_OPCODE];
		SortedMap<String,Long>  resultMethodCallCounts = new TreeMap<String, Long>();
		Map<ArrayCreation,Long>  resultArrayCreationCounts = new HashMap<ArrayCreation, Long>();

		// add up all opcode counts
		resultOpcodeCounts = addOpcodeCounts(left.opcodeCounts, right.opcodeCounts);
		// add up all method call counts
		resultMethodCallCounts = addMethodCallCounts(left.methodCallCounts, right.methodCallCounts);
		// add up all method call counts
		resultArrayCreationCounts = addArrayCreationCounts(left.arrayCreationCounts, right.arrayCreationCounts);

		CountingResultBase cr;
		cr = new CountingResultBase(
				null, //requestID
				null, //ownID
				null, //callerID
				new String(""),
				-1L,
				-1L,
				resultOpcodeCounts,
				resultMethodCallCounts,
				resultArrayCreationCounts
				);
		return cr;
	}

	/**
	 * This is extremely sub-optimal and redundant. Optimise later, because called infrequently.
	 *
	 * @param opcodeCounts2
	 * @param opcodeCounts3
	 * @return
	 */
	private static final boolean compareCounts(
			long[] opcodeCounts2,
			long[] opcodeCounts3) {
		long currCountLeft;
		long currCountRight;
		for(int i = 0; i < MAX_OPCODE; i++) {//TODO move most likely case to the top...
			currCountLeft = opcodeCounts2[i];
			currCountRight = opcodeCounts3[i];
			if(currCountLeft==0){
				if(currCountRight==0){
					//everything is fine
				}else if(currCountRight>0){
//					log.fine("For instruction "+currKey+", " +
//							"the first CountingResult has count 0 while "
//							"the second CountingResult has count "+currCountRight);
					return false;
				}else if(currCountRight<0){
//					log.fine("For instruction "+currKey+", " +
//					"the first CountingResult has count 0 while "
//					"the second CountingResult has count "+currCountRight+", "+
//					"even though negative counts are not allowed!");
					return false;
				}
			}else if(currCountLeft<0){
//				log.severe("For instruction "+currKey+", " +
//				"the first CountingResult has a negative count of "+
//				currCountLeft+" even though negative counts are not allowed!");
				return false;
			}else{//if(currCountLeft>0)
				if(currCountRight==currCountLeft){//TODO make sure equals is not needed here... object level!
					//everything is fine
				}else{
//					log.severe("opcode "+currKey+": " +
//							"positive first count of "+currCountLeft+
//							" different from second count of "+currCountRight);
					return false;
				}
			}
		}
		return true;
	}

	public static void main(String args[]){
		System.out.println("The main method of CountingResult serves as " +
				"a test case for compareCounts method");
		long[] opcodeCounts1 = new long[MAX_OPCODE];
		long[] opcodeCounts2 = new long[MAX_OPCODE];
		long[] opcodeCounts3 = new long[MAX_OPCODE];
		opcodeCounts1[1] = 11L;
		opcodeCounts1[2] =  0L;

		opcodeCounts2[1] =  11L;
		opcodeCounts2[3] =  0L;

		opcodeCounts3[1] =  12L;
		opcodeCounts3[2] =  0L;

		if(compareCounts(opcodeCounts1, opcodeCounts1)){
			System.out.println("Properly compared opcodeCounts1 with itself");
		}else{
			System.err.println("IMPROPERLY compared opcodeCounts1 with itself");
		}

		if(compareCounts(opcodeCounts2, opcodeCounts2)){
			System.out.println("Properly compared opcodeCounts2 with itself");
		}else{
			System.err.println("IMPROPERLY compared opcodeCounts2 with itself");
		}

		if(compareCounts(opcodeCounts3, opcodeCounts3)){
			System.out.println("Properly compared opcodeCounts3 with itself");
		}else{
			System.err.println("IMPROPERLY compared opcodeCounts3 with itself");
		}


		if(compareCounts(opcodeCounts1, opcodeCounts2)){
			System.out.println("Properly compared opcodeCounts1 and opcodeCounts2");
		}else{
			System.err.println("IMPROPERLY compared opcodeCounts1 and opcodeCounts2");
		}

		if(!compareCounts(opcodeCounts1, opcodeCounts3)){
			System.out.println("Properly compared opcodeCounts1 and opcodeCounts3");
		}else{
			System.err.println("IMPROPERLY compared opcodeCounts1 and opcodeCounts3");
		}

		if(compareCounts(opcodeCounts2, opcodeCounts1)){
			System.out.println("Properly compared opcodeCounts2 and opcodeCounts1");
		}else{
			System.err.println("IMPROPERLY compared opcodeCounts2 and opcodeCounts1");
		}

		if(!compareCounts(opcodeCounts2, opcodeCounts3)){
			System.out.println("Properly compared opcodeCounts2 and opcodeCounts3");
		}else{
			System.err.println("IMPROPERLY compared opcodeCounts2 and opcodeCounts3");
		}

		if(!compareCounts(opcodeCounts3, opcodeCounts1)){
			System.out.println("Properly compared opcodeCounts3 and opcodeCounts1");
		}else{
			System.err.println("IMPROPERLY compared opcodeCounts3 and opcodeCounts1");
		}

		if(!compareCounts(opcodeCounts3, opcodeCounts2)){
			System.out.println("Properly compared opcodeCounts3 and opcodeCounts2");
		}else{
			System.err.println("IMPROPERLY compared opcodeCounts3 and opcodeCounts2");
		}
	}

	/**
	 * Counts for constructions of specific array types.
	 */
	private transient Map<ArrayCreation, Long> arrayCreationCounts = null;

	/**
	 * A {@link UUID} that is linked to the method calling the method that
	 * calls the protocol function. Used with {@link #methodExecutionID} to construct a CCT.
	 */
	private UUID callerID = null;

	/**
	 * TODO
	 */
	private transient List<Object> characterisations = null;

	/**
	 * TODO
	 */
	private transient List<String> characterisationTitles = null;

	/**
	 * TODO
	 */
	private transient List<Integer> characterisationTypes = null;


	/**
	 * If this counting result describes "forced inlining",
	 * this field describes the earliest start of any of the inlined methods.
	 * Note that forced inlining may start *after* this value,
	 * since the reporting of the (forced-inlined) method can have happened
	 * *after* forced inlining was switched on.
	 */
	public long forcedInlining_earliestStartOfInlinedMethod;

	private boolean invariantMethodsAreInlined = false;

	/**
	 * This Map contains counts of method invocations, where the key is the
	 * method signature, the value is the invocation count.
	 */
	private SortedMap<String, Long> methodCallCounts = null;

	/**
	 * The timestamp which marks the beginning of execution (i.e. run)
	 * of the method for which this CountingResult holds bytecode counts.
	 */
	private long methodInvocationBeginning;

	/**
	 * The timestamp which was set immediately before this method called
	 * the CountingResultCollector. In other words, this is
	 * <b>approximately</b> the time when the method execution was finished.
	 */
	private long reportingTime;

	/**
	 * This array contains the counts of elementary bytecode instructions.
	 * The array index equals the opcode of the instruction.
	 */
	private long[] opcodeCounts;
	
	/**
	 * A {@link UUID} that is linked to the method calling the protocol function.
	 * Used with {@link #callerID} to construct a CCT.
	 */
	private UUID methodExecutionID;

	/**
	 * The name of the method whose execution was counted.
	 */
	private String qualifiedMethodName;

	/**
	 * A {@link UUID} that is linked to a request. This is used to keep track
	 * of execution sequences when dealing with parallel execution.
	 */
	private UUID requestID;

	/**
	 * Total count of all opcodes, except the four INVOKE* opcodes
	 */
	private Long totalCountExclInvokes;

	/**
	 * Total count of all opcodes, including the four INVOKE* opcodes
	 */
	private Long totalCountInclInvokes;

	public CountingResultBase(//TODO make sure the instructions are "full", even if some instruction counts are zero
			String qualifyingMethodName,
			long methodInvocationBeginning,
			long methodReportingTime,
			long[] opcodeCounts,
			SortedMap<String, Long> methodCallCounts) {
		this(//TODO make sure the instructions are "full", even if some instruction counts are zero
				null,null,null,
				qualifyingMethodName,
				methodInvocationBeginning,
				methodReportingTime,
				
				opcodeCounts,
				methodCallCounts,
				null);
	}
	
	/**
	 * Copy constructor.
	 * @param src Attributes of the {@link CountingResultBase} will be copied 
	 * in the construction of the new {@link CountingResultBase}.
	 */
	public CountingResultBase(final CountingResultBase src) {
		this.set(src);
	}

	/**
	 * Apply all properties from the given result to this result.
	 * @param src Result to copy from.
	 */
	public void set(final CountingResultBase src) {
		Map<ArrayCreation, Long> copyOfArrayCreationCounts = null;
		
		if(src.arrayCreationCounts != null) {
			copyOfArrayCreationCounts = new HashMap<ArrayCreation, Long>(src.arrayCreationCounts);
		}
		
		this.setRequestID(src.getRequestID());
		this.setMethodExecutionID(src.getMethodExecutionID());
		this.setCallerID(src.getCallerID());
		this.setQualifiedMethodName(src.qualifiedMethodName);
		this.methodInvocationBeginning = src.methodInvocationBeginning;
		this.reportingTime = src.reportingTime;
		this.opcodeCounts = Arrays.copyOf(src.opcodeCounts, src.opcodeCounts.length);
		this.methodCallCounts = new TreeMap<String,Long>(src.methodCallCounts);
		this.arrayCreationCounts = copyOfArrayCreationCounts;
		this.setThreadId(src.threadId);
		this.setIndexOfRangeBlock(src.getIndexOfRangeBlock());
		this.setCharacterisations(src.characterisations);
		this.setCharacterisationTitles(src.getCharacterisationTitles());
		this.setCharacterisationTypes(src.getCharacterisationTypes());
		this.setInvariantMethodsAreInlined(src.invariantMethodsAreInlined);
	}
	
	/**
	 * Internal field which is using for "lazy computation"
	 */
	private boolean totalCountsAlreadyComputed = false;

	/**
	 * If range blocks were used, this is the index of the range block in the
	 * method ({@link #qualifiedMethodName}).
	 * Otherwise this is -1;
	 */
	private int indexOfRangeBlock;

	/** 
	 * Id of the thread from which the result was reported.
	 * @see Thread#getId()
	 */
	protected long threadId;

	/** This constructor passes the arguments to the corresponding fields;
	 * the five fields that do not appear (this.characterisations,
	 * this.characterisationTitles, this.characterisationTypes and
	 * this.totalCountExclInvokes / this.totalCountInclInvokes) are
	 * initialised to empty collections (the first three) or set to 0
	 * (the last two).
	 * @param requestID
	 * @param ownID
	 * @param callerID
	 * @param qualifyingMethodName
	 * @param methodInvocationBeginning
	 * @param methodReportingTime
	 * @param opcodeCounts
	 * @param methodCallCounts
	 * @param arrayCreationCounts
	 */
	public CountingResultBase(//TODO make sure the instructions are "full", even if some instruction counts are zero
			UUID requestID,
			UUID ownID,
			UUID callerID,
			
			String qualifyingMethodName,
						
			long methodInvocationBeginning,
			long methodReportingTime,
			
			long[] opcodeCounts,
			SortedMap<String, Long> methodCallCounts,
			
			Map<ArrayCreation, Long> arrayCreationCounts) {
		this.setRequestID(requestID);
		this.setMethodExecutionID(ownID);
		this.setCallerID(callerID);
		this.setThreadId(-1);
		
		this.arrayCreationCounts = arrayCreationCounts;
		
		this.indexOfRangeBlock = -1;
		this.methodCallCounts = methodCallCounts;
		this.methodInvocationBeginning = methodInvocationBeginning;
		this.reportingTime = methodReportingTime;
		assert(opcodeCounts.length==MAX_OPCODE);
		this.opcodeCounts = opcodeCounts;
		this.qualifiedMethodName = qualifyingMethodName; //should be a PRIVATE setter
		this.totalCountExclInvokes = 0L;
		this.totalCountInclInvokes = 0L;

		this.characterisations = null; //now, methods modifying characterisations must check for non-nullness //used to be new ArrayList<Object>(0);//this is eating up memory...
		this.characterisationTitles = null; //now, methods modifying characterisations must check for non-nullness //used to be new ArrayList<String>(0);
		this.characterisationTypes = null; //now, methods modifying characterisations must check for non-nullness //used to be new ArrayList<Integer>(0);

//		this.computeTotalOpcodeCounts();
	}
	/**
	 * Construct result with fields set to 0/null;
	 */
	public CountingResultBase() {
		this.setRequestID(null);
		this.setMethodExecutionID(null);
		this.setCallerID(null);
		this.setThreadId(-1);
		
		this.arrayCreationCounts = null;
		
		this.indexOfRangeBlock = -1;
		this.methodCallCounts = null;
		this.methodInvocationBeginning = 0;
		this.reportingTime = 0;
		this.opcodeCounts = null;
		this.qualifiedMethodName = null; //should be a PRIVATE setter
		this.totalCountExclInvokes = 0L;
		this.totalCountInclInvokes = 0L;

		this.characterisations = null; //now, methods modifying characterisations must check for non-nullness //used to be new ArrayList<Object>(0);//this is eating up memory...
		this.characterisationTitles = null; //now, methods modifying characterisations must check for non-nullness //used to be new ArrayList<String>(0);
		this.characterisationTypes = null; //now, methods modifying characterisations must check for non-nullness //used to be new ArrayList<Integer>(0);
	}

	/** Adds the counts of this {@link CountingResultBase} instance to
	 * the counting results
	 * of the {@link CountingResultBase} instance given as parameter
	 * @param toBeAdded {@link CountingResultBase} instance whose counts are to be added
	 */
	@SuppressWarnings("dep-ann")
	public synchronized void add(CountingResultBase toBeAdded){
		CountingResultBase skeletonResult = add(this,toBeAdded);
		this.methodCallCounts = skeletonResult.getMethodCallCounts();
		this.opcodeCounts = skeletonResult.getOpcodeCounts();
		this.arrayCreationCounts = skeletonResult.getArrayCreationCounts();
		skeletonResult = null;
	}

	/** Adds the counts of this {@link CountingResultBase} instance to
	 * the counting results
	 * of the {@link CountingResultBase} instance given as parameter
	 * @param toBeAdded {@link CountingResultBase} instance whose counts are to be added
	 */
	@SuppressWarnings("dep-ann")
	public synchronized void add_methodsInstructionsOnly(CountingResultBase toBeAdded){
		CountingResultBase skeletonResult = addMethodsAndInstructionsOnly(this,toBeAdded);
		this.methodCallCounts = skeletonResult.getMethodCallCounts();
		this.opcodeCounts = skeletonResult.getOpcodeCounts();
		skeletonResult = null;
	}

	/** TODO document
	 * @param characterisationTitle
	 * @param characterisationType
	 * @param characterisationValue
	 */
	public void addCharacterisation(
			String characterisationTitle,
			Integer characterisationType,
			Object characterisationValue){
		if(this.characterisationTitles==null){
			this.characterisationTitles = new ArrayList<String>();
		}
		if(this.characterisationTypes==null){
			this.characterisationTypes = new ArrayList<Integer>();
		}
		if(this.characterisations==null){
			this.characterisations = new ArrayList<Object>();
		}
		this.characterisationTitles.add(characterisationTitle);
		this.characterisationTypes.add(characterisationType);
		this.characterisations.add(characterisationValue);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public CountingResultBase clone(){ //TODO fix/test/coverage-test this!
		CountingResultBase copy = null;

		try {
			copy = (CountingResultBase) super.clone();
		} catch (CloneNotSupportedException e) {
			// object.clone() cannot fail
			return null;
		}
		Map<ArrayCreation, Long> copyOfArrayCreationCounts = null;
		
		if(this.arrayCreationCounts != null) {
			copyOfArrayCreationCounts = new TreeMap<ArrayCreation, Long>(this.arrayCreationCounts);
		}
		
		copy.setRequestID(this.getRequestID());
		copy.setMethodExecutionID(this.getMethodExecutionID());
		copy.setCallerID(this.getCallerID());
		copy.setQualifiedMethodName(this.qualifiedMethodName);
		copy.methodInvocationBeginning = this.methodInvocationBeginning;
		copy.reportingTime = this.reportingTime;
		copy.opcodeCounts = Arrays.copyOf(this.opcodeCounts, this.opcodeCounts.length);
		copy.methodCallCounts = new TreeMap<String,Long>(this.methodCallCounts);
		copy.arrayCreationCounts = copyOfArrayCreationCounts;
		copy.setThreadId(this.threadId);
		return copy;
	}

	/**
	 * TODO
	 * @throws Exception
	 */
	private synchronized void computeTotalOpcodeCounts() {
		long tentativeNewTotalCountInclInvokes = 0L;
		long tentativeNewTotalCountExclInvokes = 0L;
		long prevNewTotalCountInclInvokes = 0L;
		long prevNewTotalCountExclInvokes = 0L;
		for(int i = 0; i < MAX_OPCODE; i++){
			tentativeNewTotalCountInclInvokes =
				prevNewTotalCountInclInvokes
				+this.opcodeCounts[i];
			if(tentativeNewTotalCountInclInvokes<prevNewTotalCountInclInvokes){
				throw new RuntimeException("Overflow detected while " +
						"computing total opcode counts INCL invoke*");
			}
			prevNewTotalCountInclInvokes = tentativeNewTotalCountInclInvokes;
			if(i==IAllJavaOpcodes.INVOKEINTERFACE
					|| i==IAllJavaOpcodes.INVOKESPECIAL
					|| i==IAllJavaOpcodes.INVOKESTATIC
					|| i==IAllJavaOpcodes.INVOKEVIRTUAL){
				//not counting them
			}else{
				tentativeNewTotalCountExclInvokes =
					prevNewTotalCountExclInvokes
					+opcodeCounts[i];
				if(tentativeNewTotalCountExclInvokes<prevNewTotalCountExclInvokes){
					throw new RuntimeException("Overflow detected while " +
							"computing total opcode counts EXCL invoke*");
				}
				prevNewTotalCountExclInvokes = tentativeNewTotalCountExclInvokes;
			}
		}
		this.totalCountExclInvokes = prevNewTotalCountExclInvokes;
		this.totalCountInclInvokes = prevNewTotalCountInclInvokes;
		this.totalCountsAlreadyComputed = true;
	}
	
	/**
	 * @return A {@link UUID} that is linked to the method calling the method that
	 * calls the protocol function. Used with {@link #getMethodExecutionID()} to construct a CCT.
	 */
	public UUID getCallerID() {
		return callerID;
	}

	/**
	 * TODO
	 * @return TODO
	 */
	public List<Object> getCharacterisations() {
		return characterisations;
	}

	/**
	 * TODO
	 * @return TODO
	 */
	public List<String> getCharacterisationTitles() {
		return characterisationTitles;
	}

	/**
	 * TODO
	 * @return TODO
	 */
	public List<Integer> getCharacterisationTypes() {
		return characterisationTypes;
	}

	/**
	 * Simple getter for method call counts.
	 * @return A {@link HashMap} were the method name is mapped to the number
	 * of calls of that method.
	 */
	public SortedMap<String, Long> getMethodCallCounts() {
		return methodCallCounts;
	}

	/** (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.execution.IFullCountingResult#getMethodCount(java.lang.String)
	 */
	public Long getMethodCount(String methodName){
		return getMethodCountByString(methodName);
	}

	/** (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.execution.IFullCountingResult#getMethodCountByString(java.lang.String)
	 */
	public Long getMethodCountByString(String methodName){
		Long count = this.methodCallCounts.get(methodName);
		if(count==null){
			return new Long(NO_COUNT_AVAILABLE);
		}
		return count;
	}

	/** (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.execution.IFullCountingResult#getMethodInvocationBeginning()
	 */
	public long getMethodInvocationBeginning() {
		return methodInvocationBeginning;
	}

	/** (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.execution.IFullCountingResult#getReportingTime()
	 */
	public long getReportingTime() {
		return reportingTime;
	}

	/**
	 * Simple getter
	 * @return Counts for array constructions when recording was enabled; <code>null</code> else.
	 */
	public Map<ArrayCreation, Long> getArrayCreationCounts() {
		return arrayCreationCounts;
	}

	/** (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.execution.IFullCountingResult#getOpcodeCount(int)
	 */
	public Long getOpcodeCount(int opcode){
		return getOpcodeCountByInteger(opcode);
	}

	/** (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.execution.IFullCountingResult#getOpcodeCount(java.lang.String)
	 */
	public Long getOpcodeCount(String opcode){
		return getOpcodeCountByString(opcode);
	}

	/** (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.execution.IFullCountingResult#getOpcodeCountByInteger(int)
	 */
	public Long getOpcodeCountByInteger(int opcode){
		Long count = this.opcodeCounts[opcode];
		if(count==null){
			return new Long(NO_COUNT_AVAILABLE);
		}
		return count;
	}

	/** (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.execution.IFullCountingResult#getOpcodeCountByString(java.lang.String)
	 */
	public Long getOpcodeCountByString(String opcode){
		Long count = this.opcodeCounts[FullOpcodeMapper.getOpcodeOfMnemonic(opcode)];
		if(count==null){
			return new Long(NO_COUNT_AVAILABLE);
		}
		return count;
	}

	/** (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.execution.IFullCountingResult#getOpcodeCounts()
	 */
	public long[] getOpcodeCounts() {
		return opcodeCounts;
	}

	/**
	 * @return The ID of the method of the result.
	 */
	public UUID getMethodExecutionID() {
		return methodExecutionID;
	}

	/** (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.execution.IFullCountingResult#getQualifiedMethodName()
	 */
	public String getQualifiedMethodName() {
		return qualifiedMethodName;
	}

	/**
	 * @return the requestID
	 */
	public UUID getRequestID() {
		return requestID;
	}

	/**
	 * @return Id of the thread from which the result was reported.
	 * @see Thread#getId()
	 */
	public long getThreadId() {
		return threadId;
	}

	/** (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.execution.IFullCountingResult#getTotalCount(boolean)
	 */
	public synchronized Long getTotalCount(boolean includeInvokeOpcodes) {
		if(!totalCountsAlreadyComputed){
			this.computeTotalOpcodeCounts();
		}
		if(includeInvokeOpcodes){
			return totalCountInclInvokes;
		}
		return totalCountExclInvokes;
	}

	/**
	 * @return totalCountExclInvokes
	 */
	public Long getTotalCountExclInvokes() {
		return totalCountExclInvokes;
	}

	/**
	 * @return totalCountInclInvokes
	 */
	public Long getTotalCountInclInvokes() {
		return totalCountInclInvokes;
	}

	/**
	 * @return Value of the flag indicating whether called methods are inlined 
	 * (already part of bytecode counts)
	 */
	public boolean isInvariantMethodsAreInlined() {
		return invariantMethodsAreInlined;
	}

	/**
	 * Returns the total number of method invocations
	 * @return the total number of method invocations
	 */
	public synchronized Long methodCountSum() {
		return getTotalCount(true) - getTotalCount(false);
	}

	/**
	 * Does not perform any plausibility checks
	 * @param methodCounts
	 * @return old method counts
	 */
	public SortedMap<String, Long> overwriteMethodCallCounts(SortedMap<String, Long> methodCounts){//TODO consider warning/logging
		SortedMap<String, Long> methodCountsOld = this.methodCallCounts;
		this.methodCallCounts = methodCounts;
		return methodCountsOld;
	}

	/**
	 * Does not perform any plausibility checks
	 * @param newOpcodeCounts
	 * @return old opcode counts (before overwriting)
	 */
	public long[] overwriteOpcodeCounts(long[] newOpcodeCounts){
		long[] oldOpcodeCounts = this.opcodeCounts;
		this.opcodeCounts = newOpcodeCounts;
		return oldOpcodeCounts;
	}

	/**
	 * Does not affect parametric counts, etc.
	 */
	public void resetMethodAndInstructionCounts(){
		this.opcodeCounts = new long[MAX_OPCODE];
		this.methodCallCounts = new TreeMap<String, Long>();
	}

	/**
	 * @param callerID A {@link UUID} that is linked to the method calling the method that
	 * calls the protocol function. Used with {@link #getMethodExecutionID()} to construct a CCT.
	 * @return The previously set caller id.
	 */
	public UUID setCallerID(UUID callerID) {
		UUID oldCallerId = this.callerID;
		this.callerID = callerID;
		return oldCallerId;
	}

	/**
	 * @param characterisations
	 * @return The previously set characterisations.
	 */
	public List<Object> setCharacterisations(List<Object> characterisations) {
		List<Object> oldCharacterisations = this.characterisations;
		this.characterisations = characterisations;
		return oldCharacterisations;
	}

	/**
	 * @param characterisationTitles
	 * @return The previously set characterisationTitles.
	 */
	public List<String> setCharacterisationTitles(List<String> characterisationTitles) {
		List<String> oldCharacterisationTitles = this.characterisationTitles;
		this.characterisationTitles = characterisationTitles;
		return oldCharacterisationTitles;
	}

	/**
	 * @param characterisationTypes
	 */
	public void setCharacterisationTypes(List<Integer> characterisationTypes) {
		this.characterisationTypes = characterisationTypes;
	}

	/**
	 * Flag indicating whether called methods are inlined (already part of bytecode counts)
	 * @param invariantMethodsAreInlined
	 */
	public void setInvariantMethodsAreInlined(boolean invariantMethodsAreInlined) {
		this.invariantMethodsAreInlined = invariantMethodsAreInlined;
	}

	/**
	 * @param methodInvocationBeginning
	 */
	public void setMethodInvocationBeginning(long methodInvocationBeginning) {
		this.methodInvocationBeginning = methodInvocationBeginning;
	}

	/**
	 * TODO consider adding a logger to CountingResult;
	 * @param reportingTime
	 */
	public void setReportingTime(long reportingTime) {
		if(this.reportingTime==0){
			log.fine("Method reporting time "+this.reportingTime+
					" about to be overwritten with "+reportingTime);
			//System.err.println("Method reporting time "+this.reportingTime+" about to be overwritten with "+reportingTime);
		}
		this.reportingTime = reportingTime;
	}
	
	/**
	 * @see #getArrayCreationCounts()
	 * @param arrayCreationCounts array creation counts
	 */
	public void setArrayCreationCounts(Map<ArrayCreation, Long> arrayCreationCounts) {
		this.arrayCreationCounts = arrayCreationCounts;
	}

	/** (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.execution.ISimpleCountingResult#setOpcodeCount(int, java.lang.Long)
	 */
	public void setOpcodeCount(int opcode, Long count) {
		this.opcodeCounts[opcode] = count;
		this.totalCountsAlreadyComputed = false;
	}
	
	/**
	 * @param opcodeCounts Execution counts per opcode.
	 * @see #getOpcodeCounts()
	 */
	public void setOpcodeCounts(long[] opcodeCounts) {
		this.opcodeCounts = opcodeCounts;
	}

	/**
	 * @param methodID The ID of the method of the result.
	 */
	public void setMethodExecutionID(UUID methodID) {
		this.methodExecutionID = methodID;
	}
	
	/**
	 * @param qualifiedMethodName {@link de.uka.ipd.sdq.ByCounter.execution.IFullCountingResult#getQualifiedMethodName()}
	 */
	public void setQualifiedMethodName(String qualifiedMethodName) {
		this.qualifiedMethodName = qualifiedMethodName;
	}

	/**
	 * @param requestID the requestID to set
	 */
	public void setRequestID(UUID requestID) {
		this.requestID = requestID;
	}

	/**
	 * @param threadId Id of the thread from which the result was reported.
	 * @see Thread#getId()
	 */
	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}

	public void setTotalCountExclInvokes(Long totalCountExclInvokes) {
		this.totalCountExclInvokes = totalCountExclInvokes;
		this.totalCountsAlreadyComputed = true; //TODO: not quite true - not portable
	}

	public void setTotalCountInclInvokes(Long totalCountInclInvokes) {
		this.totalCountInclInvokes = totalCountInclInvokes;
		this.totalCountsAlreadyComputed = true; //TODO: not quite true - not portable
	}

	/**
	 * TODO replace return type to indicate precise location of difference (using the sum 1+2+4+8+16+etc.)
	 * @param vr
	 * @return True, if counting results equal (shallow).
	 */
	public boolean shallowEquals(CountingResultBase vr){
		if(this.invariantMethodsAreInlined!=vr.invariantMethodsAreInlined){
			System.out.println("CountingResult.shallowEquals: == on invariantMethodsAreInlined: "+this.invariantMethodsAreInlined+" vs "+vr.invariantMethodsAreInlined);
			return false;
		}
		if(this.qualifiedMethodName!=vr.qualifiedMethodName){
			System.out.println("CountingResult.shallowEquals: == on qualifyingMethodName: "+this.qualifiedMethodName+" vs "+vr.qualifiedMethodName);
			return false;
		}
		if(this.totalCountExclInvokes!=vr.totalCountExclInvokes){
			System.out.println("CountingResult.shallowEquals: == on totalCountExclInvokes: "+this.totalCountExclInvokes+" vs "+vr.totalCountExclInvokes);
			return false;
		}
		if(this.totalCountInclInvokes!=vr.totalCountInclInvokes){
			System.out.println("CountingResult.shallowEquals: == on totalCountInclInvokes returns false: "+this.totalCountInclInvokes+" vs "+vr.totalCountInclInvokes);
			return false;
		}

		if(!this.methodCallCounts.equals(vr.methodCallCounts)){
			System.out.println("CountingResult.shallowEquals: equals on methodCallCounts returns false"/*+this.methodCallCounts+" vs "+vr.methodCallCounts*/);
			return false;
		}

		//TODO care for the case of zeros vs. non-figurement...
		if(this.opcodeCounts.equals(vr.opcodeCounts)){
			//could say "return true;" here directly, because no other check is following
		}else{
			if(!compareCounts(opcodeCounts,vr.opcodeCounts)){
				System.out.println("CountingResult.shallowEquals: " +
						"equals on opcodeCounts returns false"
						/*+this.opcodeCounts+" vs "+vr.opcodeCounts*/);
				return false;
			}
//			else{
				//could say "return true;" here directly, because no other check is following
				//
				//everything is fine despite the fact that "equals" returned false:
				//this is due to the possibility that CountingResult is not
				//obliged to explicitly list zero-counted opcodes; they may
				//be skipped internally because values are stored using TreeMaps
				//
				//TODO instead of this dirty hack, consider enforcing fully
				//complete TreeMaps (this may result in ca. 200 additional
				//TreeMap entries, though...
//			}
		}
//		if(!Arrays.equals(this.arrayCreationCounts, vr.arrayCreationCounts)){
//			return false;
//		}
//		if(!Arrays.equals(this.arrayCreationDimensions, vr.arrayCreationDimensions)){
//			return false;
//		}
//		if(!Arrays.equals(this.arrayCreationTypeInfo, vr.arrayCreationTypeInfo)){
//			return false;
//		}
//		if(!this.characterisations.equals(vr.characterisations)){
//			return false;
//		}
//		if(!this.characterisationTitles.equals(vr.characterisationTitles)){
//			return false;
//		}
		return true;
	}

	/** (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){ //TODO add missing fields (data type, ...)
		StringBuffer sb = new StringBuffer();
		sb.append("\n"+
				  "      "+this.getClass().getSimpleName()+" (hash code: "+this.hashCode()+")\n");
		sb.append("      > Method name     : "+this.qualifiedMethodName+ " (Own UUID: " + this.methodExecutionID + ", threadId: " + this.threadId + ")\n");
		sb.append("      > Method duration : "+(this.reportingTime-this.methodInvocationBeginning)+
				"(start: "+this.methodInvocationBeginning+", end: "+this.reportingTime+")\n");
		sb.append("      > Opcode counts   : "+Arrays.toString(this.opcodeCounts)+"\n");
		sb.append("      > Method counts   : "+this.methodCallCounts+"\n");
//		sb.append("      > Method input    : "+this.inputParams+"\n");
//		sb.append("      > Method output   : "+this.outputParams+"\n");
//		sb.append("      > Array creations : "+this.arrayCreationCounts+"\n");
//		sb.append("      > Array dimensions: "+this.arrayCreationDimensions+"\n");
//		sb.append("      > Array type infos: "+this.arrayCreationTypeInfo+"\n");
//		sb.append("      > Sect. opc. cnts : "+this.sectionInstCounts+"\n");
//		sb.append("      > Sect. meth. cnts: "+this.sectionMethCounts+"\n");
		return sb.toString();
	}
	
	/**
	 * @param indexOfRangeBlock the indexOfRangeBlock to set
	 */
	public void setIndexOfRangeBlock(int indexOfRangeBlock) {
		this.indexOfRangeBlock = indexOfRangeBlock;
	}

	/**
	 * @return If range blocks were used, this is the index of the range block 
	 * in the method ({@link #getQualifiedMethodName()}).
	 * Otherwise this is -1;
	 */
	public int getIndexOfRangeBlock() {
		return indexOfRangeBlock;
	}

	public long getForcedInlining_earliestStartOfInlinedMethod() {
		return this.forcedInlining_earliestStartOfInlinedMethod;
	}

	public boolean isTotalCountsAlreadyComputed() {
		return this.totalCountsAlreadyComputed;
	}

	/**
	 * Return an appropriate number of tabs to follow the given string.
	 * This is for log formatting purposes only.
	 * @param str String after which the tabs shall follow
	 * @param maxNumTabs maximum number of tabs to return.
	 * @return A string containing a fitting number of tabs.
	 */
	private static synchronized String getTabs(String str, int maxNumTabs) {
		StringBuilder tabs = new StringBuilder();
		for(int i = maxNumTabs; i > 0; i--) {
			if(str.length() < 8*i) {//TODO encode tab width variably?
				tabs.append("\t");
			} else {
				break;
			}
		}
		return tabs.toString();
	}

	/**
	 * Print a log message that reports the result, listing all counts and
	 * data that was collected.
	 * @param printZeros When true, opcodes with an execution count of 0 are printed.
	 * @param vertically When true, print as one opcode/method count per line.
	 * @return The string with the logged message.
	 */
	public synchronized String logResult(
			boolean printZeros, //eigentlich 3 Abstufungen: gar nicht; wie gespeichert; alle opcodes (auch wenn nicht gespeichert)
			boolean vertically //TODO currently ignored
			){
		return logResult(printZeros, vertically, Level.INFO);
	}

	/**
	 * Print a log message that reports the result, listing all counts and
	 * data that was collected.
	 * @param printZeros When true, opcodes with an execution count of 0 are printed.
	 * @param vertically When true, print as one opcode/method count per line.
	 * @param loggingLevel {@link Level} used to log the message.
	 * @return The string with the logged message.
	 */
	public synchronized String logResult(
			boolean printZeros, //eigentlich 3 Abstufungen: gar nicht; wie gespeichert; alle opcodes (auch wenn nicht gespeichert)
			boolean vertically,
			Level loggingLevel
			) {
		StringBuffer sb = new StringBuffer();
		sb.append("\n==START========= Logging CountingResult ================");
		sb.append(NEWLINE);
		String qualifyingMethodName = getQualifiedMethodName();
		if(qualifyingMethodName==null || qualifyingMethodName.equals("")) {
			log.severe("Qualifying method name is null or empty, EXITING");
			sb.append("== END ========= Logging CountingResult ================");
			sb.append(NEWLINE);
			log.info(sb.toString());
			return sb.toString();
		}
		sb.append("qualifyingMethodName: ");
		sb.append(qualifyingMethodName);
		sb.append(NEWLINE);
		sb.append("requestID: ");
		sb.append(getRequestID());
		sb.append(", ownID: ");
		sb.append(getMethodExecutionID());
		sb.append(", callerID: ");
		sb.append(getCallerID());
		sb.append(", threadId: ");
		sb.append(getThreadId());
		sb.append(NEWLINE);
		if (getIndexOfRangeBlock() == -1) {
			sb.append("The whole method was measured (cr.getIndexOfRangeBlock() == -1 in CountingResultCollector.logResult)");
			sb.append(NEWLINE);
		} else {
			sb.append("Section number ");
			sb.append(getIndexOfRangeBlock());
			sb.append(" was measured.");
			sb.append(NEWLINE);
		}
//		if(cr==null){
//			log.severe("The CountingResult to log is null - nothing to do, returning immediately.");
//			log.info("== END ========= Logging CountingResult ================");
//			return;
//		}

		long[] opcodeCounts = getOpcodeCounts();
		if(opcodeCounts == null) {
			log.severe("Opcode counts is null... EXITING");
			sb.append("== END ========= Logging CountingResult ================");
			sb.append(NEWLINE);
			log.info(sb.toString());
			return sb.toString();
		}

		SortedMap<String, Long> methodCallCounts = getMethodCallCounts();
		if(methodCallCounts == null) {
			log.severe("Method counts hashmap is null... EXITING");
			sb.append("== END ========= Logging CountingResult ================");
			sb.append(NEWLINE);
			log.info(sb.toString());
			return sb.toString();
		}

		long time = getMethodInvocationBeginning();
		if(time<0) {
			log.severe("Wrong time: "+time);//TODO which kind of time is this?
			sb.append("== END ========= Logging CountingResult ================");
			sb.append(NEWLINE);
			log.info(sb.toString());
			return sb.toString();
		}

		List<ICountingResultWriter> resultWriters = CountingResultCollector.getInstance().getAllResultWriters();
		if(resultWriters.size()>0){
			log.fine("Logging CountinResult using "+resultWriters.size()+
					" registered result writers");
			for(ICountingResultWriter rw : resultWriters){
				rw.writeResultToFile(this, false, -1);//TODO make this better/parameterised
			}
		}

		// make sure DisplayOpcodes does not interfere with the output...?
		final OpcodeLogInfo opcodeLogInfo = this.appendOpcodesToStringBuffer(sb, printZeros, vertically);
		final MethodLogInfo methodLogInfo = this.appendMethodCallsToStringBuffer(sb, printZeros, vertically);
		this.appendArrayInfoToStringBuffer(sb, vertically);
		
		sb.append("====================================================");
		sb.append(NEWLINE);
		sb.append(opcodeLogInfo.totalCountOfAllOpcodes);
		sb.append(" instruc. executions of ");
		sb.append(opcodeLogInfo.numberOfOpcodesWithNonzeroFrequencies);
		sb.append(" different opcodes were counted.");
		sb.append(NEWLINE);
		sb.append(methodLogInfo.totalCountOfAllMethods);
		sb.append(" methods invocations of ");
		sb.append(methodCallCounts.size());
		sb.append(" different signatures were counted, from "+methodLogInfo.classesContainingMethodSigs.size()+" classes.");
		sb.append(NEWLINE);
		sb.append(NEWLINE);
		int i=1;
		int approxNrOfJavaPlatformClasses = 0;
		StringBuffer sb2 = new StringBuffer();
		sb.append("API / platform classes: \n");
		for(String classs : methodLogInfo.classesContainingMethodSigs){
			if(classs.startsWith("java/") || classs.startsWith("javax/") || classs.startsWith("sun/")){
				approxNrOfJavaPlatformClasses++;
				sb.append("class "+i+": "+classs+"\n");
			}else{
				sb2.append("class "+i+": "+classs+"\n");
			}
			i++;
		}
		sb.append((methodLogInfo.classesContainingMethodSigs.size()-approxNrOfJavaPlatformClasses)+
				" are 'business' classes outside of the Java platform:\n");
		sb.append(sb2.toString());
		sb.append(NEWLINE);
		sb.append(NEWLINE);
		sb.append("== END ========= Logging CountingResult ================");
		sb.append(NEWLINE);
		String ret = sb.toString();
		log.log(loggingLevel, sb.toString());
		return ret;
	}

	/**
	 * This method is intended for logging.
	 * @param sb {@link StringBuffer} to append the output to.
	 * @param vertically When false, info will be printed in a single line.
	 */
	public void appendArrayInfoToStringBuffer(StringBuffer sb, 
			final boolean vertically) {
		// No checks here (but below!) for array results, because null is also
		// returned when array parameter recording is disabled.
		Map<ArrayCreation, Long> newArrayCounts = getArrayCreationCounts();
		// because null is a valid value for the array*Something* arrays,
		// we need to be carefull here.
		if(newArrayCounts != null) {
			for(Entry<ArrayCreation, Long> e : newArrayCounts.entrySet()) {
				sb.append("new array of type '");
				sb.append(e.getKey().getTypeDesc());
				sb.append("'");
				sb.append(", dim " + e.getKey().getNumberOfDimensions());
				sb.append(": ");
				sb.append(e.getValue());
				if(vertically) {
					sb.append(NEWLINE);
				} else {
					sb.append(", ");
				}
			}
		}
	}

	/**
	 * This method is intended for logging.
	 * @param sb {@link StringBuffer} to append the output to.
	 * @param printZeros When false, counts of 0 are not added to the output.
	 * @param vertically When false, methods will be printed in a single line.
	 * @return Total count of all methods.
	 */
	public MethodLogInfo appendMethodCallsToStringBuffer(StringBuffer sb,
			final boolean printZeros, 
			final boolean vertically) {
		MethodLogInfo result = new MethodLogInfo();
		Iterator<String> methodSigs = methodCallCounts.keySet().iterator();
		long currentMethodCount = 0;	// method count
		String currentSig, className;
		while(methodSigs.hasNext()) {
			currentSig = methodSigs.next();
			className = currentSig.split("\\.")[0];
			result.classesContainingMethodSigs.add(className);
			currentMethodCount = methodCallCounts.get(currentSig);
//			dataset.addValue(currentMethodCount, qualifyingMethodName+": methods", currentMethodSignature);
			sb.append(currentSig);
			sb.append(":");
			if(vertically) {
				String tabs = getTabs(currentSig + ":", 9);
				sb.append(tabs);
			}
			sb.append(currentMethodCount);
//			sb.append(" (class: "+className+")");
			if(vertically) {
				sb.append(NEWLINE);
			} else {
				sb.append(", ");
			}
			if(result.totalCountOfAllMethods + currentMethodCount<result.totalCountOfAllMethods){
				log.severe("OVERFLOW while adding method counts");
			}else{
				result.totalCountOfAllMethods += currentMethodCount;
			}
//			instrnames_texSB.append(currentMethodSignature+" & ");
//			instrcounts_texSB.append(currentMethodCount+" & ");
		}
//		instrnames_texSB.append("total \\\\");
//		instrcounts_texSB.append(totalCountOfAllMethods+" \\\\");		
		return result;
	}

	/**
	 * This method is intended for logging.
	 * @param sb {@link StringBuffer} to append the output to.
	 * @param printZeros When false, counts of 0 are not added to the output.
	 * @param vertically When false, opcodes will be printed in a single line.
	 * @return Counters increased when visiting the opcodes.
	 */
	public OpcodeLogInfo appendOpcodesToStringBuffer(StringBuffer sb, 
			final boolean printZeros,
			final boolean vertically) {
		String 	currentOpcodeString;	// opcode as string
		long 	currentOpcodeCount;		// opcode count

		OpcodeLogInfo opcodeLogInfo = new OpcodeLogInfo();
		for(int i = 0; i < CountingResultBase.MAX_OPCODE; i++) {
			currentOpcodeString = FullOpcodeMapper.getMnemonicOfOpcode(i);
			currentOpcodeCount 	= opcodeCounts[i];
//			dataset.addValue(currentOpcodeCount, qualifyingMethodName+": instructions", currentOpcodeString);
			if(currentOpcodeCount!=0 || printZeros){
				sb.append(currentOpcodeString);
				sb.append(":");
				if(vertically) {
					String tabs = getTabs(currentOpcodeString + ":", 2);
					sb.append(tabs);
				}
				sb.append(currentOpcodeCount);
				if(vertically) {
					sb.append(NEWLINE);
				} else {
					sb.append(", ");
				}
			}
			if((opcodeLogInfo.totalCountOfAllOpcodes+currentOpcodeCount)<opcodeLogInfo.totalCountOfAllOpcodes){
				log.severe("OVERFLOW while adding opcode counts... use BigInteger instead");
			}else{
				opcodeLogInfo.totalCountOfAllOpcodes += currentOpcodeCount;
				if(currentOpcodeCount>0){
					opcodeLogInfo.numberOfOpcodesWithNonzeroFrequencies++;
				}
			}
		}
		return opcodeLogInfo;
	}

	/** Compares {@link #getMethodInvocationBeginning()}.
	 * When {@link #getMethodInvocationBeginning()} is the same, but the objects 
	 * differ, this object is always smaller than the given object.
	 * @param o {@link IFullCountingResult} to compare to.*/
	public int compareTo(IFullCountingResult o) {
		Long long1 = new Long(methodInvocationBeginning);
		long1 = new Long(this.getMethodInvocationBeginning());
		Long long2 = new Long(o.getMethodInvocationBeginning());
		int compareInvBeginning = long1.compareTo(long2);
		if(compareInvBeginning == 0 && this.hashCode() != o.hashCode()) {
			return -1;
		}
		return compareInvBeginning;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.callerID == null) ? 0 : this.callerID.hashCode());
		result = prime
				* result
				+ (int) (this.forcedInlining_earliestStartOfInlinedMethod ^ (this.forcedInlining_earliestStartOfInlinedMethod >>> 32));
		result = prime * result + this.indexOfRangeBlock;
		result = prime * result
				+ (this.invariantMethodsAreInlined ? 1231 : 1237);
		result = prime
				* result
				+ ((this.methodCallCounts == null) ? 0 : this.methodCallCounts
						.hashCode());
		result = prime
				* result
				+ (int) (this.methodInvocationBeginning ^ (this.methodInvocationBeginning >>> 32));
		result = prime
				* result
				+ (int) (this.reportingTime ^ (this.reportingTime >>> 32));
		result = prime * result + Arrays.hashCode(this.opcodeCounts);
		result = prime * result
				+ ((this.methodExecutionID == null) ? 0 : this.methodExecutionID.hashCode());
		result = prime
				* result
				+ ((this.qualifiedMethodName == null) ? 0
						: this.qualifiedMethodName.hashCode());
		result = prime * result
				+ ((this.requestID == null) ? 0 : this.requestID.hashCode());
		result = prime * result
				+ (int) (this.threadId ^ (this.threadId >>> 32));
		result = prime
				* result
				+ ((this.totalCountExclInvokes == null) ? 0
						: this.totalCountExclInvokes.hashCode());
		result = prime
				* result
				+ ((this.totalCountInclInvokes == null) ? 0
						: this.totalCountInclInvokes.hashCode());
		result = prime * result
				+ (this.totalCountsAlreadyComputed ? 1231 : 1237);
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
		CountingResultBase other = (CountingResultBase) obj;
		if (this.callerID == null) {
			if (other.callerID != null)
				return false;
		} else if (!this.callerID.equals(other.callerID))
			return false;
		if (this.forcedInlining_earliestStartOfInlinedMethod != other.forcedInlining_earliestStartOfInlinedMethod)
			return false;
		if (this.indexOfRangeBlock != other.indexOfRangeBlock)
			return false;
		if (this.invariantMethodsAreInlined != other.invariantMethodsAreInlined)
			return false;
		if (this.methodCallCounts == null) {
			if (other.methodCallCounts != null)
				return false;
		} else if (!this.methodCallCounts.equals(other.methodCallCounts))
			return false;
		if (this.methodInvocationBeginning != other.methodInvocationBeginning)
			return false;
		if (this.reportingTime != other.reportingTime)
			return false;
		if (!Arrays.equals(this.opcodeCounts, other.opcodeCounts))
			return false;
		if (this.methodExecutionID == null) {
			if (other.methodExecutionID != null)
				return false;
		} else if (!this.methodExecutionID.equals(other.methodExecutionID))
			return false;
		if (this.qualifiedMethodName == null) {
			if (other.qualifiedMethodName != null)
				return false;
		} else if (!this.qualifiedMethodName
				.equals(other.qualifiedMethodName))
			return false;
		if (this.requestID == null) {
			if (other.requestID != null)
				return false;
		} else if (!this.requestID.equals(other.requestID))
			return false;
		if (this.threadId != other.threadId)
			return false;
		if (this.totalCountExclInvokes == null) {
			if (other.totalCountExclInvokes != null)
				return false;
		} else if (!this.totalCountExclInvokes
				.equals(other.totalCountExclInvokes))
			return false;
		if (this.totalCountInclInvokes == null) {
			if (other.totalCountInclInvokes != null)
				return false;
		} else if (!this.totalCountInclInvokes
				.equals(other.totalCountInclInvokes))
			return false;
		if (this.totalCountsAlreadyComputed != other.totalCountsAlreadyComputed)
			return false;
		return true;
	}
	
	
}
