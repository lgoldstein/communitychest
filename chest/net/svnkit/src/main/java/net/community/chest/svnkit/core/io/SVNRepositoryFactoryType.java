/*
 *
 */
package net.community.chest.svnkit.core.io;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.set.SetsUtils;

import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.dav.http.IHTTPConnectionFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 4, 2009 11:48:03 AM
 */
public enum SVNRepositoryFactoryType {
    DAV(DAVRepositoryFactory.class, "http", "https") {
        /*
         * @see net.community.chest.svn.core.io.SVNRepositoryFactoryType#resolveSetupArguments(java.lang.Object[])
         */
        @Override
        protected Class<?>[] resolveSetupArguments (final Object ... args) throws Exception
        {
            if ((args != null) && (args.length == 1))
                return ALTERNATE_DAV_SETUP_PARAMS;

            return super.resolveSetupArguments(args);
        }
    },
    FILE(FSRepositoryFactory.class, "file"),
    SVN(SVNRepositoryFactoryImpl.class, "svn", "svn+ssh");

    protected static final Class<?>[]    ALTERNATE_DAV_SETUP_PARAMS={ IHTTPConnectionFactory.class };

    private final Class<? extends SVNRepositoryFactory>    _fac;
    public final Class<? extends SVNRepositoryFactory> getSVNRepositoryFactoryClass ()
    {
        return _fac;
    }

    private final Collection<String>    _pl;
    public final Collection<String> getProtocols ()
    {
        return _pl;
    }

    protected Class<?>[] resolveSetupArguments (final Object ... args) throws Exception
    {
        if ((args != null) && (args.length > 0))
            throw new IllegalArgumentException("resolveSetupArguments(" + name() + ") unexpected arguments");
        return null;
    }

    public void setup (final Object ... args) throws Exception
    {
        final Class<?>        fc=getSVNRepositoryFactoryClass();
        final Class<?>[]    pa=resolveSetupArguments(args);
        final Method        m=fc.getDeclaredMethod("setup", pa);
        m.invoke(null, args);
    }

    SVNRepositoryFactoryType (final Class<? extends SVNRepositoryFactory> c, final Collection<String> pl)
    {
        _fac = c;
        _pl = pl;
    }

    SVNRepositoryFactoryType (final Class<? extends SVNRepositoryFactory> c, final String ... pl)
    {
        this(c, SetsUtils.setOf(String.CASE_INSENSITIVE_ORDER, pl));
    }

    public static final List<SVNRepositoryFactoryType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final SVNRepositoryFactoryType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final SVNRepositoryFactoryType fromProtocol (final String    proto)
    {
        if ((null == proto) || (proto.length() <= 0))
            return null;

        for (final SVNRepositoryFactoryType v : VALUES)
        {
            final Collection<String>    pl=(null == v) ? null : v.getProtocols();
            if (CollectionsUtils.containsElement(pl, proto, String.CASE_INSENSITIVE_ORDER))
                return v;
        }

        return null;
    }

    public static final SVNRepositoryFactoryType fromSVNURL (final SVNURL url)
    {
        return (null == url) ? null : fromProtocol(url.getProtocol());
    }

    private static final Map<SVNRepositoryFactoryType,Boolean>    _initMap=
        new EnumMap<SVNRepositoryFactoryType,Boolean>(SVNRepositoryFactoryType.class);
    // returns non-null if call actually called the setup method
    public static final SVNRepositoryFactoryType setup (
            final SVNRepositoryFactoryType t, final Object ... args)
        throws Exception
    {
        if (null == t)
            throw new IllegalStateException("setup() no repository factory type specified");

        synchronized(_initMap)
        {
            if (_initMap.containsKey(t))
                return null;

            t.setup(args);
            _initMap.put(t, Boolean.TRUE);
        }

        return t;
    }

    public static final SVNRepositoryFactoryType setup (
                final String proto, final Object ... args)
        throws Exception
    {
        return setup(fromProtocol(proto), args);
    }

    public static final SVNRepositoryFactoryType setup (
                    final SVNURL url, final Object ... args)
        throws Exception
    {
        return setup(fromSVNURL(url), args);
    }
}
