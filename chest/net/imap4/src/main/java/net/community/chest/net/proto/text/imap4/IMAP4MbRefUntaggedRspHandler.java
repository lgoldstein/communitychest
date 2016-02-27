package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import net.community.chest.CoVariantReturn;
import net.community.chest.ParsableString;
import net.community.chest.net.TextNetConnection;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 27, 2008 11:15:36 AM
 */
public class IMAP4MbRefUntaggedRspHandler extends AbstractIMAP4UntaggedResponseHandlerHelper {
	private final IMAP4FoldersListInfo    _listInfo;
	/**
	 * Original command used to ask for the references (LIST/LSUB) - we need it filter other un-tagged responses
	 */
	private final char[]	_refCmd;
	/**
	 * Collection (lazy-allocated) used to accumulated the actual folders (since we do not know in advance how many
	 * responses we will get). Once the tagged response has been found, we need to update the actual response object.
	 * @see #updateFolders(IMAP4FoldersListInfo rsp)
	 */
	private Collection<IMAP4FolderInfo>	_foldersInfo	/* =null */;
	// constructor
	public IMAP4MbRefUntaggedRspHandler (final char[] refCmd, final TextNetConnection conn)
	{
		super(conn);

		if ((null == (_refCmd=refCmd)) || (refCmd.length <= 0))
			throw new IllegalArgumentException("No MBRef command specified");

		_listInfo = new IMAP4FoldersListInfo();
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4UntaggedResponseHandler#getResponse()
	 */
	@Override
	@CoVariantReturn
	protected IMAP4FoldersListInfo getResponse ()
	{
		return _listInfo;
	}
    /**
     * Updates the array of folders in the response - up till now they have been accumulated in a
     * collection since we do not know in advance how many responses we will get
     * @param rsp response to be updated
     * @return number of folders updated (<0 if error)
     * @see #_foldersInfo
     */
	protected int updateFolders (final IMAP4FoldersListInfo rsp)
	{
		final int numFolders=(null == _foldersInfo) ? 0 : _foldersInfo.size();
        if (numFolders <= 0)    // OK if no folders returned
	        return 0;

        rsp.setFolders(_foldersInfo);
        return numFolders;
	}
	/**
	 * Extracts the hierarchy separator response
	 * @param startPos index in parse buffer where to start looking for separator (inclusive)
	 * @param hierSep (IN/OUT) hierarchy separator (ONLY at index=0)
	 * @return next index in parse buffer (<0 if error)
	 * @throws IOException if I/O errors encountered
	 */
	private int extractHierarchySeparator (final int startPos, final char[] hierSep) throws IOException
	{
		for (int    index=0; index < hierSep.length; index++)
			hierSep[index] = '\0';  // assume unknown separator

		final IMAP4ParseAtomValue	aVal=extractStringHdrVal(startPos, false);
		// copy all separators (if any)
		for (int    index=0, maxIndex=Math.min(aVal.length(), hierSep.length); index < maxIndex; index++)
			hierSep[index] = aVal.charAt(index);

		// check if escaped character
		if (('\\' == hierSep[0]) && (hierSep.length > 1) && (hierSep[1] != '\0'))
		{
			hierSep[0] = hierSep[1];
			hierSep[1] = '\0';
		}

		return aVal.startPos;
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.AbstractIMAP4UntaggedResponseHandler#handleUntaggedResponse(net.community.chest.ParsableString, int)
	 */
	@Override
	public int handleUntaggedResponse (final ParsableString ps, final int startPos) throws IOException
	{
		int maxIndex=_psHelper.getMaxIndex(),
			curPos=_psHelper.findNonEmptyDataStart(startPos),
			nextPos=_psHelper.findNonEmptyDataEnd(curPos+1);
		if ((curPos < startPos) || (curPos >= maxIndex) || (nextPos <= curPos) || (nextPos >= maxIndex))
			return 0;   // ignore failure to find un-tagged data start or nothing follows it
		if (!_psHelper.compareTo(curPos, nextPos, _refCmd, true))
			return 0;   // ignore if this is not the command we seek

		 // each member is a String
		final Collection<String>  flagsInfo=new LinkedList<String>();
		if ((curPos=extractFlagsList(nextPos, flagsInfo)) < 0)
			return curPos;

		final int							numFlags=flagsInfo.size();
		final Collection<IMAP4FolderFlag>	objFlags=
				(numFlags <= 0) ? null : new ArrayList<IMAP4FolderFlag>(numFlags);
		// extract flags objects from their corresponding strings
		if (numFlags > 0)
		{
			for (final String flgVal : flagsInfo)
			{
				if ((null == flgVal) || (flgVal.length() <= 0))
					continue;	// should not happen
					
				objFlags.add(new IMAP4FolderFlag(flgVal));
			}
		}

		final char[]  hierSep=new char[2];    // we allocate 2 for escaping purpose
		if ((curPos=extractHierarchySeparator(curPos, hierSep)) < 0)
			return curPos;

		final IMAP4ParseAtomValue	aVal=extractStringHdrVal(curPos, true);
		final String  	folderName=(aVal.length() <= 0) ? null : aVal.toString();
		if ((null == folderName) || (folderName.length() <= 0))
			return (-502);

		if (null == _foldersInfo)
			_foldersInfo = new LinkedList<IMAP4FolderInfo>();
		_foldersInfo.add(new IMAP4FolderInfo(folderName, hierSep[0], objFlags));

		return 0;
	}
}
