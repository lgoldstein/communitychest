package net.community.chest.mail.address;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import net.community.chest.CoVariantReturn;
import net.community.chest.ParsableString;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.io.EOLStyle;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;
import net.community.chest.mail.RFCMimeDefinitions;
import net.community.chest.mail.headers.RFCHeaderDefinitions;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 12, 2007 11:17:26 AM
 */
public class AddressPair implements Serializable, PubliclyCloneable<AddressPair>, XmlConvertible<AddressPair>, Comparable<AddressPair> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4019032583590818947L;
	/**
	 * Default (empty) constructor
	 */
	public AddressPair ()
	{
		super();
	}
	/**
	 * Display name
	 */
	private String	_displayName /* =null */;
	public String getDisplayName ()
	{
		return _displayName;
	}

	public void setDisplayName (String dispName)
	{
		_displayName = dispName;
	}

	public static final String	NAME_ATTR="name";
	public Element addDisplayName (Element elem)
	{
		return DOMUtils.addNonEmptyAttribute(elem, NAME_ATTR, getDisplayName());
	}

	public String setDisplayName (Element elem)
	{
		final String	val=elem.getAttribute(NAME_ATTR);
		if ((val != null) && (val.length() > 0))
			setDisplayName(val);

		return val;
	}
	/**
	 * E-mail address
	 */
	private String _emailAddress /* =null */;
	public String getEmailAddress ()
	{
		return _emailAddress;
	}

	public void setEmailAddress (String emailAddr)
	{
		_emailAddress = emailAddr;
	}

	public static final String	ADDRESS_ATTR="address";
	public Element addEmailAddress (Element elem)
	{
		return DOMUtils.addNonEmptyAttribute(elem, ADDRESS_ATTR, getEmailAddress());
	}

	public String setEmailAddress (Element elem)
	{
		final String	val=elem.getAttribute(ADDRESS_ATTR);
		if ((val != null) && (val.length() > 0))
			setEmailAddress(val);

		return val;
	}
	/**
	 * Pre-initialized constructor
	 * @param dispName display name - OK if null/empty
	 * @param emailAddr e-mail address - OK if null/empty
	 */
	public AddressPair (String dispName, String emailAddr)
	{
		_displayName = dispName;
		_emailAddress = emailAddr;
	}
	/**
	 * Copy constructor
	 * @param ap source address pair - OK if null or has null/empty members
	 */
	public AddressPair (AddressPair ap)
	{
		this((null == ap) ? null : ap.getDisplayName(), (null == ap) ? null : ap.getEmailAddress());
	}
	/*
	 * @see net.community.chest.dom.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	public AddressPair fromXml (Element elem) throws Exception
	{
		setDisplayName(elem);
		setEmailAddress(elem);

		return this;
	}

	public AddressPair (Element elem) throws Exception
	{
		if (this != fromXml(elem))
			throw new IllegalStateException("<init>(" + DOMUtils.toString(elem) + ") mismatched recovered instances");
	}

	public static final String	ADDRPAIR_ELEM_NAME="addressPair";
	public String getRootElementName ()
	{
		return ADDRPAIR_ELEM_NAME;
	}

	public Element getRootElement (Document doc)
	{
		return doc.createElement(getRootElementName());
	}
	/*
	 * @see net.community.chest.dom.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		final Element	elem=getRootElement(doc);
		addDisplayName(elem);
		addEmailAddress(elem);
		return elem;
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public AddressPair clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/**
	 * Resets contents to null/empty/illegal value(s)
	 */
	public void clear ()
	{
		setDisplayName((String) null);
		setEmailAddress((String) null);
	}
	/**
	 * @return TRUE if neither display name nor address are set
	 */
	public boolean isEmpty ()
	{
		final String	a=getEmailAddress(), n=getDisplayName();
		return ((null == a) || (a.length() <= 0))
			&& ((null == n) || (n.length() <= 0))
			;
	}
	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo (final AddressPair ap)
	{
		if (ap == null)
			return (-1);
		if (ap == this)
			return 0;

		final String[]	vals={
				getDisplayName(), ap.getDisplayName(),
				getEmailAddress(), ap.getEmailAddress()
			};
		for (int vIndex=0; vIndex < vals.length; vIndex += 2)
		{
			final String	v1=vals[vIndex], v2=vals[vIndex+1];
			final int		nRes=StringUtil.compareDataStrings(v1, v2, false);
			if (nRes != 0)
				return nRes;
		}

		return 0;
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

		return (0 == compareTo((AddressPair) obj));
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return StringUtil.getDataStringHashCode(getDisplayName(), false)
			 + StringUtil.getDataStringHashCode(getEmailAddress(), false);
	}
	/**
	 * @return contents as address pair - null/empty string if not initialized
	 */
	public String asAddressPair ()
	{
		return isEmpty() ? "" : EmailAddressHelper.buildAddressPair(getDisplayName(), getEmailAddress());
	}
	/* @see EmailAddressHelper#buildAddressPair(String,String)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
	    return asAddressPair();
	}
	/**
	 * @param oldObject current value
	 * @param newObject new value
	 * @return first non-null object
	 */
	protected static final <T> T resolveObject (T oldObject, T newObject)
	{
		return (null == oldObject) ? newObject : oldObject;
	}
	/**
	 * Returns the first non-null string
	 * @param oldStr current value
	 * @param newStr new value
	 * @return the "first" non-null string (if any)
	 */
	protected static final String resolveString (String oldStr, String newStr)
	{
		return resolveObject(oldStr, newStr);
	}
	/**
	 * Updates the current information using the provided object by trying to fill in empty
	 * fields. Initialized fields are left untouched
	 * @param ap addressee object from which to fill in (null == do nothing)
	 * @return 0 if successful
	 */
	public int fillIn (final AddressPair ap)
	{
		if (ap != null)
		{
			setEmailAddress(resolveString(getEmailAddress(), ap.getEmailAddress()));
			setDisplayName(resolveString(getDisplayName(), ap.getDisplayName()));
		}

		return 0;
	}
	/**
	 * Parses an address pair consisting of an optional display name and
	 * e-mail address. If no e-mail address found then a dummy one is used
	 * @param addrPair address pair string (if null/empty then error)
	 * @param addressee addressee to be updated
	 * @return TRUE if successful
	 */
	public static final boolean setAddressPair (final CharSequence addrPair, final AddressPair addressee)
	{
		final int	apLen=(null == addrPair) ? 0 : addrPair.length();
		if ((apLen <= 0) || (null == addressee))
			return false;
	
		final ParsableString	ps=(new ParsableString(addrPair)).trim();
		final int				startIndex=ps.getStartIndex(), maxIndex=ps.getMaxIndex();
	
		int	curIndex=ps.findNonEmptyDataStart();
		if ((curIndex < startIndex) || (curIndex >= maxIndex))
			return false;
	
		char	tch=ps.getCharAt(curIndex);
		boolean	nameSet=false;
		// check if a delimited display name
		if (('\"' == tch) || ('\'' == tch))
		{
			if ((curIndex=setDelimitedDisplayName(addressee, ps, curIndex+1, tch)) <= 0)
				return false;
	
			// check if had to use a dummy e-mail address
			if (curIndex >= maxIndex)
				return true;
	
			tch = ps.getCharAt(curIndex);
			nameSet = true;
		}
	
		// check if this is a delimited e-mail address
		if (RFCMimeDefinitions.MAIL_ADDR_START_DELIM == tch)
		{
			final String	addrVal=EmailAddressHelper.extractDelimitedEmailAddress(ps, curIndex+1);
			if ((null == addrVal) || (addrVal.length() <= 0))
				return false;
	
			addressee.setEmailAddress(addrVal);
			return true;
		}
	
		int		lastIndex=ps.lastIndexOf(' ', maxIndex - 1, curIndex);
		String	nameVal=null, addrVal=null;
	
		// if found ' ' then check what follows it
		if (lastIndex > curIndex)
		{
			if (RFCMimeDefinitions.MAIL_ADDR_START_DELIM == ps.getCharAt(lastIndex+1))
			{
				addrVal = EmailAddressHelper.extractDelimitedEmailAddress(ps, lastIndex+2);
				if ((null == addrVal) || (addrVal.length() <= 0))
					return false;
	
				nameVal = ps.substring(curIndex, lastIndex);
			}
			else
			{
				addrVal = ps.substring(lastIndex + 1);
	
				// if last component is not a valid mail address, assume entire buffer is a display name
				if (EmailAddressHelper.validateRFC822MailAddress(addrVal) != 0)
				{
					addrVal = RFCMimeDefinitions.NonMailMailAddress;
					nameVal = ps.substring(curIndex);
				}
				else
					nameVal = ps.substring(curIndex, lastIndex);
			}
		}
		else	// no space after the current index
		{
			final String	compVal=ps.substring(curIndex);
	
			// if what follows is not a valid e-mail address then assume it is a display name
			if (EmailAddressHelper.validateRFC822MailAddress(compVal) != 0)
			{
				addrVal = RFCMimeDefinitions.NonMailMailAddress;
				nameVal = compVal;
			}
			else
				addrVal = compVal;
		}
	
		if ((nameVal != null) && (nameVal.length() > 0))
		{
			if (nameSet)	// if already set then name, then formatting error - e.g.: "foo bar" foo
				return false;
	
			addressee.setDisplayName(RFCHeaderDefinitions.decodeHdrValue(nameVal.trim(), true));
		}
	
		addressee.setEmailAddress(addrVal);
		return true;
	}
	/**
	 * Parses an address pair consisting of an optional display name and
	 * e-mail address. If no e-mail address found then a dummy one is used
	 * @param addrPair address pair string (if null/empty then error)
	 * @param startPos start position in address pair string (inclusive)
	 * @param endPos start position in address pair string (exclusive)
	 * @param addressee addressee to be updated
	 * @return TRUE if successful
	 */
	public static final boolean setAddressPair (final CharSequence addrPair, final int startPos, final int endPos, final AddressPair addressee)
	{
		final int	apLen=(null == addrPair) ? 0 : addrPair.length();
		if ((apLen <= 0) || (startPos < 0) || (startPos >= apLen) || (endPos < 0) || (endPos > apLen) || (startPos >= endPos))
			return false;
	
		return setAddressPair(addrPair.subSequence(startPos, endPos), addressee);
	}
	/**
	 * @param <T> The {@link AddressPair} generic type
	 * @param addrPair address pair value (if null/empty then error/exception)
	 * @param startPos start position in address pair string (inclusive)
	 * @param endPos start position in address pair string (exclusive)
	 * @param apClass {@link Class} instance of the {@link AddressPair}
	 * derived object actually to be initialized. <B>Note:</B> must have a
	 * public default no-arguments constructor
	 * @return initialized object - null if nothing to parse
	 * @throws Exception if parsing/instantiation error
	 */
	public static final <T extends AddressPair> T extractAddressPair (final CharSequence addrPair, final int startPos, final int endPos, final Class<T> apClass) throws Exception
	{
		if ((endPos - startPos) <= 0)
			return null;

		final T	ma=apClass.newInstance();
		if (setAddressPair(addrPair, startPos, endPos, ma))
			return ma;
	
		throw new IllegalStateException("Cannot parse address pair=" + ((null == addrPair) ? null : addrPair.toString()));
	}
	/**
	 * @param <T> The {@link AddressPair} generic type
	 * @param addrPair address pair value (if null/empty then null instance returned)
	 * @param apClass {@link Class} instance of the {@link AddressPair}
	 * derived object actually to be initialized. <B>Note:</B> must have a
	 * public default no-arguments constructor
	 * @return initialized object - null if nothing to parse
	 * @throws Exception if parsing/instantiation error
	 */
	public static final <T extends AddressPair> T extractAddressPair (final CharSequence addrPair, final Class<T> apClass) throws Exception
	{
		if ((null == addrPair) || (addrPair.length() <= 0))
			return null;

		final T	ma=apClass.newInstance();
		if (setAddressPair(addrPair, ma))
			return ma;
	
		throw new IllegalStateException("Cannot parse address pair=" + addrPair);
	}
	/**
	 * Extracts a list of address pairs delimited by either ';' or ','
	 * @param <T> The {@link AddressPair} generic type
	 * @param cs address pairs delimited by either ';' or ',' - may be null/empty
	 * @param firstPos position in sequence to start parsing
	 * @param csLen number of characters allowed to parse - OK if zero
	 * @param apClass {@link Class} instance of the {@link AddressPair}
	 * derived object actually to be initialized. <B>Note:</B> must have a
	 * public default no-arguments constructor
	 * @return {@link Collection} of {@link AddressPair} derived objects - may be
	 * null/empty if null/empty input. Note: if mail address missing/not found
	 * then a dummy one is generated
	 * @throws Exception if parsing/instantiation error
	 */
	public static final <T extends AddressPair> Collection<T> extractAddressPairs (final CharSequence cs, final int firstPos, final int csLen, final Class<T> apClass) throws Exception
	{
		boolean			inMail=false;
		Collection<T>	ap=null;
		int				startPos=firstPos;
		char			startCh='\0', nameCh='\0';
	
		for (int	nPos=firstPos; nPos < csLen; nPos++)
		{
			final char	tch=cs.charAt(nPos);
			switch(tch)
			{
				case ';'	:
				case ','	:
					// ignore delimiter if found in display name or in delimited mail address
					if (inMail || (nameCh > ' '))
						break;
	
					// ignore multiple delimiters in sequence
					if (startCh > ' ')
					{
						final T	apv=extractAddressPair(cs, startPos, nPos, apClass);
						if (null == ap)
							ap = new LinkedList<T>();
						ap.add(apv);
					}
	
					startPos = nPos + 1;
					startCh = '\0';
					break;
	
				case '\''	:
				case '\"'	:
					if (nameCh > ' ')
					{
						// skip non-matching delimiter
						if (tch != nameCh)
							break;
	
						// ignore escaped quotes in display name
						if ((nPos > 0) && ('\\' == cs.charAt(nPos-1)))
							break;
	
						// check NEXT character - if not a white-space/e-mail start, then assume some malformed display name
						final char	nch=(nPos < (csLen-1)) ? cs.charAt(nPos + 1) : '\0';
						if ((nch <= ' ') || (RFCMimeDefinitions.MAIL_ADDR_START_DELIM == nch))
							nameCh = '\0';
					}
					else	// remember which delimiter started the name
					{
						nameCh = tch;
						startCh = tch;
						startPos = nPos;
					}
					break;
	
				case RFCMimeDefinitions.MAIL_ADDR_START_DELIM	:
					// ignore <> if found within name
					if (nameCh > ' ')
						break;
					if (inMail)
						throw new IllegalStateException("Duplicate mail start delimiter in " + cs);
	
					// check if already have starting point (e.g., display name)
					if (startCh <= ' ')
					{
						startCh = tch;
						startPos = nPos;
					}
	
					inMail = true;
					break;
	
				case RFCMimeDefinitions.MAIL_ADDR_END_DELIM		:
					// ignore <> if found within name
					if (nameCh > ' ')
						break;
					if (!inMail)
						throw new IllegalStateException("Duplicate mail end delimiter in " + cs);
	
					inMail = false;
					break;
	
				default	:
					// make "startPos" point to first non-empty character if not in delimited name or address mode
					if ((nameCh <= ' ') && (!inMail))
					{
						if ((tch > ' ') && (startCh <= ' '))
						{
							startPos = nPos;
							startCh = tch;
						}
					}
			}
		}
	
		// make sure not in mid-name/address when finished string
		if (inMail || (nameCh > ' '))
			throw new IllegalStateException("Premature end of name/mail in " + cs);
	
		// check if have any leftovers...
		if ((startPos < csLen) && (startCh > ' '))
		{
			final T	apv=extractAddressPair(cs, startPos, csLen, apClass);
			if (null == ap)
				ap = new LinkedList<T>();
			ap.add(apv);
		}
	
		return ap;
	}
	/**
	 * Extracts a list of address pairs delimited by either ';' or ','
	 * @param <T> The {@link AddressPair} generic type
	 * @param cs address pairs delimited by either ';' or ',' - may be null/empty
	 * @param apClass {@link Class} instance of the {@link AddressPair}
	 * derived object actually to be initialized. <B>Note:</B> must have a
	 * public default no-arguments constructor
	 * @return {@link Collection} of {@link AddressPair} derived objects - may be
	 * null/empty if null/empty input. Note: if mail address missing/not found
	 * then a dummy one is generated
	 * @throws Exception if parsing/instantiation error
	 */
	public static final <T extends AddressPair> Collection<T> extractAddressPairs (final CharSequence cs, final Class<T> apClass) throws Exception
	{
		return extractAddressPairs(cs, 0, (null == cs) ? 0 : cs.length(), apClass);
	}
	/**
	 * Extracts a delimited display name from the address pair string
	 * @param addressee addressee whose display name is to be set
	 * @param ps parsing object
	 * @param startPos position in parse object where end delimiter should be
	 * looked for (inclusive)
	 * @param delimChar delimiter to be used (usually quote or double-quote)
	 * @return index of first not empty char AFTER the delimited name:
	 * <0 if error, >= maxIndex if had to use a dummy e-mail address
	 */
	protected static final int setDelimitedDisplayName (final AddressPair addressee, final ParsableString ps, final int startPos, final char delimChar)
	{
		final int maxIndex=(null == ps) ? Integer.MIN_VALUE : ps.getMaxIndex();
		if ((null == addressee) || (startPos < 0) || (startPos >= maxIndex))
			return (-1);
	
		// find end of display name
		int	lastIndex=ps.indexOf(delimChar, startPos);
		while (true)
		{
			if ((lastIndex < startPos) || (lastIndex >= maxIndex))
				return (-2);
	
			// check if escaped double quote or the result of an already escaped replacement
			if ('\\' != ps.getCharAt(lastIndex - 1))
			{
				// check NEXT char - if not empty/e-mail address then assume malformed display name
				final char	nch=(lastIndex < (maxIndex-1)) ? ps.getCharAt(lastIndex+1) : '\0';
				if ((nch <= ' ') || (RFCMimeDefinitions.MAIL_ADDR_START_DELIM == nch))
					break;
			}
	
			lastIndex = ps.indexOf(delimChar, lastIndex+1);
		}
	
		final String	name=ps.substring(startPos, lastIndex);
		if ((name != null) && (name.length() > 0))
			addressee.setDisplayName(RFCHeaderDefinitions.decodeHdrValue(name.trim(), true));
	
		final int	nextIndex=ps.findNonEmptyDataStart(lastIndex+1);
		// if no more data (i.e., no e-mail) then set a dummy one
		if ((nextIndex <= lastIndex) || (nextIndex >= maxIndex))
		{
			addressee.setEmailAddress(RFCMimeDefinitions.NonMailMailAddress);
			return maxIndex;
		}
	
		return nextIndex;
	}

	private static final StringBuilder getAddressPairsListWorkBuf (final int numPairs)
	{
		return new StringBuilder(Math.max(1,numPairs) * 128);
	}
	/**
	 * Builds a list of address pairs
	 * @param pairDelim delimiter to be used between successive pairs - Note:
	 * not validated in any way
	 * @param useFolding if TRUE, then each pair is placed in a separate "line"
	 * except for the first one. This mode is intended for RFC822 formatting
	 * @param apList A {@link Collection} of address pairs - if null/empty/no entries
	 * then null is returned from this function
	 * @return created string - may be null if no pairs
	 * @throws IllegalStateException if unable to append pairs for any reason (e.g.
	 * bad/illegal e-mail address)
	 */
	public static final String buildAddressPairsList (final char pairDelim, final boolean useFolding, final Collection<? extends AddressPair> apList) throws IllegalStateException
	{
	    final int	numPairs=(null == apList) ? 0 : apList.size();
	    if (numPairs <= 0)
	        return null;
	
	    final StringBuilder	sb=getAddressPairsListWorkBuf(numPairs);
	    for (final AddressPair  ap : apList)
	    {
	        if (null == ap)	// should not happen
	            continue;
	
	        // "close" previous pair
	        if (sb.length() > 0)
	        {
	            sb.append(pairDelim);
	
	            // prepare for this pair folding
	            if (useFolding)
	                sb.append(EOLStyle.CRLF.getStyleChars())
	                  .append('\t')
	                  ;
	        }
	
	        try
			{
				EmailAddressHelper.appendAddressPair(sb, ap.getDisplayName(), ap.getEmailAddress());
			}
			catch(IOException e)	// should not happen
			{
				throw new RuntimeException(e);
			}
	    }
	
	    if ((null == sb) || (sb.length() <= 0))
	        return null;	// nothing appended
	
	    return sb.toString();
	}
	/**
	 * Builds a list of address pairs
	 * @param pairDelim delimiter to be used between successive pairs - Note:
	 * not validated in any way
	 * @param useFolding if TRUE, then each pair is placed in a separate "line"
	 * except for the first one. This mode is intended for RFC822 formatting
	 * @param apArray address pairs array - if null/empty/no entries then null
	 * is returned from this function
	 * @return created string - may be null if no pairs
	 * @throws IllegalStateException if unable to append pairs for any reason (e.g.
	 * bad/illegal e-mail address)
	 */
	public static final String buildAddressPairsList (final char pairDelim, final boolean useFolding, final AddressPair ... apArray) throws IllegalStateException
	{
	    final int	numPairs=(null == apArray) ? 0 : apArray.length;
	    if (numPairs <= 0)
	        return null;

	    return buildAddressPairsList(pairDelim, useFolding, Arrays.asList(apArray));
	}
	/**
	 * Builds a list of address pairs
	 * @param apArray A {@link Collection} of address pairs - if null/empty/no
	 * entries then null is returned from this function
	 * @return created string - may be null if no pairs
	 * @throws IllegalStateException if unable to append pairs for any reason (e.g.
	 * bad/illegal e-mail address)
	 */
	public static final String buildAddressPairsList (final Collection<? extends AddressPair> apArray) throws IllegalStateException
	{
		return buildAddressPairsList(';', true, apArray);
	}
	/**
	 * Builds a list of address pairs
	 * @param apArray address pairs array - if null/empty/no entries then null
	 * is returned from this function
	 * @return created string - may be null if no pairs
	 * @throws IllegalStateException if unable to append pairs for any reason (e.g.
	 * bad/illegal e-mail address)
	 */
	public static final String buildAddressPairsList (final AddressPair ... apArray) throws IllegalStateException
	{
		return buildAddressPairsList(';', true, apArray);
	}
	/**
	 * Builds and appends an address-pair string as per RFC822 specifications
	 * @param <A> The {@link Appendable} generic type
	 * @param sb The {@link Appendable} object to be used for appending - may NOT be null
	 * @param ap {@link AddressPair} instance - ignored if null/empty
	 * @return same as input {@link Appendable} instance
	 * @throws IOException if cannot append data (which never for {@link StringBuilder}
	 * and/or {@link StringBuffer})
	 */
	public static final <A extends Appendable> A appendAddressPair (final A sb, final AddressPair ap) throws IOException
	{
		if (null == sb)
			throw new IOException(ClassUtil.getArgumentsExceptionLocation(AddressPair.class, "appendAddressPair", ap) + " no " + Appendable.class.getName() + " instance provided");

		if ((null == ap) || ap.isEmpty())
			return sb;

		return EmailAddressHelper.appendAddressPair(sb, ap.getDisplayName(), ap.getEmailAddress());
	}
	/**
	 * Creates an instance of a {@link AddressPair} derived object
	 * initializing its name/address fields
	 * @param <T> The {@link AddressPair} generic type
	 * @param name display name - may be null/empty
	 * @param addr e-mail address - may be null/empty
	 * @param apClass {@link Class} instance of the {@link AddressPair}
	 * derived object actually to be initialized. <B>Note:</B> must have a
	 * public default no-arguments constructor
	 * @return initialized instance
	 * @throws Exception unable to instantiate
	 */
	public static final <T extends AddressPair> T createAddressPair (final String name, final String addr, final Class<T> apClass) throws Exception
	{
		final T ap=apClass.newInstance();
		ap.setDisplayName(name);
		ap.setEmailAddress(addr);
		return ap;
	}
}
