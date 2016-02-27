package net.community.chest.reflect;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.lang.StringUtil;
import net.community.chest.util.map.MapEntryImpl;

/**
 * Copyright 2007 as per GPLv2
 * 
 * Used to "classify" {@link Method}-s according to the Java Bean(s) model as
 * <UL>
 * 		<LI>getter - <I>getXXX</I> <U>or</U> <I>isXXX</I> method</LI>
 * 		<LI>predicate - </I>isXXX</I> method</LI>
 * 		<LI>setter - <I>setXXX</I> method </I>
 * 		<LI>operation - some other non-attribute method
 * </UL>
 * @author Lyor G.
 * @since Jun 19, 2007 12:02:53 PM
 */
public enum AttributeMethodType {
	/**
	 * "getXXX" method
	 */
	GETTER(true, false, false, "get") {
		/*
		 * @see net.community.chest.reflect.AttributeMethodType#isMatchingReturnType(java.lang.Class)
		 */
		@Override
		public boolean isMatchingReturnType (Class<?> rc)
		{
			// VOID is NOT a valid attribute type
			if ((null == rc)
			 || Void.class.isAssignableFrom(rc)
			 || Void.TYPE.isAssignableFrom(rc))
				return false;

			return true;
		}
		/*
		 * @see net.community.chest.reflect.AttributeMethodType#isMatchingNumOfParams(int)
		 */
		@Override
		public boolean isMatchingNumOfParams (int numParams)
		{
			return (numParams <= 0);
		}
		/*
		 * @see net.community.chest.reflect.AttributeMethodType#getAttributeType(java.lang.reflect.Method)
		 */
		@Override
		public Class<?> getAttributeType (final Method m)
		{
			final String	nm=(null == m) ? null : m.getName();
			if (!isMatchingPrefix(nm))
				return null;

			final Class<?>	rClass=(null == m) ? null : m.getReturnType();
			if (!isMatchingReturnType(rClass))
				return null;

			if (!isMatchingNumOfParams(m))
				return null;

			return rClass;
		}
	},
	/**
	 * "isXXX" method 
	 */
	PREDICATE(true, true, false, "is") {
		/*
		 * @see net.community.chest.reflect.AttributeMethodType#isMatchingReturnType(java.lang.Class)
		 */
		@Override
		public boolean isMatchingReturnType (Class<?> rc)
		{
			if (null == rc)
				return false;

			// predicates allow only for Boolean(s) as return types
			if (Boolean.class.isAssignableFrom(rc)
			 || Boolean.TYPE.isAssignableFrom(rc))
				return true;

			return false;
		}
		/*
		 * @see net.community.chest.reflect.AttributeMethodType#isMatchingNumOfParams(int)
		 */
		@Override
		public boolean isMatchingNumOfParams (int numParams)
		{
			return (numParams <= 0);
		}
		/*
		 * @see net.community.chest.reflect.AttributeMethodType#getAttributeType(java.lang.reflect.Method)
		 */
		@Override
		public Class<?> getAttributeType (final Method m)
		{
			final String	nm=(null == m) ? null : m.getName();
			if (!isMatchingPrefix(nm))
				return null;

			final Class<?>	rc=(null == m) ? null : m.getReturnType();
			if (!isMatchingReturnType(rc))
				return null;

			if (!isMatchingNumOfParams(m))
				return null;

			return rc;
		}
	},
	/**
	 * "setXXX" method
	 */
	SETTER(false, false, false, "set") {
		/*
		 * @see net.community.chest.reflect.AttributeMethodType#isMatchingReturnType(java.lang.Class)
		 */
		@Override
		public boolean isMatchingReturnType (Class<?> rc)
		{
			if (null == rc)
				return false;

			// setters must have a void return type
			if (Void.class.isAssignableFrom(rc)
			 || Void.TYPE.isAssignableFrom(rc))
				return true;

			return false;
		}
		/*
		 * @see net.community.chest.reflect.AttributeMethodType#isMatchingNumOfParams(int)
		 */
		@Override
		public boolean isMatchingNumOfParams (int numParams)
		{
			return (1 == numParams);
		}
		/*
		 * @see net.community.chest.reflect.AttributeMethodType#getAttributeType(java.lang.reflect.Method)
		 */
		@Override
		public Class<?> getAttributeType (final Method m)
		{
			final String	nm=(null == m) ? null : m.getName();
			if (!isMatchingPrefix(nm))
				return null;

			if (!isMatchingReturnType(m))
				return null;

			final Class<?>[]	pa=m.getParameterTypes();
			if (!isMatchingNumOfParams(pa))
				return null;

			return pa[0];
		}
	},
	/**
	 * non Java Bean attribute related method
	 */
	OPERATION(false, false, true, "") {
		/*
		 * @see net.community.chest.reflect.AttributeMethodType#getAttributeType(java.lang.reflect.Method)
		 */
		@Override
		public Class<?> getAttributeType (final Method m)
		{
			return null;	// operations have no attribute type
		}
		/*
		 * @see net.community.chest.reflect.AttributeMethodType#isMatchingReturnType(java.lang.Class)
		 */
		@Override
		public boolean isMatchingReturnType (Class<?> rc)
		{
			return false;
		}
		/*
		 * @see net.community.chest.reflect.AttributeMethodType#isMatchingNumOfParams(int)
		 */
		@Override
		public boolean isMatchingNumOfParams (int numParams)
		{
			return true;
		}
	};
	/**
	 * @param m A {@link Method} to be checked
	 * @return The associated attribute type (if getter/setter/predicate)
	 * or null if not a matching method type (or an {@link #OPERATION}).
	 */
	public abstract Class<?> getAttributeType (Method m);

