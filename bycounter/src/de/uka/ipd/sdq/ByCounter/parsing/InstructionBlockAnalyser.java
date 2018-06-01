package de.uka.ipd.sdq.ByCounter.parsing;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

import de.uka.ipd.sdq.ByCounter.instrumentation.IInstructionAnalyser;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationState;

/**
 * An implementation of {@link IInstructionAnalyser} that analyses instructions 
 * in order to find instruction blocks.  
 *
 */
public abstract class InstructionBlockAnalyser implements IInstructionAnalyser {
	
	/**
	 * Labels that start an instruction block.
	 */
	protected List<Label> instructionBlockLabels;
	
	/** Logger for logging output. */
	protected Logger log;

	/**
	 * The instrumentation state that define how to analyse the method.
	 */
	protected InstrumentationState instrumentationState;
	
	/** Descriptor of the analysed method */
	protected String methodDescriptorString;

	
	/**
	 * Construct the instruction block analyser. Needed before each method.
	 * @param methodDescriptorString Descriptor of the analysed method.
	 * @param instrumentationState Structure for instrumentation results.
	 */
	public InstructionBlockAnalyser(String methodDescriptorString,
			InstrumentationState instrumentationState) {
		this.log = Logger.getLogger(this.getClass().getCanonicalName());
		this.instructionBlockLabels = new LinkedList<Label>();
		this.methodDescriptorString = methodDescriptorString;
		this.instrumentationState = instrumentationState;
	}
	
	/**
	 * Add the given Label to the list of labels starting an instruction block.
	 * @see #instructionBlockLabels
	 * @param l Label.
	 */
	protected void addLabelForInstructionBlockStart(LabelNode l) {
		if(l != null && !this.instructionBlockLabels.contains(l.getLabel())) {
			this.instructionBlockLabels.add(l.getLabel());
		}
	}	

	/**
	 * Does {@link #addLabelForInstructionBlockStart(LabelNode)} for the {@link LabelNode}
	 * instruction that is insn or the first {@link LabelNode} that follows 
	 * after the given instruction.
	 * @param insn Instruction after which to look for a {@link LabelNode}.
	 */
	protected void addNextLabelForInstructionBlockStart(final AbstractInsnNode insn) {
		AbstractInsnNode currentInsn = insn;
		while(currentInsn != null 
				&& !(currentInsn instanceof LabelNode)) {
			currentInsn = currentInsn.getNext();
		}
		addLabelForInstructionBlockStart((LabelNode) currentInsn);
	}
	
	/**
	 * Walk through the instructions and use the {@link #instructionBlockLabels} to
	 * construct instruction blocks.
	 * @param instructions The list of instructions in the analysed method.
	 * @return The constructed instruction blocks. Line numbers are not set!
	 * @see #constructInstructionBlocksArray(Iterator)
	 */
	protected List<InstructionBlockLocation> constructInstructionBlocks(Iterator<AbstractInsnNode> instructionIterator) {
		Label[] labels = this.instructionBlockLabels.toArray(new Label[this.instructionBlockLabels.size()]);
		
		List<InstructionBlockLocation> instructionBlocks = new LinkedList<InstructionBlockLocation>();
		InstructionBlockDescriptor currentIbDesc = new InstructionBlockDescriptor();

		// go through all instructions again
		for (	Iterator<AbstractInsnNode> iterator = instructionIterator; 
				iterator.hasNext();
			) {
			AbstractInsnNode insn = iterator.next();
			
			// look for labels that are marked to start an instruction block
			if(insn instanceof LabelNode) {
				int labelIndex = -1;
				// check whether this label starts a new instruction block
				for(int i = 0; i < labels.length; i++) {
					if(((LabelNode) insn).getLabel().equals(labels[i])) {
						labelIndex = i;
						break;
					}
				}
				if(labelIndex >= 0) {
					currentIbDesc = new InstructionBlockDescriptor();
					InstructionBlockLocation loc = new InstructionBlockLocation();
					loc.label = labels[labelIndex];
					loc.labelBlock = currentIbDesc;
					currentIbDesc.setBlockIndex(labelIndex);
					instructionBlocks.add(loc);
				}
			} else {
				// add the instruction to the instruction block
				InstructionBlockDescriptor.addInstruction(currentIbDesc, insn);
			}
		}
		Collections.sort(instructionBlocks, new Comparator<InstructionBlockLocation>() {

			@Override
			public int compare(InstructionBlockLocation o1,
					InstructionBlockLocation o2) {
				return ((Integer)o1.labelBlock.getBlockIndex()).compareTo(o2.labelBlock.getBlockIndex());
			}
		});
		
		return instructionBlocks;
	}

	/**
	 * @param instructionIterator Instructions of the method.
	 * @return {@link InstructionBlockDescriptor} array with the instruction 
	 * blocks.
	 * @see #constructInstructionBlocks(Iterator)
	 */
	protected InstructionBlockDescriptor[] constructInstructionBlocksArray(Iterator<AbstractInsnNode> instructionIterator) {
		List<InstructionBlockLocation> labelBlockLocations = this.constructInstructionBlocks(instructionIterator);
		InstructionBlockDescriptor[] labelBlocks = new InstructionBlockDescriptor[labelBlockLocations.size()];
		int i = 0;
		for(final InstructionBlockLocation loc : labelBlockLocations) {
			labelBlocks[i] = loc.labelBlock;
			i++;
		}
		return labelBlocks;
	}
}
