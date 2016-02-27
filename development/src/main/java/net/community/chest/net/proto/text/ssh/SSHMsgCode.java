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
 * <P>Message ID numbers as per RFC 4250</P>
 * 
 * @author Lyor G.
 * @since Jul 2, 2009 8:57:58 AM
 */
public enum SSHMsgCode implements CodeValueEncapsulator {
	SSH_MSG_DISCONNECT(1),
	SSH_MSG_IGNORE(2),
	SSH_MSG_UNIMPLEMENTED(3),
	SSH_MSG_DEBUG(4),
	SSH_MSG_SERVICE_REQUEST(5),
	SSH_MSG_SERVICE_ACCEPT(6),

	SSH_MSG_KEXINIT(20),
	SSH_MSG_NEWKEYS(21),

	// Diffie-Hellman key exchange codes (RFC 4419)
	SSH_MSG_KEX_DH_GEX_REQUEST_OLD(30),
    SSH_MSG_KEX_DH_GEX_GROUP(31),
    SSH_MSG_KEX_DH_GEX_INIT(32),
    SSH_MSG_KEX_DH_GEX_REPLY(33),
    SSH_MSG_KEX_DH_GEX_REQUEST(34),

	SSH_MSG_USERAUTH_REQUEST(50),
	SSH_MSG_USERAUTH_FAILURE(51),
	SSH_MSG_USERAUTH_SUCCESS(52),
	SSH_MSG_USERAUTH_BANNER(53),

	SSH_MSG_USERAUTH_PK_OK(60),	// as per RFC 4252
	SSH_MSG_USERAUTH_PASSWD_CHANGEREQ(60), // as per RFC4252

	SSH_MSG_GLOBAL_REQUEST(80),
	SSH_MSG_REQUEST_SUCCESS(81),
	SSH_MSG_REQUEST_FAILURE(82),

	SSH_MSG_CHANNEL_OPEN(90),
	SSH_MSG_CHANNEL_OPEN_CONFIRMATION(91),
	SSH_MSG_CHANNEL_OPEN_FAILURE(92),
	SSH_MSG_CHANNEL_WINDOW_ADJUST(93),
	SSH_MSG_CHANNEL_DATA(94),
	SSH_MSG_CHANNEL_EXTENDED_DATA(95),
	SSH_MSG_CHANNEL_EOF(96),
	SSH_MSG_CHANNEL_CLOSE(97),
	SSH_MSG_CHANNEL_REQUEST(98),
	SSH_MSG_CHANNEL_SUCCESS(99),
	SSH_MSG_CHANNEL_FAILURE(100);

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

	SSHMsgCode (int code)
	{
		_code = code;
		_mn = SSHProtocol.getMnemonicValue(name(), "SSH_MSG_");
	}

	public static final List<SSHMsgCode>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final SSHMsgCode fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
	// NOTE !!! some duplicates exist
	public static final SSHMsgCode fromReasonCode (final int c)
	{
		return SSHProtocol.fromReasonCode(c, VALUES);
	}

	public static final SSHMsgCode fromMnemonic (final String s)
	{
		return SSHProtocol.fromMnemonic(s, false, VALUES);
	}
}
