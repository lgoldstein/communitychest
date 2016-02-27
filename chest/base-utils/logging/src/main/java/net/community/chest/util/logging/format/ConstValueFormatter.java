package net.community.chest.util.logging.format;

import net.community.chest.util.logging.LogLevelWrapper;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Returns the same "constant" value as a result of its formatting
 * 
 * @param <V> The type of value being formatted
 * @author Lyor G.
 * @since Jun 27, 2007 2:03:37 PM
 */
public class ConstValueFormatter<V> extends LogMsgComponentFormatter<V> {
	private final V	_value;
	public final /* no cheating */ V getValue ()
	{
		return _value;
	}

	public ConstValueFormatter (final V value)
	{
		super(CONSTVAL);

		_value = value;
	}
	/*
	 * @see net.community.chest.util.logging.format.LogMsgComponentFormatter#formatValue(java.lang.Object)
	 */
	@Override
	public String formatValue (V value)
	{
		return String.valueOf(value);
	}
	/*
	 * @see net.community.chest.util.logging.format.LogMsgComponentFormatter#format(java.lang.Thread, long, java.lang.Class, java.util.logging.Level, java.lang.Object, java.lang.String, java.lang.Throwable)
	 */
	@Override
	public String format (Thread th, long logTime, Class<?> logClass, LogLevelWrapper l, Object ctx, String msg, Throwable t)
	{
		return formatValue(getValue());
	}
}
