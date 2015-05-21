package locoGP.util;
import java.util.List;

import locoGP.operators.GPASTNodeData;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;


public class gpDataSetterVisitor extends ASTVisitor{
	
	/*
	 * Set the gpData values
	 */

	double val = 0;
	List<GPASTNodeData> gpASTNodeData = null;
	GPASTNodeData tempData = null;
	
	public gpDataSetterVisitor(double newVals){
		val = newVals;
	}
	
	void setData(GPASTNodeData tempData){
		if(tempData !=null)
			tempData.setProbabilityVal(val);
	}
	
	public void preVisit(ASTNode aNode) {
		tempData = ((GPASTNodeData) aNode.getProperty("gpdata"));
		setData(tempData);

		if (aNode instanceof PostfixExpression) {
			tempData = ((GPASTNodeData) ((PostfixExpression) aNode)
					.getOperand().getProperty("gpdata"));
			setData(tempData);
		} // doesn't make sense to do this for infix.. right?			
	}

}
