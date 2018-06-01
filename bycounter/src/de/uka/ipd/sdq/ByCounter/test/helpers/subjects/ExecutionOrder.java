/**
 * 
 */
package de.uka.ipd.sdq.ByCounter.test.helpers.subjects;

/**
 * Implementation used to test the preservation of the execution order in resulting measurements.
 * 
 * @author groenda
 * 
 */
public class ExecutionOrder {

	public void process() {
		int i = 0;
		i++;
		i *= 1;
	}

}
