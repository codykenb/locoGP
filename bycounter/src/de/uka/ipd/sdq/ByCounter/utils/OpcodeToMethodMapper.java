package de.uka.ipd.sdq.ByCounter.utils;

import java.lang.reflect.Method;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Not final!
 * @author Michael
 *
 */
public class OpcodeToMethodMapper extends FullOpcodeMapper{
	public static SortedMap<String, Method> mnemonicToMethod;
	
	static {
		mnemonicToMethod = new TreeMap<String, Method>();
		Method[] methods = OpcodeToMethodMapper.class.getDeclaredMethods();//could go with getMethods, but "declared methods only" is faster
		String methodName;
		for (int i = 0; i < methods.length; i++) {
			methodName = methods[i].getName();
			if (FullOpcodeMapper.mnemonicToOpcode.keySet().contains(methodName)) {
				mnemonicToMethod.put(methodName, methods[i]);
			} else {
				System.out.println("Method " + methodName
						+ " not a mnemonic translation (this can be OK)");
			}
		}
	}
	
	public static Method getMethodOfMnemonic(String mnemonic) {
		if(mnemonic==null || mnemonic.length()==0){
			return null;
		}
		return mnemonicToMethod.get(mnemonic.toUpperCase());
	}
	
	public static Method getMethodOfOpcode(Integer opcode) {
		return getMethodOfMnemonic(FullOpcodeMapper.getMnemonicOfOpcode(opcode));
	}

	public static String getMnemonicOfMethod(Method method) {
		String name = method.getName();
		if(mnemonicToMethod.get(name)==null){
			return null;
		}else{
			return name;
		}
//		return mnemonicToMethod.get(method.getName()).getName();
	}

	public static Integer getOpcodeOfMethod(Method method) {
		return FullOpcodeMapper.getOpcodeOfMnemonic(method.getName());
	}
	
	public static void main(String args[]) {
		// String key;
		// for (Iterator<String> iter = mnemonicToOpcode.keySet().iterator();
		// iter.hasNext();) {
		// key = iter.next();
		// System.out.println("public void "+key.toLowerCase()+"(){\n}\n");
		// }
		System.out.println("Method of mnemonic AALOAD: "
				+ getMethodOfMnemonic("AALOAD"));
		System.out.println("Method of mnemonic AALOADfake: "
				+ getMethodOfMnemonic("AALOADfake"));

		System.out.println("Method of opcode 1: " + getMethodOfOpcode(1));
		System.out.println("Method of opcode 1001: " + getMethodOfOpcode(1001));

		Method[] methods = FullOpcodeMapper.class.getMethods();/*getDeclaredMethods yields only successful tests*/
		
		System.out.println("Mnemonic of method " + methods[0] + ": "
				+ getMnemonicOfMethod(methods[0]));
		System.out.println("Mnemonic of method " + methods[1] + ": "
				+ getMnemonicOfMethod(methods[1]));
		System.out.println("Mnemonic of method " + methods[2] + ": "
				+ getMnemonicOfMethod(methods[2]));
		System.out.println("Mnemonic of method " + methods[methods.length - 2]
				+ ": " + getMnemonicOfMethod(methods[methods.length - 2]));
		System.out.println("Mnemonic of method " + methods[methods.length - 1]
				+ ": " + getMnemonicOfMethod(methods[methods.length - 1]));

		System.out.println("Mnemonic of opcode 1: " + FullOpcodeMapper.getMnemonicOfOpcode(1));
		System.out.println("Mnemonic of opcode 1001: "
				+ FullOpcodeMapper.getMnemonicOfOpcode(1001));
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
//		System.out.println("Opcode of mnemonic AALOAD: "
//				+ getOpcodeOfMnemonic("AALOAD"));
//		System.out.println("Opcode of mnemonic AALOADfake: "
//				+ getOpcodeOfMnemonic("AALOADfake"));

	}

	/**
	 * BySuite thinks this method is not significantly parametric, since the
	 * array index offset is computed, not searched
	 */
	public void AALOAD() {
	}

