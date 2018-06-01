package de.uka.ipd.sdq.ByCounter.test.helpers;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import de.uka.ipd.sdq.ByCounter.execution.CollectionStrategyDefault;
import de.uka.ipd.sdq.ByCounter.execution.CountingArtefactInformation;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector;
import de.uka.ipd.sdq.ByCounter.execution.IFullCountingResult;
import de.uka.ipd.sdq.ByCounter.execution.ProtocolCountStructure;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;

/**
 * This class is not meant for standard ByCounter instrumentation - 
 * instead, it supplies its own "artificial" counts for testing...
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
class DummyReportingClass {
	/**TODO
	 * @param args
	 */
	public static void main(String[] args){
    	DummyReportingClass drc = new DummyReportingClass();
    	drc.test();
    }
    
    /**
	 * see http://en.wikipedia.org/wiki/Data_log
	 */
	private Logger log;
    
	/**
	 * TODO
	 */
	CollectionStrategyDefault myCollectionStrategy;

	/** 
     * TODO
     */
    public DummyReportingClass(){
		this.log = Logger.getLogger(this.getClass().getCanonicalName());
        this.myCollectionStrategy = new CollectionStrategyDefault(CountingResultCollector.getInstance());
    }
     
    /**TODO
     * @param prm
     * @return
     */
    public double anotherPseudoInstrumentedMethod(double prm){
        this.log.fine("input prm: "+prm);
        long start = System.nanoTime();
        double ret = Math.sqrt(prm);
        int[] opcodeCounts = new int[200];
        opcodeCounts[2]=3;
        opcodeCounts[3]=4;
//        int[] methodCallCounts = new int[1];
//        methodCallCounts[0]=2;
//        String[] calledMethods = new String[1];
//        calledMethods[0] ="de.uka.ipd.sdq.ByCounter.execution.anotherPseudoInstrumentedMethod"; 
		 
        ProtocolCountStructure result = new ProtocolCountStructure();
        result.executionStart = start;
        result.qualifyingMethodName = "de.uka.ipd.sdq.ByCounter.execution.anotherPseudoInstrumentedMethod";
        result.opcodeCountsInt = opcodeCounts;
        result.methodCallCountsInt = new int[0];
        result.reportingStart = System.nanoTime();
        this.myCollectionStrategy.protocolCount(result);
        this.log.fine("returning: "+ret);
        return ret;
    }

	/**TODO
	 * @param prm
	 * @return
	 */
	public double pseudoInstrumentedMethod(double prm){
        this.log.fine("input prm: "+prm);
        long start = System.nanoTime();
        double value = prm*prm;
        value = value*value;
        double ret = anotherPseudoInstrumentedMethod(value);
        ret = anotherPseudoInstrumentedMethod(ret);
        if(ret==value){
//        	log.fine("really equals...");
        }else{
        	this.log.fine("compared primitive double numbers are not \"=-equal\", " +
        			"squared difference of "+prm+" and "+ret+": "+
        			(prm-ret)*(prm-ret));
        }
        int[] opcodeCounts = new int[200];
        opcodeCounts[0]=1;
        opcodeCounts[1]=2;
        int[] methodCallCounts = new int[1];
        methodCallCounts[0]=2;
        String[] calledMethods = new String[1];
        calledMethods[0] ="de.uka.ipd.sdq.ByCounter.execution.anotherPseudoInstrumentedMethod"; 
		 
        ProtocolCountStructure result = new ProtocolCountStructure();
        result.executionStart = start;
        result.qualifyingMethodName = "de.uka.ipd.sdq.ByCounter.execution.pseudoInstrumentedMethod";
        result.opcodeCountsInt = opcodeCounts;
        result.methodCallCountsInt = methodCallCounts;
        result.calledMethods = calledMethods;
        result.reportingStart = System.nanoTime();
        this.myCollectionStrategy.protocolCount(result);
        this.log.fine("returning: "+ret);
        return ret;
    }

    /**
     * TODO
     */
    private void test() {
		this.pseudoInstrumentedMethod(10.0D);
		
		@SuppressWarnings("unused")
		List<CountingArtefactInformation> artefactsCallee;
		List<CountingArtefactInformation> artefactsCaller;
		
		Collection<List<CountingArtefactInformation>> artefacts;
		CountingArtefactInformation countingArtefact; 
		artefacts = this.myCollectionStrategy.getCountingResultIndexing().getCountingArtefactsByBeginning().values();
		for(List<CountingArtefactInformation> list : artefacts) {
			for (Iterator<CountingArtefactInformation> iterator = list.iterator(); 
					iterator.hasNext();
					) {
				countingArtefact = iterator.next();
				this.log.fine(countingArtefact.toString());
			}
		}
		
//		this.myCollector.getCountingInformationsByMethodname();
		Collection<CountingResult> results;
		IFullCountingResult countingResult; 
		results = this.myCollectionStrategy.getCountingResultIndexing().getAllCountingResultsByArtefacts().values();
		this.log.fine("listing all available counting results");
		for (Iterator<CountingResult> iterator = results.iterator(); 
				iterator.hasNext();
				) {
			countingResult = iterator.next();
			this.log.fine(countingResult.toString());
		}
		

    	artefactsCaller = this.myCollectionStrategy.getCountingResultIndexing().getCountingArtefactsByName("de.uka.ipd.sdq.ByCounter.execution.pseudoInstrumentedMethod");
//    	log.fine("Size of caller artefacts list: "+artefactsCaller.size());
//    	log.fine("First caller artefacts in list: "+artefactsCaller.get(0));
//
		artefactsCallee = this.myCollectionStrategy.getCountingResultIndexing().getCountingArtefactsByName("de.uka.ipd.sdq.ByCounter.execution.anotherPseudoInstrumentedMethod");
//    	log.fine("Size of callee artefacts list: "+artefactsCallee.size());
//    	log.fine("First callee artefacts in list: "+artefactsCallee.get(0));
//		
//		CountingResult callerCounts = this.myCollector.getCounts(artefactsCaller.get(0));
//    	log.fine("First caller counting result in list: "+callerCounts);
//
//    	CountingResult calleeCounts = this.myCollector.getCounts(artefactsCallee.get(0));
//    	log.fine("First callee counting result in list: "+calleeCounts);
		System.out.println("================================================================");
		System.out.println("================================================================");
		System.out.println("================================================================");
		IFullCountingResult callerCounts_evaluated; 
		long callerTime = artefactsCaller.get(0).getInvocationReceivedTime();
//    	log.fine("First caller artefacts time: "+callerTime);
		callerCounts_evaluated = this.myCollectionStrategy.getCountingResultIndexing().retrieveCountingResultByStartTime_evaluateCallingTree(callerTime, false);
		this.log.fine("First caller counting result in list, fully evaluated: "+callerCounts_evaluated);
	}
}
