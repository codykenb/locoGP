package locoGP.util;
import locoGP.operators.GPASTNodeData;
import locoGP.operators.GPMaterialVisitor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.Statement;


public class ParentProbabilityReferencerVisitor extends ASTVisitor {

	private GPMaterialVisitor gpMV = null;
	private int count = 0;
	boolean resultOfModification = false;
	
	public ParentProbabilityReferencerVisitor(GPMaterialVisitor gpMV){ //, boolean resultOfModification){
		this.gpMV = gpMV;
		this.resultOfModification = resultOfModification;
		//System.out.println("-----------starting a new cloner------------------------");
	}
	
	public void preVisit(ASTNode aNode){
		//System.out.println("------------Matching \n" + gpMV.getNode(count).toString() + "\n-----------with\n" + aNode.toString());
		if(gpMV.allowedType2(aNode)){
			GPASTNodeData nodeDataFromOriginal = gpMV.getNodeProperty(count++);
			
			//GPASTNodeData gpData = new GPASTNodeData(nodeDataFromOriginal);
			//gpData.setParentIndividualNodeData(nodeDataFromOriginal);
			aNode.setProperty("gpdata",nodeDataFromOriginal );
			/*if(resultOfModification)
				gpData.setChangedFlag();*/
			
		}
	}
	
	/*public boolean visit(Statement stmt){
		GPASTNodeData gpData = new GPASTNodeData(gpMV.getNodeProperty(count++).getModifyProbability()); 
		stmt.setProperty("gpdata",gpData );
		if(resultOfModification)
			gpData.setChangedFlag();
		return true;
	}*/
}
