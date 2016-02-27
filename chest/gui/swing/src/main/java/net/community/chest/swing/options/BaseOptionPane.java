/**
 * 
 */
package net.community.chest.swing.options;

import java.awt.Component;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import net.community.chest.dom.proxy.AbstractXmlProxyConverter;
import net.community.chest.dom.proxy.ReflectiveResourceLoader;
import net.community.chest.dom.proxy.ReflectiveResourceLoaderContext;
import net.community.chest.io.EOLStyle;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 31, 2008 11:48:35 AM
 */
public class BaseOptionPane extends JOptionPane {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4125095010258684712L;

	public BaseOptionPane ()
	{
		super();
	}

	public BaseOptionPane (Object msg)
	{
		super(msg);
	}

	public BaseOptionPane (Object msg, int msgType)
	{
		super(msg, msgType);
	}

	public BaseOptionPane (Object msg, int msgType, int optType)
	{
		super(msg, msgType, optType);
	}

	public BaseOptionPane (Object msg, int msgType, int optType, Icon i)
	{
		super(msg, msgType, optType, i);
	}

	public BaseOptionPane (Object msg, int msgType, int optType, Icon i, Object ... opts)
	{
		super(msg, msgType, optType, i, opts);
	}

	public BaseOptionPane (Object msg, int msgType, int optType, Icon i, Object iValue, Object ... opts)
	{
		super(msg, msgType, optType, i, opts, iValue);
	}

    protected static final ReflectiveResourceLoader getReflectiveResourceLoader (Element elem, ReflectiveResourceLoaderContext resContext)
    {
    	if (null == resContext)
    		return null;

    	final String	icn=elem.getAttribute(ICON_PROPERTY);
    	if ((null == icn) || (icn.length() <= 0))
    		return null;

    	try
    	{
    		return resContext.getResourceLoader(Icon.class, JOptionPane.class, ICON_PROPERTY, icn);
    	}
    	catch(Exception e)
    	{
    		return null;
    	}
    }

    protected static final Icon loadMessageIcon (final Element elem, final ReflectiveResourceLoader resLoader)
    {
    	if (null == resLoader)
    		return null;
		
    	final String	icn=(null == elem) ? null : elem.getAttribute(ICON_PROPERTY);
    	if ((null == icn) || (icn.length() <= 0))
    		return null;
    	try
    	{
   			return resLoader.loadAttributeResource(Icon.class, JOptionPane.class, ICON_PROPERTY, icn);
    	}
    	catch(Exception e)
    	{
    		return null;
    	}
    }

    public static final String	TITLE_ATTR="title";

    ///////////////////// messages //////////////////////////////////

    // returns same Throwable instance as input
    public static final <T extends Throwable> T showMessageDialog (Component parentComponent, T t)
    {
    	final Throwable	c=(null == t) ? null : t.getCause();
    	final String	tMsg=(null == t) ? null : t.getMessage(),
    					cMsg=(null == c) ? null : c.getMessage(),
    					dMsg;
    	if (((null == tMsg) || (tMsg.length() <= 0)))
    		dMsg = cMsg;
    	else if ((null == cMsg) || (cMsg.length() <= 0))
    		dMsg = tMsg;
    	else	// both non-null/empty
    		dMsg = tMsg + EOLStyle.CRLF.getStyleString() + "Cause: " + cMsg;

    	showMessageDialog(parentComponent, "" + dMsg, "" + ((null == t) ? null : t.getClass().getName()), ERROR_MESSAGE);
    	return t;
    }

    public static final void showMessageDialog (Component parentComponent, String message, String title, OptionPaneMessageType mt, Icon icon)
    {
    	showMessageDialog(parentComponent, message, title, (null == mt) ? PLAIN_MESSAGE : mt.getTypeValue(), icon);
    }

    public static final void showMessageDialog (Component parentComponent, String message, String title, OptionPaneMessageType mt)
    {
    	showMessageDialog(parentComponent, message, title, mt, null);
    }

    public static final void formatMessageDialog (
    		Component parentComponent, Element elem,
    		ReflectiveResourceLoader resLoader, boolean useFormat,
    		Object ... args)
    {
    	final String				ttl=(null == elem) ? null : elem.getAttribute(TITLE_ATTR),
									txt=(null == elem) ? null : elem.getAttribute(MESSAGE_PROPERTY),
									ttv=(null == elem) ? null : elem.getAttribute(MESSAGE_TYPE_PROPERTY);
    	final Icon					icon=loadMessageIcon(elem, resLoader);
    	final OptionPaneMessageType	mtv=OptionPaneMessageType.fromString(ttv);

    	String	msg=txt;
    	try
    	{
			if ((txt != null) && (txt.length() > 0) && useFormat)
				msg = String.format(txt, args);
    	}
    	catch(RuntimeException e)
    	{
    		msg = txt;
    	}

    	showMessageDialog(parentComponent, msg, ttl, mtv, icon);
    }

