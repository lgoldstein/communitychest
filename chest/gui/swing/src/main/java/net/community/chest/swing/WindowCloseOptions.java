package net.community.chest.swing;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.WindowConstants;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulates the window closure options into an {@link Enum}</P>
 * @author Lyor G.
 * @since Mar 20, 2008 10:20:47 AM
 */
public enum WindowCloseOptions implements WindowConstants {
	// NOTE !!! ORDINAL IS SAME AS OPTION
	NOTHING(DO_NOTHING_ON_CLOSE),
	HIDE(HIDE_ON_CLOSE),
	DISPOSE(DISPOSE_ON_CLOSE),
	EXIT(EXIT_ON_CLOSE);

	private final int	_optVal;
	public final int getCloseOption ()
	{
		return _optVal;
	}

	WindowCloseOptions (int optVal)
	{
		_optVal = optVal;
	}

	public static final List<WindowCloseOptions>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final WindowCloseOptions fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final WindowCloseOptions fromCloseOption (final int opt)
	{
		for (final WindowCloseOptions o : VALUES)
		{
			if ((o != null) && (o.getCloseOption() == opt))
				return o;
		}

		return null;
	}
}
