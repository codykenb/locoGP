package de.uka.ipd.sdq.ByCounter.instrumentation;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import de.uka.ipd.sdq.ByCounter.execution.CountingMode;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;
import de.uka.ipd.sdq.ByCounter.utils.PathMapper;

/**
 * The instrumentor class is the central class to apply an 
 * instrumentation to a targeted class and receive the results.

 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
public final class Instrumenter {
	
	private ClassAdapter classAdapter = null;
	
	private String classCanonicalName;
	
	private ClassReader classReader = null;

	private ClassWriter classWriter = null;
	
	private boolean instrumentationFinished;
	
	private InstrumentationParameters instrumentationParameters;
	
	private InstrumentationState instrumentationState;

	private byte[] instrumentedBytes;
	
	private Logger log;
	
	private long timestampAfterReaderAccept_3;
	
	private long timestampBeforeASMSetup_1;
	
	private long timestampBeforeReaderAccept_2;
	
	private long timestampBeforeReaderInitialisation_0;

	private long timestampInstrumenterInitialisation;
	
	private byte[] uninstrumentedBytes;
	
	/**
	 * Configuration for debugging settings.
	 */
	private static class DebugOptions {
		/**
		 * Prints the complete list of byte instructions of all methods in the 
		 * instrumented class.
		 */
		public static final boolean traceClass = false;
		
		/**
		 * Prints the stack content types before the execution of each 
		 * instruction etc. if useCheckClassAdapter is also true.
		 */
		public static boolean printCheckClassAdapterOutput = false;
		
		/**
		 * Uses the {@link CheckClassAdapter} in the instrumentation chain to
		 * look for errors.
		 */
		public static boolean useCheckClassAdapter = false;
	}


	
	/**
	 * Creates an <code>Instrumenter</code> for the specified class.
	 * @param classAsBytes The class that holds the methods that are 
	 * to be instrumented as byte array.
	 * @param parameters {@link InstrumentationParameters} instance that specifies how and what to instrument.
	 */
	public Instrumenter(
			byte[] classAsBytes, 
			InstrumentationParameters parameters,
			InstrumentationState state) {
		this.instrumentationParameters = parameters;
		this.instrumentationState = state;
		this.classReader = new ClassReader(classAsBytes);
		this.classCanonicalName = this.classReader.getClassName();

		this.log = Logger.getLogger(this.getClass().getCanonicalName());
		this.initialize();	// needs class name
	
		// Setup instrumenting.
		this.timestampBeforeASMSetup_1 = System.nanoTime();
		setupASM();
	}

	/** 
	 * Creates an <code>Instrumenter</code> for the specified class.
	 * @param className The name of the class holding the methods that shall be instrumented.
	 * Needs to be fully qualified as this is used to find the correct class.
	 * @param params {@link InstrumentationParameters} instance that specifies how and what to instrument.
	 * @param state {@link InstrumentationState} for this Instrumenter.
	 * @throws ClassNotFoundException If the specified name cannot be resolved, this exception is thrown.
	 * Check your className if you get this exception.
	 */
	public Instrumenter(
			String className, 
			InstrumentationParameters params,
			InstrumentationState state
			) throws ClassNotFoundException {
		this.instrumentationState = state;
		this.instrumentationParameters = params;
		this.log = Logger.getLogger(this.getClass().getCanonicalName());	// the log may be needed before initialize
		try {
			this.classReader = new ClassReader(className);// try to get a reader for the given classname		
		} catch (IOException e) {
			// Note that the log is not initialised at this point!
			this.log.severe("Could not load class with the name '" +
					className.toString() + "'.");
			throw new ClassNotFoundException();
		}
		this.classCanonicalName = this.classReader.getClassName();
		
		this.initialize();	// needs class name
		
		// Setup instrumenting.
		this.timestampBeforeASMSetup_1 = System.nanoTime();
		setupASM();
	}

	/** Simple getter
	 * @return class adapter used by this instrumenter
	 */
	public ClassAdapter getClassAdapter() {
		return this.classAdapter;
	}

	/** Simple getter
	 * @return the canonical name of the class for which this instrumenter is responsible
	 */
	public String getClassCanonicalName() {
		return this.classCanonicalName;
	}

	/**
	 * Gets the name of the instrumented class.
	 * @return The internal name of the class that is instrumented.
	 * The internal name of a class is its fully qualified name (as returned by Class.getName(), where '.' are replaced by '/'.) 
	 */
	public String getClassName() {
		return this.classReader.getClassName();
	}

	/** Simple getter
	 * @return the class reader instance
	 */
	public ClassReader getClassReader() {
		return this.classReader;
	}

	/** Simple getter
	 * @return the class writer instance
	 */
	public ClassWriter getClassWriter() {
		return this.classWriter;
	}

	/** Simple getter
	 * @return the instrumentationParameters
	 */
	public InstrumentationParameters getInstrumentationParameters() {
		return this.instrumentationParameters;
	}
	
	/**
	 * @return The {@link InstrumentationState}.
	 */
	public InstrumentationState getInstrumentationState() {
		return this.instrumentationState;
	}

	/**
	 * Get the instrumented bytecode (before that, instrument if needed)
	 * @return A byte array containing the modified bytecode.
	 */
	public synchronized byte[] getInstrumentedBytes() {
		this.log.fine("getInstrumentedBytes called");
		if(!this.instrumentationFinished){
			this.log.severe("Trying to get bytes even though instrumentation was not yet done");
		}
		return this.instrumentedBytes;
	}
	
	/** 
	 * @return If already instrumented, return the bytesize of the instrumentation result.
	 * Otherwise, return -1.
	 */
	public long getInstrumentedBytesize() {
		if(this.instrumentedBytes!=null){
			return this.instrumentedBytes.length;
		}
		return -1L;
	}
		
	/** Return the time measurements and bytesizes obtained during characterisation.
	 * @return time measurements and bytesizes in an array - see source code for details.
	 */
	public long[] getMeasurements(){
		return new long[]{
				this.timestampInstrumenterInitialisation,
				this.timestampBeforeReaderInitialisation_0, 
				this.timestampBeforeASMSetup_1, 
				this.timestampBeforeReaderAccept_2, 
				this.timestampAfterReaderAccept_3, 
				this.getUninstrumentedBytesize(),
				this.getInstrumentedBytesize()};
	}
	
	public long getTimestampAfterReaderAccept_3() {
		return this.timestampAfterReaderAccept_3;
	}

	public long getTimestampBeforeASMSetup_1() {
		return this.timestampBeforeASMSetup_1;
	}

	public long getTimestampBeforeReaderAccept_2() {
		return this.timestampBeforeReaderAccept_2;
	}

	public long getTimestampBeforeReaderInitialisation_0() {
		return this.timestampBeforeReaderInitialisation_0;
	}

	public long getTimestampInstrumenterInitialisation() {
		return this.timestampInstrumenterInitialisation;
	}

	public byte[] getUninstrumentedBytes() {
		return this.uninstrumentedBytes;
	}

	/** if uninstrumentedBytes field is not null, return it size. Otherwise,
	 * return -1.
	 * @return see method description above.
	 */
	public long getUninstrumentedBytesize() {
		if(this.uninstrumentedBytes!=null){
			return this.uninstrumentedBytes.length;
		}
		return -1L;
	}

	/** 
	 * Initializes the instrumenters fields used for the instrumentation process
	 * and does some validity checks on the given {@link InstrumentationParameters}.
	 */
	private void initialize() {
		this.classAdapter = null;
		// do not set classReader to null - it is used already
		this.classWriter = null;
		this.timestampInstrumenterInitialisation = System.nanoTime(); //FIXME ???
		this.timestampAfterReaderAccept_3 = -1;
		this.timestampBeforeASMSetup_1 = -1;
		this.timestampBeforeReaderAccept_2 = -1;
		this.instrumentationFinished = false;
		this.instrumentedBytes = null;
		this.uninstrumentedBytes = null;
		this.timestampBeforeReaderInitialisation_0 = System.nanoTime();
		if(this.instrumentationParameters.getInstrumentationScopeOverrideClassLevel()
				!= InstrumentationScopeModeEnum.InstrumentEverything) {
			if(this.instrumentationState.getMethodsToInstrumentCalculated()==null){
				this.log.severe("Instrumenter has got no methods to instrument " +
						"(methodsToInstrument instance is null");
			} else {
				// count the methods for this specific class
				int nr = 0; 
				for(int i = 0; i < this.instrumentationState.getMethodsToInstrumentCalculated().size(); i++) {
					if(this.instrumentationState.getMethodsToInstrumentCalculated().get(i).
							getCanonicalClassName().equals(this.getClassCanonicalName().replace('/', '.'))) {
						nr++;
					}
				}
				if(nr==0){
					this.log.severe("Instrumenter got no methods to instrument " +
					"(methodsToInstrument size is zero");
				} else {
					this.log.fine("Instrumenter has "+nr+" methods to instrument");
				}
			}
		}
	}

	/**
	 * Instrument bytecode using ASM. To get the results, call
	 * getInstrumentedBytes().
	 * @return True, when the instrumentation of all specified methods
	 * was successful, false otherwise.
	 */
	public synchronized boolean instrument() {
		final boolean instrumentEveryThing = 
			(this.instrumentationParameters.getInstrumentationScopeOverrideClassLevel() 
			== InstrumentationScopeModeEnum.InstrumentEverything);
		
		// check the parameter setup
		this.instrumentationParameters.verify();
		
		MethodCountClassAdapter mcca = ((MethodCountClassAdapter) this.classAdapter); 
		
		// no methods instrumented yet; set that status
		if(!instrumentEveryThing) {
			mcca.resetMethodStatusTo(false);
		}
		
		this.timestampBeforeReaderAccept_2 = System.nanoTime();
		
		// run the classes through the instrumentation process
		this.classReader.accept(mcca, ClassReader.EXPAND_FRAMES);
		
		System.gc();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// do a validity check on the resulting class
		if(DebugOptions.useCheckClassAdapter) {
			CheckClassAdapter.verify(new ClassReader(this.classWriter.toByteArray()), 
					DebugOptions.useCheckClassAdapter && DebugOptions.printCheckClassAdapterOutput, 
					new PrintWriter(System.out));
		}

		this.timestampAfterReaderAccept_3 = System.nanoTime();
		this.instrumentedBytes = this.classWriter.toByteArray();
		this.instrumentationFinished = true;
		IClassFileSize classFileSize = new IClassFileSize();
		
		log.info("Uninstrumented/instrumented class file size: " 
				+ classFileSize.getOriginalClassfileSize(this.classCanonicalName) + " bytes/" 
				+ this.instrumentedBytes.length + " bytes");
		
		// check whether all methods could be instrumented,
		// if not this is very likely an error.
		boolean retValue=true;
		String canonicalClassName;
		String thisCanonicalClassName;
		MethodDescriptor methDesc;
		if(!instrumentEveryThing) {
			for(int i = 0; i < mcca.methodInstrumentationStatus.length; i++) {
				methDesc = this.instrumentationState.getMethodsToInstrumentCalculated().get(i);
				canonicalClassName = methDesc.getCanonicalClassName();
				thisCanonicalClassName = this.getClassCanonicalName().replace('/', '.');
				if(!canonicalClassName.equals(thisCanonicalClassName)) {
					// this method is from another class; skip
					this.log.fine("At this point (while instrumenting "+
							thisCanonicalClassName+") nothing to do with method "+
							methDesc.getSimpleMethodName()+" in class "+canonicalClassName);
					continue;
				}
				if(mcca.methodInstrumentationStatus[i] == false) {
					//TODO Martin: look in super-classes
					String message = "FAILURE, Method not found (check whether " +
							"object-typed parameters are given fully, i.e. including package name!):\n"+ 
							this.instrumentationState.getMethodsToInstrumentCalculated().get(i).toString_Linebreaks();
					this.log.severe(message);
					
					//TODO MK solution 1: parse superclasses (solution 2: find all classes in dir, parse them --> see project MethodByteSizer)
					try {
						MethodDescriptor md = this.instrumentationState.getMethodsToInstrumentCalculated().get(i);
						MethodDescriptor superClassMd;
						String canClassName = md.getCanonicalClassName();
						Class<?> clazz = Class.forName(canClassName);
						if(clazz==null){
							log.severe("Cannot initialise class from class name "+canClassName);
						}
						boolean methodDeclaringSuperClassFound = false;
						boolean topClassReached = false;
						Class<?> currentSuperClass = clazz;
						while(!methodDeclaringSuperClassFound && !topClassReached ){
							currentSuperClass = currentSuperClass.getSuperclass();
							log.info("Currently considered superclass: "+currentSuperClass);
							if(currentSuperClass.getCanonicalName().equals("java.lang.Object")){
								topClassReached = true;
								break;
							}
							Method[] declaredMethods = currentSuperClass.getSuperclass().getDeclaredMethods();
							for(Method declMethod : declaredMethods){
//								if(declMethod.get)
								superClassMd = new MethodDescriptor(declMethod);
								boolean compRes = superClassMd.equals(md);
								if(compRes){
									log.info("Found method implementation for "+md+
											"in superclass: "+superClassMd);
									methodDeclaringSuperClassFound = true;
								}else{
									log.info("\n"+md+" \n"+"not a submethod of \n"+superClassMd);
								}
							}
						}
					} catch (ClassNotFoundException e) {
						log.severe("Could not find class.");
						throw new RuntimeException(e);
					}
					javax.swing.JOptionPane.showMessageDialog(null, message);
					retValue = false;
					throw new RuntimeException(message);
				}else{
					String message = "SUCCESS with instrumenting \n"+
					this.instrumentationState.getMethodsToInstrumentCalculated().get(i).toString_Linebreaks();
					// set properties for the instrumentation context
					if(!this.instrumentationParameters.hasInstrumentationRegions()) {
						this.instrumentationState.getInstrumentationContext().setCountingMode(CountingMode.Default);
					} else {
						this.instrumentationState.getInstrumentationContext().setCountingMode(CountingMode.Regions);
					}
					this.instrumentationState.getInstrumentationContext().setQueryActiveEntitySupported(
							this.instrumentationParameters.getProvideOnlineSectionActiveUpdates());
					this.log.info(message);
	//				String s = javax.swing.JOptionPane.showInputDialog("OK: "+message);
				}
			}
		}
		// all methods were instrumented, return success.
		return retValue;
	}

	/**
	 * 
	 * @return When true, the instrumentation process has finished.
	 */
	public boolean isInstrumentationFinished() {
		return this.instrumentationFinished;
	}

	/**
	 * Registers a {@link ICharacterisationHook}.
	 * @see ICharacterisationHook
	 * @param hook
	 */
	public void registerCharacterisationHook(ICharacterisationHook hook) {
    	// register hooks
		MethodCountClassAdapter methodCClassAdapter = (MethodCountClassAdapter)this.classAdapter;
	    if(!this.classAdapter.getClass().getName().equals(MethodCountClassAdapter.class.getName())) {
			this.log.severe("Error: Only dynamic instrumentation supports hooks.");
		} else {
			methodCClassAdapter.registerCharacterisationHook(hook);
		}
	}

	/**
	 * @param instrumentationParameters the instrumentationParameters to set
	 */
	public void setInstrumentationParameters(
			InstrumentationParameters instrumentationParameters) {
		this.instrumentationParameters = instrumentationParameters;
	}
				

	/**
	 * Constructs the ASM visitor chain.
	 */
	@SuppressWarnings("deprecation")
	private void setupASM() {
		// report which methods are being instrumented
		if(this.instrumentationParameters.getInstrumentationScopeOverrideClassLevel()
				!= InstrumentationScopeModeEnum.InstrumentEverything) {
			for(MethodDescriptor methodd 
					: this.instrumentationState.getMethodsToInstrumentCalculated()) {
				if(methodd.getCanonicalClassName().equals(this.getClassCanonicalName().replace('/', '.'))) {
					this.log.info("Instrumenting the method '" + methodd.getSimpleMethodName()
						+ "' in the class '" + this.getClassCanonicalName() + "'.");
				}
			}
		}
		
		// set the COMPUTE_MAXS flag for automatic stack size compution
		// The COMPUTE_FRAMES flag includes the COMPUTE_MAXS flag. Note though
		// that COMPUTE_FRAMES causes severe memory overhead to the point where 
		// projects such as specJBB can not be instrumented anymore.
		this.classWriter = new ClassWriter(this.classReader, ClassWriter.COMPUTE_MAXS/* | ClassWriter.COMPUTE_FRAMES*/);
		this.uninstrumentedBytes = this.classWriter.toByteArray();

		// Remember: the order of the visitor construction does not relate to 
		// their execution order.
		
		// Debug output (for bytecode output remove comments:
		//log.fine("The new bytecode is the following:\n");
		ClassVisitor next = this.classWriter;
		if(DebugOptions.useCheckClassAdapter) {
		    ClassVisitor check = new CheckClassAdapter(next);
		    next = check;
		}
		if(DebugOptions.traceClass) {
			ClassVisitor trace = new TraceClassVisitor(next, new PrintWriter(System.out));
			next = trace;
		}
		// end Debug output
	    
	    if(!this.instrumentationParameters.getCountStatically()){
	    	this.classAdapter = new MethodCountClassAdapter(next, 
	    			this.instrumentationParameters,
	    			this.instrumentationState);
	    } else {
	    	this.classAdapter = new MethodSectionCountClassAdapter(next, 
	    			this.instrumentationParameters,
	    			this.instrumentationState);
	    }
	}
	
	/** TODO shift to test package...
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean test() {
		//TODO refactor to a separate class?
		
		// TODO add derivePackageTokensFromCanonicalName(canonicalName) testing!
		
		String input1 = "de.uka.ipd.sdq.ByCounter.Instrumenter.class";
		String input2 = "de.uka.ipd.sdq.ByCounter.Instrumenter";
		String input3 = "de/uka/ipd/sdq/ByCounter/Instrumenter.class";
		String input4 = "de/uka/ipd/sdq/ByCounter/Instrumenter.class";
		String input5 = "Instrumenter.class";
		String input6 = "Instrumenter";
		String input7 = "/de.uka.ipd.sdq.ByCounter.Instrumenter.class";
		String input8 = "/de/uka/ipd/sdq/ByCounter/Instrumenter.class";
		String input9 = "/Instrumenter.class";
		
		String output = "Instrumenter";
		boolean globalSuccess = true;
		if(!output.equals(PathMapper.deriveShortClassNameFromCanonicalName(input1))){
			globalSuccess=false;
			System.out.println(input1+" was not converted to "+output);
		}
		if(!output.equals(PathMapper.deriveShortClassNameFromCanonicalName(input2))){
			globalSuccess=false;
			System.out.println(input2+" was not converted to "+output);
		}
		if(!output.equals(PathMapper.deriveShortClassNameFromCanonicalName(input3))){
			globalSuccess=false;
			System.out.println(input3+" was not converted to "+output);
		}
		
		if(!output.equals(PathMapper.deriveShortClassNameFromCanonicalName(input4))){
			globalSuccess=false;
			System.out.println(input4+" was not converted to "+output);
		}
		if(!output.equals(PathMapper.deriveShortClassNameFromCanonicalName(input5))){
			globalSuccess=false;
			System.out.println(input5+" was not converted to "+output);
		}
		if(!output.equals(PathMapper.deriveShortClassNameFromCanonicalName(input6))){
			globalSuccess=false;
			System.out.println(input6+" was not converted to "+output);
		}
		
		if(!output.equals(PathMapper.deriveShortClassNameFromCanonicalName(input7))){
			globalSuccess=false;
			System.out.println(input7+" was not converted to "+output);
		}
		if(!output.equals(PathMapper.deriveShortClassNameFromCanonicalName(input8))){
			globalSuccess=false;
			System.out.println(input8+" was not converted to "+output);
		}
		if(!output.equals(PathMapper.deriveShortClassNameFromCanonicalName(input9))){
			globalSuccess=false;
			System.out.println(input9+" was not converted to "+output);
		}
		return globalSuccess;
	}
}
