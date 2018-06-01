package de.uka.ipd.sdq.ByCounter.test.helpers;

/**
 * This class includes control flow that stresses pitfalls of the 
 * online observation implementation.#
 * 
 * <p>WARNING: The actual test case source code is sensitive to 
 * line number changes!<p>
 * 
 * @author Martin Krogmann
 *
 */
public class TestSubjectResultObservation {
	
	/**
	 * Calling this method with true results in the following call tree:
	 * <pre>
	 * {@code
	 * method1
	 * -->method2
	 * ----->method1
	 * ---method2
	 * method1
	 * }
	 * </pre>
	 * @param firstLevel When true, calls {@link #method2()}.
	 */
	public void method1(boolean firstLevel) {
		int i = 2;
		if(!firstLevel) {
			i -= 10;	// subtract int
		} else {
			i += method2(); // add float
		}
		i += 3.0;		// add double
		System.out.println(i);
	}
	
	/**
	 * Is called by {@link #method1(boolean)} first.
	 * Calls {@link #method1(boolean)} second level.
	 * @return float value;
	 */
	public float method2() {
		float f = 2.0f;
		method1(false);
		return f;
	}

}
