package de.uka.ipd.sdq.ByCounter.test.framework.expectations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Assert;

import de.uka.ipd.sdq.ByCounter.instrumentation.EntityToInstrument;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedCodeArea;
import de.uka.ipd.sdq.ByCounter.parsing.LineNumberRange;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;

/**
 * Offers a framework to specify expectations and to compare them with the actual measurement.
 * 
 * <p>
 * To define new expectations you can write them down like a tree. Example:<br/>
 * <code>Expectation e = new Expectation();<br/>
 * e.add().add(Opcodes.ICONST_1, 1)<br/>
 * &nbsp;.add(Opcodes.ISTORE, 1)<br/>
 * &nbsp;.add(Opcodes.IINC, 1)<br/>
 * &nbsp;.add(Opcodes.RETURN, 1);</code>
 * 
 * <p>
 * If something went wrong or not as expected this framework uses JUnit to throw an AssertionError. It normally comes
 * with the following message: <i>&lt;Opcode&gt; in round &lt;RoundNumber&gt; in SectionExpectation
 * [sectionNumber=&lt;SectionNumber&gt;, range=LineNumberRange [firstLine=&lt;number&gt;, lastLine=&lt;number&gt;]]
 * expected:&lt;number&gt; but was:&lt;number&gt;</i>
 * 
 * <p>
 * The round number may help you if a section has to be counted more than once. It tells you how many sections were
 * counted before this section which caused the error. The section number is either the number you have used on
 * {@link Expectation#add(int)} or a generated one according to the section order if you used another add(..) method.
 * The rest should be self-explanatory.
 * 
 * @version 1.1
 * @author Florian Schreier
 */
public class Expectation {

	/** This number is used if there is no given section number. */
	public static final int SECTION_NUMBER_NOT_SET = -1;

	/** States if section sequence is important or not. */
	private final boolean ordered;

	/** The biggest used section number. Is used to add new entries to dictionary. */
	private int biggestSectionNumber;

	/** A dictionary with all line number ranges and corresponding section numbers. */
	private final SortedMap<LineNumberRange, Integer> lineRangeToSectionNumber;

	/** All expected sections in the order they should appear. Only if <code>ordered == true</code>. */
	private final List<SectionExpectation> orderedSections;

	/** All expected sections without order. Only if <code>ordered == false</code>. */
	private final Map<Integer, SectionExpectation> unorderedSections;
	
	/**
	 * Creates a new Expectation with ordered sections. Same behavior as Expectation(true).
	 */
	public Expectation() {
		this(true);
	}

	/**
	 * Creates a new Expectation.
	 * 
	 * @param ordered
	 *          <code>true</code>, if actual and expected sequence of sections has to be the same. <code>false</code>, if
	 *          not.
	 */
	public Expectation(final boolean ordered) {
		this.ordered = ordered;
		this.biggestSectionNumber = -1;
		this.lineRangeToSectionNumber = new TreeMap<LineNumberRange, Integer>();
		if (this.ordered) {
			this.orderedSections = new ArrayList<SectionExpectation>();
			this.unorderedSections = null;
		} else {
			this.orderedSections = null;
			this.unorderedSections = new HashMap<Integer, SectionExpectation>();
		}
	}

	/**
	 * Creates a section without a section number and marks it as expected.
	 * 
	 * @return The new <code>SectionExpectation</code>.
	 */
	public SectionExpectation add() {
		return this.add(Expectation.SECTION_NUMBER_NOT_SET, null);
	}

	/**
	 * Creates a section with the given section number and marks it as expected. If you want to create a dummy expectation
	 * use <code>add()</code> instead.
	 * 
	 * @param sectionNumber
	 *          The new section's number. Has to be greater or equal <code>0</code>.
	 * @return The new <code>SectionExpectation</code>.
	 */
	public SectionExpectation add(final int sectionNumber) {
		if (sectionNumber < 0) {
			throw new IllegalArgumentException("sectionNumber has to be greater or equal zero");
		}
		
		return this.add(sectionNumber, null);
	}

	/**
	 * Creates a section with the given range block and marks it as expected. The corresponding section number is
	 * automatically derived.
	 * 
	 * @param firstLine
	 *          The first line of the range block this section is about.
	 * @param lastLine
	 *          The last line of this range block. Has to be greater or equal <code>firstLine</code>.
	 * @return The new <code>SectionExpectation</code>.
	 */
	public SectionExpectation add(final int firstLine, final int lastLine) {
		if (firstLine > lastLine) {
			throw new IllegalArgumentException("lastLine has to be greater or equal firstLine");
		}
		
		LineNumberRange range = new LineNumberRange(firstLine, lastLine);
		return this.add(this.getSectionNumberByRange(range), range);
	}

