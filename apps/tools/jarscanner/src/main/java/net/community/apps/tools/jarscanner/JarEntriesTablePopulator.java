package net.community.apps.tools.jarscanner;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import net.community.chest.io.FileUtil;
import net.community.chest.io.jar.JarEntryHandler;
import net.community.chest.io.jar.JarEntryLocation;
import net.community.chest.io.jar.JarUtils;
import net.community.chest.lang.StringUtil;
import net.community.chest.regexp.RegexpUtils;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 22, 2007 11:14:53 AM
 */
public class JarEntriesTablePopulator extends SwingWorker<Void,JarEntryLocation> implements JarEntryHandler {
	private static final LoggerWrapper	_logger=
		WrapperFactoryManager.getLogger(JarEntriesTablePopulator.class);

	private File	_scanDir;
	public File getScanDir ()
	{
		return _scanDir;
	}

	public void setScanDir (File scanDir)
	{
		_scanDir = scanDir;
	}

	private JarEntriesTableModel	_tblModel	/* =null */;
    public JarEntriesTableModel getModel ()
    {
    	return _tblModel;
    }

    public void setModel (JarEntriesTableModel tblModel)
    {
    	_tblModel = tblModel;
    }

    private File	_jarFile	/* =null */;
	public File getJarFile ()
	{
		return _jarFile;
	}

	public void setJarFile (File jarFile)
	{
		_jarFile = jarFile;
	}
	/**
	 * The {@link Pattern}-s used to match JAR entries names - if
	 * <code>null</code>/empty then <U>all</U> entries are marked as matching.
	 */
	private Collection<Pattern>	_incEntryPattern	/* =null */;
	public Collection<Pattern> getIncludedScanPattern ()
	{
		return _incEntryPattern;
	}

	public void setIncludedScanPattern (Collection<Pattern> p)
	{
		_incEntryPattern = p;
	}
	/**
	 * The {@link Pattern}-s used to match JAR entries names - if
	 * <code>null</code>/empty then <U>no</U> entry is excluded
	 */
	private Collection<Pattern>	_excEntryPattern	/* =null */;
	public Collection<Pattern> getExcludedScanPattern ()
	{
		return _excEntryPattern;
	}

	public void setExcludedScanPattern (Collection<Pattern> p)
	{
		_excEntryPattern = p;
	}

	public void setExcludedScanPattern (Pattern... p)
	{
		setExcludedScanPattern(((null == p) || (p.length <= 0)) ? null : Arrays.asList(p));
	}

	private final MainFrame	_frame;
	public final MainFrame getMainFrame ()
	{
		return _frame;
	}
	/**
	 * The {@link Pattern}-s used to <U>exclude</U> JAR files - if
	 * <code>null</code>/empty then <U>none</U> of the files is marked as
	 * matching (i.e., no file is excluded)
	 */
	private Collection<Pattern>	_jarsExcludePattern;
	public Collection<Pattern> getJarsExcludePattern ()
	{
		return _jarsExcludePattern;
	}

	public void setJarsExcludePattern (Collection<Pattern> p)
	{
		_jarsExcludePattern = p;
	}

	public void setJarsExcludePattern (Pattern... p)
	{
		setJarsExcludePattern(((null == p) || (p.length <= 0)) ? null : Arrays.asList(p));
	}
	/**
	 * The {@link Pattern}-s used to <U>exclude</U> JAR files - if
	 * <code>null</code>/empty then <U>all</U> of the files are marked as
	 * matching (i.e., all files are included)
	 */
	private Collection<Pattern>	_jarsIncludePattern;
	public Collection<Pattern> getJarsIncludePattern ()
	{
		return _jarsIncludePattern;
	}

	public void setJarsIncludePattern (Collection<Pattern> p)
	{
		_jarsIncludePattern = p;
	}

	public void setJarsIncludePattern (Pattern... p)
	{
		setJarsIncludePattern(((null == p) || (p.length <= 0)) ? null : Arrays.asList(p));
	}
	/**
	 * The {@link Pattern}-s used to <U>exclude</U> scanned folders - if
	 * <code>null</code>/empty then <U>all</U> of the folders are marked as
	 * non-matching (i.e., all folders are scanned)
	 */
	private Collection<Pattern>	_dirExcludePattern;
	public Collection<Pattern> getDirExcludePattern ()
	{
		return _dirExcludePattern;
	}

