/*
 * 
 */
package net.community.apps.tools.xslapply;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.community.apps.common.BaseMainFrame;
import net.community.apps.tools.xslapply.resources.ResourcesAnchor;
import net.community.chest.dom.DOMUtils;
import net.community.chest.io.FileUtil;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.resources.XmlAnchoredResourceAccessor;
import net.community.chest.resources.XmlDocumentRetriever;
import net.community.chest.swing.component.table.DefaultTableScroll;
import net.community.chest.swing.component.table.JTableReflectiveProxy;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.ui.components.tree.document.BaseDocumentPanel;
import net.community.chest.ui.components.tree.document.TitledEditableFilePathDocumentPanel;
import net.community.chest.ui.helpers.panel.input.LRFieldWithLabelPanel;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 10, 2008 12:12:56 PM
 */
final class MainFrame extends BaseMainFrame<ResourcesAnchor> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7131233476426179222L;
	private static LoggerWrapper	_logger	/* =null */;
	/*
	 * @see net.community.apps.common.BaseMainFrame#getLogger()
	 */
	@Override
	protected synchronized LoggerWrapper getLogger ()
	{
		if (null == _logger)
			_logger = WrapperFactoryManager.getLogger(getClass());
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

	private TitledEditableFilePathDocumentPanel	_srcPanel;
	public String getSourceXMLFilePath ()
	{
		return (null == _srcPanel) ? null : _srcPanel.getFilePath();
	}

	private SelectionFilesList	_xslFiles	/* =null */;
	public Collection<File> getSelectedXSLFiles ()
	{
		return (null == _xslFiles) ? null : _xslFiles.getSelectedFiles();
	}

	protected void loadXSLFiles (final Collection<? extends File> fl)
	{
		if (_xslFiles != null)
			_xslFiles.addFiles(fl);
	}

	private static final String	XSL_LOAD_ELEMNAME="load-xsl-dialog";
	/*
	 * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void loadFile (File f, String cmd, Element dlgElement)
	{
		final String filePath=(null == f) ? null : f.getAbsolutePath();
		if ((null == filePath) || (filePath.length() <= 0))
			return;

		try
		{
			if (_srcPanel != null)
			{
				final Document	doc=DOMUtils.loadDocument(filePath);
				_srcPanel.clearContent();
				_srcPanel.setDocument(doc);
				_srcPanel.setFilePath(filePath);
			}
		}
		catch(Exception e)
		{
			getLogger().error("loadFile(" + filePath + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
			BaseOptionPane.showMessageDialog(this, e);
		}
	}
	/*
	 * @see net.community.apps.common.FilesLoadMainFrame#loadFiles(java.lang.String, org.w3c.dom.Element, java.util.List)
	 */
	@Override
	public void loadFiles (String cmd, Element dlgElement, List<? extends File> fl)
	{
		final String	fileType=(null == dlgElement) ? null : dlgElement.getAttribute(XmlAnchoredResourceAccessor.SECTION_NAME_ATTR);
		if (XSL_LOAD_ELEMNAME.equalsIgnoreCase(fileType))
			loadXSLFiles(fl);
		else
			super.loadFiles(cmd, dlgElement, fl);
	}

	protected TitledEditableFilePathDocumentPanel	_dstPanel;
	public String getDestinationXMLFilePath ()
	{
		return (null == _dstPanel) ? null : _dstPanel.getFilePath();
	}

	private static final Transformer getSavedFileTransformer (final File tgtFile) throws TransformerConfigurationException
	{
		final String	n=(null == tgtFile) ? null : tgtFile.getAbsolutePath();
		final int		nLen=(null == n) ? 0 : n.length(),
						sPos=(nLen <= 2) /* must be a.b */ ? (-1) : n.lastIndexOf('.');
		final String	sfx=(sPos > 0) ? n.substring(sPos) : null;
		if (XmlDocumentRetriever.XML_SUFFIX.equalsIgnoreCase(sfx))
			return DOMUtils.getDefaultXmlTransformer();
		else if (".htm".equalsIgnoreCase(sfx) || ".html".equalsIgnoreCase(sfx))
			return DOMUtils.getDefaultHtmlTransformer();
		else
			throw new TransformerConfigurationException("Unknown output file suffix: " + n);
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#saveFile(java.io.File, org.w3c.dom.Element)
	 */
	@Override
	public void saveFile (final File tgtFile, final Element dlgElement)
	{
		final String	filePath=(null == tgtFile) ? null : tgtFile.getAbsolutePath();
		if ((null == filePath) || (filePath.length() <= 0))
			return;

		try
		{
			final Document	doc=(null == _dstPanel) ? null : _dstPanel.getDocument();
			if (null == doc)	// ignore if no document
				return;

			if (tgtFile.exists())
			{
				if (JOptionPane.showConfirmDialog(this, "Overwrite existing file ?", "File already exist", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;
			}

			final Transformer	t=getSavedFileTransformer(tgtFile);
			OutputStream		out=null;
			try
			{
				out = new FileOutputStream(tgtFile);
				t.transform(new DOMSource(doc), new StreamResult(out));
			}
			finally
			{
				FileUtil.closeAll(out);
			}

			_dstPanel.setFilePath(filePath);
			JOptionPane.showMessageDialog(this, "File successfully written", filePath, JOptionPane.INFORMATION_MESSAGE);
		}
		catch(Exception e)
		{
			getLogger().error("saveFile(" + filePath + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
			BaseOptionPane.showMessageDialog(this, e);
		}
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#saveFile()
	 */
	@Override
	public void saveFile ()
	{
		final String	dstPath=getDestinationXMLFilePath();
		if ((dstPath != null) && (dstPath.length() > 0))
		{
			if (JOptionPane.showConfirmDialog(this, "Re-use existing file ?", dstPath, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
			{
				saveFile(new File(dstPath), null);
				return;
			}
		}

		super.saveFile();
	}

	public Element getLoadXSLDialogElement () throws Exception
	{
		return getResourcesAnchor().getSection(XSL_LOAD_ELEMNAME);
	}

	protected void loadXSLFiles () throws Exception
	{
		loadFile(LOAD_CMD, getLoadXSLDialogElement());
	}

	protected void apply () throws Exception
	{
		final Collection<File>	xslFiles=getSelectedXSLFiles();
		if ((null == xslFiles) || (xslFiles.size() <= 0))
			return;	// ignore if not set

		final Document	doc=(null == _srcPanel) ? null : _srcPanel.getDocument();
		if (null == doc)
			return;	// ignore if not loaded

		if (_dstPanel != null)
		{
	        final ExecutorService es=Executors.newSingleThreadExecutor();
	        es.submit(new Runnable() {
	        	/*
	        	 * @see java.lang.Runnable#run()
	        	 */
	        	@Override
				public void run ()
	        	{
        			final LoggerWrapper	l=getLogger();
        			Document	xlt=doc;
        			for (final File xf : xslFiles)
        			{
        				if (null == xf)	// should not happen
        					continue;

        				l.info("run() - processing XSL=" + xf);
    	        		try
    	        		{
    	        			xlt = DOMUtils.xlateDocument(xlt, xf);
            				l.info("run() - processed XSL=" + xf);
	        			}		        		
    	        		catch(Exception e)
    	        		{
    	        			l.error("apply(" + xf + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
    	        			BaseOptionPane.showMessageDialog(getMainFrame(), e);
    	        			return;
    	        		}
        			}

        			JOptionPane.showMessageDialog(getMainFrame(), "Done.");
        			_dstPanel.setDocument(xlt);
	        	}
	        });
		}
	}

	private ActionListener	_applyListener	/* =null */;
	protected synchronized ActionListener getApplyListener ()
	{
		if (null == _applyListener)
			_applyListener = new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent event)
				{
					try
					{
						apply();
					}
					catch(Exception e)
					{
						getLogger().error("actionPerformed(" + ((null == event) ? null : event.getActionCommand()) + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
						BaseOptionPane.showMessageDialog(getMainFrameInstance(), e);
					}
				}
			};
		return _applyListener;
	}

	private ActionListener	_xslLoader	/* =null */;
	protected synchronized ActionListener getXSLLoaderListener ()
	{
		if (null == _xslLoader)
			_xslLoader = new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent event)
				{
					try
					{
						loadXSLFiles();
					}
					catch(Exception e)
					{
						getLogger().error("actionPerformed(" + ((null == event) ? null : event.getActionCommand()) + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
						BaseOptionPane.showMessageDialog(getMainFrameInstance(), e);
					}
				}
			};

			return _xslLoader;
	}

	protected void clearDisplay ()
	{
		final BaseDocumentPanel[]	pa={ _srcPanel, _dstPanel };
		for (final BaseDocumentPanel p : pa)
		{
			final Document	doc=(null == p) ? null : p.getDocument();
			if (p == _srcPanel)
				p.clearContent();
			else if (doc != null)
				p.setDocument(null);
		}
 	}

	private ActionListener	_clrListener	/* =null */;
	protected ActionListener getClearListener ()
	{
		if (null == _clrListener)
			_clrListener = new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent e)
				{
					clearDisplay();
				}
			};

		return _clrListener;
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
		lm.put("loadXML", getLoadFileListener());
		lm.put("loadXSL", getXSLLoaderListener());
		lm.put(SAVE_CMD, getSaveFileListener());
		lm.put("new", getClearListener());
		lm.put("apply", getApplyListener());
		lm.put(EXIT_CMD, getExitActionListener());
		lm.put(ABOUT_CMD, getShowManifestActionListener());

		setActionListenersMap(lm);
		return lm;
	}

	private static final File getFileArgument (final String argType, final int aIndex, final String ... args)
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		if (aIndex >= numArgs)
			throw new IllegalArgumentException("Missing " + argType + " argument");

		return new File(args[aIndex+1]);
	}

	private void processMainArguments (final String ... args) throws Exception
	{
		final int			numArgs=(null == args) ? 0 : args.length;
		boolean				autoRun=false, autoSave=false;
		File				inputFile=null, saveFile=null;
		Collection<File>	xslFiles=null;
		for (int	aIndex=0; aIndex < numArgs; aIndex++)
		{
			final String	av=args[aIndex];
			if ((null == av) || (av.length() <= 0))
				continue;

			if ("-r".equalsIgnoreCase(av))
				autoRun = true;
			else if ("-s".equalsIgnoreCase(av))
				autoSave = true;
			else if ("-i".equalsIgnoreCase(av))
			{
				if (inputFile != null)
					throw new IllegalArgumentException("Re-specified input file argument");

				inputFile = getFileArgument("input file", aIndex, args);
				aIndex++;
			}
			else if ("-o".equalsIgnoreCase(av))
			{
				if (saveFile != null)
					throw new IllegalArgumentException("Re-specified output file argument");

				saveFile = getFileArgument("output file", aIndex, args);
				aIndex++;
			}
			else if ("-t".equalsIgnoreCase(av))
			{
				final File	xf=getFileArgument("transformer file", aIndex, args);
				if (null == xslFiles)
					xslFiles = new LinkedList<File>();
				xslFiles.add(xf);
				aIndex++;
			}
			else
				throw new IllegalArgumentException("unknown option: " + av);
		}

		if ((inputFile != null) && (_srcPanel != null))
			loadFile(inputFile, LOAD_CMD, null);
		if ((xslFiles != null) && (xslFiles.size() > 0))
			loadXSLFiles(xslFiles);

		if ((saveFile != null) && (_dstPanel != null))
			_dstPanel.setFilePath(saveFile.getAbsolutePath());

		if (autoRun)
			apply();

		if (autoSave)
			saveFile();
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#layoutSection(java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void layoutSection (String name, Element elem) throws RuntimeException
	{
		try
		{
			if ("xsl-files-list".equalsIgnoreCase(name))
			{
				if (null == _xslFiles)
					_xslFiles = new SelectionFilesList();
	
				final Object	o=JTableReflectiveProxy.TBL.fromXml(_xslFiles, elem);
				if (o != _xslFiles)
					throw new IllegalStateException("Mismatched re-constructed instances");
			}
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}

		super.layoutSection(name, elem);
	}
	/*
	 * @see net.community.apps.common.BaseMainFrame#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();

		final Container		ctPane=getContentPane();
		try
		{
			final JToolBar	b=getMainToolBar();
			setToolBarHandlers(b);
			ctPane.add(b, BorderLayout.NORTH);
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}

		if (null == _srcPanel)
			_srcPanel = new TitledEditableFilePathDocumentPanel() {
				/**
				 * 
				 */
				private static final long serialVersionUID = -2136995495503771786L;

				/*
				 * @see net.community.chest.ui.helpers.tree.trees.TitledEditableFilePathDocumentPanel#createFilePathComponent()
				 */
				@Override
				protected LRFieldWithLabelPanel createFilePathComponent ()
				{
					final LRFieldWithLabelPanel	p=super.createFilePathComponent();
					if (p != null)
						p.setTitle("Source file:");
					return p;
				}
			};
		if (null == _dstPanel)
			_dstPanel = new TitledEditableFilePathDocumentPanel() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 4698458642775059233L;

				/*
				 * @see net.community.chest.ui.helpers.tree.trees.TitledEditableFilePathDocumentPanel#createFilePathComponent()
				 */
				@Override
				protected LRFieldWithLabelPanel createFilePathComponent ()
				{
					final LRFieldWithLabelPanel	p=super.createFilePathComponent();
					if (p != null)
						p.setTitle("Result file:");
					return p;
				}
			};

		// split the XML structure trees horizontally
		final JSplitPane	spDocs=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, _srcPanel, _dstPanel);
		spDocs.setResizeWeight(0.5);

		// split the main view
		final JSplitPane	spMain=
			new JSplitPane(JSplitPane.VERTICAL_SPLIT, spDocs, new DefaultTableScroll(_xslFiles));
		spMain.setResizeWeight(0.75);
		ctPane.add(spMain, BorderLayout.CENTER);
	}
	/**
	 * @param args original arguments as received by <I>main</I> entry point
	 * @throws Exception if unable to start main frame and application
	 */
	MainFrame (final String ... args) throws Exception
	{
		super(args);

		try
		{
			processMainArguments(args);
		}
		catch(Exception e)
		{
			getLogger().error(e.getClass().getName() + " while process arguments: " + e.getMessage());
			BaseOptionPane.showMessageDialog(this, e);
		}
	}
}
