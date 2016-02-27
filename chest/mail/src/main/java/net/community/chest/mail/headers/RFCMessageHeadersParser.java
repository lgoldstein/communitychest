/*
 * 
 */
package net.community.chest.mail.headers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 4, 2009 1:53:49 PM
 */
public abstract class RFCMessageHeadersParser implements RFCMessageHeadersHandler {
	/**
	 * Internal string used to hold last parsed header name
	 */
	private String	_lastHdrName	/* =null */;
	/**
	 * Last call index for current header
	 */
	private int _lastCallIndex	/* =0 */;
	/**
	 * "Closes" if necessary the previous header "stage"
	 * @param hdrName new header name (may be null/empty)
	 * @return 0 if successful
	 */
	protected int updateLastHdrState (final String hdrName)
	{
		// check if have a previous header name, and if so if differs from this one
		if ((null == _lastHdrName) || (_lastHdrName.length() <= 0) || _lastHdrName.equalsIgnoreCase(hdrName))
			return 0;

		// signal previous header end
		int	nErr=handleHeaderStage(_lastHdrName, false);
		if (nErr != 0)
			return nErr;

		_lastHdrName = null;
		_lastCallIndex = 0;

		return 0;
	}
	/**
	 * Called to handle a header that may have continuation
	 * @param hdrName header name (excluding the terminating ':')
	 * @param hdrValue header value (may be empty/null)
	 * @return 0 if successful - Note: if non-successful code is returned
	 * then parsing is stopped and returned code is propagated "upwards".
	 */
	protected int handleHeaderCompoundData (final String hdrName, final String hdrValue)
	{
		int	nErr=updateLastHdrState(hdrName);
		if (nErr != 0)
			return nErr;

		// check if need to start new header
		if ((0 == _lastCallIndex) && ((null == _lastHdrName) || (_lastHdrName.length() <= 0)))
		{	
			if ((nErr=handleHeaderStage(hdrName, true)) != 0)
				return nErr;
			
			_lastHdrName = hdrName;
		}

		if ((nErr=handleHeaderData(hdrName, hdrValue, _lastCallIndex)) != 0)
			return nErr;

		_lastCallIndex++;
		return 0;
	}
	/**
	 * Called to handle a header that has no continuation
	 * @param hdrName header name (excluding the terminating ':')
	 * @param hdrValue header value (may be empty/null)
	 * @return 0 if successful - Note: if non-successful code is returned
	 * then parsing is stopped and returned code is propagated "upwards".
	 */
	protected int handleHeaderSimpleData (final String hdrName, final String hdrValue)
	{
		int	nErr=updateLastHdrState(hdrName);
		if (nErr != 0)
			return nErr;
		if ((nErr=handleHeaderStage(hdrName, true)) != 0)
			return nErr;
		if ((nErr=handleHeaderData(hdrName, hdrValue, 0)) != 0)
			return nErr;
		if ((nErr=handleHeaderStage(hdrName, false)) != 0)
			return nErr;
		
		return 0;
	}
	/**
	 * Size of work buffer allocated by default - also, the minimum allowed size for a work buffer
	 * @see #parseHeadersStream(InputStream in, byte[] workBuf)
	 */
	public static final int MIN_HDRS_PARSER_WORK_BUF=512;
	/**
	 * Internal buffer used to hold value of last parsed header
	 */
	private StringBuilder	_lastHdrValue=new StringBuilder(MIN_HDRS_PARSER_WORK_BUF / 4);
	/**
	 * Signals the start of a new header whose name has been accumulated in the "_lastHdrValue" so far
	 * @return 0 if successful
	 */
	protected int startAccumulatedHeader ()
	{
		if (_lastHdrValue.length() <= 0)
			return (-1);	// MUST have some accumulated header name

		final String	hdrName=_lastHdrValue.toString();
		int				nErr=updateLastHdrState(hdrName);
		if (nErr != 0)
			return nErr;
		if ((nErr=handleHeaderStage(hdrName, true)) != 0)
			return nErr;

		_lastHdrName = hdrName;
		_lastHdrValue.setLength(0);

		return 0;
	}
	/**
	 * Appends any "clear" data so far and announces the start of a new header
	 * @param data data from which characters are to be appended
	 * @param lastCharOffset offset in data from which to append character
	 * @param len number of characters to append
	 * @return 0 if successful
	 */
	protected int startAccumulatedHeader (byte[] data, int lastCharOffset, int len)
	{
		// append any "data" characters so far
		try
		{
			_lastHdrValue = StringUtil.appendASCIIBytes(_lastHdrValue, data, lastCharOffset, len);
		}
		catch(IOException e)	// should not happen
		{
			return (-1);
		}

		return startAccumulatedHeader();
	}
	/**
	 * Character offset in current/last line
	 */
	private int	_charOffset	/* =0 */;
	/**
	 * TRUE if last observed character was CR
	 */
	private boolean	_prevCR	/* =false */;

