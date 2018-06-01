package locoGP.operators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import locoGP.experiments.GPConfig;
import locoGP.individual.Individual;
import locoGP.problems.CompilationDetail;
import locoGP.problems.CompilationSet;
import locoGP.util.GPBlockVisitor;
import locoGP.util.GPSubtreeDecoratorIfNullVisitor;
import locoGP.util.Logger;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

/*
 * TODO: allow inserting/chaining/removing method calls (this should be done per the node type selected for modification)
 */

/**
 * Raw util functions which handle AST manipulations. (Should not be GP modifications)
 * 
 * @author bck
 *
 */
public class NodeOperators {

	private static Random generator = new Random();
	// public static GPMaterialVisitor GPPrimitives;
	private static float nodeSelectionTournamentSizeRatio = .1f;


	static ASTNode replaceStatementOrExpression(ASTNode nodeToReplace,
			Individual ind, boolean differentGPParents) {
		ASTNode replacementNode =  null;
				
		if (nodeToReplace instanceof Statement) { 
			replacementNode = extractStatement(ind.gpMaterial) ; //replaceStatement((Statement)nodeToReplace, extractStatement(ind.gpMaterial));
		} else if (nodeToReplace instanceof Expression) {
			replacementNode = replaceExpression((Expression)nodeToReplace, ind.gpMaterial);
		}
		if(differentGPParents)
			setNewNodeDataParentToOld(nodeToReplace, replacementNode);
		
		ind.ASTSet.recordReplacement(nodeToReplace, replacementNode);
		
		return replacementNode ; //return differentGPParents;
	}
	
	public static Statement extractStatement(GPMaterialVisitor gpMaterial ) {	
		ASTNode aNode = gpMaterial.selectStatement();
		//if (aNode instanceof Statement){
			//System.out.println("statement selected!");
			return (Statement) getClone(aNode.getAST(), aNode);
		/*}else{
			Expression anExpr = (Expression) getClone(aNode.getAST(), aNode);
			return anExpr.getAST().newExpressionStatement( anExpr );
		}*/
	}
	/*private static ASTNode replaceStatement(Statement nodeToReplace,
			ASTNode replacementNode) {
		CompilationUnit newIndAST = (CompilationUnit)nodeToReplace.getRoot();
		Statement stmtClone = (Statement) getClone(newIndAST, replacementNode);
			replacementNode = replaceStatement(newIndAST,
				(Statement) nodeToReplace, stmtClone);
		return replacementNode; 
	}*/

	/*private static boolean modifyNodeUsingParent(ASTNode nodeToReplace,
			Individual parentTwo, boolean differentGPParents) {
		
		ASTNode replacementNode =  nodeToReplace;
		boolean successfulXover = true;
		
		if(nodeToReplace == null){
			System.out.println("trying to replace null!! ");
		}
		
		if (nodeToReplace instanceof Statement) { 
			replacementNode = replaceOrInsertStatement((Statement)nodeToReplace, parentTwo );
		} else if (nodeToReplace instanceof Expression) {
			replacementNode = crossoverExpressions((Expression)nodeToReplace, parentTwo);
		} else { 
			Logger.logTrash("Couldnt find a statement to put in place of another one"
				+ nodeToReplace.toString());
			successfulXover = false;
		}
		
		// TODO still throwing errors trying to clone data (why?)
		try{
			cloneExistingGPDataRef( nodeToReplace, replacementNode);
		}catch (Exception e){
			// cause either node or replacement are null
			if(nodeToReplace == null)
				System.out.println("nodeToReplace is null");
			else if (replacementNode == null)
				System.out.println("replacementNode is null");
			else
				System.out.println("Problem cloning GPData from " + nodeToReplace.toString() + " to " + replacementNode.toString());
			return false;
		}
		if(differentGPParents)
			setNewNodeDataParentToOld(nodeToReplace, replacementNode);
	return successfulXover;
	}*/

	/*private static void cloneGPDatall(ASTNode nodeToReplace,
			ASTNode replacementNode){
		GPASTNodeData gpData = new GPASTNodeData(nodeToReplace);
		replacementNode.setProperty("gpdata", gpData);
	}*/
	
	// TODO extract the biasData structuring functionality 
	private static void setNewNodeDataParentToOld(ASTNode nodeToReplace,
			ASTNode replacementNode) {
		/*
		 * We have the parent tree, and the child tree
		 * Each node data in child tree has a reference to the parallel node data in parent
		 * When replacing a node in the child, the new node data should keep a reference to the parallel node in parent 
		 */
		GPASTNodeData oldGPData = (GPASTNodeData) nodeToReplace.getProperty("gpdata");
		GPASTNodeData newGPData = (GPASTNodeData) replacementNode.getProperty("gpdata");
		newGPData.setParentIndividualNodeData(oldGPData.getParentIndividualNodeData());

	}


