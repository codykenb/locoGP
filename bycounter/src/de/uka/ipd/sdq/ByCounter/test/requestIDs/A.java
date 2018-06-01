package de.uka.ipd.sdq.ByCounter.test.requestIDs;

import java.util.Random;
import java.util.UUID;


public class A {
	
	static {
		System.out.println("Class statically initialised.");
	}
	public A() {
		this(2);
		System.out.println("");		
	}
	
	public A(int param) {
		System.out.println("");
	}
	
	public void methodA(int reqID) {
		methodB(reqID);
		System.out.println("methodA");
	}

	public void methodB(int reqID) {
		System.out.println("methodB");
		doSth();
		doSthElse();
		doSthDifferent((short) 5);
	}
	
	public String doSth() {
		return "";
	}

	public char[] doSthElse() {
		return new char[0];
	}
	
	public static boolean doSthDifferent(short s) {
		return s > 10 ? true: false;
	}
	
	public static byte doSthStatic() {
		int i = 3;
		i++;
		return (byte) (i%8);
	}

	public boolean parameterTest(int i, float f, String s) {
		int MK1 = 10;
		int MK2 = 10;
		int MK3 = 10;
		int MK4 = 10;
		int MK5 = 10;
		
		f *= 20 + i;
//		log.info("From parameterTest(): " + s);
		return true;
	}
	
	
	public static void main(String[] args) {
		Random r = (new Random());
		ReqRunnable r1 = new ReqRunnable(r.nextInt());
		ReqRunnable r2 = new ReqRunnable(r.nextInt());
		Thread t1 = new Thread(r1);
		Thread t2 = new Thread(r2);
		t1.start();	// calls methodA{methodB{doSth;doSthElse;doSthDifferent}}
		t2.start(); // calls methodA{methodB{doSth;doSthElse;doSthDifferent}}
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
//	
	public void asdf(long[] a, boolean b) {
		this.methodX(a, b, UUID.randomUUID());
	}
//	
//
	public void methodX(long[] ba, boolean b, UUID reqID) {
//		methodB(reqID);
		System.out.println("methodX");
	}
}
