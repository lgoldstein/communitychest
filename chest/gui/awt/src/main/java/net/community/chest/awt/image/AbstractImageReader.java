/*
 * 
 */
package net.community.chest.awt.image;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;

import net.community.chest.io.FileUtil;
import net.community.chest.util.set.StringSet;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Base class for some 3rd party graphics files reader(s)</P>
 * @author Lyor G.
 * @since Dec 2, 2008 3:04:27 PM
 */
public abstract class AbstractImageReader {
	protected AbstractImageReader ()
	{
		super();
	}
	/**
	 * @param filePath File path to be processed
	 * @return TRUE if file can be read by this reader
	 */
	public abstract boolean isMatchingFile (final String filePath);

	public boolean isMatchingFile (final File f)
	{
		return (null == f) ? false : isMatchingFile(f.getAbsolutePath());
	}

	public boolean isMatchingFile (final URL u)
	{
		return (null == u) ? false : isMatchingFile(u.getPath());
	}
	/**
	 * @param in The {@link InputStream} to read the images from
	 * @return A {@link List} of the read {@link Image}-s - may be null/empty
	 * @throws IOException If cannot read the images
	 */
	public abstract List<Image> readImages (InputStream in) throws IOException;

	public List<Image> readImages (byte[] data, int offset, int len) throws IOException
	{
		return (len <= 0) ? null : readImages(new ByteArrayInputStream(data, offset, len));	// no need to close it
	}

	public List<Image> readImages (byte[] data) throws IOException
	{
		return readImages(data, 0, (null == data) ? 0 : data.length);
	}

	public List<Image> readImages (String f) throws IOException
	{
		InputStream	is=null;
		try
		{
			is = new FileInputStream(f);
			return readImages(is);
		}
		finally
		{
			FileUtil.closeAll(is);
		}
	}

	public List<Image> readImages (File f) throws IOException
	{
		InputStream	is=null;
		try
		{
			is = new FileInputStream(f);
			return readImages(is);
		}
		finally
		{
			FileUtil.closeAll(is);
		}
	}

	public List<Image> readImages (URL url) throws IOException
	{
		InputStream	is=null;
		try
		{
			is = url.openStream();
			return readImages(is);
		}
		finally
		{
			FileUtil.closeAll(is);
		}
	}

	public static final String	GIF_SUFFIX="gif",
								PNG_SUFFIX="png",
								JPG_SUFFIX="jpg";

	public static final boolean isGIFFile (final String filePath)
	{
		return FileUtil.isMatchingFileSuffix(filePath, GIF_SUFFIX);
	}

	public static final boolean isJPGFile (final String filePath)
	{
		return FileUtil.isMatchingFileSuffix(filePath, JPG_SUFFIX);
	}

	public static final boolean isPNGFile (final String filePath)
	{
		return FileUtil.isMatchingFileSuffix(filePath, PNG_SUFFIX);
	}

	public static final boolean isGIFFile (final URL filePath)
	{
		return (null == filePath) ? false : isGIFFile(filePath.getPath());
	}
	public static final boolean isJPGFile (final URL filePath)
	{
		return (null == filePath) ? false : isJPGFile(filePath.getPath());
	}

	public static final boolean isPNGFile (final URL filePath)
	{
		return (null == filePath) ? false : isPNGFile(filePath.getPath());
	}

	private static final Set<String>	_imgSuffixes=
		new StringSet(ICOReader.ICO_SUFFIX,
					  BMPReader.BMP_SUFFIX,
					  GIF_SUFFIX,
					  PNG_SUFFIX,
					  JPG_SUFFIX);
	public static final boolean addImageSuffix (final String sfx)
	{
		final String	v=FileUtil.adjustExtension(sfx, false);
		if ((null == v) || (v.length() <= 0))
			return false;

		synchronized(_imgSuffixes)
		{
			return _imgSuffixes.add(v);
		}
	}

	public static final boolean isImageFile (final String filePath)
	{
		final int		pLen=(null == filePath) ? 0 : filePath.length(),
						sPos=(pLen <= 0) ? (-1) : filePath.lastIndexOf('.');
		final String	ext=((sPos >= 0) && (sPos < (pLen-1))) ? filePath.substring(sPos + 1) : null;
		if ((null == ext) || (ext.length() <= 0))
			return false;

		synchronized(_imgSuffixes)
		{
			return _imgSuffixes.contains(ext);
		}
	}
	
	public static final boolean isImageFile (final URL filePath)
	{
		return (null == filePath) ? false : isImageFile(filePath.getFile());
	}
}
