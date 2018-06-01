package de.uka.ipd.sdq.ByCounter.instrumentation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

import de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector;
import de.uka.ipd.sdq.ByCounter.execution.ProtocolCountStructure;
import de.uka.ipd.sdq.ByCounter.parsing.LineNumberRange;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/** TODO
 *
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
@Deprecated // Static is counting not supported
public final class MethodSectionCountMethodAdapter extends MethodAdapter {
	
	private InstrumentationParameters instrumentationParameters;
	
	private MethodVisitor nextVisitor;
	
	@SuppressWarnings("unused")
	private HashMap<Integer, Integer> opcodeMap; //TODO never used locally?
	
	private String owner;

	private MethodDescriptor methodDescriptor;

	/** TODO
	 * @param owner
	 * @param access
	 * @param name
	 * @param desc
	 * @param mv
	 */
	public MethodSectionCountMethodAdapter(
			String owner, 
			int access, 
			String name,
			String desc, 
			MethodVisitor mv, 
			InstrumentationParameters params,
			MethodDescriptor method) {
		super(new MethodNode(access, name, desc, null, null));

		this.opcodeMap = new HashMap<Integer, Integer>();
		this.instrumentationParameters = params;
		this.methodDescriptor = method;
		
		this.owner = owner;
		this.nextVisitor = mv;

	}
	
	@SuppressWarnings({ "unchecked", "boxing" })
	@Override
	public void visitEnd() {
		MethodNode mn = (MethodNode) this.mv;

		int count;
		boolean inCountSection = true;
		HashMap<Integer, Integer> opcodeCounts = new HashMap<Integer, Integer>();
		
		for (	Iterator<AbstractInsnNode> iterator = mn.instructions.iterator(); 
				iterator.hasNext();
			) {
			AbstractInsnNode insn = iterator.next();
			if(insn instanceof LineNumberNode) {
				int l = ((LineNumberNode)insn).line;
				List<InstrumentedCodeArea> areasForMethod = this.instrumentationParameters.findCodeAreasForMethod(methodDescriptor);
				if(!areasForMethod.isEmpty() 
						&& LineNumberRange.findLineInAreas(areasForMethod, l) != null) {
					//log.info("Line " + l)
					inCountSection = true;
				} else {;
					inCountSection = false;
				}
			} else if (inCountSection) {
				// log.info("Opcode: " + DisplayOpcodes.instance().getOpcodeString(insn.getOpcode()));
				if(opcodeCounts.containsKey(insn.getOpcode()))
					count = opcodeCounts.get(insn.getOpcode());
				else
					count = 0;
				opcodeCounts.put(insn.getOpcode(), count + 1);
			}
			
		}
		int[] opcodeCountsArray = new int[opcodeCounts.size()];
		for(int i : opcodeCounts.keySet()) {
			opcodeCountsArray[i] = opcodeCounts.get(i);
		}
		ProtocolCountStructure result = new ProtocolCountStructure();
		result.qualifyingMethodName = this.owner;
		result.opcodeCountsInt = opcodeCountsArray;
		CountingResultCollector.getInstance().protocolCount(result);

		mn.accept(this.nextVisitor);
	}
}
