/*
 *
 */
package net.community.chest.ui.components.table.file;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Displays the file <U>name</U> + other properties inherited from
 * {@link AbstractFileDisplayNameCellRenderer}</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 8:13:01 AM
 */
public class FileNameCellRenderer extends AbstractFileDisplayNameCellRenderer {
    /**
     *
     */
    private static final long serialVersionUID = 6294038018398231900L;
    public FileNameCellRenderer (FileSystemView v)
    {
        super(v);
    }

    public FileNameCellRenderer ()
    {
        this(FileSystemView.getFileSystemView());
    }
    /*
     * @see net.community.chest.ui.components.table.file.AbstractFileDisplayNameCellRenderer#getFileDisplayText(javax.swing.filechooser.FileSystemView, java.io.File)
     */
    @Override
    protected String getFileDisplayText (FileSystemView v, File f)
    {
        return (null == f) ? null : f.getName();
    }
}
