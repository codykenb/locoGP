package de.uka.ipd.sdq.ByCounter.utils;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Logger;

import org.objectweb.asm.Type;

import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;

/**
 * Unambiguous description for a method in bytecode compatible format. Such a signature can be constructed through the
 * constructor <code>MethodDescriptor(..)</code> and for constructor descriptions through the method
 * <code>forConstructor(..)</code>. The descriptor returned by getDescriptor() does not contain a reference to the
 * containing class anymore.
 * 
 * @see #MethodDescriptor(String, String)
 * @author Michael Kuperberg
 * @author Martin Krogmann
 * @author Florian Schreier
 * @since 0.1
 * @version 1.3
 */
public final class MethodDescriptor implements Comparable<MethodDescriptor>, Serializable {

	// have a static log for the class to use
	private static transient Logger log = Logger.getLogger(MethodDescriptor.class.getCanonicalName());

	/**
	 * Version for {@link Serializable}; not used at this point.
	 */
	private static final long serialVersionUID = 1L;
	/* Java bytecode notation for a parameter/return type. */
	private static final char TYPE_ARRAY = '[';
	/* Java bytecode notation for a parameter/return type. */
	private static final char TYPE_BOOLEAN = 'Z';
	/* Java bytecode notation for a parameter/return type. */
	private static final char TYPE_BYTE = 'B';
	/* Java bytecode notation for a parameter/return type. */
	private static final char TYPE_CHAR = 'C';
	/* Java bytecode notation for a parameter/return type. */
	private static final char TYPE_DOUBLE = 'D';
	/* Java bytecode notation for a parameter/return type. */
	private static final char TYPE_FLOAT = 'F';
	/* Java bytecode notation for a parameter/return type. */
	private static final char TYPE_INT = 'I';
	/* Java bytecode notation for a parameter/return type. */
	private static final char TYPE_LONG = 'J';
	/* Java bytecode notation for a parameter/return type. */
	private static final char TYPE_OBJECT = 'L';
	/* Java bytecode notation for a parameter/return type. */
	private static final char TYPE_SHORT = 'S';
	/* Java bytecode notation for a parameter/return type. */
	private static final char TYPE_VOID = 'V';

	/**
	 * Construct a {@link MethodDescriptor} instance from the details known by ASM.<br />
	 * <b>Build for internal use only.</b>
	 * 
	 * @param owner
	 * @param name
	 * @param desc
	 * @return The MethodDescriptor for the specified method.
	 */
	public static MethodDescriptor _constructMethodDescriptorFromASM(String owner, String name, String desc) {
		// Check input
		if (owner == null || owner.length() <= 0) {
			throw new RuntimeException("Owner must not be null or empty. Check asm output.");
		}
		if (name == null || name.length() <= 0) {
			throw new RuntimeException("Name must not be null or empty. Check asm output.");
		}
		if (desc == null || desc.length() <= 0) {
			throw new RuntimeException("Desc must not be null or empty. Check asm output.");
		}

		// parse class and package
		String fqcn = owner.replace('/', '.'); // fully qualified class name
		int packageClassSeperationPlace = fqcn.lastIndexOf('.');
		String packageName = "";
		String className;
		if (packageClassSeperationPlace >= 0) {
			packageName = fqcn.substring(0, packageClassSeperationPlace);
			className = fqcn.substring(packageClassSeperationPlace + 1);
		} else {
			className = fqcn;
		}

		// construct the MethodDescriptor
		MethodDescriptor result = new MethodDescriptor();
		boolean isConstructor = name.equalsIgnoreCase("<init>");

		result.construct(packageName, className, name, desc, isConstructor);

		return result;
	}

	/**
	 * Splits the canonical classname to the package name and the simple class name.
	 * 
	 * @param className
	 *            Canonical class name.
	 * @return An array of two string. The first element is the package name. The second element is the simple class
	 *         name.
	 */
	public static String[] canonicalClassNameToPackageAndSimpleName(String className) {
		final int i = className.lastIndexOf('.');
		String packageN = "";
		String classN = "";
		if (i >= 0) {
			packageN = className.substring(0, i);
			classN = className.substring(i + 1);
		} else {
			classN = className;
		}
		String[] ret = new String[] { packageN, classN };
		return ret;
	}

