package de.uka.ipd.sdq.ByCounter.parsing;

import java.util.ListIterator;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import de.uka.ipd.sdq.ByCounter.instrumentation.IInstructionAnalyser;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationState;

/**
 * An implementation of {@link IInstructionAnalyser} that analyses instructions 
 * in order to find basic blocks. The results of this {@link IInstructionAnalyser}
 * can be queried with the method {@link InstrumentationState#getBasicBlockLabels()}.
 * @author Martin Krogmann
 *
 */
public class BasicBlockAnalyser extends InstructionBlockAnalyser {

	/**
	 * Field that is true on construction, but false after the first instruction
	 * has been analysed.
	 */
	private boolean isFirstInstruction;
	
	/**
	 * Construct the basic block analyser. Needed before each method.
	 * @param methodDescriptorString Descriptor of the analysed method.
	 * @param instrumentationState Structure for instrumentation results.
	 */
	public BasicBlockAnalyser(String methodDescriptorString,
			InstrumentationState instrumentationState) {
		super(methodDescriptorString, instrumentationState);

		this.isFirstInstruction = true;
	}
	
	@Override
	public void analyseInstruction(AbstractInsnNode insn) {
		if(isFirstInstruction) {
			// The first label always starts the first basic block 
			this.addNextLabelForInstructionBlockStart(insn);
			this.isFirstInstruction = false;
		}
		this.analyseForBasicBlocks(insn);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void analyseTryCatchBlock(final TryCatchBlockNode tryCatchNode) {
		// TODO: start and end of an exception mark a region in which the 
		// assumption of basic blocks is not valid as the block may be left
		// at any time
		addLabelForInstructionBlockStart(tryCatchNode.start);
		addLabelForInstructionBlockStart(tryCatchNode.handler);
	}

	/**
	 * Look for basic block defining instructions.
	 * @param insn Instruction to analyse.
	 */
	@SuppressWarnings("unchecked")
	private void analyseForBasicBlocks(final AbstractInsnNode insn) {
		if(insn instanceof JumpInsnNode) {
			JumpInsnNode jump = (JumpInsnNode)insn;
			addLabelForInstructionBlockStart(jump.label);
			// for conditional jumps, we need to start a new basic block after
			// the comparison because the jump ends the basic block, yet the 
			// following instruction is not "jumped" to, but executed in normal 
			// order if the comparison results in false
			if(jump.getOpcode() == Opcodes.IF_ACMPEQ
					|| jump.getOpcode() == Opcodes.IF_ACMPNE
					|| jump.getOpcode() == Opcodes.IF_ICMPEQ
					|| jump.getOpcode() == Opcodes.IF_ICMPGE
					|| jump.getOpcode() == Opcodes.IF_ICMPGT
					|| jump.getOpcode() == Opcodes.IF_ICMPLE
					|| jump.getOpcode() == Opcodes.IF_ICMPLT
					|| jump.getOpcode() == Opcodes.IF_ICMPNE
					|| jump.getOpcode() == Opcodes.IFEQ
					|| jump.getOpcode() == Opcodes.IFGE
					|| jump.getOpcode() == Opcodes.IFGT
					|| jump.getOpcode() == Opcodes.IFLE
					|| jump.getOpcode() == Opcodes.IFLT
					|| jump.getOpcode() == Opcodes.IFNE
					|| jump.getOpcode() == Opcodes.IFNONNULL
					|| jump.getOpcode() == Opcodes.IFNULL) {
				addNextLabelForInstructionBlockStart(insn.getNext());
			}
		} else if(insn instanceof LookupSwitchInsnNode) {
			LookupSwitchInsnNode switchNode = (LookupSwitchInsnNode)insn;
			// add the label for the default handler block
			addLabelForInstructionBlockStart(switchNode.dflt);
			
			// add the labels for the case handler blocks
			ListIterator<LabelNode> it = switchNode.labels.listIterator();
			while(it.hasNext()) {
				LabelNode l = it.next();
				addLabelForInstructionBlockStart(l);
			}
		} else if(insn instanceof TableSwitchInsnNode) {
			TableSwitchInsnNode switchNode = (TableSwitchInsnNode)insn;
			// add the label for the default handler block
			addLabelForInstructionBlockStart(switchNode.dflt);
			
			// add the labels for the case handler blocks
			ListIterator<LabelNode> it = switchNode.labels.listIterator();
			while(it.hasNext()) {
				LabelNode l = it.next();
				addLabelForInstructionBlockStart(l);
			}
		} else if(insn instanceof MethodInsnNode) {
			// Add the invocation to the basic block and start a new basic block
			// after the invocation.
			addNextLabelForInstructionBlockStart(insn.getNext());
		}
	}
	
	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	public void postAnalysisEvent(InsnList instructions) {
		final InstructionBlockDescriptor[] basicBlocks = this.constructInstructionBlocksArray(instructions.iterator());
		instrumentationState.getInstrumentationContext().getBasicBlocks().addInstructionBlocksForMethod(
				methodDescriptorString, 
				basicBlocks);
		this.instrumentationState.setBasicBlockLabels(
				this.instructionBlockLabels.toArray(new Label[this.instructionBlockLabels.size()]));
	}
}
