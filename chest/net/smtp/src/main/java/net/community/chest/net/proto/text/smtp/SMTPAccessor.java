package net.community.chest.net.proto.text.smtp;

import java.io.IOException;
import java.io.OutputStream;

import net.community.chest.net.dns.DNSAccess;
import net.community.chest.net.proto.text.NetServerWelcomeLine;
import net.community.chest.net.proto.text.TextProtocolNetConnection;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Represents the SMTP access protocol</P>
 * 
 * @author Lyor G.
 * @since Sep 20, 2007 7:24:28 AM
 */
public interface SMTPAccessor extends TextProtocolNetConnection {
	/**
	 * Resolves MX record and attempts to connect to an MX gateway in order
	 * of ascending preference (Note: cannot be called if already connected)
	 * @param nsa The {@link DNSAccess} instance to be used to resolve MX records
	 * @param dmName domain name to whose MX gateway we want to connect
	 * @param nPort port number on which to connect
	 * @param wl string to be used to fill in the welcome response (null == not needed)
	 * @throws IOException if connection handling error
	 */
	void mxConnect (DNSAccess nsa, String dmName, int nPort, NetServerWelcomeLine wl) throws IOException;
	/**
	 * Resolves MX record and attempts to connect to an MX gateway in order
	 * of ascending preference (Note: cannot be called if already connected)
	 * @param nsa The {@link DNSAccess} instance to be used to resolve MX records
	 * @param dmName domain name to whose MX gateway we want to connect
	 * @param nPort port number on which to connect
	 * @throws IOException if connection handling error
	 */
	void mxConnect (DNSAccess nsa, String dmName, int nPort) throws IOException;
	/**
	 * Resolves MX record and attempts to connect to an MX gateway in order
	 * of ascending preference (Note: cannot be called if already connected)
	 * @param nsa The {@link DNSAccess} instance to be used to resolve MX records
	 * @param dmName domain name to whose MX gateway we want to connect
	 * @param wl string to be used to fill in the welcome response (null == not needed)
	 * @throws IOException if connection handling error
	 */
	void mxConnect (DNSAccess nsa, String dmName, NetServerWelcomeLine wl) throws IOException;
	/**
	 * Resolves MX record and attempts to connect to an MX gateway in order
	 * of ascending preference (Note: cannot be called if already connected)
	 * @param nsa The {@link DNSAccess} instance to be used to resolve MX records
	 * @param dmName domain name to whose MX gateway we want to connect
	 * @throws IOException if connection handling error
	 */
	void mxConnect (DNSAccess nsa, String dmName) throws IOException;
	/**
	 * Sends the HELO command
	 * @param dmn domain to be sent as parameter - if null/empty then current
	 * host domain is used
	 * @return SMTP response code
	 * @throws IOException if network errors
	 */
	SMTPResponse helo (String dmn /* may be null/empty */) throws IOException;
	/**
	 * Sends the EHLO command
	 * @param dmn domain to be sent as parameter - if null/empty then current
	 * host domain is used
	 * @param reporter "callback" used to pass along the resulting reported
	 * capabilities of the EHLO command. If null, then the report is not needed
	 * @return SMTP response code
	 * @throws IOException if network errors
	 */
	SMTPResponse ehlo (String dmn, ESMTPCapabilityHandler reporter) throws IOException;
	/**
	 * Sends the EHLO command (doesn't care about reported capabilities)
	 * @param dmn domain to be sent as parameter - if null/empty then current host
	 * domain is used
	 * @return SMTP response code
	 * @throws IOException if network errors
	 */
	SMTPResponse ehlo (String dmn) throws IOException;
	/**
	 * Sends the EHLO command and returns the capabilities
	 * @param dmn domain to be sent as parameter - if null/empty then current host domain is used
	 * @return SMTP response code
	 * @throws IOException if network errors
	 */
	SMTPExtendedHeloResponse capabilities (String dmn) throws IOException;
	/**
	 * Performs the ESMTP plaintext AUTH LOGIN protocol
	 * @param username username for authentication
	 * @param password password credential
	 * @return SMTP response code
	 * @throws IOException if network errors
	 */
	SMTPResponse authLogin (String username, String password) throws IOException;
	/**
	 * Performs the ESMTP plaintext AUTH PLAIN protocol
	 * @param username username for authentication
	 * @param password password credential
	 * @return SMTP response code
	 * @throws IOException if network errors
	 */
	SMTPResponse authPlain (String username, String password) throws IOException;
	/**
	 * Performs the ESMTP AUTH CRAM-MD5 protocol
	 * @param username username for authentication
	 * @param password password credential
	 * @return SMTP response code
	 * @throws IOException if network errors
	 */
	SMTPResponse authCRAMMD5 (String username, String password) throws IOException;
	/**
	 * Sets the SMTP sender (MAIL FROM:)
	 * @param sender sender address (without enclosing "<>") - may be null/empty
	 * @return SMTP response code
	 * @throws IOException if network errors
	 */
	SMTPResponse mailFrom (String sender) throws IOException;
	/**
	 * Adds a mail target identity (RCPT TO:)
	 * @param recip recipient address (without enclosing "<>")
	 * @return SMTP response code
	 * @throws IOException if network errors
	 */
	SMTPResponse rcptTo (String recip) throws IOException;
	/**
	 * Starts the DATA transfer stage
	 * @return SMTP response code
	 * @throws IOException if network errors
	 * @see SMTPProtocol for known response codes
	 */
	SMTPResponse startData () throws IOException;
	/**
	 * Writes content data to the connection. This method should be called ONLY
	 * after successfully starting the protocol data stage.
	 * @param data data buffer to be written
	 * @param startOffset offset within the buffer to write from (inclusive)
	 * @param len number of characters to write
	 * @param flushIt if TRUE then write is flushed to the network after writing the required data 
	 * @return number of written characters (should be same as <I>"len"</I> parameter)
	 * @throws IOException If failed to write the data
	 */
	int writeData (char[] data, int startOffset, int len, boolean flushIt) throws IOException;
	/**
	 * Writes content data to the connection. This method should be called ONLY
	 * after successfully starting the protocol data stage.
	 * @param data data buffer to be written (entirely)
	 * @param flushIt if TRUE then write is flushed to the network after writing the required data 
	 * @return number of written characters (should be same as <I>"data.length"</I> value)
	 * @throws IOException if network errors
	 */
	int writeData (char[] data, boolean flushIt) throws IOException;
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
	 * @param buf buffer from which to write (may be null/empty)
	 * @param flushIt if TRUE then channel is flushed AFTER writing the data
	 * @return number of written bytes (should be EXACTLY the same as <I>"buf.length"</I> parameter) 
	 * @throws IOException if network (or other errors)
	 */
	int writeBytes (final byte[] buf, final boolean flushIt) throws IOException;
	/**
	 * Multiplier used when waiting for the DATA stage end response from the server
	 */
	public static final int DATA_END_TIMEOUT_FACTOR=4;
	/**
	 * ".CRLF" used to signal end-of-message in SMTP data stage
	 */
	public static final char[]	EOM_SIGNAL={ '.', '\r', '\n' };
	/**
	 * Ends the data stage by sending an end-of-message indicator ".CRLF".
	 * Note: for this stage, the wait time for the server response is 4 times
	 * the default timeout
	 * @param addCRLF if TRUE then an additional CRLF is sent BEFORE the
	 * end-of-message indicator
	 * @return SMTP response code
	 * @throws IOException if network errors
	 */
	SMTPResponse endData (boolean addCRLF) throws IOException;
	/**
	 * "Masks" the object as an output stream
	 * @param autoClose if TRUE then calling the <I>"close"</I> method of the
	 * output stream closes
	 * the connection as well - in this case an ORDERLY <I>"endData"</I> and
	 * <I>"quit"</I> is done.
	 * @return output stream object
	 * @throws IOException if errors
	 */
	OutputStream asOutputStream (final boolean autoClose) throws IOException;
	/**
	 * Resets the connection state (RSET)
	 * @return SMTP response code
	 * @throws IOException if network errors
	 * @see SMTPProtocol for known response codes
	 */
	SMTPResponse reset () throws IOException;
	/**
	 * Sends the QUIT command - Note: implementors SHOULD also automatically
	 * close the connection. However, callers should also call the <I>"close"</I>
	 * method (just in case)
	 * @return SMTP response code
	 * @throws IOException if network errors
	 */
	SMTPResponse quit () throws IOException;
	/**
	 * Does the handshake required to prepare the connection for receiving
	 * data - i.e., MAIL FROM: followed by RPCT TO:(s). Note: assumes that
	 * HELO/EHLO stage has been done
     * @param sender sender (MAIL FROM:) - may be empty/null
     * @param recips recipients - may not be null/empty, and MUST include
     * at least <U>one</U> recipient string.
	 * @return last valid response. Note: should be the response of the DATA
	 * request. However, if any previous stage (MAIL FROM:, RCPT TO:)
	 * fails, then its response object is returned. In other words, the caller
	 * should check that the returned response code is {@link SMTPProtocol#SMTP_E_START_INP}
	 * @throws IOException if networking errors
	 */
	SMTPResponse doDataHandshake (String sender, String... recips) throws IOException;
	/**
	 * Connects and does the handhsake up to and including the DATA stage
     * @param server server to which to connect the SMTP protocol
     * @param port port number to connect to - if <=0 then default is used
     * @param sender sender (MAIL FROM:) - may be empty/null
     * @param recips recipients - may not be null/empty, and MUST include
     * at least <U>one</U> recipient string.
	 * @return last valid response. Note: should be the response of the DATA
	 * request. However, if any previous stage (HELO, MAIL FROM:, RCPT TO:)
	 * fails, then its response object is returned. In other words, the caller
	 * should check that the returned response code is {@link SMTPProtocol#SMTP_E_START_INP}
	 * @throws IOException if networking errors
	 */
	SMTPResponse connectAndDoDataHandshake (String server, int port, String sender, String... recips) throws IOException;
}
