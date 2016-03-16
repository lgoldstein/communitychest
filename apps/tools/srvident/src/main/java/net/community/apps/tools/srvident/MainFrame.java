package net.community.apps.tools.srvident;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import net.community.apps.common.BaseMainFrame;
import net.community.apps.tools.srvident.resources.ResourcesAnchor;
import net.community.chest.io.FileUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.swing.component.table.DefaultTableScroll;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 25, 2007 9:37:51 AM
 */
final class MainFrame extends BaseMainFrame<ResourcesAnchor> {
    /**
     *
     */
    private static final long serialVersionUID = -2215172147006168587L;
    private static final LoggerWrapper    _logger=WrapperFactoryManager.getLogger(MainFrame.class);
    /*
     * @see net.community.apps.common.BaseMainFrame#getLogger()
     */
    @Override
    protected LoggerWrapper getLogger ()
    {
        return _logger;
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#getResourcesAnchor()
     */
    @Override
    public ResourcesAnchor getResourcesAnchor ()
    {
        return ResourcesAnchor.getInstance();
    }

    private static final class Refresher implements Runnable {
        private final IdTableModel    _m;
        private final MainFrame        _f;
        protected Refresher (IdTableModel m, MainFrame f)
        {
            _m = m;
            _f = f;
        }
        /*
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run ()
        {
            try
            {
                _m.refresh(_f);
            }
            finally
            {
                _f.signalRefreshEnd(this);
            }
        }
    }

    private Refresher    _r    /* =null */;
    protected synchronized void signalRefreshEnd (Refresher r)
    {
        if (_r == r)
            _r = null;
    }

    private IdTableModel    _tblModel    /* =null */;
    protected synchronized void refresh ()
    {
        if (null == _r)
        {
            _r =  new Refresher(_tblModel, this);
            new Thread(_r).start();
        }
    }
    /* Each line can be a URL (e.g., SMTP://host:port) - special handling
     * is done with the "MX" "protocol" - this is equivalent to generating
     * an SMTP URL for each of the MX records of the specified domain
     * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void loadFile (File f, String cmd, Element dlgElement)
    {
        final String filePath=(null == f) ? null : f.getAbsolutePath();
        if ((null == filePath) || (filePath.length() <= 0))
            return;

        BufferedReader    in=null;
        try
        {
            if (null == _tblModel)
                throw new IllegalStateException("No table model initialized");

            in = new BufferedReader(new FileReader(f));
            // each non-empty/remark(#,;) line is scanned for first non-empty argument that is a URL
            for (String    l=StringUtil.getCleanStringValue(in.readLine());
                 l != null;
                 l=StringUtil.getCleanStringValue(in.readLine()))
            {
                final int    len=l.length();
                if (len <= 0)
                    continue;

                final char    ch1=l.charAt(0);
                if (('#' == ch1) || (';' == ch1))
                    continue;

                int    lastPos=0;
                for ( ; lastPos < len; lastPos++)
                {
                    final char    c=l.charAt(lastPos);
                    if ((c <= ' ') || (c > 0x7E))
                        break;
                }

                final String    url=l.substring(0, lastPos);
                try
                {
                    final URI        u=new URI(url);
                    final String    p=u.getScheme();
                    if ("MX".equalsIgnoreCase(p))
                        _tblModel.resolveMXRecords(u.getHost());
                    else
                        _tblModel.addURLEntry(u);
                }
                catch(Exception e)
                {
                    getLogger().error(e.getClass().getName() + " while handle url=" + url + ": " + e.getMessage(), e);
                }
            }
        }
        catch(Exception e)
        {
            getLogger().error("loadFile(" + f + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
        }
        finally
        {
            try
            {
                FileUtil.closeAll(in);
            }
            catch(IOException ce)
            {
                getLogger().error("loadFile(" + f + ") " + ce.getClass().getName() + " on close: " + ce.getMessage(), ce);
                JOptionPane.showMessageDialog(this, ce.getMessage(), ce.getClass().getName(), JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#getActionListenersMap(boolean)
     */
    @Override
    protected Map<String,? extends ActionListener> getActionListenersMap (boolean createIfNotExist)
    {
        final Map<String,? extends ActionListener>    org=super.getActionListenersMap(createIfNotExist);
        if (((org != null) && (org.size() > 0)) || (!createIfNotExist))
            return org;

        final Map<String,ActionListener>    lm=new TreeMap<String,ActionListener>(String.CASE_INSENSITIVE_ORDER);
        lm.put("exit", getExitActionListener());
        lm.put("about", getShowManifestActionListener());
        lm.put("load", getLoadFileListener());
        lm.put("refresh", new ActionListener () {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent event) {
                    try
                    {
                        refresh();
                    }
                    catch(Exception e)
                    {
                        getLogger().error("loadFile() " + e.getClass().getName() + ": " + e.getMessage(), e);
                        BaseOptionPane.showMessageDialog(getMainFrameInstance(), e);
                    }
                }
            });

        setActionListenersMap(lm);
        return lm;
    }
    // TODO move this class to common code
    private static class RowSelectionHandler extends MouseAdapter {
        private final JTable    _tbl;
        protected RowSelectionHandler (final JTable tbl) throws IllegalArgumentException
        {
            if (null == (_tbl=tbl))
                throw new IllegalArgumentException("No " + JTable.class.getSimpleName() + " instance provided");
        }
        /*
         * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseClicked (MouseEvent e)
        {
            if (null == e)            // should not happen
                return;

            if (SwingUtilities.isLeftMouseButton(e))    // left click
            {
                if (e.getClickCount() < 2)    // only double-click(s)
                    return;

                final int    rowIndex=_tbl.getSelectedRow();
                if (rowIndex < 0)
                {
                    // interpret as "new row"
                    JOptionPane.showMessageDialog(null, "New row TBD", "Add new row", JOptionPane.ERROR_MESSAGE);
                }
                else
                {
                    // interpret as "edit row"
                    JOptionPane.showMessageDialog(null, "Edit row TBD", "Edit row", JOptionPane.ERROR_MESSAGE);
                }
            }
            else if (SwingUtilities.isRightMouseButton(e))    // show context menu
            {
                if (e.getClickCount() > 1)    // only single click
                    return;

                final int    rowIndex=_tbl.getSelectedRow();
                if (rowIndex < 0)
                {
                    // show only "New row" enabled
                    JOptionPane.showMessageDialog(null, "New row context TBD", "New row context", JOptionPane.ERROR_MESSAGE);
                }
                else
                {
                    // show only "Delete/Edit" enabled
                    JOptionPane.showMessageDialog(null, "Delete/Edit row context TBD", "Delete/Edit row context", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private JScrollPane getResultsPanel ()
    {
        if (_tblModel != null)
            throw new IllegalStateException("getResultsPanel() already initialized");

        try
        {
            final Element    tblElem=getResourcesAnchor().getSection("results-table");
            if (null == tblElem)
                throw new StreamCorruptedException("No results table section");

            _tblModel = new IdTableModel(tblElem);

        }
        catch(Exception e)
        {
            throw getLogger().errorThrowable(new RuntimeException(e.getClass().getName() + " while build results table: " + e.getMessage(), e));
        }

        final Dimension    d=getSize();
        final int        maxWidth=(int) d.getWidth();
        _tblModel.adjustRelativeColWidths(maxWidth);

        final JTable    tbl=new JTable(_tblModel);
        tbl.setAutoCreateRowSorter(true);
        tbl.setRowSelectionAllowed(true);

        final JScrollPane    tblScroll=new DefaultTableScroll(tbl);
        tblScroll.addMouseListener(new RowSelectionHandler(tbl));
        return tblScroll;
    }

    private JLabel    _statusBar    /* =null */;
    public void updateStatusBar (final String text)
    {
        if (_statusBar != null)
            _statusBar.setText((null == text) ? "" : text);
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        final Container    ctPane=getContentPane();
        ctPane.add(getResultsPanel(), BorderLayout.CENTER);

        _statusBar = new JLabel("Ready");
        ctPane.add(_statusBar, BorderLayout.SOUTH);
//        ctPane.add(getButtonsPanel(), BorderLayout.SOUTH);
    }
    /**
     * @param args original arguments as received by <I>main</I> entry point
     * @throws Exception if unable to start main frame and application
     */
    MainFrame (final String ... args) throws Exception
    {
        super(args);
    }
}