	public static ASTNode replaceNode(ASTNode nodeToReplace,
			ASTNode replacementNode2) {
		// generated for experiments.ExhaustiveChange
		
		// clone the replacementNode for the new tree
		ASTNode newClonedNode = ASTNode.copySubtree(nodeToReplace.getAST(), replacementNode2);
		
		// get the parent node, of the node being replaced 
		ASTNode parentToReplaceIn = nodeToReplace.getParent();
		
		/* blocks, statements
		 */
		
		try{ //http://www.docjar.org/html/api/org/eclipse/jdt/core/dom/ASTNode.java.html
			if(nodeToReplace instanceof Statement || nodeToReplace instanceof VariableDeclarationStatement){
				//CompilationUnit targetIndAST,				Statement nodeToReplace, Statement newStmt
				replaceStatement((Statement)nodeToReplace,(Statement)newClonedNode);
			} else if (parentToReplaceIn instanceof InfixExpression) {
				replaceNodeIn((InfixExpression) parentToReplaceIn, (Expression)nodeToReplace,(Expression)newClonedNode);
			} else if (parentToReplaceIn instanceof PostfixExpression) {
				replaceNodeIn((PostfixExpression) parentToReplaceIn,(Expression)newClonedNode);
			} else if (parentToReplaceIn instanceof PrefixExpression) {
				replaceNodeIn((PrefixExpression) parentToReplaceIn,(Expression)newClonedNode);
			} else if (parentToReplaceIn instanceof IfStatement) {
				replaceNodeIn((IfStatement) parentToReplaceIn, (Expression)newClonedNode);
			} else if (parentToReplaceIn instanceof ForStatement) {
				replaceNodeIn((ForStatement) parentToReplaceIn, (Expression)nodeToReplace, (Expression)newClonedNode);
			} else if (parentToReplaceIn instanceof WhileStatement) {
				(( WhileStatement)parentToReplaceIn).setExpression((Expression)newClonedNode);
			//replaceNodeIn((WhileStatement) parentToReplaceIn, (Expression)nodeToReplace, (Expression)newClonedNode);
			} else if (parentToReplaceIn instanceof ExpressionStatement) {
				((ExpressionStatement)parentToReplaceIn).setExpression((Expression)newClonedNode);
			//replaceNodeIn((ExpressionStatement) parentToReplaceIn, (Expression)nodeToReplace, (Expression)newClonedNode);
			} else if (parentToReplaceIn instanceof VariableDeclarationFragment) {
			// this could be left or right-hand-side
				if(((VariableDeclarationFragment)parentToReplaceIn).getInitializer().toString().compareTo(nodeToReplace.toString())==0)
					((VariableDeclarationFragment)parentToReplaceIn).setInitializer((Expression)newClonedNode);
			else
				((VariableDeclarationFragment)parentToReplaceIn).setName((SimpleName)newClonedNode);		
			//replaceNodeIn((VariableDeclarationFragment) parentToReplaceIn, nodeToReplace , parentTwo);
			} else if (parentToReplaceIn instanceof ArrayAccess) {
				replaceNodeIn((ArrayAccess) parentToReplaceIn, (Expression)nodeToReplace , (Expression)newClonedNode);
			} else if (parentToReplaceIn instanceof MethodInvocation) {
				replaceNodeIn((MethodInvocation) parentToReplaceIn,(Expression)nodeToReplace , (Expression)newClonedNode);
			} else if (parentToReplaceIn instanceof Assignment) {
				replaceNodeIn((Assignment) parentToReplaceIn, (Expression)nodeToReplace , (Expression)newClonedNode);
			} else if (parentToReplaceIn instanceof ReturnStatement) {
				((ReturnStatement)parentToReplaceIn).setExpression((Expression) newClonedNode);
			//replaceNodeIn((ReturnStatement) parentToReplaceIn, nodeToReplace , parentTwo);
			} else if (parentToReplaceIn instanceof ConditionalExpression) { // type 16 
				((ConditionalExpression)nodeToReplace).setExpression((Expression) newClonedNode);
		//	replaceNodeIn((VariableDeclarationFragment)nodeToReplace , (VariableDeclarationFragment)newClonedNode);*/
			} else if (parentToReplaceIn instanceof CastExpression) { // type 11 
				((CastExpression)nodeToReplace).setExpression((Expression) newClonedNode);
			}else{
				System.out.println("Not sure what to do with " + parentToReplaceIn.toString()+" Type: " +parentToReplaceIn.getNodeType());
				newClonedNode= null;
			}
		}catch(Exception e){
			System.out.println("Type mismatch: replacing "+nodeToReplace.toString() +" with " + newClonedNode.toString());
			newClonedNode= null;
		}
		
		
		return newClonedNode;
	}
	
	
	/*private static void replaceNodeIn(
			VariableDeclarationFragment nodeToReplace,
			VariableDeclarationFragment newClonedNode) {
		int replaceLoc = parentToReplaceIn.fragments().indexOf(nodeToReplace);
		parentToReplaceIn.fragments().remove(replaceLoc);
		parentToReplaceIn.fragments().add(replaceLoc, newClonedNode);
	}*/

	private static ASTNode replaceExpression(Expression nodeToReplace, GPMaterialVisitor gpMaterial) {
		ASTNode replacementNode = null;
		ASTNode parentToReplaceIn = nodeToReplace.getParent();
		Expression newExpression = (Expression)getClone(nodeToReplace.getAST(), gpMaterial.selectDifferentExpression(nodeToReplace));
		Expression newConditional = (Expression)getClone(nodeToReplace.getAST(),gpMaterial.selectDifferentConditional(nodeToReplace));
		
		if( doesExpressionRequireConditional(parentToReplaceIn, nodeToReplace, newConditional))
			replacementNode=newConditional;
		//if(replaceExpressionRequiringExpression(parentToReplaceIn, nodeToReplace, newExpression))
		else
			replacementNode=newExpression; 
		return replacementNode;
	}
	
	private static boolean doesExpressionRequireConditional(
			ASTNode parentToReplaceIn, Expression nodeToReplace,
			Expression newConditional) {
		if (parentToReplaceIn instanceof IfStatement||parentToReplaceIn instanceof WhileStatement) {
			return true;	
		} else {
			// System.out.println("Not sure what to do with (Conditional) " + parentToReplaceIn.toString()+" Type: " +parentToReplaceIn.getNodeType());
			return false;
		}
	}

