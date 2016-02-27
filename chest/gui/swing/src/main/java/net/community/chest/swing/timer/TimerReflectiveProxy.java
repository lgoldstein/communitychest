/*
 * 
 */
package net.community.chest.swing.timer;

import javax.swing.Timer;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <T> The type of {@link Timer} being reflected
 * @author Lyor G.
 * @since May 4, 2009 11:48:11 AM
 */
public class TimerReflectiveProxy<T extends Timer> extends UIReflectiveAttributesProxy<T> {
	protected TimerReflectiveProxy (Class<T> objClass, boolean registerAsDefault)
			throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public TimerReflectiveProxy (Class<T> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}
	// NOTE !!! throws exception if used to create a timer
	public static final TimerReflectiveProxy<Timer>	TIMER=
			new TimerReflectiveProxy<Timer>(Timer.class, true);
}
