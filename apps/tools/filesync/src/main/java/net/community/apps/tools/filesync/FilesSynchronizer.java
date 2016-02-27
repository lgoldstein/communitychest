/*
 * 
 */
package net.community.apps.tools.filesync;

import java.io.File;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import javax.swing.SwingWorker;

import net.community.chest.Triplet;
import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;
import net.community.chest.io.file.FileIOUtils;
import net.community.chest.util.datetime.TimeUnits;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 5, 2009 1:01:37 PM
 */
public class FilesSynchronizer extends SwingWorker<Void,Map.Entry<? extends File,? extends File>> implements Callable<Boolean> {
	private static final LoggerWrapper	_logger=WrapperFactoryManager.getLogger(FilesSynchronizer.class);

	private final MainFrame	_frame;
	public final MainFrame getMainFrame ()
	{
		return _frame;
	}

	private final Collection<? extends Map.Entry<? extends File,? extends File>>	_pl;
	public final Collection<? extends Map.Entry<? extends File,? extends File>> getSelectedPairs ()
	{
		return _pl;
	}

	private final FileCmpOptions	_opts;
	public final FileCmpOptions getComparisonOptions ()
	{
		return _opts;
	}

	FilesSynchronizer (
		final MainFrame 														f,
		final Collection<? extends Map.Entry<? extends File,? extends File>>	pl,
		final FileCmpOptions 													opts)
	{
		if ((null == (_frame=f))
		 || (null == (_pl=pl)) || (pl.size() <= 0)
		 || (null == (_opts=opts)))
			throw new IllegalArgumentException("Incomplete arguments");
	}
	/*
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call () throws Exception
	{
		if (isCancelled())
			return Boolean.TRUE;

		return null;
	}

	@SuppressWarnings("unchecked")
	protected void inform (Map.Entry<? extends File,? extends File> fp)
	{
		if (fp != null)
			publish(fp);
	}

	protected Collection<Triplet<File,File,SyncAction>> executeFileSync (
			final Collection<Triplet<File,File,SyncAction>> org,
			final File srcFile, final File dstFile, final FileCmpOptions opts)
	{
		if ((null == srcFile) || (!srcFile.exists()) || (!srcFile.isFile()) || (null == dstFile))
			return org;

		inform(new MapEntryImpl<File,File>(srcFile, dstFile));

		final SyncAction	sa;
		if (dstFile.exists())
		{
			if (!dstFile.isFile())
				return _logger.errorObject("executeFileSync(" + dstFile + ") not a file", org);

			try
			{
				final Boolean	cRes=
					FileIOUtils.compareFileContents(srcFile, dstFile, opts.isCompareFileContents(), TimeUnits.MINUTE.getMilisecondValue());
				if ((cRes != null) && cRes.booleanValue())
				{
					if (_logger.isDebugEnabled())
						_logger.debug("executeFileSync(" + srcFile + ")[" + dstFile + "] skip - same content");
					return org;
				}
			}
			catch(Exception e)
			{
				_logger.error("executeFileSync(" + srcFile + ")[" + dstFile + "] " + e.getClass().getName() + " while check if same content: " + e.getMessage(), e);
			}

			sa = SyncAction.UPDATE;
		}
		else
		{
			final long	l=dstFile.length(), m=dstFile.lastModified();
			if ((l > 0L) || (m > 0L))
			{
				_logger.warn("executeFileSync(" + srcFile + ")[" + dstFile + "] destination appears as N/A");

				if (opts.isIgnoreCorruptedFiles())
					return org;

				try
				{
					final Boolean	cRes=
						FileIOUtils.compareFileContents(srcFile, dstFile, true /* compare contents */, TimeUnits.MINUTE.getMilisecondValue());
					if ((cRes != null) && cRes.booleanValue())
					{
						if (_logger.isDebugEnabled())
							_logger.debug("executeFileSync(" + srcFile + ")[" + dstFile + "] skip - same (corrupted) content");
						return org;
					}
				}
				catch(Exception e)
				{
					_logger.error("executeFileSync(" + srcFile + ")[" + dstFile + "] " + e.getClass().getName() + " while check if corrupted content: " + e.getMessage(), e);
				}
			}

			sa = SyncAction.ADD;
		}

		if (!opts.isTestOnly())
		{
			try
			{
				final long	cpySize=IOCopier.copyFile(srcFile, dstFile);
				if (cpySize < 0L)
					throw new StreamCorruptedException("Error (" + cpySize + ") while copying");
			}
			catch(IOException e)
			{
				return _logger.errorObject("executeFileSync(" + srcFile + " => " + dstFile + ") " + e.getClass().getName() + " while copying: " + e.getMessage(), e, org);
			}
		}

		_logger.info("Copied " + srcFile + " => " + dstFile + " (action=" + sa + ")");

		final Collection<Triplet<File,File,SyncAction>>	ret=
			(null == org) ? new LinkedList<Triplet<File,File,SyncAction>>() : org;
		ret.add(new Triplet<File,File,SyncAction>(srcFile, dstFile, sa));
		return ret;
	}

	protected Collection<Triplet<File,File,SyncAction>> executeDirectorySync (
			final Collection<Triplet<File,File,SyncAction>> org,
			final File srcFolder, final File dstFolder,
			final FileCmpOptions opts,
			final Callable<?> hndlr)
	{
		if ((null == srcFolder) || (!srcFolder.exists()) || (!srcFolder.isDirectory()) || (null == dstFolder))
			return org;

		if ((!dstFolder.exists()) && (!opts.isTestOnly()))
		{
			try
			{
				if (!dstFolder.mkdirs())
					throw new StreamCorruptedException("Failed to create target path");
			}
			catch(IOException e)
			{
				return _logger.errorObject("executeDirectorySync(" + srcFolder + " => " + dstFolder + ") " + e.getClass().getName() + " while create target path: " + e.getMessage(), e, org);
			}
		}

		final File[]								srcs=srcFolder.listFiles();
		Map<String,File>							srcNames=null;
		Collection<Triplet<File,File,SyncAction>>	ret=org;
		if ((srcs != null) && (srcs.length > 0))
		{
			for (final File f : srcs)
			{
				final String	n=(null == f) ? null : f.getName();
				if ((null == n) || (n.length() <= 0))
					continue;

				if (hndlr != null)
				{
					try
					{
						final Object	o=hndlr.call();
						if (o != null)
							return _logger.infoObject("executeDirectorySync(" + srcFolder + " => " + dstFolder + ") stopped (" + o + ") by handler request", ret);
					}
					catch(Exception e)
					{
						_logger.error("executeDirectorySync(" + srcFolder + " => " + dstFolder + ") " + e.getClass().getName() + " while invoke handler: " + e.getMessage(), e);
					}
				}

				final File	dstFile=new File(dstFolder, n);
				if (f.isFile())
					ret = executeFileSync(ret, f, dstFile, opts);
				else if (f.isDirectory())
					ret = executeDirectorySync(ret, f, dstFile, opts, hndlr);
				else
					_logger.warn("executeDirectorySync(" + f + ") unknown file type");

				if (null == srcNames)
					srcNames = new TreeMap<String,File>();

				final File	prev=srcNames.put(n, f);
				if (prev != null)
					_logger.warn("executeDirectorySync(" + f + ")[" + n + "] multiple mappings: " + prev);
			}
		}

		if (dstFolder.isDirectory())
		{
			final File[]	dests=dstFolder.listFiles();
			if ((dests != null) && (dests.length > 0))
			{
				for (final File f : dests)
				{
					final String	n=(null == f) ? null : f.getName();
					if ((null == n) || (n.length() <= 0))
						continue;

					if ((srcNames != null) && (srcNames.size() > 0) && srcNames.containsKey(n))
						continue;

					if (!opts.isTestOnly())
					{
						try
						{
							if (f.isFile())
							{
								if (!f.delete())
									throw new IOException("Failed to delete");
							}
							else
							{
								final Collection<?>	cl=FileUtil.deleteAll(f, true);
								if ((null == cl) || (cl.size() <= 0))
									throw new IOException("Failed to delete");
							}
						}
						catch(IOException e)
						{
							_logger.error("executeDirectorySync(" + srcFolder + " => " + dstFolder + ") " + e.getClass().getName() + " while delete " + f + ": " + e.getMessage(), e);
						}
					}

					_logger.info("delete " + f.getAbsolutePath() + " - not in source folder");
					if (null == ret)
						ret = new LinkedList<Triplet<File,File,SyncAction>>();
					ret.add(new Triplet<File,File,SyncAction>(null, f, SyncAction.REMOVE));
				}
			}
		}
		else
			_logger.warn("executeDirectorySync(" + dstFolder + ") unknown destination file type");

		return ret;
	}
	/*
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground () throws Exception
	{
		final Collection<? extends Map.Entry<? extends File,? extends File>>	pl=getSelectedPairs();
		if ((pl != null) && (pl.size() > 0))
		{
			final FileCmpOptions						opts=getComparisonOptions();
			Collection<Triplet<File,File,SyncAction>> 	acts=null;
			for (final Map.Entry<? extends File,? extends File>	fp : pl)
			{
				final File	srcFolder=(null == fp) ? null : fp.getKey(),
							dstFolder=(null == fp) ? null : fp.getValue();
				if ((null == srcFolder) || (null == dstFolder))
					continue;

				acts = executeDirectorySync(acts, srcFolder, dstFolder, opts, this);
			}
		}

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
			f.doneFilesSynchronizer(this);
	}
	/*
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
	@Override
	protected void process (List<Map.Entry<? extends File,? extends File>> chunks)
	{
		final int										numPairs=
			(null == chunks) ? 0 : chunks.size();
		final Map.Entry<? extends File,? extends File>	fp=
			(numPairs != 1) ? null : chunks.get(0);
		final File										srcFolder=
			(null == fp) ? null : fp.getKey(),
														dstFolder=
			(null == fp) ? null : fp.getValue();
		if ((null == srcFolder) || (null == dstFolder))
			return;

		final MainFrame	f=getMainFrame();
		if (f != null)
			f.updateStatusBar(srcFolder.getAbsolutePath() + " => " + dstFolder.getAbsolutePath());
	}
}
