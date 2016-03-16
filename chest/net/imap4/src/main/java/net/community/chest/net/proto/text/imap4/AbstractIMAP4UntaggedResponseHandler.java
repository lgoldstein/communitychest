package net.community.chest.net.proto.text.imap4;

import java.io.IOException;

import net.community.chest.ParsableString;
import net.community.chest.net.TextNetConnection;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 24, 2008 2:18:37 PM
 */
public abstract class AbstractIMAP4UntaggedResponseHandler {
    private final TextNetConnection    _conn /* =null */;
    public final TextNetConnection getConnection ()
    {
        return _conn;
    }
    /**
     * @param conn Connection from which to get more data as required to
     * complete the response
     */
    protected AbstractIMAP4UntaggedResponseHandler (TextNetConnection conn)
    {
        if (null == (_conn=conn))
            throw new IllegalArgumentException("No input reader connection supplied");
    }
    /**
     * Makes sure all error codes are negative - used when methods either return an index or a (<0) value as an error
     * @param nErr original error
     * @return original error if "nErr < 0", otherwise the negative value of input
     */
    protected static final int adjustErr (int nErr)
    {
        return (nErr > 0) ? (0 - nErr) : nErr;
    }
    /**
     * Called to handle an untagged response response
     * @param ps parsable object response line
     * @param startPos position within parse buffer for untagged response (after the '*')
     * @return 0 if successful
     * @throws IOException if I/O errors during parsing
     */
    public abstract int handleUntaggedResponse (ParsableString ps, final int startPos) throws IOException;
    // to be allocated by need
    protected ParsableString  _psHelper /* =null */;
    // @see #psHelper
    protected ParsableString getParseString (char[] rspLine, int fromIndex, int toIndex)
    {
        if ((null == rspLine) || (0 == rspLine.length) || (fromIndex >= toIndex) || (toIndex > rspLine.length))
            return null;

        if (_psHelper != null)
        {
            if (!_psHelper.wrap(rspLine, fromIndex, toIndex - fromIndex)) // should not happen
                return null;
        }
        else
            _psHelper = new ParsableString(rspLine, fromIndex, toIndex - fromIndex);

        return _psHelper;
    }
    // @see #getParseString(char[] rspLine, int fromIndex, int toIndex)
    protected ParsableString getParseString (char[] rspLine, int nDataStart)
    {
        return getParseString(rspLine, nDataStart, (null == rspLine) ? 0 : rspLine.length);
    }
    // @see #getParseString(char[] rspLine, int nDataStart)
    protected ParsableString getParseString (char[] rspLine)
    {
        return getParseString(rspLine, 0);
    }
    /**
     * Skips the specified amount of characters from the protocol
     * @param skipSize number of characters to skip
     * @return number of actually skipped
     * @throws IOException if problems encountered
     */
    protected long skipProtocolData (long skipSize) throws IOException
    {
        final TextNetConnection    conn=getConnection();
        if (null == conn)
            throw new IOException("No IMAP4 connection to skip data from");
        return conn.skip(skipSize);
    }
    /**
     * Reads a line - up to CR/LF and returns its contents
     * @return parsable string line if successful - Note: the line may/not have ended if LF...
     * @throws IOException if errors encountered
     */
    protected ParsableString readFetchBufferLine () throws IOException
    {
        final TextNetConnection    conn=getConnection();
        if (null == conn)
            throw new IOException("No IMAP4 connection to read fetch buffer line from");

        return new ParsableString(conn.readLine());
    }
    /**
     * Re-Initializes the parsable string value with a new line
     * @param ps parsable string object to be re-initialized
     * @return first non-empty index in read data (<0 or >maxIndex if none found)
     * @throws IOException if access errors
     */
    protected int reReadFetchBufferLine (ParsableString ps) throws IOException
    {
        final ParsableString    newPs=readFetchBufferLine();
        if (!ps.wrap(newPs.array(), newPs.getStartIndex(), newPs.getMaxIndex()))
            throw new IMAP4RspHandleException("Cannot re-wrap new re-read fetch buffer line");

        return ps.findNonEmptyDataStart();
    }
    /**
     * Skips a literal argument in the parsing buffer. Note: the parsing buffer is re-initialized
     * @param ps parsing buffer - Note: is changed due to literal count, so old indices do not apply
     * @param fromIndex - index in parse buffer where literal count starts - if points to '{' then it is skipped
     * @return index of first non-empty/whitespace position AFTER skipping
     * @throws IOException if error encountered
     */
    protected int skipLiteral (ParsableString ps, int fromIndex) throws IOException
    {
        final int    litStart=(IMAP4Protocol.IMAP4_OCTCNT_SDELIM == ps.getCharAt(fromIndex)) ? fromIndex+1 : fromIndex,
                    litEnd=ps.indexOf(IMAP4Protocol.IMAP4_OCTCNT_EDELIM, litStart);

        if (litEnd <= litStart)
            throw new IMAP4RspHandleException("Non-delimted value in literal count");

        try
        {
            final long litSize=ps.getUnsignedLong(litStart, litEnd), lSkip=skipProtocolData(litSize);
            if (lSkip != litSize)
                throw new IMAP4RspHandleException("Mismatched literal count skip: req=" + litSize + ",skip=" + lSkip);
        }
        catch(NumberFormatException nfe)
        {
            throw new IMAP4RspHandleException("Non-numerical value in literal count");
        }

        return reReadFetchBufferLine(ps);
    }
    /**
     * Skips the next argument in the parsing buffer - Note: if it is a literal, then the parsing buffer is re-initialized
     * @param ps parsing buffer - Note: may be changed due to literal count, so old indices might not apply
     * @param fromIndex - index in parse buffer where argument is to be skipped
     * @return index of first non-empty/whitespace position AFTER skipping
     * @throws IOException if error encountered
     */
    protected int skipArgument (ParsableString ps, int fromIndex) throws IOException
    {
        final int argStart=ps.findNonEmptyDataStart(fromIndex);
        if (argStart < fromIndex)
            throw new IMAP4RspHandleException("No argument found to skip");

        final char    chVal=ps.getCharAt(argStart);
        if (IMAP4Protocol.IMAP4_OCTCNT_SDELIM == chVal)
            return skipLiteral(ps, argStart+1);

        // if delimited argument, then skip till its end (NOTE: assumes end delimiter in buffer
        if (IMAP4Protocol.IMAP4_QUOTE_DELIM == chVal)
        {
            final int    qtEnd=ps.indexOf(IMAP4Protocol.IMAP4_QUOTE_DELIM, argStart+1);
            if ((qtEnd <= argStart) || (qtEnd > ps.getMaxIndex()))
                throw new IMAP4RspHandleException("Cannot find quote end in parse buffer");

            return ps.findNonEmptyDataStart(qtEnd+1);
        }

        // this point is reached for "normal" atoms
        return ps.findNonEmptyDataStart(ps.findNonEmptyDataEnd(argStart) + 1);
    }
    /**
     * @return response object which will carry the final response
     */
    protected abstract IMAP4TaggedResponse getResponse ();
}
