/*
 *
 */
package net.community.chest.ui.components.datetime;

import java.awt.Component;
import java.util.Calendar;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.awt.attributes.Selectible;
import net.community.chest.awt.attributes.Textable;
import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.ui.helpers.panel.PresetFlowLayoutPanel;
import net.community.chest.util.datetime.CalendarFieldType;

/**
 * <P>Copyright as per GPLv2</P>
 * @param <C> Type of {@link AbstractSpinnerDateTimeControl} being used
 * @author Lyor G.
 * @since Nov 18, 2010 7:48:25 AM
 */
public abstract class AbstractOptionalSpinnerDateTimeControl<C extends AbstractSpinnerDateTimeControl>
        extends PresetFlowLayoutPanel
        implements TypedValuesContainer<C>, Textable, Selectible, ChangeListener, TypedComponentAssignment<Calendar> {

    /**
     *
     */
    private static final long serialVersionUID = 5614731835252434665L;
    private final Class<C>     _valsClass;
    /*
     * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
     */
    @Override
    public final Class<C> getValuesClass ()
    {
        return _valsClass;
    }

    protected AbstractOptionalSpinnerDateTimeControl (Class<C> valsClass) throws IllegalStateException
    {
        if ((_valsClass=valsClass) == null)
            throw new IllegalStateException("No values class provided");
    }

    private JCheckBox    _optionControl;
    protected JCheckBox getOptionControl (boolean createIfNotExist)
    {
        if ((null == _optionControl) && createIfNotExist)
            _optionControl = new JCheckBox();
        return _optionControl;
    }

    public JCheckBox getOptionControl ()
    {
        return getOptionControl(false);
    }

    public void setOptionControl (JCheckBox optionControl)
    {
        _optionControl = optionControl;
    }
    /*
     * @see net.community.chest.awt.attributes.Textable#getText()
     */
    @Override
    public String getText ()
    {
        final JCheckBox    c=getOptionControl();
        if (null == c)
            return null;

        return c.getText();
    }
    /*
     * @see net.community.chest.awt.attributes.Textable#setText(java.lang.String)
     */
    @Override
    public void setText (String t)
    {
        final JCheckBox    c=getOptionControl();
        if (null == c)
            return;

        c.setText(t);
    }
    /*
     * @see net.community.chest.awt.attributes.Selectible#isSelected()
     */
    @Override
    public boolean isSelected ()
    {
        final JCheckBox    c=getOptionControl();
        if (null == c)
            return false;

        return c.isSelected();
    }

    protected abstract C createSpinnerControl ();

    private C    _spinnerControl;
    protected C getSpinnerControl (boolean createIfNotExist)
    {
        if ((_spinnerControl == null) && createIfNotExist)
            _spinnerControl = createSpinnerControl();
        return _spinnerControl;
    }

    public C getSpinnerControl ()
    {
        return getSpinnerControl(false);
    }

    public void setSpinnerControl (C c)
    {
        _spinnerControl = c;
    }

    public Calendar getValue ()
    {
        final C c=getSpinnerControl();
        if (c == null)
            return null;
        else
            return c.getValue();
    }
    /*
     * @see net.community.chest.awt.TypedComponentAssignment#getAssignedValue()
     */
    @Override
    public Calendar getAssignedValue ()
    {
        return getValue();
    }

    public void setValue (Calendar cal)
    {
        final C c=getSpinnerControl();
        if (c == null)
            return;

        c.setValue(cal);
    }
    /*
     * @see net.community.chest.awt.TypedComponentAssignment#setAssignedValue(java.lang.Object)
     */
    @Override
    public void setAssignedValue (Calendar value)
    {
        setValue(value);
    }

    protected void updateSpinnersState (boolean v)
    {
        final C                                c=getSpinnerControl();
        final Collection<CalendarFieldType> fields=(c == null) ? null : c.getFieldsOrder();
        if ((fields == null) || fields.isEmpty())
            return;

        for (final CalendarFieldType calField : fields)
        {
            final JSpinner    s=c.getSpinner(calField);
            if (null == s)
                continue;

            s.setEnabled(v);
        }
    }
    /*
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
    public void stateChanged (ChangeEvent e)
    {
        final Object    src=(null == e) ? null : e.getSource();
        if (!(src instanceof JCheckBox))
            return;

        updateSpinnersState(((JCheckBox) src).isSelected());
    }
    /*
     * @see net.community.chest.awt.attributes.Selectible#setSelected(boolean)
     */
    @Override
    public void setSelected (boolean v)
    {
        updateSpinnersState(v);

        final JCheckBox    c=getOptionControl();
        if (null == c)
            return;

        c.setSelected(v);
    }
    /*
     * @see net.community.chest.ui.helpers.panel.HelperPanel#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        final JCheckBox        cb=getOptionControl(true);
        final Component[]    ca={ cb, getSpinnerControl(true) };
        for (final Component c : ca)
        {
            if (null == c)
                continue;
            add(c);
        }

        if (cb != null)
            cb.addChangeListener(this);
    }
}
