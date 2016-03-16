/*
 *
 */
package net.community.chest.ui.helpers.options;

import java.awt.Component;

import javax.swing.Icon;

import org.w3c.dom.Element;

import net.community.chest.dom.proxy.ReflectiveResourceLoader;
import net.community.chest.dom.proxy.ReflectiveResourceLoaderContext;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.ui.helpers.XmlContainerComponentInitializer;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 21, 2009 4:53:40 PM
 */
public class HelperOptionPane extends BaseOptionPane {
    /**
     *
     */
    private static final long serialVersionUID = -5671406147814351000L;

    public HelperOptionPane ()
    {
        super();
    }

    public HelperOptionPane (Object msg)
    {
        super(msg);
    }

    public HelperOptionPane (Object msg, int msgType)
    {
        super(msg, msgType);
    }

    public HelperOptionPane (Object msg, int msgType, int optType)
    {
        super(msg, msgType, optType);
    }

    public HelperOptionPane (Object msg, int msgType, int optType, Icon i)
    {
        super(msg, msgType, optType, i);
    }

    public HelperOptionPane (Object msg, int msgType, int optType, Icon i, Object ... opts)
    {
        super(msg, msgType, optType, i, opts);
    }

    public HelperOptionPane (Object msg, int msgType, int optType, Icon i, Object v, Object ... opts)
    {
        super(msg, msgType, optType, i, v, opts);
    }

    public static final Element getMessageElement (XmlContainerComponentInitializer cci, String msgKey)
    {
        return ((null == cci) || (null == msgKey) || (msgKey.length() <= 0)) ? null : cci.getSection(msgKey);
    }

    //////////////////////// messages //////////////////////////////

    public static final void formatMessageDialog (
            Component parent, XmlContainerComponentInitializer cci, String msgKey,
            ReflectiveResourceLoader resLoader, boolean useFormat, Object ... args)
    {
        formatMessageDialog(parent, getMessageElement(cci, msgKey), resLoader, useFormat, args);
    }

    public static final void formatMessageDialog (
            Component parent, XmlContainerComponentInitializer cci, String msgKey,
            ReflectiveResourceLoaderContext resContext, boolean useFormat, Object ... args)
    {
        formatMessageDialog(parent, getMessageElement(cci, msgKey), resContext, useFormat, args);
    }

    public static final void formatMessageDialog (
            Component parent, XmlContainerComponentInitializer cci, String msgKey, boolean useFormat, Object ... args)
    {
        formatMessageDialog(parent, getMessageElement(cci, msgKey), useFormat, args);
    }

    public static final void showMessageDialog (
            Component parent, XmlContainerComponentInitializer cci, String msgKey, ReflectiveResourceLoader resLoader)
    {
        formatMessageDialog(parent, cci, msgKey, resLoader, false);
    }

    public static final void showMessageDialog (
            Component parent, XmlContainerComponentInitializer cci, String msgKey, ReflectiveResourceLoaderContext resContext)
    {
        formatMessageDialog(parent, cci, msgKey, resContext, false);
    }

    public static final void showMessageDialog (
            Component parent, XmlContainerComponentInitializer cci, String msgKey)
    {
        showMessageDialog(parent, getMessageElement(cci, msgKey));
    }

    //////////////////////// confirmations //////////////////////////////

    public static final int formatConfirmDialog (
            Component parent, XmlContainerComponentInitializer cci, String msgKey,
            ReflectiveResourceLoader resLoader, boolean useFormat,
            Object ... args)
    {
        return formatConfirmDialog(parent, getMessageElement(cci, msgKey), resLoader, useFormat, args);
    }

    public static final int formatConfirmDialog (
            Component parent, XmlContainerComponentInitializer cci, String msgKey,
            ReflectiveResourceLoaderContext resContext, boolean useFormat,
            Object ... args)
    {
        return formatConfirmDialog(parent, getMessageElement(cci, msgKey), resContext, useFormat, args);
    }

    public static final int formatConfirmDialog (
            Component parent, XmlContainerComponentInitializer cci, String msgKey,
            boolean useFormat, Object ... args)
    {
        return formatConfirmDialog(parent, getMessageElement(cci, msgKey), useFormat, args);
    }

    public static final int showConfirmDialog (
            Component parent, XmlContainerComponentInitializer cci, String msgKey, ReflectiveResourceLoader resLoader)
    {
        return formatConfirmDialog(parent, cci, msgKey, resLoader, false);
    }

    public static final int showConfirmDialog (
            Component parent, XmlContainerComponentInitializer cci, String msgKey, ReflectiveResourceLoaderContext resContext)
    {
        return formatConfirmDialog(parent, cci, msgKey, resContext, false);
    }

    public static final int showConfirmDialog (
            Component parent, XmlContainerComponentInitializer cci, String msgKey)
    {
        return showConfirmDialog(parent, getMessageElement(cci, msgKey));
    }

    ///////////////////////// input //////////////////////////////

    public static final Object formatInputDialog (
            Component parent, XmlContainerComponentInitializer cci, String msgKey,
            Object[] selectionValues, Object initialSelectionValue,
            ReflectiveResourceLoader resLoader, boolean useFormat,
            Object ... args)
    {
        return formatInputDialog(parent, getMessageElement(cci, msgKey), selectionValues, initialSelectionValue, resLoader, useFormat, args);
    }

    public static final Object formatInputDialog (
            Component parent, XmlContainerComponentInitializer cci, String msgKey,
            Object[] selectionValues, Object initialSelectionValue,
            ReflectiveResourceLoaderContext resContext, boolean useFormat,
            Object ... args)
    {
        return formatInputDialog(parent, getMessageElement(cci, msgKey), selectionValues, initialSelectionValue, resContext, useFormat, args);
    }

    public static final Object formatInputDialog (
            Component parent, XmlContainerComponentInitializer cci, String msgKey,
            Object[] selectionValues, Object initialSelectionValue,
            boolean useFormat, Object ... args)
    {
        return formatInputDialog(parent, getMessageElement(cci, msgKey), selectionValues, initialSelectionValue, useFormat, args);
    }

    public static final Object showInputDialog (
            Component parent, XmlContainerComponentInitializer cci, String msgKey,
            ReflectiveResourceLoader resLoader,
            Object initialSelectionValue, Object ... selectionValues)
    {
        return formatInputDialog(parent, cci, msgKey, selectionValues, initialSelectionValue, resLoader, false);
    }

    public static final Object showInputDialog (
            Component parent, XmlContainerComponentInitializer cci, String msgKey,
            ReflectiveResourceLoaderContext resContext,
            Object initialSelectionValue, Object ... selectionValues)
    {
        return formatInputDialog(parent, cci, msgKey, selectionValues, initialSelectionValue, resContext, false);
    }

    public static final Object showInputDialog (
            Component parent, XmlContainerComponentInitializer cci, String msgKey,
            Object initialSelectionValue, Object ... selectionValues)
    {
        return showInputDialog(parent, getMessageElement(cci, msgKey), initialSelectionValue, selectionValues);
    }
}