	/**
	 * Check whether a method matching the given description is contained in the list of methods to instrument and
	 * return it's index.
	 * 
	 * @param listToSearch
	 *            The {@link MethodDescriptor} list to search.
	 * @param canonicalClassName
	 *            The fully qualified class name of the class containing the method.
	 * @param name
	 *            Simple method name (not qualified). For example: <code>getIndexOfMethodMatch</code>.
	 * @param desc
	 *            A method descriptor as used by Java bytecode. For example:
	 *            <code>(Ljava/lang/String;Ljava/lang/String;)Z</code>
	 * @return The index of the method if it matches a method description in the {@link InstrumentationParameters}. When
	 *         no matching method is found, -1 is returned.
	 */
	public static int findMethodInList(final Collection<MethodDescriptor> listToSearch, final String canonicalClassName,
			final String name, final String desc) {
		if (listToSearch == null) {
			return -1;
		}

		String mName;
		if (name.equals("<init>")) {
			mName = getConstructorName(canonicalClassName);
		} else {
			mName = name;
		}
		int i = 0;
		for (MethodDescriptor md : listToSearch) {
			if (md.getCanonicalClassName().equals(canonicalClassName)
					&& md.getSimpleMethodName().equals(mName)
					&& md.getDescriptor().equals(desc)) {
				return i;
			}
			i++;
		}

		return -1;
	}

	/**
	 * @param canonicalClassName
	 *            Fully qualified class name.
	 * @return The Java method name of the constructor for a given class name.
	 */
	private static String getConstructorName(final String canonicalClassName) {
		if (canonicalClassName == null) {
			throw new IllegalArgumentException("Canonical class name must not be null.");
		}
		int startSplitIndex = canonicalClassName.lastIndexOf('.') + 1;
		return canonicalClassName.substring(startSplitIndex);
	}

	/**
	 * @param desc
	 *            A parameter descriptor string as used in Java bytecode.
	 * @return An array with length=(nr of parameters) where the entry at index i reflects the type of the parameter at
	 *         index i.
	 */
	public static JavaType[] getParametersTypesFromDesc(String desc) {
		if (desc == null) {
			throw new IllegalArgumentException("Descriptor must not be null.");
		}
		int indOpeningBrace = desc.indexOf('(');
		int indClosingBrace = desc.lastIndexOf(')');
		String croppedDesc = desc.substring(indOpeningBrace + 1, indClosingBrace);
		return parseTypesFromBytecodeDesc(croppedDesc);
	}

	/**
	 * @param desc
	 *            A return value descriptor string as used in Java bytecode.
	 * @return The type that of the return value.
	 */
	public static JavaType getReturnTypeFromDesc(String desc) {
		if (desc == null) {
			throw new IllegalArgumentException("Descriptor must not be null.");
		}
		int indClosingBrace = desc.lastIndexOf(')');
		String croppedDesc = desc.substring(indClosingBrace + 1);
		return parseTypesFromBytecodeDesc(croppedDesc)[0];
	}

	/**
	 * Parse a type string as written in a Java signature into a type descriptor.
	 * 
	 * @param typeString
	 *            String of a type definition. For example: "void". Leading or trailing whitespaces are not allowed!
	 * @return A type descriptor string representing the type defined by typeString. For example: "V" for "void". For
	 *         class types, the the package seperator '/' is used. For example "java/lang/String".
	 */
	private static String parseType(String typeString) {
		if (typeString == null || typeString.length() < 2) {
			throw new IllegalArgumentException("Type string must not be null or empty.");
		}
		String tStr = typeString;
		StringBuilder returnString = new StringBuilder();

		while (tStr.endsWith("]")) {
			returnString.append(TYPE_ARRAY);
			// cut off "[]" and parse again
			tStr = typeString.substring(0, tStr.length() - 2);
		}

		if (tStr.equals("boolean")) {
			returnString.append(TYPE_BOOLEAN);
		} else if (tStr.equals("byte")) {
			returnString.append(TYPE_BYTE);
		} else if (tStr.equals("char")) {
			returnString.append(TYPE_CHAR);
		} else if (tStr.equals("double")) {
			returnString.append(TYPE_DOUBLE);
		} else if (tStr.equals("float")) {
			returnString.append(TYPE_FLOAT);
		} else if (tStr.equals("int")) {
			returnString.append(TYPE_INT);
		} else if (tStr.equals("long")) {
			returnString.append(TYPE_LONG);
		} else if (tStr.equals("short")) {
			returnString.append(TYPE_SHORT);
		} else if (tStr.equals("void")) {
			returnString.append(TYPE_VOID);
		} else {
			// type is a class
			if (!tStr.contains(".") && !tStr.contains("/")) {
				// no qualifiers for the type
				// Since ByCounter can not infer packages, display a warning.
				// packageless classes will also get this warning
				log.warning("Signature warning " + "Object type (\"" + tStr + "\") "
						+ "possibly not given as full canonical name. "
						+ "If the type is packageless, ignore this warning");
			}// else{
			returnString.append(TYPE_OBJECT + (tStr.replace('.', '/') + ";"));
			// }
		}

		return returnString.toString();
	}

