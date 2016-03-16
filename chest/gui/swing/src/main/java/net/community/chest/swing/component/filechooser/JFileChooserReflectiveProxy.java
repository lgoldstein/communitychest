package net.community.chest.swing.component.filechooser;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

import net.community.chest.awt.dom.converter.KeyCodeValueInstantiator;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.swing.component.JComponentReflectiveProxy;
import net.community.chest.util.map.ClassNameMap;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <C> The reflected {@link JFileChooser} instance
 * @author Lyor G.
 * @since Mar 20, 2008 10:31:58 AM
 */
public class JFileChooserReflectiveProxy<C extends JFileChooser> extends JComponentReflectiveProxy<C> {
    public JFileChooserReflectiveProxy (Class<C> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected JFileChooserReflectiveProxy (Class<C> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }
    // some special attributes handling
    public static final String    APPROVE_BTN_MNEMONIC_ATTR="approveButtonMnemonic",
                                SEL_MODE_ATTR=FileSelectionMode.class.getSimpleName(),
                                FILE_VIEW_ATTR="FileView",
                                FILE_SYSTEM_VIEW_ATTR="FileSystemView",
                                    SYSTEM_ATTR_VALUE=System.class.getSimpleName();
    /*
     * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <Z> ValueStringInstantiator<Z> resolveAttributeInstantiator (String name, Class<Z> type) throws Exception
    {
        if (SEL_MODE_ATTR.equalsIgnoreCase(name))
            return (ValueStringInstantiator<Z>) FileSelectionModeValueStringInstantiator.DEFAULT;
        else if (APPROVE_BTN_MNEMONIC_ATTR.equalsIgnoreCase(name))
            return (ValueStringInstantiator<Z>) KeyCodeValueInstantiator.DEFAULT;

        return super.resolveAttributeInstantiator(name, type);
    }
    /*
     * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
     */
    @Override
    protected C updateObjectAttribute (C src, String name, String value, Method setter) throws Exception
    {
        if (FILE_VIEW_ATTR.equalsIgnoreCase(name))
        {
            if (SYSTEM_ATTR_VALUE.equalsIgnoreCase(value))
            {
                src.setFileView(DefaultSystemFileView.DEFAULT);
                return src;
            }

            throw new NoSuchElementException(getArgumentsExceptionLocation("updateObjectAttribute", value) + " unknown '" + name + "' value");
        }
        else if (FILE_SYSTEM_VIEW_ATTR.equalsIgnoreCase(value))
        {
            if (SYSTEM_ATTR_VALUE.equalsIgnoreCase(value))
            {
                src.setFileSystemView(FileSystemView.getFileSystemView());
                return src;
            }

            throw new NoSuchElementException(getArgumentsExceptionLocation("updateObjectAttribute", value) + " unknown '" + name + "' value");
        }

        return super.updateObjectAttribute(src, name, value, setter);
    }

    private static final ClassNameMap<FileFilterXmlValueInstantiator<?>>    _filtersMap=new ClassNameMap<FileFilterXmlValueInstantiator<?>>();
    public static final synchronized XmlValueInstantiator<? extends FileFilter> getFilterXMLConstructor (final Class<?> fc) throws Exception
    {
        if (null == fc)
            return null;

        FileFilterXmlValueInstantiator<?>    c=null;
        synchronized(_filtersMap)
        {
            if (null == (c=_filtersMap.get(fc)))
            {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                final FileFilterXmlValueInstantiator<?>    nc=
                    new FileFilterXmlValueInstantiator(fc);
                _filtersMap.put(fc, nc);
                c = nc;
            }
        }

        return c;
    }

    public XmlValueInstantiator<? extends FileFilter> getFileFilterConverter (final Element elem) throws Exception
    {
        final String    ft=elem.getAttribute(CLASS_ATTR);
        if ((null == ft) || (ft.length() <= 0))
            throw new DOMException(DOMException.INVALID_STATE_ERR, "setFileFilter(" + DOMUtils.toString(elem) + ") no/missing '" + CLASS_ATTR + "' attribute");

        final Class<?>    fc=ClassUtil.loadClassByName(ft);
        return getFilterXMLConstructor(fc);
    }

    public FileFilter addFileFilter (final C src, final Element elem) throws Exception
    {
        final XmlValueInstantiator<? extends FileFilter>    inst=getFileFilterConverter(elem);
        final FileFilter                                    ff=inst.fromXml(elem);
        if (ff != null)
            src.addChoosableFileFilter(ff);
        return ff;
    }
    // special supported "child" elements
    public static final String    FILTER_ELEMNAME="filter";
    public boolean isFilterElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, FILTER_ELEMNAME);
    }

    public static final String    CHOOSER_ELEMNAME="chooser";
    /*
     * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
     */
    @Override
    public C fromXmlChild (C src, Element elem) throws Exception
    {
        final String    tagName=elem.getTagName();
        if (isFilterElement(elem, tagName))
        {
            addFileFilter(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }

    public static final JFileChooserReflectiveProxy<JFileChooser>    CHOOSER=
                new JFileChooserReflectiveProxy<JFileChooser>(JFileChooser.class, true);
}
