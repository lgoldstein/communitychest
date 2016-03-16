package net.community.chest.resources;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used to transform strings that have properties encoded as
 * <code>${name}</code> into their values</P>
 * @author Lyor G.
 * @since Jan 10, 2008 1:15:03 PM
 */
public class PropertiesResolver extends AbstractPropertiesResolver {
    private final Properties    _props;
    public final Properties getProperties ()
    {
        return _props;
    }

    public PropertiesResolver (final Properties props)
    {
        if (null == (_props=props))
            throw new IllegalArgumentException("No " + Properties.class.getSimpleName() + " instance provided");
    }
    /*
     * @see net.community.chest.resources.PropertyAccessor#getProperty(java.lang.Object)
     */
    @Override
    public String getProperty (String propName)
    {
        if ((null == propName) || (propName.length() <= 0))
            return null;

        final Properties p=getProperties();
        return (null == p) ? null : p.getProperty(propName);
    }
    /**
     * Builds a property name by prepending the {@link Package} name to the
     * actual property name
     * @param p The {@link Package} to be prepended
     * @param propName The property name to be appended
     * @return Property name - null/empty if no package or null/empty property
     * name value to prepend/append
     */
    public static final String getPackagePropertyName (final Package p, final String propName)
    {
        if ((null == propName) || (propName.length() <= 0))
            return null;

        final String    pName=(null == p) ? null : p.getName();
        if ((null == pName) || (pName.length() <= 0))
            return null;

        return pName + "." + propName;
    }
    /**
     * Builds a property name by prepending the {@link Package} name of the
     * supplied {@link Class} to the actual property name
     * @param c The {@link Class} whose {@link Package} is to be prepended
     * @param propName The property name to be appended
     * @return Property name - null/empty if no class/package or null/empty property
     * name value to prepend/append
     * @see #getPackagePropertyName(Package, String)
     */
    public static final String getClassPropertyName (final Class<?> c, final String propName)
    {
        if ((null == propName) || (propName.length() <= 0))
            return null;

        return (null == c) ? null : getPackagePropertyName(c.getPackage(), propName);
    }

    private static Field    _lookupField    /* =null */;
    private static final synchronized Field getPropertyResourceBundleLookupField () throws Exception
    {
        if (null == _lookupField)
        {
            if ((_lookupField=PropertyResourceBundle.class.getField("lookup")) != null)
            {
                if (!_lookupField.isAccessible())
                    _lookupField.setAccessible(true);
            }
        }

        return _lookupField;
    }
    // TODO review this code if new JDK version used
    @SuppressWarnings("unchecked")
    public static final Map<String,?> getPropertiesMap (final PropertyResourceBundle prb)
    {
        if (null == prb)
            return null;

        try
        {
            final Field    f=getPropertyResourceBundleLookupField();
            if (f != null)
                return (Map<String,?>) f.get(prb);

            return null;
        }
        catch(Exception e)
        {
            return null;    // debug breakpoint
        }
    }
}