	private RFCHdrLineBufParseResult	_parseRes	/* =null */;
	protected RFCHdrLineBufParseResult getParseResult ()
	{
		if (null == _parseRes)
			_parseRes = new RFCHdrLineBufParseResult();
		else
			_parseRes.reset();

		return _parseRes;
	}

	protected RFCHdrLineBufParseResult getParseResultError (final int nErr)
	{
		final RFCHdrLineBufParseResult	res=getParseResult();
		res.setErrCode(nErr);
		return res;
	}
	
	protected RFCHdrLineBufParseResult getParseResultSuccess (final int offset, final boolean haveCR)
	{
		final RFCHdrLineBufParseResult	res=getParseResult();
		res.setOffset(offset);
		res.setCRDetected(haveCR);
		res.setMoreDataRequired(false);

		return res;
	}
	/**
	 * Parses the data in the supplied buffer - Note: uses a "greedy" allocation
	 * technique for incomplete headers data - i.e., if header data is too big
	 * may cause out of memory exceptions.
	 * @param data data buffer containing ASCII/UTF-8 characters
	 * @param offset offset in data buffer where data starts
	 * @param len number of bytes/charcters available for parsing
	 * @return parsing result
	 * @see RFCHdrLineBufParseResult
	 */
	public RFCHdrLineBufParseResult parseHeadersData (byte[] data, int offset, int len)
	{
		if ((null == data) || (offset < 0) || (len < 0) || ((offset+len) > data.length))
			return getParseResultError(-1);

		int		curOffset=offset, remLen=len, lastCharOffset=curOffset;
		boolean	fNoCurHdrName=(null == _lastHdrName) || (_lastHdrName.length() <= 0);
		for (int	nErr=0; remLen > 0; remLen--, curOffset++)
		{
			final char	c=(char) (data[curOffset] & 0x00FF);
			switch(c)
			{
				case '\r'	:	// skip CR if found
					// append any "data" characters so far
					try
					{
						_lastHdrValue = StringUtil.appendASCIIBytes(_lastHdrValue, data, lastCharOffset, curOffset - lastCharOffset);
					}
					catch(IOException e)
					{
						return getParseResultError(-2);
					}

					lastCharOffset = (curOffset + 1);	// skip the CR

					/*
					 * NOTE: CR if not counted in the _charOffset since we
					 * want to allow LF to appear WITHOUT CR as well, and
					 * we need a ZERO _charOffset value to distinguish an
					 * empty/blank line. This "optimization" may yield bad
					 * results if there is a series of CR(s) terminated by
					 * an LF - but this is totally unexpected and a VERY
					 * bad RFC header format.
					 */
					_prevCR = true;
					break;

				case '\n'	:	// end of line
					// append any "data" characters so far
					try
					{
						StringUtil.appendASCIIBytes(_lastHdrValue, data, lastCharOffset, (curOffset - lastCharOffset));
					}
					catch(IOException e)
					{
						return getParseResultError(-3);
					}
					lastCharOffset = (curOffset + 1);	// skip the LF

					// if no previous accumulated data, then stop
					if (_lastHdrValue.length() <= 0)
					{
						/*
						 * Declare empty line only if LF found at "start" of
						 * line (note that CR if not counted). Otherwise, it
						 * may be an LF at the end of whitespace line
						 */ 
						if (0 == _charOffset)
							return getParseResultSuccess(curOffset, _prevCR);
					}

					if (fNoCurHdrName)
					{	
						/* 	If reached this position, then assume header name. Note:
						 * the header name is illegal since we expected a ':' (or ' ')
						 * to stop us beforehand
						 */
						if ((nErr=handleHeaderSimpleData(_lastHdrValue.toString(), null)) != 0)
							return getParseResultError(nErr);
					}
					else	// have header name and (maybe null) accumulated data
					{
						if ((nErr=handleHeaderCompoundData(_lastHdrName, _lastHdrValue.toString())) != 0)
							return getParseResultError(nErr);
					}

					_lastHdrValue.setLength(0);
					_charOffset = 0;
					_prevCR = false;
					break;

				case '\t'	:	// check if continuation
				case ' '	:
					/*
					 * NOTE: if found whitespace before ':' then allow for (illegal) header name
					 */
					if (fNoCurHdrName)
					{
						if ((nErr=startAccumulatedHeader(data, lastCharOffset, (curOffset - lastCharOffset))) != 0)
							return getParseResultError(nErr);

						lastCharOffset = (curOffset + 1);	// skip the space/tab
						fNoCurHdrName = false;
					}
					/*
					 * NOTE: a series of SPACE/TAB between the header name
					 * and its value will be "eaten up" by this code
					 */
					else if (lastCharOffset >= curOffset)
					{
						lastCharOffset = (curOffset + 1);	// skip the space/tab
					}

					_charOffset++;
					_prevCR = false;
					break;

				default	:
					// if first character in line, then starting a new header
					if (0 == _charOffset)
					{
						// terminate any previous header
						if ((nErr=updateLastHdrState(null)) != 0)
							return getParseResultError(nErr);

						// assumed '\n' of previous line already announced previous data - so just making sure
						_lastHdrValue.setLength(0);
						fNoCurHdrName = true;
					}

					if (fNoCurHdrName && (':' == c))
					{
						if ((nErr=startAccumulatedHeader(data, lastCharOffset, (curOffset - lastCharOffset) /* excluding the ':' */)) != 0)
							return getParseResultError(nErr);

						lastCharOffset = (curOffset + 1);	// skip the ':'
						fNoCurHdrName = false;
					}

					_charOffset++;
					_prevCR = false;
			}	// end of SWITCH
		}

		// append any "leftover" characters
		if (lastCharOffset < curOffset)
		{
			try
			{
				_lastHdrValue = StringUtil.appendASCIIBytes(_lastHdrValue, data, lastCharOffset, curOffset - lastCharOffset);
			}
			catch(IOException e)
			{
				return getParseResultError(-5);
			}
		}

		final RFCHdrLineBufParseResult	res=getParseResult();
		res.setMoreDataRequired(true);
		return res;
	}
	/**
	 * Parses the data in the supplied buffer - Note: uses a "greedy" allocation
	 * technique for incomplete headers data - i.e., if header data is too big
	 * may cause out of memory exceptions.
	 * @param data data buffer containing ASCII/UTF-8 characters
	 * @return parsing result.
	 * @see RFCHdrLineBufParseResult
	 */
	public RFCHdrLineBufParseResult parseHeadersData (byte[] data)
	{
		return parseHeadersData(data, 0, (null == data) ? 0 : data.length);
	}
	/**
	 * "Flushes" any accumulated headers parsing data. Should be called ONLY
	 * if "parseHeadersData" was called directly for buffers (and NOT via
	 * "parseHeadersStream"
	 * @return parsing result
	 * @see RFCHdrLineBufParseResult
	 */
	public RFCHdrLineBufParseResult finishHeadersParsing ()
	{
		int	nErr=0;

		if ((null == _lastHdrName) || (_lastHdrName.length() <= 0))
		{
			/* 		If reached this position, then assume header "value" is
			 * actually header name. NOTE !!! the header name is illegal since
			 * we expected a ':' (or ' ') to stop us beforehand
			 */
			if (_lastHdrValue.length() > 0)
			{
				if ((nErr=handleHeaderSimpleData(_lastHdrValue.toString(), null)) != 0)
					return getParseResultError(nErr);
			}
		}
		else
		{
			if ((nErr=handleHeaderCompoundData(_lastHdrName, _lastHdrValue.toString())) != 0)
				return getParseResultError(nErr);
			if ((nErr=handleHeaderStage(_lastHdrName, false)) != 0)
				return getParseResultError(nErr);
		}

		// reset data since handled via the "handleHeaderZZZData" calls
		_lastHdrName = null;
		_lastHdrValue.setLength(0);

		return getParseResultSuccess(0, _prevCR);
	}
	/**
	 * Reads from the input stream until end-of-stream or first blank line
	 * found using the supplied work buffer.
	 * @param in input stream to read from - Note: assumed to contain ASCII or UTF-8 characters.
	 * Upon return from the call the input stream position is un-defined 
	 * @param workBuf - work buffer to be used for reading - Note: minimum size required
	 * @param nOffset - offset in work buffer to be used for parsing
	 * @param len number of byte available for reading - Note: minimum size required
	 * @return offset in stream where headers end - more precisely, the offset of
	 * the character BEFORE the blank line that signals the end of the headers (<0 if error) 
	 */
	public long parseHeadersStream (InputStream in, byte[] workBuf, int nOffset, int len)
	{
		if ((null == in) || (null == workBuf) || (nOffset < 0) || (len < MIN_HDRS_PARSER_WORK_BUF) || ((nOffset + len) > workBuf.length))
			return (-1L);
		
		try
		{
			long	streamOffset=0L;

			// avoid forever loop - limit to ~2GB calls...
			for (int	readIndex=0, nErr=0; readIndex < (Integer.MAX_VALUE-1); readIndex++)
			{	
				final int	readLen=in.read(workBuf, nOffset, len);
				if (readLen < 0)
				{
					final RFCHdrLineBufParseResult	res=finishHeadersParsing();
					if ((nErr=res.getErrCode()) != 0)
						return (nErr > 0) ? (0 - nErr) : nErr;

					// if reached end of input and no LF then use CR if found
					return res.isCRDetected() ? (streamOffset - 1L) : streamOffset;
				}

				final RFCHdrLineBufParseResult	res=parseHeadersData(workBuf, nOffset, readLen);
				if ((nErr=res.getErrCode()) != 0)
					return (nErr > 0) ? (0 - nErr) : nErr;
				if (!res.isMoreDataRequired())	// if blank line found, then stop and return success
				{
					final long	lfOffset=streamOffset + res.getOffset();
					// take into account existence of CR before the LF
					return res.isCRDetected() ? (lfOffset - 2L) : (lfOffset - 1L);
				}
				
				streamOffset += readLen;
			}
			
			// this point is reached if "forever" loop terminated prematurely
			return (-2L);
		}
		catch(IOException ioe)
		{
			return (-1000L);
		}
	}
	/**
	 * Reads from the input stream until end-of-stream or first blank line
	 * found using the supplied work buffer
	 * @param in input stream to read from - Note: assumed to contain ASCII or UTF-8 characters.
	 * Upon return from the call the input stream position is un-defined 
	 * @param workBuf - work buffer to be used for reading - Note: minimum size required
	 * @return offset in stream where headers end - more precisely, the offset of
	 * the character BEFORE the blank line that signals the end of the headers (<0 if error) 
	 */
	public long parseHeadersStream (InputStream in, byte[] workBuf)
	{
		return parseHeadersStream(in, workBuf, 0, (null == workBuf) ? 0 : workBuf.length);
	}
	/**
	 * Reads from the input stream until end-of-stream or first blank line found using a
	 * temporarily allocated work buffer
	 * @param in input stream to read from - Note: assumed to contain ASCII or UTF-8 characters.
	 * Upon return from the call the input stream position is un-defined 
	 * @return offset in stream where headers end - more precisely, the offset of
	 * the character BEFORE the blank line that signals the end of the headers (<0 if error) 
	 * @see RFCMessageHeadersParser#parseHeadersStream(InputStream in, byte[] workBuf)
	 */
	public long parseHeadersStream (InputStream in)
	{
		return parseHeadersStream(in, new byte[MIN_HDRS_PARSER_WORK_BUF]);
	}
	/**
	 * Resets the internal state and prepare for a new parsing
	 */
	public void reset ()
	{
		_lastHdrName = null;
		_lastHdrValue.setLength(0);
		_charOffset = 0;
		_prevCR = false;
	}
	/**
	 * Class used to embed an (@link IRFCMessageHeadersHandler) into a (@link RFCMessageHeadersParser)
	 * @author Lyor G.
	 * Created: Oct 3, 2005 11:04:08 AM
	 */
	private static final class HeadersParserEmbedder extends RFCMessageHeadersParser {
		private final RFCMessageHeadersHandler	_handler;
		/**
		 * @param handler handler to be embedded as a parser class
		 * @throws IllegalArgumentException if null handler or already an (@link RFCMessageHeadersParser) instance
		 */
		protected HeadersParserEmbedder (RFCMessageHeadersHandler handler) throws IllegalArgumentException
		{
			if (null == (_handler=handler))
				throw new IllegalArgumentException("No handler to embed into the parser");
			if (handler instanceof RFCMessageHeadersParser)
				throw new IllegalArgumentException(handler.getClass().getName() + " already an " + RFCMessageHeadersParser.class.getName() + " instance");
		}
		/*
		 * @see com.cti2.util.mail.IRFCMessageHeadersHandler#handleHeaderData(java.lang.String, java.lang.String, int)
		 */
		@Override
		public int handleHeaderData (String hdrName, String hdrValue, int callIndex)
		{
			return (null == _handler) /* should not happen */ ? Integer.MIN_VALUE : _handler.handleHeaderData(hdrName, hdrValue, callIndex);
		}
		/*
		 * @see com.cti2.util.mail.IRFCMessageHeadersHandler#handleHeaderStage(java.lang.String, boolean)
		 */
		@Override
		public int handleHeaderStage (String hdrName, boolean fStarting)
		{
			return (null == _handler)  /* should not happen */ ? Integer.MIN_VALUE : _handler.handleHeaderStage(hdrName, fStarting);
		}
	}
	/**
	 * Creates a (@link RFCMessageHeadersParser) by embedding the (non-null)
	 * (@link RFCMessageHeadersHandler) instance
	 * @param handler handler to be embedded - may NOT be null
	 * @return embedded instance
	 * @throws IllegalArgumentException if null handler or already an (@link RFCMessageHeadersParser) instance
	 */
	public static RFCMessageHeadersParser createParser (RFCMessageHeadersHandler handler) throws IllegalArgumentException
	{
		return new HeadersParserEmbedder(handler);
	}
	/**
	 * Special exception thrown at end of headers parsing if requested
	 * to generate an output stream object for a parser
	 * @author lyorg
	 * 15/04/2004
	 */
	public static class HeadersEndException extends IOException {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7213859851494172341L;
		private static final String stdMsg=HeadersEndException.class.getName();
		protected long _streamOffset=(-1L);
		/**
		 * @return buffer offset in stream at which end of headers occurred (<0 if not set)
		 */
		public long getStreamOffset ()
		{
			return _streamOffset;
		}

