package net.community.chest.io;

import java.io.File;

import net.community.chest.lang.math.LongsComparator;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to compare the last-modified-time of 2 {@link File}-s
 * from oldest to most recent</P>
 * 
 * @author Lyor G.
 * @since Oct 2, 2007 8:57:00 AM
 */
public class LastModifiedTimeComparator extends AbstractComparator<File> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3572403381382144312L;

	public LastModifiedTimeComparator (boolean ascending)
	{
		super(File.class, !ascending);
	}
	/*
	 * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (File f1, File f2)
	{
		final long	t1=(null == f1) ? 0L : f1.lastModified(),
					t2=(null == f2) ? 0L : f2.lastModified();
		return LongsComparator.compare(t1, t2);
	}

	public static final LastModifiedTimeComparator	ASCENDING=new LastModifiedTimeComparator(true),
													DESCENDING=new LastModifiedTimeComparator(false);}
