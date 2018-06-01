package de.uka.ipd.sdq.ByCounter.parsing;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import de.uka.ipd.sdq.ByCounter.instrumentation.IInstructionAnalyser;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationState;

/**
 * An implementation of {@link IInstructionAnalyser} that analyses instructions 
 * in order to find label blocks and the instructions they consist of.
 * @author Martin Krogmann
 *
 */
public class LabelBlockAnalyser extends InstructionBlockAnalyser {

	/**
	 * Construct the label block analyser. 
	 * @param methodDescriptorString Descriptor of the analysed method.
	 * @param instrumentationState Structure for instrumentation results.
	 */
	public LabelBlockAnalyser(String methodDescriptorString,
			InstrumentationState instrumentationState) {
		super(methodDescriptorString, instrumentationState);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void analyseInstruction(AbstractInsnNode insn) {
		// add all labels
		if(insn instanceof LabelNode) {
			addLabelForInstructionBlockStart((LabelNode) insn);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void analyseTryCatchBlock(TryCatchBlockNode tryCatchNode) {
		addLabelForInstructionBlockStart(tryCatchNode.start);
		addLabelForInstructionBlockStart(tryCatchNode.handler);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void postAnalysisEvent(InsnList instructions) {
		InstructionBlockDescriptor[] labelBlocks = this.constructInstructionBlocksArray(instructions.iterator());
		instrumentationState.getInstrumentationContext().getLabelBlocks().addInstructionBlocksForMethod(
				methodDescriptorString, 
				labelBlocks);
		this.instrumentationState.setBasicBlockLabels(
				this.instructionBlockLabels.toArray(new Label[this.instructionBlockLabels.size()]));
	}
}
