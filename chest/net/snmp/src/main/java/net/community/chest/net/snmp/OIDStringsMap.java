package net.community.chest.net.snmp;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Maps objects according their sorted OID(s) values</P>
 * 
 * @param <V> The mapped type value
 * @author Lyor G.
 * @since Oct 18, 2007 12:49:22 PM
 */
public class OIDStringsMap<V> extends TreeMap<String,V> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -186093611408889991L;
	public OIDStringsMap ()
	{
		super(OIDStringsComparator.ASCENDING);
	}
	/**
	 * Translates a {@link Map} where key=alias,value=OID to one where key=OID
	 * and value=alias
	 * @param aliasMap Original {@link Map} - key=alias,value=OID
	 * @return Translated {@link Map} key=OID, value=alias
	 * @throws IllegalStateException if same OID has more than one alias
	 */
	public static final OIDStringsMap<String> toOIDMap (final Map<String,String> aliasMap) throws IllegalStateException
	{
		final Collection<? extends Map.Entry<String,String>>	ol=
			((null == aliasMap) || (aliasMap.size() <= 0)) ? null : aliasMap.entrySet();
		if ((null == ol) || (ol.size() <= 0))
			return null;


		final OIDStringsMap<String>	ret=new OIDStringsMap<String>();
		for (final Map.Entry<String,String> oe : ol)
		{
			final String	alias=(null == oe) ? null : oe.getKey(),
							oid=(null == oe) ? null : oe.getValue();
			if ((null == oid) || (oid.length() <= 0)
			 || (null == alias) || (alias.length() <= 0))
				continue;

			final String	prev=ret.put(oid, alias);
			if (prev != null)
				throw new IllegalStateException("toOIDMap(" + oid + ") has multiple aliases: " + prev + "/" + alias);
		}

		return ret;
	}
}
