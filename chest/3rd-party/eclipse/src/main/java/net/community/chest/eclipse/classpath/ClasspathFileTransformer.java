package net.community.chest.eclipse.classpath;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import javax.xml.transform.TransformerException;

import net.community.chest.dom.DOMUtils;
import net.community.chest.eclipse.AbstractEclipseFileTransformer;
import net.community.chest.io.FileUtil;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Re-arranges the Eclipse classpath file by sorting its elements</P>
 * @author Lyor G.
 * @since Nov 22, 2007 10:11:13 AM
 */
public class ClasspathFileTransformer extends AbstractEclipseFileTransformer {
    public ClasspathFileTransformer ()
    {
        super();
    }

    public Writer writeClasspathEntry (Element elem, final Writer w) throws IOException
    {
        return writeElementData(elem, w, true, true, "\t");
    }
    // NOTE !!! does not generate a "closed" entry
    public Writer writeRootElement (Element root, final Writer w) throws TransformerException, IOException
    {
        if ((null == root) || (null == w))
            throw new TransformerException("transformRootElement() no " + Element.class.getSimpleName() + "/" + Writer.class.getSimpleName() + " instance(s)");

        if (isValidatingElementName())
        {
            final String    tagName=root.getTagName();
            if (!ClasspathUtils.CLASSPATH_ELEM_NAME.equals(tagName))
                throw new TransformerException("transformRootElement(" + tagName + ") bad " + Element.class.getSimpleName() + " name - expected " + ClasspathUtils.CLASSPATH_ELEM_NAME);
        }

        return writeElementData(root, w, false, false);
    }

    public Writer transformClasspathEntry (Element elem, final Writer w) throws TransformerException, IOException
    {
        if ((null == elem) || (null == w))
            throw new TransformerException("transformClasspathEntry() no " + Element.class.getSimpleName() + "/" + Writer.class.getSimpleName() + " instance(s)");

        if (isValidatingElementName())
        {
            final String    tagName=elem.getTagName();
            if (!ClasspathUtils.CLASSPATHENTRY_ELEM_NAME.equals(tagName))
                throw new TransformerException("transformClasspathEntry(" + tagName + ") bad " + Element.class.getSimpleName() + " name - expected " + ClasspathUtils.CLASSPATHENTRY_ELEM_NAME);
        }

        return writeClasspathEntry(elem, w);
    }

    public Comparator<? super Element> getClasspathEntryComparator ()
    {
        return ClasspathEntryComparator.CASE_SENSITIVE_CPENTRY;
    }

    public Writer transformClasspathEntries (NodeList nodes, Writer org) throws TransformerException, IOException
    {
        final int    numNodes=(null == nodes) /* OK */ ? 0 : nodes.getLength();
        if (numNodes <= 0)
            return org;

        Writer        w=org;
        if (isSortedOutputEntries())
        {
            final Collection<? extends Element>    elems=
                            DOMUtils.extractAllNodes(Element.class, nodes, Node.ELEMENT_NODE);
            final int                            numElems=
                            (null == elems) ? 0 : elems.size();
            final Element[]                        entries=
                            (numElems <= 0) ? null : elems.toArray(new Element[numElems]);
            final int                            numEntries=
                            (null == entries) ? 0 : entries.length;
            if (numEntries > 1)
                Arrays.sort(entries, getClasspathEntryComparator());

            if (numEntries > 0)
            {
                for (final Element entry : entries)
                {
                    if (null == entry)    // should not happen
                        continue;

                    w = transformClasspathEntry(entry, w);
                }
            }
        }
        else
        {
            for (int    nIndex=0; nIndex < numNodes; nIndex++)
            {
                final Node    n=nodes.item(nIndex);
                if ((null == n) || (n.getNodeType() != Node.ELEMENT_NODE))
                    continue;

                w = transformClasspathEntry((Element) n, w);
            }
        }

        return w;
    }
    /*
     * @see net.community.chest.dom.transform.AbstractTransformer#transformRootElement(org.w3c.dom.Element, java.io.Writer)
     */
    @Override
    public Writer transformRootElement (Element root, Writer org) throws TransformerException, IOException
    {
        // write root entry
        Writer    w=writeRootElement(root, org);

        w = transformClasspathEntries(root.getChildNodes(), w);

        // write closing entry
        final String    tagName=root.getTagName();
        w = FileUtil.writeln(w, "</" + tagName + ">");
        return w;
    }

    public static final ClasspathFileTransformer    DEFAULT=new ClasspathFileTransformer();
}
