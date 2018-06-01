package de.uka.ipd.sdq.ByCounter.test.framework.expectations;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.junit.Assert;

import de.uka.ipd.sdq.ByCounter.parsing.LineNumberRange;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;
import de.uka.ipd.sdq.ByCounter.utils.FullOpcodeMapper;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * Defines that a section is expected. It also defines what is inside it.
 * 
 * @author Florian Schreier
 */
public class SectionExpectation {

	/**
	 * This section's number.
	 */
	private final int sectionNumber;

	/**
	 * This section's line number range.
	 */
	private final LineNumberRange range;

	/**
	 * All expected opcodes. The key stands for the opcode number as specified in {@link Opcodes}, the value stands for
	 * the expected count number.
	 */
	private final Map<Integer, Long> opcodeExpectations;

	/**
	 * All expected method calls. The key stands for the method's name, the value stands for the expected count number.
	 */
	private final Map<String, Long> methodCallExpectations;
	
	/**
	 * All expected spawned threads resulting in parallel expectation. The order
	 * of spawning within this section is independent of the order of this list.
	 * The expectation is only that one thread is spawned for each
	 * {@link SectionExpectation} in the list.
	 */
	private List<SectionExpectation> parallelExpectations;
	
	/**
	 * Creates a new selection expectation without a section number and line number range.
	 */ 
	public SectionExpectation() {
		this(Expectation.SECTION_NUMBER_NOT_SET, null);
	}

	/**
	 * Creates a new selection expectation with an unknown line number range.
	 * 
	 * @param sectionNumber
	 *          The new section's number. Has to be greater or equal zero.
	 */
	public SectionExpectation(final int sectionNumber) {
		this(sectionNumber, null);
	}

	/**
	 * Creates a new selection expectation.
	 * 
	 * @param sectionNumber
	 *          The new section's number. Has to be greater or equal zero.
	 * @param range
	 *          The line number range of the new section. If the range is unknown, this should be <code>null</code>.
	 */
	public SectionExpectation(final int sectionNumber, final LineNumberRange range) {
		this.sectionNumber = sectionNumber;
		this.range = range;
		this.opcodeExpectations = new HashMap<Integer, Long>();
		this.methodCallExpectations = new HashMap<String, Long>();
		this.parallelExpectations = new LinkedList<SectionExpectation>();
	}

	/**
	 * Returns this section's number.
	 * 
	 * @return This section's number.
	 */
	protected int getSectionNumber() {
		return this.sectionNumber;
	}

	/**
	 * Returns this section's line number range.
	 * 
	 * @return This section's line number range if it is known. Otherwise <code>null</code>.
	 */
	protected LineNumberRange getRange() {
		return this.range;
	}

	/**
	 * Adds an opcode expectation to this section. Throws an {@link IllegalArgumentException} if you want to add an opcode
	 * twice.
	 * 
	 * @param opcode
	 *          An integer value representing an opcode.
	 * @param number
	 *          The expected number (How often this opcode should be counted.). Has to be greater than zero.
	 * @return This {@link SectionExpectation} object.
	 */
	public SectionExpectation add(final int opcode, final long number) {
		if (this.opcodeExpectations.containsKey(opcode)) {
			throw new IllegalArgumentException("Cannot add Opcode #" + opcode + " twice.");
		}
		if (opcode < 0 || opcode >= 202) {
			throw new IllegalArgumentException("Opcode number out of range. Was: " + opcode);
		}
		if (number <= 0) {
			throw new IllegalArgumentException("The value of number has to be greater than zero.");
		}
		
		this.opcodeExpectations.put(opcode, number);
		return this;
	}

	/**
	 * Adds a method call expectation to this section. Throws an {@link IllegalArgumentException} if you want to add a
	 * method call twice.
	 * 
	 * @param bytecodeDescriptor
	 *          A String representing a method as used in Java Bytecode.
	 * @param number
	 *          The expected number (How often this opcode should be counted.). Has to be greater than zero.
	 * @return This {@link SectionExpectation} object.
	 */
	public SectionExpectation add(final String bytecodeDescriptor, final long number) {
		if (this.opcodeExpectations.containsKey(bytecodeDescriptor)) {
			throw new IllegalArgumentException("Cannot store " + bytecodeDescriptor + " twice.");
		}
		if (number <= 0) {
			throw new IllegalArgumentException("The value of number has to be greater than zero.");
		}
		
		this.methodCallExpectations.put(bytecodeDescriptor, number);
		return this;
	}
	
