package net.community.chest.mail.headers;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 22, 2007 9:36:10 AM
 */
public final class RFCMessageHeaders {
	private RFCMessageHeaders ()
	{
		// no instance
	}

	public static final int MIME_KEYWORD_MAX_LENGTH=32, MIME_KEYWORD_AVG_VALUE=32;
	/**
	 * Encoding prefix used for headers data
	 */
	public static final String HDRENCPREFIX="=?";
		public static final char[] HDRENCPREFIXCHARS=HDRENCPREFIX.toCharArray();
	/**
	 * Character used to delimit the various header encoding components
	 */
	public static final char HDRENCDATADELIMITER='?';
	/**
	 * Character used to indicate QUOTED-PRINTABLE encoded header value
	 */
	public static final char QP_HDRENC_CHAR='Q';
	/**
	 * Character used to indicate BASE64 encoded header value
	 */
	public static final char BASE64_HDRENC_CHAR='B';
	/**
	 * Encoding suffix used for headers data
	 */
	public static final String HDRENCSUFFIX="?=";
		public static final char[] HDRENCSUFFIXCHARS=HDRENCSUFFIX.toCharArray();
	/**
	 * Default charset used to encode headers if no override specified
	 */
	public static final String HDRENCDEFCHARSET="UTF-8".toUpperCase();
	/**
	 * Minimum length of a header encoding section
	 */
	public static final int MIN_HDRENC_LEN=HDRENCPREFIX.length()
	         + 1 /* charset name */
	         + 1 /* ? */
			 + 1 /* Q/B - encoding character */
			 + 1 /* ? */
	         + 0 /* allow no header value encoding */
	         + HDRENCSUFFIX.length()
	       ;
}
