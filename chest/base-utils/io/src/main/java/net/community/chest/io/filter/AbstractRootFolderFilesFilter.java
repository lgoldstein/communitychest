/*
 *
 */
package net.community.chest.io.filter;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Collection;

import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.set.SetsUtils;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Filters all folders that have a given name and any files
 * residing under these folders</P>
 *
 * @author Lyor G.
 * @since Aug 19, 2010 9:36:02 AM
 */
public abstract class AbstractRootFolderFilesFilter extends javax.swing.filechooser.FileFilter
            implements FileFilter, FilenameFilter, ObjectFilter<File>, XmlConvertible<AbstractRootFolderFilesFilter> {
    /*
     * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
     */
    @Override
    public final Class<File> getValuesClass ()
    {
        return File.class;
    }

    private String    _description;
    /*
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    @Override
    public String getDescription ()
    {
        return _description;
    }

    public void setDescription (String description)
    {
        _description = description;
    }

    private Collection<String>    _excludedNames;
    public Collection<String> getExcludedNames ()
    {
        return _excludedNames;
    }

    public void setExcludedNames (Collection<String> excludedNames)
    {
        _excludedNames = excludedNames;
    }

    private boolean    _caseSensitive;
    public boolean isCaseSensitive ()
    {
        return _caseSensitive;
    }

    public void setCaseSensitive (boolean caseSensitive)
    {
        _caseSensitive = caseSensitive;
    }

    private boolean    _onlyFolders    /* =false */;
    /**
     * @return TRUE if only folders are to be accepted (default=false - i.e.,
     * accept only files)
     */
    public boolean isOnlyFolders ()
    {
        return _onlyFolders;
    }

    public void setOnlyFolders (boolean onlyFolders)
    {
        _onlyFolders = onlyFolders;
    }
    /**
     * @param n The name of the <U>folder</U> (assumed) to be checked
     * @return The matching name in the excluded names {@link Collection},
     * null/empty otherwise
     */
    public String getExcludedParentFolderName (final String n)
    {
        if ((null == n) || (n.length() <= 0))
            return null;

        final Collection<String>    xl=getExcludedNames();
        if ((null == xl) || (xl.size() <= 0))
            return null;

        return isCaseSensitive()
                ? CollectionsUtils.findElement(xl, n)
                : CollectionsUtils.findElement(xl, n, String.CASE_INSENSITIVE_ORDER)
                ;
    }
    /**
     * @param n The <U>folder</U> (assumed) name to be tested
     * @return <code>true</code> if it  appears in the excluded names
     * {@link Collection} (according to the case-sensitivity).
     * @see #getExcludedParentFolderName(String)
     */
    public boolean isExcludedParentFolderName (final String n)
    {
        final String    xn=getExcludedParentFolderName(n);
        if ((xn != null) && (xn.length() > 0))
            return true;    // debug breakpoint

        return false;
    }
    /**
     * @param f The {@link File} to be checked
     * @return The matching name in the excluded names {@link Collection}
     * if this is a <U>folder</U> whose name has matched, null/empty
     * otherwise
     * @see #getExcludedParentFolderName(String)
     */
    public String getExcludedParentFolderName (final File f)
    {
        if ((null == f) || (!f.isDirectory()))
            return null;

        return getExcludedParentFolderName(f.getName());
    }
    /**
     * @param f The {@link File} to be tested
     * @return <code>true</code> if it is a <U>folder</U> whose name
     * appears in the excluded names {@link Collection} (according to
     * the case-sensitivity).
     * @see #getExcludedParentFolderName(File)
     */
    public boolean isExcludedParentFolderName (final File f)
    {
        final String    xn=getExcludedParentFolderName(f);
        if ((xn != null) && (xn.length() > 0))
            return true;    // debug breakpoint

        return false;
    }
    /*
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    @Override
    public boolean accept (File f)
    {
        final boolean onlyDirs=isOnlyFolders();
        if (onlyDirs && (f != null) && (!f.isDirectory()))
            return false;

        if (isExcludedParentFolderName(f))
            return false;

        // make sure parent is not excluded
        final File    p=f.getParentFile();
        if (isExcludedParentFolderName(p))
            return false;

        return true;
    }
    /*
     * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
     */
    @Override
    public boolean accept (File dir, String name)
    {
        if (isOnlyFolders())
        {
             if ((dir != null) && (name != null) && (name.length() > 0))
                 return accept(new File(dir, name));

             if ((dir != null) && dir.isDirectory())
                 return true;

             return false;
        }

        if (isExcludedParentFolderName(dir))
            return false;

        if (isExcludedParentFolderName(name))
            return false;

        return true;
    }

    protected AbstractRootFolderFilesFilter (boolean caseSensitive, Collection<String> excludedNames)
    {
        _caseSensitive = caseSensitive;
        _excludedNames = excludedNames;
    }

    protected AbstractRootFolderFilesFilter (boolean caseSensitive, String ... excludedNames)
    {
        this(caseSensitive, ((null == excludedNames) || (excludedNames.length <= 0)) ? null : SetsUtils.comparableSetOf(excludedNames));
    }

    protected AbstractRootFolderFilesFilter (boolean caseSensitive)
    {
        this(caseSensitive, (Collection<String>) null);
    }

    protected AbstractRootFolderFilesFilter ()
    {
        this(false);
    }

    public static final String    DESCRIPTION_ATTR="description";
    // returns retrieved description text - sets it only if non-null/empty
    public String setDescription (final Element elem) throws Exception
    {
        if (null == elem)
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, ClassUtil.getExceptionLocation(getClass(), "setDescription") + " no " + Element.class.getName() + " instance");

        final String    desc=elem.getAttribute(DESCRIPTION_ATTR);
        if ((desc != null) && (desc.length() > 0))
            setDescription(desc);

        return desc;
    }

    public static final String    ONLYFOLDERS_ATTR="folders";
    // returns value - null if none specified
    public Boolean setOnlyFolders (final Element elem) throws Exception
    {
        if (null == elem)
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, ClassUtil.getExceptionLocation(getClass(), "setOnlyFolders") + " no " + Element.class.getName() + " instance");

        final String    val=elem.getAttribute(ONLYFOLDERS_ATTR);
        if ((val != null) && (val.length() > 0))
        {
            final Boolean    mode=Boolean.valueOf(val);
            setOnlyFolders(mode.booleanValue());
            return mode;
        }

        return null;
    }

    public static final String    CASESENSE_ATTR="casesensitive";
    // returns chosen mode - null if none set
    public Boolean setCaseSensitive (final Element elem) throws DOMException
    {
        if (null == elem)
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, ClassUtil.getExceptionLocation(getClass(), "setCaseSensitive") + " no " + Element.class.getName() + " instance");

        final String    val=elem.getAttribute(ONLYFOLDERS_ATTR);
        if ((val != null) && (val.length() > 0))
        {
            final Boolean    mode=Boolean.valueOf(val);
            setCaseSensitive(mode.booleanValue());
            return mode;
        }

        return null;
    }
    /* NOTE: does not override existing values !!!
     * @see net.community.chest.dom.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public AbstractRootFolderFilesFilter fromXml (final Element elem) throws Exception
    {
        setDescription(elem);
        setOnlyFolders(elem);
        setCaseSensitive(elem);
        return this;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        // TODO implement toXml
        throw new UnsupportedOperationException(ClassUtil.getExceptionLocation(getClass(), "toXml") + " N/A");
    }
}
