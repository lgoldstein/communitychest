/*
 * 
 */
package net.community.apps.tools.adm.config;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import net.community.apps.tools.adm.AbstractAccessMainFrame;
import net.community.apps.tools.adm.DBConnectDialog;
import net.community.apps.tools.adm.config.resources.ResourcesAnchor;
import net.community.chest.Triplet;
import net.community.chest.awt.window.EscapeKeyWindowCloser;
import net.community.chest.db.DBAccessConfig;
import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.swing.component.list.JListReflectiveProxy;
import net.community.chest.swing.component.list.ListUtils;
import net.community.chest.swing.component.menu.MenuItemExplorer;
import net.community.chest.swing.component.scroll.ScrolledComponent;
import net.community.chest.swing.component.table.JTableReflectiveProxy;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 14, 2009 12:06:08 PM
 */
public class MainFrame extends AbstractAccessMainFrame<ResourcesAnchor> {
	private static final LoggerWrapper	_logger=WrapperFactoryManager.getLogger(MainFrame.class);
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


	private JMenuItem	_discMenuItem, _refreshMenuItem;
	private AbstractButton	_discBtn, _refreshBtn;
	protected void updateButtonsState (final boolean connected)
	{
		updateButtonsState(connected, _discBtn, _refreshBtn, _discMenuItem, _refreshMenuItem);
	}

	private ValuesTable	_valsTbl	/* =null */;
	private void clearValues (final boolean enabled)
	{
		final TableModel	m=(null == _valsTbl) ? null : _valsTbl.getModel();
		final int			numValues=
			(m instanceof ValuesTableModel) ? m.getRowCount() : 0;
		if (numValues > 0)
			((ValuesTableModel) m).clear(true);
		if ((_valsTbl != null) && (_valsTbl.isEnabled() != enabled))
			_valsTbl.setEnabled(enabled);
	}

	private ValuesPopulator	_valsPopl	/* =null */;
	void signalValuesPopulationDone (ValuesPopulator popl, String secName)
	{
		if (_valsPopl != popl)
		{
			_logger.warn("signalValuesPopulationDone(" + secName + ") values populator mismatch");
			return;
		}

		final String	curSec=getCurrentSectionName();
		if (StringUtil.compareDataStrings(curSec, secName, false) != 0)
		{
			_logger.warn("signalValuesPopulationDone(" + secName + ") mismatched section: expected=" + curSec);
			return;
		}

		if (_valsTbl != null)
			_valsTbl.setEnabled(true);
		if (_refreshBtn != null)
			_refreshBtn.setEnabled(true);
		if (_refreshMenuItem != null)
			_refreshMenuItem.setEnabled(true);

		if (_logger.isDebugEnabled())
			_logger.debug("signalValuesPopulationDone(" + secName + ") done");
		_valsPopl = null;
	}

	protected void populateSectionValues (final String secName, final Connection conn)
	{
		if ((null == secName) || (secName.length() <= 0) || (null == conn))
			return;

		if (_valsPopl != null)
		{
			_logger.warn("populateSectionValues(" + secName + ") in progress");
			return;
		}

		if (_logger.isDebugEnabled())
			_logger.debug("populateSectionValues(" + secName + ") start");

		// will be re-enabled when population completed
		clearValues(false);
		if (_refreshBtn != null)
			_refreshBtn.setEnabled(false);
		if (_refreshMenuItem != null)
			_refreshMenuItem.setEnabled(false);

		_curSection = secName;
		_valsPopl = new ValuesPopulator(this, conn, secName);
		_valsPopl.execute();
	}

	private Connection	_dbConn	/* =null */;
	public Connection getConnection ()
	{
		return _dbConn;
	}

	public boolean isConnected ()
	{
		return (getConnection() != null);
	}

	private String	_curSection	/* =null */;
	public String getCurrentSectionName ()
	{
		return _curSection;
	}

