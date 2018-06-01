package de.uka.ipd.sdq.ByCounter.instrumentation;

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARRAYLENGTH;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.IF_ICMPLT;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.LALOAD;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.LocalVariablesSorter;

import de.uka.ipd.sdq.ByCounter.execution.ProtocolCountStructure;
import de.uka.ipd.sdq.ByCounter.parsing.MethodPreInstrumentationParser;
import de.uka.ipd.sdq.ByCounter.utils.ASMOpcodesMapper;
import de.uka.ipd.sdq.ByCounter.utils.JavaType;
import de.uka.ipd.sdq.ByCounter.utils.JavaTypeEnum;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * This is the visitor for the class declaration. It watches each 
 * method declaration and selects the methods which have to be 
 * instrumented as specified in the instrumentation parameters.
 * For those methods, the MethodCountMethodAdapter is activated.
 * @see MethodCountMethodAdapter
 *
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
public final class MethodCountClassAdapter extends ClassAdapter {

	/**
	 * Class to save the information passed on visitMethod calls.
	 *
	 */
	private class MethodVisitInformation {
		public int access;
		public String name;
		public String desc;
		public String signature;
		public String[] exceptions;
		public String newDesc;
	}
		
	/**
	 * Token in front of every line of the result log that does not contain 
	 * an opcode count. Useful for easier parsing.
	 */
	private final static String LOG_COMMENT_TOKEN = "// ";

	/**
	 * This is the postfix to the name of instrumented methods for the versions,
	 * 
	 */
	public static final String METHOD_RENAMING_POSTFIX = "_" + UUID.randomUUID().toString().replace("-", "_"); 

	public static final String DIRECT_LOG_WRITE_SIGNATURE = "(Lde/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure;)V";

