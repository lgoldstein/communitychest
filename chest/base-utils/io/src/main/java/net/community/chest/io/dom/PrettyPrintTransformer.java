/*
 *
 */
package net.community.chest.io.dom;

import java.io.IOException;
import java.io.Writer;

import javax.xml.transform.TransformerException;


import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Prints out the XML {@link Element}-s using indentation</P>
 *
 * @author Lyor G.
 * @since Aug 26, 2008 5:34:43 PM
 */
public class PrettyPrintTransformer extends AbstractIOTransformer {
    public PrettyPrintTransformer ()
    {
        super();
    }
    /*
     * @see net.community.chest.dom.transform.AbstractTransformer#transformRootElement(org.w3c.dom.Element, java.io.Writer)
     */
    @Override
    public Writer transformRootElement (Element root, Writer w) throws TransformerException, IOException
    {
        if (isTabsIndentation())
            return writeElementData(root, w, true, true, "");
        else    // TODO implement non-tabbed indentation
            throw new UnsupportedOperationException("transformRootElement() non-tabbed indent N/A");
    }

    public static final PrettyPrintTransformer    DEFAULT=new PrettyPrintTransformer();
}
