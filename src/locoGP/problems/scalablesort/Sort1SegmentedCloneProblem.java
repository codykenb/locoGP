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

public class Sort1SegmentedCloneProblem extends Sort1Problem{

	public Sort1SegmentedCloneProblem(int numSegs){
		setSegmentedString(numSegs);
	}
	
	public void setSegmentedString(int numSegs){
		problemName = "Sort1SegmentedClone"+numSegs+"Problem";
		className = "Sort1SegmentedClone"+numSegs+"Problem";
		methodName = "sort";
		

		/*if(numSegs+1 *2 > numSegs+1 *3){
			
		}*/
		
	problemString = "public class Sort1SegmentedClone"+numSegs+"Problem { \n" +
	"	  public static Integer[] sort(Integer []a , Integer length) { \n";
	
	/*
	 * If GP deletes these clones, does it improve the sort?
	 * or are these clones beneficial?
	 * 
	 * original: 64,158,394 3530
	 * this: 78,631,244 3530
	 * rubbish!
	 *  - this is too easy to fix
	 */
	for(int i=0 ; i<numSegs; i++){
		problemString +=
"   	for (int i = "+i+" * (length/"+numSegs+") ; i < (length/"+numSegs+")*("+i+"+1) ; i++){ \n" +
"			for ( int j = "+i+" ; j < (length/"+numSegs+")*"+i+" ; j ++){ \n" +
"				if ( a [ j ] > a [ j +1]) \n" +
"				{ \n" +
"					int k = a [ j ]; \n" +
"					a [ j ] = a [ j +1]; \n" +
"					a [ j +1] = k ; \n" +
"				} \n" +
"			} \n" +
"		} \n" ;

	}

	problemString +="   	for (int i = 0 ; i < length ; i++){ \n" +
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
	"	} \n" +
	"} \n" ;
	
	//System.out.println(problemString);
	
	}

}
