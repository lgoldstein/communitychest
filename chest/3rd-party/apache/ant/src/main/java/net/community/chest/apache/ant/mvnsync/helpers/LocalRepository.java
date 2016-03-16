package net.community.chest.apache.ant.mvnsync.helpers;

import java.io.File;
import java.io.Serializable;

import net.community.chest.lang.SysPropsEnum;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 30, 2008 2:29:43 PM
 */
public class LocalRepository implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -1864570913625061065L;
    public LocalRepository ()
    {
        super();
    }

    private String _path    /* =null */;
    public String getPath ()
    {
        return _path;
    }

    public void setPath (String path)
    {
        _path = path;
    }

    private boolean    _defaultLayout=true;
    public boolean isDefaultLayout ()
    {
        return _defaultLayout;
    }

    public static final String    DEFAULT_LAYOUT="default", LEGACY_LAYOUT="legacy";
    public String getLayout ()
    {
        return isDefaultLayout() ? DEFAULT_LAYOUT : LEGACY_LAYOUT;
    }

    public void setLayout (String type)
    {
        if (DEFAULT_LAYOUT.equalsIgnoreCase(type))
            _defaultLayout = true;
        else if (LEGACY_LAYOUT.equalsIgnoreCase(type))
            _defaultLayout = false;
        else
            throw new IllegalArgumentException("setLayout(" + type + ") N/A");
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return getPath();
    }

    public static final String getDefaultLocation ()
    {
        final String    userDir=SysPropsEnum.USERHOME.getPropertyValue();
        return userDir + File.separator + ".m2" + File.separator + "repository";
    }
}
