package net.community.chest.jmx;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.io.EOLStyle;
import net.community.chest.jmx.dom.MBeanAttributeDescriptor;
import net.community.chest.jmx.dom.MBeanEntryDescriptor;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.reflect.MethodsMap;
import net.community.chest.util.map.MapEntryImpl;
import net.community.chest.util.map.entries.MapEntryKeyStringComparator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful static methods for JMX</P>
 * 
 * @author Lyor G.
 * @since Aug 14, 2007 2:17:42 PM
 */
public final class JMXUtils {
	private JMXUtils ()
	{
		// no instance
	}

	public static final Object getMBeanAttributeValue (final String attrTypeClass, final String attrValue) throws Exception
	{
		final Class<?>						attrType=
				ClassUtil.resolveDataType(attrTypeClass);
		final ValueStringInstantiator<?>	inst=
				ClassUtil.getJDKStringInstantiator(attrType);
		if (null == inst)
			throw new UnsupportedOperationException(ClassUtil.getArgumentsExceptionLocation(attrType, "getMBeanAttributeValue", attrTypeClass, attrValue) + " no instantiator");

		return inst.newInstance(attrValue);
	}
	/**
	 * @param mbi The {@link MBeanInfo} instance to use - may be null
	 * @return A {@link Map} where key=attribute name (case <U>insensitive</U>)
	 * and value is its associated {@link MBeanAttributeInfo}. May be
	 * null/empty if no initial {@link MBeanInfo} instance or no values
	 */
	public static final Map<String,MBeanAttributeInfo> getAttributesMap (final MBeanInfo mbi)
	{
		final MBeanAttributeInfo[]				mbAttrs=
			(null == mbi) ? null : mbi.getAttributes();
		final int								numAttrs=
			(null == mbAttrs) ? 0 : mbAttrs.length;
		final Map<String,MBeanAttributeInfo>	attrsMap=
			(numAttrs <= 0) ? null : new TreeMap<String, MBeanAttributeInfo>(String.CASE_INSENSITIVE_ORDER);
		if (numAttrs > 0)
		{
			for (final MBeanAttributeInfo aInfo : mbAttrs)
			{
				if (aInfo != null)	// should not be otherwise
					attrsMap.put(aInfo.getName(), aInfo);
			}
		}

		return attrsMap;
	}

	public static final AttributeList setAttributesValues (final MBeanServerConnection s, final ObjectName name, final Collection<? extends MBeanAttributeDescriptor> aValues) throws Exception
	{
		final int	numValues=(null == aValues) ? 0 : aValues.size();
		if (numValues <= 0)
			return null;

		final MBeanInfo							mbi=s.getMBeanInfo(name);
		final Map<String,MBeanAttributeInfo>	attrsMap=getAttributesMap(mbi);
		final AttributeList						aList=new AttributeList(numValues);
		for (final MBeanAttributeDescriptor av : aValues)
		{
			if (null == av)	// should not happen
				continue;

			final String	attrName=av.getName(), attrType=av.getType();
			final Object	attrValue, avValue=av.getValue();
			if ((null == attrType) || (attrType.length() <= 0))
			{
				final MBeanAttributeInfo	aInfo=
						((null == attrsMap) || (attrsMap.size() <= 0) 		// should not happen
					 || (null == attrName) || (attrName.length() <= 0))	/* should not happen */
				  	 ? null : attrsMap.get(attrName);

				final String	avString=(null == avValue) ? null : avValue.toString();
				attrValue = getMBeanAttributeValue((null == aInfo) /* should not happen */ ? null : aInfo.getType(), avString);
			}
			else
				attrValue = avValue;

			final Attribute	a=new Attribute(attrName, attrValue);
			aList.add(a);
		}

		return s.setAttributes(name, aList);
	}

