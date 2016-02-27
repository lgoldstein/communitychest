/*
 * 
 */
package net.community.chest.net.proto.text.ssh;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>SSH_MSG_CHANNEL_OPEN_FAILURE reason codes as per RFC 4250</P>
 * 
 * @author Lyor G.
 * @since Jul 2, 2009 7:51:14 AM
 */
public enum SSHChannelOpenFailureCode implements CodeValueEncapsulator {
	SSH_OPEN_ADMINISTRATIVELY_PROHIBITED(1),
    SSH_OPEN_CONNECT_FAILED(2),
    SSH_OPEN_UNKNOWN_CHANNEL_TYPE(3),
    SSH_OPEN_RESOURCE_SHORTAGE(4);

	private final int	_code;
	/*
	 * @see net.community.chest.net.proto.text.ssh.CodeValueEncapsulator#getCodeValue()
	 */
	@Override
	public final int getCodeValue ()
	{
		return _code;
	}

	private final String	_mn;
	/*
	 * @see net.community.chest.net.proto.text.ssh.CodeValueEncapsulator#getMnemonic()
	 */
	@Override
	public final String getMnemonic ()
	{
		return _mn;
	}

	SSHChannelOpenFailureCode (int code)
	{
		_code = code;
		_mn = SSHProtocol.getMnemonicValue(name(), "SSH_OPEN_");
	}

	public static final List<SSHChannelOpenFailureCode>		VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final SSHChannelOpenFailureCode fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final SSHChannelOpenFailureCode fromReasonCode (final int c)
	{
		return SSHProtocol.fromReasonCode(c, VALUES);
	}

	public static final SSHChannelOpenFailureCode fromMnemonic (final String s)
	{
		return SSHProtocol.fromMnemonic(s, false, VALUES);
	}
}
