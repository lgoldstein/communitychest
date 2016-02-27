package net.community.chest.io.encode.qp;

import net.community.chest.io.encode.DecodingException;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 22, 2007 12:05:32 PM
 */
public class QuotedPrintableDecodingException extends DecodingException {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7382091909093850920L;

	public QuotedPrintableDecodingException (String message, char c)
	{
		super(message, c);
	}
}
