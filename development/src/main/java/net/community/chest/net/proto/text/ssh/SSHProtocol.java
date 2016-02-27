/*
 * 
 */
package net.community.chest.net.proto.text.ssh;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.community.chest.io.IOCopier;
import net.community.chest.io.encode.endian.EndianEncoder;
import net.community.chest.io.file.FileIOUtils;
import net.community.chest.lang.StringUtil;
import net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder;
import net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder;
import net.community.chest.net.proto.text.ssh.io.SSHUtf8StringBuffer;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 2, 2009 7:36:08 AM
 */
public class SSHProtocol {
	/**
	 * Default protocol port
	 */
	public static final int	IPPORT_SSH=22;
	// Maximum sizes as defined in RFC4253
	public static final int MAX_UNCOMPRESSED_PAYLOAD_LEN=32768,
							MAX_PACKET_SIZE=35000;
	// initial identification value as per RFC4253
	public static final String	SSH_VERSION="2.0",
								SW_VERSION="chestSSH_1.0",
								SSH_PREFIX="SSH-",
								SSH_IDENT=SSH_PREFIX + SSH_VERSION + "-" + SW_VERSION;
	public static final char[]	SSH_IDENTChars=SSH_IDENT.toCharArray();
	/**
	 * Strips the delimiter (if found) from the initial string
	 * @param sn The {@link String} to scan and compress
	 * @param delim The delimiter to drop
	 * @return The &quot;compressed&quot; string - may be same as input if
	 * nothing to do (or string null/empty or exactly one character long)
	 */
	private static final String compressDelimiter (final String sn, final char delim)
	{
		final int	sLen=(null == sn) ? 0 : sn.length();
		if (sLen <= 1)
			return sn;

		final StringBuilder	sb=new StringBuilder(sLen);
		for (int	lastPos=0, curPos=0; ; curPos++)
		{
			if ((curPos=sn.indexOf(delim, curPos)) < lastPos)
				curPos = sLen;

			if (curPos > lastPos)
			{
				final String	v=sn.substring(lastPos, curPos);
				if (v.length() > 1)
				{
					final String	c=
						  String.valueOf(Character.toUpperCase(v.charAt(0)))
						+ v.substring(1).toLowerCase()
						;
					sb.append(c);
				}
				else
					sb.append(v);
			}

			if ((lastPos=curPos+1) >= sLen)
				break;
		}

		return sb.toString();
	}
	/**
	 * Builds a mnemonic string value
	 * @param n The original string name
	 * @param p The expected prefix of the name
	 * @return The camel-case mnemonic value after stripping the prefix
	 * and "compressing" the underlines. If null/empty original name/prefix
	 * or name does not begin with specified prefix then original name is
	 * returned
	 */
	public static final String getMnemonicValue (final String n, final String p)
	{
		if ((null == n) || (n.length() <= 0)
		 || (null == p) || (p.length() <= 0)
		 || (!StringUtil.startsWith(n, p, true, false)))
			return n;

		return compressDelimiter(n.substring(p.length()), '_');
	}

	public static final <E extends CodeValueEncapsulator> E fromReasonCode (final int c, final Collection<? extends E> vals)
	{
		if ((null == vals) || (vals.size() <= 0))
			return null;

		for (final E v : vals)
		{
			if ((v != null) && (v.getCodeValue() == c))
				return v;
		}

		return null;
	}

	public static final <E extends CodeValueEncapsulator> E fromMnemonic (
			final String s, final boolean caseSensitive, final Collection<? extends E> vals)
	{
		if ((null == s) || (s.length() <= 0))
			return null;
		if ((null == vals) || (vals.size() <= 0))
			return null;

		for (final E v : vals)
		{
			final String	n=(null == v) ? null : v.getMnemonic();
			if (StringUtil.compareDataStrings(s, n, caseSensitive) == 0)
				return v;
		}

		return null;
	}

	public static final short readByte (final InputStream in) throws IOException
	{
		if (null == in)
			throw new IOException("readByte() no " + InputStream.class.getSimpleName() + " instance provided");

		final int	v=in.read();
		if ((-1) == v)
			throw new EOFException("readByte() out of data");

		return (short) (v & 0x00FF);
	}

