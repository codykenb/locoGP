package locoGP.util;
import java.util.List;

import locoGP.operators.GPASTNodeData;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;


public class FlagToggleVisitor extends ASTVisitor{
	
	
	//List<GPASTNodeData> gpASTNodeData = null;
	
	public void preVisit(ASTNode aNode){
		((GPASTNodeData) aNode.getProperty("gpdata")).setChangedFlag();
	}	
}
