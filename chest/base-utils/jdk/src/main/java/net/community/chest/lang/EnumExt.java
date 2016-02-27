package net.community.chest.lang;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used as an "extension" for {@link Enum} that whose source cannot be
 * changed to provide some of the useful functionality (e.g., {@link #fromString(String, boolean)}},
 * cached {@link #getValues()}</P>
 * 
 * @param <E> Type of &quot;extended&quot; {@link Enum}
 * @author Lyor G.
 * @since Jan 8, 2008 12:33:27 PM
 */
public class EnumExt<E extends Enum<E>> extends BaseTypedValuesContainer<E> {
	public EnumExt (Class<E> eClass) throws IllegalArgumentException
	{
		super(eClass);
	}

	private List<E>	_values	/* =null */;
	public synchronized List<E> getValues ()
	{
		if (null == _values)
			_values = Collections.unmodifiableList(Arrays.asList(getValuesClass().getEnumConstants()));
		return _values;
	}

	public synchronized void setValues (List<E> values)
	{
		_values = values;
	}

	public E fromString (String name, boolean caseSensitive)
	{
		return CollectionsUtils.fromString(getValues(), name, caseSensitive);
	}
	/**
	 * @return TRUE if conversion from {@link String} to its {@link Enum}
	 * equivalent is done case sensitive (default=FALSE, i.e., case
	 * <U>insensitive</U>).
	 */
	public boolean isCaseSensitive ()
	{
		return false;
	}

	public E fromString (String name)
	{
		return fromString(name, isCaseSensitive());
	}
}
