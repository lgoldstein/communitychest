/*
 * 
 */
package net.community.chest.io.file;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.community.chest.CoVariantReturn;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.set.SetsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>An {@link Enum} denoting the available {@link File} attributes</P>
 * @author Lyor G.
 * @since Apr 13, 2009 9:24:22 AM
 */
public enum FileAttributeType {
	NAME(String.class) {
			/*
			 * @see net.community.chest.io.file.FileAttributeType#getValue(java.io.File)
			 */
			@Override
			@CoVariantReturn
			public String getValue (File f)
			{
				return (null == f) ? null : f.getName();
			}
		},
	PATH(String.class) {
			/*
			 * @see net.community.chest.io.file.FileAttributeType#getValue(java.io.File)
			 */
			@Override
			@CoVariantReturn
			public String getValue (File f)
			{
				return (null == f) ? null : f.getPath();
			}
		},
	ABSPATH(String.class) {
			/*
			 * @see net.community.chest.io.file.FileAttributeType#getValue(java.io.File)
			 */
			@Override
			@CoVariantReturn
			public String getValue (File f)
			{
				return (null == f) ? null : f.getAbsolutePath();
			}
		},
	ABSFILE(File.class) {
			/*
			 * @see net.community.chest.io.file.FileAttributeType#getValue(java.io.File)
			 */
			@Override
			@CoVariantReturn
			public File getValue (File f)
			{
				return (null == f) ? null : f.getAbsoluteFile();
			}
		},
	CANONPATH(String.class) {
			/*
			 * @see net.community.chest.io.file.FileAttributeType#getValue(java.io.File)
			 */
			@Override
			@CoVariantReturn
			public String getValue (File f)
			{
				try
				{
					return (null == f) ? null : f.getCanonicalPath();
				}
				catch (IOException e)
				{
					return null;
				}
			}
		},
	CANONFILE(File.class) {
			/*
			 * @see net.community.chest.io.file.FileAttributeType#getValue(java.io.File)
			 */
			@Override
			@CoVariantReturn
			public File getValue (File f)
			{
				try
				{
					return (null == f) ? null : f.getCanonicalFile();
				}
				catch (IOException e)
				{
					return null;
				}
			}
		},
	SIZE(Long.class) {
			/*
			 * @see net.community.chest.io.file.FileAttributeType#getValue(java.io.File)
			 */
			@Override
			@CoVariantReturn
			public Long getValue (File f)
			{
				return (null == f) ? null : Long.valueOf(f.length());
			}
		},
	LASTMODTIME(Long.class) {
			/*
			 * @see net.community.chest.io.file.FileAttributeType#getValue(java.io.File)
			 */
			@Override
			@CoVariantReturn
			public Long getValue (File f)
			{
				return (null == f) ? null : Long.valueOf(f.lastModified());
			}
		},
	ISFILE(Boolean.class) {
			/*
			 * @see net.community.chest.io.file.FileAttributeType#getValue(java.io.File)
			 */
			@Override
			@CoVariantReturn
			public Boolean getValue (File f)
			{
				return (null == f) ? null : Boolean.valueOf(f.isFile());
			}
		},
	ISDIR(Boolean.class) {
			/*
			 * @see net.community.chest.io.file.FileAttributeType#getValue(java.io.File)
			 */
			@Override
			@CoVariantReturn
			public Boolean getValue (File f)
			{
				return (null == f) ? null : Boolean.valueOf(f.isDirectory());
			}
		},
	ISABS(Boolean.class) {
			/*
			 * @see net.community.chest.io.file.FileAttributeType#getValue(java.io.File)
			 */
			@Override
			@CoVariantReturn
			public Boolean getValue (File f)
			{
				return (null == f) ? null : Boolean.valueOf(f.isAbsolute());
			}
		},
	ISHIDDEN(Boolean.class) {
			/*
			 * @see net.community.chest.io.file.FileAttributeType#getValue(java.io.File)
			 */
			@Override
			@CoVariantReturn
			public Boolean getValue (File f)
			{
				return (null == f) ? null : Boolean.valueOf(f.isHidden());
			}
		},
	READABLE(Boolean.class) {
			/*
			 * @see net.community.chest.io.file.FileAttributeType#getValue(java.io.File)
			 */
			@Override
			@CoVariantReturn
			public Boolean getValue (File f)
			{
				return (null == f) ? null : Boolean.valueOf(f.canRead());
			}
		},
	WRITEABLE(Boolean.class) {
			/*
			 * @see net.community.chest.io.file.FileAttributeType#getValue(java.io.File)
			 */
			@Override
			@CoVariantReturn
			public Boolean getValue (File f)
			{
				return (null == f) ? null : Boolean.valueOf(f.canWrite());
			}
		},
	EXECUTABLE(Boolean.class) {
			/*
			 * @see net.community.chest.io.file.FileAttributeType#getValue(java.io.File)
			 */
			@Override
			@CoVariantReturn
			public Boolean getValue (File f)
			{
				return (null == f) ? null : Boolean.valueOf(f.canExecute());
			}
		},
	EXISTS(Boolean.class) {
			/*
			 * @see net.community.chest.io.file.FileAttributeType#getValue(java.io.File)
			 */
			@Override
			@CoVariantReturn
			public Boolean getValue (File f)
			{
				return (null == f) ? null : Boolean.valueOf(f.exists());
			}
		},
	PARENT(String.class) {
			/*
			 * @see net.community.chest.io.file.FileAttributeType#getValue(java.io.File)
			 */
			@Override
			@CoVariantReturn
			public String getValue (File f)
			{
				return (null == f) ? null : f.getParent();
			}
		},
	PARFILE(File.class) {
			/*
			 * @see net.community.chest.io.file.FileAttributeType#getValue(java.io.File)
			 */
			@Override
			@CoVariantReturn
			public File getValue (File f)
			{
				return (null == f) ? null : f.getParentFile();
			}
		};