	/**
	 * Adds a method call expectation to this section. Throws an {@link IllegalArgumentException} if you want to add a
	 * method call twice.
	 * 
	 * @param constructor
	 *          The constructor to add.
	 * @param number
	 *          The expected number (How often this opcode should be counted.). Has to be greater than zero.
	 * @return This {@link SectionExpectation} object.
	 */
	public SectionExpectation add(final Constructor<?> constructor, final long number) {
		MethodDescriptor desc = new MethodDescriptor(constructor);
		return this.add(desc.getCanonicalMethodName(), number);
	}
	
	/**
	 * Adds a method call expectation to this section. Throws an {@link IllegalArgumentException} if you want to add a
	 * method call twice.
	 * 
	 * @param method
	 *          The method to add.
	 * @param number
	 *          The expected number (How often this opcode should be counted.). Has to be greater than zero.
	 * @return This {@link SectionExpectation} object.
	 */
	public SectionExpectation add(final Method method, final long number) {
		MethodDescriptor desc = new MethodDescriptor(method);
		return this.add(desc.getCanonicalMethodName(), number);
	}

	/**
	 * Adds a method call expectation to this section. Throws an {@link IllegalArgumentException} if you want to add a
	 * method call twice.
	 * <p>
	 * Don't use this with constructors!
	 * 
	 * @param className
	 *          The canonical name of the class declaring the method.
	 * @param signature
	 *          A string containing a standard Java method signature (i.e.
	 *          <code>public static java.lang.String valueOf(java.lang.Object obj)</code> ). Object types need to be
	 *          specified with the full canonical name. Specifically, only the two tokens before the first '(', as well as
	 *          everything between '(' and ')' is evaluated. Whitespaces and qualifiers like "public", "static" are
	 *          ignored. Generic types may be omitted (and are ignored) so that "List" and "List<Integer>" are treated as
	 *          the same since bytecode signatures ignore generics. For method parameters, only one or two tokens are
	 *          allowed (example: "int[]" or "int[] abc"). It is advised to take the method declaration from sourcecode or
	 *          from documentation and only adapt it, if necessary. The thing that needs to be adapted is type names for
	 *          object types. So instead of giving the String "String myString", this has to be expanded to
	 *          "java.lang.String". Note that inner/nested classes need to be specified using the '$' symbol as in the
	 *          following example: <code>my.packagename.OutClass$InnerClass</code>.
	 * @param number
	 *          The expected number (How often this opcode should be counted.). Has to be greater than zero.
	 * @return This {@link SectionExpectation} object.
	 */
	public SectionExpectation add(final String className, final String signature, final long number) {
		if (number <= 0) {
			throw new IllegalArgumentException("The value of number has to be greater than zero.");
		}
		
		return this.add(this.signatureToDescriptor(className, signature), number);
	}
	
	/**Adds a section expectation for a thread spawned within the observed element.
	 * @param expectation The section expectation for the thread.
	 * @return The {@link SectionExpectation} with the parallel expectations.
	 */
	public SectionExpectation addParallel(SectionExpectation expectation) {
		this.parallelExpectations.add(expectation);
		return this;
	}
	
	/**
	 * Creates the spawn point for parallel expectations, i.e. results from 
	 * threads that where spawned from a single observed element.
	 * @param threads The expectations for each parallel result.
	 * @return The {@link SectionExpectation} with added threads.
	 */
	public SectionExpectation addParallel(SectionExpectation... threads) {
		for (SectionExpectation expectation : threads) {
			this.parallelExpectations.add(expectation);
		}
		return this;
	}


