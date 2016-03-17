/*
 *
 */
package net.community.apps.tools.adm.charts;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import net.community.apps.tools.adm.AbstractAccessMainFrame;
import net.community.apps.tools.adm.DBConnectDialog;
import net.community.apps.tools.adm.charts.resources.ResourcesAnchor;
import net.community.chest.Triplet;
import net.community.chest.awt.attributes.Titled;
import net.community.chest.awt.window.EscapeKeyWindowCloser;
import net.community.chest.db.DBAccessConfig;
import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.menu.MenuItemExplorer;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.ui.helpers.combobox.TypedComboBox;
import net.community.chest.ui.helpers.combobox.TypedComboBoxActionListener;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.PieDataset;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 23, 2010 1:58:40 PM
 */
final class MainFrame extends AbstractAccessMainFrame<ResourcesAnchor> {
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
     * @see net.community.apps.common.MainComponent#getResourcesAnchor()
     */
    @Override
    public ResourcesAnchor getResourcesAnchor ()
    {
        return ResourcesAnchor.getInstance();
    }

    private Connection    _dbConn    /* =null */;
    public Connection getConnection ()
    {
        return _dbConn;
    }

    public boolean isConnected ()
    {
        return (getConnection() != null);
    }

    private JMenuItem    _discMenuItem, _refreshMenuItem;
    private AbstractButton    _discBtn, _refreshBtn;
    protected void updateButtonsState (final boolean connected)
    {
        updateButtonsState(connected, _discBtn, _refreshBtn, _discMenuItem, _refreshMenuItem);
    }