	/**
	 * This is Java code for direct writing to a log that 
	 * serves as a template for the bytecode that is inserted 
	 * by the call to <code>insertDirectWritingToLogCalls()</code>.
	 * 
	 * In order to translate this into asm code, use the asmifier plugin and 
	 * copy the instructions starting with <code>mv.visitCode();</code> and 
	 * ending with <code>mv.visitEnd();</code> to replace the existing asm code 
	 * in {@link #insertLogWritingMethod()}.
	 * Then replace the occurrences of <code>"instrumentationParameters.getResultLogFileName()"</code>
	 * (i.e. including the '"') with <code>instrumentationParameters.getResultLogFileName()</code>
	 * (i.e. removing the '"').
	 * Then similarly replace the occurrences of <code>"LOG_COMMENT_TOKEN +</code>
	 * with <code>LOG_COMMENT_TOKEN + "</code>.
	 *  
	 * Finally, the {@link String} constant {@link #DIRECT_LOG_WRITE_SIGNATURE}
	 * needs to be adapted.
	 */
	@SuppressWarnings("unused")
	private static void directWritingToLogTemplate(
			ProtocolCountStructure result) {
	
		String lineSep = System.getProperty("line.separator");
		FileOutputStream out = null;
		try {
			File resultFile = new File(constructResultLogFileName(
							"instrumentationParameters.getResultLogFileName()",
							result.qualifyingMethodName, result.executionStart));
			File directory = resultFile.getParentFile();
			if(directory == null) {
				directory = new File(".");
			}
			if(!directory.exists() && !directory.mkdirs()) {
				System.out.println("Could not create directory for log files.");
			}
			out = new FileOutputStream(resultFile);
			System.out.println("Writing counting results log file to "
					+ resultFile.getAbsolutePath());
			out.write(("LOG_COMMENT_TOKEN + Timestamp: " + result.executionStart + lineSep
				+ "LOG_COMMENT_TOKEN + RequestID: " + result.requestID + lineSep
				+ "LOG_COMMENT_TOKEN + OwnID: " + result.ownID + lineSep
				+ "LOG_COMMENT_TOKEN + CallerID: " + result.callerID + lineSep
				+ "LOG_COMMENT_TOKEN + Qualifying methodname: " + result.qualifyingMethodName + lineSep
				+ "LOG_COMMENT_TOKEN + Opcode counts:" + lineSep).getBytes());
	
			if(result.opcodeCounts != null) {
				for(int i = 0; i < result.opcodeCounts.length; i++) {
					out.write((ASMOpcodesMapper.getInstance().getOpcodeString(i)
						+ "; " + result.opcodeCounts[i] + lineSep).getBytes());
				}
			}
			out.write(("LOG_COMMENT_TOKEN + Method counts:" + lineSep).getBytes());
			if(result.methodCallCounts != null) {
				for(int i = 0; i < result.methodCallCounts.length; i++) {
					out.write(("LOG_COMMENT_TOKEN + " + result.calledMethods[i]
						+ "; " + result.methodCallCounts[i] + lineSep).getBytes());
				}
			}
			if(result.newArrayCounts != null) {
				out.write(("LOG_COMMENT_TOKEN + Array constructions:" + lineSep).getBytes());
				for(int i = 0; i < result.newArrayCounts.length; i++) {
					// TODO: improve output readability
					out.write(("LOG_COMMENT_TOKEN + "
							+ "count: " + result.newArrayCounts[i]
							+ lineSep).getBytes());
				}
			}
//			if (out != null){
				out.close();
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The set of {@link ICharacterisationHook}s that are called
	 * before, when and after methods are visited.
	 */
	private HashSet<ICharacterisationHook> characterisationHooks;

	/**
	 * 
	 * The name of the class that is visited.
	 * This is set in the visit method.
	 */
	private String className;
	
	/**
	 * The parameters for the instrumentation including which methods to instrument.
	 */
	private InstrumentationParameters instrumentationParameters;
	
	/**
	 * The internal results of the instrumentation.
	 */
	private InstrumentationState instrumentationState;
	
	/**
	 * Methods that need to be copied for versions with a requestID.
	 */
	private List<MethodVisitInformation> methodsToDuplicate;
	
	// have a static log for the class to use
	private Logger log = 
		Logger.getLogger(this.getClass().getCanonicalName());

	/**
	 * The {@link MethodCountMethodAdapter} used to instrument the selected methods.
	 */
	private MethodCountMethodAdapter methodCountMethodAdapter;
	
	/**
	 * An array of boolean where an entry is true, if the method with the same 
	 * index in the method list of instrumParams was instrumented. 
	 * Used for error checking by {@link Instrumenter}.
	 */
	protected boolean[] methodInstrumentationStatus;

	/**
	 * Name of the super class (bytecode notation).
	 */
	private String superNameBC;

	/**
	 * Class name (bytecode notation)
	 */
	private String classNameBC;

	/**
	 * Create a new MethodCountAdapter.
	 * @param visitor The preceding visitor in the chain.
	 * @param parameters {@link InstrumentationParameters}
	 * @param state {@link InstrumentationState}
	 */
	public MethodCountClassAdapter(
			ClassVisitor visitor, 
			InstrumentationParameters parameters,
			InstrumentationState state
			) {
		super(visitor);
		this.instrumentationParameters = parameters;
		this.instrumentationState = state;
		this.characterisationHooks = new HashSet<ICharacterisationHook>();		
		this.methodsToDuplicate = new LinkedList<MethodVisitInformation>();
	}

	/**
	 * Constructs the filename for the result log by replacing the 
	 * template strings.
	 * @param resultLogFileNameTemplate A prefix.
	 * @param qualifyingMethodName The qualifying name of the calling method.
	 * @param timestamp A timestamp of the execution time.
	 * @return The resulting file name string.
	 * @see InstrumentationParameters#enableResultLogWriter(String)
	 */
	public static String constructResultLogFileName(
			String resultLogFileNameTemplate,
			String qualifyingMethodName,
			long timestamp
			) {
		String result = resultLogFileNameTemplate 
						+ timestamp + "_" + qualifyingMethodName + ".log";
		return result;
	}

	/**
	 * Register a {@link ICharacterisationHook}.
	 * @see ICharacterisationHook
	 * @param hook
	 */
	public void registerCharacterisationHook(ICharacterisationHook hook) {
		this.characterisationHooks.add(hook);
	}

	/**
	 * Sets up the {@link #methodInstrumentationStatus} with the correct size and 
	 * all entries with the newStatus value.
	 * @param newStatus The status to set.
	 */
	protected void resetMethodStatusTo(boolean newStatus) {
		this.methodInstrumentationStatus = new boolean[this.instrumentationState.getMethodsToInstrumentCalculated().size()];
		for(int i = 0; i < this.methodInstrumentationStatus.length; i++) {
			this.methodInstrumentationStatus[i] = newStatus;
		}
	}

	/**
	 * Visits the header of the class and grabs the classname.
	 * This is being called from other class visitors in the chain for 
	 * classes that get passed to the Instrumenter.
	 * The classname attribute is important as it serves for describing 
	 * the output in logs or files in order to associate counts to the 
	 * correct classes/methods.
	 * {@inheritDoc}
	 * @see Instrumenter
	 */
	@Override 
	public void visit(
			int version, 
			int access, 
			String name, 
			String signature, 
			String supername, 
			String[] interfaces) {
		super.visit(version, access, name, signature, supername, interfaces);
		this.classNameBC = name;
		this.className = name.replace('/', '.');
		this.superNameBC = supername;
	}
	

	/**
	 * Overridden to insert a result log writing method.
	 * @see org.objectweb.asm.ClassAdapter#visitEnd()
	 */
	@Override
	public void visitEnd() {

		if(this.instrumentationParameters.getTraceAndIdentifyRequests()) {
			// insert the new methods that create requestIDs
			for(MethodVisitInformation mInfo : this.methodsToDuplicate) {
				this.insertNewMethod(mInfo);
			}
		}

		// Insert the log writing method into the class.
		if(this.instrumentationParameters.getUseResultLogWriter()) {
			this.insertLogWritingMethod();
		}		
				
		cv.visitEnd();
	}

	/**
	 * Insert a method as specified by mInfo. The method creates a requestID and 
	 * then calls the renamed method (mInfo.newName) using that ID.
	 * @param mInfo
	 */
	private void insertNewMethod(MethodVisitInformation mInfo) {
		MethodVisitor mv = cv.visitMethod(mInfo.access, mInfo.name, 
				mInfo.desc, mInfo.signature, mInfo.exceptions);

		mv.visitCode();
		boolean isStaticMethod = (mInfo.access & Opcodes.ACC_STATIC) > 0; 
		
		// load the parameters
		JavaType[] types = MethodDescriptor.getParametersTypesFromDesc(mInfo.desc);
		int varIndex;
		if(isStaticMethod) {
			varIndex = 0;
		} else {
			varIndex = 1;
			if(mInfo.name.equalsIgnoreCase("<init>")) {
				System.out.println("<init> is here");
				mv.visitVarInsn(ALOAD, 0);
				mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
			}
			mv.visitVarInsn(Opcodes.ALOAD, 0); // this
		}
		int varIndexInc = 0;
		int opcode = 0;
		for(JavaType type : types) {
			JavaTypeEnum t = type.getType();
			switch(t) {
			case Array:
				opcode = Opcodes.ALOAD;
				varIndexInc = 1;
				break;
			case Boolean:
				opcode = Opcodes.ILOAD;
				varIndexInc = 1;
				break;
			case Byte:
				opcode = Opcodes.ILOAD;
				varIndexInc = 1;
				break;
			case Char:
				opcode = Opcodes.ILOAD;
				varIndexInc = 1;
				break;
			case Double:
				opcode = Opcodes.DLOAD;
				varIndexInc = 2;
				break;
			case Float:
				opcode = Opcodes.FLOAD;
				varIndexInc = 1;
				break;
			case Int:
				opcode = Opcodes.ILOAD;
				varIndexInc = 1;
				break;
			case Long:
				opcode = Opcodes.LLOAD;
				varIndexInc = 2;
				break;
			case Object:
				opcode = Opcodes.ALOAD;
				varIndexInc = 1;
				break;
			case Short:
				opcode = Opcodes.ILOAD;
				varIndexInc = 1;
				break;
			case Void:
				break;
			}
			mv.visitVarInsn(opcode, varIndex);
			varIndex += varIndexInc;
		}

		// call modified method 
		// create a UUID as the final parameter
		mv.visitMethodInsn(INVOKESTATIC, "java/util/UUID", "randomUUID", "()Ljava/util/UUID;");
		mv.visitInsn(Opcodes.ACONST_NULL); // no "ownId" exists
		int callOpcode;
		if(isStaticMethod) {
			callOpcode = Opcodes.INVOKESTATIC;
		} else {
			callOpcode = Opcodes.INVOKEVIRTUAL;
		}
		mv.visitMethodInsn(callOpcode, this.className.replace('.', '/'), generateInstrumentedMethodName(mInfo.name), mInfo.newDesc);
		
		JavaType retType = MethodDescriptor.getReturnTypeFromDesc(mInfo.desc);
		switch(retType.getType()) {
		case Short:
		case Int:
		case Boolean:
		case Byte:
		case Char:
			mv.visitInsn(Opcodes.IRETURN);
			break;
		case Long:
			mv.visitInsn(Opcodes.LRETURN);
			break;
		case Float:
			mv.visitInsn(Opcodes.FRETURN);
			break;
		case Double:
			mv.visitInsn(Opcodes.DRETURN);
			break;
		case Array:
		case Object:
			mv.visitInsn(Opcodes.ARETURN);
			break;
		default:
			mv.visitInsn(Opcodes.RETURN);
			break;
		}
		
		mv.visitMaxs(99, 99);	// necessary, but are the values ignored because COMPUTE_MAXS is enabled
		mv.visitEnd();
	}

	/**
	 * This creates a new method that is able to write counting results to  a 
	 * log file.
	 */
	private void insertLogWritingMethod() {
		// the methods signature
		String strDescr = DIRECT_LOG_WRITE_SIGNATURE;
		
		// make the method private + static and use an uncommon name to avoid collisions with an already existing method
		MethodVisitor mv = 
			this.cv.visitMethod(ACC_PRIVATE + ACC_STATIC,
					"___directWritingToLog___", 
					strDescr, null, null);
		
		if (mv != null) {
			// here follows the code as converted from directWritingToLogTemplate:
			mv.visitCode();
			Label l0 = new Label();
			Label l1 = new Label();
			Label l2 = new Label();
			mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitLineNumber(115, l3);
			mv.visitLdcInsn("line.separator");
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;");
			mv.visitVarInsn(ASTORE, 1);
			Label l4 = new Label();
			mv.visitLabel(l4);
			mv.visitLineNumber(116, l4);
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, 2);
			mv.visitLabel(l0);
			mv.visitLineNumber(118, l0);
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			Label l5 = new Label();
			mv.visitLabel(l5);
			mv.visitLineNumber(119, l5);
			mv.visitLdcInsn(instrumentationParameters.getResultLogFileName());
			Label l6 = new Label();
			mv.visitLabel(l6);
			mv.visitLineNumber(120, l6);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "de/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure", "qualifyingMethodName", "Ljava/lang/String;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "de/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure", "executionStart", "J");
			Label l7 = new Label();
			mv.visitLabel(l7);
			mv.visitLineNumber(118, l7);
			mv.visitMethodInsn(INVOKESTATIC, "de/uka/ipd/sdq/ByCounter/instrumentation/MethodCountClassAdapter", "constructResultLogFileName", "(Ljava/lang/String;Ljava/lang/String;J)Ljava/lang/String;");
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V");
			mv.visitVarInsn(ASTORE, 3);
			Label l8 = new Label();
			mv.visitLabel(l8);
			mv.visitLineNumber(121, l8);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/File", "getParentFile", "()Ljava/io/File;");
			mv.visitVarInsn(ASTORE, 4);
			Label l9 = new Label();
			mv.visitLabel(l9);
			mv.visitLineNumber(122, l9);
			mv.visitVarInsn(ALOAD, 4);
			Label l10 = new Label();
			mv.visitJumpInsn(IFNONNULL, l10);
			Label l11 = new Label();
			mv.visitLabel(l11);
			mv.visitLineNumber(123, l11);
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitLdcInsn(".");
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V");
			mv.visitVarInsn(ASTORE, 4);
			mv.visitLabel(l10);
			mv.visitLineNumber(125, l10);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/File", "exists", "()Z");
			Label l12 = new Label();
			mv.visitJumpInsn(IFNE, l12);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/File", "mkdirs", "()Z");
			mv.visitJumpInsn(IFNE, l12);
			Label l13 = new Label();
			mv.visitLabel(l13);
			mv.visitLineNumber(126, l13);
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitLdcInsn("Could not create directory for log files.");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
			mv.visitLabel(l12);
			mv.visitLineNumber(128, l12);
			mv.visitTypeInsn(NEW, "java/io/FileOutputStream");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/FileOutputStream", "<init>", "(Ljava/io/File;)V");
			mv.visitVarInsn(ASTORE, 2);
			Label l14 = new Label();
			mv.visitLabel(l14);
			mv.visitLineNumber(129, l14);
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
			mv.visitInsn(DUP);
			mv.visitLdcInsn("Writing counting results log file to ");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
			Label l15 = new Label();
			mv.visitLabel(l15);
			mv.visitLineNumber(130, l15);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/File", "getAbsolutePath", "()Ljava/lang/String;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
			Label l16 = new Label();
			mv.visitLabel(l16);
			mv.visitLineNumber(129, l16);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
			Label l17 = new Label();
			mv.visitLabel(l17);
			mv.visitLineNumber(131, l17);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
			mv.visitInsn(DUP);
			mv.visitLdcInsn(LOG_COMMENT_TOKEN + " Timestamp: ");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "de/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure", "executionStart", "J");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			Label l18 = new Label();
			mv.visitLabel(l18);
			mv.visitLineNumber(132, l18);
			mv.visitLdcInsn(LOG_COMMENT_TOKEN + " RequestID: ");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "de/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure", "requestID", "Ljava/util/UUID;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			Label l19 = new Label();
			mv.visitLabel(l19);
			mv.visitLineNumber(133, l19);
			mv.visitLdcInsn(LOG_COMMENT_TOKEN + " OwnID: ");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "de/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure", "ownID", "Ljava/util/UUID;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			Label l20 = new Label();
			mv.visitLabel(l20);
			mv.visitLineNumber(134, l20);
			mv.visitLdcInsn(LOG_COMMENT_TOKEN + " CallerID: ");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "de/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure", "callerID", "Ljava/util/UUID;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			Label l21 = new Label();
			mv.visitLabel(l21);
			mv.visitLineNumber(135, l21);
			mv.visitLdcInsn(LOG_COMMENT_TOKEN + " Qualifying methodname: ");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "de/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure", "qualifyingMethodName", "Ljava/lang/String;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			Label l22 = new Label();
			mv.visitLabel(l22);
			mv.visitLineNumber(136, l22);
			mv.visitLdcInsn(LOG_COMMENT_TOKEN + " Opcode counts:");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B");
			Label l23 = new Label();
			mv.visitLabel(l23);
			mv.visitLineNumber(131, l23);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/FileOutputStream", "write", "([B)V");
			Label l24 = new Label();
			mv.visitLabel(l24);
			mv.visitLineNumber(138, l24);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "de/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure", "opcodeCounts", "[J");
			Label l25 = new Label();
			mv.visitJumpInsn(IFNULL, l25);
			Label l26 = new Label();
			mv.visitLabel(l26);
			mv.visitLineNumber(139, l26);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 5);
			Label l27 = new Label();
			mv.visitLabel(l27);
			Label l28 = new Label();
			mv.visitJumpInsn(GOTO, l28);
			Label l29 = new Label();
			mv.visitLabel(l29);
			mv.visitLineNumber(140, l29);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESTATIC, "de/uka/ipd/sdq/ByCounter/utils/ASMOpcodesMapper", "getInstance", "()Lde/uka/ipd/sdq/ByCounter/utils/ASMOpcodesMapper;");
			mv.visitVarInsn(ILOAD, 5);
			mv.visitMethodInsn(INVOKEVIRTUAL, "de/uka/ipd/sdq/ByCounter/utils/ASMOpcodesMapper", "getOpcodeString", "(I)Ljava/lang/String;");
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
			Label l30 = new Label();
			mv.visitLabel(l30);
			mv.visitLineNumber(141, l30);
			mv.visitLdcInsn("; ");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "de/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure", "opcodeCounts", "[J");
			mv.visitVarInsn(ILOAD, 5);
			mv.visitInsn(LALOAD);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B");
			Label l31 = new Label();
			mv.visitLabel(l31);
			mv.visitLineNumber(140, l31);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/FileOutputStream", "write", "([B)V");
			Label l32 = new Label();
			mv.visitLabel(l32);
			mv.visitLineNumber(139, l32);
			mv.visitIincInsn(5, 1);
			mv.visitLabel(l28);
			mv.visitVarInsn(ILOAD, 5);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "de/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure", "opcodeCounts", "[J");
			mv.visitInsn(ARRAYLENGTH);
			mv.visitJumpInsn(IF_ICMPLT, l29);
			mv.visitLabel(l25);
			mv.visitLineNumber(144, l25);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
			mv.visitInsn(DUP);
			mv.visitLdcInsn(LOG_COMMENT_TOKEN + " Method counts:");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/FileOutputStream", "write", "([B)V");
			Label l33 = new Label();
			mv.visitLabel(l33);
			mv.visitLineNumber(145, l33);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "de/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure", "methodCallCounts", "[J");
			Label l34 = new Label();
			mv.visitJumpInsn(IFNULL, l34);
			Label l35 = new Label();
			mv.visitLabel(l35);
			mv.visitLineNumber(146, l35);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 5);
			Label l36 = new Label();
			mv.visitLabel(l36);
			Label l37 = new Label();
			mv.visitJumpInsn(GOTO, l37);
			Label l38 = new Label();
			mv.visitLabel(l38);
			mv.visitLineNumber(147, l38);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
			mv.visitInsn(DUP);
			mv.visitLdcInsn(LOG_COMMENT_TOKEN + " ");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "de/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure", "calledMethods", "[Ljava/lang/String;");
			mv.visitVarInsn(ILOAD, 5);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			Label l39 = new Label();
			mv.visitLabel(l39);
			mv.visitLineNumber(148, l39);
			mv.visitLdcInsn("; ");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "de/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure", "methodCallCounts", "[J");
			mv.visitVarInsn(ILOAD, 5);
			mv.visitInsn(LALOAD);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B");
			Label l40 = new Label();
			mv.visitLabel(l40);
			mv.visitLineNumber(147, l40);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/FileOutputStream", "write", "([B)V");
			Label l41 = new Label();
			mv.visitLabel(l41);
			mv.visitLineNumber(146, l41);
			mv.visitIincInsn(5, 1);
			mv.visitLabel(l37);
			mv.visitVarInsn(ILOAD, 5);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "de/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure", "methodCallCounts", "[J");
			mv.visitInsn(ARRAYLENGTH);
			mv.visitJumpInsn(IF_ICMPLT, l38);
			mv.visitLabel(l34);
			mv.visitLineNumber(151, l34);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "de/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure", "newArrayCounts", "[J");
			Label l42 = new Label();
			mv.visitJumpInsn(IFNULL, l42);
			Label l43 = new Label();
			mv.visitLabel(l43);
			mv.visitLineNumber(152, l43);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
			mv.visitInsn(DUP);
			mv.visitLdcInsn(LOG_COMMENT_TOKEN + " Array constructions:");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/FileOutputStream", "write", "([B)V");
			Label l44 = new Label();
			mv.visitLabel(l44);
			mv.visitLineNumber(153, l44);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, 5);
			Label l45 = new Label();
			mv.visitLabel(l45);
			Label l46 = new Label();
			mv.visitJumpInsn(GOTO, l46);
			Label l47 = new Label();
			mv.visitLabel(l47);
			mv.visitLineNumber(155, l47);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
			mv.visitInsn(DUP);
			mv.visitLdcInsn(LOG_COMMENT_TOKEN + " count: ");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
			Label l48 = new Label();
			mv.visitLabel(l48);
			mv.visitLineNumber(156, l48);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "de/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure", "newArrayCounts", "[J");
			mv.visitVarInsn(ILOAD, 5);
			mv.visitInsn(LALOAD);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;");
			Label l49 = new Label();
			mv.visitLabel(l49);
			mv.visitLineNumber(157, l49);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "getBytes", "()[B");
			Label l50 = new Label();
			mv.visitLabel(l50);
			mv.visitLineNumber(155, l50);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/FileOutputStream", "write", "([B)V");
			Label l51 = new Label();
			mv.visitLabel(l51);
			mv.visitLineNumber(153, l51);
			mv.visitIincInsn(5, 1);
			mv.visitLabel(l46);
			mv.visitVarInsn(ILOAD, 5);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "de/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure", "newArrayCounts", "[J");
			mv.visitInsn(ARRAYLENGTH);
			mv.visitJumpInsn(IF_ICMPLT, l47);
			mv.visitLabel(l42);
			mv.visitLineNumber(161, l42);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/FileOutputStream", "close", "()V");
			mv.visitLabel(l1);
			Label l52 = new Label();
			mv.visitJumpInsn(GOTO, l52);
			mv.visitLabel(l2);
			mv.visitLineNumber(163, l2);
			mv.visitVarInsn(ASTORE, 3);
			Label l53 = new Label();
			mv.visitLabel(l53);
			mv.visitLineNumber(164, l53);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Exception", "printStackTrace", "()V");
			mv.visitLabel(l52);
			mv.visitLineNumber(166, l52);
			mv.visitInsn(RETURN);
			Label l54 = new Label();
			mv.visitLabel(l54);
			mv.visitLocalVariable("result", "Lde/uka/ipd/sdq/ByCounter/execution/ProtocolCountStructure;", null, l3, l54, 0);
			mv.visitLocalVariable("lineSep", "Ljava/lang/String;", null, l4, l54, 1);
			mv.visitLocalVariable("out", "Ljava/io/FileOutputStream;", null, l0, l54, 2);
			mv.visitLocalVariable("resultFile", "Ljava/io/File;", null, l8, l2, 3);
			mv.visitLocalVariable("directory", "Ljava/io/File;", null, l9, l2, 4);
			mv.visitLocalVariable("i", "I", null, l27, l25, 5);
			mv.visitLocalVariable("i", "I", null, l36, l34, 5);
			mv.visitLocalVariable("i", "I", null, l45, l42, 5);
			mv.visitLocalVariable("e", "Ljava/lang/Exception;", null, l53, l52, 3);
			mv.visitMaxs(6, 6);
			mv.visitEnd();
		} else {
			log.severe("Could not visit method: " + "directWritingToLog");
		}
	}

	/**
	 * This is called when a method declaration happens in the class.
	 */
	@Override
	public MethodVisitor visitMethod(
			int access, 
			String name, 
			String desc, 
			String signature, 
			String[] exceptions) {
		MethodVisitor nextVisitor = null;
		MethodVisitor mv = null;
		
		
		boolean isNative = false;
		if((access & Opcodes.ACC_NATIVE) != 0) {
			isNative = true;
			log.info("###### NATIVE METHOD #######");
		}
		
		// to correctly recognise constructors, replace the <init> name
		// with their Java name, e.g. the classname.
		String methodName;
		boolean isConstructor = name.equals("<init>");
		if(isConstructor) {		// we have a constructor
			int startSplitIndex = this.className.lastIndexOf('.')+1;
			methodName = this.className.substring(startSplitIndex);
		} else {	// normal method; use the given name
			methodName =  name;
		}
		

		int methodIndex = 
			MethodDescriptor.findMethodInList(
					this.instrumentationState.getMethodsToInstrumentCalculated(),
					this.className,
					methodName,
					desc);

		// method header
		if(methodIndex >= 0 && !isNative
				&& this.instrumentationParameters.getTraceAndIdentifyRequests()
				&& !isConstructor) {
			// we rewrite the header if we need to use  request IDs 
			
			// save the visitMethodInformation for use in the methods copy
			MethodVisitInformation mInfo = new MethodVisitInformation();
			mInfo.access = access;
			mInfo.name = name;
			mInfo.desc = desc;
			mInfo.signature = signature;
			mInfo.exceptions = exceptions;
			
			// now rename the method and change the descriptor
			mInfo.newDesc = getDescWithRequestID(desc);
			mv = this.cv.visitMethod(access, 
					generateInstrumentedMethodName(mInfo.name),
					mInfo.newDesc, signature, exceptions);

			this.methodsToDuplicate.add(mInfo);
		} else {
			// use the original method header
			mv = this.cv.visitMethod(access, name, desc, signature, exceptions);
		}
		boolean instrumentEverything = 
				(this.instrumentationParameters.getInstrumentationScopeOverrideClassLevel()
				== InstrumentationScopeModeEnum.InstrumentEverything);
		
		if (mv == null) {
			throw new RuntimeException(new IllegalStateException(
					"The method visitor found in the chain was null."));
		} else	if ((instrumentEverything)
				|| methodIndex >= 0) {
			if(!isNative) {
				MethodDescriptor currentMethod;
				if(methodIndex < 0) {
					// we have no MethodDescriptor yet, construct one
					currentMethod = 
						MethodDescriptor._constructMethodDescriptorFromASM(this.classNameBC, name, desc);
				} else {
					currentMethod = 
						this.instrumentationState.getMethodsToInstrumentCalculated().get(methodIndex);
				}
				String qualifyingMethodName = this.className + "." + methodName;
				
				// TODO: Argumentieren, warum beibehalten der alten Methode nicht
				// geht (der body der Methode muss angepasst werden, um 
				// Methodenaufrufe
				// auf die neuen Methodensignaturen mit requestIDs umzubiegen).
				
				// this is the method to instrument
				this.methodCountMethodAdapter = new MethodCountMethodAdapter(
						mv, 
						access,
						this.superNameBC,
						this.classNameBC,
						qualifyingMethodName, 
						desc,
						instrumentationParameters,
						instrumentationState,
						currentMethod);
				mv = this.methodCountMethodAdapter;
				
				// call the methodStart hooks
				for(ICharacterisationHook hooks : characterisationHooks) {
					hooks.methodStartHook(
							methodCountMethodAdapter, 
							instrumentationParameters,
							access, name, 
							desc, signature, exceptions);
				}
				// register hooks for method adapter
			    for(ICharacterisationHook hook : characterisationHooks) {
					methodCountMethodAdapter.registerCharacterisationHook(hook);
				}
			    nextVisitor = mv;

			    // The LocalVariablesSorter is only required when not using high registers
			    // or when introducing request IDs.
			    if(this.instrumentationParameters.getTraceAndIdentifyRequests() 
			    		|| !instrumentationParameters.getUseHighRegistersForCounting()) {
			    	nextVisitor = new LocalVariablesSorter(access, desc, mv);
			    	((MethodCountMethodAdapter)mv).setLVS((LocalVariablesSorter)nextVisitor);
			    }
			    
				nextVisitor = new MethodPreInstrumentationParser(
						nextVisitor, access, this.className, name, desc, 
						methodCountMethodAdapter, 
						instrumentationParameters,
						instrumentationState, 
						currentMethod);
				
				this.instrumentationState.getSuccessFullyInstrumentedMethods().add(currentMethod);
				// this method was instrumented successfully //TODO add errors!
				if(!instrumentEverything && methodIndex >= 0) {
					this.methodInstrumentationStatus[methodIndex] = true;
				}
			} else {
				// we have a native method; mark as successfully instrumented
				// in order to avoid errors in recursive instrumentation where
				// native methods may be selected
				if(!instrumentEverything && methodIndex >= 0) {
					this.methodInstrumentationStatus[methodIndex] = true;
				}
			}
		} else {
			// this is not a method that has to be instrumented: use default visitor
			nextVisitor = new MethodAdapter(mv);
		}

		return nextVisitor;
	}

	/**
	 * @param oldName Name of the original method.
	 * @return The name of the method with the added requestID argument.
	 */
	protected static String generateInstrumentedMethodName(String oldName) {
		// for constructors, we need to replace the braces
		String name = oldName.replace('<', '_');
		name = name.replace('>', '_');
		
		return name + MethodCountClassAdapter.METHOD_RENAMING_POSTFIX;
	}

	/**
	 * @param desc The descriptor to modify.
	 * @return The descriptor with added parameters for the UUIDs.
	 */
	protected static String getDescWithRequestID(String desc) {
		int closingBraceInd = desc.lastIndexOf(')');
		return desc.substring(0, closingBraceInd) 
			+ "Ljava/util/UUID;Ljava/util/UUID;" + desc.substring(closingBraceInd);
	}
}
