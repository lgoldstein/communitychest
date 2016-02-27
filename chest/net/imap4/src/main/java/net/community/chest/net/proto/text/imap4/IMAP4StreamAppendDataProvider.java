package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.io.InputStream;

import net.community.chest.io.input.InputStreamEmbedder;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 24, 2008 2:12:33 PM
 */
public class IMAP4StreamAppendDataProvider extends InputStreamEmbedder implements IMAP4AppendDataProvider {
	private int _copyBufSize /* =0 */;
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4AppendDataProvider#getCopyBufferSize()
	 */
	@Override
	public int getCopyBufferSize ()
	{
		return _copyBufSize;
	}

	public void setCopyBufferSize (int sz)
	{
		_copyBufSize = sz;
	}

	private long	_lTotalData /* =0L */, _lTotalRead /* =0L */;
	public long getTotalData ()
	{
		return _lTotalData;
	}
	// special setter
	public void setTotalData (long lTotalData)
	{
		_lTotalData = lTotalData;
	}

	// special constructor
	protected IMAP4StreamAppendDataProvider ()
	{
		super(null, true);
	}

	public IMAP4StreamAppendDataProvider (InputStream inStream, boolean realClosure, long lTotalData, int copyBufSize) throws IOException
	{
		super(inStream, realClosure);

		if (null == this.in)
			throw new IOException("No " + InputStream.class.getSimpleName() + " instance provided");

		_lTotalData = lTotalData;
		_copyBufSize = copyBufSize;
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4AppendDataProvider#getData(byte[], int, int)
	 */
	@Override
	public int getData (byte[] buf, int nOffset, int nLen)
	{
		if ((null == this.in)
		 || (_lTotalData <= 0)
		 || (null == buf)
		 || (nOffset < 0)
		 || (nLen < 0)
		 || ((nOffset + nLen) > buf.length))
			return (-8001);

		for (int	curOffset=nOffset, remLen=nLen, readLen=0; (remLen > 0) && (readLen < nLen); )
		{
			// check if exhausted data before filling the buffer
			if (_lTotalRead >= _lTotalData)
				return readLen;

			final int	dataToRead=(int) Math.min(remLen, (_lTotalData - _lTotalRead));
			try
			{
				int	dataRead=read(buf, curOffset, dataToRead);
				if (dataRead <= 0)
					return (-8002);	// premature EOF
				
				_lTotalRead += dataRead;
				remLen -= dataRead;
				curOffset += dataRead;
				readLen += dataRead;
			}
			catch(IOException ioe)
			{
				return (-8003);
			}
		}

		// this point is reached if filled entire buffer
		return nLen;
	}
}
