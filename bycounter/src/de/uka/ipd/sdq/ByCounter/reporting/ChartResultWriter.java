package de.uka.ipd.sdq.ByCounter.reporting;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.FontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import de.uka.ipd.sdq.ByCounter.execution.CountingResultBase;
import de.uka.ipd.sdq.ByCounter.utils.IByCounterConstants;

/**
 * Utility for exporting JFreeChart objects as .pdf files. Uses itext.
 * @author Michael Kuperberg (?)
 * @author Martin Krogmann (?)
 * @since 0.1
 * @version 1.2
 */
public class ChartResultWriter implements ICountingResultWriter{

	/** Filetypes for chart files: jpg */
	public static final int FILETYPE_JPG = 2;

	/** Filetypes for chart files: pdf */
	public static final int FILETYPE_PDF = 0;
	
	/** Filetypes for chart files: png */
	public static final int FILETYPE_PNG = 1;
	
	/**
	 * Save a JFreeChart to a File as a .pdf.
	 * From JFreeChart Developer guide.
	 * @param file File to write to.
	 * @param chart JFreeChart object to draw.
	 * @param width Width of the object.
	 * @param height Height of the object.
	 * @param mapper Fontmapper to interface with itext. (Try DefaultFontMapper).
	 * @throws IOException Thrown when errors happen while writing.
	 */
	public static void saveChartAsPDF(File file, JFreeChart chart, int width,
			int height, FontMapper mapper) throws IOException {
		OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
		writeChartAsPDF(out, chart, width, height, mapper);
		out.close();
	}

	/**
	 * Writes a JFreeChart object to a <code>OutputStream</code>.
	 * From JFreeChart Developer guide.
	 * @param out Output stream.
	 * @param chart Chart to write.
	 * @param width Width of the graphic.
	 * @param height Height of the graphic.
	 * @param mapper Fontmapper to interface with itext.
	 * @throws IOException Is thrown by the OutputStream.
	 */
	public static void writeChartAsPDF(OutputStream out, JFreeChart chart,
			int width, int height, FontMapper mapper) throws IOException {
		// variable document size; for A4 size change this
		Rectangle pagesize = new Rectangle(width, height);
		Document document = new Document(pagesize, 50, 50, 50, 50);
		try {
			PdfWriter writer = PdfWriter.getInstance(document, out);
			// these show up in the document properties - should be changed for use outside this project
			document.addAuthor(IByCounterConstants.PRODUCT_NAME+" V"+IByCounterConstants.PRODUCT_VERSION);
			document.addSubject("Performance Results"); //TODO MK add method parameters for more precise description passing 
			document.open();
			// draw to the pdf
			PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tp = cb.createTemplate(width, height);
			Graphics2D g2 = tp.createGraphics(width, height, mapper);
			Rectangle2D r2D = new Rectangle2D.Double(0, 0, width, height);
			chart.draw(g2, r2D);
			g2.dispose();
			cb.addTemplate(tp, 0, 0);
		} catch (DocumentException de) {
			System.err.println(de.getMessage());
		}
		document.close();
	}
	
	/**
	 * The directory in which charts are saved.
	 */
	private String chartDir;

	/**
	 * The chart filename.
	 */
	private String chartName;
	
	/**
	 * see http://en.wikipedia.org/wiki/Data_log
	 */
	private Logger log = Logger.getLogger(this.getClass().getCanonicalName());

	/**
	 * @param chartDir The directory in which charts are saved.
	 * @param chartName The filename for the chart.
	 */
	public ChartResultWriter(String chartDir, String chartName) {
		this.setChartDir(chartDir);
		this.setChartName(chartName);
	}

