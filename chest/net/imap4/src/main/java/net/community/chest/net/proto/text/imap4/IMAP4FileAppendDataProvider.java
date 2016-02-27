package net.community.chest.net.proto.text.imap4;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 24, 2008 2:51:05 PM
 */
public class IMAP4FileAppendDataProvider extends IMAP4StreamAppendDataProvider {
	/**
	 * @param filePath path of a file containing the data to be appended (formatted according to MIME requirements)
	 * @param copyBufSize size of data buffer (bytes) to be used for copying the data from the file
	 * @throws IOException if errors initializing the 
	 */
	public IMAP4FileAppendDataProvider (final String filePath, final int copyBufSize) throws IOException
	{
		if ((null == filePath) || (filePath.length() <= 0) || (copyBufSize <= 64))
			throw new IMAP4AccessParamsException("Bad/Illegal file append data provider params");
		setCopyBufferSize(copyBufSize);

		InputStream	fin=null;
		try
		{
			final File	fileInfo=new File(filePath);
			fin = new FileInputStream(fileInfo);
			this.in = fin;
			fin = null;
			setTotalData(fileInfo.length());
			setCopyBufferSize(copyBufSize);
		}
		catch(Exception se)
		{
			if (fin != null)
			{
				try
				{ 
					fin.close();
				}
				catch(IOException ioe)
				{
					/* ignore */
				}
				fin = null;
			}

			if (se instanceof IOException)
				throw (IOException) se;
			else
				throw new IMAP4AccessParamsException(se.getClass().getName() + " while to append file=" + filePath + ": " + se.getMessage());
		}
	}
}
