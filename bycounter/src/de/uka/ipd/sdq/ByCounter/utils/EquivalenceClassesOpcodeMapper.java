package de.uka.ipd.sdq.ByCounter.utils;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * Methods are capitalised because otherwise, some method names (e.g. goto())
 * collide with protected tokens of Java Fields (common knowledge from JVM spec)
 * copy-and-pasted from ASM for speedup
 * 
 * @author Michael Kuperberg
 * 
 */
public class EquivalenceClassesOpcodeMapper extends OpcodeToMethodMapper{
	/**
	 * Index: "master" opcode of the class, value: number of its "elements", incl. the master itself
	 * Helps to navigate faster - is redundant...
	 */
	static private int[] equivalenceClassCardinality = new int[201];
	
	/**
	 * TODO ...
	 */
	static private SortedMap<Integer,SortedSet<Integer>> equivalenceClasses;
	
	/**
	 * Array indexes: opcode, array value: class, i.e. "master" opcode of the class to which this opcode belongs. 
	 * Helps to navigate faster - is redundant...
	 */
	static private int[] equivalenceMappings = new int[201];
	
	static private Logger log;
	
	public static boolean MERGE_ALLCOMPARISONS = false;
	
	public static boolean MERGE_ARRAYLOAD_CLASSES = true;
	
	public static boolean MERGE_ARRAYSTORE_CLASSES = true;
	
	public static boolean MERGE_DUPS = false;
	
	public static boolean MERGE_FCMP_WITH_DCMP = true;
	
	public static boolean MERGE_IPUSH_WITH_ICONST = true;
	
	public static boolean MERGE_WIDE_WITH_NARROW = true;
	
	static{
		log = Logger.getLogger(EquivalenceClassesOpcodeMapper.class.getName());
		for (int i = 0; i < equivalenceMappings.length; i++) {
			equivalenceMappings[i]=i;
			equivalenceClassCardinality[i]=1;
			TreeSet<Integer> value = new TreeSet<Integer>();
//			value.put
		}			
		addToEquivalenceClass(DCONST_1, DCONST_0);
		addToEquivalenceClass(LCONST_1, LCONST_0);
		addToEquivalenceClass(FCONST_1, FCONST_0);
		addToEquivalenceClass(FCONST_2, FCONST_0);
		addToEquivalenceClass(IFNONNULL, IFNULL);
		addToEquivalenceClass(IF_ACMPNE, IF_ACMPEQ);
		
		if(MERGE_FCMP_WITH_DCMP){//can be simplified by factoring DCMPL out...
			addToEquivalenceClass(DCMPL, DCMPG);
			addToEquivalenceClass(FCMPG, DCMPG);
			addToEquivalenceClass(FCMPL, DCMPG);
		}else{
			addToEquivalenceClass(DCMPL, DCMPG);
			addToEquivalenceClass(FCMPL, FCMPG);
		}
		
		if(MERGE_DUPS){
			addToEquivalenceClass(DUP_X1, DUP);
			addToEquivalenceClass(DUP_X2, DUP);
			addToEquivalenceClass(DUP2_X1, DUP2);
			addToEquivalenceClass(DUP2_X2, DUP2);
		}
		if(MERGE_WIDE_WITH_NARROW){
			addToEquivalenceClass(GOTO_W, GOTO);
			addToEquivalenceClass(JSR_W, JSR);
			addToEquivalenceClass(LDC_W, LDC);
			addToEquivalenceClass(LDC2_W, LDC);
			addToEquivalenceClass(POP2, POP);
		}
		if(MERGE_ALLCOMPARISONS){
			treatAllComparisonsAsIfeq();
		}
		
		treatAloads();
		treatArrayloads();
		treatArraystores();
		treatAstores();
		treatCmps();
		treatDloads();
		treatDstores();
		treatFloads();
		treatFstores();
		treatIconsts();
		treatIloads();
		treatIntCmps();
		treatIstores();
		treatLloads();
		treatLstores();
		treatReturns();
	}
	