	/**TODO
	 * @param datasetTimestampForSavingChart
	 * @param dataset
	 * @param savePNGchart
	 * @param savePDFchart
	 * @param chartXsize
	 * @param chartYsize
	 * @param qualifyingMethodName
	 * @param pathForChartSaving
	 * @return The {@link JFreeChart} created using the specified parameters.
	 */
	public synchronized JFreeChart createAndSaveChart(
		long datasetTimestampForSavingChart,
		DefaultCategoryDataset dataset,
		boolean savePNGchart,
		boolean savePDFchart,
		int chartXsize,
		int chartYsize,
		String qualifyingMethodName,
		String pathForChartSaving
		) {
		JFreeChart chart = ChartFactory.createBarChart(
				"Bytecode and method counts for method "+qualifyingMethodName, // chart title
				"Bytecode instructions", // domain axis label
				"Counts", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				false, // include legend
				true, // tooltips?
				false // URLs?
				);
		chart.setBackgroundPaint(Color.white);
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
//		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//		rangeAxis.setUpperMargin(0.15);
		CategoryItemRenderer renderer = plot.getRenderer();
		//renderer.setLabelGenerator(new LabelGenerator(50.0));
		renderer.setBaseItemLabelFont(new Font("Serif", Font.PLAIN, 20));
		renderer.setBaseItemLabelsVisible(true);

		File file = new File(pathForChartSaving+
				File.separator+"Counts."+
				qualifyingMethodName+
				"."+
				datasetTimestampForSavingChart+
				".pdf");
		if(!file.exists()){
			this.log.fine("Creating path "+pathForChartSaving);
			new File(pathForChartSaving).mkdirs();
			
		}else{
			this.log.warning("Overwriting "+file);
		}
			
		if(savePDFchart){
			try {
				ChartResultWriter.saveChartAsPDF(
						file, 
						chart, 
						chartXsize, 
						chartYsize, 
						new DefaultFontMapper());
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}
		if(savePNGchart){
			try {
				ChartUtilities.saveChartAsPNG(file, chart, chartXsize, chartYsize);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return chart;
	}

	/**
	 * @return the chartDir
	 */
	public String getChartDir() {
		return chartDir;
	}


	/**
	 * @return the chartName
	 */
	public String getChartName() {
		return chartName;
	}

	/**
	 * @param chartDir the chartDir to set
	 */
	public void setChartDir(String chartDir) {
		this.chartDir = chartDir;
	}

	/**
	 * @param chartName the chartName to set
	 */
	public void setChartName(String chartName) {
		this.chartName = chartName;
	}

	/** (non-Javadoc)
	 * @see ICountingResultWriter#writeResultToFile(de.uka.ipd.sdq.ByCounter.execution.CountingResultBase, boolean, long)
	 */
	public long writeResultToFile(CountingResultBase cr,
			boolean usePrevTimestamp,
			long prevTimestampToUse) {

		this.log.info("Logging/writing results (" + 
				this.getChartDir() + File.separator + this.getChartName() +")");
		
		// TODO: add method counts to the chart
//		for(String methodCall : cr.getMethodCallCounts().keySet()) {

		SimpleHistogramDataset xyDataset = new SimpleHistogramDataset("Opcode counts");
		for(int opcode = 0; opcode < cr.getOpcodeCounts().length; opcode++) {
			SimpleHistogramBin bin = new SimpleHistogramBin(opcode-0.5, opcode);
			bin.setItemCount((int)cr.getOpcodeCounts()[opcode]);
			xyDataset.addBin(bin);
		}
		this.log.fine("Dataset: ");
		this.log.fine(xyDataset.toString());
		

		JFreeChart chart = ChartFactory.createHistogram(
		                      "CountingResult from timestamp " + prevTimestampToUse,// Title
		                      "Opcode",						// X-Axis label
		                      "Number of Occurrences",		// Y-Axis label
		                      xyDataset,					// Dataset
		                      PlotOrientation.VERTICAL,
		                      true,					// Show legend
		                      false,					// no tooltips
		                      false					// no urls
		                     );

		// write the file as a pdf
		this.writeToChartFile(chart, 800, 600, FILETYPE_PDF);
		
		return -1;
	}

	/**
	 * Write a simple XY-Chart to the charts directory.
	 * @param title Title of the chart.
	 * @param xyDataset The chart to draw.
	 * @param width Width of the chart.
	 * @param height Height of the chart.
	 * @param filetype Type of the output file. See FILETYPE_* constants.
	 */
	private void writeToChartFile(JFreeChart chart, 
			int width, int height, int filetype) {

		try {
			// make sure the directory CHART_DIR exists:
			File chartsdir = new File(getChartDir());
			if(!chartsdir.exists() || !chartsdir.isDirectory()) {
				if(chartsdir.mkdir() == false) {
					throw new IOException("Error! Could not create charts directory");
				} else {
					log.info("Creating directory '" + getChartDir() + "'.");
				}
			}
			
			String filenameExtensionless = this.getChartDir() + File.separator + this.getChartName();
			File outputFile = null;
			
			// save to the chosen filetype:
			switch(filetype) {
			case FILETYPE_PNG:
				outputFile = new File(filenameExtensionless + ".png");
				ChartUtilities.saveChartAsPNG(outputFile, chart, width, height);
				break;
				
			case FILETYPE_JPG:
				outputFile = new File(filenameExtensionless + ".jpg");
				ChartUtilities.saveChartAsJPEG(outputFile, chart, width, height);
				break;

			case FILETYPE_PDF:
				outputFile = new File(filenameExtensionless + ".pdf");
				ChartResultWriter.saveChartAsPDF(outputFile, chart, width, height, new DefaultFontMapper());
				break;
				
			default:
				log.severe("ERROR: Unknown filetype " + filetype + ". Please use a FILETYPE_* constant.");
			}

			this.log.info("Writing chart to " + outputFile.getAbsolutePath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
