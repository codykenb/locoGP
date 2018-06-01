package de.uka.ipd.sdq.ByCounter.instrumentation;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * Interface for the analysis of a method's instructions.
 * @author Martin Krogmann
 *
 */
public interface IInstructionAnalyser {
	
	/**
	 * Analyse the given instruction.
	 * @param insn Instruction as encountered in a method.
	 */
	void analyseInstruction(AbstractInsnNode insn);
	
	/**
	 * Analyse the given try catch node.
	 * @param tryCatchNode Try catch node as encountered in a method.
	 */
	void analyseTryCatchBlock(final TryCatchBlockNode tryCatchNode);
	
	/**
	 * Called after all instructions have been individually analysed.
	 * @param instructions Complete list of instructions.
	 */
	void postAnalysisEvent(InsnList instructions);

}
