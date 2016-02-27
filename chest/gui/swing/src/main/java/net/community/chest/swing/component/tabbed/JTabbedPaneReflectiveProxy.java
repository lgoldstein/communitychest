/*
 * 
 */
package net.community.chest.swing.component.tabbed;

import java.awt.Component;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.swing.JTabbedPane;

import net.community.chest.awt.dom.converter.KeyCodeValueInstantiator;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.lang.StringUtil;
import net.community.chest.lang.math.NumberTables;
import net.community.chest.reflect.AttributeMethodType;
import net.community.chest.swing.component.JComponentReflectiveProxy;
import net.community.chest.util.map.MapEntryImpl;
import net.community.chest.util.map.entries.StringPairEntry;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> The reflected {@link JTabbedPane} instance
 * @author Lyor G.
 * @since Dec 23, 2008 8:34:10 AM
 */
public class JTabbedPaneReflectiveProxy<P extends JTabbedPane> extends JComponentReflectiveProxy<P> {
	public JTabbedPaneReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected JTabbedPaneReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final String	INDEXED_TAB_METHOD_SUFFIX="At";
	public static final boolean isIndexedTabMethod (final Method m)
	{
		final String	n=(null == m) ? null : m.getName();
		final int		nLen=(null == n) ? 0 : n.length();
		// name must be "setXXXAt"
		if ((nLen <= INDEXED_TAB_METHOD_SUFFIX.length())
		 || (!n.endsWith(INDEXED_TAB_METHOD_SUFFIX))
		 || (!AttributeMethodType.SETTER.isMatchingPrefix(n)))
			return false;

		// method must be public
		if (!Modifier.isPublic(m.getModifiers()))
			return false;

		// Method must have exactly 2 parameters
		final Class<?>[]	pa=m.getParameterTypes();
		if ((null == pa) || (pa.length != 2))
			return false;

		// first parameter must be an "int"
		final Class<?>	idx=pa[0];
		if (null == idx)
			return false;
		if ((!Integer.class.isAssignableFrom(idx))
		 && (!Integer.TYPE.isAssignableFrom(idx)))
			return false;

		final Class<?>	rt=m.getReturnType();
		if (!AttributeMethodType.SETTER.isMatchingReturnType(rt))
			return false;

		return true;
	}

	private Map<String,Map.Entry<Method,Class<?>>>	_idxMap	/* =null */;
	public Map<String,Map.Entry<Method,Class<?>>> getIndexedMethodsMap ()
	{
		return _idxMap;
	}

	public void setIndexedMethodsMap (Map<String,Map.Entry<Method,Class<?>>> im)
	{
		if (_idxMap != im)
			_idxMap = im;
	}
	/**
	 * Parses a "pair" consisting of an <code><I>int</I></code> value and
	 * a {@link String} separated by a colon - used to invoke the indexed
	 * setters of {@link JTabbedPane}
	 * @param value The pair value
	 * @return A {@link java.util.Map.Entry} whose key=the index value, value=the
	 * {@link String} representation of the value to be set at that index.
	 * May be null if original value is null/empty
	 * @throws NumberFormatException If bad format
	 */
	public static final Map.Entry<String,String> getIndexedAttributeParameters (final String value) throws NumberFormatException
	{
		final int	vLen=(null == value) ? 0 : value.length(),
					sPos=(vLen <= 2) /* must be at least a,b */ ? (-1) : value.indexOf(',');
		if (vLen <= 0)
			return null;
		if ((sPos <= 0) /* cannot be 1st */ || (sPos >= (vLen-1) /* cannot be last */))
			throw new NumberFormatException("getIndexedAttributeParameters(" + value + ") bad format");

		final String	n=value.substring(0, sPos), s=value.substring(sPos + 1);
		return new StringPairEntry(n, s);
	}