	public boolean updateDBValue (
			final String pName, final String colName, final String colVal)
	{
		final String	secName=getCurrentSectionName();
		try
		{
			final Connection	c=getConnection();
			Statement			s=c.createStatement();
			try
			{
				final StringBuilder	sb=new StringBuilder(256)
							.append("UPDATE config SET ")
							.append(colName)
							.append("='")
							.append((null == colVal) ? "" : colVal)
							.append("' WHERE section = '")
							.append(secName)
							.append("' AND paramname = '")
							.append(pName)
							.append('\'')
							;
				if (s.execute(sb.toString()))
					throw new IllegalStateException("Unexpected result set returned");
				
				final int	numUpdated=s.getUpdateCount();
				if (numUpdated != 1)
					throw new IllegalStateException("Too many values updated: " + numUpdated);
				
				_logger.info("updateDBValue(" + secName + "/" + pName + ")[" + colName + "]=" + colVal);
				return true;
			}
			finally
			{
				if (s != null)
					s.close();
			}
		}
		catch(Exception e)
		{
			_logger.error("updateDBValue(" + secName + "/" + pName + ")[" + colName + "]=" + colVal + ": " + e.getClass().getName() + ": " + e.getMessage(), e);
			BaseOptionPane.showMessageDialog(this, e);
			return false;
		}
	}

	boolean addConfigValue (ValueTableEntry vte)
	{
		final String	secName=getCurrentSectionName(),
						pName=(null == vte) ? null : vte.getKey();
		if ((null == pName) || (pName.length() <= 0))
			return false;

		final TableModel	m=(null == _valsTbl) ? null : _valsTbl.getModel();
		if (m instanceof ValuesTableModel)
		{
			if (_logger.isDebugEnabled())
				_logger.debug("addConfigValue(" + secName + ":" + pName + ")[" + vte.getValue() + "]");

			((ValuesTableModel) m).add(vte, true);
			return true;
		}

		_logger.warn("addConfigValue(" + secName + ":" + pName + ")[" + vte.getValue() + "] unknown model type: " + ((null == m) ? null : m.getClass().getName()));
		return false;
	}

	ValueTableEntry addConfigValue (final String secName, final String pName, final String pValue)
	{
		final String	curSec=getCurrentSectionName();
		if ((curSec != null) && (curSec.length() > 0)
		 && (StringUtil.compareDataStrings(secName, curSec, false)) != 0)
		{
			_logger.warn("addConfigValue(" + secName + ":" + pName + ")[" + pValue + "] mismatched sections: expected=" + curSec);
			return null;
		}

		final ValueTableEntry	vte=new ValueTableEntry(pName, pValue);
		if (addConfigValue(vte))
			return vte;

		return null;
	}

	public boolean insertDBConfigValue (final ValueTableEntry vte)
	{
		if (null == vte)
			return false;

		final String	secName=getCurrentSectionName();
		try
		{
			final Connection	c=getConnection();
			Statement			s=c.createStatement();
			try
			{
				final StringBuilder	sb=new StringBuilder(256)
							.append("INSERT INTO config (id,section,paramname,paramvalue,ver) VALUES(")
								.append("(SELECT MAX(id)+1 FROM config), ")
								.append('\'').append(secName).append("', ")
								.append('\'').append(vte.getKey()).append("', ")
								.append('\'').append(vte.getValue()).append("', ")
								.append("1)")
							;
				if (s.execute(sb.toString()))
					throw new IllegalStateException("Unexpected result set returned");
				
				final int	numUpdated=s.getUpdateCount();
				if (numUpdated != 1)
					throw new IllegalStateException("Too many values inserted: " + numUpdated);
				
				_logger.info("insertDBConfigValue(" + secName + ")[" + vte + "] inserted");
				return true;
			}
			finally
			{
				if (s != null)
					s.close();
			}
		}
		catch(Exception e)
		{
			_logger.error("insertDBConfigValue(" + secName + ")[" + vte + "]: " + e.getClass().getName() + ": " + e.getMessage(), e);
			BaseOptionPane.showMessageDialog(this, e);
			return false;
		}
	}

	private JList	_secList;
	// returns selected section name - null/empty if none found
	protected String signalSectionSelectionChanged ()
	{
		final Object	selObj=(null == _secList) ? null :_secList.getSelectedValue();
		final String	secName=(null == selObj) ? null : selObj.toString();
		if ((null == secName) || (secName.length() <= 0))
			return null;

		populateSectionValues(secName, getConnection());
		return secName;
	}

