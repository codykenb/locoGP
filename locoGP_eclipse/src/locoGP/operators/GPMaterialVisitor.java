package locoGP.operators;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import locoGP.experiments.GPConfig;
import locoGP.util.ProbabilityClonerVisitor;


import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;


public class GPMaterialVisitor extends ASTVisitor implements java.io.Serializable{
	/*
	 * This goes down through the AST tree and gathers GP primitives in a useful format.
	 */
	
	private List<ASTNode> allNodes = new ArrayList<ASTNode>(); 
	// keep a reference to all nodes, so we can easily pick one at random
	private List<ASTNode> allAllowedNodes = new ArrayList<ASTNode>(); 
	
	// Operators have special uses, not ASTNodes
	private List<Operator> infixOperators = new ArrayList<Operator>();
	private List<PostfixExpression.Operator> postfixOperators = new ArrayList<PostfixExpression.Operator>();
	
	// Any expression with an operator which returns boolean, or any boolean simpleName 
	private List<InfixExpression> conditionals = new ArrayList<InfixExpression>(); 
	
	// All expression types can be interchanged, no biggie
	private List<Expression> primitives = new ArrayList<Expression>(); 
	private List<Statement> statements = new ArrayList<Statement>();
	
	// stuff which can be extracted as clone
	private List<Expression> extractableStatements = new ArrayList<Expression>(); 
	
	private ArrayList<MethodInvocation> methodInvocations= new ArrayList<MethodInvocation>();
	
	private ArrayList<Type> allTypes = new ArrayList<Type>();
	
	public void purgeRefs() {
		// TODO Auto-generated method stub

		ASTNode tempNode, originalNode;
		GPASTNodeData tempData =null;
		for (int i = 0 ; i < allNodes.size() ; i++) {
			tempData = (GPASTNodeData)allNodes.get(i).getProperty("gpdata");
			allNodes.get(i).setProperty("gpdata", null);
			allNodes.set(i, null);
		}
		allNodes.clear();
		allNodes = null;
		allAllowedNodes.clear();
		allAllowedNodes = null;
		infixOperators.clear();
		infixOperators = null;
		postfixOperators.clear();
		postfixOperators = null;
		conditionals.clear();
		conditionals = null;
		primitives.clear();
		primitives = null;
		statements.clear();
		statements = null;
		extractableStatements.clear();
		extractableStatements = null;
		methodInvocations.clear();
		methodInvocations = null;
		allTypes.clear();
		allTypes = null;
	
	}
	
	public void printAll(){
		
		printAllTypes(allNodes);
		
		System.out.println("\nNodes (Primitives) for modification: \n");
		
		System.out.println("infixOperators");
		printAll(infixOperators);
		
		System.out.println("postfixOperators");
		printAll(postfixOperators);
		
		System.out.println("conditionals");
		printAll(conditionals);
		
		System.out.println("primitives");
		printAll(primitives);
			
		System.out.println("statements");
		printAll(statements);
		
	}
	
	private void printAllTypes(List<ASTNode> nodesList) {
		ArrayList<Integer> types = new ArrayList<Integer>();
		boolean found=false;
		for(ASTNode aNode: nodesList){
			found = false;
			for(Integer aType : types){
				if(aType.compareTo(aNode.getNodeType())==0)
					found = true;
			}
			if(!found){
				types.add(aNode.getNodeType());
			}
			found = false;
		}
		System.out.println("\nAll Types found in current tree: ");
		for(Integer aType: types){
			System.out.println(ASTNode.nodeClassForType(aType).getCanonicalName());
		}
		
		System.out.println("\nAll nodes found in current tree: ");
		
		for(Integer aType: types){
			System.out.println("\nNodes of type --------------: "+ ASTNode.nodeClassForType(aType).getCanonicalName());
			for(ASTNode aNode: nodesList){
				if(aNode.getNodeType() == aType)
					System.out.println(aNode.toString());
			}
		}
	}

	private void printAll(List primList){
		Iterator iOp = primList.iterator();
		while(iOp.hasNext())
			System.out.println(" " +iOp.next().toString());
	}
	
	
	public GPASTNodeData getNodeProperty(int i){
		ASTNode tempNode  =  getNode(i);
		return (GPASTNodeData) tempNode.getProperty("gpdata");
	}
	
	public ASTNode getNode(int i){
		return allAllowedNodes.get(i);
	}
	