	protected boolean isMatchingTabComponent (P src, String name, String idx, String value, Component c)
	{
		if ((null == src) || (null == name) || (name.length() <= 0))
			throw new IllegalArgumentException("isMatchingTabComponent(" + name + ")[" + idx + "] incomplete parameters for value=" + value);

		if ((null == idx) || (idx.length() <= 0))
			return false;

		final String	n=(null == c) ? null : c.getName();
		if (0 == StringUtil.compareDataStrings(n, idx, false))
			return true;

		return false;
	}

	protected Integer findTabIndex (P src, String name, String idx, String value, Method setter, Class<?> vType) throws Exception
	{
		if ((null == setter) || (null == vType))
			throw new IllegalArgumentException("findTabIndex(" + name + ")[" + value + "] incomplete parameters");

		if ((null == idx) || (idx.length() <= 0))
			return null;

		final int	tc=(null == src) ? 0 : src.getTabCount();
		for (int	tIndex=0; tIndex < tc; tIndex++)
		{
			final Component	c=src.getComponentAt(tIndex);
			if (isMatchingTabComponent(src, name, idx, value, c))
				return Integer.valueOf(tIndex);
		}

		return null;
	}

	public Map.Entry<Integer,String> getIndexedAttributeParameters (P src, String name, String value, Method setter, Class<?> vType) throws Exception
	{
		if ((null == src) || (null == name) || (name.length() <= 0) || (null == setter) || (null == vType))
			throw new IllegalArgumentException("getIndexedAttributeParameters(" + name + ")[" + value + "] incomplete parameters");

		final Map.Entry<String,String>	ip=getIndexedAttributeParameters(value);
		final String					idx=(null == ip) ? null : ip.getKey(),
										v=(null == ip) ? null : ip.getValue();
		// if index is non-numerical assume it is a component name
		final Boolean					it=NumberTables.checkNumericalValue(idx);
		if ((it != null) && it.booleanValue())
			return new MapEntryImpl<Integer,String>(Integer.valueOf(idx), v);

		final Integer	iv=findTabIndex(src, name, idx, v, setter, vType);
		if (null == iv)
			throw new NoSuchElementException("getIndexedAttributeParameters(" + name + "[" + idx + "]) failed to locate tab");

		return new MapEntryImpl<Integer,String>(iv, v);
	}

	// some indexed attributes of interest
	public static final String	ICON_AT_ATTR="IconAt",
								DISABLED_ICON_AT_ATTR="DisabledIconAt",
								MNEMONIC_AT_ATTR="MnemonicAt";
	protected P updateIndexedAttribute (P src, String name, String value, Method setter, Class<?> vType) throws Exception
	{
		final Map.Entry<Integer,String>	vp=getIndexedAttributeParameters(src, name, value, setter, vType);
		final Integer					idx=(null == vp) ? null : vp.getKey();
		final String					effValue=(null == vp) ? null : vp.getValue();
		if (null == idx)
			throw new NumberFormatException("updateIndexedAttribute(" + name + "[" + value + "]) no data extracted");

		final Object	arg;
		if (MNEMONIC_AT_ATTR.equalsIgnoreCase(name))
		{
			arg = KeyCodeValueInstantiator.fromString(value);
			if (null == arg)
				throw new NoSuchElementException("updateIndexedAttribute(" + name + "[" + value + "]) unknown value");
		}
		else if (ICON_AT_ATTR.equalsIgnoreCase(name)
 			  || DISABLED_ICON_AT_ATTR.equalsIgnoreCase(name))
			arg = loadObjectResourceAttribute(src, name, effValue, vType);
 		else
			arg = getObjectAttributeValue(src, name, effValue, vType);

		setter.invoke(src, idx, arg);
		return src;
	}

