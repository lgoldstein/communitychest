package net.community.chest.apache.ant.helpers;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.io.jar.BaseURLClassLoader;
import net.community.chest.io.jar.JarUtils;
import net.community.chest.lang.ExceptionUtil;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful static methods</U>
 *
 * @author Lyor G.
 * @since Jul 29, 2007 1:20:12 PM
 */
public final class AntUtils {
    private AntUtils ()
    {
        // no instance
    }
    /**
     * @param args command line arguments as received via <I>main</I> entry point
     * @return map of currently defined properties via the "-Dprop=value"
     * argument line option (key=property name, value=property value) - may
     * be null/empty if no properties defined
     */
    public static final Map<String,String> getArgsLineProperties (final String ...args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        if (numArgs <= 0)
            return null;

        Map<String,String>    propsMap=null;
        for (final String argVal : args)
        {
            final int    avLen=(null == argVal) ? 0 : argVal.length();
            if ((avLen <= 0) || (!argVal.startsWith("-D")))
                continue;

            final int    eqPos=argVal.indexOf('=');
            if ((eqPos <= 2) || (eqPos >= avLen))
                continue;    // ignore malformed properties values

            final String    propName=argVal.substring(2, eqPos),
                            propVal=(eqPos < (avLen-1)) ? argVal.substring(eqPos + 1) : "";
            if (null == propsMap)
                propsMap = new TreeMap<String, String>();
            propsMap.put(propName, propVal);
           }

        return propsMap;
    }
    /**
     * @param propsMap - updated properties map (key=name, value=property
     * data) - may be null/empty (in which case, <U>all</U> "-Dprop=value"
     * definitions are deleted
     * @param args original <I>main</I> arguments
     * @return effective arguments - including <code>-D</code> properties
     * definitions
     */
    public static final String[] setArgsLineProperties (final Map<String,String> propsMap, final String ... args)
    {
        Collection<String>    effArgs=null;
        // collect all non-"-D" arguments
        {
            final int    numArgs=(null == args) ? 0 : args.length;
            if (numArgs > 0)
            {
                for (final String argVal : args)
                {
                    final int    avLen=(null == argVal) ? 0 : argVal.length();
                    if ((avLen <= 0) || argVal.startsWith("-D"))
                        continue;    // skip "-D" arguments

                    if (null == effArgs)
                        effArgs = new LinkedList<String>();
                    effArgs.add(argVal);
                }
            }
        }

        // add all properties
        {
            final Collection<? extends Map.Entry<String,String>>    eSet=((null == propsMap) || (propsMap.size()) <= 0) ? null : propsMap.entrySet();
            if ((eSet != null) && (eSet.size() > 0))
            {
                for (final Map.Entry<String,String> e : eSet)
                {
                    if (null == e)    // should not happen
                        continue;

                    final String    propName=e.getKey(), propVal=e.getValue();
                    if ((null == propName) || (propName.length() <= 0))
                        continue;    // should not happen

                    final String    propDef="-D" + propName + "="
                            + (((null == propVal) || (propVal.length() <= 0)) ? "\"\"" : propVal)
                            ;
                    if (null == effArgs)
                        effArgs = new LinkedList<String>();
                    effArgs.add(propDef);
                }
            }
        }

        final int    numEffArgs=(null == effArgs) ? 0 : effArgs.size();
        return (numEffArgs <= 0) ? null : effArgs.toArray(new String[numEffArgs]);
    }

    public static final String    DEFAULT_ANT_HOME_ENV_VAR_NAME="ANT_HOME", DEFAULT_ANT_HOME_PROP_NAME="ant.home";
    public static final String resolveAntHome (final String orgHome)
    {
        String    antHome=orgHome;
        if ((null == antHome) || (antHome.length() <= 0))
            antHome = System.getProperty(DEFAULT_ANT_HOME_PROP_NAME);
        if ((null == antHome) || (antHome.length() <= 0))
            antHome = System.getenv(DEFAULT_ANT_HOME_ENV_VAR_NAME);
        return antHome;
    }

    public static final String    ANT_TOOLS_PKG_NAME="org.apache.tools.ant";
    public static final boolean isAccessibleAntHome (final ClassLoader cl, final boolean findAll, final String ... testClassNames)
    {
        if ((cl == null) || (testClassNames == null) || (testClassNames.length <= 0))
            return false;

        for (final String cn : testClassNames)
        {
            if ((cn == null) || (cn.length() <= 0))
                continue;

            final String    cp=ANT_TOOLS_PKG_NAME + "." + cn;
            try
            {
                final Class<?>    c=cl.loadClass(cp);
                if (findAll)
                {
                    if (c == null)
                        return false;
                }
                else
                {
                    if (c != null)
                        return true;
                }
            }
            catch(ClassNotFoundException e)
            {
                if (findAll)
                    return false;
            }
            catch(NoClassDefFoundError e)
            {
                if (findAll)
                    return false;
            }
        }

        return findAll;
    }

    public static final boolean isAccessibleAntHome (final ClassLoader cl)
    {
        return isAccessibleAntHome(cl, true, "Main", "BuildLogger" );
    }

    public static final ClassLoader setupAntHome (final ClassLoader clThread, final String orgHome)
    {
        if (isAccessibleAntHome(clThread))
            return clThread;

        final String    antHome=resolveAntHome(orgHome);
        if ((null == antHome) || (antHome.length() <= 0))
            throw new IllegalStateException("No ANT home location detected");

        try
        {
            final Collection<? extends URL>    urls=JarUtils.getJarURLs(new File(antHome), true);
            if ((urls == null) || (urls.size() <= 0))
                return clThread;

            if (clThread instanceof BaseURLClassLoader)
            {
                ((BaseURLClassLoader) clThread).addAll(urls);
                return clThread;
            }
            else
            {
                return new BaseURLClassLoader(clThread, urls);
            }
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }
}
