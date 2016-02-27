/*
 * 
 */
package net.community.chest.eclipse.launch;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.lang.EnumUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.util.collection.CollectionsUtils;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 8, 2009 2:17:31 PM
 */
public enum AttributeType {
	LAUNCH(LaunchAttribute.LAUNCH_KEY_PREFIX, LaunchAttribute.class),
	DEBUG(DebugAttribute.DEBUG_KEY_PREFIX, DebugAttribute.class);

	private final String	_keyPrefix;
	public final String getKeyPrefix ()
	{
		return _keyPrefix;
	}

	private final Class<? extends AttributeDescriptor> _dc;
	public final Class<? extends AttributeDescriptor> getAttributeDescriptorClass ()
	{
		return _dc;
	}

	private AttributeType (final String prfx, final Class<? extends AttributeDescriptor> dc)
	{
		_keyPrefix = prfx;
		_dc = dc;
	}
	/*
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString ()
	{
		return getKeyPrefix();
	}

	public static final List<AttributeType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final AttributeType fromName (final String n)
	{
		return EnumUtil.fromName(VALUES, n, false);
	}

	public static final AttributeType fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
	/**
	 * Checks if the <U><B>string</B> prefix</U> of the key matches any of
	 * the enum values {@link #getKeyPrefix()} value(s)
	 * @param k The original key
	 * @return The matching enum - <code>null</code> if no match found
	 */
	public static final AttributeType fromKey (final String k)
	{
		if ((null == k) || (k.length() <= 0))
			return null;

		for (final AttributeType a : VALUES)
		{
			final String	v=(null == a) ? null : a.getKeyPrefix();
			if (StringUtil.startsWith(k, v, true, false))
				return a;
		}

		return null;
	}

	public static final AttributeType fromElement (final Element elem)
	{
		return (null == elem) ? null : fromKey(elem.getAttribute(LaunchUtils.KEY_ATTR));
	}
}
