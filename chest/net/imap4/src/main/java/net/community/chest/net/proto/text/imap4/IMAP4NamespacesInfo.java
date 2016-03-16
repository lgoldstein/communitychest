package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import net.community.chest.net.TextNetConnection;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Class used to hold NAMESPACE command result</P>
 * @author Lyor G.
 * @since Sep 20, 2007 12:11:47 PM
 */
public class IMAP4NamespacesInfo extends IMAP4TaggedResponse {
    /**
     *
     */
    private static final long serialVersionUID = 7569912179185594737L;
    private Map<IMAP4NamespaceType,IMAP4Namespace>    _nsMap    /* =null */;
    public Map<IMAP4NamespaceType,IMAP4Namespace> getNamespaces ()
    {
        return _nsMap;
    }

    public void setNamespaces (Map<IMAP4NamespaceType,IMAP4Namespace> ns)
    {
        _nsMap = ns;
    }

    protected IMAP4Namespace createNamespace (final IMAP4NamespaceType type)
    {
        return (null == type) ? null : new IMAP4Namespace(type);
    }

    public IMAP4Namespace getNamespace (final IMAP4NamespaceType type, final boolean createIfNotExist)
    {
        if (null == type)
            return null;

         final Map<IMAP4NamespaceType,IMAP4Namespace>    nsMap=getNamespaces();
         IMAP4Namespace                                    nsInfo=
             ((null == nsMap) || (nsMap.size() <= 0)) ? null : nsMap.get(type);
         if (nsInfo != null)
             return nsInfo;

         if (!createIfNotExist)
             return null;

         if (null == (nsInfo=createNamespace(type)))
             return null;

         setNamespace(type, nsInfo);
         return nsInfo;
    }
    /**
     * @return personal namespace information
     */
    public IMAP4Namespace getPersonal ()
    {
        return getNamespace(IMAP4NamespaceType.PERSONAL, false);
    }
    /**
     * @return shared namespace information
     */
    public IMAP4Namespace getShared ()
    {
        return getNamespace(IMAP4NamespaceType.SHARED, false);
    }
    /**
     * @return other namespace information
     */
    public IMAP4Namespace getOther ()
    {
        return getNamespace(IMAP4NamespaceType.OTHER, false);
    }
    // NOTE: does not check that namespace type matches reported one
    public IMAP4Namespace setNamespace (IMAP4NamespaceType type, IMAP4Namespace ns /* null == delete from map */)
    {
        if (null == type)
            return null;

        Map<IMAP4NamespaceType,IMAP4Namespace>    nsMap=getNamespaces();
        if (null == nsMap)
        {
            // OK if asked to delete it and no map available anyway
            if (null == ns)
                return null;

            setNamespaces(new EnumMap<IMAP4NamespaceType, IMAP4Namespace>(IMAP4NamespaceType.class));
            if (null == (nsMap=getNamespaces()))    // should not happen
                throw new IllegalStateException(ClassUtil.getArgumentsExceptionLocation(getClass(), "setNamespace", type, ns) + " no " + Map.class.getName() + " instance though created");
        }

        if (null == ns)
            return nsMap.remove(type);
        else
            return nsMap.put(type, ns);
    }

    public void setPersonal (IMAP4Namespace ns)
    {
        setNamespace(IMAP4NamespaceType.PERSONAL, ns);
    }

    public void setShared (IMAP4Namespace ns)
    {
        setNamespace(IMAP4NamespaceType.SHARED, ns);
    }

    public void setOther (IMAP4Namespace ns)
    {
        setNamespace(IMAP4NamespaceType.OTHER, ns);
    }

    public IMAP4NamespacesInfo ()
    {
        super();
    }

    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        final Class<?>    oc=(obj == null) ? null : obj.getClass();
        if (oc != getClass())
            return false;

        final IMAP4NamespacesInfo    nsInfo=(IMAP4NamespacesInfo) obj;
        if (!isSameResponse(nsInfo))
            return false;

        final Map<IMAP4NamespaceType,IMAP4Namespace>    m1=getNamespaces(), m2=nsInfo.getNamespaces();
        if ((null == m1) || (m1.size() <= 0))
            return ((null == m2) || (m2.size() <= 0));
        else if ((null == m2) || (m2.size() <= 0))
            return false;
        else if (m1.size() != m2.size())
            return false;

        final IMAP4NamespaceType[]    types=IMAP4NamespaceType.getValues();
        if ((null == types) || (types.length <= 0))
            return true;    // should not happen

        for (final IMAP4NamespaceType t : types)
        {
            final IMAP4Namespace    n1=m1.get(t), n2=m2.get(t);
            if (null == n1)
            {
                if (n2 != null)
                    return false;
            }
            else if (!n1.equals(n2))
                return false;
        }

        return true;
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#hashCode()
     */
    @Override
    public int hashCode ()
    {
        int    nRes=super.hashCode();

        final Map<IMAP4NamespaceType,IMAP4Namespace>    nsMap=getNamespaces();
        final Collection<IMAP4Namespace>                ns=
                ((null == nsMap) || (nsMap.size() <= 0)) ? null : nsMap.values();
        if ((ns != null) && (ns.size() > 0))
        {
            for (final IMAP4Namespace n : ns)
            {
                if (n != null)    // should not be otherwise
                    nRes += n.hashCode();
            }
        }

        return nRes;
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#reset()
     */
    @Override
    public void reset ()
    {
        super.reset();

        final Map<IMAP4NamespaceType,IMAP4Namespace>    nsMap=getNamespaces();
        if (nsMap != null)
            nsMap.clear();
    }
    /**
     * Parses a NAMESPACE command response until final tag value received
     * @param conn connection through which response is expected
     * @param tagValue initial tag value used for the command (must be >0)
     * @return NAMESPACE response
     * @throws IOException unable to extract full response
     */
    public static final IMAP4NamespacesInfo getFinalResponse (final TextNetConnection conn, final int tagValue) throws IOException
    {
        return (IMAP4NamespacesInfo) (new IMAP4NamespacesInfoRspHandler(conn)).handleResponse(tagValue);
    }
}
