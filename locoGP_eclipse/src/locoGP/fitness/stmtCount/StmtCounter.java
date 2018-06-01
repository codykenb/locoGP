package locoGP.fitness.stmtCount;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.SortedSet;

import org.eclipse.jdt.core.dom.Block;

import locoGP.individual.Individual;
import locoGP.problems.tests.TestCase;
import locoGP.util.Logger;

public class StmtCounter extends Thread{

	Individual ind = null;
	
	public StmtCounter( Individual ind){
		this.ind = ind;
	}
	
	public  void runBasicCounter(){
		try {
			run();
		} catch (Exception e) {
			Logger.logDebugConsole("Thread died (possibly infinite loop?): ");
			e.printStackTrace();
		}
	}
	
	public void run(){
		ind.zeroResults(); 

		Logger.logDebugConsole(" ----- About to execute "+ind.getClassName());
	
		Object[] _args = null ;
		Object returnValue  = null;
		long instructionCount=0;
		boolean runtimeError = false;
		
		for (int testIndex = 0; testIndex < ind.ourProblem.getNumTests(); testIndex++) {
			runtimeError = false;
			instructionCount = 0;
			
			TestCase aTestCase = ind.ourProblem.getTestData()[testIndex];
			_args = aTestCase.getTest();
			// System.gc();
			// InvocationResultData exeResult = null;
			// System.out.println("Before: "+arrayPrinter((Integer[])_args[0]));
			returnValue = null;
			try {
				// Logger.logDebugConsole(" ----- About to execute instrumented version of "+ind.getClassName());
				returnValue = ind.execute(_args);
				// exeResult =counter.execute(ourMD, _args);
				// Logger.logDebugConsole(" ----- Successful execution of instrumented version of "+ind.getClassName());
				// exeResult = counter.execute(ourMD, targetClass, _args); //
				// this is for classes we want bycounter to instatiate (not
				// static)
				// result = (Integer) m.invoke(null, _args); // null is only for
				// static methods
				// ExecutionSettings eSettings = counter.getExecutionSettings();
			}catch (RuntimeException e){
				Logger.logDebugConsole(" ---- Continuing with tests: Compiled code ("+ind.getClassName()+") threw runtime error at index "+testIndex+":\n"+e.getMessage());
				runtimeError = true;
			}  catch (Exception e) { // what do we do here? if the program has
									// runtime issues then we cannot gather
									// instruction count!
				// Infinite loop, or broken runtime
				Logger.logDebugConsole(" ---- Compiled code ("
						+ ind.getClassName() + ") threw an error at index "
						+ testIndex + ":\n" + e.getMessage());
				// estimateFitness(testIndex,functionalityScore); this never
				// runs!
				return; // make sure this thread stops if this happens,
						// otherwise we get orphaned threads!
				// Should we use a flag for this? is there anything left below
				// that we want to run? no.
			}

			instructionCount += ASTInstrumenter.getExecutionCount(ind.getID());
			ASTInstrumenter.getExecutionProfile(ind.getID()).logProfileToDebug();
			ASTInstrumenter.zeroExecutionCount(ind.getID());

			ind.addBestTime(instructionCount); // put the instruction count into
												// the time
			int errorCount = aTestCase.checkAnswer(returnValue);

			ind.setTestResult(testIndex, errorCount,runtimeError);
		}
		if(ind.getFitness()<1){
			Logger.logDebugConsole("Individual set less than one!" +ind.getClassName());
			Logger.logDebugConsole("test case results: " +ind.getTestCaseResultsText());
			Logger.logDebugConsole("\n" +ind.ASTSet.getCodeListing());
			Logger.logDebugConsole("\n Num Blocks: " +ind.gpMaterial.getBlocks().size());
			
			List<Block> allBlocks = ind.gpMaterial.getBlocks(); 
			
			// for every block
			int numBlocks = allBlocks.size();
			for(int j = 0; j< numBlocks; j++){
				Block aBlock = allBlocks.get(j);
			// for every statement in a block
				int blockLength = aBlock.statements().size() ;
				for (int i = 0; i < blockLength; i++){ 
					// add a counting statement
					Logger.logDebugConsole("Stmt "+i+": " +aBlock.statements().get(i).toString());
				
				}
			}
			
			
			//ind.ASTSet.getCompilationList()[0].AST.rewrite(null, null);
			Logger.logDebugConsole("\nListing:\n\n" +ind.ASTSet.getCodeListing());
		}
		Logger.logDebugConsole("Running Time: " +ind.getRunningTime());
		
	}
}
