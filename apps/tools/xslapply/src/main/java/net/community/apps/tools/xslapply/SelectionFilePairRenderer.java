/*
 *
 */
package net.community.apps.tools.xslapply;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.LinkedList;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.community.chest.CoVariantReturn;
import net.community.chest.swing.component.filechooser.DefaultSystemFileView;
import net.community.chest.ui.helpers.button.TypedCheckBox;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 11, 2008 9:48:26 AM
 */
public class SelectionFilePairRenderer implements TableCellEditor, TableCellRenderer {
    public SelectionFilePairRenderer ()
    {
        super();
    }

    private Collection<CellEditorListener>    _el;
    protected void fireEditorEndListenerEvent (final Object src)
    {
        // use a copy to avoid concurrent modification
        final Collection<? extends CellEditorListener>    ll;
        {
            if ((null == _el) || (_el.size() <= 0))
                return;
            ll = new ArrayList<CellEditorListener>(_el);
        }

        final ChangeEvent    ev=new ChangeEvent(src);
        for (final CellEditorListener l : ll)
        {
            if (l != null)
                l.editingStopped(ev);
        }
    }

    private TypedCheckBox<SelectionFilePair> getCellComponent (    /* hasFocus == null means edit component */
            JTable table, Object value, boolean isSelected, Boolean hasFocus, int row, int column)
    {
        if ((row < 0) || (column != 0))
            return null;
        if (!(value instanceof SelectionFilePair))
            return null;

        final SelectionFilePair                    fp=(SelectionFilePair) value;
        final File                                fv=fp.getFile();
        final String                            fn=(null == fv) ? null : fv.getAbsolutePath();
        final TypedCheckBox<SelectionFilePair>    cb=
            new TypedCheckBox<SelectionFilePair>(SelectionFilePair.class, fp);
        if (table != null)
        {
            final Color    fg=isSelected ? table.getSelectionForeground() : table.getForeground(),
                        bg=isSelected ? table.getSelectionBackground() : table.getBackground();

            cb.setForeground(fg);
            cb.setBackground(bg);
        }

        if (null == hasFocus)
            cb.addActionListener(new ActionListener() {
                    /*
                     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                     */
                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {
                        final Object    src=(null == ev) ? null : ev.getSource();
                        if (src instanceof TypedCheckBox<?>)
                        {
                            final TypedCheckBox<?>     cbVal=(TypedCheckBox<?>) src;
                            final SelectionFilePair    sfp=
                                (SelectionFilePair)    cbVal.getAssignedValue();
                            sfp.setSelected(cbVal.isSelected());
                            fireEditorEndListenerEvent(src);
                        }
                    }
                });

        final Icon    icon=DefaultSystemFileView.DEFAULT.getIcon(fv);
        if (icon != null)
            cb.setIcon(icon);
        cb.setText(fn);
        cb.setSelected(fp.isSelected());
        return cb;
    }
    /*
     * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
     */
    @Override
    @CoVariantReturn
    public TypedCheckBox<SelectionFilePair> getTableCellEditorComponent (JTable table, Object value, boolean isSelected, int row, int column)
    {
        return getCellComponent(table, value, isSelected, null, row, column);
    }
    /*
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    @CoVariantReturn
    public TypedCheckBox<SelectionFilePair> getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        return getCellComponent(table, value, isSelected, Boolean.valueOf(hasFocus), row, column);
    }
    /*
     * @see javax.swing.CellEditor#addCellEditorListener(javax.swing.event.CellEditorListener)
     */
    @Override
    public synchronized void addCellEditorListener (CellEditorListener l)
    {
        if (null == l)
            return;
        if (null == _el)
            _el = new LinkedList<CellEditorListener>();
        else if (_el.contains(l))
            return;

        _el.add(l);
    }
    /*
     * @see javax.swing.CellEditor#removeCellEditorListener(javax.swing.event.CellEditorListener)
     */
    @Override
    public synchronized void removeCellEditorListener (CellEditorListener l)
    {
        if (null == l)
            return;
        else if ((_el != null) && (_el.size() > 0))
            _el.remove(l);
    }
    /*
     * @see javax.swing.CellEditor#cancelCellEditing()
     */
    @Override
    public void cancelCellEditing ()
    {
        // TODO Auto-generated method stub
    }
    /*
     * @see javax.swing.CellEditor#getCellEditorValue()
     */
    @Override
    public Object getCellEditorValue ()
    {
        // no need to return an updated SelectionFilePair since shared instances used
        return null;
    }
    /*
     * @see javax.swing.CellEditor#isCellEditable(java.util.EventObject)
     */
    @Override
    public boolean isCellEditable (EventObject anEvent)
    {
        return true;
    }
    /*
     * @see javax.swing.CellEditor#shouldSelectCell(java.util.EventObject)
     */
    @Override
    public boolean shouldSelectCell (EventObject anEvent)
    {
        return false;
    }
    /*
     * @see javax.swing.CellEditor#stopCellEditing()
     */
    @Override
    public boolean stopCellEditing ()
    {
        return false;
    }
}