	public static final boolean readBoolean (final InputStream in) throws IOException
	{
		return (readByte(in) != 0);
	}

	public static final void writeBoolean (final OutputStream out, final boolean v) throws IOException
	{
		if (null == out)
			throw new IOException("writeBoolean() no " + OutputStream.class.getSimpleName() + " instance provided");
		out.write(v ? 1 : 0);
	}

	public static final void writeCodeValue (final OutputStream out, final CodeValueEncapsulator val) throws IOException
	{
		if (null == val)
			throw new IOException("writeCodeValue() no " + CodeValueEncapsulator.class.getSimpleName() + " instance provided");
		if (null == out)
			throw new IOException("writeCodeValue(" + val.getMnemonic() + ") no " + OutputStream.class.getSimpleName() + " instance provided");

		out.write(val.getCodeValue());
	}

	public static final int readUint32 (final InputStream in) throws IOException
	{
		return EndianEncoder.readSignedInt32(in, ByteOrder.BIG_ENDIAN);
	}

	public static final byte[] writeUint32 (final OutputStream out, final int val) throws IOException
	{
		return EndianEncoder.writeInt32(out, ByteOrder.BIG_ENDIAN, val);
	}

	public static final long readUint64 (final InputStream in) throws IOException
	{
		return EndianEncoder.readSignedInt64(in, ByteOrder.BIG_ENDIAN);
	}

	public static final byte[] writeUint64 (final OutputStream out, final long val) throws IOException
	{
		return EndianEncoder.writeInt64(out, ByteOrder.BIG_ENDIAN, val);
	}

	public static final int readStringLength (final InputStream in) throws IOException
	{
		return readUint32(in);
	}

	public static final int copyStringBytes (final InputStream in, final OutputStream out) throws IOException
	{
		final int	sLen=readStringLength(in);
		if (sLen < 0) // we do not expect strings >2GB
			throw new StreamCorruptedException("copyStringBytes() - required (" + sLen + ") data size mismatch");

		final long	cpyLen=IOCopier.copyStreams(in, out, sLen, sLen);
		if (cpyLen != sLen)
			throw new StreamCorruptedException("copyStringBytes() - required(" + sLen + ")/read(" + cpyLen + ") data size mismatch");

		return sLen;
	}

	public static final String readUTF8String (final InputStream in, final SSHUtf8StringBuffer sb) throws IOException
	{
		final int	sLen=copyStringBytes(in, sb);
		if (sLen <= 0)
			return "";

		return sb.toUTF8String();
	}

	public static final String readString (
			final InputStream in, final byte[] buf, final int off, final int len, final String charset)
		throws IOException
	{
		final int	sLen=readStringLength(in);
		if ((sLen < 0) || (sLen > len)) // we do not expect strings >2GB
			throw new StreamCorruptedException("readString(" + charset + ") - required (" + sLen + ") data size/available (" + len + ") mismatch");

		FileIOUtils.readFully(in, buf, off, sLen);
		return new String(buf, off, sLen, charset);
	}

	public static final String readUTF8String (final InputStream in, final byte[] buf, final int off, final int len)
		throws IOException
	{
		return readString(in, buf, off, len, "UTF-8");
	}

	public static final String readASCIIString (final InputStream in, final byte[] buf, final int off, final int len)
		throws IOException
	{
		return readString(in, buf, off, len, "US-ASCII");
	}

	public static final String readString (final InputStream in, final String charset)
		throws IOException
	{
		final int	sLen=readStringLength(in);
		if (sLen < 0) // we do not expect strings >2GB
			throw new StreamCorruptedException("readString(" + charset + ") - required (" + sLen + ") data size mismatch");
		if (sLen <= 0)
			return "";

		final byte[]	data=new byte[sLen];
		FileIOUtils.readFully(in, data, 0, sLen);
		return new String(data, 0, sLen, charset);
	}

	public static final String readUTF8String (final InputStream in) throws IOException
	{
		return readString(in, "UTF-8");
	}

