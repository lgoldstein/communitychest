/*
 * 
 */
package net.community.chest.svnkit.core.wc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.lang.EnumUtil;
import net.community.chest.util.collection.CollectionsUtils;

import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Encapsulates the {@link SVNRevision} values as {@link Enum}s</P>
 * 
 * @author Lyor G.
 * @since Aug 5, 2009 9:22:27 AM
 */
public enum SVNRevisionEnum {
	HEAD(SVNRevision.HEAD),
	WORKING(SVNRevision.WORKING),
	PREVIOUS(SVNRevision.PREVIOUS),
	BASE(SVNRevision.BASE),
	COMMITTED(SVNRevision.COMMITTED),
	UNDEFINED(SVNRevision.UNDEFINED);

	private final SVNRevision	_r;
	public final SVNRevision getSVNRevision ()
	{
		return _r;
	}
	
	SVNRevisionEnum (SVNRevision r)
	{
		_r = r;
	}
	/*
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString ()
	{
		return getSVNRevision().toString();
	}

	public static final List<SVNRevisionEnum>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final SVNRevisionEnum fromName (final String name)
	{
		return EnumUtil.fromName(VALUES, name, false);
	}

	public static final SVNRevisionEnum fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final SVNRevisionEnum fromRevision (final SVNRevision r)
	{
		if (null == r)
			return null;

		for (final SVNRevisionEnum v : VALUES)
		{
			final SVNRevision	vt=(null == v) ? null : v.getSVNRevision();
			if (r.equals(vt))
				return v;
		}

		return null;
	}
}
