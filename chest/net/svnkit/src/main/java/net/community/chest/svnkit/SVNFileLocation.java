package net.community.chest.svnkit;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.IOCopier;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.ISVNPropertyHandler;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

public class SVNFileLocation extends SVNLocation {
	private final File	_f;
	/*
	 * @see net.community.chest.svnkit.SVNLocation#getFile()
	 */
	@Override
	public final File getFile ()
	{
		return _f;
	}
	/*
	 * @see net.community.chest.svnkit.SVNLocation#getLocationObject()
	 */
	@Override
	@CoVariantReturn
	public final File getLocationObject ()
	{
		return getFile();
	}
	/*
	 * @see net.community.chest.svnkit.SVNLocation#getURL()
	 */
	@Override
	public final SVNURL getURL ()
	{
		return null;
	}

	public SVNFileLocation (File f) throws IllegalStateException
	{
		super(SVNLocationType.FILE);
		if ((_f=f) == null)
			throw new IllegalStateException("No file instance provided");
	}
	/*
	 * @see net.community.chest.svnkit.SVNLocation#appendSubPath(java.lang.String)
	 */
	@Override
	@CoVariantReturn
	public SVNFileLocation appendSubPath (String subPath)
		throws SVNException, IllegalStateException
	{
		if ((subPath == null) || (subPath.length() <= 0))
			throw new IllegalStateException("No sub path value provided");
		return new SVNFileLocation(new File(getFile(), subPath));
	}
	/*
	 * @see net.community.chest.svnkit.SVNLocation#getName()
	 */
	@Override
	public String getName ()
	{
		final File	f=getFile();
		return (f == null) ? null : f.getName();
	}
	/*
	 * @see net.community.chest.svnkit.SVNLocation#getInfo(org.tmatesoft.svn.core.wc.SVNWCClient)
	 */
	@Override
	public SVNInfo getInfo (final SVNWCClient wcc)
		throws SVNException, IOException
	{
		if (wcc == null)
			throw new EOFException("No WC client instance provided");

		return wcc.doInfo(getFile(), SVNRevision.UNDEFINED);
	}
	/*
	 * @see net.community.chest.svnkit.SVNLocation#listFiles(org.tmatesoft.svn.core.wc.SVNWCClient, net.community.chest.svnkit.SVNLocationFilter)
	 */
	@Override
	@CoVariantReturn	// in a sense,,,
	public List<SVNFileLocation> listFiles (final SVNWCClient wcc, final SVNLocationFilter filter)
		throws SVNException, IOException
	{
		if (wcc == null)	// enforce it even though not needed in order to be consistent
			throw new EOFException("No WC client instance provided");

		final File[]	fl=getFile().listFiles();
		if ((fl == null) || (fl.length <= 0))
			return null;

		final List<SVNFileLocation>	retVal=new ArrayList<SVNFileLocation>(Math.max(fl.length, 5));
		for (final File f : fl)
		{
			final SVNFileLocation	loc=new SVNFileLocation(f);
			if ((filter != null) && (!filter.accept(loc)))
				continue;
			if (!retVal.add(loc))
				continue;
		}

		return retVal;
	}
	/*
	 * @see net.community.chest.svnkit.SVNLocation#copyTo(org.tmatesoft.svn.core.wc.SVNWCClient, java.io.OutputStream)
	 */
	@Override
	public void copyTo (final SVNWCClient wcc, final OutputStream out)
		throws SVNException, IOException
	{
		if (wcc == null)
			throw new EOFException("No WC client instance provided");
		if (out == null)
			throw new EOFException("No output stream instance provided");

		final long	cpyLen=IOCopier.copyFromFile(getFile(), out);
		if (cpyLen < 0L)
			throw new EOFException("Error (" + cpyLen + ") while copying from " + getFile());
	}
	/*
	 * @see net.community.chest.svnkit.SVNLocation#createOutputStream(org.tmatesoft.svn.core.wc.SVNWCClient)
	 */
	@Override
	public OutputStream createOutputStream (final SVNWCClient wcc)
		throws SVNException, IOException
	{
		if (wcc == null)	// enforce it even though not needed in order to be consistent
			throw new EOFException("No WC client instance provided");

		return new FileOutputStream(getFile());
	}
	/*
	 * @see net.community.chest.svnkit.SVNLocation#getProperties(org.tmatesoft.svn.core.wc.SVNWCClient, org.tmatesoft.svn.core.wc.ISVNPropertyHandler)
	 */
	@Override
	public <H extends ISVNPropertyHandler> H getProperties (SVNWCClient wcc, H handler)
		throws SVNException, IOException
	{
		if (wcc == null)
			throw new EOFException("No WC client instance provided");

		wcc.doGetProperty(getFile(), null, SVNRevision.WORKING, SVNRevision.WORKING, SVNDepth.EMPTY, handler, Collections.emptyList());
		return handler;
	}
	/*
	 * @see net.community.chest.svnkit.SVNLocation#mergeTo(org.tmatesoft.svn.core.wc.SVNDiffClient, net.community.chest.svnkit.SVNLocation)
	 */
	@Override
	public void mergeTo (SVNDiffClient dfc, SVNLocation target)
			throws SVNException, IOException
	{
		if (dfc == null)
			throw new EOFException("No DIFF client provided");
		if (target == null)
			throw new StreamCorruptedException("No merge target provided");

		final File	srcFile=getFile(), tgtFile=target.getFile();
		if (tgtFile == null)
		{
			final SVNURL	tgtURL=target.getURL();
			if (tgtURL == null)
				throw new StreamCorruptedException("No merge target URL available");

			dfc.doMerge(srcFile, SVNRevision.WORKING, tgtURL, SVNRevision.HEAD, null, SVNDepth.EMPTY, false, false, false, false);
		}
		else
			dfc.doMerge(srcFile, SVNRevision.WORKING, tgtFile, SVNRevision.WORKING, tgtFile, SVNDepth.EMPTY, false, false, false, false);
		
	}
}