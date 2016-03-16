package net.community.chest.swing.component.button;

import java.lang.reflect.Method;

import javax.swing.AbstractButton;

import net.community.chest.awt.dom.converter.KeyCodeValueInstantiator;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.lang.StringUtil;
import net.community.chest.swing.HAlignmentValueStringInstantiator;
import net.community.chest.swing.VAlignmentValueStringInstantiator;
import net.community.chest.swing.component.JComponentReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <B> The {@link AbstractButton} type being reflected
 * @author Lyor G.
 * @since Mar 20, 2008 8:41:31 AM
 */
public abstract class AbstractButtonReflectiveProxy<B extends AbstractButton> extends JComponentReflectiveProxy<B> {
    protected AbstractButtonReflectiveProxy (Class<B> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected AbstractButtonReflectiveProxy (Class<B> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }
    /**
     * Default element name used for button(s)
     */
    public static final String BUTTON_ELEMNAME="button";
    // some special attributes
    public static final String    ACTION_COMMAND_ATTR="actionCommand",
                                MNEMONIC_ATTR="mnemonic",
                                HALIGN_ATTR="horizontalAlignment",
                                HTEXTPOS_ATTR="horizontalTextPosition",
                                VALIGN_ATTR="verticalAlignment",
                                VTEXTPOS_ATTR="verticalTextPosition";
    /*
     * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
    {
        if (MNEMONIC_ATTR.equalsIgnoreCase(name))
            return (ValueStringInstantiator<C>) KeyCodeValueInstantiator.DEFAULT;
        else if (HALIGN_ATTR.equalsIgnoreCase(name)
              || HTEXTPOS_ATTR.equalsIgnoreCase(name))
            return (ValueStringInstantiator<C>) HAlignmentValueStringInstantiator.DEFAULT;
        else if (VALIGN_ATTR.equalsIgnoreCase(name)
              || VTEXTPOS_ATTR.equalsIgnoreCase(name))
            return (ValueStringInstantiator<C>) VAlignmentValueStringInstantiator.DEFAULT;

        return super.resolveAttributeInstantiator(name, type);
    }

    public static final String    ICON_ATTR="icon",
                                PRESSED_ICON_ATTR="pressedIcon",
                                SELECTED_ICON_ATTR="selectedIcon",
                                DISABLED_ICON_ATTR="disabledIcon",
                                DISABLED_SELECTED_ICON_ATTR="disabledSelectedIcon",
                                ROLLOVER_ICON_ATTR="rolloverIcon",
                                ROLLOVER_SELECTED_ICON_ATTR="rolloverSelectedIcon";
    /*
     * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
     */
    @Override
    protected B updateObjectAttribute (B src, String name, String value, Method setter) throws Exception
    {
        // some special resource related attributes
        if (ICON_ATTR.equalsIgnoreCase(name)
         || StringUtil.endsWith(name, ICON_ATTR, true, false))
            return updateObjectResourceAttribute(src, name, value, setter);

        return super.updateObjectAttribute(src, name, value, setter);
    }
}
