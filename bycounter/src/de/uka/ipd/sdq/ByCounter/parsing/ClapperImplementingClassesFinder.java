/**
 * 
 */
package de.uka.ipd.sdq.ByCounter.parsing;

import java.util.ArrayList;

import org.clapper.util.classutil.AbstractClassFilter;
import org.clapper.util.classutil.AndClassFilter;
import org.clapper.util.classutil.ClassFilter;
import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassInfo;
import org.clapper.util.classutil.InterfaceOnlyClassFilter;
import org.clapper.util.classutil.NotClassFilter;
import org.clapper.util.classutil.SubclassClassFilter;

/**
 * Implementation of the {@link IImplementingClassesFinder} interface for the 
 * clapper library.
 * @author Martin Krogmann
 *
 */
public final class ClapperImplementingClassesFinder implements
		IImplementingClassesFinder {
	
	private ClassFinder finder;
	private ClassFilter filter;

	public ClapperImplementingClassesFinder() {

		finder = new ClassFinder();
		finder.addClassPath();
		filter = null;
	}

	/* (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.parsing.IImplementingClassesFinder#findImplementingClasses(java.lang.Class)
	 */
	public String[] findImplementingClasses(Class<?> interfaceToImplement) {
		filter = new AndClassFilter(
				new NotClassFilter(new InterfaceOnlyClassFilter()),
				// Must implement the found interface
				new SubclassClassFilter(interfaceToImplement),
				// Must not be abstract
				new NotClassFilter(new AbstractClassFilter()));

        ArrayList<ClassInfo> foundClasses = new ArrayList<ClassInfo>();
        finder.findClasses (foundClasses, filter);
        
        // fill in a string array with the class names
        String[] resultingClasses = new String[foundClasses.size()];

        for(int i = 0; i < foundClasses.size(); i++) {
        	resultingClasses[i] = foundClasses.get(i).getClassName();
        }
		return resultingClasses;
	}

}
