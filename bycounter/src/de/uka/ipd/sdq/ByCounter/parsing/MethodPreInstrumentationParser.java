package de.uka.ipd.sdq.ByCounter.parsing;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;

import de.uka.ipd.sdq.ByCounter.instrumentation.BlockCountingMode;
import de.uka.ipd.sdq.ByCounter.instrumentation.EntityToInstrument;
import de.uka.ipd.sdq.ByCounter.instrumentation.IInstructionAnalyser;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedCodeArea;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedRegion;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationState;
import de.uka.ipd.sdq.ByCounter.instrumentation.MethodCountMethodAdapter;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * MethodPreInstrumentationParser implements a quick method visiting pass for 
 * finding method invocations and array constructions. The knowledge obtained 
 * from this pass can be used to count method invocations more efficiently 
 * using simple int counters and 'iinc' instructions. In addition to that, 
 * array type and dimension information can be obtained.
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
public final class MethodPreInstrumentationParser extends MethodAdapter {
	
	/** User specified parameters */
	private InstrumentationParameters instrumentationParameters;
	
	private MethodCountMethodAdapter methodCountMethodAdapter;
	
	private MethodVisitor nextVisitor;

	private Logger log;

	private BasicBlockAnalyser basicBlockAnalyser;
	
	private RangeBlockAnalyser rangeBlockAnalyser;
	
	private RegionAnalyser regionAnalyser;
	
	private LineNumberAnalyser lineNumberAnalyser;
	
	private List<IInstructionAnalyser> instructionAnalysers;
	
	private LabelAfterInvokeAnalyser labelAfterInvokeAnalyser;

	private boolean hasRangeBlocks;
	
	/** True when label blocks are used as opposed to range/basic blocks. */
	private boolean useRegions;

	/** Intermediate results of the instrumentation. */
	private InstrumentationState instrumentationState;

	/** The currently analysed method. */
	private MethodDescriptor method;

	/**
	 * @param access As from ClassVisitor.
	 * @param name As from ClassVisitor.
	 * @param desc As from ClassVisitor.
	 * @param methodCountMethodAdapter {@link MethodCountMethodAdapter} that needs the method
	 * @param parameters Parameters for instrumentation. Also contains information that decides what is done before instrumetation.
	 * invocation information for proper instrumentation.
	 * @param method The currently analysed method.
	 */
	public MethodPreInstrumentationParser(
			MethodVisitor mv,
			int access, 
			String owner,
			String name,
			String desc, 
			MethodCountMethodAdapter methodCountMethodAdapter,
			InstrumentationParameters parameters,
			InstrumentationState state,
			MethodDescriptor method) {
		super(new MethodNode(access, name, desc, null, null));
		this.log = Logger.getLogger(this.getClass().getCanonicalName());
		this.nextVisitor = mv;
		this.methodCountMethodAdapter = methodCountMethodAdapter;
		this.instrumentationParameters = parameters;
		this.instrumentationState = state;
		this.method = method;
		List<InstrumentedCodeArea> codeAreasForMethod = this.instrumentationParameters.findCodeAreasForMethod(method);
		this.hasRangeBlocks = codeAreasForMethod.size() > 0;
		this.useRegions = this.instrumentationParameters.hasInstrumentationRegionForMethod(method);
		
		this.instructionAnalysers = new ArrayList<IInstructionAnalyser>();
		this.lineNumberAnalyser = new LineNumberAnalyser(method);
		this.instructionAnalysers.add(lineNumberAnalyser);
		if(this.instrumentationParameters.getUseBasicBlocks()) {
//			MethodNode methodNode = (MethodNode)this.mv;
			log.info("Analysing method for basic blocks.");
			this.basicBlockAnalyser = new BasicBlockAnalyser(
					method.getCanonicalMethodName(),
					this.instrumentationState);
			this.instructionAnalysers.add(basicBlockAnalyser);
			this.instrumentationState.getInstrumentationContext().setBlockCountingMode(method.getCanonicalMethodName(), BlockCountingMode.BasicBlocks);
			if(hasRangeBlocks) {
				log.info("Analysing method for range blocks.");
				this.rangeBlockAnalyser = new RangeBlockAnalyser(
						method, 
						this.instrumentationState,
						this.lineNumberAnalyser,
						codeAreasForMethod);
				this.instructionAnalysers.add(rangeBlockAnalyser);
				this.instrumentationState.getInstrumentationContext().setBlockCountingMode(method.getCanonicalMethodName(), BlockCountingMode.RangeBlocks);
			}
			// are code areas specified for the method?
			if(this.instrumentationParameters.hasInstrumentationRegionForMethod(this.method)) {
				List<InstrumentedRegion> regions = new LinkedList<InstrumentedRegion>();
				if(useRegions) {
					// calculate regions
					for(EntityToInstrument e : this.instrumentationParameters.getEntitiesToInstrument()) {
						if (e instanceof InstrumentedRegion) {
							InstrumentedRegion r = (InstrumentedRegion) e;
							regions.add(r);
						}
					}
					// analyse
					log.info("Analysing method for label blocks.");
					this.labelAfterInvokeAnalyser = new LabelAfterInvokeAnalyser();
					this.regionAnalyser = new RegionAnalyser(
							this.instrumentationState,
							method, 
							regions,
							this.lineNumberAnalyser);
					this.instructionAnalysers.add(regionAnalyser);
					this.instrumentationState.getInstrumentationContext().setBlockCountingMode(method.getCanonicalMethodName(), BlockCountingMode.LabelBlocks);
				}
			}
		}
	}

