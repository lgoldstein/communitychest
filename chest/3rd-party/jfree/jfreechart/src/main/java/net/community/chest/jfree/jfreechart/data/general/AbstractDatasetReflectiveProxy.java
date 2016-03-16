/*
 *
 */
package net.community.chest.jfree.jfreechart.data.general;

import java.lang.reflect.Method;

import net.community.chest.jfree.jfreechart.ChartReflectiveAttributesProxy;

import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.DatasetGroup;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <D> Type of reflected {@link AbstractDataset}
 * @author Lyor G.
 * @since Jan 27, 2009 2:38:53 PM
 */
public class AbstractDatasetReflectiveProxy<D extends AbstractDataset> extends ChartReflectiveAttributesProxy<D> {
    protected AbstractDatasetReflectiveProxy (Class<D> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public static final String    GROUP_ATTR="group";
    /*
     * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
     */
    @Override
    protected D updateObjectAttribute (D src, String name, String value, Method setter) throws Exception
    {
        if (GROUP_ATTR.equalsIgnoreCase(name))
        {
            setter.invoke(src, new DatasetGroup(value));
            return src;
        }

        return super.updateObjectAttribute(src, name, value, setter);
    }
    // NOTE !!! an exception will be thrown if attempting to use it as an instantiator
    public static final AbstractDatasetReflectiveProxy<AbstractDataset>    ABSDS=
        new AbstractDatasetReflectiveProxy<AbstractDataset>(AbstractDataset.class, true);
}
