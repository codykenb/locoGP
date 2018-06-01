package de.uka.ipd.sdq.ByCounter.parsing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;


/**
 * The static call graph where the nodes are methods. Two methods m1, m2 are 
 * connected if m1 contains instructions to call m2. 
 * @author Martin Krogmann
 *
 */
public final class CallGraph {
	/**
	 * All nodes in the graph.
	 */
	private ArrayList<CallGraphMethod> nodes;

	/**
	 * A set of all classes that have been considered in the call graph.
	 */
	private Set<String> parsedClasses;
	
	
	/**
	 * Creates a {@link CallGraph} with only a root node.
	 */
	public CallGraph() {
		this.parsedClasses = new HashSet<String>();
		this.nodes = new ArrayList<CallGraphMethod>();
	}

	/**
	 * Adds a link from method m1 to method m2.
	 * @param m1 Caller method.
	 * @param m2 Callee method.
	 */
	public void addMethodCall(CallGraphMethod m1, CallGraphMethod m2) {
		// first try to find the nodes if they are in the list
		int indexM1 = nodes.indexOf(m1);
		int indexM2 = nodes.indexOf(m2);
		CallGraphMethod caller;
		CallGraphMethod callee;
		
		if(indexM1 >= 0) {
			caller = nodes.get(indexM1);
		} else {
			nodes.add(m1);
			caller = m1;
		}
		if(indexM2 >= 0) {
			callee = nodes.get(indexM2);
		} else {
			nodes.add(m2);
			callee = m2;
		}
		// now add the edge
		caller.addChildMethod(callee);
	}

	/**
	 * Add a class to the set of parsed classes.
	 * @param parsedClass The canonical name of a class that has been parsed 
	 * into the call tree.
	 * @see #getParsedClasses()
	 */
	public void addParsedClass(String parsedClass) {
		this.parsedClasses.add(parsedClass);
	}

	/**
	 * @return The set of all classes that have been considered in the call graph.
	 * @see #addParsedClass(String)
	 */
	public Set<String> getParsedClasses() {
		return parsedClasses;
	}
	
	/**
	 * Find the specified method in the graph.
	 * @param method A {@link MethodDescriptor} for the method to search.
	 * @return The {@link CallGraphMethod} node in the graph if method was found.
	 * Null otherwise.
	 */
	public CallGraphMethod findMethod(MethodDescriptor method) {
		for(CallGraphMethod m : this.nodes) {
			if(m.matchesMethodDescriptor(method)) {
				return m;
			}
		}
		return null;
	}

}
