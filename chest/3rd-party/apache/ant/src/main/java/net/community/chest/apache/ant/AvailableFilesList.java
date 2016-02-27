/*
 * 
 */
package net.community.chest.apache.ant;

import java.io.File;
import java.util.StringTokenizer;

import net.community.chest.apache.ant.helpers.AbstractPropConditionTask;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Checks if a list of files (no wildcards) exist in a given folder. Usage:</P></BR>
 * <PRE>
 * 		<flistavail property="prop.name" value="1234" dir="some-dir" includes="a,b,d.txt" [else="5678"]/>
 * 
 * 			the "else" is optional - if omitted then no value is set
 * </PRE>
 * <B>Note:</B> can also be used within a <I>condition</I> statement (without
 * setting a property.
 * 
 * @author Lyor G.
 * @since Jul 8, 2009 8:05:42 AM
 */
public class AvailableFilesList extends AbstractPropConditionTask {
    private File	_dir;
    /**
     * @param dir Folder in which specified files list should be checked
     * (default={@link Project#getBaseDir()}).
     */
    public void setDir (File dir)
    {
        _dir = dir;
    }

    public File getDir ()
    {
    	return _dir;
    }

    private String _includes;
    /**
     * @param includes (comma/space separated) list of files to be checked
     */
    public void setIncludes (String includes)
    {
        _includes = includes;
    }

    public String getIncludes ()
    {
    	return _includes;
    }
    /**
     * Sets verbose mode (default=false)
     * @param verbose TRUE if required to display internal workings
     */
    public void setVerbose (boolean verbose)
    {
        if (verbose)
        	setVerbosity(Project.MSG_INFO);
        else
        	setVerbosity(Project.MSG_VERBOSE);
    }
    /* 
     * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
     */
    @Override
	public boolean eval () throws BuildException
    {
    	File	dir=getDir();
        if (null == dir)
        {
        	final Project	p=getProject();
            dir = p.getBaseDir();
			if (isVerboseMode())
				log("Using " + dir.getAbsolutePath() + " as default directory", getVerbosity());
        }
        // if directory does not exist from the start, then obviously the files don't exist
        if (!dir.exists())
        	return false;

        if (!dir.isDirectory())	// not allowed
            throw new BuildException(dir.getAbsolutePath() + " is not a directory", getLocation());

        final String	incList=getIncludes();
        if ((null == incList) || (incList.length() <= 0))
            throw new BuildException("No \"includes\" attribute specified", getLocation());

        final StringTokenizer	st=new StringTokenizer(incList, " ,;");
        int						numFound=0;
        while (st.hasMoreTokens())
        {
            final String	fileName=st.nextToken();
            if ((null == fileName) || (fileName.length() <= 0))
                continue;	// should not happen

            final File		fileInfo=new File(dir, fileName);
            final String	filePath=fileInfo.getAbsolutePath();
            if (!fileInfo.exists())
            {
    			if (isVerboseMode())
    				log(filePath + " not found - terminating evaluation", getVerbosity());
                return false;
            }

            numFound++;

            if (isVerboseMode())
				log(filePath + " found - (total checked " + numFound + " files so far)", getVerbosity());
        }

        if (numFound <= 0)	// we MUST have some successes
            throw new BuildException("No files specified in the \"includes\" section - check format", getLocation());

        return true;
    }
}
