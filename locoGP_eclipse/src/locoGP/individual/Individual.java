package locoGP.individual;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import locoGP.experiments.GPConfig;
import locoGP.operators.GPASTNodeData;
import locoGP.operators.GPMaterialVisitor;
import locoGP.operators.StatementOnlyCrossoverOperator;
import locoGP.operators.NodeOperators;
import locoGP.problems.CompilationDetail;
import locoGP.problems.CompilationSet;
import locoGP.problems.Problem;
import locoGP.problems.tests.TestCaseResult;
import locoGP.util.GPSecureClassLoader;
import locoGP.util.JavaClassObject;
import locoGP.util.Logger;
import locoGP.util.ParentProbabilityReferencerVisitor;
import locoGP.util.ProbabilityClonerVisitor;

import org.clapper.util.misc.SparseArrayList;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class Individual implements Comparable, java.io.Serializable { 
	/* This is a data object mostly, with some functions to do with the notion of a GP individual
	 * TODO Replace JDT with javassist http://www.csg.ci.i.u-tokyo.ac.jp/~chiba/javassist/
	 */
	
	private String className = "";
	/*private byte[] classByteArray;
	private Class<?> clazz = null;*/
	
	private long runningTime=0;
	//private long functionalityErrorCount=0; // replaced by testCasesResults
	private static long indIDGlobal=0; // keep a count of the number of new individuals, this is unique individuals and includes those that do not compile
	private long variantIndID = 0 ;
	
	public CompilationSet ASTSet = null;
	private int crossoverAttempts = 0;
	private int mutationAttempts = 0;
	public GPMaterialVisitor gpMaterial = null;
	private boolean changed;
	private boolean crossoverApplied=false;
	private boolean mutationApplied=false;
	
	public static Problem ourProblem = null; 
	//private GPSecureClassLoader classLoader;
	private Map<String, byte[]> classByteMap=null;
	private Map<String, JavaClassObject> classMap=null;
	private boolean compiled = false;
	
	private Class<?> clazz;

	//private String testCaseResults = "";
	private String testCaseResultText = "";
	SparseArrayList<TestCaseResult> testCasesErrorResults = new SparseArrayList<TestCaseResult>();

	private ClassLoader classLoader;
	
	public ClassLoader getClassLoader(){
		return this.classLoader;
	}
	
	public void setNullRefs() {//public void deleteRefs() {
		// the parents of this program may be in the next generation, cause
		// issues?
		this.gpMaterial.purgeRefs();
		this.gpMaterial = null;
		if(classByteMap != null){
			classByteMap.clear();
			classByteMap = null;
		}

		// break references to previous programs,
		this.ASTSet.setNullParentRefs();
		//this.classByteMap = null;
		this.ASTSet = null;
	}
	
	
	public Individual(Problem aProblem) { // this constructor should only be called once
		ourProblem = aProblem;
		//ourProblem.gpConfig = new GPConfig(); // each problem should set a default
		this.ASTSet = aProblem.getStrings();
		// This is the original problem, so revert the name back
		this.setClassName(ourProblem.getEntryClassName()); 
		// set the individual names back also.. yeauck.
		//this.ASTSet.setClassNames(ourProblem.getStrings().getCompilationList()); 
		refreshGlobalGPMaterial(); 
		//initialise(ourProblem.getStrings());
	}
	
	public void setCrossoverSucceeded(){
		crossoverApplied=true;
	}
	
	public void setMutationSucceeded(){
		mutationApplied=true;
	}
	
	public boolean crossoverApplied(){
		return crossoverApplied;
	}
	
	public boolean mutationApplied(){
		return mutationApplied;
	}

	public void addCrossoverAttempts(int numAttempts){
		crossoverAttempts+=numAttempts;
	}
	public void addMutationAttempts(int numAttempts){
		mutationAttempts+=numAttempts;
	}
	public int getCrossoverAttempts(){
		return crossoverAttempts;
	}
	public int getMutationAttempts(){
		return mutationAttempts;
	}
	
	public int getNumNodes(){
		return this.gpMaterial.getAllAllowedNodes().size();
	}
	
	public int getNumGPNodes(){ 
		return this.gpMaterial.getNumGPNodes();
	}
	

	
	/*public static void setID(long id){
		Individual.indID = id;
	}*/
	
	public long getID(){
		return variantIndID;
	}
	
	private String getIDString(){
		return "_"+variantIndID;
	}
	
	public Individual(Individual parent){
		// clone parentAST to here
		
		// TODO evolve patches instead of cloning the whole shebang
		this.ASTSet = parent.ASTSet.clone();
		//this.ASTSet = parent.ASTSet.cloneASTs();
		/*this.gpMaterial = new GPMaterialVisitor();
		this.ASTSet.accept(this.gpMaterial);*/
		//refreshGPMaterial(); // TODO Redesign so this method does not need to be called so often
		setupNewIndividualClassName();
		//this.ASTSet.updateOriginalCodeString();
		//refreshGPMaterial();
	}	
	
	public Individual(Individual seedInd, Problem variantProb, Integer variantID) {
		// special constructor for testing a variant program 
		this.ourProblem = seedInd.ourProblem;
		this.ASTSet = variantProb.getStrings();
		this.setClassName(variantProb.getEntryClassName());
		//this.ASTSet.updateOriginalCodeString();
		refreshGlobalGPMaterial();
		
		if(variantID != null){
			this.variantIndID = variantID;
			this.setClassName(variantProb.getEntryClassName() + getIDString());
			this.ASTSet.updateClassNamesinAST(this.gpMaterial, this.ourProblem, getIDString());
			//this.ASTSet.setClassNames(getIDString(), variantProb.getStrings().getCompilationList());
		}
	}
	
	private synchronized void setVariantIndID(){
		Individual.setIndIDGlobal(Individual.getIndIDGlobal() + 1); // each new individual gets its own id
		variantIndID = Individual.getIndIDGlobal();
	}

	private void setupNewIndividualClassName() {
		setVariantIndID();
		this.setClassName(this.ourProblem.getEntryClassName() + getIDString());
		//this.ASTSet.setClassNames(getIDString(), this.ourProblem.getStrings().getCompilationList());
		
		this.ASTSet.updateClassNamesinAST(this.gpMaterial, this.ourProblem, getIDString());
		refreshGlobalGPMaterial();
		//this.ASTSet.updateClassNames( this.gpMaterial);
		/* these do not update:
		 *   methoddeclaration parameters
		 *   implements clauses
		 * 	 method return types
		 *   @CallerSensitive ?
		 *   static method calls
		 *   array type declaration
		 *   static field access
		 *   @Override ?
		 */
		//this.ASTSet.updateMethodCallClassNames(this.gpMaterial, this.ourProblem);
		//this.ASTSet.updateClassInstantiationNames(this.gpMaterial, this.ourProblem);
	}
	
	/*private synchronized void setupNewIndividualFromAST(Individual parent){
		//Individual.ourProblem = parent.ourProblem; // all individuals get a reference to the same problem
		
	}*/

	private void createNewBiasDataLinkedToParentBiasData(Individual parent){
		// create new bias data objects for this individual, 
		// link each data object to the parents data object at the same node
		// this is heavy on memory, and ultimately not altogether useful
		this.ASTSet.accept( new ProbabilityClonerVisitor(parent.gpMaterial, false) ); // set the values, and link new back to old
	}
	


	public Individual cloneWithFitness(){
		Individual newInd = new Individual(this);
		newInd.createNewBiasDataLinkedToParentBiasData(this);
		newInd.setTestCasesResults(this.testCasesErrorResults);//FunctionalityErrorCount(this.getFunctionalityErrorCount());
		newInd.setRunningTime(this.getRunningTime());
		return newInd;
	}
	
private void setTestCasesResults(SparseArrayList<TestCaseResult> testCasesResultsOther) {
		this.testCasesErrorResults = (SparseArrayList<TestCaseResult>) testCasesResultsOther.clone(); 
		}


	/*	private Individual cloneWithNewChildBiasDataLinkedToParent(){
		Individual newInd = new Individual(this);
		
		return newInd;
	}
	
	private Individual cloneWithoutBias() {
		return new Individual(this);
	}
	*/
	public Individual clone(){
		Individual newInd = new Individual(this);
		if (GPConfig.getLinkNewChildBiasDataWithParent())
			newInd.createNewBiasDataLinkedToParentBiasData(this);
		if(GPConfig.getReferenceChildBiasToParentData())
			newInd.linkChildBiasToParent(this);
			
		//refreshGPMaterial();
		return newInd;
	}
	
	private void linkChildBiasToParent(Individual parent) {
		this.ASTSet.accept(new ParentProbabilityReferencerVisitor(parent.gpMaterial));
	}

	public float getRuntimeAvg(){
		return (float)runningTime/(float)ourProblem.getNumTests();
	}

	public float getCorrectness(){
		/*if(functionalityScore2 ==0 && ourProblem.getSeedFunctionalityScore() ==0)  
			return 0;
		float seedFunc = ourProblem.getSeedFunctionalityScore();*/
		/*
		 * For huffman, if the prefix code is shorter than the one created by the seed, 
		 * we'll get a functionalityScore2 smaller than seed, and then negative functionality 
		 */
		if(ourProblem.getSeedFunctionalityScore()==0)// this keeps it compatible across all problems, Ascon is allowed be 0
			return (float)getFunctionalityErrorCount();
		else
			return ( ( (float)getFunctionalityErrorCount() - (ourProblem.getSeedFunctionalityScore() )) / ourProblem.getSeedFunctionalityScore() );
	}
	
	public float getTimeFitnessRatio(){
		if(getRuntimeAvg()<=0) //should only happen when a program is so bad it overflows long, happens with infinite loop (maybe this should be normalised?)
			return 10000; // if the program didn't run, it gets penalised. 
		else
			return (getRuntimeAvg() )/ourProblem.getBaselineRuntimeAvg();
	}
	
	public float getFitnessT100C() { 
		 
		float correctness = getCorrectness();
		/*if(correctness > 0)
			correctness+= 1; // overall fitness cannot be less than 1
*/		
		
		float returnFitness =0;
		if (correctness <0 )// a negative correctness means the program is functionally better than the seed
			correctness =  -correctness; // the new version is more correct than the original (not possible with sort, hopefully happens with huffman :)
		returnFitness = getTimeFitnessRatio() + 100 * correctness;
		
		/*if( returnFitness >= 1 ) //&& returnFitness <=100 )
			returnFitness = (int)(returnFitness);*/
		//else if (returnFitness > 100)		
		//	returnFitness = ((int)(returnFitness / 100)) * 100  ;
		return returnFitness;
	}
	
	public float getFunctionalityFitness(){
		// performance only matters if result is correct
		float correctness = getCorrectness();
		if (correctness <=0 )
			return getTimeFitnessRatio();
		else return correctness;
	}
	
	public float getFitness(){
		//return getFitnessTCdiv100();
		return getFitnessT100C();
		//return getFunctionalityFitness();
	}
	
	public float getFitnessTCdiv100() { 
		float returnFitness =0; 
		float correctness = getCorrectness();
		
		if(correctness==0)
			return getTimeFitnessRatio();
		
		if (correctness<0) // should only happen if mutant is functionally better than seed
			correctness = -correctness;
				
		if(getTimeFitnessRatio()<1 && correctness >=1 ){
			correctness = correctness / ((float)100);
			return 1 + getTimeFitnessRatio() + correctness;
		}else{
			correctness = correctness / ((float)100);
			return correctness + getTimeFitnessRatio();
		}
	}
	
	public float getFitnessTC() { 
		 
		float correctness = getCorrectness();
		
		float returnFitness =0;
		if (correctness >=0 )
			returnFitness = getTimeFitnessRatio() + correctness; // if a program is not as correct, make sure we can tell
		else // a negative correctness means the program is functionally better than the seed
			returnFitness = getTimeFitnessRatio() + (-correctness); // the new version is more correct than the original (not possible with sort, hopefully happens with huffman :) 
		return returnFitness;
	}

	public String getClassName() {
		// this method should be able to extract the class name from the string using jdt?
		// TODO Fix this hardcode for later experiments
		return className;//+indID;
	}

	public void setClassName(String className) {
		this.className = className;
		
	}

	public String getMethodName() {
		return ourProblem.getMethodName(); 
	}

	public synchronized void addBestTime(long bestTime) {
		if( bestTime ==0){
			System.out.println("GP SEVERE: Setting best time to 0");
		}
		this.runningTime+=bestTime;		
	}
	
	/*public void setFunctionalityErrorCount(long l) {
		this.functionalityErrorCount = l;
	}*/

	public long getFunctionalityErrorCount() {
		// count up the number of errors
 
		// being correct on some data is worth more than returning all test cases 
		int errorCount = 0 ;
		for(int i = 0; i < this.testCasesErrorResults.size(); i++) {
			   errorCount += testCasesErrorResults.get(i).getErrorCount();
			}
		
		// Ascon decrypt can have up to 3 errors per test case, so make missing test cases equal to error count of 5
		//errorCount += (ourProblem.getNumTests() - this.testCasesResults.size()) * 5;
		
		errorCount += 5 *(ourProblem.getMissingTestCaseValue(ourProblem.getNumTests() - this.testCasesErrorResults.size() ));
		//errorCount += ourProblem.getSeedFunctionalityScore() *(ourProblem.getNumTests() - this.testCasesErrorResults.size() )*5;
		
		return errorCount;
	}
	
	

	public long getRunningTime() {
		return this.runningTime;
	}

	protected void setRunningTime(long runningTime) {
		this.runningTime = runningTime;
	}

	/*public long getClassID() {
		return this.indID; 
	}*/

	public Individual crossover(Individual tourneyWinner, GPConfig gpConfig) {
		return gpConfig.xoverOperator.crossover(this, tourneyWinner, gpConfig.isSinglePointEnforced(), gpConfig.isPickBestLocation());
	}
	
	public void clearChangedFlags() {
		GPASTNodeData tempData = null;
		for (Iterator<ASTNode> iter = this.gpMaterial.getAllAllowedNodes().iterator(); iter
				.hasNext();) {
			tempData = (GPASTNodeData) iter.next().getProperty("gpdata");
			tempData.setNotChangedFlag();
			if(tempData.getParentIndividualNodeData()!=null){ 
				tempData.getParentIndividualNodeData().setNotChangedFlag();
			}
		}
	}

	public void updateProbabilitiesWithGaussianNoiseNo() {
		GPASTNodeData tempData = null;
		for (Iterator<ASTNode> iter = this.gpMaterial.getAllAllowedNodes().iterator(); iter
				.hasNext();) {
			tempData = (GPASTNodeData) iter.next().getProperty("gpdata");
			updateNodeProbabilityGaussian(tempData);
		}
	}
	
	private void updateNodeProbabilityGaussian(GPASTNodeData tempData ){
		Random generator = new Random();
		double noise = generator.nextGaussian();
		if(noise<0)
			noise=-noise;
		if(generator.nextBoolean()){
			tempData.increaseNormalisedProbabilitySmall(noise);
		}else{
			tempData.decreaseNormalisedProbability(noise);
		}
	}
	
	
	/*private void updateProbabilitiesDecayTinyNo(){
		updateProbabilitiesDecayNo(.001); 
	}*/
	
	public void updateProbabilitiesDecaySmall(){
		updateProbabilitiesDecay(.1);
	}
	
	public void updateProbabilitiesDecayLarge(){
		updateProbabilitiesDecay(.9);
	}
	
	public void updateProbabilitiesDecayMed(){
		updateProbabilitiesDecay(.5);
	}
	
	/*private void updateProbabilitiesDecayTenthNo(){
		updateProbabilitiesDecayNo(.1);
	}*/
	
	private void updateProbabilitiesDecay(double decayVal){
		 /*Every time we cannot evaluate the program properly, we should decay the locations.
		 * If a change at a particular location produces a no-compile, or runtime error 
		 * reduce the bias at the location of change. Assumed this will be run on an individual
		 * in its role as a parent.*/  
		GPASTNodeData tempData = null;
		ASTNode curNode = null;
		
			for (Iterator<ASTNode> iter = this.gpMaterial.getAllAllowedNodes().iterator(); iter
					.hasNext();) {
				curNode=iter.next();
				tempData = (GPASTNodeData) curNode.getProperty("gpdata");
				
				if (tempData == null)  // should never happen
					Logger.logTrash("Found a location without gpdata!");
				else if ( tempData.hasChanged()){
					tempData.decreaseNormalisedProbability(decayVal);
				}
			}
			this.clearChangedFlags();
	}
	
	private void updateProbabilitiesnot(Individual parentInd, Individual otherParentInd) {
		updateProbabilities(parentInd);
		updateProbabilities(otherParentInd);	
		//updateProbabilitiesWithGaussianNoise();
	}
	
	public void updateProbabilities(Individual parentInd) {
		/* PHD what is the best way of updating these values?
		 * 
		 * When a program is modified, we have
		 *  3 programs, 2 parents, 1 offspring
		 *  Locations in parents. 
		 *  Fitness of all 3	
		 */
		
		GPASTNodeData tempData = null;
		ASTNode curNode = null;
		
			for (Iterator<ASTNode> iter = this.gpMaterial.getAllAllowedNodes().iterator(); iter
					.hasNext();) {
				curNode=iter.next();
				tempData = (GPASTNodeData) curNode.getProperty("gpdata");
				
				if (tempData == null){  // should never happen
					Logger.logTrash("Found a location without gpdata!");
				}else if ( tempData.hasChanged()){	
					updateProbabilitiesRuleset49(parentInd, tempData);				
				}
			}
			parentInd.clearChangedFlags();
	}
	
	public void updateProbabilitiesDecay(){
		updateProbabilitiesDecayLarge();
	}
	
	private void updateProbabilitiesRuleset49(Individual parentInd, GPASTNodeData tempData ){
		// set the changed node in new program to 0
		// half the chance of the parent node being selected again
		
			if(tempData.getParentIndividualNodeData() !=null){
				tempData.getParentIndividualNodeData().setProbabilityVal(tempData.getParentIndividualNodeData().getProbabilityVal()/2);			
			}
			tempData.setProbabilityVal(0);
	}
		
	private void updateProbabilitiesRuleset48(Individual parentInd, GPASTNodeData tempData ){
		// if this program compiles, then location which changed to 
		// produced this program should be updated in the parent
		
		if(this.compiled()){
			// why is this returning null? newly changed nodes should have a parent? 
			// unless a portion of the tree is marked as changed!
			if(tempData.getParentIndividualNodeData() !=null){
				tempData.getParentIndividualNodeData().incrementCompileCount();
				tempData.setProbabilityVal(0);
			}else
				tempData.incrementCompileCount();
		// the ratio of perfRed / compiled determines the bias value returned
		if(this.getRuntimeAvg()<= this.ourProblem.getBaselineRuntimeAvg())
			if(tempData.getParentIndividualNodeData() !=null){
				tempData.getParentIndividualNodeData().incrementPerfRedCount();
				tempData.setProbabilityVal(0);
			}else 
				tempData.incrementCompileCount();
		}
		//tempData.setProbabilityVal(0); // Boom! dead node, restricts changing this node back to the seed, explore the ramifications of this modification by making it the most unlikely to be edited.
	}

	private void updateProbabilitiesRuleset47(Individual parentInd, GPASTNodeData tempData ){
		// if this program compiles, then location which changed to 
		// produced this program should be updated in the parent
		if(this.compiled()){
		tempData.getParentIndividualNodeData().incrementCompileCount();
		// the ratio of perfRed / compiled determines the bias value returned
		if(this.getRuntimeAvg()<= this.ourProblem.getBaselineRuntimeAvg())
			tempData.getParentIndividualNodeData().incrementPerfRedCount();
		}
	}
	
	private void updateProbabilitiesRuleset46(Individual parentInd, GPASTNodeData tempData ){
		// buggy!!
		if(this.compiled()){
		tempData.incrementCompileCount();
		// the ratio of perfRed / compiled determines the bias value returned
		if(this.getRuntimeAvg()<= this.ourProblem.getBaselineRuntimeAvg())
			tempData.incrementPerfRedCount();
		}
	}
			
	private void updateProbabilitiesRuleset45(Individual parentInd, GPASTNodeData tempData ){
		
		/*
		 * If the Functionality decreases, change the parent there again
		 * Taken from R15, added non-return nodes in child
		 */

		if (this.getFunctionalityErrorCount()< parentInd.getFunctionalityErrorCount()){
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(.5);
			tempData.setProbabilityVal(0); // non-return "dead" node, cannot be modified again in that lineage 
			// decreaseNormalisedProbability(.1);
		}else{
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(.1);
			tempData.setProbabilityVal(0);
			//tempData.increaseNormalisedProbability(.5);
		}		
	}

	
	
	private void updateProbabilitiesRuleset44(Individual parentInd,
			GPASTNodeData tempData) {
		if (this.getRuntimeAvg() < this.ourProblem.getBaselineRuntimeAvg()) {
			tempData.getParentIndividualNodeData()
					.biasHigh();
			tempData.biasHigh();
		} else {
			tempData.getParentIndividualNodeData()
					.decreaseNormalisedProbability(.5);
			tempData.decreaseNormalisedProbability(.5);
		}
	}
	
	private void updateProbabilitiesRuleset40(Individual parentInd,
			GPASTNodeData tempData) {
		updateProbabilitiesRuleset15(parentInd, tempData);
		updateProbabilitiesRuleset38(parentInd, tempData);
	}
	
	private void updateProbabilitiesRuleset42(Individual parentInd,
			GPASTNodeData tempData) {
		if (this.getRuntimeAvg() < this.ourProblem.getBaselineRuntimeAvg()) {
			tempData.getParentIndividualNodeData()
					.increaseNormalisedProbability(1);
			tempData.increaseNormalisedProbability(1);
		} else {
			tempData.getParentIndividualNodeData()
					.decreaseNormalisedProbability(.1);
			tempData.decreaseNormalisedProbability(.1);
		}
	}
	
	private void updateProbabilitiesRuleset43(Individual parentInd,
			GPASTNodeData tempData) {
		if (this.getRuntimeAvg() < this.ourProblem.getBaselineRuntimeAvg()) {
			tempData.getParentIndividualNodeData()
					.increaseNormalisedProbabilityDiminished(1);
			tempData.increaseNormalisedProbabilityDiminished(1);
		} else {
			tempData.getParentIndividualNodeData()
					.decreaseNormalisedProbability(.5);
			tempData.decreaseNormalisedProbability(.5);
		}
	}
	
	private void updateProbabilitiesRuleset41(Individual parentInd,
			GPASTNodeData tempData) {
		if (this.getRuntimeAvg() < this.ourProblem.getBaselineRuntimeAvg()) {
			tempData.getParentIndividualNodeData()
					.increaseNormalisedProbabilityDiminished(1);
			tempData.increaseNormalisedProbabilityDiminished(1);
		} else {
			tempData.getParentIndividualNodeData()
					.decreaseNormalisedProbability(1);
			tempData.decreaseNormalisedProbability(1);
		}
	}
	
	private void updateProbabilitiesRuleset39(Individual parentInd,
			GPASTNodeData tempData) {
		if (this.getRuntimeAvg() < this.ourProblem.getBaselineRuntimeAvg()) {
			tempData.getParentIndividualNodeData()
					.increaseNormalisedProbability(1);
			tempData.increaseNormalisedProbability(1);
		} else {
			tempData.getParentIndividualNodeData()
					.decreaseNormalisedProbability(.1);
			tempData.decreaseNormalisedProbability(.1);
		}
	}
	
	private void updateProbabilitiesRuleset38(Individual parentInd,
			GPASTNodeData tempData) {
		/*
		 * 38th time a charm :/
		 */
		if (this.getRuntimeAvg() < this.ourProblem.getBaselineRuntimeAvg()) {
			tempData.getParentIndividualNodeData()
					.increaseNormalisedProbability(1);
			tempData.decreaseNormalisedProbability(1);
		} else {
			tempData.getParentIndividualNodeData()
					.decreaseNormalisedProbability(.5);
			tempData.decreaseNormalisedProbability(.5);
		}
	}

	
	private void updateProbabilitiesRuleset37(Individual parentInd, GPASTNodeData tempData ){
		
	if(this.getFunctionalityErrorCount()== parentInd.getFunctionalityErrorCount() ){
		// intron!
		tempData.decreaseNormalisedProbability(1);
		tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1);
	}else if (this.getFunctionalityErrorCount()< parentInd.getFunctionalityErrorCount()){ // if we decrease functionality
		if(this.getRunningTime() < parentInd.getRunningTime()){ // and *decrease* running time, we break the program more
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(1);
			tempData.decreaseNormalisedProbability(1);
		}else{
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1);
			tempData.increaseNormalisedProbability(1);
		}
	}else{
		tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1);
		tempData.increaseNormalisedProbability(1);
	}		
}
	
	private void updateProbabilitiesRuleset36(Individual parentInd, GPASTNodeData tempData ){
		
	if(this.getFunctionalityErrorCount()== parentInd.getFunctionalityErrorCount() ){
		// intron!
		tempData.decreaseNormalisedProbability(1);
		tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1);
	}else if (this.getFunctionalityErrorCount()< parentInd.getFunctionalityErrorCount()){ // if we decrease functionality
		if(this.getRunningTime() > parentInd.getRunningTime()){ // and increase running time, we break the program more
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(1);
			tempData.decreaseNormalisedProbability(1);
		}else{
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1);
			tempData.increaseNormalisedProbability(1);
		}
	}else{
		tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1);
		tempData.increaseNormalisedProbability(1);
	}		
}
	
	private void updateProbabilitiesRuleset35(Individual parentInd, GPASTNodeData tempData ){
		
	if(this.getFunctionalityErrorCount()== parentInd.getFunctionalityErrorCount() ){
		// intron!
		tempData.decreaseNormalisedProbability(1);
		tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1);
	}else if (this.getFunctionalityErrorCount()> parentInd.getFunctionalityErrorCount()){
		tempData.getParentIndividualNodeData().increaseNormalisedProbability(1);
		tempData.decreaseNormalisedProbability(1);
	}else{
		tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1);
		tempData.increaseNormalisedProbability(1);
	}		
}
	
	private void updateProbabilitiesRuleset34(Individual parentInd, GPASTNodeData tempData ){
		
	if(this.getRunningTime()== parentInd.getRunningTime() ){
		// intron!
		tempData.decreaseNormalisedProbability(1);
		tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1);
	}
	
	if (this.getFunctionalityErrorCount()< parentInd.getFunctionalityErrorCount()){
		tempData.getParentIndividualNodeData().increaseNormalisedProbability(1);
		tempData.decreaseNormalisedProbability(1);
	}else{
		tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1);
		tempData.increaseNormalisedProbability(1);
	}		
}
private void updateProbabilitiesRuleset33(Individual parentInd, GPASTNodeData tempData ){
		
		/*
		 * If the Functionality decreases, change the parent there again
		 * (opposite of R14)
		 */

		if (this.getFunctionalityErrorCount()< parentInd.getFunctionalityErrorCount()){
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(1);
			tempData.decreaseNormalisedProbability(1);
		}else{
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1);
			tempData.increaseNormalisedProbability(1);
		}		
	}
	
	
