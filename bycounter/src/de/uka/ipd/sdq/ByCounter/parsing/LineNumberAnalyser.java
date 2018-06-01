package de.uka.ipd.sdq.ByCounter.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import de.uka.ipd.sdq.ByCounter.instrumentation.IInstructionAnalyser;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * {@link IInstructionAnalyser} build for the purpose of finding line numbers 
 * in methods and associating them with labels even if the label is not directly
 * specified with the line number information given in the bytecode.
 * 
 * @author Martin Krogmann
 */
public class LineNumberAnalyser implements IInstructionAnalyser {

	/** The smallest value for linenumber in the analysed method.
	 */
	private int minLineNumber = -1;
	/**
	 * The largest value for linenumber in the analysed method.
	 */
	private int maxLineNumber = Integer.MAX_VALUE;
	
	/**
	 * The list of all label blocks, i.e. instruction blocks that contain the 
	 * instructions for each label.
	 */
	private List<InstructionBlockLocation> labelBlocks;

	/**
	 * Map to quickly find an {@link InstructionBlockLocation} 
	 * in {@link #labelBlocks} by label.
	 */
	private Map<Label, InstructionBlockLocation> findLabelBlockByLabel;
	
	/**
	 * Map to quickly find an {@link InstructionBlockLocation} 
	 * in {@link #labelBlocks} by line number.
	 */
	private Map<Integer, List<InstructionBlockLocation>> findLabelBlockByLine;
	/**
	 * List of line numbers found in the analysed method.
	 */
	private List<Integer> foundLineNumbers;
		
	
	/**
	 * 
	 * @param method Currently analysed method.
	 */
	public LineNumberAnalyser(MethodDescriptor method) {
		this.labelBlocks = new ArrayList<InstructionBlockLocation>();
		this.findLabelBlockByLabel = new HashMap<Label, InstructionBlockLocation>();
		this.findLabelBlockByLine = new HashMap<Integer, List<InstructionBlockLocation>>();
		this.foundLineNumbers = new LinkedList<Integer>();
	}

	/* (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.instrumentation.IInstructionAnalyser#analyseInstruction(org.objectweb.asm.tree.AbstractInsnNode)
	 */
	public void analyseInstruction(AbstractInsnNode insn) {
		if(insn instanceof LineNumberNode) {
			LineNumberNode lnNode = (LineNumberNode) insn;
			addLabelForInstructionBlock(lnNode.line, lnNode.start.getLabel());
		} else if(insn instanceof LabelNode) {
			addLabelForInstructionBlock(-1, ((LabelNode) insn).getLabel());
		}
	}


	/**
	 * Add the label to the list of labels. If the label has already been added,
	 * and the given line number is >= 0, the line number for the label is 
	 * updated.
	 * @param lineNumber line number for the label. Set to -1 if there is no 
	 * line number for the label.
	 * @param label Label to add.
	 */
	private void addLabelForInstructionBlock(int lineNumber, Label label) {
		InstructionBlockLocation loc = findLabelBlockByLabel.get(label);
		if(loc == null) {
			// this is the first time we see this label, so initialise the 
			// InstructionBlockLocation
			loc = new InstructionBlockLocation();
			loc.label = label;
			loc.labelBlock = new InstructionBlockDescriptor();
			this.findLabelBlockByLabel.put(label, loc);
			this.labelBlocks.add(loc);
		}
		if(lineNumber >= 0) {
			updateMinMaxLineNumber(lineNumber);
			loc.lineNumber = lineNumber;
			
			// add the block to the line number search structure
			List<InstructionBlockLocation> blocksForLine 
				= this.findLabelBlockByLine.get(lineNumber);
			if(blocksForLine == null) {
				blocksForLine = new LinkedList<InstructionBlockLocation>();
				this.findLabelBlockByLine.put(lineNumber, blocksForLine);
			}
			blocksForLine.add(loc);
			
			// the line lineNumber was found; remove it from the not found set
			this.foundLineNumbers.add(lineNumber);
		}
	}

	/** Updates {@link #minLineNumber} and {@link #maxLineNumber} if linenumber
	 * qualifies for either one.
	 * @param lineNumber An analysed instruction.
	 */
	private void updateMinMaxLineNumber(int lineNumber) {
		if(lineNumber < minLineNumber) {
			minLineNumber = lineNumber;
		}
		if(lineNumber > maxLineNumber) {
			maxLineNumber = lineNumber;
		}
	}
	
	@Override
	public void analyseTryCatchBlock(TryCatchBlockNode tryCatchNode) {
		// nothing to do here
	}