	public abstract boolean isMatchingNumOfParams (int numParams);

	public boolean isMatchingNumOfParams (Collection<? extends Class<?>> pl)
	{
		return isMatchingNumOfParams((null == pl) ? 0 : pl.size());
	}

	public boolean isMatchingNumOfParams (Class<?> ... params)
	{
		return isMatchingNumOfParams((null == params) ? 0 : params.length);
	}
	/**
	 * @param m A {@link Method} to be checked
	 * @return <code>true</code> if number of parameters matches the expected
	 * according to the attribute type. <B>Note(s):</B></BR>
	 * <UL>
	 * 		<LI>
	 * 		The function does not check if other characteristics are met
	 * 		(e.g., name, return type, etc.)
	 * 		</LI>
	 *
	 * 		<LI>
	 * 		For {@link #GETTER}/{@link #PREDICATE} the result is
	 * 		<code>true</code> if the number of parameters is zero
	 * 		</LI>
	 * 		<LI>
	 * 		For {@link #SETTER} the result is <code>true</code> if the
	 * 		number of parameters is exactly one
	 * 		</LI>
	 *
	 * 		<LI>
	 * 		For {@link #OPERATION} the result is
	 *  	<U>always <B><code>true</code></B></U>
	 *  	</LI>
	 *  </UL>
	 */
	public boolean isMatchingNumOfParams (Method m)
	{
		return (null == m) ? false : isMatchingNumOfParams(m.getParameterTypes());
	}

	public abstract boolean isMatchingReturnType (Class<?> rc);
	/**
	 * @param m A {@link Method} to be checked
	 * @return <code>true</code> if return type is as expected from the
	 * attribute type. <B>Note(s):</B></BR>
	 * <UL>
	 * 		<LI>
	 * 		The function does not check if other characteristics are met
	 * 		(e.g., name, number of parameters, etc.)
	 * 		</LI>
	 *
	 * 		<LI>
	 * 		For {@link #GETTER} the result is <code>true</code> if the
	 * 		return type is non-{@link Void}
	 * 		</LI>
	 * 
	 * 		<LI>
	 * 		For {@link #PREDICATE} the result is <code>true</code> if the
	 * 		return type is {@link Boolean} compatible
	 * 		</LI>
	 *
	 * 		<LI>
	 * 		For {@link #SETTER} the result is <code>true</code> if the
	 * 		return type is {@link Void} compatible
	 * 		</LI>
	 *
	 * 		<LI>
	 * 		For {@link #OPERATION} the result is
	 *  	<U>always <B><code>false</code></B></U>
	 *  	</LI>
	 *  </UL>
	 */
	public boolean isMatchingReturnType (Method m)
	{
		final Class<?>	rc=(null == m) ? null : m.getReturnType();
		return (null == rc) ? false : isMatchingReturnType(rc);
	}