private void updateProbabilitiesRuleset32(Individual parentInd, GPASTNodeData tempData ){
		
		/*
		 * From R31, small bias increase bias only (.2 instead of .5)
		 */
		 
		
	
		// taken from R15
		if (this.getFunctionalityErrorCount()< parentInd.getFunctionalityErrorCount()){
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(.2);
			//tempData.decreaseNormalisedProbability(.5);
		}else{
			//tempData.getParentIndividualNodeData().decreaseNormalisedProbability(.5);
			tempData.increaseNormalisedProbability(.2);
		}		
	}
	
private void updateProbabilitiesRuleset31(Individual parentInd, GPASTNodeData tempData ){
		
		/*
		 * From R15, increases bias only
		 */
		 
		// taken from R15
		if (this.getFunctionalityErrorCount()< parentInd.getFunctionalityErrorCount()){
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(.5);
			//tempData.decreaseNormalisedProbability(.5);
		}else{
			//tempData.getParentIndividualNodeData().decreaseNormalisedProbability(.5);
			tempData.increaseNormalisedProbability(.5);
		}		
	}
	
private void updateProbabilitiesRuleset30(Individual parentInd, GPASTNodeData tempData ){
		
		/*
		 * combines R29 and R28
		 */
		 
		// 	taken from R26
			 if(this.getRunningTime()== parentInd.getRunningTime() ){
				// intron!
				tempData.decreaseNormalisedProbability(.5);
				tempData.getParentIndividualNodeData().decreaseNormalisedProbability(.5);
			}
	
		// taken from R15
		if (this.getFunctionalityErrorCount()< parentInd.getFunctionalityErrorCount()){
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(.5);
			tempData.decreaseNormalisedProbability(.5);
		}else{
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(.5);
			tempData.increaseNormalisedProbability(.5);
		}		
	}
	
