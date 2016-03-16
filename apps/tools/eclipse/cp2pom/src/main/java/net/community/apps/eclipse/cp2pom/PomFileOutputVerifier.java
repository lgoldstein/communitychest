/*
 *
 */
package net.community.apps.eclipse.cp2pom;

import java.io.File;

import net.community.chest.ui.components.input.text.file.FileInputVerifier;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 30, 2009 9:00:55 AM
 */
public class PomFileOutputVerifier extends FileInputVerifier {
    public PomFileOutputVerifier ()
    {
        super();
    }

    /*
     * @see net.community.chest.ui.components.input.text.file.FileInputVerifier#verifyFile(java.io.File)
     */
    @Override
    public boolean verifyFile (File f) throws RuntimeException
    {
        if (null == f)
            return false;

        // if file exists make sure it is a file and not a directory
        if (f.exists())
            return f.isFile();

        // otherwise, make sure parent file is a directory
        final File    p=f.getParentFile();
        return (p != null) && p.exists() && p.isDirectory();
    }

}
