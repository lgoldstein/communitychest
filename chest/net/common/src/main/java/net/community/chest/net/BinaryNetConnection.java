package net.community.chest.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ByteChannel;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Represents a binary net connection through which <U>bytes</U> can be read
 * and written
 * 
 * @author Lyor G.
 * @since Jun 28, 2007 1:41:21 PM
 */
public interface BinaryNetConnection extends NetConnection, ByteChannel {
	/**
	 * Reads ONE character/byte from input
	 * @return read character (-1) if no more characters
	 * @throws IOException if I/O error
	 */
	int read () throws IOException;
	/**
	 * Reads data into supplied buffer as bytes
	 * @param buf buffer to read into
	 * @param offset offset in buffer to read into
	 * @param len maximum number of characters to read
	 * @return number of actual read characters
	 * @throws IOException if I/O error
	 */
	int readBytes (final byte[] buf, final int offset, final int len) throws IOException;
	/**
	 * Reads data into supplied buffer as bytes
	 * @param buf buffer to read into
	 * @return number of actual read characters
	 * @throws IOException if I/O error
	 */
	int readBytes (final byte[] buf) throws IOException;
	 /**
	  * Reads a "line" of data - defined as sequence of characters up to LF.
	  * @param buf buffer into which to place the read data - not including CRLF (!)
	  * @param startOffset start offset in buffer where to start placing the read data (inclusive)
	  * @param maxLen maximum number of characters to read - if LF found BEFORE this length
	  * is reached, then function returns immediately. Otherwise, it returns an "incomplete"
	  * line information
	  * @param li read line information to be filled
	  * @return number of read characters (including any CR/LF) - (<0) if error
	  * @throws IOException if I/O errors
	  * @see LineInfo
	  */
	 int readBinaryLine (final byte[] buf, final int startOffset, final int maxLen, final LineInfo li) throws IOException; 
	 /**
	  * Reads a "line" of data - defined as sequence of characters up to LF.
	  * @param buf buffer into which to place the read data - not including CRLF (!)
	  * @param startOffset start offset in buffer where to start placing the read data (inclusive)
	  * @param maxLen maximum number of characters to read - if LF found BEFORE this length
	  * is reached, then function returns immediately. Otherwise, it returns an "incomplete"
	  * line information
	  * @return read line information
	  * @throws IOException if I/O or arguments errors
	  * @see LineInfo
	  */
	 LineInfo readBinaryLine (final byte[] buf, final int startOffset, final int maxLen) throws IOException; 
	 /**
	  * Reads a "line" of data - defined as sequence of characters up to LF.
	  * @param buf buffer into which to place the read data (not including
	  * CRLF (!))- if LF found BEFORE entire buffer is used, then function
	  * returns immediately. Otherwise, it returns an "incomplete" line information
	  * @param li read line information to be filled
	  * @return number of read characters (including any CR/LF) - (<0) if error
	  * @throws IOException if I/O or arguments errors
	  * @see LineInfo
	  */
	 int readBinaryLine (final byte[] buf, final LineInfo li) throws IOException; 
	 /**
	  * Reads a "line" of data - defined as sequence of characters up to LF.
	  * @param buf buffer into which to place the read data (not including
	  * CRLF (!)) - if LF found BEFORE entire buffer is used, then function
	  * returns immediately. Otherwise, it returns an "incomplete" line information
	  * @return read line information
	  * @throws IOException if I/O or arguments errors
	  * @see LineInfo
	  */
	 LineInfo readBinaryLine (final byte[] buf) throws IOException;
	 /**
	 * @return Returns the number of bytes that can be read (or skipped over) from this connect without blocking
	 * on a <I>"readXXX"</I> method. The next caller might be the same thread or or another thread.
	 * @throws IOException if I/O error
	 */
	int available () throws IOException;
	/**
	 * Skips over and discards specified number of characters of data from this input stream.
	 * @param skipSize EXACT number of characters/bytes to be skipped
	 * @return number of skipped characters (should be same as <I>"skipSize"</I>) - Note: if
	 * skip size is <=0 then nothing is done
	 * @throws IOException if I/O error
	 */
	long skip (final long skipSize) throws IOException;
	/**
	 * "Masks" the object as an input stream
	 * @param autoClose if TRUE then calling the <I>"close"</I> method of the input stream closes
	 * the connection as well.
	 * @return input stream object
	 * @throws IOException if errors during creation of {@link InputStream}
	 */
	InputStream asInputStream (final boolean autoClose) throws IOException;
	/**
	 * Fills data into supplied buffer
	 * @param buf buffer to read into
	 * @param offset offset in buffer to read into
	 * @param len EXACT number of characters to read
	 * @return number of actual read characters (same as <I>"len"</I>)
	 * @throws IOException if I/O error
	 */
	int fillBytes (final byte[] buf, final int offset, final int len) throws IOException;
	/**
	 * Fills data into supplied buffer
	 * @param buf buffer to read into - EXACTLY entire buffer is to be read
	 * @return number of actual read characters (same as <I>buf.length</I>)
	 * @throws IOException
	 */
	int fillBytes (final byte[] buf) throws IOException;
	/**
	 * Writes specified bytes as if they were 8-bit ASCII characters
	 * @param buf buffer from which to write
	 * @param startPos index in buffer to start writing
	 * @param maxLen number of bytes to write
	 * @param flushIt if TRUE then channel is flushed AFTER writing the data
	 * @return number of written bytes (should be EXACTLY the same as <I>"maxLen"</I> parameter) 
	 * @throws IOException if network (or other errors)
	 */
	int writeBytes (final byte[] buf, final int startPos, final int maxLen, final boolean flushIt) throws IOException;
	/**
	 * Writes specified bytes as if they were 8-bit ASCII characters
	 * @param buf buffer from which to write
	 * @param startPos index in buffer to start writing
	 * @param maxLen number of bytes to write
	 * @return number of written bytes (should be EXACTLY the same as <I>"maxLen"</I> parameter) 
	 * @throws IOException if network (or other errors)
	 */
	int writeBytes (final byte[] buf, final int startPos, final int maxLen) throws IOException;
	/**
	 * Writes specified bytes as if they were 8-bit ASCII characters
	 * @param buf buffer from which to write (may be null/empty)
	 * @param flushIt if TRUE then channel is flushed AFTER writing the data
	 * @return number of written bytes (should be EXACTLY the same as <I>"buf.length"</I> parameter)
	 * @throws IOException if network (or other errors)
	 */
	int writeBytes (final byte[] buf, final boolean flushIt) throws IOException;
	/**
	 * Writes specified bytes as if they were 8-bit ASCII characters
	 * @param buf buffer from which to write (may be null/empty)
	 * @return number of written bytes (should be EXACTLY the same as <I>"buf.length"</I> parameter)
	 * @throws IOException if network (or other errors)
	 */
	int writeBytes (final byte[] buf) throws IOException;
	/**
	 * Writes a single character to the connection
	 * @param val character to be written (must be within allowed BYTE value)
	 * @param flushIt if TRUE, then any cached data is flushed AFTER writing the character
	 * @return number of written characters (should be EXACTLY 1)
	 * @throws IOException if errors
	 */
	int write (final int val, final boolean flushIt) throws IOException;
	/**
	 * Writes a single character to the connection
	 * @param val character to be written (must be within allowed BYTE value)
	 * @return number of written characters (should be EXACTLY 1)
	 * @throws IOException if errors
	 */
	int write (final int val) throws IOException;
	/**
	 * "Masks" the object as an output stream
	 * @param autoClose if TRUE then calling the <I>"close"</I> method of the output stream closes
	 * the connection as well.
	 * @return output stream object
	 * @throws IOException if errors while generating the {@link OutputStream}
	 */
	OutputStream asOutputStream (final boolean autoClose) throws IOException;
}
