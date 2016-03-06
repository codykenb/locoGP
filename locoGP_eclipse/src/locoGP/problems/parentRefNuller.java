package locoGP.problems;

import locoGP.operators.GPASTNodeData;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class parentRefNuller extends ASTVisitor implements java.io.Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6973295405186423857L;

	public void preVisit(ASTNode aNode){
		
			GPASTNodeData tmpData = (GPASTNodeData) aNode.getProperty("gpdata");
			if(tmpData != null){
				tmpData.setParentIndividualNodeData(null);
				aNode.setProperty("gpdata", null);
			}else{
				//System.out.println(Thread.currentThread().getStackTrace());
			}
			
		
	}
}
