package locoGP.roughwork;
/*

  Purpose of this prog is to test the algorithm that was created
  it embodies the "fitness function"

*/

import java.util.Arrays; // This package contains the original dualpivotquicksort that our recombined alg will be up against
import java.util.Random;

public class SortTester{

  private static int[] testArr = new int[] { 23,45 ,65 ,54, 34,16, 14,56,7,8,4,12,99,46,23,203,546,12,34,87,289,5,56,23 }; //generateRandomArray();
	
  public static int[] getTestArray(){
	  int[] tempTestArr = new int[testArr.length];
	  System.arraycopy( testArr, 0, tempTestArr, 0, testArr.length);
	  return tempTestArr;
  }
  
  private static int[] generateRandomArray(){
    Random randomGen = new Random();
    int[] newRandIntArr = new int[randomGen.nextInt(200)];
    for(int i = 0 ; i< newRandIntArr.length; i++){
      newRandIntArr[i]=randomGen.nextInt(1000);
    }
    return newRandIntArr;
  }

  public static int testSortAbility(int[] sortAttempt){
    
    int[] fullySorted = new int[sortAttempt.length];
    System.arraycopy( sortAttempt, 0, fullySorted, 0, sortAttempt.length);
    Arrays.sort(fullySorted);
    //int distanceToSorted = arrayDistanceFromBeingSorted(testArr, fullySorted) ;
    
    //int[] sortAttempt = new int[testArr.length];
    //System.arraycopy( testArr, 0, sortAttempt, 0, testArr.length);
//    System.out.println("\nOriginal");
//    printArray(testArr);
//    System.out.println("\nShould be");
//    printArray(fullySorted);

    //Ver2DualPivotQuicksort.sort(sortAttempt); // this calls the recombined sort alg
//    System.out.println("\nSort attempt");
//    printArray(sortAttempt);
    int numMissing = checkNumMissing(sortAttempt, testArr) ;
    int fileScore = 0;
//    System.out.println("\nStarting");
    if ( identicalArrays(sortAttempt, testArr) ) {
  //    System.out.println("\nNo Change");
      fileScore = 0;
    }else if( numMissing != 0 ) {
  //    System.out.println("\nChange but some missing "+ numMissing + " out of " +sortAttempt.length);
      // if there are some missing, it means it is doing something, the less that are missing the better
      fileScore = (int)((((float)(sortAttempt.length-numMissing)/(float)sortAttempt.length)*250)+1);
    }else{ // arrays are not the same, and contain all elements
     // System.out.println("\nChange, none missing, but unsorted");
      // furthest from original is best (500-750) 
      // closer to fully sorted is better (750-1000)
      int originalArrayDist = arrayDistanceFromBeingSorted(testArr, fullySorted);
      int sortAttemptArrayDist = arrayDistanceFromBeingSorted(sortAttempt, fullySorted);
      if(sortAttemptArrayDist > originalArrayDist){
        // if the attempt is less sorted than the original, its still good (501-750)
        // the more different it is from the original the better?
        // worse it can be is half length of array by number of elements
        int asUnsortedAsPossible = (sortAttempt.length/2) * sortAttempt.length;
        fileScore = ((int)((float)(sortAttemptArrayDist-asUnsortedAsPossible)/(float)(originalArrayDist - asUnsortedAsPossible))*250) +251;
      }else{ // the array is more sorted than the original
        // the closer to sorted the better
        fileScore = (int)((((float) originalArrayDist -sortAttemptArrayDist)/(float)originalArrayDist) * 500 ) +500; // a sortAttemptDist of 0 will give score of 1000
      }
    }

    /*
      Meta-gp
      There should also be an optimal length of the file, this should contribute to the scoring.
      Ideally our GP mechanism should be able to determine the length of file which is the best.
      We need a feedback mechnism, which at first does not know the optimal length, but through 
      iterations is able to find out the likely optimal length of the file (how??)
      This should then impact on the mutation rate, and possibly specify mutation that adds lines, or reduces them
      depending on how long the file is in comparison to the perceived optimal length

      At a certain point, making the file longer will probably never be able to achieve a proper solution,
      past this point, the removal of code may be necessary to reach the solution. 

      if individuals/files of a certain length seem to have a better fitness, should it be better that
      subsequent individuals converge around this length? closer to this length the better? or within 
      some range of this length is the best?

      Could this length be used as a method of specifying a piece of code that should be functionalised 
      and automatically reused? This would restart the GP run with this functionalised block as an 
      atomic/primitive element of the system.
      Could this functionalisation occur if the system remains in stasis
      around this length without making any significant jump in functionality for a number of generations?
    */


    //System.out.println("\nSort attempt");
    //printArray(sortAttempt);
    //checkAllElementsTheSame(sortAttempt);
    //int sortAttemptDistance = arrayDistanceFromBeingSorted(sortAttempt, fullySorted);
    //printArray(fullySorted);
//    System.out.println("Calculation is: (" + distanceToSorted +" - " +sortAttemptDistance + " / " +distanceToSorted +" ) * 1000 )");
//    System.out.println("\nSort attempt");
//    printArray(sortAttempt);
    //if(distanceToSorted>0){
//    System.out.println("\n value is : ");
//    System.out.println((((float)distanceToSorted - sortAttemptDistance)/distanceToSorted));
    //int finalScore= (int)((((float)distanceToSorted - sortAttemptDistance)/distanceToSorted)* 1000 );
    // filthy hack
     
    return fileScore;
  }

