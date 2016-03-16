/*
 *
 */
package net.community.chest.eclipse.launch;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.lang.EnumUtil;
import net.community.chest.util.collection.CollectionsUtils;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 8, 2009 1:47:24 PM
 */
public enum LaunchAttribute implements AttributeDescriptor {
    CLASSPATH(List.class),
    DEFAULT_CLASSPATH(Boolean.class),
    MAIN_TYPE(String.class),
    PROGRAM_ARGUMENTS(String.class),
    PROJECT_ATTR(String.class),
    VM_ARGUMENTS(String.class),
    WORKING_DIRECTORY(String.class);
    /**
     * Prefix of launch related attribute key(s)
     */
    public static final String    LAUNCH_KEY_PREFIX="org.eclipse.jdt.launching";

    private final String    _attrKey;
    /*
     * @see net.community.chest.eclipse.launch.AttributeDescriptor#getAttributeKey()
     */
    @Override
    public final String getAttributeKey ()
    {
        return _attrKey;
    }

    private final Class<?>    _attrClass;
    /*
     * @see net.community.chest.eclipse.launch.AttributeDescriptor#getAttributeClass()
     */
    @Override
    public final Class<?> getAttributeClass ()
    {
        return _attrClass;
    }
    /*
     * @see net.community.chest.eclipse.launch.AttributeDescriptor#getAttributeType()
     */
    @Override
    public final AttributeType getAttributeType ()
    {
        return AttributeType.LAUNCH;
    }
    /*
     * @see net.community.chest.eclipse.launch.AttributeDescriptor#newInstance(org.w3c.dom.Element)
     */
    @Override
    public Object newInstance (Element elem) throws Exception
    {
        return LaunchUtils.parseElementValue(this, elem);
    }

    private LaunchAttribute (Class<?> ac, String attrName)
    {
        _attrKey = LAUNCH_KEY_PREFIX + "."
            + (((null == attrName) || (attrName.length() <= 0)) ? name() : attrName);
        _attrClass = ac;
    }

    private LaunchAttribute (Class<?> ac)
    {
        this(ac, null);
    }
    /*
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString ()
    {
        return getAttributeKey();
    }

    public static final List<LaunchAttribute>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final LaunchAttribute fromName (final String n)
    {
        return EnumUtil.fromName(VALUES, n, false);
    }

    public static final LaunchAttribute fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }
}
