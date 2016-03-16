/*
 *
 */
package net.community.chest.ui.components.input.text.file;

import java.io.File;

import net.community.chest.ui.helpers.input.TextInputVerifier;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Verifies a {@link File} instance input</P>
 *
 * @author Lyor G.
 * @since Mar 31, 2009 1:57:54 PM
 */
public abstract class FileInputVerifier extends TextInputVerifier {
    protected FileInputVerifier ()
    {
        super();
    }
    /**
     * Called by {@link #verifyText(String)} - usually after making sure the
     * provided {@link String} is not null/empty
     * @param f The {@link File} instance to check
     * @return TRUE if it is OK to use this file
     * @throws RuntimeException If some internal error - which is equivalent
     * to <code>false</code> being returned by the {@link #verifyText(String)}
     */
    public abstract boolean verifyFile (File f) throws RuntimeException;
    /*
     * @see net.community.chest.ui.helpers.input.TextInputVerifier#verifyText(java.lang.String)
     */
    @Override
    public boolean verifyText (String text)
    {
        if (!super.verifyText(text))
            return false;

        try
        {
            return verifyFile(new File(text));
        }
        catch(RuntimeException e)
        {
            return false;
        }
    }
}
