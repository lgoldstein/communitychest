package net.community.chest.tools.javadoc.mbean;

import java.io.Serializable;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>MBean attribute descriptor</P>
 * @author Lyor G.
 * @since Aug 16, 2007 11:26:26 AM
 */
public class AttrDescriptor implements Serializable, Cloneable {
    /**
     *
     */
    private static final long serialVersionUID = 7022722839550192582L;
    /**
     * Empty constructor
     */
    public AttrDescriptor ()
    {
        super();
    }
    /**
     * Attribute "pure" name - without "set/get/is"
     */
    private    String    _name    /* =null */;
    public String getName ()
    {
        return _name;
    }

    public void setName (String name)
    {
        _name = name;
    }
    /**
     * "is/set/get"
     */
    private String    _prefix    /* =null */;
    public String getPrefix ()
    {
        return _prefix;
    }

    public void setPrefix (String prefix)
    {
        _prefix = prefix;
    }
    /**
     * TRUE if this is a getter
     */
    private boolean    _getter    /* =false */;
    public boolean isGetter ()
    {
        return _getter;
    }

    public void setGetter (boolean getter)
    {
        _getter = getter;
    }
    /**
     * Resets to null/empty value(s)
     */
    public void reset ()
    {
        setPrefix(null);
        setName(null);
        setGetter(false);
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone () throws CloneNotSupportedException
    {
        return super.clone();
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if ((null == obj) || (!(obj instanceof AttrDescriptor)))
            return false;
        if (this == obj)
            return true;

        final AttrDescriptor    od=(AttrDescriptor) obj;
        if (isGetter() != od.isGetter())
            return false;

        // check prefix
        {
            final String    tp=getPrefix(), op=od.getPrefix();
            if ((null == tp) || (tp.length() <= 0))
            {
                if ((op != null) && (op.length() > 0))
                    return false;
            }
            else
            {
                if (!tp.equals(op))
                    return false;
            }
        }

        // check name
        {
            final String    tn=getName(), on=od.getName();
            if ((null == tn) || (tn.length() <= 0))
            {
                if ((on != null) && (on.length() > 0))
                    return false;
            }
            else
            {
                if (!tn.equals(on))
                    return false;
            }
        }

        return true;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        final String    p=getPrefix(), n=getName();
        return ((null == p) ? 0 : p.hashCode())
             + ((null == n) ? 0 : n.hashCode())
            ;
    }
    /*
     * Full method name
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return getPrefix() + getName();
    }
}
