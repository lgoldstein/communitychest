package net.community.chest.apache.httpclient.methods;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful HTTP headers that are <U>not</U> used for mail
 * @author Lyor G.
 * @since Oct 10, 2007 12:26:45 PM
 */
public final class HttpHeaders {
	private HttpHeaders ()
	{
		// no instance
	}

	public static final String	stdAcceptRangesHdr="Accept-Ranges",
								stdRangeHdr="Range";

	////////// useful keywords/modifiers ///////////////////
	public static final String	bytesModifier="bytes";
}
