/*
 *
 */
package net.community.chest.ui.components.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.lang.reflect.Method;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.AttributeMethodType;
import net.community.chest.ui.helpers.dialog.LRInputTextFieldsDialog;
import net.community.chest.ui.helpers.panel.input.LRFieldWithLabelInput;

import org.w3c.dom.Document;

/**
 * <P>Copyright as per GPLv2</P>
 * <P>Uses reflection API to extract all fields that are of type {@link String}</P>
 * @param <V> Type of value being reflected
 * @author Lyor G.
 * @since Nov 10, 2010 1:38:51 PM
 *
 */
public abstract class TextFieldsOptionsDialog<V> extends LRInputTextFieldsDialog<V> implements TypedComponentAssignment<V> {
    /**
     *
     */
    private static final long serialVersionUID = -5826457867445062220L;
    protected TextFieldsOptionsDialog (Frame owner, V value, Document doc, boolean autoInit)
    {
        super(owner, doc, autoInit);

        if (autoInit)
            setContent(value);
        else
            setAssignedValue(value);
    }

    protected TextFieldsOptionsDialog (Frame owner, V value, boolean autoInit)
    {
        this(owner, value, (Document) null, autoInit);
    }

    protected TextFieldsOptionsDialog (Frame owner, V value)
    {
        this(owner, value, true);
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.LRInputTextFieldsDialog#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();
        layoutTextFieldsAccessors(getAccessorsMap(true));
    }

    protected void layoutTextFieldsAccessors (final Map<String,? extends AttributeAccessor> accsMap)
    {
        if ((accsMap == null) || (accsMap.size() <= 0))
            return;

        for (final Map.Entry<String,? extends AttributeAccessor> ae : accsMap.entrySet())
        {
            final String            name=(ae == null) ? null : ae.getKey();
            final AttributeAccessor    a=(ae == null) ? null : ae.getValue();
            final Method            g=(a == null) ? null : a.getGetter(),
                                    s=(a == null) ? null : a.getSetter();
            if ((name == null) || (name.length() <= 0) || (g == null))    // ignore write-only fields
                continue;

            final Class<?>    t=a.getType();
            if (t != String.class)    // ignore non-string fields
                continue;

            final LRFieldWithLabelInput    fld=createTextField(name, (s == null)),
                                        prev=addTextField(name, fld);
            if (prev != null)
                throw new IllegalStateException("Duplicate fields: " + name);
        }

        layoutTextFields(getFieldsMap(false));
    }

    protected void layoutTextFields (final Map<String,? extends LRFieldWithLabelInput>    fieldsMap)
    {
        if ((fieldsMap == null) || (fieldsMap.size() <= 0))
            return;

        final JPanel    optsPanel=new JPanel(new GridLayout(0, 1, 0, 5));
        for (final LRFieldWithLabelInput fld : fieldsMap.values())
        {
            if (fld == null)
                continue;
            optsPanel.add(fld);
        }

        final Container    ct=getContentPane();
        ct.add(optsPanel, BorderLayout.CENTER);
    }

    protected LRFieldWithLabelInput createTextField (final String name, final boolean readOnly)
    {
        final LRFieldWithLabelInput    fld=new LRFieldWithLabelInput();
        fld.setName(name);
        fld.setTitle(AttributeMethodType.getSpacedAttributeName(name));
        if (readOnly)
            fld.setEditable(false);

        return fld;
    }

    private V    _value;
    /*
     * @see net.community.chest.awt.TypedComponentAssignment#getAssignedValue()
     */
    @Override
    public V getAssignedValue ()
    {
        return _value;
    }
    /* NOTE: does not update the UI !!!
     * @see net.community.chest.awt.TypedComponentAssignment#setAssignedValue(java.lang.Object)
     */
    @Override
    public void setAssignedValue (V value)
    {
        _value = value;
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.SettableDialog#setContent(java.lang.Object)
     */
    @Override
    public void setContent (final V value)
    {
        setAssignedValue(value);
        fromCurrentSettings(value);
    }

    protected abstract Map<String,AttributeAccessor> getAccessorsMap (boolean createIfNotExist);

    protected void fromCurrentSettings (final V value)
    {
        final Map<String,? extends LRFieldWithLabelInput>    fieldsMap=getFieldsMap(false);
        final Map<String,AttributeAccessor>                    accsMap=getAccessorsMap(true);
        if ((fieldsMap == null) || (fieldsMap.size() <= 0)
         || (accsMap == null) || (accsMap.size() <= 0))
            return;

        for (final Map.Entry<String,? extends LRFieldWithLabelInput> fe : fieldsMap.entrySet())
        {
            final String            name=fe.getKey();
            final AttributeAccessor    a=accsMap.get(name);
            final Method            g=(a == null) ? null : a.getGetter();
            if (g == null)    // no support for write-only fields
                throw new IllegalStateException("No getter for field=" + name);

            final LRFieldWithLabelInput    iField=fe.getValue();
            final JTextComponent        tField=iField.getTextField();
            try
            {
                final String    s=(String) g.invoke(value);
                tField.setText((s == null) ? "" : s);
            }
            catch(Exception e)
            {
                throw ExceptionUtil.toRuntimeException(e);
            }
        }
    }

    protected void toCurrentSettings (final V value)
    {
        final Map<String,? extends LRFieldWithLabelInput>    fieldsMap=getFieldsMap(false);
        final Map<String,AttributeAccessor>                    accsMap=getAccessorsMap(true);
        if ((fieldsMap == null) || (fieldsMap.size() <= 0)
         || (accsMap == null) || (accsMap.size() <= 0))
            return;

        for (final Map.Entry<String,? extends LRFieldWithLabelInput> fe : fieldsMap.entrySet())
        {
            final String            name=fe.getKey();
            final AttributeAccessor    a=accsMap.get(name);
            final Method            s=(a == null) ? null : a.getSetter();
            if (s == null)    // means it is a read-only attribute
                continue;

            final LRFieldWithLabelInput    iField=fe.getValue();
            final JTextComponent        tField=iField.getTextField();
            final String                txtValue=tField.getText();
            try
            {
                s.invoke(value, txtValue);
            }
            catch(Exception e)
            {
                throw ExceptionUtil.toRuntimeException(e);
            }
        }
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.SettableDialog#clearContent()
     */
    @Override
    public void clearContent ()
    {
        setContent(null);
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.SettableDialog#refreshContent(java.lang.Object)
     */
    @Override
    public void refreshContent (V value)
    {
        setContent(value);
    }
}
