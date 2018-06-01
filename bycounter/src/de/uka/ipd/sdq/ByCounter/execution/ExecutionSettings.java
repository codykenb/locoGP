package de.uka.ipd.sdq.ByCounter.execution;

import java.util.Set;

/**
 * This class holds a collection of settings that relate to the execution of
 * instrumented Java byte code and the details of counting or aggregating the 
 * instrumentation  results.
 * 
 * @author Martin Krogmann
 *
 */
public class ExecutionSettings implements Cloneable {

	/**
	 * This character ('{@value #INTERNAL_CLASSES_DEFINITION_WILDCARD_CHAR}') is
	 * used in the definition of internal classes.
	 * @see #setInternalClassesDefinition(Set) 
	 */
	public static final char INTERNAL_CLASSES_DEFINITION_WILDCARD_CHAR = '*';

	
	/** Default value of {@link #getCountingResultCollectorMode()}. */
	private static final CountingResultCollectorMode COUNTING_RESULT_COLLECTOR_MODE_DEFAULT = CountingResultCollectorMode.UseReportingMethodChoiceByInstrumentedMethods;

	/** Default value of {@link #getAddUpResultsRecursively()}. */
	private static final boolean ADD_UP_RESULTS_RECURSIVELY_DEFAULT = false;

	/** Default value for {@link #getWaitForThreadsToFinnish()}. */
	private static final boolean WAIT_FOR_THREADS_TO_FINNISH_DEFAULT = true;


	/**
	 * Classes defined as internal when using recursive result retrieval.
	 * 
	 * @see #setInternalClassesDefinition(Set)
	 */
	private Set<String> internalClassesDefinition;
	
	/**
	 * Counting mode to use.
	 */
	private CountingResultCollectorMode countingResultCollectorMode;
	
	/**
	 * When true, result retrieval adds up counting results recursively.
	 * This means that results for methods that call other methods include the 
	 * counts of these called methods, i.e. of the entire calling tree.
	 * When false, only the counts for operation done in the method itself
	 * - not those by called methods - are returned.
	 */
	private boolean addUpResultsRecursively;

	/**
	 * @see #getParentClassLoader()
	 */
	private ClassLoader parentClassLoader;

	/**
	 * @see #getWaitForThreadsToFinnish()
	 */
	private boolean waitForThreadsToFinnish;

