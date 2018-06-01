package de.uka.ipd.sdq.ByCounter.reporting;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * For future use
 *
 * @author Michael Kuperberg
 * @author Martin Krogmann
 * @since 1.0
 * @version 1.2
*/
public class CSVGenericWriterAndAppender {
	
	public static final int BOOLEAN_COLUMN_TYPE = 4;
	public static final int DOUBLE_COLUMN_TYPE = 0;
	public static final int FLOAT_COLUMN_TYPE = 1;
	public static final int INTEGER_COLUMN_TYPE = 3;
	public static final int LONG_COLUMN_TYPE = 2;
	public static final int STRING_COLUMN_TYPE = 5;
	
	public static final String[] TYPES_BY_NAME = new String[]{
		"DOUBLE_COLUMN_TYPE",
		"FLOAT_COLUMN_TYPE",
		"LONG_COLUMN_TYPE",
		"INTEGER_COLUMN_TYPE",
		"BOOLEAN_COLUMN_TYPE",
		"STRING_COLUMN_TYPE"};
	
	public static final boolean isNumType(int type){
		if(type<=3){
			return true;
		}else{
			return false;
		}
	}
	
	
	/**
	 * Serves as the tast case...
	 * @param args
	 */
	public static void main(String[] args){
		CSVGenericWriterAndAppender csvwap = new CSVGenericWriterAndAppender();
		csvwap.test();
	}
	
	/**
	 * Used in decising whether to write booleans (type value 4, see constants of this class) as ints or not
	 */
	private int highestNumberColumnType;

	private Logger log = Logger.getLogger(this.getClass().getCanonicalName());
	
	/**
	 * Used in the file names, see constructor for details
	 */
	private long timestamp;
	private File lastWrittenFile;

	/**
	 * @return the {@link File} where the last csv file was written.
	 */
	public File getLastWrittenFile() {
		return this.lastWrittenFile;
	}


	/**
	 * Timestamp set to System.nanoTime();
	 */
	public CSVGenericWriterAndAppender() {
		super();
		this.timestamp = System.nanoTime();
	}
	 
	private void test() {
		int numberOfColumns = 3;
		int numberOfRows = 4;
		List<String> columnTitles = new ArrayList<String>();
		columnTitles.add("Column 1"); columnTitles.add("Column 2");columnTitles.add("Result");
		List<Integer> columnTypes = new ArrayList<Integer>();
		columnTypes.add(BOOLEAN_COLUMN_TYPE); columnTypes.add(BOOLEAN_COLUMN_TYPE);columnTypes.add(INTEGER_COLUMN_TYPE);
		List<List<Object>> columns = new ArrayList<List<Object>>();
		List<Object> column1 = new ArrayList<Object>();
		column1.add(new Boolean(true));
		column1.add(new Boolean(true));
		column1.add(new Boolean(false));
		column1.add(new Boolean(false));
		List<Object> column2 = new ArrayList<Object>();
		column2.add(new Boolean(true));
		column2.add(new Boolean(false));
		column2.add(new Boolean(true));
		column2.add(new Boolean(false));
		List<Object> column3 = new ArrayList<Object>();
		column3.add(new Integer(1));
		column3.add(new Integer(0));
		column3.add(new Integer(0));
		column3.add(new Integer(0));
		columns.add(column1);
		columns.add(column2);
		columns.add(column3);
		this.writeColumns(numberOfColumns, numberOfRows, columnTitles, columnTypes, columns, 
				true, 				//writeBooleansAsIntegers, 
				true, 				//writeMasterFile, 
				true, 				//appendMasterFile, 
				true, 				//writePieceFile, 
				false, 				//appendPieceFile, 
				';', 				//entriesSeparationChar
				"TestMaster.csv",	//masterFileNameWithPath, 
				".", 				//pieceFilePath, 
				"TestPiece", 		//pieceFileNameCore, 
				"csv", 				//pieceFileNameExtension
				false,
				-1
				);
//		this.writeColumns(numberOfColumns, numberOfRows, columnTitles, columnTypes, columns, 
//				false, //writeBooleansAsIntegers, 
//				true, //writeMasterFile, 
//				true, //appendMasterFile, 
//				true, //writePieceFile, 
//				false, //appendPieceFile, 
//				',', //entriesSeparationChar
//				"TestMaster.csv", //masterFileNameWithPath, 
//				".", //pieceFilePath, 
//				"TestPiece", //pieceFileNameCore, 
//				"csv" //pieceFileNameExtension
//				);
	}
	
