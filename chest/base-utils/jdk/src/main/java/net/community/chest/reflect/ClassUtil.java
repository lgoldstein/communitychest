package net.community.chest.reflect;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TimeZone;
import java.util.jar.Manifest;

import net.community.chest.convert.CharacterStringInstantiator;
import net.community.chest.convert.ClassValueStringInstantiator;
import net.community.chest.convert.DoubleValueStringConstructor;
import net.community.chest.convert.FloatValueStringConstructor;
import net.community.chest.convert.ObjectValueStringInstantiator;
import net.community.chest.convert.StringValueInstantiator;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.dom.DocumentStringInstantiator;
import net.community.chest.dom.ElementStringInstantiator;
import net.community.chest.io.FileStringInstantiator;
import net.community.chest.io.URIStringInstantiator;
import net.community.chest.io.URLStringInstantiator;
import net.community.chest.lang.EnumUtil;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;
import net.community.chest.text.CurrencyValueInstantiator;
import net.community.chest.text.DecimalFormatValueStringInstantiator;
import net.community.chest.text.SimpleDateFormatValueStringInstantiator;
import net.community.chest.util.ArraysUtils;
import net.community.chest.util.datetime.CalendarValueInstantiator;
import net.community.chest.util.datetime.DateValueInstantiator;
import net.community.chest.util.datetime.TimeZoneValueInstantiator;
import net.community.chest.util.locale.LocaleValueInstantiator;
import net.community.chest.util.map.ClassNameMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Copyright 2007 as per GPLv2
 * 
 * Useful {@link Class} related (static) functionality
 * 
 * @author Lyor G.
 * @since Jun 19, 2007 12:49:25 PM
 */
public final class ClassUtil {
	private ClassUtil ()
	{
		// no instance
	}
	/**
	 * @param c {@link Class} to be checked
	 * @param aType the atomic TYPE class
	 * @param oType the equivalent object class - if null or same as TYPE
	 * class then not checked
	 * @return TRUE if non-null and assignable to one of the atomic/object class(es)
	 */
	private static final boolean isAtomicTypeCompatible (final Class<?> c, final Class<?> aType, final Class<?> oType)
	{
		if (null == c)
			return false;

		if ((aType != null) && aType.isAssignableFrom(c))
			return true;
		if ((oType != null) && (oType != aType) && oType.isAssignableFrom(c))
			return true;

		return false;
	}
	// order is according to likelihood of usage
	private static final Class<?>[]	_primitiveTypes={
			Integer.TYPE, 	Integer.class,
			Long.TYPE,		Long.class,
			Boolean.TYPE,	Boolean.class,
			
			Float.TYPE, 	Float.class,
			Double.TYPE,	Double.class,
	
			Short.TYPE,		Short.class,
			Byte.TYPE,		Byte.class,

			Character.TYPE,	Character.class
		};
	/**
	 * @param c A {@link Class}
	 * @return The <I>TYPE</I> {@link Class} if this is one of the primitive
	 * type classes - null otherwise
	 */
	public static final Class<?> getPrimitiveClassEquivalent (final Class<?> c)
	{
		if (null == c)
			return null;

		for (int pIndex=0; pIndex < _primitiveTypes.length; pIndex += 2)
		{
			final Class<?>	pType=_primitiveTypes[pIndex],
							cType=_primitiveTypes[pIndex + 1];
			if (cType.equals(c))
				return pType;
		}
		
		return null;
	}
	/**
	 * @param c A {@link Class}
	 * @return The {@link Class} if this is one of the primitive <I>TYPE</I>-s
	 * type classes - null otherwise
	 */
	public static final Class<?> getPrimitiveTypeEquivalent (final Class<?> c)
	{
		if (null == c)
			return null;

		for (int pIndex=0; pIndex < _primitiveTypes.length; pIndex += 2)
		{
			final Class<?>	pType=_primitiveTypes[pIndex],
							cType=_primitiveTypes[pIndex + 1];
			if (pType.equals(c))
				return cType;
		}
		
		return null;
	}
	/**
	 * @param dataType The primitive data type
	 * @return The TYPE special class - e.g., <I>int</I>,<I>long</I>
	 */
	public static final Class<?> getPrimitiveClass (final String dataType)
	{
		if ((null == dataType) || (dataType.length() <= 0))	// should not happen
			return null;

		for (int pIndex=0; pIndex < _primitiveTypes.length; pIndex += 2)
		{
			final Class<?>	pType=_primitiveTypes[pIndex];
			final String	pName=(null == pType) /* should not happen */ ? null : pType.getName();
			if (dataType.equals(pName))
				return pType;
		}

		return null;
	}
	/**
	 * @param dataType A data type class name
	 * @return TRUE if this (non-null/empty) string represents one of the
	 * primitive data types (the TYPE special class - e.g., <I>int</I>,<I>long</I>)
	 */
	public static final boolean isPrimitiveClass (final String dataType)
	{
		return (getPrimitiveClass(dataType) != null);
	}
	/**
	 * @param dataType A data type class name
	 * @return The equivalent {@link Class} for the primitive type (null
	 * if no match found)
	 */
	public static final Class<?> resolvePrimitiveTypeClass (final String dataType)
	{
		if ((null == dataType) || (dataType.length() <= 0))	// should not happen
			return null;

		for (int pIndex=0; pIndex < _primitiveTypes.length; pIndex += 2)
		{
			final Class<?>	pType=_primitiveTypes[pIndex];
			final String	pName=(null == pType) /* should not happen */ ? null : pType.getName();
			if (dataType.equals(pName))
				return _primitiveTypes[pIndex + 1];
		}

		return null;
	}
	// atomic classes "pairs" - the TYPE and the .class
	private static final Class<?>[]	_atomicClasses={
		Boolean.TYPE, Boolean.class,
		Byte.TYPE, Byte.class,
		Short.TYPE, Short.class,
		Integer.TYPE, Integer.class,
		Long.TYPE, Long.class,
		
		String.class, null,	// dummy 2nd value
		Enum.class, null,	// dummy 2nd value

		Float.TYPE, Float.class,
		Double.TYPE, Double.class,

		Character.TYPE,	Character.class
	};

