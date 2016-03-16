/*
 *
 */
package net.community.chest.swing.component.filechooser;

import java.io.File;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Uses the default {@link FileSystemView#getFileSystemView()} instance
 * to provide the required {@link FileView} implementation</P>
 *
 * @author Lyor G.
 * @since Dec 31, 2008 10:12:36 AM
 */
public class DefaultSystemFileView extends FileView {
    private FileSystemView    _sysView;
    public synchronized FileSystemView getSystemView ()
    {
        if (null == _sysView)
            _sysView = FileSystemView.getFileSystemView();
        return _sysView;
    }

    public synchronized void setSystemView (FileSystemView v)
    {
        if (_sysView != v)
            _sysView = v;
    }

    public DefaultSystemFileView (FileSystemView v)
    {
        _sysView = v;
    }

    public DefaultSystemFileView ()
    {
        this(null);
    }
    /*
     * @see javax.swing.filechooser.FileView#getIcon(java.io.File)
     */
    @Override
    public Icon getIcon (final File f)
    {
        final FileSystemView    v=(null == f) ? null : getSystemView();
        if (null == v)
            return null;

        return v.getSystemIcon(f);
    }
    /*
     * @see javax.swing.filechooser.FileView#getName(java.io.File)
     */
    @Override
    public String getName (final File f)
    {
        if (null == f)
            return null;

        final FileSystemView    v=getSystemView();
        if (null == v)
            return f.getName();

        return v.getSystemDisplayName(f);
    }
    /*
     * @see javax.swing.filechooser.FileView#getTypeDescription(java.io.File)
     */
    @Override
    public String getTypeDescription (final File f)
    {
        final FileSystemView    v=(null == f) ? null : getSystemView();
        if (null == v)
            return null;

        return v.getSystemTypeDescription(f);
    }
    /*
     * @see javax.swing.filechooser.FileView#getDescription(java.io.File)
     */
    @Override
    public String getDescription (final File f)
    {
        return getTypeDescription(f);
    }
    /*
     * @see javax.swing.filechooser.FileView#isTraversable(java.io.File)
     */
    @Override
    public Boolean isTraversable (final File f)
    {
        if (null == f)
            return Boolean.FALSE;

        final FileSystemView    v=getSystemView();
        if (null == v)
            return Boolean.valueOf(f.isDirectory());

        return v.isTraversable(f);
    }

    public static final DefaultSystemFileView    DEFAULT=new DefaultSystemFileView();
}
