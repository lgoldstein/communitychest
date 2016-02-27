package net.community.chest.net.proto.text.smtp;

import java.io.IOException;
import java.nio.CharBuffer;

import net.community.chest.io.EOLStyle;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 * 
 * <P>This class holds general definitions of the SMTP protocol</P>
 * 
 * @author Lyor G.
 * @since Sep 19, 2007 3:38:33 PM
 */
public final class SMTPProtocol {
	private SMTPProtocol ()
	{
		// disable any instanciation or derivation
	}
	/**
	 * Default SMTP protocol port
	 */
	public static final int IPPORT_SMTP=25;
	/**
	 * Maximum expected single line in SMTP protocol
	 */
	public static final int	MAX_SMTP_LINE_LEN=1022 /* as per RFC821 */ + 2 /* CRLF */;
	// Some SMTP errors of interest
	public static final int SMTP_E_SYS_STAT=211,			// System status, or system help reply
							SMTP_E_HELP_MSG=214,			// Help message
							SMTP_E_DOMAIN_RDY=220,			// <domain> Service ready
							SMTP_E_DOMAIN_CLS=221,			// <domain> Service closing transmit channel
							SMTP_E_ACTION_OK=250,			// Requested mail action okay, completed
							SMTP_E_USR_NLOCAL=251,			// User not local; will forward to <path>
							SMTP_E_USR_NOT_VERIFIED=252,	// User will be accepted, even though not verified

							SMTP_E_START_INP=354,	// Start mail input; end with <CRLF>.<CRLF>

							SMTP_E_SRVC_NA=421,		// <domain> Service not available
							SMTP_E_ACTION_NA=450,	// Requested mail action not taken 
							SMTP_E_ACTION_ABRT=451,	// Requested action aborted
							SMTP_E_ACTION_MEM=452,	// insufficient system storage

							SMTP_E_CMD_UNKNOWN=500,	// Syntax error, command unrecognized
							SMTP_E_CMD_SYNTAX=501,	// Syntax error in parameters or arguments
							SMTP_E_CMD_NOTIMPL=502,	// Command not implemented
							SMTP_E_CMD_SEQUENCE=503,// Bad sequence of commands
							SMTP_E_CMD_PARAM=504,	// Command parameter not implemented
							
							SMTP_E_CLOSE_TRANS=521,	// Domain does not accept mail or closing transmission channel

							SMTP_E_MBOX_NA=550,				// mailbox unavailable
							SMTP_E_USR_TRY=551,				// User not local; please try <forward-path>
							SMTP_E_MEM_OVRFLW=552,			// exceeded storage allocation
							SMTP_E_MBOX_NAME=553,			// mailbox name not allowed
							SMTP_E_TRAN_FAILED=554,			// Transaction failed
							SMTP_E_TOO_MANY_DUP_MSGS=557,	// Too many duplicate messages: Resource temporarily unavailable
							
							SMTP_E_RELAY_NA=572;	// Relay not authorized, Not a local host, Not a gateway

	// useful SMTP commands
	public static final String SMTPHelpCmd="HELP";
		public static final char[] SMTPHelpCmdChars=SMTPHelpCmd.toCharArray();
	public static final String SMTPHeloCmd="HELO";
		public static final char[] SMTPHeloCmdChars=SMTPHeloCmd.toCharArray();
	public static final String SMTPEhloCmd="EHLO"; 
		public static final char[] SMTPEhloCmdChars=SMTPEhloCmd.toCharArray(); 
	public static final String SMTPMailFromCmd="MAIL FROM:";
		public static final char[] SMTPMailFromCmdChars=SMTPMailFromCmd.toCharArray();
	public static final String SMTPRcptToCmd="RCPT TO:";
		public static final char[] SMTPRcptToCmdChars=SMTPRcptToCmd.toCharArray();
	public static final String SMTPDataCmd="DATA";
		public static final char[] SMTPDataCmdChars=SMTPDataCmd.toCharArray();
	public static final String SMTPRsetCmd="RSET";
		public static final char[] SMTPRsetCmdChars=SMTPRsetCmd.toCharArray();
	public static final String SMTPQuitCmd="QUIT";
		public static final char[] SMTPQuitCmdChars=SMTPQuitCmd.toCharArray();
	
	// some ESMTP errors of interest
	public static final int ESMTP_E_AUTH_SUCCEED=235,	// authentication successful
							
