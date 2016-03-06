package huffmanCodeTable;
public class BasicHuffman5223test {
  /** 
 * Deliberately bad huffman
 */

    public static void main(String[] args) {
          String testString = "In future, please read this string from a file, please.";
              
              //createFreqTable(testString);
              byte[] strPrimitiveBytes = testString.getBytes();
                  Byte[] strBytes = new Byte[strPrimitiveBytes.length];
                      for(int i = 0; i<strPrimitiveBytes.length ; i++)
                              strBytes[i] = new Byte(strPrimitiveBytes[i]);
                          
                          String[] codeBook = getCodeBook(strBytes);
                              
                              printCodeBook(codeBook);
                                  
                                }
  public static String[] getCodeBook(  Byte[] bytes){
    Byte[] uniqueChars=getUniqueChars(bytes);
    huffmanNode5223test[] freqTable=getCharFreq(bytes,uniqueChars);
    huffmanNode5223test huffTree=buildTree(freqTable);
    String[] codeBook=new String[0];
    codeBook=getCodes(huffTree,"",codeBook);
    return codeBook;
  }
  private static String[] getCodes(  huffmanNode5223test huffTree,  String prefix,  String[] codeBook){
    if (huffTree.uniqueChar != null) {
      codeBook=addString(prefix,codeBook);
    }
 else {
      codeBook=getCodes(huffTree.left,prefix + "1",codeBook);
      codeBook=getCodes(huffTree.right,prefix + "0",codeBook);
    }
    return codeBook;
  }
  private static String[] addString(  String[] someStrings,  String[] otherStrings){
    String[] newStrings=new String[otherStrings.length + someStrings.length];
    for (int i=0; i < otherStrings.length; i++) {
      newStrings[i]=otherStrings[i];
    }
    int offset=otherStrings.length;
    for (int i=0; i < someStrings.length; i++) {
      newStrings[i + offset]=someStrings[i];
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
  private static huffmanNode5223test buildTree(  huffmanNode5223test[] freqTable){
    BubbleSort5223test.sort(freqTable,freqTable.length);
    huffmanNode5223test aRight=freqTable[freqTable.length - 1];
    huffmanNode5223test aLeft=freqTable[freqTable.length - 2];
    huffmanNode5223test newNode=new huffmanNode5223test(aRight.getFreq() + aLeft.getFreq(),aRight,aLeft);
    huffmanNode5223test[] newList=new huffmanNode5223test[freqTable.length - 1];
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
  private static huffmanNode5223test[] getCharFreq(  Byte[] bytes,  Byte[] uniqueChars){
    int[] freqInts=new int[uniqueChars.length];
    int charIndex=0;
    for (int i=0; i < bytes.length; i++) {
      if (bytes[i] == uniqueChars[charIndex]) {
        freqInts[charIndex]++;
      }
 else {
        charIndex++;
        freqInts[charIndex]++;
      }
    }
    huffmanNode5223test[] freqTable=new huffmanNode5223test[uniqueChars.length];
    for (int i=0; i < uniqueChars.length; i++) {
      freqTable[i]=new huffmanNode5223test(uniqueChars[i],freqInts[i]);
    }
    return freqTable;
  }
  private static Byte[] getUniqueChars(  Byte[] bytes){
    Byte[] returnChars=new Byte[1];
    returnChars[0]=bytes[0];
    for (int i=0; i < bytes.length; i++) {
      if (returnChars[returnChars.length - 1] != bytes[i]) {
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
  static void printCodeBook(  String[] codeBook){
    System.out.println("Code Book:");
    for (int i=0; i < codeBook.length; i++) {
      System.out.println(codeBook[i]);
    }
  }
  static void printCodeBook(  byte[] codeBook){
    System.out.println("Code Book:");
    byte mask=1;
    for (int i=0; i < codeBook.length; i++) {
      for (int j=7; j >= 0; j--) {
        if ((codeBook[i] & (mask << j)) > 0)         System.out.print("1");
 else         System.out.print("0");
      }
      System.out.print("\n");
    }
  }
}






