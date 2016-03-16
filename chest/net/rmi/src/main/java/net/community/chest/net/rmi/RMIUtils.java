/*
 *
 */
package net.community.chest.net.rmi;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 31, 2008 2:41:58 PM
 */
public final class RMIUtils {
    private RMIUtils ()
    {
        // no instance
    }

    public static final char    REG_KEY_SEPCHAR='@';
    public static final String getRegistryKey (final String host, final int port)
    {
        return host + String.valueOf(REG_KEY_SEPCHAR) + port;
    }

    public static final Map.Entry<String,Integer> fromRegistryKey (final String rk)
    {
        final int        rkLen=(null == rk) ? 0 : rk.length(),
                        sPos=(rkLen <= 1) ? (-1) : rk.lastIndexOf('@');
        final Integer    port=
            ((sPos >= 0) && (sPos < (rkLen-1)))? Integer.valueOf(rk.substring(sPos + 1)) : null;
        final String    host=(sPos > 0) ? rk.substring(0, sPos) : null;

        if ((null == port) && ((null == host) || (host.length() <= 0)))
            return null;

        return new MapEntryImpl<String,Integer>(host, port);
    }

    public static final String getRegistryKey (final String host)
    {
        return getRegistryKey(host, Registry.REGISTRY_PORT);
    }

    public static final String getRegistryKey (final int port)
    {
        return getRegistryKey(null, port);
    }

    private static Map<String,Registry>    _regsMap    /* =null */;
    public static final Registry getRegistry (final String host, final int port) throws RemoteException
    {
        synchronized(RMIUtils.class)
        {
            if (null == _regsMap)
                _regsMap = new TreeMap<String,Registry>();
        }

        final String    regKey=getRegistryKey(host, port);
        Registry        r=null;
        synchronized(_regsMap)
        {
            if ((r=_regsMap.get(regKey)) != null)
                return r;
        }

        if (null == (r=LocateRegistry.getRegistry(host, port)))
            throw new RemoteException("getRegistry(" + regKey + ") no " + Registry.class.getSimpleName() + " instance created");

        synchronized(_regsMap)
        {
            final Registry    prev=_regsMap.put(regKey, r);
            if (prev != null)
                return r;
        }

        return r;
    }

    public static final Registry getRegistry (final String host) throws RemoteException
    {
        return getRegistry(host, Registry.REGISTRY_PORT);
    }

    public static final Registry getRegistry (final int port) throws RemoteException
    {
        return getRegistry(null, port);
    }

    public static final Registry getRegistry () throws RemoteException
    {
        return getRegistry(Registry.REGISTRY_PORT);
    }

    public static final <R extends Remote> R lookup (Registry r, Class<R> rc, String name)
        throws RemoteException, NotBoundException
    {
        final Object    o=(null == r) ? null : r.lookup(name);
        if (null == o)
            return null;

        return rc.cast(o);
    }

    public static final <R extends Remote> R lookup (String host, int port, Class<R> rc, String name)
        throws RemoteException, NotBoundException
    {
        return lookup(getRegistry(host, port), rc, name);
    }

    public static final <R extends Remote> R lookup (String host, Class<R> rc, String name)
        throws RemoteException, NotBoundException
    {
        return lookup(host, Registry.REGISTRY_PORT, rc, name);
    }

    public static final <R extends Remote> R lookup (int port, Class<R> rc, String name)
        throws RemoteException, NotBoundException
    {
        return lookup(null, port, rc, name);
    }

    public static final Remote ensureBinding (final Registry r, final int port, final String name, final Remote stub)
        throws RemoteException
    {
        if ((null == r) || (null == name) || (name.length() <= 0) || (null == stub))
            throw new AccessException("ensureBinding(" + r + ")[" + name + "] no stub/name/registry");

        Remote    prev=null;
        try
        {
            prev = r.lookup(name);
        }
        catch(NotBoundException e)
        {
            // ignored
        }

        final Remote    ret=UnicastRemoteObject.exportObject(stub, port);
        if (prev == null)
        {
            try
            {
                r.bind(name, ret);
            }
            catch(AlreadyBoundException e)
            {
                // should not happen
                throw new AccessException("rebind(" + r + ")[" + name + "] " + e.getClass().getName() + ": " + e.getMessage(), e);
            }
        }
        else
        {
            r.rebind(name, ret);
        }

        return ret;
    }

    public static final Remote ensureBinding (
            final String host, final int port, final String name, final Remote stub)
        throws RemoteException
    {
        if ((null == name) || (name.length() <= 0) || (null == stub))
            throw new AccessException("rebind(" + getRegistryKey(host, port) + ")[" + name + "] no stub/name/registry");

        return ensureBinding(getRegistry(host, port), port, name, stub);
    }

    public static final Remote ensureBinding (
            final String host, final String name, final Remote stub)
        throws RemoteException
    {
        return ensureBinding(host, Registry.REGISTRY_PORT, name, stub);
    }

    public static final Remote ensureBinding (
            final int port, final String name, final Remote stub)
        throws RemoteException
    {
        return ensureBinding((String) null, port, name, stub);
    }
}