	/**
	 * Creates a section with the given range block and its section number and marks it as expected.
	 * 
	 * @param sectionNumber
	 *          The new section's number. Has to be greater or equal <code>0</code>.
	 * @param range
	 *          The new section's number. Can be <code>null</code> if unknown.
	 * @return The new <code>SectionExpectation</code>.
	 */
	private SectionExpectation add(final int sectionNumber, final LineNumberRange range) {
		if (sectionNumber > this.biggestSectionNumber) {
			this.biggestSectionNumber = sectionNumber;
		}
		SectionExpectation sectExpt = new SectionExpectation(sectionNumber, range);
		addSectionExpectation(sectionNumber, sectExpt);
		return sectExpt;
	}

	/**
	 * 
	 * @param sectExpt {@link SectionExpectation} to add to the expectation.
	 * @return The given {@link SectionExpectation}.
	 */
	public SectionExpectation add(final SectionExpectation sectExpt) {
		this.addSectionExpectation(SECTION_NUMBER_NOT_SET, sectExpt);
		return sectExpt;
	}

	/**
	 * Adds the given {@link SectionExpectation} to the internal data structures
	 * @param sectionNumber 
	 * The new section's number if unordered. Has to be greater or equal <code>0</code> or 
	 * {@link #SECTION_NUMBER_NOT_SET}.
	 * @param sectExpt The {@link SectionExpectation} to add.
	 */
	private void addSectionExpectation(final int sectionNumber, SectionExpectation sectExpt) {
		if (this.ordered) {
			this.orderedSections.add(sectExpt);
		} else {
			this.unorderedSections.put(sectionNumber, sectExpt);
		}
	}
	
	/**
	 * Returns all known line number ranges.
	 * 
	 * @return All known line number ranges.
	 */
	public LineNumberRange[] getRanges() {
		return this.lineRangeToSectionNumber.keySet().toArray(new LineNumberRange[0]);
	}

	/**
	 * Compares the predefined expectations with the actual measurement. The section's order is considered if
	 * <code>ordered == true</code>.
	 * 
	 * <p>
	 * If the expected and the actual values are different this method throws adequate assertions. So if you want to read
	 * the log you have to call this method after your logging method.
	 * 
	 * @param observation
	 *          ByCounter's output.
	 */
	public void compare(final CountingResult[] observation) {
		if (observation == null) {
			throw new IllegalArgumentException("observation must not be null");
		}

		String message = "Unexpected number of sections.";
		Assert.assertEquals(message, this.getNumberOfSections(), observation.length);
		for (int i = 0; i < observation.length; i++) {
			SectionExpectation sectExpt;
			if (this.ordered) {
				sectExpt = this.orderedSections.get(i);
				message = sectExpt.toString() + " not expected. Maybe wrong order.";
				Assert.assertEquals(message, sectExpt.getSectionNumber(), observation[i].getIndexOfRangeBlock());
				if(sectExpt.getRange() != null) {
					EntityToInstrument observedElement = observation[i].getObservedElement();
					Assert.assertTrue(observedElement instanceof InstrumentedCodeArea);
					Assert.assertEquals(message, sectExpt.getRange(), ((InstrumentedCodeArea) observedElement).getArea());
				}
			} else {
				sectExpt = this.unorderedSections.get(observation[i].getIndexOfRangeBlock());
				message = "Section #" + observation[i].getIndexOfRangeBlock() + " not expected.";
				Assert.assertNotNull(message, sectExpt);
			}
			sectExpt.compare(observation[i], i);
		}

	}

	/**
	 * Looks for the given <code>range</code> in a dictionary and returns the corresponding section number.
	 * 
	 * @param range
	 *          The range to look for.
	 * @return The corresponding section number.
	 */
	private int getSectionNumberByRange(final LineNumberRange range) {
		if (!this.lineRangeToSectionNumber.containsKey(range)) {
			this.lineRangeToSectionNumber.put(range, this.biggestSectionNumber + 1);
		}
		return this.lineRangeToSectionNumber.get(range);
	}

	/**
	 * Returns the number of expected sections.
	 * 
	 * @return The number of expected sections.
	 */
	private int getNumberOfSections() {
		if (this.ordered) {
			return this.orderedSections.size();
		} else {
			return this.unorderedSections.size();
		}
	}
}
