/*
 *
 */
package net.community.chest.swing.component.filechooser;

import java.lang.reflect.Constructor;

import javax.swing.filechooser.FileFilter;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.AbstractXmlProxyConverter;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.dom.transform.XmlValueInstantiator;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> Actual {@link FileFilter} class being instantiated
 * @author Lyor G.
 * @since Aug 27, 2008 12:20:29 PM
 */
public class FileFilterXmlValueInstantiator<F extends FileFilter> extends BaseTypedValuesContainer<F> implements XmlValueInstantiator<F> {
    public FileFilterXmlValueInstantiator (Class<F> objClass) throws IllegalArgumentException
    {
        super(objClass);
    }

    protected Constructor<F> resolveConstructorInstance () throws Exception
    {
        return AbstractXmlProxyConverter.resolveConstructorInstance(getValuesClass());
    }

    private Constructor<F>    _ctor    /* =null */;
    public synchronized Constructor<F> getConstructor () throws Exception
    {
        if (null == _ctor)
            _ctor = resolveConstructorInstance();
        return _ctor;
    }
    /*
     * @see net.community.chest.dom.transform.XmlValueInstantiator#fromXml(org.w3c.dom.Element)
     */
    @Override
    public F fromXml (final Element elem) throws Exception
    {
        final Constructor<F>    ctor=getConstructor();
        final Class<?>[]        pars=ctor.getParameterTypes();
        final F                    ff;
        if ((null == pars) || (pars.length <= 0))
        {
            ff = ctor.newInstance();
            if (ff instanceof XmlConvertible<?>)
            {
                final Object    o=((XmlConvertible<?>) ff).fromXml(elem);
                if (o != ff)
                    throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched recovered instances");
            }
            else
                throw new UnsupportedOperationException("fromXml(" + DOMUtils.toString(elem) + ") " + getValuesClass().getName() + " not " + XmlConvertible.class.getSimpleName());
        }
        else    // assumed to expect an Element
        {
            ff = ctor.newInstance(elem);
        }

        return ff;
    }
}
