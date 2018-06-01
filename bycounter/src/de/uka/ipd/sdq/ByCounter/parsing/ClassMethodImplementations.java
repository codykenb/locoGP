package de.uka.ipd.sdq.ByCounter.parsing;

import java.util.HashSet;

/**
 * Holds the methods implemented in a class as well as the name of the super 
 * class.
 */
public final class ClassMethodImplementations {
	
	private String superClass;
	
	private HashSet<String> methods;
	
	public ClassMethodImplementations() {
		this.methods = new HashSet<String>();
	}
	
	/**
	 * @param superClass Internal name of the super class.
	 */
	public void setSuperClass(String superClass) {
		this.superClass = superClass;
	}
	
	/**
	 * @return Internal name of the super class.
	 */
	public String getSuperClass() {
		return this.superClass;
	}

	/**
	 * @return the methods
	 */
	public HashSet<String> getMethods() {
		return methods;
	}

}
