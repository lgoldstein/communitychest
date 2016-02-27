package net.community.chest.net.proto.text.imap4;

import java.io.IOException;
import java.util.Collection;

import net.community.chest.net.TextNetConnection;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Class used to hold the result of a LIST/LSUB command</P>
 * 
 * @author Lyor G.
 * @since Sep 20, 2007 11:01:10 AM
 */
public class IMAP4FoldersListInfo extends IMAP4TaggedResponse {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6388243257151187403L;
	/**
	 * Extracted folders info - may be null/empty
	 */
	private Collection<IMAP4FolderInfo>   _folders	/* =null */;
	public Collection<IMAP4FolderInfo> getFolders ()
	{
		return _folders;
	}

	public void setFolders (Collection<IMAP4FolderInfo> folders)
	{
		_folders = folders;
	}

	public IMAP4FoldersListInfo ()
	{
		super();
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		final Class<?>	oc=(obj == null) ? null : obj.getClass();
		if (oc != getClass())
			return false;
		if (this == obj)
			return true;

		final IMAP4FoldersListInfo	fInfo=(IMAP4FoldersListInfo) obj;
		if (!isSameResponse(fInfo))
			return false;

		final Collection<IMAP4FolderInfo>	mf=getFolders(),
											of=fInfo.getFolders();
		if ((null == mf) || (mf.size() <= 0))
			return ((null == of) || (of.size() <= 0));
		else if ((null == of) || (of.size() <= 0))
			return false;
		else if (of.size() != mf.size())
			return false;

		return IMAP4FolderInfo.compareFolders(IMAP4FolderInfo.buildFoldersMap(of), IMAP4FolderInfo.buildFoldersMap(mf));
	}

	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return super.hashCode()
			+ IMAP4FolderInfo.calculateFoldersHashCode(getFolders())
			;
	}
	/*
	 * @see net.community.chest.net.proto.text.imap4.IMAP4TaggedResponse#reset()
	 */
	@Override
	public void reset ()
	{
		super.reset();

		final Collection<IMAP4FolderInfo>	mf=getFolders();
		if (mf != null)
			mf.clear();
	}
	/**
	 * Processes the responses of a LIST/LSUB command till tagged response found
	 * @param cmd listing command (LIST/LSUB)
	 * @param conn connection from which to read responses
	 * @param tagValue tag value used to signal end of responses
	 * @return tagged response + retrieved listing information
	 * @throws IOException if error in retrieval
	 */
	public static final IMAP4FoldersListInfo getFinalResponse (final char[] cmd, final TextNetConnection conn, final int tagValue) throws IOException
	{
        final IMAP4MbRefUntaggedRspHandler    rspHandler=new IMAP4MbRefUntaggedRspHandler(cmd, conn);
        try
        {
        	final IMAP4FoldersListInfo	rsp=(IMAP4FoldersListInfo) rspHandler.handleResponse(tagValue);
        	/*      If got an OK code, then we need to create the array of folders,
        	 * since up till now the information has been accumulated in a collection
        	 */
        	if (rsp.isOKResponse())
        	{
        		final int numFldrs=rspHandler.updateFolders(rsp);
        		if (numFldrs < 0)
        			throw new IMAP4RspHandleException("Cannot (err=" + numFldrs + ") update accumulated folders info");
        	}

        	return rsp;
        }
        catch(IOException ioe)
        {
        	final IMAP4FoldersListInfo	rsp=new IMAP4FoldersListInfo();
        	final int					numFldrs=rspHandler.updateFolders(rsp);
        	if (numFldrs <= 0)	// if no folders available, throw the original exception
        		throw ioe;

        	// otherwise, generate a dummy BAD response
        	rsp.setErrCode(EBAD);
        	rsp.setResponseLine(String.valueOf(tagValue) + " " + IMAP4_BAD + " internal exception - " + ioe.getMessage());
        	
        	return rsp;
        }
	}
}
