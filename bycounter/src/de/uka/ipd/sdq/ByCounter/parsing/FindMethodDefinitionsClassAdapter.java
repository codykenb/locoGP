package de.uka.ipd.sdq.ByCounter.parsing;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * This class finds the methods defined in a class.
 * @author Martin Krogmann
 *
 */
public final class FindMethodDefinitionsClassAdapter {
	
	private static Logger log = Logger.getLogger(FindMethodDefinitionsClassAdapter.class.getCanonicalName());
	
	/**
	 * A list of strings that cause a class to be ignored in the parsing 
	 * when found at the start of a package name.
	 */
	protected String[] ignoredPackagePrefixes;

	private Map<String, byte[]> classesHierarchyLoader = null;
	
	public FindMethodDefinitionsClassAdapter(final String[] ignoredPackagePrefixes, Map<String, byte[]> classHierarchyBytesToInstrument) {
		this.ignoredPackagePrefixes = ignoredPackagePrefixes;
		this.classesHierarchyLoader = classHierarchyBytesToInstrument;
	}
	
	/**
	 * @param methodDefinitions A list of full method definition descriptions
	 * for all methods in the class.
	 * @param className The name of the class holding the methods that shall be parsed.
	 * Needs to be fully qualified as this is used to find the correct class.
	 * @return True, if the class could be found and parsed successfully.
	 */
	public boolean parseClass(ClassMethodImplementations methodDefinitions, final String className) {

		ClassReader cr = null;
		
		try {		
			if(classesHierarchyLoader != null){
				// bck System.out.println("Looking for class: "+className +" in "+ classesHierarchyLoader.keySet());
				cr = new ClassReader(classesHierarchyLoader.get(className));
			}else
				cr = new ClassReader(className);
		} catch (IOException e) {
			log.severe("Could not parse class with name '" + className + "'. Skipping.");
			e.printStackTrace();
			return false;
		}/* catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.severe("Could not extract class bytes with name '" + className + "'. Skipping. (bck)");
		}*/
		return parseClass(methodDefinitions, cr);
	}
		
	/**
	 * @param methodDefinitions A list of full method definition descriptions
	 * for all methods in the class.
	 * @param classBytes The class holding the methods that shall be parsed.
	 * Needs to be fully qualified as this is used to find the correct class.
	 * @return True, if the class could be found and parsed successfully.
	 */
	public boolean parseClass(ClassMethodImplementations methodDefinitions, final byte[] classBytes) {

		ClassReader cr = null;
		cr = new ClassReader(classBytes);

		return parseClass(methodDefinitions, cr);
	}
	
	/**
	 * @param methodDefinitions A list of full method definition descriptions
	 * for all methods in the class.
	 * @param classReader An initialised {@link ClassReader} for the class holding the methods that shall be parsed.
	 * Needs to be fully qualified as this is used to find the correct class.
	 * @return True, if the class could be found and parsed successfully.
	 */
	@SuppressWarnings("unchecked")
	protected boolean parseClass(ClassMethodImplementations methodDefinitions, ClassReader classReader) {
		if(methodDefinitions == null) {
			log.severe("MethodDefinitions list was null. Aborting parsing.");
			return false;
		}
		
		
		// check if the class is to be ignored
		for(String prefix : ignoredPackagePrefixes) {
			if (classReader.getClassName().startsWith(prefix)) {
				log.finer("Class is in ignored package: " + classReader.getClassName());				
				return true;
			}
		}
		
		log.info("Parsing class: " + classReader.getClassName());
		
		// transformation chain:
		ClassNode cn = new ClassNode();		
		ClassReader cr = classReader;
		
		cr.accept(cn, 0);
		
		boolean success = true;
		
		methodDefinitions.setSuperClass(cn.superName);
		
		String simpleClassName;
		int sepInd = cn.name.lastIndexOf('/');
		// sepInd == -1 works fine in this case
		simpleClassName = cn.name.substring(sepInd + 1);
		
		// go through all methods
		Iterator<MethodNode> it = cn.methods.iterator();
		while(it.hasNext()) {
			MethodNode m = (MethodNode)it.next();
			String mName;
			if(m.name.equals("<init>")) {
				mName = simpleClassName;
			} else {
				mName = m.name;
			}
			methodDefinitions.getMethods().add(mName + m.desc);
		}
		
		return success;
	}
}
