package net.community.chest.io.encode;

import net.community.chest.io.OptionallyCloseable;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 22, 2007 9:17:52 AM
 */
public interface OutputStreamDecoder extends OptionallyCloseable {
	/**
	 * @return last set decoding exception - may be null if no exceptions
	 * or only "pure" IOExceptions were thrown
	 */
	DecodingException getDecodeException ();
}
