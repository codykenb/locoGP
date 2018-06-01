package de.uka.ipd.sdq.ByCounter.test;

import java.util.Map;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.objectweb.asm.Opcodes;

import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;
import de.uka.ipd.sdq.ByCounter.parsing.ArrayCreation;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;
import de.uka.ipd.sdq.ByCounter.test.framework.expectations.Expectation;
import de.uka.ipd.sdq.ByCounter.test.helpers.ASMBytecodeOccurences;
import de.uka.ipd.sdq.ByCounter.test.helpers.Utils;
import de.uka.ipd.sdq.ByCounter.utils.ASMOpcodesMapper;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * A test suite providing testcases for de.uka.ipd.sdq.ByCount all different
 * bytecodes that ASM can differentiate.
 *
 * _Not_ covered by test cases are:
 * A*: ATHROW
 * D*: DUP_*, DUP2*
 * J*: JSR*
 * L*: LDC_W, LDC2_W (hidden as LDC by ASM)
 * N*: NOP
 * R*: RET
 * S*: SWAP
 * W*: WIDE (hidden by ASM)
 * 'unused opcodes': BREAKPOINT, IMPDEP1, IMPDEP2, XXXUNUSEDXXX

 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
@RunWith(Parameterized.class)
public class TestASMBytecodes extends AbstractByCounterTest {
	private static Logger log = Logger.getLogger(TestASMBytecodes.class.getCanonicalName());

	private static String testClassName = ASMBytecodeOccurences.class.getCanonicalName();

	@BeforeClass
	public static void runOnceBeforeAllTests() {
		log.info("Running unittests.");
	}


	private BytecodeCounter counter;

	/**
	 * This constructor is used by the Parametrized runner
	 * for running tests with different parameters.
	 * @param params {@link InstrumentationParameters} for the counting setup.
	 */
	public TestASMBytecodes(InstrumentationParameters params) {
		super(params);
		// create a BytecodeCounter
		this.counter = new BytecodeCounter();
	}

	/**
	 * Tests for ANEWARRAY, ARRAYLENGTH, MULTINEWARRAY,
	 * AA_LOAD, AA_STORE,
	 * BALOAD, BASTORE, CALOAD, CASTORE, DALOAD, DASTORE,
	 * FALOAD, FASTORE, IALOAD, IASTORE, LALOAD, LASTORE.
	 *
	 */
	@Test
	public void testArrayOpcodes() {
		// make sure array parameters get recorded:
		this.counter.getInstrumentationParams().setUseArrayParameterRecording(true);
		CountingResult r = Utils.getCountingResultForTest(this.counter,
				new MethodDescriptor(testClassName, "public static void arrayInstructions()"));

        // define expectations
        Expectation e = new Expectation(false);
        e.add()	// "section"
         .add(Opcodes.NEWARRAY, 7)
         .add(Opcodes.ANEWARRAY, 1)
         .add(Opcodes.ARRAYLENGTH, 1)
         .add(Opcodes.MULTIANEWARRAY, 1)

         .add(Opcodes.AALOAD, 1)
         .add(Opcodes.AASTORE, 2)
         .add(Opcodes.BALOAD, 1)
         .add(Opcodes.BASTORE, 1)
         .add(Opcodes.CALOAD, 1)
         .add(Opcodes.CASTORE, 2)
         .add(Opcodes.DALOAD, 1)
         .add(Opcodes.DASTORE, 2)
         .add(Opcodes.FALOAD, 1)
         .add(Opcodes.FASTORE, 2)
         .add(Opcodes.IALOAD, 1)
         .add(Opcodes.IASTORE, 2)
         .add(Opcodes.LALOAD, 1)
         .add(Opcodes.LASTORE, 2)
         .add(Opcodes.SALOAD, 1)
         .add(Opcodes.SASTORE, 2)

        // unrelated opcodes:
         .add("java.lang.Object", "void Object()", 2)
         .add(Opcodes.NEW, 2)
         .add(Opcodes.INVOKESPECIAL, 2)
         .add(Opcodes.RETURN, 1)
         .add(Opcodes.ISUB, 1)
         .add(Opcodes.DUP, 17)
         .add(Opcodes.ASTORE, 10)
         .add(Opcodes.DSTORE, 1)
         .add(Opcodes.FSTORE, 1)
         .add(Opcodes.LSTORE, 1)
         .add(Opcodes.ISTORE, 4)
         .add(Opcodes.ALOAD, 9)
         .add(Opcodes.LDC, 2)
         .add(Opcodes.BIPUSH, 2)
         .add(Opcodes.DCONST_1, 1)
         .add(Opcodes.FCONST_2, 1)
         .add(Opcodes.FCONST_1, 1)
         .add(Opcodes.LCONST_1, 1)
         .add(Opcodes.ICONST_2, 12)
         .add(Opcodes.ICONST_1, 11)
         .add(Opcodes.ICONST_0, 15);

        e.compare(new CountingResult[] {r});
        Map<ArrayCreation, Long> accs = r.getArrayCreationCounts();

		Assert.assertNotNull(accs);
		ArrayCreation objectArray = new ArrayCreation();
		objectArray.setTypeDesc("java/lang/Object");
		Assert.assertTrue(accs.containsKey(objectArray));

		ArrayCreation dim2Array = new ArrayCreation();
		dim2Array.setTypeDesc("[[Ljava/lang/Object;");
		dim2Array.setNumberOfDimensions(2);
		Assert.assertTrue("Expected a count for the creation of a 2d array.", accs.containsKey(dim2Array));
		Assert.assertEquals(9, accs.size());
		for(ArrayCreation ac : accs.keySet()) {
			Assert.assertEquals(1, accs.get(ac).longValue());
		}
	}

