package net.community.chest.net.proto.text.imap4;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Uses a tags generator that starts at a certain value and increments by
 * 1 at each call (wraps around if negative)</P>
 * 
 * @author Lyor G.
 * @since Mar 27, 2008 9:23:43 AM
 */
public class IncrementalIMAP4TagsGenerator implements IMAP4TagsGenerator {
	/**
	 * Last returned value - next one will be +1 (wrapped around if negative)
	 */
	private int	_curTag /* =0 */;
	/**
	 * Initialized constructor
	 * @param startTag value to start from - if negative then its
	 * <U>absolute</U> value will be used
	 */
	public IncrementalIMAP4TagsGenerator (int startTag)
	{
		// need to do this because Math.abs(Integer.MIN_VALUE) => Integer.MIN_VALUE
		if ((_curTag=Math.abs(startTag)) < 0)
			_curTag = 0;
	}
	/**
	 * Default constructor - starts from {@link System#currentTimeMillis()}
	 */
	public IncrementalIMAP4TagsGenerator ()
	{
		this((int) System.currentTimeMillis());
	}
	/* NOTE !!! not synchronized
	 * @see net.community.chest.net.proto.text.imap4.IMAP4TagsGenerator#getNextTag()
	 */
	@Override
	public int getNextTag ()
	{
		_curTag++;

		if (_curTag < 0)
			_curTag = 0;

		return _curTag;
	}
}