	@Override
	public void postAnalysisEvent(InsnList instructions) {
		final Map<Label, List<Label>> jumpSourceMap = buildJumpSourceMap(instructions);
		for(InstructionBlockLocation loc : this.labelBlocks) {
			if(loc.lineNumber < 0) {
				loc.lineNumber = findLineNumberFromJumpContext(jumpSourceMap, loc.label);
			}
		}
	}

	/**
	 * Build a map that saves from where labels are jumped to
	 * so that jumpSourceMap(toLabel) == fromLabel.
	 * Jumps can be explicit jump instructions but also the normal fall-through 
	 * order of labels.
	 * @param instructions The methods instructions
	 * @return The build map.
	 */
	private static Map<Label, List<Label>> buildJumpSourceMap(InsnList instructions) {
		Label currentLabel = null;
		Map<Label, List<Label>> jumpSourceMap = new HashMap<Label, List<Label>>();

		boolean jumpedAway = false; // true when the control flow jumped away from the current label
		// go through all instructions
		for (	@SuppressWarnings("unchecked")
				Iterator<AbstractInsnNode> iterator = instructions.iterator(); 
				iterator.hasNext();
			) {
			AbstractInsnNode insn = iterator.next();
			if(insn instanceof LabelNode) {
				Label lastLabel = currentLabel;
				currentLabel = ((LabelNode)insn).getLabel();
				if(lastLabel != null && !jumpedAway) {
					addJumpSourceToMap(jumpSourceMap, lastLabel, currentLabel);
				}
			} else if(insn instanceof JumpInsnNode) {
				jumpedAway = true;
				// save the fact that there is a jump from the current label 
				// block to the target label
				JumpInsnNode jump = (JumpInsnNode)insn;
				Label target = jump.label.getLabel();
				addJumpSourceToMap(jumpSourceMap, currentLabel, target);
			}
		}
		return jumpSourceMap;
	}


	/**
	 * Add the jump relation to the map.
	 * @param jumpSourceMap Map as produced by {@link #buildJumpSourceMap(InsnList)}.
	 * @param jumpSource Source of the jump.
	 * @param destination Destination of the jump.
	 */
	private static void addJumpSourceToMap(Map<Label, List<Label>> jumpSourceMap,
			Label jumpSource, Label destination) {
		List<Label> sourceList = jumpSourceMap.get(destination);
		if(sourceList == null) {
			sourceList = new LinkedList<Label>();
			jumpSourceMap.put(destination, sourceList);
		}
		sourceList.add(jumpSource);
	}

	/**
	 * Finds the line number for the specified instruction block using the 
	 * jump source map.
	 * @param jumpSourceMap Inverse control flow description for labels where 
	 * for each key label, all labels with control flow to it are found.
	 * @param label Label to begin backward search at.
	 * @return The line number.
	 */
	private int findLineNumberFromJumpContext(final Map<Label, List<Label>> jumpSourceMap,
			Label label) {
		// the label has no line number assigned
		// find it in the label map
		List<Label> sourceLabels = jumpSourceMap.get(label);
		if(sourceLabels == null) {
			return -1;	// no line number for this label
		}
		if(sourceLabels.size() != 1) {
			// bck - why??
			Iterator<Label> iter = sourceLabels.iterator();
			while(iter.hasNext()){
				Label curLab = iter.next();
				System.out.println(curLab.toString());
			}
			throw new IllegalStateException("Expected exactly one jump source.");
		}
		Label jumpSource = sourceLabels.get(0);
		InstructionBlockLocation jumpSourceBlockLocation = findLabelBlockByLabel.get(jumpSource);
		if(jumpSourceBlockLocation.lineNumber < 0) {
			return findLineNumberFromJumpContext(jumpSourceMap, jumpSourceBlockLocation.label);
		}
		return jumpSourceBlockLocation.lineNumber;
	}

	/**
	 * @param label Label to find
	 * @return Find an {@link InstructionBlockLocation} 
	 * by label.
	 */
	public InstructionBlockLocation findLabelBlockByLabel(Label label) {
		return this.findLabelBlockByLabel.get(label);
	}
	
	/**
	 * @param line Line number
	 * @return Find an {@link InstructionBlockLocation} 
	 * by line number.
	 */
	public List<InstructionBlockLocation> findLabelBlockByLine(int line) {
		return this.findLabelBlockByLine.get(line);
	}
	
	/**
	 * @return List of line numbers found in the analysed method.
	 */
	public List<Integer> getFoundLineNumbers() {
		return this.foundLineNumbers;
	}
}
