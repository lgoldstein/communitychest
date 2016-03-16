package net.community.chest.swing.component.menu;

import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import net.community.chest.awt.attributes.Backgrounded;
import net.community.chest.awt.attributes.Enabled;
import net.community.chest.awt.attributes.FontControl;
import net.community.chest.awt.attributes.Foregrounded;
import net.community.chest.awt.attributes.Tooltiped;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 24, 2007 3:40:41 PM
 */
public class BaseMenuBar extends JMenuBar
        implements XmlConvertible<BaseMenuBar>, MenuItemExplorer, MenuExplorer,
                    FontControl, Tooltiped, Enabled, Foregrounded, Backgrounded {
    /**
     *
     */
    private static final long serialVersionUID = -6355775949080818661L;
    public BaseMenuBar ()
    {
        super();
    }

    protected XmlProxyConvertible<?> getMenuBarConverter (final Element elem)
    {
        return (null == elem) ? null : JMenuBarReflectiveProxy.BAR;
    }
    /*
     * @see net.community.chest.dom.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public BaseMenuBar fromXml (final Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getMenuBarConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    o=
            ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
        if (o != this)
            throw new IllegalStateException(ClassUtil.getExceptionLocation(getClass(), "fromXml") + " mismatched updated instances");

        return this;
    }

    public BaseMenuBar (final Element elem) throws Exception
    {
        final JMenuBar    bar=fromXml(elem);
        if (bar != this)    // not allowed
            throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " mismatched restored " + JMenuBar.class.getName() + " instances");
    }
    /*
     * @see net.community.chest.dom.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        // TODO implement toXml
        throw new UnsupportedOperationException(ClassUtil.getExceptionLocation(getClass(), "toXml") + " N/A");
    }

    private Map<String,JMenuItem>    _itemsMap    /* =null */;
    /*
     * @see net.community.chest.swing.component.menu.MenuItemExplorer#getItemsMap()
     */
    @Override
    public synchronized Map<String, ? extends JMenuItem> getItemsMap ()
    {
        if (null == _itemsMap)
            _itemsMap = MenuUtil.updateItemsMap(null, this, true);
        return _itemsMap;
    }
    /*
     * @see net.community.chest.swing.component.menu.MenuItemExplorer#resetItemsMap()
     */
    @Override
    public synchronized void resetItemsMap ()
    {
        if (_itemsMap != null)
            _itemsMap = null;
    }
    /*
     * @see net.community.chest.swing.component.menu.MenuItemExplorer#findMenuItemByCommand(java.lang.String)
     */
    @Override
    public JMenuItem findMenuItemByCommand (final String cmd)
    {
        if ((null == cmd) || (cmd.length() <= 0))
            return null;

        final Map<String, ? extends JMenuItem>    itemsMap=getItemsMap();
        return ((null == itemsMap) || (itemsMap.size() <= 0)) ? null : itemsMap.get(cmd);
    }
    /*
     * @see net.community.chest.swing.component.menu.MenuItemExplorer#addItemActionListenerByCommand(java.lang.String, java.awt.event.ActionListener)
     */
    @Override
    public JMenuItem addItemActionListenerByCommand (final String cmd, final ActionListener listener)
    {
        final JMenuItem    item=(null == listener) ? null : findMenuItemByCommand(cmd);
        if (item != null)
            item.addActionListener(listener);

        return item;
    }

    private Map<String,JMenu>    _menusMap    /* =null */;
    /*
     * @see net.community.chest.swing.component.menu.MenuExplorer#getMenusMap()
     */
    @Override
    public synchronized Map<String,? extends JMenu> getMenusMap ()
    {
        if (null == _menusMap)
            _menusMap = MenuUtil.updateMenusMap(null, this, true);
        return _menusMap;
    }
    /*
     * @see net.community.chest.swing.component.menu.MenuExplorer#resetMenusMap()
     */
    @Override
    public synchronized void resetMenusMap ()
    {
        if (_menusMap != null)
            _menusMap = null;
    }
    /*
     * @see net.community.chest.swing.component.menu.MenuExplorer#findMenuByCommand(java.lang.String)
     */
    @Override
    public JMenu findMenuByCommand (final String cmd)
    {
        if ((null == cmd) || (cmd.length() <= 0))
            return null;

        final Map<String,? extends JMenu>    menusMap=getMenusMap();
        return ((null == menusMap) || (menusMap.size() <= 0)) ? null : menusMap.get(cmd);
    }
    /*
     * @see net.community.chest.swing.component.menu.MenuExplorer#addMenuActionListenerByCommand(java.lang.String, java.awt.event.ActionListener, boolean)
     */
    @Override
    public JMenu addMenuActionListenerByCommand (String cmd, ActionListener listener, boolean recursive)
    {
        return MenuUtil.addMenuActionHandler(this, cmd, listener, recursive);
    }
}