	private final String	_prefix;
	/**
	 * @return expected method name prefix - <B>Note:</B> null/empty for
	 * {@link #OPERATION} type(s).
	 */
	public String getPrefix ()
	{
		return _prefix;
	}
	/**
	 * @param name {@link Method} name to be checked if has the same prefix
	 * as the enum value.
	 * @return TRUE if matching prefix - i.e., <U>not</U> an {@link #OPERATION}
	 * and at least one more character after the "get/set/is" prefix
	 */
	public boolean isMatchingPrefix (final String name)
	{
		/*
		 * 		Make sure have a prefix and it is shorter (!) then the name
		 * (i.e., "set"/"get"/"is" are not enough per-se - must be followed
		 * by at least one more character - e.g., "setX")
		 */ 
		return StringUtil.startsWith(name, getPrefix(), true, true);
	}

	public boolean isMatchingPrefix (final Method m)
	{
		return (null == m) ? false : isMatchingPrefix(m.getName());
	}

	public boolean isMatchingMethod (final Method m)
	{
		return (getAttributeType(m) != null);
	}
	/**
	 * @param name {@link Method} name - including "set/get/is"
	 * @return "pure" attribute name - null/empty if not a "set/get/isXXX"
	 * method name (or null/empty to begin with)
	 */
	public String getPureAttributeName (final String name)
	{
		if (!isMatchingPrefix(name))
			return null;

		final String	prefix=getPrefix();
		final int		pLen=prefix.length();
		return name.substring(pLen);
	}
	/**
	 * @param m "set/get/isXXX" {@link Method}
	 * @return "pure" attribute name - null/empty if not a "set/get/isXXX"
	 * method (or null instance to begin with)
	 */
	public String getPureAttributeName (final Method m)
	{
		return (null == m) ? null : getPureAttributeName(m.getName());
	}
	/**
	 * @param name "pure" attribute name
	 * @return expected {@link Method} name to access the attribute (may
	 * be null/empty if this is an {@link #OPERATION} or no pure attribute
	 * name to begin with.
	 */
	public String getAccessorName (final String name)
	{
		if (isOperation() || (null == name) || (name.length() <= 0))
			return null;
		else
			return getPrefix() + name;
	}

	private final boolean	_getter;
	/**
	 * @return TRUE if this is a "getXXX/isXXX" method
	 */
	public boolean isGetter ()
	{
		return _getter;
	}

	private final boolean	_operation;
	/**
	 * @return TRUE if this is <U>not</U> a "set/get/isXXX" method
	 */
	public boolean isOperation ()
	{
		return _operation;
	}
	/**
	 * @return TRUE if this is a "setXXX" method
	 */
	public boolean isSetter ()
	{
		return (!isGetter()) && (!isOperation());
	}

	private final boolean	_predicate;
	/**
	 * @return TRUE if this is an "isXXX" method
	 */
	public boolean isPredicate ()
	{
		return _predicate;
	}

	AttributeMethodType (boolean getter, boolean predicate, boolean operation, String prefix)
	{
		_getter = getter;
		_predicate = predicate;
		_operation = operation;
		_prefix = prefix;
	}