	/**
	 * @param desc
	 *            A type descriptor string as used in Java bytecode. May either be the string of the parameter list
	 *            between '(' and ')' (but without the braces) or the return type descriptor.
	 * @return An array with length=(nr of parameters) where the entry at index i reflects the type of the parameter at
	 *         index i.
	 */
	private static JavaType[] parseTypesFromBytecodeDesc(String desc) {
		if (desc == null) {
			throw new IllegalArgumentException("Descriptor must not be null.");
		}
		ArrayList<JavaType> types = new ArrayList<JavaType>();
		Stack<JavaType> unfinishedTypes = new Stack<JavaType>(); // array types need more than one parse iteration
		boolean newUnfinishedType = false;
		JavaType lastParsedType = null;

		for (int i = 0; i < desc.length(); i++) {
			switch (desc.charAt(i)) {
			case TYPE_ARRAY:
				unfinishedTypes.push(new JavaType((JavaType) null));
				newUnfinishedType = true;
				break;
			case TYPE_BOOLEAN:
				lastParsedType = new JavaType(JavaTypeEnum.Boolean);
				break;
			case TYPE_BYTE:
				lastParsedType = new JavaType(JavaTypeEnum.Byte);
				break;
			case TYPE_CHAR:
				lastParsedType = new JavaType(JavaTypeEnum.Char);
				break;
			case TYPE_DOUBLE:
				lastParsedType = new JavaType(JavaTypeEnum.Double);
				break;
			case TYPE_FLOAT:
				lastParsedType = new JavaType(JavaTypeEnum.Float);
				break;
			case TYPE_INT:
				lastParsedType = new JavaType(JavaTypeEnum.Int);
				break;
			case TYPE_LONG:
				lastParsedType = new JavaType(JavaTypeEnum.Long);
				break;
			case TYPE_OBJECT: {
				// read the class name of the object
				i++;
				int iStart = i;
				do {
					i++;
				} while (desc.charAt(i) != ';');
				lastParsedType = new JavaType(desc.substring(iStart, i).replace('/', '.'));
			}
				break;
			case TYPE_SHORT:
				lastParsedType = new JavaType(JavaTypeEnum.Short);
				break;
			case TYPE_VOID:
				lastParsedType = new JavaType(JavaTypeEnum.Void);
				break;
			}

			if (!newUnfinishedType) {
				// we have a non-array type
				while (!unfinishedTypes.isEmpty()) {
					// the last type is the type of the array elements
					JavaType stackedType = unfinishedTypes.pop();
					stackedType.setChildElementType(lastParsedType);
					lastParsedType = stackedType;
				}
				types.add(lastParsedType);
			} else {
				newUnfinishedType = false;
			}
		}
		return types.toArray(new JavaType[types.size()]);
	}

