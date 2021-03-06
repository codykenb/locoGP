package huffmanCodeTable;
//package huffmanCodeTable;

public class BasicHuffman {

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
	
	public static String[] getCodeBook(Byte[] bytes) {
		
		BubbleSort.sort(bytes, bytes.length);
		
		Byte[] uniqueChars = getUniqueChars(bytes);
		
		huffmanNode[] freqTable = getCharFreq(bytes, uniqueChars);
		
		huffmanNode huffTree = buildTree(freqTable);
		
		String[] codeBook = new String[0];
		
		codeBook = getCodes(huffTree, "", codeBook);
		
		return codeBook;
	}
	
	private static String[] getCodes(huffmanNode huffTree, String prefix, String[] codeBook){
		if(huffTree.uniqueChar != null){
			codeBook = addString(prefix, codeBook);
			//System.out.println(new Character((char)(byte)huffTree.uniqueChar) + " " + prefix);
		}else{
			codeBook = getCodes(huffTree.left, prefix +"1", codeBook);
			codeBook = getCodes(huffTree.right, prefix +"0", codeBook);
		}
		
		return codeBook;
	}
	
	private static String[] addString(String[] someStrings, String[] otherStrings){
		String[] newStrings = new String[otherStrings.length+someStrings.length];
				
		for(int i = 0 ; i < otherStrings.length; i ++){
			newStrings[i] = otherStrings[i];
		}
		int offset = otherStrings.length;
		for (int i = 0 ; i < someStrings.length; i++){
			newStrings[i+offset] = someStrings[i];
		}
		
		//newStrings[newStrings.length-1] = aStr;
		return newStrings;
	}
	
	private static String[] addString(String aStr, String[] otherStrings){
		String[] newStrings = new String[otherStrings.length+1];
		for(int i = 0 ; i < otherStrings.length; i ++){
			newStrings[i] = otherStrings[i];
		}
		newStrings[newStrings.length-1] = aStr;
		return newStrings;
	}
	
	private static huffmanNode buildTree(huffmanNode[] freqTable){

		BubbleSort.sort(freqTable, freqTable.length);
		
		huffmanNode aRight = freqTable[freqTable.length - 1];
		huffmanNode aLeft = freqTable[freqTable.length - 2];
		huffmanNode newNode = new huffmanNode(aRight.getFreq()+aLeft.getFreq() , aRight, aLeft);
		
		huffmanNode[] newList = new huffmanNode[freqTable.length-1];
		for(int i = 0 ; i <newList.length ; i++){
			newList[i] = freqTable[i];
		}
		newList[newList.length-1] = newNode;
		
		if(newList.length ==1){
			return newList[0];
		}else{ // if we remove this else, will tail recursion happen?
			return buildTree(newList);
		}
	}
	
	/*private static huffmanNode buildTreeSF(h[117, 116, 115, 114, 112, 111, 110, 109, 108, 105, 104, 103, 102, 101, 100, 97, 73, 46, 44, 32]uffmanNode[] freqTable){
		// Build a tree as per Shannon-Fano
		
		BubbleSort.sort(freqTable, freqTable.length); 
		
		if(freqTable.length!=1){
			int leftSum=freqTable[0].freq, 
					rightSum = freqTable[freqTable.length-1].freq,
					curIndex=0, lastDiff=0;
			boolean bestFound=false;
			while(!bestFound){
				leftSum=rightSum=0;
				for(int i = 0 ; i <=curIndex ; i++){
					leftSum +=freqTable[i].freq; 
				}
				for(int i = curIndex+1 ; i <freqTable.length ; i++){
					rightSum +=freqTable[i].freq; 
				}
				
			}
		}else return freqTable[0];
		
	}*/
	
	private static huffmanNode[] getCharFreq(Byte[] bytes, Byte[] uniqueChars){
		int[] freqInts = new int[uniqueChars.length];
		int charIndex = 0 ;
		for (int i = 0 ; i< bytes.length ; i ++){
			if(bytes[i].compareTo(uniqueChars[charIndex])==0){
				freqInts[charIndex]++;
			}else{
				charIndex++;
				freqInts[charIndex]++; 
			}
		}
		
		huffmanNode[] freqTable = new huffmanNode[uniqueChars.length];
		for (int i = 0 ; i< uniqueChars.length ; i ++){
			freqTable[i] = new huffmanNode(uniqueChars[i], freqInts[i]);
		}

		return freqTable;
	}

	private static Byte[] getUniqueChars(Byte[] bytes) {
		Byte[] returnChars = new Byte[1]; //
		returnChars[0] = bytes[0];
		
		for (int i = 0 ; i< bytes.length ; i ++){
			if(returnChars[returnChars.length-1].compareTo(bytes[i]) != 0 ){
				Byte[] tempChars = returnChars;
				returnChars = new Byte[tempChars.length+1];
				for(int j = 0 ; j < tempChars.length; j ++){
					returnChars[j] = tempChars[j];
				}
				returnChars[returnChars.length-1] = bytes[i];
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