	/**
	 * Tests different kind of branching instructions
	 * and comparisons.
	 * IF*, INSTANCEOF, DCMPG, DCMPL, FCMPG, FCMPL,
	 * GOTO, JSR, TABLESWITCH.
	 *
	 */
	@Test
	public void testBranches() {
		CountingResult r = Utils.getCountingResultForTest(this.counter,
				new MethodDescriptor(testClassName, "public static int branches()"));

        // define expectations
        Expectation e = new Expectation(false);
        e.add()	// "section"
         .add(Opcodes.DCMPG, 1)
         .add(Opcodes.DCMPL, 1)
         .add(Opcodes.FCMPG, 1)
         .add(Opcodes.FCMPL, 1)

         .add(Opcodes.GOTO, 1)

         .add(Opcodes.TABLESWITCH, 1)
         .add(Opcodes.LOOKUPSWITCH, 1)
         .add(Opcodes.IFGE, 2)
         .add(Opcodes.IFLE, 2)

        // unrelated opcodes:
         .add(Opcodes.BIPUSH, 2)
         .add(Opcodes.LDC, 6)
         .add(Opcodes.ILOAD, 2)
         .add(Opcodes.ISTORE, 5)
         .add(Opcodes.FLOAD, 4)
         .add(Opcodes.FSTORE, 3)
         .add(Opcodes.DLOAD, 4)
         .add(Opcodes.DSTORE, 3)
         .add(Opcodes.ICONST_2, 1)
         .add(Opcodes.ICONST_1, 2)
         .add(Opcodes.ICONST_0, 1)
         .add(Opcodes.IRETURN, 1);

        e.compare(new CountingResult[] {r});

//		Assert.assertEquals(1, getOpcCount(r, DisplayOpcodes.JSR));
	}

