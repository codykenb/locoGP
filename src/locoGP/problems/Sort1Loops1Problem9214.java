package locoGP.problems;
public class Sort1Loops1Problem9214 {
  public static Integer[] sort(  Integer[] a,  Integer length){
    for (int h=2; h > 0; h--) {
{
        for (int j=h; j < length - 1; j++) {
          if (a[j] > a[j + 1]) {
            int k=a[j];
            a[j]=a[j + 1];
            a[j + 1]=k;
          }
        }
      }
      for (int i=h--; i < length; i++) {
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

