package net.community.chest.test.gui;

import java.io.BufferedReader;
import java.io.PrintStream;
import net.community.chest.Triplet;
import net.community.chest.awt.dom.converter.InsetsValueInstantiator;
import net.community.chest.awt.layout.gridbag.ExtendedGridBagConstraints;
import net.community.chest.awt.layout.gridbag.GridBagGridSizingType;
import net.community.chest.awt.layout.gridbag.GridBagXYValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.test.TestBase;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 7, 2007 2:09:18 PM
 */
public class ResourcesTester extends TestBase {
    private static final int testElementDataParsing (final PrintStream out, final BufferedReader in, final String elemData, final Element elem)
    {
        for ( ; ; )
        {
            out.println(elemData);
            try
            {
                final ExtendedGridBagConstraints    gbc=new ExtendedGridBagConstraints(elem);
                out.println("\tgridx=" + (gbc.isAbsoluteGridX() ? String.valueOf(gbc.gridx) : GridBagXYValueStringInstantiator.RELATIVE_VALUE));
                out.println("\tgridy=" + (gbc.isAbsoluteGridY() ? String.valueOf(gbc.gridy) : GridBagXYValueStringInstantiator.RELATIVE_VALUE));

                final GridBagGridSizingType    wt=gbc.getGridWithType(), ht=gbc.getGridHeightType();
                out.println("\tgridwidth=" + ((null == wt) ? String.valueOf(gbc.gridwidth) : wt.name()));
                out.println("\tgridheight=" + ((null == ht) ? String.valueOf(gbc.gridheight) : ht.name()));

                out.println("\tfill=" + gbc.getFillType());

                out.println("\tipadx=" + gbc.ipadx);
                out.println("\tipady=" + gbc.ipady);

                out.println("\tinsets=" + InsetsValueInstantiator.toString(gbc.insets));
                out.println("\tanchor=" + gbc.getAnchorType());

                out.println("\tweightx=" + gbc.weightx);
                out.println("\tweighty=" + gbc.weighty);
            }
            catch(Exception e)
            {
                System.err.println("testElementDataParsing(" + elemData + ") " + e.getClass().getName() + ": " + e.getMessage());
            }

            final String    ans=getval(out, in, "again (y)/[n]");
            if ((null == ans) || (ans.length() <= 0) || (Character.toLowerCase(ans.charAt(0)) != 'y'))
                break;
        }

        return 0;
    }
    // each argument is an XML element string to be parsed
    private static final int testGridBagConstraintsParsing (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    elemData=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "XML element data (or Quit)");
            if ((null == elemData) || (elemData.length() <= 0))
                continue;
            if (isQuit(elemData)) break;

            try
            {
                final Triplet<? extends Element,?,?>    pe=
                    DOMUtils.parseElementString(elemData);
                testElementDataParsing(out, in, elemData, (null == pe) ? null : pe.getV1());
            }
            catch(Exception e)
            {
                System.err.println("testGridBagConstraintsParsing(" + elemData + ") " + e.getClass().getName() + ": " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
        final int                nErr=testGridBagConstraintsParsing(System.out, in, args);
        if (nErr != 0)
            System.err.println("test failed (err=" + nErr + ")");
        else
            System.out.println("OK");
    }
}