	public void setDirExcludePattern (Collection<Pattern> p)
	{
		_dirExcludePattern = p;
	}

	public void setDirExcludePattern (Pattern... p)
	{
		setDirExcludePattern(((null == p) || (p.length <= 0)) ? null : Arrays.asList(p));
	}

	/**
	 * The {@link Pattern}-s used to <U>include</U> scanned folders - if
	 * <code>null</code>/empty then <U>all</U> of the folder are marked as
	 * matching (i.e., all folders are scanned)
	 */
	private Collection<Pattern>	_dirIncludePattern;
	public Collection<Pattern> getDirIncludePattern ()
	{
		return _dirIncludePattern;
	}

	public void setDirIncludePattern (Collection<Pattern> p)
	{
		_dirIncludePattern = p;
	}

	public void setDirIncludePattern (Pattern... p)
	{
		setDirExcludePattern(((null == p) || (p.length <= 0)) ? null : Arrays.asList(p));
	}

	public JarEntriesTablePopulator (MainFrame f)
	{
		if (null == (_frame=f))
			throw new IllegalArgumentException("No " + MainFrame.class.getSimpleName() + " instance provided");
	}

	public void clear ()
	{
		setScanDir(null);
		setModel(null);
		setJarFile(null);
		setIncludedScanPattern(null);
		setJarsExcludePattern((Collection<Pattern>) null);
		setJarsIncludePattern((Collection<Pattern>) null);
		setDirExcludePattern((Collection<Pattern>) null);
		setDirIncludePattern((Collection<Pattern>) null);
	}

	protected boolean isExcludedJarEntry (final String fileName)
	{
		if ((null == fileName) || (fileName.length() <= 0))
			return true;

		final Boolean	v=RegexpUtils.checkPatterns(fileName, getExcludedScanPattern());
		if (null == v)
			return false;

		return v.booleanValue();
	}

	protected boolean isIncludedJarEntry (final String fileName)
	{
		if ((null == fileName) || (fileName.length() <= 0))
			return false;

		final Boolean	v=RegexpUtils.checkPatterns(fileName, getIncludedScanPattern());
		if (null == v)
			return true;

		return v.booleanValue();
	}
	/*
	 * @see net.community.chest.util.jar.JarEntryHandler#handleJAREntry(java.util.jar.JarEntry)
	 */
	@Override
	public int handleJAREntry (final JarEntry je)
	{
		if ((null == je) /* should not happen */ || je.isDirectory())
			return 0;

		final String	jeName=je.getName();
		final int		jnLen=(null == jeName) ? 0 : jeName.length(),
						sPos=
			(jnLen <= 1) ? (-1) : jeName.lastIndexOf('/');
		final String	eName=
			((sPos < 0) || (sPos >= (jnLen-1))) ? jeName : jeName.substring(sPos + 1);
		if ((null == eName) || (eName.length() <= 0))
			return 0;

		if (isExcludedJarEntry(eName))
			return 0;
		if (!isIncludedJarEntry(eName))
			return 0;

		final File		f=getJarFile();
		final String	fPath=(null == f) /* should not happen */ ? null : f.getAbsolutePath();
		if ((null == fPath) || (fPath.length() <= 0))
			return (-1);	// should not happen

		publish(new JarEntryLocation(fPath, je));
		return 0;
	}

	protected boolean isExcludedJar (final String fileName)
	{
		if ((null == fileName) || (fileName.length() <= 0))
			return true;

		final Boolean	v=RegexpUtils.checkPatterns(fileName, getJarsExcludePattern());
		if (null == v)
			return false;

		return v.booleanValue();
	}

	protected boolean isIncludedJar (final String fileName)
	{
		if ((null == fileName) || (fileName.length() <= 0))
			return false;

		final Boolean	v=RegexpUtils.checkPatterns(fileName, getJarsIncludePattern());
		if (null == v)
			return true;

		return v.booleanValue();
	}