private void updateProbabilitiesRuleset29(Individual parentInd, GPASTNodeData tempData ){
		
		/*
		 * From R15 add intron checking
		 */
		 
		// 	taken from R26
			 if(this.getRunningTime()== parentInd.getRunningTime() ){
				// intron!
				tempData.decreaseNormalisedProbability(.5);
				tempData.getParentIndividualNodeData().decreaseNormalisedProbability(.5);
			}
	
		// taken from R15
		if (this.getFunctionalityErrorCount()< parentInd.getFunctionalityErrorCount()){
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(.5);
			tempData.decreaseNormalisedProbability(.1);
		}else{
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(.1);
			tempData.increaseNormalisedProbability(.5);
		}		
	}
	
private void updateProbabilitiesRuleset28(Individual parentInd, GPASTNodeData tempData ){
		
		/*
		 * From R15 -  make decreases more pronounced 
		 */
		 
		// taken from R15
		if (this.getFunctionalityErrorCount()< parentInd.getFunctionalityErrorCount()){
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(.5);
			tempData.decreaseNormalisedProbability(.5);
		}else{
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(.5);
			tempData.increaseNormalisedProbability(.5);
		}		
	}
	
	private void updateProbabilitiesRuleset25(Individual parentInd, GPASTNodeData tempData ){
		// Taken from R22
		// Reduces bias magnitude (increaseNormalisedProbabilitySmall instead of increaseNormalisedProbability)
		if(	this.getFunctionalityErrorCount()< (this.ourProblem.getWorstFunctionalityScore()/2) // if we didnt break the program 
				&& this.getRuntimeAvg() != parentInd.getRuntimeAvg() ){  // but the Performance changed
			
			float change = parentInd.getFitness() - this.getFitness() ;
			if (change < 0 )
				change = change *-1;
			/* we changed the program, and this changed the performance,
			 * something interesting here, don't change again in the offspring, change again in parent
			 */
			tempData.decreaseNormalisedProbability( change / (parentInd.getFitness()+this.getFitness()) );
			tempData.getParentIndividualNodeData().increaseNormalisedProbabilitySmall(change / (parentInd.getFitness()+this.getFitness()));
		}else{ // low functionality, or runtime didnt change, not very interesting
			tempData.increaseNormalisedProbabilityTiny(1-parentInd.getCorrectness()); // changed to increase (17)
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1-parentInd.getCorrectness());
		}
	}
	
	private void updateProbabilitiesRuleset24(Individual parentInd, GPASTNodeData tempData ){
		// Taken from R22
		// Reduces bias magnitude (increaseNormalisedProbabilitySmall instead of increaseNormalisedProbability)
		if(	this.getFunctionalityErrorCount()< (this.ourProblem.getWorstFunctionalityScore()/2) // if we didnt break the program 
				&& this.getRuntimeAvg() != parentInd.getRuntimeAvg() ){  // but the Performance changed
			
			float change = parentInd.getFitness() - this.getFitness() ;
			if (change < 0 )
				change = change *-1;
			/* we changed the program, and this changed the performance,
			 * something interesting here, don't change again in the offspring, change again in parent
			 */
			tempData.decreaseNormalisedProbability( change / (parentInd.getFitness()+this.getFitness()) );
			tempData.getParentIndividualNodeData().increaseNormalisedProbabilitySmall(change / (parentInd.getFitness()+this.getFitness()));
		}else{ // low functionality, or runtime didnt change, not very interesting
			tempData.increaseNormalisedProbabilitySmallish(1-parentInd.getCorrectness()); // changed to increase (17)
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1-parentInd.getCorrectness());
		}
	}
	
	private void updateProbabilitiesRuleset22(Individual parentInd, GPASTNodeData tempData ){
		// Taken from R17
		// Reduces bias magnitude (increaseNormalisedProbabilitySmall instead of increaseNormalisedProbability)
		if(	this.getFunctionalityErrorCount()< (this.ourProblem.getWorstFunctionalityScore()/2) // if we didnt break the program 
				&& this.getRuntimeAvg() != parentInd.getRuntimeAvg() ){  // but the Performance changed
			
			float change = parentInd.getFitness() - this.getFitness() ;
			if (change < 0 )
				change = change *-1;
			/* we changed the program, and this changed the performance,
			 * something interesting here, don't change again in the offspring, change again in parent
			 */
			tempData.decreaseNormalisedProbability( change / (parentInd.getFitness()+this.getFitness()) );
			tempData.getParentIndividualNodeData().increaseNormalisedProbabilitySmall(change / (parentInd.getFitness()+this.getFitness()));
		}else{ // low functionality, or runtime didnt change, not very interesting
			tempData.increaseNormalisedProbabilitySmall(1-parentInd.getCorrectness()); // changed to increase (17)
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1-parentInd.getCorrectness());
		}
	}
	
	
	private void updateProbabilitiesRuleset21(Individual parentInd, GPASTNodeData tempData ){
		// Taken from R17 (which extends R17, R5rr) as it is the best we have.
		// increase based on change in performance, not fitness
		if(	this.getFunctionalityErrorCount()< (this.ourProblem.getWorstFunctionalityScore()/2) // if we didnt break the program 
				&& this.getRuntimeAvg() != parentInd.getRuntimeAvg() ){  // but the Performance changed
			
			//float change = parentInd.getFitness() - this.getFitness() ;
			float change = parentInd.getRuntimeAvg() - this.getRuntimeAvg();
			if (change < 0 )
				change = change *-1;
			// smaller the change the better
			tempData.decreaseNormalisedProbability(((parentInd.getRuntimeAvg()+this.getRuntimeAvg())- change) / (parentInd.getRuntimeAvg()+this.getRuntimeAvg()) );
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(((parentInd.getRuntimeAvg()+this.getRuntimeAvg())- change) / (parentInd.getRuntimeAvg()+this.getRuntimeAvg()));
		}else{ // low functionality, or runtime didnt change, not very interesting
			tempData.increaseNormalisedProbability(1-parentInd.getCorrectness()); // changed to increase (17)
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1-parentInd.getCorrectness());
		}
	}
	
	private void updateProbabilitiesRuleset20(Individual parentInd, GPASTNodeData tempData ){
		// Taken from R17 (which extends R17, R5rr) as it is the best we have.
		// increase based on change in performance, not fitness
		if(	this.getFunctionalityErrorCount()< (this.ourProblem.getWorstFunctionalityScore()/2) // if we didnt break the program 
				&& this.getRuntimeAvg() != parentInd.getRuntimeAvg() ){  // but the Performance changed
			
			//float change = parentInd.getFitness() - this.getFitness() ;
			float change = parentInd.getRuntimeAvg() - this.getRuntimeAvg();
			if (change < 0 )
				change = change *-1;
			tempData.decreaseNormalisedProbability( change / (parentInd.getRuntimeAvg()+this.getRuntimeAvg()) );
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(change / (parentInd.getRuntimeAvg()+this.getRuntimeAvg()));
		}else{ // low functionality, or runtime didnt change, not very interesting
			tempData.increaseNormalisedProbability(1-parentInd.getCorrectness()); // changed to increase (17)
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1-parentInd.getCorrectness());
		}
	}
	
	private void updateProbabilitiesRuleset19(Individual parentInd, GPASTNodeData tempData ){
		// Taken from R17 (which extends R5rr) as it is the best we have.
		// this version tests if the runtime changed only, not the overall fitness
		// and reduces the worst functionality threshold(/4 instead of /2)
		if(	this.getFunctionalityErrorCount()< (this.ourProblem.getWorstFunctionalityScore()/4) // if we didnt break the program 
				&& this.getRuntimeAvg() != parentInd.getRuntimeAvg() ){  // but the Performance changed
			
			float change = parentInd.getFitness() - this.getFitness() ;
			if (change < 0 )
				change = change *-1;
			/* we changed the program, and this changed the performance,
			 * something interesting here, don't change again in the offspring, change again in parent
			 */
			tempData.decreaseNormalisedProbability( change / (parentInd.getFitness()+this.getFitness()) );
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(change / (parentInd.getFitness()+this.getFitness()));
		}else{ // low functionality, or runtime didnt change, not very interesting
			tempData.increaseNormalisedProbability(1-parentInd.getCorrectness()); // changed to increase (17)
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1-parentInd.getCorrectness());
		}
	}
	
	private void updateProbabilitiesRuleset18(Individual parentInd, GPASTNodeData tempData ){
		/* copied from R5rr
		 * Adding bigger increases
		 */
		
		if(	this.getFunctionalityErrorCount()< this.ourProblem.getWorstFunctionalityScore() 
				&& this.getFitness() != parentInd.getFitness() ){ 
			/* some functionality, performance change
			 TODO how do we know if prefixCode does anything useful at all? Does it get anything right?
			*/
			float change = parentInd.getFitness() - this.getFitness() ;
			if (change < 0 )
				change = change *-1;
			/* we changed the program, and this changed the performance,
			 * something interesting here, don't change again in the offspring, change again in parent
			 */
			tempData.decreaseNormalisedProbability( change / (parentInd.getFitness()+this.getFitness()) );
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(.99);
		}else{ // no functionality, reduce
			// we broke the program, probably not that interesting to change in the parent
			// the offspring is borked, so low chance of being picked, but if it is, change a diff location
			tempData.decreaseNormalisedProbability(1-parentInd.getCorrectness());
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1-parentInd.getCorrectness());
		}
	}
	
	private void updateProbabilitiesRuleset17(Individual parentInd, GPASTNodeData tempData ){
		// Taken from R5rr, as it is the best we have.
		// this version tests if the runtime changed only, not the overall fitness
		// and reduces the worst functionality threshold(/2)
		if(	this.getFunctionalityErrorCount()< (this.ourProblem.getWorstFunctionalityScore()/2) // if we didnt break the program 
				&& this.getRuntimeAvg() != parentInd.getRuntimeAvg() ){  // but the Performance changed
			
			float change = parentInd.getFitness() - this.getFitness() ;
			if (change < 0 )
				change = change *-1;
			/* we changed the program, and this changed the performance,
			 * something interesting here, don't change again in the offspring, change again in parent
			 */
			tempData.decreaseNormalisedProbability( change / (parentInd.getFitness()+this.getFitness()) );
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(change / (parentInd.getFitness()+this.getFitness()));
		}else{ // low functionality, or runtime didnt change, not very interesting
			tempData.increaseNormalisedProbability(1-parentInd.getCorrectness()); // changed to increase (17)
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1-parentInd.getCorrectness());
		}
	}
	
	
	
