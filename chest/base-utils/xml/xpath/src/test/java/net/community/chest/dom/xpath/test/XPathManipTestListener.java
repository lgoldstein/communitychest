/*
 *
 */
package net.community.chest.dom.xpath.test;

import java.io.PrintStream;
import java.util.Collection;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.xpath.manip.XPathManipulationData;
import net.community.chest.dom.xpath.manip.XPathManipulationListener;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XPathManipTestListener implements XPathManipulationListener {
    private final PrintStream    _out;
    public final PrintStream getPrintStream ()
    {
        return _out;
    }

    public XPathManipTestListener (final PrintStream out)
    {
        if (null == (_out=out))
            throw new IllegalArgumentException("No print stream provided");
    }

    public void printResult (final PrintStream out, final String indent, final Object result)
    {
        if ((null == out) || (null == result))
            return;

        if (result instanceof Element)
            out.println(indent + "\t==> " + DOMUtils.toString((Element) result));
        else if (result instanceof Attr)
            out.println(indent + "\t==> " + DOMUtils.toString((Attr) result));
        else if (result instanceof Collection<?>)
        {
            final Collection<?>    cl=(Collection<?>) result;
            if (cl.size() <= 0)
                return;

            out.println(indent + "\t==> " + Collection.class.getName());

            final String    newIndent=indent + "\t";
            for (final Object o : cl)
                printResult(out, newIndent, o);
        }
        else
            out.println(indent + "\t==>(" + result.getClass().getName() + ") " + result);
    }
    /*
     * @see net.community.chest.dom.xpath.manip.XPathManipulationListener#handleManipulationExecutionResult(net.community.chest.dom.xpath.manip.XPathManipulationData, org.w3c.dom.Document, org.w3c.dom.Element, java.lang.Object)
     */
    @Override
    public void handleManipulationExecutionResult (XPathManipulationData manip, Document doc, Element elem, Object result)
    {
        final PrintStream    out=getPrintStream();
        if (null == out)
            return;

        out.println("\tEXECUTE " + manip + " => " + DOMUtils.toString(elem));
        printResult(out, "\t", result);
    }

}
