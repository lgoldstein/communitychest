package net.community.chest.net.proto.text.imap4;

import java.util.Random;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Uses a {@link Random} tags generator</P>
 * 
 * @author Lyor G.
 * @since Mar 27, 2008 9:21:23 AM
 */
public class RandomIMAP4TagsGenerator implements IMAP4TagsGenerator {
	/**
	 * Random numbers generator used to generate tags for simple commands
	 */
	private final Random  _tagGen;
	/**
	 * Initialized constructor
	 * @param seed seed to use for the {@link Random} object
	 */
	public RandomIMAP4TagsGenerator (long seed)
	{
		_tagGen = new Random(seed);
	}
	/**
	 * Default constructor - use {@link System#currentTimeMillis()} as seed
	 */
	public RandomIMAP4TagsGenerator ()
	{
		this(System.currentTimeMillis());
	}
	/* NOTE !!! not synchronized
	 * @see net.community.chest.net.proto.text.imap4.IMAP4TagsGenerator#getNextTag()
	 */
	@Override
	public int getNextTag ()
	{
		final int tagValue=_tagGen.nextInt();
		return (tagValue < 0) ? (0 - tagValue) : tagValue;
	}
}
