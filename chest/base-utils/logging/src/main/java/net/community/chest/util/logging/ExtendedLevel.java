package net.community.chest.util.logging;

import java.util.logging.Level;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Provides access to some hidden constructor of the {@link Level} class</P>
 *
 * @author Lyor G.
 * @since Oct 1, 2007 9:35:45 AM
 */
public class ExtendedLevel extends Level {
    /**
     *
     */
    private static final long serialVersionUID = 8945182493366599088L;
    public ExtendedLevel (String name, int value)
    {
        super(name, value);
    }

    public ExtendedLevel (String name, int value, String resourceBundleName)
    {
        super(name, value, resourceBundleName);
    }
    /**
     * @param baseLevel {@link Level} to be used as base number/resource
     * bundle - may NOT be null
     * @param name new name
     * @param offset offset to be added to the base level to create the
     * assigned integer value
     */
    public ExtendedLevel (Level baseLevel, String name, int offset)
    {
        this(name, baseLevel.intValue() + offset, baseLevel.getResourceBundleName());
    }
}
