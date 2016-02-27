package net.community.chest.io.encode.base64;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

import net.community.chest.io.IOCopier;
import net.community.chest.io.output.BufferedOutputStreamEmbedder;

/**
 * Helper class that encodes everything written to it into BASE64 and then
 * writes it to the "real" output stream. NOTE: on "close" no additional
 * CRLF is added, so it is up to the caller to add CRLF at end of BASE64
 * encoded block.
 * @author lyorg
 * 08/03/2004
 */
public final class Base64EncodeOutputStream extends BufferedOutputStreamEmbedder {
	/* 		Actually these are "char"-s, but we KNOW they fit in a byte,
	 * so we store them like this in order to use a single "write" call
	 * to the underlying output stream
	 */
	private int		_iVal /* =0 */;	// actually holds 3 bytes each time
	private byte	_iPos /* =0 */, _lineLen /* =0 */;
	private final boolean	_finalizeOnClose, _lineBreaks, _useCrLf;
	/**
	 * Writes BASE64 encoded data to the provided (real) stream according to the options
	 * @param os real output stream to receive BASE64 encoded data
	 * @param bufSize internal buffer size (exception if <BASE64_OUTPUT_BLOCK_LEN)
	 * @param options encoding options
	 * @param finalizeOnClose if TRUE, then any left-over data is padded. Note: if FALSE,
	 * and un-encoded data remains by the time "close" function is called, then an
	 * exception is thrown
	 * @param realClose if TRUE then {@link #close()}-ing this stream also
	 * closes the underlying stream
	 */
	public Base64EncodeOutputStream (OutputStream os, int bufSize, Collection<Base64EncodeOptions> options, boolean finalizeOnClose, boolean realClose)
	{
		super(os, bufSize, realClose);

		if (null == this.out)	// just making sure
			throw new NullPointerException("No output stream specified");
		if (bufSize < Base64.BASE64_OUTPUT_BLOCK_LEN)
			throw new IllegalArgumentException("Buffering size too smal: " + bufSize);

		_finalizeOnClose = finalizeOnClose;
		_lineBreaks = (options != null) && options.contains(Base64EncodeOptions.BREAK);
		_useCrLf = (options != null) && options.contains(Base64EncodeOptions.CRLF);
	}
	/**
	 * Writes BASE64 encoded data to the provided (real) stream according to the options
	 * @param os real output stream to receive BASE64 encoded data
	 * @param bufSize internal buffer size (exception if <BASE64_OUTPUT_BLOCK_LEN)
	 * @param options encoding options
	 * @param finalizeOnClose if TRUE, then any left-over data is padded. Note: if FALSE,
	 * and un-encoded data remains by the time "close" function is called, then an
	 * exception is thrown
	 */
	public Base64EncodeOutputStream (OutputStream os, int bufSize, Collection<Base64EncodeOptions> options, boolean finalizeOnClose)
	{
		this(os, bufSize, options, finalizeOnClose, true);
	}
	/**
	 * Writes BASE64 encoded data to the provided (real) stream according
	 * to the options. Note: uses an internal buffer of size={@link IOCopier#DEFAULT_COPY_SIZE}
	 * @param os real output stream to receive BASE64 encoded data
	 * @param options encoding options
	 * @param finalizeOnClose if TRUE, then any left-over data is padded. Note: if FALSE,
	 * and un-encoded data remains by the time "close" function is called, then an
	 * exception is thrown
	 */
	public Base64EncodeOutputStream (OutputStream os, Collection<Base64EncodeOptions> options, boolean finalizeOnClose)
	{
		this(os, IOCopier.DEFAULT_COPY_SIZE, options, finalizeOnClose);
	}