    public static final void formatMessageDialog (
    		Component parentComponent, Element elem,
    		ReflectiveResourceLoaderContext resContext, boolean useFormat,
    		Object ... args)
    {
    	formatMessageDialog(parentComponent, elem, getReflectiveResourceLoader(elem, resContext), useFormat, args);
    }

    public static final void formatMessageDialog (
    		Component parentComponent, Element elem, boolean useFormat, Object ... args)
    {
    	formatMessageDialog(parentComponent, elem, AbstractXmlProxyConverter.getDefaultLoader(), useFormat, args);
    }

    public static final void showMessageDialog (
    		Component parentComponent, Element elem, ReflectiveResourceLoader resLoader)
    {
    	formatMessageDialog(parentComponent, elem, resLoader, false);
    }

    public static final void showMessageDialog (
    		Component parentComponent, Element elem, ReflectiveResourceLoaderContext resContext)
    {
    	formatMessageDialog(parentComponent, elem, resContext, false); 
    }
    
    // uses 'text' attribute for the message, 'title' for the title and 'type' for the WebOptionPaneMessageType value
    public static final void showMessageDialog (Component parentComponent, Element elem)
    {
    	showMessageDialog(parentComponent, elem, AbstractXmlProxyConverter.getDefaultLoader());
    }

    ///////////////////////////// confirmation ////////////////////////////

    public static final int showConfirmDialog (final Component				parentComponent,
    									 	   final String 				message,
    									 	   final String 				title,
    									 	   final OptionPaneConfirmType 	otv,
    									 	   final OptionPaneMessageType 	mtv,
    									 	   final Icon 					icon)
    {
    	return showConfirmDialog(parentComponent, message, title, (null == otv) ? DEFAULT_OPTION : otv.getConfirmationType(), (null == mtv) ? PLAIN_MESSAGE : mtv.getTypeValue(), icon);
    }

    public static final int showConfirmDialog (final Component 				parentComponent,
    									 	   final String 				message,
    									 	   final String 				title,
    									 	   final OptionPaneConfirmType 	otv,
    									 	   final OptionPaneMessageType 	mtv)
    {
    	return showConfirmDialog(parentComponent, message, title, otv, mtv, null);
    }

    public static final int formatConfirmDialog (
    		Component parentComponent, Element elem,
    		ReflectiveResourceLoader resLoader, boolean useFormat,
    		Object ... args)
    {
    	final String				ttl=(null == elem) ? null : elem.getAttribute(TITLE_ATTR),
									txt=(null == elem) ? null : elem.getAttribute(MESSAGE_PROPERTY),
									ttv=(null == elem) ? null : elem.getAttribute(MESSAGE_TYPE_PROPERTY),
									oov=(null == elem) ? null : elem.getAttribute(OPTION_TYPE_PROPERTY);
    	final OptionPaneMessageType	mtv=OptionPaneMessageType.fromString(ttv);
    	final OptionPaneConfirmType	otv=OptionPaneConfirmType.fromString(oov);
    	final Icon					icon=loadMessageIcon(elem, resLoader);

    	String	msg=txt;
    	try
    	{
			if ((txt != null) && (txt.length() > 0) && useFormat)
				msg = String.format(txt, args);
    	}
    	catch(RuntimeException e)
    	{
    		msg = txt;
    	}

    	return showConfirmDialog(parentComponent, msg, ttl, otv, mtv, icon);
    }
   
    public static final int formatConfirmDialog (
    		Component parentComponent, Element elem,
    		ReflectiveResourceLoaderContext resContext, boolean useFormat,
    		Object ... args)
    {
    	return formatConfirmDialog(parentComponent, elem, getReflectiveResourceLoader(elem, resContext), useFormat, args);
    }

    public static int formatConfirmDialog (
    		Component parentComponent, Element elem, boolean useFormat, Object ... args)
    {
    	return formatConfirmDialog(parentComponent, elem, AbstractXmlProxyConverter.getDefaultLoader(), useFormat, args);
    }

    public static final int showConfirmDialog (Component parentComponent, Element elem, ReflectiveResourceLoader resLoader)
    {
    	return formatConfirmDialog(parentComponent, elem, resLoader, false);
    }

    public static final int showConfirmDialog (Component parentComponent, Element elem, ReflectiveResourceLoaderContext resContext)
    {
    	return formatConfirmDialog(parentComponent, elem, resContext, false);
    }

    public static final int showConfirmDialog (Component parentComponent, Element elem)
    {
    	return showConfirmDialog(parentComponent, elem, AbstractXmlProxyConverter.getDefaultLoader());
    }

