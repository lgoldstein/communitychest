/*
 * 
 */
package net.community.chest.net.proto.text.ssh.message.channel.request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.community.chest.net.proto.text.ssh.SSHProtocol;
import net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder;
import net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 12, 2009 12:23:16 PM
 */
public class WindowChangeChannelRequest extends AbstractChannelRequestSpecificTypeData<WindowChangeChannelRequest> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7648787648101861444L;
	public static final String	REQ_TYPE="window-change";
	public WindowChangeChannelRequest ()
	{
		super(REQ_TYPE);
	}

	private int	_charsWidth;
	public int getCharsWidth ()
	{
		return _charsWidth;
	}

	public void setCharsWidth (int charsWidth)
	{
		_charsWidth = charsWidth;
	}

	private int	_rowsHeight;
	public int getRowsHeight ()
	{
		return _rowsHeight;
	}

	public void setRowsHeight (int rowsHeight)
	{
		_rowsHeight = rowsHeight;
	}

	private int	_pixelsWidth;
	public int getPixelsWidth ()
	{
		return _pixelsWidth;
	}

	public void setPixelsWidth (int pixelsWidth)
	{
		_pixelsWidth = pixelsWidth;
	}

	private int	_pixelsHeight;
	public int getPixelsHeight ()
	{
		return _pixelsHeight;
	}

	public void setPixelsHeight (int pixelsHeight)
	{
		_pixelsHeight = pixelsHeight;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public WindowChangeChannelRequest read (final InputStream in) throws IOException
	{
		setCharsWidth(SSHProtocol.readUint32(in));
		setRowsHeight(SSHProtocol.readUint32(in));
		setPixelsWidth(SSHProtocol.readUint32(in));
		setPixelsHeight(SSHProtocol.readUint32(in));

		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public WindowChangeChannelRequest decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getRequestType() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setCharsWidth(in.readInt());
		setRowsHeight(in.readInt());
		setPixelsWidth(in.readInt());
		setPixelsHeight(in.readInt());

		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (final OutputStream out) throws IOException
	{
		SSHProtocol.writeUint32(out, getCharsWidth());
		SSHProtocol.writeUint32(out, getRowsHeight());
		SSHProtocol.writeUint32(out, getPixelsWidth());
		SSHProtocol.writeUint32(out, getPixelsHeight());
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#encode(net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder)
	 */
	@Override
	public void encode (SSHOutputDataEncoder out) throws IOException
	{
		if (null == out)
			throw new IOException("encode(" + getRequestType() + ") no " + SSHOutputDataEncoder.class.getSimpleName() + " instance");

		out.writeInt(getCharsWidth());
		out.writeInt(getRowsHeight());
		out.writeInt(getPixelsWidth());
		out.writeInt(getPixelsHeight());
	}
}