	public static Map<String,ReflectiveMBeanAttributeInfo> buildMBeanAttributes (final Collection<? extends AttributeAccessor>	accs)
	{
		if ((null == accs) || (accs.size() <= 0))
			return null;

		final Map<String,ReflectiveMBeanAttributeInfo>	res=new TreeMap<String, ReflectiveMBeanAttributeInfo>(String.CASE_INSENSITIVE_ORDER);
		for (final AttributeAccessor a : accs)
		{
			try
			{
				final ReflectiveMBeanAttributeInfo	aInfo=new ReflectiveMBeanAttributeInfo(a.getName(), "Generic MBean Attribute", a.getGetter(), a.getSetter());
				res.put(aInfo.getName(), aInfo);
			}
			catch(Exception e)
			{
				throw ExceptionUtil.toRuntimeException(e);
			}
		}

		return res;
	}

	public static Map<String,ReflectiveMBeanAttributeInfo> buildMBeanAttributes (final Map<?,? extends AttributeAccessor> attrs)
	{
		return buildMBeanAttributes(((null == attrs) || (attrs.size() <= 0)) ? null : attrs.values());
	}

	public static final MethodsMap<ReflectiveMBeanOperationInfo> buildMBeanOperations (final Class<?> instClass, final Collection<? extends Method> opers)
	{
		if ((null == opers) || (opers.size() <= 0))
			return null;

		final MethodsMap<ReflectiveMBeanOperationInfo>	res=new MethodsMap<ReflectiveMBeanOperationInfo>(ReflectiveMBeanOperationInfo.class);
		for (final Method m : opers)
		{
			final ReflectiveMBeanOperationInfo	oInfo=new ReflectiveMBeanOperationInfo("Generic MBean Operation", m);
			res.put(instClass, m, oInfo);
		}

		return res;
	}

	public static final MethodsMap<ReflectiveMBeanOperationInfo> buildMBeanOperations (final Class<?> instClass, final Map<?, ? extends Method> opers)
	{
		return buildMBeanOperations(instClass, ((null == opers) || (opers.size() <= 0)) ? null : opers.values());
	}
	