    ///////////////////////// input dialog /////////////////////////////
    
    public static final Object showInputDialog (Component parentComponent,
            Object message, String title, OptionPaneMessageType mtv, Icon icon,
            Object initialSelectionValue, Object ... selectionValues)
    {
    	return showInputDialog(parentComponent, message, title, (null == mtv) ? PLAIN_MESSAGE : mtv.getTypeValue(), icon, selectionValues, initialSelectionValue);
    }

    public static final Object showInputDialog (Component parentComponent,
            Object message, String title, OptionPaneMessageType mtv, 
            Object initialSelectionValue, Object ... selectionValues)
    {
    	return showInputDialog(parentComponent, message, title, mtv, null, initialSelectionValue, selectionValues); 
    }

    public static final Object formatInputDialog (
    		Component parentComponent, Element elem,
    		Object[] selectionValues, Object initialSelectionValue,
    		ReflectiveResourceLoader resLoader, boolean useFormat,
    		Object ... args)
    {
    	final String				ttl=(null == elem) ? null : elem.getAttribute(TITLE_ATTR),
									txt=(null == elem) ? null : elem.getAttribute(MESSAGE_PROPERTY),
									ttv=(null == elem) ? null : elem.getAttribute(MESSAGE_TYPE_PROPERTY);
		final OptionPaneMessageType	mtv=OptionPaneMessageType.fromString(ttv);
    	final Icon					icon=loadMessageIcon(elem, resLoader);

    	String	msg=txt;
    	try
    	{
    		if ((txt != null) && (txt.length() > 0) && useFormat)
    			msg = String.format(txt, args);
    	}
    	catch(RuntimeException e)
    	{
    		msg = txt;
    	}

    	return showInputDialog(parentComponent, msg, ttl, mtv, icon, initialSelectionValue, selectionValues);
    }

    public static final Object formatInputDialog (
    		Component parentComponent, Element elem,
    		Object[] selectionValues, Object initialSelectionValue,
    		ReflectiveResourceLoaderContext resContext, boolean useFormat,
    		Object ... args)
    {
    	return formatInputDialog(parentComponent, elem, selectionValues,initialSelectionValue, getReflectiveResourceLoader(elem, resContext), useFormat, args);

    }

    public static final Object formatInputDialog (
    		Component parentComponent, Element elem,
    		Object[] selectionValues, Object initialSelectionValue,
    		boolean useFormat, Object ... args)
    {
    	return formatInputDialog(parentComponent, elem, selectionValues, initialSelectionValue, AbstractXmlProxyConverter.getDefaultLoader(), useFormat, args);
    }

    public static final Object showInputDialog (
    		Component parentComponent, Element elem,
    		ReflectiveResourceLoader resLoader,
    		Object initialSelectionValue, Object ... selectionValues)
    {
    	return formatInputDialog(parentComponent, elem, selectionValues, initialSelectionValue, resLoader, false);
    }

    public static final Object showInputDialog (
    		Component parentComponent, Element elem,
    		ReflectiveResourceLoaderContext resContext,
    		Object initialSelectionValue, Object ... selectionValues)
    {
    	return formatInputDialog(parentComponent, elem, selectionValues, initialSelectionValue, resContext, false);
    }

    public static final Object showInputDialog (
    		Component parentComponent, Element elem,
    		Object initialSelectionValue, Object ... selectionValues)
    {
    	return showInputDialog(parentComponent, elem, AbstractXmlProxyConverter.getDefaultLoader(), initialSelectionValue, selectionValues);
    }

    ///////////////////// mapped resources ////////////////////

	protected static final Element getMessageElement (Map<?,? extends Element> mm, Object msgKey)
	{
		if ((null == mm) || (mm.size() <= 0) || (null == msgKey))
			return null;

		if ((msgKey instanceof CharSequence)
		 && (((CharSequence) msgKey).length() <= 0))
			return null;

		return mm.get(msgKey);
	}

	public static final void formatMessageDialog (
			Component parent, Map<?,? extends Element> mm, Object msgKey,
			ReflectiveResourceLoader resLoader, boolean useFormat, Object ... args)
	{
		formatMessageDialog(parent, getMessageElement(mm, msgKey), resLoader, useFormat, args);
	}

	public static final void formatMessageDialog (
			Component parent, Map<?,? extends Element> mm, Object msgKey,
			ReflectiveResourceLoaderContext resContext, boolean useFormat, Object ... args)
	{
		formatMessageDialog(parent, getMessageElement(mm, msgKey), resContext, useFormat, args);
	}

	public static final void formatMessageDialog (
			Component parent, Map<?,? extends Element> mm, Object msgKey, boolean useFormat, Object ... args)
	{
		formatMessageDialog(parent, getMessageElement(mm, msgKey), useFormat, args);
	}

