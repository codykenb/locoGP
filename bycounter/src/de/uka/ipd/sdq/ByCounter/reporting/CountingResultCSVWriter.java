package de.uka.ipd.sdq.ByCounter.reporting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import de.uka.ipd.sdq.ByCounter.execution.CountingResultBase;
import de.uka.ipd.sdq.ByCounter.parsing.ArrayCreation;
import de.uka.ipd.sdq.ByCounter.results.CountingResult;
import de.uka.ipd.sdq.ByCounter.utils.ASMOpcodesMapper;

/**
 * Docs TODO  
 * 
 * @author Michael Kuperberg
 * @author Martin Krogmann
 * @since 0.1
 * @version 1.2
 */
public class CountingResultCSVWriter implements ICountingResultWriter {

	private static char entriesSeparationChar = ',';


	/**
	 * Constructs an instance of {@link CountingResult} as written by 
	 * {@link #writeResultToFile(CountingResultBase, boolean, long)}.
	 * @param csvFile The {@link File} that points to the csv file.
	 * @return An instance of {@link CountingResult} or null if an error 
	 * occurred.
	 * @throws IOException When reading fails.
	 */
	public static CountingResult readCountingResultFromCSV(File csvFile) throws IOException {
		long executionStart = 1;
		long reportingStart = 1;
		long[] filteredCounts = new long[CountingResult.MAX_OPCODE];
		TreeMap<String, Long> methodCounts = new TreeMap<String, Long>();
		
		Logger log = Logger.getAnonymousLogger();
		
		// make sure the file exists
		if(!csvFile.exists()) {
			log.severe(
					"Cannot read from csv because the file '" + 
					csvFile.getAbsolutePath() + "' does not exist.");
			return null;
		}
		
		List<Integer> opcodes = new ArrayList<Integer>();
		List<String> methodNames = new ArrayList<String>();
		
		// read the file
		BufferedReader reader = new BufferedReader(new FileReader(csvFile));
		String line = reader.readLine();
		
		// since the line starts with ints for opcodes, we need to track when 
		// method names start to appear
		boolean hasOpcodes = true;
		int lineNr = 1;
				
		while(line != null) {
			String[] entries = line.split("" + entriesSeparationChar);
			// the first line has keys, i.e. opcodes and methodnames
			if(lineNr == 1) {
				for(String s : entries) {
					s = s.substring(1, s.length()-1);
					if(hasOpcodes) {
						try {
							int opcode = Integer.parseInt(s);
							opcodes.add(opcode);
						} catch(NumberFormatException nfe) {
							hasOpcodes = false;
							methodNames.add(s);
						}
					} else {
						methodNames.add(s);
					}
				}
			// the second line has the counts
			} else if(lineNr == 2) {
				if(entries.length != opcodes.size() + methodNames.size()) {
					log.severe("entries.length: " + entries.length);
					log.severe("opcodes.size(): " +opcodes.size());
					log.severe("methodNames.size(): " + methodNames.size());
					log.severe("methodNames: " + methodNames);
				}
				// read the counts for the opcodes
				for(int i = 0; i < opcodes.size(); i++) {
					filteredCounts[opcodes.get(i)] =
							Long.parseLong(entries[i]);
				}
				// read the counts for the methods
				for(int i = 0; i < methodNames.size(); i++) {
					methodCounts.put(methodNames.get(i), 
							Long.parseLong(entries[opcodes.size() + i]));					
				}
			}
			line = reader.readLine();
			lineNr++;
		}
		
		
		
		CountingResult r = new CountingResult();
		r.setMethodInvocationBeginning(executionStart);
		r.setReportingTime(reportingStart);
		r.setOpcodeCounts(filteredCounts);
		r.overwriteMethodCallCounts(methodCounts);
		
		return r;
	}
	
	private boolean appendGrandTotalAtTheEnd = false;
	
