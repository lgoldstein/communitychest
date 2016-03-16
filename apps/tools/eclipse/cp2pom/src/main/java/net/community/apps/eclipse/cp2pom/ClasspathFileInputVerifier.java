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
 * @since Jul 30, 2009 8:40:11 AM
 */
public class ClasspathFileInputVerifier extends FileInputVerifier {
    public ClasspathFileInputVerifier ()
    {
        super();
    }
    /*
     * @see net.community.chest.ui.components.input.text.FileInputVerifier#verifyFile(java.io.File)
     */
    @Override
    public boolean verifyFile (File f) throws RuntimeException
    {
        return (f != null) && f.exists() && f.isFile();
    }
}
