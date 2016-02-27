/*
 * 
 */
package net.community.chest.jfree.jcommon.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.ui.TextAnchor;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 26, 2009 1:29:21 PM
 */
public enum TextAnchorEnum {
   	TOPLEFT(TextAnchor.TOP_LEFT), 
   	TOPCENTER(TextAnchor.TOP_CENTER), 
   	TOPRIGHT(TextAnchor.TOP_RIGHT),
   	HASCENTLEFT(TextAnchor.HALF_ASCENT_LEFT),
   	HASCENTCENTER(TextAnchor.HALF_ASCENT_CENTER),
   	HASCENTRIGHT(TextAnchor.HALF_ASCENT_RIGHT),
   	CENTERLEFT(TextAnchor.CENTER_LEFT),
   	CENTER(TextAnchor.CENTER),
   	CENTERRIGHT(TextAnchor.CENTER_RIGHT),
   	BASELEFT(TextAnchor.BASELINE_LEFT),
   	BASECENTER(TextAnchor.BASELINE_CENTER),
   	BASERIGHT(TextAnchor.BASELINE_RIGHT),
   	BOTLEFT(TextAnchor.BOTTOM_LEFT),
   	BOTCENTER(TextAnchor.BOTTOM_CENTER),
   	BOTRIGHT(TextAnchor.BOTTOM_RIGHT);

   	private final TextAnchor	_a;
	public final TextAnchor getAnchor ()
	{
		return _a;
	}
	
	TextAnchorEnum (TextAnchor a)
	{
		_a = a;
	}

	public static final List<TextAnchorEnum>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final TextAnchorEnum fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final TextAnchorEnum fromAnchor (final TextAnchor a)
	{
		if (null == a)
			return null;

		for (final TextAnchorEnum  v : VALUES)
		{
			if ((v != null) && a.equals(v.getAnchor()))
				return v;
		}

		return null;
	}
}
