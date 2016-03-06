package locoGP.problems;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import locoGP.util.StringFromFile;

import org.apache.commons.io.IOUtils;

public class SortVariant extends Sort1Problem{
	//String variantFileName="";
	//private String problemName = "huffmanCodeTable.BasicHuffman"+variantNum; // Should w get this from the file?
	//private String className = "huffmanCodeTable.BasicHuffman"+variantNum;
	
	
	public SortVariant(String variantFileName, String problemName){
		problemString = StringFromFile.getStringFromFile(variantFileName);
		
		this.problemName = problemName;//this.className;//+"Test";
		className = problemName;
		methodName = "sort";
		
	}
	
	/*protected String getStringFromFile(String fileName){
		StringWriter writer = new StringWriter();
		InputStream iStream = getClass().getResourceAsStream(fileName);
		
		try {
			//IOUtils.copy(iStream, writer, null);
			IOUtils.copy(iStream, writer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return writer.toString();
	}*/
}