	public static final List<AttributeMethodType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final AttributeMethodType classifyAttributeName (final String name)
	{
		if ((null == name) || (name.length() <= 0))
			return null;

		for (final AttributeMethodType v : VALUES)
		{
			if ((v != null) && v.isMatchingPrefix(name))
				return v;
		}

		// if no match found by prefix then it must be an operation
		return OPERATION;
	}
	/**
	 * @param c The {@link Class} whose getter we wish to retrieve
	 * @param name The attribute name - if already contains the "is/get"
	 * prefix, then it is used as a hint, otherwise an attempt is made to
	 * locate the correct {@link Method}
	 * @return A pair represented as a {@link java.util.Map.Entry} whose key=the
	 * discovered {@link AttributeMethodType} and the value=its associated
	 * {@link Method}. Null if could not determine the classification
	 * @throws Exception If failed to use reflection API
	 */
	public static final Map.Entry<AttributeMethodType,Method> classifyAttributeGetterName (final Class<?> c, final String name) throws Exception
	{
		if ((null == c) || (null == name) || (name.length() <= 0))
			return null;

		for (final AttributeMethodType v : VALUES)
		{
			if ((v != null) && (v.isGetter() || v.isPredicate()))
			{
				final String	mthName;
				if (!v.isMatchingPrefix(name))
				{
					final String	aName=getAdjustedAttributeName(name);
					mthName = v.getAccessorName(aName);
				}
				else	// name already contains the "is/get"
					mthName = name;

				try
				{
					final Method	m=c.getMethod(mthName);
					if (null == m)	// should not happen
						throw new NoSuchMethodException("Unexpected null method");
					return new MapEntryImpl<AttributeMethodType,Method>(v, m);
				}
				catch(NoSuchMethodException e)
				{
					// ignored
				}
			}
		}

		return null;
	}
	/**
	 * @param m {@link Method} to be classified
	 * @return classification type - <B>Note:</B> <U>non-indexed</U> attribute
	 * <B>Note:</B> returns <I>null</I> if <I>null</I> {@link Method} instance
	 * supplied
	 */
	public static final AttributeMethodType classifyAttributeMethod (final Method m)
	{
		// first try to see if the method name is any indication
		final String				mName=(null == m) ? null : m.getName();
		final AttributeMethodType	nType=classifyAttributeName(mName);
		if ((null == nType) /* should not happen */
		 || OPERATION.equals(nType))	// if already know not "get/is/set" don't go any further
			return nType;

		final Class<?>	retType=m.getReturnType();
		if (null == retType)	// should not happen
			throw new IllegalStateException("classifyAttributeMethod(" + m + ") no return type class");

		final boolean		isVoidReturnType=
			Void.TYPE.isAssignableFrom(retType) || Void.class.isAssignableFrom(retType);
		final Class<?>[]	params=m.getParameterTypes();
		// check the by-name classification further
		switch(nType)
		{
			case GETTER		:
			case PREDICATE	:
				// make sure "get/is" have NO parameters
				if ((params != null) && (params.length != 0))
					return OPERATION;

				// make sure non-void return class
				if (isVoidReturnType)
					return OPERATION;

				// for "isXXX" make sure return type is boolean
				if (PREDICATE.equals(nType))
				{
					if (!ClassUtil.isBooleanCompatible(retType))
						return OPERATION;
				}
				break;

			case SETTER		:
				// make sure "set" has EXACTLY one parameter
				if ((null == params) || (params.length != 1) || (null == params[0]) /* should not happen */)
					return OPERATION;

				// make sure void return class
				if (!isVoidReturnType)
					return OPERATION;
				break;

			default			:	// unexpected code reached
				throw new IllegalStateException("classifyAttributeMethod(" + m + ") unexpected by-name classification: " + nType);
		}

		return nType;
	}
	/**
	 * @param name "pure" attribute name (without "set/get/is")
	 * @return attribute name with first character as uppercase (if not
	 * already such) - if null/empty to begin with then returns null/empty
	 */
	public static final String getAdjustedAttributeName (final String name)
	{
		final int	nLen=(null == name) ? 0 : name.length();
		if (nLen <= 0)
			return null;

		final char	ch1=name.charAt(0);
		if (('A' <= ch1) && (ch1 <= 'Z'))	// if already uppercase do nothing
			return name;

		final String	uch=String.valueOf(Character.toUpperCase(ch1));
		if (1 == nLen)	// if exactly one letter no need to go further
			return uch;

		return uch + name.substring(1);
	}
	
	public static final String toAttributeName (final String s)
	{
		final int	sLen=(null == s) ? 0 : s.length();
		if (sLen <= 0)
			return null;

		final String	n=s.toLowerCase(),
						ch1=String.valueOf(Character.toUpperCase(n.charAt(0)));
		if (sLen <= 1)
			return ch1;

		return ch1 + n.substring(1);
	}

