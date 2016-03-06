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
import locoGP.util.gpDataSetterVisitor;

public class Sort1LoopsProblem extends Sort1Problem{

	
	/*
	 * Add extra redundant loops around the bubblesort algorithm
	 * the original bubblesort algorithm will still get higher ranking from a profiler,
	 * though the outer loop is actually the problem.
	 * Even if this is run to show asymptotic response of lines, the bubblesort lines will still get higher bias 
	 */
	
	public Sort1LoopsProblem(int percentInputRedundantLoops){
		setString(percentInputRedundantLoops);
	}
	
	public void setStaticOptimalBias(Individual individual, int optimisationType) {
		individual.setAllGPDataNodesToZero();	// sets any existing nodes = 0
		// Make sure new nodes are set to 0 when static bias is in use
		GPASTNodeData.setGPASTNodeInitialBias(0); // specifies any new nodes = 0 
		
		if(optimisationType ==1){ // profiler should be 3
			setProfilerBias(individual); 
		}else if(optimisationType==4){ 	// 4 is always biasAccumulator
			setBiasbyNodeNumber(individual); 
		}else
			System.exit(1);
		
	}
	
	
	private void setBiasbyNodeNumber(Individual individual) {
		ASTNode tempNode;
		/* 
		 * We got these values by running biasAccumulator with R15tiny from GP117 and averaging results
		 */
		
		//GPASTNodeData tempData =null;
		double[] NodeVals = {0,0.657176,0.905159,0.625523,0.636098,0.662491,0.90516,0.670195,0.645555,0.936782,0.680494,0.703846,0.652357,0.905158,0.624099,0.624855,0.656832,0.90516,0.657815,0.648731,0.933682,0.668082,0.703643,0.653961,0.905158,0.623416,0.630696,0.67567,0.90516,0.633937,0.624663,0.633389,0.634373,0.623014,0.658429,0.689125,0.64424,0.905158,0.633712,0.663715,0.633664,0.634387,0.665767,0.625161,0.634241,0.634232,0.692637,0.905147,0.623637,0.5906,0.63273,0.66299,0.633903,0.649734,0.624602,0.643865,0.666583,0.631872,0.62957,0.663944,0.624907,0.632218,0.63276,0.648604,0.622466,0.646021,0.664473,0.623852,0.628987,0.634738,0.634645,0.677342,0.663371};
		int nodeNum=0;
					
		for (Iterator<ASTNode> iter = individual.gpMaterial.getAllAllowedNodes()
				.iterator(); iter.hasNext();) {
			tempNode = iter.next();
			tempNode.accept(new gpDataSetterVisitor(NodeVals[nodeNum]));
			
			System.out.println("Val: "+NodeVals[nodeNum]+" Node: "+nodeNum + " Str: "+tempNode.toString());
			nodeNum++;
		}
				
	}
	
	
	private void setProfilerBias(Individual individual) {
		individual.setAllGPDataNodesToZero();
		// from a number of calls to instrumented bubblesort, we have the following
				
		/*
	for (int h = (int)(0.1*length); h > 0; h--) {	5		.001
	for (int i = 0 ; i < length; i++) {				164		.03
	for (int j = 0; j < length - 1; j++) {			6800	1
	if (a[j] > a[j + 1]) {							6240	.92
	int k = a[j];									377		.06
	a[j] = a[j + 1];								377		.06
	a[j + 1] = k;									377		.06
		 */
		
		setBiasContains(individual, "if (a[j] > a[j + 1])", .92);
		setBiasDownTree(individual, "a[j] > a[j + 1]", .92);
		
		setBiasContains(individual, "for (int j=0; j < length - 1; j++)", 1);
		setBiasDownTree(individual, "int j=0", 1);
		setBiasDownTree(individual, "j < length - 1", 1);
		setBiasDownTree(individual, "j++", 1);
		
		setBiasContains(individual, "for (int i=0; i < length; i++)", .03);
		setBiasDownTree(individual, "int i=0", .03);
		setBiasDownTree(individual, "i < length", .03);
		setBiasDownTree(individual, "i++", .03);
		
		setBiasContains(individual, "for (int h = (int)(0.1*length); h > 0; h--)", .001);
		setBiasDownTree(individual, "int h = (int)(0.1*length)", .001);
		setBiasDownTree(individual, "h > 0", .001);
		setBiasDownTree(individual, "h--", .001);
		
		setBiasDownTree(individual, "int k=a[j];", .06);
		setBiasDownTree(individual, "a[j]=a[j + 1];", .06);
		setBiasDownTree(individual, "a[j + 1]=k;", .06);	
		
	}
	
	
	private void setBiasContains(Individual individual, String biasString, double newNodeVal) {
		// shamelessly copied from Sort1Problem, refactor!
		ASTNode tempNode;
		GPASTNodeData tempData =null;
					
		for (Iterator<ASTNode> iter = individual.gpMaterial.getAllAllowedNodes()
				.iterator(); iter.hasNext();) {
			tempNode = iter.next();
			if (tempNode.toString().trim().contains(biasString)) {
				tempData = (GPASTNodeData)tempNode.getProperty("gpdata");
				tempData.setProbabilityVal(newNodeVal);
				//tempNode.setProbabilityVal(newNodeVal);
				//tempNode.accept(new gpDataSetterVisitor(newNodeVal));
			}

		}
				
	}
	
	private void setBiasDownTree(Individual individual, String biasString, double newNodeVal) {
		ASTNode tempNode;
		//GPASTNodeData tempData =null;
					
		for (Iterator<ASTNode> iter = individual.gpMaterial.getAllAllowedNodes()
				.iterator(); iter.hasNext();) {
			tempNode = iter.next();
			if (tempNode.toString().trim().contentEquals(biasString)) {
				tempNode.accept(new gpDataSetterVisitor(newNodeVal));
			}

		}
				
	}
	
	public void setString(int percentInputRedundantLoops){
		problemName = "Sort1Loops"+percentInputRedundantLoops+"Problem";
		className = "Sort1Loops"+percentInputRedundantLoops+"Problem";
		methodName = "sort";
		
	problemString = "public class Sort1Loops"+percentInputRedundantLoops+"Problem { \n" +
	"	  public static Integer[] sort(Integer []a , Integer length) { \n";

		problemString +=
				//"for (int h = (int)(0."+percentInputRedundantLoops+"*length); h > 0; h--) {\n" +
				"for (int h = 2; h > 0; h--) {\n" +
				"for (int i = 0 ; i < length; i++) {\n" +
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
	
	//System.out.println(problemString);
	
	}

}
