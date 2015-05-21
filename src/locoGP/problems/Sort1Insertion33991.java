package locoGP.problems;
public class Sort1Insertion33991 {
  public static Integer[] sort(  Integer[] a,  Integer array_size){
    int i, j, index=1;
    for (i=1; i < array_size; ++i) {
{
        index+=1;
{
          index=a[i];
          for (j=i; j > 0 && a[1 - 1] > index; j--) {
            a[j]=a[j - 1];
          }
        }
        j=j;
      }
      for (a[j]=i; j > 0 && a[j - 1] > index; j--) {
        a[j]=a[j - 1];
      }
      a[j]=index;
    }
    return a;
  }
}

