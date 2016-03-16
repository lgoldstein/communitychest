/*
 *
 */
package net.community.chest.ui.components.logging;

import java.util.Map;

import javax.swing.text.AttributeSet;

import org.w3c.dom.Element;

import net.community.chest.swing.component.text.JTextPaneReflectiveProxy;
import net.community.chest.util.logging.LogLevelWrapper;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @param <P> Type of {@link LoggingTextPane} being reflected
 * @author Lyor G.
 * @since Aug 2, 2009 10:31:00 AM
 */
public class LoggingTextPaneReflectiveProxy<P extends LoggingTextPane> extends JTextPaneReflectiveProxy<P> {
    protected LoggingTextPaneReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public LoggingTextPaneReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public Map<LogLevelWrapper,AttributeSet> setLogLevelAttributesMap (
            final P src, final Element root)
        throws Exception
    {
        return (null == src) ? null : src.setLogLevelAttributesMap(root);
    }

    public static final String    LOGLEVELS_MAP_ELEM_NAME="logLevelsMap";
    public boolean isLogLevelsMapElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, LOGLEVELS_MAP_ELEM_NAME);
    }
    /*
     * @see net.community.chest.swing.component.JComponentReflectiveProxy#fromXmlChild(javax.swing.JComponent, org.w3c.dom.Element)
     */
    @Override
    public P fromXmlChild (P src, Element elem) throws Exception
    {
        final String    tagName=elem.getTagName();
        if (isLogLevelsMapElement(elem, tagName))
        {
            setLogLevelAttributesMap(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }

    public static final LoggingTextPaneReflectiveProxy<LoggingTextPane>    LOGPANE=
        new LoggingTextPaneReflectiveProxy<LoggingTextPane>(LoggingTextPane.class, true);
}
