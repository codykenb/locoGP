/**
 * 
 */
package de.uka.ipd.sdq.ByCounter.parsing;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * Descriptor for range blocks, i.e. sections of code in methods defined in 
 * {@link LineNumberRange}s using 
 * {@link MethodDescriptor#setCodeAreasToInstrument(LineNumberRange[])}.
 * The descriptor describes the basic blocks of which the range block is made up. 
 * If a range block starts or ends in the middle of a basic block, the 
 * {@link InstructionBlockDescriptor}s in {@link #getBasicBlockOffsets()} 
 * describe the difference in counts.
 * 
 * <p>
 * Results are calculated using the following algorithm (pseudo code):
 * <pre>
 * for each basic block b with instructions counts b.counts and execution count b.c:
 *    result +=  b.c*b.counts
 *    
 *    for each basicBlockOffset bbOffset for basicBlock offsetBB:
 * 		result -= bbOffset*offsetBB.c
 * </pre>
 * </p>
 */
public class RangeBlockDescriptor extends InstructionBlockDescriptor {
	
	/**
	 * Describes which instructions in the basic block with the index
	 * {@link #basicBlockIndex} are added/subtracted in addition to normal 
	 * basic block counts.
	 */
	public class BasicBlockOffset implements Serializable {
		private static final long serialVersionUID = 1L;
		public int basicBlockIndex;
		public InstructionBlockDescriptor offset;
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "BasicBlockOffset [basicBlockIndex=" + this.basicBlockIndex
					+ ", offset=" + this.offset + "]";
		}
	}

	/** Version of the {@link Serializable}.*/
	private static final long serialVersionUID = 1L;
	private int[] basicBlockCounts;
	private List<BasicBlockOffset> bbOffsets;
	
	/**
	 * Construct a new {@link RangeBlockDescriptor}.
	 * @param numOfBasicBlocks The number of basic blocks in the method; used 
	 * for indexing basic block counts in the range block.
	 */
	public RangeBlockDescriptor(final int numOfBasicBlocks) {
		this.basicBlockCounts = new int[numOfBasicBlocks];
		this.bbOffsets = new LinkedList<RangeBlockDescriptor.BasicBlockOffset>();
	}
	
	/**
	 * For {@link RangeBlockDescriptor}, this is unused.
	 */
	@Override
	public Map<String, Integer> getMethodCallCounts() {
		new RuntimeException(new IllegalAccessError("Do not call this."));
		return null;
	}

	/**
	 * For {@link RangeBlockDescriptor}, this is unused.
	 */
	@Override
	public int[] getOpcodeCounts() {
		new RuntimeException(new IllegalAccessError("Do not call this."));
		return null;
	}
	
	@Override
	public void add(InstructionBlockDescriptor instructionBlockDescriptor) {
		new RuntimeException(new IllegalAccessError("Do not call this."));
	};
	
	
	
	public int[] getBasicBlockCounts() {
		return this.basicBlockCounts;
	}

	/**
	 * Sets the counter for the specified basic block to 1.
	 * @param rangeBlockDescriptor The {@link RangeBlockDescriptor} to modify.
	 * @param basicBlockIndex Index of the basic block that has to be 
	 * counted in this rangeBlock.
	 */
	public static void setUsesBasicBlock(RangeBlockDescriptor rangeBlockDescriptor,
			int basicBlockIndex) {
		rangeBlockDescriptor.basicBlockCounts[basicBlockIndex] = 1;
		
	}
	
	/**
	 * @return The offsets defined for this basic block.
 	 */
	public List<BasicBlockOffset> getBasicBlockOffsets() {
		return this.bbOffsets;
	}
	
	/**
	 * @return A list of basic block indices. For each index in the list 
	 * there exists a non-empty basic block offset.
	 * @see #getBasicBlockOffsets()
	 */
	public List<Integer> getBasicBlockIndexesWithOffsets() {
		List<Integer> result = new LinkedList<Integer>();
		for(BasicBlockOffset o : getBasicBlockOffsets()) {
			if(!o.offset.isEmpty()) {
				result.add(o.basicBlockIndex);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RangeBlockDescriptor(" 
				+ this.getBlockIndex() + ") [basicBlockCounts="
				+ Arrays.toString(this.basicBlockCounts) + ", bbOffsets="
				+ this.bbOffsets + "]";
	}

	
}
