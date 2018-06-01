package experimental;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import de.uka.ipd.sdq.ByCounter.test.ReadBuffer;

/**
 * This class is to demonstrate that the Java platform API
 * {@link Runtime#traceInstructions(boolean)} method and 
 * {@link Runtime#traceMethodCalls(boolean)} don't work at all.
 * It was one of the motivations to develop ByCounter.
 * Also, try to run this class using the command-line switches that 
 * activate tracing of instructions execution / method execution 
 * to see that it isn't working either (TODO: try JVM debug builds).
 * TODO seems not to work on Mac, even if "Enable Tracing" is enabled in "Java Preferences". However, 
 * hints http://www.macosxhints.com/article.php?story=20091223115101622 and 
 * http://www.macworld.com/article/58081/2007/05/findrecent.html might reveal 
 * the files changed by Java. 
 * @author Michael Kuperberg
 * @since 1.1
 * @version 1.2
 */
public class JavaLangRuntimeInstructionTracingTester {

	private static final String batchFileLocation = "."+File.separator+
						"bin"+File.separator+
						"experimental"+File.separator+
						"JavaLangRuntimeInstructionTracingTester.bat";

	private static final String logFileFolder = "."+File.separator+
								"bin"+File.separator+
								"experimental"+File.separator;
	
	/**
	 * The normal main method.
	 * @param args no arguments needed or evaluated.
	 */
	public static void main(String[] args) {
//		performWork();

		JavaLangRuntimeInstructionTracingTester jlrt;
		jlrt = new JavaLangRuntimeInstructionTracingTester();
		jlrt.performWork();
	}

	Logger log = Logger.getLogger("experimental.JavaLangRuntimeInstructionTracingTester");

	private void connect(String[] cmdarray, String fileName) throws Exception {

//		CharSequence cs;

		Runtime runtime = Runtime.getRuntime();
		runtime.traceInstructions(true);
		runtime.traceMethodCalls(true);
		Process process;

		process = runtime.exec("java -version"/* MK cmdarray */);

		// any error message?
		ReadBuffer errorBuffer = new ReadBuffer(process.getErrorStream());

		// any output?
		FileOutputStream stream;

		stream = new FileOutputStream(fileName);
		ReadBuffer outputBuffer = new ReadBuffer(process.getInputStream(),
				stream);

		// kick them off
		errorBuffer.start();
		outputBuffer.start();

		try {
			process.waitFor();
		} catch (InterruptedException e) {
			throw new /* MK API */Exception(
					"System IO error. Please try this operation later.");
		}

		while (outputBuffer.isAlive() || errorBuffer.isAlive()) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			stream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void logProcessBuffers(Process process) {
		try{		
			if(process==null){return;}
			InputStream[] streams = new InputStream[]{
				process.getErrorStream(),
				process.getInputStream()
			};
			boolean stillAvailable = false;
			StringBuffer sb = new StringBuffer();
			for(InputStream es : streams){
				if(es.available()>0){
					stillAvailable = true;
				}
				byte[] readd;
				while(stillAvailable){
					readd = new byte[es.available()];
					es.read(readd);
					sb.append(new String(readd));
					if(es.available()>0){
						stillAvailable = true;
					}else{
						stillAvailable = false;
					}
				}
			
			System.out.println(sb.toString());
			}
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method performs a small amount of work during which the 
	 * instruction tracing is switched on, 
	 * then method tracing is switched on, 
	 * then method tracing is switched off, 
	 * then instruction tracing is switched off. 
	 */
	private void performWork() {
		System.out.println("A: " + (0 + 6));
		java.lang.Runtime.getRuntime().traceInstructions(true);
		System.out.println("B: " + (1 + 6));
		java.lang.Runtime.getRuntime().traceMethodCalls(true);
		System.out.println("C: " + (2 + 6));
		java.lang.Runtime.getRuntime().traceInstructions(false);
		System.out.println("D: " + (3 + 6));
		java.lang.Runtime.getRuntime().traceMethodCalls(false);
		System.out.println("E: " + (4 + 6));
	}

	/**
	 * Does the complete work.
	 */
	public void run() {
		log.info("Performing the work directly");
		try {
			log.fine("Sleeping for 1000 ms to allow Eclipse buffer flushin");
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.performWork();
		try {
			log.fine("Sleeping for 1000 ms to allow Eclipse buffer flushin");
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("Performing the work by running a batch file over Runtime.exec");
		try {
			log.fine("Sleeping for 1000 ms to prevent log file overwriting");
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.runBatchFileOnSelf();
		log.info("Performing the work by running a batch file over ProcessBuilder");
		this.runBatchFileOnSelf_usingProcessBuilder();
//		log.info("Performing the work using legacy code");
//		try {
//			this.connect(null, "1.txt");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	private void runBatchFileOnSelf(){
		Runtime runtime = Runtime.getRuntime();
		runtime.traceInstructions(true);
		runtime.traceMethodCalls(true);
		Process process = null;
		long timestamp = System.currentTimeMillis();
		try{
			process = runtime.exec(new String[]{
				batchFileLocation,
				">",
				logFileFolder + timestamp + ".JavaLangRuntimeInstructionTracingTester.log",
				"2>&1"});
		}catch (IOException e) {
			e.printStackTrace();
		}		
		
		logProcessBuffers(process);
	}

	private void runBatchFileOnSelf_usingProcessBuilder(){
		long timestamp = System.currentTimeMillis();
		ProcessBuilder pb = new ProcessBuilder(
//				"cmd", 
//				"/c", 
//				"/start", 
				batchFileLocation,
				">",
				logFileFolder+timestamp+".JavaLangRuntimeInstructionTracingTester.log",
				"2>&1");
		Runtime.getRuntime().traceInstructions(true);
		Runtime.getRuntime().traceMethodCalls(true);
		Process process = null;
		try{
			process = pb.start();
		}catch (IOException e) {
			e.printStackTrace();
		}		
		
		logProcessBuffers(process);
	}
}
