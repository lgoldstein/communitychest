package net.community.chest.dom.transform;

import java.util.NoSuchElementException;

import net.community.chest.lang.EnumExt;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provides some more {@link Enum} related functionality using XML {@link Element}-s</P>
 * 
 * @param <E> Type of extended {@link Enum} value
 * @author Lyor G.
 * @since Jan 8, 2008 12:39:26 PM
 */
public class DOMEnumExt<E extends Enum<E>> extends EnumExt<E> {
	public DOMEnumExt (Class<E> eClass) throws IllegalArgumentException
	{
		super(eClass);
	}
	/**
	 * @return Default attribute name used by {@link #toXml(Element, Enum)}
	 * and/or {@link #fromXml(Element, String)} to encode the value
	 * {@link Enum} {@link Class#getSimpleName()}
	 */
	public String getValueAttributeName ()
	{
		return getValuesClass().getSimpleName();
	}
	/**
	 * @param elem XML {@link Element} to use for attribute retrieval
	 * @param attrName Attribute used to encode the {@link Enum} value
	 * @param caseSensitive TRUE=if conversion from {@link String} to value
	 * is case sensitive
	 * @return Extract value - <code>null</code> if no value found for
	 * the attribute name
	 * @throws Exception if cannot convert the (non-null/empty) {@link String}
	 * value to its {@link Enum} equivalent
	 */
	public E fromXml (final Element elem, final String attrName, final boolean caseSensitive) throws Exception
	{
		final String	val=elem.getAttribute(attrName);
		if ((null == val) || (val.length() <= 0))
			return null;

		final E	v=fromString(val, caseSensitive);
		if (null == v)
			throw new NoSuchElementException("fromXml(" + attrName + "=" + val + ") unknown " + getValuesClass().getSimpleName() + " value");

		return v;
	}
	
	public E fromXml (final Element elem, final String attrName) throws Exception
	{
		return fromXml(elem, attrName, isCaseSensitive());
	}

	public E fromXml (final Element elem) throws Exception
	{
		return fromXml(elem, getValueAttributeName());
	}

	public E fromXml (final Element elem, final boolean caseSensitive) throws Exception
	{
		return fromXml(elem, getValueAttributeName(), caseSensitive);
	}
	/**
	 * @param elem XML {@link Element} whose attribute we want to set - may
	 * NOT be null
	 * @param attrName The attribute to set - may NOT be null/empty
	 * @param val The {@link Enum} value to set - if null then nothing added
	 * @return The {@link String} used for the encoding - null/empty if
	 * nothing was added/set as the attribute
	 */
	public String toXml (final Element elem, final String attrName, final E val)
	{
		final String	vs=(null == val) ? null : val.toString();
		if ((vs != null) && (vs.length() > 0))
			elem.setAttribute(attrName, vs);

		return vs;
	}

	public String toXml (final Element elem, final E val)
	{
		return toXml(elem, getValueAttributeName(), val);
	}
}
