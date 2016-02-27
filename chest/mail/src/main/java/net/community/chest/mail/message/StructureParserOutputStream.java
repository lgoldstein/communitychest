/*
 * 
 */
package net.community.chest.mail.message;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.InvalidObjectException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.io.SyncFailedException;
import java.io.UTFDataFormatException;
import java.io.UnsupportedEncodingException;
import java.util.InvalidPropertiesFormatException;
import java.util.Stack;

import net.community.chest.io.EOLStyle;
import net.community.chest.io.IOCopier;
import net.community.chest.io.output.NullOutputStream;
import net.community.chest.io.output.OutputStreamEmbedder;
import net.community.chest.mail.RFCMimeDefinitions;
import net.community.chest.mail.headers.RFCHdrLineBufParseResult;
import net.community.chest.mail.headers.RFCHeaderDefinitions;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 4, 2009 1:49:32 PM
 */
public class StructureParserOutputStream extends OutputStreamEmbedder {
	/**
	 * Offset within the stream
	 */
	protected long	_streamOffset	/* =0L */;
	/**
	 * Parts "saved" while sub-parts are parsed. The part at the top
	 * of the stack is the "parent" of all current sub-parts
	 */
	protected Stack<RFCMessageStructure>	_topParts=new Stack<RFCMessageStructure>();
	/**
	 * Current parsed part
	 */
	protected RFCMessageStructure	_curPart=new RFCMessageStructure();
	/**
	 * @return part ID of current part - null if error
	 */
	private String getCurrentPartId ()
	{
		return (null == _curPart) ? null : _curPart.getPartId();
	}
	/**
	 * TRUE if currently handling part headers
	 */
	protected boolean	_hdrsParse=true;
	/**
	 * Used to delimit parts if a MIME boundary is available
	 */
	protected RFCMIMEBoundaryDelimiter	_mmDelimiter=new RFCMIMEBoundaryDelimiter();
	/**
	 * Extracts the message structure while at the same time writing the
	 * data to the supplied underlying output stream.
	 * @param os "real" underling stream to write data to (as well as
	 * extracting the message structure)
	 * @param realClose TRUE if calling (@link #close()) should also close
	 * the underlying output stream (if any) that was provided.
	 */
	public StructureParserOutputStream (OutputStream os, boolean realClose)
	{
		super(os, realClose);
		// we start with the root
		_curPart.setPartId("");
	}
	/**
	 * Extracts the message structure while at the same time writing the
	 * data to the supplied underlying output stream.
	 * @param os "real" underling stream to write data to (as well as
	 * extracting the message structure). <B>Note:</B> calling (@link #close())
	 * on this stream also closes the underlying supplied stream
	 * (unless (@link #setRealClosure(boolean)) was called beforehand)
	 * @see #StructureParserOutputStream(OutputStream, boolean)
	 */
	public StructureParserOutputStream (OutputStream os)
	{
		this(os, true);
	}
	/**
	 * Default constructor extracts the message structure without passing
	 * the data to any underlying output stream
	 */
	public StructureParserOutputStream ()
	{
		this(new NullOutputStream());
	}
	/**
	 * Pushes the specified part to the parts stack
	 * @param curPart part to be pushed
	 * @return new part to be used as "current" (if current is root then root) - null if error
	 * @throws UnsupportedEncodingException if unable to set MIME boundary
	 * bytes - should NEVER happen since we use UTF-8 which is built-in
	 * into ALL JVMs as per Java specifications...
	 */
	private RFCMessageStructure pushPartsStack (final RFCMessageStructure curPart)
		throws UnsupportedEncodingException
	{
		if (null == curPart)
			return null;

		final String	mmb=curPart.isMIMEBoundarySet() ? curPart.getMIMEBoundary() : curPart.extractMIMEBoundary(true);
		if ((mmb != null) && (mmb.length() > 0))
			_mmDelimiter.setBoundary(mmb);

		// if we have a MIME boundary then we need to parse a multipart message
		if (_mmDelimiter.isBoundarySet())
			_topParts.push(curPart);

		if (curPart.isRootPart() || curPart.isCompoundPart())
			return curPart;

		final RFCMessageStructure	newPart=new RFCMessageStructure();
		newPart.setPartId(RFCMessageStructure.getNextPartId(curPart));
		newPart.setParent(curPart);
		newPart.setHeadersStartOffset(_streamOffset);

		return newPart;
	}
	/**
	 * Starts an envelope part as a sub-part of the supplied part
	 * @param curPart parent part - assumed to be a message part (not checked)
	 * @return envelope part - null if error
	 */
	protected RFCMessageStructure startEnvelopePart (final RFCMessageStructure curPart)
	{
		if ((null == curPart) || (null == _topParts))
			return null;
		_topParts.push(curPart);

		final RFCMessageStructure	newPart=new RFCMessageStructure();
		newPart.setPartId(RFCMessageStructure.getEnvelopePartId(curPart));
		newPart.setParent(curPart);
		newPart.setHeadersStartOffset(_streamOffset);

		// reset current boundary since we are parsing an envelope ATTEMPTING to detect a boundary
		_mmDelimiter.reset();
		return newPart;
	}
	/**
	 * Finalizes an envelope part and restores the parent message
	 * @param curPart envelope part (not checked)
	 * @return parent message part - null if error
	 * @throws UnsupportedEncodingException if unable to set MIME boundary
	 * bytes - should NEVER happen since we use UTF-8 which is built-in
	 * into ALL JVMs as per Java specifications...
	 */
	protected RFCMessageStructure endEnvelopePart (RFCMessageStructure curPart) throws UnsupportedEncodingException
	{
		if ((null == curPart) || (null == _topParts) || _topParts.isEmpty())
			return null;
		
		final RFCMessageStructure	parent=_topParts.pop();
		if (null == parent)
			return null;

		parent.addSubPart(curPart, true);
		parent.setMIMEBoundary(curPart.getMIMEBoundary());
		parent.setDataStartOffset(curPart.getDataStartOffset());

		// for root part, complete some data from the envelope
		if (parent.isRootPart())
		{
			parent.setMIMEType(curPart.getMIMEType());
			parent.setMIMESubType(curPart.getMIMESubType());
			parent.setHeadersStartOffset(curPart.getHeadersStartOffset());
			parent.setHeadersEndOffset(curPart.getHeadersEndOffset());
		}

		// declare a zero size data for the envelope part
		curPart.setDataEndOffset(curPart.getDataStartOffset());
		// set current MIME boundary data
		final String	mmb=parent.getMIMEBoundary();
		if ((mmb != null) && (mmb.length() > 0))
			_mmDelimiter.setBoundary(mmb);

		return parent;
	}
	/**
	 * @return TRUE if stop parsing - called during crucial stages only.
	 * Default=FALSE (i.e. ALWAYS keep parsing) - intended for special case
	 * overriding (use with GREAT(est) CARE)
	 */
	protected boolean clearXfer ()
	{
		return false;
	}
	/**
	 * Processes headers data for the current part
	 * @param buf buffer to be processed
	 * @param offset offset of data in buffer
	 * @param len number of bytes
	 * @return processed length (<0 if error)
	 * @throws UnsupportedEncodingException if unable to set MIME boundary
	 * bytes - should NEVER happen since we use UTF-8 which is built-in
	 * into ALL JVMs as per Java specs...
	 */
	protected int processHeadersData (byte[] buf, int offset, int len) throws UnsupportedEncodingException
	{
		// if processing headers for a message part then generate a virtual envelope part
		if (_curPart.isMsgPart() && (!_curPart.isEnvelopePartAvailable()))
		{
			if (null == (_curPart=startEnvelopePart(_curPart)))
				return Integer.MIN_VALUE;
		}

		// first process any data so far
		final RFCHdrLineBufParseResult	res=_curPart.parseHeadersData(buf, offset, len);
		if (res.getErrCode() != 0)	// ensure a NEGATIVE value
			return (res.getErrCode() > 0) ? (0 - res.getErrCode()) : res.getErrCode();

		if (res.isMoreDataRequired())
		{
			_streamOffset += len;
			return len;
		}

		// end of headers detected
		final int	processedLen=(res.getOffset() - offset) + 1 /* for the LF */;
		_streamOffset += processedLen;

		if (res.isCRDetected())
			// report headers end offset as the CR offset
			_curPart.setHeadersEndOffset(_streamOffset - 2L);
		else
			// report headers end offset as the LF offset
			_curPart.setHeadersEndOffset(_streamOffset-1);

		// first byte of data is the current stream offset - one position BEYOND the LF
		_curPart.setDataStartOffset(_streamOffset);

		if (!_curPart.isContentTypeSet())
			_curPart.extractContentType(true);

		// if this the end of the virtual envelope part, then restore the parent
		if (_curPart.isEnvelopePart())
		{
			if (null == (_curPart=endEnvelopePart(_curPart)))
				return Integer.MIN_VALUE;
		}

		_hdrsParse = false;

		if (clearXfer())	// check if required to stop parsing
			return processedLen;

		if (_curPart.isRootPart() || _curPart.isCompoundPart())
		{
			/*		At end of headers for embedded message we may need to
			 * re-start parsing of its envelope. At the same time, it
			 * may be that we already have an envelope since we "pop-ed"
			 * the current embedded message part when finished with its
			 * envelope headers parsing
			 */
			if (_curPart.isEmbeddedMessagePart() && (!_curPart.isEnvelopePartAvailable()))
			{	
				if (null == (_curPart=startEnvelopePart(_curPart)))
					return Integer.MIN_VALUE;

				// re-start headers parsing for embedded message headers
				_hdrsParse = true;
			}
			else
			{	
				if (null == (_curPart=pushPartsStack(_curPart)))
					return Integer.MIN_VALUE;
			}
		}

		return processedLen;
	}
	/**
	 * Called when finished with the current part and we can add it as a
	 * sub-part of whatever part is now on the stack top (the "parent")
	 * @param curPart current part that needs be linked to its parent
	 * @param dataEndOffset to be used compound parts
	 * @return new part if successful (null otherwise)
	 * @throws UnsupportedEncodingException if unable to set MIME boundary
	 * bytes - should NEVER happen since we use UTF-8 which is built-in
	 * into ALL JVMs as per Java specs...
	 */
	private RFCMessageStructure popPartsStack (RFCMessageStructure curPart, long dataEndOffset) throws UnsupportedEncodingException
	{
		if ((null == _topParts) || _topParts.isEmpty())
			return null;
		
		final RFCMessageStructure	parent=_topParts.pop();
		if (null == parent)
			return null;

		parent.addSubPart(curPart, true);
		parent.setDataEndOffset(dataEndOffset);
		if (!parent.isRootPart())
		{
			// the MIME boundary to look for is of the parent's parent - except for the root
			final RFCMessageStructure	mimeParent=_topParts.peek();
			if (null == mimeParent)
				return null;
			_mmDelimiter.setBoundary(mimeParent.getMIMEBoundary());
		}
		else
			_mmDelimiter.setBoundary(parent.getMIMEBoundary());

		return parent;
	}
	/**
	 * Closes the part and generates a new one/or pops the parent if last part
	 * @param curPart part to be closed
	 * @param dataEndOffset to be used for last part of compound parts
	 * @param isLastPart TRUE if last one
	 * @return new part if successful (null otherwise)
	 * @throws UnsupportedEncodingException if unable to set MIME boundary
	 * bytes - should NEVER happen since we use UTF-8 which is built-in
	 * into ALL JVMs as per Java specs...
	 */
	protected RFCMessageStructure closePart (RFCMessageStructure curPart, long dataEndOffset, boolean isLastPart) throws UnsupportedEncodingException
	{
		if (isLastPart)
			return popPartsStack(curPart, dataEndOffset);

		if ((null == _topParts) || _topParts.isEmpty())
			return null;
		
		// update the parent, but do not remove it from the stack
		final RFCMessageStructure	parent=_topParts.peek();
		if (null == parent)
			return null;

		if (!parent.equals(curPart))
			parent.addSubPart(curPart, true);

		final RFCMessageStructure	newPart=new RFCMessageStructure();
		newPart.setPartId(RFCMessageStructure.getNextPartId(parent));
		newPart.setHeadersStartOffset(_streamOffset);
		_hdrsParse = true;	// need to parse headers of new part

		return newPart;
	}
	/**
	 * Set to TRUE if currently processing a direct sub-part - i.e., one that
	 * does not have headers of its own, but "inherits" them from its container
	 */
	private boolean	_directSubPart	/* =false */;
	/**
	 * Called to end the direct sub-part hunt mode
	 * @param curPart current (direct) sub-part
	 * @param dataEndOffset end of (direct) sub-part data
	 * @return new sub-part to be used for parsing from now on (null if error)
	 * @throws IOException internal parsing state error(s)
	 */
	private RFCMessageStructure endDirectSubPart (final RFCMessageStructure curPart, final long dataEndOffset) throws IOException
	{
		if (null == curPart)
			return curPart;

		// not allowed to be re-called while direct sub-part parsing still on
		if (!_directSubPart)
			throw new SyncFailedException("Direct sub-part not started for part ID=" + curPart.getPartId());
		
		final RFCMessageStructure	parent=_topParts.isEmpty() /* should not happen */ ? null : _topParts.pop();
		if (null == parent)
			throw new StreamCorruptedException("No direct sub-part parent for ID=" + curPart.getPartId());

		final boolean	isEmbedded=parent.isEmbeddedMessagePart(),
						isRoot=parent.isRootPart();
		// parent MUST be the type of container than can have a direct sub-part
		if ((!isEmbedded) && (!isRoot))
			throw new SyncFailedException("Parent of sub-part=" + curPart.getPartId() + " not a direct sub-part container: " + parent.getPartHeader(RFCHeaderDefinitions.stdContentTypeHdr));

		curPart.setDataEndOffset(dataEndOffset);
		parent.setDataEndOffset(dataEndOffset);	// for direct parent, the data end is same as its (direct) sub-part
		parent.addSubPart(curPart, true /* re-state the fact that the direct sub-part belongs to its parent */);

		_directSubPart = false;	// mark that no longer hunting for direct sub-part end
		return parent;
	}
	/**
	 * Processes data for the current part looking for the MIME boundary
	 * @param buf buffer to be processed
	 * @param offset offset of data in buffer
	 * @param len number of bytes
	 * @return processed length (<0 if error)
	 * @throws IOException if internal errors
	 */
	protected int processMIMEBoundaryData (byte[] buf, int offset, int len) throws IOException
	{
		final RFCHdrLineBufParseResult	res=_mmDelimiter.processBuffer(buf, offset, len);
		if (res.getErrCode() != 0)
			return (res.getErrCode() > 0) ? (0 - res.getErrCode()) : res.getErrCode();

		// wait for next time
		if (res.isMoreDataRequired())
		{
			_streamOffset += len;
			// if more data required then entire buffer has been processed
			return len;
		}

		// found a MIME boundary - end current part and start a new one (if necessary)
		final int	processedLen=(res.getOffset() - offset) + 1 /* for the LF */;
		_streamOffset += processedLen;

		// calculate data end at one character BEFORE the MIME boundary
		long	dtEnd=_streamOffset		// points to position AFTER the LF terminating the MIME boundary
					- 1L /* the LF */
					- _mmDelimiter.length()		// the boundary itself
					- RFCMimeDefinitions.MIMEBoundaryDelimsBytes.length;	// "--" prefix used to denote start of boundary
	 	// take into account the (optional) CR
		if (res.isCRDetected())
			dtEnd--;

		// subtract the "--" suffix used to signal last delimiter
		if (_mmDelimiter.isLastBoundary())
			dtEnd -= RFCMimeDefinitions.MIMEBoundaryDelimsBytes.length;

		if ((!_curPart.isRootPart()) && (!_curPart.isCompoundPart()))
			_curPart.setDataEndOffset(dtEnd);

		// check if hunting for direct sub-part - if so, then end it and restore its parent
		if (_directSubPart)
		{
			if (null == (_curPart=endDirectSubPart(_curPart, dtEnd)))
				return Integer.MIN_VALUE;
		}

		// continue treatment of "normal" sub-part 
		if (null == (_curPart=closePart(_curPart, dtEnd, _mmDelimiter.isLastBoundary())))
			return Integer.MIN_VALUE;

		return processedLen;
	}
	/**
	 * Checks if the current parsing state requires starting a "direct"
	 * sub-part hunt mode. This check is called by the (@link #write(byte[], int, int))
	 * method if no current header/MIME/(other)direct sub-part hunt mode is on.
	 * @param curPart current part to be checked
	 * @param dataStartOffset data start offset of (direct) sub-part - if needed
	 * @return new current part to be used if direct sub-part handling is
	 * required - otherwise same as input (<B>Note:</B> it also sets the
	 * internal (@link #_directSubPart) flag to TRUE to indicate this mode).
	 * @throws IOException if internal parsing state mismatches found
	 */
	private RFCMessageStructure checkDirectSubPartStart (final RFCMessageStructure curPart, final long dataStartOffset) throws IOException
	{
		if (null == curPart)
			return curPart;

		// not allowed to be re-called while direct sub-part parsing still on
		if (_directSubPart)
			throw new SyncFailedException("Direct sub-part already started for part ID=" + curPart.getPartId());

		final boolean	isEmbedded=curPart.isEmbeddedMessagePart(),
						isRoot=curPart.isRootPart();
		// if not the type of container than can have a direct sub-part then do nothing
		if ((!isEmbedded) && (!isRoot))
			return curPart;

		// direct sub-parts must belong to a top-level part that has an envelope
		final RFCMessageStructure envPart=curPart.getEnvelopePart();
		if (null == envPart)
			throw new StreamCorruptedException("No envelope for sub-part of ID=" + curPart.getPartId());

		final RFCMessageStructure	newPart=new RFCMessageStructure();
		newPart.setParent(curPart);
		newPart.setPartId(RFCMessageStructure.getNextPartId(curPart));

		// same headers location as its parent's envelope
		newPart.setHeadersStartOffset(envPart.getHeadersStartOffset());
		newPart.setHeadersEndOffset(envPart.getHeadersEndOffset());

		// inherit only the "Content-ZZZ:" headers from the envelope - to emulate a non-direct sub-part
		int	nErr=newPart.copyHeaders(envPart, true);
		if (nErr != 0)
			throw new SyncFailedException("Failed (err=" + nErr + ") to copy content headers of part=" + curPart.getPartId());

		// update the MIME type/sub-type from the envelope
		newPart.setMIMEType(envPart.getMIMEType());
		newPart.setMIMESubType(envPart.getMIMESubType());

		newPart.setDataStartOffset(dataStartOffset);

		// if have an embedded message but no MIME boundary, then hunt till parent's MIME boundary found
		if (isEmbedded)
		{
			RFCMessageStructure	parent=curPart.getParent();
			if (null == parent)	// if parent not set yet, assume top of stack is parent
				parent = _topParts.isEmpty() /* should not happen */ ? null : _topParts.peek();

			final String	mmbPar=(null == parent) ? null : parent.getMIMEBoundary();
			// parent MUST have a MIME boundary since it embeds a message part (the current one)
			if ((null == mmbPar) || (mmbPar.length() <= 0))
				throw new SyncFailedException("No parent MIME boundary for direct sub-part of part=" + curPart.getPartId());

			_mmDelimiter.setBoundary(mmbPar);
		}

		_topParts.push(curPart);	// remember who was the parent
		_directSubPart = true;	// signal that hunting for direct sub-part end

		return newPart;
	}
	/**
	 * Called to process data when hunting for direct sub-part end
	 * @param buf buffer to process
	 * @param offset offset in buffer to check the data
	 * @param len number of valid data bytes
	 * @return number of bytes successfully processed (<0 if error)
	 * @throws IOException if unable to process
	 */
	private int processDirectSubPartData (byte[] buf, int offset, int len) throws IOException
	{
		if (!_directSubPart)
			throw new StreamCorruptedException("Not direct sub-part data mode to process for ID=" + getCurrentPartId());

		// if have the parent's MIME boundary, then use it to hunt for the end
		if (_mmDelimiter.isBoundarySet())
			return processMIMEBoundaryData(buf, offset, len);

		_streamOffset += len;
		return len;
	}
	/*
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write (byte[] buf, int offset, int len) throws IOException
	{
		if (null == this.out)
			throw new IOException("No underlying output stream to write to");

		if (!clearXfer())
		{
			if ((null == _topParts) || (null == _mmDelimiter) || (null == _curPart))
				throw new EOFException("No parsing in progress to write");

			for (int	curOffset=offset, remLen=len; (remLen > 0) && (!clearXfer()); )
			{
				final int	processedLen;
				if (_directSubPart)
				{
					if ((processedLen=processDirectSubPartData(buf, curOffset, remLen)) < 0)
						throw new InvalidPropertiesFormatException("Cannot (err=" + processedLen + ") parse direct sub-part=" + getCurrentPartId() + " data");
				}
				else if (_hdrsParse)
				{
					if ((processedLen=processHeadersData(buf, curOffset, remLen)) < 0)
						throw new InvalidPropertiesFormatException("Cannot (err=" + processedLen + ") parse part=" + getCurrentPartId() + " headers");
				}
				else if (_mmDelimiter.isBoundarySet())
				{
					if ((processedLen=processMIMEBoundaryData(buf, curOffset, remLen)) < 0)
						throw new UTFDataFormatException("Cannot (err=" + processedLen + ") parse part=" + getCurrentPartId() + " MIME data");
				}
				else
				{
					final RFCMessageStructure	newPart=checkDirectSubPartStart(_curPart, _streamOffset);
					if (newPart != _curPart)
					{
						if (null == (_curPart=newPart))
							throw new InterruptedIOException("Failed to start direct sub-part of part=" + getCurrentPartId());

						continue;
					}

					_streamOffset += remLen;
					break;
				}

				remLen -= processedLen;
				curOffset += processedLen;
			}
		}

		this.out.write(buf, offset, len);
	}
	/* just making sure not called AFTER closure
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush () throws IOException
	{
		if ((null == _topParts) || (null == _mmDelimiter))
			throw new EOFException("No parsing in progress to flush");
		if (null == this.out)
			throw new EOFException("No underlying output stream to flush");
		this.out.flush();
	}
	/* DO NOT IGNORE THE EXCEPTION IF THROWN !!!
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close () throws IOException
	{
		if (null == _curPart)
			throw new IOException("No current top-level part on closure");

		if ((_topParts != null) && (!clearXfer()))
		{
			flush();	// call to make sure

			/* 		If have a boundary but not at root part, then simulate
			 * a CRLF - just in case last MIME boundary was not terminated
			 * with a CRLF but rather via an EOF
			 */ 
			if ((!_curPart.isRootPart()) && _mmDelimiter.isBoundarySet())
			{	
				write(EOLStyle.CRLF.getStyleBytes());
				_streamOffset -= 2L;	// "pushback" simulated characters
			}

