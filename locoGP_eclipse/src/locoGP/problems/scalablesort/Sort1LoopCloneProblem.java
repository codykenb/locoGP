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

public class Sort1LoopCloneProblem extends Sort1Problem{

	
	/*
	 * put bubblesort in deeper nested loops. 
	 * 
	 * To improve this takes a crossover of bubblesort and a deletion of the whole block
	 * 
	 * (Harder than simply deleting bloat)
	 * 
	 * does the magnitude of the outer loop matter?
	 * its removal is an optimisation, regardless of if it adds 0 or 1 wasted loops. 
	 * 
	 */
	
	public Sort1LoopCloneProblem(int numReps){
		setString(numReps);
	}
	
	public void setString(int numReps){
		problemName = "Sort1LoopClone"+numReps+"Problem";
		className = "Sort1LoopClone"+numReps+"Problem";
		methodName = "sort";
		
	
	problemString = "public class Sort1LoopClone"+numReps+"Problem { \n" +
	"	  public static Integer[] sort(Integer []a , Integer length) { \n";

	for(int i= 0 ; i <numReps; i++){
		problemString +="for (int i"+i+" = 0 ; i < 1 ; i++){ \n";
	}	
						
	problemString +=		"   	for (int i = 0 ; i < length ; i++){ \n" +
				"for (int j = 0; j < length - 1; j++) {\n" +
			"		if (a[j] > a[j + 1]) {\n" +
			"			int k = a[j];\n" +
			"			a[j] = a[j + 1];\n" +
			"			a[j + 1] = k;\n" +
			"		}\n" +
			"	}\n" +
			"}\n" +
		"}\n" ;
	
	for(int i= 0 ; i <numReps; i++){
		problemString +=" } \n";	
	}

		
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