	/**
	 * @param n A getter method name
	 * @return The method name without any parentheses
	 */
	public static final String getCleanGetterName (final String n)
	{
		final int	nLen=(null == n) ? 0 : n.length();
		if (nLen <= 0)
			return n;

		final int 	sPos=n.lastIndexOf(MethodUtil.PARAMS_START_DELIM);
		if (sPos > 0)
			return n.substring(0, sPos);

		return n;
	}
	/**
	 * @param an A "pure" attribute name in "German" notation - i.e., each
	 * "sub"-component is assumed to start with a capital letter (e.g.,
	 * <code>TheBigBadWolf</code>) - may be null/empty
	 * @return A {@link String} where each capital letter is preceded by a
	 * space - including converting the 1st letter to uppercase if necessary
	 * (e.g., <code>The Big Bad Wolf</code>)
	 * May be null/empty/same as input if null/empty to begin with or no
	 * processing was necessary
	 */
	public static final String getSpacedAttributeName (final String an)
	{
		final String	cn=StringUtil.getCleanStringValue(an),
						n=getAdjustedAttributeName(cn);	// ensure 1st letter is capital
		final int		nLen=(null == n) ? 0 : n.length();
		StringBuilder	sb=null;
		int				lIndex=0;

		for (int	cIndex=1; cIndex < nLen; cIndex++)
		{
			final char	c=n.charAt(cIndex);
			if ((c < 'A') || (c > 'Z'))
				continue;	// skip non capital letters

			if (null == sb)
				sb = new StringBuilder(nLen + 16 /* some extra spaces */);

			// check how much to copy as-is
			if (cIndex > lIndex)
			{
				final String	cs=n.substring(lIndex, cIndex);
				sb.append(cs);
			}

			sb.append(' ');
			lIndex = cIndex;
		}

		if ((null == sb) || (sb.length() <= 0))
			return n;

		// check if any leftovers
		if (lIndex < nLen)
		{
			final String	cs=n.substring(lIndex);
			sb.append(cs);
		}

		return sb.toString();
	}
	/**
	 * @param an A {@link String} containing an attribute name with spaces
	 * between its "sub-components" (e.g., <code>the big bad wolf</code>).
	 * May be null/empty.
	 * @return A {@link String} with all spaces removed and each sub-component
	 * starting with a capital letter (e.g., <code>TheBigBadWolf</code>).
	 * May be null/empty/same as input if null/empty to begin with or no
	 * processing was necessary
	 */
	public static final String getUnspacedAttributeName (final String an)
	{
		final String	cn=StringUtil.getCleanStringValue(an),
						n=getAdjustedAttributeName(cn);	// ensure 1st letter is capital
		final int		nLen=(null == n) ? 0 : n.length();
		StringBuilder	sb=null;
		int				lIndex=0;

		for (int	cIndex=1; cIndex < nLen; cIndex++)
		{
			final char	c=n.charAt(cIndex);
			if (c != ' ')
				continue;
			
			if (null == sb)
				sb = new StringBuilder(nLen /* actually this is more than needed due to the extra spaces */);

			// check how much to copy as-is
			if (cIndex > lIndex)
			{
				final String	cs=n.substring(lIndex, cIndex),
								as=getAdjustedAttributeName(cs);
				sb.append(as);
			}

			lIndex = cIndex + 1;	// skip the current space
		}

		if ((null == sb) || (sb.length() <= 0))
			return n;

		// check if any leftovers
		if (lIndex < nLen)
		{
			final String	cs=n.substring(lIndex),
							as=getAdjustedAttributeName(cs);
			sb.append(as);
		}

		return sb.toString();
	}
	/**
	 * @param c {@link Class} whose setters we want
	 * @return {@link Collection} of all {@link Method}-s that represent
	 * setters - may be null/empty if none found
	 */
	public static final Collection<Method> getSetters (final Class<?> c)
	{
		final Method[]	ma=(null == c) ? null : c.getMethods();
		if ((null == ma) || (ma.length <= 0))
			return null;

		Collection<Method>	res=null;
		for (final Method m : ma)
		{
			final AttributeMethodType	mType=classifyAttributeMethod(m);
			if (!SETTER.equals(mType))
				continue;	// we are interested only in setter-s

			if (null == res)
				res = new LinkedList<Method>();
			res.add(m);
		}

		return res;
	}
	/**
	 * @param c {@link Class} whose setters we want
	 * @return {@link Collection} of all {@link Method}-s that represent
	 * <U>public</U> setters - may be null/empty if none found
	 */
	public static final Collection<Method> getAccessibleSetters (final Class<?> c)
	{
		return MethodUtil.getPubliclyAccessibleMethods(getSetters(c));
	}

