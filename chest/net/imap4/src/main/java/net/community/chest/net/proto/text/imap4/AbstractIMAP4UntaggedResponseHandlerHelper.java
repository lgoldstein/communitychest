package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.io.UTFDataFormatException;
import java.util.Collection;

import net.community.chest.ParsableString;
import net.community.chest.lang.math.NumberTables;
import net.community.chest.net.TextNetConnection;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 24, 2008 2:22:24 PM
 */
public abstract class AbstractIMAP4UntaggedResponseHandlerHelper extends AbstractIMAP4UntaggedResponseHandler {
    protected AbstractIMAP4UntaggedResponseHandlerHelper (TextNetConnection conn)
    {
        super(conn);
    }
    // helper object
    private IMAP4ParseBufferHandler    _bufHandler=new IMAP4ParseBufferHandler();
    /*
     * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4UntaggedResponseHandler#skipProtocolData(long)
     */
    @Override
    protected long skipProtocolData (long skipSize) throws IOException
    {
        if (null == _bufHandler)
            throw new IOException("No internal IMAP4 buffer handler to skip from");

        long    bufSkip=_bufHandler.skipData(skipSize);
        if (bufSkip < skipSize)
            bufSkip += super.skipProtocolData(skipSize - bufSkip);

        return bufSkip;
    }
    /**
     * Special helper for parsing headers data
     * @param conn input for filling parse buffer if necessary
     * @param rspHandler response handler object
     * @param msgSeqNo message sequence number for which the headers are being parsed
     * @param msgPart message part to which these header pertain
     * @param litSize number of characters in headers data
     * @return next index in parse buffer to be used (<0 if error)
     * @throws IOException if I/O errors
     */
    protected int handleHdrsData (final TextNetConnection conn, final IMAP4FetchResponseHandler rspHandler, final int msgSeqNo, final String msgPart, final long litSize) throws IOException
    {
        int    nextPos=_bufHandler.handleHdrsData(conn, rspHandler, msgSeqNo, msgPart, litSize);
        if (nextPos < 0)
            return nextPos;

        // re-update the current parsing helper object
        if (null == (_psHelper=_bufHandler.reReadFetchBufferLine(conn, nextPos, true)))
            return (-991);

        return _psHelper.getStartIndex();
    }
    /**
     * Special helper for extracting part data
     * @param conn input for filling parse buffer if necessary
     * @param rspHandler response handler object
     * @param msgSeqNo message sequence number for which the headers are being parsed
     * @param msgPart message part to which these header pertain
     * @param litSize number of characters in headers data
     * @return next index in parse buffer to be used (<0 if error)
     * @throws IOException if I/O errors
     */
    protected int handlePartData (final TextNetConnection conn, final IMAP4FetchResponseHandler rspHandler, final int msgSeqNo, final String msgPart, final long litSize) throws IOException
    {
        int    nextPos=_bufHandler.handlePartData(conn, rspHandler, msgSeqNo, msgPart, litSize);
        if (nextPos < 0)
            return nextPos;

        // re-update the current parsing helper object
        if (null == (_psHelper=_bufHandler.reReadFetchBufferLine(conn, nextPos, true)))
            return (-991);

        return _psHelper.getStartIndex();
    }
    /**
     * @return TRUE if currently cached response data contains the terminating CRLF
     */
    protected boolean haveCRLF ()
    {
        return _bufHandler.haveCRLF();
    }
    /**
     * Reads more data into the fetch buffer until CR/LF (or exhausted available space)
     * @param curPos if non-zero, then first index from where data in buffer is to be preserved
     * @param okIfCRLF if FALSE and CRLF already read, then error is returned
     * @return parsable string line
     * @throws java.io.IOException if errors while reading from input
     */
    protected ParsableString reReadFetchBufferLine (final int curPos, final boolean okIfCRLF) throws IOException
    {
        if (null == (_psHelper=_bufHandler.reReadFetchBufferLine(getConnection(), curPos, okIfCRLF)))
            return null;
        else
            return _psHelper;
    }
    /**
     * Reads a line - up to CR/LF and returns its contents
     * @return parsable string line if successful - Note: the line may/not have ended if LF...
     * @throws IOException if errors encountered
     */
    @Override
    protected ParsableString readFetchBufferLine () throws IOException
    {
        return reReadFetchBufferLine((null == _psHelper) ? 0 : _psHelper.getMaxIndex(), true);
    }
    /**
     * Keeps reading data into the parse buffer until some non-empty data is read
     * @param startPos index within current parse buffer where non-empty data is expected. If no non-empty
     * data is found, then the buffer is re-filled until non-empty data is found
     * @return index of first non empty data in parse buffer if successful (<0 if error)
     * @throws IOException if I/O error found
     */
    protected int getNonEmptyParseBufferData (int startPos) throws IOException
    {
        int retPos=(-1);

        if (_psHelper != null)
        {
            if (((retPos=_psHelper.findNonEmptyDataStart(startPos)) < _psHelper.getMaxIndex()) && (retPos >= startPos))
                return retPos;
        }

        /*      This is actually a FOREVER loop, but we do not expect 32K responses (we expect to time-out before that).
         * We use this to only to break infinite loops that may occur due to coding errors.
         */
        for (int    rspIndex=0; rspIndex < Short.MAX_VALUE; rspIndex++)
        {
            if (null == (_psHelper=readFetchBufferLine()))
                return (-307);

            if (((retPos=_psHelper.findNonEmptyDataStart()) >= _psHelper.getStartIndex()) && (retPos < _psHelper.getMaxIndex()))
                return retPos;
        }

        throw new IMAP4RspHandleException("Virtually infinite loop while trying to re-fill non-empty parse buffer");
    }
    /**
     * Makes sure that the parse buffer contains non-empty data, and attempts to fill in up to CRLF (if not already have it)
     * @param startPos index within current parse buffer where non-empty data is expected. If no non-empty
     * data is found, then the buffer is re-filled until non-empty data is found
     * @param maxExpLen maximum needed number of characters for correct parsing - if more than this number already found in
     * parse buffer (or have CRLF), then nothing is done. Otherwise, the buffer is "shifted" and more data is read. A (<0)
     * value is interpreted as "unknown" - i.e., the buffer is "shifted" anyway.
     * @return index of first non empty data in parse buffer if successful (<0 if error)
     * @throws IOException if I/O errors
     */
    protected int ensureParseBufferData (final int startPos, final int maxExpLen) throws IOException
    {
        int    retPos=getNonEmptyParseBufferData(startPos),
            startIndex=_psHelper.getStartIndex(),
            maxIndex=_psHelper.getMaxIndex();
        if ((retPos < startIndex) || (retPos >= maxIndex))
            return adjustErr(retPos);

        // if not at start of parse buffer and not have CRLF, then read more (hopefully till CRLF this time)
        if ((retPos > startIndex) && (!haveCRLF()))
        {
            // check if have enough data in buffer to accommodate required size
            if (maxExpLen >= 0)
            {
                int    curSize=(maxIndex - retPos);
                if (curSize >= maxExpLen)
                    return retPos;
            }

            if (null == (_psHelper=reReadFetchBufferLine(retPos, false)))
                return (-302);

            return _psHelper.getStartIndex();
        }

        return retPos;
    }
    /**
     * Finds first non-empty position in parse buffer starting from specified offset.
     * @param startPos start index in parse buffer to look at (inclusive)
     * @return next index in parse buffer of first "empty" location (<0 if error). Note: if
     * entire parse buffer exhausted before found non-empty character and CRLF has not been
     * read then error returned
     */
    protected int ensureParseBufferDataEnd (int startPos)
    {
        int    endPos=_psHelper.findNonEmptyDataEnd(startPos), maxIndex=_psHelper.getMaxIndex();
        if ((endPos < startPos) || (endPos > maxIndex))
            return (-661);
        // if exhausted parse buffer, then make sure we have the CRLF
        if ((endPos == maxIndex) && (!haveCRLF()))
            return (-662);

        return endPos;
    }
    /**
     * Skips input till CRLF found (unless already seen it)
     * @param leaveMark if TRUE, then  <I>"haveCRLF"</I> will report TRUE even AFTER finding CRLF. Otherwise,
     * the mark will be reset and  <I>"haveCRLF"</I> will report FALSE
     * @return 0 if successful
     * @throws IOException
     */
    protected int skipTillCRLF (final boolean leaveMark) throws IOException
    {
        return _bufHandler.skipTillCRLF(getConnection(), leaveMark);
    }
    /**
     * Special class used to returns BOTH an error and a position in the parse buffer
     */
    protected static final class TrackingInfo {
        public int err=(-1);
        /**
         * Position in parse buffer for continuing parsing
         */
        public int startPos=(-1);
        /**
         * Special error returned to denote the fact that the atom in the parse buffer is NOT the NIL atom
         * @see AbstractIMAP4UntaggedResponseHandlerHelper#checkNILParseBuffer(int startPos)
         */
        public static final int ENIL=(-1704169);
        /**
         * @return TRUE if NIL atom detected
         */
        public boolean isOK ()
        {
            return (0 == err);
        }
        /**
         * Initializes object to bad/illegal values
         */
        public void reset ()
        {
            err = (-1);
            startPos = (-1);
        }
        /**
         * Updates the contents of the tracker object
         * @param updErr error code
         * @param updStartPos position
         * @return THIS object
         */
        public TrackingInfo update (int updErr, int updStartPos)
        {
            err = adjustErr(updErr);
            startPos = updStartPos;
            return this;
        }
        /**
         * Updates the error
         * @param updErr error code
         * @return THIS object
         */
        public TrackingInfo updateErr (int updErr)
        {
            err = adjustErr(updErr);
            return this;
        }
    }
    /**
     * Tracking info object - auto-allocated by demand
     */
    private TrackingInfo    trackInfo /* =null */;
    // @see #trackInfo
    protected TrackingInfo getTrackInfo ()
    {
        if (null == trackInfo)
            trackInfo = new TrackingInfo();

        trackInfo.reset();
        return trackInfo;
    }
    /**
     * Checks if the NIL atom appears in the parsing buffer
     * @param startPos start index from to which to start checking (inclusive)
     * @return index of next character AFTER the NIL atom and error if not a NIL atom
     * @throws IOException if errors while reading from input
     * @see TrackingInfo
     */
    protected TrackingInfo checkNILParseBuffer (int startPos) throws IOException
    {
        TrackingInfo    trkInfo=getTrackInfo();
        // if cannot find empty parse buffer start return the error and report 1st available index beyond current buffer size
        if ((trkInfo.startPos=ensureParseBufferData(startPos, IMAP4Protocol.IMAP4_NILChars.length+1)) < 0)
            return trkInfo.update(trkInfo.startPos, _psHelper.getMaxIndex());

        // at this point, we KNOW enough data exists in parse buffer to check if NIL atom found (see "ensureParseBufferData" documentation)
        int atomEnd=trkInfo.startPos + IMAP4Protocol.IMAP4_NILChars.length, maxIndex=_psHelper.getMaxIndex();
        if ((atomEnd > maxIndex) || (!_psHelper.compareTo(trkInfo.startPos, atomEnd, IMAP4Protocol.IMAP4_NILChars, true)))
            return trkInfo.updateErr(TrackingInfo.ENIL);

        // make sure that if more data follows the NIL, then it is either whitespace or a list end delimiter
        if ((atomEnd < maxIndex) && (!_psHelper.isEmptyChar(atomEnd)) && (_psHelper.getCharAt(atomEnd) != IMAP4Protocol.IMAP4_PARLIST_EDELIM))
            return trkInfo.updateErr(TrackingInfo.ENIL);

        return trkInfo.update(0, atomEnd);
    }
    /**
     * Make sure that atom at specified position is NIL
     * @param startPos index in parse buffer where NIL atom should be found (inclusive)
     * @return next position in parse buffer (after the NIL atom) - (<0) if error
     * @throws IOException if I/O errors
     */
    protected int skipNILParseBuffer (int startPos) throws IOException
    {
        TrackingInfo    trkInfo=checkNILParseBuffer(startPos);
        if (!trkInfo.isOK())
            return trkInfo.err;

        return trkInfo.startPos;
    }
    /**
     * Extracts a literal count - Note: flushes parse buffer till CRLF, so next
     * position for parsing should be 0
     * @param startPos position in parse buffer where literal count is to be found (inclusive) - may be either
     * the '(' or the 1st digit
     * @return extracted literal count (or <0 if error)
     * @throws IOException if I/O errors
     */
    protected long extractLiteralCount (final int startPos) throws IOException
    {
        // check if have octet count delimiter end in parse buffer
        int    valStart=startPos, valEnd=_psHelper.indexOf(IMAP4Protocol.IMAP4_OCTCNT_EDELIM,valStart), maxIndex=_psHelper.getMaxIndex(), nErr=0;
        if ((valEnd <= valStart) || (valEnd >= maxIndex))
        {
            // if already have CRLF, re-filling will not help
            if (null == (_psHelper=reReadFetchBufferLine(valStart, false)))
                return (-55L);

            valStart = _psHelper.getStartIndex();
            maxIndex = _psHelper.getMaxIndex();

            // after re-fetching more data we MUST have the octet count delimiter end
            if (((valEnd=_psHelper.indexOf(IMAP4Protocol.IMAP4_OCTCNT_EDELIM, valStart)) <= valStart) || (valEnd >= maxIndex))
                return (-55);
        }

        // skip octet count delimiter
        if (IMAP4Protocol.IMAP4_OCTCNT_SDELIM == _psHelper.getCharAt(valStart))
            valStart++;

        long    litSize=0L;
        try
        {
            litSize = _psHelper.getUnsignedLong(valStart, valEnd);
        }
        catch(NumberFormatException nfe)
        {
            throw new IMAP4RspHandleException("Cannot extract literal count: " + nfe.getMessage());
        }

        // make sure we start reading from next line
        if ((nErr=skipTillCRLF(true)) != 0)
            return adjustErr(nErr);

        return litSize;
    }
    /**
     * Extracts a literal count header data and returns its start/end index in the parse buffer. If the data cannot
     * be contained in the parse buffer, and overflow is allowed, then only the starting characters of the
     * data are returned (as much as the parse buffer allows), and the rest are discarded. Otherwise, an ERROR is
     * returned.
     * @param startPos start position in parse buffer where information should be retrieved from (inclusive)
     * @param allowOverflow if TRUE, then if the parsing buffer cannot accomodate the full value, no error is returned
     * @return literal value if successful - Note: returned data may have a zero-length if no data
     * @throws IOException if I/O errors encountered
     */
    protected IMAP4ParseAtomValue extractLiteralHdrVal (final int startPos, final boolean allowOverflow) throws IOException
    {

        final long    litSize=extractLiteralCount(startPos);
        if (litSize < 0L)
            throw new IMAP4RspHandleException("Cannot extract literal count from parse buffer");

        return _bufHandler.fillData(getConnection(), _psHelper, litSize, allowOverflow);
    }
    /**
     * Special token used for partial parse buffer information extraction - auto-allocated by demand
     */
    private ParsableString.SplitInfo    spToken=null;
    // @see #spToken
    private ParsableString.SplitInfo getSplitToken ()
    {
        if (null == spToken)
            spToken = new ParsableString.SplitInfo();
        return spToken;
    }
    /**
     * Extracts a string delimited by the specified character
     * @param startPos position where to start looking for delimiter end (inclusive)
     * @param delim delimiter to be used as value end marker
     * @param allowOverflow if TRUE and cannot accomodate value in parse buffer, then rest is ignored. Otherwise
     * ERROR value is returned
     * the quote delimiter in the string. The NEXT position for parsing should be (+1) of this value
     * @return string header data - Note: returned data may have a zero-length if no data or NIL atom found
     * @throws IOException if I/O errors encountered
     */
    protected IMAP4ParseAtomValue extractDelimitedStringHdrVal (final int startPos, final char delim, final boolean allowOverflow) throws IOException
    {
        ParsableString.SplitInfo    spInfo=getSplitToken();
        int maxIndex=_psHelper.getMaxIndex();

        spInfo.valStart = startPos;

        // check if have the delimiter in the current parse buffer
        if (((spInfo.valEnd=_psHelper.indexOf(delim, spInfo.valStart)) < spInfo.valStart) || (spInfo.valEnd >= maxIndex))
        {
            // if already have CRLF, re-filling will not help
            if (null == (_psHelper=reReadFetchBufferLine(startPos, false)))
                throw new IMAP4RspHandleException("Cannot re-fill parse buffer while looking for delim=" + delim);

            spInfo.valStart = _psHelper.getStartIndex();
            maxIndex = _psHelper.getMaxIndex();

            // check if re-filling the parse buffer helped
            if (((spInfo.valEnd=_psHelper.indexOf(delim, spInfo.valStart)) < spInfo.valStart) || (spInfo.valEnd >= maxIndex))
            {
                // if not allowed to discard the rest, then return an error
                if (!allowOverflow)
                    throw new IMAP4RspHandleException("Cannot accommodate value in parse buffer while looking for delim=" + delim);

                // skip characters till delimiter found (NOTE: we limit ourselves to ~32K characters to skip
                final TextNetConnection    conn=getConnection();
                for (int    rspIndex=0, v=conn.read(); rspIndex < Short.MAX_VALUE; rspIndex++, v=conn.read())
                {
                    if ((-1) == v)  // not expecting EOF !!!
                        throw new IMAP4RspHandleException("Premature EOF while looking for delim=" + delim);
                    if (('\n' == v) || ('\r' == v))    // not expecting CR/LF
                        throw new IMAP4RspHandleException("Premature CR/LF while looking for delim=" + delim);

                    if (delim == v)
                    {
                        spInfo.endDelim = delim;
                        break;
                    }
                }

                // make sure exited due to finding the delimiter
                if (spInfo.endDelim != delim)
                    throw new IMAP4RspHandleException("Virtual infinite loop exit while looking for delim=" + delim);

                // "simulate" end of value at end of parse buffer
                spInfo.valEnd = maxIndex;
            }
        }
        else
            spInfo.endDelim = delim;

        IMAP4ParseAtomValue    aVal=_bufHandler.getAtomValue();
        aVal.val = _psHelper.subParse(spInfo);
        // if actually found the delimiter, then skip to next index, otherwise start from maximum currently available data
        aVal.startPos = (spInfo.valEnd < maxIndex) ? spInfo.valEnd + 1 : maxIndex;

        return aVal;
    }
    /**
     * Special object used to return empty value (e.g. for NIL atom) - lazily-allocated
     */
    private ParsableString    emptyPS=null;
    // @see #emptyPS
    protected ParsableString getEmptyParsableString ()
    {
        if (null == emptyPS)
            emptyPS = new ParsableString();
        else
            emptyPS.reset();    // just making sure

        return emptyPS;
    }
    /**
     * Extracts a string header value from the parse string
     * @param startPos start position in parse buffer where information should be retrieved from (inclusive)
     * @param allowOverflow if TRUE, then if the parsing buffer cannot accomodate the full value, no error is returned
     * @return atom value if successful
     * @throws IOException if I/O errors encountered
     * @see #extractDelimitedStringHdrVal(int startPos, char delim, boolean allowOverflow)
     * @see #extractLiteralHdrVal(int startPos, boolean allowOverflow)
     */
    protected IMAP4ParseAtomValue extractStringHdrVal (final int startPos, final boolean allowOverflow) throws IOException
    {
        final ParsableString.SplitInfo spInfo=getSplitToken();
        if ((spInfo.valStart=ensureParseBufferData(startPos, 1)) < 0)
            throw new IMAP4RspHandleException("Cannot find start of string hdr value");

        // handle literal header value
        if (IMAP4Protocol.IMAP4_OCTCNT_SDELIM == _psHelper.getCharAt(spInfo.valStart))
            return extractLiteralHdrVal(spInfo.valStart, allowOverflow);

        // if this is a NIL atom then return an "empty" atom info
        final TrackingInfo    trkInfo=checkNILParseBuffer(spInfo.valStart);
        if (trkInfo.isOK())
        {
            final IMAP4ParseAtomValue    aVal=_bufHandler.getAtomValue();
            aVal.val = getEmptyParsableString();
            aVal.startPos = trkInfo.startPos;

            return aVal;
        }
        else
            spInfo.valStart = trkInfo.startPos;

        // check if this is a delimited value
        if (IMAP4Protocol.IMAP4_QUOTE_DELIM == _psHelper.getCharAt(spInfo.valStart))
            return extractDelimitedStringHdrVal(spInfo.valStart+1, IMAP4Protocol.IMAP4_QUOTE_DELIM, allowOverflow);

        // if unquoted and don't have the CRLF, then fill in the parse buffer
        int    maxIndex=_psHelper.getMaxIndex();
        if (!haveCRLF())
        {
            if (null == (_psHelper=reReadFetchBufferLine(spInfo.valStart, false)))
                throw new IMAP4RspHandleException("Cannot re-fill parse buffer for extracting data");

            maxIndex = _psHelper.getMaxIndex();

            // now that we re-filled, re-update the start index
            if (((spInfo.valStart=_psHelper.findNonEmptyDataStart()) < _psHelper.getStartIndex()) || (spInfo.valStart >= maxIndex))
                throw new IMAP4RspHandleException("Cannot find non-empty data start on extracting hdr data");
        }

        // check if can find a whitespace data in parse buffer
        if (((spInfo.valEnd=_psHelper.findNonEmptyDataEnd(spInfo.valStart+1)) <= spInfo.valStart) || (spInfo.valEnd > maxIndex))
            /* NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!! NOTE !!!
             *         Theoretically, we could have allowed for overflow, but it is rather complex
             * in view of the need to handle ')' and/or CRLF in a special way (Hint: need to "push"
             * them back into the parse buffer if found). For now, since we do not expect large
             * un-quoted values, we throw this exception
             */
            throw new IMAP4RspHandleException("Cannot accomodate un-quoted data in buffer");

        // initialize end delimiter if not already at end of buffer
        if (spInfo.valEnd < maxIndex)
            spInfo.endDelim = _psHelper.getCharAt(spInfo.valEnd);

        // if un-quoted (non-empty) and ending in ')', then assume the ')' is NOT part of the value
        if (IMAP4Protocol.IMAP4_PARLIST_EDELIM == _psHelper.getCharAt(spInfo.valEnd-1))
        {
            spInfo.valEnd--;
            spInfo.endDelim = IMAP4Protocol.IMAP4_PARLIST_EDELIM;
        }

        final IMAP4ParseAtomValue    aVal=_bufHandler.getAtomValue();
        aVal.val = _psHelper.subParse(spInfo);
        aVal.startPos = spInfo.valEnd;

        return aVal;
    }
    /**
     * Extracts a list of flags that may be either NIL or '()' delimited
     * @param startPos index in parse buffer where to start looking for (inclusive)
     * @param flags (in/out) collection of String(s) to be updated with the found flags (nothing done if no flags found)
     * @return index of next character in parse buffer (after the flags) - or (<0) if error
     * @throws IOException if I/O errors
     */
    protected int extractFlagsList (final int startPos, final Collection<String> flags) throws IOException
    {
        int listStart=ensureParseBufferData(startPos,1);
        if (listStart < 0)
            return listStart;

        if (_psHelper.getCharAt(listStart) != IMAP4Protocol.IMAP4_PARLIST_SDELIM)
            return skipNILParseBuffer(listStart);

        // list MUST end in ')'
        int listEnd=_psHelper.indexOf(IMAP4Protocol.IMAP4_PARLIST_EDELIM, (listStart+1)), maxIndex=_psHelper.getMaxIndex();
        if ((listEnd <= listStart) || (listEnd >= maxIndex))
        {
            // if not found list end delimiter, then try re-filling the parse buffer - maybe this will help
            if (null == (_psHelper=reReadFetchBufferLine(listStart, false)))
                return adjustErr(-249);

            listStart = _psHelper.getStartIndex();
            maxIndex = _psHelper.getMaxIndex();

            // now make one more try
            if (((listEnd=_psHelper.indexOf(IMAP4Protocol.IMAP4_PARLIST_EDELIM, (listStart+1))) <= listStart) || (listEnd >= maxIndex))
                return (-250);
        }

        try
        {
            final Collection<String>    vals=IMAP4FlagValue.getFlags(_psHelper, listStart, listEnd);
            final int                    numFlags=(null == vals) ? 0 : vals.size();
            if (numFlags > 0)
                flags.addAll(vals);
        }
        catch (UTFDataFormatException e)
        {
            return (-251);  // should not happen
        }

        return (listEnd + 1);  // skip terminating ')'
    }
    /**
     * Helper class used to return simple numbers
     * @author lyorg
     */
    protected static final class NumInfo {
        /**
         * Number value
         */
        public long num=0L;
        /**
         * Position info
         */
        public int    startPos=(-1);
        /**
         * Resets the contents to bad/illegal values
         */
        public void reset ()
        {
            num = 0L;
            startPos = (-1);
        }
    }
    /**
     * Extracts a "simple" number (i.e. only digits, and NO enclosing quotes) from the parsing buffer
     * @param startPos index in parse buffer where to start looking for the number (inclusive)
     * @param numInfo
     * @return 0 if successful
     * @throws IOException if I/O errors
     * @see NumInfo
     */
    protected int extractSimpleNumber (final int startPos, final NumInfo numInfo) throws IOException
    {
        numInfo.reset();

        // make sure we have FULL value in parse buffer
        int    numStart=ensureParseBufferData(startPos, NumberTables.MAX_UNSIGNED_LONG_DIGITS_NUM+1), maxIndex=_psHelper.getMaxIndex();
        if ((numStart < _psHelper.getStartIndex()) || (numStart >= maxIndex))
            return (-352);

        // we KNOW that entire value should be in parse buffer, so end of value MUST be in current buffer
        // chek if terminating character is ')' (can happen if this is LAST member) - if so, then do not take it into account
        numInfo.startPos = _psHelper.findNumberEnd(numStart+1);

        try
        {
            numInfo.num = _psHelper.getUnsignedLong(numStart, numInfo.startPos);
        }
        catch(NumberFormatException nfe)
        {
            return (-354);    // should not happen
        }

        return 0;
    }
    /**
     * Cached helper object - auto-allocated
     */
    private NumInfo    ni=null;
    // @see #ni
    protected NumInfo getNumInfo ()
    {
        if (null == ni)
            ni = new NumInfo();
        else
            ni.reset();

        return ni;
    }
    /**
     * Extracts a "simple" number (i.e. only digits, and NO enclosing quotes) from the parsing buffer
     * @param startPos index in parse buffer where to start looking for the number (inclusive)
     * @return extracted number information
     * @throws IOException if I/O errors
     * @see NumInfo
     */
    protected NumInfo extractSimpleNumber (final int startPos) throws IOException
    {
        final NumInfo    numInfo=getNumInfo();
        final int        nErr=extractSimpleNumber(startPos, numInfo);
        if (nErr != 0)
            return null;
        else
            return numInfo;
    }
    /**
     * Class used to throw a parsing exception AFTER having processed the
     * final tagged response. It is used when some intermediate parsing
     * error occurred, but we recovered on next CRLF.
     * @author lyorg
     * 01/04/2004
     */
    public static class IMAP4FinalRspException extends IMAP4RspHandleException {
        /**
         *
         */
        private static final long serialVersionUID = 249171032813322015L;
        protected IMAP4TaggedResponse rsp /* =null */;

