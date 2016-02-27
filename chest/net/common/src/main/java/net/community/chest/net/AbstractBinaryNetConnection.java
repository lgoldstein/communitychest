package net.community.chest.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;

import net.community.chest.net.io.NetBinaryInputStream;
import net.community.chest.net.io.NetBinaryOutputStream;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jul 4, 2007 7:54:10 AM
 */
public abstract class AbstractBinaryNetConnection extends AbstractNetConnection implements BinaryNetConnection {
	protected AbstractBinaryNetConnection ()
	{
		super();
	}
	/*
	 * @see net.community.chest.net.BinaryNetConnection#readBytes(byte[])
	 */
	@Override
	public int readBytes (final byte[] buf) throws IOException
	{
		return readBytes(buf, 0, (null == buf) ? 0 : buf.length);
	}
	/*
	 * @see net.community.chest.net.BinaryNetConnection#fillBytes(byte[], int, int)
	 */
	@Override
	public int fillBytes (final byte[] buf, final int offset, final int len) throws IOException
	{
		for (int curLen=0; curLen < len; )
		{
			final int	readLen=readBytes(buf, offset + curLen, len - curLen);
			if (readLen <= 0)
				throw new EOFException("Premature EOF in socket after " + curLen + " bytes while trying to fill len=" + len);
			
			curLen += readLen;
		}
		
		return len;
	}
	/*
	 * @see net.community.chest.net.BinaryNetConnection#fillBytes(byte[])
	 */
	@Override
	public int fillBytes (final byte[] buf) throws IOException
	{
		return fillBytes(buf, 0, (null == buf) ? 0 : buf.length);
	}
	/*
	 * @see net.community.chest.net.BinaryNetConnection#readBinaryLine(byte[], net.community.chest.net.LineInfo)
	 */
	@Override
	public int readBinaryLine (final byte[] buf, final LineInfo li) throws IOException
	{
		return readBinaryLine(buf, 0, (null == buf) ? 0 : buf.length, li);
	}
	/*
	 * @see net.community.chest.net.BinaryNetConnection#readBinaryLine(byte[], int, int)
	 */
	@Override
	public LineInfo readBinaryLine (final byte[] buf, final int startOffset, final int maxLen) throws IOException
	{
		final LineInfo	li=new LineInfo();
		final int		nErr=readBinaryLine(buf, startOffset, maxLen, li);
		if (nErr < 0)
			throw new StreamCorruptedException("Cannot fill binary line info: err=" + nErr);
		return li;
	}
	/*
	 * @see net.community.chest.net.BinaryNetConnection#readBinaryLine(byte[])
	 */
	@Override
	public LineInfo readBinaryLine (final byte[] buf) throws IOException
	{
		return readBinaryLine(buf, 0, (null == buf) ? 0 : buf.length);
	}
	/*
	 * @see net.community.chest.net.BinaryNetConnection#asInputStream(boolean)
	 */
	@Override
	public InputStream asInputStream (final boolean autoClose) throws IOException
	{
		return NetBinaryInputStream.asInputStream(this, autoClose);
	}
	/*
	 * @see net.community.chest.net.BinaryNetConnection#readBinaryLine(byte[], int, int, net.community.chest.net.LineInfo)
	 */
	@Override
	public int readBinaryLine (final byte[] buf, final int startOffset, final int maxLen, final LineInfo li) throws IOException
	{
		if ((null == buf) || (startOffset < 0) || (maxLen < 2) || ((startOffset + maxLen) > buf.length) || (null == li))
			throw new IOException("Bad/Illegal bytes buffer and/or start offset/max length");
		li.reset();

		// we limit ourselves to ~32K character per-line (good for most known text protocols)
		for (int	curOffset=startOffset, readLen=0; readLen < Short.MAX_VALUE; readLen++)
		{
			final int	val=read();
			if ((-1) == val)
				throw new EOFException("Premature EOF while attempting to read binary line (char-by-char)");

			if ('\n' == val)
			{
				li.setLFDetected(true);
				return readLen+1;	// take into account the LF
			}
			else if ('\r' == val)
			{
				// NOTE !!! if the CR if not followed by a LF, then it will be omitted from the line data !!!
				li.setCRDetected(true);
				continue;
			}
			else	// "normal" character
			{
				buf[curOffset] = (byte) val;
				curOffset++;
					
				// if character BEFORE this was CR, then mark we do not have CR just before LF
				li.setCRDetected(false);

				// check if exahausted buffer
				if (li.incLength() >= maxLen)
					return readLen;
			}
		}

		throw new StreamCorruptedException("Virtual infinite loop exit on read binary line char-by-char");
	 }
	/*
	 * @see net.community.chest.net.BinaryNetConnection#writeBytes(byte[], int, int)
	 */
	@Override
	public int writeBytes (final byte[] buf, final int startPos, final int maxLen) throws IOException
	{
		return writeBytes(buf, startPos, maxLen, false);
	}
	/*
	 * @see net.community.chest.net.BinaryNetConnection#writeBytes(byte[], boolean)
	 */
	@Override
	public int writeBytes (final byte[] buf, final boolean flushIt) throws IOException
	{
		return writeBytes(buf, 0, (null == buf) ? 0 : buf.length, flushIt);
	}
	/*
	 * @see net.community.chest.net.BinaryNetConnection#writeBytes(byte[])
	 */
	@Override
	public int writeBytes (final byte[] buf) throws IOException
	{
		return writeBytes(buf, false);
	}
	/*
	 * @see net.community.chest.net.BinaryNetConnection#asOutputStream(boolean)
	 */
	@Override
	public OutputStream asOutputStream (boolean autoClose) throws IOException
	{
		return new NetBinaryOutputStream(this, autoClose);
	}
}
