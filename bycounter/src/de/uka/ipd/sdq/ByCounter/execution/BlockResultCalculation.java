package de.uka.ipd.sdq.ByCounter.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.uka.ipd.sdq.ByCounter.instrumentation.BlockCountingMode;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationContext;
import de.uka.ipd.sdq.ByCounter.parsing.InstructionBlockDescriptor;
import de.uka.ipd.sdq.ByCounter.parsing.RangeBlockDescriptor;
import de.uka.ipd.sdq.ByCounter.parsing.RangeBlockDescriptor.BasicBlockOffset;

/**
 * Class class provides functions to calculate counting results for 
 * instrumentation results based on range block or basic block definitions.
 * @author Martin Krogmann
 *
 */
public class BlockResultCalculation {
	

	/**
	 * see http://en.wikipedia.org/wiki/Data_log
	 */
	private Logger log;

	/** Instrumentation context containing basic block and range block definitions. */
	private InstrumentationContext instrumentationContext;	
	/** The basic blocks defined for the current method */
	private InstructionBlockDescriptor[] currentBasicBlocks;
	/** The range blocks defined for the current method */
	private InstructionBlockDescriptor[] currentRangeBlocks;
	/** The label blocks defined for the current method */
	private InstructionBlockDescriptor[] currentLabelBlocks;
	
	/**
	 * New {@link BlockResultCalculation} context.
	 * @param instrumentationContext {@link InstrumentationContext} that lead 
	 * to the production of the results.
	 */
	public BlockResultCalculation(InstrumentationContext instrumentationContext) {
		this.log = Logger.getLogger(this.getClass().getCanonicalName());
		this.instrumentationContext = instrumentationContext;
		this.currentBasicBlocks = null;
		this.currentLabelBlocks = null;
		this.currentRangeBlocks = null;
	}
	
	/**
	 * This method uses the execution frequencies of basic blocks and calculates 
	 * from these the actual opcode counts and method call counts.
	 * @param qualifyingMethodName The descriptor of the method. This is the 
	 * <code>canonicalClassName + "." + methodName + methodSignatureDescriptor
	 * </code>.
	 * @param basicBlockExecutionCounts The basic block execution frequencies.
	 * @param prevOpcodeCounts This array of opcode counters will be 
	 * incremented for opcodes in executed basic blocks if 
	 * calculateIndividualResults is false. 
	 * @param prevMethodCallCounts This map of method counters will be 
	 * incremented for method executions in executed basic blocks if 
	 * calculateIndividualResults is false.
	 */
	public synchronized CalculatedCounts calculateCountsFromBBCounts(
			final String qualifyingMethodName,
			final long[] basicBlockExecutionCounts,
			final long[] prevOpcodeCounts,
			final SortedMap<String, Long> prevMethodCallCounts) {
		// the string operations are expensive, so guard them by the log level.
		Level logLevel = log.getLevel();
		if(logLevel != null && logLevel.intValue() <= Level.FINE.intValue()) {
			log.fine("opcodeCounts for calculateCountsFromBBCounts: "+Arrays.toString(basicBlockExecutionCounts));
		}
		
		this.loadUpdatedBlockDefinitions(qualifyingMethodName, true, false, false);

		CalculatedCounts counts = null;
		counts = new CalculatedCounts();
		counts.opcodeCounts = prevOpcodeCounts;
		counts.methodCounts = prevMethodCallCounts;

		// go through all basic blocks
		for(InstructionBlockDescriptor blockDesc : this.currentBasicBlocks) {
			int bbIndex = blockDesc.getBlockIndex();
			if(bbIndex >= basicBlockExecutionCounts.length) {
				// this can happen if the basic block is never executed?
				// no actually this should not happen at all because the array
				// is of the full size no matter what is executed.
				continue;
			}

			long bbCount = basicBlockExecutionCounts[bbIndex];
			if(bbCount == 0) {
				continue;
			}
			// add opcode counts
			counts.addOpcodeCounts(blockDesc.getOpcodeCounts(), bbCount);
			// add method call counts
			counts.addMethodCallCounts(blockDesc.getMethodCallCounts(), bbCount);
		}
		return counts;
	}


