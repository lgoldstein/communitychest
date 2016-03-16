/*
 *
 */
package net.community.chest.awt.stroke;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.convert.FloatValueStringConstructor;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlTranslator;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.lang.math.NumberTables;
import net.community.chest.util.map.MapEntryImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <S> The type of {@link BasicStroke} being instantiated
 * @author Lyor G.
 * @since Feb 2, 2009 12:19:41 PM
 */
public abstract class BasicStrokeValueTranslator<S extends BasicStroke>
                extends BaseTypedValuesContainer<S> implements XmlTranslator<S> {
    public BasicStrokeValueTranslator (Class<S> objClass) throws IllegalArgumentException
    {
        super(objClass);
    }

    protected static final Map.Entry<String,Float> getFloatValue (final float f, final String aName)
    {
        if (Float.isInfinite(f) || Float.isNaN(f))
            throw new NumberFormatException("getFloatValue(" + aName + ") bad value: " + f);

        final Float    v=Float.valueOf(f);
        try
        {
            final String    vs=FloatValueStringConstructor.DEFAULT.convertInstance(v);
            if ((null == vs) || (vs.length() <= 0))
                throw new NumberFormatException("getFloatValue(" + aName + ") no string for: " + f);

            return new MapEntryImpl<String,Float>(vs, v);
        }
        catch(Exception e)    // should not happen
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }

    protected static final Float xlateFloatValue (final String vs)
    {
        if ((null == vs) || (vs.length() <= 0))
            return null;

        final Float    vf;
        try
        {
            if (null == (vf=FloatValueStringConstructor.DEFAULT.newInstance(vs)))    // should not happen
                throw new IllegalStateException("xlateFloatValue(" + vs + ") no value converted");
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }

        final float    f=vf.floatValue();
        if (Float.isInfinite(f) || Float.isNaN(f))
            throw new NumberFormatException("xlateFloatValue(" + vs + ") bad value");

        return vf;
    }

    protected static final Map.Entry<String,Float> getFloatValue (final Element elem, final String aName)
    {
        if (null == elem)
            return null;
        if ((null == aName) || (aName.length() <= 0))
            throw new IllegalArgumentException("getFloatValue(" + DOMUtils.toString(elem) + ") no attribute specified");

        final String    vs=elem.getAttribute(aName);
        final Float        vf=xlateFloatValue(vs);
        if (null == vf)
            return null;    // OK if no attribute

        return new MapEntryImpl<String,Float>(vs, vf);
    }

    protected static final Float getPureFloatValue (final Element elem, final String aName)
    {
        final Map.Entry<String,Float>    vp=getFloatValue(elem, aName);
        if (null == vp)
            return null;

        final Float    vf=vp.getValue();
        if (null == vf)
            throw new IllegalStateException("getPureFloatValue(" + DOMUtils.toString(elem) + ")[" + aName + "] no value returned");

        return vf;
    }

    public static final String    LINE_WIDTH_ATTR="lineWidth",
                                ENDCAP_ATTR="endCap",
                                LINE_JOIN_ATTR="lineJoin",
                                MITER_LIMIT_ATTR="miterLimit",
                                DASH_PHASE_ATTR="dashPhase",
                                DASH_ARRAY_ATTR="dashArray";
    public static final String addLineWidth (BasicStroke s, Element elem, String aName)
    {
        if ((null == s) || (null == elem))
            return null;
        if ((null == aName) || (aName.length() <= 0))
            throw new IllegalArgumentException("addLineWidth(" + s + ") no attribute name");

        final float                        fv=s.getLineWidth();
        final Map.Entry<String,Float>    vp=getFloatValue(fv, aName);
        final String                    vs=(null == vp) ? null : vp.getKey();
        if ((null == vs) || (vs.length() <= 0))
            throw new IllegalStateException("addLineWidth(" + aName + ")[" + fv + "] no encoding result");

        elem.setAttribute(aName, vs);
        return vs;
    }

    public static final Float getLineWidth (Element elem)
    {
        return getPureFloatValue(elem, LINE_WIDTH_ATTR);
    }

    public static final String addLineWidth (BasicStroke s, Element elem)
    {
        return addLineWidth(s, elem, LINE_WIDTH_ATTR);
    }

    public static final String addMiterLimit (BasicStroke s, Element elem, String aName)
    {
        if ((null == s) || (null == elem))
            return null;
        if ((null == aName) || (aName.length() <= 0))
            throw new IllegalArgumentException("addMiterLimit(" + s + ") no attribute name");

        final float                        fv=s.getMiterLimit();
        final Map.Entry<String,Float>    vp=getFloatValue(fv, aName);
        final String                    vs=(null == vp) ? null : vp.getKey();
        if ((null == vs) || (vs.length() <= 0))
            throw new IllegalStateException("addMiterLimit(" + aName + ")[" + fv + "] no encoding result");

        elem.setAttribute(aName, vs);
        return vs;
    }

    public static final String addMiterLimit (BasicStroke s, Element elem)
    {
        return addMiterLimit(s, elem, MITER_LIMIT_ATTR);
    }

    public static final Float getMiterLimit (Element elem)
    {
        return getPureFloatValue(elem, MITER_LIMIT_ATTR);
    }

    public static final String addDashPhase (BasicStroke s, Element elem, String aName)
    {
        if ((null == s) || (null == elem))
            return null;
        if ((null == aName) || (aName.length() <= 0))
            throw new IllegalArgumentException("addDashPhase(" + s + ") no attribute name");

        final float                        fv=s.getDashPhase();
        final Map.Entry<String,Float>    vp=getFloatValue(fv, aName);
        final String                    vs=(null == vp) ? null : vp.getKey();
        if ((null == vs) || (vs.length() <= 0))
            throw new IllegalStateException("addDashPhase(" + aName + ")[" + fv + "] no encoding result");

        elem.setAttribute(aName, vs);
        return vs;
    }

    public static final String addDashPhase (BasicStroke s, Element elem)
    {
        return addDashPhase(s, elem, DASH_PHASE_ATTR);
    }

    public static final Float getDashPhase (Element elem)
    {
        return getPureFloatValue(elem, DASH_PHASE_ATTR);
    }

    public static final BasicStrokeDecoration addEndCap (BasicStroke s, Element elem, String aName)
    {
        if ((null == s) || (null == elem))
            return null;
        if ((null == aName) || (aName.length() <= 0))
            throw new IllegalArgumentException("addEndCap(" + s + ") no attribute name");

        final int                    v=s.getEndCap();
        final BasicStrokeDecoration    d=BasicStrokeDecoration.fromDecoration(v);
        if (null == d)
            throw new NoSuchElementException("addEndCap(" + s + ")[" + aName + "] unknown value: " + v);

        elem.setAttribute(aName, d.toString());
        return d;
    }

    public static final BasicStrokeDecoration addEndCap (BasicStroke s, Element elem)
    {
        return addEndCap(s, elem, ENDCAP_ATTR);
    }

    public static final BasicStrokeDecoration getEndCap (Element elem, String aName)
    {
        if (null == elem)
            return null;
        if ((null == aName) || (aName.length() <= 0))
            throw new IllegalArgumentException("getEndCap(" + DOMUtils.toString(elem) + ") no attribute name");

        final String    vs=elem.getAttribute(aName);
        if ((null == vs) || (vs.length() <= 0))
            return null;    // OK if no value

        final BasicStrokeDecoration    d=BasicStrokeDecoration.fromString(vs);
        if (null == d)
            throw new NoSuchElementException("getEndCap(" + DOMUtils.toString(elem) + ")[" + aName + "] unknown value: " + vs);

        return d;
    }

    public static final BasicStrokeDecoration getEndCap (Element elem)
    {
        return getEndCap(elem, ENDCAP_ATTR);
    }

    public static final BasicStrokeJoin addLineJoin (BasicStroke s, Element elem, String aName)
    {
        if ((null == s) || (null == elem))
            return null;
        if ((null == aName) || (aName.length() <= 0))
            throw new IllegalArgumentException("addLineJoin(" + s + ") no attribute name");

        final int                v=s.getLineJoin();
        final BasicStrokeJoin    j=BasicStrokeJoin.fromJoin(v);
        if (null == j)
            throw new NoSuchElementException("addLineJoin(" + s + ")[" + aName + "] unknown value: " + v);

        elem.setAttribute(aName, j.toString());
        return j;
    }

    public static final BasicStrokeJoin addLineJoin (BasicStroke s, Element elem)
    {
        return addLineJoin(s, elem, LINE_JOIN_ATTR);
    }

    public static final BasicStrokeJoin getLineJoin (Element elem, String aName)
    {
        if (null == elem)
            return null;
        if ((null == aName) || (aName.length() <= 0))
            throw new IllegalArgumentException("getLineJoin(" + DOMUtils.toString(elem) + ") no attribute name");

        final String    vs=elem.getAttribute(aName);
        if ((null == vs) || (vs.length() <= 0))
            return null;    // OK if no value

        final BasicStrokeJoin    j=BasicStrokeJoin.fromString(vs);
        if (null == j)
            throw new NoSuchElementException("getLineJoin(" + DOMUtils.toString(elem) + ")[" + aName + "] unknown value: " + vs);

        return j;
    }

    public static final BasicStrokeJoin getLineJoin (Element elem)
    {
        return getLineJoin(elem, LINE_JOIN_ATTR);
    }

    public static final List<String> addDashArray (BasicStroke s, Element elem, String aName)
    {
        if ((null == s) || (null == elem))
            return null;
        if ((null == aName) || (aName.length() <= 0))
            throw new IllegalArgumentException("addEndCap(" + s + ") no attribute name");

        final float[]    da=s.getDashArray();
        if ((null == da) || (da.length <= 0))
            return null;


        final List<String>    dl=new ArrayList<String>(da.length);
        final StringBuilder    sb=new StringBuilder(da.length * NumberTables.MAX_UNSIGNED_LONG_DIGITS_NUM);
        for (final float fv : da)
        {
            final Map.Entry<String,Float>    vp=getFloatValue(fv, aName);
            final String                    vs=(null == vp) ? null : vp.getKey();
            if ((null == vs) || (vs.length() <= 0))
                throw new IllegalStateException("addDashArray(" + aName + ")[" + fv + "] no encoding result");
            dl.add(vs);

            if (sb.length() > 0)
                sb.append(',');
            sb.append(vs);
        }

        elem.setAttribute(aName, sb.toString());
        return dl;
    }

    public static final List<String> addDashArray (BasicStroke s, Element elem)
    {
        return addDashArray(s, elem, DASH_ARRAY_ATTR);
    }

    public static final List<Float> getDashArray (Element elem, String aName)
    {
        if (null == elem)
            return null;
        if ((null == aName) || (aName.length() <= 0))
            throw new IllegalArgumentException("getDashArray(" + DOMUtils.toString(elem) + ") no attribute name");

        final String                vs=elem.getAttribute(aName);
        final Collection<String>    vl=StringUtil.splitString(vs, ',');
        final int                    numValues=(null == vl) ? 0 : vl.size();
        if (numValues <= 0)
            return null;    // OK if no value

        final List<Float>    fl=new ArrayList<Float>(numValues);
        for (final String fs : vl)
        {
            final Float    vf=xlateFloatValue(fs);
            if (null == vf)    // unlikely, but OK
                continue;

            fl.add(vf);
        }

        return fl;
    }

    public static final List<Float> getDashArray (Element elem)
    {
        return getDashArray(elem, DASH_ARRAY_ATTR);
    }

    public static final float    DEFAULT_LINE_WIDTH=1.0f, DEFAULT_MITER_LIMIT=10.0f, DEFAULT_DASH_PHASE=0.0f;
    public static final BasicStroke fromElement (Element elem)
    {
        if (null == elem)
            return null;

        final Float                    lineWidth=getLineWidth(elem),
                                    lw=(null == lineWidth) ? Float.valueOf(DEFAULT_LINE_WIDTH) : lineWidth,
                                    miterJoin=getMiterLimit(elem),
                                    mj=(null == miterJoin) ? Float.valueOf(DEFAULT_MITER_LIMIT) : miterJoin,
                                    dashPhase=getDashPhase(elem),
                                    dp=(null == dashPhase) ? Float.valueOf(DEFAULT_DASH_PHASE) : dashPhase;
        final BasicStrokeJoin        lineJoin=getLineJoin(elem),
                                    j=(null == lineJoin) ? BasicStrokeJoin.MITER : lineJoin;
        final BasicStrokeDecoration    lineCap=getEndCap(elem),
                                    d=(null == lineCap) ? BasicStrokeDecoration.SQUARE : lineCap;
        final List<Float>            dl=getDashArray(elem);
        final int                    numDashes=(null == dl) ? 0 : dl.size();
        final float[]                da=(numDashes <= 0) ? null : new float[numDashes];
        for (int    fIndex=0; fIndex < numDashes; fIndex++)
        {
            final Float    fv=dl.get(fIndex);
            da[fIndex] = fv.floatValue();
        }

        return new BasicStroke(lw.floatValue(), d.getDecoration(), j.getJoin(), mj.floatValue(), da, dp.floatValue());
    }
    /*
     * @see net.community.chest.dom.transform.XmlTranslator#toXml(java.lang.Object, org.w3c.dom.Document, org.w3c.dom.Element)
     */
    @Override
    public Element toXml (S src, Document doc, Element elem) throws Exception
    {
        addLineWidth(src, elem);
        addEndCap(src, elem);
        addLineJoin(src, elem);
        addMiterLimit(src, elem);
        addDashPhase(src, elem);
        addDashArray(src, elem);

        return elem;
    }

    public static final String    STROKE_ELEM_NAME=Stroke.class.getSimpleName().toLowerCase();
    public String getRootElementName ()
    {
        return STROKE_ELEM_NAME;
    }
    /*
     * @see net.community.chest.dom.transform.XmlTranslator#toXml(java.lang.Object, org.w3c.dom.Document)
     */
    @Override
    public Element toXml (S src, Document doc) throws Exception
    {
        return (null == src) ? null : toXml(src, doc, doc.createElement(getRootElementName()));
    }

    public static final BasicStrokeValueTranslator<BasicStroke>    DEFAULT=
            new BasicStrokeValueTranslator<BasicStroke>(BasicStroke.class) {
                /*
                 * @see net.community.chest.dom.transform.XmlValueInstantiator#fromXml(org.w3c.dom.Element)
                 */
                @Override
                public BasicStroke fromXml (Element elem) throws Exception
                {
                    return fromElement(elem);
                }
            };
}
