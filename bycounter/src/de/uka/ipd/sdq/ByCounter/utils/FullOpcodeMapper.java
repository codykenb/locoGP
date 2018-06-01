package de.uka.ipd.sdq.ByCounter.utils;

import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Methods are capitalised because otherwise, some method names (e.g. goto())
 * collide with protected tokens of Java Fields (common knowledge from JVM spec)
 * copy-and-pasted from ASM for speedup
 * 
 * @author Michael Kuperberg
 * 
 */
public class FullOpcodeMapper implements IAllJavaOpcodes {
	
	/**
	 * Should not be sorted!!!
	 */
	public static final String[] mnemonics = new String[]{ "NOP", "ACONST_NULL",
			"ICONST_M1", "ICONST_0", "ICONST_1", "ICONST_2", "ICONST_3",
			"ICONST_4", "ICONST_5", "LCONST_0", "LCONST_1", "FCONST_0",
			"FCONST_1", "FCONST_2", "DCONST_0", "DCONST_1", "BIPUSH", "SIPUSH",
			"LDC", "LDC_W", "LDC2_W", "ILOAD", "LLOAD", "FLOAD", "DLOAD",
			"ALOAD", "ILOAD_0", "ILOAD_1", "ILOAD_2", "ILOAD_3", "LLOAD_0",
			"LLOAD_1",
			"LLOAD_2", "LLOAD_3", "FLOAD_0", "FLOAD_1", "FLOAD_2",
			"FLOAD_3", "DLOAD_0", "DLOAD_1", "DLOAD_2", "DLOAD_3", "ALOAD_0",
			"ALOAD_1", "ALOAD_2", "ALOAD_3", "IALOAD", "LALOAD", "FALOAD",
			"DALOAD", "AALOAD", "BALOAD", "CALOAD", "SALOAD", "ISTORE",
			"LSTORE", "FSTORE", "DSTORE", "ASTORE", "ISTORE_0", "ISTORE_1",
			"ISTORE_2", "ISTORE_3", "LSTORE_0", "LSTORE_1", "LSTORE_2",
			"LSTORE_3", "FSTORE_0", "FSTORE_1", "FSTORE_2", "FSTORE_3",
			"DSTORE_0", "DSTORE_1", "DSTORE_2", "DSTORE_3", "ASTORE_0",
			"ASTORE_1", "ASTORE_2", "ASTORE_3", "IASTORE", "LASTORE",
			"FASTORE", "DASTORE", "AASTORE", "BASTORE", "CASTORE", "SASTORE",
			"POP", "POP2", "DUP", "DUP_X1", "DUP_X2", "DUP2", "DUP2_X1",
			"DUP2_X2", "SWAP", "IADD", "LADD", "FADD", "DADD", "ISUB", "LSUB",
			"FSUB", "DSUB", "IMUL", "LMUL", "FMUL", "DMUL", "IDIV", "LDIV",
			"FDIV", "DDIV", "IREM", "LREM", "FREM", "DREM", "INEG", "LNEG",
			"FNEG", "DNEG", "ISHL", "LSHL", "ISHR", "LSHR", "IUSHR", "LUSHR",
			"IAND", "LAND", "IOR", "LOR", "IXOR", "LXOR", "IINC", "I2L", "I2F",
			"I2D", "L2I", "L2F", "L2D", "F2I", "F2L", "F2D", "D2I", "D2L",
			"D2F", "I2B", "I2C", "I2S", "LCMP", "FCMPL", "FCMPG", "DCMPL",
			"DCMPG", "IFEQ", "IFNE", "IFLT", "IFGE", "IFGT", "IFLE",
			"IF_ICMPEQ", "IF_ICMPNE", "IF_ICMPLT", "IF_ICMPGE", "IF_ICMPGT",
			"IF_ICMPLE", "IF_ACMPEQ", "IF_ACMPNE", "GOTO", "JSR", "RET",
			"TABLESWITCH", "LOOKUPSWITCH", "IRETURN", "LRETURN", "FRETURN",
			"DRETURN", "ARETURN", "RETURN", "GETSTATIC", "PUTSTATIC",
			"GETFIELD", "PUTFIELD", "INVOKEVIRTUAL", "INVOKESPECIAL",
			"INVOKESTATIC", "INVOKEINTERFACE", "INVOKEDYNAMIC", "NEW",
			"NEWARRAY", "ANEWARRAY", "ARRAYLENGTH", "ATHROW", "CHECKCAST",
			"INSTANCEOF", "MONITORENTER", "MONITOREXIT", "WIDE",
			"MULTIANEWARRAY", "IFNULL", "IFNONNULL", "GOTO_W", "JSR_W" };
	
	public static SortedMap<String, Integer> mnemonicToOpcode;
	