							ESMTP_E_AUTH_DATA=334,		// authentication challenge data

							ESMTP_E_PASSWD_TRANS=432,	// A password transition is needed (see RFC2554)
							ESMTP_E_TEMP_AUTH_FAIL=454,	// Temporary authentication failure (see RFC2554)

							ESMTP_E_AUTH_REQ=530,			// Authentication required for this operation (see RFC2554)
							ESMTP_E_AUTH_TOO_WEAK=534,		// Authentication mechanism is too weak (see RFC2554)
							ESMTP_E_AUTH_DATA_REJECTED=535,	// Authentication data sent by user is rejected (see RFC2554)
							ESMTP_E_ENCRYPT_REQ=538;		// Encryption required for requested authentication mechanism (see RFC2554)

	// useful ESMTP commands
	public static final String ESMTPAuthKwd="AUTH";
		public static final char[] ESMTPAuthKwdChars=ESMTPAuthKwd.toCharArray();
		/*
		 *  	AUTH challenge response by the client to signal cancellation - as
		 *  per RFC2554 section 4 page 1
		 */
		public static final String ESMTPAuthCancelValue="*";

	// LOGIN authentication mechanism
	public static final String ESMTPLoginKwd="LOGIN";
		public static final char[] ESMTPLoginKwdChars=ESMTPLoginKwd.toCharArray();
	public static final String ESMTPAuthLoginCmd=ESMTPAuthKwd + " " + ESMTPLoginKwd;
		public static final char[] ESMTPAuthLoginCmdChars=ESMTPAuthLoginCmd.toCharArray();

	// PLAIN authentication mechanism
	public static final String ESMTPPlainKwd="PLAIN";
		public static final char[] ESMTPPlainKwdChars=ESMTPPlainKwd.toCharArray();
	public static final String ESMTPAuthPlainCmd=ESMTPAuthKwd + " " + ESMTPPlainKwd;
		public static final char[] ESMTPAuthPlainCmdChars=ESMTPAuthPlainCmd.toCharArray();

	// CRAM-MD5 authentication mechanism
	public static final String ESMTPAuthCRAMMD5Kwd="CRAM-MD5";
		public static final char[] ESMTPAuthCRAMMD5KwdChars=ESMTPAuthCRAMMD5Kwd.toCharArray();
	public static final String ESMTPAuthCRAMMD5Cmd=ESMTPAuthKwd + " " + ESMTPAuthCRAMMD5Kwd;
		public static final char[] ESMTPAuthCRAMMD5CmdChars=ESMTPAuthCRAMMD5Cmd.toCharArray();

