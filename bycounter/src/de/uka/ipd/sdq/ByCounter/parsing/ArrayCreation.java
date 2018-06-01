package de.uka.ipd.sdq.ByCounter.parsing;

import java.io.Serializable;

import org.objectweb.asm.Opcodes;

/**
 * Objects of this type represent the construction of an array.
 * @author Martin Krogmann
 *
 */
/**
 * @author martin
 *
 */
public class ArrayCreation implements Serializable {
	
	/**
	 * Serialization version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * When {@link #getTypeOpcode()} has this type, either 
	 * {@link #getTypeDesc()} returns a non-null value or no or an invalid 
	 * type was specified.
	 */
	public static final int INVALID_TYPE = -1;

	/**
	 * The opcode of the array type.
	 * @see Opcodes#T_BOOLEAN
	 * @see Opcodes#T_BYTE
	 * @see Opcodes#T_CHAR
	 * @see Opcodes#T_DOUBLE
	 * @see Opcodes#T_FLOAT
	 * @see Opcodes#T_INT
	 * @see Opcodes#T_LONG
	 * @see Opcodes#T_SHORT
	 */
	private int typeOpcode;
	/**
	 * Descriptor of the type of the array elements.
	 */
	private String typeDesc;
	/**
	 * Number of dimensions when constructing a multidimensional 
	 * array. 1 for 1-dimensional arrays.
	 */
	private int numberOfDimensions;
	
	/**
	 * Initialise fields.
	 */
	public ArrayCreation() {
		this.typeOpcode = INVALID_TYPE;
		this.typeDesc = null;
		this.numberOfDimensions = 1;
	}

	/**
	 * @param typeOpcode The opcode of the array type.
	 * @see Opcodes#T_BOOLEAN
	 * @see Opcodes#T_BYTE
	 * @see Opcodes#T_CHAR
	 * @see Opcodes#T_DOUBLE
	 * @see Opcodes#T_FLOAT
	 * @see Opcodes#T_INT
	 * @see Opcodes#T_LONG
	 * @see Opcodes#T_SHORT
	 */
	public void setTypeOpcode(int typeOpcode) {
		this.typeOpcode = typeOpcode;
	}

	/**
	 * @return The opcode of the array type.
	 * @see Opcodes#T_BOOLEAN
	 * @see Opcodes#T_BYTE
	 * @see Opcodes#T_CHAR
	 * @see Opcodes#T_DOUBLE
	 * @see Opcodes#T_FLOAT
	 * @see Opcodes#T_INT
	 * @see Opcodes#T_LONG
	 * @see Opcodes#T_SHORT
	 */
	public int getTypeOpcode() {
		return this.typeOpcode;
	}
	
	public static String getTypeAsString(final int typeOpcode) {
		final String str;
		// convert the type to a readable string
		switch(typeOpcode) {
		case 4:
			str = "boolean";
			break;
		case 5:
			str = "char";
			break;
		case 6:
			str = "float";
			break;
		case 7:
			str = "double";
			break;
		case 8:
			str = "byte";
			break;
		case 9:
			str = "short";
			break;
		case 10:
			str = "int";
			break;
		case 11:
			str = "long";
			break;
		default:
			throw new IllegalArgumentException("Unknown object type id: " + typeOpcode);
		}
		return str;
	}

	/**
	 * @return Descriptor of the type of the array elements. If the type was
	 * specified via {@link #setTypeOpcode(int)}, a descriptor is automatically 
	 * generated.
	 */
	public String getTypeDesc() {
		if(this.typeDesc == null) {
			this.typeDesc = getTypeAsString(this.typeOpcode);
		}
		return this.typeDesc;
	}
	
	/**
	 * @param desc Descriptor of the type of the array elements.
	 */
	public void setTypeDesc(final String desc) {
		this.typeDesc = desc;
	}

	/**
	 * @return Number of dimensions when constructing a multidimensional 
	 * array. 1 for 1-dimensional arrays.
	 */
	public int getNumberOfDimensions() {
		return this.numberOfDimensions;
	}
	
	/**
	 * @param dims Number of dimensions when constructing a multidimensional 
	 * array.
	 */
	public void setNumberOfDimensions(int dims) {
		this.numberOfDimensions = dims;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.numberOfDimensions;
		result = prime * result
				+ ((this.typeDesc == null) ? 0 : this.typeDesc.hashCode());
		result = prime * result + this.typeOpcode;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArrayCreation other = (ArrayCreation) obj;
		if (this.numberOfDimensions != other.numberOfDimensions)
			return false;
		if (this.typeDesc == null) {
			if (other.typeDesc != null)
				return false;
		} else if (!this.typeDesc.equals(other.typeDesc))
			return false;
		if (this.typeOpcode != other.typeOpcode)
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ArrayCreation [typeOpcode=");
		builder.append(this.typeOpcode);
		builder.append(", typeDesc=");
		builder.append(this.getTypeDesc());
		builder.append(", numberOfDimensions=");
		builder.append(this.numberOfDimensions);
		builder.append("]");
		return builder.toString();
	}
}
