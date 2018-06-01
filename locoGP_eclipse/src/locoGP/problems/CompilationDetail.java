package locoGP.problems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import locoGP.individual.Individual;
import locoGP.operators.GPMaterialVisitor;
import locoGP.operators.NodeOperators;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

//import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
//import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
/*
 * A set of files that are to be compiled and modified as one single GP individual.
 * Everything we need for each modified file should be included here. 
 */
public class CompilationDetail {
	
	private CompilationUnit compUnit = null; // never modify this object, allchanges are done as textEdits
	private ASTRewrite rewriter = null;
	// do we need the names of all classes, or just the public ones? do internal classes cause hassle?
	//private String className = null; 
	private String codeBlob=null;
	//private String instrumentedCodeBlob=null;
	//private String classPackage=null;
	public GPMaterialVisitor gpMaterial;
	private ArrayList<ExpressionStatement> instrumentationStatements;	
	
	/*public CompilationDetail(String problemString) {
		setCodeString(problemString);
		setClassName(className);
		setClassPackage(classPackage);
		}*/
	
	public CompilationDetail(String problemString){
		this.codeBlob = problemString;
		this.compUnit = parseSource(this.codeBlob);
		refreshGPMaterial();
	}
	
	private void refreshGPMaterial(){
		this.gpMaterial = new GPMaterialVisitor();
		this.compUnit.accept(this.gpMaterial);
		this.rewriter = null;
	}
	
