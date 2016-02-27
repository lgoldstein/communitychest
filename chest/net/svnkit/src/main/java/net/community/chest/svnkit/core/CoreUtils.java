/*
 * 
 */
package net.community.chest.svnkit.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import net.community.chest.svnkit.SVNLocation;
import net.community.chest.util.collection.CollectionsUtils;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 9:15:00 AM
 */
public final class CoreUtils {
	private CoreUtils ()
	{
		// no instance
	}
	
	public static SVNURL parseURIEncoded (URI uri) throws SVNException
	{
		return (null == uri) ? null : SVNURL.parseURIEncoded(uri.toASCIIString());
	}

	/**
	 * @param m The {@link SVNErrorMessage} to check
	 * @param igList The {@link SVNErrorCode}-s to check
	 * @return TRUE if the given {@link SVNErrorMessage} has a
     * {@link SVNErrorCode} whose {@link SVNErrorCode#getCode()} matches any
     * of the provided codes instances
	 */
	public static final boolean isContainedSVNErrorMessage (
    		final SVNErrorMessage m, final Collection<? extends SVNErrorCode> igList)
    {
    	if ((null == m) || (null == igList) || (igList.size() <= 0))
    		return false;

    	final SVNErrorCode		c=(null == m) ? null : m.getErrorCode();
    	return CollectionsUtils.containsElement(igList, c, SVNErrorCodeComparator.ASCENDING);
    }

	public static final boolean isContainedSVNErrorMessage (
    		final SVNErrorMessage m, final SVNErrorCode ... igList)
	{
    	if ((null == m) || (null == igList) || (igList.length <= 0))
    		return false;
   
    	return isContainedSVNErrorMessage(m, Arrays.asList(igList));
	}
    /**
     * @param e The {@link SVNException} to be checked
     * @param igList The {@link SVNErrorCode}-s to check
     * @return TRUE if the given {@link SVNException} has a
     * {@link SVNErrorMessage} whose {@link SVNErrorCode} is in the provided
     * codes instances
     * @see #isContainedSVNErrorMessage(SVNErrorMessage, Collection)
     */
    public static final boolean isContainedSVNException (
    		final SVNException e, final Collection<? extends SVNErrorCode> igList)
    {
    	if ((null == e) || (null == igList) || (igList.size() <= 0))
    		return false;

    	return isContainedSVNErrorMessage(e.getErrorMessage(), igList);
    }
    
    public static final boolean isContainedSVNException (
    		final SVNException e, final SVNErrorCode ... igList)
    {
    	if ((null == e) || (null == igList) || (igList.length <= 0))
    		return false;
   
    	return isContainedSVNException(e, Arrays.asList(igList));
    }
    /**
     * Some errors that should usually be ignored when querying a file's SVN information
     * (e.g., properties, status, URL, etc.) and the file is not under SVN control
     */
    public static final Collection<? extends SVNErrorCode>	DEFAULT_IGNORED_ERRORS=
    	Arrays.asList(SVNErrorCode.WC_NOT_DIRECTORY,
    				  SVNErrorCode.WC_NOT_FILE,
    				  SVNErrorCode.ENTRY_NOT_FOUND,
    				  SVNErrorCode.UNVERSIONED_RESOURCE,
    				  SVNErrorCode.ENTRY_MISSING_URL);
    public static final boolean isDefaultIgnoredError (final SVNException e)
    {
    	return isContainedSVNException(e, DEFAULT_IGNORED_ERRORS); 
    }
    /**
     * @param wcc The {@link SVNWCClient} instance to use
     * @param f The {@link File} to be checked
     * @return <code>true</code> if the file is under SVN control
     * @throws IOException If cannot access the file
     * @throws SVNException If failed to retrieve SVN information (other than the {@link #DEFAULT_IGNORED_ERRORS})
     */
    public static boolean isSVNFile (final SVNWCClient wcc, final File f)
		throws IOException, SVNException
	{
		try
		{
			final SVNURL	url=wcc.getReposRoot(f, null, SVNRevision.WORKING, null, null);
			if (null == url)
				throw new FileNotFoundException("No SVN URL available for file=" + f);

			return true;
		}
		catch(SVNException e)
		{
			if (isDefaultIgnoredError(e))
				return false;
			else
				throw e;
		}
	}

    public static boolean isSVNFile (final SVNWCClient wcc, final SVNLocation loc)
    	throws SVNException, IOException
    {
    	if (loc == null)
    		throw new IOException("No location data provided");

    	try
    	{
    		final SVNInfo	info=loc.getInfo(wcc);
    		final SVNURL	url=(info == null) ? null : info.getRepositoryRootURL();
    		if (url == null)
    			return false;

    		return true;
    	}
		catch(SVNException e)
		{
			if (isDefaultIgnoredError(e))
				return false;
			else
				throw e;
		}
    }

    public static boolean isSVNFile (final SVNStatusClient stc, final File f)
		throws IOException, SVNException
	{
    	try
    	{
    		final SVNStatus	st=stc.doStatus(f, false);
			if (null == st)
				throw new FileNotFoundException("No SVN status available for file=" + f);

			return true;
		}
		catch(SVNException e)
		{
			if (isDefaultIgnoredError(e))
				return false;
			else
				throw e;
		}
	}
	
}
