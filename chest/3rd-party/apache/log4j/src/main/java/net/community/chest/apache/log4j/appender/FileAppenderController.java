/*
 * 
 */
package net.community.chest.apache.log4j.appender;

import java.io.IOException;

/**
 * <P>Copyright as per GPLv2</P>
 * <P>Exposes some of the {@link org.apache.log4j.FileAppender}-s functionality as an interface</P>
 * @author Lyor G.
 * @since Nov 21, 2010 1:58:01 PM
 */
public interface FileAppenderController extends WriterAppenderController {
	String getFile ();
	void setFile (String file);

	boolean getAppend ();
	void setAppend (boolean flag);

	boolean getBufferedIO ();
	void setBufferedIO (boolean bufferedIO);

	int getBufferSize ();
	void setBufferSize (int bufferSize);
	
	void setFile (String fileName, boolean append, boolean bufferedIO, int bufferSize)
    	throws IOException;
}
