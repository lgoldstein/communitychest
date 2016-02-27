package net.community.chest.awt;

import java.awt.Color;
import java.awt.SystemColor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulate {@link SystemColor}-s into {@link Enum}-s</P>
 * 
 * @author Lyor G.
 * @since Mar 20, 2008 3:44:22 PM
 */
public enum SystemColors {
	DESKTOP(SystemColor.desktop, SystemColor.DESKTOP),

	ACTIVE_CAPTION(SystemColor.activeCaption, SystemColor.ACTIVE_CAPTION),
	ACTIVE_CAPTION_TEXT(SystemColor.activeCaptionText, SystemColor.ACTIVE_CAPTION_TEXT),
	ACTIVE_CAPTION_BORDER(SystemColor.activeCaptionBorder, SystemColor.ACTIVE_CAPTION_BORDER),

	INACTIVE_CAPTION(SystemColor.inactiveCaption, SystemColor.INACTIVE_CAPTION),
	INACTIVE_CAPTION_TEXT(SystemColor.inactiveCaptionText, SystemColor.INACTIVE_CAPTION_TEXT),
	INACTIVE_CAPTION_BORDER(SystemColor.inactiveCaptionBorder, SystemColor.INACTIVE_CAPTION_BORDER),

	WINDOW(SystemColor.window, SystemColor.WINDOW),
	WINDOW_BORDER(SystemColor.windowBorder, SystemColor.WINDOW_BORDER),
	WINDOW_TEXT(SystemColor.windowText, SystemColor.WINDOW_TEXT),
	
	MENU(SystemColor.menu, SystemColor.MENU),
	MENU_TEXT(SystemColor.menuText, SystemColor.MENU_TEXT),
	
	TEXT(SystemColor.text, SystemColor.TEXT),
	TEXT_TEXT(SystemColor.textText, SystemColor.TEXT_TEXT),
	TEXT_HIGHLIGHT(SystemColor.textHighlight, SystemColor.TEXT_HIGHLIGHT),
	TEXT_HIGHLIGHT_TEXT(SystemColor.textHighlightText, SystemColor.TEXT_HIGHLIGHT_TEXT),
	TEXT_INACTIVE_TEXT(SystemColor.textInactiveText, SystemColor.TEXT_INACTIVE_TEXT),
	
	CONTROL(SystemColor.control, SystemColor.CONTROL),
	CONTROL_TEXT(SystemColor.controlText, SystemColor.CONTROL_TEXT),
	CONTROL_HIGHLIGHT(SystemColor.controlHighlight, SystemColor.CONTROL_HIGHLIGHT),
	CONTROL_LT_HIGHLIGHT(SystemColor.controlLtHighlight, SystemColor.CONTROL_LT_HIGHLIGHT),
	CONTROL_SHADOW(SystemColor.controlShadow, SystemColor.CONTROL_SHADOW),
	CONTROL_DK_SHADOW(SystemColor.controlDkShadow, SystemColor.CONTROL_DK_SHADOW),
	
	SCROLLBAR(SystemColor.scrollbar, SystemColor.SCROLLBAR),

	INFO(SystemColor.info, SystemColor.INFO),
	INFO_TEXT(SystemColor.infoText, SystemColor.INFO_TEXT);

	private final int	_x;
	public final int getColorIndex ()
	{
		return _x;
	}

	private final SystemColor	_color;
	public final SystemColor getSystemColor ()
	{
		return _color;
	}

	SystemColors (SystemColor c, int x)
	{
		_color = c;
		_x = x;
	}

	public static final List<SystemColors>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final SystemColors fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final SystemColors fromColorIndex (final int i)
	{
    	for (final SystemColors v : VALUES)
    	{
    		if ((v != null) && (v.getColorIndex() == i))
    			return v;
    	}

    	return null;
	}

	public static final SystemColors fromColor (final Color c)
	{
		if (null == c)
			return null;

		for (final SystemColors v : VALUES)
		{
			if (c.equals(v.getSystemColor()))
				return v;
		}

		return null;	// no match
	}

	public static SystemColors fromRGB (final int rgb)
	{
		for (final SystemColors v : VALUES)
		{
			final SystemColor	c=v.getSystemColor();
			if (c.getRGB() == rgb)
				return v;
		}

		return null;	// no match
	}
}
