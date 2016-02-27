package net.community.chest.util.logging.format;

import net.community.chest.util.logging.LogLevelWrapper;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jun 26, 2007 2:23:30 PM
 */
public class PackageNameFormatter extends LogMsgComponentFormatter<Class<?>> {
	public PackageNameFormatter ()
	{
		super(PACKAGE_NAME);
	}
	/*
	 * @see net.community.chest.util.logging.format.LogMsgComponentFormatter#formatValue(java.lang.Object)
	 */
	@Override
	public String formatValue (Class<?> value)
	{
		final Package	pkg=(null == value) ? null : value.getPackage();
		return (null == pkg) ? null : pkg.getName();
	}
	/*
	 * @see net.community.chest.util.logging.format.LogMsgComponentFormatter#format(java.lang.Thread, long, java.lang.Class, java.util.logging.Level, java.lang.Object, java.lang.String, java.lang.Throwable)
	 */
	@Override
	public String format (Thread th, long logTime, Class<?> logClass, LogLevelWrapper l, Object ctx, String msg, Throwable t)
	{
		return formatValue(logClass);
	}
}
