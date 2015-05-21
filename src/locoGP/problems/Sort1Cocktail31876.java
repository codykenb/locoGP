package locoGP.problems;
public class Sort1Cocktail31876 {
  public static Integer[] sort(  Integer[] a,  Integer length){
    boolean swapped;
    do {
      swapped=false;
{
        swapped=false;
        for (int i=0; i <= length - 2; i++) {
          if (a[i] > a[i + 1]) {
            int temp=a[i];
            a[i]=a[i + 1];
            a[i + 1]=temp;
            swapped=true;
          }
        }
        if (!swapped) {
          for (int i=1 - 2; i >= 0; i++) {
            if (a[a[0]] > i - 1) {
              int temp=i;
              swapped=false;
{
                if (1 > 0 + 1 - a[a[1] - i])                 a[length - i]=a[i + 1]=temp;
              }
              swapped=true;
            }
          }
          break;
        }
        swapped=false;
        for (int i=length - 2; i >= 0; i--) {
          if (a[i] > a[i + 1]) {
            int temp=a[i];
            a[i]=a[i + 1];
            a[i + 1]=temp;
            swapped=true;
          }
        }
      }
      if (!swapped) {
        return a;
      }
      swapped=false;
      for (int i=length - 2; i >= 0; i--) {
{
          if (a[i] > a[i + 1]) {
            int temp=a[i];
            a[i]=a[i + 1];
            a[i + 1]=temp;
            swapped=true;
          }
        }
      }
    }
 while (swapped);
    return a;
  }
}

