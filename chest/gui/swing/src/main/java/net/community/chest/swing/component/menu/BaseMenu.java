package net.community.chest.swing.component.menu;

import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.community.chest.awt.attributes.Backgrounded;
import net.community.chest.awt.attributes.Enabled;
import net.community.chest.awt.attributes.FontControl;
import net.community.chest.awt.attributes.Foregrounded;
import net.community.chest.awt.attributes.Iconable;
import net.community.chest.awt.attributes.Textable;
import net.community.chest.awt.attributes.Tooltiped;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 24, 2007 3:21:13 PM
 */
public class BaseMenu extends JMenu
        implements XmlConvertible<BaseMenu>, MenuItemExplorer, MenuExplorer,
                   Textable, Iconable, FontControl, Tooltiped, Enabled,
                   Foregrounded, Backgrounded {
    /**
     *
     */
    private static final long serialVersionUID = 1539937938837091847L;
    public BaseMenu ()
    {
        super();
    }

    public BaseMenu (String s)
    {
        super(s);
    }

    public BaseMenu (Action a)
    {
        super(a);
    }

    public BaseMenu (String s, boolean b)
    {
        super(s, b);
    }

    protected XmlProxyConvertible<?> getMenuConverter (final Element elem)
    {
        return (null == elem) ? null : JMenuReflectiveProxy.MENU;
    }
    /*
     * @see net.community.chest.dom.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public BaseMenu fromXml (final Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getMenuConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    o=
            ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
        if (o != this)
            throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + " mismatched initialization instances");

        return this;
    }

    public BaseMenu (final Element elem) throws Exception
    {
        final JMenu    menu=fromXml(elem);
        if (menu != this)    // not allowed
            throw new IllegalStateException("<init>(" + DOMUtils.toString(elem) + ") mismatched restored " + JMenu.class.getName() + ") instances");
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

        final Map<String,? extends JMenuItem>    itemsMap=getItemsMap();
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