private void updateProbabilitiesRuleset16(Individual parentInd, GPASTNodeData tempData ){
		
		/*
		 * changes that give higher fitness are better, 
		 * changes that give big performance change are good
		 *  (really clutching at straws here :/ )
		 */
	long functionalityChange = this.getFunctionalityErrorCount() - parentInd.getFunctionalityErrorCount();
	if (functionalityChange <0)
		functionalityChange = - functionalityChange; 
	float performanceChange = this.getRuntimeAvg() - parentInd.getRuntimeAvg();
	if(performanceChange <0)
		performanceChange = -performanceChange;
	
	if(this.getFitness() > parentInd.getFitness()){ // if we improved the program, don't change the new prog there again
		tempData.decreaseNormalisedProbability(performanceChange/this.ourProblem.getBaselineRuntimeAvg());
		tempData.increaseNormalisedProbability( functionalityChange / this.ourProblem.getSeedFunctionalityScore());
		tempData.getParentIndividualNodeData().increaseNormalisedProbability(performanceChange/this.ourProblem.getBaselineRuntimeAvg());
		tempData.getParentIndividualNodeData().decreaseNormalisedProbability(functionalityChange/this.ourProblem.getSeedFunctionalityScore());
	}else{ // if we broke the program, dont change the parent there again..
		tempData.increaseNormalisedProbability(performanceChange/this.ourProblem.getBaselineRuntimeAvg());
		tempData.decreaseNormalisedProbability( functionalityChange / this.ourProblem.getSeedFunctionalityScore());
		tempData.getParentIndividualNodeData().decreaseNormalisedProbability(performanceChange/this.ourProblem.getBaselineRuntimeAvg());
		tempData.getParentIndividualNodeData().increaseNormalisedProbability(functionalityChange/this.ourProblem.getSeedFunctionalityScore());
	}
	
	
	
	}

