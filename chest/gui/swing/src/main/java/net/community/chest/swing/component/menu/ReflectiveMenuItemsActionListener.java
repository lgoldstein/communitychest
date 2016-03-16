package net.community.chest.swing.component.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.swing.JMenuItem;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.dom.ElementIndicatorExceptionContainer;
import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.swing.component.button.AbstractButtonReflectiveProxy;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Uses reflection API to invoke {@link Void} no-args methods whenever
 * a {@link JMenuItem} is chosen</P>
 *
 * @param <V> The type of value associated with each item
 * @author Lyor G.
 * @since Aug 2, 2007 2:47:01 PM
 */
public class ReflectiveMenuItemsActionListener<V> extends BaseTypedValuesContainer<V> implements ActionListener {
    private final Map<String,Method>    _actionsMap=new TreeMap<String,Method>(String.CASE_INSENSITIVE_ORDER);

    private V    _actionedInstance;
    /**
     * @return object instance to be used for reflection API to invoke the
     * action {@link Method}-s - may be null if all methods are <U>static</U>
     */
    public V getActionedInstance ()
    {
        return _actionedInstance;
    }

    public void setActionedInstance (V instance)
    {
        _actionedInstance = instance;
    }
    /**
     * @param valClass The associated menu item information type
     * @param actionedInstance object instance to be used for reflection API
     * to invoke the action {@link Method}-s - may NOT be null once commands
     * are executed - i.e., must call {@link #setActionedInstance(Object)}
     * <U>before</U> {@link #actionPerformed(ActionEvent)} is expected to be
     * called.
     */
    public ReflectiveMenuItemsActionListener (final Class<V> valClass, final V actionedInstance)
    {
        super(valClass);

        _actionedInstance = actionedInstance;
    }

    public ReflectiveMenuItemsActionListener (final Class<V> valClass)
    {
        this(valClass, null);
    }
    /**
     * @param item {@link JMenuItem} to which to add this class as an
     * {@link ActionListener}
     * @param actionName void/no-args {@link Method} name to be executed
     * if this menu item is selected. <B>Note:</B> a check is made as to
     * the method's <U>visibility</U> - and if it is not <code>public</code>
     * then access is granted to it anyway via {@link Method#setAccessible(boolean)}
     * @return previously assigned method to the command - null if none
     * @throws Exception if cannot use reflection API
     */
    public Method addMenuAction (final JMenuItem item, final String actionName) throws Exception
    {
        final String    cmd=(null == item) ? null : item.getActionCommand();
        final Class<V>    actClass=getValuesClass();
        final Method    act=(null == actClass) ? null : actClass.getMethod(actionName);
        final Class<?>    retType=(null == act) ? null : act.getReturnType();
        // make sure it is VOID
        if (null == retType)
            throw new IllegalStateException("addMenuAction(" + cmd + "=>" + actionName + ") no return type");
        if ((!Void.TYPE.isAssignableFrom(retType)) && (!Void.class.isAssignableFrom(retType)))
            throw new IllegalAccessException("addMenuAction(" + cmd + "=>" + actionName + ") illegal return type: " + retType.getName());

        // if not a public method then auto-grant access
        if (!Modifier.isPublic(act.getModifiers()))
            act.setAccessible(true);

        final Method    prev;
        synchronized(_actionsMap)
        {
            prev = _actionsMap.put(cmd, act);
        }
        item.addActionListener(this);
        return prev;
    }

    public Method addMenuAction (final MenuItemExplorer exp, final String cmd, final String actionName) throws Exception
    {
        return addMenuAction(exp.findMenuItemByCommand(cmd), actionName);
    }

    public static final String METHOD_ATTR="method";
    public Method addMenuAction (final MenuItemExplorer exp, final Element itemElem) throws Exception
    {
        final String    actionName=itemElem.getAttribute(METHOD_ATTR);
        if ((null == actionName) || (actionName.length() <= 0))
            return null;

        return addMenuAction(exp, itemElem.getAttribute(AbstractButtonReflectiveProxy.ACTION_COMMAND_ATTR), actionName);
    }

    public Collection<ElementIndicatorExceptionContainer> addAllActions (final MenuItemExplorer exp, final Element rootElem) throws Exception
    {
        final NodeList                            nodes=(null == rootElem) ? null : rootElem.getChildNodes();
        final int                                numNodes=(null == nodes) ? 0 : nodes.getLength();
        Collection<ElementIndicatorExceptionContainer>    ret=null;
        for (int    nIndex=0; nIndex < numNodes; nIndex++)
        {
            final Node    n=nodes.item(nIndex);
            if ((null == n) || (n.getNodeType() != Node.ELEMENT_NODE))
                continue;

            try
            {
                addMenuAction(exp, (Element) n);
            }
            catch(Exception e)
            {
                final ElementIndicatorExceptionContainer    ind=new ElementIndicatorExceptionContainer((Element) n, e);
                if (null == ret)
                    ret = new LinkedList<ElementIndicatorExceptionContainer>();
                ret.add(ind);
            }
        }

        return ret;
    }
    /*
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed (final ActionEvent ev)
    {
        final Object    src=
            (null == ev) /* should not happen */ ? null : ev.getSource();
        final String    cmd=
            (null == ev) /* should not happen */ ? null : ev.getActionCommand();
        if ((null == src) || (!(src instanceof JMenuItem))
         || (null == cmd) || (cmd.length() <= 0))
            throw new IllegalStateException("actionPerformed(" + cmd + ") no/bad source object");

        final Method    act;
        synchronized(_actionsMap)
        {
            act = _actionsMap.get(cmd);
        }
        if (null == act)
            throw new NoSuchElementException("actionPerformed(" + cmd + ") no descriptor/action");

        try
        {
            act.invoke(getActionedInstance(), AttributeAccessor.EMPTY_OBJECTS_ARRAY);
        }
        catch(Exception e)
        {
            Throwable    t=e;

            if (t instanceof InvocationTargetException)
                t = ((InvocationTargetException) t).getCause();
            if (t instanceof RuntimeException)
                throw (RuntimeException) t;
            throw new RuntimeException("actionPerformed(" + cmd + ") " + t.getClass().getName() + " on action invocation: " + t.getMessage());
        }
    }
}