	@Test
	public void testBranches1() {

		CountingResult r = Utils.getCountingResultForTest(this.counter,
				new MethodDescriptor(testClassName, "public static void branches1()"));

		// define expectations
        Expectation e = new Expectation(false);
        e.add()	// "section"
         .add(Opcodes.IF_ACMPEQ, 1)
         .add(Opcodes.IF_ACMPNE, 1)
         .add(Opcodes.IFNULL, 1)
         .add(Opcodes.IFNONNULL, 1)
         .add(Opcodes.INSTANCEOF, 1)

         .add(Opcodes.IF_ICMPEQ, 1)
         .add(Opcodes.IF_ICMPNE, 1)
         .add(Opcodes.IF_ICMPGT, 1)
         .add(Opcodes.IF_ICMPLT, 1)
         .add(Opcodes.IF_ICMPGE, 1)
         .add(Opcodes.IF_ICMPLE, 1)

         .add(Opcodes.IFEQ, 2)
         .add(Opcodes.IFNE, 1)
         .add(Opcodes.IFLT, 1)
         .add(Opcodes.IFGT, 1)
         .add(Opcodes.IFGE, 1)
         .add(Opcodes.IFLE, 1)

        // unrelated opcodes:
         .add(Opcodes.BIPUSH, 5)
         .add(Opcodes.LDC, 6)
         .add(Opcodes.ILOAD, 18)
         .add(Opcodes.ALOAD, 7) // 6 with older compiler?
         .add(Opcodes.ASTORE, 6)
         .add(Opcodes.ISTORE, 8)
         .add(Opcodes.ICONST_4, 1)
         .add(Opcodes.ICONST_2, 1)
         .add(Opcodes.ICONST_1, 1)
//         .add(Opcodes.ICONST_0, 1)
         .add(Opcodes.RETURN, 1);

        e.compare(new CountingResult[] {r});
	}

	/**
	 * Tests CHECKCAST.
	 *
	 */
	@Test
	public void testCHECKCAST() {
		CountingResult r = Utils.getCountingResultForTest(this.counter,
				new MethodDescriptor(testClassName, "public static java.lang.String checkcast()"));

		// define expectations
        Expectation e = new Expectation(false);
        e.add()	// "section"
         .add(Opcodes.CHECKCAST, 1)

        // unrelated opcodes:
         .add(Opcodes.LDC, 1)
         .add(Opcodes.ALOAD, 1)
         .add(Opcodes.ASTORE, 1)
         .add(Opcodes.ARETURN, 1);

        e.compare(new CountingResult[] {r});
	}

	/**
	 * Tests for opcode A_CONST_NULL, i.e. Object o = null;
	 * Tests for integer constant opcodes ICONST_M1 to ICONST_5.
	 * Tests for double constant opcodes DCONST_0 (0.0) and DCONST_1 (1.0).
	 * Tests for float constant opcodes FCONST_0 (0.0f),
	 * FCONST_1 (1.0f), FCONST_2 (2.0f).
	 * Tests for long constant opcodes LCONST_0 (0L) and LCONST_1(1L).
	 * Tests LDC.
	 *
	 */
	@Test
	public void testConstantOpcodes() {
		CountingResult r = Utils.getCountingResultForTest(this.counter,
				new MethodDescriptor(testClassName, "public static void constants()"));
		// define expectations

		Expectation e = new Expectation(false);
        e.add()	// "section"
		.add(Opcodes.ACONST_NULL,  1)
        .add(Opcodes.ICONST_M1,  1)
        .add(Opcodes.ICONST_0,  1)
        .add(Opcodes.ICONST_1,  1)
        .add(Opcodes.ICONST_2,  1)
        .add(Opcodes.ICONST_3,  1)
        .add(Opcodes.ICONST_4,  1)
        .add(Opcodes.ICONST_5,  1)
        .add(Opcodes.DCONST_0,  1)
        .add(Opcodes.DCONST_1,  1)
        .add(Opcodes.FCONST_0,  1)
        .add(Opcodes.FCONST_1,  1)
        .add(Opcodes.FCONST_2,  1)
        .add(Opcodes.LCONST_0,  1)
        .add(Opcodes.LCONST_1,  1)
        .add(Opcodes.LDC,  1)


        // unrelated opcodes:
         .add(Opcodes.ISTORE, 7)
         .add(Opcodes.LSTORE, 3)
         .add(Opcodes.FSTORE, 3)
         .add(Opcodes.DSTORE, 2)
         .add(Opcodes.ASTORE, 1)
         .add(Opcodes.RETURN, 1);

        e.compare(new CountingResult[] {r});

	}

