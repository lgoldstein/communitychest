/*
 *
 */
package net.community.chest.jmx;

import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.CompilationMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import net.community.chest.CoVariantReturn;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Encapsulates the available {@link ManagementFactory} MXBean(s) as
 * enumerated values</P>
 *
 * @author Lyor G.
 * @since Dec 8, 2008 8:45:29 AM
 */
public enum ManagementFactoryBeanType {
    CLASSLOADING(ManagementFactory.CLASS_LOADING_MXBEAN_NAME, true, ClassLoadingMXBean.class) {
            /*
             * @see net.community.chest.jmx.mxbean.ManagementFactoryBeanType#getManagedMXBeans()
             */
            @Override
            @CoVariantReturn
            public List<ClassLoadingMXBean> getManagedMXBeans ()
            {
                return Arrays.asList(ManagementFactory.getClassLoadingMXBean());
            }
        },
    COMPILATION(ManagementFactory.COMPILATION_MXBEAN_NAME, true, CompilationMXBean.class) {
            /*
             * @see net.community.chest.jmx.mxbean.ManagementFactoryBeanType#getManagedMXBeans()
             */
            @Override
            @CoVariantReturn
            public List<CompilationMXBean> getManagedMXBeans ()
            {
                return Arrays.asList(ManagementFactory.getCompilationMXBean());
            }
        },
    GC(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE, false, GarbageCollectorMXBean.class) {
            /*
             * @see net.community.chest.jmx.mxbean.ManagementFactoryBeanType#getManagedMXBeans()
             */
            @Override
            @CoVariantReturn
            public List<GarbageCollectorMXBean> getManagedMXBeans ()
            {
                return ManagementFactory.getGarbageCollectorMXBeans();
            }
            /*
             * @see net.community.chest.jmx.mxbean.ManagementFactoryBeanType#appendManagedMXBeanInstanceName(java.lang.Object, java.lang.Appendable)
             */
            @Override
            protected Appendable appendManagedMXBeanInstanceName (final Object mo, final Appendable sb) throws IOException
            {
                if (null == sb)
                    throw new IOException("No " + Appendable.class.getSimpleName() + " instance provided");
                if (null == mo)
                    return sb;

                final String    n=((GarbageCollectorMXBean) mo).getName();
                sb.append(",name=").append(n);
                return sb;
            }
        },
    MEMMGRS(ManagementFactory.MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE, false, MemoryManagerMXBean.class) {
            /*
             * @see net.community.chest.jmx.mxbean.ManagementFactoryBeanType#getManagedMXBeans()
             */
            @Override
            @CoVariantReturn
            public List<MemoryManagerMXBean> getManagedMXBeans ()
            {
                return ManagementFactory.getMemoryManagerMXBeans();
            }
            /*
             * @see net.community.chest.jmx.mxbean.ManagementFactoryBeanType#appendManagedMXBeanInstanceName(java.lang.Object, java.lang.Appendable)
             */
            @Override
            protected Appendable appendManagedMXBeanInstanceName (final Object mo, final Appendable sb) throws IOException
            {
                if (null == sb)
                    throw new IOException("No " + Appendable.class.getSimpleName() + " instance provided");
                if (null == mo)
                    return sb;

                final String    n=((MemoryManagerMXBean) mo).getName();
                sb.append(",name=").append(n);
                return sb;
            }
        },
    MEMORY(ManagementFactory.MEMORY_MXBEAN_NAME, true, MemoryMXBean.class) {
            /*
             * @see net.community.chest.jmx.mxbean.ManagementFactoryBeanType#getManagedMXBeans()
             */
            @Override
            @CoVariantReturn
            public List<MemoryMXBean> getManagedMXBeans ()
            {
                return Arrays.asList(ManagementFactory.getMemoryMXBean());
            }
        },
    MEMPOOLS(ManagementFactory.MEMORY_POOL_MXBEAN_DOMAIN_TYPE, false, MemoryPoolMXBean.class) {
            /*
             * @see net.community.chest.jmx.mxbean.ManagementFactoryBeanType#getManagedMXBeans()
             */
            @Override
            @CoVariantReturn
            public List<MemoryPoolMXBean> getManagedMXBeans ()
            {
                return ManagementFactory.getMemoryPoolMXBeans();
            }
            /*
             * @see net.community.chest.jmx.mxbean.ManagementFactoryBeanType#appendManagedMXBeanInstanceName(java.lang.Object, java.lang.Appendable)
             */
            @Override
            protected Appendable appendManagedMXBeanInstanceName (final Object mo, final Appendable sb) throws IOException
            {
                if (null == sb)
                    throw new IOException("No " + Appendable.class.getSimpleName() + " instance provided");
                if (null == mo)
                    return sb;

                final String    n=((MemoryPoolMXBean) mo).getName();
                sb.append(",name=").append(n);
                return sb;
            }
        },
    OS(ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, true, OperatingSystemMXBean.class) {
            /*
             * @see net.community.chest.jmx.mxbean.ManagementFactoryBeanType#getManagedMXBeans()
             */
            @Override
            @CoVariantReturn
            public List<OperatingSystemMXBean> getManagedMXBeans ()
            {
                return Arrays.asList(ManagementFactory.getOperatingSystemMXBean());
            }
        },
    RUNTIME(ManagementFactory.RUNTIME_MXBEAN_NAME, true, RuntimeMXBean.class) {
            /*
             * @see net.community.chest.jmx.mxbean.ManagementFactoryBeanType#getManagedMXBeans()
             */
            @Override
            @CoVariantReturn
            public List<RuntimeMXBean> getManagedMXBeans ()
            {
                return Arrays.asList(ManagementFactory.getRuntimeMXBean());
            }
        },
    THREAD(ManagementFactory.THREAD_MXBEAN_NAME, true, ThreadMXBean.class) {
            /*
             * @see net.community.chest.jmx.mxbean.ManagementFactoryBeanType#getManagedMXBeans()
             */
            @Override
            @CoVariantReturn
            public List<ThreadMXBean> getManagedMXBeans ()
            {
                return Arrays.asList(ManagementFactory.getThreadMXBean());
            }
        };
    /**
     * The MBean name that can be used to retrieve this MXBean through a
     * regular JMX accessor (if MXBean has been registered)
     */
    private final String    _objName;
    public final String getMBeanName ()
    {
        return _objName;
    }
    /**
     * TRUE if the {@link ManagementFactory} getter for this MXBean returns
     * a simple instance or a list of instances
     */
    private final boolean    _oneInst;
    public final boolean isSingleInstanceMBean ()
    {
        return _oneInst;
    }
    /**
     * The basic type of the data returned by the MXBean - if a list of
     * instances, then this specifies the type of each member in the list
     * @see #isSingleInstanceMBean()
     */
    private final Class<?>    _mxbType;
    public final Class<?> getMBeanType ()
    {
        return _mxbType;
    }
    /**
     * Lazy initialized {@link ObjectName} of the MXBean registered name
     * @see #getMBeanName()
     */
    private ObjectName    _mbName;
    public final synchronized ObjectName getMBeanObjectName ()
            throws MalformedObjectNameException, NullPointerException // should not happen
    {
        if (null == _mbName)
        {
            final String    objName=getMBeanName();
            _mbName = new ObjectName(objName);
        }

        return _mbName;
    }
    /**
     * @return A {@link List} of the MXBeans represented by this enumerated
     * value - may be null/empty
     */
    public abstract List<?> getManagedMXBeans ();
    /**
     * @param mo The managed MXBean instance
     * @param sb An {@link Appendable} instance to which to append a
     * &quot;sub&quot;-name for the MXBean instance. The {@link Appendable}
     * instance is pre-populated with the {@link #getMBeanName()} value
     * @return By default the same as input {@link Appendable} instance
     * (after making the necessary changes)
     * @throws IOException if failed to append the data
     */
    protected Appendable appendManagedMXBeanInstanceName (final Object mo, final Appendable sb) throws IOException
    {
        if (null == sb)
            throw new IOException("No " + Appendable.class.getSimpleName() + " instance provided");

        // just so
        if (null == mo)
            return sb;
        else
            return sb;
    }

