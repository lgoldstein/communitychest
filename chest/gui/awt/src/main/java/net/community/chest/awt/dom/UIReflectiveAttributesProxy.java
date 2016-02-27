package net.community.chest.awt.dom;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.lang.reflect.Method;
import java.util.Map;

import net.community.chest.awt.ComponentSizeType;
import net.community.chest.awt.dom.converter.DimensionValueInstantiator;
import net.community.chest.awt.font.FontUtils;
import net.community.chest.awt.font.FontValueInstantiator;
import net.community.chest.awt.stroke.BasicStrokeValueTranslator;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.ReflectiveAttributesProxy;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.lang.StringUtil;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @param <V> The reflected type
 * @author Lyor G.
 * @since Dec 24, 2007 2:02:32 PM
 */
public class UIReflectiveAttributesProxy<V> extends ReflectiveAttributesProxy<V> {
	protected UIReflectiveAttributesProxy (Class<V> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public UIReflectiveAttributesProxy (Class<V> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}
	// attributes of interest
	public static final String	FONT_ATTR="font";
	/**
	 * Called by the {@link #updateObjectAttribute(Object, String, String, boolean, java.lang.reflect.AccessibleObject)}
	 * override when a {@link Font} reference is detected. By default it invokes
	 * {@link #updateObjectResourceAttribute(Object, String, String, Class, Method)}
	 * @param src The object instance on which the font should be applied (via
	 * the setter)
	 * @param name Name of attribute being set
	 * @param refid The "pure" reference identifier (after stripping the
	 * {@link FontUtils#FONT_REF_CHAR})
	 * @param fc The font {@link Class} being requested
	 * @param setter The setter {@link Method} to invoke once referenced font
	 * is resolved/loaded
	 * @return The updated object instance - same as input
	 * @throws Exception If failed to load/resolve the font or update the
	 * attribute
	 */
	protected V updateReferencedFont (V src, String name, String refid, Class<?> fc, Method setter) throws Exception
	{
		return updateObjectResourceAttribute(src, name, refid, fc, setter);
	}

	public boolean isFontElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, FONT_ATTR);
	}

	public XmlValueInstantiator<? extends Font> getFontConverter (final Element elem) throws Exception
	{
		return (null == elem) ? null : FontValueInstantiator.DEFAULT;
	}
	/*
	 * @see net.community.chest.dom.proxy.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.Class, java.lang.reflect.Method)
	 */
	@Override
	protected V updateObjectAttribute (final V src, final String aName, final String aValue,
			   						   final Class<?>	aType, final Method setter) 
		throws Exception
	{
		if (FONT_ATTR.equalsIgnoreCase(aName) && FontUtils.isFontReference(aValue)
		 && (aType != null) && Font.class.isAssignableFrom(aType))
			return updateReferencedFont(src, aName, FontUtils.fromFontReference(aValue).toString(), aType, setter);

		return super.updateObjectAttribute(src, aName, aValue, aType, setter);
	}
	/*
	 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
	 */
	@Override
	protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
	{
		ValueStringInstantiator<C>	vsi=super.resolveAttributeInstantiator(name, type);
		if (null == vsi)
			vsi = ConvUtil.getConverter(type);

		return vsi;
	}
	// all {@link java.awt.Component}-s have a "setName" method
	public static final String	NAME_ATTR="name";

	/* ---------- some special "virtual" attributes ----------- */
	public static final String	RELATIVE_SIZE_ATTR_PREFIX="relative", SIZE_ATTR="size";
	// returns null if this is not a relative size attribute
	protected ComponentSizeType getRelativeComponentSizeAttribute (final String name)
	{
		if (!StringUtil.startsWith(name, RELATIVE_SIZE_ATTR_PREFIX, true, false))
			return null;

		String	subType=name.substring(RELATIVE_SIZE_ATTR_PREFIX.length());
		if (StringUtil.endsWith(subType, SIZE_ATTR, true, false))
			subType = subType.substring(0, subType.length() - SIZE_ATTR.length());

		return ComponentSizeType.fromString(subType);
	}
	// each Dimension member is a percentage between 0-100 of the screen width/height
	protected Dimension getRelativeScreenSize (final String name, final String value) throws Exception
	{
		final Map.Entry<Integer,Integer>	rs=DimensionValueInstantiator.toIntPair(value);
		final Integer						rw=(null == rs) ? null : rs.getKey(),
											rh=(null == rs) ? null : rs.getValue();
		final int /* normalize to 100 */	pw=(null == rw) ? 0 : Math.min(rw.intValue(), 100),
											ph=(null == rh) ? 0 : Math.min(rh.intValue(), 100);
		if ((pw <= 0) || (ph <= 0))
			throw new IllegalArgumentException("Bad/Illegal " + name + ": " + value);

		final Toolkit	t=Toolkit.getDefaultToolkit();
		final Dimension	sd=t.getScreenSize();
		final int		sdw=sd.width, sdh=sd.height,
						// don't allow zero...
						fw=Math.max((sdw * pw) / 100, 10),
						fh=Math.max((sdh * ph) / 100, 10);
		return new Dimension(fw, fh);
	}

	public boolean isStrokeElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, BasicStrokeValueTranslator.STROKE_ELEM_NAME);
	}

	public XmlValueInstantiator<? extends Stroke> getStrokeConverter (Element elem)
	{
		return (null == elem) ? null : BasicStrokeValueTranslator.DEFAULT;
	}

	public Stroke setStrokeValue (final V src, final Element elem, final Method setter) throws Exception
	{
		final XmlValueInstantiator<? extends Stroke>	conv=getStrokeConverter(elem);
		final Stroke									s=(null == conv) ? null : conv.fromXml(elem);
		if (s != null)
			setter.invoke(src, s);
		return s;
	}

	public static final String	DEFAULT_STROKE_ATTR=Stroke.class.getSimpleName().toLowerCase();
	public Stroke setStrokeValue (final V src, final Element elem) throws Exception
	{
		if (null == elem)
			return null;

		final String						aValue=elem.getAttribute(NAME_ATTR),
											mName=
			((null == aValue) || (aValue.length() <= 0)) ? DEFAULT_STROKE_ATTR : aValue;
		final Map<String,? extends Method>	sm=getSettersMap();
		final Method						m=
			((null == sm) || (sm.size() <= 0)) ? null : sm.get(mName);
		if (null == m)
			throw new NoSuchMethodException("setStrokeValue(" + DOMUtils.toString(elem) + ") no setter for attribute=" + mName);

		return setStrokeValue(src, elem, m);
	}
}