	/**
	 * Tests D2F, D2I, D2L, F2D, F2I, F2L,
	 * I2D, I2F, I2B, I2C, I2L, I2S,
	 * L2D, L2F, L2I.
	 *
	 */
	@Test
	public void testConversions() {
		CountingResult r = Utils.getCountingResultForTest(this.counter,
				new MethodDescriptor(testClassName, "public static void conversions()"));


		Expectation e = new Expectation(false);
        e.add()	// "section"
        .add(Opcodes.D2F, 1)
        .add(Opcodes.D2I, 1)
        .add(Opcodes.D2L, 1)
        .add(Opcodes.F2D, 1)
        .add(Opcodes.F2I, 1)
        .add(Opcodes.F2L, 1)
        .add(Opcodes.I2D, 1)
        .add(Opcodes.I2F, 1)
        .add(Opcodes.I2B, 1)
        .add(Opcodes.I2C, 1)
        .add(Opcodes.I2L, 1)
        .add(Opcodes.I2S, 1)
        .add(Opcodes.L2D, 1)
        .add(Opcodes.L2F, 1)
        .add(Opcodes.L2I, 1)

        // unrelated opcodes:
         .add(Opcodes.LDC, 1)
         .add(Opcodes.ILOAD, 6)
         .add(Opcodes.ISTORE, 6)
         .add(Opcodes.LLOAD, 3)
         .add(Opcodes.LSTORE, 3)
         .add(Opcodes.FLOAD, 3)
         .add(Opcodes.FSTORE, 3)
         .add(Opcodes.DLOAD, 3)
         .add(Opcodes.DSTORE, 4)
         .add(Opcodes.RETURN, 1);

        e.compare(new CountingResult[] {r});
	}

	/**
	 * Tests object duplication.
	 *
	 */
	@Test
	public void testDUP() {
		CountingResult r = Utils.getCountingResultForTest(this.counter,
				new MethodDescriptor(testClassName, "public static void dup()"));


		Expectation e = new Expectation(false);
        e.add()	// "section"
        .add(Opcodes.DUP, 1)

        // unrelated opcodes:
         .add(Opcodes.LDC, 2)
         .add(Opcodes.ALOAD, 1)
         .add(Opcodes.ASTORE, 2)
         .add(Opcodes.INVOKEVIRTUAL, 2)
         .add(Opcodes.INVOKESPECIAL, 1)
         .add(Opcodes.INVOKESTATIC, 1)
         .add(Opcodes.NEW, 1)
         .add("java.lang.String.valueOf(Ljava/lang/Object;)Ljava/lang/String;", 1)
         .add("java.lang.StringBuilder.StringBuilder(Ljava/lang/String;)V", 1)
         .add("java.lang.StringBuilder.append(Ljava/lang/String;)Ljava/lang/StringBuilder;", 1)
         .add("java.lang.StringBuilder.toString()Ljava/lang/String;", 1)
         .add(Opcodes.RETURN, 1);

        e.compare(new CountingResult[] {r});
	}

