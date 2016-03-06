package locoGP.problems.huffmancodebook;

import locoGP.problems.CompilationDetail;
import locoGP.problems.CompilationSet;
import locoGP.util.StringFromFile;

public class HuffmanCodeBookVariant extends HuffmanCodeBookProblem{
	
	int variantNum = 106383;
	//private String problemName = "huffmanCodeTable.BasicHuffman"+variantNum; // Should w get this from the file?
	//private String className = "huffmanCodeTable.BasicHuffman"+variantNum;
	
	
	public HuffmanCodeBookVariant(int aVariantNum){
		variantNum = aVariantNum;
		problemStrings = loadFiles();
		problemName = "huffmanCodeTable.BasicHuffman"+variantNum; // Should w get this from the file?
		className = "huffmanCodeTable.BasicHuffman"+variantNum;
	}
	
	private CompilationSet loadFiles(){
		CompilationDetail[] fileSet = new CompilationDetail[3];

		String fileContents = StringFromFile.getStringFromFile("/locoGP/problems/huffmancodebook/BasicHuffman"+variantNum+".txt");
		fileSet[0] = new CompilationDetail(fileContents, "huffmanCodeTable",
				"BasicHuffman"+variantNum);

		fileContents = StringFromFile.getStringFromFile("/locoGP/problems/huffmancodebook/BubbleSort"+variantNum+".txt");
		fileSet[1] = new CompilationDetail(fileContents, "huffmanCodeTable",
				"BubbleSort"+variantNum);

		fileContents = StringFromFile.getStringFromFile("/locoGP/problems/huffmancodebook/huffmanNode"+variantNum+".txt");
		fileSet[2] = new CompilationDetail(fileContents, "huffmanCodeTable",
				"huffmanNode"+variantNum);

		CompilationSet cS = new CompilationSet(fileSet);
		return cS;
		
		
	}

}