private void updateProbabilitiesRuleset15(Individual parentInd, GPASTNodeData tempData ){
		
		/*
		 * If the Functionality decreases, change the parent there again
		 * (opposite of R14)
		 */

		if (this.getFunctionalityErrorCount()< parentInd.getFunctionalityErrorCount()){
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(.5);
			tempData.decreaseNormalisedProbability(.1);
		}else{
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(.1);
			tempData.increaseNormalisedProbability(.5);
		}		
	}

private void updateProbabilitiesRuleset14(Individual parentInd, GPASTNodeData tempData ){
		
		/*
		 * If the Functionality increases, change the parent there again
		 * 
		 */

		if (this.getFunctionalityErrorCount()> parentInd.getFunctionalityErrorCount()){
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(.5);
			tempData.decreaseNormalisedProbability(.1);
		}else{
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(.1);
			tempData.increaseNormalisedProbability(.5);
		}		
	}
private void updateProbabilitiesRuleset13(Individual parentInd, GPASTNodeData tempData ){
		
		/*
		 * If the PERFORMANCE decreases, change the parent there again
		 * (opposite of R12)
		 */

		if (this.getRuntimeAvg() < parentInd.getRuntimeAvg()){
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(.5);
			tempData.decreaseNormalisedProbability(.1);
		}else{
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(.1);
			tempData.increaseNormalisedProbability(.5);
		}		
	}
