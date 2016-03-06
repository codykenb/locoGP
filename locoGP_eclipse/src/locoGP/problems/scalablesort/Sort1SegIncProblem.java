package locoGP.problems.scalablesort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.PostfixExpression;

import locoGP.individual.Individual;
import locoGP.operators.GPASTNodeData;
import locoGP.problems.Sort1Problem;
import locoGP.problems.tests.SortTestCase;
import locoGP.problems.tests.TestCase;

public class Sort1SegIncProblem extends Sort1Problem{

	
	/*
	 * Incremental segments sorted
	 * 
	 * A problem that is not trivial to improve. Not simply a deletion
	 * A profiler will show the inner 2 loops as being the problem, and will not highlight the outer loop 
	 */
	
	public Sort1SegIncProblem(int numSegs){
		setString(numSegs);
	}
	
	public void setString(int numSegs){
		problemName = "Sort1SegInc"+numSegs+"Problem";
		className = "Sort1SegInc"+numSegs+"Problem";
		methodName = "sort";
		

		/*if(numSegs+1 *2 > numSegs+1 *3){
			
		}*/
		
	problemString = "public class Sort1SegInc"+numSegs+"Problem { \n" +
	"	  public static Integer[] sort(Integer []a , Integer length) { \n";

		problemString +=
				"for (int h = "+numSegs+"; h > 0; h--) {\n" +
				"for (int i = length -(length/h); i < length; i++) {\n" +
				"for (int j = 0; j < length - 1; j++) {\n" +
			"		if (a[j] > a[j + 1]) {\n" +
			"			int k = a[j];\n" +
			"			a[j] = a[j + 1];\n" +
			"			a[j + 1] = k;\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
		"}\n" ;
	
	problemString +=
			"	return a; \n" +
	"	} \n" +
	"} \n" ;
	
	/* bubble:
	 * 
	 * "   	for (int i = 0 ; i < length ; i++){ \n" +
	"			for ( int j = 0 ; j < length - 1 ; j ++){ \n" +
	"				if ( a [ j ] > a [ j +1]) \n" +
	"				{ \n" +
	"					int k = a [ j ]; \n" +
	"					a [ j ] = a [ j +1]; \n" +
	"					a [ j +1] = k ; \n" +
	"				} \n" +
	"			} \n" +
	"		} \n" + 
	"	return a; \n" +
	 */
	
	System.out.println(problemString);
	
	}

}
