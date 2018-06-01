package de.uka.ipd.sdq.ByCounter.test.nativeInstrumentation;

public class Caller {
	private ITime timer;
	
	public void methToInstr() {
		timer = new TimeNanoTime();
		System.out.println("nanoTime/(1 000 000): " + timer.time());
		timer = new TimeLinuxTSC();
		System.out.println("tsc: " + timer.time());
	}
	
	public static void main(String[] args) {
		Caller c = new Caller();
		c.methToInstr();
	}

}