	public void AASTORE() {
	}

	public void ACONST_NULL() {
	}

	public void ALOAD() {
	}

	public void ALOAD_0() {
	}

	public void ALOAD_1() {
	}

	public void ALOAD_2() {
	}

	public void ALOAD_3() {
	}

	public void ANEWARRAY() {
	}

	public void ARETURN() {
	}

	public void ARRAYLENGTH() {
	}

	public void ASTORE() {
	}

	public void ASTORE_0() {
	}

	public void ASTORE_1() {
	}

	public void ASTORE_2() {
	}

	public void ASTORE_3() {
	}

	/**
	 * Exceptions are not considered in BySutie
	 */
	public void ATHROW() {
	}

	public void BALOAD() {
	}

	public void BASTORE() {
	}

	public void BIPUSH() {
	}

	public void CALOAD() {
	}

	public void CASTORE() {
	}

	/**
	 * Not considered in BySuite
	 */
	public void CHECKCAST() {
	}

	public void D2F() {
	}

	public void D2I() {
	}

	public void D2L() {
	}

	public void DADD() {
	}

	public void DALOAD() {
	}

	public void DASTORE() {
	}

	public void DCMPG() {
	}

	public void DCMPL() {
	}

	public void DCONST_0() {
	}

	public void DCONST_1() {
	}

	/**
	 * TODO study
	 */
	public void DDIV() {
	}

	public void DLOAD() {
	}

	public void DLOAD_0() {
	}

	public void DLOAD_1() {
	}

	public void DLOAD_2() {
	}

	public void DLOAD_3() {
	}

	public void DMUL() {
	}

	public void DNEG() {
	}

	public void DREM() {
	}

	public void DRETURN() {
	}

	public void DSTORE() {
	}

	public void DSTORE_0() {
	}

	public void DSTORE_1() {
	}

	public void DSTORE_2() {
	}

	public void DSTORE_3() {
	}

	public void DSUB() {
	}

	public void DUP() {
	}

	public void DUP_X1() {
	}

	public void DUP_X2() {
	}

	public void DUP2() {
	}

	public void DUP2_X1() {
	}

	public void DUP2_X2() {
	}

	public void F2D() {
	}

	public void F2I() {
	}

	public void F2L() {
	}

	public void FADD() {
	}

	public void FALOAD() {
	}

	public void FASTORE() {
	}

	public void FCMPG() {
	}

	public void FCMPL() {
	}

	public void FCONST_0() {
	}

	public void FCONST_1() {
	}

	public void FCONST_2() {
	}

	public void FDIV() {
	}

	public void FLOAD() {
	}

	public void FLOAD_0() {
	}

	public void FLOAD_1() {
	}

	public void FLOAD_2() {
	}

	public void FLOAD_3() {
	}

	public void FMUL() {
	}

	public void FNEG() {
	}

	public void FREM() {
	}

	public void FRETURN() {
	}

	public void FSTORE() {
	}

	public void FSTORE_0() {
	}

	public void FSTORE_1() {
	}

	public void FSTORE_2() {
	}

	public void FSTORE_3() {
	}

	public void FSUB() {
	}

	public void GETFIELD() {
	}

	public void GETSTATIC() {
	}

	public void GOTO() {
	}

	public void GOTO_W() {
	}

	public void I2B() {
	}

	public void I2C() {
	}

	public void I2D() {
	}

	public void I2F() {
	}

	public void I2L() {
	}

	public void I2S() {
	}

	public void IADD() {
	}

	public void IALOAD() {
	}

	public void IAND() {
	}

	public void IASTORE() {
	}

	public void ICONST_0() {
	}

	public void ICONST_1() {
	}

	public void ICONST_2() {
	}

	public void ICONST_3() {
	}

	public void ICONST_4() {
	}

	public void ICONST_5() {
	}

	public void ICONST_M1() {
	}

	public void IDIV() {
	}

	public void IF_ACMPEQ() {
	}