	static {
		mnemonicToOpcode = new TreeMap<String, Integer>();
		for (int i = 0; i < mnemonics.length; i++) {
			mnemonicToOpcode.put(mnemonics[i], new Integer(i));
		}
//		System.out.println("mnemonicToOpcode: "+mnemonicToOpcode.toString());

//		Method[] methods = FullOpcodeMapper.class.getDeclaredMethods();//could go with getMethods, but "declared methods only" is faster
//		String methodName;
//		for (int i = 0; i < methods.length; i++) {
//			methodName = methods[i].getName();
//			if (mnemonicToOpcode.keySet().contains(methodName)) {
//				mnemonicToMethod.put(methodName, methods[i]);
//			} else {
//				System.out.println("Method " + methodName
//						+ " not a mnemonic translation");
//			}
//		}
	}
	
	public static String getMnemonicOfOpcode(Integer opcode) {

		if (opcode >= 0 && opcode < 202) {
			return mnemonics[opcode];
		} else {
			System.err.println("getMnemonicOfOpcode: opcode out of range: "
					+ opcode + " is not within [0, 200]");
			return null;
		}
	}
	public static Integer getOpcodeOfMnemonic(String mnemonic) {
		String upperCaseMnemonic = mnemonic.toUpperCase();
		// if(upperCaseMnemonic.equals(mnemonic)){
		// log.warn();
		// }
		// if(mnemonicToOpcode.
		Integer opcode = mnemonicToOpcode.get(upperCaseMnemonic);
		if (opcode == null) {
			return null;// TODO log
		} else {
			return opcode;
		}
	}
	
	

	public static boolean isDuplicateFree(){
		String[] copyOfMnemonics = new String[mnemonics.length]; 
		System.arraycopy(mnemonics, 0, copyOfMnemonics, 0, mnemonics.length);
		Arrays.sort(copyOfMnemonics);
		for(int i=0; i<copyOfMnemonics.length-1; i++){
			if(copyOfMnemonics[i].equals(copyOfMnemonics[i+1])){
				return false;
			}
		}
		return true;
	}
	
	public static boolean isValidMnemonic(String mnemonic){
		if(mnemonicToOpcode.keySet().contains(mnemonic.toUpperCase())){
			return true;
		}else{
			return false;
		}
	}


	public static boolean isValidOpcode(int opcode){
		if(opcode>=0 && opcode<202){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * TODO add uppercase tests
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		// String key;
		// for (Iterator<String> iter = mnemonicToOpcode.keySet().iterator();
		// iter.hasNext();) {
		// key = iter.next();
		// System.out.println("public void "+key.toLowerCase()+"(){\n}\n");
		// }
//		System.out.println("Method of mnemonic AALOAD: "
//				+ getMethodOfMnemonic("AALOAD"));
//		System.out.println("Method of mnemonic AALOADfake: "
//				+ getMethodOfMnemonic("AALOADfake"));
//
//		System.out.println("Method of opcode 1: " + getMethodOfOpcode(1));
//		System.out.println("Method of opcode 1001: " + getMethodOfOpcode(1001));
//
//		Method[] methods = FullOpcodeMapper.class.getMethods();/*getDeclaredMethods yields only successful tests*/
//		
//		System.out.println("Mnemonic of method " + methods[0] + ": "
//				+ getMnemonicOfMethod(methods[0]));
//		System.out.println("Mnemonic of method " + methods[1] + ": "
//				+ getMnemonicOfMethod(methods[1]));
//		System.out.println("Mnemonic of method " + methods[2] + ": "
//				+ getMnemonicOfMethod(methods[2]));
//		System.out.println("Mnemonic of method " + methods[methods.length - 2]
//				+ ": " + getMnemonicOfMethod(methods[methods.length - 2]));
//		System.out.println("Mnemonic of method " + methods[methods.length - 1]
//				+ ": " + getMnemonicOfMethod(methods[methods.length - 1]));

		System.out.println("Mnemonic of opcode 1: " + getMnemonicOfOpcode(1));//TODO make this a test
		System.out.println("Mnemonic of opcode 1001: "
				+ getMnemonicOfOpcode(1001));
//
//		System.out.println("Opcode of method " + methods[0] + ": "
//				+ getOpcodeOfMethod(methods[0]));
//		System.out.println("Opcode of method " + methods[1] + ": "
//				+ getOpcodeOfMethod(methods[1]));
//		System.out.println("Opcode of method " + methods[2] + ": "
//				+ getOpcodeOfMethod(methods[2]));
//		System.out.println("Opcode of method " + methods[methods.length - 2]
//				+ ": " + getOpcodeOfMethod(methods[methods.length - 2]));
//		System.out.println("Opcode of method " + methods[methods.length - 1]
//				+ ": " + getOpcodeOfMethod(methods[methods.length - 1]));
//
		System.out.println("Opcode of mnemonic AALOAD: "
				+ getOpcodeOfMnemonic("AALOAD"));
		System.out.println("Opcode of mnemonic AALOADfake: "
				+ getOpcodeOfMnemonic("AALOADfake"));

	}
}
