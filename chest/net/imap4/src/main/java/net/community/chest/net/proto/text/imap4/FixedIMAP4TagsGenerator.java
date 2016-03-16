package net.community.chest.net.proto.text.imap4;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Uses a tags generator that returns <U>always</U> a certain value</P>
 * @author Lyor G.
 * @since Mar 27, 2008 9:27:28 AM
 */
public class FixedIMAP4TagsGenerator implements IMAP4TagsGenerator {
    /**
     * The fixed tag value
     */
    private final int    _tagValue;
    /**
     * Initialized constructor
     * @param tagValue fixed value to use - if negative then its
     * <U>absolute</U> value is used
     */
    public FixedIMAP4TagsGenerator (int tagValue)
    {
        _tagValue = Math.abs(tagValue);
    }
    /**
     * Default constructor - starts from {@link System#currentTimeMillis()}
     */
    public FixedIMAP4TagsGenerator ()
    {
        this((int) System.currentTimeMillis());
    }
    /*
     * @see net.community.chest.net.proto.text.imap4.IMAP4TagsGenerator#getNextTag()
     */
    @Override
    public int getNextTag ()
    {
        // need to do this because Math.abs(Integer.MIN_VALUE) => Integer.MIN_VALUE
        return (Integer.MIN_VALUE == _tagValue) ? Integer.MAX_VALUE : _tagValue;
    }
}
