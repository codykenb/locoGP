package de.uka.ipd.sdq.ByCounter.test.helpers;

public class ClassSuper {

	public int methodOnlyImplementedInSuper() {
		for(int i = 0; i < 20; i++) {
			if(i > 9) {
				return i;
			}
		}
		return 0;
	}
}
