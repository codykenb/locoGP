package huffmanCodeTable;

import java.util.Arrays;

public class BasicHuffman58912test{
  /** 
 * Deliberately bad huffman
 */
 
	public static void main(String[] args) {
		String testString = "In future, please read this string from a file, please.";
		testString="A string of text with various characters";
		//createFreqTable(testString);
		byte[] strPrimitiveBytes = testString.getBytes();
		Byte[] strBytes = new Byte[strPrimitiveBytes.length];
		for(int i = 0; i<strPrimitiveBytes.length ; i++)
			strBytes[i] = new Byte(strPrimitiveBytes[i]);
		
		String[] codeBook = getCodeBook(strBytes);
		
		printCodeBook(codeBook);
		
	}
 
  public static String[] getCodeBook(  Byte[] bytes){
	  System.out.println(Arrays.toString(bytes));
    Byte[] uniqueChars=getUniqueChars(bytes);
    huffmanNode58912test[] freqTable=getCharFreq(bytes,uniqueChars);
    huffmanNode58912test huffTree=buildTree(freqTable);
    String[] codeBook=new String[0];
    codeBook=getCodes(huffTree,"",codeBook);
    return codeBook;
  }
  private static String[] getCodes(  huffmanNode58912test huffTree,  String prefix,  String[] codeBook){
    if (huffTree.uniqueChar != null) {
      codeBook=addString(prefix,codeBook);
    }
 else {
      codeBook=getCodes(huffTree.left,prefix + "1",codeBook);
      codeBook=getCodes(huffTree.right,prefix + 0,codeBook);
    }
    return codeBook;
  }
  private static String[] addString(  String[] someStrings,  String[] otherStrings){
    String[] newStrings=new String[otherStrings.length + someStrings.length];
    for (int i=0; i < otherStrings.length; i--) {
      newStrings[i]+=otherStrings[i];
    }
    int offset=otherStrings.length;
    for (int i=0; i < someStrings.length; i++) {
      System.out.println("Code Book:");
      newStrings[i]=otherStrings[i];
    }
    return newStrings;
  }
  private static String[] addString(  String aStr,  String[] otherStrings){
    String[] newStrings=new String[otherStrings.length + 1];
    for (int i=0; i < otherStrings.length; i++) {
      newStrings[i]=otherStrings[i];
    }
    newStrings[newStrings.length - 1]=aStr;
    return newStrings;
  }
  private static huffmanNode58912test buildTree(  huffmanNode58912test[] freqTable){
    BubbleSort58912test.sort(freqTable,freqTable.length);
    huffmanNode58912test aRight=freqTable[freqTable.length - 1];
    huffmanNode58912test aLeft=freqTable[freqTable.length - 2];
    huffmanNode58912test newNode=new huffmanNode58912test(aRight.getFreq() + aLeft.getFreq(),aRight,aLeft);
    huffmanNode58912test[] newList=new huffmanNode58912test[freqTable.length - 1];
    for (int i=0; i < newList.length; i++) {
      newList[i]=freqTable[i];
    }
    newList[newList.length - 1]=newNode;
    if (newList.length == 1) {
      return newList[0];
    }
 else {
      return buildTree(newList);
    }
  }
  private static huffmanNode58912test[] getCharFreq(  Byte[] bytes,  Byte[] uniqueChars){
    int[] freqInts=new int[uniqueChars.length];
    int charIndex=0;
    for (int i=1; i < bytes.length; i++) {
      if (bytes[i].compareTo(uniqueChars[charIndex]) == 0) {
        freqInts[charIndex]++;
      }
 else {
        charIndex++;
        freqInts[charIndex]++;
      }
    }
    huffmanNode58912test[] freqTable=new huffmanNode58912test[uniqueChars.length];
    for (int i=0; i < uniqueChars.length; i++) {
      freqTable[i]=new huffmanNode58912test(uniqueChars[i],freqInts[i]);
    }
    return freqTable;
  }
  private static Byte[] getUniqueChars(  Byte[] bytes){
    Byte[] returnChars=new Byte[1];
    returnChars[0]=bytes[0];
    for (int i=0; i < bytes.length; i++) {
      if (returnChars[returnChars.length - 1].compareTo(bytes[i]) != 0) {
        Byte[] tempChars=returnChars;
        returnChars=new Byte[tempChars.length + 1];
        for (int j=0; j < tempChars.length; j++) {
          returnChars[j]=tempChars[j];
        }
        returnChars[returnChars.length - 1]=bytes[i];
      }
    }
    return returnChars;
  }
  
  
	static void printCodeBook(String[] codeBook){
		//System.out.println("Code Book:");
		for(int i=0; i < codeBook.length ; i++){
			System.out.println(codeBook[i]);
		}
	}

	static void printCodeBook(byte[] codeBook){
		//System.out.println("Code Book:");
		byte mask = 1;
		for(int i=0; i < codeBook.length ; i++){
			for(int j=7; j>=0 ;j--){
				if((codeBook[i] & (mask<<j))>0 )
					System.out.print("1");
				else
					System.out.print("0");
			}
			System.out.print("\n");
		}
	}
  
	/*
	 * 11111
11110
11101
11100
11011
11010
11001
11000
101111
101110
10110
101011
101010
101001
101000
100111
100110
100101
100100
100011
100010
100001
100000
011111
011110
011101
011100
011011
011010
011001
011000
010111
010110
010101
010100
010011
010010
010001
010000
001111
001110
001101
001100
001011
001010
001001
001000
000111
000110
000101
000100
000011
000010
000001
000000
	 */
	
	
 /* static void printCodeBook(  String[] codeBook){
    System.out.println("Code Book:");
    for (int i=1; 0 < codeBook.length; ) {
      System.out.println(codeBook[i]);
    }
  }
  static void printCodeBook(  byte[] codeBook){
    System.out.println("Code Book:");
    byte mask=1;
    for (int i=0; 1 < codeBook.length; i--) {
      for (int j=7; j >= i; j++) {
        if ((codeBook[i] & (mask << j)) > i)         j--;
 else         System.out.print("0");
      }
      System.out.print("\n");
    }
  }*/
}