	/**
	 * Tests method invokations.
	 * INVOKEINTERFACE, INVOKESPECIAL, INVOKEVIRTUAL, INVOKESTATIC
	 *
	 *
	 */
	@Test
	public void testInvokationsAndClasses() {
		CountingResult r = Utils.getCountingResultForTest(this.counter,
				new MethodDescriptor(testClassName, "public static void invokationsAndClasses()"));

		Expectation e = new Expectation(false);
        e.add()	// "section"
        .add(Opcodes.INVOKEINTERFACE,  1)
	    .add(Opcodes.INVOKESPECIAL,  1)
	    .add(Opcodes.INVOKESTATIC,  1)
	    .add(Opcodes.INVOKEVIRTUAL,  1)
	    .add(Opcodes.PUTFIELD,  1)
	    .add(Opcodes.PUTSTATIC,  1)

        // unrelated opcodes:
	     .add(Opcodes.ICONST_2, 1)
	     .add(Opcodes.BIPUSH, 1)
         .add(Opcodes.LDC, 1)
         .add(Opcodes.ALOAD, 3)
         .add(Opcodes.ASTORE, 1)
         .add(Opcodes.POP, 1)
         .add(Opcodes.DUP, 1)
         .add(Opcodes.CHECKCAST, 1)
         .add(Opcodes.NEW, 1)
         .add(ASMBytecodeOccurences.class.getCanonicalName() + "$ISimpleInterface.test()V", 1)
         .add(ASMBytecodeOccurences.class.getCanonicalName() + "$InterfaceImplementingClass.ASMBytecodeOccurences$InterfaceImplementingClass(Lde/uka/ipd/sdq/ByCounter/test/helpers/ASMBytecodeOccurences;)V", 1)
         .add(ASMBytecodeOccurences.class.getCanonicalName() + "$InterfaceImplementingClass.staticMethod()V", 1)
         .add(Object.class.getCanonicalName()+".equals(Ljava/lang/Object;)Z", 1)
         .add(Opcodes.RETURN, 1);

        e.compare(new CountingResult[] {r});
	}

	/**
	 * Tests for math operations on primitives.
	 * DADD, DDIV, DMUL, DNEG, DREM, DSUB,
	 * FADD, FDIV, FMUL, FNEG, FREM, FSUB,
	 * IADD, IAND, IOR, IXOR, IINC, IDIV, IMUL,
	 * INEG, IREM, ISUB, ISHL, ISHR, IUSHR,
	 * LADD, LAND, LOR, LXOR, LDIV, LMUL,
	 * LNEG, LREM, LSUB, LSHL, LSHR, LUSHR.
	 *
	 */
	@Test
	public void testMathOpcodes() {
		CountingResult r = Utils.getCountingResultForTest(counter,
				new MethodDescriptor(testClassName, "public static float math()"));

		Expectation e = new Expectation(false);
        e.add()	// "section"
		.add(Opcodes.DADD,  1)
        .add(Opcodes.DDIV,  1)
        .add(Opcodes.DMUL,  1)
        .add(Opcodes.DNEG,  1)
        .add(Opcodes.DREM,  1)
        .add(Opcodes.DSUB,  1)
        .add(Opcodes.FADD,  1)
        .add(Opcodes.FDIV,  1)
        .add(Opcodes.FMUL,  1)
        .add(Opcodes.FNEG,  1)
        .add(Opcodes.FREM,  1)
        .add(Opcodes.FSUB,  1)
        .add(Opcodes.IADD,  1)
        .add(Opcodes.IAND,  1)
        .add(Opcodes.IOR,  1)
        .add(Opcodes.IXOR,  1)
        .add(Opcodes.IINC,  1)
        .add(Opcodes.IDIV,  1)
        .add(Opcodes.IMUL,  1)
        .add(Opcodes.INEG,  1)
        .add(Opcodes.IREM,  1)
        .add(Opcodes.ISUB,  1)
        .add(Opcodes.ISHL,  1)
        .add(Opcodes.ISHR,  1)
        .add(Opcodes.IUSHR,  1)
        .add(Opcodes.LADD,  1)
        .add(Opcodes.LAND,  1)
        .add(Opcodes.LOR,  1)
        .add(Opcodes.LXOR,  1)
        .add(Opcodes.LDIV,  1)
        .add(Opcodes.LMUL,  1)
        .add(Opcodes.LNEG,  1)
        .add(Opcodes.LREM,  1)
        .add(Opcodes.LSUB,  1)
        .add(Opcodes.LSHL,  1)
        .add(Opcodes.LSHR,  1)
        .add(Opcodes.LUSHR,  1)

        // unrelated opcodes:
        .add(Opcodes.ICONST_1, 1)
	     .add(Opcodes.ICONST_2, 1)
         .add(Opcodes.LDC, 5)
         .add(Opcodes.LCONST_1, 1)
         .add(Opcodes.ILOAD, 23)
         .add(Opcodes.ISTORE, 14)
         .add(Opcodes.LLOAD, 23)
         .add(Opcodes.LSTORE, 14)
         .add(Opcodes.FLOAD, 12)
         .add(Opcodes.FSTORE, 8)
         .add(Opcodes.DLOAD, 11)
         .add(Opcodes.DSTORE, 8)
         .add(Opcodes.L2I, 3)
         .add(Opcodes.FRETURN, 1);

        e.compare(new CountingResult[] {r});

	}

