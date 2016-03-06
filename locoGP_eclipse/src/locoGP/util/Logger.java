package locoGP.util;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import locoGP.Generation;



public class Logger {
	private static boolean debugLoggingEnabled = true;
	private static DateFormat dateFormat = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss:SSS");
	
	private static String operatorResult = "";
	private static String generationInfo = "";
	private static String trash = "";
	
	private static ByteArrayOutputStream bos = new ByteArrayOutputStream();
	private static OutputStreamWriter osw = new OutputStreamWriter(bos);
	private static BufferedWriter outWriter = new BufferedWriter(osw);
	
	
	private static String LogName = new SimpleDateFormat(
	"yyyyMMdd-HHmmss").format(new Date());
	
	private static String outFile = LogName+"-GPrun.log";
	private static String outFileTrash = LogName+"-GPrunCode.log";
	private static String outFileGenInfo = LogName+"-GenerationInfo.log";
	
	public static void disableDebugLogging() {
		debugLoggingEnabled = false;
	}
	
	public static void enableDebugLogging() {
		debugLoggingEnabled = true;
	}
	
	public static boolean debugLoggingEnabled() {
		return debugLoggingEnabled ;
	}

	public static void log(String stringToLog) {
		
		//if (loggingEnabled)
			appendToOperatorResult(stringToLog);
	}
	
	public static void newLogFile(){
		LogName = new SimpleDateFormat(
				"yyyyMMdd-HHmmss").format(new Date());
		outFile = LogName+"-GPrun.log";
		outFileTrash = LogName+"-GPrunCode.log";
		outFileGenInfo = LogName+"-GenerationInfo.log";
	}
	
	public static void newLogFile(String name){
		LogName = new SimpleDateFormat(
				"yyyyMMdd-HHmmss").format(new Date()) +"-"+name;
		outFile = LogName+"-GPrun.log";
		outFileTrash = LogName+"-GPrunCode.log";
		outFileGenInfo = LogName+"-GenerationInfo.log";
	}
	
	public static void logDebugConsole(String stringToLog){
		if(debugLoggingEnabled){
			System.out.println("DEBUG ------ " + stringToLog);
		}
	}
	
	public static String getLogID(){
		return LogName;
	}
	
	public static void setLogID(String logID){
		LogName = logID;
		outFile = LogName+"-GPrun.log";
		outFileTrash = LogName+"-GPrunCode.log";
		outFileGenInfo  = LogName+"-GenerationInfo.log";
		//return LogName;
	}
	
	public static  void logTrash(String stringToLog) {
		if (debugLoggingEnabled)
			appendToTrashFile(stringToLog);
	}
	
	public static void logGenInfo(String stringToLog) {
		//if (loggingEnabled)
			appendToGenFile(stringToLog);
	}
	
	private static void appendToOperatorResult(String stringToLog){
		operatorResult += formatLog(stringToLog);
		//appendToFile(stringToLog, outFile);
	}

	private static void appendToGenFile(String stringToLog){
		generationInfo += formatLog(stringToLog);
		//appendToFile(stringToLog, LogName+"-GenerationInfo.log");
	}
	
	private static void appendToTrashFile(String stringToLog) {
		trash += formatLog(stringToLog);
		//appendToFile(stringToLog, outFileTrash);
	}
	
	private static String formatLog (String stringToLog){
		Date date = new Date();
		return dateFormat.format(date) + " " + stringToLog + "\n";
	}
	
	private static void appendToLogFile(String stringToLog, String fileName) {  
		//appendToFile(formatLog (stringToLog),  fileName);
		appendToFile(stringToLog,  fileName);
	}
	
