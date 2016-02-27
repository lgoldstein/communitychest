/*
 * 
 */
package net.community.chest.io.encode.base64;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 16, 2009 1:21:11 PM
 */
public enum Base64EncodeOptions {
	/**
	 * Whether to insert line breaks every 76 characters in the output.
	 */
	BREAK,
	/**
	 * Whether to use CRLF instead of just LF
	 */
	CRLF;

	private static final List<Base64EncodeOptions>	VALUES=
		Collections.unmodifiableList(Arrays.asList(values()));

	public static final Base64EncodeOptions fromString (final String name)
	{
		return CollectionsUtils.fromString(VALUES, name, false);
	}
}
