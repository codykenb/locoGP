package locoGP.operators;

import java.util.logging.Logger;

import org.eclipse.jdt.core.dom.ASTNode;

public class GPASTNodeData {
	/*
	 * This class is an object that is attached to each modifiable node in the AST
	 * It is used to store the probability of modification for each node
	 */
	
	
	// TODO implement a clone function 
	private static double initialProbabilityVal = 0.5;
	private double probabilityVal;
	private boolean changed = false;
	private boolean executationCostDecreaseObserved = false;
	private GPASTNodeData parentNodeData = null;
	private double changeMagnitudeDiminisher = 1.0;
	private double performanceReductionCount = 0 ;
	private double compileCount = 0 ; 
	
	public boolean hasChanged(){
		return changed;
	}
	
	public void setChangedFlag(){
		changed = true;
	}
	
	public void setNotChangedFlag(){
		changed = false;
	}
	
	public void setProbabilityVal(double probVal){
		this.probabilityVal = probVal;
	}
	
	public double getProbabilityVal(){
		/*if(performanceReductionCount > 0 && compileCount >0 )
			// this is intended to override the value set by deletion analysis
			return performanceReductionCount / compileCount;
		else*/
			return this.probabilityVal;
	}
	
	public static void setGPASTNodeInitialBias(int i ){
		GPASTNodeData.initialProbabilityVal = i;
	}
	
	public GPASTNodeData() { 
		probabilityVal = GPASTNodeData.initialProbabilityVal;
	}

	public GPASTNodeData(GPASTNodeData originalNodeData) { 
		setProbabilityVal(originalNodeData.getProbabilityVal());
	}
	public GPASTNodeData(ASTNode originalNode) {
		this(((GPASTNodeData) originalNode.getProperty("gpdata")));
	}

	/*public double getModifyProbability(){
		return probabilityVal ;
	}	
	
	public void setModifyProbability(double probVal){
			probabilityVal = probVal;
	}	*/
	
	public void changeProbability(double changeVal){ // amount to add on to the probability value
		this.probabilityVal +=changeVal;
	}
	
	/*public void checkNormal(double magnitude, String action){
		System.out.println(action + " update "+this.probabilityVal+" by magnitude "+ magnitude);
		if(this.probabilityVal >1 )
			System.out.println("Normalised Probability larger than 1!");
		else if(this.probabilityVal == 1)
			this.probabilityVal=0.9; 
	}*/
	
	public void increaseNormalisedProbabilityDiminished(double magnitude){
		executationCostDecreaseObserved = true;
		increaseNormalisedProbability( magnitude * this.changeMagnitudeDiminisher );
		this.changeMagnitudeDiminisher=this.changeMagnitudeDiminisher/2;
	}
	
	public void increaseNormalisedProbability(double magnitude){ // increase probability by the magnitude of the performance change observed. 
		// magnitude should be given a number between 0 and 1
		// we should have a number between 0 and 1 inclusive
		if(magnitude>1)
			magnitude = .9;
		
		//checkNormal(magnitude, "Increase");
		this.probabilityVal += ((1-this.probabilityVal)/2)*magnitude;
		//checkNormal(magnitude, "After increase");
	}
	
	public void increaseNormalisedProbabilityTiny(double magnitude){ // increase probability by the magnitude of the performance change observed. 
		if(magnitude>1)
			magnitude = .9;
		//checkNormal(magnitude, "Increase");
		this.probabilityVal += ((1-this.probabilityVal)/15)*magnitude;
		//checkNormal(magnitude, "After increase");
	}
	
	public void increaseNormalisedProbabilitySmallish(double magnitude){ // increase probability by the magnitude of the performance change observed. 
		// magnitude should be given a number between 0 and 1
		// we should have a number between 0 and 1 inclusive
		if(magnitude>1)
			magnitude = .9;
		//checkNormal(magnitude, "Increase");
		this.probabilityVal += ((1-this.probabilityVal)/5)*magnitude;
		//checkNormal(magnitude, "After increase");
	}
	
	public void increaseNormalisedProbabilitySmall(double magnitude){ // increase probability by the magnitude of the performance change observed. 
		// magnitude should be given a number between 0 and 1
		// we should have a number between 0 and 1 inclusive
		if(magnitude>1)
			magnitude = .9;
		//checkNormal(magnitude, "Increase");
		this.probabilityVal += ((1-this.probabilityVal)/8)*magnitude;
		//checkNormal(magnitude, "After increase");
	}
	
	public void decreaseNormalisedProbability(double magnitude){ // decrease probability, how?
		if(magnitude>1)
			magnitude = .9;
		//checkNormal(magnitude, "Decrease");
		if (! executationCostDecreaseObserved )
			this.probabilityVal -= ((this.probabilityVal)/2)*magnitude; // slower decay than increase
		//checkNormal(magnitude, "After decrease");
	}
	

	
	public void increaseProbability(float magnitude){ // increase probability by the magnitude of the performance change observed. 
		this.probabilityVal += magnitude; // big jump
	}
	
	public void decreaseProbability(float magnitude){ // decrease probability, how? 
		if (! executationCostDecreaseObserved )
			this.probabilityVal -=  magnitude; // slower decay than increase
	}

	public void setParentIndividualNodeData(
			GPASTNodeData individualParentNodeData) {
		// This should be a node in the the parent tree in the GP model (not "parent" in a single tree per the jdt model)
		this.parentNodeData = individualParentNodeData;		
	}
	
	public GPASTNodeData getParentIndividualNodeData(){
		return this.parentNodeData; 
	}

	public void biasHigh() {
		this.probabilityVal = .9;
	}

	public void incrementPerfRedCount() {
		// TODO Auto-generated method stub
		this.performanceReductionCount++;
	}

	public void incrementCompileCount() {
		this.compileCount++;
	}
}
