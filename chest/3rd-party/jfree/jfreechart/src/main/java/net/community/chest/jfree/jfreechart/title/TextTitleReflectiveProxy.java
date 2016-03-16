/*
 *
 */
package net.community.chest.jfree.jfreechart.title;

import org.jfree.chart.title.DateTitle;
import org.jfree.chart.title.ShortTextTitle;
import org.jfree.chart.title.TextTitle;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <T> The reflected {@link TextTitle} type
 * @author Lyor G.
 * @since Jan 27, 2009 3:45:20 PM
 */
public class TextTitleReflectiveProxy<T extends TextTitle> extends TitleReflectiveProxy<T> {
    protected TextTitleReflectiveProxy (Class<T> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public TextTitleReflectiveProxy (Class<T> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final TextTitleReflectiveProxy<TextTitle>    TXTTTL=
        new TextTitleReflectiveProxy<TextTitle>(TextTitle.class, true) {
            /*
             * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
             */
            @Override
            public TextTitle createInstance (Element elem) throws Exception
            {
                final String    tc=elem.getAttribute(CLASS_ATTR);
                if (ShortTextTitle.class.getSimpleName().equalsIgnoreCase(tc))
                {
                    final String    it=elem.getAttribute("text");
                    if ((null == it) || (it.length() <= 0))
                        return new ShortTextTitle(tc);
                    else
                        return new ShortTextTitle(it);
                }

                return super.createInstance(elem);
            }
        };

        public static final TextTitleReflectiveProxy<? extends TextTitle> getTextTitleConverter (final Element elem)
        {
            if (null == elem)
                return null;

            final String    tc=elem.getAttribute(CLASS_ATTR);
            if (DateTitle.class.getSimpleName().equalsIgnoreCase(tc))
                return DateTitleReflectiveProxy.DATETTL;
            else
                return TXTTTL;
        }
}
