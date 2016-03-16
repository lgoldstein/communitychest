package net.community.chest.rrd4j.client.jmx;

import java.io.Closeable;
import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.jmx.dom.MBeanAttributeDescriptor;
import net.community.chest.jmx.dom.MBeanEntryDescriptor;
import net.community.chest.net.proto.jmx.JMXAccessor;
import net.community.chest.rrd4j.common.RrdUtils;
import net.community.chest.rrd4j.common.core.RrdBackendFactoryExt;
import net.community.chest.rrd4j.common.core.RrdDbExt;
import net.community.chest.rrd4j.common.jmx.MBeanDsDef;
import net.community.chest.rrd4j.common.jmx.MBeanRrdDef;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.rrd4j.core.DsDef;
import org.rrd4j.core.RrdBackendFactory;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used to poll the specified MBean(s) into their respectice RRD files</P>
 * @author Lyor G.
 * @since Jan 9, 2008 2:14:11 PM
 */
public abstract class AbstractMBeanRrdPoller implements Runnable, Closeable {
    private static final LoggerWrapper    _logger=WrapperFactoryManager.getLogger(AbstractMBeanRrdPoller.class);

    private final Map<String,MBeanRrdDef>    _defsMap;
    public final Map<String,? extends MBeanRrdDef> getDefinitionsMap ()
    {
        return _defsMap;
    }

    private final long    _stepSleep;
    public final long getStepSleep ()
    {
        return _stepSleep;
    }
    /**
     * @param defs A {@link Collection} of {@link MBeanRrdDef} - may NOT be
     * null/empty
     * @throws IllegalArgumentException if null/empty definitions {@link Collection}
     * @throws IllegalStateException if not same step for <U>all</U> the
     * supplied definitions
     */
    protected AbstractMBeanRrdPoller (Collection<? extends MBeanRrdDef> defs)
        throws IllegalArgumentException, IllegalStateException
    {
        if ((null == defs) || (defs.size() <= 0))
            throw new IllegalArgumentException("No definitions provided");

        // we are more restrictive - even though MBean(s) names are case sensitive we do not allow such duplicates
        _defsMap = new TreeMap<String,MBeanRrdDef>(String.CASE_INSENSITIVE_ORDER);

        long    s=0L;
        for (final MBeanRrdDef d : defs)
        {
            if (null == d)
                continue;

            final String    mbName=d.getMBeanName();
            if ((null == mbName) || (mbName.length() <= 0))
                throw new IllegalArgumentException("No MBean name specified for " + d);

            final long    sv=d.getStep();
            if (sv <= 0L)
                throw new IllegalArgumentException("Illegal step value: " + sv + " for " + d);
            if (s > 0L)
            {
                if (s != sv)
                    throw new IllegalStateException("Mismatched step value (should be " + s + ") for " + d);
            }
            else
                s = sv;

            final MBeanRrdDef    prev=_defsMap.put(mbName, d);
            if (prev != null)
                throw new IllegalArgumentException("Multiple definitions for MBean=" + mbName);
        }

        if ((_stepSleep=s) <= 0L)
            throw new IllegalStateException("No sleep step calculated");

        if ((null == _defsMap) || (_defsMap.size() <= 0))
            throw new IllegalArgumentException("No definitions mapped");
    }
    /**
     * @return TRUE if to include <code>null</code> values when querying the
     * JMX (default=FALSE - i.e., no <code>null</code> values)
     */
    public boolean isIncludeNulls ()
    {
        return false;
    }

    private long    _timestamp=RrdUtils.toRrdTime(System.currentTimeMillis());
    public long getTimestamp ()
    {
        return _timestamp;
    }

    protected <S extends Sample> S updateSampleValues (final RrdDef def, final S sample, final Map<String,? extends MBeanAttributeDescriptor> attrsMap) throws Exception
    {
        if ((null == sample) || (null == def) || (null == attrsMap) || (attrsMap.size() <= 0))
            return sample;

        final DsDef[]    defs=def.getDsDefs();
        if ((null == defs) || (defs.length <= 0))
            return sample;    // should not happen

        for (int    dIndex=0; dIndex < defs.length; dIndex++)
        {
            final DsDef d=defs[dIndex];
            String        aName=null;
            if (d instanceof MBeanDsDef)
                aName = ((MBeanDsDef) d).getMBeanAttributeName();
            if ((null == aName) || (aName.length() <= 0))
                aName = (null == d) ? null : d.getDsName();

            final MBeanAttributeDescriptor    a=
                ((null == aName) || (aName.length() <= 0)) ? null : attrsMap.get(aName);
            final Number                    av=
                (null == a) ? null : (Number) a.getValue();
            sample.setValue(dIndex, (null == av) ? Double.NaN : av.doubleValue());
        }

        return sample;
    }

