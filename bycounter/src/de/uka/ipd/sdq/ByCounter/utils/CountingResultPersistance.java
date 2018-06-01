package de.uka.ipd.sdq.ByCounter.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

import de.uka.ipd.sdq.ByCounter.execution.CountingResultBase;

public class CountingResultPersistance {
	
	public static final String DEFAULT_COUNTING_RESULT_EXTENSION = "ser";
	public static final String DEFAULT_COUNTING_RESULT_NAME_SUFFIX = "ByCounterResult";
	public static final String DEFAULT_COUNTING_RESULT_NAMECORE = "CountingResult";
	public static final String DEFAULT_RESULTS_DIRECTORY = "."+File.separator+"MK_COUNTING_RESULTS";
	
	private static Logger log = Logger.getLogger(CountingResultPersistance.class.getCanonicalName());
	
	public static CountingResultBase deserialiseCountingResult(
			String path){
		CountingResultBase ret = null;
		if(path==null){
			log.severe("Null path passed - skipping seralization");
			return null;
		}
		if(path.equals("")){
			log.severe("Empty path passed - skipping seralization");
			return null;
		}
		File file = new File(path);
		if(!file.exists()){
			log.severe("Passed path to serialized CountingResult points to a non-existing entity...");
			return null;
		}
		if(!file.isFile()){
			log.severe("Passed path to serialized CountingResult points to a non-file entity...");
			return null;
		}
		FileInputStream fis=null;
		try{
			fis = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fis);
			Object readObj = ois.readObject();
			if(readObj instanceof CountingResultBase){
				ret = (CountingResultBase) readObj;
			}else{
				log.severe("Read serialized object, but it is not a CountingResult ...");
				return null;
			}
		}catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			if(fis!=null){
				try{
					fis.close();
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return ret;
	}
	
	/**
	 * Delegates to the fully parameterised method
	 * @param cr
	 * @return Canonical path where the serialised result is written.
	 */
	public static String serialiseCountingResult(
			CountingResultBase cr) {
		return serialiseCountingResult(
				cr, 
				DEFAULT_RESULTS_DIRECTORY,
				DEFAULT_COUNTING_RESULT_NAMECORE,
				DEFAULT_COUNTING_RESULT_NAME_SUFFIX, 
				DEFAULT_COUNTING_RESULT_EXTENSION,
				false,
				false,
				true,
				System.currentTimeMillis()
				);
	}
	
	/**
	 * Delegates to the fully parameterised method
	 * @param cr
	 * @param nameCore
	 * @param timestamp
	 * @return Canonical path where the serialised result is written.
	 */
	public static String serialiseCountingResult(
			CountingResultBase cr, 
			String nameCore, 
			Long timestamp) {
		return serialiseCountingResult(
				cr, 
				DEFAULT_RESULTS_DIRECTORY,
				nameCore,
				DEFAULT_COUNTING_RESULT_NAME_SUFFIX, 
				DEFAULT_COUNTING_RESULT_EXTENSION,
				false,
				false,
				true,
				timestamp
				);
	}

	public static String serialiseCountingResult(
					CountingResultBase cr, 
					String directory,
					String nameCore,
					String suffix, 
					String fileExtension,
					boolean useCountingResultMethodBeginningTimestamp,
					boolean useCountingResultMethodReportingTimestamp,
					boolean useTimestampParameter,
					Long timestampToUse
					) {
		if(cr==null){
			log.severe("Null counting result passed - skipping seralization");
			return null;
		}
		if(directory==null){
			log.severe("Null directory path passed - skipping seralization");
			return null;
		}
		if(directory.equals("")){
			log.severe("Empty directory path passed - skipping seralization");
			return null;
		}
		if(nameCore==null){
			log.severe("Null name core passed - skipping seralization");
			return null;
		}
		if(nameCore.equals("")){
			log.severe("Empty name core passed - skipping seralization");
			return null;
		}
		if(suffix==null){
			log.severe("Null suffix passed - skipping seralization");
			return null;
		}
		if(suffix.equals("")){
			log.severe("Empty suffix passed - skipping seralization");
			return null;
		}
		if(fileExtension==null){
			log.severe("Null file extension passed - skipping seralization");
			return null;
		}
		if(fileExtension.equals("")){
			log.severe("Empty file extension passed - skipping seralization");
			return null;
		}
		File dir = new File(directory);
		boolean dirReady = false;
		if(!dir.exists()){
			dirReady = dir.mkdirs();
		}else if(dir.exists() && dir.isDirectory()){
			dirReady = true;
		}
		if(!dirReady){
			log.severe("Passed path to directory points to a file, " +
					"or is a directory that is not available and cannot be created - skipping seralization");
			return null;
		}
		FileOutputStream fos=null;
		String canonicalPath=null;
		try{
			String fileName = dir.getAbsolutePath()+File.separator+
			nameCore+".";
			if(useCountingResultMethodBeginningTimestamp){
				fileName += cr.getMethodInvocationBeginning()+".";
			}
			if(useCountingResultMethodReportingTimestamp){
				fileName += cr.getReportingTime()+".";
			}
			if(useTimestampParameter){
				fileName += timestampToUse+".";
			}
			fileName += suffix+"."+fileExtension;
			File file = new File(fileName);
			canonicalPath = file.getCanonicalPath();
			System.out.println("Writing counting result to "+canonicalPath);
			fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(cr);
		}catch (Exception e) {
			throw new RuntimeException(e);
		}finally{
			if(fos!=null){
				try{
					fos.close();
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return canonicalPath;
	}


}
