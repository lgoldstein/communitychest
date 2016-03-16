package net.community.chest.eclipse.classpath;

import java.util.Map;

import net.community.chest.util.map.MapEntryImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Various definitions that have to do with the <I>.classpath</I> file</P>
 *
 * @author Lyor G.
 * @since Nov 22, 2007 8:16:13 AM
 */
public final class ClasspathUtils {
    private ClasspathUtils ()
    {
        // no instance
    }
    /**
     * Root XML element name in the file
     */
    public static final String    CLASSPATH_ELEM_NAME="classpath";
    /**
     * CLASSPATH entry XML element name
     */
    public static final String    CLASSPATHENTRY_ELEM_NAME="classpathentry";
    // some related attributes
    public static final String    CLASSPATHENTRY_KIND_ATTR="kind",
                                        SRC_ENTRY_KIND="src",
                                        OUTPUT_ENTRY_KIND="output",
                                        LIB_ENTRY_KIND="lib",
                                        VAR_ENTRY_KIND="var",
                                        CON_ENTRY_KIND="con",
                                    CLASSPATHENTRY_PATH_ATTR="path",
                                    CLASSPATHENTRY_COMBINE_ACC_RULES_ATTR="combineaccessrules",
                                    CLASSPATHENTRY_SOURCEPATH_ATTR="sourcepath";
    public static final String    CLASSPATHENTRY_ATTRS_ELEM="attributes",
                                    ATTR_TAG="attribute",
                                        ATTR_NAME_ATTR="name",
                                        ATTR_VALUE_ATTR="value";
    public static final Element createSourceEntryElement (final Document doc, final String path)
    {
        return createClasspathEntry(doc, SRC_ENTRY_KIND, path);
    }

    public static final Element createContainerEntry (final Document doc, final String path)
    {
        return createClasspathEntry(doc, CON_ENTRY_KIND, path);
    }

    public static final Element createClasspathEntry (final Document doc, final String kind, final String path)
    {
        final Element    entry=doc.createElement(CLASSPATHENTRY_ELEM_NAME);
        entry.setAttribute(CLASSPATHENTRY_KIND_ATTR, kind);
        entry.setAttribute(CLASSPATHENTRY_PATH_ATTR, path);
        return entry;
    }

    public static final Map.Entry<String,String> getContainerEntryInfo (final Element elem)
    {
        if (elem == null)
            return null;

        final String    kind=elem.getAttribute(CLASSPATHENTRY_KIND_ATTR),
                        path=elem.getAttribute(CLASSPATHENTRY_PATH_ATTR);
        if (((kind != null) && (kind.length() > 0))
         || ((path != null) && (path.length() > 0)))
            return new MapEntryImpl<String,String>(kind, path);
        else
            return null;
    }

    public static final Element addContainerEntry (final Document doc, final Element parent, final String path)
    {
        final Element    elem=createContainerEntry(doc, path);
        parent.appendChild(elem);
        return elem;
    }

    // returns the attribute element
    public static final Element addWebProjectDependency (final Document doc, final Element parent)
    {
        final Element    attrElem=createEntryAttribute(doc, "org.eclipse.jst.component.dependency", "/WEB-INF/lib");
        final Element    attrsList=doc.createElement(CLASSPATHENTRY_ATTRS_ELEM);
        attrsList.appendChild(attrElem);
        parent.appendChild(attrsList);
        return attrElem;
    }

    public static final Element addSourceEntryElement (final Document doc, final Element parent, final String path)
    {
        final Element    elem=createSourceEntryElement(doc, path);
        parent.appendChild(elem);
        return elem;
    }

    public static final Element createEntryAttribute (final Document doc, final String name, final String value)
    {
        final Element    attrElem=doc.createElement(ATTR_TAG);
        attrElem.setAttribute(ATTR_NAME_ATTR, name);
        attrElem.setAttribute(ATTR_VALUE_ATTR, value);
        return attrElem;
    }
}
