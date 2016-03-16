package net.community.chest.jmx;

import java.util.Map;
import java.util.TreeMap;

import javax.management.ObjectInstance;
import javax.management.ObjectName;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to return a group of MBean registration results</P>
 * @author Lyor G.
 * @since Oct 7, 2007 9:29:03 AM
 */
public class MBeanRegistrationResult {
    public MBeanRegistrationResult ()
    {
        super();
    }
    /**
     * A {@link Map} of <U>successfully</U> registered MBean(s).
     * Key=MBean registration name, value=registered {@link ObjectInstance}
     */
    private Map<String,ObjectInstance>    _regsMap    /* =null */;
    public Map<String,ObjectInstance> getRegisteredMBeans ()
    {
        return _regsMap;
    }

    public void setRegisteredMBeans (Map<String,ObjectInstance> regsMap)
    {
        _regsMap = regsMap;
    }

    public Map<String,ObjectInstance> addRegisteredMBean (ObjectInstance inst)
    {
        Map<String,ObjectInstance>    regsMap=getRegisteredMBeans();
        final ObjectName            objName=(null == inst) ? null : inst.getObjectName();
        final String                objKey=(null == objName) ? null : objName.toString();
        if ((null == objKey) || (objKey.length() <= 0))
            return regsMap;

        if (null == regsMap)
        {
            setRegisteredMBeans(new TreeMap<String, ObjectInstance>());
            if (null == (regsMap=getRegisteredMBeans()))    // should not happen
                throw new IllegalStateException("addRegisteredMBean(" + objKey + ") no " + Map.class.getName() + " returned though set");
        }

        regsMap.put(objKey, inst);
        return regsMap;
    }
    /**
     * A {@link Map} of {@link Exception}-s that occurred while attempting to
     * register MBean(s). <B>Note:</B> an MBean may appear <U>both</U> in the
     * registered MBeans {@link Map} and in this one - e.g., registration was
     * successful, but setting the initial attributes failed.
     */
    private Map<String,Exception>    _excsMap    /* =null */;
    public Map<String,Exception> getRegistrationExceptions ()
    {
        return _excsMap;
    }

    public void setRegistrationExceptions (Map<String,Exception> excsMap)
    {
        _excsMap = excsMap;
    }

    public Map<String,Exception> addRegistrationException (String objKey, Exception e)
    {
        Map<String,Exception>    excsMap=getRegistrationExceptions();
        if ((null == objKey) || (objKey.length() <= 0) || (null == e))
            return excsMap;

        if (null == excsMap)
        {
            setRegistrationExceptions(new TreeMap<String, Exception>());
            if (null == (excsMap=getRegistrationExceptions()))    // should not happen
                throw new IllegalStateException("addRegistrationException(" + objKey + ") no " + Map.class.getName() + " returned though set");
        }

        excsMap.put(objKey, e);
        return excsMap;
    }

    public Map<String,Exception> addRegistrationException (ObjectName objName, Exception e)
    {
        return addRegistrationException((null == objName) ? null : objName.toString(), e);
    }
}