	public static final MBeanRegistrationResult registerMBeans (final MBeanServer mbs, final Collection<? extends MBeanEntryDescriptor> descs)
	{
		final MBeanRegistrationResult	res=new MBeanRegistrationResult();
		if ((descs != null) && (descs.size() > 0))
		{
			for (final MBeanEntryDescriptor d : descs)
			{
				final String	objName=d.getObjectName(),
								mbClassName=d.getClassName();
				try
				{
					final ObjectName		name=new ObjectName(objName);
					final Class<?>			mbClass=ClassUtil.loadClassByName(mbClassName);
					// NOTE !!! we assume only the default (public) constructor
					final Object			mbInst=mbClass.newInstance();
					final ObjectInstance	inst=mbs.registerMBean(mbInst, name);
					if (null == inst)
						throw new InstanceNotFoundException("No object instance created");

					final Collection<? extends MBeanAttributeDescriptor> aValues=d.getAttributes();
					if ((aValues != null) && (aValues.size() > 0))
					{
						try
						{
							setAttributesValues(mbs, name, aValues);
						}
						catch(Exception e)
						{
							res.addRegistrationException(objName, e);
						}
					}

					res.addRegisteredMBean(inst);
				}
				catch(Exception e)
				{
					res.addRegistrationException(objName, e);
				}
			}
		}		

		return res;
	}
    /**
     * Special "shortcut" invocation for operations that have a unique
     * name - i.e., don't need the signature in order to resolved overloaded
     * operations. If several overloaded methods exist, then only the
     * <U>number of parameters</U> is used to resolved them.
     * @param s The {@link MBeanServerConnection} to use in order to query
     * about the MBean's operations and ivoking them
     * @param name The {@link ObjectName} of the MBean on which the method is
     * to be invoked.
     * @param operationName The name of the operation to be invoked.
     * @param args An array containing the parameters to be set when
     * the operation is invoked. The number of arguments is used to match
     * the correct operation (as well as the name)
     * @return The object returned by the operation, which represents
     * the result of invoking the operation on the MBean specified.
     * @throws InstanceNotFoundException The MBean specified is not
     * registered in the MBean server.
     * @throws MBeanException Wraps an exception thrown by the
     * MBean's invoked method.
     * @throws ReflectionException Wraps a {@link Exception} thrown while
     * trying to invoke the method
     * @throws IntrospectionException if cannot retrieve {@link MBeanInfo} to
     * be used for invocation resolution - especially if ambiguous operation
     * name - i.e., the operation name is not unique and several overloads
     * with same number of parameters found
     * @throws IOException 
     */
	public static final Object invokeUniqueMBeanOperation (
			MBeanServerConnection s, ObjectName name, String operationName, Object... args)
		throws InstanceNotFoundException, MBeanException, ReflectionException, IntrospectionException, IOException
	{
		if ((null == operationName) || (operationName.length() <= 0))
			throw new IntrospectionException("invokeMBeanOperation(" + name + ")[" + operationName + "] no operation specified");

		final MBeanInfo	mbi=s.getMBeanInfo(name);
		if (null == mbi)
			throw new InstanceNotFoundException("invokeUniqueMBeanOperation(" + name + ")[" + operationName + "] no " + MBeanInfo.class.getName() + " instance retrieved");

		final MBeanOperationInfo[]	ops=mbi.getOperations();
		if ((null == ops) || (ops.length <= 0))
			throw new IntrospectionException("invokeMBeanOperation(" + name + ")[" + operationName + "] no operations available");

		final int			numArgs=(null == args) ? 0 : args.length;
		MBeanOperationInfo	resOp=null;

		for (final MBeanOperationInfo o : ops)
		{
			final String	oName=(null == o) /* should not happen */ ? null : o.getName();
			if (!operationName.equals(oName))
				continue;

			// make sure no ambiguity
			if (resOp != null)
				throw new IntrospectionException("invokeMBeanOperation(" + name + ")[" + operationName + "] ambiguous operation");

			final MBeanParameterInfo[]	params=o.getSignature();
			final int					numParams=(null == params) ? 0 : params.length;
			if (numParams != numArgs)
				continue;

			resOp = o;	// continue checking to detect ambiguity
		}

		if (null == resOp)
			throw new IntrospectionException("invokeMBeanOperation(" + name + ")[" + operationName + "] no match found");

		final MBeanParameterInfo[]	oPars=resOp.getSignature();
		final int					numOpPars=(null == oPars) /* OK */ ? 0 : oPars.length;
		final String[]				oSig=new String[numOpPars];
		for (int	pIndex=0; pIndex < numOpPars; pIndex++)
		{
			final MBeanParameterInfo	pInfo=oPars[pIndex];
			oSig[pIndex] = (null == pInfo)	/* should not happen */ ? null : pInfo.getType();
		}

		return s.invoke(name, operationName, args, oSig);
	}
	/**
	 * @param defDomain MBean domain to be used to locate the server - if
	 * null/empty the using first available. If none defined so far then one
	 * is created with the specified domain
	 * @return The created/located {@link MBeanServer}
	 * @throws InstanceNotFoundException if internal lookup errors
	 */
	public static final MBeanServer getLocalMBeanServer (final String defDomain) throws InstanceNotFoundException
	{
		// TODO look into using MBeanServer ManagementFactory.getPlatformMBeanServer();
		final boolean						lookupSpecificDomain=(defDomain != null) && (defDomain.length() > 0);
		final List<? extends MBeanServer>	srvList=MBeanServerFactory.findMBeanServer(null);
		final int							numServers=(null == srvList) ? 0 : srvList.size();
		MBeanServer							lclServer=null;
		if (numServers > 0)
		{
			if (lookupSpecificDomain)
			{
				for (final MBeanServer s : srvList)
				{
					final String	sDomain=(null == s) /* should not happen */ ? null : s.getDefaultDomain();
					if (defDomain.equalsIgnoreCase(sDomain))
					{
						lclServer = s;
						break;
					}
				}
			}
			else	// if not using a specific domain, the use first returned one
			{
				if (null == (lclServer=srvList.get(0)))
					throw new InstanceNotFoundException("getLocalMBeanServer(" + defDomain + ") no default instance found though " + numServers + " available");
			}
		}

		// if not found a server with the specified domain (if any) then create it
		if (null == lclServer)
		{
			lclServer = lookupSpecificDomain
					? MBeanServerFactory.createMBeanServer(defDomain)
					: MBeanServerFactory.createMBeanServer()
					;
			if (null == lclServer)
				throw new InstanceNotFoundException("getLocalMBeanServer(" + defDomain + ") no instance created");
		}

		return lclServer;
	}
	/**
	 * Can be used to format into text retrieved MBean value
	 * @param dataType The value data type class name
	 * @param value The retrieved value
	 * @param useCRLF TRUE=use CRLF for {@link Collection}/{@link Map}/array
	 * values separator. FALSE=use comma
	 * @return A {@link String} with the best effort approximation of the
	 * value's human-readable text
	 */
	public static final String formatMBeanValueText (final String dataType, final Object value, final boolean useCRLF)
	{
		// don't display "null" if no value
		if (null == value)
			return "";
	
		if ((dataType != null) && (dataType.length() > 0))
		{
			try
			{
				final Class<?>	dClass=ClassUtil.resolveDataType(dataType);
				if ((null == dClass) || ClassUtil.isAtomicClass(dClass))
					return value.toString();

				if (Date.class.isAssignableFrom(dClass) || Calendar.class.isAssignableFrom(dClass))
				{
					final DateFormat	dtf=DateFormat.getDateTimeInstance();
					final Date			dv=Calendar.class.isAssignableFrom(dClass)
						? ((Calendar) value).getTime()
						: (Date) value
						;
					if (null == dtf)
						return String.valueOf(dv.getTime());

					synchronized(dtf)
					{
						return dtf.format(dv);
					}
				}

				// special handling for array/collection of objects
				final Object[]	vals;
				if (dClass.isArray())
				{
					// display empty array as empty string
					if ((null == (vals=Object[].class.cast(value))) || (vals.length <= 0))
						return "";
				}
				else if (Collection.class.isAssignableFrom(dClass))
				{
					final Collection<?>	c=Collection.class.cast(value);
					final int			cSize=(null == c) ? 0 : c.size();
					vals = (cSize <= 0) ? null : c.toArray(new Object[cSize]);
	
					// display empty collection as empty string
					if ((null == vals) || (vals.length <= 0))
						return "";
				}
				else if (Map.class.isAssignableFrom(dClass))
				{
					final Map<?,?>								m=Map.class.cast(value);
					final Collection<? extends Map.Entry<?,?>>	em=((null == m) || (m.size() <= 0)) ? null : m.entrySet();
					final int									mSize=(null == em) ? 0 : em.size();
					final Map.Entry<?,?>[]						ea=(mSize <= 0) ? null : em.toArray(new Map.Entry[mSize]);
					final int									eaSize=(null == ea) ? 0 : ea.length;
					if (eaSize > 1)	// sort the map entries according to the key
						Arrays.sort(ea, MapEntryKeyStringComparator.CASE_SENSITIVE);
	
					final Collection<String>	vsc=(eaSize <= 0) ? null : new ArrayList<String>(eaSize);
					if (mSize > 0)
					{
						for (final Map.Entry<?,?> ev : ea)
						{
							if (null == ev)
								continue;
							vsc.add(ev.getKey() + "=" + ev.getValue());
						}
					}
	
					final int	cSize=(null == vsc) ? 0 : vsc.size();
					vals = (cSize <= 0) ? null : vsc.toArray(new Object[cSize]);
	
					// display empty map as empty string
					if ((null == vals) || (vals.length <= 0))
						return "";
				}
				else
					vals = null;
	
				final int			numVals=(null == vals) ? 0 : vals.length;
				final StringBuilder	sb=(numVals <= 0) ? null : new StringBuilder(numVals * 64);
				for (int	vIndex=0; vIndex < numVals; vIndex++)
				{
					final Object	v=vals[vIndex];
					if (!useCRLF)
					{
						if (vIndex > 0)
							sb.append(',');
					}
	
					sb.append(v);
	
					if (useCRLF)
						sb.append(EOLStyle.CRLF.getStyleChars());
				}

				if (vals != null)
					return ((null == sb) || (sb.length() <= 0)) ? "" : sb.toString();
				// else fall through to default behavior
			}
			catch(Exception e)
			{
				// ignored
			}
		}
	
		return String.valueOf(value);
	}