	private static boolean replaceExpressionRequiringExpression(
			ASTNode parentToReplaceIn, Expression nodeToReplace,
			Expression newExpression) {
		
		ASTNode returnNode = replaceNode(nodeToReplace, newExpression );
		
		if(returnNode == null )
			return false;
		else
			return true;
		/*if (parentToReplaceIn instanceof InfixExpression) {
			replaceNodeIn((InfixExpression) parentToReplaceIn, nodeToReplace, newExpression);
		} else if (parentToReplaceIn instanceof PostfixExpression) {
			replaceNodeIn((PostfixExpression) parentToReplaceIn, newExpression);
		} else if (parentToReplaceIn instanceof ForStatement) {
			replaceNodeIn((ForStatement) parentToReplaceIn, nodeToReplace, newExpression);
		} else if (parentToReplaceIn instanceof ExpressionStatement) {
			replaceNodeIn((ExpressionStatement) parentToReplaceIn, newExpression);
		} else if (parentToReplaceIn instanceof VariableDeclarationFragment) {
			replaceNodeIn((VariableDeclarationFragment) parentToReplaceIn, nodeToReplace, newExpression);
		} else if (parentToReplaceIn instanceof ArrayAccess) {
			replaceNodeIn((ArrayAccess) parentToReplaceIn, nodeToReplace, newExpression);
		} else if (parentToReplaceIn instanceof MethodInvocation) {
			replaceNodeIn((MethodInvocation) parentToReplaceIn, nodeToReplace, newExpression);
		} else if (parentToReplaceIn instanceof Assignment) {
			replaceNodeIn((Assignment) parentToReplaceIn, nodeToReplace, newExpression);
		} else if (parentToReplaceIn instanceof ReturnStatement) {
			replaceNodeIn((ReturnStatement) parentToReplaceIn, newExpression);
		} else return false;
		return true;
		*/
	}

	private static ASTNode replaceNodeIn(ReturnStatement parentToReplaceIn,
			Expression newExpression) {
		parentToReplaceIn.setExpression((Expression) newExpression);
		return newExpression;
	}

	
	private static Expression replaceNodeIn(Assignment parentToReplaceIn,
			Expression nodeToReplace, Expression replacementNode) {
		if(parentToReplaceIn.getLeftHandSide().equals(nodeToReplace))
			parentToReplaceIn.setLeftHandSide(replacementNode);
		else
			parentToReplaceIn.setRightHandSide(replacementNode);
		return replacementNode;
	}
	
	/*private static ASTNode replaceNodeIn(Assignment parentToReplaceIn,
			Expression nodeToReplace, Individual parentTwo) {
		ASTNode returnNode = getClone(nodeToReplace.getAST(), parentTwo.gpMaterial.selectDifferentExpression(nodeToReplace));
		if(parentToReplaceIn.getLeftHandSide().equals(nodeToReplace))
			parentToReplaceIn.setLeftHandSide((Expression) returnNode);
		else
			parentToReplaceIn.setRightHandSide((Expression) returnNode);
		return returnNode;
	}*/

	
	private static Expression replaceNodeIn(MethodInvocation parentToReplaceIn,
			Expression nodeToReplace, Expression  replacementNode) {
		int index = parentToReplaceIn.arguments().indexOf(nodeToReplace);
		try{
			if(index>-1){
				parentToReplaceIn.arguments().remove(nodeToReplace);
				parentToReplaceIn.arguments().add(index, replacementNode);
			}else
				parentToReplaceIn.setExpression(replacementNode);
		} catch( Exception e){
			System.out.println("Failed attempt to replace method parameter "+nodeToReplace.toString() + " with "+replacementNode.toString());
			// Picked the "optionalExpression" of a MethodInvocation e.g. - a[j].compareto(k)
		}
		return replacementNode;
	}

	
	private static Expression replaceNodeIn(ArrayAccess parentToReplaceIn,
			Expression nodeToReplace, Expression replacementNode) {
		if(parentToReplaceIn.getIndex().equals(nodeToReplace))
			parentToReplaceIn.setIndex(replacementNode);
		else
			parentToReplaceIn.setArray(replacementNode);
		
		return replacementNode;
	}
	
/*	private static ASTNode replaceNodeIn(ArrayAccess parentToReplaceIn,
			Expression nodeToReplace, Individual parentTwo) {
		ASTNode returnNode = getClone(nodeToReplace.getAST(), parentTwo.gpMaterial.selectDifferentExpression(nodeToReplace));
		if(parentToReplaceIn.getIndex().equals(nodeToReplace))
			parentToReplaceIn.setIndex((Expression) returnNode);
		else
			parentToReplaceIn.setArray((Expression) returnNode);
		return returnNode;
	}*/

	
	private static ASTNode replaceNodeIn(
			VariableDeclarationFragment parentToReplaceIn,
			Expression nodeToReplace, Expression newExpression) {
		parentToReplaceIn.setInitializer(newExpression);
		return newExpression;
	}

