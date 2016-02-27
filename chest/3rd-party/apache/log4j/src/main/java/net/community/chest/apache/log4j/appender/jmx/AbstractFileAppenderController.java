package net.community.chest.apache.log4j.appender.jmx;

import java.io.Flushable;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful interface that can be used to export an MBean representing
 * various attributes and operations associated with a file appender</P>
 * 
 * @author Lyor G.
 * @since Oct 1, 2007 2:03:35 PM
 */
public interface AbstractFileAppenderController extends Flushable {
	/**
	 * @return root folder directory under which the appender creates its
	 * files
	 */
	String getLogPath ();
	void setLogPath (String logPath);
	/**
	 * @return log file extension to be used (e.g., ".log", ".csv", etc.)
	 */
	String getExtension ();
	void setExtension (String extension);
	/**
	 * @return I/O buffer size (KB) - <=0 means no buffering
	 */
	int getIOBufSizeKB ();
	void setIOBufSizeKB (int bufSize);
	/**
	 * @return Current file path used for logging - may be null/empty if
	 * "lazy/by-need" opening implemented.
	 */
	String getCurrentLogFilePath ();
	/**
	 * @return current file size (bytes) - zero/negative if no current file open
	 */
	long getCurrentFileSize ();
}
