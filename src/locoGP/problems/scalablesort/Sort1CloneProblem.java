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

public class Sort1CloneProblem extends Sort1Problem{

	public Sort1CloneProblem(int numClones){
		setCloneString(numClones);
	}
	
	public void setCloneString(int cloneNum){
		problemName = "Sort1Clone"+cloneNum+"Problem";
		className = "Sort1Clone"+cloneNum+"Problem";
		methodName = "sort";
		
	problemString = "public class Sort1Clone"+cloneNum+"Problem { \n" +
	"	  public static Integer[] sort(Integer []a , Integer length) { \n" +
	
	"   	for (int i = 0 ; i < length ; i++){ \n" +
	"			for ( int j = 0 ; j < length - 1 ; j ++){ \n" +
	"				if ( a [ j ] > a [ j +1]) \n" +
	"				{ \n" +
	"					int k = a [ j ]; \n" +
	"					a [ j ] = a [ j +1]; \n" +
	"					a [ j +1] = k ; \n" +
	"				} \n" +
	"			} \n" +
	"		} \n" ; 
	
	for(int i=0 ; i<cloneNum; i++){
		problemString +=
"   	for (int i = 0 ; i < length ; i++){ \n" +
"			for ( int j = 0 ; j < length - 1 ; j ++){ \n" +
"				if ( a [ j ] > a [ j +1]) \n" +
"				{ \n" +
"					int k = a [ j ]; \n" +
"					a [ j ] = a [ j +1]; \n" +
"					a [ j +1] = k ; \n" +
"				} \n" +
"			} \n" +
"		} \n" ;

	}

	problemString +=	"	return a; \n" +
	"	} \n" +
	"} \n" ;
	}

}
