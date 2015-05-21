package huffmanCodeTable;

public class huffmanNode implements Comparable{ //// aaaaaghhghghghgh don't put this first! actually make it a seperate class!
	
	Byte uniqueChar = null;
	int freq = 0 ;
	huffmanNode left, right;
	
	public int getFreq(){
		return freq;
	}
	
	huffmanNode(byte aChar, int freq){
		uniqueChar = aChar;
		this.freq = freq;
	}
	
	huffmanNode(int freq, huffmanNode left, huffmanNode right){
		this.freq = freq;
		this.right = right;
		this.left = left;
	}

	@Override
	public int compareTo(Object hN) {
		return this.freq - ((huffmanNode)hN).freq;
	}
}