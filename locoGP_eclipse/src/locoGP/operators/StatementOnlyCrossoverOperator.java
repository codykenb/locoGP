package locoGP.operators;

import locoGP.individual.Individual;
import locoGP.util.Logger;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
/**
 * Basic crossover operator which exchanges an expression or a statement between different individuals.
 * Relies on standard exchange mechanisms.
 * (In contrast, mutation clones material from the same individual and allows leaf nodes to be modified) 
 * @author bck
 */
public class StatementOnlyCrossoverOperator {
	private static int numberOfAllowedCrossoverAttempts  = 100;
	
	public void setTries(int numTries){
		StatementOnlyCrossoverOperator.numberOfAllowedCrossoverAttempts = numTries;
	}
	
	public Individual crossover(Individual parentOne,
			Individual parentTwo, boolean singlePointEnforced,
			boolean pickBestLocation) {

		Individual newInd = parentOne.clone(); // we clone the first parent, modify clone
		int tries = 0 ;
		ASTNode selectedNode= null;
		try {
			do {				
				selectedNode = newInd.gpMaterial.selectStatementForCrossover();
				tries++;
				
			} while (tries <100 && ! (attemptCrossover(selectedNode, parentTwo )==null));
		} catch (Exception e) {
			Logger.logTrash("Couldnt find a statement to put in place of another "
					+ e.getStackTrace().toString());
		}
		if(tries<numberOfAllowedCrossoverAttempts){
			newInd.setChanged();
			/*if(nodeToReplace!=null){
				setChangedFlag(nodeToReplace); // done when node is changed
			}*/
		}
		return newInd;
	}
	
	private ASTNode attemptCrossover(ASTNode selectedNode, Individual parentTwo){
		ASTNode newNode ;
		if(selectedNode instanceof Block){
			//System.out.println("Inserting statmement clone in block (xover)");
			newNode = NodeOperators.insertRandomStmtInBlock((Block)selectedNode,parentTwo);
			//return true;
		}else
			newNode =  NodeOperators.replaceStatementOrExpression(selectedNode, parentTwo, true);
		
		return newNode;
		
	}
	
}
