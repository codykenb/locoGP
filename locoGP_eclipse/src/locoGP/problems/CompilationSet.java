package locoGP.problems;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;

import locoGP.operators.GPMaterialVisitor;
import locoGP.operators.NodeOperators;
import locoGP.util.ProbabilityClonerVisitor;
import locoGP.util.StringFromFile;

/*
 * It's likely that this class could be replaced with a JDT project object.
 * It contains the program and all dependencies of the program that is to be evolved. 
 */
public class CompilationSet {
	protected CompilationDetail[] compilationList = null;
	
	public CompilationSet(List<String> fileNames){ // what about multiple files?
		compilationList = loadFiles(fileNames);
	}
	
	protected CompilationDetail[] loadFiles(List<String> fileNames) {
		ArrayList<CompilationDetail> fileSet = new ArrayList<CompilationDetail>(fileNames.size()); // should be list
		for (String fileName : fileNames) {
			String fileContents = StringFromFile
					.getStringFromFile(fileName);
			fileSet.add(new CompilationDetail(fileContents));
		}
		return fileSet.toArray(new CompilationDetail[fileNames.size()]);
	}
	
	@Deprecated
	public CompilationSet(CompilationDetail[] newCDs){
		// all problems should be read from file, and not created outside this class
		this.compilationList = newCDs;
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
		for(CompilationDetail cD: this.compilationList){
			cD.accept(gpMaterial);
		}
	}

	/*public CompilationSet cloneASTs() { // am sure I should be do something diff here..
		CompilationDetail[] newCompileList = new CompilationDetail[compilationList.length];
		//System.out.println("CLoning AST's: "+compilationList.length);
		for(int i = 0 ; i < compilationList.length; i++){
			newCompileList[i] = compilationList[i].clone();
			try{
			newCompileList[i].getAST().recordModifications();
			}catch(IllegalArgumentException e){
				e.printStackTrace();
			}
		}
		return new CompilationSet(newCompileList); // this is bs
	}*/

	public void accept(ProbabilityClonerVisitor probabilityClonerVisitor) {
		for(CompilationDetail anAST: this.compilationList)
			anAST.accept(probabilityClonerVisitor); 
	}
	
	public void updateClassNames(GPMaterialVisitor gpMaterial) {
		for(int i = 0 ; i < this.compilationList.length; i++){
			NodeOperators.updateClassName(this.compilationList[i], gpMaterial);
			this.compilationList[i].getCodeString();
		}
	}
	
	/*private void updateMethodCallClassNames(GPMaterialVisitor gpMaterial, Problem ourProblem) {
		// we need all method invocations, the original classnames, and the new classnames
		NodeOperators.updateMethodInvocationClassNames(gpMaterial.getMethodCalls(),
				getClassNames(),ourProblem.getClassNames());
	}*/
	
	/*private void updateClassInstantiationNames(GPMaterialVisitor gpMaterial,
			Problem ourProblem) {
		NodeOperators.updateTypes(gpMaterial.getTypes(),
			getClassNames(),ourProblem.getClassNames());
		NodeOperators.updateClassInstantiationNames(gpMaterial.getClassInstantiations(),
				getClassNames(),ourProblem.getClassNames());
		NodeOperators.updateVarDeclarationNames(gpMaterial.getVarDeclarations(),
				getClassNames(),ourProblem.getClassNames());
	}*/

	/*private void setClassNames(String indID, CompilationDetail[] originalNames) {
		for(int i = 0 ; i < this.compilationList.length ; i++){
			compilationList[i].setClassName(originalNames[i].getClassName(), indID);
		}
	}*/
	
	public void updateClassNamesinAST(GPMaterialVisitor gpMaterial, Problem ourProblem, String newSuffix) {
		/*
		 * All references to classnames must be updated (simpleNames) Go through
		 * all simpleNames to see if they need to be updated This is important
		 * to update method calls peppered throughout the code
		 */
		
		List<SimpleName> updateableNames;
		String originalName;
		SimpleName nameToUpdate;
		for (CompilationDetail cD : this.compilationList) {
			cD.updateClassNameinAST(ourProblem.getClassNames(), newSuffix);
		}
	}

	/*public void setClassNames(CompilationDetail[] originalNames) {
	for(int i = 0 ; i < this.compilationList.length ; i++){
		compilationList[i].setClassName(originalNames[i].getClassName2());
	}
}*/


	public ArrayList<String> getClassNames() {
		ArrayList<String> classNames = new ArrayList<String>();
		for(CompilationDetail cD: compilationList)
			classNames.add(cD.getClassName());
		return classNames; 
	}


	public void setNullParentRefs() {
		parentRefNuller pRB =new parentRefNuller();
		this.accept(pRB); 
		
	}
	
	

	public CompilationSet clone() {
		CompilationDetail[] newCompileList = new CompilationDetail[compilationList.length];
			//System.out.println("CLoning AST's: "+compilationList.length);
			for(int i = 0 ; i < compilationList.length; i++){
				newCompileList[i] = compilationList[i].clone();
	
			}
			return new CompilationSet(newCompileList);
	}

	public void updateOriginalCodeStringDeprecated() {
		for(CompilationDetail cD: compilationList)
			cD.getCodeString();
	}

	public void recordReplacement(ASTNode oldNode, ASTNode newNode) {
		for( CompilationDetail cD : this.getCompilationList()){
			AST astFromList =  cD.getCompilationUnit().getAST();
			AST nodeAST = oldNode.getAST();  
					
			if(nodeAST == astFromList ){ // get the right writer 
				cD.getRewriter().replace(oldNode, newNode, null); // do not change the ast before attempting this
				break;
			}
		}
	}
	
	public void refreshASTs(){
		flattenASTChangesToString();
		recreateASTFromStrings();
	}

	private void flattenASTChangesToString() {
		for(CompilationDetail cD: compilationList)
			cD.flattenASTChangesToString();
	}
	
	private void recreateASTFromStrings() {
		for(CompilationDetail cD: compilationList)
			cD.createASTFromString();
	}

	/*public void updateCodeListing() {
		for(CompilationDetail cD: compilationList)
			cD.updateCodeString();
	}*/

/*	public void updateInstrumentedStrings() {
		// TODO Auto-generated method stub
		for(CompilationDetail cD: compilationList)
			cD.updateInstrumentedString();
	}*/


	

}
