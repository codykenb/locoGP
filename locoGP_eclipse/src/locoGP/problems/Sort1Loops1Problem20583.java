package locoGP.problems;
public class Sort1Loops1Problem20583 {
  public static Integer[] sort(  Integer[] a,  Integer length){
    for (int h=1; h > 0; h--) {
      for (int i=h; i < length; i++) {
        for (int j=0; j < length - i; j++) {
          if (a[j] > a[j + 1]) {
            int k=a[j];
            a[j]=a[j + 1];
            a[j + 1]=k;
          }
        }
      }
    }
    return a;
  }
}