	/**
	 * Tests MONITORENTER and MONITOREXIT.
	 *
	 */
	@Test
	public void testMonitor() {
		CountingResult r = Utils.getCountingResultForTest(counter,
				new MethodDescriptor(testClassName, "public static void monitor()"));

		Expectation e = new Expectation(false);
        e.add()	// "section"
    	.add(Opcodes.MONITORENTER,  1)
        .add(Opcodes.MONITOREXIT,  1)

        // unrelated opcodes:
         .add(Opcodes.LDC, 1)
         .add(Opcodes.ALOAD, 3)
         .add(Opcodes.ASTORE, 2)
         .add(Opcodes.INVOKEVIRTUAL, 1)
         .add("java.lang.String.length()I", 1)
         .add(Opcodes.GOTO, 1)
         .add(Opcodes.DUP, 1)
         .add(Opcodes.POP, 1)
         .add(Opcodes.RETURN, 1);

        e.compare(new CountingResult[] {r});
	}

	@Test
	public void testNew() {
		CountingResult r = Utils.getCountingResultForTest(counter,
				new MethodDescriptor(testClassName, "public static void newObj()"));

		Expectation e = new Expectation(false);
        e.add()	// "section"
    	.add(Opcodes.NEW,  1)

        // unrelated opcodes:
         .add(Opcodes.ASTORE, 1)
         .add(Opcodes.INVOKESPECIAL, 1)
         .add("java.lang.Object.Object()V", 1)
         .add(Opcodes.DUP, 1)
         .add(Opcodes.RETURN, 1);

        e.compare(new CountingResult[] {r});
	}

	/**
	 * Tests BIPUSH, SIPUSH, POP, POP2.
	 *
	 */
	@Test
	public void testPushPop() {
		CountingResult r = Utils.getCountingResultForTest(counter,
				new MethodDescriptor(testClassName, "public static void pushPop()"));
		Expectation e = new Expectation(false);
        e.add()	// "section"
        .add(Opcodes.POP,  1)
        .add(Opcodes.POP2,  1)
        .add(Opcodes.BIPUSH,  1)
        .add(Opcodes.SIPUSH,  1)

        // unrelated opcodes:
         .add(Opcodes.LDC, 1)
         .add(Opcodes.ISTORE, 2)
         .add(Opcodes.INVOKEVIRTUAL, 1)
         .add(Opcodes.INVOKESTATIC, 1)
         .add(ASMBytecodeOccurences.class.getCanonicalName() + ".getaDouble()D", 1)
         .add("java.lang.String.length()I", 1)
         .add(Opcodes.RETURN, 1);

        e.compare(new CountingResult[] {r});
	}