	private static void appendToFile(String stringToLog, String fileName) { 
		try {
			File f;
			f = new File(fileName);
			if (!f.exists()) {
				f.createNewFile();
			}
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName,
					true));
			Date date = new Date();
			out.write( stringToLog);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void writeNewFileNoOverwriteIfExists(String stringToLog, String fileName) { 
		try {
			File f;
			f = new File(fileName);
			if (!f.exists()) {
				f.createNewFile();
			}else{
				return;
			}
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName,
					true));
			Date date = new Date();
			out.write( stringToLog);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Writer getWriter(){
		if (debugLoggingEnabled)
			return new PrintWriter(System.out);
		else
			return outWriter;
	}

	/*public static void setLoggingEnabled(boolean loggingEnabled) {
		Logger.loggingEnabled = loggingEnabled;
	}

	public static boolean isLoggingEnabled() {
		return Logger.loggingEnabled;
	}*/

	public static void logAll(String string) {
		log(string);
		logTrash(string);
		logGenInfo(string);
	}

/*	public static void saveqGen(Generation nextGen) {
		
		 aaaaaaaaaaaaaaagh
		 * TODO - This loses bias, and is not necessary since the code has been stable 
		 
		nextGen.converASTString(); // barf
		
		try {
			FileOutputStream f_out = new FileOutputStream(LogName + "-Gen.data");
			ObjectOutputStream obj_out = new ObjectOutputStream (f_out);
			obj_out.writeObject ( nextGen );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		nextGen.converStringAST(); /// really?
		
	}*/

/*	public static Generation loadGenFromFile(String lastGenFileName) {
		Generation loadedGen = null; 
		// Read from disk using FileInputStream
		FileInputStream f_in;
		try {
			f_in = new FileInputStream(lastGenFileName);

			// Read object using ObjectInputStream
			ObjectInputStream obj_in = new ObjectInputStream(f_in);

			// Read an object
			Object obj = obj_in.readObject();

			if (obj instanceof Generation) {
				// Cast object to a Vector
				loadedGen = (Generation) obj;

				// Do something with vector....
				loadedGen.converStringAST(); /// really?
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return loadedGen;
	}*/
	
	public static void flushLog(){
		// Write pending data out to the log file
		appendToLogFile(operatorResult, outFile);
		operatorResult = "";
		appendToLogFile(generationInfo, outFileGenInfo);
		generationInfo = "";
		appendToLogFile(trash, outFileTrash);
		trash = "";
		
		//InputStream is_reader2= new ByteArrayInputStream(ByteArray2);
		//BufferedReader bufR = new BufferedReader(new ByteArrayInputStream(bos.toByteArray()));
		//flushTrashOutput();
		
		
		
	}
	
	private static void flushTrashOutput() {
		// TODO fix this so it writes out once only at the end of gen.
		File f;
		f = new File(outFileTrash);
		OutputStream outputStream = null;
		try {
			if (!f.exists()) {
			f.createNewFile();
			}
			
			outputStream = new FileOutputStream(f);
			//outWriter.append('a');
			//bos.flush();
			//osw.flush();
			byte[] bosArray = bos.toByteArray();
			outputStream.write(bosArray);
			bos.reset();
			outputStream.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		outputStream = null;
		f = null;
		/*
		File f;
		f = new File(outFileTrash);
		
		BufferedWriter out = null;
		try {
			if (!f.exists()) {
				f.createNewFile();
			}
			 out = new BufferedWriter(new FileWriter(outFileTrash,
					true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out;*/
	}

	

	public static void logJavaFile(String className, String codeString) {
		//appendToFile(codeString, className + ".java");
		writeNewFileNoOverwriteIfExists(codeString, className + ".java");
	}

	public static void logBiasFile(String className, int genCount,
			String codeProbabilities) {
		appendToFile(codeProbabilities, className + "-"+genCount + ".bias");
		// TODO Auto-generated method stub
	}

	public static void logSevere(String stringToLog) {
		System.out.println("SEVERE (GP) ------ " + stringToLog);
		logAll(stringToLog);
	}


	
	/*public static void writeJavaFile(String codeString, String className) {
		File f;
		String fileName = className +".java";
		f = new File(fileName);
		if (!f.exists()) {
			appendToFile(codeString, fileName);	
		}
	}*/

	
}