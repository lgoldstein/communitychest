package net.community.chest.awt.font;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.ElementIndicatorExceptionContainer;
import net.community.chest.dom.impl.StandaloneElementImpl;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful static utilities for {@link Font}-s</P>
 * 
 * @author Lyor G.
 * @since Jul 30, 2007 7:33:50 AM
 */
public final class FontUtils {
	private FontUtils ()
	{
		// no instance
	}
	/**
	 * @param f {@link Font}
	 * @return style string suitable for correct decoding
	 * @see Font#decode(String) for available styles 
	 */
	public static final String convertFontStyle (final Font f)
	{
		final FontStyleType	t=FontStyleType.fromFont(f);
		if (null == t)
			return null;
		else
			return t.getStyleName();
	}

	public static final String	NAME_ATTR="name",
								STYLE_ATTR="style",
								SIZE_ATTR="size",
								REFID_ATTR="refid",
								VALUE_ATTR="value";

	public static final Map<TextAttribute,Object> getFontAttributes (final Element elem) throws Exception
	{
		final Collection<? extends Element>	al=DOMUtils.extractAllNodes(Element.class, elem, Node.ELEMENT_NODE);
		final int							numAttrs=(null == al) ? 0 : al.size();
		if (numAttrs <= 0)
			return null;

		final Map<TextAttribute,Object>	am=new HashMap<TextAttribute,Object>(numAttrs, 1.0f);
		for (final Element	ae : al)
		{
			final String	n=(null == ae) ? null : ae.getAttribute(NAME_ATTR),
							v=(null == ae) ? null : ae.getAttribute(VALUE_ATTR);
			if ((null == n) || (n.length() <= 0)
			 || (null == v) || (v.length() <= 0))
				throw new IllegalStateException("getFontAttributes(" + DOMUtils.toString(ae) + ") missing name/value");

			final FontTextAttribute	a=FontTextAttribute.fromString(n);
			if (null == a)
				throw new NoSuchElementException("getFontAttributes(" + DOMUtils.toString(ae) + ") unknown attribute: " + n);

			final Object	o=a.newInstance(v);
			if (null == o)
				throw new NoSuchElementException("getFontAttributes(" + DOMUtils.toString(ae) + ")[" + n + "] no generated value for data=" + v);

			final Object	prev=am.put(a.getTextAttribute(), o);
			if ((prev != null) && (!prev.equals(o)))
				throw new IllegalStateException("getFontAttributes(" + DOMUtils.toString(ae) + ")[" + n + "] value respecified: " + o + "/" + prev);
		}

		return am;
	}

	public static final Font applyFontAttributes (Font f, Element elem) throws Exception
	{
		final Map<? extends Attribute, ?>	am=getFontAttributes(elem);
		if ((null == am) || (am.size() <= 0))
			return f;
		else
			return f.deriveFont(am);
	}
	/**
	 * Retrieves a {@link Font} represented by an XML {@link Element} using
	 * the {@link #NAME_ATTR}, {@link #STYLE_ATTR} and {@link #SIZE_ATTR}
	 * attributes. <B>Note:</B> the name cannot be omitted - all the rest
	 * are optional (i.e., there are defaults for them)
	 * @param elem XML element - may NOT be null
	 * @return recovered font
	 * @throws DOMException cannot recover font from XML element
	 * @see Font#decode(String) for name/style/size format
	 */
	public static final Font fromXml (final Element elem) throws DOMException
	{
		final String	name=elem.getAttribute(NAME_ATTR);
		final int		nmLen=(null == name) ? 0 : name.length();
		if (nmLen <= 0)
			throw new DOMException(DOMException.NAMESPACE_ERR, ClassUtil.getExceptionLocation(FontUtils.class, "fromXml") + " no '" + NAME_ATTR + "' attribute");

		final String	style=elem.getAttribute(STYLE_ATTR), size=elem.getAttribute(SIZE_ATTR);
		final int		stLen=(null == style) /* OK */ ? 0 : style.length(),
						szLen=(null == size) /* OK */ ? 0 : size.length();
		if ((stLen <= 0) && (szLen <= 0))	// if no style and/or size, no need to go further
			return Font.decode(name);

		final int			totalLen=nmLen + 1 + Math.max(stLen,1) + 1 + Math.max(szLen,1) + 4;
		final StringBuilder	sb=new StringBuilder(totalLen).append(name);
		if (stLen > 0)
			sb.append('-').append(style);
		if (szLen > 0)
			sb.append('-').append(size);

		try
		{
			return applyFontAttributes(Font.decode(sb.toString()), elem);
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}
	}
	/**
	 * Used to derive a {@link Font} from another one using a give base font.
	 * In such a case only the style or size (or both) of the base font can
	 * be changed.
	 * @param elem XML {@link Element} containing the derived specifications</P>
	 * @param baseFont base font from which to derive the font</P>
	 * @return re-constructed font - <B>Note:</B> if no overrides specified then
	 * original base font returned
	 * @throws Exception if cannot re-construct or derive the font
	 */
	public static final Font fromDerivedXml (final Element elem, final Font baseFont) throws Exception
	{
		if ((null == elem) || (null == baseFont))
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, ClassUtil.getExceptionLocation(FontUtils.class, "fromDerivedXml") + " missing " + Element.class.getName() + "/" + Font.class.getName() + " instance(s)");

