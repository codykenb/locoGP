/**
 * 
 */
package de.uka.ipd.sdq.ByCounter.instrumentation;

/**
 * When ByCounter is used to instrument classes that have already been 
 * instrumented by ByCounter, this exception is thrown.
 * 
 * @author Martin Krogmann
 */
public class AlreadyInstrumentedException extends RuntimeException {

	/** Needed for serialisation. */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs the exception.
	 * @param message Exception message with a description of the exception.
	 */
	public AlreadyInstrumentedException(String message) {
		super(message);
	}
}
