/*
 * 
 */
package net.community.apps.apache.maven.pomrunner;

import net.community.chest.util.logging.LogLevelWrapper;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 21, 2011 3:14:02 PM
 */
public class OutputEntry extends MapEntryImpl<LogLevelWrapper,String> {
	public OutputEntry (LogLevelWrapper key, String value)
	{
		super(key, value);
	}

	public OutputEntry ()
	{
		this(null, null);
	}

	public LogLevelWrapper getLevel ()
	{
		return getKey();
	}

	public void setLevel (LogLevelWrapper level)
	{
		setKey(level);
	}

	public String getMessage ()
	{
		return getValue();
	}

	public void setMessage (String msg)
	{
		setValue(msg);
	}
}
