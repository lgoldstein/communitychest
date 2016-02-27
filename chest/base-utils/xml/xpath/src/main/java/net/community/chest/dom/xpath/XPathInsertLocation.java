/*
 * 
 */
package net.community.chest.dom.xpath;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Location for inserting nodes (usually element(s))</P>
 * @author Lyor G.
 * @since May 7, 2009 8:19:33 AM
 */
public enum XPathInsertLocation {
	BEFORE,
	AFTER,
	UNDER,
	ABOVE;

	public static final List<XPathInsertLocation>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final XPathInsertLocation fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
}
