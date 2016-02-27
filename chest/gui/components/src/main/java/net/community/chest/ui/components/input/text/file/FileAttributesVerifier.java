/*
 * 
 */
package net.community.chest.ui.components.input.text.file;

import java.io.File;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import net.community.chest.io.file.FileAttributeType;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 30, 2009 8:41:37 AM
 */
public class FileAttributesVerifier extends FileInputVerifier {
	public FileAttributesVerifier ()
	{
		super();
	}

	public static final boolean verifyValue (final boolean v, final Boolean a)
	{
		return (null == a) || (a.booleanValue() == v);
	}

	private Map<FileAttributeType,Boolean>	_attrsMap;
	protected Map<FileAttributeType,Boolean> getVerifiedAttributes (boolean createIfNotExist)
	{
		if ((null == _attrsMap) && createIfNotExist)
			_attrsMap = new EnumMap<FileAttributeType,Boolean>(FileAttributeType.class);
		return _attrsMap;
	}

	public Map<FileAttributeType,Boolean> getVerifiedAttributes ()
	{
		return getVerifiedAttributes(false);
	}
	// NOTE: only BOOLEAN type attributes are checked
	public void setVerifiedAttributes (Map<FileAttributeType,Boolean> am)
	{
		_attrsMap = am;
	}

	public static final boolean isVerifiableAttribute (final FileAttributeType a)
	{
		final Class<?>	t=(null == a) ? null : a.getAttributeClass();
		if ((null == t)
		 ||	(!Boolean.TYPE.isAssignableFrom(t))
		 ||	(!Boolean.class.isAssignableFrom(t)))
			return false;	// debug breakpoint

		return true;
	}

	public Boolean getVerifiedAttributeValue (final FileAttributeType a)
	{
		final Map<FileAttributeType,Boolean>	am=
			isVerifiableAttribute(a) ? getVerifiedAttributes() : null;
		if ((null == am) || (am.size() <= 0))
			return null;

		return am.get(a);
	}
	// null value means not to check
	public void setVerifiedAttributeValue (final FileAttributeType a, final Boolean v)
	{
		if (!isVerifiableAttribute(a))
			return;

		// don't need to create the map if null value mapped
		final Map<FileAttributeType,Boolean>	am=getVerifiedAttributes(v != null);
		if (null == v)
		{
			if (am != null)
			{
				final Boolean	prev=am.remove(a);
				if (prev != null)
					return;	// debug breakpoint
			}
		}
		else
		{
			final Boolean	prev=am.put(a, v);
			if (v.equals(prev))
				return;	// debug breakpoint
		}
	}

	public static final boolean verifyValue (final FileAttributeType a, final File f, final Boolean expValue)
	{
		if ((null == f) || (!isVerifiableAttribute(a)))
			return false;

		if (null == expValue)
			return true;	// OK if no restriction

		final Boolean	curValue=(Boolean) a.getValue(f);
		if (expValue.equals(curValue))
			return true;

		return false;	// debug breakpoint
	}

	public boolean verifyValue (final FileAttributeType a, final File f)
	{
		return verifyValue(a, f, getVerifiedAttributeValue(a));
	}

	public Boolean getFileExists ()
	{
		return getVerifiedAttributeValue(FileAttributeType.EXISTS);
	}

	public void setFileExists (Boolean v)
	{
		setVerifiedAttributeValue(FileAttributeType.EXISTS, v);
	}

	public Boolean getFileWritable ()
	{
		return getVerifiedAttributeValue(FileAttributeType.WRITEABLE);
	}

	public void setFileWritable (Boolean v)
	{
		setVerifiedAttributeValue(FileAttributeType.WRITEABLE, v);
	}

	public Boolean getFileReadable ()
	{
		return getVerifiedAttributeValue(FileAttributeType.READABLE);
	}

	public void setFileReadable (Boolean v)
	{
		setVerifiedAttributeValue(FileAttributeType.READABLE, v);
	}

	public Boolean getFileExecutable ()
	{
		return getVerifiedAttributeValue(FileAttributeType.EXECUTABLE);
	}

	public void setFileExecutable (Boolean v)
	{
		setVerifiedAttributeValue(FileAttributeType.EXECUTABLE, v);
	}

	public Boolean getFileIsFile ()
	{
		return getVerifiedAttributeValue(FileAttributeType.ISFILE);
	}

	public void setFileIsFile (Boolean v)
	{
		setVerifiedAttributeValue(FileAttributeType.ISFILE, v);
	}

	public Boolean getFileIsDirectory ()
	{
		return getVerifiedAttributeValue(FileAttributeType.ISDIR);
	}

	public void setFileIsDirectory (Boolean v)
	{
		setVerifiedAttributeValue(FileAttributeType.ISDIR, v);
	}

	public Boolean getFileIsHidden ()
	{
		return getVerifiedAttributeValue(FileAttributeType.ISHIDDEN);
	}

	public void setFileIsHidden (Boolean v)
	{
		setVerifiedAttributeValue(FileAttributeType.ISHIDDEN, v);
	}

	public Boolean getFileIsAbsolute ()
	{
		return getVerifiedAttributeValue(FileAttributeType.ISABS);
	}

	public void setFileIsAbsolute (Boolean v)
	{
		setVerifiedAttributeValue(FileAttributeType.ISABS, v);
	}
	/*
	 * @see net.community.chest.ui.components.input.text.FileInputVerifier#verifyFile(java.io.File)
	 */
	@Override
	public boolean verifyFile (File f)
	{
		for (final FileAttributeType a : FileAttributeType.VALUES)
		{
			if (!isVerifiableAttribute(a))
				continue;

			if (!verifyValue(a, f))
				return false;
		}

		return true;
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof FileAttributesVerifier))
			return false;
		if (this == obj)
			return true;

		final Map<?,?>	tm=getVerifiedAttributes(),
						om=((FileAttributesVerifier) obj).getVerifiedAttributes();
		return AbstractComparator.compareObjects(tm, om);
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		final Map<?,?>	tm=getVerifiedAttributes();
		return ClassUtil.getObjectHashCode(tm);
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		final Map<?,?>								am=
			getVerifiedAttributes();
		final Collection<? extends Map.Entry<?,?>>	al=
			((null == am) || (am.size() <= 0)) ? null : am.entrySet();
		final int						numAttrs=(null == al) ? 0 : al.size();
		if (numAttrs <= 0)
			return "[]";

		final StringBuilder	sb=new StringBuilder(Math.max(numAttrs,0) * 12 + 4)
								.append('[')
								;
		for (final Map.Entry<?,?> ae : al)
		{
			final Object	k=(null == ae) ? null : ae.getKey(),
							v=(null == ae) ? null : ae.getValue();
			final String	ks=(null == k) ? null : k.toString(),
							vs=(null == v) ? null : v.toString();
			if ((null == ks) || (ks.length() <= 0)
			 || (null == vs) || (vs.length() <= 0))
				continue;

			if (sb.length() > 1)
				sb.append(',');
			sb.append(ks)
			  .append('=')
			  .append(vs)
			  ;
		}

		return sb.append(']').toString();
	}
}
