package net.community.chest.net;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 * 
 * <P>Known protocol(s)</P>
 * @author Lyor G.
 * @since Sep 20, 2007 12:51:56 PM
 */
public enum NetProtocolType {
	SMTP,
	IMAP4,
	POP3,
	FTP,
	HTTP,
	HTTPS,
	SNMP,
	DNS;

	public static final List<NetProtocolType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static NetProtocolType fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
}
