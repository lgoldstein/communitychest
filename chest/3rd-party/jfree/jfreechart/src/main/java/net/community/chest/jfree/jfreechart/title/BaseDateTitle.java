/*
 *
 */
package net.community.chest.jfree.jfreechart.title;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.text.DateFormat;
import java.util.Locale;
import java.util.NoSuchElementException;

import javax.swing.text.Style;

import net.community.chest.awt.font.FontValueInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.jfree.jcommon.ui.HAlignment;
import net.community.chest.jfree.jcommon.ui.RectEdge;
import net.community.chest.jfree.jcommon.ui.RectangleInsetsValueStringInstantiator;
import net.community.chest.jfree.jcommon.ui.VAlignment;
import net.community.chest.jfree.jfreechart.ChartColorValueInstantiator;
import net.community.chest.util.datetime.DateFormatStyle;
import net.community.chest.util.locale.LocaleUtils;

import org.jfree.chart.title.DateTitle;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.VerticalAlignment;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 27, 2009 4:04:31 PM
 */
public class BaseDateTitle extends DateTitle {
    /**
     *
     */
    private static final long serialVersionUID = -1351512782880507504L;

    public BaseDateTitle ()
    {
        this(DateFormat.LONG);
    }

    public BaseDateTitle (int s)
    {
        super(s);
    }

    public BaseDateTitle (int style, Locale locale, Font font, Paint paint)
    {
        super(style, locale, font, paint);
    }

    public BaseDateTitle (int style, Locale locale, Font font, Paint paint,
                          RectangleEdge position,
                          HorizontalAlignment hAlign, VerticalAlignment vAlign,
                          RectangleInsets padding)
    {
        super(style, locale, font, paint, position, hAlign, vAlign, padding);
    }

    public static final String    LOCALE_ATTR=Locale.class.getSimpleName(),
                                STYLE_ATTR=Style.class.getSimpleName(),
                                FONT_ATTR=Font.class.getSimpleName(),
                                PAINT_ATTR=Paint.class.getSimpleName(),
                                PADDING_ATTR="padding";

    public static final int getStyle (Element elem)
    {
        final String    s=DOMUtils.findFirstAttributeValue(elem, false, STYLE_ATTR);
        if ((null == s) || (s.length() <= 0))
            return DateFormat.LONG;

        final DateFormatStyle    dfs=DateFormatStyle.fromString(s);
        if (null == dfs)
            throw new NoSuchElementException("getStyle(" + DOMUtils.toString(elem) + ") unknown style: " + s);

        return dfs.getStyle();
    }

    public static final Locale getLocale (final Element elem)
    {
        final String    s=DOMUtils.findFirstAttributeValue(elem, false, LOCALE_ATTR);
        if ((null == s) || (s.length() <= 0))
            return Locale.getDefault();

        return LocaleUtils.getFormattingLocale(s);
    }

    public static final Font    DEFAULT_DATE_FONT=new Font("Dialog", Font.PLAIN, 12);
    public static final Font getFont (final Element elem)
    {
        final String    s=DOMUtils.findFirstAttributeValue(elem, false, FONT_ATTR);
        if ((null == s) || (s.length() <= 0))
            return DEFAULT_DATE_FONT;

        return FontValueInstantiator.fromString(s);
    }

    public static final Paint    DEFAULT_PAINT=Color.black;
    public static final Paint getPaint (final Element elem)
    {
        final String    s=DOMUtils.findFirstAttributeValue(elem, false, PAINT_ATTR);
        if ((null == s) || (s.length() <= 0))
            return DEFAULT_PAINT;

        return ChartColorValueInstantiator.fromChartColorString(s);
    }

    public static final RectangleEdge    DEFAULT_DATE_POSITION=RectangleEdge.BOTTOM;
    public static final RectangleEdge getPosition (final Element elem)
    {
        final String    s=DOMUtils.findFirstAttributeValue(elem, false, TitleReflectiveProxy.POSITION_ATTR);
        if ((null == s) || (s.length() <= 0))
            return DEFAULT_DATE_POSITION;

        final RectEdge    e=RectEdge.fromString(s);
        if (null == e)
            throw new NoSuchElementException("getPosition(" + DOMUtils.toString(elem) + ") unknown value: " + s);

        return e.getEdge();
    }

    public static final RectangleInsets getPadding (final Element elem)
    {
        final String    s=DOMUtils.findFirstAttributeValue(elem, false, PADDING_ATTR);
        if ((null == s) || (s.length() <= 0))
            return DEFAULT_PADDING;

        return RectangleInsetsValueStringInstantiator.fromString(s);
    }

    public static final HorizontalAlignment    DEFAULT_HALIGN=HorizontalAlignment.RIGHT;
    public static final VerticalAlignment    DEFAULT_VALIGN=VerticalAlignment.CENTER;

    public BaseDateTitle (Element elem) throws RuntimeException
    {
        this(getStyle(elem), getLocale(elem), getFont(elem), getPaint(elem), getPosition(elem),
             HAlignment.getAlignmentValue(elem, DEFAULT_HALIGN),
             VAlignment.getAlignmentValue(elem, DEFAULT_VALIGN),
             getPadding(elem));
    }
}