private void updateProbabilitiesRuleset12(Individual parentInd, GPASTNodeData tempData ){
		
		/*
		 * If the PERFORMANCE increases, change the parent there again
		 * 
		 */

		if (this.getRuntimeAvg() > parentInd.getRuntimeAvg()){
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(.5);
			tempData.decreaseNormalisedProbability(.1);
		}else{
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(.1);
			tempData.increaseNormalisedProbability(.5);
		}		
	}
	
private void updateProbabilitiesRuleset11(Individual parentInd, GPASTNodeData tempData ){
		
		/*
		 * If the fitness increases, don't change the parent there again, we want to break programs as much as possible
		 * (opposite of ruleset 10)  
		 */

		if (this.getFitness() < parentInd.getFitness()){
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(.5);
			tempData.decreaseNormalisedProbability(.1);
		}else{
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(.1);
			tempData.increaseNormalisedProbability(.5);
		}		
	}
	
private void updateProbabilitiesRuleset10(Individual parentInd, GPASTNodeData tempData ){
		
		/*
		 * If the fitness increases, then increase change at this location in the parent
		 * (This might get us stuck moving backwards and forwards between the same group of programs, without any proper search.  
		 */

		if (this.getFitness() > parentInd.getFitness()){
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(.5);
			tempData.decreaseNormalisedProbability(.1);
		}else{
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(.1);
			tempData.increaseNormalisedProbability(.5);
		}		
	}
	
