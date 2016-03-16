/*
 *
 */
package net.community.chest.jfree.jfreechart.plot.pie;

import java.lang.reflect.Constructor;
import java.util.Map;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.reflect.MethodsMap;

import org.jfree.chart.urls.StandardPieURLGenerator;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 26, 2009 3:16:45 PM
 */
public class StandardPieURLGeneratorConverter implements XmlValueInstantiator<StandardPieURLGenerator> {
    public StandardPieURLGeneratorConverter ()
    {
        super();
    }

    protected String getString (final Element elem, final String attrName)
    {
        return     ((null == elem) || (null == attrName) || (attrName.length() <= 0)) ? null : elem.getAttribute(attrName);
    }

    private static Map<Integer,Constructor<?>>    _ctorsMap;
    private static final synchronized Map<Integer,Constructor<?>> getConstructorsMap ()
    {
        if (null == _ctorsMap)
            _ctorsMap = MethodsMap.getConstructorsMapByNumArgs(StandardPieURLGeneratorConverter.class);
        return _ctorsMap;
    }

    private static final Constructor<?> getConstructor (final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        int            ctorArgs=0;
        for (int    aIndex=0; aIndex < numArgs; aIndex++)
        {
            if ((null == args) || (args.length <= 0))
                continue;

            ctorArgs = aIndex + 1;
        }

        final Map<Integer,? extends Constructor<?>>    cm=getConstructorsMap();
        if ((null == cm) || (cm.size() <= 0))
            return null;

        return cm.get(Integer.valueOf(ctorArgs));
    }

    public StandardPieURLGenerator fromValues (String ... vals) throws Exception
    {
        final Constructor<?>    ctor=getConstructor(vals);
        if (null == ctor)
            throw new NoSuchMethodException("fromValues(" + ((null == vals) ? 0 : vals.length) + ") no matching constructor found");

        return (StandardPieURLGenerator) ctor.newInstance((Object[]) vals);
    }
    /*
     * @see net.community.chest.dom.transform.XmlValueInstantiator#fromXml(org.w3c.dom.Element)
     */
    @Override
    public StandardPieURLGenerator fromXml (Element elem) throws Exception
    {
        final String[]    vals={
                getString(elem, "prefix"),
                getString(elem, "category"),
                getString(elem, "index")
            };
        final Constructor<?>    ctor=getConstructor(vals);
        if (null == ctor)
            throw new NoSuchMethodException("fromXml(" + DOMUtils.toString(elem) + ") no matching constructor found");

        return (StandardPieURLGenerator) ctor.newInstance((Object[]) vals);
    }

    public static final StandardPieURLGeneratorConverter    DEFAULT=new StandardPieURLGeneratorConverter();
}
