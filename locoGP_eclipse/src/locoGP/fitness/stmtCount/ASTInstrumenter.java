package locoGP.fitness.stmtCount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.dom.AST;

import locoGP.Generation;
import locoGP.experiments.GPConfig;
import locoGP.individual.Individual;
import locoGP.problems.CompilationDetail;
import locoGP.util.Logger;

public class ASTInstrumenter {
	
	/**
	 * This does not work! - due to JDT toString method inconsistencies!  
	 * Buggy behaviour whereby instrumentation statements are not being inserted.
	 * Bug is present and repeatable for some test programs.  
	 * 
	 * Add a counting statement between every statement in a program. The
	 * counting statement is executed and increments the execution count for
	 * that location in the program. Each counting statement has a different
	 * statement ID. When a program is instrumented and executed, it should
	 * gather results for how many times each statement has been executed.
	 * 
	 */

	private static ConcurrentHashMap<Long, ProgramStmtExecutionProfile> results = new ConcurrentHashMap<Long, ProgramStmtExecutionProfile>();

/*	Why doesnt this work? 
 * public synchronized static void instrumentForOverallExecutionCount(Individual ind) {
		synchronized(ind){
		ind.refreshGPMaterial();
		List<Statement> allStmts = ind.gpMaterial.getStatements();
		Long indID = ind.getID();
		results.put(indID, 0L);
		ExpressionStatement recordExecutionStatement = generateRecordExeStatement(allStmts.get(0).getAST(), ind.getID());
		ASTNode parentBlock = null;
		ExpressionStatement executionStatementClone = null ;
		for(Statement stmt: allStmts){
			executionStatementClone = (ExpressionStatement) ASTNode.copySubtree(recordExecutionStatement.getAST(),recordExecutionStatement);
			parentBlock = stmt.getParent();
			if(parentBlock instanceof Block){
				Block aBlock = (Block)parentBlock;
				int index = aBlock.statements().indexOf(stmt);
				aBlock.statements().add(index,executionStatementClone);
			}
		}
		}
	}
*/	
	
	public static void instrument(
			Individual ind) {
		
		ind.refreshGlobalGPMaterial(); // we should gather gp material for each compilation detail, access to the material should be through individual so we can ensure access to the right ASTRewrite
		
		List<Block> allBlocks;
		// go through each compilation detail object 
		
		ExpressionStatement recordExecutionStatement;
		ArrayList<ExpressionStatement> allRecordingStmts = new ArrayList<ExpressionStatement>();

		Long indID = ind.getID();
		results.put(indID, new ProgramStmtExecutionProfile(indID));

		ListRewrite listRewrite;
		ASTRewrite rewriter;
		int i = 0, j = 0, recordStmtNum = 0;
		for (CompilationDetail cD : ind.ASTSet.getCompilationList()) {
			rewriter = cD.getRewriter();
			allBlocks = cD.gpMaterial.getBlocks(); 
			
			int numBlocks = allBlocks.size();
			//Logger.logDebugConsole("Instrumenting " + numBlocks + " Blocks for Individual " + indID);
			/*System.out.println("Instrumenting " + numBlocks + " Blocks for Individual " + indID);
			if(numBlocks<4){
				System.out.println("Instrumenting " + numBlocks + " Blocks for Individual " + indID);
			}*/
			do {
				Block aBlock = allBlocks.get(i);
				int blockLength = aBlock.statements().size();
				listRewrite = rewriter.getListRewrite(aBlock, Block.STATEMENTS_PROPERTY);
				do { // even empty block
						// add a counting statement
					recordExecutionStatement = generateRecordExeStatement(allBlocks.get(0).getAST(), ind.getID(),
							recordStmtNum);
					recordStmtNum++;

					listRewrite.insertAt(recordExecutionStatement, (j * 2), null);

					/* we can't modify a block without using the appropriate
					 ASTRewrite object, which is AST-specific
					 Always use astrewrite instead of adding statements directly to a
					 block (only ASTRewrite ensures changes stick)
					 aBlock.statements().add((j * 2),
					 recordExecutionStatement); */
					
					allRecordingStmts.add(recordExecutionStatement);
					j++;
				} while (j < blockLength);
				j = 0;
				i++;
			} while (i < numBlocks);
			cD.setInstrumentationStatements(allRecordingStmts);
		}
		
		
		/* try this instead of compilationUnit.rewrite()
		 http://www.programcreek.com/2012/06/insertadd-statements-to-java-source-code-by-using-eclipse-jdt-astrewrite/
		 */
		
		/* Sanity check the resulting code to ensure it contains all record statements
		 * Do this to find out if and when jdt rewrite is buggy
		 * After testing, this seems to happen a lot and looks to be related to a bug: 
		 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=405699
		 * So it seems using rewrite is not dependable, but an astrewrite should be used.
		 * This means many of our modifications of the ast are in jeopardy of being buggy.
		 * We should change how the ast is manipulated, utilising astrewrite instead. 
		 * or potentially we should replace JDT with javaparser
		 * TODO The check below should be a test
		 */
		/*String allCode = "";
		for (CompilationDetail cD : ind.ASTSet.getCompilationList()) {
			allCode += cD.getCodeString();
		}
		allCode = allCode.replaceAll("\\s+","").replaceAll("[\n\r]", "");
		for (ExpressionStatement aStmt : allRecordingStmts) {
			if(!allCode.contains(aStmt.toString().replaceAll("\\s+","")))
					throw new RuntimeException();
		}*/
		
		
		// ind.ASTSet.setInstrumentedStrings();
	}
	
