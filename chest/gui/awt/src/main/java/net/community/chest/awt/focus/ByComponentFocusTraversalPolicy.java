/*
 *
 */
package net.community.chest.awt.focus;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.Arrays;
import java.util.List;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 4, 2010 12:35:06 PM
 */
public class ByComponentFocusTraversalPolicy extends FocusTraversalPolicy {
    private List<Component>    _componentsList;
    public List<Component> getComponentsList ()
    {
        return _componentsList;
    }

    public void setComponentsList (List<Component> componentsList)
    {
        _componentsList = componentsList;
    }
    /**
     * @param comps The {@link List} of {@link Component}-s according to the
     * desired <U>order</U>
     */
    public ByComponentFocusTraversalPolicy (List<Component> comps)
    {
        _componentsList = comps;
    }
    /**
     * @param comps The list of {@link Component}-s according to the
     * desired <U>order</U>
     */
    public ByComponentFocusTraversalPolicy (Component ... comps)
    {
        this(((null == comps) || (comps.length <= 0)) ? null : Arrays.asList(comps));
    }
    /*
     * @see java.awt.FocusTraversalPolicy#getComponentAfter(java.awt.Container, java.awt.Component)
     */
    @Override
    public Component getComponentAfter (Container aContainer, Component aComponent)
    {
        final List<? extends Component>    cl=getComponentsList();
        final int                        numComps=(null == cl) ? 0 : cl.size();
        if (numComps <= 0)
            return aComponent;

        final int    cIndex=cl.indexOf(aComponent);
        if ((cIndex < 0) || (cIndex >= numComps))
            return aComponent;

        if (cIndex < (numComps - 1))
            return cl.get(cIndex + 1);
        else
            return cl.get(0);
    }
    /*
     * @see java.awt.FocusTraversalPolicy#getComponentBefore(java.awt.Container, java.awt.Component)
     */
    @Override
    public Component getComponentBefore (Container aContainer, Component aComponent)
    {
        final List<? extends Component>    cl=getComponentsList();
        final int                        numComps=(null == cl) ? 0 : cl.size();
        if (numComps <= 0)
            return aComponent;

        final int    cIndex=cl.indexOf(aComponent);
        if ((cIndex < 0) || (cIndex >= numComps))
            return aComponent;

        if (cIndex > 0)
            return cl.get(cIndex - 1);
        else
            return cl.get(numComps - 1);
    }

    private Component    _defaultComp;
    /*
     * @see java.awt.FocusTraversalPolicy#getDefaultComponent(java.awt.Container)
     */
    @Override
    public Component getDefaultComponent (Container aContainer)
    {
        if (null == _defaultComp)
            return getFirstComponent(aContainer);
        return _defaultComp;
    }

    public void setDefaultComponent (Component c)
    {
        _defaultComp = c;
    }
    /*
     * @see java.awt.FocusTraversalPolicy#getFirstComponent(java.awt.Container)
     */
    @Override
    public Component getFirstComponent (Container aContainer)
    {
        final List<? extends Component>    cl=getComponentsList();
        if ((null == cl) || (cl.size() <= 0))
            return aContainer;

        return cl.get(0);
    }
    /*
     * @see java.awt.FocusTraversalPolicy#getLastComponent(java.awt.Container)
     */
    @Override
    public Component getLastComponent (Container aContainer)
    {
        final List<? extends Component>    cl=getComponentsList();
        final int                        numComps=(null == cl) ? 0 : cl.size();
        if (numComps <= 0)
            return aContainer;

        return cl.get(numComps - 1);
    }
}
