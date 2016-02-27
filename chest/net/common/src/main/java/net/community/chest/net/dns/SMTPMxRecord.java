package net.community.chest.net.dns;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.naming.NamingException;

import net.community.chest.CoVariantReturn;
import net.community.chest.ParsableString;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Class that holds SMTP MX DNS record(s) information
 * 
 * @author Lyor G.
 * @since Jun 28, 2007 2:24:28 PM
 */
public class SMTPMxRecord implements Comparable<SMTPMxRecord>, PubliclyCloneable<SMTPMxRecord>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 300933727741412782L;
	/**
	 * Default constructor - empty record
	 */
	public SMTPMxRecord ()
	{
		super();
	}

	private int 	_preference=Integer.MIN_VALUE;
	private String	_mxHost /* =null */;
	/**
	 * Initialized constructor
	 * @param mxHost MX host string (may be null/empty)
	 * @param preference MX preference value (may be negative) 
	 */
	public SMTPMxRecord (String mxHost, int preference)
	{
		_mxHost = mxHost;
		_preference = preference;
	}
	/**
	 * Copy constructor
	 * @param mxRec original record to copy from (may be null)
	 */
	public SMTPMxRecord (SMTPMxRecord mxRec)
	{
		this((null == mxRec) ? null : mxRec.getHost(), (null == mxRec) ? Integer.MIN_VALUE : mxRec.getPreference());
	}
	/**
	 * @return TRUE if contains valid information (non-empty host AND non-negative preference)
	 */
	public boolean isValid ()
	{
		return (_preference >= 0) && (_mxHost != null) && (_mxHost.length() > 0);
	}
	/**
	 * @return MX preference value (usually <0 if not initialized)
	 */
	public int getPreference ()
	{
		return _preference;
	}
	/**
	 * @param preference MX preference value (may be negative) 
	 */
	public void setPreference (int preference)
	{
		_preference = preference;
	}
	/**
	 * @return MX host string (may be null/empty)
	 */
	public String getHost ()
	{
		return _mxHost;
	}
	/**
	 * @param mxHost MX host string (may be null/empty)
	 */
	public void setHost (String mxHost)
	{
		_mxHost = mxHost;
	}
	/**
	 * Invalidates the contents
	 */
	public void reset ()
	{
		_mxHost = null;
		_preference = Integer.MIN_VALUE;
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public SMTPMxRecord clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @return a negative integer, zero, or a positive integer if this record
	 * has less than, equal to, or greater preference than the specified object.
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo (final SMTPMxRecord mxRec)
	{
		if (null == mxRec)
			return Integer.MAX_VALUE;
		if (this == mxRec)
			return 0;

		final boolean	mxValid=mxRec.isValid();
		if (!isValid())
			return mxValid ? (-1) : 0;
		else if (!mxValid)
			return 1;
		
		final int	thisPref=getPreference(), thatPref=mxRec.getPreference();
		return (thisPref - thatPref);
	}
	/*
	 * @return TRUE if same preference and host name (case-insensitive).
	 * Note: returns FALSE if compared object is NOT an MX record object
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (final Object o)
	{
		if (!(o instanceof SMTPMxRecord))
			return false;
		if (this == o)
			return true;

		final SMTPMxRecord	mxRec=(SMTPMxRecord) o;
		return (isValid() == mxRec.isValid())
		 	&& (getPreference() != mxRec.getPreference())
		 	&& (0 == StringUtil.compareDataStrings(getHost(), mxRec.getHost(), false))
		 	;
	}
	/*
	 * Same as naming representation - see "fromNamingString"
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return String.valueOf(getPreference()) + " " + getHost() + ".";
	}
	/*
	 * As required by the "equals" override
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return (isValid() ? 1 : 0)
			+ getPreference()
			+ StringUtil.getDataStringHashCode(getHost(), false)
			;
	}
	/**
	 * Parses a naming string result into its components
	 * @param nsVal naming string result - format is "N H" - where N is the
	 * preference value, and H is the fully-qualified-domain-name (with a
	 * possible extra '.' at its end)
	 * @param startPos position in string/sequence to start parsing
	 * @param len number of characters to be parsed
	 * @return MX record - null if unable to parse it
	 */
	public static SMTPMxRecord fromNamingString (CharSequence nsVal, int startPos, int len)
	{
		final int	nsLen=(null == nsVal) ? 0 : nsVal.length();
		if ((nsLen <= 0) || (startPos < 0) || (len <= 0) || ((startPos+len) > nsLen))
			return null; 

		final ParsableString	ps=new ParsableString(nsVal, startPos, len);
		final int				pfStart=ps.findNonEmptyDataStart(),
								pfEnd=ps.findNonEmptyDataEnd(pfStart+1),
								pfMaxIndex=ps.getMaxIndex();
		if ((pfStart < startPos) || (pfStart >= pfMaxIndex) || (pfEnd <= pfStart) || (pfEnd >= pfMaxIndex))
			return null;

		final int	mhStart=ps.findNonEmptyDataStart(pfEnd), mhEnd=ps.findNonEmptyDataEnd(mhStart+1);
		if ((mhStart < pfEnd) || (mhStart >= pfMaxIndex) || (mhEnd <= mhStart))
			return null;

		// strip off the ending '.' (if any)
		final String	mxHost=('.' == ps.getCharAt(mhEnd-1)) ? ps.substring(mhStart, mhEnd-1) : ps.substring(mhStart, mhEnd);
		if ((null == mxHost) || (mxHost.length() <= 0))
			return null;
		
		try
		{
			return new SMTPMxRecord(mxHost, ps.getUnsignedInt(pfStart, pfEnd));
		}
		catch(NumberFormatException ne)
		{
			return null;
		}
	}
	/**
	 * Parses a naming string result into its components
	 * @param nsVal naming string result - format is "N H" - where N is the
	 * preference value, and H is the fully-qualified-domain-name (with a
	 * possible extra '.' at its end)
	 * @return MX record - null if unable to parse it
	 */
	public static SMTPMxRecord fromNamingString (CharSequence nsVal)
	{
		return fromNamingString(nsVal, 0, (null == nsVal) ? 0 : nsVal.length());
	}
	/**
	 * Looks up MX records for the given domain using the supplied directory naming context
	 * @param nsa The {@link DNSAccess} instance to be used to resolve MX records
	 * @param dmName domain name (if null/empty then exception is thrown)
	 * @return MX records sorted by ascending preference (may be null/empty if no records found)
	 * @throws NamingException if internal error or DNS problem
	 */
	public static final List<SMTPMxRecord> mxLookup (final DNSAccess nsa, final String dmName) throws NamingException
	{
		if (null == nsa)
			throw new NamingException("mxLookup(" + dmName + ") no DNS accessor provided");

		final Collection<String>	att=nsa.mxLookup(dmName);
		final int					numAtts=(null == att) ? 0 : att.size();
		if (numAtts <= 0)
			return null;

		List<SMTPMxRecord>	recs=null;
		for (final String a : att)
		{	
			// format is "preference FQDN - including terminating '.')
			final SMTPMxRecord	mxRec=fromNamingString(a);
			if (null == mxRec)
				continue;	// skip malformed strings

			if (null == recs)
				recs = new ArrayList<SMTPMxRecord>(numAtts);
			recs.add(mxRec);
		}

		if ((recs != null) && (recs.size() > 1))
			Collections.sort(recs);

		return recs;
	}
}
