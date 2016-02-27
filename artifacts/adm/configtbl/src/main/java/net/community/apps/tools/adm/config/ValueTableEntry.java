/*
 * 
 */
package net.community.apps.tools.adm.config;

import net.community.chest.lang.StringUtil;
import net.community.chest.util.map.entries.StringPairEntry;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 15, 2009 11:32:04 AM
 */
public class ValueTableEntry extends StringPairEntry {
	private String	_originalValue;
	public String getOriginalValue ()
	{
		return _originalValue;
	}

	public void setOriginalValue (String originalValue)
	{
		_originalValue = originalValue;
	}
	// NOTE !!! comparison is case sensitive
	public boolean isChangedEntry ()
	{
		return (StringUtil.compareDataStrings(getValue(), getOriginalValue(), true) != 0);
	}

	public ValueTableEntry (String k, String v, String o)
	{
		super(k, v);
		_originalValue = o;
	}
	
	public ValueTableEntry (String k, String v)
	{
		this(k, v, v);
	}
	
	public ValueTableEntry (String k)
	{
		this(k, null);
	}
	
	public ValueTableEntry ()
	{
		this(null);
	}
	
	public String getName ()
	{
		return getKey();
	}
	
	public void setName (String n)
	{
		setKey(n);
	}
}
