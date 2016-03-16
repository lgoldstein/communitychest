/*
 *
 */
package net.community.chest.resources;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Applies a "chain" of {@link AnchoredResourceAccessor}-s in the same
 * order as they appear in the {@link Collection}</P>
 *
 * @author Lyor G.
 * @since Feb 8, 2009 1:12:34 PM
 */
public class AnchoredResourceAccessorsChain extends LinkedList<AnchoredResourceAccessor> implements AnchoredResourceAccessor {
    /**
     *
     */
    private static final long serialVersionUID = 1368478624987174957L;
    public AnchoredResourceAccessorsChain ()
    {
        super();
    }

    public AnchoredResourceAccessorsChain (Collection<? extends AnchoredResourceAccessor> c)
    {
        super(c);
    }

    public AnchoredResourceAccessorsChain (AnchoredResourceAccessor ... accs)
    {
        this(Arrays.asList(accs));
    }
    /*
     * @see net.community.chest.resources.AnchoredResourceAccessor#getResource(java.lang.String)
     */
    @Override
    public URL getResource (String name)
    {
        if ((null == name) || (name.length() <= 0) || isEmpty())
            return null;

        for (final AnchoredResourceAccessor a : this)
        {
            final URL    url=(null == a) ? null : a.getResource(name);
            if (url != null)
                return url;
        }

        return null;
    }
}
