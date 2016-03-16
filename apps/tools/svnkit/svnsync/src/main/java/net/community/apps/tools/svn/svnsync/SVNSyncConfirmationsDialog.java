/*
 *
 */
package net.community.apps.tools.svn.svnsync;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.ui.helpers.dialog.ButtonsPanel;
import net.community.chest.ui.helpers.dialog.SettableDialog;
import net.community.chest.ui.helpers.list.TypedList;
import net.community.chest.ui.helpers.list.TypedListModel;

import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 7, 2010 2:46:01 PM
 */
class SVNSyncConfirmationsDialog extends SettableDialog<Set<Pattern>>
        implements ActionListener, KeyListener, MouseListener {
    /**
     *
     */
    private static final long serialVersionUID = -8772846565698100181L;
    private final SVNSyncMainFrame    _f;
    public final SVNSyncMainFrame getMainFrame ()
    {
        return _f;
    }

    public SVNSyncConfirmationsDialog (SVNSyncMainFrame parent, Set<Pattern> patterns)
    {
        super(parent);

        if ((_f=parent) == null)
            throw new IllegalStateException("No frame parent provided");
        setContent(patterns);
    }

    private static class PatternsListModel extends TypedListModel<Pattern> {
        /**
         *
         */
        private static final long serialVersionUID = 3196725795344826278L;
        public PatternsListModel ()
        {
            super(Pattern.class);
        }
        /*
         * @see net.community.chest.ui.helpers.list.TypedListModel#getValueDisplayText(java.lang.Object)
         */
        @Override
        public String getValueDisplayText (Pattern value)
        {
            return (value == null) ? "" : value.pattern();
        }
    }

    private static class PatternsList extends TypedList<Pattern> {
        /**
         *
         */
        private static final long serialVersionUID = -8950448987130728899L;
        public PatternsList ()
        {
            super(new PatternsListModel());
        }
        /*
         * @see net.community.chest.ui.helpers.list.TypedList#getModel()
         */
        @Override
        public PatternsListModel getModel ()
        {
            return (PatternsListModel) super.getModel();
        }

        public Pattern[] deleteSelectedItems ()
        {
            final Pattern[]    values=getSelectedValues();
            if ((values == null) || (values.length <= 0))
                return null;

            for (final Pattern p : values)
                deletePatternValue(p);
            return values;
        }

        private boolean    _modified;
        public boolean isModified ()
        {
            return _modified;
        }

        public int deletePatternValue (Pattern p)
        {
            final PatternsListModel    model=getModel();
            final int                pIndex=(model == null) ? Integer.MIN_VALUE : model.valueIndexOf(p);
            if (pIndex < 0)
                return pIndex;

            model.remove(pIndex);
            if (!_modified)
                _modified = true;
            return pIndex;
        }

        public Pattern addNewItem ()
        {
            final String    patValue=JOptionPane.showInputDialog(this, "Regexp:", "Enter new confirmation pattern", JOptionPane.PLAIN_MESSAGE);
            if ((patValue == null) || (patValue.length() <= 0))
                return null;    // user canceled

            try
            {
                final Pattern    p=Pattern.compile(patValue);
                addValue(p);
                if (!_modified)
                    _modified = true;
                return p;
            }
            catch(RuntimeException e)
            {
                JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }

        public Pattern updateItem (int rowIndex)
        {
            final Pattern    p=getItemValue(rowIndex);
            final String    pValue=(p == null) ? null : p.pattern();
            if ((pValue == null) || (pValue.length() <= 0))
                return null;

            final Object    inpValue=JOptionPane.showInputDialog(this, "Regexp:", "Update confirmation pattern", JOptionPane.PLAIN_MESSAGE, null, null, pValue);
            final String    patValue=(inpValue == null) ? null : inpValue.toString();
            if ((patValue == null) || (patValue.length() <= 0))
                return null;    // user canceled

            if (pValue.equals(patValue))
                return p;    // nothing changed

            try
            {
                final Pattern    pNew=Pattern.compile(patValue);
                deletePatternValue(p);
                addValue(pNew);

                if (!_modified)
                    _modified = true;
                return p;
            }
            catch(RuntimeException e)
            {
                JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
                return null;
            }
        }
    }

    private PatternsList    _patternsList;
    /*
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked (MouseEvent e)
    {
        final int    clickCount=(e == null) ? 0 : e.getClickCount();
        if ((clickCount <= 1) || (_patternsList == null))
            return;

        final Point    clickPt=e.getPoint();
        final int    clickRow=_patternsList.locationToIndex(clickPt);
        if (clickRow < 0)
            _patternsList.addNewItem();
        else
            _patternsList.updateItem(clickRow);
    }
    /*
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed (MouseEvent e)
    {
        // do nothing
    }
    /*
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased (MouseEvent e)
    {
        // do nothing
    }
    /*
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered (MouseEvent e)
    {
        // do nothing
    }
    /*
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited (MouseEvent e)
    {
        // do nothing
    }
    /*
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    @Override
    public void keyTyped (KeyEvent e)
    {
        // do nothing
    }
    /*
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    @Override
    public void keyPressed (KeyEvent e)
    {
        // do nothing
    }
    /*
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    @Override
    public void keyReleased (KeyEvent e)
    {
        if (e == null)
            return;

        final int    keyCode=e.getKeyCode();
        switch(keyCode)
        {
            case KeyEvent.VK_DELETE    :
                if (_patternsList != null)
                    _patternsList.deleteSelectedItems();
                break;

            case KeyEvent.VK_INSERT    :
                if (_patternsList != null)
                    _patternsList.addNewItem();
                break;

            case KeyEvent.VK_ESCAPE    :
                if ((_patternsList != null) && _patternsList.isModified())
                {
                    final int    nRes=JOptionPane.showConfirmDialog(this, "Discard changes ?", "Confirm exit", JOptionPane.YES_NO_OPTION);
                    if (nRes != JOptionPane.YES_OPTION)
                        return;
                }
                dispose();
                break;

            default    : // do nothing
        }
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.HelperDialog#layoutSection(java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void layoutSection (String name, Element elem)
            throws RuntimeException
    {
        if ("patterns-list".equalsIgnoreCase(name))
        {
            if (_patternsList != null)
                throw new IllegalStateException("layoutSection(" + name + ") already initialized for: " + DOMUtils.toString(elem));

            try
            {
                _patternsList = (PatternsList) new PatternsList().fromXml(elem);
            }
            catch(Exception e)
            {
                throw ExceptionUtil.toRuntimeException(e);
            }
        }
        else
            super.layoutSection(name, elem);
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.FormDialog#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        if (_patternsList == null)
            _patternsList = new PatternsList();
        _patternsList.addKeyListener(this);
        _patternsList.addMouseListener(this);

        final Container    ctPane=getContentPane();
        ctPane.add(_patternsList, BorderLayout.CENTER);

        final ButtonsPanel    btnPanel=getButtonsPanel();
        if (btnPanel != null)
            btnPanel.setActionListener(this);
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.SettableDialog#clearContent()
     */
    @Override
    public void clearContent ()
    {
        if ((_patternsList != null) && (_patternsList.getItemCount() > 0))
            _patternsList.removeAll();
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.SettableDialog#refreshContent(java.lang.Object)
     */
    @Override
    public void refreshContent (Set<Pattern> value)
    {
        setContent(value);
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.SettableDialog#setContent(java.lang.Object)
     */
    @Override
    public void setContent (Set<Pattern> value)
    {
        clearContent();

        if ((_patternsList != null) && (value != null) && (value.size() > 0))
        {
            for (final Pattern p : value)
                _patternsList.addValue(p);
        }
    }
    /*
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed (ActionEvent e)
    {
        final String    cmd=(e == null) ? null : e.getActionCommand();
        if (!"ok".equalsIgnoreCase(cmd))
            return;

        final SVNSyncMainFrame    f=getMainFrame();
        if (f == null)
            return;

        if ((_patternsList != null) && (_patternsList.getItemCount() > 0))
            f.setConfirmLocations(_patternsList.toValuesList());
        else
            f.setConfirmLocations(null);
        dispose();
    }
    /*
     * @see java.awt.Window#dispose()
     */
    @Override
    public void dispose ()
    {
        final SVNSyncMainFrame    f=getMainFrame();
        if (f != null)
            f.clearConfirmationsDialog(this);

        super.dispose();
    }
}
