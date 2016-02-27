/*
 * 
 */
package net.community.chest.io.file;

import java.io.File;
import java.io.FileFilter;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <V> Type of value being compared
 * @author Lyor G.
 * @since Apr 13, 2009 12:07:32 PM
 */
public abstract class FileAttributeFilter<V> extends BaseTypedValuesContainer<V> implements FileFilter {
	private FileAttributeType	_attrType;
	public FileAttributeType getFileAttributeType ()
	{
		return _attrType;
	}
	// makes sure attribute object type and values class are compatible
	public void setFileAttributeType (final FileAttributeType a) throws IllegalArgumentException
	{
		if (AbstractComparator.compareComparables(a, _attrType) != 0)
		{
			final Class<?>	ac=(null == a) ? null : a.getAttributeClass(), vc=getValuesClass();
			if ((ac != null) && (!vc.isAssignableFrom(ac)))
				throw new IllegalArgumentException("setFileAttributeType(" + a + ") mismatched types: expect=" + vc.getName() + "/got=" + ac.getName());
						
			_attrType = a;	// debug breakpoint
		}
	}

	protected FileAttributeFilter (Class<V> vc, FileAttributeType a) throws IllegalArgumentException
	{
		super(vc);

		_attrType = a;
	}

	protected FileAttributeFilter (Class<V> vc)
	{
		this(vc, null);
	}

	private V	_cmpValue;
	public V getComparedValue ()
	{
		return _cmpValue;
	}

	public void setComparedValue (V v)
	{
		_cmpValue = v;
	}

	protected abstract Boolean checkFileAttributeValue (final File f, final FileAttributeType a, final V val, final V cmpVal);
	/*
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept (File f)
	{
		final FileAttributeType	a=getFileAttributeType();
		final Object			o=(null == a) ? null : a.getValue(f);
		final Class<V>			vc=getValuesClass();
		final V					v=(null == o) ? null : vc.cast(o), cv=getComparedValue();
		final Boolean			res=checkFileAttributeValue(f, a, v, cv);
		return (res != null) && res.booleanValue();
	}
}
