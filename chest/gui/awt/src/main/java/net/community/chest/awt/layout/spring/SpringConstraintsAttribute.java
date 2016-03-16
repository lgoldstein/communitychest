/*
 *
 */
package net.community.chest.awt.layout.spring;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.Spring;
import javax.swing.SpringLayout;

import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.set.SetsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>An {@link Enum} representing the possible get/set-ter(s) of the
 * {@link javax.swing.SpringLayout.Constraints} class</P>
 * @author Lyor G.
 * @since Apr 23, 2009 7:54:13 AM
 */
public enum SpringConstraintsAttribute {
    X {
            /*
             * @see net.community.chest.awt.layout.spring.SpringConstraintsAttribute#getSpringValue(javax.swing.SpringLayout.Constraints)
             */
            @Override
            public Spring getSpringValue (SpringLayout.Constraints c)
            {
                return (null == c) ? null : c.getX();
            }
            /*
             * @see net.community.chest.awt.layout.spring.SpringConstraintsAttribute#setSpringValue(javax.swing.SpringLayout.Constraints, javax.swing.Spring)
             */
            @Override
            public void setSpringValue (SpringLayout.Constraints c, Spring v)
            {
                if ((null == c) || (null == v))
                    return;    // debug breakpoint

                c.setX(v);
            }
        },
    Y {
            /*
             * @see net.community.chest.awt.layout.spring.SpringConstraintsAttribute#getSpringValue(javax.swing.SpringLayout.Constraints)
             */
            @Override
            public Spring getSpringValue (SpringLayout.Constraints c)
            {
                return (null == c) ? null : c.getY();
            }
            /*
             * @see net.community.chest.awt.layout.spring.SpringConstraintsAttribute#setSpringValue(javax.swing.SpringLayout.Constraints, javax.swing.Spring)
             */
            @Override
            public void setSpringValue (SpringLayout.Constraints c, Spring v)
            {
                if ((null == c) || (null == v))
                    return;    // debug breakpoint

                c.setY(v);
            }
        },
    WIDTH {
            /*
             * @see net.community.chest.awt.layout.spring.SpringConstraintsAttribute#getSpringValue(javax.swing.SpringLayout.Constraints)
             */
            @Override
            public Spring getSpringValue (SpringLayout.Constraints c)
            {
                return (null == c) ? null : c.getWidth();
            }
            /*
             * @see net.community.chest.awt.layout.spring.SpringConstraintsAttribute#setSpringValue(javax.swing.SpringLayout.Constraints, javax.swing.Spring)
             */
            @Override
            public void setSpringValue (SpringLayout.Constraints c, Spring v)
            {
                if ((null == c) || (null == v))
                    return;    // debug breakpoint

                c.setWidth(v);
            }
        },
    HEIGHT {
            /*
             * @see net.community.chest.awt.layout.spring.SpringConstraintsAttribute#getSpringValue(javax.swing.SpringLayout.Constraints)
             */
            @Override
            public Spring getSpringValue (SpringLayout.Constraints c)
            {
                return (null == c) ? null : c.getHeight();
            }
            /*
             * @see net.community.chest.awt.layout.spring.SpringConstraintsAttribute#setSpringValue(javax.swing.SpringLayout.Constraints, javax.swing.Spring)
             */
            @Override
            public void setSpringValue (SpringLayout.Constraints c, Spring v)
            {
                if ((null == c) || (null == v))
                    return;    // debug breakpoint

                c.setHeight(v);
            }
        };

    public abstract Spring getSpringValue (SpringLayout.Constraints c);
    public abstract void setSpringValue (SpringLayout.Constraints c, Spring v);

    public static final List<SpringConstraintsAttribute>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final SpringConstraintsAttribute fromString (final String name)
    {
        return CollectionsUtils.fromString(VALUES, name, false);
    }

    public static final Map<SpringConstraintsAttribute,Spring> getAttributes (
            final SpringLayout.Constraints c, final Collection<SpringConstraintsAttribute> attrs)
    {
        if ((null == c) || (null == attrs) || (attrs.size() <= 0))
            return null;

        final Map<SpringConstraintsAttribute,Spring>    aMap=
            new EnumMap<SpringConstraintsAttribute,Spring>(SpringConstraintsAttribute.class);
        for (final SpringConstraintsAttribute a : attrs)
        {
            final Spring    v=(null == a) ? null : a.getSpringValue(c);
            if (null == v)
                continue;
            aMap.put(a, v);
        }

        return aMap;
    }

    public static final Map<SpringConstraintsAttribute,Spring> getAttributes (
            final SpringLayout.Constraints c, final SpringConstraintsAttribute ... attrs)
    {
        return getAttributes(c, SetsUtils.setOf(attrs));
    }

    public static final Map<SpringConstraintsAttribute,Spring> getAttributes (final SpringLayout.Constraints c)
    {
        return getAttributes(c, VALUES);
    }

    public static final void setAttributes (
            final SpringLayout.Constraints c, final Collection<? extends Map.Entry<SpringConstraintsAttribute,? extends Spring>> aList)
    {
        if ((null == c) || (null == aList) || (aList.size() <= 0))
            return;

        for (final Map.Entry<SpringConstraintsAttribute,? extends Spring> ae : aList)
        {
            final SpringConstraintsAttribute    a=(null == ae) ? null : ae.getKey();
            final Spring                        v=(null == ae) ? null : ae.getValue();
            if ((null == a) || (null == v))
                continue;
            a.setSpringValue(c, v);
        }
    }

    public static final void setAttributes (
            final SpringLayout.Constraints c, final Map<SpringConstraintsAttribute,? extends Spring> aMap)
    {
        setAttributes(c, ((null == aMap) || (aMap.size() <= 0)) ? null : aMap.entrySet());
    }

}
