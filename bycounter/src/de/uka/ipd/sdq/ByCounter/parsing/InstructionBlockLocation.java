package de.uka.ipd.sdq.ByCounter.parsing;

import org.objectweb.asm.Label;

/**
 * This structure describes the location of an {@link InstructionBlockDescriptor}
 * for the instructions under a {@link Label} 
 * in a method by the label and optionally a line number.
 */
public class InstructionBlockLocation {
	public Label label = null;
	public int lineNumber = -1;
	public InstructionBlockDescriptor labelBlock = null;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "InstructionBlockLocation [label=" + this.label
				+ ", lineNumber=" + this.lineNumber + ", labelBlock="
				+ this.labelBlock + "]";
	}
}
