package net.community.chest.mail.headers;

import java.io.IOException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.CoVariantReturn;
import net.community.chest.ParsableString;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;
import net.community.chest.mail.RFCMimeDefinitions;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 22, 2007 9:33:50 AM
 */
public class EncodedHeaderSection implements Serializable, PubliclyCloneable<EncodedHeaderSection> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7643126746803297526L;
	/**
	 * Original header value from which this section has been extracted
	 */
	private String	_hdrValue	/* =null */;
	public String getHdrValue ()
	{
		return _hdrValue;
	}
	public void setHdrValue (String hdrValue)
	{
		_hdrValue = hdrValue;
	}
	/**
	 * Zero based index in original header value where this section starts
	 * (negative if not initialized) - inclusive
	 */
	private int	_startIndex=(-1);
	public int getStartIndex ()
	{
		return _startIndex;
	}

	public void setStartIndex (int startIndex)
	{
		_startIndex = startIndex;
	}
	/**
	 * Zero based index in original header value where this section ends
	 * (negative if not initialized) - exclusive
	 */
	private int	_endIndex=(-1);
	public int getEndIndex ()
	{
		return _endIndex;
	}
	public void setEndIndex (int endIndex)
	{
		_endIndex = endIndex;
	}
	/**
	 * Extract section data
	 */
	private String	_sectionData	/* =null */;
	public String getSectionData ()
	{
		return _sectionData;
	}

	public void setSectionData (String sectionData)
	{
		_sectionData = sectionData;
	}
	/**
	 * Extracted charset name 
	 */
	private String	_charsetName	/* =null */;
	public String getCharsetName ()
	{
		return _charsetName;
	}

	public void setCharsetName (String charsetName)
	{
		_charsetName = charsetName;
	}
	/**
	 * Character specifying which encoding to use - (Q)uoted-printable/(B)ASE64 
	 */
	private char	_encodeChar	/* ='\0' */;
	public char getEncodeChar ()
	{
		return _encodeChar;
	}

	public void setEncodeChar (char encodeChar)
	{
		_encodeChar = encodeChar;
	}
	/**
	 * Resets current contents - ready for re-use 
	 */
	public void reset ()
	{
		setHdrValue(null);
		setStartIndex(-1);
		setEndIndex(-1);
		setCharsetName(null);
		setSectionData(null);
		setEncodeChar('\0');
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public EncodedHeaderSection clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if ((null == obj) || (!(obj instanceof EncodedHeaderSection)))
			return false;
		if (this == obj)
			return true;

		final EncodedHeaderSection	hs=(EncodedHeaderSection) obj;
		return (hs.getStartIndex() == getStartIndex())
			&& (hs.getEndIndex() == getEndIndex())
			&& (hs.getEncodeChar() == getEncodeChar())
			&& (0 == StringUtil.compareDataStrings(hs.getCharsetName(), getCharsetName(), false))
			&& (0 == StringUtil.compareDataStrings(hs.getHdrValue(), getHdrValue(), true))
			&& (0 == StringUtil.compareDataStrings(hs.getSectionData(), getSectionData(), true))
			;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return getStartIndex()
			 + getEndIndex()
			 + getEncodeChar()
			 + StringUtil.getDataStringHashCode(getHdrValue(), true)
			 + StringUtil.getDataStringHashCode(getSectionData(), true)
			 + StringUtil.getDataStringHashCode(getCharsetName(), false)
			 ;
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return "=?" + getCharsetName()
			 + "?" + String.valueOf(getEncodeChar())
			 + "?" + getHdrValue()
			 + "?="
			 ;
	}
	/**
	 * @param hdrValue header that might contain encoded sub-sections
	 * @return {@link Collection} of all encoded section in the order they
	 * were encountered - null/empty if no encoded sections found
	 */
	public static final Collection<EncodedHeaderSection> getEncodedSections (final String hdrValue)
	{
    	final int nLen=((null == hdrValue) ? 0 : hdrValue.length());
   	 	// make sure at least minimum length of data available
    	if (nLen <= RFCMessageHeaders.MIN_HDRENC_LEN)
    		return null;

    	Collection<EncodedHeaderSection>	secs=null;	// lazy allocated
    	for (int    nIndex=0, nPos=hdrValue.indexOf(RFCMessageHeaders.HDRENCPREFIX, nIndex); nPos >= 0; nPos=hdrValue.indexOf(RFCMessageHeaders.HDRENCPREFIX, nIndex))
    	{
    		// check if this is a "=?charset?Q/B?xxxxxx?=" encoding
    		final int nCharsetPos=(nPos + RFCMessageHeaders.HDRENCPREFIX.length()), nRemLen=(nLen - nCharsetPos);
    		if (nRemLen <= RFCMessageHeaders.MIN_HDRENC_LEN)   // obviously, if not enough data left, then no encoding can take place
    			break;

    		// find end of charset by looking for the '?' delimiter
    		for (nIndex=nCharsetPos ; nIndex < nLen; nIndex++)
    			if (RFCMessageHeaders.HDRENCDATADELIMITER == hdrValue.charAt(nIndex))
    				break;

    		// check if stopped because found charset end delimiter (and still have enough space for the encoding character + '?')
    		if (nIndex > (nLen - 1 - 1 - RFCMessageHeaders.HDRENCSUFFIX.length()))
    			break;

    		final int nCharsetLen=(nIndex - nCharsetPos);
    		if (0 == nCharsetLen)   // if no charset, then continue searching the pattern
    		{
    			nIndex--;   // we stopped because of '?', but it may be a "=?"
    			continue;
    		}

    		final char    chEnc=Character.toUpperCase(hdrValue.charAt(nIndex+1)), chNext=hdrValue.charAt(nIndex+2);
    		if ((chNext != RFCMessageHeaders.HDRENCDATADELIMITER) || ((chEnc != RFCMessageHeaders.QP_HDRENC_CHAR) && (chEnc != RFCMessageHeaders.BASE64_HDRENC_CHAR)))
    		{
    			nIndex--;   // we stopped because of '?', but it may be a "=?"
    			continue;
    		}

    		int nEncStart=(nIndex + 3);
    		// find end of encoded section by looking for the '?' (and '=' after that)
    		for (nIndex=nEncStart; nIndex < nLen; nIndex++)
    			if (RFCMessageHeaders.HDRENCDATADELIMITER == hdrValue.charAt(nIndex))
    				break;

    		// if not found, then no need to look further since we know that we had a "=?charset?Q/B?" prefix
    		if (nIndex >= (nLen-1))
    			break;

    		if (hdrValue.charAt(nIndex + 1) != '=')
    		{
    			nIndex--;   // we stopped because of '?', but it may be a "=?"
    			continue;
    		}

			final String	cn=hdrValue.substring(nCharsetPos, nCharsetPos + nCharsetLen),
    						sd=hdrValue.substring(nEncStart, nIndex);
			final int		nLastPos=nIndex + RFCMessageHeaders.HDRENCSUFFIX.length();

			{
				final EncodedHeaderSection	hs=new EncodedHeaderSection();
				hs.setHdrValue(hdrValue);
				hs.setStartIndex(nPos);
				hs.setEndIndex(nLastPos);
				hs.setCharsetName(cn);
				hs.setSectionData(sd);
				hs.setEncodeChar(chEnc);

				if (null == secs)
					secs = new LinkedList<EncodedHeaderSection>();
				secs.add(hs);
			}

		    // skip to next section (if any)
			if ((nIndex=nLastPos) >= (nLen-RFCMessageHeaders.MIN_HDRENC_LEN))
    			break;
    	}

    	return secs;
	}
	/**
	 * Extracts all attribute/value pairs from the given string.
	 * @param listValue string containing the attribute/value pairs - may
	 * be null/empty, in which case the initial map object is returned.
	 * @param startPos start position in sequence to start looking
	 * @param listLength number of characters to be parsed
	 * @param listDelim delimiter used between each pair
	 * @param initialMap if non-null, then attributes are added to it,
	 * otherwise a new map is allocated
	 * @return map of attributes - key=attribute name, value=attribute value.
	 * <B>Note:</B> if an attribute has no value, then a null/empty value will
	 * be assigned to it.
	 * @throws IllegalStateException if bad format or same attribute appears
	 * more than once (case insensitive name check)
	 */
	public static final Map<String,String> getAttributesList (final Map<String,String>	initialMap,
											   				  final CharSequence		listValue,
											   				  final int					startPos,
											   				  final int					listLength,
											   				  final char				listDelim)
	{
		if (listLength <= 0)
			return initialMap;

		Map<String,String>	attrsMap=initialMap;	// lazy allocation
		for (int	curPos=ParsableString.findNonEmptyDataStart(listValue, startPos, listLength); (curPos >= 0) && (curPos < listLength); curPos=ParsableString.findNonEmptyDataStart(listValue, curPos+1, listLength))
		{
			CharSequence	attrName=null;
			{
				int	nameStart=curPos;
				for ( ; curPos < listLength; curPos++)
				{
					final char	chPos=listValue.charAt(curPos);
					if ((chPos == listDelim) || (chPos == RFCMimeDefinitions.RFC822_KEYWORD_VALUE_DELIM) || ParsableString.isEmptyChar(chPos))
						break;
				}
				/* This point is reached because one of the following holds:
				 * 
				 * 		1. found attribute pair delimiter
				 * 	or
				 * 		2. found attribute value delimiter
				 * 	or
				 * 		3. found "whitespace" character
				 * 	or
				 * 		4. reached end of string
				 * 
				 * either one of these conditions is enough to delimit the attribute name
				 */
				if ((null == (attrName=listValue.subSequence(nameStart, curPos))) || (attrName.length() <= 0))
					throw new IllegalStateException("No attribute name at position=" + nameStart);	// should not happen
			}

			// make sure we are positioned at start of value (or end of name - if no value)
			for ( ; curPos < listLength; curPos++)
			{	
				final char	chPos=listValue.charAt(curPos);
				if ((chPos == listDelim) || (chPos == RFCMimeDefinitions.RFC822_KEYWORD_VALUE_DELIM))
					break;

				// allow only "whitespace" between the name and the value (if any)
				if (!ParsableString.isEmptyChar(chPos))
					throw new IllegalStateException("No white space after attribute name=" + attrName);
			}
			/* at this point the following holds
			 *
			 *		1. found start of value delimiter
			 *	or
			 *		2. found end of attribute pair delimiter
			 *	or
			 *		3. reached end of string (same as (2)).
			 */
			CharSequence	attrValue=null;
			// check if have a value delimiter
			if ((curPos < listLength) && (RFCMimeDefinitions.RFC822_KEYWORD_VALUE_DELIM == listValue.charAt(curPos)))
			{
				// allow empty space between the attribute name, the '=' delimiter and the value
				int	valueStart=ParsableString.findNonEmptyDataStart(listValue, curPos+1, listLength);
				if ((valueStart <= curPos) || (valueStart >= listLength))
					throw new IllegalStateException("No value follows attribute=" + attrName);

				// not allowed to end pair without a value (even an empty one)
				final char	chStart=listValue.charAt(valueStart);
				if ((listDelim == chStart) || (RFCMimeDefinitions.RFC822_KEYWORD_VALUE_DELIM == chStart))
					throw new IllegalStateException("Premature end of value for attribute=" + attrName);

				if (('\'' == chStart) || ('\"' == chStart))
				{
					valueStart++;

					// find end of delimiting (provided it is not escaped)
					for (curPos=ParsableString.indexOf(listValue, chStart, valueStart); (curPos > valueStart) && (curPos < listLength); curPos=ParsableString.indexOf(listValue, chStart, curPos+1))
					{
						// check if escaped delimiter
						if ((curPos > 0) && ('\\' == listValue.charAt(curPos-1)))
							continue;

						// skip (malformed) duplicate delimiter
						if ((curPos < (listLength-1)) && (listValue.charAt(curPos+1) == chStart))
							continue;

						break;
					}

					// make sure we found the delimiter
					if ((curPos < valueStart) || (curPos >= listLength))
						throw new IllegalStateException("Unmatched delimited value for attribute=" + attrName);
				}
				else	// non-delimited value
				{
					for (curPos=valueStart+1; curPos < listLength; curPos++)
					{
						final char	chPos=listValue.charAt(curPos);
						// stop at either first whitespace or list delimiter
						if (ParsableString.isEmptyChar(chPos) || (chPos == listDelim))
							break;
					}
				}

				if (null == (attrValue=listValue.subSequence(valueStart, curPos)))	// should not happen
					throw new IllegalStateException("Failed to extract value for attribute=" + attrName);

				if (('\'' == chStart) || ('\"' == chStart))
					curPos++;	// prepare for next pair (if any) 
			}

			final int	vLen=(null == attrValue) ? 0 : attrValue.length();
			if (null == attrsMap)
				attrsMap = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
			attrsMap.put(attrName.toString(), (vLen <= 0) ? "" : attrValue.toString());

			// skip till list end delimiter
			for ( ; curPos < listLength; curPos++)
			{
				final char	chPos=listValue.charAt(curPos);
				// stop at list delimiter
				if (chPos == listDelim)
					break;

				// allow only white space till list delimiter
				if (!ParsableString.isEmptyChar(chPos))
					throw new IllegalStateException("Non-whitespace after attribute=" + attrName);
			}
		}

		return attrsMap;
	}
	/**
	 * Extracts all attribute/value pairs from the given string.
	 * @param listValue string containing the attribute/value pairs - may
	 * be null/empty, in which case a null/empty map is returned.
	 * @param listDelim delimiter used between each pair
	 * @param initialMap if non-null, then attributes are added to it,
	 * otherwise a new map is allocated
	 * @return map of attributes - key=attribute name, value=attribute value.
	 * <B>Note:</B> if an attribute has no value, then a null/empty value will
	 * be assigned to it.
	 * @throws IllegalStateException if bad format or same attribute appears
	 * more than once (case insensitive name check)
	 */
	public static final Map<String,String> getAttributesList (final Map<String,String> initialMap, final CharSequence listValue, final char listDelim)
	{
		return getAttributesList(initialMap, listValue, 0, (null == listValue) ? 0 : listValue.length(), listDelim);
	}
	/**
	 * Extracts all attribute/value pairs from the given string using the {@link RFCMimeDefinitions#RFC822_ATTRS_LIST_DELIM} delimiter
	 * @param listValue string containing the attribute/value pairs - may
	 * be null/empty, in which case a null/empty map is returned.
	 * @param initialMap if non-null, then attributes are added to it,
	 * otherwise a new map is allocated
	 * @return map of attributes - key=attribute name, value=attribute value.
	 * <B>Note:</B> if an attribute has no value, then a null/empty value will
	 * be assigned to it.
	 * @throws IllegalStateException if bad format or same attribute appears
	 * more than once (case insensitive name check)
	 */
	public static final Map<String,String> getAttributesList (final Map<String,String> initialMap, final CharSequence listValue)
	{
		return getAttributesList(initialMap, listValue, 0, (null == listValue) ? 0 : listValue.length(), RFCMimeDefinitions.RFC822_ATTRS_LIST_DELIM);
	}
	/**
	 * Extracts all attribute/value pairs from the given string.
	 * @param listValue string containing the attribute/value pairs - may
	 * be null/empty, in which case a null/empty map is returned.
	 * @param listDelim delimiter used between each pair
	 * @return map of attributes - key=attribute name, value=attribute value.
	 * <B>Note:</B> if an attribute has no value, then a null/empty value will
	 * be assigned to it.
	 * @throws IllegalStateException if bad format or same attribute appears
	 * more than once (case insensitive name check)
	 * @see #getAttributesList(Map initialMap, CharSequence listValue, char listDelim)
	 */
	public static final Map<String,String> getAttributesList (final CharSequence listValue, final char listDelim)
	{
		return getAttributesList(null, listValue, listDelim);
	}
	/**
	 * Extracts all attribute/value pairs from the given string.
	 * @param listValue string containing the attribute/value pairs - may
	 * be null/empty, in which case a null/empty map is returned.
	 * @param startPos start position in sequence to start looking
	 * @param listLength number of characters to be parsed
	 * @return map of attributes - key=attribute name, value=attribute value.
	 * <B>Note:</B> if an attribute has no value, then a null/empty value will
	 * be assigned to it.
	 * @throws IllegalStateException if bad format or same attribute appears
	 * more than once (case insensitive name check)
	 */
	public static final Map<String,String> getAttributesList (final CharSequence	listValue,
											   				  final int				startPos,
											   				  final int				listLength)
	{
		return getAttributesList(null, listValue, startPos, listLength, RFCMimeDefinitions.RFC822_ATTRS_LIST_DELIM);
	}
	/**
	 * Extracts all attribute/value pairs from the given string - assumes
	 * pairs are delimited using the default RFC822 atrributes list delimiter.
	 * @param listValue string containing the attribute/value pairs - may
	 * be null/empty, in which case a null/empty map is returned.
	 * @return map of attributes - key=attribute name, value=attribute value.
	 * <B>Note:</B> if an attribute has no value, then a null/empty value will
	 * be assigned to it.
	 * @throws IllegalStateException if bad format or same attribute appears
	 * more than once (case insensitive name check)
	 * @see #getAttributesList(CharSequence listValue, char listDelim)
	 * @see RFCMimeDefinitions#RFC822_ATTRS_LIST_DELIM
	 */
	public static final Map<String,String> getAttributesList (final CharSequence listValue)
	{
		return getAttributesList(listValue, RFCMimeDefinitions.RFC822_ATTRS_LIST_DELIM);
	}
	/**
	 * @param attrsMap attributes map - if null/empty then nothing is appended
	 * @param sb {@link Appendable} instance to append to
	 * @param delim delimiter to be used (<B>Caveat:</B> not checked so if you
	 * use '\0' then undefined format may be returned)
	 * @return number of added attributes (<0 if error)
	 * @throws IOException if cannot append results to {@link Appendable} instance
	 */
	public static final int buildAttributesList (final Map<String,String> attrsMap, final Appendable sb, final char delim) throws IOException
	{
		int	numAdded=0;
		if (null == sb)	// should not happen
			return Integer.MIN_VALUE;

		final int									numAttrs=(null == attrsMap) ? 0 : attrsMap.size();
		final Collection<Map.Entry<String,String>>	eSet=(numAttrs <= 0) ? null : attrsMap.entrySet();
		if ((eSet != null) && (eSet.size() > 0))
		{
			for (final Map.Entry<String,String> eAttr : eSet)
			{
				final String	attrName=(null == eAttr) /* should not happen */ ? null : eAttr.getKey();
				if ((null == attrName) || (attrName.length() <= 0))
					continue;	// should not happen

				if (numAdded > 0)
					sb.append(delim);
				sb.append(attrName);

				numAdded++;	// added an attribute regardles of whether it has a value

				final String	attrValue=eAttr.getValue();
				if ((null == attrValue) || (attrValue.length() <= 0))
					continue;	// OK to have attributes without values

				sb.append(RFCMimeDefinitions.RFC822_KEYWORD_VALUE_DELIM)
				  .append(attrValue)
				  ;
			}
		}

		return numAdded;
	}
	/**
	 * @param attrsMap attributes map - if null/empty then nothing is appended
	 * @param sb string buffer to append to - <B>Note:</B> uses {@link RFCMimeDefinitions#RFC822_ATTRS_LIST_DELIM}
	 * as its delimiter
	 * @return number of added attributes (<0 if error)
	 * @throws IOException if cannot append attributes or bad parameters
	 * @see #buildAttributesList(Map, Appendable, char)
	 */
	public static final int buildAttributesList (final Map<String,String> attrsMap, final Appendable sb) throws IOException
	{
		return buildAttributesList(attrsMap, sb, RFCMimeDefinitions.RFC822_ATTRS_LIST_DELIM);
	}
	/**
	 * @param attrsMap attributes map - if null/empty then null/empty string returned
	 * @param delim delimiter to be used (<B>Caveat:</B> not checked so if you
	 * use '\0' then undefined format may be returned)
	 * @return attribute[=value] list delimiter by the requested character
	 */
	public static final String buildAttributesList (final Map<String,String> attrsMap, final char delim)
	{
		final int	numAttrs=(null == attrsMap) ? 0 : attrsMap.size();
		if (numAttrs <= 0)
			return null;

		try
		{
			final StringBuilder	sb=new StringBuilder(numAttrs * 64);
			final int		nVals=buildAttributesList(attrsMap, sb, delim);
			if (nVals < 0)	// should not happen
				throw new StreamCorruptedException(ClassUtil.getExceptionLocation(EncodedHeaderSection.class, "buildAttributesList") + " bad number of attributes (" + nVals + ") appended to internal buffer");

			return sb.toString();
		}
		catch(IOException e)
		{
			// should not happen since StringBuilder does not throw exceptions when appended
			throw new RuntimeException(e);
		}
	}
	/**
	 * @param attrsMap attributes map - if null/empty then null/empty string returned
	 * @return attribute[=value] list delimited by {@link RFCMimeDefinitions#RFC822_ATTRS_LIST_DELIM}
	 * @throws IllegalStateException if unable to append to internal string
	 * buffer (should not happen)
	 * @see #buildAttributesList(Map, char)
	 */
	public static final String buildAttributesList (final Map<String,String> attrsMap) throws IllegalStateException
	{
		return buildAttributesList(attrsMap, RFCMimeDefinitions.RFC822_ATTRS_LIST_DELIM);
	}
	/**
	 * Appends the specified property name/value pair to the supplied buffer
	 * @param <A> The {@link Appendable} generic type
	 * @param sb string buffer for appending
	 * @param isFirst TRUE if first appended property
	 * @param propName property name - may NOTE be null/empty
	 * @param propVal property value - may be null/empty
	 * @return same as input buffer
	 * @throws IOException if unable to append
	 */
	public static final <A extends Appendable> A addContentHeaderProperty (final A sb, final boolean isFirst, final String propName, final String propVal) throws IOException
	{
		if ((null == sb) || (null == propName) || (propName.length() <= 0))
			throw new IOException(ClassUtil.getArgumentsExceptionLocation(EncodedHeaderSection.class, "addContentHeaderProperty", propName, propVal) + " incomplete arguments");

		// if this is not the first attribute, then add a separator
		if (!isFirst)
			sb.append(";\r\n\t");
		sb.append(propName);

		// OK if no property value
		if ((null == propVal) || (propVal.length() <= 0))
			return sb;

		sb.append(RFCMimeDefinitions.RFC822_KEYWORD_VALUE_DELIM)
		  .append('\"')
		  .append(propVal)
		  .append('\"')
		  ;
		return sb;
	}
	/**
	 * Builds the specified content header properties according to the supplied attributes map
	 * @param <A> The generic type
	 * @param sb {@link Appendable} instance for creating the header string - may NOT be null
	 * @param isEmptyBuffer TRUE=no previously appended data
	 * @param chProps attributes map - key=property name, value=property value (may be null/empty)
	 * @param nameProp the property used for the part name (requires special handling) - may be null/empty
	 * @param nameVal the value to be used for the part name (if specified) - may be null/empty if no property specified
	 * @return same as input {@link Appendable} instance
	 * @throws IOException if cannot append properties or bad arguments
	 * @see #addContentHeaderProperty(Appendable, boolean, String, String)
	 */
	public static final <A extends Appendable> A buildContentHeaderProperties (final A sb, final boolean isEmptyBuffer, final Map<String,String> chProps, final String nameProp, final String nameVal) throws IOException
	{
		final Collection<Map.Entry<String,String>>	eProps=
			((chProps != null) && (chProps.size() > 0)) ? chProps.entrySet() : null;

		// OK if no properties
		boolean	isFirstProp=isEmptyBuffer;
		if ((eProps != null) && (eProps.size() > 0))
		{
			for (final Map.Entry<String,String> ep : eProps)
			{
				final String propName=(null == ep) /* should not happen */ ? null : ep.getKey();
				if ((null == propName) || (propName.length() <= 0))  // should not happen, but let it slide
					continue;

				// check if this property should be excluded
				if ((nameProp != null) && propName.equalsIgnoreCase(nameProp))
					continue;

				addContentHeaderProperty(sb, isFirstProp, propName, ep.getValue());
				isFirstProp = false;
			}
		}

		if ((nameProp != null) && (nameProp.length() > 0)
		 && (nameVal != null) && (nameVal.length() > 0))
		{
			addContentHeaderProperty(sb, isFirstProp, nameProp, nameVal);
			isFirstProp = false;	// just so we have a debug breakpoint
		}

		return sb;
	}
	/**
	 * Appends a "Content-Type:" header value
	 * @param <A> The {@link Appendable} generic type
	 * @param sb {@link Appendable} to be appended into
	 * @param isEmptyBuffer TRUE=no previously appended data
	 * @param mimeType MIME type (may not be null/empty)
	 * @param mimeSubType MIME sub-type (may not be null/empty)
	 * @param partName if non-null/empty then a "name=xxx" attribute is generated
	 * @param ctProps additional properties (e.g., charset, MIME boundary) - may
	 * be null/empty
	 * @return same as input {@link Appendable} instance
	 * @throws IOException if failed to append data or bad parameters
	 */
	public static final <A extends Appendable> A buildContentTypeHeaderValue (
								final A						sb,
								final boolean				isEmptyBuffer,
								final String				mimeType,
								final String				mimeSubType,
								final String				partName,
								final Map<String,String>	ctProps) throws IOException
	{
		RFCMimeDefinitions.appendMIMETag(sb, mimeType, mimeSubType);

		final int nmLen=((null == partName) ? 0 : partName.length());
		buildContentHeaderProperties(sb, isEmptyBuffer, ctProps, ((nmLen > 0) ? RFCMimeDefinitions.MIMENameKeyword : null), ((nmLen > 0) ? partName : null));
		return sb;
	}
	/**
	 * Appends the "Content-Disposition:" header value
	 * @param <A> The {@link Appendable} generic type
	 * @param sb {@link Appendable} instance to append to
	 * @param isEmptyBuffer TRUE=no previously appended data
	 * @param partName if non-null/empty then a "filename=xxx" attribute is generated
	 * @param cdProps properties (may be null/empty)
	 * @return same as input {@link Appendable} instance
	 * @throws IOException if cannot append data or bad parameters
	 */
	public static final <A extends Appendable> A buildContentDispositionHeaderValue (
						final A						sb,
						final boolean				isEmptyBuffer,
						final String				partName,
						final Map<String,String>	cdProps) throws IOException
	{
		final int nmLen=(null == partName) ? 0 : partName.length();
		buildContentHeaderProperties(sb, isEmptyBuffer, cdProps, (nmLen > 0) ? RFCMimeDefinitions.MIMEFilenameKeyword : null, (nmLen > 0) ? partName : null);
		return sb;
	}
	/**
	 * Strips away any preceding/ending white-space and/or remark(s) 
	 * @param orgID original ID - if null/empty then nothing is done
	 * @return stripped ID - same as input if nothing stripped
	 */
	public static final String stripRFCIDRemark (final String orgID)
	{
		final String	rfcID=(null == orgID) ? null : orgID.trim();
		final int		idLen=(null == rfcID) ? 0 : rfcID.length();
		// if not even "()" or "<>" then do nothing
		if (idLen <= 1)
			return rfcID;

		// OK if start/end with <> (most likely case and quickest to check)
		if (('<' == rfcID.charAt(0)) && ('>' == rfcID.charAt(idLen-1)))
			return rfcID;

		// check if delimited ID
		final int	startIndex=rfcID.indexOf('<');
		if ((startIndex < 0) || (startIndex >= (idLen-1)))
		{
			// if un-delimited ID, then use non-whitespace sequence as ID or up to first '(' - whichever comes first
			final int	startPos=ParsableString.findNonEmptyDataStart(rfcID),
						endPos=ParsableString.findNonEmptyDataEnd(rfcID, startPos+1),
						remPos=((startPos >= 0) && (startPos < idLen)) ? rfcID.indexOf('(', startPos) : Integer.MIN_VALUE;

			if ((startPos < 0) || (startPos >= idLen)) // all white-space ID
				return null;

			// first non-empty char is remark - assume it precedes the real ID
			if (remPos == startPos)
			{
				// try to find end of remark
				final int	remEnd=rfcID.indexOf(')', remPos);
				if ((remEnd <= remPos) || (remEnd >= (idLen-1)))
					return rfcID;	// not cool, but OK

				// remove the starting remark and retry recursively
				return stripRFCIDRemark(rfcID.substring(remEnd + 1));
			}

			// if have a '(' check if it comes before or after the end of the non-empty sequence
			int	lastPos=endPos;
			if (remPos > startPos)
			{
				// if comes before the end of the non-empty sequence, use it
				if (remPos <= endPos)
					lastPos = remPos;
			}

			if (startPos >= lastPos)
				return null;

			return rfcID.substring(startPos, lastPos);
		}
		else	// delimited ID
		{
			final int	endIndex=rfcID.indexOf('>', startIndex);
			if ((endIndex <= 2) || (endIndex >= (idLen-1)) /* should not happen */)
				return rfcID;	// unexpected, but OK

			return rfcID.substring(startIndex, endIndex+1);
		}
	}
}