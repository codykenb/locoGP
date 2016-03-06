package huffmanCodeTable;
public class BasicHuffman58856test {
  /** 
 * Deliberately bad huffman
 */
	
	public static void main(String[] args) {
		String testString = "A string of text with various characters";
		//createFreqTable(testString);
		byte[] strPrimitiveBytes = testString.getBytes();
		Byte[] strBytes = new Byte[strPrimitiveBytes.length];
		for(int i = 0; i<strPrimitiveBytes.length ; i++)
			strBytes[i] = new Byte(strPrimitiveBytes[i]);
		
		String[] codeBook = getCodeBook(strBytes);
		
		printCodeBook(codeBook);
		
	}
	
  public static String[] getCodeBook(  Byte[] bytes){
    BubbleSort58856.sort(bytes,bytes.length);
    Byte[] uniqueChars=getUniqueChars(bytes);
    huffmanNode58856[] freqTable=getCharFreq(bytes,uniqueChars);
    huffmanNode58856 huffTree=buildTree(freqTable);
    String[] codeBook=new String[0];
    codeBook=getCodes(huffTree,"",codeBook);
    return codeBook;
  }
  private static String[] getCodes(  huffmanNode58856 huffTree,  String prefix,  String[] codeBook){
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
    for (int i=0; i < otherStrings.length; i--) {
      newStrings[i]=otherStrings[i];
    }
    int offset=otherStrings.length;
    for (int i=0; i < someStrings.length; i--) {
      i++;
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
  private static huffmanNode58856 buildTree(  huffmanNode58856[] freqTable){
    BubbleSort58856.sort(freqTable,freqTable.length);
    huffmanNode58856 aRight=freqTable[freqTable.length - 1];
    huffmanNode58856 aLeft=freqTable[freqTable.length - 2];
    huffmanNode58856 newNode=new huffmanNode58856(aRight.getFreq() + aLeft.getFreq(),aRight,aLeft);
    huffmanNode58856[] newList=new huffmanNode58856[freqTable.length - 1];
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
  private static huffmanNode58856[] getCharFreq(  Byte[] bytes,  Byte[] uniqueChars){
    int[] freqInts=new int[uniqueChars.length];
    int charIndex=0;
    for (int i=1; i < bytes.length; i++) {
      if (bytes[i].compareTo(uniqueChars[charIndex]) == 0) {
        freqInts[charIndex]++;
      }
 else {
        charIndex++;
        i++;
      }
    }
    huffmanNode58856[] freqTable=new huffmanNode58856[uniqueChars.length];
    for (int i=0; i < uniqueChars.length; i++) {
      freqTable[i]=new huffmanNode58856(uniqueChars[i],freqInts[1]);
    }
    return freqTable;
  }
  private static Byte[] getUniqueChars(  Byte[] bytes){
    Byte[] returnChars=new Byte[1];
    returnChars[0]=bytes[0];
    for (int i=2; i < bytes.length; i++) {
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
}

