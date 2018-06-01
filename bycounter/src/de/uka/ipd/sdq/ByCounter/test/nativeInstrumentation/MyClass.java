package de.uka.ipd.sdq.ByCounter.test.nativeInstrumentation;

public class MyClass {
	public static void main(String[] args) {
		MyClass myClassA = new MyClass();
		System.out.println(myClassA.doSmthg());
		ExtendingMyClass myClassB = new ExtendingMyClass();
		MyClass myClassC = (MyClass) myClassB;
		System.out.println(myClassC.doSmthg());
	}
	public static boolean doSmthg() {
		return true;
	}
	
	public int nonStaticMethod(){
		return 0;
	}
//	static abstract boolean doSmthg2();

}
//public interface AbstractInterface {
//	
//	public abstract /*static*/ boolean doSmthg();
//	boolean  doSmthg2();
//
//}
