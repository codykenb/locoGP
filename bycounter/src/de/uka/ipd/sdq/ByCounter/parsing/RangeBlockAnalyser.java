package de.uka.ipd.sdq.ByCounter.parsing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.InsnList;

import de.uka.ipd.sdq.ByCounter.instrumentation.IInstructionAnalyser;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationContext;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationState;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedCodeArea;
import de.uka.ipd.sdq.ByCounter.parsing.RangeBlockDescriptor.BasicBlockOffset;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * An implementation of {@link IInstructionAnalyser} that analyses instructions 
 * in order to find the basicBlocks that comprise range blocks.
 * Therefore this class depends on {@link InstructionBlockAnalyser}. 
 * The results of this {@link IInstructionAnalyser} are serialised to the file
 * specified by {@link InstrumentationContext#FILE_SERIALISATION_DEFAULT_NAME}.
 *
 */
public final class RangeBlockAnalyser extends LabelBlockAnalyser {

	/**
	 * The ranges as specified by the user as well as those computed 
	 * from regions.
	 */
	private List<InstrumentedCodeArea> codeAreas;
	
	/**
	 * This is a list of line numbers used in the specification of line number 
	 * ranges but not yet found in the visited method.
	 * Used for error finding.
	 */
	private Set<Integer> lineNumbersNotYetFound;
	
	/**
	 * Labels that are visited first when entering a range block.
	 * Maps to the range block index.
	 */
	private Map<Label, Integer> rangeBlockContainsLabels;

	/**
	 * {@link LineNumberAnalyser} used to map {@link Label}s to line numbers.
	 */
	private LineNumberAnalyser lineNumberAnalyser;
	
	/**
	 * Construct the {@link RangeBlockAnalyser} and prepare it for analysing the 
	 * specified method.
	 * @param currentMethod {@link MethodDescriptor} for the method to analyse.
	 * @param instrumentationState State information used for the 
	 * instrumentation.
	 * @param lineNumberAnalyser {@link LineNumberAnalyser} reference.
	 * @param codeAreasForMethod The code areas defined for this method.
	 */
	public RangeBlockAnalyser(
			MethodDescriptor currentMethod, 
			InstrumentationState instrumentationState,
			LineNumberAnalyser lineNumberAnalyser, List<InstrumentedCodeArea> codeAreasForMethod) {
		super(currentMethod.getCanonicalMethodName(), instrumentationState);
		this.codeAreas = codeAreasForMethod;
		this.lineNumberAnalyser = lineNumberAnalyser;
		this.rangeBlockContainsLabels = new HashMap<Label, Integer>();
		
		this.lineNumbersNotYetFound = new HashSet<Integer>();
		// construct the set of all specified line numbers for error checking
		for(InstrumentedCodeArea r : codeAreas) {
			lineNumbersNotYetFound.add(r.getArea().firstLine);
			lineNumbersNotYetFound.add(r.getArea().lastLine);
		}
	}
	
	/**
	 * Walk through the instructions and use the {@link #instructionBlockLabels} to
	 * construct range blocks.
	 * @param labelBlocks Instruction blocks for each label.
	 */
	private RangeBlockDescriptor[] constructRangeBlocks(List<InstructionBlockLocation> labelBlocks) {
		RangeBlockDescriptor[] rangeBlocks = new RangeBlockDescriptor[this.codeAreas.size()];
		boolean[] rangeWasEnteredSinceBasicBlockStarted = new boolean[rangeBlocks.length];
		boolean[] rangeWasLeftSinceBasicBlockStarted = new boolean[rangeBlocks.length];
		for(int i = 0; i < rangeBlocks.length; i++) {
			rangeWasEnteredSinceBasicBlockStarted[i] = false;
		}
		for(int i = 0; i < rangeBlocks.length; i++) {
			rangeWasLeftSinceBasicBlockStarted[i] = false;
		}

		
		// go through all labels by line number and find the basic blocks that 
		// need to be included in a linenumber range
		// basic blocks that are not fully included in a linenumberrange are 
		// still added to the range block but in addition to that, the 
		// instructions that are not in the specified linenumberrange are added 
		// to an offset counter to be able to subtract these from the results.
		if(this.instructionBlockLabels.isEmpty()) {
			// empty method?
			return null;
		}
		
		int currentBasicBlockIndex = 0;
		
		// the current basic block from its start to the currentLine
		InstructionBlockDescriptor partialBB = new InstructionBlockDescriptor();
		
		// initialise range blocks
		for(int i = 0; i < this.codeAreas.size(); i++) {
			rangeBlocks[i] = new RangeBlockDescriptor(
					instrumentationState.getBasicBlockLabels().length);
			rangeBlocks[i].setBlockIndex(i);
		}
				
		// we need the basic blocks to calculate partial blocks
		final InstructionBlockDescriptor[] basicBlocks = 
			this.instrumentationState.getInstrumentationContext().getBasicBlocks()
					.getInstructionBlocksByMethod().get(this.methodDescriptorString);
		
		for(InstructionBlockLocation loc : labelBlocks) {
			final Label currentLabel =  loc.label;
			final int currentLine = this.lineNumberAnalyser.findLabelBlockByLabel(currentLabel).lineNumber;
			

			// look for a basic block start label
			int bbFind = findLabelIndex(instrumentationState.getBasicBlockLabels(), 
					currentLabel);
			if(bbFind >= 0) {
				// a new basic block starts
				currentBasicBlockIndex = bbFind;
				// since a new basic block starts, the old basic block is complete
				// start a new partialBB
				partialBB = new InstructionBlockDescriptor();
				// reset range flags
				for(int i = 0; i < rangeBlocks.length; i++) {
					rangeWasEnteredSinceBasicBlockStarted[i] = false;
				}
				for(int i = 0; i < rangeBlocks.length; i++) {
					rangeWasLeftSinceBasicBlockStarted[i] = false;
				}
			}
			
			// TODO: at this point, currentLine may be -1, so we need to find 
			// to which line the label belongs so we can answer whether the 
			// instructions belong to a range or not.

			// calculate current ranges
			{
				int r = 0;
				for(final InstrumentedCodeArea currentRange : this.codeAreas) {
					if(currentLine >= currentRange.getArea().firstLine
							&& currentLine <= currentRange.getArea().lastLine) {
	
						// add the basic block to all current ranges
						RangeBlockDescriptor.setUsesBasicBlock(
								rangeBlocks[r], currentBasicBlockIndex);
						
						// add the label to the labels of the range
						rangeBlockContainsLabels.put(currentLabel, r); // range r is visited
						
						if(!rangeWasEnteredSinceBasicBlockStarted[r]) {
							// this is the first time we are in this range
							rangeWasEnteredSinceBasicBlockStarted[r] = true;
							
							BasicBlockOffset bbOffset = rangeBlocks[r].new BasicBlockOffset();
							bbOffset.offset = 
								InstructionBlockDescriptor.subtract(	// offset=-partialBB
										new InstructionBlockDescriptor(), partialBB);
							if(!bbOffset.offset.isEmpty()) {
								// no need to add empty offsets
								bbOffset.basicBlockIndex = currentBasicBlockIndex;
								rangeBlocks[r].getBasicBlockOffsets().add(bbOffset);
							}
						}
					} else if(!rangeWasLeftSinceBasicBlockStarted[r]
							&& rangeBlocks[r].getBasicBlockCounts()[currentBasicBlockIndex] != 0) {
						// This label is not part of the range.
						// Handle the partial basic block instructions by					
						// subtracting the instructions that are in the basic block, 
						// but not yet in the partialBB from the range.
						BasicBlockOffset bbOffset = rangeBlocks[r].new BasicBlockOffset();
						// the offset 
						bbOffset.offset = 
							InstructionBlockDescriptor.subtract(
									partialBB, basicBlocks[currentBasicBlockIndex]);
						if(!bbOffset.offset.isEmpty()) {
							// no need to add empty offsets
							bbOffset.basicBlockIndex = currentBasicBlockIndex;
							rangeBlocks[r].getBasicBlockOffsets().add(bbOffset);
						}
						// the range was left
						rangeWasLeftSinceBasicBlockStarted[r] = true;
					}
					r++;
				}
			}
			
			// add the instructions for this label to the partialBB
			partialBB.add(loc.labelBlock);
		}
		
		return rangeBlocks;
	}
	
	/**
	 * @param labels Array of labels to search.
	 * @param label {@link Label} to find the index of.
	 * @return The index of the label in the array of labels or -1 if the label
	 * is not in the array
	 */
	private int findLabelIndex(Label[] labels, Label label) {
		for(int i = 0; i < labels.length; i++) {
			if(labels[i].equals(label)) {
				return i;
			}
		}
		return -1;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	public void postAnalysisEvent(InsnList instructions) {
		if(this.instrumentationState.getBasicBlockLabels() == null) {
			throw new IllegalStateException("RangeBlockAnalyser: basic block labels have not been specified.");
		}
		
		// * first of all, gather the instructions for each label block		Map<Label, InstructionBlockDescriptor> instructionBlocks = this.constructInstructionBlocks(instructions.iterator());
		List<InstructionBlockLocation> labelBlocks = this.constructInstructionBlocks(instructions.iterator());

		// construct line number information
		this.lineNumberAnalyser.postAnalysisEvent(instructions);
		// update set of line numbers to find
		for(int line : this.lineNumberAnalyser.getFoundLineNumbers()) {
			this.lineNumbersNotYetFound.remove(line);
		}
		// Check whether all specified line numbers exist in this method
		if(!this.lineNumbersNotYetFound.isEmpty()) {
			log.severe("Could not find specified line numbers in " + this.methodDescriptorString);
			final String errorMessage = "This can happen if no information for that specific "
					+ "line number is available in bytecode. Examples where this "
					+ "can happen are loop declarations that contain line breaks. "
					+ "For instance in the case of foreach loops, the line number "
					+ "to specify can either be the line of the loop variable "
					+ "or the line of the ':' token. If possible, look up the line "
					+ "numbers in bytecode or try variations on your line number "
					+ "specifications.";
			StringBuilder exceptionText = new StringBuilder();
			exceptionText.append("The following " + this.lineNumbersNotYetFound.size() 
					+ " source code line numbers should be instrumented, but do not exist in the Bytecode: ");
			for(int lineNr : this.lineNumbersNotYetFound) {
				exceptionText.append(lineNr + ";");
			}
			exceptionText.append(errorMessage);
			IllegalArgumentException iae = new IllegalArgumentException(exceptionText.toString());
			log.log(Level.SEVERE, "", iae);
			throw iae; 
		}
		
		// * then, in a second step: merge label blocks to range blocks
		//   (take care of the goto containing label blocks there)
		RangeBlockDescriptor[] rangeBlocks = this.constructRangeBlocks(labelBlocks);

		// serialise the detected blocks
		instrumentationState.getInstrumentationContext().getRangeBlocks().addInstructionBlocksForMethod(
				methodDescriptorString, 
				rangeBlocks);
		instrumentationState.getInstrumentationContext().getRangesByMethod().put(
				methodDescriptorString, 
				this.codeAreas.toArray(new InstrumentedCodeArea[this.codeAreas.size()]));
		
		this.instrumentationState.setRangeBlockContainsLabels(this.rangeBlockContainsLabels);
	}
}
