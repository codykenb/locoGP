package locoGP.util;

import java.io.IOException;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaFileObject.Kind;

public class ClassFileManager extends
ForwardingJavaFileManager {
/**
* Instance of JavaClassObject that will store the
* compiled bytecode of our class
*/
private ArrayList<JavaClassObject> jclassObjects = null;

/**
* Will initialize the manager with the specified
* standard java file manager
*
* @param standardManger
*/
	public ClassFileManager(StandardJavaFileManager standardManager) {
		super(standardManager);
		jclassObjects = new ArrayList<JavaClassObject>();
	}

/**
* Will be used by us to get the class loader for our
* compiled class. It creates an anonymous class
* extending the SecureClassLoader which uses the
* byte code created by the compiler and stored in
* the JavaClassObject, and returns the Class for it
*/
@Override
public ClassLoader getClassLoader(Location location) {
	//return ClassLoader.getSystemClassLoader();
	GPSecureClassLoader tmpCL = new GPSecureClassLoader();
	tmpCL.setClasses(jclassObjects);

	return tmpCL;

}

/**
* Gives the compiler an instance of the JavaClassObject
* so that the compiler can write the byte code into it.
*/
public JavaFileObject getJavaFileForOutput(Location location,
String className, Kind kind, FileObject sibling)
    throws IOException {
	for (JavaClassObject jCO : jclassObjects) {
		if (jCO.getName().contains(className.replace(".", "/"))) {
			return jCO;
		}
	}
	JavaClassObject jclassObject = new JavaClassObject(className, kind); 
    jclassObjects.add(jclassObject); 
return jclassObject;
}

/*public JavaFileObject getJavaFileForOutput(){
	return jclassObjects.get(0);
}
*/

    public Map<String, byte[]> getClassByteMap(){
    	Map<String, byte[]> returnMap = new HashMap<String, byte[]>();
    	for (JavaClassObject jCO : jclassObjects) {
    		returnMap.put(jCO.getSimpleName(), jCO.getBytes());
    	}
    	return returnMap;
    }
    
	/*public byte[] getClassForOutput(String name) throws ClassNotFoundException {
		boolean found = false;
		byte[] b = null;
		for (JavaClassObject jCO : jclassObjects) {
			//String aName = jCO.getName();
			if (jCO.getName().contains(name.replace(".", "/"))) {
				b = jCO.getBytes();
				found = true;
				break;
			}
		}
		if (!found)
			throw new ClassNotFoundException(name);
		return b;
	}*/

}
