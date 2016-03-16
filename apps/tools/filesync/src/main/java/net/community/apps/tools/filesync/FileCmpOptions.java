/*
 *
 */
package net.community.apps.tools.filesync;

import java.io.Serializable;

import net.community.chest.CoVariantReturn;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 28, 2009 8:43:38 AM
 */
public class FileCmpOptions implements Serializable, Cloneable {
    /**
     *
     */
    private static final long serialVersionUID = -8756796643673323560L;
    public FileCmpOptions ()
    {
        super();
    }

    private boolean    _testOnly;
    public boolean isTestOnly ()
    {
        return _testOnly;
    }

    public void setTestOnly (boolean testOnly)
    {
        _testOnly = testOnly;
    }

    private boolean    _compareFileContents;
    public boolean isCompareFileContents ()
    {
        return _compareFileContents;
    }

    public void setCompareFileContents (boolean compareFileContents)
    {
        _compareFileContents = compareFileContents;
    }

    private boolean    _ignoreCorruptedFiles;
    public boolean isIgnoreCorruptedFiles ()
    {
        return _ignoreCorruptedFiles;
    }

    public void setIgnoreCorruptedFiles (boolean ignoreCorruptedFiles)
    {
        _ignoreCorruptedFiles = ignoreCorruptedFiles;
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if (obj == this)
            return true;
        if (!(obj instanceof FileCmpOptions))
            return false;

        final FileCmpOptions    other=(FileCmpOptions) obj;
        return (isTestOnly() == other.isCompareFileContents())
            && (isCompareFileContents() == other.isCompareFileContents())
            && (isIgnoreCorruptedFiles() == other.isIgnoreCorruptedFiles())
            ;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return (isTestOnly() ? 1 : 0)
             + (isCompareFileContents() ? 1 : 0)
             + (isIgnoreCorruptedFiles() ? 1 : 0)
             ;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return "test=" + isTestOnly()
            + ";content=" + isCompareFileContents()
            + ";ignore=" + isIgnoreCorruptedFiles()
            ;
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public FileCmpOptions clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
}