		protected HeadersEndException (long streamOffset)
		{
			super(stdMsg);
			
			_streamOffset = streamOffset;
		}

		protected HeadersEndException ()
		{
			this(-1L);
		}
	}
	/**
	 * Class that make a headers parser object look like an output stream.
	 * Anything written to it is parsed and the abstract methods of the
	 * parser are called. Once a blank line is found, parsing
	 * stops and any subsequent written data is ignored (depending on the
	 * value of <I>"throwExceptionAtHdrsEnd"</I>)
	 * @see RFCMessageHeadersParser.HeadersEndException
	 * @author lyorg
	 * 15/04/2004
	 */
	private static final class HeadersParserOutputStream extends OutputStream {
		private RFCMessageHeadersParser	_parser	/* =null */;
		private long 					_streamOffset	/* =0L */;
		private final boolean			_throwExceptionAtHdrsEnd	/* =false */;
		private boolean					_eoh	/* =false */;
		/**
		 * Constructor
		 * @param parser headers parser object (if null, then exception is thrown)
		 * @param throwExceptionAtHdrsEnd if TRUE then throws a (@link HeadersEndException)
		 * when end of headers is signaled
		 * @throws IllegalArgumentException if null parser
		 */
		protected HeadersParserOutputStream (RFCMessageHeadersParser parser, boolean throwExceptionAtHdrsEnd) throws IllegalArgumentException
		{
			super();
			
			if (null == (_parser=parser))
				throw new IllegalArgumentException("No headers parser object supplied");
			
			_throwExceptionAtHdrsEnd = throwExceptionAtHdrsEnd;
		}
		/*
		 * @see java.io.OutputStream#write(byte[], int, int)
		 */
		@Override
		public void write (byte[] buf, int offset, int len) throws IOException
		{
			if (null == _parser)
				throw new IOException("No headers parser in output stream");

			// check if already have end of headers
			if (_eoh)
			{
				if (_throwExceptionAtHdrsEnd)
					throw new HeadersEndException(_streamOffset);
				
				return;
			}

			RFCHdrLineBufParseResult	res=_parser.parseHeadersData(buf, offset, len);
			int							nErr=res.getErrCode();
			if (nErr != 0)
				throw new IOException("Failed to parse buffer (err=" + nErr + ")");

			if (!res.isMoreDataRequired())
			{
				final int	lfOffset=res.getOffset();

				_eoh = true;

				res = _parser.finishHeadersParsing();

				if ((nErr=res.getErrCode()) != 0)
					throw new IOException("Error on finish (write) parsing: " + nErr);
				
				// check if required to throw exception at end of headers
				if (_throwExceptionAtHdrsEnd)
					throw new HeadersEndException(_streamOffset + lfOffset);
			}

			_streamOffset += len;
		}
		/*
		 * @see java.io.OutputStream#write(byte[])
		 */
		@Override
		public void write (byte[] buf) throws IOException
		{
			write(buf, 0, (null == buf) ? 0 : buf.length);
		}
		/*
		 * @see java.io.OutputStream#write(int)
		 */
		@Override
		public void write (int val) throws IOException
		{
			write(new byte[] { (byte) val });
		}
		/*
		 * @see java.io.OutputStream#close()
		 */
		@Override
		public void close () throws IOException
		{
			if (null == _parser)
				return;	// OK if already closed

			if (!_eoh)
			{
				final RFCHdrLineBufParseResult	res=_parser.finishHeadersParsing();
				final int						nErr=res.getErrCode();
				if (nErr != 0)
					throw new IOException("Error on finish (close) parsing: " + nErr);
				
				_eoh = true;
			}

			_parser = null;
		}
		/*
		 * @see java.io.OutputStream#flush()
		 */
		@Override
		public void flush () throws IOException
		{
			if (null == _parser)	// make sure not called after close
				throw new IOException("Stream is closed on flush call");
		}
	}
	/**
	 * Creates an output stream object where anything written to it is parsed
	 * and the abstract methods called. Once a blank line is found, parsing
	 * stops and any subsequent written data is ignored (depending on the
	 * value of <I>"throwExceptionAtHdrsEnd"</I>)
	 * @param throwExceptionAtHdrsEnd if TRUE then once blank line is found
	 * a special IOException derived exception is thrown
	 * @return the output stream object representing this parser object
	 * @see RFCMessageHeadersParser.HeadersEndException
	 */
	public OutputStream asOutputStream (boolean throwExceptionAtHdrsEnd)
	{
		return new HeadersParserOutputStream(this, throwExceptionAtHdrsEnd);
	}
	/**
	 * @param handler Handler to be informed about extracted headers - may NOT be null
	 * @param throwExceptionAtHdrsEnd if TRUE then once blank line is found
	 * a special IOException derived exception is thrown
	 * @return the output stream object representing this parser object
	 * @throws IllegalArgumentException if null handler or already an (@link RFCMessageHeadersParser) instance
	 */
	public static OutputStream asOutputStream (RFCMessageHeadersHandler handler, boolean throwExceptionAtHdrsEnd) throws IllegalArgumentException
	{
		return new HeadersParserOutputStream(createParser(handler), throwExceptionAtHdrsEnd);
	}

}