	/**
	 * This method uses the execution frequencies of basic blocks and calculates 
	 * from these the actual opcode counts and method call counts for the 
	 * specified range blocks.
	 * @param qualifyingMethodName The descriptor of the method. This is the 
	 * <code>canonicalClassName + "." + methodName + methodSignatureDescriptor
	 * </code>.
	 * @param basicBlockExecutionCounts The basic block execution frequencies.
	 * @param prevOpcodeCounts This array of opcode counters will be 
	 * incremented for opcodes in executed basic blocks if 
	 * calculateIndividualResults is false. 
	 * @param prevMethodCallCounts This map of method counters will be 
	 * incremented for method executions in executed basic blocks if 
	 * calculateIndividualResults is false.
	 */
	public synchronized CalculatedCounts[] calculateCountsFromRBCounts(
			final String qualifyingMethodName,
			final long[] basicBlockExecutionCounts,
			final long[] prevOpcodeCounts,
			final SortedMap<String, Long> prevMethodCallCounts) {
		// the string operations are expensive, so guard them by the log level.
		Level logLevel = log.getLevel();
		if(logLevel != null && logLevel.intValue() <= Level.FINE.intValue()) {
			log.fine("opcodeCounts for calculateCountsFromRBCounts: "+Arrays.toString(basicBlockExecutionCounts));
		}

		this.loadUpdatedBlockDefinitions(qualifyingMethodName, true, true, false);
				
		ArrayList<CalculatedCounts> resultList = new ArrayList<CalculatedCounts>();
		for(InstructionBlockDescriptor currentRB  : this.currentRangeBlocks) {
			CalculatedCounts rbCounts = 
				getCountsForRangeBlock((RangeBlockDescriptor) currentRB, basicBlockExecutionCounts);
			if(rbCounts != null) {
				resultList.add(rbCounts);
			}
			
		}
		return resultList.toArray(new CalculatedCounts[resultList.size()]);
	}

	/**
	 * Load the block definitions from the {@link #instrumentationContext}
	 * to set 
	 * {@link #currentBasicBlocks} and {@link #currentRangeBlocks}.
	 * @param qualifyingMethodName Current method.
	 * @param loadBasicBlocks False skips basic blocks.
	 * @param loadRangeBlocks False skips range blocks.
	 * @param loadRangeBlocks False skips label blocks.
	 */
	private void loadUpdatedBlockDefinitions(
			final String qualifyingMethodName,
			final boolean loadBasicBlocks, 
			final boolean loadRangeBlocks,
			final boolean loadLabelBlocks) {
		instrumentationContext = InstrumentationContext.loadFromDefaultPath(); //bck
		//instrumentationContext = this.instrumentationContext.
		if(loadBasicBlocks) {
			currentBasicBlocks = instrumentationContext.getBasicBlocks().getInstructionBlocksByMethod().get(qualifyingMethodName);
			if(currentBasicBlocks == null) {
				throw new IllegalStateException("Could not find the basic block definition for the method '" 
						+ qualifyingMethodName + "'");
			}
		}
		if(loadRangeBlocks) {
			currentRangeBlocks = instrumentationContext.getRangeBlocks().getInstructionBlocksByMethod().get(qualifyingMethodName);
			if(currentRangeBlocks == null) {
				throw new IllegalStateException("Could not find the range block definition for the method '" 
						+ qualifyingMethodName + "'");
			}
		}
		if(loadLabelBlocks) {
			currentLabelBlocks = instrumentationContext.getLabelBlocks().getInstructionBlocksByMethod().get(qualifyingMethodName);
			if(currentLabelBlocks == null) {
				throw new IllegalStateException("Could not find the range block definition for the method '" 
						+ qualifyingMethodName + "'");
			}
		}
	}

