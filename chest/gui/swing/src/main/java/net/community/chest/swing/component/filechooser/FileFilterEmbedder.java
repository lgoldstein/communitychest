/*
 *
 */
package net.community.chest.swing.component.filechooser;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import net.community.chest.lang.TypedValuesContainer;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> The type of embedded {@link FileFilter}
 * @author Lyor G.
 * @since Jan 28, 2009 2:06:56 PM
 */
public class FileFilterEmbedder<F extends FileFilter> extends FileFilter implements TypedValuesContainer<F> {
    private final Class<F>    _fc;
    @Override
    public final Class<F> getValuesClass ()
    {
        return _fc;
    }

    private F    _f;
    public F getEmbeddedFilter ()
    {
        return _f;
    }

    public void setEmbeddedFilter (F f)
    {
        _f = f;
    }

    public FileFilterEmbedder (Class<F> fc, F f)
    {
        if (null == (_fc=fc))
            throw new IllegalArgumentException("No filter class specified");
        _f = f;
    }

    public FileFilterEmbedder (Class<F> fc)
    {
        this(fc, null);
    }

    @SuppressWarnings("unchecked")
    public FileFilterEmbedder (F f)
    {
        this((null == f) ? null : (Class<F>) f.getClass(), f);
    }

    /*
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    @Override
    public boolean accept (File f)
    {
        final FileFilter    ff=getEmbeddedFilter();
        return (ff != null) && ff.accept(f);
    }
    /*
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    @Override
    public String getDescription ()
    {
        final FileFilter    ff=getEmbeddedFilter();
        return (null == ff) ? null : ff.getDescription();
    }
}
