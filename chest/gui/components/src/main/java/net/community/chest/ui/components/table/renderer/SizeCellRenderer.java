/*
 *
 */
package net.community.chest.ui.components.table.renderer;

import java.awt.Component;
import java.io.IOException;

import javax.swing.JTable;

import net.community.chest.awt.attributes.AttrUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.lang.math.SizeUnits;
import net.community.chest.swing.HAlignmentValue;
import net.community.chest.swing.component.table.BaseTableCellRenderer;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 4, 2009 4:46:39 PM
 */
public class SizeCellRenderer extends BaseTableCellRenderer {
    /**
     *
     */
    private static final long serialVersionUID = 3479699822101343050L;
    private SizeUnits    _units;
    // NULL == byte(s)
    public SizeUnits getSizeUnits ()
    {
        return _units;
    }

    public void setSizeUnits (SizeUnits u)
    {
        _units = u;
    }

    private boolean    _showSizeName;
    public boolean isShowSizeName ()
    {
        return _showSizeName;
    }

    public void setShowSizeName (boolean showSizeName)
    {
        _showSizeName = showSizeName;
    }

    public SizeCellRenderer (SizeUnits u, boolean showSizeName, HAlignmentValue hv)
    {
        _units = u;
        _showSizeName = showSizeName;

        if (hv != null)
            setHorizontalAlignment(hv.getAlignmentValue());
    }

    public SizeCellRenderer (SizeUnits u, boolean showSizeName)
    {
        this(u, showSizeName, HAlignmentValue.RIGHT);
    }

    public SizeCellRenderer ()
    {
        this(SizeUnits.B, false);
    }

    protected String setText (final Component c, final long szVal)
    {
        final long            v=
            AttrUtils.isTextableComponent(c) ? szVal : (-1L);
        final SizeUnits        u=(v <= 0L) ? null : getSizeUnits();
        final boolean        useRawValue=(null == u) || SizeUnits.B.equals(u);
        final StringBuilder    sb=new StringBuilder(32);
        final long            hv, lv;
        if ((v <= 0L) || useRawValue)
        {
            hv = 0L;
            lv = 0L;
        }
        else
        {
            final double    x=u.convertToThisUnit(v, SizeUnits.B);
            final long        xv=(long) (x * 100.0d);

            hv = xv / 100L;
            lv = xv % 100L;
        }

        sb.append(String.valueOf(hv));
        if (!useRawValue)
        {
            sb.append('.');
            try
            {
                StringUtil.appendPadded(sb, String.valueOf(lv), 2, '0');
            }
            catch(IOException e)
            {
                throw ExceptionUtil.toRuntimeException(e);
            }
        }

        if (isShowSizeName())
        {
            final String    n=
                (null == u) ? SizeUnits.B.getSizeName() : u.getSizeName();
            sb.append(' ').append(n);
        }

        final String    t=sb.toString();
        AttrUtils.setComponentText(c, t);
        return t;
    }
    /*
     * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent (JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column)
    {
        final Component    c=super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (null == value)
            setText(c, 0L);
        else if (value instanceof Number)
            setText(c, ((Number) value).longValue());
        else if (value instanceof String)
            setText(c, Long.parseLong(value.toString()));
        else
            throw new IllegalArgumentException("getTableCellRendererComponent(" + row + "," + column + ")[" + value + "] unknown value type: " + ((null == value) ? null : value.getClass().getName()));

        return c;
    }
}