	/**
	 * Tests ALOAD, ASTORE, DLOAD, DSTORE, FLOAD, FSTORE,
	 * LLOAD, LSTORE, ILOAD, ISTORE.
	 * Not specifically tested are xLOAD_0 to xLOAD_3 since
	 * ASM groups them as xLOAD.
	 * The same goes for xSTORE_n.
	 *
	 */
	@Test
	public void testRefLoadStore() {
		CountingResult r = Utils.getCountingResultForTest(counter,
				new MethodDescriptor(testClassName, "public static void refLoadStore()"));

		Expectation e = new Expectation(false);
        e.add()	// "section"
        .add(Opcodes.ALOAD,  1)
        .add(Opcodes.ASTORE, 2)
        .add(Opcodes.DLOAD,  1)
        .add(Opcodes.DSTORE, 2)
        .add(Opcodes.FLOAD,  1)
        .add(Opcodes.FSTORE, 2)
        .add(Opcodes.LLOAD,  1)
        .add(Opcodes.LSTORE, 2)
        .add(Opcodes.ILOAD,  1)
        .add(Opcodes.ISTORE, 2)

        // unrelated opcodes:
        .add(Opcodes.ICONST_0, 1)
        .add(Opcodes.LCONST_0, 1)
        .add(Opcodes.FCONST_0, 1)
        .add(Opcodes.DCONST_0, 1)
        .add(Opcodes.ACONST_NULL, 1)
         .add(Opcodes.RETURN, 1);

        e.compare(new CountingResult[] {r});
	}

	/**
	 * RETURN statements:
	 * RETURN
	 */
	@Test
	public void testReturn() {
		CountingResult r;
		// RETURN
		r = Utils.getCountingResultForTest(counter,
				new MethodDescriptor(testClassName, "public static void refLoadStore()"));
		Assert.assertEquals(1, Utils.getOpcCount(r, ASMOpcodesMapper.RETURN));

		Expectation e = new Expectation(false);
        e.add()	// "section"
        .add(Opcodes.RETURN,  1)

        // unrelated opcodes:
        .add(Opcodes.ICONST_0, 1)
        .add(Opcodes.LCONST_0, 1)
        .add(Opcodes.FCONST_0, 1)
        .add(Opcodes.DCONST_0, 1)
        .add(Opcodes.ACONST_NULL, 1)
        .add(Opcodes.ALOAD, 1)
        .add(Opcodes.ASTORE, 2)
        .add(Opcodes.LLOAD, 1)
        .add(Opcodes.LSTORE, 2)
        .add(Opcodes.DLOAD, 1)
        .add(Opcodes.DSTORE, 2)
        .add(Opcodes.FLOAD, 1)
        .add(Opcodes.FSTORE, 2)
        .add(Opcodes.ILOAD, 1)
        .add(Opcodes.ISTORE, 2);

        e.compare(new CountingResult[] {r});
		cleanResults();
	}

	/**
	 * RETURN statements:
	 * ARETURN
	 */
	@Test
	public void testAReturn() {
		CountingResult r;
		// ARETURN
		r = Utils.getCountingResultForTest(counter,
				new MethodDescriptor(testClassName, "public static java.lang.String checkcast()"));
		// define expectations
		Expectation e = new Expectation(false);
        e.add()	// "section"
         .add(Opcodes.ARETURN, 1)

        // unrelated opcodes:
         .add(Opcodes.LDC, 1)
         .add(Opcodes.ALOAD, 1)
         .add(Opcodes.ASTORE, 1)
         .add(Opcodes.CHECKCAST, 1);

        e.compare(new CountingResult[] {r});
	}

	/**
	 * RETURN statements:
	 * DRETURN
	 */
	@Test
	public void testDReturn() {
		CountingResult r;
		Expectation e;
		// DRETURN
		r = Utils.getCountingResultForTest(counter,
				new MethodDescriptor(testClassName, "public static double getaDouble()"));
		// define expectations
        e = new Expectation(false);
        e.add()	// "section"
         .add(Opcodes.DRETURN, 1)

        // unrelated opcodes:
         .add(Opcodes.LDC, 1);
        e.compare(new CountingResult[] {r});
	}

