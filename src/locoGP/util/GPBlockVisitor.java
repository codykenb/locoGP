package locoGP.util;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;


public class GPBlockVisitor extends ASTVisitor{
	
	private List<Block> blockList = new ArrayList<Block>();
	
	public boolean visit(Block aB){
		blockList.add(aB);
		
		return true;
	}
	public List<Block> getBlockList(){
		return blockList;
	}
}