	public void IF_ACMPNE() {
	}

	public void IF_ICMPEQ() {
	}

	public void IF_ICMPGE() {
	}

	public void IF_ICMPGT() {
	}

	public void IF_ICMPLE() {
	}

	public void IF_ICMPLT() {
	}

	public void IF_ICMPNE() {
	}

	public void IFEQ() {
	}

	public void IFGE() {
	}

	public void IFGT() {
	}

	public void IFLE() {
	}

	public void IFLT() {
	}

	public void IFNE() {
	}

	public void IFNONNULL() {
	}

	public void IFNULL() {
	}

	public void IINC() {
	}

	public void ILOAD() {
	}

	public void ILOAD_0() {
	}

	public void ILOAD_1() {
	}

	public void ILOAD_2() {
	}

	public void ILOAD_3() {
	}

	public void IMUL() {
	}

	public void INEG() {
	}

	public void INSTANCEOF() {
	}

	public void INVOKEDYNAMIC() {
	}

	public void INVOKEINTERFACE() {
	}

	public void INVOKESPECIAL() {
	}

	public void INVOKESTATIC() {
	}

	public void INVOKEVIRTUAL() {
	}

	public void IOR() {
	}

	public void IREM() {
	}

	public void IRETURN() {
	}

	public void ISHL() {
	}

	public void ISHR() {
	}

	public void ISTORE() {
	}

	public void ISTORE_0() {
	}

	public void ISTORE_1() {
	}

	public void ISTORE_2() {
	}

	public void ISTORE_3() {
	}

	public void ISUB() {
	}

	public void IUSHR() {
	}

	public void IXOR() {
	}

	public void JSR() {
	}

	public void JSR_W() {
	}

	public void L2D() {
	}

	public void L2F() {
	}

	public void L2I() {
	}

	public void LADD() {
	}

	public void LALOAD() {
	}

	public void LAND() {
	}

	public void LASTORE() {
	}

	public void LCMP() {
	}

	public void LCONST_0() {
	}

	public void LCONST_1() {
	}

	public void LDC() {
	}

	public void LDC_W() {
	}

	public void LDC2_W() {
	}

	public void LDIV() {
	}

	public void LLOAD() {
	}

	public void LLOAD_0() {
	}

	public void LLOAD_1() {
	}

	public void LLOAD_2() {
	}

	public void LLOAD_3() {
	}

	public void LMUL() {
	}

	public void LNEG() {
	}

	public void LOOKUPSWITCH() {
	}

	public void LOR() {
	}

	public void LREM() {
	}

	public void LRETURN() {
	}

	public void LSHL() {
	}

	public void LSHR() {
	}

	public void LSTORE() {
	}

	public void LSTORE_0() {
	}

	public void LSTORE_1() {
	}

	public void LSTORE_2() {
	}

	public void LSTORE_3() {
	}

	public void LSUB() {
	}

	public void LUSHR() {
	}

	public void LXOR() {
	}

	public void MONITORENTER() {
	}

	public void MONITOREXIT() {
	}

	public void MULTIANEWARRAY() {
	}

	public void NEW() {
	}

	public void NEWARRAY() {
	}

	public void NOP() {
	}

	public void POP() {
	}

	public void POP2() {
	}

	public void PUTFIELD() {
	}

	public void PUTSTATIC() {
	}

	public void RET() {
	}

	public void RETURN() {
	}

	public void SALOAD() {
	}

	public void SASTORE() {
	}

	public void SIPUSH() {
	}

	public void SWAP() {
	}

	public void TABLESWITCH() {
	}

	public void WIDE() {
	}

