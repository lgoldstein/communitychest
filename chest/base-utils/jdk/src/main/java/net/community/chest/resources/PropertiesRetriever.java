package net.community.chest.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Reads a {@link Properties} from file
 * @author Lyor G.
 * @since Jul 19, 2007 12:28:01 PM
 */
public class PropertiesRetriever extends ResourceDataRetriever<Properties> {
	/*
	 * @see net.community.chest.resources.ResourceDataRetriever#loadResourceData(java.io.InputStream)
	 */
	@Override
	public Properties loadResourceData (final InputStream in) throws IOException
	{
		final URL		resURL=getResourceURL();
		final String	resPath=(null == resURL) ? null : resURL.getPath();
		final int		nLen=(null == resPath) ? 0 : resPath.length();
		if (nLen <= 0)	// should not happen
			throw new StreamCorruptedException(getArgumentsExceptionLocation("loadResourceData", resPath) + " no file name retrieved from URL");

		final int		sfxPos=resPath.lastIndexOf('.');
		final boolean	loadXml;
		if ((sfxPos > 0) && (sfxPos < nLen))
		{
			final String	sfxValue=resPath.substring(sfxPos);
			loadXml = ".xml".equalsIgnoreCase(sfxValue);
		}
		else
			loadXml = false;

		final Properties	props=new Properties();
		if (loadXml)
			props.loadFromXML(in);
		else
			props.load(in);

		return props;
	}

	public PropertiesRetriever (final Class<?> anchor, final Locale lcl, final String name) throws IOException
	{
		super(Properties.class, anchor, lcl, name);
	}

	public PropertiesRetriever (final Class<?> anchor, final String name) throws IOException
	{
		super(Properties.class, anchor, name);
	}
}
