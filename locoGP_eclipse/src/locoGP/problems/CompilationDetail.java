package locoGP.problems;
import java.util.Arrays;

import locoGP.individual.Individual;
import locoGP.operators.NodeOperators;

import org.eclipse.jdt.core.dom.CompilationUnit;
/*
 * A set of files that are to be compiled and modified as one single GP individual.
 * Everything we need for each modified file should be included here. 
 */
public class CompilationDetail {
	
	public CompilationUnit AST = null;
	// do we need the names of all classes, or just the public ones? do internal classes cause hassle?
	private String className = null; 
	private String codeBlob=null;
	private String classPackage=null;
	
	
	public CompilationDetail(String problemString, String classPackage, String className) {
		setCodeString(problemString);
		setClassName(className);
		setClassPackage(classPackage);
		}

	private void setClassPackage(String classPackage2) {
		this.classPackage = classPackage2;
	}

	public String getClassName2(){
		return className;
	}
	
	public String getFQN(){
		if (classPackage.compareTo("") == 0)
			return className;
		else
			return classPackage + "." + className;
	}
	
	public void setCodeString(String codeAsString) {
		this.codeBlob = codeAsString;
	}
	
	public void setClassName(String className) {
		this.className = className;
	}

	public String getCodeString() {
		if(this.AST == null)
			return this.codeBlob;
		else
			return this.AST.toString();
	}

	public void setAST(CompilationUnit parseSource) {
		this.AST = parseSource;
	}
	
	public CompilationDetail clone(){
		CompilationDetail returnCD = new CompilationDetail(this.AST.toString(),classPackage, className);
		returnCD.setAST(NodeOperators.cloneAST(this.AST));
		return returnCD;
	}

	public CompilationUnit getAST() {
		return this.AST;
	}

}
