package net.community.chest.text;

import java.text.NumberFormat;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> Type of {@link NumberFormat} being reflected
 * @author Lyor G.
 * @since Jan 12, 2009 3:16:01 PM
 */
public abstract class NumberFormatReflectiveProxy<F extends NumberFormat> extends FormatReflectiveProxy<F> {
    protected NumberFormatReflectiveProxy (Class<F> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public static final NumberFormatReflectiveProxy<? extends NumberFormat> getNumberFormatConverter (Element elem)
    {
        final String    nfc=(null == elem) ? null : elem.getAttribute(CLASS_ATTR);
        if ("decimal".equalsIgnoreCase(nfc))
            return DecimalFormatReflectiveProxy.DECFMT;
        else if ("choice".equalsIgnoreCase(nfc))
            return ChoiceFormatReflectiveProxy.CHCFMT;
        else
            return null;
    }
}