	public int getNumGPNodes() {
		return infixOperators.size() + postfixOperators.size()
				+ conditionals.size() + primitives.size()
				//+ numberLiterals.size() + arithmeticExprs.size() + updaters.size() 
				+ statements.size();
	}
	
	private Random generator = new Random(); 
	
	public GPMaterialVisitor(){
		/* 
			This is where we define all operators that are to be used,
			Any that do not exist in the original program should be 
			added here.
		*/ 

		this.infixOperators.add(Operator.toOperator("=="));
		this.infixOperators.add(Operator.toOperator("||"));
		this.infixOperators.add(Operator.toOperator("&&"));
		this.infixOperators.add(Operator.toOperator("<"));
		this.infixOperators.add(Operator.toOperator("<="));
		this.infixOperators.add(Operator.toOperator(">"));
		this.infixOperators.add(Operator.toOperator(">="));
		this.postfixOperators.add(PostfixExpression.Operator.toOperator("--"));
		this.postfixOperators.add(PostfixExpression.Operator.toOperator("++"));
	}

	private static boolean isAllowedType(ASTNode aNode){
		boolean ok = false;
		if (aNode == null) // TODO why is this sometimes null?
			return ok;
		try {
				ok = ( aNode.getNodeType() != 22 && // Integer
				aNode.getNodeType() != 43 && // Comparable
				aNode.getNodeType() != 44 && // SingleVariableDeclaration
				aNode.getNodeType() != 78 && // @Override	
				aNode.getNodeType() != 36 && // CastExpression
				aNode.getNodeType() != 11 && // CastExpression :/
				aNode.getNodeType() != 40 && //
				(
						aNode instanceof SimpleName||
						//aNode instanceof NullLiteral||
						aNode instanceof NumberLiteral||
						aNode instanceof StringLiteral||
						aNode instanceof QualifiedName ||
						aNode instanceof org.eclipse.jdt.core.dom.BooleanLiteral ||
						aNode instanceof VariableDeclarationFragment ||
						isExpressionOrStatement(aNode)
				)
				);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ok;
	}
	
	private static boolean isExpressionOrStatement(ASTNode aNode){
		if (
				!(aNode instanceof SimpleName || 
						aNode instanceof NumberLiteral ||
						aNode instanceof QualifiedName ||
						aNode instanceof org.eclipse.jdt.core.dom.BooleanLiteral ||
						aNode instanceof VariableDeclarationFragment ||
						aNode instanceof StringLiteral)
				&& 
				// aNode instanceof QualifiedName||
				// aNode instanceof ArrayAccess||
				// aNode instanceof StringLiteral||
				// aNode instanceof FieldAccess||
				(aNode instanceof InfixExpression
				||
				// aNode instanceof ParenthesizedExpression||
				// aNode instanceof CastExpression||
				aNode instanceof Expression
				|| aNode instanceof PostfixExpression
				|| aNode instanceof Statement ||
				/*
				 * Block aNode instanceof ExpressionStatement|| aNode instanceof
				 * IfStatement|| aNode instanceof ForStatement ||
				 */
				aNode instanceof VariableDeclarationFragment
				|| aNode instanceof Assignment
				|| aNode instanceof MethodInvocation))
			return true;
		else
			return false;
						
	}
	
	public static boolean allowedType2(ASTNode aNode){
		
		// A block is allowed, regardless if its parent 
		if(aNode instanceof Block)
			return true;
		
		// The Simplename of a method invocation shouldn't be changed.
		if(aNode instanceof SimpleName && aNode.getParent() instanceof MethodInvocation )
			if(((MethodInvocation)aNode.getParent()).getName().equals(aNode))
				return false;
		
		// allowed nodes, with parents which are allowed
		if(GPConfig.useFineGranularityChange()){
			if(isAllowedType(aNode)&& isAllowedType(aNode.getParent()))
				return true;
		}else{
			return isExpressionOrStatement(aNode);
		}
		
		return false; // "if you don't know what it is, don't touch it"
	}
	
	public void preVisit(ASTNode aNode){
		allNodes.add(aNode);
		
		if( allowedType2( aNode) ){ //&& (GPConfig.getLinkNewChildBiasDataWithParent() || GPConfig.getReferenceChildBiasToParentData())){
			GPASTNodeData tmpData = (GPASTNodeData) aNode.getProperty("gpdata");
			if(tmpData == null){
				aNode.setProperty("gpdata", new GPASTNodeData());
			}
			allAllowedNodes.add(aNode);
		}
	}
	// -------------------------------------------------------------Statements:	
	public boolean visit(IfStatement stmt) {
		return visit((Statement) stmt);
	}
	public boolean visit(ExpressionStatement stmt) {
		return visit((Statement) stmt);
	}
	public boolean visit(WhileStatement stmt) {
		return visit((Statement) stmt);
	}
	public boolean visit(ForStatement stmt) {
		return visit((Statement) stmt);
	}
	public boolean visit(ReturnStatement stmt) {
		return visit((Statement) stmt);
	}
	public boolean visit(Block stmt) {
		return visit((Statement) stmt);
	}
	public boolean visit(Statement stmt){ 
		// doesn't override anything. No visit function for statement object
		if(!statements.contains(stmt))
			statements.add(stmt);
		return true;
	}
	
	// -------------------------------------------------------------Expressions:
	public boolean visit(Expression expr){
		if(allowedType2(expr)  &&!this.primitives.contains(expr))
			this.primitives.add(expr);
		return true;
	}

	public boolean visit(NumberLiteral nL){
		visit((Expression)nL);
		return true;
	}
	
	public boolean visit(SimpleName sN){
		//TODO it could be a boolean, which could be used as a conditional, but what are the chances?
		if( !(sN.getParent() instanceof BodyDeclaration)){
			visit((Expression)sN);		
		}
		return true;
	}
	
	public boolean visit(NullLiteral nL){
		visit((Expression)nL);		
		return true;
	}
	
	public boolean visit(QualifiedName expr){
		visit((Expression)expr);		
		return true;
	}
	
	public boolean visit(ArrayAccess expr){
		visit((Expression)expr);		
		return true;
	}
	public boolean visit(StringLiteral expr){
		visit((Expression)expr);		
		return true;
	}
	public boolean visit(FieldAccess expr){
		visit((Expression)expr);		
		return true;
	}
	public boolean visit(ParenthesizedExpression expr){
		visit((Expression)expr);		
		return true;
	}
	public boolean visit(CastExpression expr){
		visit((Expression)expr);		
		return true;
	}

	public boolean visit(InfixExpression infixExpr){
		visit((Expression)infixExpr);
		if(hasBooleanOperator(infixExpr) && !this.conditionals.contains(infixExpr))
			this.conditionals.add(infixExpr);
		if(!this.infixOperators.contains(infixExpr.getOperator()))
			this.infixOperators.add(infixExpr.getOperator());
		/*if(infixExpr.getOperator().toString().contains("+")){
			if(!this.arithmeticExprs.contains(infixExpr))
				arithmeticExprs.add(infixExpr);
		}*/
		return true;
	}
	private boolean hasBooleanOperator(InfixExpression infixExpr) {
		return isBooleanOperator(infixExpr.getOperator());
	}
	private boolean isBooleanOperator(Operator operator) {
		String[] allowedOperators = { "==", "||", "&&", "<", "<=", ">" , ">=","<>","!=" };
		boolean isBoolean = false;
		for(String op : allowedOperators){
			if(operator.toString().compareTo(op)==0)
				isBoolean=true;
		}
		return isBoolean;
	}
	public boolean visit(MethodInvocation expr){
		visit((Expression)expr);
		addToExtractableStmts(expr);
		this.methodInvocations.add(expr);
		return true;
	}
/*	public boolean visit(ClassInstanceCreation classICreation){
		visit((Expression)classICreation);
		this.classInstanceCreations.add(classICreation);
		return true;
	}
	public boolean visit(VariableDeclarationStatement varDecStmt){
		//visit((Expression)varDecStmt);
		this.variableDeclarationStmts.add(varDecStmt);
		return true;
	}*/
	public boolean visit(SimpleType aType){
		return visit((Type)aType);
	}
	public boolean visit(ArrayType aType){
		return visit((Type)aType);
	}
	public boolean visit(Type aType){
		this.allTypes.add(aType);
		return true;
	}
	public boolean visit(PostfixExpression postfixExpr){
		visit((Expression)postfixExpr); 
		// TODO can be used as a new statement for the purposes of cloning
		addToExtractableStmts(postfixExpr);
				
		if(!this.postfixOperators.contains(postfixExpr.getOperator()))
			this.postfixOperators.add(postfixExpr.getOperator());
		/*if(postfixExpr.getOperator().toString().contains("+")){
			if(!this.arithmeticExprs.contains(postfixExpr))
				arithmeticExprs.add(postfixExpr);
		}*/
		return true;
	}
	public boolean visit(Assignment expr){
		visit((Expression)expr);	
		addToExtractableStmts(expr);
		return true;
	}
	
	private void addToExtractableStmts(Expression expr) {
		this.extractableStatements.add(expr);
	}

	public Expression getDifferentRandomConditionalClone(Expression oldExp) {
		Expression newExp ; 
		CompilationUnit indAST = (CompilationUnit)oldExp.getRoot();
		do {
			newExp = (Expression) getRandomClone( indAST, this.conditionals);//getRandomConditionalClone(indAST);
		}while ( oldExp.equals(newExp));
		return newExp;
	}
/*	private Expression getRandomConditionalClone(CompilationUnit indAST) {
		
		 * This method gets things that are for making up left or right hand side of
		 * conditional statements in if/for/while. we should replace these as the are picked 
		 * 
		 * From what we have gathered, this can be a conditional or a primitive
		 
		//int choice = generator.nextInt(3); // 0, 1 or 2
		Expression originalExpr = conditionals.get(generator.nextInt(conditionals.size()));
		Expression cloneExpr = (Expression) ASTNode.copySubtree(indAST.getAST(), originalExpr);
		cloneProbabilities(originalExpr, cloneExpr);
		//if( choice == 1){
			
		}else {
			newExpr = getRandomSimpleOperand(indAST);
		}
		return cloneExpr;
	}*/

	public Operator getDifferentRandomInfixOperator(Operator oldOp) {
		Operator newOp ; 
		do {
			newOp = infixOperators.get(generator.nextInt(infixOperators.size()));
		}while ( newOp.equals(oldOp));
		return newOp;
	}
	
	public List<Statement> getStatementsClones2(CompilationUnit cloneAST) { 
		ArrayList<Statement> returnList = new ArrayList<Statement>();
		for(Statement originalStmt : this.statements){
			Statement newStmt = (Statement) CompilationUnit.copySubtree(cloneAST.getAST(), originalStmt);
			NodeOperators.getExistingGPDataRef( originalStmt, newStmt);
			//cloneProbabilities(originalStmt, newStmt);
			returnList.add(newStmt);
		}
		
		for(Expression clonableExpr :this.extractableStatements ){
			Expression newExpr = (Expression) CompilationUnit.copySubtree(cloneAST.getAST(), clonableExpr);
			Statement newStmt = cloneAST.getAST().newExpressionStatement( newExpr );
			//cloneProbabilities(clonableExpr, newExpr);
			NodeOperators.getExistingGPDataRef( clonableExpr, newExpr);
			NodeOperators.getExistingGPDataRef( clonableExpr, newStmt);
			returnList.add( newStmt );
		}
		return returnList;
	}
	
	// This returns the actual objects, not clones! 
	// Use for mutation or directly manipulating the relevant individual 
	public List<Statement> getStatements() { 
		ArrayList<Statement> returnList = new ArrayList<Statement>();
		returnList.addAll(this.statements);
		return returnList;
	}
	
	public List<ASTNode> getStatementsAsNodes() { 
		ArrayList<ASTNode> returnList = new ArrayList<ASTNode>();
		returnList.addAll(this.statements);
		return returnList;
	}

	/*	public ASTNode getRandomGPNode(boolean pickBestLocation) {
		ASTNode returnNode = allAllowedNodes.get(generator.nextInt(allAllowedNodes.size()));
		ArrayList<ASTNode> returnNodes = new ArrayList<ASTNode>(); // we can extend this for multiple nodes in future
		if ( pickBestLocation ){ // implement tournament selection
			ASTNode curNode =null;
			do{
			 curNode = allAllowedNodes.get(generator.nextInt(allAllowedNodes.size()));
			} while (curNode instanceof MethodDeclaration);
			
			 if( ((GPASTNodeData)curNode.getProperty("gpdata")).getModifyProbability() > ((GPASTNodeData)returnNode.getProperty("gpdata")).getModifyProbability()) {
					returnNode = curNode;
			}
			returnNodes.add(returnNode);
		}else{
			returnNodes.add(returnNode);
		}
		return returnNodes.get( generator.nextInt(returnNodes.size() ));
	}*/

	public List<ASTNode> getAllAllowedNodes() {
		return this.allAllowedNodes;
	}
	

	
/*	public static void cloneProbabilitiesDownTreeNo(ASTNode originalNode,
			ASTNode targetNode) {
		
		 * This method is used when creating a cloned tree (child) from a parent tree
		 
		GPMaterialVisitor tempVisit = new GPMaterialVisitor();
		originalNode.accept(tempVisit); // gather the probabilities
		targetNode.accept(new ProbabilityClonerVisitor(tempVisit, true)); 
	}*/
	
	// not always easy is it?
	private ASTNode getRandomClone(CompilationUnit indAST, List elementList) {
		ASTNode originalNode = null, cloneNode = null;
		originalNode = (ASTNode) elementList.get(generator.nextInt(elementList.size()));
		cloneNode = (Expression) ASTNode.copySubtree(indAST.getAST(), originalNode);
		NodeOperators.getExistingGPDataRef( originalNode, cloneNode);
		return cloneNode;
	}
	
	public Expression getDifferentRandomPrimitiveClone(Expression oldExpr) {
		Expression newExpr ; 
		do {
			newExpr = (Expression) getRandomClone((CompilationUnit)oldExpr.getRoot(), this.primitives);
		}while (  oldExpr.equals(newExpr)); 
		return newExpr;
	}
	
	public Operator getDifferentRandomOperator(InfixExpression.Operator oldOp) {
		// TODO add boolean operators
		if(oldOp.equals(InfixExpression.Operator.toOperator("+")))
			return InfixExpression.Operator.toOperator("-");
		else
			return InfixExpression.Operator.toOperator("+");
	}
	
	public org.eclipse.jdt.core.dom.Assignment.Operator getDifferentRandomOperator(
			org.eclipse.jdt.core.dom.Assignment.Operator oldOp) {
		if( oldOp.equals(org.eclipse.jdt.core.dom.Assignment.Operator.toOperator("+="))){
			if(generator.nextBoolean())
				return org.eclipse.jdt.core.dom.Assignment.Operator.toOperator("=");
			else 
				return org.eclipse.jdt.core.dom.Assignment.Operator.toOperator("-=");
		}else if(oldOp.equals(org.eclipse.jdt.core.dom.Assignment.Operator.toOperator("-="))){
			if(generator.nextBoolean())
				return org.eclipse.jdt.core.dom.Assignment.Operator.toOperator("+=");
			else 
				return org.eclipse.jdt.core.dom.Assignment.Operator.toOperator("=");
		}else{
			if(generator.nextBoolean())
				return org.eclipse.jdt.core.dom.Assignment.Operator.toOperator("+=");
			else 
				return org.eclipse.jdt.core.dom.Assignment.Operator.toOperator("-=");
		}
	}
	
	public PostfixExpression.Operator getDifferentRandomOperator( PostfixExpression.Operator operator) {
		PostfixExpression.Operator newOp ; 

		boolean diffFound = false;
		for(int i =0 ; i < postfixOperators.size(); i++){
			if (!postfixOperators.get(i).equals(operator))
				diffFound = true;
		}
		
		if(diffFound){
		do {
			newOp = postfixOperators.get(generator.nextInt(postfixOperators.size()));
		}while(  newOp.toString().equals(operator.toString()) );
		return newOp;
		}else {
			return postfixOperators.get(generator.nextInt(postfixOperators.size()));
		}
	}

	
	public Statement getRandomStatement() {
		return this.statements.get(generator.nextInt(this.statements.size())); 
	}
	
	/*public Block getRandomBlock() {
		ArrayList<Block> aR = new ArrayList<Block>();
		for(Statement stmt: this.statements){
			if(stmt instanceof Block)
				aR.add((Block)stmt);
		}
		return aR.get(generator.nextInt(aR.size())); 
	}*/
	
/*	private Block getRandomBlockClone() {
		Block aNode = getRandomBlock();
		return (Block) ASTNode.copySubtree(aNode.getAST(), aNode); 
	}*/
	
	public List<MethodDeclaration> getConstructors(String oldName) {
		ArrayList<MethodDeclaration> constructorList = new ArrayList<MethodDeclaration>();
		for(ASTNode aNode : allNodes)
			if(aNode.getNodeType() == ASTNode.METHOD_DECLARATION)
				if(((MethodDeclaration)aNode).isConstructor() && ((MethodDeclaration)aNode).getName().toString().compareTo(oldName)==0 )
					constructorList.add((MethodDeclaration)aNode);
		return constructorList;
	}

	public ArrayList<MethodInvocation> getMethodCalls() {
		return this.methodInvocations;
	}

	public ArrayList<Type> getTypes(){
		return this.allTypes;
	}

	

}
