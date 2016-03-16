/*
 *
 */
package net.community.chest.awt.focus;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.community.chest.awt.AWTUtils;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * <P>Implements the {@link FocusTraversalPolicy} by using the
 * {@link Component#getName()} and a {@link List} of names
 * @author Lyor G.
 * @since Apr 4, 2010 11:43:00 AM
 */
public class ByNameFocusTraversalPolicy extends FocusTraversalPolicy {
    private List<String>    _namesList;
    public List<String> getNamesList ()
    {
        return _namesList;
    }

    public void setNamesList (List<String> namesList)
    {
        _namesList = namesList;
    }

    public List<String> addComponentName (String n)
    {
        List<String>    nl=getNamesList();
        if ((null == n) || (n.length() <= 0))
            return nl;

        if ((null == nl) || (nl.size() <= 0))
        {
            setNamesList(new ArrayList<String>());
            if (null == (nl=getNamesList()))
                throw new IllegalStateException("No names list available though created");
        }

        if (nl.add(n))
            return nl;

        return nl;
    }

    public List<String> addComponentName (Component c)
    {
        return addComponentName((null == c) ? null : c.getName());
    }
    /**
     * @param namesList The {@link List} of components names - according
     * to the <U>order</U> in which the components should be traversed
     */
    public ByNameFocusTraversalPolicy (List<String> namesList)
    {
        _namesList = namesList;
    }
    /**
     * @param names The array of components names - according
     * to the <U>order</U> in which the components should be traversed
     */
    public ByNameFocusTraversalPolicy (String ... names)
    {
        this(((null == names) || (names.length <= 0)) ? null : Arrays.asList(names));
    }

    public ByNameFocusTraversalPolicy ()
    {
        this((List<String>) null);
    }

    protected String getNextComponentName (String n)
    {
        if ((null == n) || (n.length() <= 0))
            return null;

        final List<String>    nl=getNamesList();
        final int            numNames=(null == nl) ? 0 : nl.size();
        if (numNames <= 0)
            return null;

        final int    nIndex=CollectionsUtils.findElementIndex(n, String.CASE_INSENSITIVE_ORDER, nl);
        if ((nIndex < 0) || (nIndex >= numNames))
            return null;

        if (nIndex < (numNames - 1))
            return nl.get(nIndex + 1);
        else
            return nl.get(0);
    }

    protected String getNextComponentName (Component c)
    {
        return getNextComponentName((null == c) ? null : c.getName());
    }

    protected Component getMatchingComponent (Container aContainer, String n, Component defComponent)
    {
        final Component    c=AWTUtils.findComponentByName(aContainer, n, false);
        if (null == c)
            return defComponent;

        return c;
    }
    /*
     * @see java.awt.FocusTraversalPolicy#getComponentAfter(java.awt.Container, java.awt.Component)
     */
    @Override
    public Component getComponentAfter (Container aContainer, Component aComponent)
    {
        final String    nextName=getNextComponentName(aComponent);
        return getMatchingComponent(aContainer, nextName, aComponent);
    }

    protected String getPrevComponentName (String n)
    {
        if ((null == n) || (n.length() <= 0))
            return null;

        final List<String>    nl=getNamesList();
        final int            numNames=(null == nl) ? 0 : nl.size();
        if (numNames <= 0)
            return null;

        final int    nIndex=CollectionsUtils.findElementIndex(n, String.CASE_INSENSITIVE_ORDER, nl);
        if ((nIndex < 0) || (nIndex >= numNames))
            return null;

        if (nIndex > 0)
            return nl.get(nIndex - 1);
        else
            return nl.get(numNames - 1);
    }

    protected String getPrevComponentName (Component c)
    {
        return getNextComponentName((null == c) ? null : c.getName());
    }
    /*
     * @see java.awt.FocusTraversalPolicy#getComponentBefore(java.awt.Container, java.awt.Component)
     */
    @Override
    public Component getComponentBefore (Container aContainer, Component aComponent)
    {
        final String    prevName=getPrevComponentName(aComponent);
        return getMatchingComponent(aContainer, prevName, aComponent);
    }

    private String    _defaultComponentName;
    /**
     * @return Name of the component to be returned for the
     * {@link #getDefaultComponent(Container)} call. If null/empty then
     * the 1st name in the list is used
     */
    public String getDefaultComponentName ()
    {
        return _defaultComponentName;
    }

    public void setDefaultComponentName (String n)
    {
        _defaultComponentName = n;
    }
    /*
     * @see java.awt.FocusTraversalPolicy#getDefaultComponent(java.awt.Container)
     */
    @Override
    public Component getDefaultComponent (Container aContainer)
    {
        final String    n=getDefaultComponentName();
        if ((null == n) || (n.length() <= 0))
            return getFirstComponent(aContainer);

        return getMatchingComponent(aContainer, n, aContainer);
    }
    /*
     * @see java.awt.FocusTraversalPolicy#getFirstComponent(java.awt.Container)
     */
    @Override
    public Component getFirstComponent (Container aContainer)
    {
        final List<String>    nl=getNamesList();
        final String        n=
            ((null == nl) || (nl.size() <= 0)) ? null : nl.get(0);
        return getMatchingComponent(aContainer, n, aContainer);
    }
    /*
     * @see java.awt.FocusTraversalPolicy#getLastComponent(java.awt.Container)
     */
    @Override
    public Component getLastComponent (Container aContainer)
    {
        final List<String>    nl=getNamesList();
        final int            numNames=(null == nl) ? 0 : nl.size();
        final String        n=(numNames <= 0) ? null : nl.get(numNames - 1);
        return getMatchingComponent(aContainer, n, aContainer);
    }

}