	public final static Integer[] getEquivalenceClassByKeyAsArray(int keyOpcode){
		SortedSet<Integer> entry = equivalenceClasses.get(new Integer(keyOpcode));
		if(entry==null){
			return null;
		}else{
			return entry.toArray(new Integer[]{});
		}
	}
	
	public final static SortedSet<Integer> getEquivalenceClassByKeyAsSortedSet(int keyOpcode){
		return equivalenceClasses.get(new Integer(keyOpcode));
	}
	
	public final static int getEquivalenceClassCardinality(int keyOpcode){
		if(keyOpcode>=0 && keyOpcode<equivalenceClassCardinality.length){
			int fromMappings = equivalenceClasses.get(new Integer(keyOpcode)).size();
			int fromCardinilitiesStructure =equivalenceClassCardinality[keyOpcode];
			if(fromCardinilitiesStructure == fromMappings){
				return fromMappings;
			}else{
				log.severe("For key "+keyOpcode+", cardinalities do not match: "+
						fromMappings+" from mappings, "+
						fromCardinilitiesStructure+" from cardinalities.");
				return -1;
			}
		}else{
			return -1;
		}
	}
	
	public final static int getEquivalenceClassRepresentative(int valueOpcode){
		if(valueOpcode>=0 && valueOpcode<equivalenceMappings.length){
			return equivalenceMappings[valueOpcode];
		}else{
			return -1;
		}
	}
	