	/*private static Statement updateRecordExeStatement(ExpressionStatement recExeStmt, Long newIndID ) {
		ExpressionStatement newStmt = (ExpressionStatement) ASTNode.copySubtree(recExeStmt.getAST(), recExeStmt);
		((MethodInvocation)newStmt.getExpression()).arguments().remove(0);
		((MethodInvocation)newStmt.getExpression()).arguments().add(recExeStmt.getAST().newNumberLiteral(newIndID.toString()+"L"));
		return newStmt;
	}*/
	
	private static ExpressionStatement generateRecordExeStatement(
			AST anAST, long l, int stmtIndex) {
		// locoGP.fitness.ASTInstrumenter.recordExecution(1);
		NumberLiteral nL = anAST.newNumberLiteral(l + "L");
		NumberLiteral stmtNL = anAST.newNumberLiteral(Integer
				.toString(stmtIndex));

		MethodInvocation mI = anAST.newMethodInvocation();
		SimpleName mN = anAST.newSimpleName("recordExecution");

		mI.setName(mN); // recordExecution()
		mI.arguments().add(nL); // recordExecution(1) // indID
		mI.arguments().add(stmtNL); // recordExecution(1,0) // indID, stmtIndex

		SimpleName pN1 = anAST.newSimpleName("ASTInstrumenter");
		SimpleName pN2 = anAST.newSimpleName("stmtCount");
		SimpleName pN3 = anAST.newSimpleName("fitness");
		SimpleName pN4 = anAST.newSimpleName("locoGP");

		QualifiedName qN = anAST.newQualifiedName(pN4, pN3); // locoGP.fitness
		QualifiedName qN2 = anAST.newQualifiedName(qN, pN2); // locoGP.fitness.stmtCount
		QualifiedName qN3 = anAST.newQualifiedName(qN2, pN1);// locoGP.fitness.stmtCount.ASTInstrumenter

		mI.setExpression(qN3); // locoGP.fitness.ASTInstrumenter.recordExecution(1,0)

		ExpressionStatement theStatement = anAST.newExpressionStatement(mI);

		return theStatement;
	}

	public static void recordExecution(long individualID,
			int stmtIndex) {
		ProgramStmtExecutionProfile indVals = results
				.get(individualID);
		if(indVals==null){
			indVals = new ProgramStmtExecutionProfile(individualID);
		}
		indVals.recordExecution(stmtIndex);
		results.put(individualID, indVals);
		if (Generation.originalIndividual != null
				&& indVals.getTotal() > Generation.originalIndividual
						.getRunningTime() * 2.5
				&& Individual.getIndIDGlobal() > 0)
			throw new IllegalStateException(
					"Individual execution count out of allowed range (This is a GP timeout)");
	}
	
	// http://www.programcreek.com/2012/06/insertadd-statements-to-java-source-code-by-using-eclipse-jdt-astrewrite/
	/*private void AddStatements() throws MalformedTreeException, BadLocationException, CoreException {
		 
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testAddComments");
		IJavaProject javaProject = JavaCore.create(project);
		IPackageFragment package1 = javaProject.getPackageFragments()[0];
 
		// get first compilation unit
		ICompilationUnit unit = package1.getCompilationUnits()[0];
 
		// parse compilation unit
		CompilationUnit astRoot = parse(unit);
 
		// create a ASTRewrite
		AST ast = astRoot.getAST();
		ASTRewrite rewriter = ASTRewrite.create(ast);
 
		// for getting insertion position
		TypeDeclaration typeDecl = (TypeDeclaration) astRoot.types().get(0);
		MethodDeclaration methodDecl = typeDecl.getMethods()[0];
		Block block = methodDecl.getBody();
 
		// create new statements for insertion
		MethodInvocation newInvocation = ast.newMethodInvocation();
		newInvocation.setName(ast.newSimpleName("add"));
		Statement newStatement = ast.newExpressionStatement(newInvocation);
 
		//create ListRewrite
		ListRewrite listRewrite = rewriter.getListRewrite(block, Block.STATEMENTS_PROPERTY);
		listRewrite.insertFirst(newStatement, null);
 
		TextEdit edits = rewriter.rewriteAST();
 
		// apply the text edits to the compilation unit
		Document document = new Document(unit.getSource());
 
		edits.apply(document);
 
		// this is the code for adding statements
		unit.getBuffer().setContents(document.get());
	}*/
	
/**
 * Remove the instrumentation statements
 * @param ind
 */
/*	public synchronized static void deInstrumentForOverallExecutionCount(Individual ind) {
		List<Statement> allStmts = ind.gpMaterial.getStatements();
		
		for(Statement stmt: allStmts){
			if(stmt.toString().contains("locoGP.fitness.stmtCount")){
				ASTNode parentBlock = stmt.getParent();
				if(parentBlock instanceof Block){
					Block aBlock = (Block)parentBlock;
					int index = aBlock.statements().indexOf(stmt);
					aBlock.statements().remove(index);
				}
			}
		}
		results.remove(ind.getID());
	}*/
	
	public static void deInstrumentForOverallExecutionCount(Individual ind) {
		ASTRewrite rewriter;
		for (CompilationDetail cD : ind.ASTSet.getCompilationList()) {
			rewriter = cD.getRewriter();
			for(Statement stmt : cD.getInstrumentationStatements()){
				rewriter.remove(stmt, null); // aaah much better :)
			}
			// cD.clearInstrumentationStatements();
		}
	}

	public static long getExecutionCount(long individualID) {
		ProgramStmtExecutionProfile indVals = results
				.get(individualID);
		return indVals.getTotal();
	}
	
	public static ProgramStmtExecutionProfile getExecutionProfile(long individualID) {
		return results.get(individualID);
	}

	public static void zeroExecutionCount(long individualID) {
		results.put(individualID, new ProgramStmtExecutionProfile(
				individualID));
	}

}


