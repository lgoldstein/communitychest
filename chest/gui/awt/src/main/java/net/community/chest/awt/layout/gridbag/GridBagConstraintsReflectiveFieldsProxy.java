/*
 *
 */
package net.community.chest.awt.layout.gridbag;

import java.awt.GridBagConstraints;

import net.community.chest.dom.proxy.ReflectiveFieldsProxy;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <C> The reflected {@link GridBagConstraints} instance
 * @author Lyor G.
 * @since Mar 11, 2009 1:50:08 PM
 *
 */
public class GridBagConstraintsReflectiveFieldsProxy<C extends GridBagConstraints> extends ReflectiveFieldsProxy<C> {
    public GridBagConstraintsReflectiveFieldsProxy (Class<C> valsClass)
            throws IllegalArgumentException
    {
        super(valsClass, GridBagConstraints.class);
    }

    public static final GridBagConstraintsReflectiveFieldsProxy<GridBagConstraints>    GBC=
        new GridBagConstraintsReflectiveFieldsProxy<GridBagConstraints>(GridBagConstraints.class);
}
