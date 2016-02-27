/*
 * 
 */
package net.community.chest.ui.components.datetime;

import java.util.Calendar;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @param <C> Type of {@link SpinnerDateControl} being used
 * @author Lyor G.
 * @since May 11, 2010 3:40:29 PM
 */
public abstract class OptionalDateControl<C extends SpinnerDateControl> extends AbstractOptionalSpinnerDateTimeControl<C> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2993033598197544059L;

	public OptionalDateControl (Class<C> valsClass)
	{
		super(valsClass);
	}

	public Calendar getValue (boolean asEndOfDay)
	{
		final C	c=getSpinnerControl();
		if (null == c)
			return null;
		
		return c.getValue(asEndOfDay);
	}
	/*
	 * @see net.community.chest.ui.components.datetime.AbstractOptionalSpinnerDateTimeControl#getValue()
	 */
	@Override
	public Calendar getValue ()
	{
		return getValue(false);
	}

	public static class DefaultOptionalDateControl extends OptionalDateControl<SpinnerDateControl> {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1696295817629730064L;
		public DefaultOptionalDateControl ()
		{
			super(SpinnerDateControl.class);
		}
		/*
		 * @see net.community.chest.ui.components.datetime.AbstractOptionalSpinnerDateTimeControl#createSpinnerControl()
		 */
		@Override
		protected SpinnerDateControl createSpinnerControl ()
		{
			return new SpinnerDateControl();
		}
	}
}
