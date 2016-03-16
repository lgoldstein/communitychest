/*
 *
 */
package net.community.chest.jfree.jfreechart.plot.category;

import java.util.List;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.jfree.jfreechart.chart.renderer.BaseGeneratorConverter;
import net.community.chest.lang.StringUtil;

import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2009 12:53:04 PM
 */
public class StandardCategoryURLGeneratorConverter
        extends BaseGeneratorConverter<StandardCategoryURLGenerator>
        implements ValueStringInstantiator<StandardCategoryURLGenerator> {
    public StandardCategoryURLGeneratorConverter ()
    {
        super(StandardCategoryURLGenerator.class);
    }
    /*
     * @see net.community.chest.dom.transform.XmlValueInstantiator#fromXml(org.w3c.dom.Element)
     */
    @Override
    public StandardCategoryURLGenerator fromXml (Element elem) throws Exception
    {
        final String    prfx=elem.getAttribute("prefix"),
                        sn=elem.getAttribute("series"),
                        cn=elem.getAttribute("category");
        if ((null == prfx) || (prfx.length() <= 0))
        {
            if (((sn != null) && (sn.length() > 0))
             || ((cn != null) && (cn.length() > 0)))
                throw new IllegalArgumentException("fromXml(" + DOMUtils.toString(elem) + ") superfluous arguments");
            return new StandardCategoryURLGenerator();
        }

        if (((sn == null) || (sn.length() <= 0))
         && ((cn == null) ||(cn.length() <= 0)))
            return new StandardCategoryURLGenerator(prfx);

        if ((sn != null) && (sn.length() > 0)
         && (cn != null) && (cn.length() > 0))
            return new StandardCategoryURLGenerator(prfx, sn, cn);

        throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") missing data");
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (StandardCategoryURLGenerator inst) throws Exception
    {
        throw new UnsupportedOperationException("convertInstance(" + inst + ") N/A");
    }

    public static final StandardCategoryURLGenerator fromString (final String s)
        throws IllegalArgumentException
    {
        final List<String>    args=StringUtil.splitString(s, ',');
        final int            numArgs=(null == args) ? 0 : Math.max(0, args.size());
        switch(numArgs)
        {
            case 0    :
                return new StandardCategoryURLGenerator();

            case 1     :
                return new StandardCategoryURLGenerator(args.get(0));

            case 3    :
                return new StandardCategoryURLGenerator(args.get(0), args.get(1), args.get(2));

            default    :
                throw new IllegalArgumentException("fromString(" + s + ") unexpected number of arguments");
        }
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public StandardCategoryURLGenerator newInstance (String s) throws Exception
    {
        return fromString(StringUtil.getCleanStringValue(s));
    }

    public static final StandardCategoryURLGeneratorConverter    DEFAULT=new StandardCategoryURLGeneratorConverter();
}
