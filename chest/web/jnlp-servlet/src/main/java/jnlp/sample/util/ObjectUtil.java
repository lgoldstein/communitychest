/*
 * 
 */
package jnlp.sample.util;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 23, 2009 9:15:16 AM
 */
public final class ObjectUtil {
	private ObjectUtil ()
	{
		// no instance
	}

    public static final boolean match (Object o1, Object o2)
    {
    	if (o1 == null)
    		return (o2 == null);
    	else if (o1 == o2)
    		return true;
    	return o1.equals(o2);
    }

    public static final int match (final String s1, final String s2, final boolean caseSensitive)
	{
		if ((null == s1) || (s1.length() <= 0))
		{
			if ((null == s2) || (s2.length() <= 0))
				return 0;
			else	// null/empty comes last
				return (+1);
		}

		// s1 cannot be null/empty here
		if ((null == s2) || (s2.length() <= 0))
			return (-1);	// null/empty comes last

		if (s1 == s2)	// check the obvious
			return 0;

		if (caseSensitive)
			return s1.compareTo(s2);
		else
			return s1.compareToIgnoreCase(s2);
	}

    public static final int objectHashCode (final CharSequence s, final boolean caseSensitive)
	{
		final int	sLen=(null == s) ? 0 : s.length();
		if (sLen <= 0)
			return 0;

		if (caseSensitive)
			return s.hashCode();

		int	h=sLen;
		for (int i = 0; i < sLen; i++)
		{
			final char	oc=s.charAt(i),
						lc=Character.toLowerCase(oc);
		    h = 31 * h + lc;
		}

		return h;
	}

    public static final int objectHashCode (Object o)
    {
    	return (null == o) ? 0 : o.hashCode();
    }

    public static final boolean isFileResource (final URL url)
    {
    	final String	scheme=(null == url) ? null : url.getProtocol();
    	if ("file".equalsIgnoreCase(scheme))
    		return true;
  
    	return false;
    }
    // must ask "isFileResource" first
    public static final File toFile (final URL url) throws IllegalArgumentException
    {
    	try
    	{
    		final URI	uri=(null == url) ? null : url.toURI();
    		return (null == uri) ? null : new File(uri);
    	}
    	catch(URISyntaxException e)
    	{
    		throw new IllegalArgumentException("toFile(" + url + ") " + e.getClass().getName() + ": " + e.getMessage());
    	}
    }

    public static InputStream openResource (final URL url) throws IOException
    {
    	if (null == url)
    		throw new EOFException("openResource() no " + URL.class.getSimpleName() + " provided");

		try
		{
			if (isFileResource(url))
			{
    			final File	f=toFile(url);
    			if (null == f)
    				throw new FileNotFoundException("openResource(" + url + ") no " + File.class.getSimpleName() + " converted");

    			return new FileInputStream(f);
			}

			return url.openStream();
    	}
		catch(RuntimeException e)
		{
			throw new StreamCorruptedException("openResource(" + url + ") " + e.getClass().getName() + ": " + e.getMessage());
		}
    }

    public static final String showEntry (String msg, String value)
    {
    	if (value == null)
    		return "";
    	return msg + value;		
    }

    public static final String showEntry (String msg, String[] value)
    {
    	if ((value == null) || (value.length <= 0))
    		return "";
	
    	return msg + java.util.Arrays.asList(value).toString();	
    }
}
