package experimental;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO document
 * 
 * @author Michael Kuperberg
 * @author Martin Krogmann
 * 
 * @since 0.1
 * @version 1.2
 */
public class PoorDynamicInvocationTargetIdentifier {
	public static void main(String[] args){
		PoorDynamicInvocationTargetIdentifier pditi = new PoorDynamicInvocationTargetIdentifier();
		pditi.test();
	}
	
	public void test(){
		List<String> l = null;
//		try{
//			System.out.print("l is un-initialised: ");
//			System.out.println(l.getClass());
//		}catch(NullPointerException npe){
//			System.out.println("NullPointerException while trying to get the class type of a \"null\" instance of an interface");
//		}
//		l = new AbstractList<String>();
//		System.out.println("l should be an AbstractList: "+l.getClass());
//		l.size(); //may be null
		l = new ArrayList<String>();
		l.size();
		Collections.sort(l);
		System.out.print(l.getClass());
		System.out.println(" is the class type of the object (should be an ArrayList!)");
		Exception keineAhnung = new Exception();
		try {
			throw keineAhnung;
		} catch (Exception e) {
			StackTraceElement[] elts = keineAhnung.getStackTrace();
			System.out.println(""+elts[1].getMethodName());
//			e.printStackTrace();
		}
//		l = new LinkedList<String>(); 
//		System.out.println("l should be an LinkedList: "+l.getClass());
//		l = new Vector<String>();
//		System.out.println("l should be an Vector: "+l.getClass());
	}
}
