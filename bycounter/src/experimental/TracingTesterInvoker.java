package experimental;

/**
 * Calls JavaLangRuntimeInstructionTracingTester
 * @author Michael Kuperberg
 * @since 1.2
 * @version 1.2
 */
public class TracingTesterInvoker {
	/**
	 * Conventional main method.
	 * @param args No parameters needed or evaluated.
	 */
	public static void main(String[] args) {
		JavaLangRuntimeInstructionTracingTester jlrt;
		jlrt = new JavaLangRuntimeInstructionTracingTester();
		jlrt.run();
	}
}
