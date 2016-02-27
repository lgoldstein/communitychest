package net.community.chest.net.proto.text.imap4;

import java.io.IOException;

import net.community.chest.CoVariantReturn;
import net.community.chest.ParsableString;
import net.community.chest.net.TextNetConnection;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 27, 2008 11:32:23 AM
 */
public class IMAP4NamespacesInfoRspHandler extends AbstractIMAP4UntaggedResponseHandlerHelper {
	private final IMAP4NamespacesInfo	_nsInfo /* =null */;
	/*
	 * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4UntaggedResponseHandler#getResponse()
	 */
	@Override
	@CoVariantReturn
	protected IMAP4NamespacesInfo getResponse()
	{
		return _nsInfo;
	}

	public IMAP4NamespacesInfoRspHandler (TextNetConnection conn)
	{
		super(conn);
		_nsInfo = new IMAP4NamespacesInfo();
	}
	/**
	 * Handles a specific namespace data that is within a list
	 * @param startPos start index in parse buffer of namespace information (inclusive)
	 * @param nsInfo namespace information to be updated
	 * @return next index to be parsed in data buffer (<0 if error) 
	 * @throws IOException if I/O errors
	 */
	private int handleNamespaceList (final int startPos, final IMAP4Namespace nsInfo)throws IOException
	{
		nsInfo.reset();

		int	curPos=ensureParseBufferData(startPos, 1);
		if (curPos < 0)
			return curPos;

		// format is "(namespace delimiter)"
		if (_psHelper.getCharAt(curPos) != IMAP4Protocol.IMAP4_PARLIST_SDELIM)
			return (-1003);
		curPos++;
		
		IMAP4ParseAtomValue	aVal=extractStringHdrVal(curPos, false);
		if (null == aVal)
			return (-1004);
		nsInfo.setPrefix(aVal.toString());
		
		if (null == (aVal=extractStringHdrVal(aVal.startPos, false)))
			return (-1005);

		// if have more than one separator, then get first printable one
		final ParsableString	ps=aVal.val;
		if (ps != null)
		{	
			final char[]	sepVal=ps.array();
			for (int	index=ps.getStartIndex(), maxIndex=ps.getMaxIndex(); index < maxIndex; )
			{	
				final char	delim=sepVal[index];
				nsInfo.setDelimiter(delim);

				if ((delim > ' ') && (delim < 0x7F))
					break;
			}
		}
	
		if ((curPos=ensureParseBufferData(aVal.startPos, 1)) < 0)
			return curPos;
	
		if (_psHelper.getCharAt(curPos) != IMAP4Protocol.IMAP4_PARLIST_EDELIM)
			return (-1006);
		
		return (curPos + 1);
	}
	/**
	 * Handles a specific namespace data
	 * @param startPos start index in parse buffer of namespace information (inclusive)
	 * @param nsInfo namespace information to be updated
	 * @return next index to be parsed in data buffer (<0 if error)
	 * @throws IOException if I/O errors
	 */
	private int handleNamespace (final int startPos, final IMAP4Namespace nsInfo) throws IOException
	{
		int	curPos=ensureParseBufferData(startPos, 1);
		if (curPos < 0)
			return curPos;

		// allow NIL name space
		if (_psHelper.getCharAt(curPos) != IMAP4Protocol.IMAP4_PARLIST_SDELIM)
			return skipNILParseBuffer(curPos);
		
		if ((curPos=handleNamespaceList(curPos+1, nsInfo)) < 0)
			return curPos;

		if ((curPos=ensureParseBufferData(curPos, 1)) < 0)
			return curPos;

		// make sure stopped because found list delimiter end
		if (_psHelper.getCharAt(curPos) != IMAP4Protocol.IMAP4_PARLIST_EDELIM)
			return (-1002);

		return (curPos + 1);
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4UntaggedResponseHandler#handleUntaggedResponse(net.community.chest.ParsableString, int)
	 */
	@Override
	public int handleUntaggedResponse (final ParsableString ps, final int startPos) throws IOException
	{
		final int curPos=ps.findNonEmptyDataStart(startPos), maxIndex=ps.getMaxIndex();
		// should not happen, but OK if no modifiers found
		if ((curPos < startPos) || (startPos >= maxIndex))
			return 0;

		final int nextPos=ps.findNonEmptyDataEnd(curPos+1);
		// should not happen, but OK...
		if ((nextPos <= curPos) || (nextPos > maxIndex))
			return 0;
		
		if (!ps.compareTo(curPos, nextPos, IMAP4Protocol.IMAP4NamespaceCmdChars, true))
			return 0;			// if not a known modifier, then ignore it

		_nsInfo.reset();	// initialize namespaces

		int	nsPos=handleNamespace(nextPos, _nsInfo.getNamespace(IMAP4NamespaceType.PERSONAL, true));
		if (nsPos < 0)
			return nsPos;
		if ((nsPos=handleNamespace(nsPos, _nsInfo.getNamespace(IMAP4NamespaceType.OTHER, true))) < 0)
			return nsPos;
		if ((nsPos=handleNamespace(nsPos, _nsInfo.getNamespace(IMAP4NamespaceType.SHARED, true))) < 0)
			return nsPos;
		
		return 0;
	}
}