	// public void aaload(){
	// }
	//
	// public void aastore(){
	// }
	//
	// public void aconst_null(){
	// }
	//
	// public void aload(){
	// }
	//
	// public void aload_0(){
	// }
	//
	// public void aload_1(){
	// }
	//
	// public void aload_2(){
	// }
	//
	// public void aload_3(){
	// }
	//
	// public void anewarray(){
	// }
	//
	// public void areturn(){
	// }
	//
	// public void arraylength(){
	// }
	//
	// public void astore(){
	// }
	//
	// public void astore_0(){
	// }
	//
	// public void astore_1(){
	// }
	//
	// public void astore_2(){
	// }
	//
	// public void astore_3(){
	// }
	//
	// public void athrow(){
	// }
	//
	// public void baload(){
	// }
	//
	// public void bastore(){
	// }
	//
	// public void bipush(){
	// }
	//
	// public void caload(){
	// }
	//
	// public void castore(){
	// }
	//
	// public void checkcast(){
	// }
	//
	// public void d2f(){
	// }
	//
	// public void d2i(){
	// }
	//
	// public void d2l(){
	// }
	//
	// public void dadd(){
	// }
	//
	// public void daload(){
	// }
	//
	// public void dastore(){
	// }
	//
	// public void dcmpg(){
	// }
	//
	// public void dcmpl(){
	// }
	//
	// public void dconst_0(){
	// }
	//
	// public void dconst_1(){
	// }
	//
	// public void ddiv(){
	// }
	//
	// public void dload(){
	// }
	//
	// public void dload_0(){
	// }
	//
	// public void dload_1(){
	// }
	//
	// public void dload_2(){
	// }
	//
	// public void dload_3(){
	// }
	//
	// public void dmul(){
	// }
	//
	// public void dneg(){
	// }
	//
	// public void drem(){
	// }
	//
	// public void dreturn(){
	// }
	//
	// public void dstore(){
	// }
	//
	// public void dstore_0(){
	// }
	//
	// public void dstore_1(){
	// }
	//
	// public void dstore_2(){
	// }
	//
	// public void dstore_3(){
	// }
	//
	// public void dsub(){
	// }
	//
	// public void dup(){
	// }
	//
	// public void dup2(){
	// }
	//
	// public void dup2_x1(){
	// }
	//
	// public void dup2_x2(){
	// }
	//
	// public void dup_x1(){
	// }
	//
	// public void dup_x2(){
	// }
	//
	// public void f2d(){
	// }
	//
	// public void f2i(){
	// }
	//
	// public void f2l(){
	// }
	//
	// public void fadd(){
	// }
	//
	// public void faload(){
	// }
	//
	// public void fastore(){
	// }
	//
	// public void fcmpg(){
	// }
	//
	// public void fcmpl(){
	// }
	//
	// public void fconst_0(){
	// }
	//
	// public void fconst_1(){
	// }
	//
	// public void fconst_2(){
	// }
	//
	// public void fdiv(){
	// }
	//
	// public void fload(){
	// }
	//
	// public void fload_0(){
	// }
	//
	// public void fload_1(){
	// }
	//
	// public void fload_2(){
	// }
	//
	// public void fload_3(){
	// }
	//
	// public void fmul(){
	// }
	//
	// public void fneg(){
	// }
	//
	// public void frem(){
	// }
	//
	// public void freturn(){
	// }
	//
	// public void fstore(){
	// }
	//
	// public void fstore_0(){
	// }
	//
	// public void fstore_1(){
	// }
	//
	// public void fstore_2(){
	// }
	//
	// public void fstore_3(){
	// }
	//
	// public void fsub(){
	// }
	//
	// public void getfield(){
	// }
	//
	// public void getstatic(){
	// }
	//
	// /**
	// * TODO explain the suffic: goto is a protected word in Java
	// */
	// public void goto_(){
	// }
	//
	// public void goto_w(){
	// }
	//
	// public void i2b(){
	// }
	//
	// public void i2c(){
	// }
	//
	// public void i2d(){
	// }
	//
	// public void i2f(){
	// }
	//
	// public void i2l(){
	// }
	//
	// public void i2s(){
	// }
	//
	// public void iadd(){
	// }
	//
	// public void iaload(){
	// }
	//
	// public void iand(){
	// }
	//
	// public void iastore(){
	// }
	//
	// public void iconst_0(){
	// }
	//
	// public void iconst_1(){
	// }
	//
	// public void iconst_2(){
	// }
	//
	// public void iconst_3(){
	// }
	//
	// public void iconst_4(){
	// }
	//
	// public void iconst_5(){
	// }
	//
	// public void iconst_m1(){
	// }
	//
	// public void idiv(){
	// }
	//
	// public void ifeq(){
	// }
	//
	// public void ifge(){
	// }
	//
	// public void ifgt(){
	// }
	//
	// public void ifle(){
	// }
	//
	// public void iflt(){
	// }
	//
	// public void ifne(){
	// }
	//
	// public void ifnonnull(){
	// }
	//
	// public void ifnull(){
	// }
	//
	// public void if_acmpeq(){
	// }
	//
	// public void if_acmpne(){
	// }
	//
	// public void if_icmpeq(){
	// }
	//
	// public void if_icmpge(){
	// }
	//
	// public void if_icmpgt(){
	// }
	//
	// public void if_icmple(){
	// }
	//
	// public void if_icmplt(){
	// }
	//
	// public void if_icmpne(){
	// }
	//
	// public void iinc(){
	// }
	//
	// public void iload(){
	// }
	//
	// public void iload_0(){
	// }
	//
	// public void iload_1(){
	// }
	//
	// public void iload_2(){
	// }
	//
	// public void iload_3(){
	// }
	//
	// public void imul(){
	// }
	//
	// public void ineg(){
	// }
	//
	// /**
	// * TODO explain the suffic: instanceof is a protected word in Java
	// */
	// public void instanceof_(){
	// }
	//
	// public void invokedynamic(){
	// }
	//
	// public void invokeinterface(){
	// }
	//
	// public void invokespecial(){
	// }
	//
	// public void invokestatic(){
	// }
	//
	// public void invokevirtual(){
	// }
	//
	// public void ior(){
	// }
	//
	// public void irem(){
	// }
	//
	// public void ireturn(){
	// }
	//
	// public void ishl(){
	// }
	//
	// public void ishr(){
	// }
	//
	// public void istore(){
	// }
	//
	// public void istore_0(){
	// }
	//
	// public void istore_1(){
	// }
	//
	// public void istore_2(){
	// }
	//
	// public void istore_3(){
	// }
	//
	// public void isub(){
	// }
	//
	// public void iushr(){
	// }
	//
	// public void ixor(){
	// }
	//
	// public void jsr(){
	// }
	//
	// public void jsr_w(){
	// }
	//
	// public void l2d(){
	// }
	//
	// public void l2f(){
	// }
	//
	// public void l2i(){
	// }
	//
	// public void ladd(){
	// }
	//
	// public void laload(){
	// }
	//
	// public void land(){
	// }
	//
	// public void lastore(){
	// }
	//
	// public void lcmp(){
	// }
	//
	// public void lconst_0(){
	// }
	//
	// public void lconst_1(){
	// }
	//
	// public void ldc(){
	// }
	//
	// public void ldc2_w(){
	// }
	//
	// public void ldc_w(){
	// }
	//
	// public void ldiv(){
	// }
	//
	// public void lload(){
	// }
	//
	// public void lload_0(){
	// }
	//
	// public void lload_1(){
	// }
	//
	// public void lload_2(){
	// }
	//
	// public void lload_3(){
	// }
	//
	// public void lmul(){
	// }
	//
	// public void lneg(){
	// }
	//
	// public void lookupswitch(){
	// }
	//
	// public void lor(){
	// }
	//
	// public void lrem(){
	// }
	//
	// public void lreturn(){
	// }
	//
	// public void lshl(){
	// }
	//
	// public void lshr(){
	// }
	//
	// public void lstore(){
	// }
	//
	// public void lstore_0(){
	// }
	//
	// public void lstore_1(){
	// }
	//
	// public void lstore_2(){
	// }
	//
	// public void lstore_3(){
	// }
	//
	// public void lsub(){
	// }
	//
	// public void lushr(){
	// }
	//
	// public void lxor(){
	// }
	//
	// public void monitorenter(){
	// }
	//
	// public void monitorexit(){
	// }
	//
	// public void multianewarray(){
	// }
	//
	// /**
	// * TODO explain the suffic: goto is a protected word in Java
	// */
	// public void new_(){
	// }
	//
	// public void newarray(){
	// }
	//
	// public void nop(){
	// }
	//
	// public void pop(){
	// }
	//
	// public void pop2(){
	// }
	//
	// public void putfield(){
	// }
	//
	// public void putstatic(){
	// }
	//
	// public void ret(){
	// }
	//
	// /**
	// * TODO explain the suffic: goto is a protected word in Java
	// */
	// public void return_(){
	// }
	//
	// public void saload(){
	// }
	//
	// public void sastore(){
	// }
	//
	// public void sipush(){
	// }
	//
	// public void swap(){
	// }
	//
	// public void tableswitch(){
	// }
	//
	// public void wide(){
	// }
	// public void executeAALOAD(){
	// }
	//
	// public void executeAASTORE(){
	// }
	//
	// public void executeACONST_NULL(){
	// }
	//
	// public void executeALOAD(){
	// }
	//
	// public void executeALOAD_0(){
	// }
	//
	// public void executeALOAD_1(){
	// }
	//
	// public void executeALOAD_2(){
	// }
	//
	// public void executeALOAD_3(){
	// }
	//
	// public void executeANEWARRAY(){
	// }
	//
	// public void executeARETURN(){
	// }
	//
	// public void executeARRAYLENGTH(){
	// }
	//
	// public void executeASTORE(){
	// }
	//
	// public void executeASTORE_0(){
	// }
	//
	// public void executeASTORE_1(){
	// }
	//
	// public void executeASTORE_2(){
	// }
	//
	// public void executeASTORE_3(){
	// }
	//
	// public void executeATHROW(){
	// }
	//
	// public void executeBALOAD(){
	// }
	//
	// public void executeBASTORE(){
	// }
	//
	// public void executeBIPUSH(){
	// }
	//
	// public void executeCALOAD(){
	// }
	//
	// public void executeCASTORE(){
	// }
	//
	// public void executeCHECKCAST(){
	// }
	//
	// public void executeD2F(){
	// }
	//
	// public void executeD2I(){
	// }
	//
	// public void executeD2L(){
	// }
	//
	// public void executeDADD(){
	// }
	//
	// public void executeDALOAD(){
	// }
	//
	// public void executeDASTORE(){
	// }
	//
	// public void executeDCMPG(){
	// }
	//
	// public void executeDCMPL(){
	// }
	//
	// public void executeDCONST_0(){
	// }
	//
	// public void executeDCONST_1(){
	// }
	//
	// public void executeDDIV(){
	// }
	//
	// public void executeDLOAD(){
	// }
	//
	// public void executeDLOAD_0(){
	// }
	//
	// public void executeDLOAD_1(){
	// }
	//
	// public void executeDLOAD_2(){
	// }
	//
	// public void executeDLOAD_3(){
	// }
	//
	// public void executeDMUL(){
	// }
	//
	// public void executeDNEG(){
	// }
	//
	// public void executeDREM(){
	// }
	//
	// public void executeDRETURN(){
	// }
	//
	// public void executeDSTORE(){
	// }
	//
	// public void executeDSTORE_0(){
	// }
	//
	// public void executeDSTORE_1(){
	// }
	//
	// public void executeDSTORE_2(){
	// }
	//
	// public void executeDSTORE_3(){
	// }
	//
	// public void executeDSUB(){
	// }
	//
	// public void executeDUP(){
	// }
	//
	// public void executeDUP2(){
	// }
	//
	// public void executeDUP2_X1(){
	// }
	//
	// public void executeDUP2_X2(){
	// }
	//
	// public void executeDUP_X1(){
	// }
	//
	// public void executeDUP_X2(){
	// }
	//
	// public void executeF2D(){
	// }
	//
	// public void executeF2I(){
	// }
	//
	// public void executeF2L(){
	// }
	//
	// public void executeFADD(){
	// }
	//
	// public void executeFALOAD(){
	// }
	//
	// public void executeFASTORE(){
	// }
	//
	// public void executeFCMPG(){
	// }
	//
	// public void executeFCMPL(){
	// }
	//
	// public void executeFCONST_0(){
	// }
	//
	// public void executeFCONST_1(){
	// }
	//
	// public void executeFCONST_2(){
	// }
	//
	// public void executeFDIV(){
	// }
	//
	// public void executeFLOAD(){
	// }
	//
	// public void executeFLOAD_0(){
	// }
	//
	// public void executeFLOAD_1(){
	// }
	//
	// public void executeFLOAD_2(){
	// }
	//
	// public void executeFLOAD_3(){
	// }
	//
	// public void executeFMUL(){
	// }
	//
	// public void executeFNEG(){
	// }
	//
	// public void executeFREM(){
	// }
	//
	// public void executeFRETURN(){
	// }
	//
	// public void executeFSTORE(){
	// }
	//
	// public void executeFSTORE_0(){
	// }
	//
	// public void executeFSTORE_1(){
	// }
	//
	// public void executeFSTORE_2(){
	// }
	//
	// public void executeFSTORE_3(){
	// }
	//
	// public void executeFSUB(){
	// }
	//
	// public void executeGETFIELD(){
	// }
	//
	// public void executeGETSTATIC(){
	// }
	//
	// public void executeGOTO(){
	// }
	//
	// public void executeGOTO_W(){
	// }
	//
	// public void executeI2B(){
	// }
	//
	// public void executeI2C(){
	// }
	//
	// public void executeI2D(){
	// }
	//
	// public void executeI2F(){
	// }
	//
	// public void executeI2L(){
	// }
	//
	// public void executeI2S(){
	// }
	//
	// public void executeIADD(){
	// }
	//
	// public void executeIALOAD(){
	// }
	//
	// public void executeIAND(){
	// }
	//
	// public void executeIASTORE(){
	// }
	//
	// public void executeICONST_0(){
	// }
	//
	// public void executeICONST_1(){
	// }
	//
	// public void executeICONST_2(){
	// }
	//
	// public void executeICONST_3(){
	// }
	//
	// public void executeICONST_4(){
	// }
	//
	// public void executeICONST_5(){
	// }
	//
	// public void executeICONST_M1(){
	// }
	//
	// public void executeIDIV(){
	// }
	//
	// public void executeIFEQ(){
	// }
	//
	// public void executeIFGE(){
	// }
	//
	// public void executeIFGT(){
	// }
	//
	// public void executeIFLE(){
	// }
	//
	// public void executeIFLT(){
	// }
	//
	// public void executeIFNE(){
	// }
	//
	// public void executeIFNONNULL(){
	// }
	//
	// public void executeIFNULL(){
	// }
	//
	// public void executeIF_ACMPEQ(){
	// }
	//
	// public void executeIF_ACMPNE(){
	// }
	//
	// public void executeIF_ICMPEQ(){
	// }
	//
	// public void executeIF_ICMPGE(){
	// }
	//
	// public void executeIF_ICMPGT(){
	// }
	//
	// public void executeIF_ICMPLE(){
	// }
	//
	// public void executeIF_ICMPLT(){
	// }
	//
	// public void executeIF_ICMPNE(){
	// }
	//
	// public void executeIINC(){
	// }
	//
	// public void executeILOAD(){
	// }
	//
	// public void executeILOAD_0(){
	// }
	//
	// public void executeILOAD_1(){
	// }
	//
	// public void executeILOAD_2(){
	// }
	//
	// public void executeILOAD_3(){
	// }
	//
	// public void executeIMUL(){
	// }
	//
	// public void executeINEG(){
	// }
	//
	// public void executeINSTANCEOF(){
	// }
	//
	// public void executeINVOKEDYNAMIC(){
	// }
	//
	// public void executeINVOKEINTERFACE(){
	// }
	//
	// public void executeINVOKESPECIAL(){
	// }
	//
	// public void executeINVOKESTATIC(){
	// }
	//
	// public void executeINVOKEVIRTUAL(){
	// }
	//
	// public void executeIOR(){
	// }
	//
	// public void executeIREM(){
	// }
	//
	// public void executeIRETURN(){
	// }
	//
	// public void executeISHL(){
	// }
	//
	// public void executeISHR(){
	// }
	//
	// public void executeISTORE(){
	// }
	//
	// public void executeISTORE_0(){
	// }
	//
	// public void executeISTORE_1(){
	// }
	//
	// public void executeISTORE_2(){
	// }
	//
	// public void executeISTORE_3(){
	// }
	//
	// public void executeISUB(){
	// }
	//
	// public void executeIUSHR(){
	// }
	//
	// public void executeIXOR(){
	// }
	//
	// public void executeJSR(){
	// }
	//
	// public void executeJSR_W(){
	// }
	//
	// public void executeL2D(){
	// }
	//
	// public void executeL2F(){
	// }
	//
	// public void executeL2I(){
	// }
	//
	// public void executeLADD(){
	// }
	//
	// public void executeLALOAD(){
	// }
	//
	// public void executeLAND(){
	// }
	//
	// public void executeLASTORE(){
	// }
	//
	// public void executeLCMP(){
	// }
	//
	// public void executeLCONST_0(){
	// }
	//
	// public void executeLCONST_1(){
	// }
	//
	// public void executeLDC(){
	// }
	//
	// public void executeLDC2_W(){
	// }
	//
	// public void executeLDC_W(){
	// }
	//
	// public void executeLDIV(){
	// }
	//
	// public void executeLLOAD(){
	// }
	//
	// public void executeLLOAD_0(){
	// }
	//
	// public void executeLLOAD_1(){
	// }
	//
	// public void executeLLOAD_2(){
	// }
	//
	// public void executeLLOAD_3(){
	// }
	//
	// public void executeLMUL(){
	// }
	//
	// public void executeLNEG(){
	// }
	//
	// public void executeLOOKUPSWITCH(){
	// }
	//
	// public void executeLOR(){
	// }
	//
	// public void executeLREM(){
	// }
	//
	// public void executeLRETURN(){
	// }
	//
	// public void executeLSHL(){
	// }
	//
	// public void executeLSHR(){
	// }
	//
	// public void executeLSTORE(){
	// }
	//
	// public void executeLSTORE_0(){
	// }
	//
	// public void executeLSTORE_1(){
	// }
	//
	// public void executeLSTORE_2(){
	// }
	//
	// public void executeLSTORE_3(){
	// }
	//
	// public void executeLSUB(){
	// }
	//
	// public void executeLUSHR(){
	// }
	//
	// public void executeLXOR(){
	// }
	//
	// public void executeMONITORENTER(){
	// }
	//
	// public void executeMONITOREXIT(){
	// }
	//
	// public void executeMULTIANEWARRAY(){
	// }
	//
	// public void executeNEW(){
	// }
	//
	// public void executeNEWARRAY(){
	// }
	//
	// public void executeNOP(){
	// }
	//
	// public void executePOP(){
	// }
	//
	// public void executePOP2(){
	// }
	//
	// public void executePUTFIELD(){
	// }
	//
	// public void executePUTSTATIC(){
	// }
	//
	// public void executeRET(){
	// }
	//
	// public void executeRETURN(){
	// }
	//
	// public void executeSALOAD(){
	// }
	//
	// public void executeSASTORE(){
	// }
	//
	// public void executeSIPUSH(){
	// }
	//
	// public void executeSWAP(){
	// }
	//
	// public void executeTABLESWITCH(){
	// }
	//
	// public void executeWIDE(){
	// }


}
