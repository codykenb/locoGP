package locoGP.problems;
public class Sort1Radix27404 {
  public static Integer[] sort(  Integer[] a,  Integer length){
    for (int shift=Integer.SIZE - 1; shift > 0; shift--) {
      Integer[] tmp=new Integer[a.length];
      int j=0;
      for (int i=0; i < length; i++) {
        boolean move=a[i] << shift >= 0;
        if (shift == 0 ? !move : move) {
          tmp[j]=a[i];
          j++;
        }
 else {
          a[i - j]=a[i];
        }
      }
      for (int i=j; i < tmp.length; i++) {
        tmp[i]=a[i - j];
      }
      a=tmp;
    }
{
      
      return a;
    }
  }
}