		final String	style=elem.getAttribute(STYLE_ATTR),
						size=elem.getAttribute(SIZE_ATTR);
		final int		stLen=(null == style) /* OK */ ? 0 : style.length(),
						szLen=(null == size) /* OK */ ? 0 : size.length();
		if ((stLen <= 0) && (szLen <= 0))	// if no style and/or size, no need to go further
			return applyFontAttributes(baseFont, elem);

		final FontStyleType	st=FontStyleType.fromStyleName(style);
		final Float			sz=(szLen <= 0) ? null : Float.valueOf(size);
		final Font			f;
		if (null == st)
			f = baseFont.deriveFont(sz.floatValue());
		else if (null == sz)
			f = baseFont.deriveFont(st.getStyleValue());
		else
			f = baseFont.deriveFont(st.getStyleValue(), sz.floatValue());

		return applyFontAttributes(f, elem);
	}
	/**
	 * Retrieves a {@link Font} represented by an XML {@link Element} using
	 * the {@link #NAME_ATTR}, {@link #STYLE_ATTR} and {@link #SIZE_ATTR}
	 * attributes. <B>Note:</B> the name cannot be omitted - all the rest
	 * are optional (i.e., there are defaults for them)
	 * @param elem XML element - may NOT be null
	 * @param fontsMap {@link Map} used for derived fonts lookup - in other
	 * words, if an XML element refers (via {@link #REFID_ATTR} to some
	 * other font as its base, then the base font <U>must already exist</U> in
	 * the fonts map. ID case sensitivity is left to the caller, but is is
	 * highly recommended to use a case <U>insensitive</U> map. <B>Note:</B>
	 * may be null - in which case a derived font lookup causes an exception</P>
	 * @return recovered font
	 * @throws Exception if cannot recover font from XML element
	 * @see Font#decode(String) for name/style/size format
	 */
	public static final Font fromXml (final Element elem, final Map<String,Font> fontsMap) throws Exception
	{
		if (null == elem)
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, ClassUtil.getExceptionLocation(FontUtils.class, "fromXml") + " missing " + Element.class.getName() + " instance");

		final String	refid=elem.getAttribute(REFID_ATTR);
		if ((refid != null) && (refid.length() > 0))
			return fromDerivedXml(elem, (null == fontsMap) /* should not happen */ ? null : fontsMap.get(refid));
		else
			return fromXml(elem);
	}
	/**
	 * Adds attributes to the XML {@link Element} so that the equivalent
	 * {@link #fromXml(Element)} method can decode it
	 * @param elem XML element whose attributes are to be added - may NOT
	 * be null
	 * @param font {@link Font} to be converted to XML element - may NOT be
	 * null
	 * @return same as input element
	 * @throws DOMException if cannot convert front
	 */
	public static final Element toXml (final Element elem, final Font font) throws DOMException
	{
		if (null == elem)
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, ClassUtil.getArgumentsExceptionLocation(FontUtils.class, "fromXml", font) + " missing " + Element.class.getName() + " instance");
		if (null == font)
			throw new DOMException(DOMException.NAMESPACE_ERR, ClassUtil.getArgumentsExceptionLocation(FontUtils.class, "fromXml", DOMUtils.toString(elem)) + " missing " + Font.class.getName() + " instance");

		elem.setAttribute(NAME_ATTR, font.getName());
		elem.setAttribute(STYLE_ATTR, convertFontStyle(font));
		elem.setAttribute(SIZE_ATTR, String.valueOf(font.getSize()));

		return elem;
	}

	public static final String	DEFAULT_FONT_ATTR_ELEM_NAME="attribute";
	public static final Collection<Element> toXml (final Document doc, final Map<? extends TextAttribute,?> attrsMap) throws DOMException
	{
		final Collection<? extends Map.Entry<? extends TextAttribute,?>>	al=
			((null == attrsMap) || (attrsMap.size() <= 0)) ? null : attrsMap.entrySet();
		final int															numAttrs=
			(null == al) ? 0 : al.size();
		if (numAttrs <= 0)
			return null;

		Collection<Element>	ret=null;
		for (final Map.Entry<? extends TextAttribute,?> ae : al)
		{
			final TextAttribute		ta=(null == ae) ? null : ae.getKey();
			final Object			tv=(null == ae) ? null : ae.getValue();
			final FontTextAttribute	ev=FontTextAttribute.fromAttribute(ta);
			if (null == ev)
				continue;

			final String	vs;
			try
			{
				if ((null == (vs=ev.convertInstance(tv))) || (vs.length() <= 0))
					continue;
			}
			catch(Exception e)
			{
				if (e instanceof DOMException)
					throw (DOMException) e;
				else
					throw new DOMException(DOMException.VALIDATION_ERR, "toXml(" + ev + ") " + e.getClass().getName() + ": " + e.getMessage());
			}

			final Element	elem=
				  (null == doc)
				? new StandaloneElementImpl(DEFAULT_FONT_ATTR_ELEM_NAME)
				: doc.createElement(DEFAULT_FONT_ATTR_ELEM_NAME)
				;
			elem.setAttribute(NAME_ATTR, ev.toString());
			elem.setAttribute(VALUE_ATTR, vs);
	
			if (null == ret)
				ret = new ArrayList<Element>(numAttrs);
			ret.add(elem);
		}

		return ret;
	}
	/**
	 * Default XML element name used in call to {@link #toXml(Document, Font)}
	 */
	public static final String	DEFAULT_FONT_ELEMNAME="font";
	/**
	 * @param doc {@link Document} to be used to create XML {@link Element}
	 * representing the {@link Font} argument - if null then a standalone
	 * element is created
	 * @param font {@link Font} to be represented as an XML element - may
	 * NOT be null
	 * @return XML {@link Element} representing the {@link Font} argument.
	 * <B>Note:</B> uses {@link #DEFAULT_FONT_ELEMNAME} as the element's name.
	 * @throws DOMException if unable to generate an element
	 * @see #toXml(Element, Font) for using your pre-initialized element
	 */
	public static final Element toXml (Document doc, Font font) throws DOMException
	{
		final Element						elem=
			toXml((null == doc) ? new StandaloneElementImpl(DEFAULT_FONT_ELEMNAME) : doc.createElement(DEFAULT_FONT_ELEMNAME), font);
		final Collection<? extends Element>	al=
			(null == elem) ? null : toXml(doc, (null == font) ? null : font.getAttributes());
		if ((al != null) && (al.size() > 0))
		{
			for (final Element ae : al)
			{
				if (null == ae)
					continue;
				elem.appendChild(ae);
			}
		}

		return elem;
	}
	// default comparator instance
	private static final RefElementsComparator	REFSCOMP=new RefElementsComparator();
	/**
	 * Converts extract XML {@link Element}-s into their {@link Font}-s
	 * counterparts.
	 * @param elemsMap {@link Map} of XML elements to be converted into fonts.
	 * Key=font logical "id", value=XML element representing the font. <B>Note:</B>
	 * map case sensitivity is left to the caller, but it is highly recommended
	 * to use a case <U>insensitive</U> map. If null/empty then nothing is done</P>
	 * @param fontsMap target map into which to place the re-constructed font(s).
	 * Key=font logical "id", value=re-constructed font.</P>
	 * <P><B>Note:</B> this map is also used for derived fonts lookup - in
	 * other words, if an XML element refers (via {@link #REFID_ATTR} to some
	 * other font as its base, then the base font <U>must already exist</U> in
	 * the fonts map. <B>Note:</B> may not be null</P>
	 * @return {@link Collection} of {@link ElementIndicatorExceptionContainer} of all
	 * exceptions incurred during parsing - including trying to override an
	 * already existing mapped font.
	 */
	public static final Collection<ElementIndicatorExceptionContainer> updateFontsMap (
					final Map<String,? extends Element> elemsMap,
					final Map<String,Font>				fontsMap)
	{
		final Collection<? extends Map.Entry<String,? extends Element>>	es=
			(null == elemsMap) ? null : elemsMap.entrySet();
		final int 														numElems=
			(null == es) ? 0 : es.size();
		@SuppressWarnings("unchecked")
		final Map.Entry<String,? extends Element>[]						fs=
			(numElems <= 0) ? null : es.toArray(new Map.Entry[numElems]);
		if ((null == fs) || (fs.length <= 0))
			return null;
		if (fs.length > 1)
			Arrays.sort(fs, REFSCOMP);

		Collection<ElementIndicatorExceptionContainer>	ret=null;
		for (final Map.Entry<String,? extends Element> fe : fs)
		{
			if (null == fe)	// should not happen
				continue;

			final String	id=fe.getKey();
			try
			{
				if ((null == id) || (id.length() <= 0))
					throw new IllegalStateException("No " + Font.class.getName() + " ID");

				if (fontsMap.get(id) != null)
					throw new IllegalStateException("Duplicate " + Font.class.getName() + " mapped ID=" + id);

				final Font	f=FontUtils.fromXml(fe.getValue(), fontsMap);
				if (null == f)
					throw new NoSuchElementException("No " + Font.class.getName() + " extracted for ID=" + id);

				fontsMap.put(id, f);
			}
			catch(Exception e)
			{
				final ElementIndicatorExceptionContainer	ind=new ElementIndicatorExceptionContainer(fe.getValue(), e);
				if (null == ret)
					ret = new LinkedList<ElementIndicatorExceptionContainer>();
				ret.add(ind);
			}
		}

		return ret;
	}
	/**
	 * Prefix character used to indicate a {@link Font} reference rather
	 * than a name
	 */
	public static final char	FONT_REF_CHAR='#';
	public static final boolean isFontReference (final CharSequence cs)
	{
		final int	csLen=(null == cs) ? 0 : cs.length();
		final char	ch1=(csLen <= 0) ? '\0' : cs.charAt(0);
		if ((ch1 == FONT_REF_CHAR) && (csLen > 1))
			return true;

		return false;
	}
	/**
	 * @param id A {@link Font} reference identifier
	 * @return A {@link Font} reference value - may be same as input if
	 * input already a reference value. <code>Null</code>/empty if original
	 * identifier is <code>null</code>/empty
	 * @see #isFontReference(CharSequence)
	 */
	public static final String toFontReference (String id)
	{
		if ((null == id) || (id.length() <= 0) || isFontReference(id))
			return id;

		return String.valueOf(FONT_REF_CHAR) + id;
	}
	/**
	 * @param ref Input {@link Font} reference value
	 * @return The {@link Font} reference "pure" identifier - may be same as
	 * input if input is not a reference value. <code>Null</code>/empty if
	 * original reference is <code>null</code>/empty
	 * @see #isFontReference(CharSequence)
	 */
	public static final CharSequence fromFontReference (CharSequence ref)
	{
		if (!isFontReference(ref))
			return ref;

		final int	csLen=(null == ref) ? 0 : ref.length();
		return (csLen <= 0) ? null : ref.subSequence(1, csLen);
	}

	public static final <V extends FontFloatAttributeValue> V fromAttributeValue (final float f, final Collection<? extends V> vals)
	{
		if ((null == vals) || (vals.size() <= 0))
			return null;

		for (final V v : vals)
		{
			final Float	vf=(null == v) ? null : v.getAttributeValue();
			if ((vf != null) && (vf.floatValue() == f))
				return v;
		}

		return null;
	}

	public static final <V extends FontFloatAttributeValue> V fromFloatAttributeValue (final Number n, final Collection<? extends V> vals)
	{
		return (null == n) ? null : fromAttributeValue(n.floatValue(), vals);
	}

	public static final <V extends FontIntAttributeValue> V fromAttributeValue (final int n, final Collection<? extends V> vals)
	{
		if ((null == vals) || (vals.size() <= 0))
			return null;

		for (final V v : vals)
		{
			final Integer	vf=(null == v) ? null : v.getAttributeValue();
			if ((vf != null) && (vf.intValue() == n))
				return v;
		}

		return null;
	}

	public static final <V extends FontIntAttributeValue> V fromIntAttributeValue (final Number n, final Collection<? extends V> vals)
	{
		return (null == n) ? null : fromAttributeValue(n.intValue(), vals);
	}
}
