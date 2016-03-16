/*
 *
 */
package net.community.chest.ui.helpers.input;

import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 12, 2009 2:04:58 PM
 */
public final class ValidatorUtils {
    private ValidatorUtils ()
    {
        // no instance
    }

    public static final Border resolveValidatorBorder (
            final JComponent comp, final boolean validData, final Border okBorder, final Border errBorder)
    {
        if (null == comp)
            return null;

        final Border[]    ba={ okBorder, errBorder }, ca;
        final Border    curBorder=comp.getBorder(),
                        newBorder=validData ? okBorder : errBorder;
        if (curBorder != null)
        {
            // if compound border check if one of the contained ones
            if (curBorder instanceof CompoundBorder)
            {
                final CompoundBorder    cb=(CompoundBorder) curBorder;
                ca = new Border[] { cb.getInsideBorder(), cb.getOutsideBorder() };
            }
            else
                ca = new Border[] { curBorder };
        }
        else
            ca = null;

        int            oIndex=(-1);
        final int    numBorders=(null == ca) ? 0 : ca.length;
        for (int bIndex=0; (bIndex < numBorders) && (oIndex < 0); bIndex++)
        {
            final Border    cb=ca[bIndex];
            // check if any of the text borders are included
            for (final Border    ob : ba)
            {
                if (null == ob)
                    continue;

                if ((ob == cb) || ob.equals(cb))
                    oIndex = bIndex;
            }
        }

        // check if any of the generated borders already appears
        Border    orgBorder=null;
        if ((oIndex >= 0) && (oIndex < numBorders))
        {
            if (oIndex > 1)
                orgBorder = ca[oIndex - 1];
            else if (oIndex < (numBorders - 1))
                orgBorder = ca[oIndex + 1];
        }

        if (orgBorder != null)
        {
            if (newBorder == null)
                return orgBorder;
            else
                return BorderFactory.createCompoundBorder(orgBorder, newBorder);
        }
        else
            return newBorder;
    }

    public static final <N extends Number> InputVerifier createNumberVerifier (Class<N> numClass, NumberFormat fmt)
    {
        if (null == fmt)
            return new TextNumberInputVerifier<N>(numClass);
        else
            return new FormattedNumberInputVerifier(fmt);
    }
}
