/*
 *
 */
package net.community.chest.ui.components.input.text.number;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 12, 2009 3:11:50 PM
 */
public class DoubleInputNumberField extends InputNumberField<Double> {
    /**
     *
     */
    private static final long serialVersionUID = -1683007667835849192L;
    public DoubleInputNumberField (Element elem, boolean autoLayout)
    {
        super(Double.class, elem, autoLayout);
    }

    public DoubleInputNumberField (boolean autoLayout)
    {
        this(null, autoLayout);
    }

    public DoubleInputNumberField ()
    {
        this(true);
    }
    /* Disallows also NaN and INFINITY
     * @see net.community.chest.ui.components.input.text.InputNumberField#isValidNumber(java.lang.Number)
     */
    @Override
    public boolean isValidNumber (Double n)
    {
        if (null == n)
            return false;

        final double    d=n.doubleValue();
        if (Double.isNaN(d) || Double.isInfinite(d))
            return false;

        return super.isValidNumber(n);
    }
}
