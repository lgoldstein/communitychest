/*
 * 
 */
package net.community.chest.javaagent.dumper.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import org.w3c.dom.Document;

import net.community.chest.dom.DOMUtils;
import net.community.chest.javaagent.dumper.ui.data.SelectibleClassInfo;
import net.community.chest.javaagent.dumper.ui.data.SelectiblePackageInfo;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 14, 2011 11:38:34 AM
 */
class DumperDataLoaderThread extends SwingWorker<List<SelectiblePackageInfo>,SelectiblePackageInfo> {
	private final SelectiblePackageInfoHandler	_frame;
	private final File	_rootFolder;
	DumperDataLoaderThread (SelectiblePackageInfoHandler frame, File rootFolder)
	{
		_frame = frame;
		_rootFolder = rootFolder;
	}
	/*
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected List<SelectiblePackageInfo> doInBackground () throws Exception
	{
		return updatePackagesList(_rootFolder);
	}

	List<SelectiblePackageInfo> updatePackagesList (File rootFolder)
	{
		return updatePackagesList("", rootFolder, null);
	}

	List<SelectiblePackageInfo> updatePackagesList (
			final String pkgName, final File rootFolder, final List<SelectiblePackageInfo> orgList)
	{
		if (!rootFolder.isDirectory())
			return orgList;

		final File[]	files=rootFolder.listFiles();
		final int		numFiles=(files == null) ? 0 : files.length;
		if (numFiles <= 0)
			return orgList;

		SelectiblePackageInfo		pkgInfo=null;
		List<SelectiblePackageInfo>	retList=orgList;
		for (final File f : files)
		{
			if (f.isDirectory())
			{
				final String	subPkgName=
						((pkgName == null) || (pkgName.length() <= 0)) ? f.getName() : (pkgName + "." + f.getName());
				retList = updatePackagesList(subPkgName, f, retList);
				continue;
			}

			if (!f.isFile())
				continue;

			final SelectibleClassInfo	classInfo;
			try
			{
				final Document	doc=DOMUtils.loadDocument(f);
				classInfo = new SelectibleClassInfo(doc);
			}
			catch(Exception e)
			{
				throw new RuntimeException("Failed (" + e.getClass().getName() + ") to read class data from file=" + f.getAbsolutePath() + ": " + e.getMessage());
			}

			if (pkgInfo == null)
				pkgInfo = new SelectiblePackageInfo(pkgName, numFiles);
			pkgInfo.add(classInfo);

			if (retList == null)
				retList = new ArrayList<SelectiblePackageInfo>();
			retList.add(pkgInfo);
		}

		if (pkgInfo != null)
			publish(pkgInfo);
		return retList;
	}
	/*
	 * @see javax.swing.SwingWorker#process(java.util.List)
	 */
	@Override
	protected void process (List<SelectiblePackageInfo> chunks)
	{
		if ((chunks == null) || (chunks.size() <= 0))
			return;

		for (final SelectiblePackageInfo pkgInfo : chunks)
			_frame.processSelectiblePackageInfo(pkgInfo);
	}
	/*
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done ()
	{
		_frame.doneLoadingDumperData(this);
	}
}