	private static final <V> ValueStringInstantiator<V> addAtomicStringConstructor (
												final ClassNameMap<ValueStringInstantiator<?>> 	m,
												final Class<V> 									aType,
												final Class<V> 									cType,
												final ValueStringInstantiator<V>				ctor)
	{
		if (aType != null)
			m.put(aType, ctor);
		if (cType != null)
			m.put(cType, ctor);

		return ctor;
	}

	private static final <V> ValueStringInstantiator<V> addAtomicStringConstructor (
			final ClassNameMap<ValueStringInstantiator<?>> m, final Class<V> aType, final Class<V> cType)
	{
		final ValueStringConstructor<V>	ctor=(null == cType) ? new ValueStringConstructor<V>(aType) : new ValueStringConstructor<V>(cType);
		return addAtomicStringConstructor(m, aType, cType, ctor);
	}

	public static final <M extends ClassNameMap<ValueStringInstantiator<?>>> M updateDefaultInstantiatorsMap (final M m)
	{
		if (null == m)
			return m;

		addAtomicStringConstructor(m, Boolean.TYPE, Boolean.class);
		addAtomicStringConstructor(m, Byte.TYPE, Byte.class);
		addAtomicStringConstructor(m, Short.TYPE, Short.class);
		addAtomicStringConstructor(m, Integer.TYPE, Integer.class);
		addAtomicStringConstructor(m, Long.TYPE, Long.class);

		// special handling
		addAtomicStringConstructor(m, Float.TYPE, Float.class, FloatValueStringConstructor.DEFAULT);
		addAtomicStringConstructor(m, Double.TYPE, Double.class, DoubleValueStringConstructor.DEFAULT);
		addAtomicStringConstructor(m, Character.TYPE, Character.class, CharacterStringInstantiator.DEFAULT);

		m.put(String.class, StringValueInstantiator.DEFAULT);
		m.put(Object.class, ObjectValueStringInstantiator.DEFAULT);

		// some special extra classes that are not exactly atomic
		m.put(Date.class, DateValueInstantiator.DEFAULT);
		m.put(File.class, FileStringInstantiator.DEFAULT);
		m.put(Element.class, ElementStringInstantiator.DEFAULT);
		m.put(Document.class, DocumentStringInstantiator.DEFAULT);
		m.put(Class.class, ClassValueStringInstantiator.DEFAULT);
		m.put(Calendar.class, CalendarValueInstantiator.DEFAULT);
		m.put(Locale.class, LocaleValueInstantiator.DEFAULT);
		m.put(Currency.class, CurrencyValueInstantiator.DEFAULT);
		m.put(TimeZone.class, TimeZoneValueInstantiator.DEFAULT);
		m.put(DateFormat.class, SimpleDateFormatValueStringInstantiator.DEFAULT);
		m.put(NumberFormat.class, DecimalFormatValueStringInstantiator.DEFAULT);
		m.put(URL.class, URLStringInstantiator.DEFAULT);
		m.put(URI.class, URIStringInstantiator.DEFAULT);

		return m;
	}

