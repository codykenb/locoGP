package locoGP.util;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import locoGP.individual.Individual;
import locoGP.problems.CompilationDetail;
import locoGP.problems.CompilationSet;
import locoGP.roughwork.CustomClassLoader;

/*
 * borrowed from
 * http://www.java2s.com/Tutorial/Java/0120__Development/CompileString.htm
 */
public class StringCompiler implements java.io.Serializable{
	
	/*public boolean compileClass(Individual ind){
		
		  TODO use ecj instead of suns javac like this: 
		  
		 
		//org.eclipse.jdt.internal.compiler.Compiler ourComp = new org.eclipse.jdt.internal.compiler.Compiler(null, null, null, null, null);
		//ourComp.accept(sourceUnit, accessRestriction);

		org.eclipse.jdt.internal.compiler.Compiler ourCompiler = new org.eclipse.jdt.internal.compiler.Compiler(new NameEnvironmentImpl(ind.getAST()),
		                     DefaultErrorHandlingPolicies.proceedWithAllProblems(),
		                     settings,requestor,new DefaultProblemFactory(Locale.getDefault()));
		ourCompiler.compile(new ICompilationUnit[] { unit });
		
		ourCompiler.
		return true;
	}*/

	public boolean compileClass(Individual ind) {
		boolean compileStatus = false;
		CompilationDetail[] cD = ind.ASTSet.getCompilationList();

		/*//ToolProvider.getSystemToolClassLoader(); // aha!
		for(int i = cD.length-1 ; i >=0 ; i--){ // TODO put all files in for compilation at the same time..
			compiled = compileClass(cD[i]);	
			if(!compiled) 
				break;
		}
		return compileClasses(cD);
	}
	
	private boolean compileClasses(CompilationDetail[] cD) {
		boolean compiledStatus = false;*/
		//JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		//JavaFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));
		Iterable<? extends JavaFileObject> fileObjects = getJavaSourceFromStrings(cD);
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	/*	return compile(fileObjects);
	}
	
	

	private boolean compileClass(CompilationDetail ind) { //dont use this any more
		
		
		
		//Logger.writeJavaFile(ind.getCodeString(), ind.getClassName());
		Iterable<? extends JavaFileObject> fileObjects;
		fileObjects = getJavaSourceFromString(ind.getClassName(), ind.getString());
		
		return compile(fileObjects);
	}*/
	
	/*private boolean compile(Iterable<? extends JavaFileObject> fileObjects){
		boolean compileStatus = false;*/
		
		
		if(compiler==null){
			System.out.println("No compiler installed (install jdk!)");
			Logger.logAll("No compiler installed (install jdk!)");
			System.exit(1);
		}
			
		//compiler.getStandardFileManager(arg0, arg1, arg2)
		JavaFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));
		//fileObjects.
		// thank you: http://www.accordess.com/wpblog/2011/03/06/an-overview-of-java-compilation-api-jsr-199/
		/*String[] compileOptions = new String[]{"-d", "bin"} ;
		Iterable<String> compilationOptionss = Arrays.asList(compileOptions);*/

		Writer tempWriter = Logger.getWriter();
		
		//http://stackoverflow.com/questions/12173294/compiling-fully-in-memory-with-javax-tools-javacompiler
		//ClassLoader cl = ClassLoader.getSystemClassLoader();
		ClassLoader cl = fileManager.getClassLoader(null); 
		//if( compiler.getTask(tempWriter, null, null, compilationOptionss, null, fileObjects).call()) // eclipse only!!! makes sure class goes into bin
		//if( compiler.getTask(tempWriter, null, null, null, null, fileObjects).call()) // not eclipse 
		//try {
		
			if (compiler.getTask(tempWriter, fileManager, null, null, null,
			//if (compiler.getTask(tempWriter, null, null, null, null,
					fileObjects).call()) { // not eclipse
				compileStatus = true;
				//fileManager.getJavaFileForOutput(arg0, arg1, arg2, arg3)
				/*ind.setClassByteArray(((JavaClassObject) ((ClassFileManager) fileManager)
						.getJavaFileForOutput()).getBytes());*/
			}
			//tempWriter.close();
		//} catch (IOException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}	
		//compiler.getTask(null, null, null, null, null, fileObjects).call(); // this creates a compiled class file in the current dir

		// Reload classes, by generating a new classloader each time - http://www.exampledepot.com/egs/java.lang/ReloadClass.html
		//forName(String name, boolean initialize, ClassLoader loader) 
		
		
		//TODO ClassLoader cL = new CustomClassLoader(ClassLoader.getSystemClassLoader());
		//ind.setClassLoader( cL );
		/* Once this method ends, and the individual is no longer referenced, the classloader and the class should be GC'd.
		 * This will happen when generations roll over.
		 * 
		 * 
		 * 	URLClassLoader child = new URLClassLoader (myJar.toURL(), this.getClass().getClassLoader());
			Class classToLoad = Class.forName ("com.MyClass", true, child);
			Method method = classToLoad.getDeclaredMethod ("myMethod");
			Object instance = classToLoad.newInstance ();
			Object result = method.invoke (instance);
		 * 
		 */
		if (compileStatus) {
			try { // needed?
				//String className = ind.getClassName();
				//ClassLoader.getSystemClassLoader();
				//Class<?> clazz = Class.forName(className); //, true, cL); //cL.loadClass(className);//
				//ind.setClassLoader( fileManager.getClassLoader(null));
				//ind.setClassByteArray(((ClassFileManager)fileManager).getClassForOutput(ind.getClassName()));
						//((JavaClassObject) ((ClassFileManager) fileManager).getJavaFileForOutput()).getBytes());
				//Class<?> clazz =cL.loadClass(className); 
				/*ClassLoader sysCL = ClassLoader.getSystemClassLoader(); // this will try load from disk..
				//ClassLoader.
				Class<?> clazz = cl.loadClass(className);*/
				//Class<?> clazz = Class.forName(className);
				
				// TODO one or the other
				ind.setClassByteMap(((ClassFileManager)fileManager).getClassByteMap());
				ind.setClass(((ClassFileManager)fileManager).getClassLoader(null).loadClass(ind.getClassName())); 
				/*Thread.currentThread().setContextClassLoader(cl);*/
				//SimpleVerifier.setClassLoader(cl);
		
				/*ind.setClassByteArray(((ClassFileManager)fileManager).getClassForOutput(className));
				Method[] meths = clazz.getMethods();
				//ind.setClass(clazz);
				anURL = sysCL.getSystemResource(className.replace('.', '/')+".class");*/
				
				
				/*Method m = clazz.getMethod("classify", new Class[] {int.class, int.class, int.class});
				Object[] _args = Triangle2.testData[0].getTest(); //new Object[] { testArray };
					int result = (Integer) m.invoke(null, _args);							
					System.out.println("result " + result);*/
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				compileStatus = false;
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				compileStatus = false;
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}/*else{
			System.out.print("No Compile");
		}*/
		
		//compiler=null;
		ind.setClassLoader(((ClassFileManager)fileManager).getClassLoader(null));
		cl=null;
		
		return compileStatus;
	}
	
	
	class ByteClassLoader extends URLClassLoader {
	    private final Map<String, byte[]> extraClassDefs;

	    public ByteClassLoader(URL[] urls, ClassLoader parent, Map<String, byte[]> extraClassDefs) {
	      super(urls, parent);
	      this.extraClassDefs = new HashMap<String, byte[]>(extraClassDefs);
	    }

	    @Override
	    protected Class<?> findClass(final String name) throws ClassNotFoundException {
	      byte[] classBytes = this.extraClassDefs.remove(name);
	      if (classBytes != null) {
	        return defineClass(name, classBytes, 0, classBytes.length); 
	      }
	      return super.findClass(name);
	    }

	  }
	
	/*public static void testClassLoaders(){
		
		// TODO Work custom classloaders into the rest of the code! & test!
		
		ClassLoader cL = new CustomClassLoader(ClassLoader.getSystemClassLoader());
		System.out.println(cL.toString());
		try {
			
			 Enumeration ourE = cL.getResources("");
			ourE.nextElement().toString();
			while( ourE.hasMoreElements())
				System.out.println("Classloader: " + cL.toString() + ourE.nextElement().toString());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ClassLoader systemCL = ClassLoader.getSystemClassLoader();
		try {
			
			 Enumeration ourE = systemCL.getResources("");
			ourE.nextElement().toString();
			while( ourE.hasMoreElements())
				System.out.println("Classloader: " + systemCL.toString() + ourE.nextElement().toString());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ClassLoader cL2 =  ToolProvider.getSystemToolClassLoader();
		if( cL==null)
			System.out.println("cl null ");
		if( cL2==null)
			System.out.println("cl2 null ");
		if( cL.equals(cL2)){
			System.out.println("Same flipping classloader!! ");
		}
	}*/
	
	// -------------deprecated 
	
	/*public void callMethod (GPClass gpClass, int[] testArray)
	throws Exception {
		
		callMethod(gpClass.getClassString(), testArray, gpClass.getClassName(), gpClass.getMethodName());
	}*/
	
	
	/*public void callMethod(String program, int[] testArray, String className, String methodName)
			throws Exception {
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		
		Iterable<? extends JavaFileObject> fileObjects;
		fileObjects = getJavaSourceFromString(className, program);
		
		// thank you: http://www.accordess.com/wpblog/2011/03/06/an-overview-of-java-compilation-api-jsr-199/
		String[] compileOptions = new String[]{"-d", "bin"} ;
		Iterable<String> compilationOptionss = Arrays.asList(compileOptions);

		// this load the class
		Writer tempWriter = Logger.getWriter();
		//if( ! compiler.getTask(tempWriter, null, null, compilationOptionss, null, fileObjects).call()) // eclipse only!!! makes sure class goes into bin
		if( ! compiler.getTask(tempWriter, null, null, null, null, fileObjects).call()) // 
			throw new Exception("Did not compile");
		
		tempWriter.close();
		
		//compiler.getTask(null, null, null, null, null, fileObjects).call(); // this creates a compiled class file in the current dir

		// Reload classes, by generating a new classloader each time - http://www.exampledepot.com/egs/java.lang/ReloadClass.html
		//forName(String name, boolean initialize, ClassLoader loader) 
		Class<?> clazz = Class.forName(className); // this line loads the class
		// TODO seperate the actual evaluation of the class from the compilation of the class
		//clazz.getClassLoader().loadClass(gpClass.getClassName());
		// Object newGPObj = clazz.newInstance(); // the method is static, no
		// need
		// Method m = clazz.getMethod("main", new Class[] { String[].class });
		//Logger.log("\n"+program+"\n");
		//Logger.log("Going to try modify the array " + clazz.getCanonicalName());
		// printArray(testArray);
		
		Method m = clazz.getMethod(methodName, new Class[] {int[].class});
		Object[] _args = new Object[] { testArray };
		
		 * null is here because this is a static method, it can be replaced with
		 * an object, which has this method
		 
		
		int[] testArray2 = (int[]) m.invoke(null, _args); // only for static "class" methods
		printArray(testArray2);
		//Logger.log("done");
		// m.invoke(null, new Object[] {}); // only for static "class" methods
		// m.invoke(newGPObj, new Object[] {}); // for an instantiated object
	}*/

	private void printArray(int[] testArray) {
		String outLine = "";
		for( int i = 0 ; i< testArray.length; i++){
			outLine += " "+testArray[i];
		}
		//Logger.log(outLine);
	}

	private Iterable<? extends JavaFileObject> getJavaSourceFromStrings(
			CompilationDetail[] cD) {
		ArrayList<JavaFileObject> sourceArr = new ArrayList<JavaFileObject>();
		for(int i =cD.length-1; i>=0 ; i--) //CompilationDetail aCD : cD)
			sourceArr.add(new JavaSourceFromString(cD[i].getCodeString(), cD[i].getFQN()));
		return sourceArr;
	}
	
	/*static Iterable<JavaSourceFromString> getJavaSourceFromString(String name, String code) {
		final JavaSourceFromString jsfs;
		jsfs = new JavaSourceFromString(name, code);
		return new Iterable<JavaSourceFromString>() {
			public Iterator<JavaSourceFromString> iterator() {
				return new Iterator<JavaSourceFromString>() {
					boolean isNext = true;

					public boolean hasNext() {
						return isNext;
					}

					public JavaSourceFromString next() {
						if (!isNext)
							throw new NoSuchElementException();
						isNext = false;
						return jsfs;
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
	}*/

}
