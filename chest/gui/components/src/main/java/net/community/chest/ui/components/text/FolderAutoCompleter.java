/*
 *
 */
package net.community.chest.ui.components.text;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.text.JTextComponent;

import net.community.chest.io.filter.AbstractEmbeddedFilenameFilter;
import net.community.chest.io.filter.FolderOnlyFilesFilter;

/**
 * <P>Copyright as per GPLv2</P>
 * Provides a {@link FileAutoCompleter} implementation that provides only folder candidates
 * @param <C> Type of {@link JTextComponent} being used for auto-completion
 * @author Lyor G.
 * @since Feb 27, 2011 1:51:11 PM
 */
public class FolderAutoCompleter<C extends JTextComponent> extends FileAutoCompleter<C> {
    public FolderAutoCompleter (C comp, FilenameFilter filter) throws IllegalArgumentException
    {
        super(comp, filter);
    }

    public FolderAutoCompleter (C comp) throws IllegalArgumentException
    {
        this(comp, null);
    }
    /*
     * @see net.community.chest.ui.components.text.FileAutoCompleter#resolveFilter(java.io.File, java.lang.String)
     */
    @Override
    protected FilenameFilter resolveFilter (File dir, String prefix)
    {
        final FilenameFilter    orgFilter=super.resolveFilter(dir, prefix);
        if (orgFilter == null)
            return new FolderOnlyFilesFilter();

        return new AbstractEmbeddedFilenameFilter(orgFilter) {
                /*
                 * @see net.community.chest.io.filter.AbstractEmbeddedFilenameFilter#accept(java.io.File, java.lang.String)
                 */
                @Override
                public boolean accept (File parent, String name)
                {
                    if (!super.accept(parent, name))
                        return false;

                    if ((parent != null) && (name != null) && (name.length() > 0))
                    {
                        final File    f=new File(parent, name);
                        if (f.exists() && f.isDirectory())
                            return true;
                    }

                    return false;
                }
            };
    }

}