	public static CompilationUnit parseSource(String newIndCode) {
		ASTParser parser = ASTParser.newParser(AST.JLS3); 
		
		// TODO optimisation around repeat parsing
		
		// use ast rewrite to allow changes
		// http://publib.boulder.ibm.com/infocenter/iadthelp/v6r0/index.jsp?topic=/org.eclipse.jdt.doc.isv/guide/jdt_api_manip.htm

		// TODO get bindings working, for now we hack this
		parser.setResolveBindings(true);
		// parser.setBindingsRecovery(true);
		parser.setSource(newIndCode.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		// ICompilationUnit icompUnit = (ICompilationUnit)
		CompilationUnit compUnit = (CompilationUnit) parser.createAST(null);

		compUnit.recordModifications();

		return compUnit;
	}

	/*private void setClassPackage(String classPackage2) {
		this.classPackage = classPackage2;
	}*/
	
	public void setClassName(String oldClassName, String newID) {
		// this is bad
		NodeOperators.rename(getClassNameNode(oldClassName),
				oldClassName+newID, getRewriter());
		getCodeString();
	}
	
	public void updateClassNameinAST(List<String> originalNames, String newSuffix) {
		/*
		 * All references to classnames must be updated (simpleNames) Go through
		 * all simpleNames to see if they need to be updated This is important
		 * to update method calls peppered throughout the code
		 */
		
		List<SimpleName> updateableNames;
		String originalName;
		SimpleName nameToUpdate;
			
		updateableNames = this.gpMaterial.getUpdateableNames();
			
		Collections.sort(updateableNames, new Comparator<SimpleName>() {
			public int compare(SimpleName f1, SimpleName f2) {
				return f1.toString().compareTo(f2.toString());
			}
		});
		Collections.sort(originalNames);
		int origNameIndex = 0;
		boolean foundAtLeastOnce = false;
		
		for (int upNamesIndex = 0; upNamesIndex < updateableNames.size()
				&& origNameIndex < originalNames.size(); upNamesIndex++) {
					
				nameToUpdate = updateableNames.get(upNamesIndex);
				originalName = originalNames.get(origNameIndex);
				if (nameToUpdate.toString().startsWith(originalName + "_")
						|| nameToUpdate.toString().equals(originalName)) {

					NodeOperators.rename(nameToUpdate,
							originalName + newSuffix, getRewriter());
					
					foundAtLeastOnce = true;
				} else if (foundAtLeastOnce) {
					// we've found at least one instance of the simpleName that
					// needed updating
					origNameIndex++; // on to the next name that needs to be
										// updated
					foundAtLeastOnce = false;
					upNamesIndex--;
				}
		}
		getCodeString();
	}

	public String getClassName(){
		return this.gpMaterial.getClassName().toString();
	}
	
	public String getClassPackage(){
		return this.gpMaterial.getClassPackage();
	}
	
	public String getFQN(){
		if (getClassPackage().compareTo("") == 0)
			return getClassName();
		else
			return getClassPackage() + "." + getClassName();
	}
	
	public void setCodeString(String codeAsString) {
		this.codeBlob = codeAsString;
		getCodeString();
	}
	
	

	/*public void updateOriginalCodeString() {
		if(this.AST != null)
			this.codeBlob = updateCodeString(this.codeBlob);
	}
	
	public String getOriginalCodeString() {
		return this.codeBlob;
	}
	
	public String getInstrumentedCodeString() {
		this.instrumentedCodeBlob= updateCodeString(this.codeBlob);
		return this.instrumentedCodeBlob;
	}*/
	
	
	private SimpleName getClassNameNode(String className) {
		for( SimpleName sN: this.gpMaterial.getUpdateableNames()){
			if(sN.toString().equals(className))
				return sN;
		}
		return null;
	}
	
	/*public String getCodeString(){
		return this.codeBlob;
	}*/

	public String getCodeString() {
		//return this.AST.toString();
		//http://prosseek.blogspot.hr/2012/10/writing-file-from-compilationunit.html
		if(this.compUnit == null)
			return this.codeBlob;
		//https://rpanzer.wordpress.com/2011/08/25/generating-java-code-using-jdt/
		Document document = new Document(this.codeBlob);
		
		
		//ASTRewrite rewriter = ASTRewrite.create(); // ? check if the parameter object type is correct
		
		TextEdit te;
		try {
			//te = rewriter.rewriteAST();
			//te = this.AST.rewrite(document, null); //rewriter.rewriteAST(document, null);
			te = getRewriter().rewriteAST(document, null);
					//this.AST.rewrite(document, JavaCore.getOptions()); //rewriter.rewriteAST(document, null);
			te.apply(document);
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//this.AST.rewrite(document, null);
		//ICompilationUnit unit = (ICompilationUnit)this.AST.getJavaElement();//getJavaElement();
		/*try {
			rewriter.rewriteAST().apply(document);
			this.AST.rewrite(document, null);
		} catch (MalformedTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		//String source = this.AST.getSource(); //document.get();
		//this.instrumentedCodeBlob=document.get();
		this.codeBlob = document.get();
		this.compUnit = parseSource(this.codeBlob);
		refreshGPMaterial();
		return codeBlob;
	}

	public void flattenASTChangesToString(){
		// after changes to the program, it's not clear how to use astrewrite 
		this.codeBlob = getCodeString();
		this.compUnit = null;
		this.rewriter = null;
	}
	
	public void createASTFromString(){ 
		this.compUnit = parseSource(this.codeBlob);
	}
	
	public void setAST(CompilationUnit parseSource) {
		this.compUnit = parseSource;
	}
	
	public CompilationDetail clone(){
		CompilationDetail returnCD = new CompilationDetail(getCodeString());
		//returnCD.setAST(NodeModifierUtil.cloneAST(this.AST));
		return returnCD;
	}

	public CompilationUnit getCompilationUnit() {
		return this.compUnit;
	}

	public void accept(ASTVisitor globalGPMaterial) {
		this.compUnit.accept(globalGPMaterial);
	}

	public ASTRewrite getRewriter() {
		if(this.rewriter == null)
			this.rewriter = ASTRewrite.create(this.compUnit.getAST());
		return this.rewriter;
	}

	public void setInstrumentationStatements(ArrayList<ExpressionStatement> allRecordingStmts) {
		this.instrumentationStatements = allRecordingStmts;
	}
	
	public ArrayList<ExpressionStatement> getInstrumentationStatements( ) {
		return this.instrumentationStatements ;
	}


	
}