	public static final Map<String,Method> buildWriteableAttributesMap (final Collection<? extends Method> setters) throws IllegalStateException
	{
		if ((null == setters) || (setters.size() <= 0))
			return null;

		Map<String,Method>	res=null;
		for (final Method m : setters)
		{
			final String	name=SETTER.getPureAttributeName(m);
			if ((null == name) || (name.length() <= 0))
				continue;	// should not happen
	
			if (null == res)
				res = new TreeMap<String, Method>(String.CASE_INSENSITIVE_ORDER);

			final Method	prev=res.put(name, m);
			if (prev != null)	// should not happen
				throw new IllegalStateException("buildWriteableAttributesMap(" + m.getDeclaringClass().getName() + ")[" + name + "] multiple setters found: " + m + "/" + prev);
		}

		return res;
	}
	/**
	 * @param c {@link Class} whose "setXXX" attributes we want
	 * @return {@link Map} of writable attributes - key=case
	 * <U>insensitive</U>attribute "pure" name (without the "set"),
	 * value={@link Method} that can be used to set this attribute. May be
	 * null/empty if no such attributes found (or <I>null</I> {@link Class}
	 * instance to begin with)
	 * @throws IllegalStateException if multiple setters found for same
	 * attribute (<B>Caveat</B>: attribute name is case <U>insensitive</U> so
	 * "setX" and "setx" access the "same" attribute)
	 */
	public static final Map<String,Method> getWriteableAttributes (final Class<?> c) throws IllegalStateException
	{
		return buildWriteableAttributesMap(getSetters(c));
	}
	/**
	 * @param o {@link Object} whose "setXXX" attributes we want
	 * @return {@link Map} of writable attributes - key=case
	 * <U>insensitive</U>attribute "pure" name (without the "set"),
	 * value={@link Method} that can be used to set this attribute. May be
	 * null/empty if no such attributes found (or <I>null</I> {@link Object}
	 * instance to begin with)
	 * @throws IllegalStateException if multiple setters found for same
	 * attribute (<B>Caveat</B>: attribute name is case <U>insensitive</U> so
	 * "setX" and "setx" access the "same" attribute)
	 * @see #getWriteableAttributes(Class)
	 */
	public static final Map<String,Method> getWriteableAttributes (final Object o) throws IllegalStateException
	{
		return (null == o) ? null : getWriteableAttributes(o.getClass());
	}
	/**
	 * @param c {@link Class} whose <U>public</U> "setXXX" attributes we want
	 * @return {@link Map} of <U>publicly</U> writable attributes - key=case
	 * <U>insensitive</U>attribute "pure" name (without the "set"),
	 * value={@link Method} that can be used to set this attribute. May be
	 * null/empty if no such attributes found (or <I>null</I> {@link Class}
	 * instance to begin with)
	 * @throws IllegalStateException if multiple setters found for same
	 * attribute (<B>Caveat</B>: attribute name is case <U>insensitive</U> so
	 * "setX" and "setx" access the "same" attribute)
	 * @see #getWriteableAttributes(Class)
	 */
	public static final Map<String,Method> getAccessibleWriteableAttributes (final Class<?> c) throws IllegalStateException
	{
		return buildWriteableAttributesMap(getAccessibleSetters(c));
	}
	/**
	 * @param o {@link Object} whose <U>public</U> "setXXX" attributes we wan
	 * @return {@link Map} of <U>publicly</U> writable attributes - key=case
	 * <U>insensitive</U>attribute "pure" name (without the "set"),
	 * value={@link Method} that can be used to set this attribute. May be
	 * null/empty if no such attributes found (or <I>null</I> {@link Object}
	 * instance to begin with)
	 * @throws IllegalStateException if multiple setters found for same
	 * attribute (<B>Caveat</B>: attribute name is case <U>insensitive</U> so
	 * "setX" and "setx" access the "same" attribute)
	 * @see #getWriteableAttributes(Object)
	 */
	public static final Map<String,Method> getAccessibleWriteableAttributes (final Object o) throws IllegalStateException
	{
		return (null == o) ? null : getAccessibleWriteableAttributes(o.getClass());
	}
	/**
	 * @param c The {@link Class} whose attributes accessors to retrieve
	 * @param modVal The {@link Modifier} mask of the access allowed - if
	 * zero, then <U>all</U> access modes are allowed
	 * @return A {@link Map} where key="pure" attribute name and the value is
	 * the {@link AttributeAccessor} that can be used to access the attribute.
	 * <B>Note:</B> the {@link Map} is case <U>insensitive</U> unless
	 * otherwise specified. May be null/empty if no attributes found (or null
	 * {@link Class} instance to begin with).
	 */
	public static final Map<String,AttributeAccessor> extractAttributes (final Class<?> c, final int modVal /* 0 == all */)
	{
		final Method[]	ma=(null == c) ? null : c.getMethods();
		if ((null == ma) || (ma.length <= 0))
			return null;

		Map<String,AttributeAccessor>	attrsMap=null;
		for (final Method m : ma)
		{
			final AttributeMethodType	aType=classifyAttributeMethod(m);
			if ((null == aType)	/* should not happen */ || OPERATION.equals(aType))
				continue;

			// check if only specific modifiers required
			if (modVal != 0)
			{
				final int	mm=m.getModifiers();
				if (0 == (mm & modVal))
					continue;
			}

			final String	aName=aType.getPureAttributeName(m);
			if (null == attrsMap)
				attrsMap = new TreeMap<String, AttributeAccessor>(String.CASE_INSENSITIVE_ORDER);

			AttributeAccessor	aa=attrsMap.get(aName);
			if (null == aa)
			{
				aa = new AttributeAccessor(aName);
				attrsMap.put(aName, aa);
			}

			if (SETTER.equals(aType))
				aa.setSetter(m);
			else
				aa.setGetter(m);
		}

		return attrsMap;
	}
	/**
	 * @param c The {@link Class} whose attributes we want
	 * @return A {@link Map} where key="pure" attribute name and the value is
	 * the {@link AttributeAccessor} that can be used to access the attribute.
	 * <B>Note:</B> the {@link Map} is case <U>insensitive</U> unless
	 * otherwise specified. May be null/empty if no attributes found (or null
	 * {@link Class} instance to begin with).
	 */
	public static final Map<String,AttributeAccessor> getAllAttributes (final Class<?> c)
	{
		return extractAttributes(c, 0);
	}
	/**
	 * @param o An {@link Object} whose {@link Class} attributes we want to
	 * extract
	 * @return  A {@link Map} where key="pure" attribute name and the value is
	 * the {@link AttributeAccessor} that can be used to access the attribute.
	 * <B>Note:</B> the {@link Map} is case <U>insensitive</U> unless
	 * otherwise specified. May be null/empty if no attributes found (or null
	 * {@link Object} instance to begin with).
	 */
	public static final Map<String,AttributeAccessor> getAllAttributes (final Object o)
	{
		return (null == o) ? null : getAllAttributes(o.getClass());
	}
	/**
	 * @param c The {@link Class} whose <U>public</U> attributes we want
	 * @return A {@link Map} where key="pure" attribute name and the value is
	 * the {@link AttributeAccessor} that can be used to access the attribute.
	 * <B>Note:</B> the {@link Map} is case <U>insensitive</U> unless
	 * otherwise specified. May be null/empty if no attributes found (or null
	 * {@link Class} instance to begin with).
	 */
	public static final Map<String,AttributeAccessor> getAllAccessibleAttributes (final Class<?> c)
	{
		return extractAttributes(c, Modifier.PUBLIC);
	}
	/**
	 * @param o An {@link Object} whose {@link Class} <U>public</U> attributes we
	 * want to extract
	 * @return  A {@link Map} where key="pure" attribute name and the value is
	 * the {@link AttributeAccessor} that can be used to access the attribute.
	 * <B>Note:</B> the {@link Map} is case <U>insensitive</U> unless
	 * otherwise specified. May be null/empty if no attributes found (or null
	 * {@link Object} instance to begin with).
	 */
	public static final Map<String,AttributeAccessor> getAllAccessibleAttributes (final Object o)
	{
		return (null == o) ? null : getAllAccessibleAttributes(o.getClass());
	}
    /**
     * @param c The {@link Class} whose {@link AttributeAccessor} is required
     * @param propName The &quot;pure&quot; property name
     * @param propType The expected property type - may not be null
     * @return An {@link AttributeAccessor} with either get-ter/set-ter/both
     * if the property exists - <code>null</code> otherwise
     */
    public static final AttributeAccessor getPropertyAccessor (final Class<?>	c,
    														   final String		propName,
    														   final Class<?>	propType)
    {
    	if ((null == c)
    	 || (null == propName) || (propName.length() <= 0)
    	 || (null == propType))
    		return null;

    	final AttributeMethodType	gt=
    			Boolean.class.isAssignableFrom(propType) || Boolean.TYPE.isAssignableFrom(propType)
    		? AttributeMethodType.PREDICATE 
    		: AttributeMethodType.GETTER
    		;
    	final String	pn=AttributeMethodType.getAdjustedAttributeName(propName),
						gn=gt.getAccessorName(pn),
						sn=AttributeMethodType.SETTER.getAccessorName(pn);
		Method	gm=null;
		try
		{
			if ((gm=c.getMethod(gn)) != null)
			{
				// make sure getter return type is compatible
				final Class<?>	rt=gm.getReturnType();
				if (!propType.isAssignableFrom(rt))
					gm = null;
			}
		}
		catch(NoSuchMethodException e)
		{
			// ignored
		}

		Method	sm=null;
		try
		{
			sm = c.getMethod(sn, propType);
		}
		catch(NoSuchMethodException e)
		{
			// ignored
		}

		if ((gm != null) || (sm != null))
			return new AttributeAccessor(pn, gm, sm);

		return null;
    }

