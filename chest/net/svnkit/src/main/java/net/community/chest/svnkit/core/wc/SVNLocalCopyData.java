/*
 * 
 */
package net.community.chest.svnkit.core.wc;

import java.io.File;

import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.svnkit.core.SVNURLComparator;
import net.community.chest.util.compare.AbstractComparator;

import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Holds SVN related data about a local copy</O>
 * @author Lyor G.
 * @since Aug 5, 2009 1:31:27 PM
 */
public class SVNLocalCopyData
		implements PubliclyCloneable<SVNLocalCopyData>,
				   Comparable<SVNLocalCopyData> {
	private File	_f;
	public File getFile ()
	{
		return _f;
	}

	public void setFile (File f)
	{
		_f = f;
	}

	private SVNStatusTypeEnum	_status;
	public SVNStatusTypeEnum getStatus ()
	{
		return _status;
	}

	public void setStatus (SVNStatusTypeEnum s)
	{
		_status = s;
	}
	// returns TRUE if changed
	public boolean updateStatus (SVNStatus status)
	{
		final SVNStatusType		stVal=
			(null == status) ? null : status.getContentsStatus();
		final SVNStatusTypeEnum	newType=SVNStatusTypeEnum.fromStatus(stVal),
								oldType=getStatus();
		if (AbstractComparator.compareObjects(newType, oldType))
			return false;
			
		setStatus(newType);
		return true;
	}

	public boolean isVersioned ()
	{
		return SVNStatusTypeEnum.isVersioned(getStatus());
	}

	public SVNLocalCopyData (File f, SVNStatusTypeEnum s)
	{
		_f = f;
		_status = s;
	}

	public SVNLocalCopyData (File f)
	{
		this(f, SVNStatusTypeEnum.UNKNOWN);
	}

	public SVNLocalCopyData ()
	{
		this(null);
	}

	private SVNURL	_url;
	public SVNURL getURL ()
	{
		return _url;
	}

	public void setURL (SVNURL u)
	{
		_url = u;
	}
	// returns true if changed
	public boolean updateURL (final SVNStatus status)
	{
		final SVNURL	newVal=(null == status) ? null : status.getURL(),
						oldVal=getURL();
		if (AbstractComparator.compareObjects(newVal, oldVal))
			return false;

		setURL(newVal);
		return true;
	}

	private SVNRevision	_rev;
	public SVNRevision getRevision ()
	{
		return _rev;
	}

	public void setRevision (SVNRevision r)
	{
		_rev = r;
	}

	public boolean updateRevision (SVNStatus status)
	{
		final SVNRevision	newVal=(null == status) ? null : status.getRevision(),
							oldVal=getRevision();
		if (AbstractComparator.compareObjects(newVal, oldVal))
			return false;

		setRevision(newVal);
		return true;
	}
	// returns true if updated anything
	public boolean fromSVNStatus (SVNStatus status)
	{
		final boolean[]	stVals={
				updateRevision(status),
				updateStatus(status),
				updateURL(status)
			};
		for (final boolean v : stVals)
		{
			if (v)
				return true;
		}

		return false;
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	public SVNLocalCopyData clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo (SVNLocalCopyData o)
	{
		if (null == o)	// push nulls to end
			return (-1);
		if (this == o)
			return 0;

		int	nRes=AbstractComparator.compareComparables(getFile(), o.getFile());
		if (nRes != 0)
			return nRes;

		if ((nRes=AbstractComparator.compareComparables(getStatus(), o.getStatus())) != 0)
			return nRes;

		if ((nRes=SVNURLComparator.ASCENDING.compare(getURL(), o.getURL())) != 0)
			return nRes;

		if ((nRes=SVNRevisionComparator.ASCENDING.compare(getRevision(), o.getRevision())) != 0)
			return nRes;

		return 0;
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof SVNLocalCopyData))
			return false;
		return (0 == compareTo((SVNLocalCopyData) obj));
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return ClassUtil.getObjectHashCode(getFile())
			 + ClassUtil.getObjectHashCode(getStatus())
			 + ClassUtil.getObjectHashCode(getURL())
			 ;
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		final File				f=getFile();
		final SVNStatusTypeEnum	s=getStatus();
		final SVNURL			u=getURL();
		final SVNRevision		r=getRevision();
		final String			fs=(null == f) ? null : f.getAbsolutePath(),
								ss=(null == s) ? null : s.toString(),
								us=(null == u) ? null : u.toString(),
								rs=(null == r) ? null : r.toString();
		final int				fl=(null == fs) ? 0 : fs.length(),
								sl=(null == ss) ? 0 : ss.length(),
								ul=(null == us) ? 0 : us.length(),
								rl=(null == rs) ? 0 : rs.length(),
								tl=Math.max(0, fl) 
								 + Math.max(0, sl)
								 + Math.max(0, ul)
								 + Math.max(0, rl)
								 ;
		if (tl <= 0)
			return "";

		final StringBuilder	sb=new StringBuilder(tl + 8);
		if (fl > 0)
			sb.append(fs);

		sb.append('[');
		if (sl > 0)
			sb.append(ss);
		sb.append(']');

		if (ul > 0)
			sb.append(us);

		if (rl > 0)
			sb.append('@').append(rs);

		return sb.toString();
	}
}