	/**
	 * Visiting the end of the method allows to collect the needed method 
	 * invocation information.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void visitEnd() {
		// Use a MethodNode to analyse all instructions in the method
		MethodNode mn = (MethodNode) this.mv;

		if(this.useRegions) {			
			this.labelAfterInvokeAnalyser.postAnalysisEvent(mn.instructions);
		}
		
		{
			this.instrumentationState.getMethodInvocations().put(
					method.getCanonicalMethodName(), 
					new LinkedList<String>());
			this.instrumentationState.getInstrumentationContext().getArrayCreations().put(
					this.method.getCanonicalMethodName(), new LinkedList<ArrayCreation>());
			Iterator<AbstractInsnNode> iterator = mn.instructions.iterator();
			while(iterator.hasNext()) {
				AbstractInsnNode insn = iterator.next();
				analyseCountedInstruction(insn);
			}
		}
		
		// try catch blocks are not in the normal instructions list and need to 
		// be analysed separately.
		{
			Iterator<TryCatchBlockNode> iterator = mn.tryCatchBlocks.iterator();
			while(iterator.hasNext()) {
				TryCatchBlockNode tryCatchNode = iterator.next();
				for(IInstructionAnalyser analyser : this.instructionAnalysers) {
					analyser.analyseTryCatchBlock(tryCatchNode);
				}
			}
		}
		
		// execute post analysis methods
		

		if(this.instrumentationParameters.getUseBasicBlocks()) {
			// range block analyser depends on the basic block analyser
			this.basicBlockAnalyser.postAnalysisEvent(mn.instructions);
			if(this.hasRangeBlocks) {
				this.rangeBlockAnalyser.postAnalysisEvent(mn.instructions);
			}
		}
		
		for(IInstructionAnalyser analyser : this.instructionAnalysers) {
			if(analyser == this.basicBlockAnalyser 
					|| analyser == this.rangeBlockAnalyser) {
				// already done
				continue;
			}
			// bck analyser.postAnalysisEvent(mn.instructions);
		}
		
		// check whether the method is marked as instrumented.
		checkMethodMarkedAsInstrumented();

		mn.accept(this.nextVisitor);
	}


	/**
	 * Parse the instruction in order to prepare data structures etc.
	 * @param insn An instruction that has to be counted.
	 */
	private void analyseCountedInstruction(final AbstractInsnNode insn) {

		// Initialize registers for all appearing method invocations
		if(insn instanceof MethodInsnNode) {
			// gather signature for the method invocation
			MethodInsnNode method = ((MethodInsnNode)insn);
			String sig = MethodDescriptor._constructMethodDescriptorFromASM(
					method.owner, method.name, method.desc).getCanonicalMethodName();
			List<String> methodInvocations = this.instrumentationState.getMethodInvocations().get(this.method.getCanonicalMethodName());
			if(!methodInvocations.contains(sig)) {
				methodInvocations.add(sig);
			}
		} else if(this.instrumentationParameters.getUseArrayParameterRecording()) { 
			ArrayCreation arrayCreation = analyseForArrayParameterRecording(insn);
			if(arrayCreation != null) {
				List<ArrayCreation> arrayCreations = this.instrumentationState.getInstrumentationContext().getArrayCreations().get(this.method.getCanonicalMethodName());
				if(!arrayCreations.contains(arrayCreation)) {
					arrayCreations.add(arrayCreation);
				}
			}
		}

		for(IInstructionAnalyser analyser : this.instructionAnalysers) {
			analyser.analyseInstruction(insn);
		}
	}
	


	/** 
	 * @param insn Instruction to analyse.
	 * @return An instance of {@link ArrayCreation} if the given instruction 
	 * creates an array. Else null.
	 */
	private ArrayCreation analyseForArrayParameterRecording(final AbstractInsnNode insn) {
		ArrayCreation arrayCreation = new ArrayCreation();
		if(insn instanceof IntInsnNode 
				&& insn.getOpcode() == Opcodes.NEWARRAY) {
			// get the type integer for the newarray call
			IntInsnNode node = ((IntInsnNode)insn);
			arrayCreation.setTypeOpcode(node.operand);
			return arrayCreation;
		} else if(insn instanceof TypeInsnNode 
				&& insn.getOpcode() == Opcodes.ANEWARRAY) {
			// get the type string for the anewarray call
			TypeInsnNode node = ((TypeInsnNode)insn);
			arrayCreation.setTypeDesc(node.desc);
			return arrayCreation;
		} else if(insn instanceof MultiANewArrayInsnNode) {
			// get the type string and dimension integer
			MultiANewArrayInsnNode node = ((MultiANewArrayInsnNode)insn);
			arrayCreation.setTypeDesc(node.desc);
			arrayCreation.setNumberOfDimensions(node.dims);
			return arrayCreation;
		}
		return null;
	}


	/**
	 * Checks whether the method is marked as instrumented and sets the status
	 * on the {@link #methodCountMethodAdapter}.
	 */
	private void checkMethodMarkedAsInstrumented() {
		MethodNode methodNode = (MethodNode)mv;
		if(methodNode.instructions.size() > 0) {
			int i = 0;
			// skip nodes that are no "real" instructions
			while(methodNode.instructions.get(i) instanceof LabelNode
					|| methodNode.instructions.get(i) instanceof LineNumberNode) {
				i++;
			}
			AbstractInsnNode firstInsn = methodNode.instructions.get(i);
			if(firstInsn.getOpcode() == Opcodes.LDC) {
				LdcInsnNode ldcInsn = ((LdcInsnNode)firstInsn);
				if(ldcInsn.cst.getClass().equals(String.class)) {//"cst" means "constant"
					String strConstant = (String)ldcInsn.cst;
					if(strConstant.equals(MethodCountMethodAdapter.INSTRUMENTATION_MARKER)) {
						this.methodCountMethodAdapter.setIsAlreadyInstrumented(true, methodNode.name, methodNode.signature);
					}
				}
			}
		}
	}

}
