/*
 *
 */
package net.community.chest.awt.menu;

import java.awt.Menu;
import java.awt.MenuItem;

import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <M> The reflected {@link Menu} type
 * @author Lyor G.
 * @since Sep 7, 2008 3:49:38 PM
 */
public class MenuReflectiveProxy<M extends Menu> extends MenuItemReflectiveProxy<M> {
    public MenuReflectiveProxy (Class<M> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected MenuReflectiveProxy (Class<M> objClass, boolean registerAsDefault)
            throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public M createMenuSeparator (final M src, final Element elem) throws Exception
    {
        if (null == elem)    // just so compiler does not complain about unreferenced parameter
            throw new IllegalArgumentException(ClassUtil.getExceptionLocation(getClass(), "createMenuSeparator") + " no " + Element.class.getSimpleName() + " instance");

        src.addSeparator();
        return src;
    }

    // XML sub-elements names
    public static final String    SEPARATOR_ELEMNAME="separator";
    public boolean isSeparatorElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, SEPARATOR_ELEMNAME);
    }

    public static final String    MENU_ELEMNAME=Menu.class.getSimpleName().toLowerCase();
    public boolean isSubMenuElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, MENU_ELEMNAME);
    }

    public XmlValueInstantiator<? extends Menu> getMenuConverter (final Element elem) throws Exception
    {
        return (null == elem) ? null : this;
    }

    public Menu createSubMenu (final M src, final Element elem) throws Exception
    {
        final XmlValueInstantiator<? extends Menu>    proxy=getMenuConverter(elem);
        final Menu                                    subMenu=proxy.fromXml(elem);
        if (subMenu != null)
            src.add(subMenu);

        return subMenu;
    }

    public static final String    ITEM_ELEMNAME="item";
    public boolean isSubItemElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, ITEM_ELEMNAME);
    }

    public static final String    CHECKBOX_ELEM_NAME="checkbox";
    public boolean isCheckboxMenuItemElement (final Element elem, final String itemClass)
    {
        return isMatchingAttribute(elem, itemClass, CHECKBOX_ELEM_NAME);
    }

    public XmlValueInstantiator<? extends MenuItem> getSubItemConverter (final Element elem) throws Exception
    {
        final String    itemClass=elem.getAttribute(CLASS_ATTR);
        if (isCheckboxMenuItemElement(elem, itemClass))
            return CheckboxMenuItemReflectiveProxy.CHECKBOX;
        else    // default
            return MenuItemReflectiveProxy.MENUITEM;
    }

    public MenuItem createSubItemInstance (final Element elem) throws Exception
    {
        final XmlValueInstantiator<? extends MenuItem>    proxy=getSubItemConverter(elem);
        final MenuItem                                    item=(null == proxy) ? null : proxy.fromXml(elem);
        return item;
    }

    public MenuItem createSubItem (final M src, final Element elem) throws Exception
    {
        final MenuItem    item=createSubItemInstance(elem);
        if (item != null)
            src.add(item);

        return item;
    }
    /*
     * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
     */
    @Override
    public M fromXmlChild (final M src, final Element elem) throws Exception
    {
        final String    tagName=elem.getTagName();
        if (isSubMenuElement(elem, tagName))
        {
            createSubMenu(src, elem);
            return src;
        }
        else if (isSubItemElement(elem, tagName))
        {
            createSubItem(src, elem);
            return src;
        }
        else if (isSeparatorElement(elem, tagName))
            return createMenuSeparator(src, elem);

        return super.fromXmlChild(src, elem);
    }

    public static final MenuReflectiveProxy<Menu>    MENU=
                new MenuReflectiveProxy<Menu>(Menu.class, true);
}
