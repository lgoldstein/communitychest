/*
 *
 */
package net.community.chest.text;

import java.text.ChoiceFormat;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> The reflected {@link ChoiceFormat}
 * @author Lyor G.
 * @since Jan 12, 2009 3:27:13 PM
 */
public class ChoiceFormatReflectiveProxy<F extends ChoiceFormat> extends NumberFormatReflectiveProxy<F> {
    protected ChoiceFormatReflectiveProxy (Class<F> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public ChoiceFormatReflectiveProxy (Class<F> objClass)
    {
        this(objClass, false);
    }

    public static final ChoiceFormatReflectiveProxy<ChoiceFormat>    CHCFMT=
        new ChoiceFormatReflectiveProxy<ChoiceFormat>(ChoiceFormat.class, true) {
            /*
             * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
             */
            @Override
            public ChoiceFormat createInstance (Element elem) throws Exception
            {
                final String    fmt=elem.getAttribute(FORMAT_VIRTATTR);
                return new ChoiceFormat(fmt);
            }
        };
}
