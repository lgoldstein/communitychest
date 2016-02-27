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
 * @since Jul 12, 2009 10:31:43 AM
 */
public class UserAuthPasswordChangeReq extends AbstractSSHMsgEncoder<UserAuthPasswordChangeReq> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8248598106405113531L;
	public UserAuthPasswordChangeReq ()
	{
		super(SSHMsgCode.SSH_MSG_USERAUTH_PASSWD_CHANGEREQ);
	}

	private String	_prompt;
	public String getPrompt ()
	{
		return _prompt;
	}

	public void setPrompt (String message)
	{
		_prompt = message;
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
	public UserAuthPasswordChangeReq read (InputStream in) throws IOException
	{
		setPrompt(SSHProtocol.readUTF8String(in));
		setLanguageTag(SSHProtocol.readASCIIString(in));

		return this;
	}
	/*
	 * @see net.community.chest.net.proto.text.ssh.SSHDataObjectEncoder#decode(net.community.chest.net.proto.text.ssh.io.SSHInputDataDecoder)
	 */
	@Override
	public UserAuthPasswordChangeReq decode (SSHInputDataDecoder in) throws IOException
	{
		if (null == in)
			throw new IOException("decode(" + getMsgCode() + ") no " + SSHInputDataDecoder.class.getSimpleName() + " instance");

		setPrompt(in.readUTF());
		setLanguageTag(in.readASCII());

		return this;
	}
	/*
	 * @see net.community.chest.io.encode.ElementEncoder#write(java.io.OutputStream)
	 */
	@Override
	public void write (OutputStream out) throws IOException
	{
		SSHProtocol.writeStringBytes(out, false, getPrompt());
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

		out.writeUTF(getPrompt());
		out.writeASCII(getLanguageTag());
	}
}
