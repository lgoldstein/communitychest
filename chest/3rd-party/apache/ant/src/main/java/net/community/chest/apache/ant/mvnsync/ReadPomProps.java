/*
 *
 */
package net.community.chest.apache.ant.mvnsync;

import java.io.File;
import java.util.Collection;
import java.util.Map;

import net.community.chest.apache.ant.helpers.AbstractFilesInputTask;
import net.community.chest.apache.maven.helpers.BuildProject;
import net.community.chest.dom.DOMUtils;
import net.community.chest.resources.AbstractPropertiesResolver;
import net.community.chest.resources.PropertyAccessor;
import net.community.chest.resources.TreeMapPropertiesAccessor;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 9, 2009 10:16:56 AM
 */
public class ReadPomProps extends AbstractFilesInputTask<Map<String,String>> {
    public ReadPomProps ()
    {
        super();
    }
    /**
     * Prefix to be appended to the defined properties - ignored if null/empty
     */
    private String    _prefix;
    public String getPrefix ()
    {
        return _prefix;
    }

    public void setPrefix (String prefix)
    {
        _prefix = prefix;
    }

    protected String getEffectivePropName (final String n)
    {
        final String    p=getPrefix();
        final int        nLen=(null == n) ? 0 : n.length(),
                        pLen=(null == p) ? 0 : p.length();
        if ((nLen <= 0) || (pLen <= 0))
            return n;

        return new StringBuilder(nLen + pLen)
                    .append(p)
                    .append(n)
                .toString()
                ;
    }
    /*
     * @see net.community.chest.apache.ant.helpers.AbstractFilesInputTask#processSingleFile(java.io.File)
     */
    @Override
    protected Map<String,String> processSingleFile (final File inFile) throws BuildException
    {
        if (isVerboseMode())
            log("Processing " + inFile.getAbsolutePath(), getVerbosity());

        try
        {
            final Document    doc=DOMUtils.loadDocument(inFile);
            final Element    root=(null == doc) ? null : doc.getDocumentElement();
            final String    tagName=(null == root) ? null : root.getTagName();
            if (!BuildProject.PROJECT_ELEMENT_NAME.equals(tagName))
                throw new DOMException(DOMException.NAMESPACE_ERR, "Unexpected root element name: " + tagName);

            final Collection<? extends Element>    el=
                DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE);
            for (final Element elem : el)
            {
                final String    eName=(null == elem) ? null : elem.getTagName();
                if (!BuildProject.PROPERTIES_ELEMENT_NAME.equals(eName))
                    continue;

                final TreeMapPropertiesAccessor<String,String>    r=
                    new TreeMapPropertiesAccessor<String,String>(String.CASE_INSENSITIVE_ORDER);
                BuildProject.updateProperties(r, elem, false);

                final Collection<? extends Map.Entry<String,String>>    pl=
                    ((null == r) || (r.size() <= 0)) ? null : r.entrySet();
                if ((pl != null) && (pl.size() > 0))
                {
                    final Project    p=getProject();
                    for (final Map.Entry<String,String>    pe : pl)
                    {
                        final String    n=
                            getEffectivePropName((null == pe) ? null : pe.getKey()),
                                        v=
                            AbstractPropertiesResolver.format((null == pe) ? null : pe.getValue(), (PropertyAccessor<String,String>) r);
                        if (isVerboseMode())
                            log("\t<property name=\"" + n + "\" value=\"" + v + "\"/>");
                        p.setProperty(n, v);
                    }

                    return r;
                }

                break;    // don't continue after the properties element
            }

            return null;
        }
        catch(Exception e)
        {
            throw new BuildException("Failed to parse file=" + inFile + ": " + e.getMessage(), getLocation());
        }
    }
}
