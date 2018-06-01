package locoGP.operators;

import java.util.List;
import java.util.Random;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

/** Select nodes by random.
 * 
 */
public class NodeSelector implements NodeSelectorI {
	static private Random generator = new Random();
	
	/* (non-Javadoc)
	 * @see locoGP.operators.NodeSelectorI#selectANodeForModification(java.util.List)
	 */
	@Override
	public ASTNode selectANodeForModification(List<ASTNode> allAllowedNodes){
		return allAllowedNodes.get(generator.nextInt(allAllowedNodes.size()));
	}
	
	/* (non-Javadoc)
	 * @see locoGP.operators.NodeSelectorI#selectStatementForCrossover(java.util.List)
	 */
	@Override
	public Statement selectStatementForCrossover(List<Statement> allStatements){
			return allStatements.get(generator.nextInt(allStatements.size())); 
	}

}
