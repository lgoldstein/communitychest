package net.community.chest.io.encode.base64;

import net.community.chest.io.encode.DecodingException;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 22, 2007 9:04:35 AM
 */
public class Base64DecodingException extends DecodingException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8567268499457794972L;

	public Base64DecodingException (String message, char c)
	{
		super(message, c);
	}
}
