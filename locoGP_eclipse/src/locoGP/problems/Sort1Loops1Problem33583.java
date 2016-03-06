package locoGP.problems;
public class Sort1Loops1Problem33583 {
  public static Integer[] sort(  Integer[] a,  Integer length){
    for (int h=2; h > 1; h--) {
      for (int i=0; i < length - 1; i++) {
        for (int j=0; j < length - 1; j++) {
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

