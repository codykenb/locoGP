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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
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

// TODO restructure this whole program, an individual should contain all information needed for mutation

public class NodeOperators {

	private static Random generator = new Random();
	private static GPMaterialVisitor GPPrimitives;
	private static float nodeSelectionTournamentSizeRatio = .1f;

	public static void initialise(CompilationSet initialCodeSet) {
		GPPrimitives = new GPMaterialVisitor();
		for(CompilationDetail cD : initialCodeSet.getCompilationList())
			initialise(cD.getCodeString());
		GPPrimitives.printAll();
	}
	
	public static void initialise(String initialCode) {
		CompilationUnit indAST = parseSource(initialCode);
		indAST.accept(GPPrimitives);
	}

	public static Individual crossover(Individual parentOne,
			Individual parentTwo, boolean singlePointEnforced,
			boolean pickBestLocation) {

		Individual newInd = parentOne.clone(); // we clone the first parent, modify clone
		int tries = 0 ;
		ASTNode nodeToReplace = null;
		try {
			do {				
				//ref to node in cloned tree which is to be overwritten
				nodeToReplace = pickNodes(newInd.gpMaterial.getAllAllowedNodes(),1,pickBestLocation).get(0);
				
				//Logger.logTrash("Picked node type: "+ ASTNode.nodeClassForType(nodeToReplace.getNodeType()));
				tries++;
				
			} while (tries <100 && ! modifyNodeUsingParent(nodeToReplace, parentTwo, true));
		} catch (Exception e) {
			Logger.logTrash("Couldnt find a statement to put in place of another "
					+ e.getStackTrace().toString());
		}
		if(tries<100){
			newInd.setChanged();
			if(nodeToReplace!=null){
				setChangedFlag(nodeToReplace);
			}
		}
		return newInd;
	}
	
	private static boolean modifyNodeUsingParent(ASTNode nodeToReplace,
			Individual parentTwo, boolean differentParents) {
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
			getExistingGPDataRef( nodeToReplace, replacementNode);
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
		if(differentParents)
			setNewNodeDataParentToOld(nodeToReplace, replacementNode);
	if(successfulXover){
		
		setChangedFlag(replacementNode);
	}
	return successfulXover;
	}

	/*private static void cloneGPDatall(ASTNode nodeToReplace,
			ASTNode replacementNode){
		GPASTNodeData gpData = new GPASTNodeData(nodeToReplace);
		replacementNode.setProperty("gpdata", gpData);
	}*/
	
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
		
