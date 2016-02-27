package net.community.chest.net.proto.text.imap4;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Interface that can be used to generate various sequences of IMAP4 tags</P>
 * 
 * @author Lyor G.
 * @since Mar 27, 2008 9:20:14 AM
 */
public interface IMAP4TagsGenerator {
	/**
	 * @return next available <U>non-negative</U> tag value
	 */
	int getNextTag ();
}