			// make sure not looking still for direct sub-part end
			if (_directSubPart)
			{
				final String	curId=getCurrentPartId();
				// delimiter being set should have been taken care of by previous code 
				if (_mmDelimiter.isBoundarySet())
					throw new SyncFailedException("MIME boundary still set on direct sub-part of " + curId);

				if (null == (_curPart=endDirectSubPart(_curPart, _streamOffset)))
					throw new SyncFailedException("cannot terminate direct sub-part of " + curId);
			}

			/* 		At end of parsing the stack should be empty since we
			 * push into it only "parents", while we parse sub-parts. Once
			 * a sub-part is complete, we "pop" its parent. Since we start
			 * with an empty stack - at the end, there should be an empty
			 * one.
			 */
			if (!_topParts.isEmpty())
				throw new StreamCorruptedException("Mismatched parts depth");
			// make sure the top-level part is indeed "top" (just as added insurance)
			if (!_curPart.isRootPart())
				throw new InvalidObjectException("Non-root top level part: " + _curPart.getPartId());

			// check if end of stream contains only header or also data
			final long	hStart=_curPart.getHeadersStartOffset(), hEnd=_curPart.getHeadersEndOffset();
			if (hStart != 0L)	// just making sure
				throw new SyncFailedException("Bad/Illegal top-level headers start offset: " + hStart);