	/**
	 * Compares the predefined expectations with the actual measurement.
	 * 
	 * @param observation
	 * 			The result that was observed. Must not be null.
	 * @param round
	 *          The comparison round. Used for better human readable error messages. Must be greater or equal to zero.
	 */
	protected void compare(CountingResult observation,
			final int round) {
		final long[] measuredOpcodeCounts = observation.getOpcodeCounts();
		final Map<String, Long> measuredMethodCallCounts = observation.getMethodCallCounts();
		assert measuredOpcodeCounts != null : "measuredOpcodeCounts must not be null";
		assert measuredMethodCallCounts != null : "measuredMethodCallCounts must not be null";
		assert round >= 0 : "round must not be less than zero";
		
		// compare opcodes
		for (int j = 0; j < measuredOpcodeCounts.length; j++) {
			long expected = 0;
			if (this.opcodeExpectations.containsKey(j)) {
				expected = this.opcodeExpectations.get(j);
			}
			long actual = measuredOpcodeCounts[j];
			Assert.assertEquals(message(j, round), expected, actual);
		}
		// compare method calls (compare all expected with corresponding actuals)
		for (String method : this.methodCallExpectations.keySet()) {
			String message = "Expected " + message(method, round) + " not found";
			Assert.assertTrue(message, measuredMethodCallCounts.containsKey(method));
			long expected = this.methodCallExpectations.get(method);
			long actual = measuredMethodCallCounts.remove(method);
			Assert.assertEquals(message(method, round), expected, actual);
		}
		// ensure that no method not expected is measured
		for (String method : measuredMethodCallCounts.keySet()) {
			String message;
			long actual = measuredMethodCallCounts.get(method);
			message = "Actual " + message(method, round) + " not expected but counted as " + actual;
			Assert.assertTrue(message, actual < 0);
		}
		// compare parallel expectations and ensure that the result is threaded if parallel expectations exist
		compare_parallel(observation, round);
	}

	/**
	 * Compares the predefined parallel expectations to the observation.
	 * @see #compare(CountingResult, int)
	 * @param observation
	 * 			The result that was observed. Must not be null.
	 * @param round
	 *          The comparison round. Used for better human readable error messages. Must be greater or equal to zero.
	 */
	protected void compare_parallel(CountingResult observation, final int round) {
		if(this.parallelExpectations == null || this.parallelExpectations.isEmpty()) {
			// no parallel expectations exist
			Assert.assertEquals(message("Unexpected spawnded threads", round), 0, observation.getSpawnedThreadedCountingResults().size());
			return;
		}
		final SortedSet<CountingResult> observedThreads = 
				observation.getSpawnedThreadedCountingResults();
		// ensure that results from spawned threads are correct
		Assert.assertEquals("Wrong number of threads and parallel expectations.", this.parallelExpectations.size(), observedThreads.size());
		for(CountingResult spawn : observedThreads) {
			Assert.assertEquals("The observed thread was not spawned wihtin the observed section.", observation, spawn.getThreadedCountingResultSource());
		}
		if(this.parallelExpectations.size() == 1
				&& observedThreads.size() == 1) {
			// this is a case where the mapping of the observation to the 
			// expectation is possible: compare conventionally
			this.parallelExpectations.get(0).compare(observedThreads.first(), round);
		} else {
			/* The order of the observed spawned threads within the section does not depend on the
			 * on the order of the parallelExpectations list. Each spawned thread is compared if
			 * it matches an expectation. Only if all SectionExpectations are successfully matched, the
			 * comparison itself is successful.
			 */
			List<CountingResult> unmatchedResults = new LinkedList<CountingResult>(observedThreads);
			List<SectionExpectation> unmatchedExpectations = new LinkedList<SectionExpectation>();
			boolean matchFound;
			for(SectionExpectation ex : this.parallelExpectations) {
				matchFound = false;
				for(CountingResult tcr : unmatchedResults) {
					if(ex.matches(tcr)) {
						unmatchedResults.remove(tcr);
						matchFound = true;
						break;
					}
				}
				if(!matchFound) {
					unmatchedExpectations.add(ex);
				}
			}
			if (unmatchedExpectations.size() > 0) { // report error and details
				StringBuilder msg = new StringBuilder();
				msg.append("Some expectations were not met. Available Expectations: ");
				for (int i = 0; i < unmatchedExpectations.size(); i++) {
					msg.append(unmatchedExpectations.get(i));
					if (i < unmatchedExpectations.size() - 1) {
						msg.append(",");
					}
				}
				msg.append(". Available results: ");
				for (int i = 0; i < unmatchedResults.size(); i++) {
					msg.append(unmatchedResults.get(i));
					if (i < unmatchedResults.size() - 1) {
						msg.append(",");
					}
				}
				Assert.fail(msg.toString());
			}
		}
	}
	
