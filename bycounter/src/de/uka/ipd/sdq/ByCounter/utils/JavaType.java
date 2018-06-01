package de.uka.ipd.sdq.ByCounter.utils;

/**
 * Representation of a Java type.
 * @author Martin Krogmann
 */
public class JavaType {
	
	private JavaTypeEnum type;
	private String canonicalClassName;
	private JavaType childElementType;

	/**
	 * Construct a {@link JavaType} instance.
	 * @param type see {@link #getType()}
	 * @param canonicalClassName see {@link #getCanonicalClassName()}
	 * @param childElementType see {@link #getClass()}
	 */
	public JavaType(JavaTypeEnum type, String canonicalClassName,
			JavaType childElementType) {
		this.type = type;
		this.canonicalClassName = canonicalClassName;
		this.childElementType = childElementType;
	}
	
	/**
	 * Construct a simple type (not an object or array).
	 * @param type {@link JavaTypeEnum}.
	 */
	public JavaType(JavaTypeEnum type) {
		this.type = type;
		this.canonicalClassName = null;
		this.childElementType = null;
	}

	/**
	 * Construct an object type.
	 * @param canonicalClassName Canonical class name of the object type.
	 */
	public JavaType(String canonicalClassName) {
		this.type = JavaTypeEnum.Object;
		this.canonicalClassName = canonicalClassName;
		this.childElementType = null;
	}
	

	/**
	 * Construct an array type.
	 * @param childElementType Type of the array elements.
	 */
	public JavaType(JavaType childElementType) {
		this.type = JavaTypeEnum.Array;
		this.canonicalClassName = null;
		this.childElementType = childElementType;
	}

	/**
	 * @return The {@link JavaTypeEnum}, i.e. the category of this type.
	 */
	public JavaTypeEnum getType() {
		return type;
	}
	
	/**
	 * @return If {@link #getType()} equals {@link JavaTypeEnum#Object},
	 *  this is the canonical class name of the object type. Null in all other
	 *  cases. 
	 */
	public String getCanonicalClassName() {
		return canonicalClassName;
	}

	/**
	 * @return If {@link #getType()} equals {@link JavaTypeEnum#Array},
	 *  this is the type of the array elements.
	 */
	public JavaType getChildElementType() {
		return childElementType;
	}
	
	@Override
	public String toString() {
		switch(this.type) {
		case Object:
			return "Object (" + this.canonicalClassName + ")";
			
		case Array:
			return "Array (" + this.childElementType + ")";
			
		default:
			return this.type.toString();
		}
	}

	protected void setChildElementType(JavaType type) {
		this.childElementType = type;
	}
}
