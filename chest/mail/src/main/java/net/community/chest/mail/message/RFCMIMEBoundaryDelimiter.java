package net.community.chest.mail.message;

import java.io.UnsupportedEncodingException;

import net.community.chest.mail.RFCMimeDefinitions;
import net.community.chest.mail.headers.RFCHdrLineBufParseResult;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Class used to detect MIME boundaries delimiters in a stream of data</P>
 * @author Lyor G.
 * @since Sep 19, 2007 9:21:33 AM
 */
public class RFCMIMEBoundaryDelimiter implements CharSequence {
	/**
	 * If set, then represents the MIME boundary used to delimit the
	 * sub-parts of the current part (without the '--')
	 */
	private byte[]	_mmBoundaryBytes	/* =null */;
	/**
	 * @return MIME boundary UTF-8 bytes - WITHOUT the "--" prefix
	 */
	public byte[] getBoundary ()
	{
		return _mmBoundaryBytes;
	}
	/**
	 * @param mmBoundaryBytes boundary UTF-8 bytes to be processed
	 * (without the "--" prefix). If null/empty then internal state
	 * is reset
	 */
	public void setBoundary (byte[] mmBoundaryBytes)
	{
		reset();
		
		if ((mmBoundaryBytes != null) && (mmBoundaryBytes.length > 0))
		{
			_mmBoundaryBytes = mmBoundaryBytes;
			// OK to start accumulating data for "hunting"
			_mmAccumulate = true;
			// since changed the boundary
			_mmLastOne = false;
		}
	}
	/**
	 * @return true if have a boundary to hunt for
	 */
	public boolean isBoundarySet ()
	{
		return (_mmBoundaryBytes != null) && (_mmBoundaryBytes.length > 0);
	}
	/*
	 * @see java.lang.CharSequence#length()
	 */
	@Override
	public int length ()
	{
		final byte[]	mmb=getBoundary();
		if (null == mmb)
			return 0;
		else
			return mmb.length;
	}
	/*
	 * @see java.lang.CharSequence#charAt(int)
	 */
	@Override
	public char charAt (int index)
	{
		final byte[]	mmb=getBoundary();
		// this will cause the necessary IndexOutOfBoundsException if illegal index
		return (char) (mmb[index] & 0x00FF); 
	}
	/*
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	@Override
	public CharSequence subSequence (final int fromIndex, final int toIndex)
	{
		if (fromIndex == toIndex)
			return "";

		final String	val=toString();
		return val.substring(fromIndex, toIndex);
	}
	/**
	 * @return boundary string (null if none set)
	 * @throws UnsupportedEncodingException if UTF-8 not supported by
	 * the JVM (should NEVER happen according to Java specs...)
	 */
	public String getBoundaryAsString () throws UnsupportedEncodingException
	{
		final byte[]	mmb=getBoundary();
		if ((null == mmb) || (mmb.length <= 0))
			return null;

		return new String(mmb, "UTF-8");
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		try
		{
			return getBoundaryAsString();
		}
		catch(UnsupportedEncodingException uee)
		{
			return "";	// should not happen since UTF-8 is ALWAYS supported
		}
	}
	/**
	 * Current number of matched bytes so far
	 */
	private int	_mmMatchPos /* =0 */;
	public int getLastMatchedLength ()
	{
		return _mmMatchPos;
	}

	public void setLastMatchedLength (int pos)
	{
		_mmMatchPos = pos;
	}
	/**
	 * If TRUE, then current match MAY contain a potential MIME
	 * boundary value and we need to accumulate more. Otherwise,
	 * we can safely hunt for the next LF.
	 */
	private boolean	_mmAccumulate	/* =false */;
	public boolean isAccumulating ()
	{
		return _mmAccumulate;
	}

	public void setAccumulating (boolean flag)
	{
		_mmAccumulate = flag;
	}
	/**
	 * TRUE if last detected MIME boundary is last
	 */
	private boolean	_mmLastOne	/* =false */;
	/**
	 * @return TRUE if last detected MIME boundary is last
	 */
	public boolean isLastBoundary ()
	{
		return _mmLastOne;
	}

	public void setLastBoundary (boolean flag)
	{
		_mmLastOne = flag;
	}
	/**
	 * TRUE if last character was CR
	 */
	private boolean	_haveCR	/* =false */;
	public boolean isCRDetected ()
	{
		return _haveCR;
	}

