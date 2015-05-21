package locoGP.problems;
public class Sort1SelectionProblem70 {
  public static Integer[] sort(  Integer[] a,  Integer length){
    for (int currentPlace=0; currentPlace < length - 1; currentPlace++) {
      int smallest=Integer.MAX_VALUE;
      int smallestAt=currentPlace;
      for (int check=currentPlace; check < length; check++) {
        if (a[check] < smallest) {
          smallestAt=check;
          smallest=a[check];
        }
      }
      int temp=a[currentPlace];
      a[currentPlace]=a[smallestAt];
      a[smallestAt]=temp;
    }
    return a;
  }
}

