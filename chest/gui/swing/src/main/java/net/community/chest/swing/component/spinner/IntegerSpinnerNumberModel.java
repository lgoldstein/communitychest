/*
 *
 */
package net.community.chest.swing.component.spinner;

import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 11, 2009 9:42:30 AM
 *
 */
public class IntegerSpinnerNumberModel extends BaseSpinnerNumberModel {
    /**
     *
     */
    private static final long serialVersionUID = 3896174167302631947L;

    public IntegerSpinnerNumberModel (Integer value, Integer minimum, Integer maximum, Integer stepSize)
    {
        super(value, minimum, maximum, stepSize);
    }

    public IntegerSpinnerNumberModel (int value, int minimum, int maximum, int stepSize)
    {
        this(Integer.valueOf(value), Integer.valueOf(minimum), Integer.valueOf(maximum), Integer.valueOf(stepSize));
    }

    public IntegerSpinnerNumberModel ()
    {
        this(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
    }

    public IntegerSpinnerNumberModel (Element elem) throws Exception
    {
        super(elem);
    }
}
