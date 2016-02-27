package net.community.chest.net.proto.text.imap4;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Uses the high 5 digits of the (max.) 10 digits integer value as a
 * "prefix" and increases the lower 5 digits</P>
 * 
 * @author Lyor G.
 * @since Mar 27, 2008 9:26:06 AM
 */
public class PrefixIMAP4TagsGenerator implements IMAP4TagsGenerator {
	/**
	 * Fixed "prefix" used for the tags "upper" value
	 */
	private final int	_tagsPrefix;
	public PrefixIMAP4TagsGenerator (final short tagsPrefix)
	{
		_tagsPrefix = ((tagsPrefix & 0x3FFF) * 100000) & 0x7FFFFFFF; 
	}
	
	public PrefixIMAP4TagsGenerator ()
	{
		this((short) System.currentTimeMillis());
	}
	/**
	 * Sequence number used as the tag "lower" value
	 */
	private int	_curOffset	/* =0 */;
	/* NOTE !!! not synchronized
	 * @see net.community.chest.net.proto.text.imap4.IMAP4TagsGenerator#getNextTag()
	 */
	@Override
	public int getNextTag ()
	{
		_curOffset++;

		if ((_curOffset < 0) || (_curOffset > Short.MAX_VALUE))
			_curOffset = 0;

		return (_tagsPrefix + _curOffset);
	}
}
