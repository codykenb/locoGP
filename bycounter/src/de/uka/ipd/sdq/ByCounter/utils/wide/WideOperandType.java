package de.uka.ipd.sdq.ByCounter.utils.wide;
/**
 * Type (i.e. instruction prefixes) used by some instructions 
 * which are permitted for use with the special bytecode instruction "wide"
 * @author Michael Kuperberg
 */
public enum WideOperandType {
D, F, //	INT_PRIMITIVE, FLOAT_PRIMITIVE, OBJECT, LONG_PRIMITIVE, DOUBLE_PRIMITIVE
	I, L, O
}

