/**
 * 
 */
package net.community.chest.web.servlet.jmx;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.ServiceNotFoundException;
import javax.management.loading.ClassLoaderRepository;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;

import net.community.chest.Version;
import net.community.chest.jmx.JMXErrorHandler;
import net.community.chest.jmx.JMXProtocol;
import net.community.chest.jmx.JMXUtils;
import net.community.chest.jmx.dom.MBeanAttributeDescriptor;
import net.community.chest.jmx.dom.MBeanEntryDescriptor;
import net.community.chest.jmx.dom.MBeanOperationDescriptor;
import net.community.chest.jmx.dom.MBeanParameterDescriptor;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.regexp.RegexpUtils;
import net.community.chest.resources.ResourceDataRetriever;
import net.community.chest.util.map.MapEntryImpl;
import net.community.chest.web.servlet.ServletRequestParameters;
import net.community.chest.web.servlet.ServletUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provides a basic access to JMX via a servlet using the &quot;language&quot;
 * specified in {@link JMXProtocol}</P>
 * 
 * @author Lyor G.
 * @since Jul 28, 2008 2:11:13 PM
 */
public abstract class AbstractJMXServlet extends HttpServlet implements MBeanServer, JMXErrorHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = 773857985260335997L;

	protected AbstractJMXServlet ()
	{
		super();
	}
	/*
	 * @see net.community.chest.jmx.JMXErrorHandler#mbeanError(java.lang.String, java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void mbeanError (String mbName, String msg, Throwable t)
	{
		log(mbName + "[ERROR]: " + msg, t);
	} 
	/*
	 * @see net.community.chest.jmx.JMXErrorHandler#mbeanWarning(java.lang.String, java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void mbeanWarning (String mbName, String msg, Throwable t)
	{
		log(mbName + "[WARNING]: " + msg, t);
	}
	/*
	 * @see net.community.chest.jmx.JMXErrorHandler#errorThrowable(java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T errorThrowable (T t)
	{
		mbeanError("<unknown>", t.getMessage(), t);
		return t;
	}
	/*
	 * @see net.community.chest.jmx.JMXErrorHandler#warnThrowable(java.lang.Throwable)
	 */
	@Override
	public <T extends Throwable> T warnThrowable (T t)
	{
		mbeanWarning("<unknown>", t.getMessage(), t);
		return t;
	}
	/*
	 * @see javax.management.MBeanServer#addNotificationListener(javax.management.ObjectName, javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
	 */
	@Override
	public void addNotificationListener (ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException
	{
		throw new UnsupportedOperationException("addNotificationListener(" + name + ") N/A");
	}
	/*
	 * @see javax.management.MBeanServer#addNotificationListener(javax.management.ObjectName, javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
	 */
	@Override
	public void addNotificationListener (ObjectName name, ObjectName listener, NotificationFilter filter, Object handback)
		throws InstanceNotFoundException
	{
		throw new UnsupportedOperationException("addNotificationListener(" + name + ")[" + listener + "] N/A");
	}
	/*
	 * @see javax.management.MBeanServer#createMBean(java.lang.String, javax.management.ObjectName, javax.management.ObjectName, java.lang.Object[], java.lang.String[])
	 */
	@Override
	public ObjectInstance createMBean (String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature)
		throws ReflectionException, InstanceAlreadyExistsException,
			   MBeanRegistrationException, MBeanException,
			   NotCompliantMBeanException, InstanceNotFoundException
	{
		throw new UnsupportedOperationException("createMBean(" + name + ")[" + className + "]{" + loaderName + "} N/A");
	}
	/*
	 * @see javax.management.MBeanServer#createMBean(java.lang.String, javax.management.ObjectName, java.lang.Object[], java.lang.String[])
	 */
	@Override
	public ObjectInstance createMBean (String className, ObjectName name, Object[] params, String[] signature)
	 	throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
			   MBeanException, NotCompliantMBeanException
	{
		throw new UnsupportedOperationException("createMBean(" + name + ") N/A");
	}
	/*
	 * @see javax.management.MBeanServer#createMBean(java.lang.String, javax.management.ObjectName, javax.management.ObjectName)
	 */
	@Override
	public ObjectInstance createMBean (String className, ObjectName name, ObjectName loaderName)
		throws ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException,
			   MBeanException, NotCompliantMBeanException, InstanceNotFoundException
	{
		return createMBean(className, name, loaderName, null, null);
	}
	/*
	 * @see javax.management.MBeanServer#createMBean(java.lang.String, javax.management.ObjectName)
	 */
	@Override
	public ObjectInstance createMBean (String className, ObjectName name)
			throws ReflectionException, InstanceAlreadyExistsException,
				   MBeanRegistrationException, MBeanException, NotCompliantMBeanException
	{
		throw new UnsupportedOperationException("createMBean(" + name + ")[" + className + "] N/A");
	}
	/*
	 * @see javax.management.MBeanServer#deserialize(javax.management.ObjectName, byte[])
	 */
	@Override
	@SuppressWarnings("deprecation")
	public ObjectInputStream deserialize (ObjectName name, byte[] data)
		throws InstanceNotFoundException, OperationsException
	{
		throw new UnsupportedOperationException("deserialize(" + name + ") N/A");
	}
	/*
	 * @see javax.management.MBeanServer#deserialize(java.lang.String, byte[])
	 */
	@Override
	@SuppressWarnings("deprecation")
	public ObjectInputStream deserialize (String className, byte[] data)
		throws OperationsException, ReflectionException
	{
		throw new UnsupportedOperationException("deserialize(" + className + ") N/A");
	}
	/*
	 * @see javax.management.MBeanServer#deserialize(java.lang.String, javax.management.ObjectName, byte[])
	 */
	@Override
	@SuppressWarnings("deprecation")
	public ObjectInputStream deserialize (String className, ObjectName loaderName, byte[] data)
			throws InstanceNotFoundException, OperationsException, ReflectionException
	{
		throw new UnsupportedOperationException("deserialize(" + className + ")[" + loaderName + "] N/A");
	}
	/*
	 * @see javax.management.MBeanServer#getAttribute(javax.management.ObjectName, java.lang.String)
	 */
	@Override
	public Object getAttribute (ObjectName name, String attribute)
		throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException
	{
		final AttributeList	al=getAttributes(name, new String[] { attribute });
		final int			numAttrs=(null == al) ? 0 : al.size();
		if (numAttrs <= 0)
			return null;
		if (numAttrs != 1)
			throw new MBeanException(new IllegalStateException("getAttribute(" + name + ")[" + attribute + "] ambiguous " + numAttrs + " results"));

		final Attribute		a=(Attribute) al.get(0);
		if (null == a)
			throw new AttributeNotFoundException("getAttribute(" + name + ")[" + attribute + "] no value returned though signaled");

		return a.getValue();
	}
	/*
	 * @see javax.management.MBeanServer#getClassLoader(javax.management.ObjectName)
	 */
	@Override
	public ClassLoader getClassLoader (ObjectName loaderName)
			throws InstanceNotFoundException
	{
		throw new UnsupportedOperationException("getClassLoader(" + loaderName + ") N/A");
	}
	/*
	 * @see javax.management.MBeanServer#getClassLoaderFor(javax.management.ObjectName)
	 */
	@Override
	public ClassLoader getClassLoaderFor (ObjectName mbeanName)
			throws InstanceNotFoundException
	{
		throw new UnsupportedOperationException("getClassLoaderFor(" + mbeanName + ") N/A");
	}
	/*
	 * @see javax.management.MBeanServer#getClassLoaderRepository()
	 */
	@Override
	public ClassLoaderRepository getClassLoaderRepository ()
	{
		throw new UnsupportedOperationException("getClassLoaderRepository() N/A");
	}
	/*
	 * @see javax.management.MBeanServer#queryMBeans(javax.management.ObjectName, javax.management.QueryExp)
	 */
	@Override
	public Set<ObjectInstance> queryMBeans (ObjectName name, QueryExp query)
	{
		throw new UnsupportedOperationException("queryMBeans(" + name + ") N/A");
	}
	/**
	 * Called by various implementations when no specific agent domain specified
	 * in order to provide some default agent access
	 * @return The default {@link MBeanServerConnection} to be used
	 * @throws Exception If cannot retrieve / provide such an instance
	 */
	public abstract MBeanServerConnection getDefaultMBeanServer () throws Exception;
	
	protected MBeanServerConnection resolveMBeanServerConnection (final String agentDomain) throws Exception
	{
		if ((agentDomain == null) || (agentDomain.length() <= 0))
			return getDefaultMBeanServer();

    	final Collection<? extends MBeanServer>	agentsList=MBeanServerFactory.findMBeanServer(null);
    	if ((agentsList == null) || (agentsList.size() <= 0))
    		return null;

    	for (final MBeanServer mbs : agentsList)
    	{
    		final String	defDomain=mbs.getDefaultDomain();
    		if (agentDomain.equalsIgnoreCase(defDomain))
    			return mbs;

    		final String[]	mbDomains=mbs.getDomains();
    		if ((mbDomains == null) || (mbDomains.length <= 0))
    			continue;
   
    		for (final String mbd : mbDomains)
    		{
    			if (agentDomain.equalsIgnoreCase(mbd))
    				return mbs;
    		}
    	}

    	return null;
	}
	/**
	 * @param agentDomain the required agent domain - if <code>null</code>/empty then
	 * some internal default will be used
	 * @return A {@link Collection} of all exported MBeans {@link ObjectName}-s
	 * @throws Exception if cannot access the names registry
	 */
	public Collection<ObjectName> listMBeansNames (String agentDomain) throws Exception
	{
		final MBeanServerConnection	s=resolveMBeanServerConnection(agentDomain);
		if (s == null)
			throw new ServiceNotFoundException("No agent for domain=" + agentDomain);

		return s.queryNames(null, null);
	}

	public Collection<MBeanEntryDescriptor> listMBeansDescriptors (String agentDomain, Collection<? extends Pattern> patterns) throws Exception
	{
		final Collection<ObjectName>	mbNames=listMBeansNames(agentDomain);
		final int						numMBeans=(null == mbNames) ? 0 : mbNames.size();
		if (numMBeans <= 0)
			return null;

		final Collection<MBeanEntryDescriptor>	descs=
			new ArrayList<MBeanEntryDescriptor>(numMBeans);
		for (final ObjectName objName: mbNames)
		{
			final String	nameVal=(objName == null) ? null : objName.getCanonicalName();
			if ((nameVal == null) || (nameVal.length() <= 0))
				continue;

			final Boolean	matches=RegexpUtils.checkPatterns(nameVal, patterns);
			if ((matches != null) && (!matches.booleanValue()))
				continue;

			descs.add(new MBeanEntryDescriptor(objName));
		}

		return descs;
	}

	public Collection<MBeanEntryDescriptor> listMBeansDescriptors (String agentDomain) throws Exception
    {
		return listMBeansDescriptors(agentDomain, null);
    }
	/*
	 * @see javax.management.MBeanServer#getMBeanCount()
	 */
	@Override
	public Integer getMBeanCount ()
	{
		final Set<? extends ObjectName>	mbn=queryNames(null, null);
		final int						numMBeans=(null == mbn) ? 0 : mbn.size();
		return Integer.valueOf(numMBeans);
	}
	/*
	 * @see javax.management.MBeanServer#getObjectInstance(javax.management.ObjectName)
	 */
	@Override
	public ObjectInstance getObjectInstance (ObjectName name)
			throws InstanceNotFoundException
	{
		throw new UnsupportedOperationException("getObjectInstance(" + name + ") N/A");
	}
	/*
	 * @see javax.management.MBeanServer#instantiate(java.lang.String, javax.management.ObjectName, java.lang.Object[], java.lang.String[])
	 */
	@Override
	public Object instantiate (String className, ObjectName loaderName, Object[] params, String[] signature)
		throws ReflectionException, MBeanException, InstanceNotFoundException
	{
		throw new UnsupportedOperationException("instantiate(" + className + ")[" + loaderName + "] N/A");
	}
	/*
	 * @see javax.management.MBeanServer#instantiate(java.lang.String, java.lang.Object[], java.lang.String[])
	 */
	@Override
	public Object instantiate (String className, Object[] params, String[] signature)
		throws ReflectionException, MBeanException
	{
			throw new UnsupportedOperationException("instantiate(" + className + ") N/A");
	}
	/*
	 * @see javax.management.MBeanServer#instantiate(java.lang.String, javax.management.ObjectName)
	 */
	@Override
	public Object instantiate (String className, ObjectName loaderName)
			throws ReflectionException, MBeanException, InstanceNotFoundException
	{
		return instantiate(className, loaderName, null, null);
	}
	/*
	 * @see javax.management.MBeanServer#instantiate(java.lang.String)
	 */
	@Override
	public Object instantiate (String className) throws ReflectionException, MBeanException
	{
		return instantiate(className, null, null);
	}
	/*
	 * @see javax.management.MBeanServer#invoke(javax.management.ObjectName, java.lang.String, java.lang.Object[], java.lang.String[])
	 */
	@Override
	public Object invoke (ObjectName name, String operationName, Object[] params, String[] signature)
			throws InstanceNotFoundException, MBeanException, ReflectionException
	{
		throw new UnsupportedOperationException("invoke(" + name + ")[" + operationName + "] N/A");
	}
	/*
	 * @see javax.management.MBeanServer#isInstanceOf(javax.management.ObjectName, java.lang.String)
	 */
	@Override
	public boolean isInstanceOf (ObjectName name, String className) throws InstanceNotFoundException
	{
		throw new UnsupportedOperationException("isInstanceOf(" + name + ")[" + className + "] N/A");
	}
	/*
	 * @see javax.management.MBeanServer#registerMBean(java.lang.Object, javax.management.ObjectName)
	 */
	@Override
	public ObjectInstance registerMBean (Object object, ObjectName name)
			throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException
	{
		throw new UnsupportedOperationException("registerMBean(" + name + ") N/A");
	}
	/*
	 * @see javax.management.MBeanServer#removeNotificationListener(javax.management.ObjectName, javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
	 */
	@Override
	public void removeNotificationListener (ObjectName name, ObjectName listener, NotificationFilter filter, Object handback)
			throws InstanceNotFoundException, ListenerNotFoundException
	{
		throw new UnsupportedOperationException("removeNotificationListener(" + name + ")[" + listener + "] N/A");
	}
	/*
	 * @see javax.management.MBeanServer#removeNotificationListener(javax.management.ObjectName, javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
	 */
	@Override
	public void removeNotificationListener (ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
		throws InstanceNotFoundException, ListenerNotFoundException
	{
			throw new UnsupportedOperationException("removeNotificationListener(" + name + ") N/A");
	}
	/*
	 * @see javax.management.MBeanServer#removeNotificationListener(javax.management.ObjectName, javax.management.ObjectName)
	 */
	@Override
	public void removeNotificationListener (ObjectName name, ObjectName listener)
			throws InstanceNotFoundException, ListenerNotFoundException
	{
		removeNotificationListener(name, listener, null, null);
	}
	/*
	 * @see javax.management.MBeanServer#removeNotificationListener(javax.management.ObjectName, javax.management.NotificationListener)
	 */
	@Override
	public void removeNotificationListener (ObjectName name, NotificationListener listener)
		throws InstanceNotFoundException, ListenerNotFoundException
	{
		removeNotificationListener(name, listener, null, null);
	}
	/*
	 * @see javax.management.MBeanServer#unregisterMBean(javax.management.ObjectName)
	 */
	@Override
	public void unregisterMBean (ObjectName name)
			throws InstanceNotFoundException, MBeanRegistrationException
	{
		throw new UnsupportedOperationException("unregisterMBean(" + name + ") N/A");
	}
	/*
	 * @see javax.management.MBeanServer#getDefaultDomain()
	 */
	@Override
	public String getDefaultDomain ()
	{
		try
		{
			final MBeanServerConnection s=getDefaultMBeanServer();
			return (null == s) ? null : s.getDefaultDomain();
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}
	}
	/*
	 * @see javax.management.MBeanServer#getDomains()
	 */
	@Override
	public String[] getDomains ()
	{
		try
		{
			final MBeanServerConnection s=getDefaultMBeanServer();
			return (null == s) ? null : s.getDomains();
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}
	}
	/*
	 * @see net.community.chest.web.servlet.jmx.AbstractJMXServlet#isRegistered(javax.management.ObjectName)
	 */
	@Override
	public boolean isRegistered (ObjectName name)
	{
		try
		{
			final MBeanServerConnection s=getDefaultMBeanServer();
			return (null == s) ? false : s.isRegistered(name);
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}
	}
	/*
	 * @see net.community.chest.web.servlet.jmx.AbstractJMXServlet#queryNames(javax.management.ObjectName, javax.management.QueryExp)
	 */
	@Override
	public Set<ObjectName> queryNames (ObjectName name, QueryExp query)
	{
		try
		{
			final MBeanServerConnection s=getDefaultMBeanServer();
			return (null == s) ? null : s.queryNames(name, query);
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}
	}
	/*
	 * @see net.community.chest.web.servlet.jmx.AbstractJMXServlet#setAttribute(javax.management.ObjectName, javax.management.Attribute)
	 */
	@Override
	public void setAttribute (ObjectName name, Attribute attribute)
			throws InstanceNotFoundException, AttributeNotFoundException,
				   InvalidAttributeValueException, MBeanException, ReflectionException
	{
		try
		{
			final MBeanServerConnection s=getDefaultMBeanServer();
			s.setAttribute(name, attribute);
		}
		catch(Exception e)
		{
			if (e instanceof InstanceNotFoundException)
				throw (InstanceNotFoundException) e;
			else if (e instanceof AttributeNotFoundException)
				throw (AttributeNotFoundException) e;
			else if (e instanceof InvalidAttributeValueException)
				throw (InvalidAttributeValueException) e;
			else if (e instanceof MBeanException)
				throw (MBeanException) e;
			else if (e instanceof ReflectionException)
				throw (ReflectionException) e;
			else
				throw ExceptionUtil.toRuntimeException(e);
		}
	}
	/*
	 * @see net.community.chest.web.servlet.jmx.AbstractJMXServlet#setAttributes(javax.management.ObjectName, javax.management.AttributeList)
	 */
	@Override
	public AttributeList setAttributes (ObjectName name, AttributeList attributes)
		throws InstanceNotFoundException, ReflectionException
	{
		try
		{
			final MBeanServerConnection s=getDefaultMBeanServer();
			return (s == null) ? null : s.setAttributes(name, attributes);
		}
		catch(Exception e)
		{
			if (e instanceof InstanceNotFoundException)
				throw (InstanceNotFoundException) e;
			else if (e instanceof ReflectionException)
				throw (ReflectionException) e;
			else
				throw ExceptionUtil.toRuntimeException(e);
		}
	}
	/*
	 * @see javax.management.MBeanServer#getAttributes(javax.management.ObjectName, java.lang.String[])
	 */
	@Override
	public AttributeList getAttributes (ObjectName name, String[] attributes)
			throws InstanceNotFoundException, ReflectionException
	{
		try
		{
			final MBeanServerConnection s=getDefaultMBeanServer();
			return (null == s) ? null : s.getAttributes(name, attributes);
		}
		catch(Exception e)
		{
			if (e instanceof InstanceNotFoundException)
				throw (InstanceNotFoundException) e;
			else if (e instanceof ReflectionException)
				throw (ReflectionException) e;
			else
				throw ExceptionUtil.toRuntimeException(e);
		}
	}
	/*
	 * @see javax.management.MBeanServer#getMBeanInfo(javax.management.ObjectName)
	 */
	@Override
	public MBeanInfo getMBeanInfo (ObjectName name)
			throws InstanceNotFoundException, IntrospectionException, ReflectionException
	{
		try
		{
			final MBeanServerConnection s=getDefaultMBeanServer();
			return (null == s) ? null : s.getMBeanInfo(name);
		}
		catch(Exception e)
		{
			if (e instanceof InstanceNotFoundException)
				throw (InstanceNotFoundException) e;
			else if (e instanceof ReflectionException)
				throw (ReflectionException) e;
			else if (e instanceof IntrospectionException)
				throw (IntrospectionException) e;
			else
				throw ExceptionUtil.toRuntimeException(e);
		}
	}
    /**
     * @return Default XML {@link Document} root element used if none found
     * in original request
     */
    public String getMBeansListRootElementName ()
    {
    	return JMXProtocol.DEFAULT_MBEANS_LIST_ROOT_ELEM_NAME;
    }
    /**
     * Writes the XML {@link Document} using a default {@link Transformer}
     * to the servlet's {@link OutputStream}
     * @param doc The {@link Document} to be dumped (may NOT be null)
     * @param resp The {@link HttpServletResponse} instance whose {@link OutputStream}
     * is to be used
     * @throws Exception if cannot access the servlet or transform the document
     */
    public void dumpDocument (final Document doc, final HttpServletResponse resp) throws Exception
	{
    	ServletUtils.dumpDocument(doc, resp);
	}
    /**
     * @param mbServer The {@link MBeanServerConnection} to use
	 * @param mbNames A {@link Collection}/array of the MBean(s) names whose
	 * information is requested
	 * @return Retrieved {@link Map} of MBeans where key=MBean name, value=its
	 * associated {@link MBeanInfo} - may be null/empty
     * @throws Exception if cannot access the local server
     */
	public Map<String,MBeanInfo> getMBeansInformation (final MBeanServerConnection	mbServer,
													   final Collection<String> 	mbNames)
			throws Exception
    {
    	return JMXUtils.getMBeansInformation(mbServer, null, mbNames);
    }
    /**
     * @param mbServer The {@link MBeanServerConnection} to use
     * @param mbNames A {@link Collection} of MBean(s) names whose attributes
     * we want
     * @return A {@link Map} whose key=the MBean name and the value=a
     * {@link Collection} of {@link MBeanAttributeDescriptor} each describing
     * an attribute of the associated MBeam 
     * @throws Exception if cannot access the JMX or process its results
     */
    public Map<String,Collection<MBeanAttributeDescriptor>> getMBeansAttributesMap (final MBeanServerConnection	mbServer,
    																				final Collection<String> 	mbNames)
    	throws Exception
    {
		final Map<String,MBeanInfo>								mbiMap=getMBeansInformation(mbServer, mbNames);
		final Map<String,Collection<MBeanAttributeInfo>>		mbaMap=JMXUtils.getMBeansAttributesInfo(mbiMap);
		return MBeanAttributeDescriptor.getMBeansAttributesDescriptors(mbaMap);
    }
    /**
     * @param mbServer The {@link MBeanServerConnection} to use
     * @param mbl A {@link Collection} of {@link MBeanEntryDescriptor}-s whose
     * attributes we want 
     * @return A {@link Map} whose key=the MBean name and the value=a
     * {@link Collection} of {@link MBeanAttributeDescriptor} each describing
     * an attribute of the associated MBean
     * @throws Exception if cannot access the JMX or process its results
     */
    public Map<String,Collection<MBeanAttributeDescriptor>> getMBeansAttributes (final MBeanServerConnection						mbServer,
    																			 final Collection<? extends MBeanEntryDescriptor>	mbl)
    		throws Exception
    {
		final int					numDescs=(null == mbl) ? 0 : mbl.size();
		final Collection<String>	mbNames=(numDescs <= 0) ? null : new ArrayList<String>(numDescs);
		if (numDescs > 0)	// unlikely
		{
			for (final MBeanEntryDescriptor mbe : mbl)
			{
				final String	mbName=mbe.getObjectName();
				if ((null == mbName) || (mbName.length() <= 0))
					continue;	// should not happen
				mbNames.add(mbName);
			}
		}

		return getMBeansAttributesMap(mbServer, mbNames);
    }

    public Map<String,MBeanEntryDescriptor> updateMBeansAttributes (
    		final MBeanServerConnection				mbServer,
    		final Map<String,MBeanEntryDescriptor>	descsMap)
    	throws Exception
    {
    	final Collection<String>	mbKeys=
    		((null == descsMap) || (descsMap.size() <= 0)) ? null : descsMap.keySet();
    	final int					numNames=(null == mbKeys) ? 0 : mbKeys.size();
    	// NOTE: we use a clone to avoid concurrent modification exception since updater may change the map...
    	final Collection<String>	mbNames=(numNames <= 0) ? null : new ArrayList<String>(mbKeys);
    	final Map<String,Collection<MBeanAttributeDescriptor>>	madMap=getMBeansAttributesMap(mbServer, mbNames);
    	return MBeanEntryDescriptor.updateDescriptorsMap(descsMap, mbNames, madMap);
    }
    /**
     * @param name MBean {@link ObjectName} whose attributes are requested
     * @param attributes The requested attributes' names
     * @return A {@link Map} whose key=attribute name and value=value
     * {@link Object} - may be null/empty if no values retrieved
     * @throws Exception if cannot retrieve the attributes
     */
	public Map<String,Object> getMBeanAttributesValuesMap (ObjectName name, String ... attributes) throws Exception
    {
    	final AttributeList	al=getAttributes(name, attributes);
    	if ((null == al) || (al.size() <= 0))
    		return null;

    	final Map<String,Object>	aMap=new TreeMap<String,Object>();
    	for (final Object a : al)
    	{
    		final String	aName=(a instanceof Attribute) ? ((Attribute) a).getName() : null;
    		if ((null == aName) || (aName.length() <= 0))
    			continue;
    		aMap.put(aName, ((Attribute) a).getValue());
    	}
   
    	return aMap;
    }

	public Map<String,Collection<Map.Entry<MBeanAttributeInfo,Object>>> getMBeansAttributes (
			MBeanServerConnection mbServer, Map<String,? extends Collection<String>> mbMap, boolean includeNulls)
		throws Exception
	{
		return JMXUtils.getMBeansAttributes(mbServer, mbMap, includeNulls, this);
	}

	public Map<String,MBeanEntryDescriptor> updateMBeansAttributesValues (
							final MBeanServerConnection				mbServer,
							final Map<String,MBeanEntryDescriptor>	descsMap,
							final boolean includeNulls)
			throws Exception
    {
    	final Collection<MBeanEntryDescriptor>	descs=
    		((null == descsMap) || (descsMap.size() <= 0)) ? null : descsMap.values();
		final Map<String,Collection<String>>	mbMap=MBeanEntryDescriptor.buildAttributesMap(descs);
		if ((null == mbMap) || (mbMap.size() <= 0))
			return null;

		final Map<String,Collection<Map.Entry<MBeanAttributeInfo,Object>>>	mbValues=
			getMBeansAttributes(mbServer, mbMap, includeNulls);
		return MBeanEntryDescriptor.updateMBeansAttributesValues(descsMap, mbValues);
    }

    public Collection<? extends MBeanEntryDescriptor> processDescriptorsRequest (
							final Collection<? extends MBeanEntryDescriptor>	inDescs,
							final String										agentDomain,
							final boolean										fetchAttributes,
							final boolean										fetchValues,
							final boolean										includeNulls,
							final boolean										fetchOperations,
							final boolean										fetchParams)
				throws Exception
	{
    	final int	numDescs=(null == inDescs) ? 0 : inDescs.size();
    	if (numDescs <= 0)
    		return inDescs;

		if (fetchAttributes || fetchValues || fetchOperations || fetchParams)
		{
	    	final MBeanServerConnection	mbServer=resolveMBeanServerConnection(agentDomain);
			if (mbServer == null)
				throw new ServiceNotFoundException("No agent for domain=" + agentDomain);
			
	    	final Map<String,MBeanEntryDescriptor>	descsMap=new TreeMap<String,MBeanEntryDescriptor>();
	    	for (final MBeanEntryDescriptor d : inDescs)
	    	{
	    		final String	mbName=(null == d) ? null : d.getObjectName();
	    		if ((null == mbName) || (mbName.length() <= 0))
	    			continue;
	    		descsMap.put(mbName, d);
	    	}

	    	final Map<String,? extends MBeanInfo>								infosMap=
	    		getMBeansInformation(mbServer, descsMap.keySet());
	    	final Collection<? extends Map.Entry<String,? extends MBeanInfo>>	infoEntries=
	    		((infosMap == null) || (infosMap.size() <= 0)) ? null : infosMap.entrySet();
	    	if ((infoEntries != null) && (infoEntries.size() > 0))
	    	{
	    		for (final Map.Entry<String,? extends MBeanInfo> ie : infoEntries)
	    		{
	    			final String	mbName=(ie == null) ? null : ie.getKey();
	    			final MBeanInfo	mbInfo=(ie == null) ? null : ie.getValue();
	    			if ((mbName == null) || (mbName.length() <= 0) || (mbInfo == null))
	    				continue;	// should not happen

	    			final MBeanEntryDescriptor	desc=descsMap.get(mbName);
	    			if (desc == null)
	    				throw new IllegalStateException("processDescriptorsRequest(" + mbName + ") no descriptor");
	    			desc.fromMBeanInfo(mbInfo);
	    		}
	    	}

	    	if (fetchValues)
	    		updateMBeansAttributesValues(mbServer, descsMap, includeNulls);
		}

		return inDescs;
	}

    protected boolean doListMBeansInfo (final HttpServletRequest	req,
    									final HttpServletResponse	resp,
    									final Boolean				fetchAttributesParam,
    									final Boolean				fetchValuesParam,
    									final Boolean				includeNullsParam,
    									final Boolean				fetchOperationsParam,
    									final Boolean				fetchParamsParam)
    	throws Exception
    {
		if ((null == req) || (null == resp))	// just so compiler does not complain
			throw new ServletException("No request/response");

		final ServletRequestParameters						reqParams=
					new ServletRequestParameters(req);
		final String										mbName=
					reqParams.getParameter(JMXProtocol.NAME_PARAM),
															agentDomain=
					reqParams.getParameter(JMXProtocol.DOMAIN_PARAM);
   		final Collection<? extends MBeanEntryDescriptor>	descs;
		if ((null == mbName) || (mbName.length() <= 0))
			descs = listMBeansDescriptors(agentDomain);
		else if (mbName.indexOf('*') >= 0)	// check if wildcard
			descs = listMBeansDescriptors(agentDomain, RegexpUtils.getPatternsList(mbName, ','));
		else
			descs = Arrays.asList(new MBeanEntryDescriptor(mbName));

		final boolean										fetchAttributes=
			(fetchAttributesParam == null) ? reqParams.getFlagParameter(JMXProtocol.ATTRIBUTES_PARAM, false) : fetchAttributesParam.booleanValue(),
															fetchValues=
			(fetchValuesParam == null) ? reqParams.getFlagParameter(JMXProtocol.VALUES_PARAM, false) : fetchValuesParam.booleanValue(),
															includeNulls=
			(includeNullsParam == null) ? reqParams.getFlagParameter(JMXProtocol.NULLS_PARAM, false) : includeNullsParam.booleanValue(),
															fetchOperations=
			(fetchOperationsParam == null) ? reqParams.getFlagParameter(JMXProtocol.OPERATIONS_PARAM, false) : fetchOperationsParam.booleanValue(),
															fetchParams=
			(fetchParamsParam == null) ? reqParams.getFlagParameter(JMXProtocol.PARAMS_PARAM, false) : fetchParamsParam.booleanValue();
		final Collection<? extends MBeanEntryDescriptor>	retDescs=
   			processDescriptorsRequest(descs,
   									  agentDomain,
   									  fetchAttributes,
   									  fetchValues,
   									  includeNulls,
   									  fetchOperations,
   									  fetchParams);
		final Document	doc=JMXUtils.buildDescriptorsDocument(getMBeansListRootElementName(), retDescs,
										fetchAttributes, fetchValues, includeNulls, fetchOperations, fetchParams);
		dumpDocument(doc, resp);
		return true;
    }
    // request format: req=list[&domain=some domain][&name=some name][&attributes=true/false(default)][&values=true/false(default)][&null=true/false(default)][operations=true/false][params=true/false]
    protected boolean doListMBeansInfo (HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
    	return doListMBeansInfo(req, resp, null, null, null, null, null);
	}
    // request format: req=all[&domain=some domain][&name=some name]
    protected boolean doAllMBeansInfo (HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
    	return doListMBeansInfo(req, resp, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE, Boolean.TRUE);
	}
	/**
	 * @param req The original {@link HttpServletRequest} assumed to contain
	 * a list of XML encoded {@link MBeanEntryDescriptor}-s.
	 * @return A "pair" represented as a {@link java.util.Map.Entry} whose key=root XML
	 * element name, and value=a {@link Collection} of {@link MBeanEntryDescriptor}
	 * extracted from the XML. May be null/empty if no definitions extracted 
	 * @throws Exception if cannot parse the XML
	 */
	public static final Map.Entry<String,Collection<MBeanEntryDescriptor>> getDescriptors (HttpServletRequest req) throws Exception
	{
		final Document							doc=ServletUtils.loadDocument(req);
		final Element							rootElem=doc.getDocumentElement();
		final String							tagName=rootElem.getTagName();
		final Collection<MBeanEntryDescriptor>	desc=MBeanEntryDescriptor.readMBeans(rootElem, null);
		if ((null == desc) || (desc.size() <= 0))
			return null;

		return new MapEntryImpl<String,Collection<MBeanEntryDescriptor>>(tagName, desc);	
	}
    // request format: req=get[&null=true/false(default)] - payload must be an XML
    protected boolean doGetMBeansInfo (HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		final Map.Entry<String,? extends Collection<? extends MBeanEntryDescriptor>>	reqPair=
				getDescriptors(req);
		final ServletRequestParameters													reqParams=
				new ServletRequestParameters(req);
		final boolean																	fetchAttributes=
			reqParams.getFlagParameter(JMXProtocol.ATTRIBUTES_PARAM, false),
																						fetchValues=
			reqParams.getFlagParameter(JMXProtocol.VALUES_PARAM, false),
																						includeNulls=
			reqParams.getFlagParameter(JMXProtocol.NULLS_PARAM, false),
																						fetchOperations=
			reqParams.getFlagParameter(JMXProtocol.OPERATIONS_PARAM, false),
																						fetchParams=
			reqParams.getFlagParameter(JMXProtocol.PARAMS_PARAM, false);
		final Collection<? extends MBeanEntryDescriptor>								descs=
			(null == reqPair) ? null : reqPair.getValue(),
																						retDescs=
   			processDescriptorsRequest(descs, reqParams.getParameter(JMXProtocol.DOMAIN_PARAM),
						  			  fetchAttributes,
						  			  fetchValues,
						  			  includeNulls,
						  			  fetchOperations,
						  			  fetchParams);

		final Document	doc=
			JMXUtils.buildDescriptorsDocument((null == reqPair) ? null : reqPair.getKey(), retDescs,
						fetchAttributes, fetchValues, includeNulls, fetchOperations, fetchParams);
		dumpDocument(doc, resp);
		return true;
	}
    
    protected boolean doListJMXAgents (HttpServletRequest req, HttpServletResponse resp) throws Exception
    {
		final ServletRequestParameters			reqParams=new ServletRequestParameters(req);
    	final String							reqAgentId=reqParams.getParameter(JMXProtocol.NAME_PARAM);
    	final Collection<? extends MBeanServer>	agentsList=MBeanServerFactory.findMBeanServer(reqAgentId);
    	final Document							doc=JMXUtils.buildAgentsDocument(JMXProtocol.AGENTS_REQ, agentsList);
		dumpDocument(doc, resp);
		return true;
    }
    /**
     * Called by default {@link #processRequest(HttpServletRequest, HttpServletResponse)}
     * implementation when an unknown request found in query parameters
     * @param opReq Extracted request opcode - may be null/empty if the
     * request query does not contain a {@link JMXProtocol#REQ_PARAM} value
     * @param req Original {@link HttpServletRequest}
     * @param resp Original {@link HttpServletResponse}
     * @return TRUE if request processed or the <code>super</code> method
     * should be called.
     * @throws Exception by default - unless overridden
     */
    protected boolean processUnknownRequest (String opReq, HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
    	if (null == req)
    		throw new ServletException("processUnknownRequest(" + opReq + ") no request");
    	if (null == resp)
    		throw new IOException("processUnknownRequest(" + opReq + ") no response");

    	throw new UnsupportedOperationException("Unknown request type: " + opReq);
	}
    /**
     * Called by various {@link HttpServlet} overrides (e.g., {@link #doGet(HttpServletRequest, HttpServletResponse)}
     * in order to allow for specialized requests handling
     * @param req Original {@link HttpServletRequest}
     * @param resp Original {@link HttpServletResponse}
     * @return TRUE if request processed or the <code>super</code> method
     * should be called.
     * @throws ServletException as per {@link HttpServlet} specification
     * @throws IOException as per {@link HttpServlet} specification
     */
    protected boolean processRequest (HttpServletRequest req, HttpServletResponse resp)
    	throws ServletException, IOException
    {
    	if ((null == req) || (null == resp))
    		throw new ServletException("No request/response");
 
    	try
    	{
    		final String	opReq=req.getParameter(JMXProtocol.REQ_PARAM);
    		if (JMXProtocol.ALL_REQ.equalsIgnoreCase(opReq))
    			return doAllMBeansInfo(req, resp);
    		else if (JMXProtocol.LIST_REQ.equalsIgnoreCase(opReq))
    			return doListMBeansInfo(req, resp);
    		else if (JMXProtocol.GET_REQ.equals(opReq))
    			return doGetMBeansInfo(req, resp);
    		else if (JMXProtocol.INVOKE_REQ.equals(opReq))
    			return doInvokeMBeanOperation(req, resp);
    		else if (JMXProtocol.AGENTS_REQ.equals(opReq))
    			return doListJMXAgents(req, resp);
    		else if (JMXProtocol.WHEREAMI_REQ.equalsIgnoreCase(opReq))
    			return showWhereAmI(resp);
    		else if (JMXProtocol.SYSPROPS_REQ.equalsIgnoreCase(opReq))
    			return showSystemProperties(resp);
    		else if (JMXProtocol.ENV_REQ.equalsIgnoreCase(opReq))
    			return showEnvironment(resp);
    		else if (JMXProtocol.CONTEXT_REQ.equalsIgnoreCase(opReq))
    			return showContext(getServletContext(), resp);
    		else if (JMXProtocol.CONFIG_REQ.equalsIgnoreCase(opReq))
    			return showConfiguration(getServletConfig(), resp);
    		else if (JMXProtocol.VERINFO_REQ.equalsIgnoreCase(opReq))
    			return showVersionInfo(resp);
    		else
    			return processUnknownRequest(opReq, req, resp);
    	}
    	catch(Exception e)
    	{
    		if (e instanceof ServletException)
    			throw (ServletException) e;
    		if (e instanceof IOException)
    			throw (IOException) e;

    		throw new StreamCorruptedException(e.getClass().getName() + ": " + e.getMessage());
    	}
    }
    /*
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet (HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		if (!processRequest(req, resp))
			super.doGet(req, resp);
	}
	/*
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost (HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException
	{
		if (!processRequest(req, resp))
			super.doPost(req, resp);
	}

	protected boolean showWhereAmI (HttpServletResponse resp)
		throws IOException, URISyntaxException
	{
		final File	f=ResourceDataRetriever.getAnchorClassContainerLocation(getClass());
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");

		try(PrintWriter	w=resp.getWriter()) {
			w.append(f.getAbsolutePath());
		}

		return true;
	}

	private static final Comparator<Map.Entry<Object,Object>>	PROPENTRY_COMPARATOR=
		new Comparator<Map.Entry<Object,Object>> () {
			/*
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare (Map.Entry<Object,Object> o1, Map.Entry<Object,Object> o2)
			{
				final Object	k1=(o1 == null) ? null : o1.getKey(),
								k2=(o2 == null) ? null : o2.getKey();
				final String	s1=(k1 == null) ? null : k1.toString(),
								s2=(k2 == null) ? null : k2.toString();
				return StringUtil.compareDataStrings(s1, s2, true);
			}
		};

	protected static final <W extends PrintWriter> W writeMapEntries (final W w, final Collection<? extends Map.Entry<?,?>> el)
	{
		if ((el == null) || el.isEmpty())
			return w;

		for (final Map.Entry<?,?> ep : el)
			w.append(String.valueOf(ep.getKey()))
			 .append('=')
			 .append(String.valueOf(ep.getValue()))
			 .println()
			 ;
		return w;
	}

	protected static final <W extends PrintWriter> W writeMap (final W w, final Map<?,?> m)
	{
		return ((m == null) || m.isEmpty()) ? w : writeMapEntries(w, m.entrySet());
	}

	protected boolean showSystemProperties (HttpServletResponse resp)
		throws IOException
	{
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");

		final Properties	p=System.getProperties();
		try(PrintWriter	w=resp.getWriter()) {
			if ((p == null) || (p.size() <= 0))
			{
				w.println("No properties");
			}
			else
			{
				final List<Map.Entry<Object,Object>>	el=new ArrayList<Map.Entry<Object,Object>>(p.entrySet());
				Collections.sort(el, PROPENTRY_COMPARATOR);
				writeMapEntries(w, el);
			}
		}

		return true;
	}

	protected boolean showEnvironment (HttpServletResponse resp)
		throws IOException
	{
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");

		final Map<String,String>	p=System.getenv();
        try(PrintWriter w=resp.getWriter()) {
			if ((null == p) || (p.size() <= 0))
			{
				w.println("No environment");
			}
			else
			{
				writeMap(w, (p instanceof SortedMap<?,?>) ? (SortedMap<String,String>) p : new TreeMap<String,String>(p)); 
			}
		}

		return true;
	}

	protected boolean showContext (ServletContext ctx, HttpServletResponse resp)
		throws IOException
	{
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");

		final SortedMap<String,Object>	contextParams=new TreeMap<String,Object>();
		contextParams.put("Context-Path: ", ctx.getContextPath());
		contextParams.put("Context-Name: ", ctx.getServletContextName());
		contextParams.put("Server-Info: ", ctx.getServerInfo());
		contextParams.put("Major-Version: ", Integer.valueOf(ctx.getMajorVersion()));
		contextParams.put("Minor-Version: ", Integer.valueOf(ctx.getMinorVersion()));
		contextParams.put("WEB-INF-Path: ", ctx.getRealPath("WEB-INF"));

        try(PrintWriter w=resp.getWriter()) {
			writeMap(w, contextParams);
		}

		return true;
	}
	
	protected boolean showConfiguration (ServletConfig config, HttpServletResponse resp)
		throws IOException
	{
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");

		final SortedMap<String,String>	paramsMap=new TreeMap<String,String>();
		for (final Enumeration<?>	names=config.getInitParameterNames(); (names != null) && names.hasMoreElements(); )
		{
			final String	name=String.valueOf(names.nextElement()),
							value=config.getInitParameter(name),
							prev=paramsMap.put(name, value);
			if (prev != null)	// debug breakpoints
				continue;
		}

        try(PrintWriter w=resp.getWriter()) {
			if (paramsMap.isEmpty())
			{
				w.println("No initialization parameters");
			}
			else
			{
				writeMap(w, paramsMap); 
			}
		}

		return true;
	}

	protected boolean showVersionInfo (HttpServletResponse resp)
		throws IOException
	{
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");
		
        try(PrintWriter w=resp.getWriter()) {
			w.append("Version: ").println(Version.getVersionString());
			w.append("Build timestamp: ").println(Version.getBuildTimestampString());
		}

		return true;
	}
	
	protected boolean doInvokeMBeanOperation (HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		final ServletRequestParameters	reqParams=new ServletRequestParameters(req);
		final String					agentDomain=reqParams.getParameter(JMXProtocol.DOMAIN_PARAM);
		final MBeanServerConnection		server=resolveMBeanServerConnection(agentDomain);
		if (server == null)
			throw new NoSuchElementException("No agent domain server found");

		final String	mbName=reqParams.getParameter(JMXProtocol.NAME_PARAM);
		if ((mbName == null) || (mbName.length() <= 0))
			throw new IllegalArgumentException("No MBean name specified");

		final String	opName=reqParams.getParameter(JMXProtocol.OPNAME_PARAM);
		final Object	result;
		if (!reqParams.getFlagParameter(JMXProtocol.PARAMLESS_PARAM, false))
		{
			final Document					doc=ServletUtils.loadDocument(req);
			final Element					rootElem=doc.getDocumentElement();
			final MBeanOperationDescriptor	opDesc=new MBeanOperationDescriptor(rootElem);
			if ((opName != null) && (opName.length() > 0))
				opDesc.setName(opName);

			final Collection<? extends MBeanParameterDescriptor>	params=opDesc.getParameters();
			final int												numParams=(params == null) ? 0 : params.size();
			final Object[]											paramVals=new Object[numParams];
			if (numParams > 0)
			{
				int	pIndex=0;
				for (final MBeanParameterDescriptor p : params)
				{
					paramVals[pIndex] = p.getValue();
					pIndex++;
				}
			}

			final boolean	uniqueName=reqParams.getFlagParameter(JMXProtocol.UNIQUE_PARAM, true);
			if (uniqueName)
				result = JMXUtils.invokeUniqueMBeanOperation(server, new ObjectName(mbName), opDesc.getName(), paramVals);
			else	// TODO implement non-unique operation matching
				throw new UnsupportedOperationException("Non-unique operations N/A yet");
		}
		else
			result = JMXUtils.invokeUniqueMBeanOperation(server, new ObjectName(mbName), opName);

		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain");
		
        try(PrintWriter w=resp.getWriter()) {
			w.append(String.valueOf(result));
		}

		return true;
	}
}
