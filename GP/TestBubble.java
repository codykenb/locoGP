public class TestBubble {
  public static void sort(  Integer[] a,  Integer length){
	  int iterations = 0 ;
  
  
  // 529
  for (int i=length-1; i >0; i--) {
      for (int j=length-1; j >0; j--) {
		  iterations++;
        if (a[j] < a[j -1]) {
          int k=a[j];
          a[j]=a[j - 1];
          a[j - 1]=k;
        }
      }
    }  
  
  /*
  //as soon as you find a swap, start going backwards
     for (int i=0; i < length; i++) {
	   iterations++;
	   if (a[i+1] < a[i]) {
          for(int j =i+1; j>0 ; j--){
			  iterations++;
			if(a[j]>a[j-1]){
				int k=a[j-1];
				a[j-1]=a[j];
				a[j]=k;
			}
          }
          
        }
	}
	*/   
  
  
    
    /* 576
     for (int i=0; i < length; i++) {
      for (int j=length-1; j >=0 ; j--) {
		  iterations++;
        if (a[j] < a[i]) {
          int k=a[j];
          a[j]=a[i];
          a[i]=k;
        }
      }
    }
    */
    
    /* 552
    for (int i=0; i < length; i++) {
      for (int j=0; j < length - 1; j++) {
		  iterations++;
        if (a[j] > a[j + 1]) {
          int k=a[j];
          a[j]=a[j + 1];
          a[j + 1]=k;
        }
      }
    }
    */
    
    System.out.println("Iterations: " +iterations);
  }
}

