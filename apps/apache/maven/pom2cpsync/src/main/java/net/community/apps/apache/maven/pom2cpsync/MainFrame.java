/*
 * 
 */
package net.community.apps.apache.maven.pom2cpsync;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;

import net.community.apps.apache.maven.pom2cpsync.resources.ResourcesAnchor;
import net.community.apps.common.BaseMainFrame;
import net.community.chest.apache.maven.helpers.BaseTargetDetails;
import net.community.chest.apache.maven.helpers.BuildProject;
import net.community.chest.apache.maven.helpers.DependenciesList;
import net.community.chest.lang.StringUtil;
import net.community.chest.resources.XmlAnchoredResourceAccessor;
import net.community.chest.swing.component.menu.MenuExplorer;
import net.community.chest.swing.component.menu.MenuItemExplorer;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;
import net.community.chest.util.map.MapEntryImpl;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 14, 2008 1:51:12 PM
 */
final class MainFrame extends BaseMainFrame<ResourcesAnchor> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6695575329547036473L;
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
	 * @see net.community.apps.common.BaseMainFrame#getResourcesAnchor()
	 */
	@Override
	public ResourcesAnchor getResourcesAnchor ()
	{
		return ResourcesAnchor.getInstance();
	}

	private final Collection<? extends BaseTargetDetails> loadDependenciesFile (
			final DependencyDetailsPanel	pnl, final String path)
	{
		final Class<?>	pClass=(null == pnl) ? null : pnl.getClass();
		final String	loadType=(null == pClass) ? null : pClass.getSimpleName();
		if ((null == loadType) || (loadType.length() <= 0))
			return null;

		try
 		{
			final Collection<? extends BaseTargetDetails>	deps=pnl.setDependencies(path);
			final int										numDeps=(null == deps) ? 0 : deps.size();
			final LoggerWrapper								l=getLogger();
			l.info("loadDependenciesFile(" + loadType + ")[" + path + "] loaded " + numDeps + " dependencies");

			if ((numDeps > 0) && l.isDebugEnabled())
			{
				for (final BaseTargetDetails d : deps)
				{
					if (null == d)
						continue;
					l.debug("loadDependenciesFile(" + loadType + ")[" + path + "] " + d.getGroupId() + "/" + d.getArtifactId() + "/" + d.getVersion());
				}
			}

			return deps;
		}
		catch(Exception e)
		{
			getLogger().error("loadDependenciesFile(" + loadType + ")[" + path + "] " + e.getClass().getName() + ": " + e.getMessage(), e);
			BaseOptionPane.showMessageDialog(this, e);
			return null;
		}
	}
	/**
	 * @param src Source dependencies
	 * @param tgt Target dependencies
	 * @param l A {@link LoggerWrapper} instance to be used for logging
	 * @return A {@link Collection} of &quot;pairs&quot; represented as a
	 * {@link java.util.Map.Entry} whose key=the mismatched {@link BaseTargetDetails}
	 * instance from the source when compared with the target, value={@link Integer}
	 * showing if the instance was found but it did not have a matching version
	 * (if non-null) or if instance was not found (null) 
	 */
	private static Collection<Map.Entry<BaseTargetDetails,Integer>> checkDependencies (
			final DependencyDetailsPanel src, final DependencyDetailsPanel tgt, final LoggerWrapper	l)
	{
		final List<? extends BaseTargetDetails>	sl=(null == src) ? null : src.getDependencies(),
												tl=(null == tgt) ? null : tgt.getDependencies();
		final int								numSrc=(null == sl) ? 0 : sl.size();
		final String							srcName=(null == src) ? null : src.getName(),
												tgtName=(null == tgt) ? null : tgt.getName();
		if (numSrc <= 0)
			return null;

		Collection<Map.Entry<BaseTargetDetails,Integer>>	res=null;
		for (final BaseTargetDetails dSrc : sl)
		{
			final String	groupId=(null == dSrc) ? null : dSrc.getGroupId(),
							artifactId=(null == dSrc) ? null : dSrc.getArtifactId();
			if (null == dSrc)
				continue;

			final int	tIndex=DependenciesList.indexOf(tl, groupId, artifactId);
			if (tIndex >= 0)	// if found by group + artifact then check version
			{
				final BaseTargetDetails	dTgt=tl.get(tIndex);
				final String			sVersion=dSrc.getVersion(),
										tVersion=(null == dTgt) ? null : dTgt.getVersion();
				if (0 == StringUtil.compareDataStrings(sVersion, tVersion, false))
					continue;	// OK if same version

				if ((l != null) && l.isDebugEnabled())
					l.debug("checkDependencies(" + srcName + ")[" + tgtName + "] " + groupId + "/" + artifactId + " mismatched versions: " + sVersion + " vs. " + tVersion);
			}
			else
			{
				if ((l != null) && l.isDebugEnabled())
					l.debug("checkDependencies(" + srcName + ")[" + tgtName + "] " + groupId + "/" + artifactId + " no match found");
			}

			final Map.Entry<BaseTargetDetails,Integer>	re=
				new MapEntryImpl<BaseTargetDetails,Integer>(dSrc, (tIndex < 0) ? null : Integer.valueOf(tIndex));
			if (null == res)
				res = new LinkedList<Map.Entry<BaseTargetDetails,Integer>>();
			res.add(re);
		}

		return res;
	}
	// marks all the differences in the source vs. the target
	// returns pairs of changed rows in the source - each one with its assigned background color
	private static Collection<Map.Entry<Integer,Color>> markMismatchedDependencies (
			final DependencyDetailsPanel src, final DependencyDetailsPanel tgt, final LoggerWrapper l)
	{
		final Collection<? extends Map.Entry<? extends BaseTargetDetails,Integer>>	diffs=
			checkDependencies(src, tgt, l);
		final int																	numDiffs=
			(null == diffs) ? 0 : diffs.size();
		if (numDiffs <= 0)
			return null;	// OK if no mismatches

		final List<? extends BaseTargetDetails>	sl=(null == src) ? null : src.getDependencies();
		final int								numSrc=(null == sl) ? 0 : sl.size();
		if (numSrc <= 0)	// should not happen
			return null;

		final Collection<Map.Entry<Integer,Color>>	res=new ArrayList<Map.Entry<Integer,Color>>(numDiffs);
		for (final Map.Entry<? extends BaseTargetDetails,Integer> de : diffs)
		{
			final BaseTargetDetails	dSrc=(null == de) ? null : de.getKey();
			final String			groupId=(null == dSrc) ? null : dSrc.getGroupId(),
									artifactId=(null == dSrc) ? null : dSrc.getArtifactId();
			if (null == dSrc)
				continue;
	
			final int	sIndex=DependenciesList.indexOf(sl, groupId, artifactId);
			if (sIndex < 0)	// should not happen since all dependencies come from the source
				continue;

			final Integer					tIndex=
				(null == de) ? null : de.getValue();
			final Color						c=
				(null == tIndex) ? DependencyTargetEntry.NO_ENTRY_COLOR : DependencyTargetEntry.BAD_VERSION_COLOR;
			final Map.Entry<Integer,Color>	ce=
				new MapEntryImpl<Integer,Color>(Integer.valueOf(sIndex), c);
			res.add(ce);
		}

		src.markMismatchedDependencies(res);
		return res;
	}

	private POMDependenciesPanel		_pomDetails;
	private ClasspathDependenciesPanel	_classpathDetails;
	protected void markMismatchedDependencies ()
	{
		final LoggerWrapper	l=getLogger();
		markMismatchedDependencies(_pomDetails, _classpathDetails, l);
		markMismatchedDependencies(_classpathDetails, _pomDetails, l);
	}
	// NOTE !!! these values must match the resources XML
	private static final String	POM_DLGELEM_NAME="load-POM-dialog", CLASSPATH_DLG_ELEM_NAME="load-classpath-dialog";
	protected final Collection<? extends BaseTargetDetails> loadDependenciesFile (final String path, final String loadType)
	{
		final boolean					isPOMFile=POM_DLGELEM_NAME.equalsIgnoreCase(loadType);
		final DependencyDetailsPanel	pnl;
		if (isPOMFile)
			pnl = _pomDetails;
		else if (CLASSPATH_DLG_ELEM_NAME.equalsIgnoreCase(loadType))
			pnl = _classpathDetails;
		else
			throw new IllegalStateException("loadDependenciesFile(" + loadType + ")[" + path + "] unknown file type");

		final Collection<? extends BaseTargetDetails>	deps=loadDependenciesFile(pnl, path);
		if ((null == deps) || (deps.size() <= 0))
			return deps;	// if nothing loaded don't go any further

		// check if the matching equivalent of the loaded file exists and has not been loaded yet
		final String					pairFileName=isPOMFile ? ".classpath" : BuildProject.DEFAULT_POM_FILE_NAME;
		final DependencyDetailsPanel	pairPnl=isPOMFile ? _classpathDetails : _pomDetails;
		final File						loadedFile=new File(path),
										loadedDir=loadedFile.getParentFile(),
										pairFile=loadedDir.isDirectory() ? new File(loadedDir, pairFileName) : null;
		if ((pairFile != null) && pairFile.exists() && pairFile.isFile() && (pairFile.length() > 0L))
		{
			// make sure file not already loaded
			final String	pairPath=pairFile.getAbsolutePath(),
							pnlPath=pairPnl.getFilePath();
			if (StringUtil.compareDataStrings(pairPath, pnlPath, false) != 0)
			{
				final Collection<? extends BaseTargetDetails>	pairDeps=
					loadDependenciesFile(pairPath, isPOMFile ? CLASSPATH_DLG_ELEM_NAME : POM_DLGELEM_NAME);
				if ((pairDeps != null) && (pairDeps.size() > 0))
					markMismatchedDependencies();
			}
		}

		return deps;
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void loadFile (File f, String cmd, Element dlgElement)
	{
		final String filePath=(null == f) ? null : f.getAbsolutePath();
		if ((null == filePath) || (filePath.length() <= 0))
			return;

		loadDependenciesFile(filePath, (null == dlgElement) ? null : dlgElement.getAttribute(XmlAnchoredResourceAccessor.SECTION_NAME_ATTR));
	}

	protected void loadFile (final ActionEvent event)
	{
		final String	cmd=(null == event) ? null : event.getActionCommand();
		try
		{
			final ResourcesAnchor	anc=getResourcesAnchor();
			final Element			dlgElement=anc.getSection(cmd);
			loadFile(cmd, dlgElement);
		}
		catch(Exception e)
		{
			getLogger().error("loadFile(" + cmd + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
			BaseOptionPane.showMessageDialog(this, e);
		}
	}

	protected void refresh ()
	{
		final DependencyDetailsPanel[]	pnls={ _pomDetails, _classpathDetails };
		for (final DependencyDetailsPanel p : pnls)
		{
			final String	filePath=(null == p) ? null : p.getFilePath();
			if ((null == filePath) || (filePath.length() <= 0))
				continue;	// OK means no file loaded in panel

			loadDependenciesFile(p, filePath);
		}
		markMismatchedDependencies();
	}

	private static final Collection<Map.Entry<DependencyMismatchType,JMenuItem>> getModeItemsList (final MenuItemExplorer ie)
	{
		final Map<String,? extends JMenuItem>	itemsMap=(null == ie) ? null : ie.getItemsMap();
		if ((null == itemsMap) || (itemsMap.size() <= 0))
			return null;

		Collection<Map.Entry<DependencyMismatchType,JMenuItem>>	nodeItemsList=null;
		for (final DependencyMismatchType m : DependencyMismatchType.VALUES)
		{
			final String	n=m.toString();
			final JMenuItem	i=itemsMap.get(n);
			if (null == i)
				continue;

			// default initial state
			i.setSelected(DependencyMismatchType.ALL.equals(m));

			if (null == nodeItemsList)
				nodeItemsList = new LinkedList<Map.Entry<DependencyMismatchType,JMenuItem>>();
			nodeItemsList.add(new MapEntryImpl<DependencyMismatchType,JMenuItem>(m, i));
		}

		return nodeItemsList;
	}

	private Collection<? extends Map.Entry<DependencyMismatchType,? extends JMenuItem>>	_modeItemsList	/* =null */;
	protected void setFilterMode (final DependencyMismatchType	mode)
	{
		if (null == mode)
			return;

		final DependencyDetailsPanel[]	pnls={ _pomDetails, _classpathDetails };
		for (final DependencyDetailsPanel p : pnls)
			p.setFilterMode(mode);

		// update selection state for menu items
		if ((_modeItemsList != null) && (_modeItemsList.size() > 0))
		{
			for (final Map.Entry<DependencyMismatchType,? extends JMenuItem> mie : _modeItemsList)
			{
				final DependencyMismatchType	m=(null == mie) ? null : mie.getKey();
				final JMenuItem					i=(null == mie) ? null : mie.getValue();
				if (i != null)
					i.setSelected(mode.equals(m));
			}
		}
	}
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
		lm.put("exit", getExitActionListener());
		lm.put("about", getShowManifestActionListener());

		final ActionListener	fileLoader=new ActionListener () {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent event)
				{
					loadFile(event);
				}
			};
		lm.put(POM_DLGELEM_NAME, fileLoader);
		lm.put(CLASSPATH_DLG_ELEM_NAME, fileLoader);
		lm.put("refresh", new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent event)
				{
					refresh();
				}
			});
		lm.put("show", new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent event)
				{
					final String					cmd=
						(null == event) ? null : event.getActionCommand();
					final DependencyMismatchType	mode=
						DependencyMismatchType.fromString(cmd);
					setFilterMode(mode);
				}
			});

		setActionListenersMap(lm);
		return lm;
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#setMainMenuActionHandlers(net.community.chest.swing.component.menu.MenuItemExplorer, net.community.chest.swing.component.menu.MenuExplorer)
	 */
	@Override
	protected void setMainMenuActionHandlers (MenuItemExplorer ie, MenuExplorer me)
	{
		super.setMainMenuActionHandlers(ie, me);
		_modeItemsList = getModeItemsList(ie);
	}

	private <P extends DependencyDetailsPanel> P createDetailsPanel (final String name, final P pnl)
	{
		final Element							elem=
			applyDefinitionElement(name, pnl, DependencyDetailsPanelReflectiveProxy.DDPNL);
		if (null == elem)
			_logger.warn("createDetailsPanel(" + name + ") no definition applied");
		final DependencyDetailsTable			tbl=pnl.getDependencyDetailsTable();
		final DependencyDetailsTableModel		m=tbl.getTypedModel();
		final Dimension							d=getSize();
    	final int								maxWidth=(null == d) ? 0 : (int) d.getWidth();
    	if (maxWidth > 0)
    		m.adjustRelativeColWidths(maxWidth / 2);

		return pnl;
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();

		_pomDetails = createDetailsPanel("pom-details-panel", new POMDependenciesPanel());
		_classpathDetails = createDetailsPanel("classpath-details-panel", new ClasspathDependenciesPanel());

		final JSplitPane	sp=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, _pomDetails, _classpathDetails);
		sp.setResizeWeight(0.5);

		final Container	ctPane=getContentPane();	
		ctPane.add(sp, BorderLayout.CENTER);
	}
	// -pom=<path> or -cp=<path>
	private String processFileLoadArgument (final String a)
	{
		final int	aLen=(null == a) ? 0 : a.length(),
					sPos=(aLen <= 1) ? (-1) : a.indexOf('=');
		if ((aLen <= 0) || (a.charAt(0) != '-')
		 || (sPos <= 0) || (sPos >= (aLen-2))) 
			return null;

		final String	loadType=a.substring(1, sPos),
						loadArg=a.substring(sPos + 1),
						loadPath=StringUtil.stripDelims(loadArg);
		if ((null == loadPath) || (loadPath.length() <= 0))
			return null;	// must have a file path argument

		if ("pom".equalsIgnoreCase(loadType))
			loadDependenciesFile(loadPath, POM_DLGELEM_NAME);
		else if ("cp".equalsIgnoreCase(loadType))
			loadDependenciesFile(loadPath, CLASSPATH_DLG_ELEM_NAME);
		else	// unknown load type
			return null;

		return loadType;
	}

	private void processMainArguments (final String ... args)
	{
		if ((null == args) || (args.length <= 0))
			return;

		final LoggerWrapper	l=getLogger();
		for (final String a : args)
		{
			final int	aLen=(null == a) ? 0 : a.length();
			if (aLen <= 0)
				continue;

			final String	loadType=processFileLoadArgument(a);
			if ((null == loadType) || (loadType.length() <= 0))
				JOptionPane.showMessageDialog(this, a, "Bad/Unknown argument", JOptionPane.ERROR_MESSAGE);
			else
				l.info("processMainArguments(" + a + ")");
		}
	}
	/**
	 * @param args original arguments as received by <I>main</I> entry point
	 * @throws Exception if unable to start main frame and application
	 */
	MainFrame (final String ... args) throws Exception
	{
		super(args);

		processMainArguments(args);
	}
}