        public IMAP4FinalRspException (IMAP4TaggedResponse rspVal, IMAP4RspHandleException rspExc)
        {
            super(rspExc.getMessage());

            if (null == (this.rsp=rspVal))
                throw new IllegalArgumentException("No final response supplied");
        }

        public IMAP4TaggedResponse getResponse ()
        {
            return rsp;
        }
    }
    // override base class method due to special nature of handling
    public IMAP4TaggedResponse handleResponse (final int tagValue) throws IOException
    {
        if (tagValue < 0)
            throw new IMAP4RspHandleException("Bad/Illegal TAG value: " + tagValue);

        final int    minTaggedRspLen=String.valueOf(tagValue).length() + 1 + IMAP4TaggedResponse.IMAP4_BADChars.length,
                    minRespLen=Math.max(IMAP4FolderSelectionInfo.IMAP4_PERMANENTFLAGSChars.length + 2,minTaggedRspLen);
        IMAP4RspHandleException    rspExc=null;    // will be set if intermediate error found

        // we limit the maximum handled response to ~2GB just to make sure no infinite loops occur - we fully
        // expect either to find a tagged response ro to time-out
        for (int    nIndex=0, nErr=0; nIndex < (Integer.MAX_VALUE-1); nIndex++, nErr=0 /* just make sure we start fresh */)
        {
            if (null == (_psHelper=readFetchBufferLine()))
                throw new IMAP4RspHandleException("Cannot fetch response buffer line #" + nIndex + " err=" + nErr);

            // ensure have enough data for a reasonable response
            {
                final int    psLen=_psHelper.length();
                if (psLen < minRespLen)
                {
                    if (!haveCRLF())
                        throw new IMAP4RspHandleException("Not enough data read for a response: " + _psHelper.length());
                    // skip empty lines - may be leftovers from previous parsing error
                    if (psLen <= 0)
                        continue;
                }
            }
            final int    startIndex=_psHelper.getStartIndex();

            // if this is not an untagged response, then check if it is the tag we seek
            if (IMAP4Protocol.IMAP4_UNTAGGED_RSP != _psHelper.getCharAt(startIndex))
            {
                final IMAP4TaggedResponse cachedRsp=getResponse(), rsp=IMAP4TaggedResponse.getFinalResponse(tagValue, _psHelper, cachedRsp);
                if (rsp != null)
                {
                    if (cachedRsp != rsp)    // check if using the same initialized instance
                        cachedRsp.update(rsp);

                    if ((nErr=skipTillCRLF(false)) != 0)
                    {
                        if (null == rspExc)
                            rspExc = new IMAP4RspHandleException("Failed (err=" + nErr + ") to skip till CRLF of tagged response");
                    }

                    // if got final response but had an intermediate exception, then throw it.
                    if (rspExc != null)
                        throw new IMAP4FinalRspException(cachedRsp, rspExc);

                    return cachedRsp;
                }
            }
            else    // untagged response - check if this is a response start
            {
                if ((nErr=handleUntaggedResponse(_psHelper, _psHelper.findNonEmptyDataStart(startIndex+2))) != 0)
                {
                    // if error, then remember this and try to recover anyway on next CRLF
                    if (null == rspExc)
                        rspExc = new IMAP4RspHandleException("Failed (err=" + nErr + ") to parse response=" + _psHelper);
                }

                if ((nErr=skipTillCRLF(false)) != 0)
                    throw new IMAP4RspHandleException("Failed (err=" + nErr + ") to skip till CRLF on parsing untagged response");
            }
        }

        throw new IMAP4RspHandleException("Virtual infinite loop exit on responses handling");
    }
}
