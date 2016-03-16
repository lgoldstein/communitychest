/*
 *
 */
package net.community.chest.jfree.jfreechart.plot.category;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.NoSuchElementException;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.ReflectiveAttributesProxy;
import net.community.chest.jfree.jfreechart.chart.renderer.BaseGeneratorConverter;

import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.IntervalCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2009 12:26:04 PM
 */
public class CategoryToolTipGeneratorConverter extends BaseGeneratorConverter<CategoryToolTipGenerator> {
    public CategoryToolTipGeneratorConverter ()
    {
        super(CategoryToolTipGenerator.class);
    }

    public StandardCategoryToolTipGenerator createStandardCategoryToolTipGenerator (Element elem) throws Exception
    {
        final String        lbl=getLabelFormat(elem);
        final NumberFormat    nf=getNumberFormat(elem);
        final DateFormat    df=getDateFormat(elem);
        if ((null == lbl) || (lbl.length() <= 0))
        {
            if ((nf != null) || (df != null))
                throw new IllegalArgumentException("createStandardCategoryToolTipGenerator(" + DOMUtils.toString(elem) + ") superfluous arguments to empty constructor");

            return new StandardCategoryToolTipGenerator();
        }

        if ((nf != null) && (df != null))
            throw new IllegalStateException("createStandardCategoryToolTipGenerator(" + DOMUtils.toString(elem) + ") ambiguous formatting arguments");
        if ((nf == null) && (df == null))
            throw new IllegalStateException("createStandardCategoryToolTipGenerator(" + DOMUtils.toString(elem) + ") no formatting arguments");

        if (null == df)
            return new StandardCategoryToolTipGenerator(lbl, nf);
        else
            return new StandardCategoryToolTipGenerator(lbl, df);
    }

    public BoxAndWhiskerToolTipGenerator createBoxAndWhiskerToolTipGenerator (Element elem) throws Exception
    {
        final String        lbl=getLabelFormat(elem);
        final NumberFormat    nf=getNumberFormat(elem);
        if ((null == lbl) || (lbl.length() <= 0))
        {
            if (nf != null)
                throw new IllegalArgumentException("createBoxAndWhiskerToolTipGenerator(" + DOMUtils.toString(elem) + ") superfluous arguments to empty constructor");

            return new BoxAndWhiskerToolTipGenerator();
        }

        if (null == nf)
            throw new IllegalStateException("createBoxAndWhiskerToolTipGenerator(" + DOMUtils.toString(elem) + ") no formatting arguments");

        return new BoxAndWhiskerToolTipGenerator(lbl, nf);
    }

    public IntervalCategoryToolTipGenerator createIntervalCategoryToolTipGenerator (Element elem) throws Exception
    {
        final String        lbl=getLabelFormat(elem);
        final NumberFormat    nf=getNumberFormat(elem);
        final DateFormat    df=getDateFormat(elem);
        if ((null == lbl) || (lbl.length() <= 0))
        {
            if ((nf != null) || (df != null))
                throw new IllegalArgumentException("createIntervalCategoryToolTipGenerator(" + DOMUtils.toString(elem) + ") superfluous arguments to empty constructor");

            return new IntervalCategoryToolTipGenerator();
        }

        if ((nf != null) && (df != null))
            throw new IllegalStateException("createIntervalCategoryToolTipGenerator(" + DOMUtils.toString(elem) + ") ambiguous formatting arguments");
        if ((nf == null) && (df == null))
            throw new IllegalStateException("createIntervalCategoryToolTipGenerator(" + DOMUtils.toString(elem) + ") no formatting arguments");

        if (null == df)
            return new IntervalCategoryToolTipGenerator(lbl, nf);
        else
            return new IntervalCategoryToolTipGenerator(lbl, df);
    }
    /*
     * @see net.community.chest.dom.transform.XmlValueInstantiator#fromXml(org.w3c.dom.Element)
     */
    @Override
    public CategoryToolTipGenerator fromXml (Element elem) throws Exception
    {
        final String    cls=elem.getAttribute(ReflectiveAttributesProxy.CLASS_ATTR);
        if ((null == cls) || (cls.length() <= 0) || "standard".equalsIgnoreCase(cls))
            return createStandardCategoryToolTipGenerator(elem);
        else if ("box".equalsIgnoreCase(cls))
            return createBoxAndWhiskerToolTipGenerator(elem);
        else if ("interval".equalsIgnoreCase(cls))
            return createIntervalCategoryToolTipGenerator(elem);
        else
            throw new NoSuchElementException("fromXml(" + DOMUtils.toString(elem) + ") unknown class: " + cls);
    }

    public static final CategoryToolTipGeneratorConverter    DEFAULT=new CategoryToolTipGeneratorConverter();
}
