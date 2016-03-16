package net.community.chest.apache.ant.helpers;

import java.io.Serializable;

import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.lang.PubliclyCloneable;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Serves as a base class for ANT file "executable" elements - project,
 * target, etc.</P>
 *
 * @author Lyor G.
 * @since Jul 29, 2007 12:51:08 PM
 */
public abstract class BaseExecutableElement implements PubliclyCloneable<BaseExecutableElement>, XmlConvertible<BaseExecutableElement>, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -4384370458804494496L;
    protected BaseExecutableElement ()
    {
        super();
    }

    private String    _name    /* =null */;
    /**
     * @return executable element name
     */
    public String getName ()
    {
        return _name;
    }

    public void setName (String name)
    {
        _name = name;
    }

    private String    _description    /* =null */;
    /**
     * @return the element's description - may be null/empty
     */
    public String getDescription ()
    {
        return _description;
    }

    public void setDescription (String description)
    {
        _description = description;
    }

    public boolean isHiddenComponent ()
    {
        final String    desc=getDescription();
        return ((null == desc) || (desc.length() <= 0));
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    public BaseExecutableElement clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
}
