package locoGP.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

public class StringFromFile {
	public static String getStringFromFile(String fileName){
		StringWriter writer = new StringWriter();
		InputStream iStream =StringFromFile.class.getResourceAsStream(fileName); 
				//getClass().getResourceAsStream(fileName);
		
		try {
			//IOUtils.copy(iStream, writer, null);
			IOUtils.copy(iStream, writer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			iStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return writer.toString();
	}
}
