/*
 *
 */
package net.community.apps.tools.adm.config;

import net.community.chest.lang.EnumUtil;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 15, 2009 10:41:00 AM
 */
public enum ValuesTableColumn {
    NAME,
    VALUE,
    SOURCE;

    private static ValuesTableColumn[]    _values;
    public static final synchronized ValuesTableColumn[] getValues ()
    {
        if (null == _values)
            _values = values();
        return _values;
    }

    public static final ValuesTableColumn fromString (final String s)
    {
        return EnumUtil.fromString(getValues(), s, false);
    }
}
