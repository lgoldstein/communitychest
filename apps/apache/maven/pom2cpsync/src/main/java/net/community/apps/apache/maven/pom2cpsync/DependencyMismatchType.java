/**
 * 
 */
package net.community.apps.apache.maven.pom2cpsync;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used to filter the entries shown in the tables</P>
 * @author Lyor G.
 * @since Aug 18, 2008 9:28:16 AM
 */
public enum DependencyMismatchType {
	ALL,		// show all entries
	MATCHING,	// show only matching entries
	MISMATCHED,	// show only entries with mismatched versions
	MISSING;	// show only entries that do not have a counterpart

	public static final List<DependencyMismatchType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final DependencyMismatchType fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
}
