/*
 * 
 */
package net.community.apps.tools.commenter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StreamCorruptedException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.border.Border;

import net.community.apps.common.BaseMainFrame;
import net.community.apps.tools.commenter.resources.ResourcesAnchor;
import net.community.chest.awt.attributes.AttrUtils;
import net.community.chest.dom.DOMUtils;
import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;
import net.community.chest.io.file.FileIOUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.swing.component.menu.MenuItemExplorer;
import net.community.chest.swing.component.scroll.ScrolledComponent;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.ui.helpers.HelperUtils;
import net.community.chest.ui.helpers.panel.input.LRFieldWithButtonPanel;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Adds a comment text string or file data into a file while formatting
 * it according to the specific commenting rules of the specific file syntax
 * (e.g., Java, C/C++, etc.)</P>
 * 
 * @author Lyor G.
 * @since Jun 30, 2009 11:19:16 AM
 */
final class MainFrame extends BaseMainFrame<ResourcesAnchor> implements Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5316309836266410451L;
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

	public static final MainFrame getContainerFrameInstance ()
	{
		return MainFrame.class.cast(getMainFrameInstance());
	}

	private static final Map<String,Commenter> updateCommentersMap (
			final Map<String,Commenter> org, final Commenter c, final String ... exts)
	{
		if ((null == c) || (null == exts) || (exts.length <= 0))
			return org;

		Map<String,Commenter>	ret=org;
		for (final String e : exts)
		{
			final String	x=FileUtil.adjustExtension(e, true);
			if ((null == x) || (x.length() <= 0))
				continue;
			if (null == ret)
				ret = new TreeMap<String,Commenter>(String.CASE_INSENSITIVE_ORDER);
			ret.put(x, c);
		}

		return ret;
	}

	private static final Map<String,Commenter> createDefaultCommentersMap ()
	{
		final Object[]	cp={
				new ProgramFileCommenter(), new String[] { ".cpp", ".c", ".c#", ".h" },
				new PropertiesFileCommenter(), new String[] { ".properties" },
				new XmlFileCommenter(), new String[] { ".xml", ".htm", ".html" },
				new DosBatchFileCommenter(), new String[] { ".bat" },
				new SqlFileCommenter(), new String[] { ".sql" },
				new JavaFileCommenter(), new String[] { ".java", ".aj", ".groovy" }
			};

		Map<String,Commenter>	ret=null;
		for (int	oIndex=0; oIndex < cp.length; oIndex += 2)
			ret = updateCommentersMap(ret, (Commenter) cp[oIndex], (String[]) cp[oIndex+1]);
		return ret;
	}

	private Map<String,Commenter>	_cmntrsMap;
	private synchronized Map<String,Commenter> getCommentersMap ()
	{
		if (null == _cmntrsMap)
			_cmntrsMap = createDefaultCommentersMap();
		return _cmntrsMap;
	}

	private final static String[]	USAGE={
			"[options] file1 file2 ....",
			"\tWhere options are:",
			"\t\t-t \"text to use as comment\"",
			"\t\t-f <comment-file-path>",
			"\t\t-nogui run immediately from command line",
			"\t\t-b <backup-file-suffix> - e.g. -b .bak - if missing then no backup executed",
			"\t\t-is or -xs <included/excluded suffixes list> - e.g., -is \"*.foo,*.bar\"",
			"\t\t-ip or -xp <included/excluded file patterns list> - e.g. -ip \"Event*File.c??,MyFile.cpp\""
		};
	public static final void showUsage (final PrintStream out, final int exitStatus)
	{
		for (final String	u : USAGE)
			out.println(u);
		if (exitStatus != 0)
			System.exit(exitStatus);
	}

	protected void popupUsage ()
	{
		final StringBuilder	sb=new StringBuilder(USAGE.length * 64);
		for (final String	u : USAGE)
			sb.append(u).append("\r\n");
		JOptionPane.showMessageDialog(this, sb.toString(), "Command line arguments", JOptionPane.INFORMATION_MESSAGE);
	}

	private Class<?>	_commentClass;
	public boolean isTextFileComment ()
	{
		return (_commentClass != null) && File.class.isAssignableFrom(_commentClass);
	}
	// meaning depends on the "isTextFileComment" value
	private String	_commentContent;
	public String getCommentContent ()
	{
		return _commentContent;
	}

	private String	_backupSuffix;
	public String getBackupSuffix ()
	{
		return _backupSuffix;
	}

	private File createBackupFile (final PrintStream out, final File f) throws IOException
	{
		final String	bkpSuffix=getBackupSuffix();
		if ((null == bkpSuffix) || (bkpSuffix.length() <= 0))
			return null;

		if ((null == f) || (!f.exists())|| (!f.isFile()) || (f.length() <= 0L))
			return null;

		final String	bkpPath=f.getAbsolutePath() + FileUtil.adjustExtension(bkpSuffix, true);
		final File		bkpFile=new File(bkpPath);
		if (bkpFile.exists())
		{
			if (!bkpFile.delete())
				System.err.println("Failed to delete backup file=" + bkpPath);
			else
				out.append("Deleted old backup file=")
				   .append(bkpPath)
				   .println()
				   ;
		}

		final long	cpyLen=IOCopier.copyFile(f, bkpFile);
		if (cpyLen < 0)
			throw new StreamCorruptedException("Failed (err=" + cpyLen + ") to copy file");

		return bkpFile;
	}

	private static final String findMatchingPattern (final String n, final Collection<? extends Pattern> pl)
	{
		if ((null == n) || (n.length() <= 0))
			return n;

		if ((null == pl) || (pl.size() <= 0))
			return null;

		for (final Pattern p : pl)
		{
			final Matcher	m=(null == p) ? null : p.matcher(n);
			if ((m != null) && m.matches())
				return p.toString();
		}

		return null;
	}

	private Collection<Pattern>	_excludedPatterns;
	public String getExcludedNamePatternMatch (final String n)
	{
		return findMatchingPattern(n, _excludedPatterns);
	}

	private Collection<Pattern>	_includedPatterns;
	public String getIncludedNamePatternMatch (final String n)
	{
		if ((null == _includedPatterns) || (_includedPatterns.size() <= 0))
			return n;

		return findMatchingPattern(n, _includedPatterns);
	}

	private static final String findMatchingFileSuffix (final String v, final Collection<String> sl)
	{
		final String	e=FileUtil.adjustExtension(v, true);
		if ((null == e) || (e.length() <= 0))
			return e;

		if ((null == sl) || (sl.size() <= 0))
			return null;

		return CollectionsUtils.findElement(sl, e, String.CASE_INSENSITIVE_ORDER);
	}

	private Collection<String>	_excludedSuffixes;	
	public String getExcludedFileSuffixMatch (final String v)
	{
		return findMatchingFileSuffix(v, _excludedSuffixes);
	}

	private Collection<String>	_includedSuffixes;	
	public String getIncludedFileSuffixMatch (final String v)
	{
		if ((null == _includedSuffixes) || (_includedSuffixes.size() <= 0))
			return v;

		return findMatchingFileSuffix(v, _includedSuffixes);
	}

	private boolean	_running	/* =false */;
	protected boolean isRunningMode ()
	{
		return _running;
	}

	public void updateFolderComment (final PrintStream out, final File f)
	{
		if (!isRunningMode())
		{
			out.append("Skip ")
			   .append(f.getAbsolutePath())
			   .append(" - stopped by user request")
			   .println()
			   ;
			return;
		}

		final String	n=f.getName();
		if ((n != null) && (n.length() > 0) && (n.charAt(0) == '.'))
		{
			out.append("Skip ")
			   .append(f.getAbsolutePath())
			   .append(" - ignored (hidden folder)")
			   .println()
			   ;
			return;
		}

		out.append("Entering folder ")
		   .append(f.getAbsolutePath())
		   .println()
		   ;

		final File[]	fa=f.listFiles();
		if ((fa != null) && (fa.length > 0))
		{
			for (final File ff : fa)
				updateFileComment(out, ff);
		}

		out.append("Exiting folder ")
		   .append(f.getAbsolutePath())
		   .println()
		   ;
	}

	public void updateFileComment (final PrintStream out, final File f)
	{
		if (!isRunningMode())
		{
			out.append("Skip ")
			   .append(f.getAbsolutePath())
			   .append(" - stopped by user request")
			   .println()
			   ;
			return;
		}

		if (f.isDirectory())
		{
			updateFolderComment(out, f);
			return;
		}
		else if (!f.isFile())
		{
			System.err.println("Failed to process " + f + " - not a file or a folder");
			return;
		}

		final String	n=f.getName(), pr=getIncludedNamePatternMatch(n);
		if ((null == pr) || (pr.length() <= 0))
		{
			out.append("Skip ")
		       .append(f.getAbsolutePath())
		       .append(" - not included (file name pattern=")
		       .append(n)
		       .append(')')
		       .println()
		       ;
			return;
		}
		else
		{
			final String	p=getExcludedNamePatternMatch(n);
			if ((p != null) && (p.length() > 0))
			{
				out.append("Skip ")
				    .append(f.getAbsolutePath())
				    .append(" - excluded (file name pattern=")
				    .append(p)
				    .append(')')
				    .println()
				    ;
				return;
			}
		}

		final int	nLen=(null == n) ? 0 : n.length(),
					sPos=(nLen <= 0) ? (-1) : n.lastIndexOf('.');
		if ((sPos < 0) || (sPos >= (nLen-1)))
		{
			out.append("Skip ")
			   .append(f.getAbsolutePath())
			   .append(" - no suffix")
			   .println()
			   ;
			return;
		}

		final String	ext=n.substring(sPos), er=getIncludedFileSuffixMatch(ext);
		if ((null == er) || (er.length() <= 0))
		{
			out.append("Skip ")
		       .append(f.getAbsolutePath())
		       .append(" - not included (extension pattern=")
		       .append(ext)
		       .append(')')
		       .println()
		       ;
			return;
		}
		else
		{
			final String	p=getExcludedFileSuffixMatch(ext);
			if ((p != null) && (p.length() > 0))
			{
				out.append("Skip ")
			       .append(f.getAbsolutePath())
			       .append(" - excluded (extension pattern=")
			       .append(p)
			       .append(')')
			       .println()
			       ;
				return;
			}
		}

		final Map<String,? extends Commenter>	cmMap=getCommentersMap();
		final Commenter							c=
			((null == cmMap) || (cmMap.size() <= 0)) ? null : cmMap.get(ext);
		if (null == c)
		{
			out.append("Skip ")
			   .append(f.getAbsolutePath())
			   .append(" - ignored (no commenter for extension=")
			   .append(ext)
			   .append(')')
			   .println()
			   ;
			return;
		}

		out.append("Adding comment to ")
		   .append(f.getAbsolutePath())
		   .println()
		   ;

		BufferedReader	r=null;
		Writer	w=null;
		try
		{
			final File	bf=createBackupFile(out, f);
			if (bf != null)
			{
				out.append("Created backup file=")
				   .append(bf.getAbsolutePath())
				   .println()
				   ;
				
				r = new BufferedReader(new FileReader(bf), IOCopier.DEFAULT_COPY_SIZE);
			}
			else
			{
				final String	fc=FileIOUtils.readFileAsString(f);
				r = new BufferedReader(new StringReader(fc), IOCopier.DEFAULT_COPY_SIZE);
			}

			w = new BufferedWriter(new FileWriter(f), IOCopier.DEFAULT_COPY_SIZE);

			c.addComment(r, w, getCommentContent(), isTextFileComment());
		}
		catch(IOException e)
		{
			System.err.println("\t" + e.getClass().getName() + ": " + e.getMessage());
		}
		finally
		{
			try
			{
				FileUtil.closeAll(r, w);
			}
			catch(IOException e)
			{
				System.err.println("\t" + e.getClass().getName() + " on close: " + e.getMessage());
			}
		}
		
		out.append("Processed file ")
		   .append(f.getAbsolutePath())
		   .println()
		   ;
	}

	private static final Collection<String> createSuffixesList (final Collection<String> cur, final String aName, final String aValue)
	{
		if ((cur != null) && (cur.size() > 0))
			throw new IllegalStateException("Option " + aName + " (or equivalent) re-specified");

		final Collection<String>	sl=StringUtil.splitString(aValue, ',');
		if ((null == sl) || (sl.size() <= 0))
			throw new IllegalStateException("No value specified for option " + aName);

		final Collection<String>	rl=new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		for (final String v : sl)
		{
			if ((null == v) || (v.length() <= 2)
			 || (v.charAt(0) != '*')
			 || (v.charAt(1) != '.'))
				throw new IllegalArgumentException("Bad suffix (" + v + ") in option " + aName);
			if (!rl.add(v.substring(1)))
				continue;	// debug breakpoint
		}

		return rl;
	}

	private static final List<Pattern> createPatternsList (final Collection<? extends Pattern> cur, final String aName, final String aValue)
	{
		if ((cur != null) && (cur.size() > 0))
			throw new IllegalStateException("Option " + aName + " (or equivalent) re-specified");

		final Collection<String>	sl=StringUtil.splitString(aValue, ',');
		if ((null == sl) || (sl.size() <= 0))
			throw new IllegalStateException("No value specified for option " + aName);

		final List<Pattern>	pl=new ArrayList<Pattern>(sl.size());
		for (final String v : sl)
		{
			final Pattern	p=
				((null == v) || (v.length() <= 0)) ? null : Pattern.compile(v);
			if (null == p)
				continue;
			pl.add(p);
		}

		return pl;
	}

	private boolean _guiAllowed=true;
	public boolean isGUIAllowed ()
	{
		return _guiAllowed;
	}

	private Collection<String>	_filePaths;
	private void processMainArguments (final String ... args) throws IllegalArgumentException
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		int	aIndex=0;
		for ( ; aIndex < numArgs; aIndex++)
		{
			final String	a=args[aIndex];
			if ((null == a) || (a.length() <= 0))
				throw new IllegalArgumentException("Null/empty option specified");

			if ("-t".equals(a) || "-f".equals(a))
			{
				aIndex++;

				if (aIndex >= numArgs)
					throw new IllegalArgumentException("Missing " + a + " option argument");

				if (_commentClass != null)
					throw new IllegalStateException("Option " + a + " (or equivalent) re-specified");
	
				if ((null == (_commentContent=args[aIndex])) || (_commentContent.length() <= 0))
					throw new IllegalArgumentException("No " + a + " option value specified");
					
				_commentClass = "-t".equals(a) ? String.class : File.class;
			}
			else if ("-b".equals(a))
			{
				aIndex++;

				if (aIndex >= numArgs)
					throw new IllegalArgumentException("Missing " + a + " option argument");

				if ((_backupSuffix != null) || (_backupSuffix.length() <= 0))
					throw new IllegalStateException("Option " + a + " (or equivalent) re-specified");

				if ((null == (_backupSuffix = args[aIndex])) || (_backupSuffix.length() <= 0))
					throw new IllegalArgumentException("No " + a + " option value specified");
			}
			else if ("-is".equals(a) || "-xs".equals(a))
			{
				aIndex++;

				if (aIndex >= numArgs)
					throw new IllegalArgumentException("Missing " + a + " option argument");

				if ('i' == a.charAt(1))
					_includedSuffixes = createSuffixesList(_includedSuffixes, a, args[aIndex]);
				else
					_excludedSuffixes = createSuffixesList(_excludedSuffixes, a, args[aIndex]);
			}
			else if ("-ip".equals(a) || "-xp".equals(a))
			{
				aIndex++;

				if (aIndex >= numArgs)
					throw new IllegalArgumentException("Missing " + a + " option argument");

				if ('i' == a.charAt(1))
					_includedPatterns = createPatternsList(_includedPatterns, a, args[aIndex]);
				else
					_excludedPatterns = createPatternsList(_excludedPatterns, a, args[aIndex]);
			}
			else if ("-nogui".equals(a))
			{
				_guiAllowed = false;
			}
			else if (a.charAt(0) == '-')
				throw new NoSuchElementException("Unknown option: " + a);
			else
				break;
		}

		final int	numPaths=(numArgs - aIndex);
		if (numPaths > 0)
		{
			_filePaths = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
			for ( ; aIndex < numArgs; aIndex++)
			{
				final String	a=args[aIndex];
				if ((null == a) || (a.length() <= 0))
					throw new IllegalArgumentException("Null/empty file/path specified");

				if ('-' == a.charAt(0))
					throw new IllegalArgumentException("Unknown option: " + a);

				_filePaths.add(a);
			}
		}
	}

	void run (final PrintStream out)
	{
		if ((null == _commentClass)
		 || (null == _commentContent) || (_commentContent.length() <= 0))
			throw new IllegalStateException("No comment content specified");
				
		final Collection<String>	pl=getFilePaths();
		if ((null == pl) || (pl.size() <= 0))
			throw new IllegalStateException("No files/paths specified");

		for (final String p : pl)
		{
			if (!isRunningMode())
				break;

			updateFileComment(out, new File(p));
		}
	}

	@Override
	public void run ()
	{
		if (isGUIAllowed())
		{
		    _commentClass = ((_commentAsFile != null) && _commentAsFile.isSelected()) ? File.class : String.class;

			final CommentWorker	worker=new CommentWorker(this);
			setRunningMode(true, true);
			worker.execute();
		}
		else
		{
			_running = true;
			run(System.out);
		}
	}

	private JLabel	_statusBar	/* =null */;
	public void updateStatusBar (final String text)
	{
		if (_statusBar != null)
			_statusBar.setText((null == text) ? "" : text);
	}

	private JMenuItem		_loadMenuItem	/* =null */;
	private AbstractButton	_loadBtn, _runBtn, _stopBtn;
	protected void updateRunButton (final boolean enable)
	{
		if ((_runBtn != null) && (!isRunningMode()) && (_runBtn.isEnabled() != enable))
			_runBtn.setEnabled(enable);
	}

	private void setUIRunningMode (final boolean running)
	{
		AttrUtils.setComponentEnabledState(!running, _loadMenuItem, _loadBtn);
		final Cursor	c=running
				? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
				: Cursor.getDefaultCursor()
				;
		if (c != null)
			setCursor(c);

		if (_stopBtn != null)
		{
			_stopBtn.setEnabled(running);
			_stopBtn.setVisible(running);
		}

		if (_runBtn != null)
		{
			_runBtn.setEnabled(!running);
			_runBtn.setVisible(!running);
		}
	}

	void setRunningMode (boolean running, boolean updateUI)
	{
		if ((_running != running) || updateUI)
		{
			if (updateUI)
				setUIRunningMode(running);
	
			_running = running;
		}
	}

	private ActionListener	_runExec;
	private synchronized ActionListener getRunExecutionListener ()
	{
		if (null == _runExec)
			_runExec = new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e)
				{
					if (e != null) {
						run();
					}
				}
			};
		return _runExec;
	}

	private static final String	RUN_CMD="run", STOP_CMD="stop";
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
		lm.put(LOAD_CMD, getLoadFileListener());
		lm.put(EXIT_CMD, getExitActionListener());
		lm.put(ABOUT_CMD, getShowManifestActionListener());
		lm.put(RUN_CMD, getRunExecutionListener());
		lm.put("usage", new ActionListener() {
				/*
				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
				 */
				@Override
				public void actionPerformed (ActionEvent e)
				{
					if (e != null)
						popupUsage();
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
		_loadMenuItem = (null == im) ? null : im.get(LOAD_CMD);
		return im;
	}

	private static Collection<String> getFilePaths (final DefaultListModel model, final boolean asSet)
	{
    	final int					mSize=(null == model) ? 0 : model.getSize();
    	final Collection<String>	curPaths=
    		  asSet
    		? new TreeSet<String>(String.CASE_INSENSITIVE_ORDER)
    		: new ArrayList<String>(Math.max(5, mSize))
    		;
    	for (int	mIndex=0; mIndex < mSize; mIndex++)
    	{
    		final Object	o=model.get(mIndex);
    		final String	p=(null == o) ? null : o.toString();
    		if ((null == p) || (p.length() <= 0))
    			continue;
    		
    		curPaths.add(p);
    	}
    	return curPaths;
	}

	private static Collection<String> getFilePaths (final JList l, final boolean asSet)
	{
    	final ListModel	m=(null == l) ? null : l.getModel();
    	if (!(m instanceof DefaultListModel))
    		return null;
    	return getFilePaths((DefaultListModel) m, asSet);
	}

	private JList	_pathsList;
	private Collection<String> getFilePaths ()
	{
		return getFilePaths(_pathsList, false);
	}
    /*
     * @see net.community.apps.common.FilesLoadMainFrame#loadFiles(java.lang.String, org.w3c.dom.Element, java.util.List)
     */
    @Override
	public void loadFiles (final String cmd, final Element dlgElement, final List<? extends File> fl)
    {
    	if ((null == fl) || (fl.size() <= 0))
    		return;
 
    	final ListModel	m=(null == _pathsList) ? null : _pathsList.getModel();
    	if (!(m instanceof DefaultListModel))
    		return;

    	final DefaultListModel		model=(DefaultListModel) m;
    	final Collection<String>	curPaths=getFilePaths(model, true);
    	for (final File f : fl)
    	{
    		final String	p=(null == f) ? null : f.getAbsolutePath();
    		if ((null == p) || (p.length() <= 0) || curPaths.contains(p))
    			continue;

    		model.addElement(p);
    		curPaths.add(p);
    	}
    }

    private Border setBorder (final JComponent c, final String secName)
    {
    	try
    	{
    		final Map.Entry<Element,Border>	ret=HelperUtils.setBorder(c, this, secName);
    		if (null == ret)
    			return _logger.warnObject("setDefaultBorder(" + secName + ") no definition found", null);

    		final Element	elem=ret.getKey();
    		final Border	b=ret.getValue();
    		if (null == b)
    			_logger.warn("setDefaultBorder(" + secName + ")[" + DOMUtils.toString(elem) + "] no border generated", null);
    		return b;
    	}
    	catch(Exception e)
    	{
    		return _logger.warnObject("setDefaultBorder(" + secName + ") " + e.getClass().getName() + ": " + e.getMessage(), e, null);
    	}
    }

    private LRFieldWithButtonPanel	_commentInput;
    private JCheckBox _commentAsFile;
    private Component layoutCommentInputComponent ()
    {
    	if (null == _commentInput)
    	{
    		_commentInput = new LRFieldWithButtonPanel();
    		_commentInput.setTitle("Browse");
    	}
    	_commentInput.setText(getCommentContent());

    	final JButton	btn=_commentInput.getButton();
    	if (btn != null)
    	{
//    		btn.setVisible(false);
//    		btn.addActionListener($$$);
    	}

    	if (null == _commentAsFile)
    		_commentAsFile = new JCheckBox("File input");
    	_commentAsFile.setSelected(isTextFileComment());
    	// _commentAsFile.addActionListener($$$);

    	final JComponent	p=new JPanel(new GridLayout(0, 1, 0, 0));
    	final Component[]	comps={ _commentInput, _commentAsFile };
    	for (final Component c : comps)
    	{
    		if (null == c)
    			continue;
    		p.add(c);
    	}

    	setBorder(p, "input-comment-border");
    	return p;
    }
   
    private Component layoutFilesListComponent ()
    {
		if (null == _pathsList)
		{
			final DefaultListModel		model=new DefaultListModel();
			if ((_filePaths != null) && (_filePaths.size() > 0))
			{
				for (final String p : _filePaths)
				{
					if ((null == p) || (p.length() <= 0))
						continue;
					model.addElement(p);
				}
				
				_filePaths = null;	// no longer need them
			}

			_pathsList = new JList(model);
		}

		final JComponent	c=new ScrolledComponent<JList>(_pathsList);
		setBorder(c, "files-border");
		return c;
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
     */
    @Override
	public void loadFile (File f, String cmd, Element dlgElement)
    {
    	if (f != null)
    		loadFiles(LOAD_CMD, dlgElement, Arrays.asList(f));
    }
	/*
	 * @see net.community.apps.common.BaseMainFrame#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();

		final Container	ctPane=getContentPane();	
		try
		{
			final JToolBar						b=getMainToolBar();
			final Map<String,AbstractButton>	hm=setToolBarHandlers(b);
			if ((hm != null) && (hm.size() > 0))
			{
				_loadBtn = hm.get(LOAD_CMD);
				_runBtn = hm.get(RUN_CMD);
				_stopBtn = hm.get(STOP_CMD);
			}

			ctPane.add(b, BorderLayout.NORTH);
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}

		final Container	centerPane=new JPanel(new BorderLayout());
		{
			final Object[]	comps={
					BorderLayout.NORTH, layoutCommentInputComponent(),
					BorderLayout.CENTER, layoutFilesListComponent()
				};
			for (int	cIndex=0; cIndex < comps.length; cIndex += 2)
			{
				final Object	pos=comps[cIndex], c=comps[cIndex + 1];
				if (c instanceof Component)
					centerPane.add((Component) c, pos);
			}
		}
		ctPane.add(centerPane, BorderLayout.CENTER);

		_statusBar = new JLabel("Ready");
		ctPane.add(_statusBar, BorderLayout.SOUTH);
	}
	/**
	 * @param args original arguments as received by <I>main</I> entry point
	 * @throws Exception if unable to start main frame and application
	 */
	MainFrame (final String ... args) throws Exception
	{
		super(false /* no auto-init till GUI allowed flag set */, args);

		try
		{
			processMainArguments(args);
		}
		catch(Exception e)
		{
			if (isGUIAllowed())
			{
				getLogger().error(e.getClass().getName() + " while process arguments: " + e.getMessage());
				BaseOptionPane.showMessageDialog(this, e);
			}
			else
			{
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
				e.printStackTrace(System.err);
				showUsage(System.err, (-1));
			}
		}
	}
}
