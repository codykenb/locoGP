package de.uka.ipd.sdq.ByCounter.instrumentation;

import java.io.Serializable;

import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * Entity for instrumentation of a complete method.
 * @see InstrumentedCodeArea
 * @see InstrumentedRegion
 * @see InstrumentedClass
 * @author Martin Krogmann
 */
public class InstrumentedMethod extends EntityToInstrument implements Serializable {

	/**
	 * Serialisation version.
	 */
	private static final long serialVersionUID = 1L;
	
	/** The descriptor for the method to instrument. */
	private MethodDescriptor method;
	
	/**
	 * @param method The descriptor for the method to instrument.
	 */
	public InstrumentedMethod(final MethodDescriptor method) {
		this.method = method;
	}

	/**
	 * @return The descriptor for the method to instrument.
	 */
	public MethodDescriptor getMethod() {
		return this.method;
	}
	
	/** {@inheritDoc} */
	@Override
	public MethodDescriptor[] getMethodsToInstrument() {
		return new MethodDescriptor[] {method};
	}

	/**
	 * @param method The descriptor for the method to instrument.
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
		builder.append("InstrumentedMethod [method=");
		builder.append(this.method);
		builder.append("]");
		return builder.toString();
	}	
}
