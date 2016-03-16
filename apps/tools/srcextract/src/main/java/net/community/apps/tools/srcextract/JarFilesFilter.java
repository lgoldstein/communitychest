package net.community.apps.tools.srcextract;

import net.community.chest.ui.helpers.filechooser.SuffixesFileFilter;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Nov 25, 2007 12:37:18 PM
 */
public class JarFilesFilter extends SuffixesFileFilter {
    public JarFilesFilter ()
    {
        super("JAR files", false);
        addSuffix("jar");
    }

    public static final JarFilesFilter    DEFAULT=new JarFilesFilter();
}
