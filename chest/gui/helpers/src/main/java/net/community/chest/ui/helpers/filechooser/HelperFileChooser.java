/*
 *
 */
package net.community.chest.ui.helpers.filechooser;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

import net.community.chest.swing.component.filechooser.BaseFileChooser;
import net.community.chest.swing.component.filechooser.DefaultSystemFileView;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 31, 2008 10:25:02 AM
 */
public class HelperFileChooser extends BaseFileChooser {
    /**
     *
     */
    private static final long serialVersionUID = -7428125060822570459L;

    // common to all constructors
    {
        setFileView(DefaultSystemFileView.DEFAULT);
    }

    public HelperFileChooser (File currentDirectory, FileSystemView fsv)
    {
        super(currentDirectory, fsv);
    }

    public HelperFileChooser (File currentDirectory)
    {
        this(currentDirectory, FileSystemView.getFileSystemView());
    }

    public HelperFileChooser (FileSystemView fsv)
    {
        super(fsv);
    }

    public HelperFileChooser ()
    {
        this(FileSystemView.getFileSystemView());
    }

    public HelperFileChooser (String currentDirectoryPath, FileSystemView fsv)
    {
        super(currentDirectoryPath, fsv);
    }

    public HelperFileChooser (String currentDirectoryPath)
    {
        this(currentDirectoryPath, FileSystemView.getFileSystemView());
    }

    public HelperFileChooser (Element elem) throws Exception
    {
        this(FileSystemView.getFileSystemView());

        final Object    inst=fromXml(elem);
        if (inst != this)
            throw new IllegalStateException("Mismatched re-constructed instances");
    }
}
