package de.uka.ipd.sdq.ByCounter.test.helpers;

/**
 * A class containing methods that provoke the occurrence 
 * of specific Java bytecodes.

 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
@SuppressWarnings("unused")
public class ASMBytecodeOccurences {

	/**
	 * Test class for invoke tests.
	 *
	 */
	public static class InterfaceImplementingClass implements ISimpleInterface {
		
		public static char character = 'c';
		
		public static void staticMethod() {
			Object o = "1";
		}
		
		public int integer = 0;
		
		public void test() {
			Object o = "0";
		}
	}
	
	/**
	 * Test interface for invoke tests.
	 *
	 */
	public static interface ISimpleInterface {
		void test();
	}
	
	/**
	 * Loads a reference of different types from an array.
	 *
	 */
	public static void arrayInstructions() {
		Object[] numbers = {new Object(), new Object()};
		Object n = numbers[numbers.length-1];
		Object[][] multichars = new Object[2][2];
		boolean[] bools = {true, false};
		boolean b = bools[0];
		char[] chars = {'a', 'b'};
		char c = chars[0];
		double[] doubles = {1.0, 2.0};
		double d = doubles[0];
		float[] floats = {1.0f, 2.0f};
		float f = floats[0];
		int[] ints = {1, 2};
		int i = ints[0];
		long[] longs = {1, 2};
		long l = longs[0];
		short[] shorts = {1, 2};
		short s = shorts[0];
	}
	
	public static void branches1() {
		Object obj1 = "a";
		Object obj2 = "a";
		if(obj1 == obj2) {
			obj1 = "b";
		}
		if (obj1 != obj2) {
			obj1 = "c";
		}
		if(obj1 == null) {
			obj1 = "d";
		}
		if(obj1 != null) {
			obj1 = "e";
		}
		if(obj1 instanceof String) {	// this also causes IFEQ!
			obj1 = "f";
		}
		
		int i1 = 1;
		int i2 = 2;
		
		if(i1 == i2) {
			i1 = 3;
		}
		if(i1 != i2) {
			i1 = 4;
		}
		if(i1 < i2) {
			i1 = 5;
		}
		if(i1 <= i2) {
			i1 = 6;
		}
		if(i1 >= i2) {
			i1 = 7;
		}
		if(i1 > i2) {
			i1 = 8;
		}
		if(i1 < 0) {
			i1 = 9;
		}
		if(i1 <= 0) {
			i1 = 10;
		}
		if(i1 == 0) {
			i1 = 11;
		}
		if(i1 != 0) {
			i1 = 12;
		}
		if(i1 >= 0) {
			i1 = 13;
		}
		if(i1 > 0) {
			i1 = 14;
		}
	
	}
	
	/**
	 * Branches, comparisons
	 *
	 */
	public static int branches() {
		int i1 = 1;
		int i2 = 2;
		
		switch(i1) {
		case 0:
			i1 = 15;
			break;
		default:
			i1 = 16;
			break;
		}

		while(true) {
			try{
				i1 = 0;
				break;
			} finally {
				i1 = 1;
			}
		}
		
		double d1 = 0.1;
		double d2 = 0.2;
		if(d1 > d2) {	// also causes IFLE
			d1 = 3.3;
		}
		if(d1 < d2) {	// also causes IFGE
			d1 = 1.2;
		}
		
		float f1 = 0.1f;
		float f2 = 0.2f;
		if(f1 > f2) {	// also causes IFLE
			f1 = 3.3f;
		}
		if(f1 < f2) {	// also causes IFGE
			f1 = 1.2f;
		}
		
		// LOOKUPSWITCH:
		switch (i1) {
			case 1:     return(10);
			case 10:    return(100);
			default:    return(0);
		}
	}
	
	/**
	 * Casting an Object (a String; "") back to a String.
	 * @return The casted object.
	 */
	public static String checkcast() {
		Object obj = "";
		return ((String)obj);
	}

	/**
	 * Constant assignmentments.
	 *
	 */
	public static void constants() {
		Object o = null;
		int a = 0;
		a = 1;
		a = 2;
		a = 3;
		a = 4;
		a = 5;
		a = -1;
		double d = 0.0;
		d = 1.0;
		float f = 0.0f;
		f = 1.0f;
		f = 2.0f;
		long l = 0L;
		l = 1L;
		
		// ldc:
		l = 33;
	}
	
	/**
	 * Converts/casts primitive types into one another.
	 *
	 */
	public static void conversions() {
		double d = 2.0;
		float f = (float)d;
		int i = (int)d;
		long l = (long)d;
		
		d = (double)f;
		i = (int)f;
		l = (long)f;
		
		d = (double)i;
		f = (float)i;
		l = (long)i;
		byte b = (byte)i;
		char c = (char)i;
		short s = (short)i;
		
		d = (double)l;
		f = (float)l;
		i = (int)l;
	}

	/**
	 * Object duplication.
	 *
	 */
	public static void dup() {
		String a = "a";
		String b = a + "b";
	}
	
	/**
	 * Simple returns a double. Needed for POP2 opcode.
	 * @return A double.
	 */
	public static double getaDouble() {
		return 2222.2222;
	}
	
	/**
	 * Uses invokations as well as PUTFIELD; PUTSTATIC.
	 *
	 */
	public static void invokationsAndClasses() {
		InterfaceImplementingClass.staticMethod();				// INVOKESTATIC
		ISimpleInterface inter = new InterfaceImplementingClass();	// INVOKESPECIAL
		inter.test();		// INVOKEINTERFACE
		((InterfaceImplementingClass)inter).integer = 2;
		InterfaceImplementingClass.character = 'd';
		inter.equals(".");	// INVOKEVIRTUAL
	}
	
	/**
	 * Uses math operators on primitive types.
	 *
	 */
	public static float math() {
		double d1 = 0.1;
		double d2 = 2.3;
		d1 += d2;
		d1 -= d2;
		d1 *= d2;
		// d2 has not changed and is therefore != 0
		d1 /= d2;
		d1 = -d1;
		d1 %= d2;
		
		float f1 = 0.1f;
		float f2 = 2.3f;
		f1 += f2;
		f1 -= f2;
		f1 *= f2;
		f1 /= f2;
		f1 = -f1;
		f1 %= f2;
		
		int i1 = 1;
		int i2 = 2;
		i1 += i2;
		i1 -= i2;
		i1 *= i2;
		i1 /= i2;
		i1 %= i2;
		i1 = -i1;
		i1 = i1 & i2;
		i1 = i1 | i2;
		i1 = i1 >> i2;
		i1 = i1 << i2;
		i1 = i1 >>> i2;
		i1++;
		i1 = i1 ^ i2;

		long l1 = 1;
		long l2 = 2;
		l1 += l2;
		l1 -= l2;
		l1 *= l2;
		l1 /= l2;
		l1 %= l2;
		l1 = -l1;
		l1 = l1 & l2;
		l1 = l1 | l2;
		l1 = l1 >> l2;
		l1 = l1 << l2;
		l1 = l1 >>> l2;
		l1 = l1 ^ l2;
		
		return f2;
	}
	
	/**
	 * Monitor enter and monitor exit. 
	 *
	 */
	public static void monitor() {
		String abc = "abc";
		synchronized(abc) {
			abc.length();
		}
	}
	
	/**
	 * Object creation.
	 *
	 */
	public static void newObj() {
		Object o = new Object();
	}
	
	/**
	 * Tests stack push operations for integers.
	 * BIPUSH, SIPUSH, ISTORE.
	 *
	 */
	public static void pushPop() {
		"POP".length();
		getaDouble();
		int i = 6;	// BIPUSH (smallest nonconstant integer as byte)
		i = 128;	// SIPUSH
	}

	/**
	 * Storing and loading of a reference.
	 *
	 */
	public static void refLoadStore() {
		Object obj0 = null;
		Object obj1 = obj0;
		double d0 = 0.0;
		double d1 = d0;
		float f0 = 0.0f;
		float f1 = f0;
		long l0 = 0;
		long l1 = l0;
		int i0 = 0;
		int i1 = i0;
	}
}
