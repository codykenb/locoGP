/**
 *
 */
package de.uka.ipd.sdq.ByCounter.instrumentation;

import java.util.logging.Logger;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

/**
 * Class for managing the indices for local variables in bytecode.
 * For all getNew* methods, the outcome depends on the setting of
 * getUseHighRegisters().
 * @author Martin Krogmann
 *
 */
public final class LocalVariableManager {
	private static final Logger log = Logger.getLogger("de.uka.ipd.sdq.ByCounter.instrumentation.LocalVariableManager");

	private static final int MAX_LOCALS = 65535;	// maximum number of locals in a method
	private LocalVariablesSorter lvars = null;
	private int registerOffset;		// remembered in the class instance for the next allocation;

	/**
	 * When true, use register numbers near the MAX_LOCALS constant.
	 */
	private boolean useHighRegisters;

	public LocalVariableManager(boolean useHighRegisters) {
		this.useHighRegisters = useHighRegisters;
		this.registerOffset = 0;
	}

	/**
	 * Register a new int[]. Does not initialize it.
	 * @param mv The {@link MethodVisitor} to use for inserting initialisation instructions.
	 * @return The variable index/identifier.
	 */
	public int getNewIntArrayVar(MethodVisitor mv) {
		return this.getNewVarFor(mv, Type.getObjectType("[I"), 1);
	}

	/**
	 * Register a new int and initialize it with 0.
	 * @param mv The {@link MethodVisitor} to use for inserting initialisation instructions.
	 * @return The variable index/identifier.
	 */
	@SuppressWarnings("boxing")
	public int getNewIntVar(MethodVisitor mv) {
		log.finest("registerOffset: "+this.registerOffset);
		mv.visitLdcInsn(0);	// initialise with 0
		int var = this.getNewVarFor(mv, Type.INT_TYPE, 1);
		mv.visitVarInsn(Opcodes.ISTORE, var);
		return var;
	}

	/**
	 * Register a new long[]. Does not initialize it.
	 * @param mv The {@link MethodVisitor} to use for inserting initialisation instructions.
	 * @return The variable index/identifier.
	 */
	public int getNewLongArrayVar(MethodVisitor mv) {
		return this.getNewVarFor(mv, Type.getObjectType("[J"), 1);
	}

	/**
	 * Register a new long and initialize it with 0.
	 * @param mv The {@link MethodVisitor} to use for inserting initialisation instructions.
	 * @return The variable index/identifier.
	 */
	@SuppressWarnings("boxing")
	public int getNewLongVar(MethodVisitor mv) {
		log.finest("registerOffset: "+this.registerOffset);
		mv.visitLdcInsn(0L);	// initialise with 0
		int var = this.getNewVarFor(mv, Type.LONG_TYPE, 2);
		mv.visitVarInsn(Opcodes.LSTORE, var);
		return var;
	}

	/**
	 * Register a new String[]. Does not initialize it.
	 * @param mv The {@link MethodVisitor} to use for inserting initialisation instructions.
	 * @return The variable index/identifier.
	 */
	public int getNewStringArrayVar(MethodVisitor mv) {
		return this.getNewVarFor(mv, Type.getObjectType("[Ljava/lang/String;"), 1);
	}

	/**
	 * Register a new variable of type 'type'.
	 * Do not use unless a more specialised method does not exist (yet).
	 * @param mv The {@link MethodVisitor} to use for inserting initialisation instructions.
	 * @param type The type of the new local.
	 * @param localVarSize The number of local variables that the var blocks. This is 1 for int, 2 for long, 1 for Object etc.
	 * @return The variable index/identifier.
	 */
	public int getNewVarFor(MethodVisitor mv,
			Type type, int localVarSize) {
		log.finest("nextRegisterOffset: "+this.registerOffset);
		int var = -1;
		if(getUseHighRegisters()) {
			this.registerOffset += localVarSize;	// update pointer for register
			var = MAX_LOCALS - this.registerOffset;
		} else {
			var = this.lvars.newLocal(type);
		}
		String readableDescr = type.getClassName();
		log.finest("Registered new "+readableDescr+"@localvar "+var);
		log.finest("registerOffset: "+this.registerOffset);
		return var;
	}

	/**
	 * Use {@link #getNewIntVar(MethodVisitor)} etc. instead wherever possible!
	 * @param type variable type.
	 */
	protected void reserveLocalVar(Type type) {
		this.lvars.newLocal(type);
	}

	/**
	 * @return True when register numbers near the MAX_LOCALS constant are used.
	 * False otherwise.
	 */
	public boolean getUseHighRegisters() {
		return this.useHighRegisters;
	}

	/**
	 * Sets the LocalVariableSorter that is used to generate new locals.
	 * This must be called before this Manager is used.
	 * @param pLvars LocalVariableSorter
	 */
	public void setLVS(LocalVariablesSorter pLvars) {
		this.lvars = pLvars;
	}

	/**
	 *
	 * @param useHighRegisters When true, use register numbers near the MAX_LOCALS constant.
	 */
	public void setUseHighRegisters(boolean useHighRegisters) {
		this.useHighRegisters = useHighRegisters;
	}

}
