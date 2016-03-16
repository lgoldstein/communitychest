/**
 *
 */
package net.community.apps.eclipse.cparrange;

import java.net.URL;
import java.util.Comparator;

import net.community.chest.awt.image.AbstractImageReader;
import net.community.chest.eclipse.classpath.ClasspathFileTransformer;
import net.community.chest.io.FileUtil;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 12, 2008 10:29:28 AM
 */
public class Arranger extends ClasspathFileTransformer {
    public Arranger ()
    {
        super();
    }
    /*
     * @see net.community.chest.eclipse.classpath.ClasspathFileTransformer#getClasspathEntryComparator()
     */
    @Override
    public Comparator<? super Element> getClasspathEntryComparator ()
    {
        return EntriesComparator.DEFAULT;
    }

    public final URL getDefaultIcon ()
    {
        final Class<?>    c=getClass();
        final String    cn=(null == c) ? null : c.getSimpleName();
        if ((null == cn) || (cn.length() <= 0))
            return null;

        final String    fn=FileUtil.adjustFileName(cn, AbstractImageReader.PNG_SUFFIX);
        return c.getResource(fn);
    }

    public static final Arranger    ARRANGER=new Arranger();
}