	public static final Collection<Map.Entry<MBeanAttributeInfo,Object>> getMBeanAttributes (
			MBeanServerConnection s, ObjectName name, Map<String,MBeanAttributeInfo> aMap, Collection<String> aNames, boolean includeNulls)
				throws InstanceNotFoundException, ReflectionException, IOException
	{
		final int	numNames=(null == aNames) ? 0 : aNames.size();
		if ((numNames <= 0) || (null == aMap) || (aMap.size() <= 0))
			return null;

		final String[]		strNames=aNames.toArray(new String[numNames]);
		final AttributeList	aList=s.getAttributes(name, strNames);
		final int			numValues=(null == aList) ? 0 : aList.size();
		if (numValues <= 0)	// unusual, but OK
			return null;

		final Collection<Map.Entry<MBeanAttributeInfo,Object>>	retVals=
				new ArrayList<Map.Entry<MBeanAttributeInfo,Object>>(numValues);
		for (int	vIndex=0; vIndex < numValues; vIndex++)
		{
			final Attribute	a=(Attribute) aList.get(vIndex);
			final String	attrName=(null == a) ? null : a.getName();
			if ((null == attrName) || (attrName.length() <= 0))
				continue;	// should not happen

			final Object	aValue=a.getValue();
			if ((null == aValue) && (!includeNulls))
				continue;

			final MBeanAttributeInfo	aInfo=aMap.get(attrName);
			if (null == aInfo)	// should not happen (or rather not recommended)
				continue;

			final Map.Entry<MBeanAttributeInfo,Object>	p=new MapEntryImpl<MBeanAttributeInfo,Object>(aInfo, aValue);
			retVals.add(p);
		}

		return retVals;
	}
	/**
	 * @param s The {@link MBeanServerConnection} to use for querying
	 * @param name MBean {@link ObjectName} - may NOT be null/empty
	 * @param aList A {@link Collection} of {@link String}-s representing
	 * the values to be retrieved. <B>Note:</B> if an attribute does not
	 * exist then it is silently ignored (and not returned in the result). If
	 * same name is repeated then its value will be <U>duplicated</U> in the
	 * returned result
	 * @param includeNulls FALSE=filter out <code>null</code> values from the
	 * returned value(s)
	 * @return A {@link Collection} of "pairs" represented as
	 * {@link java.util.Map.Entry}-ies where the key is the {@link MBeanAttributeInfo}
	 * and the value is its associated value. May be null empty if none of
	 * the attributes found or all <code>null</code> values filtered out.
	 * @throws InstanceNotFoundException If no such MBean found
	 * @throws ReflectionException If cannot invoke MBean's attributes getters
	 * @throws IOException If cannot contact the MBean server/agent
	 * @throws IntrospectionException If cannot access the MBean's information
	 */
	public static final Collection<Map.Entry<MBeanAttributeInfo,Object>> getMBeanAttributes (
				MBeanServerConnection s, ObjectName name, Collection<String> aList, boolean includeNulls)
					throws InstanceNotFoundException, ReflectionException, IOException, IntrospectionException
	{
		final MBeanInfo	mbInfo=s.getMBeanInfo(name);
		if (null == mbInfo)
			throw new InstanceNotFoundException("getAllMBeanAttributes(" + name + ") no " + MBeanInfo.class.getName() + " instance retrieved");

		return getMBeanAttributes(s, name, getAttributesMap(mbInfo), aList, includeNulls);
	}
	/**
	 * Retrieves all available attributes values for the specified MBean
	 * @param s The {@link MBeanServerConnection} to use for querying
	 * @param name The MBean {@link ObjectName}
	 * @return A {@link Collection} of "pairs" represented as {@link java.util.Map.Entry}-ies
	 * where key=the retrieved {@link MBeanAttributeInfo}, value=the retrieved
	 * value for the attribute
	 * @param includeNulls FALSE=filter out <code>null</code> values from the
	 * returned value(s)
	 * @throws InstanceNotFoundException If no such MBean found
	 * @throws ReflectionException If cannot invoke MBean's attributes getters
	 * @throws IOException If cannot contact the MBean server/agent
	 * @throws IntrospectionException If cannot access the MBean's information
	 */
	public static final Collection<Map.Entry<MBeanAttributeInfo,Object>> getAllMBeanAttributes (
								final MBeanServerConnection s, final ObjectName name, final boolean includeNulls)
			throws InstanceNotFoundException, ReflectionException, IOException, IntrospectionException
    {
		final MBeanInfo	mbInfo=s.getMBeanInfo(name);
		if (null == mbInfo)
			throw new InstanceNotFoundException("getAllMBeanAttributes(" + name + ") no " + MBeanInfo.class.getName() + " instance retrieved");

		final Map<String,MBeanAttributeInfo>	aMap=getAttributesMap(mbInfo);
		final Collection<String>				aNames=((null == aMap) || (aMap.size() <= 0)) ? null : aMap.keySet();
		return getMBeanAttributes(s, name, aMap, aNames, includeNulls);
    }
	/**
	 * @param s The {@link MBeanServerConnection} to use for querying
	 * @param mbMap A {@link Map} of requested MBeans and attributes. Each
	 * entry key=MBean name, value={@link Collection} of {@link String}-s
	 * representing the requested attributes names.
	 * @param includeNulls TRUE=return <code>null</code> values as well (FALSE
	 * means filter out such values)
	 * @param eh A {@link JMXErrorHandler} to be used for logging exceptions. If
	 * <code>null</code> then any {@link Exception} is re-thrown. Otherwise,
	 * the exception is logged via {@link JMXErrorHandler#mbeanError(String, String, Throwable)}
	 * where the {@link Throwable} is the caught exception and the {@link Object}
	 * is the MBean name {@link String}. If the logger does not (re-)throw
	 * an exception, then next MBean in line is processed
	 * @return A {@link Map} of results where key=MBean name and value=a
	 * {@link Collection} of "pair" represented via a {@link java.util.Map.Entry} whose
	 * key=the retrieved {@link MBeanAttributeInfo} and the value is the
	 * attribute's {@link Object} value - may be null/empty
	 * @throws Exception if no {@link JMXErrorHandler} provided and an exception
	 * occurs during processing.
	 */
	public static final Map<String,Collection<Map.Entry<MBeanAttributeInfo,Object>>> getMBeansAttributes (
			MBeanServerConnection s, Map<String,? extends Collection<String>> mbMap, boolean includeNulls, JMXErrorHandler eh) throws Exception
	{
		final Collection<? extends Map.Entry<String,? extends Collection<String>>>	mbSet=
			((null == mbMap) || (mbMap.size() <= 0)) ? null : mbMap.entrySet();
		if ((null == mbSet) || (mbSet.size() <= 0))
			return null;

		Map<String,Collection<Map.Entry<MBeanAttributeInfo,Object>>>	retMap=null;
		long	totalTime=0L;
		int		totalValues=0;
		for (final Map.Entry<String,? extends Collection<String>> mbe : mbSet)
		{
			if (null == mbe)	// should not happen
				continue;

			final String				mbName=mbe.getKey();
			final Collection<String>	names=mbe.getValue();
			if ((null == names) || (names.size() <= 0))
				continue;

			final long	gStart=System.currentTimeMillis();
			try
			{
				final Collection<Map.Entry<MBeanAttributeInfo,Object>>	vals=
					getMBeanAttributes(s, new ObjectName(mbName), names, includeNulls);

				final long	gEnd=System.currentTimeMillis(), gDuration=gEnd - gStart;
				if (gDuration > 0L)
					totalTime += gDuration;

				final int	numVals=(null == vals) ? 0 : vals.size();
				if (numVals <= 0)
					continue;	// OK if no values returned (maybe all null(s))
				totalValues += numVals;

				if (null == retMap)
					retMap = new TreeMap<String,Collection<Map.Entry<MBeanAttributeInfo,Object>>>(String.CASE_INSENSITIVE_ORDER);

				Collection<Map.Entry<MBeanAttributeInfo,Object>>	prev=retMap.get(mbName);
				if (prev != null)
				{
					if (eh != null)
						eh.mbeanWarning(mbName, "merging duplicate(s)", null);
					prev.addAll(vals);
				}
				else
					retMap.put(mbName, vals);
			}
			catch(Exception e)
			{
				final long	gEnd=System.currentTimeMillis(), gDuration=gEnd - gStart;
				if (eh != null)
					eh.mbeanError(mbName,
								  e.getClass().getName() + " after " + gDuration + " msec."
								   + " (total=" + totalTime + "/values=" + totalValues + ") : " + e.getMessage(),
								  e);
				else
					throw e;
			}
		}

		return retMap;
	}
	/**
	 * @param s The {@link MBeanServerConnection} to use to retrieve MBean(s)
	 * information
	 * @param eh A {@link JMXErrorHandler} to be used for logging exceptions. If
	 * <code>null</code> then any {@link Exception} is re-thrown. Otherwise,
	 * the exception is logged via {@link JMXErrorHandler#mbeanError(String, String, Throwable)}
	 * where the {@link Throwable} is the caught exception and the {@link Object}
	 * is the MBean name {@link String}. If the logger does not (re-)throw
	 * an exception, then next MBean in line is processed
	 * @param mbNames A {@link Collection} of the MBean(s) names whose
	 * information is requested
	 * @return Retrieved {@link Map} of MBeans where key=MBean name, value=its
	 * associated {@link MBeanInfo} - may be null/empty
	 * @throws Exception if no {@link JMXErrorHandler} provided and an exception
	 * occurs during processing.
	 */
	public static final Map<String,MBeanInfo> getMBeansInformation (
			final MBeanServerConnection s, final JMXErrorHandler eh, final Collection<String> mbNames) throws Exception
	{
		if ((null == mbNames) || (mbNames.size() <= 0))
			return null;

		Map<String,MBeanInfo>	mbm=null;
		for (final String n : mbNames)
		{
			try
			{
				final ObjectName	on=((null == n) || (n.length() <= 0)) ? null : new ObjectName(n);
				final MBeanInfo		mbi=(null == on) ? null : s.getMBeanInfo(on);
				if (null == mbi)
					continue;

				if (null == mbm)
					mbm = new TreeMap<String,MBeanInfo>();
				mbm.put(n, mbi);
			}
			catch(Exception e)
			{
				if (eh != null)
					eh.mbeanError(n, "failed read mbean info", e);
				else
					throw e;
			}
		}

		return mbm;
	}
	/**
	 * @param s The {@link MBeanServerConnection} to use to retrieve MBean(s)
	 * information
	 * @param eh A {@link JMXErrorHandler} to be used for logging exceptions. If
	 * <code>null</code> then any {@link Exception} is re-thrown. Otherwise,
	 * the exception is logged via {@link JMXErrorHandler#mbeanError(String, String, Throwable)}
	 * where the {@link Throwable} is the caught exception and the {@link Object}
	 * is the MBean name {@link String}. If the logger does not (re-)throw
	 * an exception, then next MBean in line is processed
	 * @param mbNames An array of the MBean(s) names whose information is requested
	 * @return Retrieved {@link Map} of MBeans where key=MBean name, value=its
	 * associated {@link MBeanInfo}
	 * @throws Exception if no {@link JMXErrorHandler} provided and an exception
	 * occurs during processing.
	 */
	public static final Map<String,MBeanInfo> getMBeansInformation (
			MBeanServerConnection s, JMXErrorHandler eh, String ... mbNames) throws Exception
	{
		if ((null == mbNames) || (mbNames.length <= 0))
			return null;
		return getMBeansInformation(s, eh, Arrays.asList(mbNames));
	}

