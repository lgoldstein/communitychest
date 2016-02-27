package net.community.chest.apache.ant.mvnsync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.apache.ant.helpers.ExtendedTask;
import net.community.chest.apache.ant.mvnsync.helpers.Dependency;
import net.community.chest.apache.ant.mvnsync.helpers.LocalRepository;
import net.community.chest.apache.ant.mvnsync.helpers.RemoteRepository;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 30, 2008 2:33:52 PM
 */
public class MvnSync extends ExtendedTask {
	public MvnSync ()
	{
		super();
	}

	private LocalRepository	_repLocal	/* =null */;
	public LocalRepository getLocalRepository ()
	{
		return _repLocal;
	}

	public void setLocalRepository (LocalRepository repLocal)
	{
		if (_repLocal != null)
			throw new IllegalStateException("setLocalRepository(" + repLocal + ") already set to: " + _repLocal);
		_repLocal = repLocal;
	}
    /* NOTE !!! the name of this method MUST be "addConfiguredLocalRepository",
     * 		otherwise the <localRepository> sub-element is NOT handled
     * 		correctly by the ANT parser
     */
	public void addConfiguredLocalRepository (LocalRepository repLocal)
	{
		setLocalRepository(repLocal);
	}

	private Collection<RemoteRepository>	_remReps	/* =null */;	
    /* NOTE !!! the name of this method MUST be "addConfiguredRemoteRepository",
     * 		otherwise the <remoteRepository> sub-element(s) are NOT handled
     * 		correctly by the ANT parser
     */
	public void addConfiguredRemoteRepository (RemoteRepository repRemote)
	{
		if (null == _remReps)
			_remReps = new LinkedList<RemoteRepository>();
		_remReps.add(repRemote);

		// TODO if not ref-id then map it to some static Map FFU
	}

	private Collection<Dependency>	_deps	/* =null */;
	public void addConfiguredDependency (Dependency d)
	{
		if (null == _deps)
			_deps = new LinkedList<Dependency>();
		_deps.add(d);
	}
	/**
	 * Target directory to which the JAR(s) are to be downloaded. This is
	 * also the default for sources, javadoc(s) and POM files if not
	 * otherwise overridden 
	 */
	private File	_toDir	/* =null */;
	public synchronized File getToDir ()
	{
		if (null == _toDir)
			_toDir = new File(_repLocal.getPath());

		return _toDir;
	}

	public synchronized void setToDir (File toDir)
	{
		_toDir = toDir;
	}
	/**
	 * Location to download sources - default=same as {@link #getToDir()} 
	 */
	private File	_srcDir	/* =null */;
	public File getSrcDir ()
	{
		if (null == _srcDir)
			return getToDir();
		else
			return _srcDir;
	}

	public void setSrcDir (File srcDir)
	{
		_srcDir = srcDir;
	}
	/**
	 * Location to download POM files to - default=same as {@link #getToDir()} 
	 */
	private File	_pomDir	/* =null */;
	public File getPomDir ()
	{
		return _pomDir;
	}

	public void setPomDir (File pomDir)
	{
		_pomDir = pomDir;
	}
    /**
     * Sets verbose mode (default=false)
     * @param verbose TRUE if required to display internal workings
     */
    public void setVerbose (final boolean verbose)
    {
        if (verbose)
        	setVerbosity(Project.MSG_INFO);
        else
        	setVerbosity(Project.MSG_VERBOSE);
    }

    private static final Map<String,File> buildTargetsMap (final File dir)
    {
    	final File[]	files=dir.listFiles();
    	if ((null == files) || (files.length <= 0))
    		return null;

    	final Map<String,File>	res=new TreeMap<String,File>();
    	for (final File f : files)
    		res.put(f.getName(), f);
    	return res;
    }

    private static final Map<String,Dependency> buildDependenciesMap (final Collection<? extends Dependency> deps)
    {
    	if ((null == deps) || (deps.size() <= 0))
    		return null;

    	final Map<String,Dependency>	res=new TreeMap<String,Dependency>();
    	for (final Dependency d : deps)
    		res.put(d.getTargetJarFileName(), d);
    	return res;
    }
    /**
     * TRUE (default)=stop the execution with {@link BuildException} if failed
     * to download a dependent file. FALSE=warn to go on 
     */
    private boolean	_failIfNotFound=true;
	public boolean isFailIfNotFound ()
	{
		return _failIfNotFound;
	}

