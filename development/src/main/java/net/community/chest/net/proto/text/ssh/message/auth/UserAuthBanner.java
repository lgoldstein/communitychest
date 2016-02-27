/*
 * 
 */
package net.community.chest.net.proto.text.ssh.message.auth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.community.chest.net.proto.text.ssh.SSHMsgCode;
import net.community.chest.net.proto.text.ssh.SSHProtocol;
import net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder;
import net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder;
import net.community.chest.net.proto.text.ssh.message.AbstractSSHMsgEncoder;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 2, 2009 9:54:00 AM
 */
public class UserAuthBanner extends AbstractSSHMsgEncoder<UserAuthBanner> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8923063284915000766L;
	public UserAuthBanner ()
	{
		super(SSHMsgCode.SSH_MSG_USERAUTH_BANNER);
	}

	private String	_message;
	public String getMessage ()
	{
		return _message;
	}

	public void setMessage (String message)
	{
		_message = message;
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
	public UserAuthBanner read (InputStream in) throws IOException
	{
		setMessage(SSHProtocol.readUTF8String(in));
		setLanguageTag(SSHProtocol.readASCIIString(in));

		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public UserAuthBanner decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getMsgCode() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setMessage(in.readUTF());
		setLanguageTag(in.readASCII());

		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (OutputStream out) throws IOException
	{
		SSHProtocol.writeStringBytes(out, false, getMessage());
		SSHProtocol.writeStringBytes(out, true, getLanguageTag());
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#encode(net.community.chest.net.proto.text.ssh.io.SSHOutputDataEncoder)
	 */
	@Override
	public void encode (SSHOutputDataEncoder out) throws IOException
	{
		if (null == out)
			throw new IOException("encode(" + getMsgCode() + ") no " + SSHOutputDataEncoder.class.getSimpleName() + " instance");

		out.writeUTF(getMessage());
		out.writeASCII(getLanguageTag());
	}
}