  private static int checkNumMissing(int[] sortAttempt, int[] originalArray){
    int checkNum = 0 ;
    int currentCount=0;
    int secondCount=0;
    int numMissing=0;
    boolean alreadyCounted=false;

    for(int i = 0 ; i< originalArray.length; i++){
     checkNum = originalArray[i];
     secondCount = currentCount=0;
     alreadyCounted=false;
    
     for(int j = 0 ; j<i; j++){
       if(originalArray[i]==originalArray[j])
         alreadyCounted = true;
     }

     if( !alreadyCounted){
       // how many should we have?
       for(int j = 0 ; j<originalArray.length; j++){
         if(checkNum == originalArray[j])
           currentCount++;
       }
       // how many do we have?
       for(int j = 0 ; j<originalArray.length; j++){
         if(checkNum == sortAttempt[j])
           secondCount++;
       }
       if(secondCount < currentCount)
         numMissing += currentCount - secondCount;
     }
    }
    return numMissing;
  }

  private static boolean identicalArrays(int[] firstArray, int[] secondArray){
    boolean theSame = true;
    for (int i = 0 ; i< firstArray.length; i++){
    //  System.out.println("comparing " + firstArray[i] +" with "+ secondArray[i]);
      if(firstArray[i] != secondArray[i])
        theSame = false;
    }
    return theSame;
  }

  private static void checkAllElementsTheSame(int[] arrayToCheck){
    int initialVal=arrayToCheck[0];
    int sameCount=0;
    for (int i =0; i < arrayToCheck.length; i++){
    //System.out.println("Same count"+sameCount+" "+arrayToCheck.length+" " + arrayToCheck[i] +" against " + initialVal);
      if (arrayToCheck[i] == initialVal)
        sameCount++;
    }
    //System.out.println("Same count"+sameCount+" "+arrayToCheck.length);
    if(sameCount == arrayToCheck.length){
    //  System.out.print("Theyre the same");
      System.out.print("250");
      System.exit(0);
    }
  }
 
  private static int arrayDistanceFromBeingSorted(int[] arrayToCheck, int[] refArray){
    int totalDistance = 0;
    int tempDistance = 0;
    int lowestTempDist = 0;
    //int occuranceDiff=0;
    boolean found = false;
    for( int i = 0 ; i < refArray.length ; i++){
      found=false;
      for ( int j =0 ; j < arrayToCheck.length; j++){
        if( arrayToCheck[j] == refArray[i] ){
          tempDistance = (i-j);
          if (tempDistance <0)
            tempDistance=-tempDistance;
          if(found == false)
            lowestTempDist = tempDistance;
          if (found == true && tempDistance < lowestTempDist){
            lowestTempDist=tempDistance;
          }
            
          found = true;
        }
      }
      totalDistance += lowestTempDist;
      //System.out.println("Just checked "+refArray[i] +" " + totalDistance);
      if (found ==false)
        totalDistance += 1; //(int)(arrayToCheck.length/2);
     // occuranceDiff=  findNumOccurance(arrayToCheck[i], arrayToCheck) - findNumOccurance(arrayToCheck[i], refArray);
     // if (occuranceDiff<0)
     //   occuranceDiff = -occuranceDiff; 
     // totalDistance += occuranceDiff ;
    }
    //System.out.println("Total " + totalDistance);
    return totalDistance;
  }

  public static int findNumOccurance(int valToFind, int[] arrayToCheck){
    int count =0 ;
    for (int i =0; i < arrayToCheck.length; i++){
      if (valToFind == arrayToCheck[i])
        count++;
    }
    return count;
  }


  public static void printArray(int[] arrayToPrint){
    for(int i = 0 ; i< arrayToPrint.length; i++){
      System.out.print(" " + arrayToPrint[i] +" " );
    }
  }
}