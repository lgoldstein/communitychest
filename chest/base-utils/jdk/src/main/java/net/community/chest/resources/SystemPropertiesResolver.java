/*
 *
 */
package net.community.chest.resources;

import java.util.Map;

import net.community.chest.lang.StringUtil;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 26, 2009 11:41:49 AM
 */
public class SystemPropertiesResolver extends PropertiesResolver {
    public SystemPropertiesResolver ()
    {
        super(System.getProperties());
    }
    /**
     * Properties that start with this prefix and not found in the default
     * {@link System} properties are assumed to point to an environment
     * variable (whose name is follows the prefix).
     * @see System#getenv(String)
     */
    public static final String    ENV_PREFIX="env.";
    /*
     * @see net.community.chest.resources.PropertiesResolver#getProperty(java.lang.String)
     */
    @Override
    public String getProperty (final String propName)
    {
        final String    pv=super.getProperty(propName);
        if ((pv != null) && (pv.length() > 0))
            return pv;

        if (!StringUtil.startsWith(propName, ENV_PREFIX, true, true))
            return null;

        final String    evName=propName.substring(ENV_PREFIX.length());
        return System.getenv(evName);
    }
    /**
     * Looks up a property value using the following order:</BR>
     * <UL>
     *         <LI>
     *         invoke {@link System#getProperty(String)} on the provided
     *         property name
     *         </LI>
     *
     *         <LI>
     *         invoke {@link System#getenv(String)} on the provided environment
     *         variable name
     *         </LI>
     *
     *         <LI>
     *         return a default value - if one provided
     *         </LI>
     * </UL>
     * @param propName The property name to us in call to {@link System#getProperty(String)}
     * (ignored if <code>null</code>/empty)
     * @param envVarName The environment variable name to use in call to
     * {@link System#getenv(String)} (ignored if <code>null</code>/empty)
     * @param defValue Default value to return if none of the above invocations
     * was successful (ignored if <code>null</code>/empty)
     * @return Lookup result "pair" as a {@link java.util.Map.Entry} whose key=the
     * successful property/environment variable name (<code>null</code> if
     * default value was used), value=the retrieved value (always <B>not</B>
     * <code>null</code>/empty). Returns <code>null</code> if no option
     * (including the default value) yielded a non-<code>null</code>/empty result.
     */
    public static final Map.Entry<String,String> lookupSystemProperty (
            final String propName, final String envVarName, final String defValue)
    {
        final String[]    vals={
                propName,     ((null == propName) || (propName.length() <= 0)) ? null : System.getProperty(propName),
                envVarName,    ((null == envVarName) || (envVarName.length() <= 0)) ? null : System.getenv(envVarName),
                null,        defValue
            };
        for (int    vIndex=0; vIndex < vals.length; vIndex += 2)
        {
            final String    vv=vals[vIndex + 1];
            if ((null == vv) || (vv.length() <= 0))
                continue;    // skip empty value

            final String    vn=vals[vIndex];
            return new MapEntryImpl<String,String>(vn, vv);
        }

        return null;
    }
    /**
     * Looks up a property value using the following order:</BR>
     * <UL>
     *         <LI>
     *         invoke {@link System#getProperty(String)} on the provided
     *         property name
     *         </LI>
     *
     *         <LI>
     *         invoke {@link System#getenv(String)} on the provided environment
     *         variable name
     *         </LI>
     * </UL>
     * @param propName The property name to us in call to {@link System#getProperty(String)}
     * (ignored if <code>null</code>/empty)
     * @param envVarName The environment variable name to use in call to
     * {@link System#getenv(String)} (ignored if <code>null</code>/empty)
     * @return Lookup result "pair" as a {@link java.util.Map.Entry} whose key=the
     * successful property/environment variable name, value=the retrieved
     * value (always <B>not</B> <code>null</code>/empty). Returns
     * <code>null</code> if no option yielded a non-<code>null</code>/empty
     * result.
     * @see #lookupSystemProperty(String, String, String)
     */
    public static final Map.Entry<String,String> lookupSystemProperty (
                            final String propName, final String envVarName)
    {
        return lookupSystemProperty(propName, envVarName, null);
    }

    public static final SystemPropertiesResolver    SYSTEM=new SystemPropertiesResolver();
}
