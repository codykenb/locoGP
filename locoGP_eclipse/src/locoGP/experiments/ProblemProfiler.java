package locoGP.experiments;

// Brendan Cody-Kenny <codykenny@gmail.com>
 

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;


import locoGP.Generation;
import locoGP.experiments.GPConfig;
import locoGP.fitness.IndividualEvaluator;
import locoGP.fitness.stmtCount.StmtCountIndividualEvaluator;
import locoGP.individual.Individual;
import locoGP.problems.Problem;

import locoGP.problems.Sort1Problem; // bubblesort
import locoGP.problems.Sort1CocktailProblem; // depending on the problem you select
import locoGP.problems.Sort1HeapProblem;
import locoGP.problems.Sort1InsertionProblem;
import locoGP.problems.Sort1MergeProblem;
import locoGP.problems.Sort1ProblemVariantSeed;
import locoGP.problems.Sort1QuickProblem;
import locoGP.problems.Sort1RadixProblem;
import locoGP.problems.Sort1Selection2Problem;
import locoGP.problems.Sort1SelectionProblem;
import locoGP.problems.Sort1ShellProblem;
import locoGP.problems.huffmancodebook.HuffmanCodeBookProblem;
import locoGP.problems.scalablesort.Sort1CloneProblem;
import locoGP.problems.scalablesort.Sort1LoopCloneProblem;
import locoGP.problems.scalablesort.Sort1LoopsProblem;
import locoGP.problems.scalablesort.Sort1SegIncProblem;
import locoGP.problems.scalablesort.Sort1SegmentedCloneProblem;

import locoGP.util.Logger;
import locoGP.util.StringCompiler;

public class ProblemProfiler {

	public static void main(String args[]) {
		try{
			realMain(args);
		}catch (Exception e){
			e.printStackTrace();
			Logger.flushLog();
		}
	}
	
	public static void realMain(String args[]) {
		//             popSize, numGens, tournSize, pickBestLocation, updateLocationBias, debug
		
        //int expNum = Integer.parseInt(args[0]);
		
		IndividualEvaluator indEval = new StmtCountIndividualEvaluator();

		Problem[] problems = {new Sort1Problem(), new Sort1HeapProblem(),
				new Sort1ShellProblem(),new Sort1QuickProblem(),
				new Sort1MergeProblem(),new Sort1CocktailProblem(),
				new Sort1RadixProblem(),new Sort1InsertionProblem(),
				new Sort1SelectionProblem(),new Sort1Selection2Problem(),
				new Sort1LoopsProblem(1),new HuffmanCodeBookProblem()};
		
		for( int probInd = 0 ; probInd< problems.length ; probInd++){
			Individual originalIndividual = new Individual(problems[probInd]); 	
			System.out.println("\n"+originalIndividual.getClassName()+"\n");
			Logger.logAll("Our problem is "
					+ originalIndividual.ourProblem.getProblemName());
			indEval.evaluateIndNoTimeLimit(originalIndividual);			
		}
	}
}
	/*// get all blocks in the program
	List<ASTNode> allNodes = originalIndividual.gpMaterial
			.getAllAllowedNodes();
	for (int i = 0; i < allNodes.size(); i++) {
		if (allNodes.get(i) instanceof Block) {
			// add print statements for each line in the block
			addLinePrintStmts((Block) allNodes.get(i));
		}
	}

	Logger.logJavaFile(
			originalIndividual.getClassName(),
			originalIndividual.ASTSet.getCodeListing());
	// run the program
	JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	compiler.run(null, null, null, "-cp", "", originalIndividual.getClassName()+".java");
	Class<?> clazz;
	try {
		clazz = Class.forName(originalIndividual.getClassName());
		
		Method m = clazz.getMethod("getCodeBook", new Class[] {Byte[].class});
		Object[] _args = originalIndividual.ourProblem.getTestData()[0].getTest(); //new Object[] { testArray };
		m.invoke(null, _args);							
		//System.out.println("result " + result);
			
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/

	
	/*static int lineCount =0;	
	
	private static void addLinePrintStmts(Block aBlock) {
		int numLines = aBlock.statements().size();
		AST ourAST = aBlock.getAST();
		
		for (int j = numLines-1; j >= 0; j--) {
			String newPrintStmtString = "System.out.println(\"Line: \"+ "
					+ lineCount + ");";
			MethodInvocation aMI = aBlock.getAST().newMethodInvocation();
			
			aMI.setExpression(ourAST.newQualifiedName(
					ourAST.newSimpleName("System"),
					ourAST.newSimpleName("out")));
			aMI.setName(ourAST.newSimpleName("println"));
			
			StringLiteral literal = ourAST.newStringLiteral();
			literal.setLiteralValue("Line: " + lineCount);
			  
			StringLiteral aPrintStmtNode = aBlock.getAST().newStringLiteral();
			aPrintStmtNode.setLiteralValue(newPrintStmtString);
			
			
			aMI.arguments().add(literal);
			Statement newStmt = aBlock.getAST().newExpressionStatement(aMI);
			aBlock.statements().add(j, newStmt);
			// newStmt.
			lineCount++;
		}
	}	
*/