    public static final Map<String,Collection<MBeanAttributeInfo>> getMBeansAttributesInfo (final Map<String,? extends MBeanInfo> mbiMap)
    {
		final Collection<? extends Map.Entry<String,? extends MBeanInfo>>	mbiSet=
			((null == mbiMap) || (mbiMap.size() <= 0)) ? null : mbiMap.entrySet();
		if	((null == mbiSet) || (mbiSet.size() <= 0))
			return null;

		final Map<String,Collection<MBeanAttributeInfo>>	mbaMap=new TreeMap<String,Collection<MBeanAttributeInfo>>();
		for (final Map.Entry<String,? extends MBeanInfo> mbe : mbiSet)
		{
			final String				mbName=(null == mbe) ? null : mbe.getKey();
			final MBeanInfo				mbi=(null == mbe) ? null : mbe.getValue();
			final MBeanAttributeInfo[]	attrs=(null == mbi) ? null : mbi.getAttributes();
			if ((null == mbName) || (mbName.length() <= 0)	// should not happen
			 || (null == attrs) || (attrs.length <= 0))		// unlikely
				continue;
			mbaMap.put(mbName, Arrays.asList(attrs));
		}

		return mbaMap;
    }

    public static final Document buildDescriptorsDocument (
    		final String rootElemName, final Collection<? extends MBeanEntryDescriptor> descs,
			final boolean																fetchAttributes,
			final boolean																fetchValues,
			final boolean																includeNulls,
			final boolean																fetchOperations,
			final boolean																fetchParams)

