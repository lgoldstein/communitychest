/*
 * 
 */
package net.community.chest.net.proto.text.ssh.message.channel.request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;

import net.community.chest.net.proto.text.ssh.SSHProtocol;
import net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder;
import net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 12, 2009 12:34:01 PM
 */
public class ExitSignalChannelRequest extends AbstractChannelRequestSpecificTypeData<ExitSignalChannelRequest> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6554760410550003775L;
	public static final String	REQ_TYPE="exit-signal";
	public ExitSignalChannelRequest ()
	{
		super(REQ_TYPE);
	}

	private String	_signalName;
	public String getSignalName()
	{
		return _signalName;
	}

	public void setSignalName(String v)
	{
		_signalName = v;
	}

	private Boolean	_coreDumped;
	public Boolean getCoreDumped ()
	{
		return _coreDumped;
	}

	public void setCoreDumped (Boolean coreDumped)
	{
		_coreDumped = coreDumped;
	}

	private String	_errorMessage;
	public String getErrorMessage ()
	{
		return _errorMessage;
	}

	public void setErrorMessage (String v)
	{
		_errorMessage = v;
	}

	private String	_languageTag;
	public String getLanguageTag ()
	{
		return _languageTag;
	}

	public void setLanguageTag (String languageTag)
	{
		_languageTag = languageTag;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#read(java.io.InputStream)
	 */
	@Override
	public ExitSignalChannelRequest read (final InputStream in) throws IOException
	{
		setSignalName(SSHProtocol.readASCIIString(in));
		setCoreDumped(Boolean.valueOf(SSHProtocol.readBoolean(in)));
		setErrorMessage(SSHProtocol.readUTF8String(in));
		setLanguageTag(SSHProtocol.readASCIIString(in));

		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public ExitSignalChannelRequest decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getRequestType() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setSignalName(in.readASCII());
		setCoreDumped(Boolean.valueOf(in.readBoolean()));
		setErrorMessage(in.readUTF());
		setLanguageTag(in.readASCII());

		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (final OutputStream out) throws IOException
	{
		final Boolean	rr=getCoreDumped();
		if (null == rr)
			throw new StreamCorruptedException("write(" + getRequestType() + ") missing core-dumped value");

		SSHProtocol.writeStringBytes(out, true, getSignalName());
		SSHProtocol.writeBoolean(out, rr.booleanValue());
		SSHProtocol.writeStringBytes(out, false, getErrorMessage());
		SSHProtocol.writeStringBytes(out, true, getLanguageTag());
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#encode(net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder)
	 */
	@Override
	public void encode (SSHOutputDataEncoder out) throws IOException
	{
		if (null == out)
			throw new IOException("encode(" + getRequestType() + ") no " + SSHOutputDataEncoder.class.getSimpleName() + " instance");

		final Boolean	rr=getCoreDumped();
		if (null == rr)
			throw new StreamCorruptedException("encode(" + getRequestType() + ") missing core-dumped value");

		out.writeASCII(getSignalName());
		out.writeBoolean(rr.booleanValue());
		out.writeUTF(getErrorMessage());
		out.writeASCII(getLanguageTag());
	}
}