	/**
	 * Construct {@link ExecutionSettings} by setting every field to it's 
	 * default value. 
	 */
	public ExecutionSettings() {
		this.countingResultCollectorMode = COUNTING_RESULT_COLLECTOR_MODE_DEFAULT;
		this.internalClassesDefinition = null; // this in not initialised with an empty set intentionally because the semantics of null are used!
		this.addUpResultsRecursively = ADD_UP_RESULTS_RECURSIVELY_DEFAULT;
		this.setParentClassLoader(null);
		this.waitForThreadsToFinnish = WAIT_FOR_THREADS_TO_FINNISH_DEFAULT;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public ExecutionSettings clone() {
		ExecutionSettings copy = null;
		
		try {
			copy = (ExecutionSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			// object.clone() cannot fail
			return null;
		}
		// copy fields
		copy.internalClassesDefinition = this.internalClassesDefinition;
		copy.countingResultCollectorMode = this.countingResultCollectorMode;
		copy.parentClassLoader = this.parentClassLoader;
		copy.addUpResultsRecursively = this.addUpResultsRecursively;
		copy.waitForThreadsToFinnish = this.waitForThreadsToFinnish;
		
		return copy;
	}

	/**
	 * Uses {@link #getInternalClassesDefinition()} to decide whether the given 
	 * name is considered an internal class.
	 * @param qualifyingMethodName Name of the class to check.
	 * @return True when the class is internal.
	 */
	public boolean isInternalClass(String qualifyingMethodName) {
		if(this.internalClassesDefinition == null) {
			return true;
		}
		// find public parent class in case of internal class
		int i = qualifyingMethodName.indexOf('$');
		String className = qualifyingMethodName;
		if(i >= 0) {
			className = qualifyingMethodName.substring(0, i);
		}
		i = className.lastIndexOf('.');
		if(i >= 0 && className.indexOf("(") >= 0) {
			className = className.substring(0, i);
		}
		
		for(String s : this.internalClassesDefinition) {
			if(s.charAt(s.length() - 1) == INTERNAL_CLASSES_DEFINITION_WILDCARD_CHAR) {
				final String prefix = s.substring(0, s.length() - 1);
				// prefix matching
				if(qualifyingMethodName.startsWith(prefix)) {
					return true;
				}
			} else {
				// exact matching 
				if(className.equals(s)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * @param internalClassesDefinition The definition of internal classes.
	 * When adding up results when retrieving results recursively, this 
	 * definition allows for adding up only results for classes defined as 
	 * internal. A value of null means all classes are considered internal.
	 * <p>
	 * For each string, specifying a 
	 * '{@value #INTERNAL_CLASSES_DEFINITION_WILDCARD_CHAR}' at the end enables 
	 * prefix matching, 
	 * i.e. all classes with the prefix are matched. If a string specifies a 
	 * class name, non-public/internal classes are also considered internal.
	 * </p>
	 * <p>
	 * Examples:
	 * <code>
	 * <list>
	 * <li>de.uka{@value #INTERNAL_CLASSES_DEFINITION_WILDCARD_CHAR} matches de.ukap.Test, de.uka.ipd, ...</li>
	 * <li>de.uka.Test matches de.uka.Test, de.uka.Test$XXX, de.uka.Test$XXX$YYY, ...</li>
	 * <li>de.uka.Test matches de.uka.Test, but not de.uka.Test.{ENUM Y}</li>
	 * </list>
	 * </code>
	 * </p>
	 */
	public void setInternalClassesDefinition(
			Set<String> internalClassesDefinition) {
		this.internalClassesDefinition = internalClassesDefinition;
	}

	/**
	 * @return The definition of internal classes.
	 * @see #setInternalClassesDefinition(Set)
	 */
	public Set<String> getInternalClassesDefinition() {
		return internalClassesDefinition;
	}

	/**
	 * @return The mode in which the {@link CountingResultCollector} operates.
	 */
	public CountingResultCollectorMode getCountingResultCollectorMode() {
		return countingResultCollectorMode;
	}

	/**
	 * @param countingResultCollectorMode The mode in which the 
	 * {@link CountingResultCollector} operates.
	 */
	public void setCountingResultCollectorMode(
			CountingResultCollectorMode countingResultCollectorMode) {
		this.countingResultCollectorMode = countingResultCollectorMode;
	}

	/**
	 * @return True, when result retrieval adds up counting results recursively.
	 * This means that results for methods that call other methods include the 
	 * counts of these called methods, i.e. of the entire calling tree.
	 * When false, only the counts for operations done in the method itself
	 * - not those by called methods - are returned.
	 */
	public boolean getAddUpResultsRecursively() {
		return addUpResultsRecursively;
	}

	/**
	 * @param addUpResultsRecursively 
	 * When true, result retrieval adds up counting results recursively.
	 * This means that results for methods that call other methods include the 
	 * counts of these called methods, i.e. of the entire calling tree.
	 * When false, only the counts for operations done in the method itself
	 * - not those by called methods - are returned.
	 */
	public void setAddUpResultsRecursively(boolean addUpResultsRecursively) {
		this.addUpResultsRecursively = addUpResultsRecursively;
	}

	/**
	 * This is used for instantiation of classes that are set to execute.
	 * For some applications it may be necessary to use a different ClassLoader 
	 * than the SystemClassLoader. For instance Eclipse plugins each have their 
	 * own ClassLoader which means Class.forName() may not have access to the 
	 * correct classpath.
	 * @param parentClassLoader The {@link InstrumentationClassLoader} that will be used to 
	 * create instances of the classes to execute.
	 */
	public void setParentClassLoader(ClassLoader parentClassLoader) {
		this.parentClassLoader = parentClassLoader;
	}

	/**
	 * @return The {@link InstrumentationClassLoader} set using {@link #setParentClassLoader(ClassLoader)}.
	 */
	public ClassLoader getParentClassLoader() {
		return parentClassLoader;
	}

	/**
	 * @return When true, 
	 * {@link BytecodeCounter#execute(de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor, Object, Object[])}
	 * waits for all threads from instrumented methods to finish before 
	 * returning.
	 */
	public boolean getWaitForThreadsToFinnish() {
		return this.waitForThreadsToFinnish;
	}
	
	/**
	 * @param waitForThreadsToFinnish When true, 
	 * {@link BytecodeCounter#execute(de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor, Object, Object[])}
	 * waits for all threads from instrumented methods to finish before 
	 * returning.
	 */
	public void setWaitForThreadsToFinnish(boolean waitForThreadsToFinnish) {
		this.waitForThreadsToFinnish = waitForThreadsToFinnish;
	}
}