	protected boolean isExcludedDir (final String dirName)
	{
		if ((null == dirName) || (dirName.length() <= 0))
			return true;

		final Boolean	v=RegexpUtils.checkPatterns(dirName, getDirExcludePattern());
		if (null == v)
			return false;

		return v.booleanValue();
	}

	protected boolean isIncludedDir (final String dirName)
	{
		if ((null == dirName) || (dirName.length() <= 0))
			return false;

		final Boolean	v=RegexpUtils.checkPatterns(dirName, getDirIncludePattern());
		if (null == v)
			return true;

		return v.booleanValue();
	}

	protected int scanJARs (final File scanDir)
	{
		if ((null == scanDir) || (!scanDir.exists()) || (!scanDir.isDirectory()))
		{
			_logger.error("scanJAR(s) no/bad selected scan directory");
			return (-1);	// should not happen
		}

		final File[]	files=scanDir.listFiles();
		if ((null == files) || (files.length <= 0))
			return 0;

		for (final File f : files)
		{
			if (null == f)	// should not happen
				continue;
			if (isCancelled())
				return (+1);

			final String	filePath=f.getAbsolutePath(), fileName=f.getName();
			if (f.isDirectory())
			{
				if (!isIncludedDir(fileName))
					_logger.info("scanJARs(" + filePath + ")[~included]");
				else if (isExcludedDir(fileName))
					_logger.info("scanJARs(" + filePath + ")[excluded]");
				else
					scanJARs(f);
				continue;
			}

			if (!JarUtils.isJarFile(filePath))
				continue;

			final String	jarName=FileUtil.stripExtension(fileName, JarUtils.JAR_SUFFIX);
			if (isExcludedJar(jarName))
			{
				_logger.info("scanJARs(" + filePath + ")[excluded]");
				continue;
			}

			if (!isIncludedJar(jarName))
			{
				_logger.info("scanJARs(" + filePath + ")[~included]");
				continue;
			}

			setJarFile(f);
			publish(new JarEntryLocation(filePath));

			_logger.info("scanJARs(" + filePath + ") scanning...");
			try
			{
				final int	retCode=JarUtils.enumerateJAREntries(f, this);
				if (retCode != 0)
					return retCode;

				_logger.info("scanJARs(" + filePath + ") done...");
			}
			catch(Exception e)
			{
				final int	retChoice=JOptionPane.showConfirmDialog(getMainFrame(),
						e.getClass().getName() + " while processing file: " + e.getMessage() + " - continue ?",
						filePath,
						JOptionPane.YES_NO_OPTION);
				if (retChoice != JOptionPane.YES_OPTION)
					return (+1);
			}
			finally
			{
				setJarFile(null);
			}
		}

		return 0;
	}
	/*
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground () throws Exception
	{
		final File	scanDir=getScanDir();
		final int	nRes=scanJARs(scanDir);
		if (nRes < 0)
			throw new IllegalStateException("scanJars(" + scanDir + ") bad error code: " + nRes);

		return null;
	}
	/*
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done ()
	{
		final MainFrame	f=getMainFrame();
		if (f != null)
			f.signalSearchDone(this);
	}
	/*
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
	@Override
	protected void process (List<JarEntryLocation> chunks)
	{
		final JarEntriesTableModel	m=
			((null == chunks) || (chunks.size() <= 0)) ? null : getModel();
		if (null == m)	// should not happen
			return;

		final File		scanDir=getScanDir();
		final String	scanPath=(null == scanDir) ? null : scanDir.getAbsolutePath();
		for (final JarEntryLocation	jl : chunks)
		{
			final String	jarPath=(null == jl) ? null : jl.getKey();
			if ((null == jarPath) || (jarPath.length() <= 0))
				continue;

			final JarEntry	je=jl.getValue();
			if (null == je)
			{
				final MainFrame	f=getMainFrame();
				if (f != null)
					f.setText(jarPath);
			}
			else
			{
				// remove the scan dir prefix and show only relative path
				if (StringUtil.startsWith(jarPath, scanPath, true, true))
					jl.setKey(jarPath.substring(scanPath.length() + 1));
				m.add(jl);
			}
		}
	}
}