			// if headers end is set, then assume this is the end of data
			if (hEnd > hStart)
			{
				final long	dStart=_curPart.getDataStartOffset(), dEnd=_curPart.getDataEndOffset();
				if (dEnd < dStart)	// if data end not set, then assume this is the data end
					_curPart.setDataEndOffset(_streamOffset);
			}
			else	// we have only headers in the stream
				_curPart.setHeadersEndOffset(_streamOffset);

			// signal "closure"
			_topParts = null;
			_mmDelimiter = null;
		}

		super.close();
	}
	/**
	 * @return "top"-level message structure - may be called ONLY AFTER
	 * closing the stream to ensure final parsing (do NOT ignore the
	 * exception thrown by the "close" method), otherwise result is
	 * undefined (may be null or an invalid structure)
	 */
	public RFCMessageStructure getParsedStructure ()
	{
		return (_topParts != null) ? null : _curPart;
	}
	/**
	 * Parses the input stream in an attempt to get a message structure from it
	 * @param in input stream to read from
	 * @return message structure
	 * @throws IOException if unable to extract message structure
	 */
	public static final RFCMessageStructure parseInputStream (InputStream in) throws IOException
	{
		return parseInputStream(in, (-1L));
	}
	/**
	 * Parses the input stream in an attempt to get a message structure from it
	 * @param in input stream to read from
	 * @param copySize if >=0 then number of bytes from input stream to parse.
	 * Otherwise, input stream is read till EOF
	 * @return message structure
	 * @throws IOException if unable to extract message structure
	 */
	public static final RFCMessageStructure parseInputStream (InputStream in, long copySize) throws IOException
	{
		return parseInputStream(in, IOCopier.DEFAULT_COPY_SIZE, copySize);
	}
	/**
	 * Parses the input stream in an attempt to get a message structure from it
	 * @param in input stream to read from
	 * @param bufSize number of bytes to be used from the work buffer
	 * @return message structure
	 * @throws IOException if unable to extract message structure
	 */
	public static final RFCMessageStructure parseInputStream (InputStream in, int bufSize) throws IOException
	{
		return parseInputStream(in, bufSize, (-1L));
	}
	/**
	 * Parses the input stream in an attempt to get a message structure from it
	 * @param in input stream to read from
	 * @param bufSize number of bytes to be used from the work buffer
	 * @param copySize if >=0 then number of bytes from input stream to parse.
	 * Otherwise, input stream is read till EOF
	 * @return message structure
	 * @throws IOException if unable to extract message structure
	 */
	public static final RFCMessageStructure parseInputStream (InputStream in, int bufSize, long copySize) throws IOException
	{
		return parseInputStream(in, new byte[bufSize], copySize);
	}
	/**
	 * Parses the input stream in an attempt to get a message structure from it
	 * @param in input stream to read from
	 * @param workBuf work buffer to be used for processing - the large the better
	 * @return message structure
	 * @throws IOException if unable to extract message structure
	 */
	public static final RFCMessageStructure parseInputStream (InputStream in, byte[] workBuf) throws IOException
	{
		return parseInputStream(in, workBuf, (-1L));
	}
	/**
	 * Parses the input stream in an attempt to get a message structure from it
	 * @param in input stream to read from
	 * @param workBuf work buffer to be used for processing - the large the better
	 * @param copySize if >=0 then number of bytes from input stream to parse.
	 * Otherwise, input stream is read till EOF
	 * @return message structure
	 * @throws IOException if unable to extract message structure
	 */
	public static final RFCMessageStructure parseInputStream (InputStream in, byte[] workBuf, long copySize) throws IOException
	{
		return parseInputStream(in, workBuf, 0, (null == workBuf) ? 0 : workBuf.length, copySize);
	}
	/**
	 * Parses the input stream in an attempt to get a message structure from it
	 * @param in input stream to read from
	 * @param out parsing output stream - Note: closed by this call !!!
	 * @param bufSize number of bytes to be used from the work buffer
	 * @return message structure
	 * @throws IOException if unable to extract message structure
	 */
	public static final RFCMessageStructure parseInputStream (InputStream in, StructureParserOutputStream out, int bufSize) throws IOException
	{
		return parseInputStream(in, out, bufSize, (-1L));
	}
	/**
	 * Parses the input stream in an attempt to get a message structure from it
	 * @param in input stream to read from
	 * @param out parsing output stream - Note: closed by this call !!!
	 * @param bufSize number of bytes to be used from the work buffer
	 * @param copySize if >=0 then number of bytes from input stream to parse.
	 * Otherwise, input stream is read till EOF
	 * @return message structure
	 * @throws IOException if unable to extract message structure
	 */
	public static final RFCMessageStructure parseInputStream (InputStream in, StructureParserOutputStream out, int bufSize, long copySize) throws IOException
	{
		return parseInputStream(in, out, new byte[bufSize], 0, bufSize, copySize);
	}
	/**
	 * Parses the input stream in an attempt to get a message structure from it
	 * @param in input stream to read from
	 * @param out parsing output stream - Note: closed by this call !!!
	 * @param workBuf work buffer to be used for processing - the large the better
	 * @return message structure
	 * @throws IOException if unable to extract message structure
	 */
	public static final RFCMessageStructure parseInputStream (InputStream in, StructureParserOutputStream out, byte[] workBuf) throws IOException
	{
		return parseInputStream(in, out, workBuf, 0, (null == workBuf) ? 0 : workBuf.length);
	}
	/**
	 * Parses the input stream in an attempt to get a message structure from it
	 * @param in input stream to read from
	 * @param workBuf work buffer to be used for processing - the large the better
	 * @param offset offset in work buffer where processing data may be stored
	 * @param bufSize number of bytes to be used from the work buffer
	 * @return message structure
	 * @throws IOException if unable to extract message structure
	 */
	public static final RFCMessageStructure parseInputStream (InputStream in, byte[] workBuf, int offset, int bufSize) throws IOException
	{
		return parseInputStream(in, workBuf, offset, bufSize, (-1L));
	}
	/**
	 * Parses the input stream in an attempt to get a message structure from it
	 * @param in input stream to read from
	 * @param workBuf work buffer to be used for processing - the large the better
	 * @param offset offset in work buffer where processing data may be stored
	 * @param bufSize number of bytes to be used from the work buffer
	 * @param copySize if >=0 then number of bytes from input stream to parse.
	 * Otherwise, input stream is read till EOF
	 * @return message structure
	 * @throws IOException if unable to extract message structure
	 */
	public static final RFCMessageStructure parseInputStream (InputStream in, byte[] workBuf, int offset, int bufSize, long copySize) throws IOException
	{
		return parseInputStream(in, new StructureParserOutputStream(), workBuf, offset, bufSize, copySize); 
	}
	/**
	 * Parses the input stream in an attempt to get a message structure from it
	 * @param in input stream to read from
	 * @param out parsing output stream - Note: closed by this call !!!
	 * @param workBuf work buffer to be used for processing - the large the better
	 * @param offset offset in work buffer where processing data may be stored
	 * @param bufSize number of bytes to be used from the work buffer
	 * @return message structure
	 * @throws IOException if unable to extract message structure
	 */
	public static final RFCMessageStructure parseInputStream (InputStream in, StructureParserOutputStream out, byte[] workBuf, int offset, int bufSize) throws IOException
	{
		return parseInputStream(in, out, workBuf, offset, bufSize, (-1L));
	}
	/**
	 * Parses the input stream in an attempt to get a message structure from it
	 * @param in input stream to read from
	 * @param out parsing output stream - Note: closed by this call !!!
	 * @param workBuf work buffer to be used for processing - the large the better
	 * @param offset offset in work buffer where processing data may be stored
	 * @param bufSize number of bytes to be used from the work buffer
	 * @param copySize if >=0 then number of bytes from input stream to parse.
	 * Otherwise, input stream is read till EOF
	 * @return message structure
	 * @throws IOException if unable to extract message structure
	 */
	public static final RFCMessageStructure parseInputStream (final InputStream in, final StructureParserOutputStream out, byte[] workBuf, int offset, int bufSize, long copySize) throws IOException
	{
		final long	cpyLen=IOCopier.copyStreams(in, out, workBuf, offset, bufSize, copySize);
		if (cpyLen < 0L)
			throw new IOException("MIME stream copying failed: " + cpyLen);
		out.close();	// DO NOT CATCH THE EXCEPTION !!!
		
		final RFCMessageStructure	rootPart=out.getParsedStructure();
		if (null == rootPart)
			throw new IOException("No data parsed");
		
		return rootPart;
	}
	/**
	 * Parses the input stream in an attempt to get a message structure from it
	 * @param in input stream to read from
	 * @param out parsing stream
	 * @param copySize if >=0 then number of bytes from input stream to parse.
	 * Otherwise, input stream is read till EOF
	 * @return message structure
	 * @throws IOException if unable to extract message structure
	 */
	public static final RFCMessageStructure parseInputStream (InputStream in, StructureParserOutputStream out, long copySize) throws IOException
	{
		return StructureParserOutputStream.parseInputStream(in, out, IOCopier.DEFAULT_COPY_SIZE, copySize);
	}
	/**
	 * Parses the input stream in an attempt to get a message structure from it
	 * @param in input stream to read from
	 * @param out parsing stream
	 * @return message structure
	 * @throws IOException if unable to extract message structure
	 */
	public static final RFCMessageStructure parseInputStream (InputStream in, StructureParserOutputStream out) throws IOException
	{
		return parseInputStream(in, out, (-1L));
	}
}
