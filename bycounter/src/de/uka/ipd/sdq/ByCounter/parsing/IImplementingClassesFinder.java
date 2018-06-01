package de.uka.ipd.sdq.ByCounter.parsing;

/**
 * This interface describes the possibility to find classes on the classpath or
 * otherwise that implement a specific interface.
 * @author Martin Krogmann
 *
 */
public interface IImplementingClassesFinder {
	/**
	 * Find classes implementing the given interface.
	 * @param interfaceToImplement The interface to find implementing classes
	 * for.
	 * @return An array of strings where each string is a canonical class name.
	 * These classes have to implement the given interface.
	 */
	String[] findImplementingClasses(Class<?> interfaceToImplement);

}
