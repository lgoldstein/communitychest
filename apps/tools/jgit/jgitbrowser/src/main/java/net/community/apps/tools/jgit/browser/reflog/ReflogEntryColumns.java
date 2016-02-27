/*
 * 
 */
package net.community.apps.tools.jgit.browser.reflog;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 20, 2011 11:51:45 AM
 *
 */
public enum ReflogEntryColumns {
	NAME,
	EMAIL,
	TIMESTAMP,
	COMMENT;

	public static final List<ReflogEntryColumns>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final ReflogEntryColumns fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
}