	protected P updateIndexedAttribute (P src, String name, String value) throws Exception
	{
		final Map<String,? extends Map.Entry<? extends Method,Class<?>>>	im=
			((null == name) || (name.length() <= 0)) ? null : getIndexedMethodsMap();
		final Map.Entry<? extends Method,Class<?>>							mp=
			 ((null == im) || (im.size() <= 0)) ? null : im.get(name);
		final Method														setter=
			(null == mp) ? null : mp.getKey();
		final Class<?>														vType=
			(null == mp) ? null : mp.getValue();
		if ((null == setter) || (null == vType))
			throw new NoSuchMethodException("updateIndexedAttribute(" + name + ")[" + value + "] no setter/value type");

		return updateIndexedAttribute(src, name, value, setter, vType);
	}

	public boolean isIndexedTabAttribute (String name)
	{
		final Map<String,? extends Map.Entry<? extends Method,Class<?>>>	im=
			((null == name) || (name.length() <= 0)) ? null : getIndexedMethodsMap();

		return (im != null) && (im.size() > 0) && im.containsKey(name);
	}
	/*
	 * @see net.community.chest.awt.dom.proxy.ComponentReflectiveProxy#handleUnknownAttribute(java.awt.Component, java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	protected P handleUnknownAttribute (P src, String name, String value, Map<String,? extends Method> accsMap) throws Exception
	{
		if (isIndexedTabAttribute(name))
			return updateIndexedAttribute(src, name, value);

		return super.handleUnknownAttribute(src, name, value, accsMap);
	}

	protected Map<String,Map.Entry<Method,Class<?>>> extractIndexedSettersMap (Class<P> valsClass)
	{
		Map<String,Map.Entry<Method,Class<?>>>	im=getIndexedMethodsMap();
		final Method[]							ma=
			(null == valsClass) ? null : valsClass.getMethods();
		if ((null == ma) || 	(ma.length <= 0))	// should not happen
			return im;

		for (final Method m : ma)
		{
			if (!isIndexedTabMethod(m))
				continue;

			final String	name=AttributeMethodType.SETTER.getPureAttributeName(m);
			if ((null == name) || (name.length() <= 0))
				continue;	// should not happen

			if (null == im)
			{
				setIndexedMethodsMap(new TreeMap<String,Map.Entry<Method,Class<?>>>(String.CASE_INSENSITIVE_ORDER));

				if (null == (im=getIndexedMethodsMap()))
					throw new IllegalStateException("extractSettersMap() failed to set indexed methods map");
			}

			final Class<?>[]					pa=m.getParameterTypes();
			final Map.Entry<Method,Class<?>>	me=new MapEntryImpl<Method,Class<?>>(m, pa[1]),
												prev=im.put(name, me);
			if (prev != null)	// should not happen
				throw new IllegalStateException("Duplicate indexed setter(s): " + name);
		}

		return im;
	}
	/* Add the "setXXXAt(int,...)" methods as well
	 * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#extractSettersMap(java.lang.Class)
	 */
	@Override
	protected Map<String,Method> extractSettersMap (Class<P> valsClass)
	{
		final Map<String,Method>	sMap=super.extractSettersMap(valsClass);
		extractIndexedSettersMap(valsClass);
		return sMap;
	}

	// some attributes of interest
	public static final String	TAB_PLACEMENT_ATTR=TabPlacement.class.getSimpleName(),
								TAB_LAYOUT_POLICY_ATTR=TabLayoutPolicy.class.getSimpleName();
	/*
	 * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
	{
		if (TAB_PLACEMENT_ATTR.equalsIgnoreCase(name))
			return (ValueStringInstantiator<C>) TabPlacementValueStringInstantiator.DEFAULT;
		else if (TAB_LAYOUT_POLICY_ATTR.equalsIgnoreCase(name))
			return (ValueStringInstantiator<C>) TabLayoutPolicyValueStringInstantiator.DEFAULT;

		return super.resolveAttributeInstantiator(name, type);
	}

	public static final JTabbedPaneReflectiveProxy<JTabbedPane>	TABBED=
		new JTabbedPaneReflectiveProxy<JTabbedPane>(JTabbedPane.class, true);
}
