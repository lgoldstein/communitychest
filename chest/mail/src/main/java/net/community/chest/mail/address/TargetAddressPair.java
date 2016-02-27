package net.community.chest.mail.address;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.EnumUtil;
import net.community.chest.util.compare.AbstractComparator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Allows specifying the {@link MessageAddressType} of the {@link AddressPair}</P>
 * 
 * @author Lyor G.
 * @since Sep 12, 2007 11:40:12 AM
 */
public class TargetAddressPair extends AddressPair {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8262832215911360949L;
	/**
	 * Empty constructor
	 */
	public TargetAddressPair ()
	{
		super();
	}
	/**
	 * Address type
	 */
	private MessageAddressType	_addressType	/* =null */;
	public MessageAddressType getAddressType ()
	{
		return _addressType;
	}

	public void setAddressType (MessageAddressType addressType)
	{
		_addressType = addressType;
	}

	public static final String	TYPE_ATTR="type";
	public Element addAddressType (Element elem)
	{
		return DOMUtils.addNonEmptyAttributeObject(elem, TYPE_ATTR, getAddressType());
	}

	public MessageAddressType setAddressType (final Element elem)
	{
		final String	val=elem.getAttribute(TYPE_ATTR);
		if ((null == val) || (val.length() <= 0))
			return null;	// OK if no type specified

		final MessageAddressType	aType=MessageAddressType.fromString(val);
		if (null == aType)
			throw new NoSuchElementException("setAddressType(" + val + ") unknown value");

		setAddressType(aType);
		return aType;
	}
	/**
	 * Pre-initialized constructor
	 * @param dispName display name - OK if null/empty
	 * @param emailAddr e-mail address - OK if null/empty
	 * @param aType - OK if null empty
	 */
	public TargetAddressPair (String dispName, String emailAddr, MessageAddressType aType)
	{
	    super(dispName, emailAddr);
	    _addressType = aType;
	}
	/**
	 * Pre-initialized constructor
	 * @param dispName display name - OK if null/empty
	 * @param emailAddr e-mail address - OK if null/empty
	 */
	public TargetAddressPair (String dispName, String emailAddr)
	{
		this(dispName, emailAddr, null);
	}
	/**
	 * Copy constructor
	 * @param tp source object - OK if null
	 */
	public TargetAddressPair (TargetAddressPair tp)
	{
		this((null == tp) ? null : tp.getDisplayName(), (null == tp) ? null : tp.getEmailAddress(), (null == tp) ? null : tp.getAddressType());
	}
	/*
	 * @see net.community.chest.mail.address.AddressPair#compareTo(net.community.chest.mail.address.AddressPair)
	 */
	@Override
	public int compareTo (AddressPair ap)
	{
		if (ap == null)
			return (-1);
		if (this == ap)
			return 0;

		final Class<?>	oc=ap.getClass();
		if (oc != getClass())
			throw new IllegalArgumentException("Comparing " + getClass() + " with " + oc);

		return compareTo((TargetAddressPair) ap);
	}
	// NOTE: is NOT an implementation of Comparable interface
	public int compareTo (final TargetAddressPair tp)
	{
		if (tp == null)
			return (-1);
		if (this == tp)
			return 0;

		final MessageAddressType	tt=getAddressType(), ot=tp.getAddressType();
		final int					nRes=AbstractComparator.compareComparables(tt, ot);
		if (nRes != 0)
			return nRes;

		return super.compareTo(tp);
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		final Class<?>	oc=(obj == null) ? null : obj.getClass();
		if (oc != getClass())
			return false;
		if (this == obj)
			return true;

		return (0 == compareTo((TargetAddressPair) obj));
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return super.hashCode()
			 + EnumUtil.getValueHashCode(getAddressType());
	}
	/**
	 * "Partial" copy constructor
	 * @param ap address pair object - OK if null
	 */
	public TargetAddressPair (AddressPair ap)
	{
		super(ap);
	}
	/*
	 * @see net.community.chest.mail.address.AddressPair#clear()
	 */
	@Override
	public void clear ()
	{
		super.clear();
		setAddressType((MessageAddressType) null);
	}
	/**
	 * @param oldType current value
	 * @param newType new value
	 * @return "first" OK value
	 */
	protected static final MessageAddressType resolveAddrType (MessageAddressType oldType, MessageAddressType newType)
	{
		if (null == oldType)
			return newType;
		
		return oldType;
	}
	/**
	 * Updates the current information using the provided object by trying to fill in empty
	 * fields. Initialized fields are left untouched
	 * @param tp addressee object from which to fill in (null == do nothing)
	 * @return 0 if successful
	 */
	public int fillIn (final TargetAddressPair tp)
	{
		if (tp != null)
		{
			final int	nErr=super.fillIn(tp);
			if (nErr != 0)
				return nErr;

			setAddressType(resolveAddrType(getAddressType(), tp.getAddressType()));
		}

		return 0;
	}
	/**
	 * Retrieves only the targets having the specified type (e.g. To/Cc)
	 * and builds a comma separated formatted list of them
	 * @param tgts targets array - may be null/empty
	 * @param aType requested type - if null/bad then same as if no
	 * matching targets found
	 * @param fAddName TRUE if also include the display name(s)
	 * @return comma-separated formatted list of elements - may be
	 * null/empty if no targets or no matching type(s) found
	 */
	public static final String buildTargetsList (final TargetAddressPair[] tgts, final MessageAddressType aType, final boolean fAddName)
	{
		if (null == aType)
			return null;

		// OK if no targets
		final int		nTgts=(null == tgts) ? 0 : tgts.length;
        StringBuilder   sb=null;
        for (int	tIndex=0; tIndex < nTgts; tIndex++)
        {
        	final TargetAddressPair  ma=tgts[tIndex];
            if (null == ma) // should not happen
                continue;

            // skip addressess not of required type
            if (!aType.equals(ma.getAddressType()))
                continue;

            if (null == sb)
            	sb = new StringBuilder((nTgts - tIndex) * 128);
            else if (sb.length() > 0)	// terminate previous value
                sb.append(",\r\n\t");

            try
			{
				EmailAddressHelper.appendAddressPair(sb, fAddName ? ma.getDisplayName() : null, ma.getEmailAddress());
			}
			catch(IOException e)	// should not happen
			{
				throw new RuntimeException(e);
			}
        }

        // OK if no targets of specified type found
        return ((null == sb) || (sb.length() <= 0)) ? null : sb.toString();
	}
	/**
	 * @param <T> The {@link TargetAddressPair} generic type
	 * @param aType type of address being extracted - may be null/unknown
	 * @param cs sequence to extract from - if null/empty then null result
	 * is returned
	 * @param startPos position in sequence to start parsing (inclusive)
	 * @param endPos position in sequence to end parsing (exclusive)
	 * @param apClass {@link Class} instance of the {@link TargetAddressPair}
	 * derived object actually to be initialized. <B>Note:</B> must have a
	 * public default no-arguments constructor
	 * @return initialized instance - null if nothing to parse
	 * @throws Exception if parsing/instantiation error
	 */
	public static final <T extends TargetAddressPair> T extractTargetAddressPair (final MessageAddressType aType, final CharSequence cs, final int startPos, final int endPos, final Class<T> apClass) throws Exception
	{
		final T	ap=extractAddressPair(cs, startPos, endPos, apClass);
		if (ap != null)
			ap.setAddressType(aType);
		return ap;
	}
	/**
	 * @param <T> The {@link TargetAddressPair} generic type
	 * @param aType type of address being extracted - may be null/unknown
	 * @param cs sequence to extract from - if null/empty then null result
	 * is returned
	 * @param apClass {@link Class} instance of the {@link TargetAddressPair}
	 * derived object actually to be initialized. <B>Note:</B> must have a
	 * public default no-arguments constructor
	 * @return initialized instance - null if nothing to parse
	 * @throws Exception if parsing/instantiation error
	 */
	public static final <T extends TargetAddressPair> T extractTargetAddressPair (final MessageAddressType aType, final CharSequence cs, final Class<T> apClass) throws Exception
	{
		return extractTargetAddressPair(aType, cs, 0, (null == cs) ? 0 : cs.length(), apClass);
	}
	/**
	 * @param <T> The {@link TargetAddressPair} generic type
	 * @param aType type of <U>all</U> extracted address pairs - may be null/empty
	 * @param cs address pairs delimited by either ';' or ',' - may be null/empty
	 * @param firstPos position in sequence to start parsing
	 * @param csLen number of characters allowed to parse - OK if zero
	 * @param apClass {@link Class} instance of the {@link TargetAddressPair}
	 * derived object actually to be initialized. <B>Note:</B> must have a
	 * public default no-arguments constructor
	 * @return {@link Collection} of {@link TargetAddressPair} derived objects - may be
	 * null/empty if null/empty input. Note: if mail address missing/not found
	 * then a dummy one is generated
	 * @throws Exception if parsing/instantiation error
	 */
	public static final <T extends TargetAddressPair> Collection<T> extractTargetAddressPairs (final MessageAddressType aType, final CharSequence cs, final int firstPos, final int csLen, final Class<T> apClass) throws Exception
	{
		final Collection<T> apColl=extractAddressPairs(cs, firstPos, csLen, apClass);
		if ((null == apColl) || (apColl.size() <= 0))
			return apColl;	// OK if no pairs extracted

		for (final T t : apColl)
		{
			if (t != null)	// should not be otherwise
				t.setAddressType(aType);
		}

		return apColl;
	}
	/**
	 * @param <T> The {@link TargetAddressPair} generic type
	 * @param aType type of <U>all</U> extracted address pairs - may be null/empty
	 * @param cs address pairs delimited by either ';' or ',' - may be null/empty
	 * @param apClass {@link Class} instance of the {@link TargetAddressPair}
	 * derived object actually to be initialized. <B>Note:</B> must have a
	 * public default no-arguments constructor
	 * @return {@link Collection} of {@link TargetAddressPair} derived objects - may be
	 * null/empty if null/empty input. Note: if mail address missing/not found
	 * then a dummy one is generated
	 * @throws Exception if parsing/instantiation error
	 */
	public static final <T extends TargetAddressPair> Collection<T> extractTargetAddressPairs (final MessageAddressType aType, final CharSequence cs, final Class<T> apClass) throws Exception
	{
		return extractTargetAddressPairs(aType, cs, 0, (null == cs) ? 0 : cs.length(), apClass);
	}
	/**
	 * Marks all extracted pairs as unknown address type
	 * @param <T> The {@link TargetAddressPair} generic type
	 * @param cs address pairs delimited by either ';' or ',' - may be null/empty
	 * @param firstPos position in sequence to start parsing
	 * @param csLen number of characters allowed to parse - OK if zero
	 * @param apClass {@link Class} instance of the {@link TargetAddressPair}
	 * derived object actually to be initialized. <B>Note:</B> must have a
	 * public default no-arguments constructor
	 * @return {@link Collection} of {@link TargetAddressPair} derived objects - may be
	 * null/empty if null/empty input. Note: if mail address missing/not found
	 * then a dummy one is generated
	 * @throws Exception if parsing/instantiation error
	 */
	public static final <T extends TargetAddressPair> Collection<T> extractTargetAddressPairs (final CharSequence cs, final int firstPos, final int csLen, final Class<T> apClass) throws Exception
	{
		return extractTargetAddressPairs(null, cs, firstPos, csLen, apClass);
	}
	/**
	 * Marks all extracted pairs as &quot;unknown&quot; (i.e., <code>null</code).
	 * @param <T> The {@link TargetAddressPair} generic type
	 * @param cs address pairs delimited by either ';' or ',' - may be null/empty
	 * @param apClass {@link Class} instance of the {@link TargetAddressPair}
	 * derived object actually to be initialized. <B>Note:</B> must have a
	 * public default no-arguments constructor
	 * @return {@link Collection} of {@link TargetAddressPair} derived objects - may be
	 * null/empty if null/empty input. Note: if mail address missing/not found
	 * then a dummy one is generated
	 * @throws Exception if parsing/instantiation error
	 */
	public static final <T extends TargetAddressPair> Collection<T> extractTargetAddressPairs (final CharSequence cs, final Class<T> apClass) throws Exception
	{
		return extractTargetAddressPairs(cs, 0, (null == cs) ? 0 : cs.length(), apClass);
	}
	/**
	 * Creates an instance of a {@link TargetAddressPair} derived object
	 * initializing its name/address fields
	 * @param <T> The {@link TargetAddressPair} generic type
	 * @param aType type of address pair - may be null/empty/unknown
	 * @param name display name - may be null/empty
	 * @param addr e-mail address - may be null/empty
	 * @param apClass {@link Class} instance of the {@link TargetAddressPair}
	 * derived object actually to be initialized. <B>Note:</B> must have a
	 * public default no-arguments constructor
	 * @return initialized instance
	 * @throws Exception unable to instantiate
	 */
	public static final <T extends TargetAddressPair> T createTargetAddressPair (final MessageAddressType aType, final String name, final String addr, final Class<T> apClass) throws Exception
	{
		final T ap=createAddressPair(name, addr, apClass);
		ap.setAddressType(aType);
		return ap;
	}
	/**
	 * @param <T> The {@link TargetAddressPair} generic type
	 * @param curList initial list - if non-null, it is added to, otherwise a
	 * new one is created
	 * @param aType type of addresses to mark <U>all</U> extracted pairs
	 * @param cs address pairs delimited by either ';' or ',' - may be null/empty
	 * @param firstPos position in sequence to start parsing
	 * @param csLen number of characters allowed to parse - OK if zero
	 * @param apClass {@link Class} instance of the {@link TargetAddressPair}
	 * derived object actually to be initialized. <B>Note:</B> must have a
	 * public default no-arguments constructor
	 * @return updated list - may be null if no initial list and nothing added
	 * @throws Exception if instantiation/parsing errors encountered
	 */
	public static final <T extends TargetAddressPair> Collection<T> addTargetAddressPairs (final Collection<T> curList, final MessageAddressType aType, final CharSequence cs, final int firstPos, final int csLen, final Class<T> apClass) throws Exception
	{
		final Collection<T>	ia=extractTargetAddressPairs(aType, cs, firstPos, csLen, apClass);
		if ((null == ia) || (ia.size() <= 0))
			return curList;	// if nothing extracted then return original list - whatever its value 

		final Collection<T>	rl=(null == curList) ? new LinkedList<T>() : curList;
		rl.addAll(ia);
		return rl;
	}
	/**
	 * @param <T> The {@link TargetAddressPair} generic type
	 * @param curList initial list - if non-null, it is added to, otherwise a
	 * new one is created
	 * @param aType type of addresses to mark <U>all</U> extracted pairs
	 * @param cs address pairs delimited by either ';' or ',' - may be null/empty
	 * @param apClass {@link Class} instance of the {@link TargetAddressPair}
	 * derived object actually to be initialized. <B>Note:</B> must have a
	 * public default no-arguments constructor
	 * @return updated list - may be null if no initial list and nothing added
	 * @throws Exception if instantiation/parsing errors encountered
	 */
	public static final <T extends TargetAddressPair> Collection<T> addTargetAddressPairs (final Collection<T> curList, final MessageAddressType aType, final CharSequence cs, final Class<T> apClass) throws Exception
	{
		return addTargetAddressPairs(curList, aType, cs, 0, (null == cs) ? 0 : cs.length(), apClass);
	}
	/*
	 * @see net.community.chest.mail.address.AddressPair#clone()
	 */
	@Override
	@CoVariantReturn
	public TargetAddressPair clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see net.community.chest.mail.address.AddressPair#fromXml(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	public TargetAddressPair fromXml (Element elem) throws Exception
	{
		if (this != super.fromXml(elem))
			throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched re-constructed instances");

		setAddressType(elem);
		return this;
	}
	/*
	 * @see net.community.chest.mail.address.AddressPair#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		final Element	elem=super.toXml(doc);
		addAddressType(elem);
		return elem;
	}
}