	public void setCRDetected (boolean flag)
	{
		_haveCR = flag;
	}
	/**
	 * Re-initializes the internal state to "empty"
	 */
	public void reset ()
	{
		_mmBoundaryBytes = null;
		_mmMatchPos = 0;
		_mmAccumulate = false;
		_mmLastOne = false;
		_haveCR = false;
	}
	/**
	 * Default constructor
	 */
	public RFCMIMEBoundaryDelimiter ()
	{
		super();
	}
	/**
	 * Pre-initialized constructor
	 * @param mmBoundaryBytes boundary bytes to be processed (without
	 * the '--' prefix). If null/empty then internal state is reset
	 */
	public RFCMIMEBoundaryDelimiter (byte[] mmBoundaryBytes)
	{
		setBoundary(mmBoundaryBytes);
	}
	/**
	 * @param mmBoundary MIME boundary string (without the "--" prefix).
	 * If null/empty then internal state is simply reset
	 * @throws UnsupportedEncodingException if unable to convert it to bytes
	 */
	public void setBoundary (final String mmBoundary) throws UnsupportedEncodingException
	{
		setBoundary(((null == mmBoundary) || (mmBoundary.length() <= 0)) ? null : mmBoundary.getBytes("UTF-8"));
	}
	/**
	 * Pre-initialized constructor
	 * @param mmBoundary MIME boundary string (without the "--" prefix).
	 * If null/empty then internal state is simply reset
	 * @throws UnsupportedEncodingException if unable to convert it to bytes
	 */
	public RFCMIMEBoundaryDelimiter (final String mmBoundary) throws UnsupportedEncodingException
	{
		this(((null == mmBoundary) || (mmBoundary.length() <= 0)) ? null : mmBoundary.getBytes("UTF-8"));
	}
	/**
	 * Called when a current line cannot possibly match the MIME boundary
	 */
	protected void restartAccumulation ()
	{
		_mmAccumulate = false;
		_mmMatchPos = 0;
		_haveCR = false;
	}

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
	/**
	 * Processes the buffer in an attempt to find a matching boundary.
	 * @param buf buffer to be processed
	 * @param off offset in buffer of data
	 * @param len number of bytes to be processed
	 * @return RFCHdrLineBufParseResult. If no more data needed, then
	 * offset is the location of the LF AFTER the MIME boundary in the
	 * parsed buffer. <B>Note:</B> the return value is <U>cached</U> - i.e.,
	 * subseqent calls to this method return the same instance - so if you
	 * need to "share" the returned instance, then <U>clone</U> it - otherwise
	 * undefined behavior may occur
	 */
	public RFCHdrLineBufParseResult processBuffer (byte[] buf, int off, int len)
	{
		// make sure we have a valid buffer to parse 
		if ((null == buf) || (off < 0) || (len < 0) || ((off+len) > buf.length))
			return getParseResultError(-1);

		// make sure we have a boundary to hunt for
		if (!isBoundarySet())
			return getParseResultError(-2);

		// make sure we are not called to "hunt" for boundary after the last one
		if (isLastBoundary())
			return getParseResultError(-4);

		// some useful constants
		final int	lastMatchPos=RFCMimeDefinitions.MIMEBoundaryDelimsBytes.length + _mmBoundaryBytes.length,
					maxMatchPos=lastMatchPos + RFCMimeDefinitions.MIMEBoundaryDelimsBytes.length;
		for (int	curOffset=off, remLen=len; remLen > 0; curOffset++, remLen--)
		{
			final byte	bVal=buf[curOffset];

			// if accumulated data so far signals a potential boundary, keep looking
			if (_mmAccumulate)
			{
				// check if prefix match
				if (_mmMatchPos < RFCMimeDefinitions.MIMEBoundaryDelimsBytes.length)
				{
					if (RFCMimeDefinitions.MIMEBoundaryDelimsBytes[_mmMatchPos] == bVal)
					{	
						_mmMatchPos++;
						_haveCR = false;
					}
					else
						restartAccumulation();
				}
				// check if content match
				else if (_mmMatchPos < lastMatchPos)
				{
					if (_mmBoundaryBytes[_mmMatchPos - RFCMimeDefinitions.MIMEBoundaryDelimsBytes.length] == bVal)
					{	
						_mmMatchPos++;
						_haveCR = false;
					}
					else
						restartAccumulation();
				}
				// check if end of line or last boundary signalled
				else
				{
					if ('\n' == bVal)
					{
						// if end of line, then check if have a match
						if ((lastMatchPos == _mmMatchPos) || (maxMatchPos == _mmMatchPos))
						{
							final RFCHdrLineBufParseResult	res=getParseResult();
							res.setOffset(curOffset);
							res.setCRDetected(_haveCR);
							res.setMoreDataRequired(false);

							_mmLastOne = (maxMatchPos == _mmMatchPos);
							restartAccumulation();
							_mmAccumulate = true;	// continue hunting after this line

							return res;
						}
						
						// fall through to the default behavior
					}
					else if ((byte) '\r' == bVal)
						_haveCR = true;
					else if (_mmMatchPos < maxMatchPos)
					{	
						if (RFCMimeDefinitions.MIMEBoundaryDelimsBytes[_mmMatchPos - lastMatchPos] == bVal)
						{	
							_mmMatchPos++;
							_haveCR = false;
						}
						else
							restartAccumulation();
					}
					else // exceeded even the last boundary match
						restartAccumulation();
				}
			}
			else	// regardless of whether we really see a CR, since we do not accumulate the result
				_haveCR = false;

			if ('\n' == bVal)
			{	
				restartAccumulation();
				_mmAccumulate = true;	// continue hunting after this line
			}
		}

		// signal more data required
		final RFCHdrLineBufParseResult	res=getParseResult();
		res.setMoreDataRequired(true);

		// if have a promising start, then signal how much is matched so far
		if (_mmMatchPos > 0)
			res.setOffset(off + len - _mmMatchPos);

		return res;
	}
	/**
	 * Processes the buffer in an attempt to find a matching boundary.
	 * @param buf buffer to be processed
	 * @return RFCHdrLineBufParseResult. If no more data needed, then
	 * offset is the location of the LF AFTER the MIME boundary in the
	 * parsed buffer 
	 */
	public RFCHdrLineBufParseResult processBuffer (byte[] buf)
	{
		return processBuffer(buf, 0, (null == buf) ? 0 : buf.length);
	}
}
