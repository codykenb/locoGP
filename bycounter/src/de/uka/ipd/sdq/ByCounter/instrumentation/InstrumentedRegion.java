package de.uka.ipd.sdq.ByCounter.instrumentation;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Label;

import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * This class describes a region of instrumentation that starts in a specific
 * line number of a specified method and ends in a specific line number of a 
 * (possible different) method.
 * <p>
 * Instrumentation regions must not overlap.
 * </p>
 * @see InstrumentedCodeArea
 * @see InstrumentedMethod
 * @see InstrumentedClass
 * @author Martin Krogmann
 *
 */
public class InstrumentedRegion extends EntityToInstrument implements Serializable {
	/**
	 * Serialisation version.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * The ids of the {@link Label}s in 
	 * {@link #getStartMethod()} that start
	 * in line {@link #getStartLine()}.
	 */
	private List<Integer> startLabelIds;
	/**
	 * First line in {@link #startMethod} included in the region.
	 */
	private int startLine;
	/**
	 * Method referenced by {@link #startLine}.
	 */
	private MethodDescriptor startMethod;
	/**
	 * The ids of the {@link Label}s in 
	 * {@link #getStopMethod()} that stop
	 * in line {@link #getStopLine()}.
	 */
	private List<Integer> stopLabelIds;
	/**
	 * Last line in {@link #stopMethod} included in the region.
	 */
	private int stopLine;
	/**
	 * Method referenced by {@link #stopLine}.
	 */
	private MethodDescriptor stopMethod;
	
	/**
	 * Construct the region.
	 * @param startMethod Method referenced by firstLineNumber.
	 * @param firstLineNumber First line in startMethod included in the instrumentation.
	 * @param stopMethod Method referenced by lastLineNumber.
	 * @param lastLineNumber Last line in stopMethod included in the instrumentation.
	 */
	public InstrumentedRegion(final MethodDescriptor startMethod, final int firstLineNumber,
			final MethodDescriptor stopMethod, final int lastLineNumber) {
		setStart(startMethod, firstLineNumber);
		setStop(stopMethod, lastLineNumber);
		this.startLabelIds = new LinkedList<Integer>();
		this.stopLabelIds = new LinkedList<Integer>();
	}
	
	/** {@inheritDoc} */
	@Override
	public MethodDescriptor[] getMethodsToInstrument() {
		return new MethodDescriptor[] {this.startMethod, this.stopMethod};
	}

	/**
	 * @return The ids of the {@link Label}s in 
	 * {@link #getStartMethod()} that start
	 * in line {@link #getStartLine()}.
	 */
	public List<Integer> getStartLabelIds() {
		return startLabelIds;
	}

	/**
	 * @return First line in {@link #getStartMethod()} included in the region.
	 */
	public int getStartLine() {
		return this.startLine;
	}
	
	/**
	 * @return Method referenced by {@link #getStartLine()}.
	 */
	public MethodDescriptor getStartMethod() {
		return this.startMethod;
	}
	
	/**
	 * @return The ids of the {@link Label}s in 
	 * {@link #getStopMethod()} that stop
	 * in line {@link #getStopLine()}.
	 */
	public List<Integer> getStopLabelIds() {
		return stopLabelIds;
	}
	
	/**
	 * @return Last line in {@link #getStopMethod()} included in the region.
	 */
	public int getStopLine() {
		return this.stopLine;
	}

	/**
	 * @return Method referenced by {@link #getStopLine()}.
	 */
	public MethodDescriptor getStopMethod() {
		return this.stopMethod;
	}

	/**
	 * Specify the start of the region.
	 * @param startMethod Method referenced by firstLineNumber.
	 * @param firstLineNumber First line in startMethod included in the instrumentation.
	 */
	public void setStart(final MethodDescriptor startMethod, final int firstLineNumber) {
		this.startMethod = startMethod;
		this.startLine = firstLineNumber;
	}

	/**
	 * @param labelIds The ids of the {@link Label}s in 
	 * {@link #getStartMethod()} that start
	 * in line {@link #getStartLine()}.
	 */
	public void setStartLabelIds(List<Integer> labelIds) {
		this.startLabelIds = labelIds;
	}

	/**
	 * Specify the stop of the region.
	 * @param stopMethod Method referenced by lastLineNumber.
	 * @param lastLineNumber Last line in stopMethod included in the instrumentation.
	 */
	public void setStop(final MethodDescriptor stopMethod, final int lastLineNumber) {
		this.stopMethod = stopMethod;
		this.stopLine = lastLineNumber;
	}

	/**
	 * @param stopLabelIds The ids of the {@link Label}s in 
	 * {@link #getStopMethod()} that stop
	 * in line {@link #getStopLine()}.
	 */
	public void setStopLabelIds(List<Integer> stopLabelIds) {
		this.stopLabelIds = stopLabelIds;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InstrumentationRegion [");
		builder.append(this.startMethod.getCanonicalMethodName());
		builder.append(":");
		builder.append(this.startLine);
		builder.append(" - ");
		builder.append(this.stopMethod.getCanonicalMethodName());
		builder.append(":");
		builder.append(this.stopLine);
		builder.append("]");
		return builder.toString();
	}
	
	
}
