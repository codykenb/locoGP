package de.uka.ipd.sdq.ByCounter.execution;

/**
 * Thrown to indicate that a query (i.e. send to 
 * {@link CountingResultCollector}) was not applicable.
 * @author Martin Krogmann
 *
 */
public class InvalidQueryException extends Exception {

	/**
	 * Serialisation version.
	 */
	private static final long serialVersionUID = 1L;

	public InvalidQueryException(final String message) {
		super(message);
	}
}
