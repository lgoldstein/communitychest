/*
 *
 */
package net.community.chest.awt.attributes;

import java.awt.Component;
import java.awt.Font;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.swing.Icon;

import net.community.chest.dom.proxy.ReflectiveResourceLoader;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.AttributeMethodType;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 30, 2008 8:46:05 AM
 */
public final class AttrUtils {
    private AttrUtils ()
    {
        // no instance
    }
    /**
     * Static {@link Map} used to cache results of <code>isTextable/Iconable/etc.</code>
     * checks. Key=&quot;pure&quot; method name, value={@link Map} whose
     * whose key=class path, value={@link AttributeAccessor} showing if the class has
     * (or not) the required method(s)
     */
    private static Map<String,Map<String,AttributeAccessor>>    _cpMap    /* =null */;
    private static final synchronized Map<String,Map<String,AttributeAccessor>> getCompsMap ()
    {
        if (null == _cpMap)
            _cpMap = new TreeMap<String,Map<String,AttributeAccessor>>(String.CASE_INSENSITIVE_ORDER);
        return _cpMap;
    }

    public static final AttributeAccessor getComponentClassPropertyAccessor (final Class<?>    c,
                                                                              final String    propName,
                                                                              final Class<?>    propType)
    {
        if ((null == c)
         || (null == propName) || (propName.length() <= 0)
         || (null == propType))
            return null;

        final Map<String,Map<String,AttributeAccessor>>    cpm=getCompsMap();
        Map<String,AttributeAccessor>                    cmm=null;
        synchronized(cpm)
        {
            if (null == (cmm=cpm.get(propName)))
            {
                cmm = new TreeMap<String,AttributeAccessor>();
                cpm.put(propName, cmm);
            }
        }

        AttributeAccessor    aa=null;
        final String        cn=c.getName();
        synchronized(cmm)
        {
            aa = cmm.get(cn);
        }

        if (null == aa)
        {
            if (null == (aa=AttributeMethodType.getPropertyAccessor(c, propName, propType)))
                aa = new AttributeAccessor(propName);    // means property does not exist

            synchronized(cmm)
            {
                cmm.put(cn, aa);
            }
        }

        if ((aa.getGetter() != null) || (aa.getSetter() != null))
            return aa;

        // if no getter and no setter then return null
        return null;
    }

    public static final AttributeAccessor getComponentPropertyAccessor (final Object    o,
                                                                         final String    propName,
                                                                         final Class<?>    propType)
    {
        return (null == o) ? null : getComponentClassPropertyAccessor(o.getClass(), propName, propType);
    }

