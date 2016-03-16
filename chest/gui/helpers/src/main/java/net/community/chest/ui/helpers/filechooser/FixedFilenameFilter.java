/*
 *
 */
package net.community.chest.ui.helpers.filechooser;

import java.io.File;
import java.io.FilenameFilter;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.io.filter.AbstractRootFolderFilesFilter;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Uses only one filename pattern</P>
 * @author Lyor G.
 * @since Aug 14, 2008 11:19:46 AM
 */
public class FixedFilenameFilter extends AbstractRootFolderFilesFilter {
    private String    _fileName    /* =null */;
    public String getFileName ()
    {
        return _fileName;
    }

    public void setFileName (String fileName)
    {
        _fileName = fileName;
    }

    public FixedFilenameFilter (String description, String fileName, boolean caseSensitive)
    {
        _fileName = fileName;
        setDescription(description);
        setCaseSensitive(caseSensitive);
    }

    public FixedFilenameFilter (String description, String fileName)
    {
        this(description, fileName, false);
    }

    public FixedFilenameFilter (String fileName, boolean caseSensitive)
    {
        this(null, fileName, caseSensitive);
    }

    public FixedFilenameFilter (String fileName)
    {
        this(fileName, false);
    }

    public FixedFilenameFilter (boolean caseSensitive)
    {
        this(null, caseSensitive);
    }

    public FixedFilenameFilter ()
    {
        this(false);
    }
    /*
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    @Override
    public boolean accept (final File f)
    {
        if (!super.accept(f))
            return false;

        if (!f.isFile())
            return true;

        final String    name=f.getName(), fixedName=getFileName();
        if ((null == fixedName) || (fixedName.length() <= 0))
            return false;    // if no specific name set accept nothing

        return (0 == StringUtil.compareDataStrings(name, fixedName, isCaseSensitive()));
    }

    public static final String    FILENAME_ATTR="fileName";
    public String setFileName (Element elem)
    {
        final String    n=elem.getAttribute(FILENAME_ATTR);
        if ((n != null) && (n.length() > 0))
            setFileName(n);

        return n;
    }
    /*
     * @see net.community.chest.swing.component.filechooser.BaseFileFilter#fromXml(org.w3c.dom.Element)
     */
    @Override
    @CoVariantReturn
    public FixedFilenameFilter fromXml (final Element elem) throws Exception
    {
        final FilenameFilter    f=super.fromXml(elem);
        if (f != this)
            throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + " mismatched recovered instance");

        setFileName(elem);
        return this;
    }

    public FixedFilenameFilter (final Element elem) throws Exception
    {
        final FixedFilenameFilter    f=fromXml(elem);
        if (f != this)
            throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " mismatched recovered instance");
    }
}
