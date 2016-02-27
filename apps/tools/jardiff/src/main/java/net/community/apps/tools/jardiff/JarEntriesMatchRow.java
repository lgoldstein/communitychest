/*
 * 
 */
package net.community.apps.tools.jardiff;

import java.util.zip.ZipEntry;

import net.community.chest.Triplet;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 2, 2011 12:11:25 PM
 */
public class JarEntriesMatchRow extends Triplet<String,ZipEntry,ZipEntry> {
	public JarEntriesMatchRow (String ownerId, ZipEntry current, ZipEntry matching)
	{
		super(ownerId, current, matching);
	}

	public JarEntriesMatchRow ()
	{
		this(null, null, null);
	}

	public String getOwnerId ()
	{
		return getV1();
	}

	public void setOwnerId (String ownerId)
	{
		setV1(ownerId);
	}
	public ZipEntry getCurrentJarEntry ()
	{
		return getV2();
	}

	public void setCurrentJarEntry (ZipEntry entry)
	{
		setV2(entry);
	}

	public ZipEntry getMatchingJarEntry ()
	{
		return getV3();
	}

	public void setMatchingJarEntry (ZipEntry entry)
	{
		setV3(entry);
	}

	private boolean	_dataMismatch;
	public boolean isDataMismatch ()
	{
		return _dataMismatch;
	}

	public void setDataMismatch (boolean dataMismatch)
	{
		_dataMismatch = dataMismatch;
	}

	public boolean isMatchingEntry ()
	{
		return (getMatchingJarEntry() != null) && (!isDataMismatch());
	}
}
