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

import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.IntervalCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2009 12:54:20 PM
 */
public class CategoryItemLabelGeneratorConverter extends BaseGeneratorConverter<CategoryItemLabelGenerator> {
    public CategoryItemLabelGeneratorConverter ()
    {
        super(CategoryItemLabelGenerator.class);
    }

    public StandardCategoryItemLabelGenerator createStandardCategoryItemLabelGenerator (Element elem) throws Exception
    {
        final String        lbl=getLabelFormat(elem);
        final NumberFormat    nf=getNumberFormat(elem),
                            pf=getPercentFormat(elem);
        final DateFormat    df=getDateFormat(elem);
        if ((null == lbl) || (lbl.length() <= 0))
        {
            if ((nf != null) || (pf != null) || (df != null))
                throw new IllegalArgumentException("createStandardCategoryItemLabelGenerator(" + DOMUtils.toString(elem) + ") superfluous arguments to empty constructor");

            return new StandardCategoryItemLabelGenerator();
        }

        if (((nf != null) || (pf != null)) && (df != null))
            throw new IllegalStateException("createStandardCategoryItemLabelGenerator(" + DOMUtils.toString(elem) + ") ambiguous formatting arguments");
        if ((nf == null) && (df == null))
            throw new IllegalStateException("createStandardCategoryItemLabelGenerator(" + DOMUtils.toString(elem) + ") no formatting arguments");

        if (df != null)
            return new StandardCategoryItemLabelGenerator(lbl, df);

        if (null == pf)
            return new StandardCategoryItemLabelGenerator(lbl, nf);

        return new StandardCategoryItemLabelGenerator(lbl, nf, pf);
    }

    public IntervalCategoryItemLabelGenerator createIntervalCategoryItemLabelGenerator (Element elem) throws Exception
    {
        final String        lbl=getLabelFormat(elem);
        final NumberFormat    nf=getNumberFormat(elem);
        final DateFormat    df=getDateFormat(elem);
        if ((null == lbl) || (lbl.length() <= 0))
        {
            if ((nf != null) || (df != null))
                throw new IllegalArgumentException("createIntervalCategoryItemLabelGenerator(" + DOMUtils.toString(elem) + ") superfluous arguments to empty constructor");

            return new IntervalCategoryItemLabelGenerator();
        }

        if ((nf != null) && (df != null))
            throw new IllegalStateException("createIntervalCategoryItemLabelGenerator(" + DOMUtils.toString(elem) + ") ambiguous formatting arguments");
        if ((nf == null) && (df == null))
            throw new IllegalStateException("createIntervalCategoryItemLabelGenerator(" + DOMUtils.toString(elem) + ") no formatting arguments");

        if (df != null)
            return new IntervalCategoryItemLabelGenerator(lbl, df);
        else
            return new IntervalCategoryItemLabelGenerator(lbl, nf);
    }
    /*
     * @see net.community.chest.dom.transform.XmlValueInstantiator#fromXml(org.w3c.dom.Element)
     */
    @Override
    public CategoryItemLabelGenerator fromXml (Element elem) throws Exception
    {
        final String    cls=elem.getAttribute(ReflectiveAttributesProxy.CLASS_ATTR);
        if ((null == cls) || (cls.length() <= 0) || "standard".equalsIgnoreCase(cls))
            return createStandardCategoryItemLabelGenerator(elem);
        else if ("interval".equalsIgnoreCase(cls))
            return createIntervalCategoryItemLabelGenerator(elem);
        else
            throw new NoSuchElementException("fromXml(" + DOMUtils.toString(elem) + ") unknown class: " + cls);
    }

    public static final CategoryItemLabelGeneratorConverter    DEAULT=new CategoryItemLabelGeneratorConverter();
}