	/**
	 * This method is similar to {@link #compare(CountingResult, int)} but 
	 * instead of throwing {@link AssertionError}s, it returns the result of the 
	 * comparison.
	 * @param cr {@link CountingResult} to compare this expectation to.
	 * @return True, if the given {@link CountingResult} matches this 
	 * expectation. False otherwise.
	 */
	private boolean matches(final CountingResult cr) {
		try {
			this.compare(cr, -1);
			return true;
		} catch(AssertionError ae) {
			return false;
		}
	}

	/**
	 * Builds an error message that shows if an assertion is false. This workaround for non-assertEquals()-methods adds an
	 * additional info about the expected and actual value.
	 * 
	 * @param name
	 *          The name of the wrong opcode or method.
	 * @param round
	 *          Counting round.
	 * @param expected
	 *          The expected value.
	 * @param actual
	 *          The actual value.
	 * @return Error message.
	 */
	@SuppressWarnings("unused")
	private String message(final String name, final int round, final int expected, final int actual) {
		StringBuilder sb = new StringBuilder();
		sb.append(this.message(name, round));
		sb.append(" expected:<");
		sb.append(expected);
		sb.append("> but was:<");
		sb.append(actual);
		sb.append(">");
		return sb.toString();
	}

	/**
	 * Builds an error message that shows if an assertion is false.
	 * 
	 * @param opcode
	 *          Current opcode.
	 * @param round
	 *          Counting round.
	 * @return Error message.
	 */
	private String message(final int opcode, final int round) {
		assert opcode >= 0 && opcode < 202 : "Opcode number out of range. Was: " + opcode;
		
		String opString = FullOpcodeMapper.getMnemonicOfOpcode(opcode);
		return this.message(opString, round);
	}

	/**
	 * Builds an error message that shows if an assertion is false.
	 * 
	 * @param name
	 *          The name of the wrong opcode or method.
	 * @param round
	 *          Counting round.
	 * @return Error message.
	 */
	private String message(final String name, final int round) {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(" in round ");
		sb.append(round);
		sb.append(" in ");
		sb.append(this);
		return sb.toString();
	}

	/**
	 * Converts a typical java signature to the equivalent bytecode descriptor.
	 * 
	 * For example:<br />
	 * java.lang.String valueOf(java.lang.Object obj) => java.lang.String.valueOf(Ljava/lang/Object;)Ljava/lang/String;
	 * 
	 * @param className
	 *          The canonical name of the class declaring the method.
	 * @param signature
	 *          A string containing a standard Java method signature (i.e.
	 *          <code>public static java.lang.String valueOf(java.lang.Object obj)</code> ). Object types need to be
	 *          specified with the full canonical name. Specifically, only the two tokens before the first '(', as well as
	 *          everything between '(' and ')' is evaluated. Whitespaces and qualifiers like "public", "static" are
	 *          ignored. Generic types may be ommited (and are ignored) so that "List" and "List<Integer>" are treated as
	 *          the same since bytecode signatures ignore generics. For method parameters, only one or two tokens are
	 *          allowed (example: "int[]" or "int[] abc"). It is advised to take the method declaration from sourcecode or
	 *          from documentation and only adapt it, if necessary. The thing that needs to be adapted is type names for
	 *          object types. So instead of giving the String "String myString", this has to be expanded to
	 *          "java.lang.String". Note that inner/nested classes need to be specified using the '$' symbol as in the
	 *          following example: <code>my.packagename.OutClass$InnerClass</code>.
	 * @return A bytecode descriptor to uniquely identify methods consisting of the canonical classname, the method name
	 *         and the method descriptor.
	 */
	private String signatureToDescriptor(String className, String signature) {
		MethodDescriptor test = new MethodDescriptor(className, signature);
		return test.getCanonicalMethodName();
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SectionExpectation [sectionNumber=");
		sb.append(this.sectionNumber);
		sb.append(", range=");
		sb.append(this.range);
		sb.append(", opcodeExpectations=");
		sb.append(this.opcodeExpectations);
		sb.append(", parallelExpectations=");
		sb.append(this.parallelExpectations);
		sb.append("]");
		return sb.toString();
	}
}
