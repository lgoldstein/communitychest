package net.community.chest.mail;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 22, 2007 12:51:38 PM
 */
public enum MimeEncodingTypeEnum {
    UNKNOWN("unknown"),
    BASE64("base64"),
    QUOTEDPRINTABLE("quoted-printable"),
    BINARY7BIT("7bit"),
    BINARY8BIT("8bit"),
    BINARYPURE("binary");

    private final String    _xferEncoding;
    public final String getXferEncoding ()
    {
        return _xferEncoding;
    }

    MimeEncodingTypeEnum (String xferEncoding)
    {
        _xferEncoding = xferEncoding;
    }

    private static MimeEncodingTypeEnum[]    _values    /* =null */;
    public static final synchronized MimeEncodingTypeEnum[] getValues ()
    {
        if (null == _values)
            _values = values();
        return _values;
    }

    public static MimeEncodingTypeEnum fromXferEncoding (final String s)
    {
        if ((null == s) || (s.length() <= 0))
            return null;

        final MimeEncodingTypeEnum[]    vals=getValues();
        if ((null == vals) || (vals.length <= 0))
            return null;    // should not happen

        for (final MimeEncodingTypeEnum v : vals)
        {
            if ((v != null) && s.equalsIgnoreCase(v.getXferEncoding()))
                return v;
        }

        // no match found
        return null;
    }
}