		try{
			if(nodeToReplace instanceof Statement || nodeToReplace instanceof VariableDeclarationStatement){
				//CompilationUnit targetIndAST,				Statement nodeToReplace, Statement newStmt
				replaceSingleLine((CompilationUnit)nodeToReplace.getRoot(), (Statement)nodeToReplace,(Statement)newClonedNode);
			} else if (parentToReplaceIn instanceof InfixExpression) {
			replaceNodeIn((InfixExpression) parentToReplaceIn, (Expression)nodeToReplace,(Expression)newClonedNode);
		} else if (parentToReplaceIn instanceof PostfixExpression) {
			replaceNodeIn((PostfixExpression) parentToReplaceIn,(Expression)newClonedNode); 			
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
		} else if (nodeToReplace instanceof VariableDeclarationFragment) {
			// then the child node is a fragment, don't modify
			System.out.println("We don't modify expressions " + nodeToReplace.toString()+" Type: " +nodeToReplace.getNodeType());
			newClonedNode= null;
		//	replaceNodeIn((VariableDeclarationFragment)nodeToReplace , (VariableDeclarationFragment)newClonedNode);*/
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

	private static ASTNode crossoverExpressions(Expression nodeToReplace, Individual parentTwo) {
		// doesnt matter what the expression is, only what its parent is..
		// what node types do expressions exist in?
		ASTNode replacementNode = null;
		ASTNode parentToReplaceIn = nodeToReplace.getParent();
		if (parentToReplaceIn instanceof InfixExpression) {
			replacementNode = replaceNodeIn(
					(InfixExpression) parentToReplaceIn, nodeToReplace,
					parentTwo.gpMaterial.getDifferentRandomPrimitiveClone(nodeToReplace));
		} else if (parentToReplaceIn instanceof PostfixExpression) {
			replacementNode = parentTwo.gpMaterial.getDifferentRandomPrimitiveClone(nodeToReplace);
			replaceNodeIn((PostfixExpression) parentToReplaceIn,(Expression) replacementNode); 			
		} else if (parentToReplaceIn instanceof IfStatement) {
			replacementNode = parentTwo.gpMaterial.getDifferentRandomConditionalClone(nodeToReplace);
			replaceNodeIn((IfStatement) parentToReplaceIn, (Expression) replacementNode);
		} else if (parentToReplaceIn instanceof ForStatement) {
			replacementNode = replaceNodeIn((ForStatement) parentToReplaceIn, nodeToReplace , parentTwo);
		} else if (parentToReplaceIn instanceof WhileStatement) {
			replacementNode = replaceNodeIn((WhileStatement) parentToReplaceIn, nodeToReplace , parentTwo);
		} else if (parentToReplaceIn instanceof ExpressionStatement) {
			replacementNode = replaceNodeIn((ExpressionStatement) parentToReplaceIn, nodeToReplace , parentTwo);
		} else if (parentToReplaceIn instanceof VariableDeclarationFragment) {
				replacementNode = replaceNodeIn((VariableDeclarationFragment) parentToReplaceIn, nodeToReplace , parentTwo);
		} else if (parentToReplaceIn instanceof ArrayAccess) {
			replacementNode = replaceNodeIn((ArrayAccess) parentToReplaceIn, nodeToReplace , parentTwo);
		} else if (parentToReplaceIn instanceof MethodInvocation) {
			replacementNode = replaceNodeIn((MethodInvocation) parentToReplaceIn, nodeToReplace , parentTwo);
		} else if (parentToReplaceIn instanceof Assignment) {
			replacementNode = replaceNodeIn((Assignment) parentToReplaceIn, nodeToReplace , parentTwo);
		} else if (parentToReplaceIn instanceof ReturnStatement) {
			replacementNode = replaceNodeIn((ReturnStatement) parentToReplaceIn, nodeToReplace , parentTwo);
		}else{
			System.out.println("Not sure what to do with " + parentToReplaceIn.toString()+" Type:" +parentToReplaceIn.getNodeType());
		}
		return replacementNode;
	}

	private static ASTNode replaceNodeIn(ReturnStatement parentToReplaceIn,
			Expression nodeToReplace, Individual parentTwo) {
		ASTNode returnNode = parentTwo.gpMaterial.getDifferentRandomPrimitiveClone(nodeToReplace);
		parentToReplaceIn.setExpression((Expression) returnNode);
		return returnNode;
	}

	
	private static void replaceNodeIn(Assignment parentToReplaceIn,
			Expression nodeToReplace, Expression replacementNode) {
		if(parentToReplaceIn.getLeftHandSide().equals(nodeToReplace))
			parentToReplaceIn.setLeftHandSide(replacementNode);
		else
			parentToReplaceIn.setRightHandSide(replacementNode);

	}
	
	private static ASTNode replaceNodeIn(Assignment parentToReplaceIn,
			Expression nodeToReplace, Individual parentTwo) {
		ASTNode returnNode = parentTwo.gpMaterial.getDifferentRandomPrimitiveClone(nodeToReplace);
		if(parentToReplaceIn.getLeftHandSide().equals(nodeToReplace))
			parentToReplaceIn.setLeftHandSide((Expression) returnNode);
		else
			parentToReplaceIn.setRightHandSide((Expression) returnNode);
		return returnNode;
	}

	
	private static void replaceNodeIn(MethodInvocation parentToReplaceIn,
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
	}
	
	private static ASTNode replaceNodeIn(MethodInvocation parentToReplaceIn,
			Expression nodeToReplace, Individual parentTwo) {
		ASTNode returnNode = parentTwo.gpMaterial.getDifferentRandomPrimitiveClone(nodeToReplace);
		int index = parentToReplaceIn.arguments().indexOf(nodeToReplace);
		try{
			if(index>-1){
				parentToReplaceIn.arguments().remove(nodeToReplace);
				parentToReplaceIn.arguments().add(index, returnNode);
			}else
				parentToReplaceIn.setExpression((Expression) returnNode);
		} catch( Exception e){
			System.out.println("Failed attempt to replace method parameter "+nodeToReplace.toString() + " with "+returnNode.toString());
			// Picked the "optionalExpression" of a MethodInvocation e.g. - a[j].compareto(k)
		}
		return returnNode;
	}

	
	private static void replaceNodeIn(ArrayAccess parentToReplaceIn,
			Expression nodeToReplace, Expression replacementNode) {
		if(parentToReplaceIn.getIndex().equals(nodeToReplace))
			parentToReplaceIn.setIndex(replacementNode);
		else
			parentToReplaceIn.setArray(replacementNode);
	}
	
	private static ASTNode replaceNodeIn(ArrayAccess parentToReplaceIn,
			Expression nodeToReplace, Individual parentTwo) {
		ASTNode returnNode = parentTwo.gpMaterial.getDifferentRandomPrimitiveClone(nodeToReplace);
		if(parentToReplaceIn.getIndex().equals(nodeToReplace))
			parentToReplaceIn.setIndex((Expression) returnNode);
		else
			parentToReplaceIn.setArray((Expression) returnNode);
		return returnNode;
	}

	
	private static ASTNode replaceNodeIn(
			VariableDeclarationFragment parentToReplaceIn,
			Expression nodeToReplace, Individual parentTwo) {
		ASTNode returnNode = parentTwo.gpMaterial.getDifferentRandomPrimitiveClone(nodeToReplace);
		parentToReplaceIn.setInitializer((Expression) returnNode);
		return returnNode;
	}

	private static ASTNode replaceNodeIn(ExpressionStatement parentToReplaceIn,
			Expression nodeToReplace, Individual parentTwo) {
		ASTNode returnNode = parentTwo.gpMaterial.getDifferentRandomConditionalClone(nodeToReplace);
		parentToReplaceIn.setExpression((Expression) returnNode);
		return returnNode;
	}


	
	private static ASTNode replaceNodeIn(WhileStatement parentToReplaceIn,
			Expression nodeToReplace, Individual parentTwo) {
		ASTNode returnNode = parentTwo.gpMaterial.getDifferentRandomConditionalClone(nodeToReplace);
		parentToReplaceIn.setExpression((Expression) returnNode);
		return returnNode;
	}

	private static void replaceNodeIn(ForStatement parentToReplaceIn,
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
	}
	
	private static ASTNode replaceNodeIn(ForStatement parentToReplaceIn,
			Expression nodeToReplace, Individual parentTwo) {
		ASTNode returnNode = null;
		if(parentToReplaceIn.getExpression().equals(nodeToReplace)){ // i<10
			returnNode = parentTwo.gpMaterial.getDifferentRandomConditionalClone(nodeToReplace);
			parentToReplaceIn.setExpression((Expression) returnNode);
		}else {
			returnNode =parentTwo.gpMaterial.getDifferentRandomPrimitiveClone(nodeToReplace);
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

	private static ASTNode replaceNodeIn(IfStatement ifToReplaceIn,
			Expression differentRandomConditional) {
		ifToReplaceIn.setExpression(differentRandomConditional);
		return differentRandomConditional;
	}

	private static ASTNode replaceNodeIn(
			PostfixExpression parentToReplaceIn,
			Expression differentRandomPrimitive) {
		parentToReplaceIn.setOperand(differentRandomPrimitive);
		return differentRandomPrimitive;
	}

	private static ASTNode replaceNodeIn(
			InfixExpression parentToReplaceIn, Expression nodeToReplace,
			Expression differentRandomPrimitive) {
		if(parentToReplaceIn.getLeftOperand().equals(nodeToReplace))
			parentToReplaceIn.setLeftOperand(differentRandomPrimitive);
		else
			parentToReplaceIn.setRightOperand(differentRandomPrimitive);
		return differentRandomPrimitive;
	}
	

	private static ASTNode replaceOrInsertStatement(Statement nodeToReplace, Individual parentTwo) {
		ASTNode replacementNode;
		CompilationUnit newIndAST = (CompilationUnit)nodeToReplace.getRoot();
		List<Statement> stmtClones = parentTwo.gpMaterial.getStatementsClones2(newIndAST);
		if ( generator.nextBoolean() && nodeToReplace instanceof Block  ) {
			// 1/4 of the time, insert a line into, instead of replacing a block
			replacementNode = insertSingleLine(newIndAST,
					(Block) nodeToReplace, stmtClones.get(generator
							.nextInt(stmtClones.size())));
		} else{ 
			replacementNode = replaceSingleLine(newIndAST,
				(Statement) nodeToReplace, stmtClones.get(generator
						.nextInt(stmtClones.size())));
		}
		return replacementNode; 
	}

	public static int mutate(Individual newInd, int choice, int numChanges,
			GPConfig gpConfig) {

		List<ASTNode> mutationCandidates = newInd.gpMaterial.getAllAllowedNodes();
		List<ASTNode> mutNodes = null; 

		if (gpConfig.isSinglePointEnforced()) {
			/*
			 * We pick a node and a statement, depending on whether we want to modify the node (replace) or delete or clone a statement
			 * Interesting statements (with high bias) are more likely to be chosen for modification, or cloning to a random location in the program. 
			 */
			mutNodes = pickNodes(mutationCandidates, numChanges,
					gpConfig.isPickBestLocation());
		}		
		if (!(mutNodes.size() == 0) /*&& !(mutStmts.size() == 0)*/) {
			// --------------- which mutator?

			/*
			 * if (choice == 0){ // BCK removed delete 2 Aug 2013
			 * 
			 * PHD if deletion is selected, go nuts. The lower the probability,
			 * the more likely a line is selected for deletion the probability
			 * has the opposite effect for removal...
			 * 
			 * 
			 * setChangedFlags(mutStmts); deleteRandomNode(mutStmts); // pick
			 * one, delete it }else if (choice == 1){
			 */
			
			// choice: 0 = delete, 1 = insert, 2-9 = modify
			setChangedFlags(mutNodes);
			
			if(mutNodes.get(0) instanceof Block){ // 1/50 times this will happen, instead of 1/10 under previous design
				insertRandomStmtInBlock((Block)mutNodes.get(0),newInd);
			}else{
				if (choice < 2 && mutNodes.get(0) instanceof Statement) {
					/* only setting the flag for 1 statement (usually) */
					  
					deleteRandomNode(mutNodes);
				} else
					modifyNodes(mutNodes,newInd);
			}

		} else {
			Logger.logTrash("Nothing to mutate (empty program?");
		}
		return numChanges;
	}

	private static void insertRandomStmtInBlock(Block mutBlock, Individual newInd) {
		Statement stmt = duplicateSingleLineToSameAST(newInd.gpMaterial.getRandomStatement());
		insertSingleLine((CompilationUnit)stmt.getRoot(),
				mutBlock, stmt);
		setChangedFlag(mutBlock);
		//cloneGPData(nodeToReplace, replacementNode);
	}

	private static void setChangedFlags(List<ASTNode> mutStmts) {
		for (ASTNode aNode : mutStmts) {
			setChangedFlag(aNode);
		}
	}

	private static void setChangedFlag(ASTNode astNode) {
		// Set the flag on the data which is attached to this stmt
		GPASTNodeData gpDataToSetFlag = (GPASTNodeData) astNode
				.getProperty("gpdata");
		gpDataToSetFlag.setChangedFlag();
		// set the flag on the data attached to the node in the parent program
		// from which this node was cloned (although it may not be cloned, if we're mutating
		if(gpDataToSetFlag.getParentIndividualNodeData() !=null)
			gpDataToSetFlag.getParentIndividualNodeData().setChangedFlag();
	}


	private static Statement duplicateRandomSingleLineToSameAST( // should this be in GPMaterialVisitor?
			List<ASTNode> mutStmts){
		Statement stmtToCopy = (Statement) mutStmts.get(generator.nextInt(mutStmts.size()));
		return duplicateSingleLineToSameAST(stmtToCopy);
	}
				
	private static Statement duplicateSingleLineToSameAST(Statement mutStmt){
		Statement dupe = duplicateSingleLine(mutStmt, (CompilationUnit)mutStmt.getRoot());
		getExistingGPDataRef(mutStmt,dupe);
		return dupe;
	}
	
	public static void getExistingGPDataRef(ASTNode nodeWithGPData, ASTNode undecoratedNode){
		/*
		 * Take a reference to gpData from an existing node.
		 * Set other nodes down the tree to have new gpData
		 */
		GPASTNodeData tempData = (GPASTNodeData) nodeWithGPData.getProperty("gpdata");
		if(tempData == null){ // conniption
			System.out.println("NodeOperators null GPData found: " + (new Throwable()).getStackTrace());
			Logger.logAll("NodeOperators null GPData found: " + (new Throwable()).getStackTrace());
			undecoratedNode.accept(new GPSubtreeDecoratorIfNullVisitor());
			//System.exit(1); // maybe too far though
		}else
			undecoratedNode.setProperty("gpdata", nodeWithGPData.getProperty("gpdata"));
	}

	private static Statement duplicateSingleLine(Statement mutStmt,
			CompilationUnit targetIndAST) {
		Statement stmtClone = (Statement) ASTNode.copySubtree(
				targetIndAST.getAST(), mutStmt);

		getExistingGPDataRef(mutStmt,stmtClone);
		
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
	}

	

	private static Statement insertSingleLine(CompilationUnit targetIndAST,
			Block aBlock, Statement newStmt) {
		insertStatementInBlock(aBlock, newStmt);
		return newStmt;
	}
	

	private static Statement replaceSingleLine(CompilationUnit targetIndAST,
			Statement nodeToReplace, Statement newStmt) {

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

	private static void insertStatementInBlock(Block aBlock, Statement stmtClone) {
		// TODO Debug this, does this do anything?
		ASTRewrite astRewrite = ASTRewrite.create(aBlock.getAST());
		ListRewrite lrw = astRewrite.getListRewrite(aBlock, Block.STATEMENTS_PROPERTY);
		lrw.insertAt(stmtClone,generator.nextInt(aBlock.statements().size()),null);
	}
	
	private static void replaceStatmentInParentBlock(Block parentBlock,
			Statement nodeToReplace, Statement stmtClone) {
		int replaceLoc = parentBlock.statements().indexOf(nodeToReplace);
		parentBlock.statements().remove(replaceLoc);
		parentBlock.statements().add(replaceLoc, stmtClone);
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

	private static void modifyNodes(List<ASTNode> mutStmts, Individual anInd) {
		Iterator<ASTNode> mutStmtsIter = mutStmts.iterator();
		ASTNode cur;
		while (mutStmtsIter.hasNext()) {
			cur = mutStmtsIter.next();
			Logger.logTrash("\nSelected for modification - "
					+ cur.toString().replace("\n", "").replace("\r", ""));
			modifyNode(cur, anInd);
		}
	}

	private static void modifyNode(ASTNode node, Individual anInd) {
		/*	depending on the node type, we should go in and mess with it?
		 *  or should we replace the node with some other type?
		 *  the node can be a primitive in an expression, or statement 
		 *  (this is crossover with itself?)
		 *  and operator messing
		 */
		
		/* change operators in assignment, postfix or infix expression
		 * otherwise
		 * replace primitives with any other primitive 
		 * replace statements with statements
		 * 
		 */
		setChangedFlag(node);
		if (node instanceof InfixExpression) {
			modifyInfixExpressionOperator((InfixExpression) node);
		} else if (node instanceof Assignment) {
			modifyAssignmentOperator((Assignment) node);
		} else if (node instanceof PostfixExpression) {
			modifypostFixOperator((PostfixExpression) node);
		}else {
			modifyNodeUsingParent(node,	anInd, false);
		}
	}

	private static void modifypostFixOperator(PostfixExpression upExpr) {
		setChangedFlag(upExpr);
		upExpr.setOperator(GPPrimitives
				.getDifferentRandomOperator(upExpr.getOperator()));
	}

	private static void modifyAssignmentOperator(Assignment exp) {
		setChangedFlag(exp);
		exp.setOperator(GPPrimitives.getDifferentRandomOperator(exp
				.getOperator())); 
	}

	private static void modifyInfixExpressionOperator(InfixExpression condExp) {
		setChangedFlag(condExp);
			condExp.setOperator(GPPrimitives.getDifferentRandomOperator(condExp
					.getOperator()));
	}

	private static List<ASTNode> pickNodes(
			// TODO this should be merged with
			// GPMaterialVisitor.getRandomGPNode(),
			// they both do roughly the same thing, 
			// should really be moved to GPMaterialVisitor

			List<ASTNode> mutationCandidates, int numChanges,
			boolean pickBestLocation) {

		if (pickBestLocation)
			return pickBestNodes(mutationCandidates, numChanges);
		else
			return pickNodes(mutationCandidates, numChanges);
	}

	private static List<ASTNode> pickBestNodes(
			List<ASTNode> mutationCandidates, int numChanges) {

		List<ASTNode> returnList = new ArrayList<ASTNode>();

		int numberToPick = (int) (mutationCandidates.size() * nodeSelectionTournamentSizeRatio);
		ArrayList<Integer> indexToPick = new ArrayList<Integer>();
		Integer newInt;
		while (indexToPick.size() < numberToPick) { // get unique set of index's
			newInt = generator.nextInt(mutationCandidates.size());
			if (!indexToPick.contains(newInt)) {
				indexToPick.add(newInt);
			}
		}
		ASTNode returnNode = null;
		try{
		returnNode = mutationCandidates.get(indexToPick.get(0));
		} catch (Exception e){
			System.out.println("Missing node choices");
		}
		ASTNode curNode;
		for (Integer curIndex : indexToPick) { // find best from this tournament
			curNode = mutationCandidates.get(curIndex);
			//bork! problem here with block which doesnt have gpdata, because its parent isnt allowed!
			if (((GPASTNodeData) curNode.getProperty("gpdata"))
					.getProbabilityVal() > ((GPASTNodeData) returnNode
					.getProperty("gpdata")).getProbabilityVal()) {
				returnNode = curNode;
			}
		}

		returnList.add(returnNode);
		return returnList;
	}

	private static List<ASTNode> pickNodes(List<ASTNode> mutationCandidates,
			int numChanges) {

		List<ASTNode> returnList = new ArrayList<ASTNode>();
		if (mutationCandidates.size() > 0) {
			int randNum;
			do {
				randNum = generator.nextInt(mutationCandidates.size());
				if (!returnList.contains(mutationCandidates.get(randNum)))
					returnList.add(mutationCandidates.get(randNum));
			} while (returnList.size() < numChanges);
		}
		return returnList;
	}


	private static void deleteRandomNode(List<ASTNode> mutNodes) {
		// TODO write this so it deletes if,while,for statements without
		// deleting all child statements.
		// Guaranteed to delete a line
		int stmtIndexToDelete = generator.nextInt(mutNodes.size()); 
		System.out.println("Deleting: "
				+ (mutNodes.get(stmtIndexToDelete)).toString()
						.replace("\n", "").replace("\r", ""));
		deleteNode(mutNodes.get(stmtIndexToDelete));
	}

	public static void deleteNode(ASTNode stmt) {
		try {
			stmt.delete();
		} catch (IllegalArgumentException e) { 
			/*
			 * TODO should we change this so it modifies the statements parent,
			 * to allow deletion of this statement leaving the body empty nah...
			 */

			// delete the parent so....
			deleteNode((Statement) stmt.getParent());
		}
	}

	public static CompilationSet parseSource(Individual newInd) {
		return parseSource(newInd.ourProblem.getStrings()); 

	}
	
	public static CompilationSet parseSource(CompilationSet codeSet){
		for(CompilationDetail codeString : codeSet.getCompilationList()){
			codeString.setAST(parseSource(codeString.getCodeString()));
		}
		return codeSet;
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

		// ICompilationUnit icompUnit = (ICompilationUnit)
		// parser.createAST(null); // No!
		CompilationUnit compUnit = (CompilationUnit) parser.createAST(null);

		compUnit.recordModifications();

		return compUnit;
	}

	public static void updateClassName(CompilationDetail compilationDetail, String className, GPMaterialVisitor gpMaterial) {
		CompilationUnit compilationU = compilationDetail.AST;
		int closest =-1, bestIndex = 0, tmp = 0 ;
		for(int i =0; i < compilationU.types().size(); i ++){ //TypeDeclaration tD : (List<TypeDeclaration>) parsedAST.types()){
			// this lark was added when to skip over internal classes, could be allright to remove. 
			tmp = className.compareTo(((TypeDeclaration)compilationU.types().get(i)).getName().toString());
			if(tmp<0)
				tmp = - tmp;
			if( closest<0 || tmp <closest){
				bestIndex = i;
				closest = tmp;
			}
		}
		TypeDeclaration classRef = ((TypeDeclaration) compilationU.types().get(bestIndex));
		String oldName = classRef.getName().toString();
		SimpleName newName = compilationU.getAST().newSimpleName(className);
		classRef.setName(newName);
		updateConstructorNames(compilationU.getAST(), className, oldName, gpMaterial); // this is convoluted
	}

	private static void updateConstructorNames(AST parsedAST,
			String newName, String oldName, GPMaterialVisitor gpMaterial) {
		// goddamn constructors in huffman... 
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
		cloneAST.types().addAll(CompilationUnit.copySubtrees(cloneAST.getAST(), originalAST.types()));
		
		// why?
		cloneAST.setPackage((PackageDeclaration) CompilationUnit.copySubtree( cloneAST.getAST(), originalAST.getPackage()));
		//GPMaterialVisitor.cloneProbabilitiesDownTree(originalAST, cloneAST); // This cloning can be done a couple of ways, and is handled in the Individual object now 
		return cloneAST;
	}
	
	

	public static void updateMethodInvocationClassNames(
			ArrayList<MethodInvocation> methodCalls,
			ArrayList<String> newClassNames, ArrayList<String> originalClassNames) {

		if (!originalClassNames.get(0).contains(newClassNames.get(0))) { // skip if they're the same, ie the original program
			String allOriginal = originalClassNames.toString();
			for (MethodInvocation mI : methodCalls) { // update each method call with teh appropriate name
				if (mI.getExpression() != null	) { // gotcha! bug ->&& allOriginal.contains(mI.getExpression().toString())) {
					/* Optimisation was meant to check if the expression contained a static method call with reference to a classname 
					 * checking if its not null is enough.
					 * we can easily check if the expression contains one of the original names without going through them all.. 
					 */
					for (int i = 0; i < originalClassNames.size(); i++) {
						if (mI.getExpression().toString().contains(originalClassNames.get(i)))
							mI.setExpression(mI.getAST().newSimpleName(newClassNames.get(i)));
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
		if (!originalClassNames.get(0).contains(newClassNames.get(0))) { // skip if they're the same, ie the original program
			for (Type aType : types) {
				if (aType != null) {
					for (int i = 0; i < originalClassNames.size(); i++) {
						if (aType.toString().contains(originalClassNames.get(i)))
						{
							renameType(aType, newClassNames.get(i));
						}
					}
				}
			}
		}
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
