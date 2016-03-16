/*
 *
 */
package net.community.chest.text;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

import net.community.chest.util.locale.LocaleUtils;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> The reflected {@link SimpleDateFormat} instance
 * @author Lyor G.
 * @since Jan 12, 2009 3:49:53 PM
 */
public class SimpleDateFormatReflectiveProxy<F extends SimpleDateFormat> extends DateFormatReflectiveProxy<F> {
    protected SimpleDateFormatReflectiveProxy (Class<F> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public SimpleDateFormatReflectiveProxy (Class<F> objClass)
    {
        this(objClass, false);
    }
    // special attribute used to initialize with Locale
    public static final String LOCALE_VIRTATTR=Locale.class.getSimpleName().toLowerCase();
    /*
     * @see net.community.chest.text.FormatReflectiveProxy#handleUnknownAttribute(java.text.Format, java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    protected F handleUnknownAttribute (F src, String name, String value, Map<String,? extends Method> accsMap) throws Exception
    {
        if (LOCALE_VIRTATTR.equalsIgnoreCase(name))
            return src;

        return super.handleUnknownAttribute(src, name, value, accsMap);
    }

    public static final SimpleDateFormatReflectiveProxy<SimpleDateFormat>    SMPLFMT=
        new SimpleDateFormatReflectiveProxy<SimpleDateFormat>(SimpleDateFormat.class, true) {
            /*
             * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
             */
            @Override
            public SimpleDateFormat createInstance (Element elem) throws Exception
            {
                final String    fmt=elem.getAttribute(FORMAT_VIRTATTR);
                if ((fmt != null) && (fmt.length() > 0))
                {
                    // check if also have the Locale attribute
                    final String    lcl=elem.getAttribute(LOCALE_VIRTATTR);
                    if ((null == lcl) || (lcl.length() <= 0))
                        return new SimpleDateFormat(fmt);

                    final Locale    l=LocaleUtils.getFormattingLocale(lcl);
                    return new SimpleDateFormat(fmt, l);
                }

                return super.createInstance(elem);
            }
        };
}