	/**
	 * Used for parsing from a Java signature. Since generic types are not respected at bytecode signature level, remove
	 * those declarations from the signature.
	 * 
	 * @param signature
	 *            A Java signature.
	 * @return The cleaned signature.
	 */
	public static String removeGenericTyping(String signature) {
		if (signature == null || signature.length() <= 0) {
			throw new IllegalArgumentException("Signature must not be null or empty.");
		}
		String sig = signature;
		int braceLevel = 0;
		int cutStart = -1;
		int cutEnd = -1;
		while (sig.contains("<")) {
			for (int i = 0; i < sig.length(); i++) {
				if (sig.charAt(i) == '<') {
					braceLevel++;
					if (braceLevel == 1) {
						// first brace
						cutStart = i;
					}
				} else if (sig.charAt(i) == '>') {
					braceLevel--;
					if (braceLevel == 0) {
						cutEnd = i;
						// cut the part from cutStart to cutEnd out
						sig = sig.substring(0, cutStart) + sig.substring(cutEnd + 1, sig.length());
						// start to search again
						i = 0;
					} else if (braceLevel < 0) {

						throw new IllegalArgumentException("Error in method signature " + signature
								+ ". Number of '<' does not match number of '>'");
					}
				}
			}
		}
		return sig;
	}

	/**
	 * Name of the class containing the method.
	 */
	private String className = null;

	/** @see #getContext() */
	private UUID context = null;

	/** @see #getDescriptor() */
	private String descriptor = null;

	/** @see #setInlineImmediately(boolean) */
	private boolean inlineImmediately = false;

	/**
	 * Signals whether the method underlying this MethodDescriptor is a constructor.
	 */
	private boolean isConstructor;

	/** @see #isInvariant() */
	private boolean isInvariant = false;

	/**
	 * When true, the method described is static.
	 */
	private boolean methodIsStatic;

	/**
	 * The name of the package containing the class of the method.
	 */
	private String packageName = null;

	/**
	 * The name of the method without any qualifiers etc.
	 */
	private String simpleMethodName = null;

	/**
	 * Only used for {@link #_constructMethodDescriptorFromASM(String, String, String)}
	 */
	private MethodDescriptor() {
	}
	
	/**
	 * Copy constructor.
	 * @param template {@link MethodDescriptor} to create a copy of.
	 */
	public MethodDescriptor(final MethodDescriptor template) {
		this.className = template.className;
		this.context = template.context;
		this.descriptor = template.descriptor;
		this.inlineImmediately = template.inlineImmediately;
		this.isConstructor = template.isConstructor;
		this.isInvariant = template.isInvariant;
		this.methodIsStatic = template.methodIsStatic;
		this.packageName = template.packageName;
		this.simpleMethodName = template.simpleMethodName;
	}

	/**
	 * Constructs a MethodDescriptor directly from a Java reflection Constructor.
	 * 
	 * @param c
	 *            {@link Constructor} that is described by the MethodDescriptor.
	 */
	public MethodDescriptor(Constructor<?> c) {
		if (c == null) {
			throw new IllegalArgumentException("Constructor object must not be null.");
		}
		construct(c.getDeclaringClass().getPackage().getName(), c.getDeclaringClass().getSimpleName(), c.getName(),
				Type.getConstructorDescriptor(c), false);
	}

	/**
	 * Constructs a MethodDescriptor directly from a Java reflection Method.
	 * 
	 * @param m
	 *            Method that is described by the MethodDescriptor.
	 */
	public MethodDescriptor(Method m) {
		if (m == null) {
			throw new IllegalArgumentException("Method object must not be null.");
		}
		Package p = m.getDeclaringClass().getPackage();
		String packageName = "";
		String simpleClassName = "";
		if (p == null) {
			// this can happen if the class was not loaded yet
			// we can parse the canonical classname though.
			String[] names = canonicalClassNameToPackageAndSimpleName(m.getDeclaringClass().getCanonicalName());
			packageName = names[0];
			simpleClassName = names[1];
		} else {
			packageName = p.getName();
			simpleClassName = m.getDeclaringClass().getSimpleName();
		}
		this.construct(packageName, simpleClassName, m.getName(), Type.getMethodDescriptor(m), false);
	}

