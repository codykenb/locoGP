package locoGP.roughwork;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Random;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PostfixExpression.Operator;

import locoGP.fitness.IndividualEvaluator;
import locoGP.fitness.bytecodeCount.ByteCodeIndividualEvaluator;
import locoGP.individual.Individual;
import locoGP.operators.GPASTNodeData;
import locoGP.problems.Problem;
import locoGP.problems.Sort1Optimised;
import locoGP.problems.Sort1Problem;
import locoGP.problems.Sort2Problem;
import locoGP.problems.tests.SortTestCase;

import de.uka.ipd.sdq.ByCounter.execution.BytecodeCounter;
import de.uka.ipd.sdq.ByCounter.utils.MethodDescriptor;




public class Benchmarker {

	/**
	 * @param args
	 */
	
	
	static IndividualEvaluator indEval = new ByteCodeIndividualEvaluator();
	
	public static void main(String[] args){
		
		//testProbabilityPropagation();
		
		realMain(args);
		
		//while(true){
		//singleMain(args);
		//}
		//operatorTest();
	}
	
	private static void operatorTest() {
		Operator op1 = Operator.toOperator("++");
		Operator op2 = Operator.toOperator("++");
		System.out.println("Are operators equal: " + op1.equals(op2)); 
	}

	private static void testProbabilityPropagation() {
		
		Sort1Optimised test = new Sort1Optimised();
		Random generator = new Random();
		
		Individual ind=new Individual(test);
		
		
		GPASTNodeData gpData = null;
		ASTNode tempNode = null;
		Iterator<ASTNode> iter = ind.gpMaterial.getAllAllowedNodes().iterator();
		while(iter.hasNext()){
			tempNode = iter.next();
			gpData =(GPASTNodeData) tempNode.getProperty("gpdata"); 
			gpData.changeProbability(generator.nextGaussian());
		}
		
		
		System.out.print("-------------------------------------------------------------");
		printIndividualProbabilities(ind);
		ind = ind.clone();
		System.out.print("-------------------------------------------------------------");
		printIndividualProbabilities(ind);
		ind = ind.clone();
		System.out.print("-------------------------------------------------------------");
		printIndividualProbabilities(ind);
		
		
	}
	
	private static void printIndividualProbabilities(Individual tempInd){
		GPASTNodeData gpData = null;
		ASTNode tempNode = null;
		Iterator<ASTNode> iter = tempInd.gpMaterial.getAllAllowedNodes().iterator();
		while(iter.hasNext()){
			tempNode = iter.next();
			gpData =(GPASTNodeData) tempNode.getProperty("gpdata"); 
			System.out.println( "\n"+gpData.getProbabilityVal()+" : "+tempNode.toString());
		}
		
	}

	public static void singleMain(String[] args){
		//problems.Sort1Optimised te = new problems.Sort1Optimised();
		//sort1ProblemBroken test = new sort1ProblemBroken();
		Sort1Optimised test = new Sort1Optimised();
		
		Individual ind=new Individual(test);

		//ind.addBestTime(5);
		
		//ind = ind.clone();
		indEval.evaluateIndNoTimeLimit(ind);
		
		System.out.println("Get in there " + ind.getFunctionalityErrorCount()+" "+ ind.getRuntimeAvg());
		/*try {
			testFunction(te);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		/*Integer[] a = createRandomIntArray();
		try{
		problems.Sort1Example.sort(a, a.length);
		}catch(Exception e){
			System.out.println("no Compily");
		}*/
	}
	
	
	public static void testFunction(sort1ProblemBroken te){
		Integer[] testArr={1,2,3,4,5};
		//Method m = ind.getMethod();
		Method m = null;
		try {
			m = te.getClass().getMethod("sort", new Class[]{ testArr.getClass(), Integer.class});
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		BytecodeCounter counter = new BytecodeCounter();
		
		String className = "sort1ProblemBroken";//ind.getCompiledClass().getCanonicalName();
		
		//MethodDescriptor ourMD = new MethodDescriptor(m); 
		MethodDescriptor ourMD =new MethodDescriptor( className, "public static void sort(java.lang.Integer[] a,java.lang.Integer length)");
		//public static void Sort1Problem.sort(java.lang.Integer[],java.lang.Integer)
				//"public static void sort(Integer[] a , Integer length)"); // Could be a problem here!!!!!! ---------------
		
		//setClassToInstrument(byte[] classToInstrument)
		//addEntityToInstrumentationState(MethodDescriptor m, EntityToInstrument e) {
		
		/*
		String className = FibonacciAlgorithm.class.getCanonicalName();
		MethodDescriptor fibonacciAlgorithmMD = new MethodDescriptor(
				className,
				"public long fibonacci(long rounds)"); //$NON-NLS-1$
		counter.addEntityToInstrument(fibonacciAlgorithmMD);
		*/
		
		counter.addEntityToInstrument(ourMD);
		//counter.getInstrumentationParams().setUseBasicBlocks(true);
		//counter.getInstrumentationParams().setInstrumentRecursively(true);
		//setClassToInstrument(ind.getCompiledClass().);
		counter.instrument();
		Integer[] anInt = {1,2,3,4,5,6,7,8};
		//counter.getInstrumentationParams().setCountStatically(true);
		counter.execute(ourMD, te.getClass(), new Object[]{anInt,anInt.length});
		
	}
	
	public static void realMain(String[] args) {
		// TODO Auto-generated method stub
	
		Sort1Problem sortProb = new Sort1Problem();
		
		Individual originalIndividual;
		for(int i =0; i< 20 ; i ++){
			originalIndividual=new Individual(sortProb);
			indEval.evaluateInd(originalIndividual); // This is our reference individual
			originalIndividual.ourProblem.setBaselineRuntimeAvg( originalIndividual.getRuntimeAvg() );
			System.out.println("Benchmarked the original individual - " + originalIndividual.getFitness());
		}
		
		
		
		startTest(sortProb);
		/*
		Sort1Tester sortOpt = new Sort1Tester();
		sortOpt.setTestData( sortProb.getTestData() );
		
		startTest(sortOpt);*/
		
/*		Problem triangleProblem = new Triangle1Example();
		Problem triangleSolution = new Triangle1Optimised();
		Problem triangleSolution2 = new TriangleOptimised2();
		Individual iProb=new Individual(triangleProblem);
		Individual iSoln = new Individual(triangleSolution);
		Individual iSoln2 = new Individual(triangleSolution2);

		eval(iProb);
		eval(iSoln);
		eval(iSoln2);

		eval(iProb);
		eval(iSoln);
		eval(iSoln2);*/
		
	}
	
	static Individual startTest(Problem p ){
		Individual ind=new Individual(p);
		return eval(ind);
	}
	
	static Individual eval(Individual anInd ){
		System.out.println("Evalutating " +anInd.ourProblem.getProblemName());
		for ( int i = 0 ; i< 20 ; i++){
			anInd = anInd.clone();
			indEval.evaluateInd(anInd);
			System.out.println("Fit: " + anInd.getFitness() + " score: " + anInd.getFunctionalityErrorCount() + " Running Time: " + anInd.getRunningTime());
		}
		System.out.println("Max: " + anInd.ourProblem.getSeedFunctionalityScore());
		return anInd;
	}
	private static Integer[] createRandomIntArray(){
		Random ran = new Random();
		Integer[] randomArray = new Integer[100 + ran.nextInt(900)];
		for(int i = 0 ; i< randomArray.length; i++)
			randomArray[i] = ran.nextInt();
		return randomArray;
	}

}