	public static final String readASCIIString (final InputStream in) throws IOException
	{
		return readString(in, "US-ASCII");
	}

	// NOTE: throws StreamCorruptedException if failed to accommodate string in buffer
	public static final int readStringBytes (final InputStream in, final byte[] buf, final int off, final int len) throws IOException
	{
		final int	sLen=readStringLength(in);
		if ((sLen < 0) /* we do not expect strings >2GB */ || (sLen > len))
			throw new StreamCorruptedException("readStringBytes() - required(" + sLen + ")/available(" + len + ") data size mismatch");

		FileIOUtils.readFully(in, buf, off, sLen);
		return sLen;
	}

	public static final byte[]	EMPTY_STRING_BYTES=new byte[0];
	public static final byte[] readBlobData (final InputStream in) throws IOException
	{
		final int	sLen=readStringLength(in);
		if (sLen < 0) /* we do not expect strings >2GB */
			throw new StreamCorruptedException("readBlobData() - required(" + sLen + ") data size mismatch");

		final byte[]	data=(0 == sLen) ? EMPTY_STRING_BYTES : new byte[sLen];
		FileIOUtils.readFully(in, data, 0, sLen);
		return data;
	}

	public static final byte[] writeStringLength (final OutputStream out, final int len) throws IOException
	{
		return writeUint32(out, len);
	}

	public static final void writeStringBytes (final OutputStream out, final byte[] buf, final int off, final int len) throws IOException
	{
		if (len < 0) // we do not expect strings >2GB
			throw new StreamCorruptedException("writeStringBytes() - required (" + len + ") data size mismatch");

		writeStringLength(out, len);
		if (len > 0)	// OK if zero-length
			out.write(buf, off, len);
	}

	public static final void writeStringBytes (final OutputStream out, final byte ... data) throws IOException
	{
		writeStringBytes(out, data, 0, (null == data) ? 0 : data.length);
	}

	public static final byte[] writeStringBytes (
			final OutputStream out, final String data, final String charset)
		throws IOException
	{
		final int		sLen=(null == data) ? 0 : data.length();
		final byte[]	buf=
			(sLen <= 0) ? EMPTY_STRING_BYTES : data.getBytes(charset);
		writeStringBytes(out, buf);
		return buf;
	}

	public static final byte[] writeStringBytes (
			final OutputStream out, final boolean useASCII, final String data)
		throws IOException
	{
		return writeStringBytes(out, data, useASCII ? "US-ASCII" : "UTF-8");
	}

	public static final <A extends Appendable> A appendASCIIBytes (final InputStream in, final A sb, final int sLen) throws IOException
	{
		if (null == sb)
			throw new IOException("appendASCIIBytes(len=" + sLen + ") no " + Appendable.class.getSimpleName() + " instance provided");
		if (null == in)
			throw new IOException("appendASCIIBytes(len=" + sLen + ") no " + InputStream.class.getSimpleName() + " instance provided");
		if (sLen < 0) // we do not expect strings >2GB
			throw new StreamCorruptedException("appendASCIIBytes(len=" + sLen + ") - required data size mismatch");

		for (int	cIndex=0; cIndex < sLen; cIndex++)
		{
			final int	cv=in.read();
			if (cv == (-1))
				throw new EOFException("appendASCIIBytes((len=" + sLen + ") premature EOF after " + cIndex + " bytes");
			sb.append((char) (cv & 0x00FF));
		}

		return sb;
	}

	public static final <A extends Appendable> int appendASCIIBytes (final InputStream in, final A sb) throws IOException
	{
		final int	sLen=readStringLength(in);
		appendASCIIBytes(in, sb, sLen);
		return sLen;
	}
	// as per section 6 of RFC 4251
	public static final int	MAX_METHOD_NAME=64;
	public static final String readMethodName (final InputStream in) throws IOException
	{
		final StringBuilder	sb=new StringBuilder(MAX_METHOD_NAME);
		final int			sLen=appendASCIIBytes(in, sb);
		if (sLen <= 0)
			return "";
		return sb.toString();
	}

