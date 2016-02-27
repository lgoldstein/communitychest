package net.community.chest.util.logging;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Defines some useful log levels</P>
 * 
 * @author Lyor G.
 * @since Oct 1, 2007 11:29:17 AM
 */
public enum LogLevelWrapper {
	FATAL,
	ERROR,
	WARNING,
	INFO,
	DEBUG,
	VERBOSE,
	TRACE;

	public static final List<LogLevelWrapper>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static LogLevelWrapper fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
}
