package net.community.chest.net.auth;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Currently supported authentication mechanisms</P>
 * 
 * @author Lyor G.
 * @since Sep 19, 2007 10:07:15 AM
 */
public enum AuthMechanismEnum {
	PLAIN,
	CRAMMD5,
	DIGESTMD5;

	public static final List<AuthMechanismEnum>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static AuthMechanismEnum fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
}
