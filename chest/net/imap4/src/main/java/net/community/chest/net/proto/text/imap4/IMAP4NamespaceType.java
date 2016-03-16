package net.community.chest.net.proto.text.imap4;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Represents the various types of namespaces</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 12:10:24 PM
 */
public enum IMAP4NamespaceType {
    PERSONAL,
    SHARED,
    OTHER;

    private static IMAP4NamespaceType[]    _values    /* =null */;
    public static synchronized IMAP4NamespaceType[] getValues ()
    {
        if (null == _values)
            _values = values();
        return _values;
    }
}
