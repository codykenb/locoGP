package locoGP.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.SecureClassLoader;
import java.util.ArrayList;

public class GPSecureClassLoader extends SecureClassLoader{

	private ArrayList<JavaClassObject> jclassObjects=null;

	/*    public Class<?> loadClass(String name) throws ClassNotFoundException {
    return loadClass(name, true);
}*/
	public GPSecureClassLoader(){
		super(GPSecureClassLoader.class.getClassLoader());
	}
	
	
	public void  setClasses(ArrayList<JavaClassObject> jclassObjects){
		this.jclassObjects = jclassObjects;
	}
		    @Override
		    protected Class<?> findClass(String name)
		        throws ClassNotFoundException {
		    	boolean found = false;
		        byte[] b = null; 
		        JavaClassObject foundCO = null;
		        String tmpName = "";
		        for(JavaClassObject jCO : jclassObjects){
		        	tmpName = jCO.getName();
		        	
		        	// TODO clean up this utter mess...
		        	/*String[] splitName = tmpName.split("/");
		        	String secondPart = splitName[2];
		        	String[] secondName = secondPart.split("\\.");
		        	addClassToParentLoader( jCO, splitName[1]+"."+secondName[0], jCO.getBytes(), 0, jCO.getBytes().length); // if this works
		        	*/
		        	
		        	//if(tmpName.contains((name.replace(".", "/")))){
		        	if(tmpName.equals("/"+(name.replace(".", "/"))+".class")){
		        		b = jCO.getBytes();
		        		found = true;
		        		foundCO = jCO;
		        		 
		        		break;
		        	}
		        }
		        if(!found)
		        	throw new ClassNotFoundException(name);
		        //super.loadClass(name);
		        
		        // Load into the system class loader here?
		        // ClassLoader.getSystemClassLoader().
		        //this.getParent().
		        Class cl = super.defineClass(name, b, 0, b.length);
		        super.loadClass(name,true);
		        
		        //addClassToParentLoader( foundCO, name, b, 0, b.length);
		       
		        //Class returnClass = ClassLoader.getSystemClassLoader().loadClass(name);
		        
		        //resolveClass(cl);
		        try {
					cl.newInstance();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        //classes.put(name, cl);
		        //super.
		        return cl;
		        
		    }

		    public byte[] getClassForOutput(String name) throws ClassNotFoundException {
				boolean found = false;
				byte[] b = null;
				for (JavaClassObject jCO : jclassObjects) {
					//String aName = jCO.getName();
					if (jCO.getName().contains(name.replace(".", "/"))) { // this is dangerous, ambiguity here
						b = jCO.getBytes();
						found = true;
						break;
					}
				}
				if (!found)
					throw new ClassNotFoundException(name);
				return b;
			}
		    

			private void addClassToParentLoader(
					JavaClassObject foundCO, String name, byte[] b, int i, int length) {
				// Hackity hackity!
				Class sysclass = ClassLoader.class; //parentCL.class;
				 ClassLoader ultimateCL = ClassLoader.getSystemClassLoader();
				Class[] parameters = new Class[]{String.class, byte[].class, int.class, int.class};

				
				try {
				     Method method = sysclass.getDeclaredMethod("defineClass", parameters);
				     method.setAccessible(true);
				     method.invoke(ultimateCL, new Object[]{name,b,i,length});
				     System.out.println("Added " + name + " to system class loader");
				  } catch (Throwable t) {
				     t.printStackTrace();
				     //throw new IOException("Error, could not hack system classloader");
				  }//end try catch
			}
		
		
}