    public static final <V> V getComponentProperty (final Object     o,
                                                    final String     propName,
                                                    final Class<V>    propType) throws RuntimeException
    {
        try
        {
            final AttributeAccessor    aa=getComponentPropertyAccessor(o, propName, propType);
            final Method            gm=(null == aa) ? null : aa.getGetter();
            final Object            tv=(null == gm) ? null : gm.invoke(o, AttributeAccessor.EMPTY_OBJECTS_ARRAY);
            return (null == tv) ? null : propType.cast(tv);
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }

    public static final void setComponentProperty (final Object     o,
                                                   final String     propName,
                                                   final Object        propVal,
                                                   final Class<?>    propType)
        throws RuntimeException
    {
        try
        {
            final AttributeAccessor    aa=getComponentPropertyAccessor(o, propName, propType);
            final Method            sm=(null == aa) ? null : aa.getSetter();
            if (sm != null)
                sm.invoke(o, propVal);
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }

    public static final AttributeAccessor getTextableComponentClassAccessor (final Class<?> c)
    {
        return getComponentClassPropertyAccessor(c, Textable.ATTR_NAME, Textable.ATTR_TYPE);
    }

    public static final AttributeAccessor getTextableComponentAccessor (final Object o)
    {
        return (null == o) ? null : getTextableComponentClassAccessor(o.getClass());
    }
    /**
     * Invokes the <code>getText</code> method (if any) on the supplied object
     * @param o The object whose "getText" method we want - may be null (in
     * which case nothing occurs)
     * @return Invocation result (null if no object)
     * @throws RuntimeException if invocation error occurs
     */
    public static final String getComponentText (final Object o) throws RuntimeException
    {
        return getComponentProperty(o, Textable.ATTR_NAME, String.class);
    }
    /**
     * Invokes the <code>setText</code> method (if any) on the supplied object
     * @param o The object whose "setText" method we want - may be null (in
     * which case nothing occurs)
     * @param t The text value to be set
     * @throws RuntimeException if invocation error occurs
     */
    public static final void setComponentText (final Object o, final String t) throws RuntimeException
    {
        if (o instanceof Textable)
            ((Textable) o).setText(t);
        else
            setComponentProperty(o, Textable.ATTR_NAME, t, Textable.ATTR_TYPE);
    }
    /**
     * Checks if a component has a <code>get/setText</code> method
     * @param c The {@link Class} to check
     * @return <code>true</code> if the component has a <code>get/setText</code>
     * method. <B>Note:</B> the code first check if it is a &quot;well-known&quot;
     * {@link Class} that is known to have the methods. If not, then it uses
     * reflection API in order to check for the existence of the methods.
     */
    public static final boolean isTextableComponentClass (Class<?> c)
    {
        return (getTextableComponentClassAccessor(c) != null);
    }
    /**
     * Checks if a component has a <code>get/setText</code> method
     * @param o The {@link Object} to check
     * @return <code>true</code> if the component has a <code>get/setText</code>
     * method. <B>Note:</B> the code first check if it is a &quot;well-known&quot;
     * {@link Class} that is known to have the methods. If not, then it uses
     * reflection API in order to check for the existence of the methods.
     */
    public static final boolean isTextableComponent (final Object o)
    {
        return (null == o) ? false : isTextableComponentClass(o.getClass());
    }

    public static final AttributeAccessor getIconableComponentClassAccessor (final Class<?> c)
    {
        return getComponentClassPropertyAccessor(c, Iconable.ATTR_NAME, Iconable.ATTR_TYPE);
    }

    public static final AttributeAccessor getIconableComponentAccessor (final Object o)
    {
        return (null == o) ? null : getIconableComponentClassAccessor(o.getClass());
    }
    /**
     * Invokes the <code>getIcon</code> method (if any) on the supplied object
     * @param o The object whose "getIcon" method we want - may be null (in
     * which case nothing occurs)
     * @return Invocation result (null if no object)
     * @throws RuntimeException if invocation error occurs
     */
    public static final Icon getComponentIcon (final Object o) throws RuntimeException
    {
        return getComponentProperty(o, Iconable.ATTR_NAME, Icon.class);
    }
    /**
     * Invokes the <code>setIcon</code> method (if any) on the supplied object
     * @param o The object whose "setIcon" method we want - may be null (in
     * which case nothing occurs)
     * @param i The {@link Icon} value to be set
     * @throws RuntimeException if invocation error occurs
     */
    public static final void setComponentIcon (final Object o, final Icon i) throws RuntimeException
    {
        if (o instanceof Iconable)
            ((Iconable) o).setIcon(i);
        else
            setComponentProperty(o, Iconable.ATTR_NAME, i, Iconable.ATTR_TYPE);
    }
    /**
     * Checks if a component has a <code>get/setIcon</code> method
     * @param c The {@link Class} to check
     * @return <code>true</code> if the component has a <code>get/setIcon</code>
     * method. <B>Note:</B> the code first check if it is a &quot;well-known&quot;
     * {@link Class} that is known to have the methods. If not, then it uses
     * reflection API in order to check for the existence of the methods.
     */
    public static final boolean isIconableComponentClass (Class<?> c)
    {
        return (getIconableComponentClassAccessor(c) != null);
    }
    /**
     * Checks if a component has a <code>get/setIcon</code> method
     * @param o The {@link Object} to check
     * @return <code>true</code> if the component has a <code>get/setIcon</code>
     * method. <B>Note:</B> the code first check if it is a &quot;well-known&quot;
     * {@link Class} that is known to have the methods. If not, then it uses
     * reflection API in order to check for the existence of the methods.
     */
    public static final boolean isIconableComponent (final Object o)
    {
        return (null == o) ? false : isIconableComponentClass(o.getClass());
    }

    public static final AttributeAccessor getTooltipedComponentClassAccessor (final Class<?> c)
    {
        return getComponentClassPropertyAccessor(c, Tooltiped.ATTR_NAME, Tooltiped.ATTR_TYPE);
    }

    public static final AttributeAccessor getTooltipedComponentAccessor (final Object o)
    {
        return (null == o) ? null : getTooltipedComponentClassAccessor(o.getClass());
    }
    /**
     * Invokes the <code>getToolTipText</code> method (if any) on the supplied
     * object
     * @param o The object whose "getToolTipText" method we want - may be null
     * (in which case nothing occurs)
     * @return Invocation result (null if no object)
     * @throws RuntimeException if invocation error occurs
     */
    public static final String getComponentToolTipText (final Object o) throws RuntimeException
    {
        return getComponentProperty(o, Tooltiped.ATTR_NAME, String.class);
    }
    /**
     * Invokes the <code>setToolTipText</code> method (if any) on the supplied object
     * @param o The object whose "setToolTipText" method we want - may be null (in
     * which case nothing occurs)
     * @param t The text value to be set
     * @throws RuntimeException if invocation error occurs
     */
    public static final void setComponentToolTipText (final Object o, final String t) throws RuntimeException
    {
        if (o instanceof Tooltiped)
            ((Tooltiped) o).setToolTipText(t);
        else
            setComponentProperty(o, Tooltiped.ATTR_NAME, t, Tooltiped.ATTR_TYPE);
    }
    /**
     * Checks if a component has a <code>get/setTooltipText</code> method
     * @param c The {@link Class} to check
     * @return <code>true</code> if the component has a <code>get/setTooltipText</code>
     * method. <B>Note:</B> the code first check if it is a &quot;well-known&quot;
     * {@link Class} that is known to have the methods. If not, then it uses
     * reflection API in order to check for the existence of the methods.
     */
    public static final boolean isTooltipedComponentClass (Class<?> c)
    {
        return (getTooltipedComponentClassAccessor(c) != null);
    }
    /**
     * Checks if a component has a <code>get/setTooltipText</code> method
     * @param o The {@link Object} to check
     * @return <code>true</code> if the component has a <code>get/setTooltipText</code>
     * method. <B>Note:</B> the code first check if it is a &quot;well-known&quot;
     * {@link Class} that is known to have the methods. If not, then it uses
     * reflection API in order to check for the existence of the methods.
     */
    public static final boolean isTooltipedComponent (final Object o)
    {
        return (null == o) ? false : isTooltipedComponentClass(o.getClass());
    }
    /**
     * Updates the <code>enabled</code> of the provided {@link Component}-s
     * @param enabled The required new state
     * @param comps The {@link Component}-s to update - if a component
     * already has the required state (via {@link Component#isEnabled()})
     * then its {@link Component#setEnabled(boolean)} method is <U>not called</U>.
     * @return A {@link Collection} of the  {@link Component}-s whose state
     * was <U>changed</U>
     */
    public static final Collection<Component> setComponentEnabledState (
            final boolean enabled, final Collection<? extends Component> comps)
    {
        if ((null == comps) || (comps.size() <= 0))
            return null;

        Collection<Component>    ret=null;
        for (final Component c : comps)
        {
            if ((null == c) || (c.isEnabled() == enabled))
                continue;

            c.setEnabled(enabled);
            if (null == ret)
                ret = new LinkedList<Component>();
            ret.add(c);
        }

        return ret;
    }
    /**
     * Updates the <code>enabled</code> of the provided {@link Component}-s
     * @param enabled The required new state
     * @param comps The {@link Component}-s to update - if a component
     * already has the required state (via {@link Component#isEnabled()})
     * then its {@link Component#setEnabled(boolean)} method is <U>not called</U>.
     * @return A {@link Collection} of the  {@link Component}-s whose state
     * was <U>changed</U>
     */
    public static final Collection<Component> setComponentEnabledState (
            final boolean enabled, final Component ... comps)
    {
        return ((null == comps) || (comps.length <= 0)) ? null : setComponentEnabledState(enabled, Arrays.asList(comps));
    }

    public static final <I> I getComponentAttributeInterface (
            final Object o, final Class<I> ifc, final String attrName, final Class<?> attrType)
    {
        if (null == o)
            return null;

        final AttributeAccessor    aa=getComponentPropertyAccessor(o, attrName, attrType);
        if (null == aa)
            throw new NoSuchElementException("getComponentAttributeInterface(" + attrName + ")[" + attrType.getName() + "] no accessor");

        return AttributeAccessor.getAttributeAccessorInterface(o, aa, ifc);
    }
    /**
     * @param <R> Type of resource being loaded
     * @param org Original {@link Map} of the resources - key=resource key
     * @param em A {@link Map} of the XML {@link Element}-s to be used for
     * loading the resources. Key=resource key value - same one used in the
     * original/return value, value={@link Element} to use for retrieving the
     * resource's value string. If <code>null</code> then one will be
     * <U>automatically</U> allocated (using case <U>insensitive</U> key(s))
     * @param aName The attribute to use for retrieving the resource value
     * {@link String} - if <code>null</code>/empty then nothing is loaded
     * @param resClass The expected resources {@link Class} - if not specified
     * then nothing is loaded
     * @param resLoader The {@link ReflectiveResourceLoader} to use - if
     * <code>null</code> then nothing is loaded
     * @param errIfDuplicate If TRUE and a resource is already mapped for a
     * key then throw an {@link IllegalStateException}
     * @return A {@link Map} whose key=the resource key, value=the resource
     * value (if loader did not return <code>null</code>
     * @throws Exception If failed to load resource or duplicate found and
     * duplicates not allowed
     */
    public static final <R> Map<String,R> updateMappedResources (
                            final Map<String,R>                    org,
                            final Map<String,? extends Element>    em,
                            final String                        aName,
                            final Class<R>                        resClass,
                            final ReflectiveResourceLoader        resLoader,
                            final boolean                        errIfDuplicate)
        throws Exception
    {
        final Collection<? extends Map.Entry<String,? extends Element>>    el=
            ((null == em) || (em.size() <= 0)) ? null : em.entrySet();
        if ((null == el) || (el.size() <= 0)
         || (null == aName) || (aName.length() <= 0)
         || (null == resClass)
         || (null == resLoader))
            return org;

        Map<String,R>    ret=org;
        for (final Map.Entry<String,? extends Element>    ee : el)
        {
            final String    rk=(null == ee) ? null : ee.getKey();
            final Element    re=(null == ee) ? null : ee.getValue();
            final String    rv=(null == re) ? null : re.getAttribute(aName);
            if ((null == rk) || (rk.length() <= 0)
             || (null == rv) || (rv.length() <= 0))
                continue;

            final R    rr=resLoader.loadAttributeResource(resClass, null, rk, rv);
            if (null == rr)
                continue;

            if (null == ret)
                ret = new TreeMap<String,R>(String.CASE_INSENSITIVE_ORDER);

            final R    prev=ret.put(rk, rr);
            if ((prev != null) && errIfDuplicate)
                throw new IllegalStateException("updateMappedResources(" + rk + ") multiple resources found");
        }

        return ret;
    }

    public static final <R> Map<String,R> getMappedResources (
                        final Map<String,? extends Element>    em,
                        final String                        aName,
                        final Class<R>                        resClass,
                        final ReflectiveResourceLoader        resLoader)
        throws Exception
    {
        return updateMappedResources(null, em, aName, resClass, resLoader, true);
    }

    public static final Map<String,Icon> getMappedIcons (
            final Map<String,? extends Element>    em,
            final ReflectiveResourceLoader        resLoader)
           throws Exception
       {
        return getMappedResources(em, Iconable.ATTR_NAME, Icon.class, resLoader);
       }

    public static final Map<String,Font> getMappedFonts(
            final Map<String,? extends Element>    em,
            final ReflectiveResourceLoader        resLoader)
           throws Exception
       {
        return getMappedResources(em, FontControl.ATTR_NAME, Font.class, resLoader);
       }

    public static final Map<String,String> updateMappedTextResources (
            final Map<String,String>            org,
            final Map<String,? extends Element>    em,
            final String                        aName,
            final boolean                        errIfDuplicate)
        throws IllegalStateException
    {
        final Collection<? extends Map.Entry<String,? extends Element>>    el=
            ((null == em) || (em.size() <= 0)) ? null : em.entrySet();
        if ((null == el) || (el.size() <= 0)
         || (null == aName) || (aName.length() <= 0))
            return org;

        Map<String,String>    ret=org;
        for (final Map.Entry<String,? extends Element>    ee : el)
        {
            final String    rk=(null == ee) ? null : ee.getKey();
            final Element    re=(null == ee) ? null : ee.getValue();
            final String    rv=(null == re) ? null : re.getAttribute(aName);
            if ((null == rk) || (rk.length() <= 0)
             || (null == rv) || (rv.length() <= 0))
                continue;

            if (null == ret)
                ret = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);

            final String    prev=ret.put(rk, rv);
            if ((prev != null) && errIfDuplicate)
                throw new IllegalStateException("updateMappedTextResources(" + rk + ") multiple resources found: prev=" + prev + "/new=" + rv);
        }

        return ret;
    }

    public static final Map<String,String> getMappedTextResources (
                                final Map<String,? extends Element>    em,
                                final String                        aName)
        throws IllegalStateException
    {
        return updateMappedTextResources(null, em, aName, true);
    }

    public static final Map<String,String> getMappedTextResources (final Map<String,? extends Element>    em)
        throws IllegalStateException
    {
        return getMappedTextResources(em, Textable.ATTR_NAME);
    }

    public static final Map<String,String> getMappedTitleResources (final Map<String,? extends Element>    em)
        throws IllegalStateException
    {
        return getMappedTextResources(em, Titled.ATTR_NAME);
    }

    public static final Map<String,String> getMappedTooltipResources (final Map<String,? extends Element>    em)
        throws IllegalStateException
    {
        return getMappedTextResources(em, Tooltiped.ATTR_NAME);
    }
}
