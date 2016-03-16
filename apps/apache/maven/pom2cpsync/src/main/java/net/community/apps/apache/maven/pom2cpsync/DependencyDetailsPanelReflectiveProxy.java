/*
 *
 */
package net.community.apps.apache.maven.pom2cpsync;

import java.lang.reflect.Method;

import net.community.chest.awt.attributes.Iconable;
import net.community.chest.swing.component.panel.BasePanelReflectiveProxy;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @param <P> The reflected {@link DependencyDetailsPanel} type
 * @since Mar 12, 2009 1:56:47 PM
 */
public class DependencyDetailsPanelReflectiveProxy<P extends DependencyDetailsPanel>
            extends BasePanelReflectiveProxy<P> {
    protected DependencyDetailsPanelReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public DependencyDetailsPanelReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }
    /*
     * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
     */
    @Override
    protected P updateObjectAttribute (P src, String name, String value, Method setter) throws Exception
    {
        if (Iconable.ATTR_NAME.equalsIgnoreCase(name))
            return updateObjectResourceAttribute(src, name, value, setter);

        return super.updateObjectAttribute(src, name, value, setter);
    }

    public static final DependencyDetailsPanelReflectiveProxy<DependencyDetailsPanel>    DDPNL=
        new DependencyDetailsPanelReflectiveProxy<DependencyDetailsPanel>(DependencyDetailsPanel.class, true);
}
