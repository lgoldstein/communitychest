/*
 * 
 */
package net.community.chest.net.snmp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 25, 2009 2:11:32 PM
 */
public class MIBAttributeEntry implements Serializable, PubliclyCloneable<MIBAttributeEntry> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1292521100251688556L;
	/**
	 * MBean attribute name to be used for this entry 
	 */
	private String	_attrName	/* =null */;
	public String getAttrName ()
	{
		return _attrName;
	}

	public void setAttrName (String attrName)
	{
		_attrName = attrName;
	}
	/**
	 * @param attrName initial attribute name
	 * @throws IllegalArgumentException if null/empty attribute name
	 */
	public MIBAttributeEntry (final String attrName) throws IllegalArgumentException
	{
		if ((null == (_attrName=attrName)) || (attrName.length() <= 0))
			throw new IllegalArgumentException("No attribute name supplied");
	}
	/**
	 * Default (empty) constructor
	 */
	public MIBAttributeEntry ()
	{
		this((String) null);
	}
	/**
	 * Entry FULL object identifier
	 */
	private String	_oid	/* =null */;
	public String getOid ()
	{
		return _oid;
	}

	public void setOid (String oid)
	{
		_oid = oid;
	}
	/**
	 * Converts the entry OID to scalar (if not already such)
	 * @return previous OID value - may be null/empty same as current
	 * if current is null/empty or already a scalar OID
	 */
	public String adjustScalarOid ()
	{
		final String	prev=getOid();
		setOid(SNMPProtocol.adjustScalarOID(prev));
		return prev;
	}
	/**
	 * Entry SYNTAX field - uses (dummy) OCTETSTRINGMod for string(s)  
	 */
	private String	_syntax	/* =null */;
	public String getSyntax ()
	{
		return _syntax;
	}

	public void setSyntax (String syntax)
	{
		_syntax = syntax;
	}
	// access types
	/**
	 * Entry access field value - null/empty if not specified
	 */
	private String	_access	/* =null */;
	public String getAccess ()
	{
		return _access;
	}

	public void setAccess (String access)
	{
		_access = access;
	}

	public boolean isReadable ()
	{
		return AttributeAccessType.isReadable(getAccess());
	}

	public boolean isWriteable ()
	{
		return AttributeAccessType.isWriteable(getAccess());
	}
	/**
	 * Entry status field value - null/empty if not specified
	 */
	private String	_status;
	public String getStatus ()
	{
		return _status;
	}

	public void setStatus (String status)
	{
		_status = status;
	}
	/**
	 * Entry sequence index/type - null/empty if non-indexed/sequential entry 
	 */
	private Collection<String>	_indices	/* =null */;
	public Collection<String> getIndices ()
	{
		return _indices;
	}

	public void setIndices (final Collection<String> indices)
	{
		if ((indices != null) && (indices.size() > 0))
			_indices = indices;
		else
			_indices = null;
	}
	/**
	 * Adds an index to the current indices - <B>Note:</B> does not check if
	 * index already specified
	 * @param idx index to be added - ignored if null/empty
	 * @return updated indices - may be null/empty if no original indices and
	 * none added
	 */
	public Collection<String> addIndex (final String idx)
	{
		final Collection<String>	ic=getIndices();
		if ((idx != null) && (idx.length() > 0))
		{
			if (null == ic)
			{
				final Collection<String>	ni=new LinkedList<String>();
				ni.add(idx);
				setIndices(ni);
				return ni;
			}

			ic.add(idx);
		}

		return ic;
	}
	/**
	 * Builds a (@link Map) of the indices where key=index name (case
	 * insensitive) and value is whatever object has been passed as
	 * the <I>val</I> parameter
	 * @param <T> Type of mapped value
	 * @param ic original indices (@link Collection) = may be null/empty
	 * @param val value to be mapped for each index - <B>Caveat:</B> if
	 * null then there will be no way to distinguish between a missing index
	 * and a mapped one when invoking (@link Map#get(Object))
	 * @return (@link Map) instance - null/empty if no indices to begin with
	 */
	public static final <T> Map<String,T> buildIndicesMap (final Collection<String> ic, final T val)
	{
		if ((null == ic) || (ic.size() <= 0))
			return null;

		final Map<String,T>	icm=new TreeMap<String, T>(String.CASE_INSENSITIVE_ORDER);
		for (final String i : ic)
		{
			if ((i != null) && (i.length() > 0))	// should not be otherwise
				icm.put(i, val);
		}

		return icm;
	}
	// returns first index in collection not found in map - null if ALL found in map
	private static final String checkIndices (final Collection<String> ic, final Map<String,?> icm)
	{
		if ((null == ic) || (ic.size() <= 0))
			return null;	// if empty list, then all "appear" in map

		for (final String i : ic)
		{
			if ((null == i) || (i.length() <= 0))
				continue;	// should not happen

			final Object	prev=((null == icm) || (icm.size() <= 0)) ? null : icm.get(i);
			if (null == prev)
				return i;
		}

		return null;
	}

	public static final boolean compareIndices (final Collection<String> i1, final Collection<String> i2)
	{
		final int	n1=(null == i1) ? 0 : i1.size(), n2=(null == i2) ? 0 : i2.size();
		if (n1 != n2)	// if not of same size, then obviously not same
			return false;

		final Map<String,Collection<String>>	im1=buildIndicesMap(i1,i1),im2=buildIndicesMap(i2,i2);
		final String 							d1=checkIndices(i2, im1),
												d2=((null == d1) || (d1.length() <= 0))
													? checkIndices(i1, im2)
													: d1;
		return ((null == d1) || (d1.length() <= 0)) && ((null == d2) || (d2.length() <= 0));
	}
	/**
	 * Copy constructor
	 * @param e entry to copy from
	 * @throws IllegalArgumentException if null entry to copy from
	 */
	public MIBAttributeEntry (final MIBAttributeEntry e) throws IllegalArgumentException
	{
		if (null == e)
			throw new IllegalArgumentException("No entry to copy-construct from");

		setAttrName(e.getAttrName());
		setOid(e.getOid());
		setSyntax(e.getSyntax());
		setStatus(e.getStatus());
		setAccess(e.getAccess());
		setIndices(e.getIndices());
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof MIBAttributeEntry))
			return false;
		if (this == obj)
			return true;

		final MIBAttributeEntry e=(MIBAttributeEntry) obj;
		return (0 == StringUtil.compareDataStrings(getAttrName(), e.getAttrName(), false))
			&& (0 == StringUtil.compareDataStrings(getOid(), e.getOid(), true))
			&& (0 == StringUtil.compareDataStrings(getSyntax(), e.getSyntax(), false))
			&& (0 == StringUtil.compareDataStrings(getStatus(), e.getStatus(), false))
			&& (0 == StringUtil.compareDataStrings(getAccess(), e.getAccess(), false))
			&& compareIndices(getIndices(), e.getIndices())
			;
	}

	private static final int getIndicesHashCode (final Collection<String> ic)
	{
		if ((null == ic) || (ic.size() <= 0))
			return 0;

		int	nRes=0;
		for (final String i : ic)
			nRes += StringUtil.getDataStringHashCode(i, false);

		return nRes;
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return StringUtil.getDataStringHashCode(getAttrName(), true)
			 + StringUtil.getDataStringHashCode(getOid(), false)
			 + StringUtil.getDataStringHashCode(getSyntax(), false)
			 + StringUtil.getDataStringHashCode(getStatus(), false)
			 + StringUtil.getDataStringHashCode(getAccess(), false)
			 + getIndicesHashCode(getIndices())
			 ;
	}

	public static final String buildIndicesNamesList (final Collection<String> ic)
	{
		final int	numIdx=(null == ic) ? 0 : ic.size();
		if (numIdx <= 0)
			return null;

		StringBuilder	sb=null;
		for (final String i : ic)
		{
			if ((null == i) || (i.length() <= 0))
				continue;

			// use first creation to generate start bracket
			if (null == sb)
			{
				sb = new StringBuilder(4 + numIdx * 16);
				sb.append("{ ");
			}

			sb.append(i);
			sb.append(' ');	// separate from next entry (or from end bracket)
		}

		if ((null == sb) || (sb.length() <= 0))
			return null;

		// terminate list
		sb.append('}');
		return sb.toString();
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		return getOid()
			+ "[" + getAttrName() + "]"
			+ "(" + getAccess() + ")"
			+ "::" + getSyntax()
			+ "=" + getStatus()
			+ "@" + buildIndicesNamesList(getIndices())
			;
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	public MIBAttributeEntry clone () throws CloneNotSupportedException
	{
		final MIBAttributeEntry		ret=getClass().cast(super.clone());
		final Collection<String>	idl=ret.getIndices();
		if (idl != null)
			ret.setIndices(new ArrayList<String>(idl));
		return ret;
	}
	/**
	 * Used in MBean/reflection API to strip away any common prefix string
	 * used in the MIB entries declarations to retrieve the "pure" name
	 * @param anPrefix common prefix (case insensitive) - if null/empty then
	 * no such prefix was used/found
	 * @param attrName original attribute name
	 * @return stripped attribute name if prefix available and same as the
	 * specified one (case insensitive). Original name - otherwise
	 */
	public static final String getEffectiveAttributeName (final String anPrefix, final String attrName)
	{
		final int	apLen=(null == anPrefix) ? 0 : anPrefix.length(),
					nmLen=(null == attrName) ? 0 : attrName.length();
		if ((apLen >= nmLen) /* means no way can be prefix */ || (apLen <= 0) || (nmLen <= 0))
			return attrName;
	
		final String	nmPrefix=attrName.substring(0, apLen);
		if (nmPrefix.equalsIgnoreCase(anPrefix))
			return attrName.substring(apLen);
	
		return attrName;
	}
	/**
	 * @param entries entries collection - may be null/empty
	 * @return longest OID that is prefix of <U>all</U> OID(s) in the
	 * supplied collection. Null/empty if no entries or no common prefix
	 */
	public static final String findBaseOid (final Collection<? extends MIBAttributeEntry> entries)
	{
		String	baseOid=null;

		if ((entries != null) && (entries.size() > 0))
		{
			for (final MIBAttributeEntry e : entries)
			{
				final String	oid=(null == e) /* should not happen */ ? null : e.getOid();
				if ((null == oid) || (oid.length() <= 0))
					continue;	// should not happen

				// if first OID then assume it is the common prefix
				if (null == baseOid)
				{
					baseOid = oid;
					continue;
				}

				// if current base is already a prefix, then skip this OID
				if (oid.startsWith(baseOid))
					continue;

				if ((null == (baseOid=SNMPProtocol.getCommonOidPrefix(baseOid, oid)))
				 || (baseOid.length() <= 0))
					return baseOid;	// stop if no sub-common OID prefix found
			}
		}

		return baseOid;
	}
	// returns longest matching prefix (case-insensitive) - null/empty if no such prefix
	private static final String compareAttrNamePrefix (final String n1, final String n2)
	{
		final int	l1=(null == n1) ? 0 : n1.length(),
					l2=(null == n2) ? 0 : n2.length(),
					len=Math.min(l1, l2);
		if (len <= 0)	// if one of them is null/empty then no common prefix possible
			return null;

		for (int	nIndex=0; nIndex < len; nIndex++)
		{
			final char	c1=n1.charAt(nIndex), c2=n2.charAt(nIndex);
			if (c1 == c2)	// many times same case is used in names prefixes
				continue;

			// test for case insensitive match (usually shows a difference)
			final char	lc1=Character.toLowerCase(c1), lc2=Character.toLowerCase(c2);
			if (lc1 != lc2)
				return (nIndex <= 0) ? null : n1.substring(0, nIndex);
		}

		// at this point we known that the shorter one is prefix of longer one
		if (l1 <= l2)
			return n1;
		else
			return n2;
	}
	/**
	 * Finds longest common prefix string for the supplied entries
	 * <U>names</U> - case <U>insensitive</U>. Usually MIB entries names
	 * have a common "prefix" string (e.g., <I><B>vm</B>PortNumber</I>,
	 * <I><B>vm</B>NumTrunks</I>)
	 * @param entries attribute entries - may be null/empty
	 * @return longest common match - null/empty if no common prefix (case
	 * insensitive) found.
	 */
	public static final String findCommonNamePrefix (final Collection<? extends MIBAttributeEntry>	entries)
	{
		if ((null == entries) || (entries.size() <= 0))
			return null;

		String	anPrefix=null;
		for (final MIBAttributeEntry e : entries)
		{
			final String	eName=(null == e) /* should not happen */ ? null : e.getAttrName();
			if (null == anPrefix)	// if none set, then use first name
			{
				anPrefix = eName;
				continue;
			}
				
			final String	resVal=compareAttrNamePrefix(anPrefix, eName);
			if (!anPrefix.equalsIgnoreCase(resVal))
			{
				if ((null == (anPrefix=resVal)) || (anPrefix.length() <= 0))
					return anPrefix;
			}
		}

		return anPrefix;
	}

	public static final MIBAttributeEntry getEntryByName (final Collection<? extends MIBAttributeEntry> entries, final String eName)
	{
		if ((null == entries) || (entries.size() <= 0)
		 || (null == eName) || (eName.length() <= 0))
			return null;

		for (final MIBAttributeEntry e : entries)
		{
			final String	aName=(null == e) /* should not happen */ ? null : e.getAttrName();
			if (eName.equalsIgnoreCase(aName))
				return e;
		}

		return null;
	}
}