    private DBAccessConfig    _connCfg;
    protected void doConnect (final DBAccessConfig cfg)
    {
        try
        {
            final int    nErr=DBAccessConfig.checkDBAccessConfig(cfg);
            if (nErr != 0)
                throw new IllegalStateException("Bad (" + nErr + ") DB access configuration");

            if (isConnected())
                throw new IllegalStateException("Previous connection still active: " + _connCfg);

            final Triplet<?,?,? extends Connection>    cRes=cfg.createConnection();
            if (null == (_dbConn=cRes.getV3()))
                throw new IllegalStateException("No connection generated");

            if (null == _connCfg)
                _connCfg = new DBAccessConfig(cfg);
            else
                _connCfg.update(cfg);

            _logger.info("doConnect(" + _connCfg + ") connected");

            doRefresh();
        }
        catch(Exception e)
        {
            _logger.error("doConnect(" + cfg + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
            BaseOptionPane.showMessageDialog(this, e);
        }
    }

    private final DBAccessConfig    _dbAccess=fillDefaults(new DBAccessConfig());
    public DBAccessConfig getDBAccessConfig ()
    {
        return _dbAccess;
    }

    private Element    _connDlgElem    /* =null */;
    protected void doConnect ()
    {
        if (null == _connDlgElem)
        {
            JOptionPane.showMessageDialog(this, "Missing configuration element", "Cannot show dialog", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final DBAccessConfig    cfg=getDBAccessConfig();
        final DBConnectDialog    dlg=new DBConnectDialog(this, cfg, _connDlgElem, true);
        dlg.setVisible(true);
        if (!dlg.isOkExit())
            return;    // debug breakpoint

        if (isConnected() && (!dlg.isChangedConfig()))
            return;    // debug breakpoint

        doDisconnect();    // disconnect from previous instance
        doConnect(cfg);
    }

    protected boolean okToDisconnect ()
    {
        return true;
    }

    protected void doDisconnect ()
    {
        if (!okToDisconnect())
            return;

        if (isConnected())
        {
            _logger.info("Disconnect from " + _connCfg);

            try
            {
                _dbConn.close();
            }
            catch(Exception e)
            {
                _logger.error("doDisconnect(" + _connCfg + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
                BaseOptionPane.showMessageDialog(this, e);
            }
            finally
            {
                _dbConn = null;
            }
        }
    }

    private static final String cleanStringValue (final String s)
    {
        final int    sLen=(null == s) ? 0 : s.length();
        if (sLen <= 0)
            return s;

        StringBuilder    sb=null;
        int                lastPos=0;
        for (int    curPos=0; curPos < sLen; curPos++)
        {
            final char    ch=s.charAt(curPos);
            if ((ch >= ' ') && (ch < 0x007F))
                continue;

            if (null == sb)
                sb = new StringBuilder(sLen - 1);

            if (curPos > lastPos)
            {
                final String    subText=s.substring(lastPos, curPos);
                sb.append(subText);
            }

            if ((lastPos=curPos+1) >= sLen)
                break;
        }

        if (null == sb)
            return s;

        if (lastPos < sLen)
            sb.append(s.substring(lastPos));
        return sb.toString();
    }

    private final Map<String,? extends Number> runPieChartQuery (
            final String title, final String qry)
        throws SQLException
    {
        if ((null == qry) || (qry.length() <= 0) || (!isConnected()))
            return null;

        if (_logger.isDebugEnabled())
            _logger.debug("runPieChartQuery(" + title + ") " + qry);

        final Connection    c=getConnection();
        Statement            s=c.createStatement();
        try
        {
            final Map<String,Integer>    valsMap=
                new TreeMap<String,Integer>(String.CASE_INSENSITIVE_ORDER);
            for (final ResultSet    rs=s.executeQuery(qry);
                 rs != null && rs.next();
                 )
            {
                final String    name=cleanStringValue(rs.getString(1));
                final int        value=rs.getInt(2);
                final Number    prev=valsMap.put(name, Integer.valueOf(value));
                if (prev != null)
                    throw new IllegalStateException("runPieChartQuery(" + title + ") multiple values for name=" + name);

                if (_logger.isDebugEnabled())
                    _logger.debug("runPieChartQuery(" + title + ") " + name + "=" + value);

            }

            return valsMap;
        }
        finally
        {
            if (s != null)
                s.close();
        }
    }

    private static final CategoryDataset populateCategoryDataset (
            final String title, final Map<String,? extends Number>    res)
    {
        final Collection<? extends Map.Entry<String,? extends Number>>    vl=
            ((null == res) || (res.size() <= 0)) ? null : res.entrySet();
        if ((null == vl) || (vl.size() <= 0))
            return null;

        final DefaultCategoryDataset    ds=new DefaultCategoryDataset();
        for (final Map.Entry<String,? extends Number> vp : vl)
        {
            final String    vn=(null == vp) ? null : vp.getKey();
            final Number    vv=(null == vp) ? null : vp.getValue();
            if ((null == vn) || (vn.length() <= 0) || (null == vv))
                continue;

            ds.addValue(vv, title, vn);
        }

        return ds;
    }

    private JFreeChart    _chart;
    protected JFreeChart populatePieChart (final String title, final Map<String,? extends Number>    res)
    {
        final CategoryDataset    ds=populateCategoryDataset(title, res);
        final PieDataset        pds=
            ((null == title) || (title.length() <= 0) || (null == ds)) ? null : DatasetUtilities.createPieDatasetForRow(ds, title);
        if (null == _chart)
        {
            _chart = ChartFactory.createPieChart3D(title,    // chart title
                                                   pds,    // dataset
                                                   false,  // legend
                                                   true,    // tooltips
                                                   false    // url(s)
                                                   );
            final Plot    p=(null == _chart) ? null : _chart.getPlot();
            if (p instanceof PiePlot)
                ((PiePlot) p).setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({1} / {3}) {2}"));

            if (_chart != null)
            {
                final Container        ctPane=getContentPane();
                ctPane.add(new ChartPanel(_chart), BorderLayout.CENTER);
            }
        }
        else
        {
            final Plot    p=_chart.getPlot();
            if (p instanceof PiePlot)
                ((PiePlot) p).setDataset(pds);
            _chart.setTitle(title);
        }

        return _chart;
    }

    protected JFreeChart populatePieChart (final String title, final String qry)
    {
        if ((null == qry) || (qry.length() <= 0))
            return null;
        if (!isConnected())
            return null;

        try
        {
            final Map<String,? extends Number>    res=runPieChartQuery(title, qry);
            return populatePieChart(title, res);
        }
        catch(Exception e)
        {
            _logger.error("populatePieChart(" + title + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
            BaseOptionPane.showMessageDialog(this, e);
            return null;
        }
    }

    protected void doRefresh ()
    {
        populatePieChart((null == _pieQrySelector) ? null : _pieQrySelector.getSelectedText(),
                         (null == _pieQrySelector) ? null : _pieQrySelector.getSelectedValue());
    }

    private static final Map<String,String>    extractQueriesMap (
            final Collection<? extends Element> el)
    {
        if ((null == el) || (el.size() <= 0))
            return null;

        Map<String,String>    ret=null;
        for (final Element elem : el)
        {
            final String    key=(null == elem) ? null : elem.getAttribute(Titled.ATTR_NAME),
                            val=DOMUtils.getElementStringValue(elem);
            if ((null == key) || (key.length() <= 0)
             || (null == val) || (val.length() <= 0))
                continue;

            if (null == ret)
                ret = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);

            final String    qry=val.replace('\n', ' ')
                                   .replace('\t', ' ')
                                   .replaceAll("[ ]+", " ")
                                   .trim(),
                            prev=ret.put(key, qry);
            if ((prev != null) && (prev.length() > 0))
                throw new IllegalStateException("Duplicat item for title=" + key);
        }

        return ret;
    }

    private static final Map<String,String>    extractQueriesMap (final Element elem)
    {
        return extractQueriesMap(DOMUtils.extractAllNodes(Element.class, elem, Node.ELEMENT_NODE));
    }

    private static final Map<String,String>    extractQueriesMap (final Document doc)
    {
        return extractQueriesMap((null == doc) ? null : doc.getDocumentElement());
    }

    private Map<String,String> loadDefaultQueriesMap ()
    {
        try
        {
            final ResourcesAnchor    ra=getResourcesAnchor();
            final Document            doc=
                (null == ra) ? null : ra.getDocument("built-in-piechart-queries.xml");
            return extractQueriesMap(doc);
        }
        catch(Exception e)
        {
            _logger.error("Failed (" + e.getClass().getName() + ") to load default piechart queries: " + e.getMessage(), e);
            BaseOptionPane.showMessageDialog(this, e);
            return null;
        }
    }

    private static <V, C extends TypedComboBox<V>> C createQueriesSelection (
            final C cb, final Map<String,? extends V> qrysMap)
    {
        if (null == cb)
            return null;

        final Collection<? extends Map.Entry<String,? extends V>>    ql=
            ((null == qrysMap) || (qrysMap.size() <= 0)) ? null : qrysMap.entrySet();
        if ((null == ql) || (ql.size() <= 0))
            return cb;

        final int    numItems=cb.getItemCount();
        if (numItems > 0)
            cb.removeAllElements();

        for (final Map.Entry<String,? extends V> qe : ql)
        {
            if (null == qe)
                continue;

            cb.addItem(qe.getKey(), qe.getValue());
        }

        return cb;
    }

    private Map<String,String>    _pieQueriesMap;
    private Map<String,String> getPieQueriesMap ()
    {
        if (null == _pieQueriesMap)
            _pieQueriesMap = loadDefaultQueriesMap();
        return _pieQueriesMap;
    }

    private TypedComboBox<String> createPieQueriesSelection (TypedComboBox<String> cb)
    {
        return createQueriesSelection(cb, getPieQueriesMap());
    }

    private TypedComboBox<String>    _pieQrySelector;
    /*
     * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, org.w3c.dom.Element)
     */
    @Override
    public void loadFile (File f, Element dlgElement)
    {
        try
        {
            final Document                doc=DOMUtils.loadDocument(f);
            final Map<String,String>    newMap=extractQueriesMap(doc),
                                        curMap=getPieQueriesMap();
            if ((null == newMap) || (newMap.size() <= 0))
                return;

            final JComboBox    cb;
            if (curMap != null)
            {
                curMap.putAll(newMap);
                cb = createQueriesSelection(_pieQrySelector, curMap);
            }
            else
                cb = createQueriesSelection(_pieQrySelector, newMap);

            if (cb != null)
                cb.setSelectedIndex(0);

            doRefresh();
        }
        catch(Exception e)
        {
            _logger.error("loadFile(" + f + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
            BaseOptionPane.showMessageDialog(this, e);
        }
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#layoutSection(java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void layoutSection (String name, Element elem)
            throws RuntimeException
    {
        if ("db-connect-dialog".equalsIgnoreCase(name))
        {
            if (_connDlgElem != null)
                throw new IllegalStateException("layoutSection(" + name + ") re-specified");

            _connDlgElem = elem;
        }
        else
            super.layoutSection(name, elem);
    }

    private static final String    CONNECT_CMD="connect",
                                DISCONNECT_CMD="disconnect",
                                REFRESH_CMD="refresh";
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
        lm.put(EXIT_CMD, getExitActionListener());
        lm.put(ABOUT_CMD, getShowManifestActionListener());
        lm.put(LOAD_CMD, getLoadFileListener());
        lm.put(CONNECT_CMD, new ActionListener() {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed (ActionEvent e)
            {
                doConnect();
            }
        });
        lm.put(DISCONNECT_CMD, new ActionListener() {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed (ActionEvent e)
            {
                doDisconnect();
            }
        });
        lm.put(REFRESH_CMD, new ActionListener() {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed (ActionEvent e)
            {
                doRefresh();
            }
        });

        setActionListenersMap(lm);
        return lm;
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#setMainMenuItemsActionHandlers(net.community.chest.swing.component.menu.MenuItemExplorer)
     */
    @Override
    protected Map<String,JMenuItem> setMainMenuItemsActionHandlers (MenuItemExplorer ie)
    {
        final Map<String,JMenuItem>    im=super.setMainMenuItemsActionHandlers(ie);
        _discMenuItem = (null == im) ? null : im.get(DISCONNECT_CMD);
        _refreshMenuItem = (null == im) ? null : im.get(REFRESH_CMD);
        return im;
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        final Container        ctPane=getContentPane();
        final KeyListener    kl=new EscapeKeyWindowCloser(this);
        addKeyListener(kl);

        try
        {
            final JToolBar                                b=getMainToolBar();
            final Map<String,? extends AbstractButton>    hm=setToolBarHandlers(b);
            if ((hm != null) && (hm.size() > 0))
            {
                _discBtn = hm.get(DISCONNECT_CMD);
                _refreshBtn = hm.get(REFRESH_CMD);
            }

            if (null == _pieQrySelector)
            {
                _pieQrySelector = createPieQueriesSelection(new TypedComboBox<String>(String.class));
                _pieQrySelector.addActionListener(
                        new TypedComboBoxActionListener<String,TypedComboBox<String>>() {
                            /*
                             * @see net.community.chest.ui.helpers.combobox.TypedComboBoxActionListener#handleSelectedItem(java.awt.event.ActionEvent, net.community.chest.ui.helpers.combobox.TypedComboBox, java.lang.String, java.lang.Object)
                             */
                            @Override
                            public void handleSelectedItem (ActionEvent e,
                                                            TypedComboBox<String> cb,
                                                            String text,
                                                            String value)
                            {
                                populatePieChart(text, value);
                            }
                        });
                _pieQrySelector.setSelectedIndex(0);
                b.add(_pieQrySelector);
            }

            ctPane.add(b, BorderLayout.NORTH);
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }
    /**
     * @param args initial arguments as received by the (@link #main(String[]))
     * @throws Exception if illegal option(s) encountered
     */
    private void processMainArgs (final String... args) throws Exception
    {
        final int            numArgs=
            (null == args) ? 0 : args.length;
        Map<String,String>    valsMap=null;
        DBAccessConfig        cfg=null;
        File                extFile=null;
        for (int aIndex=0; aIndex < numArgs; aIndex++)
        {
            final String    arg=args[aIndex];
            if ((null == arg) || (arg.length() <= 1) || (arg.charAt(0) != '-'))
                throw new IllegalArgumentException("Malformed option: " + arg);

            aIndex++;

            if (aIndex >= numArgs)
                throw new IllegalArgumentException("No value provided for option=" + arg);

            final String    val=args[aIndex];
            if ((null == val) || (val.length() <= 0))
                throw new IllegalArgumentException("Null/empty value provided for option=" + arg);

            if (null == valsMap)
                valsMap = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);

            final String    prev=valsMap.put(arg, val);
            if ((prev != null) && (prev.length() > 0))
                throw new IllegalArgumentException("Option=" + arg + " value re-specified");

            if ("-file".equalsIgnoreCase(arg))
            {
                extFile = new File(val);
            }
            else
            {
                cfg = getDBAccessConfig();

                if (!processDBAccessConfigParameter(cfg, valsMap, arg, val))
                    throw new IllegalArgumentException("Unknown option: " + arg);
            }
        }

        if (extFile != null)
            loadFile(extFile, null);

        if (0 == DBAccessConfig.checkDBAccessConfig(cfg))
            doConnect(cfg);
    }
    /**
     * @param args original arguments as received by <I>main</I> entry point
     * @throws Exception if unable to start main frame and application
     */
    MainFrame (final String ... args) throws Exception
    {
        super(args);
        processMainArgs(args);
    }
}
