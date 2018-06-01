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
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;


/**
 * Moves down the AST and gathers lists of leaf (simplenames, operators) and non-leaf nodes.
 * Leaf nodes should be excluded from crossover.
 * None of the methods here clone the AST. 
 * This class (and subclasses) defines how material is selected from a program.
 * This particular class selects nodes randomly. (without bias or tournament selection)
 * 
 *  TODO capture methodDeclarations so they can be inserted around expressions
 */
public class GPMaterialVisitor extends ASTVisitor implements java.io.Serializable{
	
	private List<ASTNode> allNodes = new ArrayList<ASTNode>(); 
	// keep a reference to all nodes, so we can easily pick one at random
	private List<ASTNode> allAllowedNodes = new ArrayList<ASTNode>();
	
	// Operators have special uses, not ASTNodes
	private List<Operator> infixOperators = new ArrayList<Operator>();
	private List<PostfixExpression.Operator> postfixOperators = new ArrayList<PostfixExpression.Operator>();
	private List<PrefixExpression.Operator> prefixOperators = new ArrayList<PrefixExpression.Operator>();
	
	// Collect any expression with an operator which returns boolean, or any boolean simpleName 
	private List<InfixExpression> conditionals = new ArrayList<InfixExpression>(); 

	private List<SimpleName> updateadbleNames = new ArrayList<SimpleName>(); // sub-statement non-leaves
	private List<Expression> expressions = new ArrayList<Expression>(); // sub-statement non-leaves 
	private List<Statement> statements = new ArrayList<Statement>();
	
	private List<Block> blocks = new ArrayList<Block>();
	
	// stuff which can be extracted as clone
	//private List<Expression> extractableStatements = new ArrayList<Expression>(); 
	
	//private ArrayList<TypeDeclaration> declaredClasses = new ArrayList<TypeDeclaration>();
	private ArrayList<MethodInvocation> methodInvocations= new ArrayList<MethodInvocation>();
	
	private ArrayList<Type> allTypes = new ArrayList<Type>();

	private Random generator = new Random();
	private ArrayList<TypeDeclaration> typeDecsnew = new ArrayList<TypeDeclaration>();
	private SimpleName classNameNode;
	private Name packageName;
	
	public static NodeSelectorI nodeSelector= new NodeSelector();
	
