package net.community.chest.rrd4j.common.core;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.dom.DOMUtils;

import org.rrd4j.core.RrdBackend;
import org.rrd4j.core.RrdBackendFactory;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 8, 2008 2:49:17 PM
 */
public abstract class RrdBackendFactoryExt extends RrdBackendFactory {
    protected RrdBackendFactoryExt ()
    {
        super();
    }

    public static final String FACTORY_NAME_ATTR="factoryName";
    public static final RrdBackendFactory resolveFactory (Element elem, RrdBackendFactory defFactory) throws Exception
    {
        final String    facName=elem.getAttribute(FACTORY_NAME_ATTR);
        if ((null == facName) || (facName.length() <= 0) || "default".equalsIgnoreCase(facName))
            return defFactory;

        return getFactory(facName);
    }

    public static final RrdBackendFactory resolveFactory (Element elem) throws Exception
    {
        return resolveFactory(elem, getDefaultFactory());
    }

    public static final Element addNonEmptyFactory (Element elem, RrdBackendFactory fac)
    {
        return DOMUtils.addNonEmptyAttribute(elem, FACTORY_NAME_ATTR, (null == fac) ? null : fac.getFactoryName());
    }

    private static final Map<String,Map<String,Method>>    _methodsMap=new TreeMap<String,Map<String,Method>>();
    public static Method getFactoryMethod (Class<? extends RrdBackendFactory> fcClass, String name, Class<?> ... parameterTypes) throws Exception
    {
        final String    cName=(null == fcClass) ? null : fcClass.getName();
        if ((null == cName) || (cName.length() <= 0)
         || (null == name) || (name.length() <= 0))
            throw new IllegalArgumentException("getFactoryMethod(" + cName + "[" + name + "] incomplete arguments");

        Map<String,Method>    cMap=null;
        synchronized(_methodsMap)
        {
            if (null == (cMap=_methodsMap.get(cName)))
            {
                _methodsMap.put(cName, new TreeMap<String,Method>());
                if (null == (cMap=_methodsMap.get(cName)))    // should not happen since just put it
                    throw new IllegalStateException("getFactoryMethod(" + cName + "[" + name + "] no " + Map.class.getSimpleName() + " available though created");
            }
        }

        Method    m=null;
        synchronized(cMap)
        {
            if (null == (m=cMap.get(name)))
            {
                final Method[]    ma=fcClass.getDeclaredMethods();
                for (final Method dm : ma)
                {
                    final String    mName=(null == dm) ? null : dm.getName();
                    if (!name.equals(mName))
                        continue;

                    final Class<?>[]    mPars=dm.getParameterTypes();
                    // match number of parameters
                    if ((parameterTypes != null) && (parameterTypes.length > 0))
                    {
                        if ((null == mPars) || (mPars.length != parameterTypes.length))
                            continue;

                        // match type of parameters
                        boolean    match=true;
                        for (int    pIndex=0; pIndex < mPars.length; pIndex++)
                        {
                            final Class<?> c1=mPars[pIndex], c2=parameterTypes[pIndex];
                            if (!(match=(c1 == c2)))
                                break;
                        }

                        if (!match)
                            continue;
                    }
                    else
                    {
                        if ((mPars != null) && (mPars.length > 0))
                            continue;
                    }

                    m = dm;
                    break;
                }

                if (m != null)
                {
                    if (!Modifier.isPublic(m.getModifiers()))
                        m.setAccessible(true);
                    cMap.put(name, m);
                }
            }
        }

        // if still no method, check if inherited from superclass
        if (null == m)
        {
            final Class<?>    parClass=fcClass.getSuperclass();
            // if parent not RrdBackendFactory then stop
            if (RrdBackendFactory.class.isAssignableFrom(parClass))
            {
                @SuppressWarnings("unchecked")
                final Method facMethod=getFactoryMethod((Class<? extends RrdBackendFactory>) parClass, name, parameterTypes);
                if ((m=facMethod) != null)
                {
                    // if found in parent, map as if for derived class - will work according to reflection API rules
                    synchronized(cMap)
                    {
                        cMap.put(name, m);
                    }
                }
            }
        }

        return m;
    }

    public static final Method getExistsMethod (Class<? extends RrdBackendFactory> fcClass) throws Exception
    {
        return getFactoryMethod(fcClass, "exists", String.class);
    }

    public static final boolean exists (RrdBackendFactory fac, String path) throws Exception
    {
        final Method    m=getExistsMethod(fac.getClass());
        final Boolean    v=(Boolean) m.invoke(fac, path);
        return v.booleanValue();
    }

    public static final Method getOpenMethod (Class<? extends RrdBackendFactory> fcClass) throws Exception
    {
        return getFactoryMethod(fcClass, "open", String.class, Boolean.TYPE);
    }

    public static final RrdBackend open (RrdBackendFactory fac, String path, boolean readOnly) throws Exception
    {
        final Method        m=getOpenMethod(fac.getClass());
        final RrdBackend    v=(RrdBackend) m.invoke(fac, path, Boolean.valueOf(readOnly));
        return v;
    }

    public static final Method getValidateHeaderMethod (Class<? extends RrdBackendFactory> fcClass) throws Exception
    {
        return getFactoryMethod(fcClass, "shouldValidateHeader", String.class);
    }

    public static final boolean shouldValidateHeader (RrdBackendFactory fac, String path) throws Exception
    {
        final Method    m=getValidateHeaderMethod(fac.getClass());
        final Boolean    v=(Boolean) m.invoke(fac, path);
        return v.booleanValue();
    }
}
