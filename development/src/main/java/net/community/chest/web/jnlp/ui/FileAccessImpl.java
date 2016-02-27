/*
 * 
 */
package net.community.chest.web.jnlp.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.jnlp.FileContents;
import javax.jnlp.FileOpenService;
import javax.jnlp.FileSaveService;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import net.community.chest.io.IOCopier;
import net.community.chest.ui.helpers.filechooser.SuffixesFileFilter;
import net.community.chest.web.jnlp.FileContentsEmbedder;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 9, 2009 12:50:49 PM
 */
public class FileAccessImpl implements FileOpenService, FileSaveService {
	public FileAccessImpl ()
	{
		super();
	}

	public static final List<FileContents> toFileContents (final Collection<? extends File> files)
	{
		final int	numFiles=(null == files) ? 0 : files.size();
		if (numFiles <= 0)
			return null;	// debug breakpoint

		final List<FileContents>	fl=new ArrayList<FileContents>(numFiles);
		for (final File f : files)
		{
			final FileContents	fc=(null == f) ? null : new FileContentsEmbedder(f);
			if (null == fc)
				continue;
			if (!fl.add(fc))
				continue;	// debug breakpoint
		}

		return fl;
	}

	public static final List<FileContents> toFileContents (final File ... files)
	{
		return ((null == files) || (files.length <= 0)) ? null : toFileContents(Arrays.asList(files)); 
 	}

	protected List<FileContents> showOpenFileDialog (String pathHint, boolean isMultiSelect, String ... extensions)
	{
		final JFileChooser	fc=new JFileChooser(FileSystemView.getFileSystemView());
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setMultiSelectionEnabled(isMultiSelect);

		if ((pathHint != null) && (pathHint.length() > 0))
			fc.setCurrentDirectory(new File(pathHint));

		if ((extensions != null) && (extensions.length > 0))
		{
			final SuffixesFileFilter	f=new SuffixesFileFilter();
			f.addSuffixes(extensions);
			fc.setFileFilter(f);
		}

		final int	nRes=fc.showOpenDialog(null);
		if (nRes != JFileChooser.APPROVE_OPTION)
			return null;

		// can be null if user did not select anything
		if (isMultiSelect)
			return toFileContents(fc.getSelectedFiles());

		final File	f=fc.getSelectedFile();
		if (null == f)
			return null;

		return toFileContents(f);
	}
	/*
	 * @see javax.jnlp.FileOpenService#openFileDialog(java.lang.String, java.lang.String[])
	 */
	@Override
	public FileContents openFileDialog (String pathHint, String[] extensions) throws IOException
	{
		final List<? extends FileContents>	fl=showOpenFileDialog(pathHint, false, extensions);
		final int							numFiles=(null == fl) ? 0 : fl.size();
		if (numFiles <= 0)
			return null;
		if (numFiles != 1)
			throw new StreamCorruptedException("openFileDialog(" + pathHint + ") multiple (" + numFiles + ") selections");

		return fl.get(0);
	}
	/*
	 * @see javax.jnlp.FileOpenService#openMultiFileDialog(java.lang.String, java.lang.String[])
	 */
	@Override
	public FileContents[] openMultiFileDialog (String pathHint, String[] extensions) throws IOException
	{
		final Collection<? extends FileContents>	fl=showOpenFileDialog(pathHint, true, extensions);
		final int									numFiles=(null == fl) ? 0 : fl.size();
		if (numFiles <= 0)
			return null;

		return fl.toArray(new FileContents[numFiles]);
	}

	protected FileContents showFileSaveDialog (String pathHint, String name, String ... extensions) throws IOException
	{
		final JFileChooser	fc=new JFileChooser(FileSystemView.getFileSystemView());
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setMultiSelectionEnabled(false);

		if ((pathHint != null) && (pathHint.length() > 0))
		{
			final File	pathDir=new File(pathHint);
			fc.setCurrentDirectory(pathDir);
			
			if ((name != null) && (name.length() > 0))
				fc.setSelectedFile(new File(pathDir, name));
		}

		if ((extensions != null) && (extensions.length > 0))
		{
			final SuffixesFileFilter	f=new SuffixesFileFilter();
			f.addSuffixes(extensions);
			fc.setFileFilter(f);
		}

		final int	nRes=fc.showSaveDialog(null);
		if (nRes != JFileChooser.APPROVE_OPTION)
			return null;

		// can be null if user did not select anything
		final File							selFile=fc.getSelectedFile();
		final List<? extends FileContents>	fl=
			(null == selFile) ? null : toFileContents(selFile);
		final int							numFiles=(null == fl) ? 0 : fl.size();
		if (numFiles <= 0)
			return null;
		if (numFiles != 1)
			throw new StreamCorruptedException("showFileSaveDialog(" + pathHint + ") multiple (" + numFiles + ") selections");

		return fl.get(0);
	}
	/*
	 * @see javax.jnlp.FileSaveService#saveAsFileDialog(java.lang.String, java.lang.String[], javax.jnlp.FileContents)
	 */
	@Override
	public FileContents saveAsFileDialog (String pathHint, String[] extensions, FileContents fc) throws IOException
	{
		return showFileSaveDialog(pathHint, fc.getName(), extensions);
	}
	/*
	 * @see javax.jnlp.FileSaveService#saveFileDialog(java.lang.String, java.lang.String[], java.io.InputStream, java.lang.String)
	 */
	@Override
	public FileContents saveFileDialog (String pathHint, String[] extensions, InputStream in, String name) throws IOException
	{
		final FileContents	sc=showFileSaveDialog(pathHint, name, extensions);
		if (null == sc)
			return null;

		OutputStream	out=null;
		try
		{
			out = sc.getOutputStream(true);

			final long	cpyLen=IOCopier.copyStreams(in, out);
			if (cpyLen < 0L)
				throw new StreamCorruptedException("saveFileDialog(" + sc + ") error (" + cpyLen + ") while copying");
		}
		finally
		{
			if (out != null)
				out.close();
		}

		return sc;
	}
}
