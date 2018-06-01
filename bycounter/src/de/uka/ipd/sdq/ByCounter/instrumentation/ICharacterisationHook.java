package de.uka.ipd.sdq.ByCounter.instrumentation;

/**
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
public interface ICharacterisationHook {
	
	/**
	 * Called on method invocations. TODO explain parameters
	 * @param methodCountMethodAdapter
	 * @param instrumentationParameters
	 * @param opcode Method call opcode.
	 * @param owner The class/interface on which the method is invoked.
	 * @param name The name of the method.
	 * @param desc The descriptor of the method.
	 */
	public void methodCallHook(
			MethodCountMethodAdapter methodCountMethodAdapter,
			InstrumentationParameters instrumentationParameters, 
			int opcode,
			String owner, 
			String name, 
			String desc);
	
	/**
	 * TODO explain method and its parameters
	 */
	public void methodReturnHook(
			MethodCountMethodAdapter methodCountMethodAdapter,
			InstrumentationParameters instrumentationParameters);
	
	
	/**
	 * Called on method entry. TODO explain parameters
	 * @param methodCountMethodAdapter
	 * @param instrumentationParameters
	 * @param access
	 * @param name
	 * @param desc
	 * @param signature
	 * @param exceptions
	 */
	public void methodStartHook(
			MethodCountMethodAdapter methodCountMethodAdapter,
			InstrumentationParameters instrumentationParameters, 
			int access, 
			String name, 
			String desc, 
			String signature, 
			String[] exceptions);
}
