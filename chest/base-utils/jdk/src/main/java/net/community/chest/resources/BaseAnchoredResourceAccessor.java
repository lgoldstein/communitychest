/*
 *
 */
package net.community.chest.resources;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Default implementation of {@link AnchoredResourceAccessor}</p>
 * @author Lyor G.
 * @since Aug 21, 2008 12:31:57 PM
 */
public class BaseAnchoredResourceAccessor implements AnchoredResourceAccessor {
    public BaseAnchoredResourceAccessor ()
    {
        super();
    }
    /*
     * @see net.community.chest.resources.AnchoredResourceAccessor#getResource(java.lang.String)
     */
    @Override
    public URL getResource (final String name)
    {
        final int    nLen=(null == name) ? 0 : name.length();
        if (nLen <= 0)
            return null;

        // check if already in URL form
        final int    sPos=name.indexOf(':');
        if ((sPos > 0) && (sPos < (nLen-2)))
        {
            if ('/' == name.charAt(sPos + 1))
            {
                try
                {
                    return new URL(name);
                }
                catch(MalformedURLException e)
                {
                    throw new IllegalArgumentException("getResource(" + name + ") " + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        }

        final Class<?>    c=getClass();
        return (null == c) ? null : c.getResource(name);
    }
}