	void addConfigSection (final String n)
	{
		if ((null == n) || (n.length() <= 0))
			return;
		final ListModel	m=(null == _secList) ? null : _secList.getModel();
		if (!(m instanceof DefaultListModel))
			return;		// should not happen

		final DefaultListModel	lm=(DefaultListModel) m;
		lm.addElement(n);
		if (_logger.isDebugEnabled())
			_logger.debug("addConfigSection(" + n + ")");
	}

	private SectionsPopulator	_secsPopl	/* =null */;
	void signalSectionsPopulationDone (SectionsPopulator popl)
	{
		if (_secsPopl != popl)
		{
			_logger.warn("Sections populator mismatch");
			return;
		}

		_secsPopl = null;

		if (_secList != null)
		{
			final ListModel	lm=_secList.getModel();
			final int		numObjs=(null == lm) ? 0 : lm.getSize();
			// restore previous selection (if any) - otherwise set selection to 1st item
			if (numObjs > 0)
			{
				String	curSectName=getCurrentSectionName();
				// have previous selection - check if still appears in list model
				if ((curSectName != null) && (curSectName.length() > 0))
				{
					final int	mdlIndex=
						ListUtils.findElementIndex(_secList, String.class, curSectName, String.CASE_INSENSITIVE_ORDER);
					// check if current selected index same as model index
					if (mdlIndex >= 0)
					{
						final int		selIndex=_secList.getSelectedIndex();
						final Object	selObj=
							((selIndex < 0) || (selIndex >= numObjs)) ? null : lm.getElementAt(selIndex);
						final String	selName=(null == selObj) ? null : selObj.toString();
						// check if selection same as before
						if (StringUtil.compareDataStrings(curSectName, selName, false) == 0)
							populateSectionValues(curSectName, getConnection());
						else
							_secList.setSelectedIndex(mdlIndex);
					}
					else	// cached section name no longer exists
					{
						_curSection = null;
						curSectName = null;
					}
				}

				// no previous selection - set 1st one
				if ((null == curSectName) || (curSectName.length() <= 0))
					_secList.setSelectedIndex(0);
				_secList.setEnabled(true);
			}
			else
			{
				_secList.setEnabled(false);
			}
		}

		if (_refreshBtn != null)
			_refreshBtn.setEnabled(true);
		if (_refreshMenuItem != null)
			_refreshMenuItem.setEnabled(true);
	}

	private void clearSections (final boolean	enabled)
	{
		final ListModel	m=(null == _secList) ? null : _secList.getModel();
		if (!(m instanceof DefaultListModel))
			return;

		final DefaultListModel	lm=(DefaultListModel) m;
		final int				numSects=lm.getSize();
		if (numSects > 0)
			lm.clear();

		if (_secList.isEnabled() != enabled)
			_secList.setEnabled(enabled);
	}

	protected void populateSections (final Connection conn)
	{
		if (_secsPopl != null)
		{
			_logger.warn("Sections population already in progress");
			return;
		}

		// will be re-enabled when population completed
		clearSections(false);
		clearValues(false);

		if (_refreshBtn != null)
			_refreshBtn.setEnabled(false);
		if (_refreshMenuItem != null)
			_refreshMenuItem.setEnabled(false);

		_secsPopl = new SectionsPopulator(this, conn);
		_secsPopl.execute();
	}

