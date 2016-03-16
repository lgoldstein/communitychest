package net.community.chest.dom.proxy;

import java.lang.reflect.AccessibleObject;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.Triplet;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.util.map.MapEntryImpl;
import net.community.chest.util.set.SetsUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> The reflected object type
 * @param <O> The {@link AccessibleObject} type used for reflection access
 * @author Lyor G.
 * @since Jan 24, 2008 1:24:31 PM
 */
public abstract class AbstractReflectiveProxy<V,O extends AccessibleObject>
			extends AbstractXmlProxyConverter<V> {
	private final Class<O>	_accClass;
	public final Class<O> getAccessObjectClass ()
	{
		return _accClass;
	}

	protected AbstractReflectiveProxy (Class<V> objClass, Class<O> accClass) throws IllegalArgumentException
	{
		super(objClass);

		if (null == (_accClass=accClass))
			throw new IllegalArgumentException("No accessible object instance provided");
	}

	protected abstract Map<String,O> extractAccessorsMap (Class<V> valsClass);

	private Map<String,O>	_accsMap	/* =null */;
	public synchronized Map<String,O> getAccessorsMap ()
	{
		if (null == _accsMap)
		{
			// make sure accessors are accessible
			if (((_accsMap=extractAccessorsMap(getValuesClass())) != null) && (_accsMap.size() > 0))
			{
				final Collection<? extends AccessibleObject>	accs=_accsMap.values();
				if ((accs != null) && (accs.size() > 0))
				{
					for (final AccessibleObject a : accs)
					{
						if ((a != null) && (!a.isAccessible()))
							a.setAccessible(true);
					}
				}
			}
		}

		return _accsMap; 
	}

	public synchronized void setAccessorsMap (Map<String,O> accsMap)
	{
		_accsMap = accsMap;
	}
	/**
	 * Called by default implementation of {@link #fromXmlAttributes(Object, NamedNodeMap)}
	 * when an attribute for which no matching setter is found
	 * @param src current object on which the proxy is working
	 * @param aName attribute name
	 * @param aValue attribute value
	 * @param accsMap The setters {@link Map} - key="pure" setter name, value={@link AccessibleObject}
	 * that can be invoked to set the attribute
	 * @return updated input object (highly recommended), but overriding
	 * classes may decide otherwise.
	 * @throws Exception (default) if unable to handle the attribute
	 */
	protected V handleUnknownAttribute (final V src, final String aName, final String aValue, final Map<String,? extends O> accsMap) throws Exception
	{
		throw new NoSuchMethodException("handleUnknownAttribute(" + aName + ")[" + aValue + "] for object=" + src + " with " + ((null == accsMap) ? 0 : accsMap.size()));
	}
	/**
	 * Called by default implementation of {@link #fromXmlAttributes(Object, NamedNodeMap)}
	 * when an attribute with a matching setter is found
	 * @param src current object on which the proxy is working
	 * @param aName attribute name
	 * @param aValue attribute value
	 * @param setter Setter {@link AccessibleObject} that can be used to update the object
	 * @return updated input object (highly recommended), but overriding
	 * classes may decide otherwise.
	 * @throws Exception if cannot set the object. <B>Note:</B> the default
	 * implementation takes care of all the primitive types conversion from
	 * the supplied string value (including {@link String} itself and {@link Enum}).
	 * If other conversions are required, then an <U>overriding</U> class must
	 * be implemented
	 */
	protected abstract V updateObjectAttribute (final V src, final String aName, final String aValue, final O setter) throws Exception;

	protected V handleDeferredAttribute (final V src, final String aName, final String aValue, final O setter) throws Exception
	{
		if ((null == src) || (null == setter))
			throw new UnsupportedOperationException("handleDeferredAttribute(" + aName + ")[" + aValue + "] no deferred context");
		else
			throw new UnsupportedOperationException("handleDeferredAttribute(" + aName + ")[" + aValue + "] no deferred attribute handling");
	}

	protected V updateObjectAttribute (
			final V src, final String aName, final String aValue, final boolean deferredAttr, final O setter) throws Exception
	{
		if (deferredAttr)
			return handleDeferredAttribute(src, aName, aValue, setter);
		else
			return updateObjectAttribute(src, aName, aValue, setter);
	}
	/**
	 * <P>Copyright 2008 as per GPLv2</P>
	 *
	 * <P>Used to return the result of the {@link AbstractReflectiveProxy#fromXmlAttributes(Object, NamedNodeMap)}
	 * invocation. Each "pair" contains the {@link Node} that was used to
	 * initialize an attribute and the {@link AccessibleObject} instance used
	 * for it</P>
	 * 
	 * @param <A> The {@link AccessibleObject} that was used to initialize
	 * the attribute
	 * @author Lyor G.
	 * @since Dec 31, 2008 1:03:23 PM
	 */
	public static class AttributeHandlingResult<A extends AccessibleObject> extends Triplet<Node,A,String> {
		public AttributeHandlingResult (Node v1, A v2, String v3)
		{
			super(v1, v2, v3);
		}

		public AttributeHandlingResult (Node v1, A v2)
		{
			this(v1, v2, null);
		}

		public AttributeHandlingResult (Node v1)
		{
			this(v1, null);
		}

		public AttributeHandlingResult ()
		{
			this(null);
		}

		public Node getNode ()
		{
			return getV1();
		}

		public void setNode (Node n)
		{
			setV1(n);
		}

		public A getAccessor ()
		{
			return getV2();
		}

		public void setAccessor (A a)
		{
			setV2(a);
		}

		public String getValue ()
		{
			return getV3();
		}

		public void setValue (String v)
		{
			setV3(v);
		}
	}

	public V fromXmlAttribute (final V 							src,
							   final String 					aName,
							   final AttributeHandlingResult<O> aRes,
							   final boolean	 				deferredAttr,
							   final Map<String,? extends O> 	sMap) throws Exception
	{
		if (null == aRes)
			return src;

		final String	aValue=aRes.getValue();
		final O			m=aRes.getAccessor();
		if (m != null)
			return updateObjectAttribute(src, aName, aValue, deferredAttr, m);
		else
			return handleUnknownAttribute(src, aName, aValue, sMap);
	}

	public V fromXmlAttributes (
			final V 										src, 
			final Map<String,AttributeHandlingResult<O>> 	resMap,
			final boolean	 								deferredAttrs,
			final Collection<String> 						aList) throws Exception
	{
		if ((null == aList) || (aList.size() <= 0)
		 || (null == resMap) || (resMap.size() <= 0))
			return src;

		Map<String,? extends O> sMap=null;
		V						retVal=src;
		for (final String aName : aList)
		{
			if ((null == aName) || (aName.length() <= 0))
				continue;

			final AttributeHandlingResult<O>	aRes=resMap.get(aName);
			if (null == aRes)
				continue;

			if (null == sMap)
				sMap = getAccessorsMap();

			retVal = fromXmlAttribute(retVal, aName, aRes, deferredAttrs, sMap);
		}

		return retVal;
	}

	public V fromXmlAttributes (
			final V 										src,
			final Map<String,AttributeHandlingResult<O>> 	resMap,
			final boolean 									deferredAttrs,
			final String ... 								attrs)
		throws Exception
	{
		return fromXmlAttributes(src, resMap, deferredAttrs, SetsUtils.setOf(String.CASE_INSENSITIVE_ORDER, attrs));
	}

	public Map.Entry<V,Map<String,AttributeHandlingResult<O>>> fromXmlAttributes (final V src, final NamedNodeMap attrs) throws Exception
	{
		final int								numAttrs=
			(null == attrs) /* OK */ ? 0 : attrs.getLength();
		final Map<String,? extends O> 			sMap=
			(numAttrs <= 0) /* don't need it if no attributes */ ? null : getAccessorsMap();
		V										retVal=src;
		Map<String,AttributeHandlingResult<O>>	resMap=null;
		for (int	aIndex=0; aIndex < numAttrs; aIndex++)
		{
			final Node	n=attrs.item(aIndex);
			if ((null == n) || (n.getNodeType() != Node.ATTRIBUTE_NODE))
				continue;	// should not happen

			final String						nName=n.getNodeName(),
												aName=getEffectiveAttributeName(nName),
												aValue=n.getNodeValue();
			final O								m=
				((null == sMap) || (sMap.size() <= 0)) ? null : sMap.get(aName);
			final AttributeHandlingResult<O>	aRes=new AttributeHandlingResult<O>(n, m, aValue);
			retVal = fromXmlAttribute(retVal, aName, aRes, false, sMap);

			if (null == resMap)
				resMap = new TreeMap<String,AttributeHandlingResult<O>>(String.CASE_INSENSITIVE_ORDER);

			final AttributeHandlingResult<O>	prev=resMap.put(aName, aRes);
			if (prev != null)
				throw new IllegalStateException("fromXmlAttributes(" + aName + ")[" + aValue + "] multiple handling results");
		}

		return new MapEntryImpl<V,Map<String,AttributeHandlingResult<O>>>(retVal, resMap);
	}
	/**
	 * Called by {@link #fromXml(Object, Element)} default implementation
	 * after handling the root element attributes.
	 * @param src The return value from {@link #fromXml(Object, Element)}
	 * call 
	 * @param cl A {@link Collection} of child {@link Element}-s - may be
	 * null/empty (though usually not the case)
	 * @param resMap A {@link Map} of all the attributes that were processed
	 * so far on the main instance - key=attribute name (usually case
	 * insensitive), value=the {@link AttributeHandlingResult} of the
	 * attribute's initialization
	 * @return Updated instance - should be same as input unless very good
	 * reason not to (in which case caller code must be <U>thoroughly</U>
	 * tested)
	 * @throws Exception if cannot initialize the instance properly.
	 * <B>Note:</B> unless overridden and a non-null/empty {@link Collection}
	 * of elements is received then by default it throws an {@link UnsupportedOperationException}
	 */
	public V fromXmlChildren (
			final V src, final Collection<? extends Element> cl,
			final Map<String,AttributeHandlingResult<O>> resMap) throws Exception
	{
		final int	numChildren=(null == cl) ? 0 : cl.size();
		if (numChildren <= 0)
			return src;

		V	ret=src;
		for (final Element elem : cl)
		{
			if (null == elem)
				continue;

			ret = fromXmlChild(ret, elem);
		}
	
		return ret;
	}
	// NOTE !!! returns null if no children (optimization) - in which case the source is assumed to be the same 
	protected Map.Entry<V,Collection<Element>> getXmlChildren (final V src, final Element elem) throws Exception
	{
		final Collection<Element>	cl=
			DOMUtils.extractAllNodes(Element.class, elem, Node.ELEMENT_NODE);
		if ((cl != null) && (cl.size() > 0))
			return new MapEntryImpl<V,Collection<Element>>(src, cl);

		return null;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlProxyConvertible#fromXml(java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public V fromXml (final V src, Element elem) throws Exception
	{
		final Map.Entry<? extends V,? extends Map<String,AttributeHandlingResult<O>>>	pe=
			fromXmlAttributes(src, elem.getAttributes());
		final V																			attrsSrc=
			(null == pe) ? null : pe.getKey();
		final Map.Entry<? extends V,? extends Collection<? extends Element>>			cp=
			getXmlChildren(attrsSrc, elem);
		final Collection<? extends Element>												cl=
			(null == cp) ? null : cp.getValue();
		final V																			chSrc=
			(null == cp) ? attrsSrc : cp.getKey();

		return fromXmlChildren(chSrc, cl, (null == pe) ? null : pe.getValue());
	}
	/**
	 * @param src Object instance
	 * @param attrName Attribute name
	 * @param aType Attribute type
	 * @return The {@link AccessibleObject} that can be used to access the
	 * specified attribute
	 * @throws Exception If failed to resolve accessor
	 */
	public abstract O getAttributeRetriever (final V src, final String attrName, final Class<?> aType) throws Exception;
	/**
	 * @param src Object instance
	 * @param attrName Attribute name
	 * @param aType Attribute type
	 * @return The specified attribute current value in the supplied instance
	 * @throws Exception If cannot access the attribute
	 */
	public abstract Object getAttributeValue (final V src, final String attrName, final Class<?> aType) throws Exception;

	public String setAttributeString (final V src, final String attrName, final Class<?> aType,
			final Document doc, final Element elem, final Object aValue) throws Exception
	{
		if ((null == src) || (null == aValue))
			return null;

		@SuppressWarnings("unchecked")
		final ValueStringInstantiator<? super Object>	vsi=
			(ValueStringInstantiator<? super Object>) getAttributeInstantiator(src, attrName, aType);
		final String									vString=
			(null == vsi) ? aValue.toString() : vsi.convertInstance(aValue); 
		if ((null == vString) || (vString.length() <= 0))
			return vString;

		elem.setAttribute(attrName, vString);
		return vString;
	}
	/**
	 * @param acc The accessor object
	 * @return The {@link Class} representing the accessed attribute type
	 */
	public abstract Class<?> resolveAccessorType (final O acc);

	public Collection<Map.Entry<String,?>> setAttributes (V src, Document doc, Element elem) throws Exception
	{
		final Map<String,? extends O>								sMap=getAccessorsMap();
		final Collection<? extends Map.Entry<String,? extends O>>	sList=
			((null == sMap) || (sMap.size() <= 0)) ? null : sMap.entrySet();
		final int													numAttrs=
			(null == sList) ? 0 : sList.size();
		if (numAttrs <= 0)
			return null;

		Collection<Map.Entry<String,?>>	attrs=null;
		for (final Map.Entry<String,? extends O> me : sList)
		{
			final String	aName=(null == me) ? null : me.getKey();
			final O			m=(null == me) ? null : me.getValue();
			if ((null == aName) || (aName.length() <= 0) || (null == m))
				continue;	// should not happen

			final Class<?>	pType=resolveAccessorType(m);
			final Object	v=getAttributeValue(src, aName, pType);
			if (null == v)
				continue;

			final String	vString=setAttributeString(src, aName, pType, doc, elem, v);
			if ((null == vString) || (vString.length() <= 0))
				continue;	// OK - may mean that this was an XML sub element

			if (null == attrs)
				attrs = new LinkedList<Map.Entry<String,?>>();
			attrs.add(new MapEntryImpl<String,Object>(aName, v));
		}

		return attrs;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlTranslator#toXml(java.lang.Object, org.w3c.dom.Document, org.w3c.dom.Element)
	 */
	@Override
	public Element toXml (V src, Document doc, Element elem) throws Exception
	{
		setAttributes(src, doc, elem);
		return elem;
	}
}
