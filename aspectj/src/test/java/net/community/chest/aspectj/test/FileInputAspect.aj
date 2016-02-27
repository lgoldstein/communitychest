/*
 * 
 */
package net.community.chest.aspectj.test;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.Closeable;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Lyor G.
 * @since Jul 19, 2010 2:34:25 PM
 */
public privileged aspect FileInputAspect {
	protected static final Map<String,String>	_filesMap=
			Collections.synchronizedMap(new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER));
	public FileInputAspect ()
	{
		super();
	}

	/*
	 * NOTE: we cannot define the aspect on the execution since these are
	 * core classes, so we cannot instrument them - only the classes that
	 * call them (as long as these classes are not core classes themselves)
	 */
	after (File f) returning(Closeable r)
		: (call(FileInputStream+.new(File))
		|| call(FileReader+.new(File)))
		&& args(f)
	{
		mapOpenedFile(r, f, "r");
	}

	after (File f, String mode) returning(Closeable r)
		: call(RandomAccessFile+.new(File,String))
	   && args(f,mode)
	{
		mapOpenedFile(r, f, mode);
	}

	after (Closeable c) returning
		:  call(void Closeable+.close())
		&& target(c)
	{
		unmapClosedFile(c);
	}

	public static final String mapOpenedFile (final Closeable in, final File f, final String mode)
	{
		return mapOpenedFile(in, (f == null) ? null : f.getAbsolutePath(), mode);
	}

	public static final String mapOpenedFile (final Closeable in, final String filePath, final String mode)
	{
		final String	k=getFileKey(in);
		if ((null == k) || (k.length() <= 0))
			return null;
		if ((null == filePath) || (filePath.length() <= 0))
			return null;

		final String		prev=_filesMap.put(k, filePath);
		final PrintStream	out=(prev == null) ? System.out : System.err;
		out.append("mapOpenedFile[")
		   .append(k)
		   .append("](").append(mode).append(")=")
		   .append(filePath)
		   .append(" - prev=")
		   .println(prev);

		return prev;
	}

	public static final String unmapClosedFile (final Closeable in)
	{
		final String	k=getFileKey(in);
		if ((null == k) || (k.length() <= 0))
			return null;

		final String	filePath=_filesMap.remove(k);
		System.out.append("unmapClosedFile[").append(k).append("]=").println(filePath);
		return filePath;
	}

	public static final String getFileKey (final Closeable in)
	{
		final Class<?>	c=(null == in) ? null : in.getClass();
		final String	n=(null == c) ? null : c.getName();
		final int		h=(null == in) ? Integer.MIN_VALUE : System.identityHashCode(in);
		if ((null == n) || (n.length() <= 0))
			return null;

		return n + "@" + Integer.toHexString(h);
	}
}
