package net.community.chest.spring.test.beans;

/**
 * @author Lyor G.
 * @since Jul 21, 2010 2:19:06 PM
 */
public enum DateTimeEntitySortOrder {
    ID("id", 'i'),
    NAME("name", 'n'),
    DATEVALUE("dateValue", 'v');

    private final String    _attrName;
    public final String getAttributeName ()
    {
        return _attrName;
    }

    private final char    _opChar;
    public final char getOpChar ()
    {
        return _opChar;
    }

    DateTimeEntitySortOrder (String aName, char opChar)
    {
        _attrName = aName;
        _opChar = opChar;
    }

    private static DateTimeEntitySortOrder[]    _values;
    public static final synchronized DateTimeEntitySortOrder[] getValues ()
    {
        if (null == _values)
            _values = values();
        return _values;
    }

    public static final DateTimeEntitySortOrder fromOpChar (final char oc)
    {
        final DateTimeEntitySortOrder[]    vals=getValues();
        if ((null == vals) || (vals.length <= 0))
            return null;    // should not happen

        for (final DateTimeEntitySortOrder v : vals)
        {
            if ((v != null) && (v.getOpChar() == oc))
                return v;
        }

        return null;
    }
}