	/**
	 * Uses the results blockExecutionSequence to calculate counting results.
	 * @param result Result as reported by an instrumented method.
	 * @return {@link CalculatedCounts}.
	 */
	public CalculatedCounts[] calculateCountsFromBlockExecutionSequence(
			final ProtocolCountStructure result) {
		
		this.loadUpdatedBlockDefinitions(result.qualifyingMethodName, 
				result.blockCountingMode != BlockCountingMode.LabelBlocks, 
				result.blockCountingMode == BlockCountingMode.RangeBlocks,
				result.blockCountingMode == BlockCountingMode.LabelBlocks);
		

		// possibly more than one counting result per item in the sequence
		List<CalculatedCounts> resultCounts = new ArrayList<CalculatedCounts>();
		
		if(result.blockCountingMode == BlockCountingMode.BasicBlocks) {
			// just add the complete basic block
			CalculatedCounts c = new CalculatedCounts();
			c.init();
			for(Integer blockIndex : result.blockExecutionSequence) {
				c.addMethodCallCounts(this.currentBasicBlocks[blockIndex].getMethodCallCounts(), 1);
				c.addOpcodeCounts(this.currentBasicBlocks[blockIndex].getOpcodeCounts(), 1);
			}
			resultCounts.add(c);
		} else if(result.blockCountingMode == BlockCountingMode.LabelBlocks) {
			// label blocks
			// just add the complete label block
			CalculatedCounts c = new CalculatedCounts();
			c.init();
			for(Integer blockIndex : result.blockExecutionSequence) {
				c.addMethodCallCounts(this.currentLabelBlocks[blockIndex].getMethodCallCounts(), 1);
				c.addOpcodeCounts(this.currentLabelBlocks[blockIndex].getOpcodeCounts(), 1);
			}
			resultCounts.add(c);
		} else if(result.blockCountingMode == BlockCountingMode.RangeBlocks) {
			// range blocks
			final Map<Integer, List<RangeBlockDescriptor>> rangeBlocksByBasicBlock = 
				getRangeBlocksByBasicBlock();
			
			// A list of active range blocks, i.e. blocks where the currently 
			// considered block (index) in the execution sequence is part of the range block
			List<RangeBlocksBBExecutionCounts> currentRBECs = new LinkedList<RangeBlocksBBExecutionCounts>();
			
			for(Integer blockIndex : result.blockExecutionSequence) {

				List<RangeBlockDescriptor> rangeBlocksContainingBBblockIndex = 
					rangeBlocksByBasicBlock.get(blockIndex);	// can be null!
				
				// list of range blocks that end
				List<RangeBlocksBBExecutionCounts> toRemoveFromCurrentRBs = new LinkedList<BlockResultCalculation.RangeBlocksBBExecutionCounts>();
				
				// 1. Find active range blocks that do not contain the new basic block
				// or have offsets for the basic block.
				// These range blocks end.
				for(RangeBlocksBBExecutionCounts rbec : currentRBECs) {
					if(rangeBlocksContainingBBblockIndex == null
							|| !rangeBlocksContainingBBblockIndex.contains(rbec.rbd)
							|| rbec.rbd.getBasicBlockIndexesWithOffsets().contains(blockIndex)) {
						// range block ends
						resultCounts.add(getCountsForRangeBlock(rbec.rbd, rbec.basicBlockExecutionCounts));
						// mark for removal
						toRemoveFromCurrentRBs.add(rbec);
					}
				}
				
				// actually remove the range blocks from the list
				currentRBECs.removeAll(toRemoveFromCurrentRBs);
				
				// 2. Find range blocks that become active
				
				// no new range block can start here if no range block contains the bb
				if(rangeBlocksContainingBBblockIndex == null) {
					continue;
				}
				
				// find inactive range blocks that contain the new basic block
				for(RangeBlockDescriptor rbd : rangeBlocksContainingBBblockIndex) {
					if(!findRangeBlockDescriptorInList(currentRBECs, rbd)) {
						// a new range block started
						RangeBlocksBBExecutionCounts newRB = new RangeBlocksBBExecutionCounts();
						newRB.rbd = rbd;
						newRB.basicBlockExecutionCounts = new long[this.currentBasicBlocks.length];
						currentRBECs.add(newRB);
					}
				}
				// add the executed basic block to the execution counts of active range blocks
				for(RangeBlocksBBExecutionCounts rb : currentRBECs) {
					rb.basicBlockExecutionCounts[blockIndex] += 1;
				}
			}
			
			// Add range block execution counts that are still active 
			// (i.e. have not been removed from the active list)
			for(RangeBlocksBBExecutionCounts rbec : currentRBECs) {
				resultCounts.add(getCountsForRangeBlock(rbec.rbd, rbec.basicBlockExecutionCounts));
			}
			
			resultCounts = sortResultsByRangeExecutionOrder((ArrayList<CalculatedCounts>)resultCounts, result.rangeBlockExecutionSequence);
			
		}
		return resultCounts.toArray(new CalculatedCounts[resultCounts.size()]);
	}