	private final Class<?>	_aClass;
	public final Class<?> getAttributeClass ()
	{
		return _aClass;
	}
	// NOTE: ignores IOException(s)
	public abstract Comparable<?> getValue (File f);

	FileAttributeType (final Class<?> ac)
	{
		_aClass = ac;
	}

	public static final List<FileAttributeType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final FileAttributeType fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final Map<FileAttributeType,Object> updateFileAttributes (final Map<FileAttributeType,Object> org, final File f, final Collection<FileAttributeType> attrs)
	{
		if ((null == f) || (null == attrs) || (attrs.size() <= 0))
			return null;

		Map<FileAttributeType,Object>	aMap=org;
		for (final FileAttributeType a : attrs)
		{
			if (null == a)
				continue;

			final Object	o=a.getValue(f), prev;
			if (null == o)	// null object is interpreted as removal 
			{
				if ((aMap == null) || (aMap.size() <= 0))
					continue;
				prev = aMap.remove(a);
			}
			else
			{
				if (null == aMap)
					aMap = new EnumMap<FileAttributeType,Object>(FileAttributeType.class);
				prev = aMap.put(a, o);
			}

			if (prev != null)
				continue;	// debug breakpoint
		}

		return aMap;
	}

	public static final Map<FileAttributeType,Object> updateFileAttributes (final Map<FileAttributeType,Object> org, final File f, final FileAttributeType ... attrs)
	{
		return updateFileAttributes(org, f, ((null == f) || (null == attrs) || (attrs.length <= 0)) ? null : SetsUtils.setOf(attrs));
	}

	public static final Map<FileAttributeType,Object> getFileAttributes (final File f, final Collection<FileAttributeType> attrs)
	{
		return updateFileAttributes(null, f, attrs);
	}

	public static final Map<FileAttributeType,Object> getFileAttributes (final File f, final FileAttributeType ... attrs)
	{
		return getFileAttributes(f, ((null == f) || (null == attrs) || (attrs.length <= 0)) ? null : SetsUtils.setOf(attrs));
	}

	public static final Map<FileAttributeType,Object> updateAllFileAttributes (final Map<FileAttributeType,Object> org, final File f)
	{
		return updateFileAttributes(org, f, VALUES);
	}

	public static final Map<FileAttributeType,Object> getAllFileAttributes (final File f)
	{
		return (null == f) ? null : getFileAttributes(f, VALUES);
	}
}
