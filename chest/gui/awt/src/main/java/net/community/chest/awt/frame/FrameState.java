/*
 * 
 */
package net.community.chest.awt.frame;

import java.awt.Frame;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulates the values for the {@link Frame#setState(int)} and
 * {@link Frame#setExtendedState(int)} calls</P>
 * 
 * @author Lyor G.
 * @since Dec 30, 2008 3:51:49 PM
 */
public enum FrameState {
	NORMAL(Frame.NORMAL),
	ICONIFIED(Frame.ICONIFIED),
	MAXHORIZ(Frame.MAXIMIZED_HORIZ),
	MAXVERT(Frame.MAXIMIZED_VERT),
	MAXBOTH(Frame.MAXIMIZED_BOTH);

	private final int	_state;
	public final int getState ()
	{
		return _state;
	}

	FrameState (int s)
	{
		_state = s;
	}

	public static final List<FrameState>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final FrameState fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final FrameState fromState (final int opt)
	{
		for (final FrameState o : VALUES)
		{
			if ((o != null) && (o.getState() == opt))
				return o;
		}

		return null;
	}
}
