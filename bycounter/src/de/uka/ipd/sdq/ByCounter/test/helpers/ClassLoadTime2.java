package de.uka.ipd.sdq.ByCounter.test.helpers;

public class ClassLoadTime2 {
	public static void main(String[] args) {
		final String myClassName = ClassLoadTime2.class.getCanonicalName();
		final String loadClassName = myClassName.substring(0, myClassName.length()-1);
		ClassLoader classLoader = ClassLoadTime2.class.getClassLoader();
		long beforeLoad, afterLoad;
		System.nanoTime();
		try {
			beforeLoad = System.nanoTime();
			classLoader.loadClass(loadClassName);
			afterLoad = System.nanoTime();
			System.out.println("Loaded in " + (afterLoad-beforeLoad) + " ns");
		} catch (Exception e) {
			e.printStackTrace();
		};
	}

}
