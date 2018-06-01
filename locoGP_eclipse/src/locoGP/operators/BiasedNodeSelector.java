package locoGP.operators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;

public class BiasedNodeSelector implements NodeSelectorI{
	
	private double nodeSelectionTournamentSizeRatio = 0.3;
	private Random generator = new Random();
	
	public BiasedNodeSelector(double nodeSelectionTournamentSizeRatio ){
		this.nodeSelectionTournamentSizeRatio = nodeSelectionTournamentSizeRatio;
	}

	public BiasedNodeSelector() {
	}

	public ASTNode selectANodeForModification(List<ASTNode> allAllowedNodes){
		return selectANodePerBias(allAllowedNodes);
	}
	
	@Override
	public Statement selectStatementForCrossover(List<Statement> allStatements) {
		return (Statement) selectANodePerBias(allStatements);
	}
	
	private ASTNode selectANodePerBias(List<? extends ASTNode> listOfNodes){
		ASTNode returnNode =null;
		int numberToPick = (int) (listOfNodes.size() * nodeSelectionTournamentSizeRatio);
		ArrayList<Integer> indexToPick = new ArrayList<Integer>();
		Integer newInt;
		while (indexToPick.size() < numberToPick) { // get unique set of index's
			newInt = generator.nextInt(listOfNodes.size());
			if (!indexToPick.contains(newInt)) {
				indexToPick.add(newInt);
			}
		}
		try{
		returnNode = listOfNodes.get(indexToPick.get(0));
		} catch (Exception e){
			System.out.println("Missing node choices");
		}
		ASTNode curNode;
		for (Integer curIndex : indexToPick) { // find best from this tournament
			curNode = listOfNodes.get(curIndex);
			if (((GPASTNodeData) curNode.getProperty("gpdata"))
					.getProbabilityVal() > ((GPASTNodeData) returnNode
					.getProperty("gpdata")).getProbabilityVal()) {
				returnNode = curNode;
			}
		}
		return returnNode;
	}


}
