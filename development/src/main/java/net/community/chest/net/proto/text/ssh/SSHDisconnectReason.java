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
 * <P>SSH_MSG_DISCONNECT reason codes as per RFC 4250</P>
 * 
 * @author Lyor G.
 * @since Jul 2, 2009 7:44:17 AM
 */
public enum SSHDisconnectReason implements CodeValueEncapsulator {
	SSH_DISCONNECT_HOST_NOT_ALLOWED_TO_CONNECT(1),
	SSH_DISCONNECT_PROTOCOL_ERROR(2),
	SSH_DISCONNECT_KEY_EXCHANGE_FAILED(3),
	SSH_DISCONNECT_RESERVED(4),
	SSH_DISCONNECT_MAC_ERROR(5),
	SSH_DISCONNECT_COMPRESSION_ERROR(6),
	SSH_DISCONNECT_SERVICE_NOT_AVAILABLE(7),
	SSH_DISCONNECT_PROTOCOL_VERSION_NOT_SUPPORTED(8),
	SSH_DISCONNECT_HOST_KEY_NOT_VERIFIABLE(9),
	SSH_DISCONNECT_CONNECTION_LOST(10),
	SSH_DISCONNECT_BY_APPLICATION(11),
	SSH_DISCONNECT_TOO_MANY_CONNECTIONS(12),
	SSH_DISCONNECT_AUTH_CANCELLED_BY_USER(13),
	SSH_DISCONNECT_NO_MORE_AUTH_METHODS_AVAILABLE(14),
	SSH_DISCONNECT_ILLEGAL_USER_NAME(15);

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

	SSHDisconnectReason (int code)
	{
		_code = code;
		_mn = SSHProtocol.getMnemonicValue(name(), "SSH_DISCONNECT_");
	}

	public static final List<SSHDisconnectReason>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final SSHDisconnectReason fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final SSHDisconnectReason fromReasonCode (final int c)
	{
		return SSHProtocol.fromReasonCode(c, VALUES);
	}

	public static final SSHDisconnectReason fromMnemonic (final String s)
	{
		return SSHProtocol.fromMnemonic(s, false, VALUES);
	}
}
