package de.uka.ipd.sdq.ByCounter.parsing;

import java.util.ArrayList;
import java.util.List;

import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * A node in the call graph that represents a method.
 * 
 * @author Martin Krogmann
 *
 */
public final class CallGraphMethod {
	private String owner;
	private String name;
	private String desc;
	private List<CallGraphMethod> childMethods;
	
	/**
	 * @param desc
	 * @param name
	 * @param owner
	 */
	public CallGraphMethod(String owner, String name, String desc) {
		this.owner = owner;
		this.name = name;
		this.desc = desc;
		this.childMethods = new ArrayList<CallGraphMethod>();
	}
	
	/**
	 * @param obj The {@link Object} to compare to.
	 * @return True, if this node represents the same method  as obj,
	 * i.e. all fields are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CallGraphMethod) {
			CallGraphMethod cgm = (CallGraphMethod)obj;
			return cgm.desc.equals(this.desc) &&
				cgm.owner.equals(this.owner) &&
				cgm.name.equals(this.name);
		} else {
			return false;
		}
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return this.owner;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the desc
	 */
	public String getDesc() {
		return this.desc;
	}

	/**
	 * @return the childMethods
	 */
	public List<CallGraphMethod> getChildMethods() {
		return this.childMethods;
	}
	
	/**
	 * Add m to the list of child methods, i.e. methods called by this method.
	 * If m is already in the list of child methods, it will not be added again.
	 * @param m the {@link CallGraphMethod} to add as child.
	 */
	public void addChildMethod(CallGraphMethod m) {
		if(!this.childMethods.contains(m)) {
			this.childMethods.add(m);
		}
	}

	/**
	 * Constructs a <code>String</code> with all attributes
	 * in name = value format.
	 *
	 * @return A <code>String</code> representation 
	 * of this object.
	 */
	public String toString() {
	    final String TAB = "    ";
	    
	    String retValue = "";
	    
	    // dont  use the full string representation of childs; too much recursion
	    StringBuilder childs = new StringBuilder("[");
	    for(CallGraphMethod m : this.childMethods) {
	    	childs.append(m.toStringNoChilds() + ",");
	    }
	    childs.append("]");
	    
	    retValue = "CallGraphMethod ( "
	        + "owner = " + this.owner + TAB
	        + "name = " + this.name + TAB
	        + "desc = " + this.desc + TAB
	        + "childMethods = " + childs + TAB
	        + " )";
	
	    return retValue;
	}
	
	/**
	 * Similar to {@link #toString()}, but not displaying childs (only their 
	 * number is returned).
	 * @return A {@link String} representation of this.
	 */
	public String toStringNoChilds() {
	    final String TAB = "    ";
	    
	    String retValue = "";
	    
	    retValue = "CallGraphMethod ( "
	        + "owner = " + this.owner + TAB
	        + "name = " + this.name + TAB
	        + "desc = " + this.desc + TAB
	        + this.childMethods.size() + " childMethods" + TAB
	        + " )";
	
	    return retValue;
	}
	
	/**
	 * @param method The {@link MethodDescriptor} for a method to compare to.
	 * @return True, if this node represents the same method as method,
	 * i.e. all fields equal their equivalents.
	 */
	public boolean matchesMethodDescriptor(MethodDescriptor method) {
//		System.out.println(method.getDescriptor()+  " ## " + this.desc);
//		System.out.println(method.getCanonicalClassName()+  " ## " + this.owner.replace('.', '/'));
//		System.out.println(method.getMethodName()+  " ## " + this.name);
		return method.getDescriptor().equals(this.desc) &&
			method.getCanonicalClassName().equals(this.owner.replace('/', '.')) &&
			method.getSimpleMethodName().equals(this.name);
	}

	/**
	 * @return {@link MethodDescriptor} for this method.
	 */
	public MethodDescriptor getMethodDescriptor() {
		return MethodDescriptor._constructMethodDescriptorFromASM(owner, name, desc);
	}
}