	private DBAccessConfig	_connCfg;
	protected void doRefresh ()
	{
		final Connection	conn=getConnection();
		if (null == conn)
			return;

		try
		{
			populateSections(conn);
		}
		catch(Exception e)
		{
			_logger.error("doRefresh(" + _connCfg + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
			BaseOptionPane.showMessageDialog(this, e);
		}
	}

	protected void doConnect (final DBAccessConfig cfg)
	{
		try
		{
			final int	nErr=DBAccessConfig.checkDBAccessConfig(cfg);
			if (nErr != 0)
				throw new IllegalStateException("Bad (" + nErr + ") DB access configuration");

			if (isConnected())
				throw new IllegalStateException("Previous connection still active: " + _connCfg);

			final Triplet<?,?,? extends Connection>	cRes=cfg.createConnection();
			if (null == (_dbConn=cRes.getV3()))
				throw new IllegalStateException("No connection generated");

			if (null == _connCfg)
				_connCfg = new DBAccessConfig(cfg);
			else
				_connCfg.update(cfg);

			_logger.info("doConnect(" + _connCfg + ") connected");

			populateSections(_dbConn);
		}
		catch(Exception e)
		{
			_logger.error("doConnect(" + cfg + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
			BaseOptionPane.showMessageDialog(this, e);
		}
	}

	private final DBAccessConfig	_dbAccess=fillDefaults(new DBAccessConfig());
	public DBAccessConfig getDBAccessConfig ()
	{
		return _dbAccess;
	}

	private Element	_connDlgElem	/* =null */;
	protected void doConnect ()
	{
		if (null == _connDlgElem)
		{
			JOptionPane.showMessageDialog(this, "Missing configuration element", "Cannot show dialog", JOptionPane.ERROR_MESSAGE);
			return;
		}

		final DBAccessConfig	cfg=getDBAccessConfig();
		final DBConnectDialog	dlg=new DBConnectDialog(this, cfg, _connDlgElem, true);
		dlg.setVisible(true);
		if (!dlg.isOkExit())
			return;	// debug breakpoint

		if (isConnected() && (!dlg.isChangedConfig()))
			return;	// debug breakpoint

		doDisconnect();	// disconnect from previous instance
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

		clearSections(false);
		clearValues(false);

		if (_curSection != null)
			_curSection = null;

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
	/*
	 * @see net.community.apps.common.FilesLoadMainFrame#exitApplication()
	 */
	@Override
	public void exitApplication ()
	{
		doDisconnect();
		super.exitApplication();
	}
	/*
	 * @see java.awt.Window#dispose()
	 */
	@Override
	public void dispose ()
	{
		doDisconnect();
		super.dispose();
	}
	
	private Element	_valEditDlgElem;
	/*
	 * @see net.community.apps.common.BaseMainFrame#layoutSection(java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void layoutSection (String name, Element elem)
			throws RuntimeException
	{
		if ("value-edit-dialog".equalsIgnoreCase(name))
		{
			if (_valEditDlgElem != null)
				throw new IllegalStateException("layoutSection(" + name + ") re-specified");

			_valEditDlgElem = elem;
		}
		else if ("db-connect-dialog".equalsIgnoreCase(name))
		{
			if (_connDlgElem != null)
				throw new IllegalStateException("layoutSection(" + name + ") re-specified");

			_connDlgElem = elem;
		}
		else if ("sections-list".equalsIgnoreCase(name))
		{
			if (_secList != null)
				throw new IllegalStateException("layoutSection(" + name + ") re-specified");

			try
			{
				if (null == (_secList=JListReflectiveProxy.LIST.fromXml(new JList(new DefaultListModel()), elem)))
					throw new IllegalStateException("No list generated");
			}
			catch (Exception e)
			{
				throw ExceptionUtil.toRuntimeException(e);
			}

			_secList.addListSelectionListener(new ListSelectionListener() {
					/*
					 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
					 */
					@Override
					public void valueChanged (ListSelectionEvent e)
					{
						if ((null == e) || e.getValueIsAdjusting())
							return;

						final int	i1=e.getFirstIndex(), il=e.getLastIndex();
						if ((i1 < 0) || (il < 0) )
							return;

						signalSectionSelectionChanged();
					}
				});
		}
		else if ("values-table-model".equalsIgnoreCase(name))
		{
			if (_valsTbl != null)
				throw new IllegalStateException("layoutSection(" + name + ") re-specified");

			try
			{
				final ValuesTableModel	m=new ValuesTableModel();
				if (m.fromXml(elem) != m)
					throw new IllegalStateException("layoutSection(" + name + ") mismatched initialization for " + DOMUtils.toString(elem));
				
				_valsTbl = new ValuesTable(m);
				_valsTbl.setRowSorter(new ValuesTableSorter(m));

				final Element	tblElem=
					applyDefinitionElement("values-table-instance", _valsTbl, JTableReflectiveProxy.TBL);
				if ((tblElem != null) && _logger.isDebugEnabled())
					_logger.debug("layoutSection(" + name + ")[" + DOMUtils.toString(tblElem) + "]");
			}
			catch(Exception e)
			{
				throw ExceptionUtil.toRuntimeException(e);
			}
		}
		else
			super.layoutSection(name, elem);
	}

	private static final String	CONNECT_CMD="connect",
								DISCONNECT_CMD="disconnect",
								REFRESH_CMD="refresh";
	/*
	 * @see net.community.apps.common.BaseMainFrame#getActionListenersMap(boolean)
	 */
	@Override
	protected Map<String,? extends ActionListener> getActionListenersMap (boolean createIfNotExist)
	{
		final Map<String,? extends ActionListener>	org=super.getActionListenersMap(createIfNotExist);
		if (((org != null) && (org.size() > 0)) || (!createIfNotExist))
			return org;

		final Map<String,ActionListener>	lm=new TreeMap<String,ActionListener>(String.CASE_INSENSITIVE_ORDER);
		lm.put(EXIT_CMD, getExitActionListener());
		lm.put(ABOUT_CMD, getShowManifestActionListener());
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
		final Map<String,JMenuItem>	im=super.setMainMenuItemsActionHandlers(ie);
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

		final Container		ctPane=getContentPane();	
		final KeyListener	kl=new EscapeKeyWindowCloser(this);
		addKeyListener(kl);
		try
		{
			final JToolBar								b=getMainToolBar();
			final Map<String,? extends AbstractButton>	hm=setToolBarHandlers(b);
			if ((hm != null) && (hm.size() > 0))
			{
				_discBtn = hm.get(DISCONNECT_CMD);
				_refreshBtn = hm.get(REFRESH_CMD);
			}

			ctPane.add(b, BorderLayout.NORTH);

			final JComponent	listComp=
				(null == _secList) ?  null : new ScrolledComponent<JList>(JList.class, _secList),
								tblComp=
				(null == _valsTbl) ? null : new ScrolledComponent<ValuesTable>(ValuesTable.class, _valsTbl);
			if ((listComp != null) && (tblComp != null))
			{
				_secList.addKeyListener(kl);
				_valsTbl.addKeyListener(kl);
				_valsTbl.addMouseListener(new ValueRowMouseAdapter(this, _valsTbl, _valEditDlgElem));

				final JSplitPane	sp=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listComp, tblComp);
				sp.setResizeWeight(0.33);
				ctPane.add(sp, BorderLayout.CENTER);
			}
			else if (listComp != null)
				ctPane.add(listComp, BorderLayout.WEST);
			else if (tblComp != null)
				ctPane.add(tblComp, BorderLayout.CENTER);
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
    	final int			numArgs=
    		(null == args) ? 0 : args.length;
		Map<String,String>	valsMap=null;
		DBAccessConfig		cfg=null;
    	for (int aIndex=0; aIndex < numArgs; aIndex++)
    	{
    		final String	arg=args[aIndex];
    		if ((null == arg) || (arg.length() <= 1) || (arg.charAt(0) != '-'))
    			throw new IllegalArgumentException("Malformed option: " + arg);
    		
    		aIndex++;

    		if (aIndex >= numArgs)
    			throw new IllegalArgumentException("No value provided for option=" + arg);

    		final String	val=args[aIndex];
    		if ((null == val) || (val.length() <= 0))
    			throw new IllegalArgumentException("Null/empty value provided for option=" + arg);
    		
    		if (null == valsMap)
    			valsMap = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);

    		final String	prev=valsMap.put(arg, val);
    		if ((prev != null) && (prev.length() > 0))
    			throw new IllegalArgumentException("Option=" + arg + " value re-specified");

    		cfg = getDBAccessConfig();

    		if (!processDBAccessConfigParameter(cfg, valsMap, arg, val))
    			throw new IllegalArgumentException("Unknown option: " + arg);
    	}
   
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
