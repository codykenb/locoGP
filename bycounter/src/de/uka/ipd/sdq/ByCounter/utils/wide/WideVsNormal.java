package de.uka.ipd.sdq.ByCounter.utils.wide;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * This class generates source code which serves as input for analysis. 
 * It works as follows: 
 * a) adjust nrOfVars, nrOfIters, arraySize and useArrayExplicitly at the beginning of main -  
 *    keep in mind that JVM specification limits the maximum bytesize of a method
 * b) run main (with generationInsteadOfRun set to true)
 * c) copy cmd output to this class (thus adding a new method to it)
 * d) run ASMifier on the new method (or the entire class)
 * e) create a new Java class in the workspace which contains the ASM bytecode-generating code
 *    obtained in step d)
 * f) remove and modify the <thisclassname>Dump class created in step e) - note that 
 *    if the dump() method is too large, it can be (i) split into several methods or 
 *    (ii) be reduced to generate the code for the considered method only
 * g) Ensure that the dump method of <thisclassname>Dump class writes the bytecode into a file
 * h) Create a class that calls the method in the dumped file, in a way similar to what the 
 *    measurement code in this class' main method is doing
 * i) compare the results - enjoy and publish! 
 * adjust  
 * TODO -server, -Xint etc. testen
 * @author Michael Kuperberg
 *
 */
public class WideVsNormal extends WideVsNormalEmptyHelper{
	
	static boolean generationInsteadOfRun = true;
	
	static boolean numbersAndSizesSmall = true;
	
	static boolean useVerboseOutputInsideGeneratedSource = false;
	
	/**
	 * This method generates source code (by printing it to standard out aka System.out), 
	 * or executes a method (name starts with test). 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		WideVsNormal wvn = new WideVsNormal();
		
		int nrOfVars, nrOfIters, arraySize; //keep maxBytesize of methods in mind...
		if(numbersAndSizesSmall) nrOfVars  = 3; else nrOfVars  = 128;
		if(numbersAndSizesSmall) nrOfIters = 4; else nrOfIters = 1024;
		if(numbersAndSizesSmall) arraySize = 5; else arraySize = 128;
		
		//explicit use of arrays inside the generated source code - increases the bytesize VERY significantly...
		boolean useArrayExplicitly = false;
		
		ArrayList<Long> measurements = new ArrayList<Long>();
		int nrOfMeasurements = 1001;
		
//		int[] selection_numberOfVariables = new int[]{256,512,1024,2048}; 
//		int[] selection_nrOfIterations = new int[]{1024,2048,4096,8192}; 
//		WideOperandType[] selection_type = new WideOperandType[]{WideOperandType.D, WideOperandType.F, WideOperandType.I, WideOperandType.L, WideOperandType.O}; 
//		WideInstructionType[] selection_instr = new WideInstructionType[]{WideInstructionType.LOAD, WideInstructionType.STORE}; 
//		boolean[] selection_useArrayExplicitly = new boolean[]{true,false}; 
//		int[] selection_arraySize = new int[]{0,512,1024,2048};
		
		if(generationInsteadOfRun){
			//fixed operand type and instruction type - current limitation of the generation method itself...
			wvn.randomVariablesIterationGeneration(nrOfVars, nrOfIters, WideOperandType.I, WideInstructionType.STORE, useArrayExplicitly, arraySize);
		}else{
			for(int i=0; i<nrOfMeasurements; i++){
				measurements.add(wvn.testWide_ISTORE_128Vars_1024Iters_trueArrayExplicit_128ArraySize());
			}
			Collections.sort(measurements);
			//TODO compute standard deviation, etc.
			System.out.println("med: "+measurements.get(500)+", "+"min: "+measurements.get(0)+", "+"max: "+measurements.get(1000));
			System.out.println("5%: "+measurements.get(50)+", "+"10%: "+measurements.get(100)+", "+"25%: "+measurements.get(250));
			System.out.println("75%: "+measurements.get(750)+", "+"90%: "+measurements.get(900)+", "+"95%: "+measurements.get(950));
		}
	}
	
	/**
	 * TODO modify: use StringBuffer
	 * @param numberOfVariables
	 * @param nrOfIterations
	 * @param type
	 * @param useArrayExplicitly
	 * @param precomputedArraySize
	 * @param methodName
	 */
	public void printSourceCodeToCmd(int numberOfVariables,
			int nrOfIterations, WideOperandType type,
			boolean useArrayExplicitly, int precomputedArraySize,
			String methodName) {
		String typeAsString;
		if(type==WideOperandType.I){
			typeAsString = "int";
		}else{
			typeAsString = "long";
		}
		Random rd = new Random();
		int[] precomputedArrayInt = new int[precomputedArraySize];
		long[] precomputedArrayLong = new long[precomputedArraySize];

		System.out.println("public long "+methodName+"{");
		System.out.println("  long start = TimerHelper.time();");
		System.out.println("  long stop;");
		if(useArrayExplicitly){
			System.out.println("  Random rd = new Random();");
			System.out.println("  int precomputedArraySize = "+precomputedArraySize+";");
			System.out.println("  "+typeAsString+"[] precomputedArray = new "+typeAsString+"[precomputedArraySize];");
		}else{
			//TODO
		}
		for(int i=0; i<precomputedArraySize; i++){
			precomputedArrayInt[i] = rd.nextInt();
			precomputedArrayLong[i] = rd.nextLong();
			if(useArrayExplicitly){
				if(type==WideOperandType.I){
					System.out.println("  precomputedArray["+i+"] = rd.nextInt();");
				}else{//for future
					System.out.println("  precomputedArray["+i+"] = rd.nextLong();");
				}
			}else{
				//TODO
			}
		}
		for(int i=0; i<numberOfVariables; i++){//TODO parameterise
			System.out.println("  "+typeAsString+" var"+i+" = 0;");
		}
		
		for(int i=0; i<nrOfIterations; i++){
			if(useArrayExplicitly){
				System.out.println("  var"+rd.nextInt(numberOfVariables)+" = precomputedArray["+rd.nextInt(precomputedArraySize)+"];");
			}else{
				if(type==WideOperandType.I){
					System.out.println("  var"+rd.nextInt(numberOfVariables)+" = "+precomputedArrayInt[rd.nextInt(precomputedArraySize)]+";");
				}else{
					System.out.println("  var"+rd.nextInt(numberOfVariables)+" = "+precomputedArrayLong[rd.nextInt(precomputedArraySize)]+";");
				}
			}
		}
//		System.out.println("int numberOfVariables="+numberOfVariables);
//		System.out.println("for(int i=0; i<")
		for(int i=0; i<numberOfVariables; i++){//TODO parameterise
			if(useVerboseOutputInsideGeneratedSource){
				System.out.println("  System.out.println(\"var"+i+" = \"+var"+i+");");
			}else{
				System.out.println("  System.out.println(var"+i+");");
			}
		}
		System.out.println("  stop = TimerHelper.time();");
		System.out.println("  System.out.println(\"Method "+methodName+" : \"+(stop-start)+\" ns " +
				"= \"+(stop-start)/(1000L*1000L)+\" ms\");");
		System.out.println("  return stop-start;");
		System.out.println("}");
	}