	public void purgeRefs() {
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
		expressions.clear();
		expressions = null;
		statements.clear();
		statements = null;
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
		printAll(expressions);
			
		System.out.println("statements");
		printAll(statements);
		
		System.out.println("All disallowed nodes");
		List<ASTNode> allDisallowedNodes = new ArrayList<ASTNode>();
		allDisallowedNodes.addAll(allNodes);
		allDisallowedNodes.removeAll(allAllowedNodes);
		printAll(allDisallowedNodes );
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
	
	private ASTNode getNode(int i){
		return allAllowedNodes.get(i);
	}
	
	public int getNumGPNodes() { // TODO Doublecheck this is correct, are the more in allowednodes?
		return infixOperators.size() + postfixOperators.size()
				+ conditionals.size() + expressions.size()
				//+ numberLiterals.size() + arithmeticExprs.size() + updaters.size() 
				+ statements.size();
	}
	
	 
	
	/**
	 * 	In this constructor we define all operators that are to be used,
	 * 	Any that do not exist in the original program should be
	 * 	added here.
	 **/ 
	public GPMaterialVisitor(){
		

		this.infixOperators.add(Operator.toOperator("*"));
		this.infixOperators.add(Operator.toOperator("/"));
		this.infixOperators.add(Operator.toOperator("%"));
		this.infixOperators.add(Operator.toOperator("+"));
		this.infixOperators.add(Operator.toOperator("-"));
		this.infixOperators.add(Operator.toOperator("<<"));
		this.infixOperators.add(Operator.toOperator(">>"));
		this.infixOperators.add(Operator.toOperator(">>>"));
		this.infixOperators.add(Operator.toOperator("<"));
		this.infixOperators.add(Operator.toOperator(">"));
		this.infixOperators.add(Operator.toOperator("<="));
		this.infixOperators.add(Operator.toOperator(">="));
		this.infixOperators.add(Operator.toOperator("=="));
		this.infixOperators.add(Operator.toOperator("!="));
		this.infixOperators.add(Operator.toOperator("^"));
		this.infixOperators.add(Operator.toOperator("&"));
		this.infixOperators.add(Operator.toOperator("|"));
		this.infixOperators.add(Operator.toOperator("&&"));
		this.infixOperators.add(Operator.toOperator("||"));
		
		this.postfixOperators.add(PostfixExpression.Operator.toOperator("--"));
		this.postfixOperators.add(PostfixExpression.Operator.toOperator("++"));
		
		this.prefixOperators.add(PrefixExpression.Operator.toOperator("--"));
		this.prefixOperators.add(PrefixExpression.Operator.toOperator("++"));
		this.prefixOperators.add(PrefixExpression.Operator.toOperator("-"));
		this.prefixOperators.add(PrefixExpression.Operator.toOperator("+"));
		this.prefixOperators.add(PrefixExpression.Operator.toOperator("~"));
		this.prefixOperators.add(PrefixExpression.Operator.toOperator("!"));
	}

	private static boolean isAllowedType(ASTNode aNode){
		boolean ok = false;
		if (aNode == null) // TODO why is this sometimes null?
			return ok;
		try {
				ok = ( //aNode.getNodeType() != 22 && // Integer // allowed for ascon?
				aNode.getNodeType() != 43 && // Comparable
				aNode.getNodeType() != 44 && // SingleVariableDeclaration
				aNode.getNodeType() != 78 && // @Override	
				//aNode.getNodeType() != 36 && // CastExpression // allowed for ascon
				//aNode.getNodeType() != 11 && // CastExpression :/ // allowed for ascon
				aNode.getNodeType() != 40 && //
				(
						aNode instanceof PrimitiveType || // only for use in CastExpressions (and variable declarations? - specialise code to input size)
						aNode instanceof SimpleName||
						aNode instanceof NullLiteral||
						aNode instanceof NumberLiteral||
						aNode instanceof StringLiteral||
						aNode instanceof QualifiedName ||
						aNode instanceof org.eclipse.jdt.core.dom.BooleanLiteral ||
						aNode instanceof VariableDeclarationFragment ||
						aNode instanceof MethodInvocation || 
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
				aNode instanceof ArrayAccess||
				// aNode instanceof StringLiteral||
				aNode instanceof FieldAccess||
				(aNode instanceof InfixExpression ||
				aNode instanceof ParenthesizedExpression||
				aNode instanceof CastExpression||
				aNode instanceof Expression
				|| aNode instanceof PostfixExpression
				|| aNode instanceof PrefixExpression
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
	
	public static boolean allowedTypeAndParentType(ASTNode aNode){
		
		// A block is allowed, regardless if its parent 
		if(aNode instanceof Block)
			return true;
		
		// The Simplename of a method invocation shouldn't be changed.
		/*if(aNode instanceof SimpleName && aNode.getParent() instanceof MethodInvocation )
			if(((MethodInvocation)aNode.getParent()).getName().equals(aNode))
				return false;*/
		
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
		
		if( allowedTypeAndParentType( aNode) ){ //&& (GPConfig.getLinkNewChildBiasDataWithParent() || GPConfig.getReferenceChildBiasToParentData())){
			GPASTNodeData tmpData = (GPASTNodeData) aNode.getProperty("gpdata");
			if(tmpData == null){
				aNode.setProperty("gpdata", new GPASTNodeData());
			}
			allAllowedNodes.add(aNode);
		}
		
		//	return Bits.byteOrder()
		if(aNode.toString().equals("return Bits.byteOrder()")) //"ByteOrder") 
/*				&& !(aNode instanceof SimpleType 
						|| aNode instanceof SimpleName))*/{
			System.out.println("Found ByteOrder: " + aNode.getNodeType());
		}
		
	}
	
	/*
	 *  ------------------------------------------- Material that needs to be updated in new cloned programs
	 *  Method declarations (to update constructor names to match class names)
	 *  Class declarations (update static method calls and new object generations) 
	 */
	public List<MethodDeclaration> getConstructors(String oldName) {
		ArrayList<MethodDeclaration> constructorList = new ArrayList<MethodDeclaration>();
		for(ASTNode aNode : allNodes)
			if(aNode.getNodeType() == ASTNode.METHOD_DECLARATION)
				if(((MethodDeclaration)aNode).isConstructor() && ((MethodDeclaration)aNode).getName().toString().compareTo(oldName)==0 )
					constructorList.add((MethodDeclaration)aNode);
		return constructorList;
	}
	// -------------------------------------------------------------Class declarations:
	/*public boolean visit(TypeDeclaration typeDeclaration) {
		this.declaredClasses.add(typeDeclaration);
		//return visit((Statement) stmt);
		return true;
	}*/
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
		this.blocks.add(stmt);
		return visit((Statement) stmt);
	}

	public boolean visit(Statement stmt){ 
		// doesn't override anything. No visit function for statement object
		//if(!statements.contains(stmt))
		statements.add(stmt);
		return true;
	}
	
	// -------------------------------------------------------------Expressions:
	public boolean visit(Expression expr){
		if(allowedTypeAndParentType(expr)  )//&&!this.expressions.contains(expr))
			this.expressions.add(expr);
		return true;
	}

	public boolean visit(NumberLiteral nL){
		visit((Expression)nL);
		return true;
	}
	
	public boolean visit(TypeDeclaration tD){
		if(tD.getParent() instanceof CompilationUnit){
			this.classNameNode = tD.getName();
			if(((CompilationUnit)tD.getParent()).getPackage() != null)
				this.packageName = ((CompilationUnit)tD.getParent()).getPackage().getName();
		}
		
		return true;
	}

	public String getClassName(){
		return this.classNameNode.toString();
	}
	
	public String getClassPackage(){
		if( this.packageName == null )
			return "";
		else
			return this.packageName.toString();
	}
	
	public boolean visit(SimpleName sN){
		//TODO it could be a boolean, which could be used as a conditional, but what are the chances?
		if( sN.getParent() instanceof BodyDeclaration 
				|| sN.getParent() instanceof Type
				|| sN.getParent() instanceof QualifiedName
				|| sN.getParent() instanceof ConditionalExpression
				|| sN.getParent() instanceof MethodInvocation
				|| sN.getParent() instanceof TypeDeclaration){ 
			/*if(!(sN.getParent() instanceof MethodDeclaration // eh.. why? 
					&& !((MethodDeclaration)sN.getParent()).isConstructor() )){*/
			this.updateadbleNames.add(sN);
			//}	
		} else{
			visit((Expression)sN);		
		}
		
		// gather these so references can be updated easily
		/*if(sN.getParent() instanceof MethodDeclaration || 
				sN.getParent() instanceof ){
		}*/
		return true;
	}
	
	/*public boolean visit(ConditionalExpression cE){

		System.out.println(cE.toString());
		return true;
	}*/
	
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
			if(operator.toString().compareTo(op)==0){
				isBoolean=true;
				break;
			}
		}
		return isBoolean;
	}
	public boolean visit(MethodInvocation expr){
		visit((Expression)expr);
		// addToExtractableStmts(expr);
		this.methodInvocations.add(expr);
		/*if(expr.toString().contains("Bits.byteOrder")){
			System.out.println("found!" + expr.getExpression().toString());
			System.out.println("found!");
		}*/
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
	public boolean visit(PrefixExpression prefixExpr){
		visit((Expression)prefixExpr); 
		if(!this.prefixOperators.contains(prefixExpr.getOperator()))
			this.prefixOperators.add(prefixExpr.getOperator());
		return true;
	}
	
	public boolean visit(PostfixExpression postfixExpr){
		visit((Expression)postfixExpr); 
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
		//addToExtractableStmts(expr);
		return true;
	}
	
	/*
	 * Gathers expressions which can be used as a statement
	 * @param expr
	  Allow any expression to be placed as a statement!
	private void addToExtractableStmts(Expression expr) {
		this.extractableStatements.add(expr);
	}*/

	
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


	
	
	/*private Statement extractStatementClone(CompilationUnit cloneAST) {
		int numChoices = this.statements.size() + this.expressions.size();
		int choice = generator.nextInt(numChoices);
		Statement returnStatement;
		ASTNode cloneableNode = selectStatementOrExpression();
		if(choice >= this.statements.size()){
			cloneableNode = this.expressions.get(choice - this.statements.size());
			Expression newExpr = (Expression) NodeModifierUtil.getClone(cloneAST, cloneableNode);
			returnStatement = cloneAST.getAST().newExpressionStatement( newExpr );
		}else{
			cloneableNode = this.statements.get(choice);
			returnStatement = (Statement) CompilationUnit.copySubtree(cloneAST.getAST(), cloneableNode);
		}
		NodeModifierUtil.cloneExistingGPDataRef( cloneableNode, returnStatement);
		return returnStatement;
	} */
	
	/**
	 * Picks a random statement or (non-leaf) expression. If expression is picked, a new parent statement is created and returned.  
	 * @return a new statement
	 */
/*	public Statement extractStatement() {
		int numChoices = this.statements.size() + this.expressions.size();
		int choice = generator.nextInt(numChoices);
		Statement returnStatement;
		// Expression cloneableNode = selectStatementOrExpression();
		if(choice >= this.statements.size()){
			Expression anExpr= this.expressions.get(choice - this.statements.size());
			returnStatement = anExpr.getAST().newExpressionStatement( anExpr );
		}else{
			returnStatement= this.statements.get(choice);
		}
		return returnStatement;
	}*/
	
	public ASTNode selectStatementOrExpression(){
		int numChoices = this.statements.size() + this.expressions.size();
		int choice = generator.nextInt(numChoices);
		ASTNode selectedNode ;
		if(choice >= this.statements.size()){
			selectedNode = this.expressions.get(choice - this.statements.size());
		}else{
			selectedNode = this.statements.get(choice);
		}
		return selectedNode;
	}
 

	public ASTNode selectStatement(){
		return this.statements.get(generator.nextInt(this.statements.size()));
	}
 
/*	public List<Statement> getStatementsClones(CompilationUnit cloneAST) { 
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
			NodeOperators.cloneExistingGPDataRef( clonableExpr, newExpr);
			NodeOperators.cloneExistingGPDataRef( clonableExpr, newStmt);
			returnList.add( newStmt );
		}
		return returnList;
	}*/
	
	// This returns the actual objects, not clones! 
	// Use for mutation or directly manipulating the relevant individual 
	public List<Statement> getStatements() { 
		ArrayList<Statement> returnList = new ArrayList<Statement>();
		returnList.addAll(this.statements);
		return returnList;
	}
	
/*	private List<ASTNode> getStatementsAsNodes() { 
		ArrayList<ASTNode> returnList = new ArrayList<ASTNode>();
		returnList.addAll(this.statements);
		return returnList;
	}*/

	public List<ASTNode> getAllAllowedNodes() { // TODO needed?
		return this.allAllowedNodes;
	}
	
	/**
	 * Return a sub-statement non-leaf ASTNode subtree.  
	 * @param oldExp
	 * @return
	 */
	public Expression selectDifferentExpression(Expression oldExp) {
		return (Expression) getDifferentObject(oldExp, this.expressions);
	}
	
	public Expression selectDifferentConditional(Expression oldExp) {
		return (Expression) getDifferentObject(oldExp, this.conditionals);
	}	
	
	public org.eclipse.jdt.core.dom.Assignment.Operator selectDifferentOperator(
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
	
	public PostfixExpression.Operator selectDifferentOperator( PostfixExpression.Operator operator) {
		return (PostfixExpression.Operator) getDifferentObject(operator, postfixOperators);
	}
	
	public PrefixExpression.Operator selectDifferentOperator( PrefixExpression.Operator operator) {
		return (PrefixExpression.Operator) getDifferentObject(operator, prefixOperators);
	}

	public Operator selectDifferentOperator(InfixExpression.Operator oldOp) {
		return (Operator) getDifferentObject(oldOp,infixOperators);
	}
	
	/**
	 * Get a different looking node from a list. Accepts objects because operators are not ASTNode subtypes.
	 * @param anItem
	 * @param objList
	 * @return
	 */
	private Object getDifferentObject(Object anItem, List objListOriginal){
		Object chosenItem = anItem;
		List objList = new ArrayList();
		objList.addAll(objListOriginal);
		
		while(objList.size() > 0 && anItem.toString().equals(chosenItem.toString())) {
			chosenItem = objList.get(generator.nextInt(objList.size()));
			objList.remove(chosenItem);
		}
		
		return chosenItem; 
	}
	




	public ArrayList<MethodInvocation> getMethodCalls() {
		return this.methodInvocations;
	}

	public ArrayList<Type> getTypes(){
		return this.allTypes;
	}

	
	
	public ASTNode selectANodeForModification(){
		return nodeSelector.selectANodeForModification(this.allAllowedNodes);
	}
	public ASTNode selectStatementForCrossover() {
		return nodeSelector.selectStatementForCrossover(this.statements);
	}

	public List<SimpleName> getUpdateableNames() {
		return this.updateadbleNames;
	}

	public List<Block> getBlocks() {
		return this.blocks;
	}
	
	
/*	private static List<ASTNode> pickNodes(int numChanges,
			boolean pickBestLocation) {
		// split this out into two types of material visitor
		if (pickBestLocation)
			return pickBestNodes(numChanges); 
		else
			return pickNodes(numChanges);
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
	}*/
	

}
