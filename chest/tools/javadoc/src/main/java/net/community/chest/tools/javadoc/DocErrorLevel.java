package net.community.chest.tools.javadoc;


/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 16, 2007 11:09:04 AM
 */
public enum DocErrorLevel  {
    NOTICE("NOTICE"),
    WARNING("WARNING"),
    ERROR("ERROR");

    private final String    _name;
    public final String getName ()
    {
        return _name;
    }

    DocErrorLevel (String name)
    {
        _name = name;
    }
}
