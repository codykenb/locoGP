/**
 * 
 */
package de.uka.ipd.sdq.ByCounter.instrumentation;

import java.io.Serializable;

import de.uka.ipd.sdq.ByCounter.parsing.LineNumberRange;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * Entity for instrumentation of code area that is part of a method.
 * The code area is specified in absolute line numbers.
 * @see InstrumentedMethod
 * @see InstrumentedRegion
 * @see InstrumentedClass
 * @author Martin Krogmann
 */
public class InstrumentedCodeArea extends EntityToInstrument implements Serializable {
	
	/**
	 * Serialisation version.
	 */
	private static final long serialVersionUID = 1L;

	/** Descriptor for the method that the code area is a part of. */
	private MethodDescriptor method;
	
	/** {@link LineNumberRange} that of the code area. */
	private LineNumberRange area;
	
	/**
	 * Construct the {@link InstrumentedCodeArea}.
	 * @param method Descriptor for the method that the code area is a part of.
	 * @param area {@link LineNumberRange} that of the code area.
	 */
	public InstrumentedCodeArea(
			final MethodDescriptor method, 
			final LineNumberRange area) {
		this.method = method;
		this.area = area;
	}

	/**
	 * @return {@link LineNumberRange} that of the code area.
	 */
	public LineNumberRange getArea() {
		return area;
	}

	/**
	 * @return Descriptor for the method that the code area is a part of.
	 */
	public MethodDescriptor getMethod() {
		return method;
	}

	/** {@inheritDoc} */
	@Override
	public MethodDescriptor[] getMethodsToInstrument() {
		return new MethodDescriptor[] {this.method};
	}

	/**
	 * @param area {@link LineNumberRange} that of the code area.
	 */
	public void setArea(LineNumberRange area) {
		this.area = area;
	}

	/**
	 * @param method Descriptor for the method that the code area is a part of.
	 */
	public void setMethod(MethodDescriptor method) {
		this.method = method;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InstrumentedCodeArea [method=");
		builder.append(this.method);
		builder.append(":");
		builder.append(this.area.firstLine);
		builder.append("-");
		builder.append(this.area.lastLine);
		builder.append("]");
		return builder.toString();
	}
}
