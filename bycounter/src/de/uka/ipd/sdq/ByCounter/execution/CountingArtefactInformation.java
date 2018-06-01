package de.uka.ipd.sdq.ByCounter.execution;

import java.util.List;

import de.uka.ipd.sdq.ByCounter.results.CountingResult;


/**
 * A small class to describe results of a counting step.
 * 
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
public final class CountingArtefactInformation 
implements Comparable<CountingArtefactInformation>{
	
	/**
	 * Instance of the corresponding {@link CountingResultIndexing}.
	 */
	private CountingResultIndexing myIndexing;
	
	/**
	 * A simple getter 
	 * @return the underlying instance of {@link CountingResultIndexing}.
	 */
	public CountingResultIndexing getMyIndexing() {
		return myIndexing;
	}
	
	/**
	 * Input parameters used for counting
	 */
	private List<Object[]> inputPrms;
	
	/**
	 * TODO {@link CountingArtefactInformation} always corresponds to an instrumented class/method, or not?
	 */
	private boolean isInstrumented;
	
    /**
     * Flag to describe whether log was written directly to disk ("true") 
     * or saved by {@link CountingResultCollector} ("false") 
     */
    private boolean logWrittenToDisk;
    
    /**
     * TODO instrumented method? invoked (executed) method?
     */
    private String methodName;
    
    /**
     * TODO what if several methods executed? 
     * or, then, one artefact information per executed method? 
     */
    private List<Object[]> outputPrms;
    
    /**
     * The timestamp (from System.nanoTime()) when the method 
     * invocation started.
     */
    private Long time_InvocationReceived;
    
    /**
     * The timestamp (from System.nanoTime()) when the results 
     * were received by {@link CountingResultCollector}
     */
    private Long time_resultsReceivedByCollector;
    
    /**
     * The default constructor.
     * @param indexing The corresponding indexing infrastructure.
     */
    public CountingArtefactInformation(CountingResultIndexing indexing) {
		myIndexing = indexing;
	}
    
    /** Parametrised constructor
     * @param methodName reporting method (thus, it is an instrumented one)
     * @param invocationReceivedTime the timestamp when the 
     * instrumented method detected that it is being invoked 
     * @param inputPrms input parameters of the instrumented method
     * @param resultsReceivedByCollectorTime the timestamp 
     * when {@link CountingResultCollector}
     * received the information
     * @param outputPrms output parameters of the method
     */
    public CountingArtefactInformation(
    		CountingResultIndexing indexing,
			String methodName,
			Long invocationReceivedTime,
			List<Object[]> inputPrms,
			Long resultsReceivedByCollectorTime,
			List<Object[]> outputPrms){
		this(indexing);
//		this.exitingTime = null;//field removed
		this.inputPrms = inputPrms;
		//TODO isInstrumented is missing
		//TODO logWrittenToDisk is missing
		this.methodName = methodName;
		this.outputPrms = outputPrms;
		this.time_InvocationReceived = invocationReceivedTime;
		this.time_resultsReceivedByCollector = resultsReceivedByCollectorTime;
	}

	/** (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(CountingArtefactInformation/*Object*/ o) {
//		if(!(o instanceof CountingArtefactInformation)){
//			return -1;
//		}
		CountingArtefactInformation cai = (CountingArtefactInformation) o;//TODO fixme - heritage...
		if(this.getTime_InvocationReceived()<cai.getTime_InvocationReceived()){
			return -1;
		}else if(this.getTime_InvocationReceived()>cai.getTime_InvocationReceived()){
			return +1;
		}else{//same invocation time
			if(this.getTime_resultsReceivedByCollector()<cai.getTime_resultsReceivedByCollector()){
				return -1;
			}else if(this.getTime_resultsReceivedByCollector()>cai.getTime_resultsReceivedByCollector()){
				return +1;
			}else{
				return this.getMethodName().compareTo(cai.getMethodName());
			}
		}
	}

