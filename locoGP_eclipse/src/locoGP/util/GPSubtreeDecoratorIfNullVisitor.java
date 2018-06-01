package locoGP.util;
import java.util.ArrayList;
import java.util.List;

import locoGP.operators.GPASTNodeData;
import locoGP.operators.GPMaterialVisitor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;


public class GPSubtreeDecoratorIfNullVisitor extends ASTVisitor{
	
	public void preVisit(ASTNode aNode){
		
		if( GPMaterialVisitor.allowedTypeAndParentType( aNode) ){
			GPASTNodeData tmpData = (GPASTNodeData) aNode.getProperty("gpdata");
			if(tmpData == null){
				aNode.setProperty("gpdata", new GPASTNodeData());
			}
		}
	}
}
