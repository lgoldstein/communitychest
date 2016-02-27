package net.community.chest.net.proto.text.imap4;

import java.io.IOException;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 20, 2007 9:53:27 AM
 */
public class IMAP4RspHandleException extends IOException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6268683900244805976L;

	public IMAP4RspHandleException (String s)
	{
		super(s);
	}
}
