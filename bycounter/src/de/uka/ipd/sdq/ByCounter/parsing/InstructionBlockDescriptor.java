package de.uka.ipd.sdq.ByCounter.parsing;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;

import de.uka.ipd.sdq.ByCounter.execution.CountingResultBase;
import de.uka.ipd.sdq.ByCounter.utils.FullOpcodeMapper;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * Descriptor for a instruction blocks in Java bytecode.
 * @author Martin Krogmann
 *
 */
public class InstructionBlockDescriptor implements Serializable {

	/**
	 * Version for the {@link Serializable} implementation.
	 */
	private static final long serialVersionUID = 1L;
	
	private int blockIndex;
	private int[] opcodeCounts;
	private Map<String, Integer> methodCallCounts;
	
	public InstructionBlockDescriptor() {
		opcodeCounts = new int[CountingResultBase.MAX_OPCODE];
		methodCallCounts = new HashMap<String, Integer>();
	}
	
	/**
	 * Theblock index is an identifier for the local variable in 
	 * bytecode that is associated to this instruction block.
	 * @param blockIndex the blockIndex to set
	 */
	public void setBlockIndex(int blockIndex) {
		this.blockIndex = blockIndex;
	}
	
	/**
	 * The block index is an identifier for the local variable in 
	 * bytecode that is associated to this instruction block.
	 * @return the basicIndex
	 */
	public int getBlockIndex() {
		return blockIndex;
	}

	/**
	 * Opcode counts is an array where at the index of each opcode the
	 * number of occurrences of that opcode is the value.
	 * @return the opcodeCounts
	 */
	public int[] getOpcodeCounts() {
		return opcodeCounts;
	}

	/**
	 * Method call counts is a map containing all occurring method calls as 
	 * keys and the number of occurrences as values.
	 * @return the methodCallCounts
	 */
	public Map<String, Integer> getMethodCallCounts() {
		return methodCallCounts;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder opcodeCountsStr = new StringBuilder();
		opcodeCountsStr.append("{");
		for(int opcode = 0; opcode < this.opcodeCounts.length; opcode++) {
			if(opcodeCounts[opcode] != 0) {
				opcodeCountsStr.append(FullOpcodeMapper.getMnemonicOfOpcode(opcode));
				opcodeCountsStr.append("=" + this.opcodeCounts[opcode] + ", ");
			}
		}
		opcodeCountsStr.append("}");
		return "InstructionBlockDescriptor [blockIndex=" + this.blockIndex
				+ ", methodCallCounts=" + this.methodCallCounts
				+ ", opcodeCounts=" + opcodeCountsStr.toString() + "]";
	}
	

	/**
	 * Adds an instruction to the {@link InstructionBlockDescriptor}.
	 * @param currentIBDesc The {@link InstructionBlockDescriptor} to add to.
	 * @param insn The instruction to add.
	 */
	public static void addInstruction(InstructionBlockDescriptor currentIBDesc,
			AbstractInsnNode insn) {
		if(insn instanceof LineNumberNode 
				|| insn instanceof FrameNode) {
			return;
		} else if(insn instanceof MethodInsnNode) {
			// add the method call details
			final MethodInsnNode mNode = (MethodInsnNode) insn;
			final String mId = 
				MethodDescriptor._constructMethodDescriptorFromASM(
						mNode.owner, mNode.name, mNode.desc).getCanonicalMethodName();
			addMethodCount(currentIBDesc, mId, 1);
		}
		// add the instruction opcode
		addOpcodeCount(currentIBDesc, insn.getOpcode(), 1);
	}

	private static void addOpcodeCount(InstructionBlockDescriptor currentIBDesc,
			int opcode, int countToAdd) {
		int prevCount = currentIBDesc.getOpcodeCounts()[opcode];
		currentIBDesc.getOpcodeCounts()[opcode] = prevCount + countToAdd;
	}

	private static void addMethodCount(InstructionBlockDescriptor currentIBDesc,
			final String mId, int countToAdd) {
		Integer prevCount = currentIBDesc.getMethodCallCounts().get(mId);
		prevCount = (prevCount == null ? 0 : prevCount);
		currentIBDesc.getMethodCallCounts().put(mId, prevCount + countToAdd);
	}

	/**
	 * Adds instruction counts of the given block to this block.
	 * Ignores block index.
	 * @param instructionBlockDescriptor
	 */
	public void add(InstructionBlockDescriptor instructionBlockDescriptor) {
		for(String mId : instructionBlockDescriptor.methodCallCounts.keySet()) {
			addMethodCount(this, mId, instructionBlockDescriptor.methodCallCounts.get(mId));
		}
		
		for(int opcode = 0; opcode < instructionBlockDescriptor.opcodeCounts.length; opcode++) {
			addOpcodeCount(this, opcode, instructionBlockDescriptor.opcodeCounts[opcode]);
		}
		
	}

	/**
	 * @param bb1 An {@link InstructionBlockDescriptor}.
	 * @param bb2 An {@link InstructionBlockDescriptor}.
	 * @return A new {@link InstructionBlockDescriptor} containing the count
	 * difference of bb1 and bb2, i.e. bb1-bb2.
	 */
	public static InstructionBlockDescriptor subtract(
			InstructionBlockDescriptor bb1, InstructionBlockDescriptor bb2) {
		InstructionBlockDescriptor result = new InstructionBlockDescriptor();
		// subtract each opcode count
		for(int i = 0; i < bb1.opcodeCounts.length; i++) {
			result.opcodeCounts[i] = bb1.opcodeCounts[i] - bb2.opcodeCounts[i];
		}
		// subtract method call counts
		for(String method : bb2.methodCallCounts.keySet()) {
			int bb2Value = bb2.methodCallCounts.get(method);
			Integer bb1Value = bb1.methodCallCounts.get(method);
			if(bb1Value == null) {
				bb1Value = 0;
			}
			result.methodCallCounts.put(method, bb1Value-bb2Value);
		}
		return result;
	}
	
	/**
	 * @return True if neither opcode offsets nor methodCallCount offsets exist.
	 * False otherwise.
	 */
	public boolean isEmpty() {
		if(!methodCallCounts.isEmpty()) {
			return false;
		}
		if(opcodeCounts.length == 0) {
			return true;
		}
		for(int i : opcodeCounts) {
			if(i != 0) {
				return false;
			}
		}
		return true;
	}
}