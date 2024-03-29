package huffmanCodeTable;
public class BubbleSort5223test {
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
  public static <T extends Comparable<? super T>>void sort(  T[] a,  Integer length){
    for (int i=0; i < length; i++) {
      for (int j=0; j < length - 1; j++) {
        if (a[j].compareTo(a[j + 1]) < 0) {
          T k=a[j];
          a[j]=a[j + 1];
          a[j + 1]=k;
        }
      }
    }
  }
}
