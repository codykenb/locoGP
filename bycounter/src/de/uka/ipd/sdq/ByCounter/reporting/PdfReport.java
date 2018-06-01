package de.uka.ipd.sdq.ByCounter.reporting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import com.lowagie.text.Chapter;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Section;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import de.uka.ipd.sdq.ByCounter.execution.CountingResultBase;
import de.uka.ipd.sdq.ByCounter.execution.CountingResultCollector;
import de.uka.ipd.sdq.ByCounter.execution.MethodExecutionRecord;
import de.uka.ipd.sdq.ByCounter.utils.FullOpcodeMapper;

/**
 * {@link ICountingResultWriter} that writes reports in the pdf format.
 */
public class PdfReport implements ICountingResultWriter {
	private static final int SPACING_TABLE = 12;
	private static final Font FONT_TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 36);
	private static final Font FONT_CHAPTER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
	private static final Font FONT_HEADER_METHOD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);

	private static float MARGIN = 50;
	private int chapterCounter = 1;
	public Configuration configuration;

	/**
	 * Configuration options for the generation of a Pdf report.
	 */
	public static class Configuration {
		public boolean printZeros = false;
	}

	private Logger log;
	private File lastWrittenFile;

	public PdfReport() {
		this.configuration = new Configuration();
		this.log = Logger.getLogger(PdfReport.class.getCanonicalName());
	}

	public void generatePdf(CountingResultBase[] cResults, MethodExecutionRecord methodExecution) throws DocumentException {
		Document doc = new Document(PageSize.A4, MARGIN, MARGIN, MARGIN, MARGIN);


		SimpleDateFormat dateFormat = new SimpleDateFormat("y.M.d.h'h'm'm's's'");
		//2011.04.28.16h58m16s.edu.kit.ipd.sdq.MyTestClass.txt
		String fileName =
			dateFormat.format(new Date(System.currentTimeMillis()))
			+ "." + methodExecution.canonicalClassName
			+ ".pdf";
		System.out.println(fileName);
		openFile(doc, fileName);
		doc.open();

		// Header
		doc.addTitle("ByCounter Report");
		doc.add(new Paragraph("ByCounter Report", FONT_TITLE));
		Date date = new Date(System.currentTimeMillis());
		String dateString = DateFormat.getInstance().format(date);
		doc.add(new Paragraph("generated on " + dateString));
		Paragraph placeHolder = new Paragraph();
		placeHolder.setSpacingBefore(SPACING_TABLE);
		doc.add(placeHolder);

		Paragraph p = createExecutedMethods(methodExecution);
		doc.add(p);

		// Results
		Chapter chResults = new Chapter(
				new Paragraph("Results", FONT_CHAPTER), chapterCounter++);
		for(CountingResultBase r : cResults) {
			Section s = chResults.addSection(
					new Paragraph(r.getQualifiedMethodName(), FONT_HEADER_METHOD));
			s.add(new Paragraph("Opcode execution counts"));
			s.add(createBytecodeTable(r.getOpcodeCounts()));
			s.add(new Paragraph("Method call counts"));
			s.add(createMethodCallTable(r.getMethodCallCounts()));
		}

		doc.add(chResults);

		// System information
		Chapter chSystem = new Chapter(
				new Paragraph("Execution System", FONT_CHAPTER), chapterCounter++);
//		chSystem.add(new Paragraph(SystemEnvironment.getSystemEnvironment().getHostname()));
		chSystem.add(createSystemInformationTable());
		doc.add(chSystem);
		doc.close();
		this.lastWrittenFile = new File(fileName);
	}

	private Paragraph createExecutedMethods(MethodExecutionRecord methodExecution) {
		Paragraph p = new Paragraph();
		int numExecutedMethods = methodExecution.methodsCalled.size();
		if(numExecutedMethods == 1) {
			p.add(new Paragraph("The following method was executed"));
		} else {
			p.add(new Paragraph("The following methods were executed"));
		}
		List list = new List(false);
		for(int i = 0; i < numExecutedMethods; i++) {
			Paragraph p1 = new Paragraph();
			p1.add(new Paragraph(methodExecution.methodsCalled.get(i).getCanonicalMethodName()));
			p1.add(new Paragraph("Parameters: " + Arrays.toString(methodExecution.methodCallParams.get(i))));
			list.add(new ListItem(p1));
		}
		p.add(list);

		return p;
	}

	/**
	 * @return A table that lists {@link System#getProperties()}.
	 * @throws DocumentException
	 */
	private PdfPTable createSystemInformationTable() throws DocumentException {

		// TODO: sort by name
		PdfPTable table = new PdfPTable(2);
		setDefaultTableOptions(table);
		table.setWidths(new float[] {0.4f, 0.6f});

		Properties properties = System.getProperties();
		ArrayList<Object> sortedList = new ArrayList<Object>(properties.keySet());
		Collections.sort(sortedList, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				String key1 = (String)o1;
				String key2 = (String)o2;
				return key1.compareTo(key2);
			}
		});

		for(Object key : sortedList) {
			table.addCell(key.toString());
			table.addCell(properties.getProperty(key.toString()));
		}
		return table;
	}

	private void openFile(Document doc, String fileName) throws DocumentException {
		try {
			PdfWriter writer = PdfWriter.getInstance(
					doc,
					new FileOutputStream(fileName));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Configure the look of the given table to a standard.
	 * @param table Table to modify.
	 * @throws DocumentException
	 */
	private void setDefaultTableOptions(PdfPTable table) throws DocumentException {
		table.setHeaderRows(1);
		table.setSpacingBefore(SPACING_TABLE);
		table.setSpacingAfter(2*SPACING_TABLE);
		table.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);
		table.setWidthPercentage(100);
		table.setWidths(new float[] {0.92f, 0.08f});
	}

	/**
	 * You can use {@link Configuration#printZeros} to enable/disable rows with
	 * a count of 0.
	 * @param opcodeCounts
	 * @return A table listing the execution counts for different opcodes.
	 * @throws DocumentException
	 */
	private PdfPTable createBytecodeTable(long[] opcodeCounts) throws DocumentException {
		PdfPTable table = new PdfPTable(2);
		setDefaultTableOptions(table);
		table.addCell("Opcode");
		table.addCell("Count");


		int numberOfOpcodesWithNonzeroFrequencies=0;
		long totalCountOfAllOpcodes = 0;
		for(int i = 0; i < CountingResultBase.MAX_OPCODE; i++) {
			String currentOpcodeString = FullOpcodeMapper.getMnemonicOfOpcode(i);
			long currentOpcodeCount 	= opcodeCounts[i];
			if(currentOpcodeCount!=0 || configuration.printZeros){
				table.addCell(currentOpcodeString);
				table.addCell(String.valueOf(currentOpcodeCount));
			}
			if((totalCountOfAllOpcodes+currentOpcodeCount)<totalCountOfAllOpcodes){
				log.severe("OVERFLOW while adding opcode counts... use BigInteger instead");
			}else{
				totalCountOfAllOpcodes += currentOpcodeCount;
				if(currentOpcodeCount>0){
					numberOfOpcodesWithNonzeroFrequencies++;
				}
			}
		}
		return table;
	}


	/**
	 * @param methodCallCounts As in {@link CountingResultBase}.
	 * @return A table listing the count of method calls.
	 * @throws DocumentException
	 */
	private PdfPTable createMethodCallTable(Map<String, Long> methodCallCounts) throws DocumentException {
		PdfPTable table = new PdfPTable(2);
		setDefaultTableOptions(table);
		table.addCell("Method");
		table.addCell("Call Count");
		for(Entry<String, Long> e : methodCallCounts.entrySet()) {
			table.addCell(e.getKey());
			table.addCell(String.valueOf(e.getValue()));
		}

		return table;
	}

	public long writeResultToFile(CountingResultBase cr, boolean usePrevTimestamp,
			long prevTimestampToUse) {
		try {
			this.generatePdf(new CountingResultBase[] {cr}, CountingResultCollector.getInstance().getLastMethodExecutionDetails());
		} catch (DocumentException e) {
			throw new RuntimeException(e);
		}
		return -1;
	}

	public File getLastWrittenFile() {
		return this.lastWrittenFile;
	}
}
