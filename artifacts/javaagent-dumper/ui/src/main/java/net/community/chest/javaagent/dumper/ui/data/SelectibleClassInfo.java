/*
 *
 */
package net.community.chest.javaagent.dumper.ui.data;

import java.util.Collection;
import java.util.Comparator;

import net.community.chest.awt.attributes.Selectible;
import net.community.chest.javaagent.dumper.data.ClassInfo;
import net.community.chest.lang.StringUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 14, 2011 11:45:30 AM
 */
public class SelectibleClassInfo extends ClassInfo implements Selectible {
    private static final long serialVersionUID = -8717955832248080539L;

    public SelectibleClassInfo ()
    {
        super();
    }

    public SelectibleClassInfo (Document doc) throws Exception
    {
        super(doc);
    }

    public SelectibleClassInfo (Element root) throws Exception
    {
        super(root);
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
            _selected = v;

        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Collection<? extends SelectibleMethodInfo>    methods=(Collection) getMethods();
        if ((methods == null) || (methods.size() <= 0))
            return;

        for (final SelectibleMethodInfo info : methods)
            info.setSelected(v);
    }

    private transient String    _simpleName;
    public final String getSimpleName ()
    {
        if (_simpleName == null)
        {
            final String    fullName=getName();
            if ((fullName == null) || (fullName.length() <= 0))
                return null;

            final int    dotPos=fullName.lastIndexOf('.');
            if (dotPos > 0)
                _simpleName = fullName.substring(dotPos + 1);
            else
                _simpleName = fullName;
        }

        return _simpleName;
    }

    public static final Comparator<SelectibleClassInfo> BY_SIMPLE_NAME_COMP=
            new Comparator<SelectibleClassInfo>() {
                /*
                 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                 */
                @Override
                public int compare (SelectibleClassInfo o1, SelectibleClassInfo o2)
                {
                    final String    n1=(o1 == null) ? null : o1.getSimpleName(),
                                    n2=(o2 == null) ? null : o2.getSimpleName();
                    return StringUtil.compareDataStrings(n1, n2, true);
                }
            };
    /*
     * @see net.community.chest.javaagent.dumper.data.ClassInfo#setName(java.lang.String)
     */
    @Override
    public void setName (String name)
    {
        if (_simpleName != null)
            _simpleName = null;    // debug breakpoint

        super.setName(name);
    }

    protected Boolean setSelected (Element elem)
    {
        final String    value=elem.getAttribute(ATTR_NAME);
        if ((value == null) || (value.length() <= 0))
            return null;

        final Boolean    selected=Boolean.valueOf(value);
        setSelected(selected.booleanValue());
        return selected;
    }
    /*
     * @see net.community.chest.javaagent.dumper.data.MethodInfo#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        final Element    elem=super.toXml(doc);
        elem.setAttribute(ATTR_NAME, String.valueOf(isSelected()));
        return elem;
    }
    /*
     * @see net.community.chest.javaagent.dumper.data.ClassInfo#restoreMethodInfo(org.w3c.dom.Element)
     */
    @Override
    protected SelectibleMethodInfo restoreMethodInfo (Element elem) throws Exception
    {
        return new SelectibleMethodInfo(elem);
    }
    /*
     * @see net.community.chest.javaagent.dumper.data.MethodInfo#fromXml(org.w3c.dom.Element)
     */
    @Override
    public SelectibleClassInfo fromXml (Element root) throws Exception
    {
        final Object    info=super.fromXml(root);
        if (info != this)
            throw new IllegalStateException("Mismatched super-class re-constructed instance");

        setSelected(root);
        return this;
    }
}