    private static final Map.Entry<AttributeMethodType,Class<?>>	OPERATION_INDEX=
    	new MapEntryImpl<AttributeMethodType,Class<?>>(OPERATION, Void.TYPE);
	/**
	 * Checks if a method is an indexed set/get-ter
	 * @param m The {@link Method} to be classified
	 * @return A pair represented as a {@link java.util.Map.Entry} where key=method type
	 * and value=the index {@link Class}.</BR>
	 * <P><B>Note(s):</B></P></BR>
	 * <UL>
	 * 		<LI>
	 * 		Return value is <code>null</code> if null method/name
	 * 		</LI>
	 * 
	 *  	<LI>
	 *  	The index for an {@link #OPERATION} type method is {@link Void}
	 *  	</LI>
	 * </UL>
	 */
	public static final Map.Entry<AttributeMethodType,Class<?>> classifyIndexedAttributeMethod (final Method m)
	{
		// first try to see if the method name is any indication
		final String				mName=(null == m) ? null : m.getName();
		final AttributeMethodType	nType=classifyAttributeName(mName);
		if (null == nType) // should not happen
			return null;
		 if (OPERATION.equals(nType))	// if already know not "get/is/set" don't go any further
			return OPERATION_INDEX;

		final Class<?>	retType=m.getReturnType();
		if (null == retType)	// should not happen
			throw new IllegalStateException("classifyIndexedAttributeMethod(" + m + ") no return type class");

		final boolean		isVoidReturnType=
			Void.TYPE.isAssignableFrom(retType) || Void.class.isAssignableFrom(retType);
		final Class<?>[]	params=m.getParameterTypes();
		switch(nType)
		{
			case GETTER		:
			case PREDICATE	:
				if ((null == params) || (params.length != 1))
					return OPERATION_INDEX;
				if (isVoidReturnType)
					return OPERATION_INDEX;

				// for "isXXX" make sure return type is boolean
				if (PREDICATE.equals(nType))
				{
					if (!ClassUtil.isBooleanCompatible(retType))
						return OPERATION_INDEX;
				}
				break;

			case SETTER	:
				// make sure "set" has EXACTLY two parameters
				if ((null == params) || (params.length != 2)
				 || (null == params[0]) || (null == params[1]) /* should not happen */)
					return OPERATION_INDEX;

				// make sure void return class
				if (!isVoidReturnType)
					return OPERATION_INDEX;
				break;

			default			:	// unexpected code reached
				throw new IllegalStateException("classifyIndexedAttributeMethod(" + m + ") unexpected by-name classification: " + nType);
		}
		
		final Class<?>	idxClass=params[0];
		if (Void.TYPE.isAssignableFrom(idxClass) || Void.class.isAssignableFrom(idxClass))
			return OPERATION_INDEX;

		return new MapEntryImpl<AttributeMethodType,Class<?>>(nType, idxClass);
	}
}