	/**
	 * @param numberOfColumns
	 * @param numberOfRows
	 * @param columnTitles
	 * @param columnTypes
	 * @param columns
	 * @param writeBooleansAsIntegers
	 * @param writeMasterFile
	 * @param appendMasterFile
	 * @param writePieceFile
	 * @param appendPieceFile
	 * @param entriesSeparationChar
	 * @param masterFileNameWithPath
	 * @param pieceFilePath
	 * @param pieceFileNameCore
	 * @param pieceFileNameExtension
	 * @param usePrevTimestamp
	 * @param prevTimestampToUse
	 * @return timestamp that was used for writing the CSV file (if usePrevTimestamp==true, then prevTimestampToUse is returned)
	 */
	public long writeColumns(//TODO make a non-typed version for simpler creating of columns by callers
			int numberOfColumns,
			int numberOfRows,
			List<String> columnTitles,
			List<Integer> columnTypes,
			List<List<Object>> columns,
			boolean writeBooleansAsIntegers,
			boolean writeMasterFile,
			boolean appendMasterFile,
			boolean writePieceFile,
			boolean appendPieceFile,
			char entriesSeparationChar,
			String masterFileNameWithPath,
			String pieceFilePath,
			String pieceFileNameCore,//supplemented by timestamp...
			String pieceFileNameExtension,
			boolean usePrevTimestamp,
			long prevTimestampToUse
			){
		
		if(usePrevTimestamp){
			this.timestamp = prevTimestampToUse; //TODO add plausibility checks for past times, etc. ...
		}else{
			//maintaining the prev. value --> debug
		}
		
		if(writeBooleansAsIntegers){
			this.highestNumberColumnType = 4;
		}else{
			this.highestNumberColumnType = 3;
		}
		
		if(columns==null || columns.size()==0){
			this.log.severe("No data (i.e., nocolumns)");
			return -1;
		}
		
		if(numberOfColumns!=columns.size()
				|| numberOfColumns!=columnTitles.size()
				|| numberOfColumns!=columnTypes.size()
				){
			this.log.severe("Wrong number of columns");
			return -1;
		}
		
		if(numberOfRows!=columns.get(0).size()){
			this.log.severe("Wrong number of rows");
			return -1;
		}
		
		FileWriter fwMaster = null;
		FileWriter fwPiece  = null;
		try {
//			log.fine(masterFile.length());
			StringBuffer sb = new StringBuffer();
			for(String columnTitle:columnTitles){
				sb.append("\""+columnTitle+"\""+entriesSeparationChar);
			}
			sb.append("\n");
			this.log.info("Column titles: "+sb.toString());
			
			if(writeMasterFile){
				File masterFile = new File(masterFileNameWithPath);
				this.log.fine("Master file path: "+masterFile.getAbsolutePath());
				boolean masterFileExists = masterFile.exists();
				fwMaster = new FileWriter(masterFileNameWithPath, appendMasterFile);
				if(!masterFileExists){
					this.log.fine("Master file does not exist");
					fwMaster.append(sb.toString());
				}else{
					this.log.fine("Master file already exists");
				}
				
			}
			
			if(writePieceFile){
				String piecePath = 
					"."+File.separator+pieceFilePath+File.separator+
					pieceFileNameCore+"."+this.timestamp+"."+
					pieceFileNameExtension;
				File pieceFile = new File(piecePath);
				this.log.fine("Piece file path: "+pieceFile.getAbsolutePath());
				boolean pieceFileExists = pieceFile.exists();
				fwPiece = new FileWriter(piecePath, appendPieceFile);
				if(!pieceFileExists){
					this.log.fine("Piece file did not exist (yet), creating header row");
					/*if(appendPieceFile) */
					fwPiece.append(sb.toString());
				}else{
					this.log.fine("Piece file already exists");
				}
				this.lastWrittenFile = pieceFile;
			}

			//now writing the rows
			Boolean tempBool = false;
			sb = new StringBuffer();
//			Integer[] columnTypesArray = Arrays.fcolumnTypes
			for(int rowIndex=0; rowIndex<columns.get(0).size(); rowIndex++){
				for(int columnIndex=0; columnIndex<columns.size(); columnIndex++){
					int columnType = columnTypes.get(columnIndex);
					if(columnType>this.highestNumberColumnType){
						sb.append("\"");
					}
					if(      columnType==DOUBLE_COLUMN_TYPE){
						sb.append(((Double) columns.get(columnIndex).get(rowIndex)).doubleValue()+""+entriesSeparationChar);
					}else if(columnType==FLOAT_COLUMN_TYPE){
						sb.append(((Float) columns.get(columnIndex).get(rowIndex)).floatValue()+""+entriesSeparationChar);
					}else if(columnType==LONG_COLUMN_TYPE){
						Object something = columns.get(columnIndex).get(rowIndex);
						long longSomething = 0L;
						if(something!=null){
							longSomething = ((Long) something).longValue();
						}else{
							this.log.severe("TODO CSVWriterAndAppender cannot write null " +
									"(columnIndex="+columnIndex+
									", rowIndex="+rowIndex+")");
						}
						sb.append(longSomething+""+entriesSeparationChar);
					}else if(columnType==INTEGER_COLUMN_TYPE){
						sb.append(((Integer) columns.get(columnIndex).get(rowIndex)).intValue()+""+entriesSeparationChar);
					}else if(writeBooleansAsIntegers && columnType==BOOLEAN_COLUMN_TYPE){
						tempBool = (Boolean) columns.get(columnIndex).get(rowIndex);
						if(tempBool){
							sb.append("1"+entriesSeparationChar);
						}else{
							sb.append("0"+entriesSeparationChar);
						}
					}else{
						sb.append(columns.get(columnIndex).get(rowIndex).toString());
					}
					if(columnType>this.highestNumberColumnType){
						sb.append("\""+entriesSeparationChar);
					}
				}
				sb.append("\n");
//				sb.append(
//						"\""+platfromDesc.get(rowIndex)+"\""+","+
//						"\""+jvmConfDesc.get(rowIndex)+"\""+","+
//						"\""+jitConfDesc.get(rowIndex)+"\""+","+
//						"\""+digestAlgorithm.get(rowIndex)+"\""+","+
//						nrOfMeasurements.get(rowIndex)+"\""+","+
//						"\""+digestImmediately.get(rowIndex)+"\""+","+
//						inputSize.get(rowIndex)+","+
//						nrOfDifferentRandomInputs.get(rowIndex));
//				Long[] valuesToWrite = values.get(rowIndex);
//				for(int w = 0; w<valuesToWrite.length; w++){
//					sb.append(","+valuesToWrite[w]);
//				}
//				sb.append("\n");
			}
			if(fwMaster!=null && writeMasterFile) fwMaster.append(sb.toString());
			if(fwPiece!=null && writePieceFile) fwPiece.append(sb.toString());
			
			if(fwMaster!=null && writeMasterFile) fwMaster.close();
			if(fwPiece!=null && writePieceFile) fwPiece.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return this.timestamp;
	}
	/**
	 * @param numberOfColumns
	 * @param numberOfRows
	 * @param columnTitles
	 * @param columnTypes
	 * @param columns
	 * @param writeBooleansAsIntegers
	 * @param writeMasterFile
	 * @param appendMasterFile
	 * @param writePieceFile
	 * @param appendPieceFile
	 * @param entriesSeparationChar
	 * @param masterFileNameWithPath
	 * @param pieceFilePath
	 * @param pieceFileNameCore
	 * @param pieceFileNameExtension
	 * @param usePrevTimestamp
	 * @param prevTimestampToUse
	 * @return timestamp that was used for writing the CSV file (if usePrevTimestamp==true, then prevTimestampToUse is returned)
	 */
	public long writeColumns_arr(//TODO make a non-typed version for simpler creating of columns by callers
			int numberOfColumns,
			int numberOfRows,
			String[] columnTitles,
			Integer[] columnTypes,
			Object[][] columns,
			boolean writeBooleansAsIntegers,
			boolean writeMasterFile,
			boolean appendMasterFile,
			boolean writePieceFile,
			boolean appendPieceFile,
			char entriesSeparationChar,
			String masterFileNameWithPath,
			String pieceFilePath,
			String pieceFileNameCore,//supplemented by timestamp...
			String pieceFileNameExtension,
			boolean usePrevTimestamp,
			long prevTimestampToUse
			){
		
		if(usePrevTimestamp){
			this.timestamp = prevTimestampToUse; //TODO add plausibility checks for past times, etc. ...
		}else{
			//maintaining the prev. value --> debug
		}
		
		if(writeBooleansAsIntegers){
			this.highestNumberColumnType = 4;//TODO rework this
		}else{
			this.highestNumberColumnType = 3;
		}
		
		if(columns==null || columns.length==0){
			this.log.severe("No data (i.e., no columns)");
			return -1;
		}
		
		if(numberOfColumns!=columns.length
				|| numberOfColumns!=columnTitles.length
				|| numberOfColumns!=columnTypes.length
				){
			this.log.severe("Wrong number of columns");
			return -1;
		}
		
		if(numberOfRows!=columns[0].length){
			this.log.severe("Wrong number of rows");
			return -1;
		}
		
		FileWriter fwMaster = null;
		FileWriter fwPiece  = null;
		try {
//			log.fine(masterFile.length());
			StringBuffer sb = new StringBuffer();
			for(String columnTitle:columnTitles){
				sb.append("\""+columnTitle+"\""+entriesSeparationChar);
			}
			sb.append("\n");
			this.log.info("Column titles: "+sb.toString());
			
			if(writeMasterFile){
				File masterFile = new File(masterFileNameWithPath);
				this.log.fine("Master file path: "+masterFile.getAbsolutePath());
				boolean masterFileExists = masterFile.exists();
				fwMaster = new FileWriter(masterFileNameWithPath, appendMasterFile);
				if(!masterFileExists){
					this.log.fine("Master file does not exist");
					fwMaster.append(sb.toString());
				}else{
					this.log.fine("Master file already exists");
				}
				
			}
			
			if(writePieceFile){
				String piecePath = 
					"."+File.separator+pieceFilePath+File.separator+
					pieceFileNameCore+"."+this.timestamp+"."+
					pieceFileNameExtension;
				File pieceFile = new File(piecePath);
				this.log.fine("Piece file path: "+pieceFile.getAbsolutePath());
				boolean pieceFileExists = pieceFile.exists();
				fwPiece = new FileWriter(piecePath, appendPieceFile);
				if(!pieceFileExists){
					this.log.fine("Piece file did not exist (yet), creating header row");
					/*if(appendPieceFile) */
					fwPiece.append(sb.toString());
				}else{
					this.log.fine("Piece file already exists");
				}
			}

			//now writing the rows
			Boolean tempBool = false;
			sb = new StringBuffer();
//			Integer[] columnTypesArray = Arrays.fcolumnTypes
			for(int rowIndex=0; rowIndex<columns[0].length; rowIndex++){
				for(int columnIndex=0; columnIndex<columns.length; columnIndex++){
					int columnType = columnTypes[columnIndex];
					if(columnType>this.highestNumberColumnType){
						sb.append("\"");
					}
					if(      columnType==DOUBLE_COLUMN_TYPE){
						sb.append(((Double) columns[columnIndex][rowIndex]).doubleValue()+""+entriesSeparationChar);
					}else if(columnType==FLOAT_COLUMN_TYPE){
						sb.append(((Float) columns[columnIndex][rowIndex]).floatValue()+""+entriesSeparationChar);
					}else if(columnType==LONG_COLUMN_TYPE){
						Object something = columns[columnIndex][rowIndex];
						long longSomething = 0L;
						if(something!=null){
							longSomething = ((Long) something).longValue();
						}else{
							log.severe("TODO CSVWriterAndAppender cannot write null " +
									"(columnIndex="+columnIndex+
									", rowIndex="+rowIndex+")");
						}
						sb.append(longSomething+""+entriesSeparationChar);
					}else if(columnType==INTEGER_COLUMN_TYPE){
						sb.append(((Integer) columns[columnIndex][rowIndex]).intValue()+""+entriesSeparationChar);
					}else if(writeBooleansAsIntegers && columnType==BOOLEAN_COLUMN_TYPE){
						tempBool = (Boolean) columns[columnIndex][rowIndex];
						if(tempBool){
							sb.append("1"+entriesSeparationChar);
						}else{
							sb.append("0"+entriesSeparationChar);
						}
					}else{
						sb.append(columns[columnIndex][rowIndex].toString());
					}
					if(columnType>highestNumberColumnType){
						sb.append("\""+entriesSeparationChar);
					}
				}
				sb.append("\n");
//				sb.append(
//						"\""+platfromDesc.get(rowIndex)+"\""+","+
//						"\""+jvmConfDesc.get(rowIndex)+"\""+","+
//						"\""+jitConfDesc.get(rowIndex)+"\""+","+
//						"\""+digestAlgorithm.get(rowIndex)+"\""+","+
//						nrOfMeasurements.get(rowIndex)+"\""+","+
//						"\""+digestImmediately.get(rowIndex)+"\""+","+
//						inputSize.get(rowIndex)+","+
//						nrOfDifferentRandomInputs.get(rowIndex));
//				Long[] valuesToWrite = values.get(rowIndex);
//				for(int w = 0; w<valuesToWrite.length; w++){
//					sb.append(","+valuesToWrite[w]);
//				}
//				sb.append("\n");
			}
			if(fwMaster!=null && writeMasterFile) fwMaster.append(sb.toString());
			if(fwPiece!=null && writePieceFile) fwPiece.append(sb.toString());
			
			if(fwMaster!=null && writeMasterFile) fwMaster.close();
			if(fwPiece!=null && writePieceFile) fwPiece.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return this.timestamp;
	}
}
