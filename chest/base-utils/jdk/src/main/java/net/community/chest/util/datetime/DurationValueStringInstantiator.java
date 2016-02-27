/*
 * 
 */
package net.community.chest.util.datetime;

import java.lang.reflect.Constructor;

import net.community.chest.reflect.ValueStringConstructor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 16, 2008 5:05:14 PM
 */
public class DurationValueStringInstantiator extends ValueStringConstructor<Duration> {
	public DurationValueStringInstantiator () throws IllegalArgumentException
	{
		super(Duration.class);
	}

	/*
	 * @see net.community.chest.reflect.ValueStringConstructor#getConstructor(java.lang.Class)
	 */
	@Override
	protected Constructor<Duration> getConstructor (Class<Duration> valsClass) throws Exception
	{
		return valsClass.getConstructor(CharSequence.class);
	}

	public static final DurationValueStringInstantiator	DEFAULT=new DurationValueStringInstantiator();
}
