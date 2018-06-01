package de.uka.ipd.sdq.ByCounter.instrumentation;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * Visitor for the class declaration.
 *
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
@Deprecated // Static is counting not supported
public final class MethodSectionCountClassAdapter extends ClassAdapter {

	private String className;
	
	private InstrumentationParameters instrumentationParameters;

	private InstrumentationState instrumentationState;
	
	/**
	 * Create a new MethodSectionCountClassAdapter.
	 * @param visitor The preceding visitor in the chain.
	 * @param params InstrumentationParameters
	 */
	public MethodSectionCountClassAdapter(
			ClassVisitor visitor, 
			InstrumentationParameters params,
			InstrumentationState state) {
		super(visitor);
		this.instrumentationParameters = params;
		this.instrumentationState = state;
	}

	/**
	 * Visits the header of the class and grabs the classname.
	 */
	@Override
	public void visit(int version, int access, String name, String signature, String supername, String[] interfaces) {
		super.visit(version, access, name, signature, supername, interfaces);
		this.className = name.replace('/', '.');
	}

	/**
	 * This is called when a method declaration happens in the class
	 */
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodVisitor nextVisitor = null;
		MethodVisitor mv = null;
		
		// call other classvisitors in the chain:
		mv = this.cv.visitMethod(access, name, desc, signature, exceptions);
		
		if (mv != null) {
			int indexOfMethodMatch = MethodDescriptor.findMethodInList(
					this.instrumentationState.getMethodsToInstrumentCalculated(), 
					this.className,
					name, 
					desc);
			if (indexOfMethodMatch >= 0) {
				// this is the method to instrument
				mv = new MethodSectionCountMethodAdapter(name, access, 
						this.className + "." + name, desc, mv, 
						this.instrumentationParameters,
						this.instrumentationState.getMethodsToInstrumentCalculated().get(indexOfMethodMatch));
				nextVisitor = mv;
			} else {
				// this is not the right method: use default visitor
				nextVisitor = new MethodAdapter(mv);
			}
		}
		return nextVisitor;
	}
}
