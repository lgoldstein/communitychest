/*
 *
 */
package net.community.apps.tools.xslapply;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.community.chest.CoVariantReturn;
import net.community.chest.ui.helpers.table.TypedTable;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 11, 2008 9:21:17 AM
 */
public class SelectionFilesList extends TypedTable<SelectionFilePair> {
    /**
     *
     */
    private static final long serialVersionUID = 6340897509934868202L;
    public SelectionFilesList ()
    {
        super(new SelectionFilesModel());
    }
    /*
     * @see javax.swing.JTable#getModel()
     */
    @Override
    @CoVariantReturn
    public SelectionFilesModel getModel ()
    {
        return (SelectionFilesModel) super.getModel();
    }

    public SelectionFilePair addFile (final File f)
    {
        final SelectionFilePair    fp=(null == f) ? null : new SelectionFilePair(f);
        if (null == fp)
            return null;

        final SelectionFilesModel    m=getModel();
        m.add(fp);
        return fp;
    }

    public void addFiles (final Collection<? extends File> fl)
    {
        if ((null == fl) || (fl.size() <= 0))
            return;

        for (final File f : fl)
            addFile(f);
    }

    public void addFiles (final File ... files)
    {
        addFiles(((null == files) || (files.length <= 0)) ? null : Arrays.asList(files));
    }

    public Collection<File> getSelectedFiles ()
    {
        final SelectionFilesModel    m=getModel();
        final int                    numItems=(null == m) ? 0 : m.getRowCount();
        if (numItems <= 0)    // debug breakpoint
            return null;

        Collection<File>    fl=null;
        for (final SelectionFilePair    fp : m)
        {
            final File    fv=(null == fp) ? null : fp.getFile();
            if ((null == fp) || (null == fv) || (!fp.isSelected()))
                continue;

            if (null == fl)
                fl = new LinkedList<File>();
            fl.add(fv);
        }

        return fl;
    }

    public void setSelectedFiles (final Collection<? extends File> fl)
    {
        final int    numItems=getRowCount();
        if (numItems > 0)    // debug breakpoint
            removeAll();

        addFiles(fl);
    }

    public void setSelectedFiles (final File ... files)
    {
        setSelectedFiles(((null == files) || (files.length <= 0)) ? null : Arrays.asList(files));
    }

    private SelectionFilePairRenderer    _pairComponent    /* =null */;
    private synchronized SelectionFilePairRenderer getCellDisplayComponent (int row, int column)
    {
        if ((row < 0) || (column != 0))
            return null;

        synchronized(this)
        {
            if (null == _pairComponent)
                _pairComponent = new SelectionFilePairRenderer();
        }

        return _pairComponent;
    }
    /*
     * @see javax.swing.JTable#getCellRenderer(int, int)
     */
    @Override
    public TableCellRenderer getCellRenderer (int row, int column)
    {
        return getCellDisplayComponent(row, column);
    }
    /*
     * @see javax.swing.JTable#getCellEditor(int, int)
     */
    @Override
    public TableCellEditor getCellEditor (int row, int column)
    {
        return getCellDisplayComponent(row, column);
    }
}
