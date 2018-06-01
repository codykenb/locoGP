package de.uka.ipd.sdq.ByCounter.test.helpers;

public class TestSubjectInterfaceMethods {
	
	InterfaceX instanceX;
	
	public TestSubjectInterfaceMethods() {
		instanceX = new ClassY();
	}
	
	public void methodA1() {
		instanceX.methodX1();
		instanceX = new ClassZ();
		instanceX.methodX1();
	}
	
	public static void main(String[] args) {
		TestSubjectInterfaceMethods subject = new TestSubjectInterfaceMethods();
		subject.methodA1();
	}
}