	/**
	 * Since the execution order of basic blocks is insufficient to calculate 
	 * the correct order of range block execution in all cases, use the explicit 
	 * range block execution sequence to sort the results in execution order.
	 * @param resultCounts Results, only sorted by basic block execution order.
	 * @param rangeBlockExecutionSequence Correct order of range block 
	 * executions.
	 */
	private List<CalculatedCounts> sortResultsByRangeExecutionOrder(
			ArrayList<CalculatedCounts> resultCounts,
			ArrayList<Integer> rangeBlockExecutionSequence) {
		// For region updates, there is no 1:1 mapping from results to entries 
		// in the rangeBlockExecution sequence. Therefore use stable sorting.
		if(resultCounts.size() < rangeBlockExecutionSequence.size()) {
			throw new IllegalStateException("Not enough range block result results.");
		}
		LinkedList<CalculatedCounts> orderedResult = new LinkedList<CalculatedCounts>();
		for(int rbIndex : rangeBlockExecutionSequence) {
			int foundIndex = -1;
			int i = 0; // index in resultCounts
			for(CalculatedCounts cc : resultCounts) {
				if(cc.indexOfRangeBlock == rbIndex) {
					// move the result to the ordered list
					orderedResult.add(cc);
					foundIndex = i;
					break;
				}
				i++;
			}
			if(foundIndex >= 0) {
				resultCounts.remove(foundIndex);
			} else {
				// when this happens, the algorithm above is probably broken
				throw new IllegalStateException("Could not find range block index in the computed range block results.");
			}
		}
		return orderedResult;
	}