	private static ASTNode replaceNodeIn(ExpressionStatement parentToReplaceIn, Expression newExpression) {
		//ASTNode returnNode = getClone(nodeToReplace.getAST(), parentTwo.gpMaterial.selectDifferentConditional(nodeToReplace));
		//ASTNode returnNode = parentTwo.gpMaterial.getDifferentRandomConditionalClone(nodeToReplace);
		parentToReplaceIn.setExpression((Expression) newExpression);
		return newExpression;
	}


	
	private static ASTNode replaceNodeIn(WhileStatement parentToReplaceIn,
			 Expression newConditional) {
		//ASTNode returnNode = parentTwo.gpMaterial.getDifferentRandomConditionalClone(nodeToReplace);
		parentToReplaceIn.setExpression((Expression) newConditional);
		return newConditional;
	}

	private static ASTNode replaceNodeIn(ForStatement parentToReplaceIn,
			Expression nodeToReplace, Expression replacementNode) {
		if(parentToReplaceIn.getExpression().equals(nodeToReplace)){ // i<10
			parentToReplaceIn.setExpression(replacementNode);
		}else {
			if(parentToReplaceIn.initializers().contains(nodeToReplace)){ // try it :/
				parentToReplaceIn.initializers().remove(nodeToReplace);
				parentToReplaceIn.initializers().add(replacementNode);
			}else{
				parentToReplaceIn.updaters().remove(nodeToReplace);
				parentToReplaceIn.updaters().add(replacementNode);
			}
		}
		return replacementNode;
	}
	
	private static ASTNode replaceNodeIn(ForStatement parentToReplaceIn,
			Expression nodeToReplace, Individual parentTwo) {
		ASTNode returnNode = null;
		if(parentToReplaceIn.getExpression().equals(nodeToReplace)){ // i<10
			returnNode = getClone(nodeToReplace.getAST(), parentTwo.gpMaterial.selectDifferentConditional(nodeToReplace));
			//returnNode = parentTwo.gpMaterial.getDifferentRandomConditionalClone(nodeToReplace);
			parentToReplaceIn.setExpression((Expression) returnNode);
		}else {
			returnNode = getClone(nodeToReplace.getAST(), parentTwo.gpMaterial.selectDifferentExpression(nodeToReplace));
			//returnNode =parentTwo.gpMaterial.getDifferentRandomExpressionClone(nodeToReplace);
			if(parentToReplaceIn.initializers().contains(nodeToReplace)){ // try it :/
				parentToReplaceIn.initializers().remove(nodeToReplace);
				parentToReplaceIn.initializers().add(returnNode);
			}else{
				parentToReplaceIn.updaters().remove(nodeToReplace);
				parentToReplaceIn.updaters().add(returnNode);
			}
		}
		return returnNode;
	}

	private static ASTNode replaceNodeIn(IfStatement ifToReplaceIn,Expression newConditional){
			ifToReplaceIn.setExpression(newConditional);
		return newConditional;
	}

	private static ASTNode replaceNodeIn(
			PostfixExpression parentToReplaceIn,
			GPMaterialVisitor gpMaterial) {
		Expression newExpression = (Expression)getClone(parentToReplaceIn.getAST(),gpMaterial.selectDifferentExpression(parentToReplaceIn.getOperand()));
		return replaceNodeIn(parentToReplaceIn, newExpression);
	}
	
private static ASTNode replaceNodeIn(
				PostfixExpression parentToReplaceIn,
			Expression newExpression){		
		parentToReplaceIn.setOperand(newExpression);
		return newExpression;
	}

	private static ASTNode replaceNodeIn(
			InfixExpression parentToReplaceIn, Expression nodeToReplace,
			GPMaterialVisitor gpMaterial) {
		Expression newExpression = (Expression)getClone(nodeToReplace.getAST(),gpMaterial.selectDifferentExpression(nodeToReplace));
		return replaceNodeIn(parentToReplaceIn, nodeToReplace, newExpression);
	}
	private static ASTNode replaceNodeIn(InfixExpression parentToReplaceIn,
			Expression nodeToReplace, Expression newExpression) {
		
		if(parentToReplaceIn.getLeftOperand().equals(nodeToReplace))
			parentToReplaceIn.setLeftOperand(newExpression);
		else
			parentToReplaceIn.setRightOperand(newExpression);
		return newExpression;
	}
	
	private static ASTNode replaceNodeIn(
			PrefixExpression parentToReplaceIn,
		Expression newExpression){		
	parentToReplaceIn.setOperand(newExpression);
	return newExpression;
}

/*	private static ASTNode replaceOrInsertStatement(Statement nodeToReplace, Individual parentTwo) {
		ASTNode replacementNode;
		CompilationUnit newIndAST = (CompilationUnit)nodeToReplace.getRoot();
		Statement stmtClone = (Statement) getClone(newIndAST, parentTwo.gpMaterial.extractStatement());
		if ( generator.nextBoolean() && nodeToReplace instanceof Block  ) {
			// 1/4 of the time, insert a line into, instead of replacing a block
			replacementNode = insertSingleLine(newIndAST,
					(Block) nodeToReplace, stmtClone);
		} else{ 
			replacementNode = replaceStatement(newIndAST,
				(Statement) nodeToReplace, stmtClone);
		}
		return replacementNode; 
	}*/

	

	static ASTNode insertRandomStmtInBlock(Block mutBlock, Individual newInd) {
		
		Statement stmtClone = extractStatement(newInd.gpMaterial); //duplicateSingleLineToSameAST(newInd.gpMaterial.extractStatement());
		insertStatementInBlock(mutBlock, stmtClone);
		/*insertSingleLine((CompilationUnit)stmt.getRoot(),
				mutBlock, stmt);*/
		//cloneGPData(nodeToReplace, replacementNode);
		return stmtClone;
	}

/*	private static void setChangedFlags(List<ASTNode> mutStmts) {
		for (ASTNode aNode : mutStmts) {
			setChangedFlag(aNode);
		}
	}*/

