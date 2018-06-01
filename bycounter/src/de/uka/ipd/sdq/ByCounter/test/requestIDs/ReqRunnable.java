package de.uka.ipd.sdq.ByCounter.test.requestIDs;

public class ReqRunnable implements Runnable {
	private int reqID;

	public ReqRunnable(int i) {
		this.reqID = i;
	}
	
	public void run() {
		A a = new A(2);
		a.methodA(this.reqID);
	}
};
