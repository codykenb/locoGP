package de.uka.ipd.sdq.ByCounter.execution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import de.uka.ipd.sdq.ByCounter.instrumentation.EntityToInstrument;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationContext;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationParameters;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedClass;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedMethod;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentedRegion;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationScopeModeEnum;
import de.uka.ipd.sdq.ByCounter.instrumentation.InstrumentationState;
import de.uka.ipd.sdq.ByCounter.instrumentation.Instrumenter;
import de.uka.ipd.sdq.ByCounter.parsing.InstructionBlockSerialisation;
import de.uka.ipd.sdq.ByCounter.parsing.CallGraph;
import de.uka.ipd.sdq.ByCounter.parsing.CallGraphClassAdapter;
import de.uka.ipd.sdq.ByCounter.parsing.CallGraphMethod;
import de.uka.ipd.sdq.ByCounter.parsing.ClassMethodImplementations;
import de.uka.ipd.sdq.ByCounter.parsing.FindMethodDefinitionsClassAdapter;
import de.uka.ipd.sdq.ByCounter.utils.InvocationResultData;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;

/**
 * Use this class to trigger counting of instructions for your methods.
 * 
 * Bytecode instructions are counted accurately with the exception of 
 * certain special versions of opcodes, as described in the ASM JavaDoc 
 * (http://asm.objectweb.org/asm30/javadoc/user/org/objectweb/asm/Opcodes.html):
 * "[..] some opcodes are automatically handled. For example, the xLOAD 
 * and xSTORE opcodes are automatically replaced by xLOAD_n and xSTORE_n 
 * opcodes when possible. The xLOAD_n and xSTORE_n opcodes are therefore 
 * not defined in this interface. Likewise for LDC, IINC, RET automatically 
 * replaced by LDC_W or LDC2_W when necessary, WIDE, GOTO_W and JSR_W."
 * 
 * To instrument methods of classes, call instrument(...).
 * To execute instrumented code, call execute(...).
 * To access the results use {@link CountingResultCollector} singleton.
 *
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 * TODO investigate whether slash-separated class names should be supported alongside "."-supported class names
 */
public final class BytecodeCounter {

	private static final boolean TRY_TO_FIND_IMPLEMENTATIONS_IN_SUPER = false;

	/**
	 * A logger instance (java.util Logging)
	 */
	private static Logger log;
	
	/**
	 * A flag which is <code>true</code> if class to count was given as bytes.
	 */
	private boolean classAsBytes;
	private boolean classesAsBytes = false; // added bck March 2014 
	
	/**
	 * The class to count as byte array, if <code>classAsBytes</code> is true.
	 * This is null otherwise.
	 */
	private byte[] classBytesToInstrument; 
	
	
	private Map<String, byte[]> classHierarchyBytesToInstrument; // yay! :D 
			
	/**
	 * Descriptors for the constructors that correspond to the 
	 * constructionParameters.
	 * Index is in sync with constructionParameters.
	 */
	private ArrayList<MethodDescriptor> constructionDescriptors = 
		new ArrayList<MethodDescriptor>();
	
	/**
	 * Parameters for class construction that are needed when execute is called 
	 * on a class with no default constructor.
	 * Index is in sync with constructionDescriptors.
	 */
	private ArrayList<Object[]> constructionParameters = 
		new ArrayList<Object[]>();
	
	/**
	 * An array of bytes representing the instrumented class once 
	 * instrument() was called.
	 */
	private byte[] instrumentedClassBytes;
	
	/**
	 * Parameters for instrumentation, can be set by the user.
	 */
	private InstrumentationParameters instrumentationParameters;
	
	/**
	 * Parameters relevant to execution and counting.
	 */
	private ExecutionSettings executionSettings;
	
	/**
	 * Internal results of the instrumentation process.
	 */
	private InstrumentationState instrumentationState;
	
	/**
	 * The callgraph used for 'recursive' instrumentation.
	 */
	private CallGraph callGraph = null;
	

	private List<MethodDescriptor> successFullyInstrumentedMethods;

	/**
	 * ClassLoader that handles instrumented classes.
	 */
	private InstrumentationClassLoader classLoader;
	
	/**
	 * Setup a new BytecodeCounter.
	 */
	public BytecodeCounter() {
		this.instrumentationParameters = new InstrumentationParameters();
		this.executionSettings = new ExecutionSettings();
		this.instrumentationState = new InstrumentationState();
		this.successFullyInstrumentedMethods = new ArrayList<MethodDescriptor>();
		this.classLoader = new InstrumentationClassLoader(null);
		setupLogging();
	}
	
