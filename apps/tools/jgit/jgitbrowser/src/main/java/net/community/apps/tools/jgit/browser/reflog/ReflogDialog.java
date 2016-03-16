/*
 *
 */
package net.community.apps.tools.jgit.browser.reflog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.community.chest.awt.AWTUtils;
import net.community.chest.awt.window.EscapeKeyWindowCloser;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.scroll.ScrolledComponent;
import net.community.chest.swing.component.table.DefaultTableScroll;
import net.community.chest.swing.component.text.BaseTextArea;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.ui.helpers.dialog.ButtonsPanel;
import net.community.chest.ui.helpers.dialog.SettableDialog;

import org.eclipse.jgit.revwalk.ObjectWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 20, 2011 12:11:00 PM
 */
public class ReflogDialog extends SettableDialog<ObjectWalk> {
    /**
     *
     */
    private static final long serialVersionUID = -5333020343812710654L;
    private final RevObject    _revStart;
    private final RevObject getRevStart ()
    {
        return _revStart;
    }

    public ReflogDialog (Frame owner, ObjectWalk value, RevObject revStart)
    {
        super(owner);
        _revStart = revStart;
        setContent(value);
    }

    private JTable    _tbl;
    private ReflogTableModel    _model;
    private JTextArea    _message;
    protected ReflogEntryRow showCurrentSelectedComment ()
    {
        final int                selIndex=_tbl.getSelectedRow(),
                                rowIndex=_tbl.convertRowIndexToModel(selIndex),
                                numRows=(_model == null) ? 0 : _model.size();
        final ReflogEntryRow    row=
            ((rowIndex < 0) || (rowIndex >= numRows)) ? null : _model.get(rowIndex);
        final RevCommit            logEntry=(row == null) ? null : row.getLogEntry();
        final String            comment=(logEntry == null) ? null : logEntry.getFullMessage();
        if (_message != null)
            _message.setText((comment == null) ? "" : comment);
        return row;
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.HelperDialog#layoutSection(java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void layoutSection (String name, Element elem) throws RuntimeException
    {
        try
        {
            if ("table".equalsIgnoreCase(name))
            {
                _model = new ReflogTableModel(elem);
                _tbl = new JTable(_model);
                _tbl.setRowSelectionAllowed(true);
                _tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            }
            else if ("message".equalsIgnoreCase(name))
            {
                _message = new BaseTextArea(elem);

                final Font    f=_message.getFont();
                _message.setFont(f.deriveFont((float) (f.getSize() + 4)));
            }
            else
                super.layoutSection(name, elem);
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e, true);
        }
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.FormDialog#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        final ListSelectionModel    selModel=(_tbl == null) ? null : _tbl.getSelectionModel();
        if (selModel != null)
            selModel.addListSelectionListener(new ListSelectionListener() {
                    /*
                     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
                     */
                    @Override
                    public void valueChanged (ListSelectionEvent e)
                    {
                        if ((e == null) || e.getValueIsAdjusting())
                            return;

                        showCurrentSelectedComment();
                    }
                });

        final Component        tblScroll=(_tbl == null) ? null : new DefaultTableScroll(_tbl),
                            msgScroll=(_message == null) ? null : new ScrolledComponent<Component>(_message);
        final JSplitPane    divider=
            ((tblScroll == null) || (msgScroll == null)) ? null : new JSplitPane(JSplitPane.VERTICAL_SPLIT, tblScroll, msgScroll);
        if (divider != null)
        {
            final Container        ctPane=getContentPane();
            divider.setDividerLocation(0.5);
            ctPane.add(divider, BorderLayout.CENTER);
        }

        final ButtonsPanel    buttons=getButtonsPanel();
        buttons.addActionListener("next", new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    fillNextLogsDisplay();
                }
            });
        AWTUtils.addKeyListener(new EscapeKeyWindowCloser(this), _tbl, _message, buttons, this);
     }

    private Iterator<RevCommit>    _revsIter;
    protected void clearContent (boolean clearIterator)
    {
        if (_model != null)
            _model.clear();
        if (_message != null)
            _message.setText("");
        if (clearIterator && (_revsIter != null))
            _revsIter = null;
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.SettableDialog#clearContent()
     */
    @Override
    public void clearContent ()
    {
        clearContent(true);
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.SettableDialog#refreshContent(java.lang.Object)
     */
    @Override
    public void refreshContent (ObjectWalk value)
    {
        setContent(value);
    }

    private ObjectWalk    _walker;
    /*
     * @see net.community.chest.ui.helpers.dialog.SettableDialog#setContent(java.lang.Object)
     */
    @Override
    public void setContent (ObjectWalk value)
    {
        clearContent();

        if (_walker != value)
        {
            _walker = value;
            try
            {
                _walker.markStart(getRevStart());
            }
            catch (Exception e)
            {
                BaseOptionPane.showMessageDialog(this, e);
            }
        }
        _revsIter = (value == null) ? null : value.iterator();

        if ((_model == null) || (value == null) || (_revsIter == null))
            return;

        fillLogsDisplay();
    }

    private static final int    MAX_ROWS=100;
    private int fillLogsDisplay ()
    {
        int    numRows=0;
        while ((_model != null) && (_revsIter != null) && _revsIter.hasNext() && (numRows < MAX_ROWS))
        {
            final RevCommit            entry=_revsIter.next();
            final ReflogEntryRow    row=(entry == null) ? null : new ReflogEntryRow(entry);
            if (row == null)
                continue;
            _model.add(row);
            numRows++;
        }

        if (numRows <= 0)
            return 0;

        if (_tbl != null)
            _tbl.addRowSelectionInterval(0, 0);
        return numRows;
    }

    protected void fillNextLogsDisplay ()
    {
        final int    numRows=(_model == null) ? 0 : _model.size();
        // if have less than MAX then don't page any further since no more
        if ((numRows > 0) && (numRows < MAX_ROWS))
        {
            JOptionPane.showMessageDialog(this, "No more log messages available", "End of log", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        clearContent(false);
        fillLogsDisplay();
    }
}