    protected void updateData (final MBeanRrdDef def, final Map<String,? extends MBeanAttributeDescriptor> attrsMap) throws Exception
    {
        final RrdBackendFactory    fac=def.getBackendFactory();
        final String            mbName=def.getMBeanName(), path=def.getPath();
        final boolean            exists=RrdBackendFactoryExt.exists(fac, path);
        RrdDb                    db=null;
        if (!exists)
        {
            // set the timestamp a bit back in time just in case
            def.setStartTime(getTimestamp() - def.getStep());
            db = new RrdDbExt(def);
            _logger.info("updateData(" + mbName + ") created path=" + path + " for RRD=" + def);
        }
        else
            db = new RrdDbExt(path, false, fac);

        try
        {
            final Sample sample=updateSampleValues(def, db.createSample(getTimestamp()), attrsMap);
            if (_logger.isDebugEnabled())
                _logger.debug("updateData(" + mbName + ")[" + sample.dump() + "] path=" + path);
            sample.update();
        }
        finally
        {
            try
            {
                db.close();
            }
            catch(Exception e)
            {
                _logger.warn("updateData(" + mbName + ") " + e.getClass().getName() + " while closing path=" + path + ": " + e.getMessage(), e);
            }
            finally
            {
                db = null;
            }
        }
    }

    protected Collection<? extends MBeanEntryDescriptor> updateData (final JMXAccessor acc, final Map<String,? extends MBeanRrdDef> defsMap) throws Exception
    {
        final Collection<? extends MBeanEntryDescriptor>    mbl=
            MBeanRrdDef.getDescriptors((null == defsMap) ? null : defsMap.values());
        // DON'T put these checks in the try-catch block because these are not "OK" I/O exceptions that are expected
        if ((null == mbl) || (mbl.size() <= 0))
            throw new BindException("updateData() no MBean(s) definitions");
        if (null == acc)
            throw new ConnectException("updateData() no JMX accessor");

        final long    qStart=System.currentTimeMillis();
        try
        {
            final Collection<? extends MBeanEntryDescriptor>    res=acc.getValues(mbl, isIncludeNulls());
            final long                                            qEnd=System.currentTimeMillis(), qDuration=qEnd - qStart;
            final int                                            numItems=(null == res) ? 0 : res.size();
            if (_logger.isDebugEnabled())
                _logger.debug("updateData() fetched " + numItems + " items in " + qDuration + " msec.");

            if (numItems > 0)
            {
                for (final MBeanEntryDescriptor mbd : res)
                {
                    final String    mbName=(null == mbd) ? null : mbd.getObjectName();
                    if ((null == mbName) || (mbName.length() <= 0))
                        continue;    // should not happen

                    final MBeanRrdDef    d=defsMap.get(mbName);
                    try
                    {
                        updateData(d, mbd.getAttributesMap());
                    }
                    catch(Exception e)
                    {
                        _logger.warn("updateData() " + e.getClass().getName() + " while updating data of mbean=" + mbName + ": " + e.getMessage(), e);
                    }
                }
            }

            return res;
        }
        catch(IOException e)
        {
            final long    qEnd=System.currentTimeMillis(), qDuration=qEnd - qStart;
            _logger.warn("updateData() " + e.getClass().getName() + " after " + qDuration + " msec.: " + e.getMessage(), e);
            return null;
        }
    }

    public abstract JMXAccessor getJMXAccessor () throws Exception;
    private Thread    _thisThread    /* =null */;
    public Thread getThisThread ()
    {
        return _thisThread;
    }
    /*
     * @see java.lang.Runnable#run()
     */
    public void run ()
    {
        _logger.info("run() running...");
        try
        {
            if (_thisThread != null)
                throw new IllegalStateException("Instance re-use not allowed");
            if (null == (_thisThread=Thread.currentThread()))
                throw new IllegalStateException("No current thread available");

            for (long rIndex=0L; getThisThread() != null; rIndex++)
            {
                // re-initialize it to reflect current value
                _timestamp = RrdUtils.toRrdTime(System.currentTimeMillis());

                updateData(getJMXAccessor(), getDefinitionsMap());

                final long    ss=getStepSleep(), sd=RrdUtils.fromRrdTime(ss);
                if (sd <= 0L)
                    throw new IllegalStateException("Illegal step #" + rIndex + " sleep: " + sd);

                Thread.sleep(sd);
            }
        }
        catch(InterruptedException ie)
        {
            _logger.info("run() - exiting due to interruption");
        }
        catch(Throwable t)
        {
            _logger.error("run() " + t.getClass().getName() + ": " + t.getMessage(), t);
        }
        finally
        {
            _thisThread = null;
        }
    }
    /*
     * @see java.io.Closeable#close()
     */
    public void close () throws IOException
    {
        if (_thisThread != null)
        {
            try
            {
                _thisThread.interrupt();
            }
            finally
            {
                _thisThread = null;
            }
        }
    }
}
