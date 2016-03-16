/*
 *
 */
package net.community.chest.eclipse.launch;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.lang.EnumUtil;
import net.community.chest.util.collection.CollectionsUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 8, 2009 2:10:55 PM
 */
public enum DebugAttribute implements AttributeDescriptor {
    MAPPED_RESOURCE_PATHS(List.class),
    MAPPED_RESOURCE_TYPES(List.class),
    SOURCE_LOCATOR_ID(String.class, "source_locator_id"),
    SOURCE_LOCATOR_MEMENTO(Document.class, "source_locator_memento");
    /**
     * Prefix of launch related attribute key(s)
     */
    public static final String    DEBUG_KEY_PREFIX="org.eclipse.debug.core";

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
        return AttributeType.DEBUG;
    }
    /*
     * @see net.community.chest.eclipse.launch.AttributeDescriptor#newInstance(org.w3c.dom.Element)
     */
    @Override
    public Object newInstance (Element elem) throws Exception
    {
        return LaunchUtils.parseElementValue(this, elem);
    }

    private DebugAttribute (Class<?> ac, String attrName)
    {
        _attrKey = DEBUG_KEY_PREFIX + "."
            + (((null == attrName) || (attrName.length() <= 0)) ? name() : attrName);
        _attrClass = ac;
    }

    private DebugAttribute (Class<?> ac)
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

    public static final List<DebugAttribute>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final DebugAttribute fromName (final String n)
    {
        return EnumUtil.fromName(VALUES, n, false);
    }

    public static final DebugAttribute fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }
}
