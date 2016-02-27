package net.community.chest.net.proto.text.imap4;

import java.util.LinkedList;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 24, 2008 2:09:03 PM
 */
public class IMAP4BodyFetchModifier extends IMAP4FetchModifier {
	private String  _partPath /* =null */;
	public String getPartPath ()
	{
		return _partPath;
	}

	public void setPartPath (String partPath)
	{
		_partPath = partPath;
	}

	private LinkedList<String>  _hdrs /* =null */;
	public void clearHdrs ()
	{
		_hdrs = null;
	}
	/**
	 * Finalizes the internal characters array - Note: calling it several times
	 * is inefficient.
	 */
	public void finalizeIt ()
	{
		_modChars = toCharArray();
	}
	/**
	 * Adds the specified headers to the list of headers issued in the modifier
	 * @param moreHdrs headers to be added - may be null/empty, in which case nothing is added
     * @param isFinal if TRUE, then no more changes may be made, and the matching char array is generated
     * (for efficiency). Note: any subsequent changes may cause undefined behavior if "isFinal" set to TRUE (!)
	 */
	public void addHdrs (final String[] moreHdrs, final boolean isFinal)
	{
		for (int nHdx=0, nHdrs=(null == moreHdrs) ? 0 : moreHdrs.length; nHdx < nHdrs; nHdx++)
		{
			final String  hdr=moreHdrs[nHdx];
			if ((null == hdr) || (hdr.length() <= 0))   // should not happen
				continue;

			if (null == _hdrs)
				_hdrs = new LinkedList<String>();
			_hdrs.add(hdr);
		}

		if (isFinal)
			finalizeIt();
	}
    /**
     * Adds the specified header to the list of headers that will be issued
     * @param hdrName header to be added - if NULL/empty then ignored. Note:
     * if last character is ':' it will be removed when modifier string is created
     * @param isFinal if TRUE, then no more changes may be made, and the matching char array is generated
     * (for efficiency). Note: any subsequent changes may cause undefined behavior if "isFinal" set to TRUE (!)
     */
	public void addHdr (final String hdrName, final boolean isFinal)
	{
		if ((hdrName != null) && (hdrName.length() > 0))
			addHdrs(new String[] { hdrName }, isFinal);
	}
    /**
     * Sets the issued headers list removing any previous instances
     * @param moreHdrs headers to be set (may be null/zero-length in which case, is same as "clearHdrs")
     * @param isFinal if TRUE, then no more changes may be made, and the matching char array is generated
     * (for efficiency). Note: any subsequent changes may cause undefined behavior if "isFinal" set to TRUE (!)
     */
	public void setHdrs (final String[] moreHdrs, boolean isFinal)
	{
		clearHdrs();
		addHdrs(moreHdrs, isFinal);
	}
		/* body sub parts with headers */
	public static final String IMAP4BodyHeaderFields="HEADER.FIELDS";
	public static final String IMAP4BodyNotHeaderFields="HEADER.FIELDS.NOT";
    /**
     * @param modifierName modifier name (BODY/BODY.PEEK)
     * @param fetchPartPath part path
     * @param moreHdrs additional headers (may be null/empty)
     * @param isFinal if TRUE, then no more changes may be made, and the matching char array is generated
     * (for efficiency). Note: any subsequent changes may cause undefined behavior if "isFinal" set to TRUE (!)
     * @see #toCharArray()
     */
	public IMAP4BodyFetchModifier (String modifierName, String fetchPartPath, String[] moreHdrs, boolean isFinal)
	{
		super(modifierName);
		_partPath = fetchPartPath;
		setHdrs(moreHdrs, isFinal);
	}
	/**
	 * @param modifierName modifier name (BODY/BODY.PEEK)
	 * @param fetchPartPath part path
	 * @param moreHdrs additional headers (may be null/empty) - no finalized
	 * @see #IMAP4BodyFetchModifier(String modName, String partPath, String[] hdrs, boolean isFinal)
	 * @see #finalizeIt()
	 */
	public IMAP4BodyFetchModifier (String modifierName, String fetchPartPath, String[] moreHdrs)
	{
		this(modifierName, fetchPartPath, moreHdrs, false);
	}
	/**
	 * @param modifierName modifier name (BODY/BODY.PEEK)
	 * @param fetchPartPath part path
	 * @param isFinal if TRUE, then no more changes may be made, and the matching char array is generated
	 * (for efficiency). Note: any subsequent changes may cause undefined behavior if "isFinal" set to TRUE (!)
	 * @see #toCharArray()
	 * @see #IMAP4BodyFetchModifier(String modName, String partPath, String[] hdrs, boolean isFinal)
	 */
	public IMAP4BodyFetchModifier (String modifierName, String fetchPartPath, boolean isFinal)
	{
		this(modifierName, fetchPartPath, null, isFinal);
	}
	/**
	 * @param modifierName modifier name (BODY/BODY.PEEK)
	 * @param fetchPartPath part path
	 * @see #IMAP4BodyFetchModifier(String modName, String partPath, String[] hdrs, boolean isFinal)
	 */
	public IMAP4BodyFetchModifier (String modifierName, String fetchPartPath)
	{
		this(modifierName, fetchPartPath, null);
	}
	/**
	 * @param modifierName modifier name (BODY/BODY.PEEK)
	 * @see #IMAP4BodyFetchModifier(String modName, String partPath, String[] hdrs, boolean isFinal)
	 */
	public IMAP4BodyFetchModifier (String modifierName)
	{
		this(modifierName, null);
	}

	private static final int AVG_HDR_LEN=32;
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4FetchModifier#getSubModifierInfo()
	 */
	@Override
	public String getSubModifierInfo ()
	{
		final int         	nHdrs=(null == _hdrs) ? 0 : _hdrs.size();
		final StringBuilder	sb=new StringBuilder(((null == _partPath) ? 0 : _partPath.length()) + (Math.max(nHdrs,0) * AVG_HDR_LEN) + 4);

		sb.append(IMAP4Protocol.IMAP4_BRCKT_SDELIM);
		sb.append(_partPath);

		if (nHdrs > 0)
		{
			sb.append(' ');
			sb.append(IMAP4Protocol.IMAP4_PARLIST_SDELIM);

			final int	sbLen=sb.length();
			for (final String  hdrName : _hdrs)
			{
				final int     nameLen=(null == hdrName) ? 0 : hdrName.length();
				if (nameLen <= 0)
					continue;

				// if header ends in ':' then remove it
				final int effLen=(':' == hdrName.charAt(nameLen - 1)) ? (nameLen - 1) : nameLen;
				if (0 == effLen)
					continue;

				if (sb.length() > sbLen)
					sb.append(' ');

				if (effLen != nameLen)
					sb.append(hdrName.substring(0, effLen));
				else
					sb.append(hdrName);
			}

			sb.append(IMAP4Protocol.IMAP4_PARLIST_EDELIM);
		}

		sb.append(IMAP4Protocol.IMAP4_BRCKT_EDELIM);
		return sb.toString();
	}
}
