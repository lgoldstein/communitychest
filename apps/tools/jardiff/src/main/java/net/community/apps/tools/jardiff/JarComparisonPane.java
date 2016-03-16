/*
 *
 */
package net.community.apps.tools.jardiff;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.JTable;
import javax.swing.text.JTextComponent;

import net.community.chest.awt.attributes.AttrUtils;
import net.community.chest.awt.attributes.Textable;
import net.community.chest.swing.component.scroll.ScrolledComponent;
import net.community.chest.swing.component.table.DefaultTableScroll;
import net.community.chest.ui.components.text.AutoCompleter;
import net.community.chest.ui.components.text.FileAutoCompleter;
import net.community.chest.ui.helpers.panel.PresetBorderLayoutPanel;
import net.community.chest.ui.helpers.panel.input.LRFieldWithButtonPanel;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 7, 2011 12:11:48 PM
 *
 */
public final class JarComparisonPane extends PresetBorderLayoutPanel implements Textable {
    /**
     *
     */
    private static final long serialVersionUID = 3242175322259365082L;
    public JarComparisonPane (final String name, final JarEntriesTableModel model, final int maxWidth)
    {
        super(5, 5);
        setName(name);

        if ((_model=model) == null)
            throw new IllegalArgumentException("setModel(" + getName() + ") no model provided");

        {
            _fileSelPanel = new LRFieldWithButtonPanel(true /* left-pos button */, true /* auto-layout */);
            _fileCompleter = new FileAutoCompleter<JTextComponent>(_fileSelPanel.getTextField());
            _fileSelPanel.setTitle("...");
            add(_fileSelPanel, BorderLayout.NORTH);
        }

        {
            _model.adjustRelativeColWidths(maxWidth);

            final JTable    tbl=new JTable(model);
            tbl.setAutoCreateRowSorter(true);
            _scroller = new DefaultTableScroll(tbl);
            add(_scroller, BorderLayout.CENTER);
        }
    }

    private final JarEntriesTableModel _model;
    public JarEntriesTableModel getModel ()
    {
        return _model;
    }

    private final ScrolledComponent<? extends JTable>    _scroller;
    public ScrolledComponent<? extends JTable> getScroller ()
    {
        return _scroller;
    }

    private final LRFieldWithButtonPanel    _fileSelPanel;
    public LRFieldWithButtonPanel getFileSelectionPanel ()
    {
        return _fileSelPanel;
    }

    public void addFileSelectionAction (ActionListener l)
    {
        if (l != null)
            _fileSelPanel.addActionListener(l);
    }

    private final FileAutoCompleter<? extends JTextComponent>    _fileCompleter;
    public FileAutoCompleter<? extends JTextComponent> getFileCompleter ()
    {
        return _fileCompleter;
    }
    /*
     * @see net.community.chest.awt.attributes.Textable#getText()
     */
    @Override
    public String getText ()
    {
        final AutoCompleter<?>    ac=getFileCompleter();
        if (ac != null)
            return ac.getText();
        return null;
    }
    /*
     * @see net.community.chest.awt.attributes.Textable#setText(java.lang.String)
     */
    @Override
    public void setText (String t)
    {
        final AutoCompleter<?>    ac=getFileCompleter();
        if (ac != null)
            ac.setText((t == null) ? "" : t);
    }
    /*
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    @Override
    public void setEnabled (boolean enabled)
    {
        AttrUtils.setComponentEnabledState(enabled, getFileSelectionPanel(), getScroller());
        super.setEnabled(enabled);
    }
}