    /**
     * @return A {@link List} of pairs represented by a {@link java.util.Map.Entry}
     * whose key=the recommended {@link ObjectName} to register the MBean,
     * value=the {@link DynamicMBean} instance to register with the JMX.
     * @throws Exception If failed to instantiate the MBean or its name
     */
    public List<Map.Entry<ObjectName,DynamicMBean>> getMBeansInstances () throws Exception
    {
        final Collection<?>    mgmtObjects=getManagedMXBeans();
        final int            numObjs=(null == mgmtObjects) ? 0 : mgmtObjects.size();
        if (numObjs <= 0)
            return null;

        final String                                mbName=getMBeanName();
        final StringBuilder                            sb=
            new StringBuilder(mbName.length() + 64).append(mbName);
        final int                                    orgLen=sb.length();
        final Class<?>                                mxbType=getMBeanType();
        List<Map.Entry<ObjectName,DynamicMBean>>    res=null;
        for (final Object mo : mgmtObjects)
        {
            final Class<?>    mc=(null == mo) /* OK - ignore */ ? null : mo.getClass();
            if (null == mc)
                continue;

            if (!mxbType.isAssignableFrom(mc))    // should not happen
                throw new ClassCastException("getMBeansInstances(" + this + ") mismatched MXBean instances - expected=" + mxbType.getName() + "/got=" + mc.getName());

            if (sb.length() > orgLen)
                sb.setLength(orgLen);

            final Appendable    n=appendManagedMXBeanInstanceName(mo, sb);
            final ObjectName    mn=new ObjectName(n.toString());
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final DynamicMBean    mb=
                (null == mo) ? null : new StaticMBeanClassEmbedder(mxbType, mo);
            if (null == res)
                res = new ArrayList<Map.Entry<ObjectName,DynamicMBean>>(numObjs);
            res.add(new MapEntryImpl<ObjectName,DynamicMBean>(mn, mb));
        }

        return res;
    }
    /**
     * Registers the <U>unregistered</U> MXBeans to the provided server
     * @param s The {@link MBeanServer} instance to register to
     * @return The {@link MBeanRegistrationResult}
     * @throws Exception if cannot create initial MBean(s) instances
     * @see javax.management.MBeanServerConnection#isRegistered(ObjectName)
     * @see #getMBeansInstances()
     */
    public MBeanRegistrationResult registerMBeans (final MBeanServer s) throws Exception
    {
        final Collection<? extends Map.Entry<? extends ObjectName,? extends DynamicMBean>>    mbl=getMBeansInstances();
        final int                                                                            numMBeans=(null == mbl) ? 0 : mbl.size();
        if (numMBeans <= 0)
            return null;

        MBeanRegistrationResult    res=null;
        for (final Map.Entry<? extends ObjectName,? extends DynamicMBean> me : mbl)
        {
            final ObjectName    mn=(null == me) ? null : me.getKey();
            final DynamicMBean    mb=(null == me) ? null : me.getValue();
            if ((null == mn) || (null == mb))
                continue;

            try
            {
                if (s.isRegistered(mn))
                    continue;

                final ObjectInstance    inst=s.registerMBean(mb, mn);
                if (null == inst)
                    throw new InstanceNotFoundException("No object instance created");

                if (null == res)
                    res = new MBeanRegistrationResult();

                res.addRegisteredMBean(inst);
            }
            catch(Exception e)
            {
                if (null == res)
                    res = new MBeanRegistrationResult();
                res.addRegistrationException(mn, e);
            }
        }

        return res;
    }

