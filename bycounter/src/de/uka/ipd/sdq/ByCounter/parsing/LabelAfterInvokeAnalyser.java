package de.uka.ipd.sdq.ByCounter.parsing;

import java.util.Iterator;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import de.uka.ipd.sdq.ByCounter.instrumentation.IInstructionAnalyser;

/**
 * This {@link IInstructionAnalyser} adds new labels directly after method 
 * invocations so that i.e. pop instructions are not in the same label block.
 * For analysers to pick up on the changes made by this class, it needs to be
 * in a seperate pass before those analysers.
 * @author Martin Krogmann
 *
 */
public class LabelAfterInvokeAnalyser implements IInstructionAnalyser {

	@Override
	public void analyseInstruction(AbstractInsnNode insn) {
		// do nothing
	}

	@Override
	public void analyseTryCatchBlock(TryCatchBlockNode tryCatchNode) {
		// do nothing
	}

	@Override
	public void postAnalysisEvent(InsnList instructions) {

		// go through all instructions
		for (	
				@SuppressWarnings("unchecked")
				Iterator<AbstractInsnNode> iterator = instructions.iterator(); 
				iterator.hasNext();
			) {
			AbstractInsnNode insn = iterator.next();
			
			// look for labels that are marked to start an instruction block
			if(insn instanceof MethodInsnNode) {
				instructions.insert(insn, new LabelNode());
			}
		}
	}

}
