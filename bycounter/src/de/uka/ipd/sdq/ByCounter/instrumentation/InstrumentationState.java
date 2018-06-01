/**
 * 
 */
package de.uka.ipd.sdq.ByCounter.instrumentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Label;

import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * @author Martin Krogmann
 *
 * This class holds various intermediate results of instrumentation stages and 
 * can be passed from one stage to the next. Every field of this class is 
 * computed by ByCounter, i.e. not user specified.
 */
public class InstrumentationState {

	
	/**
	 * Context information that is important even after instrumentation.
	 */
	private InstrumentationContext instrumentationContext;
	
	/**
	 * This {@link List} also contains methods selected for recursive instrumentation.
	 * @see {@link #methodsToInstrument}, {@link #instrumentRecursively}
	 * <b>This is for ByCounter internal use only!</b> 
	 */
	private List<MethodDescriptor> methodsToInstrumentCalculated;
	
	/**
	 * This {@link Map} contains for each method to instrument the 
	 * instances of {@link EntityToInstrument} that are specified for that method.
	 */
	private Map<String, List<EntityToInstrument>> entitiesToInstrumentByMethod;
	
	/**
	 * Set of methods that have been instrumented successfully.
	 */
	private List<MethodDescriptor> successFullyInstrumentedMethods;

	/**
	 * The labels that start a basic block.
	 */
	private Label[] basicBlockLabels;
	
	/**
	 * The labels that are part of a range block.
	 */
	private Map<Label, Integer> rangeBlockContainsLabels;

	/**
	 * Method invocations by method containing the call.
	 */
	private Map<String, List<String>> methodInvocations;

	/**
	 * Classes that have been marked to be completely instrumented using
	 * {@link InstrumentedClass}.
	 */
	private Map<String, EntityToInstrument> fullyInstrumentedClasses;

	/**
	 * Initialises all fields.
	 */
	public InstrumentationState() {
		this.instrumentationContext = new InstrumentationContext();
    	this.setSuccessFullyInstrumentedMethods(new ArrayList<MethodDescriptor>());
    	this.rangeBlockContainsLabels = new HashMap<Label, Integer>();
    	this.basicBlockLabels = new Label[0];
    	this.methodInvocations = new HashMap<String, List<String>>();
    	this.methodsToInstrumentCalculated = new ArrayList<MethodDescriptor>();
    	this.entitiesToInstrumentByMethod = new HashMap<String, List<EntityToInstrument>>();
    	this.fullyInstrumentedClasses = new HashMap<String, EntityToInstrument>();
	}

	/**
	 * This is used in the instrumentation process to save basic block 
	 * definitions etc.
	 * @return the {@link InstrumentationContext}
	 */
	public InstrumentationContext getInstrumentationContext() {
		return this.instrumentationContext;
	}
	/**
	 * @param methodsToInstrumentCalculated the methodsToInstrumentCalculated to set
	 */
	public void setMethodsToInstrumentCalculated(
			List<MethodDescriptor> methodsToInstrumentCalculated) {
		this.methodsToInstrumentCalculated = methodsToInstrumentCalculated;
	}

	/**
	 * @return the methodsToInstrumentCalculated
	 */
	public List<MethodDescriptor> getMethodsToInstrumentCalculated() {
		return methodsToInstrumentCalculated;
	}

	/**
	 * @return This {@link Map} contains for each method to instrument the 
	 * instances of {@link EntityToInstrument} that are specified for that method.
	 */
	public Map<String, List<EntityToInstrument>> getEntitiesToInstrumentByMethod() {
		return this.entitiesToInstrumentByMethod;
	}

	/**
	 * @return the successFullyInstrumentedMethods
	 */
	public List<MethodDescriptor> getSuccessFullyInstrumentedMethods() {
		return successFullyInstrumentedMethods;
	}

	/**
	 * @param successFullyInstrumentedMethods the successFullyInstrumentedMethods to set
	 */
	public void setSuccessFullyInstrumentedMethods(
			List<MethodDescriptor> successFullyInstrumentedMethods) {
		this.successFullyInstrumentedMethods = successFullyInstrumentedMethods;
	}

	/**
	 * @return The labels that start a basic block.
	 */
	public Label[] getBasicBlockLabels() {
		return basicBlockLabels;
	}

	/**
	 * @param basicBlockLabels The labels that start a basic block.
	 */
	public void setBasicBlockLabels(Label[] basicBlockLabels) {
		this.basicBlockLabels = basicBlockLabels;
	}
	
	/**
	 * @return The labels that are part of a range block.
	 */
	public Map<Label, Integer> getRangeBlockContainsLabels() {
		return this.rangeBlockContainsLabels;
	}

	/**
	 * @param rangeBlockContainsLabels The labels that are part of a range block.
	 */
	public void setRangeBlockContainsLabels(
			Map<Label, Integer> rangeBlockContainsLabels) {
		this.rangeBlockContainsLabels = rangeBlockContainsLabels;
	}

	/**
	 * Get the list containing all method signatures that were called in the 
	 * visited method.
	 * @return A map containing method signatures of invocations per method.
	 * @see MethodDescriptor#getCanonicalMethodName()
	 */
	public Map<String, List<String>> getMethodInvocations() {
		return this.methodInvocations;
	}

	/**
	 * @return Classes that have been marked to be completely instrumented using
	 * {@link InstrumentedClass}.
	 */
	public Map<String, EntityToInstrument> getFullyInstrumentedClasses() {
		return this.fullyInstrumentedClasses;
	}

}
