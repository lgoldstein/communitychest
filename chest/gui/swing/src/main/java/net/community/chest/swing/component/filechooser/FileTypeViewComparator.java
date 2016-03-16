/*
 *
 */
package net.community.chest.swing.component.filechooser;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

import net.community.chest.io.file.FileTypeComparator;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2009 12:26:34 PM
 */
public class FileTypeViewComparator extends FileTypeComparator {
    /**
     *
     */
    private static final long serialVersionUID = -3841212467059692174L;
    private FileSystemView    _v;
    public FileSystemView getFileSystemView ()
    {
        return _v;
    }

    public void setFileSystemView (FileSystemView v)
    {
        _v = v;
    }
    /*
     * @see net.community.chest.io.file.FileTypeComparator#getFileType(java.io.File)
     */
    @Override
    public String getFileType (File f)
    {
        final FileSystemView    v=(null == f) ? null : getFileSystemView();
        if (v != null)
            return v.getSystemTypeDescription(f);
        return super.getFileType(f);
    }

    public FileTypeViewComparator (FileSystemView v, boolean ascending)
    {
        super(ascending);
        _v = v;
    }

    public FileTypeViewComparator (boolean ascending)
    {
        this(FileSystemView.getFileSystemView(), ascending);
    }

    public static final FileTypeViewComparator    UPWARD=new FileTypeViewComparator(true),
                                                DOWNWARD=new FileTypeViewComparator(false);
}