	public void setFailIfNotFound (boolean failIfNotFound)
	{
		_failIfNotFound = failIfNotFound;
	}
	/**
	 * Timeout (msec.) for connecting to remote repository when attempting to
	 * download a file 
	 */
	private int	_connectTimeout=5 * 1000;
	public int getConnectTimeout ()
	{
		return _connectTimeout;
	}

	public void setConnectTimeout (int connectTimeout)
	{
		_connectTimeout = connectTimeout;
	}
	/**
	 * Timeout (msec.) when reading the data from a remote repository 
	 */
	private int	_readTimeout=30 * 1000;
	public int getReadTimeout ()
	{
		return _readTimeout;
	}

	public void setReadTimeout (int readTimeout)
	{
		_readTimeout = readTimeout;
	}
	/**
	 * Size (KB) for copying the downloaded data
	 */
	private int	_copyBufSize=4;
	public int getCopyBufSize ()
	{
		return _copyBufSize;
	}

	public void setCopyBufSize (final int copyBufSize)
	{
		if (((_copyBufSize=copyBufSize) < 0) || (copyBufSize > Short.MAX_VALUE))
			throw new IllegalArgumentException("setCopyBufSize(" + copyBufSize + ") bad value");
	}
	/**
	 * TRUE=attempt to preserve the last-modified-time value of the downloaded file
	 */
	private boolean	_preserveLastModified;
	public boolean isPreserveLastModified ()
	{
		return _preserveLastModified;
	}

	public void setPreserveLastModified (boolean preserveLastModified)
	{
		_preserveLastModified = preserveLastModified;
	}

	private byte[]	_workBuf	/* =null */;
	private synchronized byte[] getWorkBuf ()
	{
		if (null == _workBuf)
		{
			final int	kbSize=getCopyBufSize();
			if (kbSize <= 0)
				throw new IllegalStateException("Bad/Illegal copy buffer size: " + kbSize);
			_workBuf = new byte[kbSize * 1024];
		}

		return _workBuf;
	}