	//	182 corresponds to INVOKEVIRTUAL
	//	183 corresponds to INVOKESPECIAL
	//	184 corresponds to INVOKESTATIC
	//	185 corresponds to INVOKEINTERFACE
	private boolean listInvokeOpcodes = true;
	private Logger log = Logger.getLogger(this.getClass().getCanonicalName());
	private boolean performIntegrityCheckOnInvokeOpcodes = false;
	private String pieceFileNameCore = "CBSE";
	private String pieceFileNameExtension = "csv";
	private String pieceFilePath = ".";
	private boolean truncatedUndefindedOpcodes = false;
	private boolean writeArrayDetailsToSeparateFile = false;//write array details to a separate file
	private boolean writeBooleansAsIntegers = true;
	private boolean writeOpcodesAsIntegers = false;
	
	private boolean writeUnusedOpcodes = false;

	private File lastWrittenFile;
	
	public CountingResultCSVWriter() {
		super();
	}
	
	/**
	 * TODO
	 * @param appendGrandTotalAtTheEnd
	 * @param entriesSeparationChar
	 * @param listInvokeOpcodes
	 * @param performIntegrityCheckOnInvokeOpcodes
	 * @param pieceFileNameCore
	 * @param pieceFileNameExtension
	 * @param pieceFilePath
	 * @param writeBooleansAsIntegers
	 * @param writeOpcodesAsIntegers
	 * @param writeUnusedOpcodes
	 * @param truncateUndefinedOpcodes
	 * @param writeArrayDetailsToSeparateFile
	 */
	public CountingResultCSVWriter(
			boolean appendGrandTotalAtTheEnd,
			char entriesSeparationChar, 
			boolean listInvokeOpcodes,
			boolean performIntegrityCheckOnInvokeOpcodes,
			String pieceFileNameCore, 
			String pieceFileNameExtension,
			String pieceFilePath, 
			boolean writeBooleansAsIntegers,
			boolean writeOpcodesAsIntegers,
			boolean writeUnusedOpcodes,
			boolean truncateUndefinedOpcodes,
			boolean writeArrayDetailsToSeparateFile) {
		this();
		this.appendGrandTotalAtTheEnd = appendGrandTotalAtTheEnd;
		CountingResultCSVWriter.entriesSeparationChar = entriesSeparationChar;
		this.listInvokeOpcodes = listInvokeOpcodes;
		this.performIntegrityCheckOnInvokeOpcodes = performIntegrityCheckOnInvokeOpcodes;
		this.pieceFileNameCore = pieceFileNameCore;
		this.pieceFileNameExtension = pieceFileNameExtension;
		this.pieceFilePath = pieceFilePath;
		this.writeBooleansAsIntegers = writeBooleansAsIntegers;
		this.writeOpcodesAsIntegers = writeOpcodesAsIntegers;
		this.writeUnusedOpcodes = writeUnusedOpcodes;
		this.truncatedUndefindedOpcodes = truncateUndefinedOpcodes;
		this.writeArrayDetailsToSeparateFile = writeArrayDetailsToSeparateFile;
	}

	public boolean isWriteBooleansAsIntegers() {
		return this.writeBooleansAsIntegers;
	}

	public boolean isWriteOpcodesAsIntegers() {
		return this.writeOpcodesAsIntegers;
	}

	public void setWriteBooleansAsIntegers(boolean writeBooleansAsIntegers) {
		this.writeBooleansAsIntegers = writeBooleansAsIntegers;
	}

	public void setWriteOpcodesAsIntegers(boolean writeOpcodesAsIntegers) {
		this.writeOpcodesAsIntegers = writeOpcodesAsIntegers;
	}
	