	public final static boolean isInEquivalenceClass(int classRepresentativeOpcode, int valueOpcode){
		if(equivalenceMappings[valueOpcode]==classRepresentativeOpcode){//not checking the SortedMap
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Implicitly: set to own equivalence class
	 * @param classRepresentative
	 * @param consideredOpcode
	 */
	public final static synchronized boolean removeFromEquivalenceClass(int classRepresentative, int consideredOpcode){
		if(equivalenceMappings[consideredOpcode]==classRepresentative){
			equivalenceClassCardinality[classRepresentative] -= 1;
			equivalenceClassCardinality[consideredOpcode] += 1;
			equivalenceMappings[consideredOpcode]=consideredOpcode;
			equivalenceClasses.get(new Integer(classRepresentative)).remove(new Integer(consideredOpcode));
			equivalenceClasses.get(new Integer(consideredOpcode)).add(new Integer(consideredOpcode));
			return true;
		}else{
			log.severe("Opcode "+consideredOpcode+" is not in the class represented by " +
					classRepresentative);
			return false;
		}
	}

	
//	public final static int removeValueFromMappings(int value){
//		return 0;
//	}

	/**
	 * Formulate assumptions, e.g. "equivalenceMappings set for 'opcode' parameter" 
	 * @param opcode
	 * @param equivalenceClass
	 * @return False in case of a failure. The log will contain details.
	 */
	public final static synchronized boolean addToEquivalenceClass(int opcode, int equivalenceClass){
		int oldClass; //, oldCardinality;
		if(FullOpcodeMapper.isValidOpcode(opcode)){
			if(FullOpcodeMapper.isValidOpcode(equivalenceClass)){
				if(equivalenceMappings[opcode]==equivalenceClass){//already set
					log.info("equivalence class of "+opcode+" already set to "+equivalenceClass);
					return true;
				}else{//a change is needed
					if(equivalenceMappings[equivalenceClass]==opcode){//this would result in a circularity! -> abort
						log.severe("danger of circularity detected: master of equivalence class " +equivalenceClass+
								" is the opcode "+opcode+" itself");
						return false;
					}else{
						oldClass = equivalenceMappings[opcode];
//							oldCardinality = equiClassCardinality[oldClass];
						
						equivalenceClassCardinality[oldClass] -= 1;
						equivalenceMappings[opcode]=equivalenceClass;
						equivalenceClassCardinality[equivalenceClass] += 1;
						equivalenceClasses.get(new Integer(oldClass)).remove(new Integer(opcode));
						equivalenceClasses.get(new Integer(equivalenceClass)).add(new Integer(opcode));
						log.info("equivalence class of "+opcode+" set to "+equivalenceClass);
						return true;
					}
				}
			}else{
				log.severe("opcode of equivalence class invalid: "+equivalenceClass+", not setting equivalence classe");
				return false;
			}
		}else{
			log.severe("opcode invalid: "+opcode+", not setting equivalence classe");
			return false;
		}
	}
	
	private static synchronized void treatAllComparisonsAsIfeq() {//incl. jumps, but also "pure comparisons"
		addToEquivalenceClass(DCMPG, IFEQ);
		addToEquivalenceClass(DCMPL, IFEQ);
		addToEquivalenceClass(FCMPG, IFEQ);
		addToEquivalenceClass(FCMPL, IFEQ);
		addToEquivalenceClass(LCMP, IFEQ);

		addToEquivalenceClass(IF_ACMPEQ, IFEQ);
		addToEquivalenceClass(IF_ACMPNE, IFEQ);
		addToEquivalenceClass(IF_ICMPEQ, IFEQ);
		addToEquivalenceClass(IF_ICMPGE, IFEQ);
		addToEquivalenceClass(IF_ICMPGT, IFEQ);
		addToEquivalenceClass(IF_ICMPLE, IFEQ);
		addToEquivalenceClass(IF_ICMPLT, IFEQ);
		addToEquivalenceClass(IF_ICMPNE, IFEQ);
		addToEquivalenceClass(IFGE, IFEQ);
		addToEquivalenceClass(IFGT, IFEQ);
		addToEquivalenceClass(IFLE, IFEQ);
		addToEquivalenceClass(IFLT, IFEQ);
		addToEquivalenceClass(IFNE, IFEQ);
		addToEquivalenceClass(IFNONNULL, IFEQ);
		addToEquivalenceClass(IFNULL, IFEQ);
	}

	private static synchronized void treatAloads() {
		addToEquivalenceClass(ALOAD_0, ALOAD);
		addToEquivalenceClass(ALOAD_1, ALOAD);
		addToEquivalenceClass(ALOAD_2, ALOAD);
		addToEquivalenceClass(ALOAD_3, ALOAD);
	}
	
	private static synchronized void treatArrayloads() {
		addToEquivalenceClass(BALOAD, AALOAD);
		addToEquivalenceClass(CALOAD, AALOAD);
		addToEquivalenceClass(FALOAD, AALOAD);
		addToEquivalenceClass(IALOAD, AALOAD);
		addToEquivalenceClass(SALOAD, AALOAD);
		if(MERGE_ARRAYLOAD_CLASSES){
			addToEquivalenceClass(DALOAD, AALOAD);
			addToEquivalenceClass(LALOAD, AALOAD);
		}else{
			addToEquivalenceClass(DALOAD, DALOAD);//or just skip
			addToEquivalenceClass(LALOAD, DALOAD);//or just skip
		}
	}
	
	private static synchronized void treatArraystores() {
		addToEquivalenceClass(BASTORE, AASTORE);
		addToEquivalenceClass(CASTORE, AASTORE);
		addToEquivalenceClass(FASTORE, AASTORE);
		addToEquivalenceClass(IASTORE, AASTORE);
		addToEquivalenceClass(SASTORE, AASTORE);
		if(MERGE_ARRAYSTORE_CLASSES){
			addToEquivalenceClass(DASTORE, AASTORE);
			addToEquivalenceClass(LASTORE, AASTORE);
		}else{
			addToEquivalenceClass(DASTORE, DASTORE);//or just skip
			addToEquivalenceClass(LASTORE, DASTORE);//or just skip
		}
	}
	
	private static synchronized void treatAstores() {
		addToEquivalenceClass(ASTORE_0, ASTORE);
		addToEquivalenceClass(ASTORE_1, ASTORE);
		addToEquivalenceClass(ASTORE_2, ASTORE);
		addToEquivalenceClass(ASTORE_3, ASTORE);
	}
	
	private static synchronized void treatCmps() {
		addToEquivalenceClass(IFGE, IFEQ);
		addToEquivalenceClass(IFGT, IFEQ);
		addToEquivalenceClass(IFLE, IFEQ);
		addToEquivalenceClass(IFLT, IFEQ);
		addToEquivalenceClass(IFNE, IFEQ);
	}
	
	private static synchronized void treatDloads() {
		addToEquivalenceClass(DLOAD_0, DLOAD);
		addToEquivalenceClass(DLOAD_1, DLOAD);
		addToEquivalenceClass(DLOAD_2, DLOAD);
		addToEquivalenceClass(DLOAD_3, DLOAD);
	}
	
	private static synchronized void treatDstores() {
		addToEquivalenceClass(DSTORE_0, DSTORE);
		addToEquivalenceClass(DSTORE_1, DSTORE);
		addToEquivalenceClass(DSTORE_2, DSTORE);
		addToEquivalenceClass(DSTORE_3, DSTORE);
	}
	
	private static synchronized void treatFloads() {
		addToEquivalenceClass(FLOAD_0, FLOAD);
		addToEquivalenceClass(FLOAD_1, FLOAD);
		addToEquivalenceClass(FLOAD_2, FLOAD);
		addToEquivalenceClass(FLOAD_3, FLOAD);
	}
	
	private static synchronized void treatFstores() {
		addToEquivalenceClass(FSTORE_0, FSTORE);
		addToEquivalenceClass(FSTORE_1, FSTORE);
		addToEquivalenceClass(FSTORE_2, FSTORE);
		addToEquivalenceClass(FSTORE_3, FSTORE);
	}
	
	private static synchronized void treatIconsts() {
		addToEquivalenceClass(ICONST_1, ICONST_0);
		addToEquivalenceClass(ICONST_2, ICONST_0);
		addToEquivalenceClass(ICONST_3, ICONST_0);
		addToEquivalenceClass(ICONST_4, ICONST_0);
		addToEquivalenceClass(ICONST_5, ICONST_0);
		addToEquivalenceClass(ICONST_M1, ICONST_0);
		if(MERGE_IPUSH_WITH_ICONST){
			addToEquivalenceClass(BIPUSH, ICONST_0);
			addToEquivalenceClass(SIPUSH, ICONST_0);
		}else{
			addToEquivalenceClass(SIPUSH, BIPUSH);
		}
	}
	
	private static synchronized void treatIloads() {
		addToEquivalenceClass(ILOAD_0, ILOAD);
		addToEquivalenceClass(ILOAD_1, ILOAD);
		addToEquivalenceClass(ILOAD_2, ILOAD);
		addToEquivalenceClass(ILOAD_3, ILOAD);
	}
	
	private static synchronized void treatIntCmps() {
		addToEquivalenceClass(IF_ICMPGE, IF_ICMPEQ);
		addToEquivalenceClass(IF_ICMPGT, IF_ICMPEQ);
		addToEquivalenceClass(IF_ICMPLE, IF_ICMPEQ);
		addToEquivalenceClass(IF_ICMPLT, IF_ICMPEQ);
		addToEquivalenceClass(IF_ICMPNE, IF_ICMPEQ);
	}
	
	private static synchronized void treatIstores() {
		addToEquivalenceClass(ISTORE_0, ISTORE);
		addToEquivalenceClass(ISTORE_1, ISTORE);
		addToEquivalenceClass(ISTORE_2, ISTORE);
		addToEquivalenceClass(ISTORE_3, ISTORE);
	}
	
	private static synchronized void treatLloads() {
		addToEquivalenceClass(LLOAD_0, LLOAD);
		addToEquivalenceClass(LLOAD_1, LLOAD);
		addToEquivalenceClass(LLOAD_2, LLOAD);
		addToEquivalenceClass(LLOAD_3, LLOAD);
	}
	
	private static synchronized void treatLstores() {
		addToEquivalenceClass(LSTORE_0, LSTORE);
		addToEquivalenceClass(LSTORE_1, LSTORE);
		addToEquivalenceClass(LSTORE_2, LSTORE);
		addToEquivalenceClass(LSTORE_3, LSTORE);
	}
	
	private static synchronized void treatReturns() {
		addToEquivalenceClass(ARETURN, RETURN);
		addToEquivalenceClass(DRETURN, RETURN);
		addToEquivalenceClass(FRETURN, RETURN);
		addToEquivalenceClass(IRETURN, RETURN);
		addToEquivalenceClass(LRETURN, RETURN);
	}
	

}
