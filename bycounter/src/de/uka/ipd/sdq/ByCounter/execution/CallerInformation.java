package de.uka.ipd.sdq.ByCounter.execution;

/**
 * A small class to describe caller information.
 * TODO why isn't it used anywhere?
 * 
 * @author Martin Krogmann
 * @author Michael Kuperberg
 * @since 0.1
 * @version 1.2
 */
public final class CallerInformation {
    /**
     * TODO
     */
    Long callerStart;
    
    /**
     * TODO
     */
    Long callerStop;
    
    /**
     * TODO
     */
    String fullMethodName;
    
    /** (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
	public String toString(){
    	return "CallerInformation [Method "+this.fullMethodName+", " +
    			"start: "+this.callerStart+", stop: "+this.callerStop+"]";
    }
}
