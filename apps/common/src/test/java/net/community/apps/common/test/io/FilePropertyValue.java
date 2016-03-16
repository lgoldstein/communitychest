/*
 *
 */
package net.community.apps.common.test.io;

import java.awt.GridBagConstraints;
import java.io.File;

import javax.swing.JLabel;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.awt.attributes.Textable;
import net.community.chest.awt.attributes.Titled;
import net.community.chest.io.file.FileAttributeType;
import net.community.chest.swing.component.label.BaseLabel;
import net.community.chest.ui.helpers.panel.PresetGridBagLayoutPanel;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 26, 2009 9:51:07 AM
 */
public class FilePropertyValue extends PresetGridBagLayoutPanel
        implements TypedComponentAssignment<File>, Titled, Textable {
    /**
     *
     */
    private static final long serialVersionUID = -3968275814265144614L;
    private JLabel    _ttl;
    /*
     * @see net.community.chest.awt.attributes.Titled#getTitle()
     */
    @Override
    public String getTitle ()
    {
        return (null == _ttl) ? null : _ttl.getText();
    }
    /*
     * @see net.community.chest.awt.attributes.Titled#setTitle(java.lang.String)
     */
    @Override
    public void setTitle (String t)
    {
        if (_ttl != null)
            _ttl.setText((null == t) ? "" : t);
    }

    private FileAttributeType    _a;
    public FileAttributeType getFileAttribute ()
    {
        return _a;
    }

    public void setFileAttribute (FileAttributeType a)
    {
        if (!AbstractComparator.compareObjects(_a, a))
        {
            if ((_a=a) != null)
                setTitle(a.toString());
            else
                setTitle("");
        }
    }

    private JLabel    _txt;
    /*
     * @see net.community.chest.awt.attributes.Textable#getText()
     */
    @Override
    public String getText ()
    {
        return (null == _txt) ? null : _txt.getText();
    }
    /*
     * @see net.community.chest.awt.attributes.Textable#setText(java.lang.String)
     */
    @Override
    public void setText (String t)
    {
        if (_txt != null)
            _txt.setText((null == t) ? "" : t);
    }

    private File    _value;
    /*
     * @see net.community.chest.awt.TypedComponentAssignment#getAssignedValue()
     */
    @Override
    public File getAssignedValue ()
    {
        return _value;
    }
    /*
     * @see net.community.chest.awt.TypedComponentAssignment#setAssignedValue(java.lang.Object)
     */
    @Override
    public void setAssignedValue (File value)
    {
        final FileAttributeType    a=getFileAttribute();
        try
        {
            final Object    o=((null == a) || (null == value)) ? null : a.getValue(value);
            setText((null == o) ? null : o.toString());
        }
        catch(Exception e)
        {
            setText(e.getClass().getName() + ": " + e.getMessage());
        }

        _value = value;
    }
    /*
     * @see net.community.chest.ui.helpers.panel.HelperPanel#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        if (null == _ttl)
            _ttl = new BaseLabel();
        {
            final GridBagConstraints    gbc=getGridBagConstraints(true, true);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.NONE;
            gbc.gridheight = 1;
            gbc.gridwidth = 1;
            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.ipadx = 5;

            add(_ttl, gbc);
        }

        if (null == _txt)
            _txt = new BaseLabel();

        {
            final GridBagConstraints    gbc=getGridBagConstraints(true, true);
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridheight = 1;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0;
            gbc.weighty = 0.0;

            add(_txt, gbc);
        }
    }

    public FilePropertyValue (FileAttributeType a)
    {
        setFileAttribute(a);
    }

    public FilePropertyValue ()
    {
        this(null);
    }
}
