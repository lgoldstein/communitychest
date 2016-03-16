/*
 *
 */
package net.community.chest.ui.helpers.input;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 12, 2009 2:08:43 PM
 */
public class FormattedNumberInputVerifier extends TextInputVerifier {
    private NumberFormat    _fmt;
    public NumberFormat getNumberFormat ()
    {
        return _fmt;
    }

    public void setNumberFormat (NumberFormat fmt)
    {
        if (_fmt != fmt)
            _fmt = fmt;
    }

    public FormattedNumberInputVerifier (NumberFormat fmt)
    {
        _fmt = fmt;
    }

    public FormattedNumberInputVerifier ()
    {
        this(null);
    }
    /*
     * @see net.community.chest.ui.helpers.input.TextInputVerifier#verifyText(java.lang.String)
     */
    @Override
    public boolean verifyText (final String text)
    {
        if (!super.verifyText(text))
            return false;

        final NumberFormat    fmt=getNumberFormat();
        if (null == fmt)    // if no format then cannot declare the input as valid
            return false;

        try
        {
            final Number    n=fmt.parse(text);
            return (n != null);
        }
        catch (ParseException e)
        {
            return false;
        }
    }
}
