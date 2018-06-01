package de.uka.ipd.sdq.ByCounter.parsing;

import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * This class is used to serialise and deserialise instruction block definitions 
 * of an instrumentation run.
 */
public final class InstructionBlockSerialisation implements Serializable {
	
	/**
	 * Serialisation version.
	 */
	private static final long serialVersionUID = 1L;
	
	private HashMap<String, InstructionBlockDescriptor[]> instructionBlocksByMethod;
	
	public InstructionBlockSerialisation() {
		this.instructionBlocksByMethod = new HashMap<String, InstructionBlockDescriptor[]>();
	}
	
	@Override
	public String toString() {
		return "InstructionBlockSerialization["+this.instructionBlocksByMethod+"]";
	}

	/**
	 * Prints the instruction blocks in the loaded serialisation.
	 * @param log Logger to use for printing.
	 */
	public void printInstructionBlocks(Logger log) {
		for(final String method : this.instructionBlocksByMethod.keySet()) {
			log.info(method + ":");
			for(final InstructionBlockDescriptor d : this.instructionBlocksByMethod.get(method)) {
				log.info(d.toString());
			}
		}
	}

	/**
	 * Adds basic block definitions for a given method.
	 * @param method Method description for the method to which the instuction block
	 * belongs.
	 * @param instructionBlocks Instruction blocks to add.
	 * method.
	 */
	public void addInstructionBlocksForMethod(String method, InstructionBlockDescriptor[] instructionBlocks) {
		this.instructionBlocksByMethod.put(method, instructionBlocks);
	}

	/**
	 * @return The instruction blocks by method.
	 */
	public HashMap<String, InstructionBlockDescriptor[]> getInstructionBlocksByMethod() {
		return instructionBlocksByMethod;
	}

}