	/**
	 * RETURN statements:
	 * FRETURN
	 */
	@Test
	public void testFReturn() {
		CountingResult r;
		Expectation e;
		// FRETURN
		r = Utils.getCountingResultForTest(counter,
				new MethodDescriptor(testClassName, "public static float math()"));
		// define expectations
        e = new Expectation(false);
        e.add()	// "section"
         .add(Opcodes.FRETURN, 1)

        // unrelated opcodes:
        .add(Opcodes.DADD,  1)
        .add(Opcodes.DDIV,  1)
        .add(Opcodes.DMUL,  1)
        .add(Opcodes.DNEG,  1)
        .add(Opcodes.DREM,  1)
        .add(Opcodes.DSUB,  1)
        .add(Opcodes.FADD,  1)
        .add(Opcodes.FDIV,  1)
        .add(Opcodes.FMUL,  1)
        .add(Opcodes.FNEG,  1)
        .add(Opcodes.FREM,  1)
        .add(Opcodes.FSUB,  1)
        .add(Opcodes.IADD,  1)
        .add(Opcodes.IAND,  1)
        .add(Opcodes.IOR,  1)
        .add(Opcodes.IXOR,  1)
        .add(Opcodes.IINC,  1)
        .add(Opcodes.IDIV,  1)
        .add(Opcodes.IMUL,  1)
        .add(Opcodes.INEG,  1)
        .add(Opcodes.IREM,  1)
        .add(Opcodes.ISUB,  1)
        .add(Opcodes.ISHL,  1)
        .add(Opcodes.ISHR,  1)
        .add(Opcodes.IUSHR,  1)
        .add(Opcodes.LADD,  1)
        .add(Opcodes.LAND,  1)
        .add(Opcodes.LOR,  1)
        .add(Opcodes.LXOR,  1)
        .add(Opcodes.LDIV,  1)
        .add(Opcodes.LMUL,  1)
        .add(Opcodes.LNEG,  1)
        .add(Opcodes.LREM,  1)
        .add(Opcodes.LSUB,  1)
        .add(Opcodes.LSHL,  1)
        .add(Opcodes.LSHR,  1)
        .add(Opcodes.LUSHR,  1)
        .add(Opcodes.ICONST_1, 1)
	    .add(Opcodes.ICONST_2, 1)
	    .add(Opcodes.LDC, 5)
	    .add(Opcodes.LCONST_1, 1)
	    .add(Opcodes.ILOAD, 23)
	    .add(Opcodes.ISTORE, 14)
	    .add(Opcodes.LLOAD, 23)
	    .add(Opcodes.LSTORE, 14)
	    .add(Opcodes.FLOAD, 12)
	    .add(Opcodes.FSTORE, 8)
	    .add(Opcodes.DLOAD, 11)
	    .add(Opcodes.DSTORE, 8)
	    .add(Opcodes.L2I, 3);

        e.compare(new CountingResult[] {r});
	}

	/**
	 * RETURN statements:
	 * IRETURN
	 */
	@Test
	public void testIReturn() {
		CountingResult r;
		Expectation e;
		// IRETURN
		r = Utils.getCountingResultForTest(counter,
				new MethodDescriptor(testClassName, "public static int branches()"));
		// define expectations
        e = new Expectation(false);
        e.add()	// "section"
         .add(Opcodes.IRETURN, 1)

        // unrelated opcodes:
         .add(Opcodes.DCMPG, 1)
         .add(Opcodes.DCMPL, 1)
         .add(Opcodes.FCMPG, 1)
         .add(Opcodes.FCMPL, 1)
         .add(Opcodes.GOTO, 1)
         .add(Opcodes.TABLESWITCH, 1)
         .add(Opcodes.LOOKUPSWITCH, 1)
         .add(Opcodes.IFGE, 2)
         .add(Opcodes.IFLE, 2)
         .add(Opcodes.BIPUSH, 2)
         .add(Opcodes.LDC, 6)
         .add(Opcodes.ILOAD, 2)
         .add(Opcodes.ISTORE, 5)
         .add(Opcodes.FLOAD, 4)
         .add(Opcodes.FSTORE, 3)
         .add(Opcodes.DLOAD, 4)
         .add(Opcodes.DSTORE, 3)
         .add(Opcodes.ICONST_2, 1)
         .add(Opcodes.ICONST_1, 2)
         .add(Opcodes.ICONST_0, 1);

        e.compare(new CountingResult[] {r});
	}
}
