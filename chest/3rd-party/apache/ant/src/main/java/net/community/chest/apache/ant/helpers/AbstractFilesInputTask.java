/*
 *
 */
package net.community.chest.apache.ant.helpers;

import java.io.File;
import java.util.Collection;
import java.util.Vector;


import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @param <V> The type of result returned for processing file(s)
 * @author Lyor G.
 * @since Jul 9, 2009 10:18:11 AM
 */
public abstract class AbstractFilesInputTask<V> extends ExtendedTask {
    protected AbstractFilesInputTask ()
    {
        super();
    }
    /**
     * Source filesets vector
     */
    private final Vector<FileSet> _filesets=new Vector<FileSet>();
    public Vector<FileSet> getFilesets ()
    {
        return _filesets;
    }
    /**
     * (Single) source file
     */
    private File    _file    /* =null */;
    public File getFile ()
    {
        return _file;
    }

    public void setFile (final File srcFile) throws BuildException
    {
        final Vector<FileSet>    filesets=getFilesets();
        final int                numFilesets=
            (null == filesets) ? 0 : filesets.size();
        if (numFilesets > 0)
            throw new BuildException("Cannot specify 'srcfile' and 'fileset' in same task", getLocation());

        _file = srcFile;
    }
    /**
     * Adds a set of files.
     * @param set set of files to be added
     * @throws BuildException if 'srcfile' already specified
     */
    public void addFileset (final FileSet set) throws BuildException
    {
        if (getFile() != null)
            throw new BuildException("Cannot specify 'srcfile' and 'fileset' in same task", getLocation());
        _filesets.addElement(set);
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
    /**
     * @param inFile input {@link File} to be processed
     * @return processing result
     * @throws BuildException unable to process file
     */
    protected abstract V processSingleFile (final File inFile) throws BuildException;
    /**
     * @param filesets The {@link Collection} of {@link FileSet}-s to be
     * processed
     * @return processed files result. The default implementation calls
     * {@link #processSingleFile(File)} and returns the <U>last</U> call
     * result
     * @throws BuildException unable to build
     */
    protected V processFileSets (final Collection<? extends FileSet> filesets) throws BuildException
    {
        final int    numFilesets=(null == filesets) ? 0 : filesets.size();
        if (numFilesets <= 0)    // some fileset(s) MUST be specified
            throw new BuildException("no filesets specified", getLocation());

        V    ret=null;
        for (final FileSet fs : filesets)
        {
            if (null == fs)
                continue;    // should not happen

            final FileScanner    scanner=fs.getDirectoryScanner(getProject());
            final String[]        files=(null == scanner) ? null : scanner.getIncludedFiles();
            final int            numFiles=(null == files) ? 0 : files.length;
            if (numFiles <= 0)
                continue;    // OK if no files in file set

            final File    baseDir=scanner.getBasedir();
            for (final String    fileName : files)
            {
                if ((null == fileName) || (fileName.length() <= 0))
                    continue;    // should not happen

                final File    inFile=new File(baseDir, fileName);
                ret = processSingleFile(inFile);
            }
        }

        return ret;
    }

    protected V executeProcessing () throws BuildException
    {
        final File    srcFile=getFile();
        return     (null == srcFile) ? processFileSets(getFilesets()) : processSingleFile(srcFile);
    }
    /*
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute () throws BuildException
    {
        executeProcessing();
    }
}