	public static final Collection<Base64EncodeOptions>	DEFAULT_OPTIONS=
		Collections.unmodifiableSet(EnumSet.allOf(Base64EncodeOptions.class));
	/**
	 * Masks the provided output stream so that long lines are broken
	 * according to the standard length using CRLF
	 * @param os real output stream to write encoded data to
	 */
	public Base64EncodeOutputStream (OutputStream os)
	{
		this(os, DEFAULT_OPTIONS, true);
	}
	/**
	 * Checks if need to issue a line break
	 * @throws IOException if errors in flushing/writing
	 */
	protected void checkLineBreaks () throws IOException
	{
		/*
		 * NOTE: we check if PREVIOUS line needs termination so
		 * that if the BASE64 encoding is an EXACT multiple of
		 * the standard line length, there will be only ONE CRLF
		 * at end of encoded block
		 */
		if (_lineBreaks && (_lineLen >= Base64.ENCLINE_STDLEN))
		{
			if (this.buf.length > Base64.BASE64_OUTPUT_BLOCK_LEN)
			{	
				// check if can accommodate the CRLF
				if ((this.buf.length - this.count) < 2)
					flush();

				if (_useCrLf)
				{	
					this.buf[this.count] = (byte) '\r';
					this.count++;
				}

				this.buf[this.count] = (byte) '\n';
				this.count++;
			}
			else	// no real buffering available
			{
				if (_useCrLf)
					this.out.write((byte) '\r');
				this.out.write((byte) '\n');
			}

			_lineLen = 0;	// restart counting line length
		}
	}
	/*
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public synchronized void write (byte[] wbuf, int offset, int len) throws IOException
	{
		if (null == this.out)
			throw new IOException("No underlying output stream to write to");
		if ((offset < 0) || (len < 0))
			throw new IOException("Bad/Illegal BASE64 buffer specs to encode");
		if (0 == len)
			return;

		final int	maxOffset=(offset + len);
		if ((null == wbuf) || (maxOffset > wbuf.length))
			throw new IOException("Bad/Illegal BASE64 buffer to encode");
		
		// start going over the buffer 3 bytes at a time for as long as possible
		for (int	curOffset=offset; curOffset < maxOffset; curOffset++)
		{
			_iVal = (_iVal << 8) & 0x00FFFF00;	// make room for next value
			_iVal |= (wbuf[curOffset] & 0x00FF);	// "append" next value
			_iPos++;

			// check if have a full input buffer
			if (_iPos < Base64.BASE64_INPUT_BLOCK_LEN)
				continue;

			// check if required to format the output into lines
			if (_lineBreaks)
			{
			    checkLineBreaks();
				// take into account buffer we are about to write
				_lineLen += Base64.BASE64_OUTPUT_BLOCK_LEN;
			}

			// check if can accommodate the new block (and have some buffering)
			if ((this.buf.length > Base64.BASE64_OUTPUT_BLOCK_LEN) && ((this.buf.length - this.count) < Base64.BASE64_OUTPUT_BLOCK_LEN))
				flush();

			/* 		The basic idea is that the three bytes get split
			 * into four bytes along these lines:
			 *
			 * 		[AAAAAABB] [BBBBCCCC] [CCDDDDDD]
			 * into:
			 *		[xxAAAAAA] [xxBBBBBB] [xxCCCCCC] [xxDDDDDD]
			 */
			this.buf[this.count	  ]  = Base64.base64Chars[(_iVal >> 18) & Base64.BASE64_MASK_VALUE];
			this.buf[this.count + 1] = Base64.base64Chars[(_iVal >> 12) & Base64.BASE64_MASK_VALUE];
			this.buf[this.count + 2] = Base64.base64Chars[(_iVal >>  6) & Base64.BASE64_MASK_VALUE];
			this.buf[this.count + 3] = Base64.base64Chars[ _iVal        & Base64.BASE64_MASK_VALUE];

			if (this.buf.length > Base64.BASE64_OUTPUT_BLOCK_LEN)
				this.count += Base64.BASE64_OUTPUT_BLOCK_LEN;
			else	// no real buffering, so write out immediately
				this.out.write(this.buf, 0, Base64.BASE64_OUTPUT_BLOCK_LEN);
			
			_iPos = 0;
			_iVal = 0;
		}
	}
	/*
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close () throws IOException
	{
		if (this.out != null)
		{
			if (_finalizeOnClose && (_iPos > 0))
			{
			    checkLineBreaks();

				// check if can accommodate final block
				if ((this.buf.length - this.count) < Base64.BASE64_OUTPUT_BLOCK_LEN)
					flush();

				/*		Since the input is encoded in blocks of 3 octets, the remainder
				 * can only be between 0-2 (and we have handled 0).
				 */
				switch(_iPos)
				{
					case 1	:
						// 6 high bits, followed by 2 low bits padded with 4 zeros
						this.buf[this.count    ] = Base64.base64Chars[(_iVal >> 2) & Base64.BASE64_MASK_VALUE];
						this.buf[this.count + 1] = Base64.base64Chars[(_iVal << 4) & 0x0030];

						this.count += 2;
						_lineLen += 2;
						break;

					case 2	:
						// high 6 bits of the available 16
						this.buf[this.count    ] = Base64.base64Chars[(_iVal >> 10) & Base64.BASE64_MASK_VALUE];
						this.buf[this.count + 1] = Base64.base64Chars[(_iVal >> 4) & Base64.BASE64_MASK_VALUE];
						this.buf[this.count + 2] = Base64.base64Chars[(_iVal << 2) & 0x003C];
						
						this.count += 3;
						_lineLen += 3;
						break;

					default	:	// should not happen
						throw new IOException("Too much left-over data to finalize: " + _iPos + " bytes");
				}

				// pad the remaining size
				for (_iPos++ ; _iPos < Base64.BASE64_OUTPUT_BLOCK_LEN; _iPos++, this.count++, _lineLen++)
					this.buf[this.count] = Base64.BASE64_PAD_CHAR;

				_iPos = 0;	// mark that all data has been handled to avoid exception further on
			}

			if (_iPos != 0)	// make sure no data left un-encoded
				throw new IOException("Incomplete BASE64 data - " + _iPos + " bytes left un-encoded");

			flush();	// output any remaining data

			super.close();
		}
	}
	/*
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable
	{
		try { close(); } catch(IOException ioe) { /* ignored */ }
		super.finalize();
	}
}