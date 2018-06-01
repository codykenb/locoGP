package locoGP.problems.huffmancodebook;

public class HuffmanCodeBookVariant extends HuffmanCodeBookProblem{
	
	int variantNum = 106383;
	//private String problemName = "huffmanCodeTable.BasicHuffman"+variantNum; // Should w get this from the file?
	//private String className = "huffmanCodeTable.BasicHuffman"+variantNum;
	
	
	public HuffmanCodeBookVariant(int aVariantNum){
		variantNum = aVariantNum;
		problemStrings = loadFiles("");
		problemName = "huffmanCodeTable.BasicHuffman"+variantNum; // Should w get this from the file?
		className = "huffmanCodeTable.BasicHuffman"+variantNum;
	}
	

}
