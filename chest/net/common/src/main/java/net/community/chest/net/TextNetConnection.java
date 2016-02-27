package net.community.chest.net;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Copyright 2007 as per GPLv2
 * 
 * provides an abstract view of a textual (US-ASCII) network connection -
 * e.g., FTP, SMTP, IMAP, POP3, etc.
 * 
 * @author Lyor G.
 * @since Jun 28, 2007 1:51:46 PM
 */
public interface TextNetConnection extends BinaryNetConnection, Readable, Appendable {
	 /**
	  * Reads data into supplied buffer
	  * @param buf buffer to read into
	  * @param offset offset in buffer to read into
	  * @param len maximum number of characters to read
	  * @return number of actual read characters
	  * @throws IOException if I/O error
	  */
	 int read (final char[] buf, final int offset, final int len) throws IOException;
	 /**
	  * Reads data into supplied buffer
	  * @param buf buffer to read into
	  * @return number of actual read characters
	  * @throws IOException if I/O error
	  */
	 int read (final char[] buf) throws IOException;
	 /**
	  * Fills data into supplied buffer
	  * @param buf buffer to read into
	  * @param offset offset in buffer to read into
	  * @param len EXACT number of characters to read
	  * @return number of actual read characters (same as <I>"len"</I>)
	  * @throws IOException if I/O error
	  */
	 int fill (final char[] buf, final int offset, final int len) throws IOException;
	 /**
	  * Fills data into supplied buffer
	  * @param buf buffer to read into - EXACTLY entire buffer is to be read
	  * @return number of actual read characters (same as <I>buf.length</I>)
	  * @throws IOException if I/O error
	  */
	 int fill (final char[] buf) throws IOException;
	 /**
	  * Reads a "line" of data - defined as sequence of characters up to LF.
	  * @param buf buffer into which to place the read data - not including CRLF(!)
	  * @param startOffset start offset in buffer where to start placing the read data (inclusive)
	  * @param maxLen maximum number of characters to read - if LF found BEFORE this length
	  * is reached, then function returns immediately. Otherwise, it returns an "incomplete"
	  * line information
	  * @param li read line information to be filled
	  * @return number of read characters (including any CR/LF) - (<0) if error
	  * @throws IOException if I/O errors
	  */
	 int readLine (final char[] buf, final int startOffset, final int maxLen, final LineInfo li) throws IOException; 
	 /**
	  * Reads a "line" of data - defined as sequence of characters up to LF.
	  * @param buf buffer into which to place the read data - not including CRLF(!)
	  * @param startOffset start offset in buffer where to start placing the read data (inclusive)
	  * @param maxLen maximum number of characters to read - if LF found BEFORE this length
	  * is reached, then function returns immediately. Otherwise, it returns an "incomplete"
	  * line information
	  * @return read line information
	  * @throws IOException if I/O or arguments errors
	  */
	 LineInfo readLine (final char[] buf, final int startOffset, final int maxLen) throws IOException; 
	 /**
	  * Reads a "line" of data - defined as sequence of characters up to LF.
	  * @param buf buffer into which to place the read data (not including
	  * CRLF (!))- if LF found BEFORE entire buffer is used, then function
	  * returns immediately. Otherwise, it returns an "incomplete" line information
	  * @param li read line information to be filled
	  * @return number of read characters (including any CR/LF) - (<0) if error
	  * @throws IOException if I/O or arguments errors
	  */
	 int readLine (final char[] buf, final LineInfo li) throws IOException; 
	 /**
	  * Reads a "line" of data - defined as sequence of characters up to LF.
	  * @param buf buffer into which to place the read data (not including
	  * CRLF (!))- if LF found BEFORE entire buffer is used, then function
	  * returns immediately. Otherwise, it returns an "incomplete" line information
	  * @return read line information
	  * @throws IOException if I/O or arguments errors
	  * @see #readLine(char[] buf, int startOffset, int maxLen)
	  */
	 LineInfo readLine (final char[] buf) throws IOException;
	 /**
	  * Reads a line of data from input - as much as required
	  * @return read line as string - not including CRLF (!)
	  * @throws IOException if errors encountered
	  */
	 String readLine () throws IOException;
	 /**
	  * Masks this object as a Reader - Note: if you want to use "readLine()" then
	  * better to use the interface call rather than masking it as a (Buffered)Reader
	  * @param autoClose if TRUE then call to Reader#close() also closes the underlying connection
	  * @return Reader object
	  * @throws IOException if problems creating the object
	  */
	 Reader asReader (boolean autoClose) throws IOException;
	 /**
	  * Writes specified data to connection
	  * @param buf buffer from which to write
	  * @param startOffset offset in data buffer to start writing from (inclusive)
	  * @param maxLen number of characters to write
	  * @param flushIt if TRUE then channel is flushed AFTER writing the data
	  * @return number of actually written characters (should be same as <I>"maxLen"</I>) - or (<0) if error
	  * @throws IOException if I/O error
	  */
	 int write (final char[] buf, final int startOffset, final int maxLen, final boolean flushIt) throws IOException;
	 /**
	  * Writes specified data to connection
	  * @param buf buffer from which to write - Note: ENTIRE buffer is written
	  * @param flushIt if TRUE then channel is flushed AFTER writing the data
	  * @return number of actually written characters (should be same as <I>buf.length</I>) - or (<0) if error
	  * @throws IOException if I/O error
	  * @see #write(char[] buf, int startOffset, int maxLen)
	  */
	 int write (final char[] buf, final boolean flushIt) throws IOException;
	 /**
	  * Writes specified data to connection
	  * @param s string to write - Note: ENTIRE string is written
	  * @param flushIt if TRUE then channel is flushed AFTER writing the data
	  * @return number of actually written characters (should be same as <I>s.length()</I>) - or (<0) if error
	  * @throws IOException if I/O error
	  * @see #write(char[] buf, int startOffset, int maxLen)
	  */
	 int write (String s, final boolean flushIt) throws IOException;
	 /**
	  * Writes specified data to connection
	  * @param buf buffer from which to write
	  * @param startOffset offset in data buffer to start writing from (inclusive)
	  * @param maxLen number of characters to write
	  * @return number of actually written characters (should be same as <I>"maxLen"</I>) - or (<0) if error
	  * @throws IOException if I/O error
	  */
	 int write (final char[] buf, final int startOffset, final int maxLen) throws IOException;
	 /**
	  * Writes specified data to connection
	  * @param buf buffer from which to write - Note: ENTIRE buffer is written
	  * @return number of actually written characters (should be same as <I>buf.length</I>) - or (<0) if error
	  * @throws IOException if I/O error
	  * @see #write(char[] buf, int startOffset, int maxLen)
	  */
	 int write (char ... buf) throws IOException;
	 /**
	  * Writes specified data to connection
	  * @param s string to write - Note: ENTIRE string is written
	  * @return number of actually written characters (should be same as <I>s.length()</I>) - or (<0) if error
	  * @throws IOException if I/O error
	  * @see #write(char[] buf, int startOffset, int maxLen)
	  */
	 int write (String s) throws IOException;
	 /**
	  * Masks this object as a Writer
	  * @param autoClose if true then closes the underlying connection when Writer#close() is called
	  * @return writer object
	  * @throws IOException if problems creating the object
	  */
	 Writer asWriter (boolean autoClose) throws IOException;
	 /**
	  * Writes CRLF to the output channel
	  * @param flushIt if TRUE then channel is flushed AFTER writing the CRLF
	  * @return number of written characters (or <0 if error) - should be same as <I>CRLF.length</I>
	  * @throws IOException
	  */
	 int writeln (final boolean flushIt) throws IOException;
	 /**
	  * Writes specified data to connection and then adds a CRLF to its end
	  * @param buf buffer from which to write
	  * @param startOffset offset in data buffer to start writing from (inclusive)
	  * @param maxLen number of characters to write
	  * @param flushIt if TRUE then channel is flushed AFTER writing the CRLF
	  * @return number of actually written characters including CRLF (should be same as <I>"maxLen+2"</I>) - or (<0) if error
	  * @throws IOException if I/O error
	  */
	 int writeln (final char[] buf, final int startOffset, final int maxLen, final boolean flushIt) throws IOException;
	 /**
	  * Writes specified data to connection and then adds a CRLF
	  * @param buf buffer from which to write - Note: ENTIRE buffer is written
	  * @param flushIt if TRUE then channel is flushed AFTER writing the CRLF
	  * @return number of actually written characters including CRLF (should be same as <I>buf.length+2</I>) - or (<0) if error
	  * @throws IOException if I/O error
	  * @see #write(char[] buf, int startOffset, int maxLen)
	  */
	 int writeln (final char[] buf, final boolean flushIt) throws IOException;
	 /**
	  * Writes specified data to connection and then adds a CRLF
	  * @param s string to write - Note: ENTIRE string is written
	  * @param flushIt if TRUE then channel is flushed AFTER writing the CRLF
	  * @return number of actually written characters including CRLF (should be same as <I>s.length()+2</I>) - or (<0) if error
	  * @throws IOException if I/O error
	  * @see #write(char[] buf, int startOffset, int maxLen)
	  */
	 int writeln (final String s, final boolean flushIt) throws IOException;
	 /**
	  * Writes CRLF to the output channel (but does not flush it)
	  * @return number of written characters (or <0 if error) - should be
	  * exactly 2 (CR+LF)
	  * @throws IOException if I/O error
	  * @see #writeln(boolean flushIt)
	  */
	 int writeln () throws IOException;
	 /**
	  * Writes specified data to connection and then adds a CRLF to its end
	  * @param buf buffer from which to write
	  * @param startOffset offset in data buffer to start writing from (inclusive)
	  * @param maxLen number of characters to write
	  * @return number of actually written characters including CRLF (should be same as <I>"maxLen+2"</I>) - or (<0) if error
	  * @throws IOException if I/O error
	  */
	 int writeln (final char[] buf, final int startOffset, final int maxLen) throws IOException;
	 /**
	  * Writes specified data to connection and then adds a CRLF
	  * @param buf buffer from which to write - Note: ENTIRE buffer is written
	  * @return number of actually written characters including CRLF (should be same as <I>buf.length+2</I>) - or (<0) if error
	  * @throws IOException if I/O error
	  * @see #write(char[] buf, int startOffset, int maxLen)
	  */
	 int writeln (char ... buf) throws IOException;
	 /**
	  * Writes specified data to connection and then adds a CRLF
	  * @param s string to write - Note: ENTIRE string is written
	  * @return number of actually written characters including CRLF (should be same as <I>s.length()+2</I>) - or (<0) if error
	  * @throws IOException if I/O error
	  * @see #write(char[] buf, int startOffset, int maxLen)
	  */
	 int writeln (final String s) throws IOException;
}
