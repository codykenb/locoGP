package locoGP.operators;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

public interface NodeSelectorI {

	public abstract ASTNode selectANodeForModification(
			List<ASTNode> allAllowedNodes);

	public abstract Statement selectStatementForCrossover(
			List<Statement> allStatements);

}