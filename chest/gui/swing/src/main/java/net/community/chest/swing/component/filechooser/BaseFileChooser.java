package net.community.chest.swing.component.filechooser;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 26, 2007 1:25:31 PM
 */
public class BaseFileChooser extends JFileChooser implements XmlConvertible<BaseFileChooser> {
    /**
     *
     */
    private static final long serialVersionUID = -4046455818861674255L;
    public BaseFileChooser ()
    {
        super();
    }

    public BaseFileChooser (File currentDirectory, FileSystemView fsv)
    {
        super(currentDirectory, fsv);
    }

    public BaseFileChooser (File currentDirectory)
    {
        super(currentDirectory);
    }

    public BaseFileChooser (FileSystemView fsv)
    {
        super(fsv);
    }

    public BaseFileChooser (String currentDirectoryPath, FileSystemView fsv)
    {
        super(currentDirectoryPath, fsv);
    }

    public BaseFileChooser (String currentDirectoryPath)
    {
        super(currentDirectoryPath);
    }

    public FileSelectionMode getFileSelectionModeValue ()
    {
        return FileSelectionMode.fromModeValue(getFileSelectionMode());
    }

    public void setFileSelectionModeValue (final FileSelectionMode mode)
    {
        setFileSelectionMode((null == mode) ? (-1) : mode.getModeValue());
    }

    protected XmlProxyConvertible<?> getFileChooserConverter (final Element elem) throws Exception
    {
        return (null == elem) ? null : JFileChooserReflectiveProxy.CHOOSER;
    }
    /* NOTE: does not override existing values !!!
     * @see net.community.chest.dom.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public BaseFileChooser fromXml (final Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getFileChooserConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    o=
            ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
        if (o != this)
            throw new IllegalStateException(ClassUtil.getExceptionLocation(getClass(), "fromXml") + " mismatched initialization instances");

        return this;
    }

    public BaseFileChooser (final Element elem) throws Exception
    {
        final BaseFileChooser    fc=fromXml(elem);
        if (fc != this)
            throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " mismatched recovered instance");
    }
    /*
     * @see net.community.chest.dom.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        // TODO implement toXml
        throw new UnsupportedOperationException(ClassUtil.getExceptionLocation(getClass(), "toXml") + " N/A");
    }
}