	/* (non-Javadoc)
	 * @see de.uka.ipd.sdq.ByCounter.utils.IResultWriter#writeResult(de.uka.ipd.sdq.ByCounter.execution.CountingResult, boolean, long)
	 */
	public long writeResultToFile(
			CountingResultBase cr,
			boolean usePrevTimestamp,
			long prevTimestampToUse
	) {
		this.log.fine("Logging/writing results (" +
				"file path: "+this.pieceFilePath+", " +
				"file core: "+this.pieceFileNameCore+", " +
				"file extension: "+this.pieceFileNameExtension+")");
		
		long time = cr.getMethodInvocationBeginning(); 
		String qualifyingMethodName = cr.getQualifiedMethodName(); 

		TreeMap<Integer, Long> opcodeCounts 		= new TreeMap<Integer, Long>();
		for(int i = 0; i < cr.getOpcodeCounts().length; i++) {
			if(cr.getOpcodeCounts()[i] != 0) {
				opcodeCounts.put(i, cr.getOpcodeCounts()[i]);
			}
		}
		SortedMap<String, Long> methodCallCounts 	= cr.getMethodCallCounts();
		

		if(time<0) {
			this.log.severe("Wrong time: "+time);//TODO which kind of time is this? //TODO throw exception
			return -1/*null*/;
		}
		if(qualifyingMethodName==null || qualifyingMethodName.equals("")) {
			this.log.severe("Qualifying method name is null or empty, EXITING");
			return -1/*null*/;
		}
		this.log.info("qualifyingMethodName: " + qualifyingMethodName);
		if(methodCallCounts == null) {
			this.log.severe("Method counts hashmap is null... EXITING");
			return -1/*null*/;
		}
		
		//TODO add such checks for array stuff as well...
		
		ASMOpcodesMapper dop = ASMOpcodesMapper.getInstance();
		
		Long totalCountOfAllOpcodes = 0L; //you need longs for that...
		Long totalCountOfAllMethods = 0L; //you need longs for that...
		BigInteger totalCountOfAllOpcodesBig = BigInteger.valueOf(0L);
		BigInteger totalCountOfAllMethodsBig = BigInteger.valueOf(0L);
		
		String 		currentOpcodeString;	// opcode as string
		Long 	currentOpcodeCount = 0L;	// opcode count
		Long 	currentMethodCount = 0L;	// method count
		
		List<Integer> listOpcodes = new ArrayList<Integer>(opcodeCounts.keySet());
		if(listOpcodes.contains(new Integer(-1))){
			this.log.severe("Strangely, list of opcode contains a key with value -1!");
		}
		int upperOpcodeBoundExcl = 256;
		if(this.truncatedUndefindedOpcodes){
			upperOpcodeBoundExcl = 200;
		}
		if(this.writeUnusedOpcodes){
			listOpcodes = new ArrayList<Integer>();//TODO find out why this creates errors.... dop.getAllOpcodesAsList();
			for(int i=0; i<upperOpcodeBoundExcl; i++){
				listOpcodes.add(i);
			}
		}
		int indexOfMinusOneOpcode = listOpcodes.indexOf(new Integer(-1));
		if(indexOfMinusOneOpcode>=0){
			listOpcodes.remove(indexOfMinusOneOpcode);
			this.log.fine("Strange \"-1\" opcode removed from position "+indexOfMinusOneOpcode);
		}
		if(listOpcodes.contains(new Integer(-1))){
			this.log.severe("Strangely, list of opcode contains a key with value -1 " +
					"after all available but uncounted opcodes are included!");
		}
		if(!this.listInvokeOpcodes){
			List<Integer> invokelessListOfOpcodes = new ArrayList<Integer>();
			for(Integer opcode:listOpcodes){
				if(opcode.intValue()<182 || opcode.intValue()>185){
					invokelessListOfOpcodes.add(opcode);
				}
			}
			listOpcodes = invokelessListOfOpcodes;
		}
		if(listOpcodes.contains(new Integer(-1))){
			this.log.severe("Strangely, list of opcode contains a key with value -1 " +
					"after invoke* opcodes are excluded!");
		}
		Integer numberOfValueColumns = listOpcodes.size()+methodCallCounts.size();
		Integer numberOfRows = 1; //here: implicitly TODO document
		List<String> columnTitles = new ArrayList<String>();
		List<Integer> columnTypes = new ArrayList<Integer>();
		List<List<Object>> columns = new ArrayList<List<Object>>();
		
		List<Object> characterisations		= cr.getCharacterisations();
		List<String> characterisationTitles	= cr.getCharacterisationTitles();
		List<Integer> characterisationTypes	= cr.getCharacterisationTypes();

		int nrOfCharacterisationColumns = 0;
		if(characterisations!=null 
				&& characterisationTitles!=null
				&& characterisationTypes!=null
				&&characterisations.size()==characterisationTitles.size()
				&&characterisations.size()==characterisationTypes.size()
				&&characterisations.size()!=0
				&&characterisationTitles.size()!=0
				&&characterisationTypes.size()!=0){
			nrOfCharacterisationColumns = characterisations.size();
			for(int i=0; i<characterisationTitles.size(); i++){
				columnTitles.add(characterisationTitles.get(i)); //replace through Iterator
				columnTypes.add(characterisationTypes.get(i));
				columns.add(new ArrayList<Object>());
			}
//			Integer currType = 0;
//			String currString = "";
//			int highstNumberColumnType;
//			if(writeBooleansAsIntegers){
//				highstNumberColumnType = 4;
//			}else{
//				highstNumberColumnType = 3;
//			}
			for(int i=0; i<characterisations.size(); i++){
//				currType = characterisationTypes.get(i);
//				if(currType>highstNumberColumnType){
//					currString = "\""+characterisations.get(i);
					columns.get(i).add(characterisations.get(i));	
//				}else{
//					
//				}
//				
			}
		}
		@SuppressWarnings("unused")
		int numberOfColumns = nrOfCharacterisationColumns+numberOfValueColumns;
		
		for (int i=0; i<numberOfValueColumns; i++){
			columnTypes.add(new Integer(CSVGenericWriterAndAppender.LONG_COLUMN_TYPE));
		}
		for (int i=0; i<numberOfValueColumns; i++){
			columns.add(new ArrayList<Object>());
		}

//		log.info("\n================ CSVResultWriter ================");
		
		//TODO FIXME TESTME
		Collections.sort(listOpcodes);
		Integer currentOpcode = -1;
		Long currentOpcodeCountLongObject;
		for(int i=0; i<listOpcodes.size(); i++) {
			currentOpcode = listOpcodes.get(i);
			currentOpcodeString = dop.getOpcodeString(currentOpcode);
			if(this.writeOpcodesAsIntegers){
				columnTitles.add(""+currentOpcode);
			}else{
				columnTitles.add(currentOpcodeString);
			}
			currentOpcodeCountLongObject = opcodeCounts.get(currentOpcode);
			if(currentOpcodeCountLongObject!=null){
				currentOpcodeCount = currentOpcodeCountLongObject.longValue();
			}else{
				currentOpcodeCount = 0L;
			}
			columns.get(nrOfCharacterisationColumns+i).add(currentOpcodeCount);
			if((totalCountOfAllOpcodes+currentOpcodeCount)<totalCountOfAllOpcodes){
				this.log.severe("OVERFLOW if adding opcode counts... " +
						"adding skipped... use BigInteger instead!");
			}else{
				totalCountOfAllOpcodes += currentOpcodeCount;
			}
			totalCountOfAllOpcodesBig = totalCountOfAllOpcodesBig.add(
					BigInteger.valueOf(currentOpcodeCount));
		}
		
		List<String> methodSigs = new ArrayList<String>(methodCallCounts.keySet());
		Collections.sort(methodSigs);
		String currentMethodSignature = "";
		int methodsOffset = nrOfCharacterisationColumns+listOpcodes.size();
		for(int i=0; i<methodSigs.size(); i++) {
			currentMethodSignature = methodSigs.get(i);
			columnTitles.add(currentMethodSignature);
			currentMethodCount = methodCallCounts.get(currentMethodSignature);
			columns.get(i+methodsOffset).add(currentMethodCount);
			if(totalCountOfAllMethods + currentMethodCount<totalCountOfAllMethods){
				this.log.severe("OVERFLOW if adding method counts... " +//... or negative addition! --> use assertions!
						"adding skipped... use BigInteger instead!");
			}else{
				totalCountOfAllMethods += currentMethodCount;
			}
			totalCountOfAllMethodsBig = totalCountOfAllMethodsBig.add(BigInteger.valueOf(currentMethodCount));
		}

		if(this.performIntegrityCheckOnInvokeOpcodes){
			Long counts182 = cr.getOpcodeCounts()[182];
			Long counts183 = cr.getOpcodeCounts()[183];
			Long counts184 = cr.getOpcodeCounts()[184];
			Long counts185 = cr.getOpcodeCounts()[185];
			Long totalInvokeOpcodesCounts = 0L;
			if(counts182!=null && counts182>=0){
				totalInvokeOpcodesCounts+=counts182;
			}
			if(counts183!=null && counts183>=0){
				totalInvokeOpcodesCounts+=counts183;
			}
			if(counts184!=null && counts184>=0){
				totalInvokeOpcodesCounts+=counts184;
			}
			if(counts185!=null && counts185>=0){
				totalInvokeOpcodesCounts+=counts185;
			}
			if(totalInvokeOpcodesCounts.longValue()!=totalCountOfAllMethods.longValue()){
				this.log.severe("Integrity check on invoke* opcodes produced a " +
						"DIFFERENT total sum ("+totalInvokeOpcodesCounts+") " +
						"than the number of counted " +
						"method invocations ("+totalCountOfAllMethods+") !!!");
			}else{
				log.info("Integrity check on invoke* opcodes produced a " +
						"SAME total sum than the number of counted " +
						"method invocations, namely "+totalCountOfAllMethods+"!!!");
			}
		}
		@SuppressWarnings("unused")
		long timestampFromArrayWriting = -1;
		if(writeArrayDetailsToSeparateFile){//TODO characterisation currently omitted
			@SuppressWarnings("unused")
			boolean writeDown = true; //TODO if false, write "to the left"
			
			Map<ArrayCreation, Long> newArrayCounts = cr.getArrayCreationCounts();
			
			if(newArrayCounts != null) {
				int numberOfColumns_arr = 3;
				int numberOfRows_arr = newArrayCounts.size();
				List<String> columnTitles_arr = new ArrayList<String>();
				columnTitles_arr.add("Type");//"\"Type\"");
				columnTitles_arr.add("Dimension");//"\"Dimension\"");
				columnTitles_arr.add("Counts");//"\"Counts\"");
				List<Integer> columnTypes_arr = new ArrayList<Integer>();
				columnTypes_arr.add(new Integer(CSVGenericWriterAndAppender.STRING_COLUMN_TYPE));
				columnTypes_arr.add(new Integer(CSVGenericWriterAndAppender.INTEGER_COLUMN_TYPE));
				columnTypes_arr.add(new Integer(CSVGenericWriterAndAppender.LONG_COLUMN_TYPE));
				List<List<Object>> columns_arr = new ArrayList<List<Object>>();
				List<Object> typesColumn_arr = new ArrayList<Object>();
				List<Object> dimColumn_arr = new ArrayList<Object>();
				List<Object> countsColumn_arr = new ArrayList<Object>();
				columns_arr.add(typesColumn_arr);
				columns_arr.add(dimColumn_arr);
				columns_arr.add(countsColumn_arr);
				
				//TODO check if aggregated...
				for(Entry<ArrayCreation, Long> e : newArrayCounts.entrySet()) {
					typesColumn_arr.add(e.getKey().getTypeDesc());
					dimColumn_arr.add(e.getKey().getNumberOfDimensions());
					countsColumn_arr.add(e.getValue());//hier auch potentiell Ueberlauf
//					log.info("SKIPPED new array of type '" + newArrayTypes[i] + "'" 
//							+ (newArrayDims[i] > 0 ? ", dim " + newArrayDims[i] : "")
//							+ ": " + newArrayCounts[i]);
				}
				CSVGenericWriterAndAppender csvwap = new CSVGenericWriterAndAppender();
				timestampFromArrayWriting = csvwap.writeColumns(
						numberOfColumns_arr, 
						numberOfRows_arr, 
						columnTitles_arr, 
						columnTypes_arr, 
						columns_arr, 
						true, 	//egal hier... writeBooleansAsIntegers, 
						false, 	//egal hier... writeMasterFile, 
						false, 	//egal hier... appendMasterFile,
						true, 	//writePieceFile, 
						true, 	//appendPieceFile, 
						entriesSeparationChar, 	//entriesSeparationChar, 
						"egal", //masterFileNameWithPath, 
						pieceFilePath, 
						pieceFileNameCore, 
						"arrays."+pieceFileNameExtension,
						usePrevTimestamp,
						prevTimestampToUse);
				
			}else{
				log.severe("One of the structures with new array information was null...");
			}

		}else{
			log.info("Not writing array initialisations at the moment!");
//			log.severe("Array initialisations are important for LZW, however!");
		}
		CSVGenericWriterAndAppender csvwap = new CSVGenericWriterAndAppender();
//		boolean writeBooleansAsIntegers = true;
		boolean writeMasterFile = false;
		boolean appendMasterFile = false;
		boolean writePieceFile = true;
		boolean appendPieceFile = false;
//		char entriesSeparationChar = ';';
		String masterFileNameWithPath = "";
//		String pieceFilePath = ".";
//		String pieceFileNameCore = "CBSE";
//		String pieceFileNameExtension = "csv";
		if(appendGrandTotalAtTheEnd){
			columnTitles.add("GRANDTOTAL");
			columnTypes.add(new Integer(CSVGenericWriterAndAppender.LONG_COLUMN_TYPE));
			long grandTotal = totalCountOfAllOpcodes+totalCountOfAllMethods;
			@SuppressWarnings("unused")
			BigInteger grandTotalBig =  totalCountOfAllOpcodesBig.add(totalCountOfAllMethodsBig);//TODO use 
			List<Object> grandTotalColumn = new ArrayList<Object>();
			grandTotalColumn.add(grandTotal);//TODO replace through grand total...!
			columns.add(grandTotalColumn);
		}

//		boolean usePrevTimestamp = false;
//		if(timestampFromArrayWriting>0){
//			usePrevTimestamp = true;
//		}
		long result = csvwap.writeColumns(
				columnTitles.size(), 
				numberOfRows, 
				columnTitles, 
				columnTypes, 
				columns, 
				writeBooleansAsIntegers, 
				writeMasterFile, 
				appendMasterFile,
				writePieceFile, 
				appendPieceFile, 
				entriesSeparationChar, 
				masterFileNameWithPath, 
				pieceFilePath, 
				pieceFileNameCore, 
				pieceFileNameExtension,
				usePrevTimestamp,
				prevTimestampToUse //ignored if usePrevTimestamp_a=false
			); 
		this.lastWrittenFile = csvwap.getLastWrittenFile();
		
		return result;
//		System.out.println("====================================================");
//		log.info(totalCountOfAllOpcodes + " opcodes of "+opcodeCounts.size() + " different types were counted.\n");
//		log.info(totalCountOfAllMethods + " methods of "+methodCallCounts.size() + " different types were counted.\n");
//		System.out.println("====================================================");
//		System.out.println("\n");
//		System.out.println("\n");
//		System.out.println("\n");
	
	}

	/**
	 * @return  the {@link File} where the last csv file was written.
	 */
	public File getLastWrittenFile() {
		return this.lastWrittenFile;
	}

}