private void updateProbabilitiesRuleset9(Individual parentInd, GPASTNodeData tempData ){
		
		/*
		 * Break the parent as much as possible
		 * 
		 */

		long functionalityChangeRatio = this.getFunctionalityErrorCount() / parentInd.getFunctionalityErrorCount();
		float performanceChangeRatio = this.getRuntimeAvg() / parentInd.getRuntimeAvg();
		
		long functionalityChange = this.getFunctionalityErrorCount() - parentInd.getFunctionalityErrorCount();
		if (functionalityChange <0)
			functionalityChange = - functionalityChange; 
		
		float performanceChange = this.getRuntimeAvg() - parentInd.getRuntimeAvg();
		// bigger this ratio the better
				float perfFunRatio = performanceChange / functionalityChange;
		if(performanceChange <0)
			performanceChange = -performanceChange;
		
		if (performanceChangeRatio > functionalityChangeRatio){ // don't change the parent there again
			tempData.getParentIndividualNodeData().decreaseProbability(perfFunRatio);
			tempData.increaseProbability(perfFunRatio);
		}else{ // if we really broke the program, change there again
			tempData.getParentIndividualNodeData().increaseProbability(perfFunRatio);
			tempData.decreaseProbability(perfFunRatio);
		}		
	}
	
	private void updateProbabilitiesRuleset8(Individual parentInd, GPASTNodeData tempData ){
		
		/*
		 * If funtionality changes more than performance, don't change there again
		 * 
		 */
		// this number will be smaller, if we didn't break the program much, unlikely though
		// plus you have to break the program to redesign right?
		long functionalityChangeRatio = this.getFunctionalityErrorCount() / parentInd.getFunctionalityErrorCount();
		float performanceChangeRatio = this.getRuntimeAvg() / parentInd.getRuntimeAvg();
		
		long functionalityChange = this.getFunctionalityErrorCount() - parentInd.getFunctionalityErrorCount();
		if (functionalityChange <0)
			functionalityChange = - functionalityChange; 
		
		float performanceChange = this.getRuntimeAvg() - parentInd.getRuntimeAvg();
		// bigger this ratio the better
				float perfFunRatio = performanceChange / functionalityChange;
		if(performanceChange <0)
			performanceChange = -performanceChange;
		
		if (performanceChangeRatio > functionalityChangeRatio){ // change the parent there again
			tempData.getParentIndividualNodeData().increaseProbability(perfFunRatio);
			tempData.decreaseProbability(perfFunRatio);
			
		}else{ // if we really broke the program, don't change there again
			tempData.getParentIndividualNodeData().decreaseProbability(perfFunRatio);
			tempData.increaseProbability(perfFunRatio);
		}		
	}
	
	private void updateProbabilitiesRuleset7(Individual parentInd, GPASTNodeData tempData ){
		// reduce the threshold for acceptable functionality
		/*
		 * if we can change the performance, without changing the functionality
		 * then we have found an interesting location. 
		 * this could be just adding redundant code = increases performance
		 * 
		 * the more we can impact performance without affecting functionality, the better
		 * 
		 */
		long functionalityChange = this.getFunctionalityErrorCount() - parentInd.getFunctionalityErrorCount();
		if (functionalityChange <0)
			functionalityChange = - functionalityChange; 
		
		float performanceChange = this.getRuntimeAvg() - parentInd.getRuntimeAvg();
		if(performanceChange <0)
			performanceChange = -performanceChange;
		
		// bigger this ratio the better
		float perfFunRatio = performanceChange / functionalityChange;
		
		// we'll change the parent there again 
		tempData.getParentIndividualNodeData().increaseProbability(perfFunRatio);
		tempData.decreaseProbability(perfFunRatio);
		
	}
	
	
	private void updateProbabilitiesRuleset27(Individual parentInd, GPASTNodeData tempData ){
		// Take from R6, reduce the threshold for acceptable functionality
		if( this.getFunctionalityErrorCount()< (this.ourProblem.getWorstFunctionalityScore()/2)
				&& this.getFitness() != parentInd.getFitness() ){ // some functionality, performance change
			// increase can be larger than 1
			tempData.increaseNormalisedProbability( parentInd.getFitness() / this.getFitness() );
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(parentInd.getFitness() / this.getFitness());
		}else{ // no functionality, reduce
			// how bad was it broken? how much functionality did we lose?
			// decrease by 1 or less every time
			tempData.decreaseNormalisedProbability(1-parentInd.getCorrectness());
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1-parentInd.getCorrectness());
		}
	}
	
	
	private void updateProbabilitiesRuleset6(Individual parentInd, GPASTNodeData tempData ){
		// reduce the threshold for acceptable functionality
		if( this.getFunctionalityErrorCount()< (this.ourProblem.getWorstFunctionalityScore()/2)
				&& this.getFitness() != parentInd.getFitness() ){ // some functionality, performance change
			// increase can be larger than 1
			tempData.increaseProbability( parentInd.getFitness() / this.getFitness() );
			tempData.getParentIndividualNodeData().increaseProbability(parentInd.getFitness() / this.getFitness());
		}else{ // no functionality, reduce
			// how bad was it broken? how much functionality did we lose?
			// decrease by 1 or less every time
			tempData.decreaseProbability(1-parentInd.getCorrectness());
			tempData.getParentIndividualNodeData().decreaseProbability(1-parentInd.getCorrectness());
		}
	}
	
	private void updateProbabilitiesRuleset3rr(Individual parentInd, GPASTNodeData tempData ){
		// bck 19-May-2013, reverting to checking functionality score, why was this removed? (cause we inverted the functionality measure!)
		// Ruleset #3 (Finding a "lever" on performance)
		// how do we check that the program compiled and ran, and does *something*?
		// for sort this could be a "same" flag (input=output), for huffman?
		int baseFuncWorst = this.ourProblem.getWorstFunctionalityScore();
		int thisFunc = (int) this.getFunctionalityErrorCount();
		if( thisFunc< baseFuncWorst
				&& this.getFitness() != parentInd.getFitness() ){ // some functionality, performance change
			// increase can be larger than 1
			tempData.increaseProbability( parentInd.getFitness() / this.getFitness() );
			tempData.getParentIndividualNodeData().increaseProbability(parentInd.getFitness() / this.getFitness());
		}else{ // no functionality, reduce
			// how bad was it broken? how much functionality did we lose?
			// decrease by 1 or less every time
			tempData.decreaseProbability(1-parentInd.getCorrectness());
			tempData.getParentIndividualNodeData().decreaseProbability(1-parentInd.getCorrectness());
		}
	}
	
	
	private void updateProbabilitiesRuleset23(Individual parentInd, GPASTNodeData tempData ){
		// Ruleset #3 (Finding a "lever" on performance)
		if( this.getFitness() != parentInd.getFitness() ){ // some functionality, performance change
			// increase can be larger than 1
			tempData.increaseNormalisedProbabilitySmall( parentInd.getFitness() / this.getFitness() );
			tempData.getParentIndividualNodeData().increaseNormalisedProbabilitySmall(parentInd.getFitness() / this.getFitness());
		}else{ // no functionality, reduce
			// this probably doesnt get invoked too often. As it would be rare for parent fit = child fit.  
			// how bad was it broken? how much functionality did we lose?
			// decrease by 1 or less every time
			tempData.decreaseProbability(1-parentInd.getCorrectness());
			tempData.getParentIndividualNodeData().decreaseProbability(1-parentInd.getCorrectness());
		}
	}
	
	private void updateProbabilitiesRuleset26(Individual parentInd, GPASTNodeData tempData ){
		if(this.getRunningTime()== parentInd.getRunningTime() ){
			// intron!
			tempData.decreaseNormalisedProbability(1-parentInd.getCorrectness());
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1-parentInd.getCorrectness());
		}
		// whats the performance difference?
		long perfRatio = 0 ;
		if(this.getRunningTime() > parentInd.getRunningTime() )
			perfRatio = parentInd.getRunningTime()/this.getRunningTime();
		else 
			perfRatio = this.getRunningTime()/parentInd.getRunningTime();
			
		long funcRatio = 0 ;
		if(this.getFunctionalityErrorCount() > parentInd.getFunctionalityErrorCount() )
			funcRatio = parentInd.getFunctionalityErrorCount()/this.getFunctionalityErrorCount();
		else 
			funcRatio = this.getFunctionalityErrorCount()/parentInd.getFunctionalityErrorCount();
		
		if(perfRatio > funcRatio){ // changed performance more than functionality,
			// interesting, change there again in parent
			tempData.increaseNormalisedProbability(1-parentInd.getCorrectness());
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(1-parentInd.getCorrectness());
		}else{ // more destructive than performance affecting, don't change there again
			tempData.decreaseNormalisedProbability(1-parentInd.getCorrectness());
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1-parentInd.getCorrectness());
		}
			
		
		
		/*if( this.getFitness() != parentInd.getFitness() ){ // some functionality, performance change
			// increase can be larger than 1
			tempData.increaseProbability( parentInd.getFitness() / this.getFitness() );
			tempData.getParentIndividualNodeData().increaseProbability(parentInd.getFitness() / this.getFitness());
		}else{ // no functionality, reduce
			// this probably doesnt get invoked too often. As it would be rare for parent fit = child fit.  
			// how bad was it broken? how much functionality did we lose?
			// decrease by 1 or less every time
			tempData.decreaseProbability(1-parentInd.getCorrectness());
			tempData.getParentIndividualNodeData().decreaseProbability(1-parentInd.getCorrectness());
		}*/
		
	}
	
	private void updateProbabilitiesRuleset3(Individual parentInd, GPASTNodeData tempData ){
		// Ruleset #3 (Finding a "lever" on performance)
		if( this.getFitness() != parentInd.getFitness() ){ // some functionality, performance change
			// increase can be larger than 1
			tempData.increaseProbability( parentInd.getFitness() / this.getFitness() );
			tempData.getParentIndividualNodeData().increaseProbability(parentInd.getFitness() / this.getFitness());
		}else{ // no functionality, reduce
			// this probably doesnt get invoked too often. As it would be rare for parent fit = child fit.  
			// how bad was it broken? how much functionality did we lose?
			// decrease by 1 or less every time
			tempData.decreaseProbability(1-parentInd.getCorrectness());
			tempData.getParentIndividualNodeData().decreaseProbability(1-parentInd.getCorrectness());
		}
	}
	
	
	private void updateProbabilitiesRuleset4rr(Individual parentInd, GPASTNodeData tempData ){
		// bck 19-May-2013, reverting to checking functionality score, why was this removed?
		// Ruleset #4 (Finding a "lever" on performance without runaway bias updates)
		if(	 this.getFunctionalityErrorCount()< this.ourProblem.getWorstFunctionalityScore() 
				&& this.getFitness() != parentInd.getFitness() ){ // some functionality, performance change
			// increase can be larger than 1
			
			//float parentFit = parentInd.getFitness();
			//float thisFit = this.getFitness();
			float change = parentInd.getFitness() - this.getFitness() ;
			if (change < 0 )
				change = change *-1;
			tempData.increaseNormalisedProbability( change / (parentInd.getFitness()+this.getFitness()) );
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(change / (parentInd.getFitness()+this.getFitness()));
		}else{ // no functionality, reduce
			// how bad was it broken? how much functionality did we lose?
			
			
			tempData.decreaseNormalisedProbability(1-parentInd.getCorrectness());
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(1-parentInd.getCorrectness());
		}
	}
	private void updateProbabilitiesRuleset4(Individual parentInd, GPASTNodeData tempData ){
		
		// Ruleset #4 (Finding a "lever" on performance without runaway bias updates)
		if(	 this.getFitness() != parentInd.getFitness() ){ // some functionality, performance change
			// increase can be larger than 1
			
			//float parentFit = parentInd.getFitness();
			//float thisFit = this.getFitness();
			float change = parentInd.getFitness() - this.getFitness() ;
			if (change < 0 )
				change = change *-1;
			tempData.increaseNormalisedProbability( change / (parentInd.getFitness()+this.getFitness()) );
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(change / (parentInd.getFitness()+this.getFitness()));
		}else{ // no functionality, reduce
			// how bad was it broken? how much functionality did we lose?
			
			
			tempData.decreaseNormalisedProbability(1-parentInd.getCorrectness());
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(1-parentInd.getCorrectness());
		}
	}
	private void updateProbabilitiesRuleset5rr(Individual parentInd, GPASTNodeData tempData ){
		// bck 19-May-2013, reverting to checking functionality score, why was this removed?
		if(	this.getFunctionalityErrorCount()< this.ourProblem.getWorstFunctionalityScore() 
				&& this.getFitness() != parentInd.getFitness() ){ 
			/* some functionality, performance change
			 TODO how do we know if prefixCode does anything useful at all? Does it get anything right?
			*/
			float change = parentInd.getFitness() - this.getFitness() ;
			if (change < 0 )
				change = change *-1;
			/* we changed the program, and this changed the performance,
			 * something interesting here, don't change again in the offspring, change again in parent
			 */
			tempData.decreaseNormalisedProbability( change / (parentInd.getFitness()+this.getFitness()) );
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(change / (parentInd.getFitness()+this.getFitness()));
		}else{ // no functionality, reduce
			// we broke the program, probably not that interesting to change in the parent
			// the offspring is borked, so low chance of being picked, but if it is, change a diff location
			tempData.decreaseNormalisedProbability(1-parentInd.getCorrectness());
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1-parentInd.getCorrectness());
		}
	}
	
	private void updateProbabilitiesRuleset5(Individual parentInd, GPASTNodeData tempData ){
		if(	this.getFitness() != parentInd.getFitness() ){ 
			/* some functionality, performance change
			 TODO how do we know if prefixCode does anything useful at all? Does it get anything right?
			*/
			float change = parentInd.getFitness() - this.getFitness() ;
			if (change < 0 )
				change = change *-1;
			/* we changed the program, and this changed the performance,
			 * something interesting here, don't change again in the offspring, change again in parent
			 */
			tempData.decreaseNormalisedProbability( change / (parentInd.getFitness()+this.getFitness()) );
			tempData.getParentIndividualNodeData().increaseNormalisedProbability(change / (parentInd.getFitness()+this.getFitness()));
		}else{ // no functionality, reduce
			// we broke the program, probably not that interesting to change in the parent
			// the offspring is borked, so low chance of being picked, but if it is, change a diff location
			tempData.decreaseNormalisedProbability(1-parentInd.getCorrectness());
			tempData.getParentIndividualNodeData().decreaseNormalisedProbability(1-parentInd.getCorrectness());
		}
	}
	
	/*private void fillNulls() {
		GPASTNodeData curData;
		GPASTNodeData lastData = null;
		int count = 0;
		List<GPASTNodeData> propsList = this.getPropertiesList();
		

		for (Iterator<GPASTNodeData> propsListIter = propsList
				.iterator(); propsListIter.hasNext();) {
			curData = propsListIter.next();
			count ++;
			if (curData == null) {
				if (lastData == null) {// something really wrong if we get here
					propsList.set(count-1,  new GPASTNodeData());
					System.outugh ime
							.println("ERROR: first element is null in fillNulls");
				} else {
					propsList.set(count-1,  new GPASTNodeData(lastData.getModifyProbability()));
				}
			}
			lastData = propsList.get(count-1);
		}
	}*/

	public String getCodeProbabilitiesLogString() {
		String outString = " Probabilities for "+this.className;
			for(Iterator<ASTNode> iter = this.gpMaterial.getAllAllowedNodes().iterator(); iter.hasNext() ; ){
				ASTNode tempNode =  iter.next();
				GPASTNodeData tempData = ((GPASTNodeData) tempNode.getProperty("gpdata"));
				outString += "\n   - " + tempData.getProbabilityVal();
				outString += " : " + tempNode.toString(); 
			}
		return outString;
	}
	
	public String getCodeProbabilities() {
		String outString = ""; //" /* ";
		int nodeCount = 0 ;
			for(Iterator<ASTNode> iter = this.gpMaterial.getAllAllowedNodes().iterator(); iter.hasNext() ; ){
				ASTNode tempNode =  iter.next();
				GPASTNodeData tempData = ((GPASTNodeData) tempNode.getProperty("gpdata"));
				outString += "\n " + nodeCount + " : " +tempData.getProbabilityVal();
				outString += " : " + tempNode.toString().replaceAll("\n",  "").replaceAll("\r", "") ;
				nodeCount++;
			}
			outString += "\n";
		return outString;
	}



	@Override
	public int compareTo(Object arg0) {
		Individual ind = (Individual) arg0;
		if(ind == null)
			return 1;
		if( ind.getFitness() == this.getFitness())
			return 0; // same
		else if ((ind.getFitness() - this.getFitness()) > 0 )
			return -1; // after
		else
			return 1; // before (best first)
	}

	public int getLOCCount() {
		this.gpMaterial = new GPMaterialVisitor();
		this.ASTSet.accept(this.gpMaterial); 
		return this.gpMaterial.getStatements().size();
	}

	public void setChanged() {
		this.changed=true;
	}
	
	public void setUnChanged() {
		this.changed=false;
	}

	public boolean hasChanged() {
		return this.changed;
	}

	public void setStaticOptimalBias(int optimisationType) {
		ourProblem.setStaticOptimalBias(this, optimisationType);
	}

	public void clearRuntime() {
		ourProblem.setBaselineRuntimeAvg(0);
		this.runningTime = 0;
	}
	
	public Method getEntryMethod() {
		Method m = null;
		try {

			m = this.getEntryClazz().getMethod(ourProblem.getMethodName(),
					ourProblem.getMethodParameterTypes());
					//ourProblem.getClassParams());
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return m;
}	

	public void setClass(Class<?> clazz) {
		this.clazz = clazz;
	}

	public Class<?> getCompiledClass() {
		return this.clazz;
	}

	public Class<?> getEntryClazz() {
		return this.clazz;
}
	
	public void setClassMap(Map<String, JavaClassObject> classByteMap) {
		this.classMap = classByteMap;
	}
	
	public Map<String, JavaClassObject> getClassMap() {
		return this.classMap;
	}	
	
	public void setClassByteMap(Map<String, byte[]> classByteMap) {
		this.classByteMap = classByteMap;
	}
	
	public Map<String, byte[]> getClassByteMap() {
		return this.classByteMap;
	}



	public void setAllGPDataNodesToZero() {
		ASTNode tempNode;
		GPASTNodeData tempData =null;
		for (Iterator<ASTNode> iter = this.gpMaterial.getAllAllowedNodes()
				.iterator(); iter.hasNext();) {
			tempNode = iter.next();
			tempData = (GPASTNodeData)tempNode.getProperty("gpdata");
			if(tempData != null)
				tempData.setProbabilityVal(0);
			tempData = ((GPASTNodeData) tempNode.getParent().getProperty("gpdata"));
			if(tempData != null)
				tempData.setProbabilityVal(0);
		}
	}
	
	public void setAllGPDataNodesTo(float newVal) {
		ASTNode tempNode;
		GPASTNodeData tempData =null;
		for (Iterator<ASTNode> iter = this.gpMaterial.getAllAllowedNodes()
				.iterator(); iter.hasNext();) {
			tempNode = iter.next();
			tempData = (GPASTNodeData)tempNode.getProperty("gpdata");
			if(tempData != null)
				tempData.setProbabilityVal(newVal);
			tempData = ((GPASTNodeData) tempNode.getParent().getProperty("gpdata"));
			if(tempData != null)
				tempData.setProbabilityVal(newVal);
		}
	}

	public void getRefsToOriginalBiasObjects(Individual originalIndividual) {
		ASTNode tempNode, originalNode;
		GPASTNodeData tempData =null;
		for (int i = 0 ; i < this.gpMaterial.getAllAllowedNodes().size() ; i++) {
			tempData = (GPASTNodeData)originalIndividual.gpMaterial.getAllAllowedNodes().get(i).getProperty("gpdata");
			this.gpMaterial.getAllAllowedNodes().get(i).setProperty("gpdata", tempData);
		}
	}

	public void zeroResults() {
		setRunningTime(0);
		testCasesErrorResults = new SparseArrayList<TestCaseResult>();
	}

	public void setCompiled() {
		this.compiled = true;
		
	}

	public boolean compiled(){
		return compiled;
	}

	public String getTestCaseResultsText() {
		
		return testCaseResultText;
	}


	public synchronized void setTestResult(int testCaseIndex, int errorCount, boolean runtimeException) {
		testCaseResultText += "i"+testCaseIndex+":"+errorCount+"R"+runtimeException;
		this.testCasesErrorResults.add(testCaseIndex, new TestCaseResult(errorCount,runtimeException));
	}

	public void refreshGlobalGPMaterial(){ // this should be totally internal, and needs to happen every time the ast is changed.
		//this.ASTSet.refreshASTs();// //this.ASTSet.flattenASTChangesToString(); = NodeOperators.parseSource( this.ASTSet );
		this.gpMaterial = new GPMaterialVisitor();
		this.ASTSet.accept(this.gpMaterial);// gathers references to the parts of the AST we are interested in, adds gp data to statements
	}


	public void setClassLoader(ClassLoader cl) {
		this.classLoader = cl;
	}

	public Object execute(Object[] _args) throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		/*		for (Entry<String, byte[]> entry : this.getClassByteMap().entrySet())
				{
				    this.classLoader.defineClass(entry.getKey(), entry.getValue(), 0, entry.getValue().length);
				}
		*/
		
		Class<? extends Object> cl = this.classLoader.loadClass(this.getClassName());
		Method m = cl.getMethod(ourProblem.getMethodName(), ourProblem.getMethodParameterTypes()); //, methodCallParams);
		Object returnVals = m.invoke(null, _args);
		return returnVals;
	}

	public static long getIndIDGlobal() {
		return indIDGlobal;
	}

	public static void setIndIDGlobal(long indIDGlobal) {
		Individual.indIDGlobal = indIDGlobal;
	}

