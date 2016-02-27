/*
 * 
 */
package net.community.chest.svnkit;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import net.community.chest.CoVariantReturn;
import net.community.chest.lang.StringUtil;
import net.community.chest.svnkit.core.CoreUtils;
import net.community.chest.svnkit.core.wc.SVNPropsMap;
import net.community.chest.util.compare.AbstractComparator;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.ISVNPropertyHandler;
import org.tmatesoft.svn.core.wc.SVNDiffClient;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNWCClient;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 8, 2010 1:04:16 PM
 */
public abstract class SVNLocation implements Cloneable, Comparable<SVNLocation> {
	public static enum SVNLocationType { FILE, URL }

	private final SVNLocationType	_locType;
	public final SVNLocationType getLocationType ()
	{
		return _locType;
	}

	protected SVNLocation (final SVNLocationType locType) throws IllegalStateException
	{
		if ((_locType=locType) == null)
			throw new IllegalStateException("No location type provided");
	}

	public abstract File getFile ();
	public abstract SVNURL getURL ();
	public abstract Object getLocationObject ();
	public abstract SVNLocation appendSubPath (String subPath)
		throws SVNException, IllegalStateException;
	public abstract String getName ();
	public abstract SVNInfo getInfo (final SVNWCClient wcc)
		throws SVNException, IOException;
	public abstract List<? extends SVNLocation> listFiles (final SVNWCClient wcc, final SVNLocationFilter filter)
		throws SVNException, IOException;
	public abstract void copyTo (final SVNWCClient wcc, final OutputStream out)
		throws SVNException, IOException;
	public abstract OutputStream createOutputStream (final SVNWCClient wcc)
		throws SVNException, IOException;
	public abstract <H extends ISVNPropertyHandler> H getProperties (final SVNWCClient wcc, final H handler)
		throws SVNException, IOException;
	public abstract void mergeTo (final SVNDiffClient dfc, final SVNLocation target)
		throws SVNException, IOException;

	public SVNPropsMap getProperties (final SVNWCClient wcc)
		throws SVNException, IOException
	{
		return getProperties(wcc, new SVNPropsMap());
	}

	public boolean isFile (final SVNWCClient wcc)
		throws SVNException, IOException
	{
		final SVNInfo	info=getInfo(wcc);
		if (info == null)
			return false;

		final SVNNodeKind	nodeKind=info.getKind();
		if (SVNNodeKind.FILE.equals(nodeKind))
			return true;

		final File	fLoc=info.getFile();
		if (fLoc != null)
			return fLoc.isFile();

		return false;
	}

	public boolean isDirectory (final SVNWCClient wcc)
		throws SVNException, IOException
	{
		final SVNInfo	info=getInfo(wcc);
		if (info == null)
			return false;

		final SVNNodeKind	nodeKind=info.getKind();
		if (SVNNodeKind.DIR.equals(nodeKind))
			return true;

		final File	fLoc=info.getFile();
		if (fLoc != null)
			return fLoc.isDirectory();

		return false;
	}

	public boolean exists (final SVNWCClient wcc)
		throws SVNException, IOException
	{
		final SVNInfo	info;
		try
		{
			if ((info=getInfo(wcc)) == null)
				return false;
		}
		catch(SVNException e)
		{
			if (CoreUtils.isDefaultIgnoredError(e))
				return false;
			else
				throw e;
		}

		final File	fLoc=info.getFile();
		if (fLoc != null)
			return fLoc.exists();

		final SVNURL	url=info.getURL();
		if (url == null)
			return false;

		return true;
	}

	public List<? extends SVNLocation> listFiles (final SVNWCClient wcc)
		throws SVNException, IOException
	{
		return listFiles(wcc, null);
	}

	public void copyTo (final SVNWCClient wcc, final SVNLocation outLocation)
		throws SVNException, IOException
	{
		if (outLocation == null)
			throw new EOFException("No output location");

		try(OutputStream	out=outLocation.createOutputStream(wcc)) {
			copyTo(wcc, out);
		}
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		final Object	o=getLocationObject();
		return (o == null) ? 0 : o.hashCode();
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof SVNLocation))
			return false;
		if (this == obj)
			return true;

		final SVNLocation	ol=(SVNLocation) obj;
		final Object		tObj=getLocationObject(), oObj=ol.getLocationObject();
		if (!AbstractComparator.compareObjects(tObj, oObj))
			return false;

		return true;
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public SVNLocation clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		final Object	o=getLocationObject();
		return (o == null) ? "" : o.toString();
	}
	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo (SVNLocation o)
	{
		if (o == null)	// push null(s) to end
			return (-1);
		if (this == o)
			return 0;

		final SVNLocationType	l1=getLocationType(), l2=o.getLocationType();
		int						nRes=l1.compareTo(l2);
		if (nRes != 0)
			return nRes;

		if ((nRes=StringUtil.compareDataStrings(toString(), o.toString(), true)) != 0)
			return nRes;	// debug breakpoint

		return 0;
	}

	public static final SVNLocation fromString(String s)
		throws SVNException, IllegalStateException
	{
		if ((s == null) || (s.length() <= 0))
			throw new IllegalStateException("No string value provided");

		if (s.contains("://"))
			return new SVNURLLocation(SVNURL.parseURIEncoded(s));
		else
			return new SVNFileLocation(new File(s));
	}
}
