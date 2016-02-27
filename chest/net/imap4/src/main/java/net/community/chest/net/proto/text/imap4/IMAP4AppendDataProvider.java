package net.community.chest.net.proto.text.imap4;

import net.community.chest.io.OptionallyCloseable;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>This interface is used as a "callback" to provide data to the APPEND command</P>
 * @author Lyor G.
 * @since Sep 20, 2007 10:24:01 AM
 */
public interface IMAP4AppendDataProvider extends OptionallyCloseable {
	/**
	 * @return data buffer size (in bytes) to be used for processing the APPEND data
	 */
	int getCopyBufferSize ();
	/**
	 * Called by the APPEND implementation in order to get data buffer to be output
	 * @param buf buffer where data should be placed
	 * @param nOffset offset in data buffer for placing data to be APPEND-ed
	 * @param nLen maximum size of data to be placed
	 * @return number of actually valid data placed in the buffer (<0 if error). 
	 * Note: if returned (positive) value is less than required, then end of stream is assumed
	 */
	int getData (byte[] buf, int nOffset, int nLen);
}