	static void setChangedFlag(ASTNode astNode) {
		// Set the flag on the data which is attached to this stmt
		GPASTNodeData gpDataToSetFlag = (GPASTNodeData) astNode
				.getProperty("gpdata");
		gpDataToSetFlag.setChangedFlag();
		// set the flag on the data attached to the node in the parent program
		// from which this node was cloned (although it may not be cloned, if we're mutating
		if(gpDataToSetFlag.getParentIndividualNodeData() !=null)
			gpDataToSetFlag.getParentIndividualNodeData().setChangedFlag();
	}


	/*private static Statement duplicateRandomSingleLineToSameAST( // should this be in GPMaterialVisitor?
			List<ASTNode> mutStmts){
		Statement stmtToCopy = (Statement) mutStmts.get(generator.nextInt(mutStmts.size()));
		return duplicateSingleLineToSameAST(stmtToCopy);
	}
				
	private static Statement duplicateSingleLineToSameAST(Statement mutStmt){
		Statement dupe = duplicateSingleLine(mutStmt, (CompilationUnit)mutStmt.getRoot());
		cloneExistingGPDataRef(mutStmt,dupe);
		return dupe;
	}*/
	
	public static void cloneExistingGPDataRef(ASTNode nodeWithGPData, ASTNode undecoratedNode){
		/*
		 * Take a reference to gpData from an existing node.
		 * Set other nodes down the tree to have new gpData
		 * TODO clone data down the tree!
		 */
		GPASTNodeData tempData = (GPASTNodeData) nodeWithGPData.getProperty("gpdata");
		if(tempData == null){ // conniption
			//System.out.println("NodeOperators null GPData found: " + (new Throwable()).getStackTrace());
			Logger.logTrash("NodeOperators null GPData found: " + (new Throwable()).getStackTrace());
			undecoratedNode.accept(new GPSubtreeDecoratorIfNullVisitor());
			//System.exit(1); // maybe too far though
		}else
			undecoratedNode.setProperty("gpdata", tempData);
	}

	/*private static Statement duplicateSingleLine(Statement mutStmt,
			CompilationUnit targetIndAST) {
		Statement stmtClone = (Statement) getClone(
				targetIndAST.getAST(), mutStmt);
		
		GPBlockVisitor gpBV = new GPBlockVisitor();
		targetIndAST.accept(gpBV);

		Block bL = gpBV.getBlockList().get(
				generator.nextInt(gpBV.getBlockList().size()));
		// what happens if the block is empty!
		if (bL.statements().size() == 0)
			bL.statements().add(stmtClone); // pow! thats what!
		else
			bL.statements().add(generator.nextInt(bL.statements().size()),
					stmtClone);
		return stmtClone;
	}*/

	

/*	private static Statement insertSingleLine(CompilationUnit targetIndAST,
			Block aBlock, Statement newStmt) {
		insertStatementInBlock(aBlock, newStmt);
		return newStmt;
	}*/
	

	private static Statement replaceStatement(Statement nodeToReplace, Statement newStmt) {
		
		// we need the appropriate ASTRewrite object here.
		
		// could be block, if stmt, for stmt, method declaration
		if (nodeToReplace.getParent() instanceof Block)
			replaceStatmentInParentBlock((Block) nodeToReplace.getParent(),
					nodeToReplace, newStmt);
		else if (nodeToReplace.getParent() instanceof IfStatement)
			replaceStatmentInParentIf((IfStatement) nodeToReplace.getParent(),
					nodeToReplace, newStmt);
		else if (nodeToReplace.getParent() instanceof ForStatement)
			replaceStatmentInParentFor(
					(ForStatement) nodeToReplace.getParent(), nodeToReplace,
					newStmt);
		return newStmt;
	}

	private static void replaceStatmentInParentFor(ForStatement parentFor,
			Statement nodeToReplace, Statement stmtClone) {
		parentFor.setBody(stmtClone);
	}

	private static void replaceStatmentInParentIf(IfStatement parentIf,
			Statement nodeToReplace, Statement stmtClone) {
		parentIf.setThenStatement(stmtClone);
	}

/*	private static void insertStatementInBlock(Block aBlock, Statement stmtClone) {
		System.out.println("Before insert " + aBlock.toString());
		try {
			ASTRewrite astRewrite = ASTRewrite.create(aBlock.getAST());
			ListRewrite lrw = astRewrite.getListRewrite(aBlock,
					Block.STATEMENTS_PROPERTY);
			lrw.insertAt(stmtClone,
					generator.nextInt(aBlock.statements().size()), null);
			astRewrite.rewriteAST();
			System.out.println("Successfully inserted " + stmtClone.toString()
					+ " statement in block (clone line using astrewrite):"
					+ aBlock.toString());
		} catch (Exception e) {
			System.out.println("Random statement in block error: "
					+ aBlock.statements().size() + "\n");
		}
	}*/
	
	private static void insertStatementInBlock(Block aBlock, Statement stmtClone) {
		//System.out.println("Before insert " + aBlock.toString());
		try {
			aBlock.statements().add(generator.nextInt(aBlock.statements().size()),stmtClone);
			/*System.out.println("Successfully inserted " + stmtClone.toString()
					+ " statement in block (clone line using astrewrite):"
					+ aBlock.toString());*/
		} catch (Exception e) {
			/*System.out.println("Random statement in block error: "
					+ aBlock.statements().size() + "\n");*/
		}
	}
	
