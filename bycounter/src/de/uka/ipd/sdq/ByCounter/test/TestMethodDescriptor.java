package de.uka.ipd.sdq.ByCounter.test;

import org.junit.Assert;
import org.junit.Test;

import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @author Florian Schreier
 * @since 0.1
 * @version 1.3
 */
public class TestMethodDescriptor {

	private static final String CLASS_NAME = "de.uka.ipd.sdq.ByCounter.test.Test";

	/**
	 * Test MethodDescriptor construction from a Java signature.
	 */
	@Test
	public void testGetFromJavaSignature() {
		// TODO: add many more different signatures for testing.

		// 1
		MethodDescriptor d = new MethodDescriptor(CLASS_NAME,
				"public static void testVoid(int[][] a, org.junit.Test b)");
		Assert.assertNotNull("Method descriptor was null. This means it could not be parsed correctly", d);
		Assert.assertEquals("Package name is not correct.", "de.uka.ipd.sdq.ByCounter.test", d.getPackageName());
		Assert.assertEquals("Class name was not parsed correctly", "Test", d.getSimpleClassName());
		Assert.assertEquals("Canonical class name was not parsed correctly", "de.uka.ipd.sdq.ByCounter.test.Test", d
				.getCanonicalClassName());
		Assert.assertEquals("Method descriptor method name is not correct.", "testVoid", d.getSimpleMethodName());
		Assert.assertEquals("Method descriptor is not correct.", "([[ILorg/junit/Test;)V", d.getDescriptor());
		Assert.assertTrue("Method was not recognised as static", d.getMethodIsStatic());

		// 2
		d = new MethodDescriptor(CLASS_NAME, "public int factor(double A[][],  int pivot[])");
		Assert.assertNotNull("Method descriptor was null. This means it could not be parsed correctly", d);
		Assert.assertEquals("Method descriptor method name is not correct.", "factor", d.getSimpleMethodName());
		Assert.assertEquals("Method descriptor is not correct.", "([[D[I)I", d.getDescriptor());
		Assert.assertFalse("Method was falsely recognised as static", d.getMethodIsStatic());

		// 3
		d = new MethodDescriptor(CLASS_NAME,
				"public static java.util.List <Integer> factor(java.util.List<java.util.List<Double>> A,  final int pivot[])");
		Assert.assertNotNull("Method descriptor was null. This means it could not be parsed correctly", d);
		Assert.assertEquals("Method descriptor method name is not correct.", "factor", d.getSimpleMethodName());
		Assert.assertEquals("Method descriptor is not correct.", "(Ljava/util/List;[I)Ljava/util/List;", d
				.getDescriptor());

		Assert.assertEquals("new ArrayList();", MethodDescriptor.removeGenericTyping("new ArrayList<List<Double>>();"));
		Assert.assertTrue("Method was not recognised as static", d.getMethodIsStatic());

		// 4 - garbage input
		Exception e = null;
		try {
			d = new MethodDescriptor("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890",
					"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890");
		} catch (RuntimeException r) {
			e = r;
		}
		Assert.assertNotNull("Expected runtime exception on garbage input.", e);

		// 5 - garbage input + braces
		e = null;
		try {
			d = new MethodDescriptor(
					"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890",
					"abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890(abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890)");
		} catch (RuntimeException r) {
			e = r;
		}
		Assert.assertNotNull("Expected runtime exception on garbage input.", e);

		// 6 - garbage input + empty braces + garbage
		d = new MethodDescriptor("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890",
				"abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890() abbdd");
		Assert.assertNotNull("Method descriptor was null. " + "This means it could not be parsed correctly", d);
		Assert.assertEquals("Method descriptor method name is not correct.", "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890", d
				.getSimpleMethodName());
		Assert.assertEquals("Method descriptor is not correct.", "()Labcdefghijklmnopqrstuvwxyz;", d.getDescriptor());
		Assert.assertFalse("Method was recognised as static", d.getMethodIsStatic());

	}

	/**
	 * Test calling methods with null parameters.
	 */
	@Test
	public void testNullInput() {
		boolean exceptionCaught = false;
		try {
			MethodDescriptor._constructMethodDescriptorFromASM("", "", "");
		} catch (RuntimeException r) {
			exceptionCaught = true;
		}
		Assert.assertTrue("MethodDescriptor did not throw expected exception", exceptionCaught);

		exceptionCaught = false;
		try {
			MethodDescriptor._constructMethodDescriptorFromASM("", "", null);
		} catch (RuntimeException r) {
			exceptionCaught = true;
		}
		Assert.assertTrue("MethodDescriptor did not throw expected exception", exceptionCaught);

		exceptionCaught = false;
		try {
			MethodDescriptor._constructMethodDescriptorFromASM("", null, "");
		} catch (RuntimeException r) {
			exceptionCaught = true;
		}
		Assert.assertTrue("MethodDescriptor did not throw expected exception", exceptionCaught);

		exceptionCaught = false;
		try {
			MethodDescriptor._constructMethodDescriptorFromASM(null, "", "");
		} catch (RuntimeException r) {
			exceptionCaught = true;
		}
		Assert.assertTrue("MethodDescriptor did not throw expected exception", exceptionCaught);
	}

	@Test
	public void testMethodSignatureParser() {
		final String expected = CLASS_NAME + ".getInteger()I";
		MethodDescriptor md;

		md = new MethodDescriptor(CLASS_NAME, "public static int getInteger()");
		assertCorrectDescriptor(md, expected, false, true);

		md = new MethodDescriptor(CLASS_NAME, "protected static int getInteger()");
		assertCorrectDescriptor(md, expected, false, true);

		md = new MethodDescriptor(CLASS_NAME, "static int getInteger()");
		assertCorrectDescriptor(md, expected, false, true);

		md = new MethodDescriptor(CLASS_NAME, "public int getInteger()");
		assertCorrectDescriptor(md, expected, false, false);

		md = new MethodDescriptor(CLASS_NAME, "int getInteger()");
		assertCorrectDescriptor(md, expected, false, false);
	}

	@Test
	public void testConstructorSignatureParser() {
		final String expected = CLASS_NAME + ".Test()V";
		MethodDescriptor md;

		md = new MethodDescriptor(CLASS_NAME, "Test()");
		assertCorrectDescriptor(md, expected, true, false);

		md = new MethodDescriptor(CLASS_NAME, "public Test()");
		assertCorrectDescriptor(md, expected, true, false);
	}

	private static void assertCorrectDescriptor(MethodDescriptor md, String expectedDescriptor,
			boolean expectedConstructor, boolean expectedStatic) {
		Assert.assertEquals("Failure on parsing method name", expectedDescriptor, md.getCanonicalMethodName());
		Assert.assertEquals("Failure on parsing constructor", expectedConstructor, md.isConstructor());
		Assert.assertEquals("Failure on parsing static", expectedStatic, md.getMethodIsStatic());
	}
}
