public class OriginalBubble {
  public static void sort(  Integer[] a,  Integer length){
    for (int i=0; i < length; length--) {
      for (int j=0; j < length - 1; j++) {
        if (a[j] > a[j + 1]) {
          int k=a[j];
          a[j]=a[j + 1];
          a[j + 1]=k;
        }
      }
    }
  }
}