	public static final void showMessageDialog (
			Component parent, Map<?,? extends Element> mm, Object msgKey, ReflectiveResourceLoader resLoader)
	{
		formatMessageDialog(parent, mm, msgKey, resLoader, false);
	}

	public static final void showMessageDialog (
			Component parent, Map<?,? extends Element> mm, Object msgKey, ReflectiveResourceLoaderContext resContext)
	{
		formatMessageDialog(parent, mm, msgKey, resContext, false);
	}

	public static final void showMessageDialog (
			Component parent, Map<?,? extends Element> mm, Object msgKey)
	{
		showMessageDialog(parent, getMessageElement(mm, msgKey));
	}

	//////////////////////// confirmations //////////////////////////////
	
    public static final int formatConfirmDialog (
    		Component parent, Map<?,? extends Element> mm, Object msgKey,
    		ReflectiveResourceLoader resLoader, boolean useFormat,
    		Object ... args)
    {
    	return formatConfirmDialog(parent, getMessageElement(mm, msgKey), resLoader, useFormat, args);
    }

    public static final int formatConfirmDialog (
    		Component parent, Map<?,? extends Element> mm, Object msgKey,
    		ReflectiveResourceLoaderContext resContext, boolean useFormat,
    		Object ... args)
    {
    	return formatConfirmDialog(parent, getMessageElement(mm, msgKey), resContext, useFormat, args);
    }

    public static final int formatConfirmDialog (
    		Component parent, Map<?,? extends Element> mm, Object msgKey,
    		boolean useFormat, Object ... args)
    {
    	return formatConfirmDialog(parent, getMessageElement(mm, msgKey), useFormat, args);
    }
    
    public static final int showConfirmDialog (
    		Component parent, Map<?,? extends Element> mm, Object msgKey, ReflectiveResourceLoader resLoader)
    {
    	return formatConfirmDialog(parent, mm, msgKey, resLoader, false);
    }

    public static final int showConfirmDialog (
    		Component parent, Map<?,? extends Element> mm, Object msgKey, ReflectiveResourceLoaderContext resContext)
    {
    	return formatConfirmDialog(parent, mm, msgKey, resContext, false);
    }

    public static final int showConfirmDialog (
    		Component parent, Map<?,? extends Element> mm, Object msgKey)
    {
    	return showConfirmDialog(parent, getMessageElement(mm, msgKey));
    }

    ///////////////////////// input //////////////////////////////
    
    public static final Object formatInputDialog (
    		Component parent, Map<?,? extends Element> mm, Object msgKey,
    		Object[] selectionValues, Object initialSelectionValue,
    		ReflectiveResourceLoader resLoader, boolean useFormat,
    		Object ... args)
    {
    	return formatInputDialog(parent, getMessageElement(mm, msgKey), selectionValues, initialSelectionValue, resLoader, useFormat, args);
    }

    public static final Object formatInputDialog (
    		Component parent, Map<?,? extends Element> mm, Object msgKey,
    		Object[] selectionValues, Object initialSelectionValue,
    		ReflectiveResourceLoaderContext resContext, boolean useFormat,
    		Object ... args)
    {
    	return formatInputDialog(parent, getMessageElement(mm, msgKey), selectionValues, initialSelectionValue, resContext, useFormat, args);
    }

    public static final Object formatInputDialog (
    		Component parent, Map<?,? extends Element> mm, Object msgKey,
    		Object[] selectionValues, Object initialSelectionValue,
    		boolean useFormat, Object ... args)
    {
    	return formatInputDialog(parent, getMessageElement(mm, msgKey), selectionValues, initialSelectionValue, useFormat, args);
    }
    
    public static final Object showInputDialog (
    		Component parent, Map<?,? extends Element> mm, Object msgKey,
    		ReflectiveResourceLoader resLoader,
    		Object initialSelectionValue, Object ... selectionValues)
    {
    	return formatInputDialog(parent, mm, msgKey, selectionValues, initialSelectionValue, resLoader, false);
    }

    public static final Object showInputDialog (
    		Component parent, Map<?,? extends Element> mm, Object msgKey,
    		ReflectiveResourceLoaderContext resContext,
    		Object initialSelectionValue, Object ... selectionValues)
    {
    	return formatInputDialog(parent, mm, msgKey, selectionValues, initialSelectionValue, resContext, false);
    }

    public static final Object showInputDialog (
    		Component parent, Map<?,? extends Element> mm, Object msgKey,
    		Object initialSelectionValue, Object ... selectionValues)
    {
    	return showInputDialog(parent, getMessageElement(mm, msgKey), initialSelectionValue, selectionValues);
    }
}