	/**
	 * Construct a MethodDescriptor from a Java method signature. It is able to handle both static and non-static method
	 * and constructors.
	 * 
	 * @param canonicalClassName
	 *            The canonical name of the class declaring the method.
	 * @param signature
	 *            A string containing a standard Java method signature with fully qualified types. For example:
	 *            <code>public static java.lang.String[] canonicalClassNameToPackageAndSimpleName(java.lang.String className)</code>
	 *            <p>
	 *            Qualifiers defining visibility (like <code>public</code>) may be omitted (and are ignored), while the
	 *            qualifier <code>static</code> is handled. For method parameters, it is allowed to provide just the
	 *            type or the type and its name (example: <code>int[]</code> or <code>int[] abc</code>). Generic types
	 *            may be omitted (and are ignored) so that <code>List</code> and <code>List&lt;Integer&gt;</code> are
	 *            treated as the same since bytecode signatures ignore generics. It is advised to take the method
	 *            signature from source code or from documentation and only adapt it, if necessary.
	 *            <p>
	 *            <b>Important:</b> The names of object types need to be adapted! So instead of giving the String
	 *            <code>String myString</code>, this has to be expanded to <code>java.lang.String myString</code>. Note
	 *            that inner/nested classes need to be specified using the '$' symbol as in the following example:
	 *            <code>my.packagename.OutClass$InnerClass</code>.
	 */
	public MethodDescriptor(String canonicalClassName, String signature) {
		// do some error checks
		if (canonicalClassName == null || canonicalClassName.length() <= 0) {
			throw new IllegalArgumentException("The classname for the methoddescriptor was not supplied.");
		}
		if (signature == null || signature.length() <= 0) {
			throw new IllegalArgumentException("The signature for the methoddescriptor was not supplied.");
		}

		String[] packageAndClass = MethodDescriptor.canonicalClassNameToPackageAndSimpleName(canonicalClassName);
		String mPackageName = packageAndClass[0];
		String mClassName = packageAndClass[1];

		String retType;
		String simpleMethodName = null;
		ArrayList<String> argTypes = new ArrayList<String>();
		this.methodIsStatic = false;

		String sig = removeGenericTyping(signature);

		final int split1 = sig.indexOf('(');
		final int split2 = sig.lastIndexOf(')');

		// make sure we found something
		if (split1 < 0 || split2 < 0) {
			throw new IllegalArgumentException(
					"The signature supplied for the methoddescriptor does not have the correct braces '(' or ')'");
		}

		final String returnStr = sig.substring(0, split1); // the first part contains the return type
		final String inBracesStr = sig.substring(split1 + 1, split2); // the part in braces contains the parameters

		String[] tokens = null;
		tokens = returnStr.split("(\\s)+"); // split at whitespaces
		if (tokens.length < 1) {
			throw new IllegalArgumentException("No tokens before '(' in signature.");
		} else {
			// the last token should be the method name.
			simpleMethodName = tokens[tokens.length - 1];
			// check if method is a constructor
			if (simpleMethodName.equals(getConstructorName(canonicalClassName))) {
				if (tokens.length > 2) {
					throw new IllegalArgumentException("Error parsing constructor. Expecting one or two tokens before"
							+ " '(' for constructors.");
				}
				this.isConstructor = true;
				// in java byte code constructors have return type void
				retType = String.valueOf(TYPE_VOID);
			} else {
				if (tokens.length < 2) {
					throw new IllegalArgumentException("Error parsing return type for Java signature. "
							+ "Expecting at least two tokens before '(' for non-constructor methods.");
				}
				this.isConstructor = false;
				// parse the return type (the second last token should be the type)
				// example: "public static void main"
				retType = parseType(tokens[tokens.length - 2]);

			}
			// look for modifiers
			for (int i = 0; i < tokens.length - 2; i++) {
				if (tokens[i].equals("static")) {
					this.methodIsStatic = true;
				}
			}
		}

		if (inBracesStr.length() > 0) {
			// try to parse the parameters
			String[] params = inBracesStr.split(",");
			for (String s : params) {
				s = s.trim(); // remove leading and trailing whitespaces
				tokens = s.split("(\\s)+"); // split at whitespaces
				if (tokens.length < 1 || tokens.length > 3) {
					throw new IllegalArgumentException("Error parsing Java signature \"" + signature + "\": "
							+ "Parameter definition does not contain 1, 2 or 3 tokens");
				} else {
					// if the array braces "[]" are appended to the parameter name,
					// cut them of and append them to the type string:
					String parameterName = tokens[tokens.length - 1];
					String arrayBraces = "";
					int end;
					for (end = parameterName.length(); end > 0 && parameterName.substring(0, end).endsWith("[]"); end -= 2) {
						arrayBraces += "[]";
					}

					// the second last token should be the type.
					// example: "int xyz"
					argTypes.add(parseType(tokens[tokens.length - 2] + arrayBraces));
				}
			}
		}

		// construct the descriptor
		StringBuilder constructedDescriptor = new StringBuilder();
		constructedDescriptor.append("(");
		for (String arg : argTypes) {
			constructedDescriptor.append(arg);
		}
		constructedDescriptor.append(")");
		constructedDescriptor.append(retType);
		this.construct(mPackageName, mClassName, simpleMethodName, constructedDescriptor.toString(), isConstructor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(MethodDescriptor o) {
		// compare package name
		int comparisonResult = this.packageName.compareTo(o.packageName);
		if (comparisonResult != 0) {
			return comparisonResult;
		}
		// compare class name
		comparisonResult = this.className.compareTo(o.className);
		if (comparisonResult != 0) {
			return comparisonResult;
		}
		// compare simple method name
		comparisonResult = this.simpleMethodName.compareTo(o.simpleMethodName);
		if (comparisonResult != 0) {
			return comparisonResult;
		}
		// compare descriptor
		comparisonResult = this.descriptor.compareTo(o.descriptor);
		if (comparisonResult != 0) {
			return comparisonResult;
		}
		// consider the MethodDescriptors the same
		return 0;
	}

	/**
	 * Sets the essential properties of the {@link MethodDescriptor}. No checks are done here, so make sure the given
	 * parameters are valid!
	 * 
	 * @param packageName
	 *            Package name ({@link #getPackageName()}).
	 * @param className
	 *            Simple class name ({@link #getSimpleClassName()}).
	 * @param methodName
	 *            Simple method name ({@link #getSimpleMethodName()}).
	 * @param descriptor
	 *            Method descriptor {@link #getDescriptor()}.
	 * @param isConstructor
	 *            Is the method a constructor.
	 */
	protected void construct(String packageName, String className, String methodName, String descriptor,
			boolean isConstructor) {
		this.packageName = packageName;
		this.setSimpleClassName(className);

		if (isConstructor) { // we have a constructor
			this.simpleMethodName = getConstructorName(this.className);
		} else { // normal method; use the given name
			this.simpleMethodName = methodName;
		}
		this.descriptor = descriptor;
		this.isConstructor = isConstructor;
		this.setInlineImmediately(inlineImmediately);
		this.setInvariant(isInvariant);

		// For inner/nested classes, an otherwise hidden parameter
		// (the outer class) needs to be added to the constructor:
		if (isConstructor && this.className.contains("$")) {
			int i = this.descriptor.indexOf("("); // '(' is always in the descriptor
			int j = this.className.lastIndexOf("$");
			this.descriptor = this.descriptor.substring(0, i + 1) + "L" + packageName.replace(".", "/") + "/"
					+ this.className.substring(0, j) + ";" // outer class
					+ this.descriptor.substring(i + 1);
		}
	}

	/**
	 * Checks for equaling class name, method name, package name and descriptor.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MethodDescriptor) {
			MethodDescriptor other = (MethodDescriptor) obj;
			if (this.className.equals(other.className) && this.simpleMethodName.equals(other.simpleMethodName)
					&& this.packageName.equals(other.packageName) && this.descriptor.equals(other.descriptor)) {
				return true;
			} else {
				return false;
			}
		} else {
			return super.equals(obj);
		}
	}

	/**
	 * @return The canonical name of the class declaring the method.
	 */
	public String getCanonicalClassName() {
		if (this.packageName.length() > 0) {
			return this.packageName + "." + this.className;
		} else {
			// default (no) package
			return this.className;
		}
	}

	/**
	 * @return A descriptor string to uniquely identify methods consisting of the canonical classname, the method name
	 *         and the method descriptor.
	 */
	public String getCanonicalMethodName() {
		return this.getCanonicalClassName() + "." + this.simpleMethodName + this.descriptor;
	}

	/**
	 * @return Simple name of the class containing the method.
	 */
	public String getClassName() {
		return this.className;
	}

	/**
	 * @return A {@link UUID} for this instance of the MethodDescriptor. This can be used to provide context to the
	 *         counting of the specified method. Used for debugging at this point.
	 */
	public UUID getContext() {
		return this.context;
	}

	/**
	 * Gets the descriptor string as used in Java bytecode for descriptor. The descriptor returned does not contain a
	 * reference to the containing class anymore.
	 * 
	 * @return The method descriptor string.
	 */
	public String getDescriptor() {
		return this.descriptor;
	}

	/**
	 * 
	 * @return True if the described method is a constructor; false otherwise.
	 */
	public boolean getIsConstructor() {
		return this.isConstructor;
	}

	/**
	 * @return When true, the method described is static.
	 */
	public boolean getMethodIsStatic() {
		return methodIsStatic;
	}

	/**
	 * Gets the simple method signature of the described method (i.e. without package and/or class names).
	 * 
	 * @return The method signature.
	 */
	public String getMethodSignature() {
		return this.simpleMethodName + this.descriptor;
	}

	/**
	 * @return The package name.
	 */
	public String getPackageName() {
		return this.packageName;
	}

	/**
	 * Gets the qualifying method signature of the described method. This consists of the qualifying method name 
	 *  and the descriptor ({@link #getDescriptor()}; i.e. with the types of
	 * input/output parameters).
	 * 
	 * @return The method signature.
	 */
	public String getQualifyingMethodName() {
		return this.getCanonicalClassName() + "." + this.simpleMethodName + this.descriptor;
	}

	/**
	 * @return The simple name of the class declaring the method.
	 */
	public String getSimpleClassName() {
		return className;
	}

	/**
	 * Gets the simple method name of the described method.
	 * 
	 * @return The method name.
	 */
	public String getSimpleMethodName() {
		return this.simpleMethodName;
	}

	/**
	 * @return True iff the method was found to be a constructor.
	 */
	public boolean isConstructor() {
		return isConstructor;
	}

	/** @see #setInlineImmediately(boolean) */
	public boolean isInlineImmediately() {
		return inlineImmediately;
	}

	/** @see #setInvariant(boolean) */
	public boolean isInvariant() {
		return isInvariant;
	}

	/**
	 * @see #getContext()
	 * @param context
	 *            A new context UUID to set, replacing the old one.
	 */
	public void setContext(UUID context) {
		if (this.context != null) {
			log.fine("Replacing context " + this.context + " with context " + context);
		}
		this.context = context;
	}

	/**
	 * Some methods (e.g. those invariant w.r.t. bytecode counts, irrespective of parameters, if any) may not warrant an
	 * own CountingResult object, and their counts should be "inlined" immediately instead.
	 * 
	 * @param inlineImmediately
	 *            When true, inlining of this method is active.
	 */
	public void setInlineImmediately(boolean inlineImmediately) {
		this.inlineImmediately = inlineImmediately;
	}

	/**
	 * Some methods are invariant w.r.t. bytecode counts, i.e. their runtime bytecode counts is constant (invariant)
	 * irrespective of method input parameters (if any).
	 * 
	 * @param isInvariant
	 *            True marks the method as invariant.
	 */
	public void setInvariant(boolean isInvariant) {
		this.isInvariant = isInvariant;
	}

	/**
	 * Change the package name to describe a different method.
	 * 
	 * @see #getPackageName()
	 * @param packageName
	 *            The package name to change to.
	 */
	public void setPackageName(final String packageName) {
		this.packageName = packageName;
	}

	/**
	 * Change the class name to describe a different method.
	 * 
	 * @see #getSimpleClassName()
	 * @param className
	 *            The class name to change to.
	 */
	public void setSimpleClassName(final String className) {
		this.className = className.replace(".", "$");
	}

	/**
	 * One line string with the basic properties of this {@link MethodDescriptor}.
	 */
	public String toString() {
		String ret = "Package: " + this.packageName + "  Class: " + this.className + "  Method name: "
				+ this.simpleMethodName + "  Descriptor: " + this.descriptor + "  isConstructor=" + isConstructor
				+ "  isInvariant=" + isInvariant() + "  inlineImmediately=" + isInlineImmediately();
		return ret;
	}

	/**
	 * Multi line string with the basic properties of this {@link MethodDescriptor}.
	 */
	public String toString_Linebreaks() {
		String ret = "Package: " + this.packageName + "  Class: " + this.className + "\n" + "Method name: "
				+ this.simpleMethodName + "  Descriptor: " + this.descriptor + "\n" + "isConstructor=" + isConstructor
				+ "  isInvariant=" + isInvariant() + "  inlineImmediately=" + isInlineImmediately();
		return ret;
	}
}