	/**
	 * Find a {@link RangeBlocksBBExecutionCounts} instance with the given 
	 * {@link RangeBlockDescriptor} in the given list.
	 * @param currentRBECs List of {@link RangeBlocksBBExecutionCounts} to search.
	 * @param rbd {@link RangeBlockDescriptor} to find.
	 * @return True when found, false otherwise.
	 */
	private boolean findRangeBlockDescriptorInList(
			List<RangeBlocksBBExecutionCounts> currentRBECs,
			RangeBlockDescriptor rbd) {
		for(RangeBlocksBBExecutionCounts rbec : currentRBECs) {
			if(rbec.rbd.equals(rbd)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the opcode counts and method call counts, i.e. CalculatedCounts 
	 * for the specified range block.
	 * <p>Assumes {@link #loadUpdatedBlockDefinitions(String, boolean, boolean)}
	 * was called appropriately.</p>
	 * @param rb Range block to get the counts for.
	 * @param basicBlockExecutionCounts The execution counts for the defined 
	 * basic blocks.
	 * @return An instance of {@link CalculatedCounts} if the range block was 
	 * actually executed. Null otherwise.
	 */
	private CalculatedCounts getCountsForRangeBlock(
			final RangeBlockDescriptor rb, 
			final long[] basicBlockExecutionCounts) {
		CalculatedCounts result = new CalculatedCounts();
		result.init();
		result.indexOfRangeBlock = rb.getBlockIndex();

		// go through all basic blocks and add the counts if the basic block is part of the range block
		for(InstructionBlockDescriptor blockDesc : this.currentBasicBlocks) {
			int bbIndex = blockDesc.getBlockIndex();
			if(bbIndex >= basicBlockExecutionCounts.length) {
				// this can happen if the basic block is never executed?
				// no actually this should not happen at all because the array
				// is of the full size no matter what is executed.
				// TODO: eliminate
				throw new RuntimeException("Basic block index is too big.");
			}
			
			// count of the currently handled BB in the currently handled RB
			int currentRBBBCount = rb.getBasicBlockCounts()[bbIndex];
			if(currentRBBBCount == 0) {
				// basic block is not part of rb
				continue;
			}
			
			// number of times the basic block was executed * number of times the basic block is part of the range block
			long bbCount = basicBlockExecutionCounts[bbIndex] * currentRBBBCount;
			if(bbCount == 0) {
				continue;
			}
			
			// add opcode counts
			result.addOpcodeCounts(blockDesc.getOpcodeCounts(), bbCount);
			// add method call counts
			result.addMethodCallCounts(blockDesc.getMethodCallCounts(), bbCount);
			
			// find basic block offsets for the basic block
			for(BasicBlockOffset bbOffset : rb.getBasicBlockOffsets()) {
				if(bbOffset == null || bbOffset.offset == null) {
					continue;
				}
				if(bbOffset.basicBlockIndex == bbIndex) {
					// there is an offset for this basic block; apply it
					result.addOpcodeCounts(bbOffset.offset.getOpcodeCounts(), bbCount);
					result.addMethodCallCounts(bbOffset.offset.getMethodCallCounts(), bbCount);
					// purge methods that are not actually part of the range block (but of the basic block)
					for(Entry<String, Integer> methodOffset : bbOffset.offset.getMethodCallCounts().entrySet()) {
						final String methodName = methodOffset.getKey();
						final Integer methodCountOffset = methodOffset.getValue();
						final int countInBB = blockDesc.getMethodCallCounts().get(methodName);
						if(countInBB + methodCountOffset == 0) {
							// method is not in range block; remove entry
							result.methodCounts.remove(methodName);
						}
					}
				}
			}
		}
		
		return result;
	}

	/**
	 * @return A map that holds the range blocks sorted by the basic blocks of 
	 * which they consist.
	 */
	private Map<Integer, List<RangeBlockDescriptor>> getRangeBlocksByBasicBlock() {
		HashMap<Integer, List<RangeBlockDescriptor>> resultMap = new HashMap<Integer, List<RangeBlockDescriptor>>();

		for(InstructionBlockDescriptor rangeBlockI : this.currentRangeBlocks) {
			RangeBlockDescriptor rangeBlock = (RangeBlockDescriptor)rangeBlockI;
			
			int basicBlockIndex = 0;
			for(Integer basicBlockCount : rangeBlock.getBasicBlockCounts()) {
				if(basicBlockCount == 0) {
					// basic block is 0 times in range block -> skip
					basicBlockIndex++;
					continue;
				}
				// create a new list for the basic block
				if(!resultMap.containsKey(basicBlockIndex)) {
					List<RangeBlockDescriptor> rbList = new LinkedList<RangeBlockDescriptor>();
					resultMap.put(basicBlockIndex, rbList);
				}
				// add the range block to the list
				resultMap.get(basicBlockIndex).add(rangeBlock);
				// next basic block
				basicBlockIndex++;
			}
		}
		
		
		return resultMap;
	}
	
	private class RangeBlocksBBExecutionCounts {
		public RangeBlockDescriptor rbd;
		long[] basicBlockExecutionCounts;
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("RangeBlocksBBExecutionCounts [rbd=");
			builder.append(this.rbd);
			builder.append(", basicBlockExecutionCounts=");
			builder.append(Arrays.toString(this.basicBlockExecutionCounts));
			builder.append("]");
			return builder.toString();
		}
	}
}
