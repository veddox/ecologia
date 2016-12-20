package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;

import controller.World;

/**
 * This class provides unified I/O methods for debugging,
 * logging, error messages, etc.
 * 
 * @author Daniel Vedder
 * @version 3.12.2014
 */
public abstract class EcologiaIO
{
	public static boolean verbose = false;
	public static boolean debugging = false;
	public static boolean analysing = false;
	public static boolean logging = false;
	
	public final static int CONTINUABLE_ERROR = 0;
	public final static int BREAK_ERROR = 1;
	public final static int FATAL_ERROR = 2;

	/**
	 * Print a log message if the verbose flag is set.
	 * This is meant to be used for important runtime events in the program,
	 * and for fundamental (e.g. birth and death) events during the simulation.
	 * For more detailed output, use either debug() or analysis().
	 *
	 * @param message
	 */
	public static void log(String message)
	{
		if (verbose) {
			message = "LOG: "+message;
			System.out.println(message);
			if (logging) writeFile(message);
		}
	}
	
	/**
	 * Print a debug message if the debug flag is set.
	 * This is primarily intended for use during development.
	 * Experimental data should go to analysis(), important
	 * messages to log().
	 *
	 * @param message
	 */
	public static void debug(String message)
	{
		if (debugging) {
			message = "DEBUG: "+message;
			System.out.println(message);
			if (logging) writeFile(message);
		}
	}

	/**
	 * Print an analysis message if the analysing flag is set.
	 * This is meant to be used for simulation data output relevant only to a
	 * current experiment.
	 *
	 * FIXME A lot of analysis() calls slow the program down drastically.
	 * Implement caching?
	 *
	 * @param message
	 */
	public static void analysis(String message)
	{
		if (analysing) {
			message = "ANALYSIS: "+message;
			System.out.println(message);
			if (logging) writeFile(message);
		}
	}
	
	/**
	 * Print an error message
	 * @param message
	 */
	public static void error(String message)
	{
		message = "ERROR: "+message;
		System.out.println(message);
		if (logging) writeFile(message);
	}
	
	/**
	 * Print an error message and the stack trace
	 * @param message
	 * @param error
	 */
	public static void error(String message, Exception error)
	{
		message = "ERROR: "+message;
		System.out.println(message);
		error.printStackTrace();
		//TODO Print stack trace to file
		if (logging) writeFile(message);
	}
	
	/**
	 * Give an error message and pause/shut down
	 * @param message
	 * @param errorType CONTINUABLE_ERROR, BREAK_ERROR, FATAL_ERROR
	 */
	public static void error(String message, int errorType)
	{
		//FIXME How do we deal with break/fatal errors when in no-graphics mode?
		String logMessage = "ERROR: "+message;
		if (errorType == BREAK_ERROR) {
			World.getInstance().setRunning(false);
			logMessage = logMessage+" - simulation paused";
			JOptionPane.showMessageDialog(null, message, "Error!",
										  JOptionPane.ERROR_MESSAGE);
		}
		else if (errorType == FATAL_ERROR) {
			logMessage = logMessage+" - simulation will terminate";
			JOptionPane.showMessageDialog(null, message+"\nEcologia is shutting down.",
										  "Error!", JOptionPane.ERROR_MESSAGE);
		}
		System.out.println(logMessage);
		if (logging) writeFile(logMessage);
		if (errorType == FATAL_ERROR) System.exit(0);
	}
	
	/**
	 * Archive the current log file, ready for a new run
	 */
	public static void archiveLog()
	{
		File logfile = new File("ecologia.log");
		//Read in the old log and rewrite it to an archive
		//XXX: probably an expensive operation on long log files
		String log = "\n - archived on "+EcologiaIO.getDate()+"\n";
		try {
			BufferedReader logReader = new BufferedReader(new FileReader(logfile));
			String line = logReader.readLine();
			while (line != null) {
				log = log+line+"\n";
				line = logReader.readLine();
			}
			logReader.close();
			
			File logArchive = new File("ecologia-archive.log");
			FileWriter logWriter = new FileWriter(logArchive, true);
			logWriter.write(log);
			logWriter.flush();
			logWriter.close();
		}
		catch (IOException ioe) { /*ignore*/ }
		//Then renew the old logfile
		try {
			FileWriter writer = new FileWriter(logfile, false);
			writer.write(" === ECOLOGIA "+Ecologia.version+" LOG ===\n");
			writer.flush();
			writer.close();
		}
		catch (IOException ioe) {
			logging = false;
			error("Failed to write to logfile! Logging turned off.", ioe);
		}
	}
	
	/**
	 * Print out which flags are set.
	 */
	public static void printStatus()
	{
		if (logging) EcologiaIO.debug("Logging ON");
		else EcologiaIO.debug("Logging OFF");
		if (EcologiaIO.verbose) EcologiaIO.debug("Verbose ON");
		else EcologiaIO.debug("Verbose OFF");
		if (EcologiaIO.analysing) EcologiaIO.debug("Analysing ON");
		else EcologiaIO.debug("Analysing OFF");
		if (EcologiaIO.debugging) EcologiaIO.debug("Debugging ON");
	}
	
	/**
	 * Write a message to file
	 */
	private static void writeFile(String message)
	{
		File logfile = new File("ecologia.log");
		try {
			FileWriter writer = new FileWriter(logfile, true);
			writer.write(getDate() + message + "\n");
			writer.flush();
			writer.close();
		}
		catch (IOException ioe) {
			logging = false;
			error("Failed to write to logfile! Logging turned off.", ioe);
		}
	}
	
	/**
	 * @return time stamp
	 */
	private static String getDate()
	{
		return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss - ").format(new Date());
	}
}