	/**
	 * This undoes the call of setClassToInstrument() and returns
	 * BytecodeCounter into its standard instrumentation mode.
	 */
	public void clearClassFileToInstrument() {
		this.classAsBytes = false;
		this.classBytesToInstrument = null;
	}

	/**
	 * Removes all construction parameters passed by calling 
	 * setConstructionParameters(..);.
	 * This is necessary if you want to call execute() on a method that 
	 * was called before and that was constructed differently. 
	 */
	public void clearConstructionParameters() {
		this.constructionDescriptors.clear();
		this.constructionParameters.clear();
	}
	
	/**
	 * Execute the method specified by methodToExecute using the given parameters.
	 * Static methods can be executed with parameter target equals {@code null}. 
	 * @param methodToExecute A {@link MethodDescriptor} describing the 
	 * specific method that shall be executed.
	 * @param params Parameters that are 
	 * used to execute the method specified by methodToExecute. Use 
	 * <code>new Object[0]</code> for methods that take no parameters.
	 * @return Result and duration of execution. 
	 * @see BytecodeCounter#setExecutionSettings(ExecutionSettings)
	 */
	public synchronized InvocationResultData execute(MethodDescriptor methodToExecute, 
			Object[] params) {
		return this.execute(methodToExecute, instantiate(methodToExecute), params);
	}
	