	private long downloadDependency (final URLConnection conn, final File tgtFile) throws IOException
	{
		InputStream		in=null;
		OutputStream	out=null;
		try
		{
			conn.connect();

			final String	xferCode=conn.getContentEncoding();
			// TODO handle BASE64 encoding
			if ("base64".equalsIgnoreCase(xferCode))
				throw new UnsupportedOperationException("downloadDependency(" + tgtFile + ") unsupported encoding: " + xferCode);

			if (null == (in=conn.getInputStream()))
				throw new StreamCorruptedException("downloadDependency(" + tgtFile + ") no input stream");

			final File	parDir=tgtFile.getParentFile();
			if (!parDir.exists())
			{
				if (!parDir.mkdirs())
					throw new StreamCorruptedException("downloadDependency(" + tgtFile + ") cannot create parent folder");
    			if (isVerboseMode())
    				log("downloadDependency(" + tgtFile + ")  created parent folder", getVerbosity());
			}

			out = new FileOutputStream(tgtFile);

			final byte[]	workBuf=getWorkBuf();
			long			curSize=0L, maxSize=conn.getContentLength();
			if (maxSize <= 0L)
				maxSize = Long.MAX_VALUE - Short.MAX_VALUE;
			if (isVerboseMode())
				log("downloadDependency(" + tgtFile + ") downloading from " + conn.getURL() + " (max. size=" + maxSize + " bytes)", getVerbosity());

			for (int    rLen=0, nCurPos=0, nRemLen=workBuf.length; (rLen != (-1)) && (curSize < maxSize); nCurPos=0, nRemLen=workBuf.length)
			{
				// fill work buffer as much as possible from the input stream
	            for (rLen=in.read(workBuf, nCurPos, nRemLen); (rLen != (-1)) && (curSize < maxSize) && (nRemLen > 0); rLen=in.read(workBuf, nCurPos, nRemLen))
	            {
	                nRemLen -= rLen;
		            nCurPos += rLen;
		            curSize += rLen;
	            }

	            out.write(workBuf, 0, nCurPos);
    			if (isVerboseMode())
    				log("downloadDependency(" + tgtFile + ") downloaded " + curSize + " out of " + maxSize + " from " + conn.getURL(), getVerbosity());
			}

			// set the downloaded file value accordingly if 'preserveLastModified'=true
			if (isPreserveLastModified())
			{
				final long	lastModTime=conn.getLastModified();
				if (lastModTime != 0L)
				{
					if (!tgtFile.setLastModified(lastModTime))
						throw new StreamCorruptedException("downloadDependency(" + tgtFile + ") failed to set last-modified-time=" + lastModTime);
	    			if (isVerboseMode())
	    				log("downloadDependency(" + tgtFile + ") updated last-modify-time=" + lastModTime, getVerbosity());
				}
			}

			return curSize;
		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				finally
				{
					in = null;
				}
			}

			if (out != null)
			{
				try
				{
					out.close();
				}
				finally
				{
					out = null;
				}
			}
		}
	}

	private URLConnection prepareRemoteConnection (final String srcPath) throws IOException
	{
		final URL			url=new URL(srcPath);
		final URLConnection	conn=url.openConnection();
		conn.setAllowUserInteraction(false);
		conn.setConnectTimeout(getConnectTimeout());
		conn.setReadTimeout(getReadTimeout());
		return conn;
	}
	// returns non-negative value if successful
	private long downloadDependency (final Dependency d, final RemoteRepository r, final File tgtFile) throws IOException
	{
		// TODO add support for refid repository
		// TODO add support for systemPath dependency
		final String		srcPath=r.getDependencyFilePath(d, tgtFile.getName());
		final URLConnection	conn=prepareRemoteConnection(srcPath);
		final long	dldStart=System.currentTimeMillis(),
					fileSize=downloadDependency(conn, tgtFile),
					dldEnd=System.currentTimeMillis(), dldDuration=dldEnd - dldStart;
		if (fileSize > 0L)
		{
			log("Downloaded " + tgtFile + " (" + fileSize + " bytes) in " + dldDuration + " msec. from " + srcPath, Project.MSG_INFO);
			return dldDuration;
		}

		return (0L == fileSize) ? Long.MIN_VALUE : fileSize;
	}

	private File downloadDependency (final File tgtFile, final Dependency d, final Collection<? extends RemoteRepository> reps)
	{
		if ((null == d) || (null == reps) || (reps.size() <= 0))
			return null;

		log("Attempting to download " + tgtFile, Project.MSG_INFO);
		for (final RemoteRepository r : reps)
		{
			try
			{
				final long	dldDuration=downloadDependency(d, r, tgtFile);
				if (dldDuration >= 0L)
					return tgtFile;
			}
			catch(IOException e)
			{
				log(e.getClass().getName() + " while attempting to download from " + r + ": " + e.getMessage(), Project.MSG_WARN);
			}
		}

		return tgtFile;
	}

	private Collection<File> downloadMissingFiles (final Map<String,? extends Dependency> depMap, final Map<String,? extends File> tgtMap)
    {
    	final Collection<? extends Map.Entry<String,? extends Dependency>>	depEntries=
    			((null == depMap) || (depMap.size() <= 0)) ? null : depMap.entrySet();
    	if ((null == depEntries) || (depEntries.size() <= 0))
    		return null;

    	Collection<File>	res=null;
    	for (final Map.Entry<String,? extends Dependency> de : depEntries)
    	{
    		final String		tgtName=de.getKey();
    		final Dependency	d=de.getValue();
    		File				tgtFile=((null == tgtMap) || (tgtMap.size() <= 0)) ? null : tgtMap.get(tgtName);
    		if (tgtFile != null)
    		{
    			if (isVerboseMode())
    				log("Found " + tgtName + " (" + d + ")", getVerbosity());
    			continue;
    		}

    		if ((null == (tgtFile=downloadDependency(new File(getToDir(), tgtName), d, _remReps)))
    		 || (!tgtFile.exists())
    		 || (!tgtFile.isFile())
    		 || (tgtFile.length() <= 0L))
    		{
    			if (isFailIfNotFound())
    				throw new BuildException("downloadMissingFile(" + tgtName + ") failed to download", getLocation());

    			log("Failed to download " + tgtName + "(" + d + ")", Project.MSG_WARN);
    			continue;
    		}

    		if (null == res)
    			res = new LinkedList<File>();
    		res.add(tgtFile);
    	}

    	return res;
    }
	/**
	 * TRUE (default)=delete any files in the target directory that are
	 * not specified as a dependancy
	 */
	private boolean	_delUnrefFiles=true;
	public boolean isDelUnrefFiles ()
	{
		return _delUnrefFiles;
	}

	public void setDelUnrefFiles (boolean delUnrefFiles)
	{
		_delUnrefFiles = delUnrefFiles;
	}

	private Collection<File> deleteUnreferencedFiles (final File tgtDir, final Collection<? extends Dependency> deps)
	{
		final File[]	files=tgtDir.listFiles();
		if ((null == files) || (files.length <= 0))
			return null;	// unexpected

		Collection<File>	res=null;
		for (final File f : files)
		{
			if ((null == f) || (f.isDirectory()))
				continue;	// ignore directories

			final String	tgtName=f.getName();
			if ((deps != null) && (deps.size() > 0))
			{
				boolean	isDependencyFile=false;
				for (final Dependency d : deps)
				{
					if ((d != null) && (d.isDependencyFile(tgtName)))
					{
						isDependencyFile = true;
						break;
					}
				}

				if (isDependencyFile)
				{
	    			if (isVerboseMode())
	    				log("deleteUnreferencedFiles(" + tgtName + ") found/skipped", getVerbosity());
					continue;
				}
			}

			if (f.exists())
			{
				if (!f.delete())
				{
					log("deleteUnreferencedFiles(" + tgtName + ") failed to delete file=" + f, Project.MSG_WARN);
					continue;
				}

				if (null == res)
					res = new LinkedList<File>();
				res.add(f);

				log("deleteUnreferencedFiles(" + tgtName + ") deleted file=" + f, Project.MSG_INFO);
			}
		}

		return res;
	}
	/**
	 * TRUE=attempt to download sources as well 
	 */
	private boolean	_downloadSources	/* =false */;
	public boolean isDownloadSources ()
	{
		return _downloadSources;
	}

	public void setDownloadSources (boolean downloadSources)
	{
		_downloadSources = downloadSources;
	}

	private Collection<File> downloadSourceFiles (final File tgtDir, final Collection<? extends Dependency>	deps)
	{
		if ((null == tgtDir) || (null == deps) || (deps.size() <= 0))
			return null;

		Collection<File>	ret=null;
		for (final Dependency d : deps)
		{
			final String	srcFileName=(null == d) ? null : d.getTargetSourcesFileName();
			if ((null == srcFileName) || (srcFileName.length() <= 0))
				continue;

			File	tgtFile=new File(tgtDir, srcFileName);
			if (tgtFile.exists() && (tgtFile.length() > 0L))
			{
    			if (isVerboseMode())
    				log("downloadSourceFiles(" + tgtFile + ") found/skipped", getVerbosity());
				continue;
			}

			if ((null == (tgtFile=downloadDependency(tgtFile, d, _remReps)))
    		 || (!tgtFile.exists())
    		 || (!tgtFile.isFile())
    		 || (tgtFile.length() <= 0L))
    		{
    			log("Failed to download " + srcFileName + "(" + d + ")", Project.MSG_WARN);
    			continue;
    		}

			if (null == ret)
				ret = new LinkedList<File>();
			ret.add(tgtFile);
		}

		return ret;
	}

	private Collection<Dependency> findMissingDependencies (final File pomFile, final File[] jarFiles, final Collection<? extends Dependency> deps)
	{
		if ((null == deps) || (deps.size() <= 0))
			return null;

		Collection<Dependency>	ret=null;
		for (final Dependency d : deps)
		{
			if (null == d)
				continue;

			final File	jarFile=d.findBestMatchingJar(jarFiles);
			if (jarFile != null)
			{
    			if (isVerboseMode())
    				log("findMissingDependencies(" + d + ")[" + pomFile + "] found " + jarFile, getVerbosity());
				continue;
			}

			if (null == ret)
				ret = new LinkedList<Dependency>();
			ret.add(d);

			log("findMissingDependencies(" + d + ")[" + pomFile + "] no match found", Project.MSG_WARN);

			final String	jarName=d.getArtifactId();
			if ((null == jarName) || (jarName.length() <= 0))
				continue;
		}

		return ret;
	}

	private Collection<Dependency> checkDependencies (final File[] jarFiles, final Collection<? extends File> poms)
	{
		if ((null == poms) || (poms.size() <= 0))
			return null;

		Collection<Dependency>	ret=null;
		for (final File f : poms)
		{
			final Collection<? extends Dependency>	deps;
			try
			{
				deps = Dependency.loadPomDependencies(f);
			}
			catch(Exception e)
			{
				log("checkDependencies(" + f + ") " + e.getClass().getName() + ": " + e.getMessage(), Project.MSG_WARN);
				continue;
			}

			final Collection<? extends Dependency>	missingDeps=findMissingDependencies(f, jarFiles, deps);
			if ((null == missingDeps) || (missingDeps.size() <= 0))
			{
    			if (isVerboseMode())
    				log("checkDependencies(" + f + ") found all dependencies", getVerbosity());
				continue;
			}

			if (null == ret)
				ret = new LinkedList<Dependency>();
			ret.addAll(missingDeps);
		}

		return ret;
	}

	private Collection<Dependency> checkDependencies (final File jarsDir, final Collection<? extends File> poms)
	{
		if ((null == poms) || (poms.size() <= 0))
			return null;
		else
			return checkDependencies(jarsDir.listFiles(), poms);
	}

	private Collection<Dependency> checkDependencies (final File jarsDir, final File pomsDir, final Collection<? extends Dependency> deps)
	{
		if ((null == deps) || (deps.size() <= 0))
			return null;

		Collection<File>	poms=null;
		for (final Dependency d : deps)
		{
			final String	pomFileName=(null == d) ? null : d.getTargetPomFileName();
			if ((null == pomFileName) || (pomFileName.length() <= 0))
				continue;

			File	pomFile=new File(pomsDir, pomFileName);
			if ((!pomFile.exists()) || (pomFile.length() <= 0L))
			{
				if ((null == (pomFile=downloadDependency(pomFile, d, _remReps)))
				 || (!pomFile.exists())
				 || (!pomFile.isFile())
				 || (pomFile.length() <= 0L))
				{
	    			log("Failed to download " + pomFileName + "(" + d + ")", Project.MSG_WARN);
	    			continue;
				}
			}
			else
			{
    			if (isVerboseMode())
    				log("checkDependencies(" + pomFile + ") found/skipped", getVerbosity());
			}

			if (null == poms)
				poms = new LinkedList<File>();
			poms.add(pomFile);
		}

		return checkDependencies(jarsDir, poms);
	}
	/**
	 * TRUE=download POM files as well and check if dependencies exist 
	 */
	private boolean	_checkDependencies=true;
	public boolean isCheckDependencies ()
	{
		return _checkDependencies;
	}

	public void setCheckDependencies (boolean checkDependencies)
	{
		_checkDependencies = checkDependencies;
	}
	/*
	 * @see org.apache.tools.ant.Task#execute()
	 */
	@Override
	public void execute () throws BuildException
	{
		final File	tgtDir=getToDir();
		{
			final Map<String,? extends Dependency>	depMap=buildDependenciesMap(_deps);
			final Map<String,? extends File>		tgtMap=buildTargetsMap(tgtDir);
			final Collection<? extends File>		dldFiles=downloadMissingFiles(depMap, tgtMap);
			final int								numDownloaded=(null == dldFiles) ? 0 : dldFiles.size();
			if (numDownloaded > 0)
				log("Downloaded " + numDownloaded + " files", Project.MSG_INFO);
		}

		if (isDownloadSources())
		{
			final Collection<? extends File>	dldSrcs=downloadSourceFiles(getSrcDir(), _deps);
			final int							numDownloaded=(null == dldSrcs) ? 0 : dldSrcs.size();
			if (numDownloaded > 0)
				log("Downloaded " + numDownloaded + " sources", Project.MSG_INFO);
		}

		if (isCheckDependencies())
		{
			final Collection<? extends Dependency>	missingDeps=checkDependencies(tgtDir, getPomDir(), _deps);
			final int								numMissing=(null == missingDeps) ? 0 : missingDeps.size();
			if (numMissing > 0)
				log("Missing " + numMissing + " depedencies", Project.MSG_WARN);
		}
	
		if (isDelUnrefFiles())
		{
			final Collection<? extends File>	delFiles=deleteUnreferencedFiles(tgtDir, _deps);
			final int							numDeleted=(null == delFiles) ? 0 : delFiles.size();
			if (numDeleted > 0)
				log("Deleted " + numDeleted + " files", Project.MSG_INFO);
		}
	}
}
