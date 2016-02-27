/*
 * 
 */
package net.community.chest.awt.font;

import java.awt.Font;
import java.awt.Paint;
import java.awt.font.GraphicAttribute;
import java.awt.font.NumericShaper;
import java.awt.font.TextAttribute;
import java.awt.font.TransformAttribute;
import java.awt.im.InputMethodHighlight;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import net.community.chest.CoVariantReturn;
import net.community.chest.awt.dom.ConvUtil;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.lang.StringUtil;
import net.community.chest.lang.math.NumberTables;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Encapsulates the {@link TextAttribute}-s into an {@link Enum}</P>
 * 
 * @author Lyor G.
 * @since Jun 17, 2009 3:43:49 PM
 */
public enum FontTextAttribute implements ValueStringInstantiator<Object> {
	FAMILY(TextAttribute.FAMILY, String.class) {
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#newInstance(java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public String newInstance (final String s) throws Exception
			{
				final FontFamilyValue	v=FontFamilyValue.fromString(s);
				if (v != null)
					return v.getAttributeValue();
				else
					return s;
			}
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#convertInstance(java.lang.Object)
			 */
			@Override
			public String convertInstance (final Object inst) throws Exception
			{
				final FontFamilyValue	v=
					(null == inst) ? null : FontFamilyValue.fromValue((String) inst);
				if (v != null)
					return v.toString();
				else
					return super.convertInstance(inst);
			}
		},
	WEIGHT(TextAttribute.WEIGHT, Float.class) {
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#newInstance(java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Float newInstance (final String s) throws Exception
			{
				final FontWeightValue	v=FontWeightValue.fromString(s);
				if (v != null)
					return v.getAttributeValue();
				else
					return (Float) super.newInstance(s);
			}
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#convertInstance(java.lang.Object)
			 */
			@Override
			public String convertInstance (final Object inst) throws Exception
			{
				final FontWeightValue	v=
					(null == inst) ? null : FontWeightValue.fromValue((Float) inst);
				if (v != null)
					return v.toString();
				else
					return super.convertInstance(inst);
			}
		},
	WIDTH(TextAttribute.WIDTH, Float.class) {
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#newInstance(java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Float newInstance (final String s) throws Exception
			{
				final FontWidthValue	v=FontWidthValue.fromString(s);
				if (v != null)
					return v.getAttributeValue();
				else
					return (Float) super.newInstance(s);
			}
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#convertInstance(java.lang.Object)
			 */
			@Override
			public String convertInstance (final Object inst) throws Exception
			{
				final FontWidthValue	v=
					(null == inst) ? null : FontWidthValue.fromValue((Float) inst);
				if (v != null)
					return v.toString();
				else
					return super.convertInstance(inst);
			}
		},
	POSTURE(TextAttribute.POSTURE, Float.class) {
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#newInstance(java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Float newInstance (final String s) throws Exception
			{
				final FontPostureValue	v=FontPostureValue.fromString(s);
				if (v != null)
					return v.getAttributeValue();
				else
					return (Float) super.newInstance(s);
			}
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#convertInstance(java.lang.Object)
			 */
			@Override
			public String convertInstance (final Object inst) throws Exception
			{
				final FontPostureValue	v=
					(null == inst) ? null : FontPostureValue.fromValue((Float) inst);
				if (v != null)
					return v.toString();
				else
					return super.convertInstance(inst);
			}
		},
	SIZE(TextAttribute.SIZE, Float.class) {
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#newInstance(java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Float newInstance (final String s) throws Exception
			{
				final Boolean	t=NumberTables.checkNumericalValue(s);
				if ((null == t) || t.booleanValue())
					return (Float) super.newInstance(s);
				
				throw new NumberFormatException(name() + " attribute value cannot be floating point");
			}			
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#convertInstance(java.lang.Object)
			 */
			@Override
			public String convertInstance (final Object inst) throws Exception
			{
				if (null == inst)
					return null;
				if (!(inst instanceof Number))
					throw new ClassCastException(name() + " attribute data (" + inst + ") is not a number - " + inst.getClass().getName());

				return String.valueOf(((Number) inst).intValue());
			}
		},
	TRANSFORM(TextAttribute.TRANSFORM, TransformAttribute.class),
	SUPERSCRIPT(TextAttribute.SUPERSCRIPT, Integer.class) {
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#newInstance(java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Integer newInstance (final String s) throws Exception
			{
				final FontSuperscriptValue	v=FontSuperscriptValue.fromString(s);
				if (v != null)
					return v.getAttributeValue();
				else
					return (Integer) super.newInstance(s);
			}
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#convertInstance(java.lang.Object)
			 */
			@Override
			public String convertInstance (final Object inst) throws Exception
			{
				final FontSuperscriptValue	v=
					(null == inst) ? null : FontSuperscriptValue.fromValue((Integer) inst);
				if (v != null)
					return v.toString();
				else
					return super.convertInstance(inst);
			}
		},
	FONT(TextAttribute.FONT, Font.class),
	CHARREPLACE(TextAttribute.CHAR_REPLACEMENT, GraphicAttribute.class),
	FOREGROUND(TextAttribute.FOREGROUND, Paint.class),
	BACKGROUND(TextAttribute.BACKGROUND, Paint.class),
	UNDERLINE(TextAttribute.UNDERLINE, Integer.class) {
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#newInstance(java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Integer newInstance (final String s) throws Exception
			{
				final FontUnderlineValue	v=FontUnderlineValue.fromString(s);
				if (v != null)
					return v.getAttributeValue();
				else
					return (Integer) super.newInstance(s);
			}
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#convertInstance(java.lang.Object)
			 */
			@Override
			public String convertInstance (final Object inst) throws Exception
			{
				final FontUnderlineValue	v=
					(null == inst) ? null : FontUnderlineValue.fromValue((Integer) inst);
				if (v != null)
					return v.toString();
				else
					return super.convertInstance(inst);
			}
		},
	STRIKETHROUGH(TextAttribute.STRIKETHROUGH, Boolean.class),
	RUNDIRECTION(TextAttribute.RUN_DIRECTION, Boolean.class) {
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#newInstance(java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Boolean newInstance (final String s) throws Exception
			{
				final FontRunDirectionValue	v=FontRunDirectionValue.fromString(s);
				if (v != null)
					return v.getAttributeValue();
				else
					return (Boolean) super.newInstance(s);
			}
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#convertInstance(java.lang.Object)
			 */
			@Override
			public String convertInstance (final Object inst) throws Exception
			{
				final FontRunDirectionValue	v=FontRunDirectionValue.fromValue((Boolean) inst);
				if (v == null)	// cannot happen since null is valid value 
					throw new NoSuchElementException(name() + "#convertInstance(" + inst + ") no match found");
				return v.toString();
			}
		},
	BIDIEMBEDDING(TextAttribute.BIDI_EMBEDDING, Integer.class),
	JUSTIFICATION(TextAttribute.JUSTIFICATION, Float.class) {
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#newInstance(java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Float newInstance (final String s) throws Exception
			{
				final FontJustificationValue	v=FontJustificationValue.fromString(s);
				if (v != null)
					return v.getAttributeValue();
				else
					return (Float) super.newInstance(s);
			}
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#convertInstance(java.lang.Object)
			 */
			@Override
			public String convertInstance (final Object inst) throws Exception
			{
				final FontJustificationValue	v=
					(null == inst) ? null : FontJustificationValue.fromValue((Float) inst);
				if (v != null)
					return v.toString();
				else
					return super.convertInstance(inst);
			}
		},
	INPMETHODHIGHLIGHT(TextAttribute.INPUT_METHOD_HIGHLIGHT, InputMethodHighlight.class),
	INPMETHODUNDERLINE(TextAttribute.INPUT_METHOD_UNDERLINE, Integer.class) {
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#newInstance(java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Integer newInstance (final String s) throws Exception
			{
				final FontInputMethodUnderline	v=FontInputMethodUnderline.fromString(s);
				if (v != null)
					return v.getAttributeValue();
				else
					return (Integer) super.newInstance(s);
			}
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#convertInstance(java.lang.Object)
			 */
			@Override
			public String convertInstance (final Object inst) throws Exception
			{
				final FontInputMethodUnderline	v=
					(null == inst) ? null : FontInputMethodUnderline.fromValue((Integer) inst);
				if (v != null)
					return v.toString();
				else
					return super.convertInstance(inst);
			}
		},
	SWAPCOLORS(TextAttribute.SWAP_COLORS, Boolean.class),
	NUMSHAPING(TextAttribute.NUMERIC_SHAPING, NumericShaper.class),
	KERNING(TextAttribute.KERNING, Integer.class) {
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#newInstance(java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Integer newInstance (final String s) throws Exception
			{
				final FontKerningValue	v=FontKerningValue.fromString(s);
				if (v != null)
					return v.getAttributeValue();
				else
					return (Integer) super.newInstance(s);
			}
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#convertInstance(java.lang.Object)
			 */
			@Override
			public String convertInstance (final Object inst) throws Exception
			{
				final FontKerningValue	v=
					(null == inst) ? null : FontKerningValue.fromValue((Integer) inst);
				if (v != null)
					return v.toString();
				else
					return super.convertInstance(inst);
			}
		},
	LIGATURES(TextAttribute.LIGATURES, Integer.class) {
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#newInstance(java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Integer newInstance (final String s) throws Exception
			{
				final FontLigaturesValue	v=FontLigaturesValue.fromString(s);
				if (v != null)
					return v.getAttributeValue();
				else
					return (Integer) super.newInstance(s);
			}
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#convertInstance(java.lang.Object)
			 */
			@Override
			public String convertInstance (final Object inst) throws Exception
			{
				final FontLigaturesValue	v=
					(null == inst) ? null : FontLigaturesValue.fromValue((Integer) inst);
				if (v != null)
					return v.toString();
				else
					return super.convertInstance(inst);
			}
		},
	TRACKING(TextAttribute.TRACKING, Float.class) {
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#newInstance(java.lang.String)
			 */
			@Override
			@CoVariantReturn
			public Float newInstance (final String s) throws Exception
			{
				final FontTrackingValue	v=FontTrackingValue.fromString(s);
				if (v != null)
					return v.getAttributeValue();

				return (Float) super.newInstance(s);
			}
			/*
			 * @see net.community.chest.awt.font.FontTextAttribute#convertInstance(java.lang.Object)
			 */
			@Override
			public String convertInstance (final Object inst) throws Exception
			{
				final FontTrackingValue	v=
					(null == inst) ? null : FontTrackingValue.fromValue((Float) inst);
				if (v != null)
					return v.toString();
				else
					return super.convertInstance(inst);
			}
		};

