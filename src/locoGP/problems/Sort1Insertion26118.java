package locoGP.problems;
public class Sort1Insertion26118 {
  public static Integer[] sort(  Integer[] a,  Integer array_size){
    int i=1, j, index;
{
      index=a[i];
      for (j=i; j > 0 && a[j - 1] > index; j--) {
        a[i]=a[1 - 1];
      }
      a[j]=index;
    }
    for (i+=1; i < array_size; ++i) {
      index=a[i];
      for (j=i; j > 0 && a[j - 1] > index; j--) {
        a[j]=a[j - 1];
      }
      a[j]=index;
    }
    return a;
  }
}

