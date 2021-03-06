/*
 *
 */
package net.community.chest.ui.helpers.text;

import java.awt.Color;
import java.awt.event.KeyListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.InputVerifier;
import javax.swing.border.Border;
import javax.swing.event.ChangeListener;

import net.community.chest.lang.StringUtil;
import net.community.chest.swing.event.ChangeListenerSet;
import net.community.chest.ui.helpers.input.InputFieldValidator;
import net.community.chest.ui.helpers.input.InputFieldValidatorKeyListener;
import net.community.chest.ui.helpers.input.TextInputVerifier;
import net.community.chest.ui.helpers.input.ValidatorUtils;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 12, 2009 12:03:45 PM
 */
public class InputTextField extends HelperTextField implements InputFieldValidator {
    /**
     *
     */
    private static final long serialVersionUID = -5630602964207048653L;
    public InputTextField (String text, int columns, Element elem, boolean autoLayout)
    {
        super(text, columns, elem, autoLayout);
    }

    public InputTextField (String text, Element elem, boolean autoLayout)
    {
        this(text, 0, elem, autoLayout);
    }

    public InputTextField (String text, boolean autoLayout)
    {
        this(text, 0, autoLayout);
    }

    public InputTextField (String text, Element elem)
    {
        this(text, elem, true);
    }

    public InputTextField (String text, int columns, boolean autoLayout)
    {
        this(text, columns, null, autoLayout);
    }

    public InputTextField (String text, int columns)
    {
        this(text, columns, true);
    }

    public InputTextField (int columns)
    {
        this(null, columns);
    }

    public InputTextField (String text)
    {
        this(text, 0);
    }

    public InputTextField (Element elem, boolean autoLayout)
    {
        this(null, elem, autoLayout);
    }

    public InputTextField (Element elem)
    {
        this(elem, true);
    }

    public InputTextField (boolean autoLayout)
    {
        this((Element) null, autoLayout);
    }

    public InputTextField ()
    {
        this(true);
    }

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
    /* Invokes the {@link InputVerifier} (if any - otherwise returns true)
     * @see net.community.chest.ui.helpers.input.InputFieldValidator#isValidData()
     */
    @Override
    public boolean isValidData ()
    {
        final InputVerifier    v=getInputVerifier();
        if (v != null)
            return v.verify(this);

        return true;
    }

    protected InputVerifier createDefaultVerifier ()
    {
        return TextInputVerifier.TEXT;
    }

    protected KeyListener createDefaultKeyListener ()
    {
        return new InputFieldValidatorKeyListener<InputTextField>(this);
    }
    /*
     * @see net.community.chest.ui.helpers.text.HelperTextField#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        InputVerifier    v=getInputVerifier();
        if (null == v)
        {
            if ((v=createDefaultVerifier()) != null)
                setInputVerifier(v);
        }

        Border    okBorder=getOkBorder();
        if (null == okBorder)
            setOkBorder(getBorder());

        signalDataChanged(false);

        final KeyListener    kl=createDefaultKeyListener();
        if (kl != null)
            addKeyListener(kl);
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

    protected Border updateBorderColor (final boolean validData)
    {
        final Border    b=ValidatorUtils.resolveValidatorBorder(this, validData, getOkBorder(), getErrBorder());
        super.setBorder(b);    // do not disturb the current OK/ERR borders
        return b;
    }

    private Collection<ChangeListener>    _cl;
    protected int fireChangeEvent ()
    {
        return ChangeListenerSet.fireChangeEventForSource(this, _cl, true);
    }
    /*
     * @see net.community.chest.ui.helpers.input.InputFieldValidator#addDataChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public boolean addDataChangeListener (ChangeListener l)
    {
        if (null == l)
            return false;

        synchronized(this)
        {
            if (null == _cl)
                _cl = new ChangeListenerSet();
        }

        synchronized(_cl)
        {
            return _cl.add(l);
        }
    }
    /*
     * @see net.community.chest.ui.helpers.input.InputFieldValidator#removeDataChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public boolean removeDataChangeListener (ChangeListener l)
    {
        if (null == l)
            return false;

        synchronized(this)
        {
            if ((null == _cl) || (_cl.size() <= 0))
                return false;
        }

        synchronized(_cl)
        {
            return _cl.remove(l);
        }
    }
    /*
     * @see net.community.chest.ui.helpers.input.InputFieldValidator#signalDataChanged(boolean)
     */
    @Override
    public int signalDataChanged (boolean fireEvent)
    {
        updateBorderColor(isValidData());

        if (fireEvent)
            return fireChangeEvent();
        else
            return 0;
    }
    /*
     * @see net.community.chest.ui.helpers.input.InputFieldValidator#signalDataChanged()
     */
    public void signalDataChanged ()
    {
        signalDataChanged(true);
    }

    public void setText (String t, boolean fireEvent)
    {
        super.setText(t);

        if (fireEvent)
            signalDataChanged();
    }
    /*
     * @see javax.swing.text.JTextComponent#setText(java.lang.String)
     */
    @Override
    public void setText (String t)
    {
        setText(t, StringUtil.compareDataStrings(t, getText(), true) != 0);
    }
}
