package de.uka.ipd.sdq.ByCounter.instrumentation;

import java.io.Serializable;

import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * Entity for instrumentation of a complete class (all of its methods).
 * @see InstrumentedCodeArea
 * @see InstrumentedRegion
 * @see InstrumentedMethod
 * @author Martin Krogmann
 */
public class InstrumentedClass extends EntityToInstrument implements Serializable {

	/**
	 * Serialisation version.
	 */
	private static final long serialVersionUID = 1L;
	
	/** Canonical name of the class to instrument. */
	private String canonicalClassName;
	
	/**
	 * @param className Canonical name of the class to instrument.
	 */
	public InstrumentedClass(final String className) {
		this.canonicalClassName = className;
	}
	
	/**
	 * @return Canonical name of the class to instrument.
	 */
	public String getCanonicalClassName() {
		return this.canonicalClassName;
	}
	
	/**
	 * @param canonicalClassName Canonical name of the class to instrument.
	 */
	public void setCanonicalClassName(String canonicalClassName) {
		this.canonicalClassName = canonicalClassName;
	}

	/** {@inheritDoc} */
	@Override
	public MethodDescriptor[] getMethodsToInstrument() {
		return new MethodDescriptor[] {};
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InstrumentedClass [canonicalClassName=");
		builder.append(this.canonicalClassName);
		builder.append("]");
		return builder.toString();
	}
}
