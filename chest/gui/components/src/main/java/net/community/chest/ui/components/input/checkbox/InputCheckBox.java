/*
 *
 */
package net.community.chest.ui.components.input.checkbox;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;

import org.w3c.dom.Element;

import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.swing.event.EventListenerListUtils;
import net.community.chest.ui.helpers.button.TypedCheckBox;
import net.community.chest.ui.helpers.input.InputFieldValidator;
import net.community.chest.ui.helpers.input.ValidatorUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 13, 2009 3:05:52 PM
 */
public class InputCheckBox extends TypedCheckBox<Boolean> implements InputFieldValidator {
    /**
     *
     */
    private static final long serialVersionUID = 5180009996531970195L;
    public InputCheckBox (Boolean v, Element elem, boolean autoLayout)
    {
        super(Boolean.class, v, elem, autoLayout);
    }

    public InputCheckBox (Boolean v, Element elem)
    {
        this(v, elem, true);
    }

    public InputCheckBox (Element elem, boolean autoLayout)
    {
        this(null, elem, autoLayout);
    }

    public InputCheckBox (Element elem)
    {
        this(elem, true);
    }

    public InputCheckBox (boolean autoLayout)
    {
        this(null, autoLayout);
    }

    public InputCheckBox ()
    {
        this(true);
    }
    /*
     * @see net.community.chest.ui.helpers.button.TypedCheckBox#getCheckBoxConverter(org.w3c.dom.Element)
     */
    @Override
    public XmlProxyConvertible<?> getCheckBoxConverter (Element elem) throws Exception
    {
        return (null == elem) ? null : InputCheckBoxReflectiveProxy.INPCB;
    }

    public static final int DEFAULT_LINE_THICKNESS=2;
    protected Border createStateBorder (Color c)
    {
        return (null == c) ? null : BorderFactory.createLineBorder(c, DEFAULT_LINE_THICKNESS);
    }

    private Color    _errColor;
    /*
     * @see net.community.chest.ui.helpers.input.InputFieldValidator#getErrFieldColor()
     */
    @Override
    public Color getErrFieldColor ()
    {
        if (null == _errColor)
            _errColor = DEFAULT_ERR_COLOR;
        return _errColor;
    }

    private Border    _errBorder    /* =null */;
    public Border getErrBorder ()
    {
        if (null == _errBorder)
            _errBorder = createStateBorder(getErrFieldColor());
        return _errBorder;
    }

    public void setErrBorder (Border b)
    {
        _errBorder = b;
    }
    /*
     * @see net.community.chest.ui.helpers.input.InputFieldValidator#setErrFieldColor(java.awt.Color)
     */
    @Override
    public void setErrFieldColor (Color errColor)
    {
        if ((errColor != null) && (!errColor.equals(_errColor)))
        {
            _errColor = errColor;
            // force re-creation on next update
            setErrBorder(null);
        }
    }

    private Color    _okColor;
    /*
     * @see net.community.chest.ui.helpers.input.InputFieldValidator#getOkFieldColor()
     */
    @Override
    public Color getOkFieldColor ()
    {
        if (null == _okColor)
            _okColor = DEFAULT_OK_COLOR;
        return _okColor;
    }

    private Border    _okBorder    /* =null */;
    public Border getOkBorder ()
    {
        // NOTE: OK border is empty by default whereas ERR border is auto-created
        return _okBorder;
    }

    public void setOkBorder (Border b)
    {
        if (_okBorder != b)
            _okBorder = b;
    }
    /*
     * @see net.community.chest.ui.helpers.input.InputFieldValidator#setOkFieldColor(java.awt.Color)
     */
    @Override
    public void setOkFieldColor (Color okColor)
    {
        if ((okColor != null) && (!okColor.equals(_okColor)))
        {
            _okColor = okColor;
            // force re-creation on next update
            setOkBorder(null);
        }
    }
    /*
     * @see javax.swing.JComponent#setBorder(javax.swing.border.Border)
     */
    @Override
    public void setBorder (Border b)
    {
        super.setBorder(b);
        // force re-creation on next update
        setErrBorder(null);
        setOkBorder(null);
    }
    /*
     * @see net.community.chest.ui.helpers.input.InputFieldValidator#addDataChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public boolean addDataChangeListener (ChangeListener l)
    {
        if ((null == l) || EventListenerListUtils.contains(listenerList, ChangeListener.class, l))
            return false;

        addChangeListener(l);
        return true;
    }
    /*
     * @see net.community.chest.ui.helpers.input.InputFieldValidator#removeDataChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public boolean removeDataChangeListener (ChangeListener l)
    {
        if ((null == l) || (!EventListenerListUtils.contains(listenerList, ChangeListener.class, l)))
            return false;

        removeChangeListener(l);
        return true;
    }

    protected Border updateBorderColor (final boolean validData)
    {
        final Border    b=ValidatorUtils.resolveValidatorBorder(this, validData, getOkBorder(), getErrBorder());
        super.setBorder(b);    // do not disturb the current OK/ERR borders
        return b;
    }
    /*
     * @see net.community.chest.ui.helpers.input.InputFieldValidator#isValidData()
     */
    @Override
    public boolean isValidData ()
    {
        return (getAssignedValue() != null);
    }
    /*
     * @see net.community.chest.ui.helpers.input.InputFieldValidator#signalDataChanged(boolean)
     */
    @Override
    public int signalDataChanged (boolean fireEvent)
    {
        updateBorderColor(isValidData());
        // no need to fire change listeners since this is done by the base component itself
        if (fireEvent)
        {
            final int    nCount=
                (null == listenerList) ? 0 : listenerList.getListenerCount(ChangeListener.class);
            if (nCount > 0)
                fireStateChanged();
            return nCount;
        }

        return 0;
    }
    /*
     * @see net.community.chest.ui.helpers.button.TypedCheckBox#setAssignedValue(java.lang.Object)
     */
    @Override
    public void setAssignedValue (Boolean v)
    {
        final Boolean    prev=getAssignedValue();
        super.setAssignedValue(v);

        if (v != null)
            super.setSelected(v.booleanValue());

        if (prev != v)
        {
            if ((null == v) || (!v.equals(prev)))
                signalDataChanged(true);
        }
    }
    /*
     * @see javax.swing.AbstractButton#setSelected(boolean)
     */
    @Override
    public void setSelected (boolean f)
    {
        // TODO check if need to register a ChangeListener of our own in order to call "signalDataChanged"
        setAssignedValue(Boolean.valueOf(f));
    }
}