//	public Long getExitingTime() {
//		return exitingTime;
//	}

	/** Delegating getter for counting results that correspond to this artefact
	 * @return counting results that correspond to this artefact
	 */
	public CountingResult getCountingResult() {
		return myIndexing.getAllCountingResultsByArtefacts().get(this);
	}

	/**Simple getter
	 * @return inputPrms
	 */
	public List<Object[]> getInputPrms() {
		return this.inputPrms;
	}

	/**Simple getter
	 * @return time_InvocationReceived
	 */
	public Long getInvocationReceivedTime() {
		return this.time_InvocationReceived;
	}

	/**Simple getter
	 * @return methodName
	 */
	public String getMethodName() {
		return this.methodName;
	}

	/**Simple getter
	 * @return outputPrms
	 */
	public List<Object[]> getOutputPrms() {
		return this.outputPrms;
	}

	/**Simple getter
	 * @return time_resultsReceivedByCollector
	 */
	public Long getResultsReceivedByCollectorTime() {
		return this.time_resultsReceivedByCollector;
	}
	
	/**Simple getter
	 * @return time_InvocationReceived
	 */
	public Long getTime_InvocationReceived() {
		return this.time_InvocationReceived;
	}

	/**Simple getter
	 * @return time_resultsReceivedByCollector
	 */
	public Long getTime_resultsReceivedByCollector() {
		return this.time_resultsReceivedByCollector;
	}

	/**Simple getter
	 * @return isInstrumented
	 */
	public boolean isInstrumented() {
		return this.isInstrumented;
	}

	/**Simple getter
	 * @return logWrittenToDisk
	 */
	public boolean isLogWrittenToDisk() {
		return this.logWrittenToDisk;
	}

	/** Simple setter
	 * @param inputPrms to be set
	 */
	public void setInputPrms(List<Object[]> inputPrms) {
		this.inputPrms = inputPrms;
	}

	/** Simple setter
	 * @param isInstrumented to be set
	 */
	public void setInstrumented(boolean isInstrumented) {
		this.isInstrumented = isInstrumented;
	}

	/** Simple setter
	 * @param logWrittenToDisk to be set
	 */
	public void setLogWrittenToDisk(boolean logWrittenToDisk) {
		this.logWrittenToDisk = logWrittenToDisk;
	}

	/** Simple setter
	 * @param methodName to be set
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/** Simple setter
	 * @param outputPrms to be set
	 */
	public void setOutputPrms(List<Object[]> outputPrms) {
		this.outputPrms = outputPrms;
	}

	/** Simple setter
	 * @param time_InvocationReceived to be set
	 */
	public void setTime_InvocationReceived(Long time_InvocationReceived) {
		this.time_InvocationReceived = time_InvocationReceived;
	}

	/** Simple setter
	 * @param time_resultsReceivedByCollector to be set
	 */
	public void setTime_resultsReceivedByCollector(
			Long time_resultsReceivedByCollector) {
		this.time_resultsReceivedByCollector = time_resultsReceivedByCollector;
	}

	/** Returns a String representation of this class
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		@SuppressWarnings("unused")
		String inputPrmsSize = "none";
		if(this.inputPrms!=null){
			inputPrmsSize = ""+this.inputPrms.size();
		}
		@SuppressWarnings("unused")
		String outputPrmsSize = "none";
		if(this.outputPrms!=null){
			outputPrmsSize = ""+this.outputPrms.size();
		}
		sb.append("\n"+
				  "      "+this.getClass().getSimpleName()+" (hash code: "+this.hashCode()+")\n");
		sb.append("      > methodName: "+this.methodName+"\n");
		sb.append("      > invocationReceivedTime: "+this.time_InvocationReceived+"\n");
		sb.append("      > resultsReceivedByCollectorTime: "+this.time_resultsReceivedByCollector+"\n");
//		sb.append("      > input prms ("+inputPrmsSize+"): "+inputPrms+"\n");
//		sb.append("      > output prms ("+outputPrmsSize+"): "+outputPrms+"\n");
//		sb.append("      > isInstrumented: "+isInstrumented+"\n"); //TODO pre-init wrongful
//		sb.append("      > logWrittenToDisk: "+logWrittenToDisk+"\n"); //TODO pre-init wrongful
	    return sb.toString();
	}

}
