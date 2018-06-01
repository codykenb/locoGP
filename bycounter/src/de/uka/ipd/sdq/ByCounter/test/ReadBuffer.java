package de.uka.ipd.sdq.ByCounter.test;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ReadBuffer extends Thread {
	/**
	 * Input Stream which should be redirected.
	 */
	InputStream is;

	// /**
	// * Object of the class which helps to handle strings.
	// */
	// HandleStrings strings = new HandleStrings();
	/**
	 * The output stream to a file.
	 */
	FileOutputStream stream;

	/**
	 * This constructor is needed to clear the error buffer which could be
	 * filled from si.exe.
	 * 
	 * @param is
	 *            The input buffer which should be cleared.
	 */

	public ReadBuffer(InputStream is) {
		this.is = is;
	}

	/**
	 * This constructor which is needed to redirect the output from an
	 * executable to a file.
	 * 
	 * @param is
	 *            The input stream from the executable which should be
	 *            redirected.
	 * @param stream
	 *            The file output stream to the file where the information is
	 *            stored.
	 */

	public ReadBuffer(InputStream is, FileOutputStream stream) {
		this.is = is;
		this.stream = stream;
	}

	/**
	 * This function runs the thread which clears the buffer which is filled by
	 * an executable.
	 */

	public void run() {
		PrintWriter write = null;

		if (this.stream != null)
			write = new PrintWriter(this.stream);
		else {
			InputStreamReader isr = new InputStreamReader(this.is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;

			try {
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			InputStreamReader isr = new InputStreamReader(this.is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;

			while ((line = br.readLine()) != null) {
				if (write != null) {
					// MK
					// line = strings.removeBeginningSpace(line);
					line = "";// MK
					write.println(line);
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}