/*	public void initialise(CompilationSet initialCodeSet) {
		//NodeModifierUtil.GPPrimitives = new GPMaterialVisitor();
		this.gpMaterial = new GPMaterialVisitor();
		for(CompilationDetail cD : initialCodeSet.getCompilationList()){
			CompilationUnit indAST = NodeModifierUtil.parseSource(cD.getCodeString());
			indAST.accept(this.gpMaterial);
		}
		this.gpMaterial.printAll();
	}*/

	/*private void initialise(String initialCode) {
		CompilationUnit indAST = parseSource(initialCode);
		indAST.accept(GPPrimitives);
	}*/

	


/*	public void reduceBiasForInitialChangeAttempt() {
		
		//setGPDataNodesToZero();
		
			GPASTNodeData tempData = null;
			for (Iterator<ASTNode> iter = this.gpMaterial.getAllNodes().iterator(); iter
					.hasNext();) {
				tempData = (GPASTNodeData) iter.next().getProperty("gpdata");
				tempData.setNotChangedFlag();
				if(tempData.getParentIndividualNodeData()!=null && tempData.getParentIndividualNodeData().hasChanged()){
					//tempData.getParentIndividualNodeData().setProbabilityVal(0);
					if(tempData.getParentIndividualNodeData().getProbabilityVal() >=1 ){
						tempData.getParentIndividualNodeData().setProbabilityVal(.3);
					}
					tempData.getParentIndividualNodeData().setNotChangedFlag();
				}
			}
		
		
		//clearChangedFlags();
	}*/
}


	/*public byte[] getClassByteArray() {
		return this.classByteArray;
	}

	public void setClassLoader(GPSecureClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	public GPSecureClassLoader getClassLoader( ) {
		return this.classLoader ;
	}*/

	/*public void convertToStringOnlyASTs(){ // for serialisation purposes
    if(ASTSet !=null){
		  ASTString = AST.toString();
  		AST = null;
    }
		if(workingAST != null){
			CompilationUnitString = workingAST.toString();
			workingAST = null;
		}
		this.gpMaterial = null;
	}*/
	
	/*public void convertFromStringToASTs(){
		AST= NodeOperators.parseSource(this);
		if( CompilationUnitString != null && ! CompilationUnitString.equals(""))
			workingAST = NodeOperators.parseSource(CompilationUnitString);
		CompilationUnitString = null;
		refreshGPMaterial();
	}*/
	