	private final TextAttribute	_attr;
	public final TextAttribute getTextAttribute ()
	{
		return _attr;
	}

	private final Class<?>	_vc;
	/*
	 * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public final Class getValuesClass ()
	{
		return _vc;
	}
	
	FontTextAttribute (TextAttribute a, Class<?> c)
	{
		_attr = a;
		_vc = c;
	}

	private ValueStringInstantiator<?>	_vsi;
	public synchronized ValueStringInstantiator<?> getValueStringInstantiator ()
	{
		if (null == _vsi)
		{
			final Class<?>				vc=getValuesClass();
			ValueStringInstantiator<?>	vsi=ConvUtil.getConverter(vc);
			if (null == vsi)
				vsi = ClassUtil.getJDKStringInstantiator(vc);
			_vsi = vsi;
		}

		return _vsi;
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public Object newInstance (final String vs) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(vs);
		if ((null == s) || (s.length() <= 0))
			return null;

		final ValueStringInstantiator<?>	vsi=getValueStringInstantiator();
		if (null == vsi)
			throw new NoSuchElementException("newInstance(" + name() + ")[" + s + "] no matching instantiator");

		return vsi.newInstance(s);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (final Object inst) throws Exception
	{
		final Class<?>	ic=(null == inst) ? null : inst.getClass();
		if (null == ic)
			return null;

		final ValueStringInstantiator<?>	vsi=getValueStringInstantiator();
		if (null == vsi)
			throw new NoSuchElementException("convertInstance(" + name() + ")[" + inst + "] no matching instantiator");

		final Class<?>	vc=vsi.getValuesClass();
		if (!vc.isAssignableFrom(ic))
			throw new ClassCastException("convertInstance(" + name() + ")[" + inst + "] mismatched types - got=" + ic.getName() + "/expected=" + vc.getName());

		@SuppressWarnings("unchecked")
		final String	s=((ValueStringInstantiator<Object>) vsi).convertInstance(inst);
		return s;
	}

	public static final List<FontTextAttribute>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final FontTextAttribute fromString (final String name)
	{
		return CollectionsUtils.fromString(VALUES, name, false);
	}

	public static final FontTextAttribute fromAttribute (final TextAttribute ta)
	{
		if (null == ta)
			return null;
		
		for (final FontTextAttribute v : VALUES)
		{
			final TextAttribute	va=(null == v) ? null : v.getTextAttribute();
			if ((ta == va) || ta.equals(va))
				return v;
		}

		return null;
	}
}
