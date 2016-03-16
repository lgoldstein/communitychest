/*
 *
 */
package net.community.apps.tools.svn.wc;

import java.io.File;

import net.community.chest.svnkit.SVNFoldersFilter;
import net.community.chest.ui.components.input.text.file.FileInputVerifier;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 12:39:39 PM
 */
public class WCLocationFileInputVerifier extends FileInputVerifier {
    public WCLocationFileInputVerifier ()
    {
        super();
    }
    /*
     * @see net.community.chest.ui.components.input.text.file.FileInputVerifier#verifyFile(java.io.File)
     */
    @Override
    public boolean verifyFile (File f) throws RuntimeException
    {
        if ((null == f) || (!f.isDirectory()))
            return false;

        if (SVNFoldersFilter.isSVNParentFolder(f))
            return false;

        return true;
    }

    public static final WCLocationFileInputVerifier    DEFAULT=
            new WCLocationFileInputVerifier();
}
