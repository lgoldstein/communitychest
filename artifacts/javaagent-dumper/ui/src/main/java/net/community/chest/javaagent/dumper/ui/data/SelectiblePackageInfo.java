/*
 *
 */
package net.community.chest.javaagent.dumper.ui.data;

import java.util.ArrayList;

import net.community.chest.awt.attributes.Selectible;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 14, 2011 11:48:32 AM
 */
public class SelectiblePackageInfo extends ArrayList<SelectibleClassInfo> implements Selectible {
    private static final long serialVersionUID = 6092878648107097212L;

    public SelectiblePackageInfo ()
    {
        this(10);
    }

    public SelectiblePackageInfo (int initialCapacity)
    {
        this(null, initialCapacity);
    }

    public SelectiblePackageInfo (String name, int initialCapacity)
    {
        super(initialCapacity);
        _name = name;
    }

    private String    _name;
    public String getName ()
    {
        return _name;
    }

    public void setName (String name)
    {
        _name = name;
    }

    private boolean    _selected;
    /*
     * @see net.community.chest.awt.attributes.Selectible#isSelected()
     */
    @Override
    public boolean isSelected ()
    {
        return _selected;
    }
    /*
     * @see net.community.chest.awt.attributes.Selectible#setSelected(boolean)
     */
    @Override
    public void setSelected (boolean v)
    {
        if (_selected != v)
            _selected = v;    // debug breakpoint

        for (final SelectibleClassInfo info : this)
            info.setSelected(v);
    }
    /*
     * @see java.util.AbstractList#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof SelectiblePackageInfo))
            return false;
        if (!super.equals(o))
            return false;
        if (StringUtil.compareDataStrings(getName(), ((SelectiblePackageInfo) o).getName(), true) != 0)
            return false;    // debug breakpoint

        return true;
    }
    /*
     * @see java.util.AbstractList#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return super.hashCode() + StringUtil.getDataStringHashCode(getName(), true);
    }
    /*
     * @see java.util.AbstractCollection#toString()
     */
    @Override
    public String toString ()
    {
        return getName() + ": " + super.toString();
    }
}
