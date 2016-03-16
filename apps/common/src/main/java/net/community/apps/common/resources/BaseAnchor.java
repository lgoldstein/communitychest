package net.community.apps.common.resources;

import java.awt.Image;
import java.net.URL;
import java.util.Collection;
import java.util.TreeSet;

import javax.swing.Icon;

import net.community.chest.awt.attributes.Iconable;
import net.community.chest.awt.image.AbstractImageReader;
import net.community.chest.awt.image.ImageUtils;
import net.community.chest.dom.proxy.ReflectiveResourceLoader;
import net.community.chest.dom.proxy.ReflectiveResourceLoaderContext;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.ui.helpers.HelperUtils;
import net.community.chest.ui.helpers.resources.HelperUIAnchoredResourceAccessor;

import org.w3c.dom.Document;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful class that can serve as a base "anchor" for resources
 * @author Lyor G.
 * @since Aug 8, 2007 1:54:56 PM
 */
public class BaseAnchor extends HelperUIAnchoredResourceAccessor
        implements ReflectiveResourceLoaderContext, ReflectiveResourceLoader {

    protected BaseAnchor  ()
    {
        super();
    }
    /*
     * @see net.community.chest.resources.AbstractXmlAnchoredResourceAccessor#getDefaultDocument()
     */
    @Override
    public Document getDefaultDocument () throws Exception
    {
        return getDocument("resources.xml");
    }

    public URL resolveImageLocation (final String value) throws Exception
    {
        return HelperUtils.getDefaultClassImageLocation(getClass(), value);
    }
    // cache empty responses to accelerate already looked up resources
    private final Collection<String>    _emptyResources=
        new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
    /*
     * @see net.community.chest.resources.BaseAnchoredResourceAccessor#getResource(java.lang.String)
     */
    @Override
    public URL getResource (final String name)
    {
        if ((null == name) || (name.length() <= 0))
            return null;

        synchronized(_emptyResources)
        {
            if (_emptyResources.contains(name))
                return null;
        }

        final URL    resURL;
        if (AbstractImageReader.isImageFile(name))
        {
            try
            {
                resURL = resolveImageLocation(name);
            }
            catch(Exception e)
            {
                throw ExceptionUtil.toRuntimeException(e);
            }
        }
        else
            resURL = super.getResource(name);

        if (null == resURL)
        {
            synchronized(_emptyResources)
            {
                if (!_emptyResources.add(name))
                    return null;    // debug breakpoint to detect when resource re-added
            }
        }

        return resURL;
    }
    /*
     * @see net.community.chest.dom.transform.ReflectiveResourceLoader#loadAttributeResource(java.lang.Class, java.lang.Object, java.lang.String, java.lang.String)
     */
    @Override
    public <V> V loadAttributeResource (Class<V> resClass, Object src, String name, String value) throws Exception
    {
        if (null == resClass)
            return null;

        if (Image.class.isAssignableFrom(resClass))
        {
            final Image    img=getImage(value);
            if (img != null)
                return resClass.cast(img);
        }
        else if (Icon.class.isAssignableFrom(resClass))
        {
            final Icon    org=getIcon(value), icon=ImageUtils.adjustIconSize(org, Iconable.DEFAULT_WIDTH, Iconable.DEFAULT_HEIGHT);
            if (icon != null)
                return resClass.cast(icon);
        }

        return null;
    }
    /*
     * @see net.community.chest.dom.transform.ReflectiveResourceLoaderContext#getResourceLoader(java.lang.Class, java.lang.Object, java.lang.String, java.lang.String)
     */
    @Override
    public ReflectiveResourceLoader getResourceLoader (Class<?> resClass, Object src, String name, String value) throws Exception
    {
        if (null == resClass)
            return null;

        if (Icon.class.isAssignableFrom(resClass)
         || Image.class.isAssignableFrom(resClass))
            return this;

        return null;
    }
}