	public static final List<String> readNamesList (final InputStream in) throws IOException
	{
		final int	sLen=readStringLength(in);
		if (sLen < 0) // we do not expect strings >2GB
			throw new StreamCorruptedException("readNamesList() - required (" + sLen + ") data size mismatch");

		if (sLen == 0)
			return null;

		final StringBuilder	sb=
			appendASCIIBytes(in, new StringBuilder(sLen + 4), sLen);
		return StringUtil.splitString(sb.toString(), ',');
	}

	public static final int writeNamesList (final OutputStream out, final Collection<? extends CharSequence> nl) throws IOException
	{
		int	totalLen=0;
		if ((nl != null) && (nl.size() > 0))
		{
			for (final CharSequence	cs : nl)
			{
				final int	csLen=(null == cs) ? 0 : cs.length();
				if (csLen <= 0)
					throw new StreamCorruptedException("writeNamesList() - null/empty value in list");

				totalLen += csLen + 1 /* for the ',' */;
			}

			totalLen--;	// last value has no ',' after it
		}

		writeUint32(out, totalLen);
		if (totalLen > 0)
		{
			int	nIndex=0;
			for (final CharSequence	cs : nl)
			{
				final int	csLen=(null == cs) ? 0 : cs.length();
				if (csLen <= 0)
					throw new StreamCorruptedException("writeNamesList() - null/empty value in list");
				
				if (nIndex > 0)
					out.write(',');

				writeStringBytes(out, true, cs.toString());
				nIndex++;
			}
		}

		return totalLen;
	}
	
	public static final int writeNamesList (final OutputStream out, final CharSequence ... nl) throws IOException
	{
		return writeNamesList(out, ((null == nl) || (nl.length <= 0)) ? null : Arrays.asList(nl));
	}

	public static final byte[] writeNonNullStringBytes (
			final OutputStream out, final boolean useASCII, final String s)
		throws IOException
	{
		if ((null == s) || (s.length() <= 0))
			throw new IOException("writeNonNullStringBytes() no data provided");

		return writeStringBytes(out, useASCII, s);
	}

	public static final byte[] writeMethodName (final OutputStream out, final String n)
		throws IOException
	{
		return writeStringBytes(out, true, n);
	}

	public static final SSHMultiPrecisionInteger readMpint (final InputStream in)
		throws IOException
	{
		return new SSHMultiPrecisionInteger(in);
	}
	/**
	 * @param in The {@link InputStream} to read from
	 * @param org The {@link SSHMultiPrecisionInteger} to read into. If null,
	 * then a new instance is created
	 * @return The created/updated instance
	 * @throws IOException if failed to read
	 */
	public static final SSHMultiPrecisionInteger readMpint (
			final InputStream in, final SSHMultiPrecisionInteger org)
		throws IOException
	{
		if (null == org)
			return readMpint(in);
		else
			return org.read(in);
	}

	public static final SSHMultiPrecisionInteger decodeMpint (SSHInputDataDecoder in)
		throws IOException
	{
		return new SSHMultiPrecisionInteger(in);
	}
	/**
	 * @param in The {@link SSHInputDataDecoder} to read from
	 * @param org The {@link SSHMultiPrecisionInteger} to decode into. If null,
	 * then a new instance is created
	 * @return The created/updated instance
	 * @throws IOException if failed to read
	 */
	public static final SSHMultiPrecisionInteger decodeMpint (
			final SSHInputDataDecoder in, final SSHMultiPrecisionInteger org)
		throws IOException
	{
		if (null == org)
			return decodeMpint(in);
		else
			return org.decode(in);
	}

	public static final void writeMpint (
			final OutputStream out, final SSHMultiPrecisionInteger val)
		throws IOException
	{
		if (null == val)
			throw new StreamCorruptedException("writeMpint() no value to write");
		val.write(out);
	}

	public static final void encodeMpint (
			final SSHOutputDataEncoder out, final SSHMultiPrecisionInteger val)
		throws IOException
	{
		if (null == val)
			throw new StreamCorruptedException("encodeMpint() no value to write");
		val.encode(out);
	}
}
