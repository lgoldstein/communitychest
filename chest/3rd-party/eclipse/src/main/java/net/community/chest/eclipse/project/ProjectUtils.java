/*
 *
 */
package net.community.chest.eclipse.project;

import java.util.Collection;
import java.util.TreeSet;

import net.community.chest.dom.DOMUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Various utilities to handle the <code>.project</code> file
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 7, 2011 11:19:15 AM
 */
public final class ProjectUtils {
    private ProjectUtils ()
    {
        // no instance
    }

    public static final String    BUILD_SPEC_ELEM_NAME="buildSpec",
                                    BUILD_CMD_ELEM_NAME="buildCommand",
                                        BUILD_CMD_NAME_TAG="name",
                                        BUILD_CMD_ARGS_TAG="arguments";
    public static final Element createBuildCommand (final Document doc, final String name)
    {
        final Element    nameElem=doc.createElement(BUILD_CMD_NAME_TAG);
        nameElem.setTextContent(name);

        final Element    cmdElem=doc.createElement(BUILD_CMD_ELEM_NAME);
        cmdElem.appendChild(nameElem);
        cmdElem.appendChild(doc.createElement(BUILD_CMD_ARGS_TAG));
        return cmdElem;
    }

    public static final String    NATURES_ELEM_NAME="natures",
                                    NATURE_TAG="nature";
    public static final Element createNature (final Document doc, final String name)
    {
        final Element    natureElem=doc.createElement(NATURE_TAG);
        natureElem.setTextContent(name);
        return natureElem;
    }

    public static final Collection<String> getNatures (final Element root)
    {
        final NodeList        children=(root == null) ? null : root.getChildNodes();
        final int            numChildren=(children == null) ? 0 : children.getLength();
        Collection<String>    result=null;
        for (int    index=0; index < numChildren; index++)
        {
            final Node    n=children.item(index);
            if ((n == null) || (n.getNodeType() != Node.ELEMENT_NODE))
                continue;

            final Element    elem=(Element) n;
            final String    tagName=elem.getTagName();
            if (!NATURE_TAG.equalsIgnoreCase(tagName))
                continue;

            final String    tagValue=DOMUtils.getElementStringValue(elem);
            if ((tagValue == null) || (tagValue.length() <= 0))
                continue;

            if (result == null)
                result = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
            if (!result.add(tagValue))
                continue;    // debug breakpoint
        }

        return result;
    }
}