	private static void replaceStatmentInParentBlock(Block parentBlock,
			Statement nodeToReplace, Statement stmtClone) {
		int replaceLoc = parentBlock.statements().indexOf(nodeToReplace);
		parentBlock.statements().remove(replaceLoc);
		try{
		parentBlock.statements().add(replaceLoc, getClone(stmtClone.getAST(),stmtClone));
		//System.out.println("Successfully replaced statement in parent block");
		} catch (Exception e){
			//System.out.println("ParentBlockException: "+parentBlock.statements().size()+"\n"+parentBlock.toString());
			//System.out.println("Replacing at:"+replaceLoc +" with " + stmtClone.toString());
		}
	}

	/*private static Statement replaceSingleLine(Statement originalStmt,
			CompilationUnit targetIndAST) {
		Statement stmtClone = (Statement) ASTNode.copySubtree(
				targetIndAST.getAST(), originalStmt);

		cloneProbabilities(originalStmt, stmtClone);

		GPBlockVisitor gpBV = new GPBlockVisitor();
		targetIndAST.accept(gpBV);

		if (gpBV.getBlockList().size() > 1
				|| gpBV.getBlockList().get(0).statements().size() > 0) { // want
																			// to
																			// check
																			// that
																			// there
																			// is
																			// at
																			// least
																			// one
																			// block,
																			// with
																			// at
																			// least
																			// 1
																			// statements
			Block bL = gpBV.getBlockList().get(
					generator.nextInt(gpBV.getBlockList().size()));
			while (bL.statements() == null || bL.statements().size() == 0)
				bL = gpBV.getBlockList().get(
						generator.nextInt(gpBV.getBlockList().size()));

			int indexToChange = generator.nextInt(bL.statements().size());
			bL.statements().remove(indexToChange);

			bL.statements().add(indexToChange, stmtClone);
		}
		return stmtClone;
	}*/

	// This method written when we expect to modify many statements (never used)
	/*private static void modifyNodes(List<ASTNode> mutStmts, Individual anInd) {
		Iterator<ASTNode> mutStmtsIter = mutStmts.iterator();
		ASTNode cur;
		while (mutStmtsIter.hasNext()) {
			cur = mutStmtsIter.next();
			Logger.logTrash("\nSelected for modification - "
					+ cur.toString().replace("\n", "").replace("\r", ""));
			modifyNode(cur, anInd);
		}
	}*/
	
	public static void modifypostFixOperator(PostfixExpression upExpr, GPMaterialVisitor gpMaterial) {
		upExpr.setOperator(gpMaterial.selectDifferentOperator(upExpr.getOperator()));
	}

	public static void modifyAssignmentOperator(Assignment exp, GPMaterialVisitor gpMaterial) {
		exp.setOperator(gpMaterial.selectDifferentOperator(exp
				.getOperator())); 
	}

	public static void modifyInfixExpressionOperator(InfixExpression condExp, GPMaterialVisitor gpMaterial) {
			condExp.setOperator(gpMaterial.selectDifferentOperator(condExp
					.getOperator()));
	}

/*	static void deleteRandomNode(ASTNode mutNode) {
		// TODO write this so it deletes if,while,for statements without
		// deleting all child statements.
		// Guaranteed to delete a line
		deleteNode(mutNode);
	}*/

	public static void deleteNode(ASTNode aNode) {
		try {
			if (aNode instanceof Statement)
				aNode.delete(); // ah this can be used to remove any node
			if (aNode instanceof MethodInvocation) {
				Expression innerExpression = ((MethodInvocation) aNode)
						.getExpression();
				replaceExpressionRequiringExpression(aNode.getParent(),
						(MethodInvocation) aNode, innerExpression);
			} else
				deleteNode((Statement) aNode.getParent());
		} catch (Exception e) {
			Logger.logDebugConsole("locoGP when deleting " + aNode.toString()
					+ "\n" + e.getMessage());
		}
	}

	/*public static CompilationSet parseSource(Individual newInd) {
		// TODO This should be in ASTDetail, refactor!
		return parseSource(newInd.ourProblem.getStrings()); 

	}
	
	public static CompilationSet parseSource(CompilationSet codeSet){
		for(CompilationDetail codeString : codeSet.getCompilationList()){
			codeString.setAST(parseSource(codeString.getCodeString()));
		}
		return codeSet;
	}*/
	


	
	private static TypeDeclaration findClassDeclaration(CompilationDetail compilationDetail, GPMaterialVisitor gpMaterial){
		CompilationUnit  compilationU= compilationDetail.getCompilationUnit();
		int closest =-1, bestIndex = 0, tmp = 0 ;
		for(int i =0; i < compilationU.types().size(); i ++){ //TypeDeclaration tD : (List<TypeDeclaration>) parsedAST.types()){
			// this lark was added when to skip over internal classes, could be allright to remove. 
			tmp = compilationDetail.getClassName().compareTo(((TypeDeclaration)compilationU.types().get(i)).getName().toString());
			if(tmp<0)
				tmp = - tmp;
			if( closest<0 || tmp <closest){
				bestIndex = i;
				closest = tmp;
			}
		}
		return ((TypeDeclaration) compilationU.types().get(bestIndex));
	}
	
	public static void updateClassName(CompilationDetail compilationDetail, GPMaterialVisitor gpMaterial) {
		TypeDeclaration classRef = findClassDeclaration(compilationDetail, gpMaterial);
		String oldName = classRef.getName().toString();
		String className = compilationDetail.getClassName();
		SimpleName newName = compilationDetail.getCompilationUnit().getAST().newSimpleName(className);
		classRef.setName(newName);
		updateConstructorNames(compilationDetail.getCompilationUnit().getAST(), className, oldName, gpMaterial); // this is convoluted
	}

