package net.community.chest.swing.dom.proxy;

import javax.swing.ToolTipManager;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;

import org.w3c.dom.Element;

public class ToolTipManagerReflectiveProxy<M extends ToolTipManager> extends UIReflectiveAttributesProxy<M> {
    public ToolTipManagerReflectiveProxy (Class<M> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected ToolTipManagerReflectiveProxy (Class<M> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public static final ToolTipManagerReflectiveProxy<ToolTipManager>    TTMGR=
        new ToolTipManagerReflectiveProxy<ToolTipManager>(ToolTipManager.class, true) {
                /*
                 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
                 */
                @Override
                public ToolTipManager createInstance (Element elem) throws Exception
                {
                    return (null == elem) ? null : ToolTipManager.sharedInstance();
                }
            };
}
