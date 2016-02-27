package net.community.chest.mail.address;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.mail.headers.RFCHeaderDefinitions;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Provides various address type(s) identification</P>
 * @author Lyor G.
 * @since Sep 12, 2007 11:07:07 AM
 */
public enum MessageAddressType {
	ORIGINATOR,	// usually META address not necessarily in message - e.g., SMTP MAIL FROM
	FROM, SENDER,	// usually the From/Sender header
	RECIPIENT,	// usually META address not necessarily in message - e.g., SMTP RCPT TO
	TO, CC, BCC,	// usually the To/Cc header (Bcc is a role rather than a header)
	REPLIED,	// usually META address
	REPLY_TO, REPLY_CC, REPLY_BCC;	// the equivalent Reply-NN header(s)

	public static final List<MessageAddressType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final MessageAddressType fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final boolean isMetaAddress (final MessageAddressType aType)
	{
		if (null == aType)
			return false;	// just so we have a debug breakpoint

		return ORIGINATOR.equals(aType)
			|| RECIPIENT.equals(aType)
			|| REPLIED.equals(aType)
			;
	}
	/**
	 * @param aType Address type for which the default header to be used is
	 * requested.
	 * @return requested header - null if original null parameter or a META
	 * address type. <B>Note:</B> {@link #BCC} and {@link #REPLY_BCC} have
	 * not matching headers
	 */
	public static final String getAddressHeader (final MessageAddressType aType)
	{
		if ((null == aType) || isMetaAddress(aType))
			return null;

		switch(aType)
		{
			case FROM		: return RFCHeaderDefinitions.stdFromHdr;
			case SENDER		: return RFCHeaderDefinitions.stdSenderHdr;
			case TO			: return RFCHeaderDefinitions.stdToHdr;
			case CC			: return RFCHeaderDefinitions.stdCcHdr;
			case REPLY_TO	: return RFCHeaderDefinitions.stdReplyToHdr;
			case REPLY_CC	: return RFCHeaderDefinitions.stdReplyCcHdr;
			default			: return null;	// e.g., BCC
		}
	}
	/**
	 * @param s Mail address header
	 * @return matching {@link MessageAddressType} - null if no match found
	 * (or original null/empty header)
	 */
	public static final MessageAddressType fromAddressHeader (final String s)
	{
		if ((null == s) || (s.length() <= 0))
			return null;

		for (final MessageAddressType v : VALUES)
		{
			final String	vHdr=getAddressHeader(v);
			if (s.equalsIgnoreCase(vHdr))
				return v;
		}

		return null;	// no match found
	}

	public static final boolean isSourceAddress (final MessageAddressType aType)
	{
		if (null == aType)
			return false;	// just so we have a debug breakpoint

		return ORIGINATOR.equals(aType)
			|| FROM.equals(aType)
			|| SENDER.equals(aType)
			;
	}

	public static final boolean isRecipientTarget (final MessageAddressType aType)
	{
		if (null == aType)
			return false;	// just so we have a debug breakpoint

		return RECIPIENT.equals(aType)
			|| TO.equals(aType)
			|| CC.equals(aType)
			|| BCC.equals(aType)
			;
	}

	public static final boolean isReplyTarget  (final MessageAddressType aType)
	{
		if (null == aType)
			return false;	// just so we have a debug breakpoint

		return REPLIED.equals(aType)
			|| REPLY_TO.equals(aType)
			|| REPLY_CC.equals(aType)
			|| REPLY_BCC.equals(aType)
			;
	}
}