	/**
	 * TODO still unparameterised...
	 * @param numberOfVariables
	 * @param nrOfIterations
	 * @param type
	 * @param instr
	 * @param useArrayExplicitly
	 * @param arraySize
	 */
	public void randomVariablesIterationGeneration(
			int numberOfVariables, 
			int nrOfIterations, 
			WideOperandType type, 
			WideInstructionType instr, 
			boolean useArrayExplicitly, 
			int arraySize){
		if(type==WideOperandType.D){
			System.err.println("D not supported");
			return;
		}
		if(type==WideOperandType.F){
			System.err.println("F not supported");
			return;
		}
		if(type==WideOperandType.O){
			System.err.println("O not supported");
			return;
		}
		if(instr==WideInstructionType.IINC){
			System.err.println("IINC not supported");
			return;
		}
		if(instr==WideInstructionType.LOAD){
			System.err.println("LOAD not supported");
			return;
		}
		if(instr==WideInstructionType.RET){
			System.err.println("RET not supported");
			return;
		}
		
		int precomputedArraySize;
		if(arraySize<0){
			precomputedArraySize = 1000;
		}else{
			precomputedArraySize = arraySize;
		}
		
		String methodName = "testWide_"+type+instr+"_"+numberOfVariables+"Vars_"+nrOfIterations+"Iters_"+useArrayExplicitly+"ArrayExplicit_"+precomputedArraySize+"ArraySize()";
		
		printSourceCodeToCmd(
				numberOfVariables, 
				nrOfIterations, 
				type,
				useArrayExplicitly, 
				precomputedArraySize, 
				methodName);
	}
	
}
