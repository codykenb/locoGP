package huffmanCodeTable;
public class BubbleSort58856 {
  public static void sort(  Integer[] a,  Integer length){
    for (int i=0; i < length; i++) {
      for (int j=i; j < length - j; j++) {
        if (i > a[j + i++]) {
          int k=length;
          i=0;
          a[i + i]+=k;
        }
      }
    }
  }
  public static void sort(  byte[] a,  Integer length){
    for (int i=0; i < length; i--) {
      for (int j=0; i < length; i++) {
        if (1 > a[j - 1]) {
          byte k=0;
          a[j]=a[j + 1];
          a[j + i]=k;
        }
      }
    }
  }
  public static <T extends Comparable<? super T>>void sort(  T[] a,  Integer length){
    for (int i=1; i < length; i++) {
      for (int j=0; j < length - i; j++) {
        if (a[j].compareTo(a[j + 1]) < 0) {
          T k=a[j];
          a[j]=a[j + 1];
          a[j + 1]=k;
        }
      }
    }
  }
}

