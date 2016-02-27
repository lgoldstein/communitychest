package net.community.chest.net.proto.text.imap4;

import java.io.IOException;

import net.community.chest.ParsableString;
import net.community.chest.net.TextNetConnection;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 24, 2008 2:23:47 PM
 */
public class IMAP4ParseBufferHandler {
	/**
	 * Holds TRUE if when filling the buffer we read the LF
	 */
	private boolean _readCRLF	/* =false */;
	/**
	 * @return TRUE if currently cached response data contains the terminating CRLF
	 */
	public boolean haveCRLF ()
	{
		return _readCRLF;
	}
	/**
	 * Parsing fetch buffer cache
	 */
	private char[] _parseBuf	/* =null */;
	/**
	 * Current number of characters in parse buffer
	 */
	private int _parseBufLen	/* =0 */;
	/**
	 * Last parsed position in buffer
	 */
	private int _lastParsedPos	/* =0 */;
	/**
	 * Size of parse buffer to allocate
	 */
	public static final int MAX_FETCH_RSP_BUF_LEN=1536;	// ~ Ethernet packet size
	/**
	 * Skips up to specified number of characters from buffer - if less
	 * than required number available, then skips whatever it can. 
	 * @param skipSize required number of characters to skip
	 * @return actual number of skipped characters 
	 */
	public long skipData (long skipSize)
	{
		if (skipSize <= 0L)	// should not be <0 but what the heck...
			return skipSize;

		final int	availLen=_parseBufLen - _lastParsedPos;
		if (availLen <= 0)	// should not be <0 but what the heck...
			return 0L;
		
		final long	availSkip=Math.min(availLen, skipSize);
		_lastParsedPos += (int) availSkip;

		return availSkip;
	}
	/**
	 * Checks if current parse buffer contains a line
	 * @return parsable string for the line (null if no line found)
	 */
	private ParsableString getParsableLine ()
	{
		for (int	lineEnd=_lastParsedPos, index=_lastParsedPos; index < _parseBufLen; index++)
		{
			if ('\r' == _parseBuf[index])	// skip CR
				continue;

			if ('\n' == _parseBuf[index])
			{
				final int	lineLen=(lineEnd - _lastParsedPos);
				// if have CR before the LF, then remove it as well
				if ((lineLen > 0) && ('\r' == _parseBuf[index-1]))
					_parseBuf[index-1] = '\0';
			
				_readCRLF = true;
				_parseBuf[index] = '\0';

				final ParsableString	ps=new ParsableString(_parseBuf, _lastParsedPos, lineLen);
				_lastParsedPos = (index + 1);
				return ps;
			}
			
			lineEnd++;
		}

		// this point is reached if no CRLF found
		return null;
	}
	/**
	 * "Shifts" the data "down" starting at give index
	 * @param startIndex index from which to "shift" down (inclusive)
	 */
	private void compactBuffer (final int startIndex)
	{
		final int	bufStartIndex=Math.max(_lastParsedPos,startIndex);	// if start index below last parsed, then start from last parsed
		if (0 == bufStartIndex)	// do nothing if already at start of buffer
			return;

		if (bufStartIndex < _parseBufLen)
		{	
			_parseBufLen -= bufStartIndex;
			System.arraycopy(_parseBuf, bufStartIndex, _parseBuf, 0, _parseBufLen);
		}
		else
			_parseBufLen = 0;

		_parseBuf[_parseBufLen] = '\0';	// safety measure
		_lastParsedPos = 0;	// last parsed position is start of buffer because we shifted down
	}
	/**
	 * Reads more data into the fetch buffer until CR/LF (or exhausted available space)
	 * @param conn reader from which to retrieve more data if necessary
	 * @param curPos if non-zero, then first index from where data in buffer is to be preserved
	 * @param okIfCRLF if FALSE and CRLF already read, then error is returned
	 * @return parsable string for the line (or null if error)
	 * @throws java.io.IOException if errors while reading from input
	 */
	public ParsableString reReadFetchBufferLine (final TextNetConnection conn, final int curPos, final boolean okIfCRLF) throws IOException
	{
		if ((!okIfCRLF) && _readCRLF)
			throw new IMAP4RspHandleException("Unexpected CR/LF on re-filling parse buffer");

		// check if have an existing line
		ParsableString	ps=getParsableLine();
		if (ps != null)
			return ps;

		compactBuffer(curPos);

		if (null == _parseBuf)    // we allocate a little bit more for special usages
			_parseBuf = new char[MAX_FETCH_RSP_BUF_LEN+4];

		_readCRLF = false;   // reset the value since reading anew
		
		// start filling from wherever last time we left off
		for (int    readLen=MAX_FETCH_RSP_BUF_LEN - _parseBufLen, readIndex=0; (_parseBufLen < MAX_FETCH_RSP_BUF_LEN) && (readIndex < Short.MAX_VALUE); readLen=MAX_FETCH_RSP_BUF_LEN - _parseBufLen)
		{
			int	nChars=conn.read(_parseBuf, _parseBufLen, readLen);
			if (nChars <= 0)
				throw new IMAP4RspHandleException("Bad/Illegal number of read characters in parse buffer: " + nChars);
			_parseBufLen += nChars;

			if ((ps=getParsableLine()) != null)
				return ps;
		}

		// this point is reached if exhausted buffer BEFORE finding CR/LF
		if (_parseBufLen < MAX_FETCH_RSP_BUF_LEN)	// make sure exited because filled buffer
			throw new IMAP4RspHandleException("Virtually infinite read loop exit");

		_parseBuf[MAX_FETCH_RSP_BUF_LEN] = '\0';

		return new ParsableString(_parseBuf, 0, MAX_FETCH_RSP_BUF_LEN);
	}
	/**
	 * Skips input till CRLF found (unless already seen it)
	 * @param conn reader from which to read data
	 * @param leaveMark if TRUE, then  <I>"haveCRLF"</I> will report TRUE even AFTER finding CRLF. Otherwise,
	 * the mark will be reset and  <I>"haveCRLF"</I> will report FALSE
	 * @return 0 if successful
	 * @throws IOException
	 */
	public int skipTillCRLF (final TextNetConnection conn, final boolean leaveMark) throws IOException
	{
		if (_readCRLF)
		{	
			_readCRLF = leaveMark;
			return 0;
		}

		// skip till first CRLF or space character (we limit the loop to ~23K to break infinite loops
		for (int	rspIndex=0; rspIndex < Short.MAX_VALUE; rspIndex++)
		{
			// find out if have LF in current buffer data
			for (; _lastParsedPos < _parseBufLen; _lastParsedPos++)
			{
				if ('\n' == _parseBuf[_lastParsedPos])
				{
					_readCRLF = leaveMark;
					_parseBuf[_lastParsedPos] = '\0';
					_lastParsedPos++;
					return 0;
				}
			}

			if ((_parseBufLen=conn.read(_parseBuf, 0, MAX_FETCH_RSP_BUF_LEN)) <= 0)
				return (-951);
			
			_lastParsedPos = 0;
		}

		// this point is reached if virtually infinite loop end reached
		throw new IMAP4RspHandleException("Virtually infinite loop exit while skipping till CRLF");
	}
	// lazy allocation
	private IMAP4ParseAtomValue	atomValue	/* =null */;
	// @see #atomValue
	public IMAP4ParseAtomValue getAtomValue ()
	{
		if (null == atomValue)
			atomValue = new IMAP4ParseAtomValue();
		else
			atomValue.reset();
		
		return atomValue;
	}
	/**
	 * Fills the fetch buffer with enough data as specified (if not already have it)
	 * @param conn input to read from
	 * @param ps parsing buffer defined over the buffer
	 * @param dataSize requested number of characters 
	 * @param allowOverflow if TRUE, and the whole buffer cannot accomodate the required data size,
	 * then the start of the data is read, and the rest is skipped
	 * @return literal value object containing the requested data
	 * @throws IOException if errors encountered
	 */
	public IMAP4ParseAtomValue fillData (final TextNetConnection conn, final ParsableString ps, final long dataSize, final boolean allowOverflow) throws IOException
	{
		final int	maxFill=(int) Math.min(MAX_FETCH_RSP_BUF_LEN, dataSize);
		int			remLen=(_lastParsedPos <= _parseBufLen) ? (_parseBufLen - _lastParsedPos) : 0;

		// check if already have enough data to accomodate
		if (remLen < dataSize)
		{
			// if not enough data, then check if we can compact the buffer and make some room
			compactBuffer(_lastParsedPos);

			// after making some room, try to fill as much as possible
			for (int	fillIndex=0; (fillIndex < Short.MAX_VALUE) && (_parseBufLen < maxFill); fillIndex++)
			{
				final int	nChars=conn.read(_parseBuf, _parseBufLen, (maxFill - _parseBufLen));
				if (nChars <= 0)
					throw new IMAP4RspHandleException("Zero/Negative read len while attempting to fill data: " + nChars);
				
				_parseBufLen += nChars;
			}

			// make sure exited because filled enough data
			if (_parseBufLen < maxFill)
				throw new IMAP4RspHandleException("Virtual infinite loop exit while attempting to fill data");

			// check if at end we can accommodate required data size
			if (maxFill < dataSize)
			{
				if (!allowOverflow)
					throw new IMAP4RspHandleException("Cannot accommodate " + dataSize + " literal data in buffer");

				// ignore unread size
				for (long	lDataToSkip=dataSize - maxFill; lDataToSkip > 0L; )
				{	
					final long	lSkipped=conn.skip(lDataToSkip);
					if (lSkipped <= 0L)
						throw new IOException("Cannot skip " + lDataToSkip + " bytes - skipped " + lSkipped); 

					lDataToSkip -= lSkipped;
				}
			}

			// update the parse string since we may have compacted it
			if (!ps.wrap(_parseBuf, _lastParsedPos, _parseBufLen))	// should not happen
				throw new IMAP4RspHandleException("Cannot re-wrap IMAP4 parse buffer");
		}

		// at this stage we know that we can accomodate "maxFill" characters starting at "lastParsedPos"
		final IMAP4ParseAtomValue	aVal=getAtomValue();
		aVal.val = new ParsableString(_parseBuf, _lastParsedPos, maxFill);
		_lastParsedPos += maxFill;
		aVal.startPos = _lastParsedPos; 

		return aVal;
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
	int handleHdrsData (final TextNetConnection conn, final IMAP4FetchResponseHandler rspHandler, final int msgSeqNo, final String msgPart, final long litSize) throws IOException
	{
		// if not enough data, then return max. index to force re-read
		if (litSize <= 2)	// a header data must be AT LEAST 3 characters long ("A:" + [CR] + LF)
		{	
			_lastParsedPos += (int) litSize;
			return _lastParsedPos;
		}

		String	lastHdr=null;
		long	dataToParse=litSize;
		boolean	fPartStage=false;
		int		nErr=0;

		// we allow for ~32K headers lines
		for (int rspIndex=0, curParsePos=_lastParsedPos; (dataToParse > 0) && (rspIndex < Short.MAX_VALUE); rspIndex++, curParsePos=_lastParsedPos)
		{
			ParsableString	ps=getParsableLine();	// check if have a line in memory already

			// keep trying to build a full line (if not have one already
			for ( ; null == ps; ps=getParsableLine())
			{
				// if don't have a line, then compact the buffer and read some more 
				compactBuffer(_lastParsedPos);
				curParsePos = _lastParsedPos;

				// check if used entire buffer and still have some data to parse
				if ((_parseBufLen < MAX_FETCH_RSP_BUF_LEN) && (dataToParse > 0))
				{
					final int	availLen=(MAX_FETCH_RSP_BUF_LEN - _parseBufLen);
					final int	readLen=(int) Math.min(dataToParse, availLen);
					final int	nChars=conn.read(_parseBuf, _parseBufLen, readLen);
					if (nChars <= 0)	// we expect to read something
						return (-892);

					_parseBufLen += nChars;
					_parseBuf[_parseBufLen] = '\0';	// safety measure

					continue;	// retry finding a line
				}

				/* Note: after compacting we STILL might not have a full line,
				 * so we "simulate" a line with a continuation character. Also,
				 * although we expect CRLF at end of ALL lines, should this not
				 * be TRUE, we will reach this point when the data to parse count
				 * has been exhausted
				 */
				_parseBuf[_parseBufLen] = '\t';
				_lastParsedPos = _parseBufLen;
				_parseBufLen++;
			}

			dataToParse -= (_lastParsedPos - curParsePos);

			if (ps.length() <= 0)	// skip empty lines
				continue;

			final int	startIndex=ps.getStartIndex(), maxIndex=ps.getMaxIndex();
			int			valStart=startIndex;
			final char	firstChar=ps.getCharAt(valStart);

			// check if continuation header data
			if ((' ' == firstChar) || ('\t' == firstChar))
			{
				// make sure we have a previous header name
				if ((null == lastHdr) || (0 == lastHdr.length()))
					return (-893);
				
				valStart++;
			}
			else
			{
				// find 1st argument and use it as header name
				int	nameEnd=ps.findNonEmptyDataEnd(valStart+1);
				if ((nameEnd <= valStart) || (nameEnd > maxIndex))
					return (-894);
				
				// check if have ':' "stuck" to the value next to it
				final int	colSep=ps.indexOf(':', valStart+1, nameEnd);
				if ((colSep < (nameEnd-1)) && (colSep > valStart))
				{	
					nameEnd = colSep + 1;
					valStart = nameEnd;
				}
				else	// index of ':' is EXACTLY at header name end
				{
					// skip headers that have only name but no data
					if (((valStart=ps.findNonEmptyDataStart(nameEnd)) < nameEnd) || (valStart >= maxIndex))
						continue;
				}

				lastHdr = ps.substring(startIndex, nameEnd);
				if ((null == lastHdr) || (0 == lastHdr.length()))
					return (-895);
			}

			// use first non-empty header to announce stage start
			if (!fPartStage)
			{	
				if ((nErr=rspHandler.handleMsgPartStage(msgSeqNo, msgPart, true)) != 0)
					return AbstractIMAP4UntaggedResponseHandler.adjustErr(nErr);
				fPartStage = true;
			}
			
			if (valStart >= maxIndex)	// skip headers with no value
				continue;

			final String	hdrValue=ps.substring(valStart, maxIndex);
			if ((nErr=rspHandler.handleMsgPartHeader(msgSeqNo, msgPart, lastHdr, null, hdrValue)) != 0)
				return AbstractIMAP4UntaggedResponseHandler.adjustErr(nErr);
		}

		// make sure exited due to exhausting all data and not due to virtual loop exit
		if (dataToParse != 0)
			return (-896);

		// check if need to "close" the message part stage
		if (fPartStage)
		{	
			if ((nErr=rspHandler.handleMsgPartStage(msgSeqNo, msgPart, false)) != 0)
				return AbstractIMAP4UntaggedResponseHandler.adjustErr(nErr);
		}

		return _lastParsedPos;	// resume parsing immediately after last parsed position
	}
	/**
	 * Data buffer used for pure part data - allocated by need
	 */
	private byte[] dataBuf	/* =null */;
	// @see #dataBuf
	private byte[] getDataBuffer ()
	{
		if (null == dataBuf)
			dataBuf = new byte[MAX_FETCH_RSP_BUF_LEN];
		else
			dataBuf[0] = 0;
		return dataBuf;
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
	int handlePartData (final TextNetConnection conn, final IMAP4FetchResponseHandler rspHandler, final int msgSeqNo, final String msgPart, final long litSize) throws IOException
	{
		int	nErr=rspHandler.handlePartDataStage(msgSeqNo, msgPart, true);
		if (nErr != 0)
			return AbstractIMAP4UntaggedResponseHandler.adjustErr(nErr);

		long	dataToHandle=litSize;
		byte[]	partData=getDataBuffer();
		int		fillIndex=0;
		
		// we allow for ~2GB of data in the virtually infinite loop
		for (int	rspIndex=0; rspIndex < (Integer.MAX_VALUE/MAX_FETCH_RSP_BUF_LEN) && (dataToHandle > 0); rspIndex++)
		{
			// exhaust contents of parse buffer
			for ( ; (_lastParsedPos < _parseBufLen) && (dataToHandle > 0); _lastParsedPos++, dataToHandle--)
			{	
				// convert characters (that we KNOW are 8-bit) to bytes
				// TODO need to check how non-7-bit characters behave...
				partData[fillIndex] = (byte) _parseBuf[_lastParsedPos];

				fillIndex++;
				if (fillIndex >= partData.length)
				{	
					if ((nErr=rspHandler.handlePartData(msgSeqNo, msgPart, partData, 0, partData.length)) != 0)
						return AbstractIMAP4UntaggedResponseHandler.adjustErr(nErr);
					
					fillIndex = 0;
				}
			}

			// we could have reached this point due to end of data to handle BEFORE exhausting parse buffer contents
			if (0 == dataToHandle)
				break;

			// re-fill the parse buffer
			final int	remFill=(partData.length - fillIndex);
			final int	dataLen=(int) Math.min(remFill, dataToHandle);
			final int	readLen=Math.min(dataLen, MAX_FETCH_RSP_BUF_LEN);
			if ((_parseBufLen=conn.read(_parseBuf, 0, readLen)) <= 0)
				return (-882);	// we MUST read some data

			_lastParsedPos = 0;
		}

		// make sure exited because exhausted data and NOT because of "infinite" loop exit
		if (dataToHandle != 0)
			return (-883);

		// check if have any leftovers from re-filling loop...
		if (fillIndex > 0)
		{	
			if ((nErr=rspHandler.handlePartData(msgSeqNo, msgPart, partData, 0, fillIndex)) != 0)
				return AbstractIMAP4UntaggedResponseHandler.adjustErr(nErr);
		}

		if ((nErr=rspHandler.handlePartDataStage(msgSeqNo, msgPart, false)) != 0)
			return AbstractIMAP4UntaggedResponseHandler.adjustErr(nErr);

		return _lastParsedPos;
	}
}
