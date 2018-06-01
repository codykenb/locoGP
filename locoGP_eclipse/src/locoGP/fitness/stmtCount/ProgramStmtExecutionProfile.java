package locoGP.fitness.stmtCount;

import java.util.HashMap;
import java.util.Iterator;

import org.clapper.util.misc.SparseArrayList;

import locoGP.util.Logger;

public class ProgramStmtExecutionProfile{
	HashMap<Integer,Long> results = new HashMap<Integer,Long>();
	long individualID;

	ProgramStmtExecutionProfile(Long indID) {
		this.individualID = indID; 
	}

	public long getStmtTotal(int stmtIndex) {
		return results.get(stmtIndex);
	}

	public long getTotal() {
		long total = 0l;
		for (Integer rKey : results.keySet()){
			total += results.get(rKey);
		}
		return total;
	}

	public void recordExecution(Integer stmtIndex) {
		Long stmtVal;
		try{
			stmtVal = results.get(stmtIndex);
			if(stmtVal==null)
				results.put(stmtIndex, 1l);
			else
				results.put(stmtIndex, ++stmtVal);
		}catch(IndexOutOfBoundsException e){
			results.put(stmtIndex, 1l);
		}
		
		/*Long stmtVal;
		if(results.size()<=stmtIndex)
			results.add(1l); // risky, what if execution flow does not match ast tree traversal?
		else{
			stmtVal = results.get(stmtIndex);
			results.set(stmtIndex, stmtVal);
		}*/
	}

	public void logProfileToDebug() {
		int stmtIndex = 0;
		
		Logger.logDebugConsole("Statement Execution Profile results for "
				+ individualID + ":");
				
		for (Integer rKey : results.keySet()) {
			Logger.logDebugConsole(
					  " StatmentIndex: " + stmtIndex
					+ " ExecutionCount: " + results.get(rKey));
			stmtIndex++;
		}
	}
}