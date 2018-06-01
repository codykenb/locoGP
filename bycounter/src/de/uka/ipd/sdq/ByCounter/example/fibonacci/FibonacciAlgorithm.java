/**
 * 
 */
package de.uka.ipd.sdq.ByCounter.example.fibonacci;

/**Implementation of an algorithm calculating Fibonacci numbers.
 * Chosen as the calculation requires only CPU. Algorithm is also used in class
 * <code>de.uka.ipd.sdq.measurement.strategies.activeresource.cpu.FibonacciDemand</code> of the
 * ProtoCom framework.
 * @author groenda
 *
 */
public class FibonacciAlgorithm {
	
	/**Calculates a Fibonacci number given the number of rounds the algorithm should run.
	 * @param rounds Number of calculation rounds.
	 * @return Fibonacci number.
	 */
	public long fibonacci(long rounds) {
		long i1 = 0;
		long i2 = 1;
		long i3 = 0;
		// normalized loop
		for (
				long i = 0; 
				i < rounds; 
				i++) {
			i3 = i1 + i2;
			i2 = i1;
			i1 = i3;
		}
		return i3;
	}
}
