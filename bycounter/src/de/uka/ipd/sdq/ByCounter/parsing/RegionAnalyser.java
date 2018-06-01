package de.uka.ipd.sdq.ByCounter.parsing;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.tree.InsnList;

import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedRegion;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationState;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

public class RegionAnalyser extends LabelBlockAnalyser {
	
	/** User-specified {@link InstrumentedRegion}s. */
	private List<InstrumentedRegion> regions;
	/** Current method. */
	private MethodDescriptor method;
	/**
	 * {@link LineNumberAnalyser} to find line numbers of labels.
	 */
	private LineNumberAnalyser lineNumberAnalyser;


	/**
	 * @param instrumentationState {@link InstrumentationState}.
	 * @param method current method
	 * @param regions User-specified {@link InstrumentedRegion}s.
	 * @param lineNumberAnalyser {@link LineNumberAnalyser}.
	 */
	public RegionAnalyser(InstrumentationState instrumentationState,
			MethodDescriptor method,
			List<InstrumentedRegion> regions,
			LineNumberAnalyser lineNumberAnalyser) {
		super(method.getCanonicalMethodName(), instrumentationState);
		this.regions = regions;
		this.method = method;
		this.lineNumberAnalyser = lineNumberAnalyser;
	}

	@Override
	public void postAnalysisEvent(InsnList instructions) {
		super.postAnalysisEvent(instructions);
		this.saveLabelIdsForRegions();
	}


	/**
	 * For regions specified by the user, create ranges that apply to the 
	 * current method. The ids of ranges are also saved in the instrumentation
	 * context.
	 */
	private void saveLabelIdsForRegions() {
		// calculate current regions
		for(InstrumentedRegion reg : regions) {
			if(reg.getStartMethod().getCanonicalMethodName().equals(this.method.getCanonicalMethodName())) {
				List<InstructionBlockLocation> startLabels = this.lineNumberAnalyser.findLabelBlockByLine(reg.getStartLine());
				List<Integer> labelIds = new LinkedList<Integer>();
				for(InstructionBlockLocation loc : startLabels) {
					labelIds.add(this.instructionBlockLabels.indexOf(loc.label));
				}
				reg.setStartLabelIds(labelIds);
				// save the region
				instrumentationState.getInstrumentationContext().getInstrumentationRegions().add(reg);
			}
			if(reg.getStopMethod().getCanonicalMethodName().equals(this.method.getCanonicalMethodName())) {
				List<InstructionBlockLocation> stopLabels = this.lineNumberAnalyser.findLabelBlockByLine(reg.getStopLine());
				List<Integer> labelIds = new LinkedList<Integer>();
				for(InstructionBlockLocation loc : stopLabels) {
					labelIds.add(this.instructionBlockLabels.indexOf(loc.label));
				}
				reg.setStopLabelIds(labelIds);
				// save the region
				instrumentationState.getInstrumentationContext().getInstrumentationRegions().add(reg);
			}
		}
	}
}