	public static final JDKInstantiatorsMap createDefaultInstantiatorsMap ()
	{
		return updateDefaultInstantiatorsMap(new JDKInstantiatorsMap());
	}

	private static Map<String,ValueStringInstantiator<?>>	_atomicCtorsMap	/* =null */;
	// CAVEAT EMPTOR
	public static final synchronized Map<String,ValueStringInstantiator<?>> getAtomicStringConstructorsMap ()
	{
		if (null == _atomicCtorsMap)
			_atomicCtorsMap = createDefaultInstantiatorsMap();
		return _atomicCtorsMap;
	}
	// returns previous instance
	public static final synchronized Map<String,ValueStringInstantiator<?>> setAtomicStringConstructorsMap (Map<String,ValueStringInstantiator<?>> m)
	{
		final Map<String,ValueStringInstantiator<?>>	prev=_atomicCtorsMap;
		_atomicCtorsMap = m;
		return prev;
	}
	/**
	 * Copyright 2007 as per GPLv2
	 * 
	 * Special {@link ValueStringInstantiator} implementation for "generic"
	 * but "unknown" {@link Enum}-s instantiation from string.
	 * @author Lyor G.
	 * @since Jul 10, 2007 1:40:48 PM
	 */
	private static final class UnknownEnumStringInstantiator extends AbstractXmlValueStringInstantiator<Object> {
		@SuppressWarnings("unchecked")
		protected UnknownEnumStringInstantiator (final Class<?> c) throws IllegalArgumentException
		{
			super((Class<Object>) c);

			if (!Enum.class.isAssignableFrom(c))	// should not happen
				throw new IllegalArgumentException(getClass().getName() + "#<init> bad/illegal class: " + ((null == c) ? null : c.getName()));
		}
		// cached instance for improved efficiency
		private List<Enum<?>>	_values;
		@SuppressWarnings({ "unchecked" })
		private synchronized List<Enum<?>> getValues ()
		{
			if (null == _values)
				_values = (List<Enum<?>>) ((List<?>) Collections.unmodifiableList(Arrays.asList(getValuesClass().getEnumConstants())));
			return _values;
		}
		/*
		 * @see net.community.chest.reflect.ValueStringInstantiator#newInstance(java.lang.String)
		 */
		@Override
		public Object newInstance (final String v) throws Exception
		{
			final String	s=StringUtil.getCleanStringValue(v);
			if ((null == s) || (s.length() <= 0))
				return null;

			@SuppressWarnings({ "unchecked", "rawtypes" })
			final Enum<?>	val=EnumUtil.fromName((Collection) getValues(), s, false);
			if (null == val)
				throw new NoSuchElementException(getClass().getName() + "#newInstance(" + s + ") no matching " + getValuesClass().getName() + " value");

			return val;
		}
		/*
		 * @see net.community.chest.reflect.ValueStringInstantiator#convertInstance(java.lang.Object)
		 */
		@Override
		public String convertInstance (Object inst) throws Exception
		{
			return (null == inst) ? null : inst.toString();
		}
	}
	// cache to avoid re-creation of instantiator(s)
	private static final StringInstantiatorsMap	_enumCtorsMap=new StringInstantiatorsMap();
	private static final <E extends Enum<E>> ValueStringInstantiator<E> getEnumClassStringInstantiator (final Class<E> c)
	{
		if ((null == c) || (!Enum.class.isAssignableFrom(c)))
			return null;

		final String	cName=c.getName();
		if ((null == cName) || (cName.length() <= 0))
			return null;	// should not happen

		ValueStringInstantiator<?>	vsi=null;
		synchronized(_enumCtorsMap)
		{
			if (null == (vsi=_enumCtorsMap.get(c)))
			{
				vsi = new UnknownEnumStringInstantiator(c);
				_enumCtorsMap.put(cName, vsi);
			}
		}

		@SuppressWarnings("unchecked")
		final ValueStringInstantiator<E>	vse=(ValueStringInstantiator<E>) vsi;
		return vse;
	}
	/**
	 * @param c {@link Class} to be checked
	 * @return TRUE if non-null instance and one of the atomic classes
	 * (including {@link Enum}-s)
	 */
	public static final boolean isAtomicClass (final Class<?> c)
	{
		if (null == c)
			return false;

		if (c.isPrimitive())
			return true;	// all primitive types are atomic

		for (int	cIndex=0; cIndex < _atomicClasses.length; cIndex += 2)
		{
			if (isAtomicTypeCompatible(c, _atomicClasses[cIndex], _atomicClasses[cIndex+1]))
				return true;
		}

		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final <V> ValueStringInstantiator<V> getAtomicStringInstantiator (final Class<V> c)
	{
		if (!isAtomicClass(c))
			return null;

		if (Enum.class.isAssignableFrom(c)) {
		    @SuppressWarnings("cast")
            ValueStringInstantiator<V>   vsi=
		            (ValueStringInstantiator<V>) getEnumClassStringInstantiator((Class<? extends Enum>) c);
		    return vsi;
		}

		final Map<String,? extends ValueStringInstantiator<?>>	cm=getAtomicStringConstructorsMap();
		if ((null == cm) || (cm.size() <= 0))
			return null;

		return (ValueStringInstantiator<V>) cm.get(c.getName());
	}

	@SuppressWarnings("unchecked")
	public static final <V> ValueStringInstantiator<V> getJDKStringInstantiator (final Class<V> c)
	{
		final ValueStringInstantiator<V>	vsi=getAtomicStringInstantiator(c);
		if (vsi != null)
			return vsi;

		final Map<String,? extends ValueStringInstantiator<?>>	cm=getAtomicStringConstructorsMap();
		return (ValueStringInstantiator<V>) ClassNameMap.get(cm, c);
	}
	/**
	 * @param c {@link Class} to be checked
	 * @return TRUE if this class is compatible with {@link Boolean#TYPE} or {@link Boolean} class(es)
	 */
	public static final boolean isBooleanCompatible (final Class<?> c)
	{
		return isAtomicTypeCompatible(c, Boolean.TYPE, Boolean.class);
	}
	/**
	 * @param c {@link Class} whose <U>public</U> {@link Method}-s we wany
	 * @return {@link Collection} of <U>public</U> {@link Method}-s - may be
	 * null/empty if none found (or no {@link Class} instance to begin with)
	 */
	public static final Collection<Method> getPubliclyAccessibleMethods (final Class<?> c)
	{
		final Method[]	ma=(null == c) ? null : c.getMethods();
		if ((null == ma) || (ma.length <= 0))
			return null;

		return MethodUtil.getPubliclyAccessibleMethods(Arrays.asList(ma));
	}
	/**
	 * @param c {@link Class} to be queried for the getter
	 * @param attrName "pure" attribute name (automatically "adjusted" if
	 * necessary)
	 * @param attrType attribute type - if {@link Boolean} compatible then an
	 * "is" method is looked up, otherwise a "get" one
	 * @return resulting {@link Method} - null if not found
	 */
	public static final Method findMatchingGetter (final Class<?> c, final String attrName, final Class<?> attrType)
	{
		if ((null == c) || (null == attrName) || (attrName.length() <= 0) || (null == attrType))
			return null;

		final AttributeMethodType	aType=isBooleanCompatible(attrType) ? AttributeMethodType.PREDICATE : AttributeMethodType.GETTER;
		final String				mnSuffix=AttributeMethodType.getAdjustedAttributeName(attrName),
									mthName=aType.getAccessorName(mnSuffix);
		try
		{
			return c.getMethod(mthName);
		}
		catch(Exception e)
		{
			// ignored
			return null;
		}
	}
	/**
	 * @param c {@link Class} to be queried for the getter
	 * @param attrName "pure" attribute name (automatically "adjusted" if
	 * necessary)
	 * @return attempts to find a "get" method, and failing that an "is" one.
	 * Null if no matching {@link Method} found
	 */
	public static final Method findMatchingGetter (final Class<?> c, final String attrName)
	{
		Method	m=findMatchingGetter(c, attrName, Void.TYPE /* dummy - as long as not Boolean */);
		if (null == m)
			m = findMatchingGetter(c, attrName, Boolean.TYPE);
		return m;
	}
	// same as findMatchingGetter only checks also if public
	public static final Method findAccessibleGetter (final Class<?> c, final String attrName, final Class<?> attrType)
	{
		final Method	gm=findMatchingGetter(c, attrName, attrType);
		if (null == gm)
			return null;
		
		if (!Modifier.isPublic(gm.getModifiers()))
			return null;	// ignore non-publicly accessible

		return gm;
	}
	// same as findMatchingGetter only checks also if public
	public static final Method findAccessibleGetter (final Class<?> c, final String attrName)
	{
		final Method	gm=findMatchingGetter(c, attrName);
		if (null == gm)
			return null;
		
		if (!Modifier.isPublic(gm.getModifiers()))
			return null;	// ignore non-publicly accessible

		return gm;
	}
	/**
	 * Good for informative text of the exception location - inefficient, so
	 * use sparingly (only for exceptions)
	 * @param c {@link Class} that originated the exception
	 * @param location location indication
	 * @return class name + "#" + location
	 */
	public static final String getExceptionLocation (final Class<?> c, final String location)
	{
		return ((null == c) ? "Class???" : c.getName()) + "#" + location;
	}
	/**
	 * Good for informative text of exception location that also has
	 * some arguments
	 * @param c {@link Class} that originated the exception
	 * @param location base location
	 * @param args arguments - added as a '['/']' comma delimited list
	 * @return formatted string
	 */
	public static final String getArgumentsExceptionLocation (final Class<?> c, final String location, final Object... args)
	{
		final String		baseLocation=getExceptionLocation(c, location);
		final int			locLen=(null == baseLocation) ? 0 : Math.max(0, baseLocation.length()),
							numArgs=(null == args) ? 0 : Math.max(0, args.length);
		final StringBuilder	sb=new StringBuilder(locLen + (numArgs * 64) + 16)
										.append(baseLocation)
										.append('[')
										;
		for (int	oIndex=0; oIndex < numArgs; oIndex++)
		{
			if (oIndex > 0)
				sb.append(',');
			sb.append(args[oIndex]);
		}

		sb.append(']');
		return sb.toString();
	}
	/**
	 * Default location name used in {@link #getConstructorExceptionLocation(Class)}
	 * and/or {@link #getConstructorArgumentsExceptionLocation(Class, Object...)}
	 */
	public static final String	DEFAULT_CTOR_LOCATION="<init>";
	/**
	 * Informative text for exceptions thrown by constructors
	 * @param c {@link Class} that originated the exception
	 * @return class name + "#<init>"
	 */
	public static final String getConstructorExceptionLocation (final Class<?> c)
	{
		return getExceptionLocation(c, DEFAULT_CTOR_LOCATION);
	}
	/**
	 * Good for informative text of exception in constructor that also has
	 * some arguments
	 * @param c {@link Class} that originated the exception
	 * @param args arguments - added as a '['/']' comma delimited list
	 * @return formatted string
	 */
	public static final String getConstructorArgumentsExceptionLocation (final Class<?> c, final Object... args)
	{
		return getArgumentsExceptionLocation(c, DEFAULT_CTOR_LOCATION, args);
	}
	/**
	 * @param o {@link Object} whose hash code is required
	 * @return 0 if null object, {@link Object#hashCode()} otherwise
	 */
	public static final int getObjectHashCode (final Object o)
	{
		return (null == o) ? 0 : o.hashCode();
	}
	

    // makes sure that only same typed objects are compared
    public static <T> boolean typedEquals (T o1, T o2) {
        if (o1 == o2)
            return true;
        if ((o1 == null) || (o2 == null))
            return false;
        return o1.equals(o2);
    }

    public static int hashCode (Object o) {
        return (o == null) ? 0 : o.hashCode(); 
    }

	/**
	 * Attempts to resolve a {@link Class} from a given string while taking
	 * into account the <U>primitive</U> types (e.g., <code>int,long</code>)
	 * for which simply calling {@link Class#forName(String)} will fail with
	 * a {@link NoClassDefFoundError}.
	 * @param dtVal The {@link String} containing the <U>fully qualified</U>
	 * class name/path
	 * @return resolved {@link Class} instance - it first checks if the name
	 * is one of the primitive types. If not, then {@link Class#forName(String)}
	 * is called.
	 * @throws Exception if null/empty type name or {@link Class#forName(String)}
	 * thrown exception(s)
	 */
	public static final Class<?> resolveDataType (final String dtVal) throws Exception
	{
		return resolveDataType(getDefaultClassLoader(), dtVal);
	}

	public static final Class<?> resolveDataType (final ClassLoader cl, final String dtVal) throws Exception
	{
		if ((null == dtVal) || (dtVal.length() <= 0))
			throw new IllegalArgumentException(ClassUtil.getArgumentsExceptionLocation(ClassUtil.class, "resolveDataType", dtVal) + " null/empty argument");

		for (final Class<?>	pClass : _primitiveTypes)
		{
			final String	pName=(null == pClass) /* should not happen */ ? null : pClass.getName();
			if (dtVal.equals(pName))
				return pClass;
		}

		// if not a primitive class then try dynamic loading
		return loadClassByName(cl, dtVal);
	}

    public static Manifest loadContainerManifest (Class<?> anchor) throws IOException
	{
        URL     classBytesURL=getClassBytesURL(anchor);
        String  scheme=(classBytesURL == null) ? null : classBytesURL.getProtocol();
        if ((scheme == null) || (scheme.length() <= 0))
            return null;

        String			classPath=classBytesURL.toExternalForm();
        int				sepPos=classPath.lastIndexOf('!');
        final String	pathPrefix;
        if (sepPos < 0)
		{
        	String	className=anchor.getName().replace('.', '/');
        	if ((sepPos=classPath.indexOf(className)) <= 1)	// should be at least a:b
        		return null;

        	if (classPath.charAt(sepPos - 1) == '/')
        		sepPos--;

        	pathPrefix = classPath.substring(0, sepPos);
        }
		else
		{
        	pathPrefix = classPath.substring(0, sepPos + 1);
        }

        String		manifestPath=pathPrefix + "/META-INF/MANIFEST.MF";
        URL     	url=new URL(manifestPath);
        try(InputStream in=url.openStream()) {
            return new Manifest(in);
        }
    }
    /**
     * @param clazz A {@link Class} object
     * @return A {@link File} of the location of the class bytes container
     * - e.g., the root folder, the containing JAR, etc.. Returns
     * <code>null</code> if location could not be resolved
     * @throws IllegalArgumentException If location is not a valid
     * {@link File} location
     * @see #getClassContainerLocationURI(Class)
     * @see File#File(URI) 
     */
    public static File getClassContainerLocationFile (Class<?> clazz)
            throws IllegalArgumentException {
        try {
            URI uri=getClassContainerLocationURI(clazz);
            return (uri == null) ? null : new File(uri);
        } catch(URISyntaxException e) {
            throw new IllegalArgumentException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }
    /**
     * @param clazz A {@link Class} object
     * @return A {@link URI} to the location of the class bytes container
     * - e.g., the root folder, the containing JAR, etc.. Returns
     * <code>null</code> if location could not be resolved
     * @throws URISyntaxException if location is not a valid URI
     * @see #getClassContainerLocationURL(Class)
     */
    public static URI getClassContainerLocationURI (Class<?> clazz) throws URISyntaxException {
        URL url=getClassContainerLocationURL(clazz);
        return (url == null) ? null : url.toURI();
    }
    /**
     * @param clazz A {@link Class} object
     * @return A {@link URL} to the location of the class bytes container
     * - e.g., the root folder, the containing JAR, etc.. Returns
     * <code>null</code> if location could not be resolved
     */
    public static URL getClassContainerLocationURL (Class<?> clazz) {
        ProtectionDomain    pd=clazz.getProtectionDomain();
        CodeSource          cs=(pd == null) ? null : pd.getCodeSource();
        return (cs == null) ? null : cs.getLocation();
    }
    /**
     * @param clazz The request {@link Class}
     * @return A {@link URL} to the location of the <code>.class</code> file
     * - <code>null</code> if location could not be resolved
     */
    public static URL getClassBytesURL (Class<?> clazz)
	{
		String	className=clazz.getName();
    	int		sepPos=className.indexOf('$');
    	// if this is an internal class, then need to use its parent as well
    	if (sepPos > 0)
		{
    		if ((sepPos=className.lastIndexOf('.')) > 0)
    			className = className.substring(sepPos + 1);
    	}
		else
		{
    		className = clazz.getSimpleName();
    	}

        return clazz.getResource(className + ".class");
	}
    /**
     * Determine whether the {@link Class} identified by the supplied name is present
     * and can be loaded. Will return <code>false</code> if either the class or
     * one of its dependencies is not present or cannot be loaded using the default
     * class loader.
     * @param fqcn The fully qualified class name - may NOT be null/empty
     * @return whether the specified class is present
     * @throws IllegalArgumentException if <code>null</code>empty class name specified
     * @see #getDefaultClassLoader()
     * @see #isPresent(String, ClassLoader)
     */
    public static boolean isPresent(String fqcn) throws IllegalArgumentException {
        return isPresent(fqcn, getDefaultClassLoader());
    }
    /**
     * Determine whether the {@link Class} identified by the supplied name is present
     * and can be loaded. Will return <code>false</code> if either the class or
     * one of its dependencies is not present or cannot be loaded.
     * @param fqcn The fully qualified class name - may NOT be null/empty
     * @param classLoader The {@link ClassLoader} to use - may NOT be null/empty
     * @return whether the specified class is present
     * @see #loadClassByName(ClassLoader, String)
     * @throws IllegalArgumentException if no loader or <code>null</code>empty
     * class name specified
     */
    public static boolean isPresent(String fqcn, ClassLoader classLoader)
            throws IllegalArgumentException {
        try {
            return (loadClassByName(classLoader, fqcn) != null);
        } catch (ClassNotFoundException e) {
            // Class or one of its dependencies is not present...
            return false;
        }
    }
	/**
	 * Used as a replacement for {@link Class#forName(String)} call in order
	 * to provide a centralized location for loading classes by name. This
	 * method uses the {@link Thread#getContextClassLoader()} of the current
	 * thread in order to load the requested class
	 * @param clsPath The class fully qualified path - may NOT be null/empty
	 * @return The loaded {@link Class} instance
	 * @throws ClassNotFoundException If cannot load the class.
	 */
	public static final Class<?> loadClassByName (final String clsPath)	throws ClassNotFoundException
	{
        return loadClassByName(getDefaultClassLoader(), clsPath);
	}
    /**
     * Used as a replacement for {@link Class#forName(String, boolean, ClassLoader)} call
     * in order to provide a centralized location for loading classes by name. This
     * method uses the provided {@link ClassLoader} in order to load the requested class.
     * <B>Note:</B> the built-in <U>primitive</U> classes are resolved using the
     * {@link #getPrimitiveClass(String)} and not loaded via the specified loader
     * @param cl The {@link ClassLoader} instance to use - <B>Note:</B> must not
     * @param fqcn The fully qualified class name - may NOT be null/empty even if
     * it is not used (e.g., when requesting primitive types)
     * @return The loaded {@link Class} instance
     * @throws IllegalArgumentException if no loader or <code>null</code>empty
     * class name specified
     * @throws ClassNotFoundException If cannot load the class.
     * @see #loadClassByName(ClassLoader, String)
     */
    public static Class<?> loadClassByName (ClassLoader cl, String fqcn)
            throws IllegalArgumentException, ClassNotFoundException {
        if ((fqcn == null) || (fqcn.length() <= 0)) {
            throw new IllegalArgumentException("No fully qualified class name specified");
        }
        if (cl == null) {
            throw new IllegalArgumentException("loadClassByName(" + fqcn + ") no loader specified");
        }

        Class<?>  pClass=getPrimitiveClass(fqcn);
        if (pClass != null) {
            return pClass;
        }

        return cl.loadClass(fqcn);
    }
    /**
     * Convenience <I>varargs</I> method for {@link Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)}
     * using the {@link #getDefaultClassLoader()} result as the proxy loader
     * @param resultType The proxy result {@link Class} to be cast to
     * @param h The target {@link InvocationHandler}
     * @param interfaces The interface classes being proxy-ied
     * @return The create proxy
     * @see #newProxyInstance(Class, ClassLoader, InvocationHandler, Class...)
     */
    public static <T> T newProxyInstance(Class<T> resultType,InvocationHandler h, Class<?> ... interfaces) {
        return newProxyInstance(resultType, getDefaultClassLoader(), h, interfaces);
    }
    /**
     * Convenience <I>varargs</I> method for {@link Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)}
     * @param resultType The proxy result {@link Class} to be cast to
     * @param loader The {@link ClassLoader} to use
     * @param h The target {@link InvocationHandler}
     * @param interfaces The interface classes being proxy-ied
     * @return The create proxy
     * @see Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)
     */
    public static <T> T newProxyInstance(Class<T> resultType, ClassLoader loader, InvocationHandler h, Class<?> ... interfaces) {
        if (ArraysUtils.length(interfaces) <= 0) {
            throw new IllegalArgumentException("No interfaces specified");
        }

        for (Class<?> i : interfaces) {
            if (i == null) {
                throw new IllegalArgumentException("Blank spot in " + Arrays.toString(interfaces));
            }
            if (!i.isInterface()) {
                throw new IllegalArgumentException("Not an interface: " + i.getSimpleName());
            }
        }

        return resultType.cast(Proxy.newProxyInstance(loader, interfaces, h));
    }
    /**
     * @return A {@link ClassLoader} to be used by the caller. The loader is
     * resolved in the following manner:</P></BR>
     * <UL>
     *      <LI>
     *      If a non-<code>null</code> loader is returned from the
     *      {@link Thread#getContextClassLoader()} call then use it.
     *      </LI>
     *      
     *      <LI>
     *      Otherwise, use the same loader that was used to load this class.
     *      </LI>
     * </UL>
     * @see #getDefaultClassLoader(Class)
     */
    public static ClassLoader getDefaultClassLoader() {
        return getDefaultClassLoader(ClassUtil.class);
    }
    /**
     * @param anchor An &quot;anchor&quot; {@link Class} to be used in case
     * no thread context loader is available
     * @return A {@link ClassLoader} to be used by the caller. The loader is
     * resolved in the following manner:</P></BR>
     * <UL>
     *      <LI>
     *      If a non-<code>null</code> loader is returned from the
     *      {@link Thread#getContextClassLoader()} call then use it.
     *      </LI>
     *      
     *      <LI>
     *      Otherwise, use the same loader that was used to load the anchor class.
     *      </LI>
     * </UL>
     * @throws IllegalArgumentException if no anchor class provided (regardless of
     * whether it is used or not) 
     */
    public static ClassLoader getDefaultClassLoader(Class<?> anchor) {
        if (anchor == null) {
            throw new IllegalArgumentException("No anchor class provided");
        }

        Thread      t=Thread.currentThread();
        ClassLoader cl=t.getContextClassLoader();
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = anchor.getClassLoader();
        }

        if (cl == null) {	// no class loader - assume system
        	cl = ClassLoader.getSystemClassLoader();
        }

        return cl;
    }

	public static final String getParentPackage (final String pkgName)
	{
		final int	pnLen=(null == pkgName) ? 0 : pkgName.length(),
					cPos=(pnLen <= 2) /* must be at least a.b */ ? (-1) : pkgName.lastIndexOf('.');
		if ((cPos <= 0) || (cPos >= (pnLen-1)))
			return null;

		return pkgName.substring(0, cPos);
	}

	public static final String getParentPackage (final Package pkg)
	{
		return (null == pkg) ? null : getParentPackage(pkg.getName());
	}

	public static final <V extends PubliclyCloneable<V>> V clonePublic (V obj) throws CloneNotSupportedException
	{
		return (null == obj) ? null : obj.clone();
	}
	/**
	 * @param o Any {@link Object}
	 * @return A unique "key" string representing this specific instance (null
	 * if null object). The key is constructed from the {@link Class#getName()}
	 * value concatenated to the {@link System#identityHashCode(Object)} value
	 * of the object as a HEX string with '@' between them. May be
	 * <code>null</code> if no object instance provided.
	 */
	public static final String getUniqueObjectInstanceKey (final Object o)
	{
		final Class<?>	c=(null == o) ? null : o.getClass();
		final String	n=(null == c) ? null : c.getName();
		final int		h=(null == o) ? Integer.MIN_VALUE : System.identityHashCode(o);
		if ((null == n) || (n.length() <= 0))
			return null;

		return n + "@" + Integer.toHexString(h);
	}
}
