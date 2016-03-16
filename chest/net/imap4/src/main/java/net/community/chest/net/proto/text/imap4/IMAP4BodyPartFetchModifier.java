package net.community.chest.net.proto.text.imap4;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 24, 2008 2:38:33 PM
 */
public class IMAP4BodyPartFetchModifier extends IMAP4BodyFetchModifier {
    /* standard part (sub-)paths */
    public static final String IMAP4BodyMIME="MIME";
    public static final String IMAP4BodyText="TEXT";
    public static final String IMAP4BodyHeaders="HEADER";

    public IMAP4BodyPartFetchModifier (String modifierName, String partPath, String partText)
    {
        super(modifierName, (null == partPath) ? null : partPath + ((null == partText) ? "" : '.' + partText));
    }

    public IMAP4BodyPartFetchModifier (String modifierName, String partPath)
    {
        this(modifierName, partPath, null);
    }
}
