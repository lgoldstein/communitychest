package net.community.apps.tools.jarscanner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 22, 2007 10:52:06 AM
 */
public enum JarEntriesTableColumns {
	JAR_PATH,
	ENTRY_PATH,
	ENTRY_NAME;

	public static final List<JarEntriesTableColumns>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static JarEntriesTableColumns fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
}