	private static void updateConstructorNames(AST parsedAST,
			String newName, String oldName, GPMaterialVisitor gpMaterial) {
		List<MethodDeclaration> constructors = gpMaterial.getConstructors(oldName);
		if(constructors.size() >0){
			for(MethodDeclaration constructor : constructors)
				constructor.setName(parsedAST.newSimpleName(newName));
		}
	}

	public static CompilationUnit[] cloneASTs(CompilationUnit[] originalASTSet){
		CompilationUnit[] returnASTs = new CompilationUnit[originalASTSet.length];
		for(int i = 0 ; i < originalASTSet.length; i++){
			returnASTs[i] = cloneAST(originalASTSet[i]);
		}
		return returnASTs;
	}
	
	public static CompilationUnit cloneAST(CompilationUnit originalAST) {
		AST ast = AST.newAST(AST.JLS3);
		CompilationUnit cloneAST = ast.newCompilationUnit();
		List allNodes = CompilationUnit.copySubtrees(cloneAST.getAST(), originalAST.types());
		cloneAST.types().addAll(allNodes); // needed? yes... ?
		//CompilationUnit.copySubtrees(cloneAST.getAST(), originalAST.types());
		
		// why?
		cloneAST.setPackage((PackageDeclaration) CompilationUnit.copySubtree( cloneAST.getAST(), originalAST.getPackage()));
		//GPMaterialVisitor.cloneProbabilitiesDownTree(originalAST, cloneAST); // This cloning can be done a couple of ways, and is handled in the Individual object now 
		return cloneAST;
	}

	public static void updateMethodInvocationClassNames(
			ArrayList<MethodInvocation> methodCalls,
			ArrayList<String> newClassNames, ArrayList<String> originalClassNames) {
		String originalName;
		String compName;
		if (!originalClassNames.get(0).contains(newClassNames.get(0))) { // skip if they're the same, ie the original program
			String allOriginal = originalClassNames.toString();
			for (MethodInvocation mI : methodCalls) { // update each method call with teh appropriate name
				if (mI.getExpression() != null	) { // gotcha! bug ->&& allOriginal.contains(mI.getExpression().toString())) {
					/* Optimisation was meant to check if the expression contained a static method call with reference to a classname 
					 * checking if its not null is enough.
					 * we can easily check if the expression contains one of the original names without going through them all.. 
					 */
					compName = mI.getExpression().toString();
					for (int i = 0; i < originalClassNames.size(); i++) {
						originalName = originalClassNames.get(i);
						if (compName.equals(originalName) || compName.startsWith(originalName+".") || compName.startsWith(originalName+"_") ){
							mI.setExpression(mI.getAST().newSimpleName(newClassNames.get(i)));
							break;
						}
					}
				}
			}
		}
	}

/*	public static void updateClassInstantiationNames(
			ArrayList<ClassInstanceCreation> classInstantiations, ArrayList<String> newClassNames,
			ArrayList<String> originalClassNames) {
		
		if (!originalClassNames.get(0).contains(newClassNames.get(0))) { // skip if they're the same, ie the original program
			String allOriginal = originalClassNames.toString();
			for (ClassInstanceCreation cI : classInstantiations) { // update each method call with teh appropriate name
				if (cI.getType() != null	&& allOriginal.contains(((SimpleType)cI.getType()).getName().toString())) {
					for (int i = 0; i < originalClassNames.size(); i++) {
						if (((SimpleType)cI.getType()).getName().toString().contains(originalClassNames.get(i)))
							((SimpleType)cI.getType()).setName(cI.getAST().newSimpleName(newClassNames.get(i)));
							//cI.setExpression(cI.getAST().newSimpleName(newClassNames.get(i)));
					}
				}
			}
		}
		
	}

	public static void updateVarDeclarationNames(ArrayList<VariableDeclarationStatement> varDeclarations,
			ArrayList<String> newClassNames, ArrayList<String> originalClassNames) {
		if (!originalClassNames.get(0).contains(newClassNames.get(0))) { // skip if they're the same, ie the original program
			for (VariableDeclarationStatement varDec : varDeclarations) {
				if (varDec.getType() != null) {
					for (int i = 0; i < originalClassNames.size(); i++) {
						if (varDec.getType().toString().contains(originalClassNames.get(i)))
						{
							renameType(varDec.getType(), newClassNames.get(i));
						}
					}
				}
			}
		}
	}*/

	private static void renameType(Type type, String newName) {
		if(type instanceof ArrayType)
			renameType(((ArrayType)type).getComponentType(), newName);
		else if(type instanceof SimpleType)
			((SimpleType)type).setName((type).getAST().newSimpleName(newName));
	}

	public static void updateTypes(ArrayList<Type> types,
			ArrayList<String> newClassNames, ArrayList<String> originalClassNames) {
		String typeStr;
		String origStr;
		if (!originalClassNames.get(0).contains(newClassNames.get(0))) { // skip if they're the same, ie the original program
			for (Type aType : types) {
				if (aType != null) {
					typeStr = aType.toString();
					for (int i = 0; i < originalClassNames.size(); i++) {
						origStr = originalClassNames.get(i);
						if (typeStr.equals(origStr) || typeStr.startsWith(origStr+"_")) // changed from contains!
						{
							renameType(aType, newClassNames.get(i));
						}
					}
				}
			}
		}
	}

