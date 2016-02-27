package net.community.chest.apache.log4j;

import org.apache.log4j.Level;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 1, 2007 11:54:15 AM
 */
public class ExtendedLevel extends Level {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5689676334344732540L;
	public ExtendedLevel (int level, String levelStr, int syslogEquivalent)
	{
		super(level, levelStr, syslogEquivalent);
	}

	public ExtendedLevel (Level baseLevel, String name, int offset, int syslogEquivalent)
	{
		this(baseLevel.toInt() + offset, name, syslogEquivalent);
	}
	
	public ExtendedLevel (Level baseLevel, String name, int offset)
	{
		this(baseLevel, name, offset, baseLevel.getSyslogEquivalent());
	}

	public static final ExtendedLevel	VERBOSE=new ExtendedLevel(Level.TRACE, "VERBOSE", 500);
    public static Level toExtendedLevel (String sArg, Level defaultLevel)
    {
    	if ((null == sArg) || (sArg.length() <= 0))
    		return defaultLevel;

    	if (sArg.equalsIgnoreCase(VERBOSE.toString()))
    		return VERBOSE;

    	return toLevel(sArg, defaultLevel);
    }
}
