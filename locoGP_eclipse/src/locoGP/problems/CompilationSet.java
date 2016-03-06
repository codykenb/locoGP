package locoGP.problems;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTVisitor;

import locoGP.operators.GPMaterialVisitor;
import locoGP.operators.NodeOperators;
import locoGP.util.ProbabilityClonerVisitor;

public class CompilationSet {
	protected CompilationDetail[] compilationList = null;
	public CompilationSet(CompilationDetail[] newCompileList) {
		this.compilationList = newCompileList;
	}
	

	public String getCodeListing(){
		String returnString = "";
		for (CompilationDetail cD : compilationList)
			returnString += cD.getCodeString()+"\n";
			return returnString;
	}

	public CompilationDetail[] getCompilationList() {
		return compilationList;
	}

	public void accept(ASTVisitor gpMaterial) {
		for(CompilationDetail anAST: this.compilationList)
			anAST.AST.accept(gpMaterial); 
	}

	public CompilationSet cloneASTs() { // am sure I should be do something diff here..
		CompilationDetail[] newCompileList = new CompilationDetail[compilationList.length];
		//System.out.println("CLoning AST's: "+compilationList.length);
		for(int i = 0 ; i < compilationList.length; i++){
			newCompileList[i] = compilationList[i].clone();
		}
		return new CompilationSet(newCompileList); // this is bs
	}

	public void accept(ProbabilityClonerVisitor probabilityClonerVisitor) {
		for(CompilationDetail anAST: this.compilationList)
			anAST.AST.accept(probabilityClonerVisitor); 
	}
	
	public void updateClassNames(CompilationSet ASTSet, GPMaterialVisitor gpMaterial) {
		//gpMaterial contains all the constructors
		for(int i = 0 ; i < ASTSet.compilationList.length; i++){
			NodeOperators.updateClassName(this.compilationList[i], this.compilationList[i].getClassName2(), gpMaterial);
		}
	}
	
	public void updateMethodCallClassNames(GPMaterialVisitor gpMaterial, Problem ourProblem) {
		// we need all method invocations, the original classnames, and the new classnames
		NodeOperators.updateMethodInvocationClassNames(gpMaterial.getMethodCalls(),
				getClassNames(),ourProblem.getClassNames());
	}
	
	public void updateClassInstantiationNames(GPMaterialVisitor gpMaterial,
			Problem ourProblem) {
		NodeOperators.updateTypes(gpMaterial.getTypes(),
			getClassNames(),ourProblem.getClassNames());
		/*NodeOperators.updateClassInstantiationNames(gpMaterial.getClassInstantiations(),
				getClassNames(),ourProblem.getClassNames());
		NodeOperators.updateVarDeclarationNames(gpMaterial.getVarDeclarations(),
				getClassNames(),ourProblem.getClassNames());*/
	}

	public void setClassNames(CompilationDetail[] originalNames) {
		for(int i = 0 ; i < this.compilationList.length ; i++){
			compilationList[i].setClassName(originalNames[i].getClassName2());
		}
	}
	
	public void setClassNames(long indID, CompilationDetail[] originalNames) {
		for(int i = 0 ; i < this.compilationList.length ; i++){
			compilationList[i].setClassName(originalNames[i].getClassName2()+ indID);
		}
	}


	public ArrayList<String> getClassNames() {
		ArrayList<String> classNames = new ArrayList<String>();
		for(CompilationDetail cD: compilationList)
			classNames.add(cD.getClassName2());
		return classNames; 
	}


	public void setNullParentRefs() {
		parentRefNuller pRB =new parentRefNuller();
		this.accept(pRB); 
		
	}

}