		// (D)elivery (S)tatus (N)otification options
	public static final String ESMTPDSNKwd="DSN";
		public static final char[] ESMTPDSNKwdChars=ESMTPDSNKwd.toCharArray();
	public static final String ESMTPDSNENVIDKwd="ENVID";
		public static final char[] ESMTPDSNENVIDKwdChars=ESMTPDSNENVIDKwd.toCharArray();
	public static final String ESMTPDSNRETKwd="RET";
		public static final char[]	ESMTPDSNRETKwdChars=ESMTPDSNRETKwd.toCharArray();
	public static final String ESMTPDSNNotifyKwd="NOTIFY";
		public static final char[] ESMTPDSNNotifyKwdChars=ESMTPDSNNotifyKwd.toCharArray();
			public static final String	ESMTPDSNNotifyNeverOpt="NEVER";
				public static final char[]	ESMTPDSNNotifyNeverOptChars=ESMTPDSNNotifyNeverOpt.toCharArray();
			public static final String	ESMTPDSNNotifySuccessOpt="SUCCESS";
				public static final char[]	ESMTPDSNNotifySuccessOptChars=ESMTPDSNNotifySuccessOpt.toCharArray();
			public static final String	ESMTPDSNNotifyFailureOpt="FAILURE";
				public static final char[]	ESMTPDSNNotifyFailureOptChars=ESMTPDSNNotifyFailureOpt.toCharArray();
			public static final String	ESMTPDSNNotifyDelayOpt="DELAY";
				public static final char[]	ESMTPDSNNotifyDelayOptChars=ESMTPDSNNotifyDelayOpt.toCharArray();
	public static final String ESMTPDSNORCPTKwd="ORCPT";
		public static final char[] ESMTPDSNORCPTKwdChars=ESMTPDSNORCPTKwd.toCharArray();
	/**
	 * Builds a final command line (including CRLF) given the command and its (optional) argument
	 * @param <A> The {@link Appendable} instance to use
	 * @param sb string buffer to append the command to
	 * @param cmd command to be sent
	 * @param arg argument (may be null/empty)
	 * @return same as input buffer
	 * @throws IOException if cannot build the command
	 */
	public static final <A extends Appendable> A buildFinalCommand (final A sb, final char[] cmd, final char... arg) throws IOException
	{
		if ((null == sb) || (null == cmd) || (cmd.length <= 0))
			throw new IOException(ClassUtil.getArgumentsExceptionLocation(SMTPProtocol.class, "buildFinalCommand", (null == cmd) ? null : new String(cmd), (null == arg) ? null : new String(arg)) + " bad/illegal arguments");

		sb.append(CharBuffer.wrap(cmd));
		
		if ((arg != null) && (arg.length > 0))
			sb.append(' ').append(CharBuffer.wrap(arg));

		return EOLStyle.CRLF.appendEOL(sb);
	}
	/**
	 * Replaces the command address argument with the new one - leaving all
	 * other extra arguments (if any) intact (e.g., ESMTP ORCPT)
	 * @param cmdName command name - may NOT be null/empty
	 * @param orgLine original line - may NOT be null/empty. Assumed to
	 * contain a colon delimited SMTP command (MAIL FROM: RCPT TO:).
	 * <B>Note:</B> the original command is <U>ignored</U> and replaced by the
	 * <I>cmdName</I> argument (only the ':' is sought - it <U>must</U> exist
	 * and <U>cannot</U> be the first)
	 * @param newAddr new address to be used - may be null/empty (in which case
	 * the "<>" value will be used instead)
	 * @return new line after replacement (null if internal error(s)).
	 * <B>Note:</B> replaced address will be delimited by "<>" if not already
	 * done so
	 */
	public static final String replaceCommandAddress (final String cmdName, final String orgLine, final String newAddr)
	{
		final int	oLen=(null == orgLine) ? 0 : orgLine.length();
		if ((null == cmdName) || (cmdName.length() <= 0) || (oLen <= 0))
			return null;

		final int	colonIndex=orgLine.indexOf(':');
		if ((colonIndex <= 0) // MUST exist and CANNOT be first
		 || (colonIndex >= oLen))	// should not happen but check it anyway
			return null;

		// look for first non-whitespace position and assume it is the address value
		int	addrStartIndex=colonIndex+1;
		for ( ; addrStartIndex < oLen; addrStartIndex++)
		{
			final char	ch=orgLine.charAt(addrStartIndex);
			if ((ch != ' ') && (ch != ':'))
				break;
		}

		if (addrStartIndex >= oLen)	// an address argument MUST exist
			return null;

		// check if delimited address
		int	addrEndIndex=(-1);
		if ('<' == orgLine.charAt(addrStartIndex))
		{
			// make sure end delimiter exists
			if (((addrEndIndex=orgLine.indexOf('>', addrStartIndex + 1)) <= addrStartIndex)
			 || (addrEndIndex >= oLen))
				return null;

			// skip the delimiter since address end index must point one place BEYOND the actual end
			addrEndIndex++;
		}
		else	// non-delimited address => stop at first white space, ',' or end of line
		{
			for (addrEndIndex=addrStartIndex+1; addrEndIndex < oLen; addrEndIndex++)
			{
				final char	ch=orgLine.charAt(addrEndIndex);
				if ((' ' == ch) || ('\t' == ch) || (',' == ch))
					break;
			}
		}

		String	repAddr=((null == newAddr) || (newAddr.length() <= 0)) ? "<>" : newAddr;
		if ('<' == repAddr.charAt(0))
		{
			// if already starting with '<' make sure it also ends with '>'
			if (repAddr.charAt(repAddr.length() - 1) != '>')
				return null;
		}
		else	// add missing delimiter
			repAddr = "<" + repAddr + ">";

		// if address was the only argument, then we can optimize the result
		if (addrEndIndex >= oLen)
			return cmdName + " " + repAddr;

		final String	extraArgs=orgLine.substring(addrEndIndex);
		return cmdName + " " + repAddr + extraArgs;
	}
}