	// TODO should be in NodeModifierUtil
	static ASTNode getClone(CompilationUnit indAST, ASTNode nodeToClone){
		ASTNode clonedNode = ASTNode.copySubtree(indAST.getAST(), nodeToClone);
		NodeOperators.cloneExistingGPDataRef( nodeToClone, clonedNode);
		return clonedNode;
	}

	
	static ASTNode getClone(AST indAST, ASTNode nodeToClone){
		ASTNode clonedNode = ASTNode.copySubtree(indAST, nodeToClone);
		NodeOperators.cloneExistingGPDataRef( nodeToClone, clonedNode);
		return clonedNode;
	}

	public static void rename(SimpleName oldName, String newName, ASTRewrite rewriter) {
		/*
		 * MethodInvocation.optionalExpression
		 * QualifiedName.qualifier
		 * TypeDeclaration.typeName
		 * MethodDeclaration.methodName
		 * SimpleType.typeName
		 * ConditionalExpression
		 * 
		 * ConstructorInvocation
		 * ClassInstanceCreation
		 */
		//ASTNode parentASTNode = oldName.getParent();
		SimpleName newSimpleName = oldName.getAST().newSimpleName(newName);
		rewriter.replace(oldName, newSimpleName, null);
		/*if(parentASTNode instanceof SimpleType){
			((SimpleType)parentASTNode).setName(newSimpleName);
		}else if(parentASTNode instanceof MethodInvocation){
			changeNameIn((MethodInvocation)parentASTNode, oldName, newSimpleName);
		}else if(parentASTNode instanceof TypeDeclaration){
			((TypeDeclaration)parentASTNode).setName(newSimpleName);
		}else if(parentASTNode instanceof QualifiedName){
			((QualifiedName)parentASTNode).setQualifier(newSimpleName);
		}else if(parentASTNode instanceof MethodDeclaration){
			//changeNameIn((MethodDeclaration)parentASTNode);
			((MethodDeclaration)parentASTNode).setName(newSimpleName);
		}else if(parentASTNode instanceof ConditionalExpression){
			changeNameIn((ConditionalExpression)parentASTNode, oldName, newSimpleName);
		}*/
		
		/*else if(parentASTNode instanceof Type){
			System.out.println((Type)parentASTNode);//.setType(newSimpleName);
		}else {
			System.out.print("\nError trying to rename "+oldName.toString() + " with " + newName.toString() );
			if(parentASTNode == null)
				System.out.print(" Parent node is null");				
		}*/
	}
	/*private static void changeNameIn(MethodDeclaration parentMethodDeclaration, SimpleName oldName, SimpleName newSimpleName){
		if(parentMethodDeclaration.getName().equals(oldName))
			parentMethodDeclaration.setName(newSimpleName);
		else
			(parent)
	}*/
			
	private static void changeNameIn(ConditionalExpression parentConditionalExpression, SimpleName oldName, SimpleName newSimpleName){
		if(parentConditionalExpression.getElseExpression().equals(oldName))
			parentConditionalExpression.setElseExpression(newSimpleName);
		else if(parentConditionalExpression.getThenExpression().equals(oldName))
			parentConditionalExpression.setThenExpression(newSimpleName);
		else if(parentConditionalExpression.getExpression().equals(oldName))
			parentConditionalExpression.setExpression(newSimpleName);
		else
			throw new RuntimeException("Error trying to rename (ConditionalExpression) "+oldName.toString() + " with " + newSimpleName.toString()); 		
	}
	
	private static void changeNameIn(MethodInvocation parentInvocation, SimpleName oldName, SimpleName newSimpleName){
		if(parentInvocation.getExpression().equals(oldName)){ // qualifier for a static method call
			parentInvocation.setExpression(newSimpleName);
		}else if(parentInvocation.getName().equals(oldName)){ // probably should'nt ever be true
			throw new RuntimeException("We should not be updating a MethodInvocation method name (constructor?)");
		}else
			throw new RuntimeException("Unknown updating a MethodInvocation");
	}

	
	/*
	 * http://publib.boulder.ibm.com/infocenter/rtnlhelp/v6r0m0/index.jsp?topic=%
	 * 2Forg.eclipse.jdt.doc.isv%2Fguide%2Fjdt_api_manip.htm
	 * 
	 * 
	 * // creation of a Document ICompilationUnit cu = ... ; // content is
	 * "public class X {\n}" String source = cu.getBuffer().getContents();
	 * Document document= new Document(source);
	 * 
	 * // creation of DOM/AST from a ICompilationUnit ASTParser parser =
	 * ASTParser.newParser(AST.JLS2); parser.setSource(cu); CompilationUnit
	 * astRoot = (CompilationUnit) parser.createAST(null);
	 * 
	 * // creation of ASTRewrite ASTRewrite rewrite = new
	 * ASTRewrite(astRoot.getAST());
	 * 
	 * // description of the change SimpleName oldName =
	 * ((TypeDeclaration)astRoot.types().get(0)).getName(); SimpleName newName =
	 * astRoot.getAST().newSimpleName("Y"); rewrite.replace(oldName, newName,
	 * null);
	 * 
	 * // computation of the text edits TextEdit edits =
	 * rewrite.rewriteAST(document, cu.getJavaProject().getOptions(true));
	 * 
	 * // computation of the new source code edits.apply(document); String
	 * newSource = document.get();
	 * 
	 * // update of the compilation unit cu.getBuffer().setContents(newSource);
	 */

}
