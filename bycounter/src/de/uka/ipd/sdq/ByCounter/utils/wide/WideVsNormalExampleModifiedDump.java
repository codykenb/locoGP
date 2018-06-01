package de.uka.ipd.sdq.ByCounter.utils.wide;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class WideVsNormalExampleModifiedDump implements Opcodes {
	static int localVarAddressIncrease;
	public static byte[] dump() throws Exception {

		ClassWriter cw = new ClassWriter(0);
//		FieldVisitor fv;
		MethodVisitor mv;
//		AnnotationVisitor av0;

		cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, "WideVsNormalExample", null,
				"java/lang/Object", null);

		cw.visitSource("WideVsNormalExample.java", null);

		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			//mv.visitLineNumber(1, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>",
					"()V");
			mv.visitInsn(RETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "LWideVsNormalExample;", null, l0,
					l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw
					.visitMethod(
							ACC_PUBLIC,
							"testWide_ISTORE_3Vars_4Iters_falseArrayExplicit_5ArraySize",
							"()J", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			//mv.visitLineNumber(3, l0);
			mv.visitMethodInsn(INVOKESTATIC, "TimerHelper", "time", "()J");
			mv.visitVarInsn(LSTORE, 1);
			
			//1. initialisation
			
			Label l1 = new Label();
			mv.visitLabel(l1);
			//mv.visitLineNumber(5, l1);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, localVarAddressIncrease+5);
			Label l2 = new Label();
			mv.visitLabel(l2);
			//mv.visitLineNumber(6, l2);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, localVarAddressIncrease+6);
			Label l3 = new Label();
			mv.visitLabel(l3);
			//mv.visitLineNumber(7, l3);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, localVarAddressIncrease+7);
			
			//2. assignments
			
			Label l4 = new Label();
			mv.visitLabel(l4);
			//mv.visitLineNumber(8, l4);
			mv.visitLdcInsn(new Integer(-1390993650));
			mv.visitVarInsn(ISTORE, localVarAddressIncrease+7);
			Label l5 = new Label();
			mv.visitLabel(l5);
			//mv.visitLineNumber(9, l5);
			mv.visitLdcInsn(new Integer(1255477124));
			mv.visitVarInsn(ISTORE, localVarAddressIncrease+5);
			Label l6 = new Label();
			mv.visitLabel(l6);
			//mv.visitLineNumber(10, l6);
			mv.visitLdcInsn(new Integer(1255477124));
			mv.visitVarInsn(ISTORE, localVarAddressIncrease+5);
			Label l7 = new Label();
			mv.visitLabel(l7);
			//mv.visitLineNumber(11, l7);
			mv.visitLdcInsn(new Integer(-2000435098));
			mv.visitVarInsn(ISTORE, localVarAddressIncrease+7);
			
			//3. output
			
			Label l8 = new Label();
			mv.visitLabel(l8);
			//mv.visitLineNumber(12, l8);
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitVarInsn(ILOAD, localVarAddressIncrease+5);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
					"(I)V");
			Label l9 = new Label();
			mv.visitLabel(l9);
			//mv.visitLineNumber(13, l9);
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitVarInsn(ILOAD, localVarAddressIncrease+6);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
					"(I)V");
			Label l10 = new Label();
			mv.visitLabel(l10);
			//mv.visitLineNumber(14, l10);
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitVarInsn(ILOAD, localVarAddressIncrease+7);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
					"(I)V");
			
			//4. wrapup
			
			Label l11 = new Label();
			mv.visitLabel(l11);
			//mv.visitLineNumber(15, l11);
			mv.visitMethodInsn(INVOKESTATIC, "TimerHelper", "time", "()J");
			mv.visitVarInsn(LSTORE, 3);
			Label l12 = new Label();
			mv.visitLabel(l12);
			//mv.visitLineNumber(16, l12);
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
					"Ljava/io/PrintStream;");
			mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
			mv.visitInsn(DUP);
			mv
					.visitLdcInsn("Method testWide_ISTORE_3Vars_4Iters_falseArrayExplicit_5ArraySize() : ");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder",
					"<init>", "(Ljava/lang/String;)V");
			mv.visitVarInsn(LLOAD, 3);
			mv.visitVarInsn(LLOAD, 1);
			mv.visitInsn(LSUB);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder",
					"append", "(J)Ljava/lang/StringBuilder;");
			mv.visitLdcInsn(" ns = ");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder",
					"append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitVarInsn(LLOAD, 3);
			mv.visitVarInsn(LLOAD, 1);
			mv.visitInsn(LSUB);
			mv.visitLdcInsn(new Long(1000000L));
			mv.visitInsn(LDIV);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder",
					"append", "(J)Ljava/lang/StringBuilder;");
			mv.visitLdcInsn(" ms");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder",
					"append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder",
					"toString", "()Ljava/lang/String;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println",
					"(Ljava/lang/String;)V");
			Label l13 = new Label();
			mv.visitLabel(l13);
			//mv.visitLineNumber(17, l13);
			mv.visitVarInsn(LLOAD, 3);
			mv.visitVarInsn(LLOAD, 1);
			mv.visitInsn(LSUB);
			mv.visitInsn(LRETURN);
			Label l14 = new Label();
			mv.visitLabel(l14);
			mv.visitLocalVariable("this", "LWideVsNormalExample;", null, l0,
					l14, 0);
			mv.visitLocalVariable("start", "J", null, l1, l14, 1);
			mv.visitLocalVariable("stop", "J", null, l12, l14, 3);
			mv.visitLocalVariable("var0", "I", null, l2, l14, localVarAddressIncrease+5);
			mv.visitLocalVariable("var1", "I", null, l3, l14, localVarAddressIncrease+6);
			mv.visitLocalVariable("var2", "I", null, l4, l14, localVarAddressIncrease+7);
			mv.visitMaxs(6, 8);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}
	public static void main(String[] args) throws IOException, Exception {
//		WideVsNormalExampleModifiedDump wvnemd = new WideVsNormalExampleModifiedDump();
		FileOutputStream fos;
		try {
			fos = new FileOutputStream("WideVsNormalExample.class");
			fos.write(WideVsNormalExampleModifiedDump.dump());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	{
		localVarAddressIncrease = 500;
	}
}