    ManagementFactoryBeanType (final String objName, final boolean oneInst, final Class<?> mxbType)
    {
        _objName = objName;
        _oneInst = oneInst;
        _mxbType = mxbType;
    }

    private static List<ManagementFactoryBeanType>    _values    /* =null */;
    public static final synchronized List<ManagementFactoryBeanType> getValues ()
    {
        if (_values == null)
            _values = Collections.unmodifiableList(Arrays.asList(values()));
        return _values;
    }

    public static final ManagementFactoryBeanType fromString (final String s)
    {
        return CollectionsUtils.fromString(getValues(), s, false);
    }

    public static final ManagementFactoryBeanType fromMBeanName (final String n)
    {
        if ((null == n) || (n.length() <= 0))
            return null;

        final Collection<ManagementFactoryBeanType>    vals=getValues();
        if ((null == vals) || (vals.size() <= 0))
            return null;    // should not happen

        for (final ManagementFactoryBeanType v : vals)
        {
            final String    vn=(null == v) ? null : v.getMBeanName();
            if (n.equalsIgnoreCase(vn))
                return v;
        }

        return null;
    }

    public static final ManagementFactoryBeanType fromMBeanObjectName (final ObjectName n)
    {
        return (null == n) ? null : fromMBeanName(n.toString());
    }
    /**
     * Registers all MBean(s) that are not registered yet with the provided
     * {@link MBeanServer} instance
     * @param s The {@link MBeanServer} instance to register to
     * @return A {@link Map} of all MXBean(s) that <U>registered</U> some
     * MBean(s) with the server. Key={@link ManagementFactoryBeanType}, value=
     * the {@link MBeanRegistrationResult} for the MXBean
     * @throws Exception if cannot create initial MBean(s) instances
     * @see #registerMBeans(MBeanServer)
     */
    public static final Map<ManagementFactoryBeanType,MBeanRegistrationResult> registerAllMBeans (final MBeanServer s) throws Exception
    {
        Map<ManagementFactoryBeanType,MBeanRegistrationResult>    res=null;
        final Collection<ManagementFactoryBeanType>                vals=getValues();
        for (final ManagementFactoryBeanType mbt : vals)
        {
            final MBeanRegistrationResult    mbr=(null == mbt) ? null : mbt.registerMBeans(s);
            if (null == mbr)
                continue;

            if (null == res)
                res = new EnumMap<ManagementFactoryBeanType,MBeanRegistrationResult>(ManagementFactoryBeanType.class);

            final MBeanRegistrationResult    prev=res.put(mbt, mbr);
            if (prev != null)    // should not happen
                throw new IllegalStateException("Multiple MBean(s) registered for type=" + mbt);
        }

        return res;
    }
}