	/**
	 * Execute the method specified by methodToExecute using the given parameters.
	 * Static methods can be executed with parameter target equals {@code null}. 
	 * @param methodToExecute A {@link MethodDescriptor} describing the 
	 * specific method that shall be executed.
	 * @param target Instance on which the method is executed. 
	 * @param params Parameters that are 
	 * used to execute the method specified by methodToExecute. Use 
	 * <code>new Object[0]</code> for methods that take no parameters.
	 * @return Result and duration of execution. 
	 * @see BytecodeCounter#setExecutionSettings(ExecutionSettings)
	 */
	public synchronized InvocationResultData execute(MethodDescriptor methodToExecute, Object target, 
			Object[] params) {
		int expected = MethodDescriptor.getParametersTypesFromDesc(methodToExecute.getDescriptor()).length;
		int actual = params.length;
		if (expected != actual) {
			throw new IllegalArgumentException("Wrong number of parameters! expected: " + expected + ", actual: " + actual);
		}
		InvocationResultData invocationResult = new InvocationResultData();
		log.info(">>> Executing method on instrumented class");
		// call the method
		List<MethodDescriptor> methodsToCall = new ArrayList<MethodDescriptor>(1);
		methodsToCall.add(methodToExecute);
		List<Object[]> methodCallParams = new ArrayList<Object[]>(1);
		methodCallParams.add(params);
		if(!methodToExecute.getMethodIsStatic() && target==null) {
			log.severe("objInstance is null");
		}
		Class<?> targetClass = null;
		if(target != null) {
			targetClass = target.getClass();
		} else {
			// we have a static method
			try {
				// use the class loader to get the class
				targetClass = this.classLoader.loadClass(methodToExecute.getCanonicalClassName());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		// supply CountingResultCollector with details on how execute was called (for reporting purposes)
		MethodExecutionRecord lastMethodExecutionDetails = new MethodExecutionRecord();
		lastMethodExecutionDetails.canonicalClassName = targetClass.getCanonicalName();
		lastMethodExecutionDetails.methodsCalled = methodsToCall;
		lastMethodExecutionDetails.methodCallParams = methodCallParams;
		lastMethodExecutionDetails.executionSettings = this.executionSettings.clone();
		CountingResultCollector.getInstance().setLastMethodExecutionDetails(lastMethodExecutionDetails);

		// invoke
		invocationResult = MethodInvocationHelper.callMethods(
				log, 
				targetClass,
				target, 
				methodsToCall, 
				methodCallParams);
		if(this.executionSettings.getWaitForThreadsToFinnish()) {
			// wait for everything to finish before returning
			try {
				CountingResultCollector.getInstance().joinSpawnedThreads();
			} catch (InterruptedException e) {
				throw new RuntimeException("Could not wait for spawned threads to finish.", e);
			}
		}
		return invocationResult;
	}

	/**
	 * Use these parameters to influence the way execution and counting of 
	 * instrumentation results are handled.
	 * @return The current execution parameters.
	 */
	public ExecutionSettings getExecutionSettings() {
		return executionSettings;
	}

	/**
	 * Returns the current instrumentation parameters. Use this to change 
	 * the way the instrumenter works. Note that the methods to instrument 
	 * will be overridden by the instrument* methods.
	 * @return the instrumentation parameters. 
	 */
	public InstrumentationParameters getInstrumentationParams() {
		return this.instrumentationParameters;
	}

	/**
	 * Gets a byte[] representing a {@link Class} of the type specified by the 
	 * className attribute of the first MethodDescriptor supplied to instrument(..) 
	 * The class is modified in such a way that the methods specified by 
	 * methodsToInstrument (again see instrument(..)) have been instrumented 
	 * by ByCounter.
	 * This makes the assumption that the class for all methods is the same. 
	 * @return Class as byte[] with instrumented methods if the instrumentation 
	 * succeeded. 'null' otherwise.
	 */
	public byte[] getInstrumentedBytes() {
		if(this.instrumentedClassBytes == null) {
			log.warning("Could not get the instrumented class as bytes. "
					+ "Please make sure to call BytecodeCounter.instrument(..).");
			log.warning("Automatically calling instrument with the existing settings.");
			this.instrument();
		}
		return this.instrumentedClassBytes;
	}
	
	/**
	 * Convenience method for adding an {@link InstrumentedMethod} to
	 * the entityToInstrument collection in {@link #getInstrumentationParams()}.
	 * with a {@link InstrumentedMethod}.
	 * {@link #instrument()} needs to be called seperately.
	 * @param fullMethod Method to fully instrument.
	 */
	public void addEntityToInstrument(MethodDescriptor fullMethod) {
		this.instrumentationParameters.getEntitiesToInstrument().add(new InstrumentedMethod(fullMethod));
	}
	
	public void addEntityToInstrument(String className) {
		this.instrumentationParameters.getEntitiesToInstrument().add(new InstrumentedClass(className));

	}
	
	/**
	 * Convenience method for adding {@link EntityToInstrument} to
	 * the entityToInstrument collection in {@link #getInstrumentationParams()}.
	 * with a {@link InstrumentedMethod}.
	 * {@link #instrument()} needs to be called seperately.
	 * @param entitiesToInstrument Method to fully instrument.
	 */
	public synchronized void addEntityToInstrument(List<EntityToInstrument> entitiesToInstrument) {
		this.instrumentationParameters.getEntitiesToInstrument().addAll(entitiesToInstrument);
	}

	/**
	 * Instrument the methods specified in the 
	 * {@link InstrumentationParameters} of this BytecodeCounter.
	 * The {@link InstrumentationParameters} can be accessed using 
	 * get- or setInstrumentationParameters().
	 * Make sure you set the methodsToInstrument or this call will fail with 
	 * an error.
	 * @return True, if the instrumentation was successful, false otherwise.
	 */
	public synchronized boolean instrument() {
		List<EntityToInstrument> entitiesToInstrument = 
				new LinkedList<EntityToInstrument>(
						this.instrumentationParameters.getEntitiesToInstrument());
		// check the supplied list
		if(entitiesToInstrument == null || entitiesToInstrument.isEmpty()) {
			log.severe("Trying to instrument but no entities to instrument were specified.");
		}
		// reset instrumentation state
		this.instrumentationState = new InstrumentationState();

		// finally do the instrumentation on the classes
		boolean success = true;
		
		// a map that holds the set of all method definitions for each class
		Map<String, ClassMethodImplementations> classMethodDefinitions = 
			new HashMap<String, ClassMethodImplementations>();
		
		// create a set of class names to instrument
		// create an initial set of methods to instrument
		Set<String> classesToInstrument = new HashSet<String>();
		Set<String> classesInstrumentEverything = new HashSet<String>();
		for(EntityToInstrument e : instrumentationParameters.getEntitiesToInstrument()) {
			if(e instanceof InstrumentedClass) {
				String canonicalClassName = ((InstrumentedClass) e).getCanonicalClassName();
				classesToInstrument.add(canonicalClassName);
				classesInstrumentEverything.add(canonicalClassName);
				instrumentationState.getFullyInstrumentedClasses().put(canonicalClassName, e);
			} else {
				for(MethodDescriptor m : e.getMethodsToInstrument()) {
					// add method
					if(!this.instrumentationState.getMethodsToInstrumentCalculated().contains(m)) {
						this.instrumentationState.getMethodsToInstrumentCalculated().add(m);
					}
					// add entity
					addEntityToInstrumentationState(m, e);
					// add class name
					success = findClassesToInstrument(classesToInstrument, classMethodDefinitions, m)
								&& success;
				}
			}
		}

		// for recursive instrumentation we need to preparse the classes
		if(this.instrumentationParameters.getInstrumentRecursively()) {
			// iterate through all selected classes
			CallGraphClassAdapter callGraphAdapter = new CallGraphClassAdapter(
					this.instrumentationParameters.getIgnoredPackagePrefixes());
			this.callGraph = new CallGraph();
			for(String className : classesToInstrument) {
				ClassReader cr;
				if(this.classAsBytes) {
					cr = new ClassReader(this.classBytesToInstrument);
					callGraphAdapter.parseClass(callGraph, cr, null);
				} else {
					try {
						if(this.classesAsBytes) // bck
							cr = new ClassReader(this.classHierarchyBytesToInstrument.get(className));
						else
							cr = new ClassReader(className);
						success = success && callGraphAdapter.parseClass(callGraph, cr, this.classHierarchyBytesToInstrument);
					} catch (IOException e) {
						log.severe("Could not parse class with name '" + className + "'. Skipping.");
						success = false;
					}
				}
			}
			this.instrumentationState.setMethodsToInstrumentCalculated(
					this.selectMethodsFromCallGraph(
							this.instrumentationState.getMethodsToInstrumentCalculated()));
			// since we created the call graph, we may now have to instrument
			// additional classes, so add them to the list here
			for(MethodDescriptor m : instrumentationState.getMethodsToInstrumentCalculated()) {
				success = findClassesToInstrument(classesToInstrument, classMethodDefinitions, m)
				 		&& success;
			}
		}
		// select methods if instrumentation regions have been specified
		{
			List<MethodDescriptor> mtiCalc = instrumentationState.getMethodsToInstrumentCalculated();
			for(EntityToInstrument e : this.instrumentationParameters.getEntitiesToInstrument()) {
				if(e instanceof InstrumentedRegion) {
					final InstrumentedRegion ir = (InstrumentedRegion) e;
					if(!mtiCalc.contains(ir.getStartMethod())) {
						mtiCalc.add(ir.getStartMethod());
					}
					if(!mtiCalc.contains(ir.getStopMethod())) {
						mtiCalc.add(ir.getStopMethod());
					}
				}
			}
		}
		
		log.info("Instrumenting following methods: " + instrumentationState.getMethodsToInstrumentCalculated());
		
    	this.successFullyInstrumentedMethods = new ArrayList<MethodDescriptor>();
    	
    	if(classesToInstrument.isEmpty()) {
    		log.severe("classesToInstrument is empty: why?");
    		return false;
    	}
		// iterate through all selected classes
		for(String className : classesToInstrument) {
			InstrumentationScopeModeEnum oldV = instrumentationParameters.getInstrumentationScopeOverrideClassLevel();
			if(classesInstrumentEverything.contains(classesToInstrument)) {
				instrumentationParameters.setInstrumentationScopeOverrideClassLevel(InstrumentationScopeModeEnum.InstrumentEverything);
			}
			success = instrumentSingleClass(
					instrumentationState.getMethodsToInstrumentCalculated(), 
					className) && success;
			instrumentationParameters.setInstrumentationScopeOverrideClassLevel(oldV);
		}
		this.printInstrumentationSummary();

		return success;
	}

	/**
	 * Adds to {@link InstrumentationContext#getEntitiesToInstrument()}.
	 * @param m Method the entity is relevant to.
	 * @param e {@link EntityToInstrument} for the method.
	 */
	private void addEntityToInstrumentationState(MethodDescriptor m, EntityToInstrument e) {
		List<EntityToInstrument> entityListForMethod = this.instrumentationState.getEntitiesToInstrumentByMethod().get(m);
		if(entityListForMethod == null) {
			entityListForMethod = new LinkedList<EntityToInstrument>();
			this.instrumentationState.getEntitiesToInstrumentByMethod().put(m.getCanonicalMethodName(), entityListForMethod);
		}
		entityListForMethod.add(e);
	}
	
	/**
	 * @param classesToInstrument A list of classes that is to be instrumented.
	 * This is modified by this method
	 * @param classMethodDefinitions {@link ClassMethodImplementations} for all
	 * classes considered to this point.
	 * @param methodToAnalyse Method to analyse. If the methods implementing class is not in
	 * the list of classes to instrument, it will be added.
	 * @return True, if the method has an implementation. False otherwise.
	 */
	private boolean findClassesToInstrument(Set<String> classesToInstrument,
			Map<String, ClassMethodImplementations> classMethodDefinitions, 
			MethodDescriptor methodToAnalyse) {
		FindMethodDefinitionsClassAdapter findMethodsCA = 
			new FindMethodDefinitionsClassAdapter(
					this.instrumentationParameters.getIgnoredPackagePrefixes(), this.classHierarchyBytesToInstrument);
		boolean descent;
		MethodDescriptor currentM = methodToAnalyse;
		do {
			descent = false;
			// make sure that the method has an implementation in the class
			String canonicalClassName = currentM.getCanonicalClassName();
			ClassMethodImplementations methods = classMethodDefinitions.get(canonicalClassName);
			if(methods == null) {
				methods = new ClassMethodImplementations();
				if(!findMethodsCA.parseClass(methods, canonicalClassName)) {
					return false;
				}
				classMethodDefinitions.put(canonicalClassName, methods);
			}
			// if the method is not defined, see if the super class has an 
			// implementation.
			if(TRY_TO_FIND_IMPLEMENTATIONS_IN_SUPER && !methods.getMethods().contains(currentM.getMethodSignature())) {
				if(currentM.getCanonicalClassName().equals(Object.class.getCanonicalName())) {
					log.severe("Could not find an implementation of the following method: " + currentM);
					return false;
				} else {
					if(methods.getSuperClass() == null) {
						log.severe("Could not find an implementation of the following method: " + currentM);
						log.severe("Superclass is null");
						return false;
					}
					String superCanonicalClassName = methods.getSuperClass().replace('/', '.');
					// if there already is a methoddescriptor for the superclasses method, skip it
					if(MethodDescriptor.findMethodInList(
							instrumentationState.getMethodsToInstrumentCalculated(), 
							superCanonicalClassName, 
							currentM.getSimpleClassName(), 
							currentM.getDescriptor()) != -1) {
						continue;
					} else {
						String[] str = 
							MethodDescriptor.canonicalClassNameToPackageAndSimpleName(
									superCanonicalClassName);
						if(!superCanonicalClassName.contains(".") || str[1].equals("Object")) {
							str[0].hashCode();//DEBUG
						}
						currentM.setPackageName(str[0]);
						currentM.setSimpleClassName(str[1]);
						descent = true;
						continue;
					}
				}
			} else {
				if(!classesToInstrument.contains(canonicalClassName)) {
					classesToInstrument.add(canonicalClassName);
				}
			}
		} while(descent);
		return true;
	}

	/**
	 * If instrumentRecursivly is set in the instrumentation parameters,
	 * we need to instrument methods called from the methods to instrument.
	 * This method uses the {@link #callGraph} to select these methods.
	 * @param rootMethods Descriptors of the methods that start the search in 
	 * the call graph. 
	 * @return Calculated {@link Set} of methods to instrument. 
	 */
	private List<MethodDescriptor> selectMethodsFromCallGraph(List<MethodDescriptor> rootMethods) {
		// copy all methods specified in instrumentation parameters
		List<MethodDescriptor> results = 
				new ArrayList<MethodDescriptor>(rootMethods);//initial copy

		log.info("Selecting methods from the call graph.");
		//TODO add duplicate check here (or earlier)
		// now traverse the callgraph to add the recursive methods
		for(MethodDescriptor md : rootMethods) {
			// find the appropriate node in the callgraph
			final CallGraphMethod m = this.callGraph.findMethod(md);
			EntityToInstrument e = this.instrumentationState.getEntitiesToInstrumentByMethod().get(md.getCanonicalMethodName()).get(0);
			if(m == null) {
				continue;
			}
			// recursively add the methods child methods
			selectMethodsFromCallGraph_forMethod(
					results,
					m);
		}
		return new ArrayList<MethodDescriptor>(results);
	}
	
	/**
	 * @see {@link #selectMethodsFromCallGraph()}
	 * Used to traverse the call graph.
	 * @param methodsList The list that will be extended by the methods called 
	 * by method m if they are not already in the list.
	 * methodsAlreadySelected.
	 * @param root Method which children are considered for adding.
	 */
	private void selectMethodsFromCallGraph_forMethod(
			final Collection<MethodDescriptor> methodsList,
			final CallGraphMethod root) {
		
		Collection<CallGraphMethod> roots = new LinkedList<CallGraphMethod>();
		roots.add(root);
		HashSet<String> methodsDone = new HashSet<String>();
		
		while(!roots.isEmpty()) {
			Collection<CallGraphMethod> newRoots = new LinkedList<CallGraphMethod>();
			for(final CallGraphMethod m : roots) {
				// look at all methods called by m
				for(final CallGraphMethod child : m.getChildMethods()) {
					String id = child.getOwner()+child.getName()+child.getDesc();
					if(methodsDone.contains(id)) {
						continue;
					}
					
					String canonicalClassName = child.getOwner().replace('/', '.');
					final int methodIndex = 
						MethodDescriptor.findMethodInList(
								methodsList,
							canonicalClassName,
							child.getName(), 
							child.getDesc());
		
					if(instrumentationParameters.isClassExcluded(canonicalClassName) 
							|| methodIndex >= 0) {
						// do not instrument method; it is in an excluded package
						// OR method is already in list; skip
					} else {
						final MethodDescriptor descriptor = 
							MethodDescriptor._constructMethodDescriptorFromASM(
									child.getOwner(), child.getName(), child.getDesc());
						// add the method to the list
						methodsList.add(descriptor);
						Map<String, List<EntityToInstrument>> entitiesToInstrumentByMethod = this.instrumentationState.getEntitiesToInstrumentByMethod();
						List<EntityToInstrument> list = entitiesToInstrumentByMethod.get(m.getMethodDescriptor().getCanonicalMethodName());
						EntityToInstrument e = list.get(0);
						addEntityToInstrumentationState(descriptor, e);
						
					}
					// Now select from all childs.
					// This is necessary even with if this method was not added, 
					// because other
					// methods deeper in the call tree can still be relevant.
					newRoots.add(child);
					methodsDone.add(id);
				}
			}
			roots = newRoots;
		}
	}

	/**
	 * Instrument the specified methods with ByCounter instructions for 
	 * counting, reporting etc.
	 * @param methodsToInstrument A {@link List} of {@link MethodDescriptor}s 
	 * where each MethodDescriptor represent a method that will be 
	 * instrumented.
	 * @param className Name of the (single) class for which methods shall
	 * be instrumented.
	 * @return True, if the instrumentation was successful, false otherwise.
	 */
	private synchronized boolean instrumentSingleClass(
			List<MethodDescriptor> methodsToInstrument, 
			String className) {
		// instrument the method
		Instrumenter instr = null;
		// make sure parameters are set
		log.fine(this.instrumentationParameters.toString());
		
		boolean success = false;

		try {
			if(this.classesAsBytes){
				log.fine("Getting instrumenter over class bytes from memory compile (bck)");
				instr = new Instrumenter(this.classHierarchyBytesToInstrument.get(className), 
						this.instrumentationParameters,
						this.instrumentationState);
			}else if(this.classAsBytes) {
				log.fine("Getting instrumenter over class bytes");
				instr = new Instrumenter(this.classBytesToInstrument, 
						this.instrumentationParameters,
						this.instrumentationState);
			} else {
				log.fine("Getting instrumenter over class name");
				instr = new Instrumenter(className, 
						this.instrumentationParameters,
						this.instrumentationState);
			}
//			long uninstrBytesize = instr.getUninstrumentedBytesize();
			log.info(className+" instrumenting: TracingCharacterisationHook " +
					"registration used to be here..."); 
			//TODO reinstate instr.registerCharacterisationHook(new TracingCharacterisationHook());
			
			if(this.instrumentationParameters.getInstrumentationScopeOverrideClassLevel()
					!= InstrumentationScopeModeEnum.InstrumentEverything) {
				log.fine("Instrumenting "+methodsToInstrument.size()+" methods," +
						"checking how many of them are in class "+className);
			}
			//System.out.println("Instrumenting " +className);
			success = instr.instrument();
			
			if(success == false) {
				log.severe("Not all specified methods could be instrumented.");
			}
			this.successFullyInstrumentedMethods.addAll(instr.getInstrumentationState().getSuccessFullyInstrumentedMethods());
			log.fine("Getting instrumented bytes");
			byte[] b = instr.getInstrumentedBytes();
			// save entitiesToInstrument
			for(EntityToInstrument eti : this.instrumentationParameters.getEntitiesToInstrument()) {
				this.instrumentationState.getInstrumentationContext().getEntitiesToInstrument().put(
						eti.getId(), eti);
			}
//			long instrBytesize = b.length;
			
			this.instrumentedClassBytes = b;

			// write classes to disk ('bin_instrumented' or as specified) 
			// if specified in the instrumentation parameters
			if(this.instrumentationParameters.getWriteClassesToDisk()) {
				int split = className.lastIndexOf('.');
				if(split < 0) {
					// no package
					writeClassFile("", className, b);
				} else {
					final String packageN = className.substring(0, split);
					final String simpleClassN = className.substring(split+1);
					writeClassFile(packageN, simpleClassN, b);
				}
			}

//			TODO fix the error with uninstrumented bytesize (as well as the problem the re-outputting uninstrumented bytes yields a non-executable something, i.e. not a class at all
//			log.fine("\n\t\t("+instrBytesize+" instrumented, " +
//					""+uninstrBytesize+" uninstrumented)");
 
			this.classLoader.updateClassInClassPool(className, b);
			
			// write context (is this even needed? bck) 
			// yes,
			saveContext();
			
		} catch (ClassNotFoundException e3) {
			 e3.printStackTrace();
			log.severe("Could not find the specified class");
		}
		return success;
	}
	
	
	private void saveContext(){
		try {		
			File file = new File(InstrumentationContext.FILE_SERIALISATION_DEFAULT_NAME);
			log.info("Writing ByCounter instrumentation context to " + file.getAbsolutePath());
			InstrumentationContext.serialise(this.instrumentationState.getInstrumentationContext(), file);
		} catch (IOException e) {
			log.severe("Failed to serialise basic or range block definitions. " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Summarise for the user what methods have been instrumented successfully.
	 */
	private void printInstrumentationSummary() {
		StringBuilder msg = new StringBuilder();
		msg.append("[Instrumentation Summary]\n");
		if(this.successFullyInstrumentedMethods.size() == 0) {
			msg.append("No methods have been instrumented.\n");
		} else {
			msg.append("The following " 
					+ this.successFullyInstrumentedMethods.size() 
					+ " methods have been instrumented:\n");
			for(MethodDescriptor m : this.successFullyInstrumentedMethods) {
				msg.append(m.toString() + "\n");
			}
		}

		// Load the instrumentation context that was saved before.
		InstrumentationContext iContext = null;
		File file = new File(InstrumentationContext.FILE_SERIALISATION_DEFAULT_NAME);
		try {
			//iContext = InstrumentationContext.deserialise(file); (bck)
			iContext = this.instrumentationState.getInstrumentationContext();

			// print all basic blocks
			InstructionBlockSerialisation loaded;

			
			loaded = iContext.getLabelBlocks();
			if(loaded != null) {
				log.info("Label blocks:");
				loaded.printInstructionBlocks(log);
			}
			if(this.instrumentationParameters.getUseBasicBlocks()) {
				loaded = iContext.getBasicBlocks();
				if(loaded != null) {
					log.info("Basic blocks:");
					loaded.printInstructionBlocks(log);
					// print all range blocks
					if(this.instrumentationParameters.hasMethodsWithCodeAreas()
							|| this.instrumentationParameters.hasInstrumentationRegions()) {
						loaded = iContext.getRangeBlocks();
						if(loaded != null) {
							log.info("Range blocks:");
							loaded.printInstructionBlocks(log);
						} else {
							log.info("No range blocks.");
						}
					}
				} else {
					log.info("No basic blocks.");
				}
			}
			
		} catch (Exception e) {
			String emsg = "Failed to load instrumentation context. ";
			log.severe(emsg + e.getMessage());
			throw new RuntimeException(emsg, e);
		}
		
		log.info(msg.toString());
//		log.fine("fine");
//		log.finer("finer");
//		log.finest("finest");
	}

	/**
	 * Specifies (changes) the class to instrument to be the class specified 
	 * as the byte array.
	 * <p>
	 * To stop using the here specified class and use the standard 
	 * instrumentation mode again, call clearClassFileToInstrument().
	 * </p>
	 * @param classToInstrument A byte array representing the class to instrument.
	 */
	public synchronized void setClassToInstrument(byte[] classToInstrument) {
		this.classBytesToInstrument = classToInstrument;
		this.classAsBytes = true;
	}
	
	public synchronized void setClassesToInstrument(Map<String, byte[]> map) {
		this.classHierarchyBytesToInstrument = map;
		this.classesAsBytes = true;
	}

	/**
	 * Parameters for class construction are needed when execute is called 
	 * on a class with no default constructor.
	 * Use this method to supply these construction parameters.
	 * @param constructor A {@link MethodDescriptor} for the constructor that 
	 * shall be used for a following execute() call on the same class.
	 * Use MethodDescriptor.forConstructor() to describe constructors.
	 * @param parameters
	 */
	public void setConstructionParameters(MethodDescriptor constructor,
			Object[] parameters) {
		assert this.constructionDescriptors.size() == this.constructionParameters.size();
		if(constructor == null) {
			throw new IllegalArgumentException("setConstructionParameters: You need to supply a MethodDescriptor that is not null!");
		}
		if(parameters == null || parameters.length == 0) {
			throw new IllegalArgumentException("setConstructionParameters: You need to supply a parameters!");
		}
		this.constructionDescriptors.add(constructor);
		this.constructionParameters.add(parameters);
	}

	/**
	 * Use these parameters to influence the way execution and counting of 
	 * instrumentation results are handled.
	 * @param executionSettings The current execution parameters to use.
	 */
	public void setExecutionSettings(ExecutionSettings executionSettings) {
		if(executionSettings != null) {
			this.executionSettings = executionSettings;
		} else {
			throw new IllegalArgumentException("ExecutionSettings must not be set to null.");
		}
	}

	/**
	 * Sets the parameters for instrumentation.
	 * @param params An {@link InstrumentationParameters} object with
	 * all properties for instrumentation set.
	 */
	public void setInstrumentationParams(InstrumentationParameters params) {
		if(params != null) {
			this.instrumentationParameters = params;
		} else {
			throw new IllegalArgumentException("InstrumentationParameters must not be set to null.");
		}
	}

	/**
	 * Sets up logging
	 */
	private void setupLogging() {
		log = Logger.getLogger(this.getClass().getCanonicalName());
		log.setLevel(Level.FINEST);
	}

	/**
	 * Writes .class file given as byte[].
	 * @param packageName Package name of the class.
	 * @param className Simple class name used to determine where to save the class.
	 * @param b Class as byte[] to write.
	 */
	private synchronized void writeClassFile(
		String packageName, 
		String className, byte[] b) {
		FileOutputStream fileOut = null;
		String classPath = packageName.replace('.', File.separatorChar);
		String path = (
				this.instrumentationParameters.getWriteClassesToDiskDirectory().getAbsolutePath() 
				+ File.separatorChar + classPath
				+ File.separatorChar);
		File directory = new File (path);
		// make sure the directories exist
		if(!directory.exists() && !directory.mkdirs()) {
			log.severe("Could not create directory for instrumented class files.");
		} else {
			try {
				try {
					File f = new File(directory, className + ".class");
					fileOut = new FileOutputStream(f);
//					log.info("Writing "+packageName+"."+className+" to "+fileOut.getFD().toString());//FileDesciptor does not have a usable toString method
					log.info("Writing "+packageName+"."+className+" to "+f.getAbsolutePath());
					fileOut.write(b);
				} catch (FileNotFoundException e) {
					log.severe("Could not write class file to " + path + "." + e.getMessage());
					e.printStackTrace();
				} finally {
					if(fileOut != null) {
						fileOut.close();
					}
				}
			} catch (IOException e) {
				log.severe("Could not write class file to " + path + "." + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/**Creates an instance of the class which contains the provided method.
	 * @param methodToExecute Method (which will later be executed on the created instance).
	 * @return Class instance.
	 */
	public Object instantiate(MethodDescriptor methodToExecute) {
		log.fine("Instantiating class for method " + methodToExecute);
		// load the context, basic/range block serialisations in the result collector
		//CountingResultCollector.getInstance().instrumentationContext = InstrumentationContext.loadFromDefaultPath();
		CountingResultCollector.getInstance().instrumentationContext = this.instrumentationState.getInstrumentationContext();
		
		if(this.executionSettings.getParentClassLoader() != null) {
			this.classLoader.setParentClassLoader(this.executionSettings.getParentClassLoader());
		}

		// create a class instance
		Object objInstance = null;	// class instance
		try {
			// Load the class from the bytecode
			log.fine("+ Getting class from class pool.");
			Class<?> classToExecute = this.classLoader.loadClass(methodToExecute.getCanonicalClassName());
			
			if(!methodToExecute.getMethodIsStatic()) {

				// supply CountingResultCollector with details on how execute was called (for reporting purposes)
				MethodExecutionRecord lastMethodExecutionDetails = new MethodExecutionRecord();
				lastMethodExecutionDetails.executionSettings = this.executionSettings.clone();
				CountingResultCollector.getInstance().setLastMethodExecutionDetails(lastMethodExecutionDetails);
				
				if(methodToExecute.isConstructor()) {
					// look for constructors for the class
					for(MethodDescriptor constD : this.constructionDescriptors) {
						if (constD.getCanonicalClassName().equals(methodToExecute.getCanonicalClassName())) {
							// we found a matching constructor
							// now use the parameters to construct the instance
							Constructor<?>[] constructors = classToExecute.getDeclaredConstructors();
							for(int i = 0; i < constructors.length; i++ ) {
								Constructor<?> c = constructors[i];
								if((new MethodDescriptor(c)).getDescriptor().equals(constD.getDescriptor())) {
									try {
										objInstance = c.newInstance(this.constructionParameters.get(i));
									} catch (Exception e) {
										throw new RuntimeException("Could not call constructor with the given arguments.", e);
									}
									break;
								}
							}
							break;
						}
					}
				}
	
				// instantiate the class; this only works for Classes without 
				// constructors or parameterless constructors
				if(objInstance == null) {
					try{
						objInstance = classToExecute.newInstance();
					} catch (InstantiationException e) {
						throw new RuntimeException("Could not instantiate class. " 
								+ "Please make sure that "
								+ "valid construction parameters are available.");
					} catch (IllegalAccessException e2) {
						throw new RuntimeException("Access denied to create a " 
								+ "new instance of the class to execute.", e2);
					}
				}
				log.fine("Instantiating class FINISHED.");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return objInstance;
	}

	
}
