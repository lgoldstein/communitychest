package net.community.chest.io;

import java.io.File;
import java.lang.reflect.Constructor;

import net.community.chest.reflect.ValueStringConstructor;
import net.community.chest.resources.SystemPropertiesResolver;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> The generic {@link File} type 
 * @author Lyor G.
 * @since Mar 6, 2008 7:50:27 AM
 */
public class FileStringInstantiator<F extends File> extends ValueStringConstructor<F> {
	public FileStringInstantiator (Class<F> valsClass) throws IllegalArgumentException
	{
		super(valsClass);
	}

	public FileStringInstantiator (Constructor<F> ctor) throws Exception
	{
		super(ctor);
	}
	/*
	 * @see net.community.chest.reflect.ValueStringConstructor#newInstance(java.lang.String)
	 */
	@Override
	public F newInstance (final String s) throws Exception
	{
		final String v=SystemPropertiesResolver.SYSTEM.format(s);
		if ((v != null) && (v.length() > 0))
			return super.newInstance(v);
	
		return null;
	}

	public static final FileStringInstantiator<File>	DEFAULT=new FileStringInstantiator<File>(File.class);
}
