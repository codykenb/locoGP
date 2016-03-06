package huffmanCodeTable;
//package huffmanCodeTable;
public class BubbleSort {
  public static void sort(  Integer[] a,  Integer length){
    for (int i=0; i < length; i++) {
      for (int j=0; j < length - 1; j++) {
        if (a[j] > a[j + 1]) {
          int k=a[j];
          a[j]=a[j + 1];
          a[j + 1]=k;
        }
      }
    }
  }
  
  public static void sort(  byte[] a,  Integer length){
	    for (int i=0; i < length; i++) {
	      for (int j=0; j < length - 1; j++) {
	        if (a[j] > a[j + 1]) {
	          byte k=a[j];
	          a[j]=a[j + 1];
	          a[j + 1]=k;
	        }
	      }
	    }
	  }
  
/*  public static <T extends Comparable<? super T>> void sort(T[] a, Integer length){
	  System.out.println("line starting sort!");
	  System.out.println("line 1");
	  //for (int h = (int)(0.1*length); h > 0; h--) {
		  System.out.println("line 2");
	  for (int i=0; i < length; i++) {
		  System.out.println("line 3");
	      for (int j=0; j < length - 1; j++) {
	    	  System.out.println("line 4");
	        if (a[j].compareTo(a[j + 1])<0) {
	        	System.out.println("line 5");
	          T k=a[j];
	          System.out.println("line 6");
	          a[j]=a[j + 1];
	          System.out.println("line 7");
	          a[j + 1]=k;
	          System.out.println("line 8");
	        }
	        System.out.println("line 9");
	      }
	      System.out.println("line 10");
	    }
	  System.out.println("line 11");
	  //}
	  System.out.println("Finished sort!");
  }*/
  
  /*
   * 	  public static Integer[] sort(Integer []a , Integer length) { 
for (int h = (int)(0.1*length); h > 0; h--) {
for (int i = 0 ; i < length; i++) {
for (int j = 0; j < length - 1; j++) {
		if (a[j] > a[j + 1]) {
			int k = a[j];
			a[j] = a[j + 1];
			a[j + 1] = k;
		}
	}
}
}
	return a; 
	} 
}
   */
  
  
  public static <T extends Comparable<? super T>> void sort(T[] a, Integer length){
	  for (int i=0; i < length; i++) {
	      for (int j=0; j < length - 1; j++) {
	        if (a[j].compareTo(a[j + 1])<0) {
	          T k=a[j];
	          a[j]=a[j + 1];
	          a[j + 1]=k; 
	        } 
	      }
	    }
  }
  
}
