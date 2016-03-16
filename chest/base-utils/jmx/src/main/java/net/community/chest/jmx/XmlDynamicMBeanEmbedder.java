package net.community.chest.jmx;

import java.util.Map;
import java.util.NoSuchElementException;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

import net.community.chest.jmx.dom.MBeanInfoXMLBuilder;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.reflect.MethodsMap;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Uses an XML file to describe the attributes and operations</P>
 *
 * @param <T> Type of embedded MBean
 * @author Lyor G.
 * @since Aug 14, 2007 3:58:17 PM
 */
public abstract class XmlDynamicMBeanEmbedder<T> extends AbstractDynamicMBeanEmbedder<T> {
    protected XmlDynamicMBeanEmbedder (Class<T> instClass, T instance, JMXErrorHandler eh)
    {
        super(instClass, instance, eh);
    }

    protected XmlDynamicMBeanEmbedder (Class<T> instClass, JMXErrorHandler eh)
    {
        this(instClass, null, eh);
    }

    protected XmlDynamicMBeanEmbedder (Class<T> instClass)
    {
        this(instClass, null, null);
    }

    protected XmlDynamicMBeanEmbedder (Class<T> instClass, T instance)
    {
        this(instClass, instance, null);
    }

    @SuppressWarnings("unchecked")
    protected XmlDynamicMBeanEmbedder (T instance, JMXErrorHandler eh)
    {
        this((null == instance) ? null : (Class<T>) instance.getClass(), eh);
    }

    protected XmlDynamicMBeanEmbedder (T instance)
    {
        this(instance, null);
    }
    /**
     * @return accessor to be used to retrieve the MBean's definitions
     * and any imported sub-definitions
     */
    public abstract XmlMBeanDescriptorAccessor getDescriptorAccessor ();
    /**
     * Cached instance - lazy allocated by first call to {@link #getMBeanInfo()}
     */
    private MBeanInfoXMLBuilder    _mbInfo    /* =null */;
    protected synchronized MBeanInfoXMLBuilder getMBeanInfoBuilder () throws Exception
    {
        if (null == _mbInfo)
            _mbInfo = new MBeanInfoXMLBuilder(getValuesClass(), getDescriptorAccessor());
        if (null == _mbInfo.getMBeanInfo()) // force (re-)evaluation if necessary
            throw new IllegalStateException(ClassUtil.getExceptionLocation(getClass(), "getMBeanInfoBuilder") + " no " + MBeanInfo.class.getName() + " instance returned");

        return _mbInfo;
    }
    /*
     * @see javax.management.DynamicMBean#getMBeanInfo()
     */
    @Override
    public MBeanInfo getMBeanInfo ()
    {
        try
        {
            return getMBeanInfoBuilder().getMBeanInfo();
        }
        catch(Exception e)
        {
            final String    msg=ClassUtil.getExceptionLocation(getClass(), "getMBeanInfo") + " " + e.getClass().getName() + ": " + e.getMessage();
            mbeanError(null, msg, e);

            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            throw new IllegalStateException(msg);
        }
    }

    protected ReflectiveMBeanAttributeInfo getAttributeInfo (final String attrName) throws Exception
    {
        if ((null == attrName) || (attrName.length() <= 0))
            throw new IllegalArgumentException(ClassUtil.getArgumentsExceptionLocation(getClass(), "getAttributeInfo", attrName) + " null/empty name");

        final Map<String,ReflectiveMBeanAttributeInfo>    aMap=getMBeanInfoBuilder().getAttributesMap();
        return ((null == aMap) || (aMap.size() <= 0)) ? null : aMap.get(attrName);
    }
    /*
     * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute (final String attrName)
            throws AttributeNotFoundException, MBeanException, ReflectionException
    {
        try
        {
            final ReflectiveMBeanAttributeInfo    aInfo=getAttributeInfo(attrName);
            if (null == aInfo)
                throw new AttributeNotFoundException(ClassUtil.getArgumentsExceptionLocation(getClass(), "getAttribute", attrName) + " no " + ReflectiveMBeanAttributeInfo.class.getName() + " mapping");

            final T    inst=getEmbeddedInstance();
            return aInfo.getValue(inst);
        }
        catch(Exception e)
        {
            if (e instanceof AttributeNotFoundException)
                throw (AttributeNotFoundException) e;
             if (e instanceof ReflectionException)
                 throw (ReflectionException) e;
             if (e instanceof MBeanException)
                 throw (MBeanException) e;
            throw new ReflectionException(e, ClassUtil.getArgumentsExceptionLocation(getClass(), "getAttribute", attrName));
        }
    }
    /*
     * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
     */
    @Override
    public void setAttribute (final Attribute attribute)
        throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        final String    attrName=(null == attribute) /* should not happen */ ? null : attribute.getName();
        final Object    attrValue=(null == attribute) /* should not happen */ ? null : attribute.getValue();

        try
        {
            final ReflectiveMBeanAttributeInfo    aInfo=getAttributeInfo(attrName);
            if (null == aInfo)
                throw new AttributeNotFoundException(ClassUtil.getArgumentsExceptionLocation(getClass(), "setAttribute", attrName, attrValue) + " no " + ReflectiveMBeanAttributeInfo.class.getName() + " mapping");

            final T    inst=getEmbeddedInstance();
            aInfo.setValue(inst, attrValue);
        }
        catch(Exception e)
        {
            if (e instanceof AttributeNotFoundException)
                throw (AttributeNotFoundException) e;
             if (e instanceof ReflectionException)
                 throw (ReflectionException) e;
             if (e instanceof MBeanException)
                 throw (MBeanException) e;
             if (e instanceof InvalidAttributeValueException)
                 throw (InvalidAttributeValueException) e;

             throw new ReflectionException(e, ClassUtil.getArgumentsExceptionLocation(getClass(), "setAttribute", attrName, attrValue));
        }
    }

    protected ReflectiveMBeanOperationInfo getOperationInfo (final String actionName, final String ... signature) throws Exception
    {
        if ((null == actionName) || (actionName.length() <= 0))
            throw new IllegalArgumentException(ClassUtil.getArgumentsExceptionLocation(getClass(), "getOperationInfo", actionName) + " null/empty name");

        final MethodsMap<ReflectiveMBeanOperationInfo>    oMap=getMBeanInfoBuilder().getOperationsMap();
        if ((null == oMap) || (oMap.size() <= 0))
            return null;

        return oMap.get(getValuesClass(), actionName, signature);
    }
    /*
     * @see javax.management.DynamicMBean#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
     */
    @Override
    public Object invoke (final String actionName, final Object[] params, final String[] signature)
        throws MBeanException, ReflectionException
    {
        try
        {
            final ReflectiveMBeanOperationInfo    oInfo=getOperationInfo(actionName, signature);
            if (null == oInfo)
                throw new MBeanException(new NoSuchElementException(), ClassUtil.getArgumentsExceptionLocation(getClass(), "invoke", actionName, signature) + " no such operation");

            final T    inst=getEmbeddedInstance();
            return oInfo.invoke(inst, params);
        }
        catch(Exception e)
        {
            if (e instanceof ReflectionException)
                throw (ReflectionException) e;
             if (e instanceof MBeanException)
                 throw (MBeanException) e;

             throw new ReflectionException(e, ClassUtil.getArgumentsExceptionLocation(getClass(), "invoke", actionName, signature));
        }
    }
}
