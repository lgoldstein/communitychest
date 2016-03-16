/*
 *
 */
package net.community.chest.awt.layout.gridbag;

import java.awt.GridBagLayout;

import net.community.chest.awt.layout.dom.AbstractLayoutConstraintXmlValueInstantiator;
import net.community.chest.dom.proxy.AbstractReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <L> The {@link GridBagLayout} type being used
 * @author Lyor G.
 * @since Jan 8, 2009 9:53:05 AM
 */
public class GridBagLayoutXmlConstraintValueInstantiator<L extends GridBagLayout>
        extends AbstractLayoutConstraintXmlValueInstantiator<L,ExtendedGridBagConstraints> {
    public GridBagLayoutXmlConstraintValueInstantiator (Class<L> lmc) throws IllegalArgumentException
    {
        super(lmc, ExtendedGridBagConstraints.class);
    }
    /*
     * @see net.community.chest.awt.layout.dom.AbstractLayoutConstraintXmlValueInstantiator#getConstraintProxy(java.lang.Object)
     */
    @Override
    public AbstractReflectiveProxy<ExtendedGridBagConstraints,?> getConstraintProxy (ExtendedGridBagConstraints src) throws Exception
    {
        return (null == src) ? null : ExtendedGridBagConstraintsReflectiveProxy.EGBC;
    }

    public static final GridBagLayoutXmlConstraintValueInstantiator<GridBagLayout>    GBCONST=
            new GridBagLayoutXmlConstraintValueInstantiator<GridBagLayout>(GridBagLayout.class);
}