    	throws Exception
    {
		final DocumentBuilderFactory	docFactory=DOMUtils.getDefaultDocumentsFactory();
		final DocumentBuilder			docBuilder=docFactory.newDocumentBuilder();
		final Document					doc=docBuilder.newDocument();
		if (null == doc)	// should not happen
			throw new IllegalStateException("buildAttributesDocument(" + rootElemName + ") no " + Document.class.getName() + " instance created by " + DocumentBuilder.class.getName());

		final Element	rootElem=doc.createElement(rootElemName);
		doc.appendChild(rootElem);
    	
		if ((descs != null) && (descs.size() > 0))
		{
			for (final MBeanEntryDescriptor d : descs)
			{
				final Element	el=(null == d) ? null : d.toXml(doc, fetchAttributes, fetchValues, includeNulls, fetchOperations, fetchParams);
				if (el != null)	// should not be otherwise
					rootElem.appendChild(el);
			}
		}

		return doc;
    }
    
    public static final Element buildAgentElement (final String elemName, final Document doc, final MBeanServer mbs)
    {
    	if ((mbs == null) || (doc == null))
    		return null;
   
    	final Element	elem=doc.createElement(elemName);
    	{
    		final String[]	domains=mbs.getDomains();
    		final String	domainsList=StringUtil.asStringList(',', (Object[]) domains);
    		DOMUtils.addNonEmptyAttribute(elem, "domains", domainsList);
    	}
    	{
    		final String	defDomain=mbs.getDefaultDomain();
   			DOMUtils.addNonEmptyAttribute(elem, "default", defDomain);
    	}
    	
    	return elem;
    }

    public static final Document buildAgentsDocument (
    		final String rootElemName, final Collection<? extends MBeanServer>	agentsList)
    	throws Exception
    {
		final DocumentBuilderFactory	docFactory=DOMUtils.getDefaultDocumentsFactory();
		final DocumentBuilder			docBuilder=docFactory.newDocumentBuilder();
		final Document					doc=docBuilder.newDocument();
		if (null == doc)	// should not happen
			throw new IllegalStateException("buildAgentsDocument(" + rootElemName + ") no " + Document.class.getName() + " instance created by " + DocumentBuilder.class.getName());

		final Element	rootElem=doc.createElement(rootElemName);
		doc.appendChild(rootElem);
		
		if ((agentsList != null) && (agentsList.size() > 0))
		{
			for (final MBeanServer mbs : agentsList)
			{
				final Element	el=buildAgentElement("agent", doc, mbs);
				if (el != null)	// should not be otherwise
					rootElem.appendChild(el);
			}
		}
    	
		return doc;
    }
}
