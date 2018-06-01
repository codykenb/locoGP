package de.uka.ipd.sdq.ByCounter.utils;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * TODO document
 * 
 * @author Michael Kuperberg
 * @author Martin Krogmann
 *
 * @since 0.1
 * @version 1.2
 */
public final class PathMapper {
	/** 
	 * Gets the list of package names of the class given as canonical name.
	 * @param canonicalName Canonical class name.
	 * @return An ordered list of package name strings. The highest in the hierarchy first. Example: First.Second.MyClass returns {"First", "Second"}.
	 */
	public static ArrayList<String> derivePackageTokensFromCanonicalName(String canonicalName){
		ArrayList<String> tokens = new ArrayList<String>();
		if(canonicalName.indexOf('.')<0 && canonicalName.indexOf('/')<0){
			return tokens;
		}
		StringTokenizer st = new StringTokenizer(canonicalName, "./");
		for(;st.hasMoreTokens();){
			tokens.add(st.nextToken());
		}
		tokens.remove(tokens.size()-1); //remove class name...
		if(tokens.get(0)==null || tokens.get(0).equals("")){
			tokens.remove(0); //remove class name...
		}
		return tokens;
	}

	/**TODO test
	 * @param canonicalName A canonical class name.
	 * @return The simple class name.
	 */
	public static String deriveShortClassNameFromCanonicalName(String canonicalName){
		if(canonicalName.indexOf('.')<0 && canonicalName.indexOf('/')<0){
			return canonicalName.trim();
		}
		StringTokenizer st = new StringTokenizer(canonicalName, "./");
		String retString;
		String currToken = null;
		String prevToken = null;
		for(;st.hasMoreTokens();){
			prevToken = currToken;
			currToken = st.nextToken();
		}
		if(currToken!=null && currToken.equals("class")){
			retString = prevToken;
		} else {
			retString = currToken;
		}
		return retString;
	}


}
