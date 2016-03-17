/*
 *
 */
package net.community.apps.tools.adm;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.community.chest.awt.attributes.Textable;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.db.DBAccessConfig;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.ui.helpers.dialog.ButtonsPanel;
import net.community.chest.ui.helpers.dialog.LRInputTextFieldsDialog;
import net.community.chest.ui.helpers.panel.input.LRFieldWithLabelInput;
import net.community.chest.ui.helpers.text.InputTextField;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 14, 2009 12:43:42 PM
 */
public class DBConnectDialog extends LRInputTextFieldsDialog<DBAccessConfig>
            implements ActionListener, ChangeListener {
    private DBAccessConfig    _cfg;
    public final DBAccessConfig getContent ()
    {
        return _cfg;
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.LRInputTextFieldsDialog#createTextField(java.lang.String, org.w3c.dom.Element)
     */
    @Override
    protected LRFieldWithLabelInput createTextField (String name, Element elem)
    {
        final LRFieldWithLabelInput    fld=super.createTextField(name, elem);
        fld.addDataChangeListener(this);
        return fld;
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.HelperDialog#layoutSection(java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void layoutSection (String name, Element elem) throws RuntimeException
    {
        final String    tagName=(null == elem) ? null : elem.getTagName();
        if ("field".equalsIgnoreCase(tagName))
        {
            final LRFieldWithLabelInput    fld=createTextField(name, elem);
            addField(fld);
        }
        else
            super.layoutSection(name, elem);
    }

    public static final String    OK_CMD="ok";

    private AbstractButton    _okBtn;
    /*
     * @see net.community.chest.ui.helpers.dialog.LRInputTextFieldsDialog#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        final ButtonsPanel    bp=getButtonsPanel();
        if (null == (_okBtn=bp.getButton(OK_CMD)))
            throw new IllegalStateException("Missing OK button");
        _okBtn.addActionListener(this);
        _okBtn.setEnabled(false);    // will be updated by "setContent"
    }

    private DBAccessConfig    _cfgCopy;
    public DBConnectDialog (Frame owner, DBAccessConfig cfg, Element elem, boolean autoInit)
    {
        super(owner, elem, autoInit);

        setContent(cfg);
    }

    private static final void updateContent (
            final DBAccessConfig                     value,
            final Map<String,? extends Textable>     fieldsMap,
            final boolean                            fromFields)
    {
        if (null == value)
            return;

        final Map<String,? extends AttributeAccessor>    accsMap=
            DBAccessConfig.getAccessorsMap();
        if ((null == accsMap) || (accsMap.size() <= 0))
            return;    // should not happen

        final Collection<? extends Map.Entry<String,? extends Textable>>    fl=
            ((null == fieldsMap) || (fieldsMap.size() <= 0)) ? null : fieldsMap.entrySet();
        if ((null == fl) || (fl.size() <= 0))
            return;    // should not happen

        for (final Map.Entry<String,? extends Textable> fe : fl)
        {
            final String            fn=(null == fe) ? null : fe.getKey();
            final Textable            ft=(null == fe) ? null : fe.getValue();
            final AttributeAccessor    fa=
                ((null == fn) || (fn.length() <= 0) || (null == ft)) ? null : accsMap.get(fn);
            final Method            fm=
                (null == fa) ? null : (fromFields ? fa.getSetter() : fa.getGetter());
            if (null == fm)    // should not happen
                continue;

            try
            {
                if (fromFields)
                {
                    final String                        fs=ft.getText();
                    final Class<?>                        vt=fa.getType();
                    final ValueStringInstantiator<?>     vsi=
                        ClassUtil.getJDKStringInstantiator(vt);
                    final Object                        fv=
                        ((null == fs) || (fs.length() <= 0)) ? null : vsi.newInstance(fs);
                    fm.invoke(value, fv);
                }
                else
                {
                    final Object    fv=fm.invoke(value, AttributeAccessor.EMPTY_OBJECTS_ARRAY);
                    final String    fs=(null == fv) ? null : fv.toString();
                    ft.setText((null == fs) ? "" : fs);
                }
            }
            catch(Exception e)    // should not happen
            {
                throw ExceptionUtil.toRuntimeException(e);
            }
        }
    }
    /*
     * @see net.community.chest.ui.helpers.dialog.SettableDialog#setContent(java.lang.Object)
     */
    @Override
    public void setContent (final DBAccessConfig value)
    {
        if (_okBtn != null)
            _okBtn.setEnabled(DBAccessConfig.checkDBAccessConfig(value) == 0);
        if (null == _cfgCopy)
            _cfgCopy = new DBAccessConfig(value);
        else
            _cfgCopy.update(value);

        updateContent(value, getFieldsMap(false), false);
        _cfg = value;
    }

    private boolean    _changedConfig    /* =false */;
    public boolean isChangedConfig ()
    {
        return _changedConfig;
    }

    private boolean    _okExit    /* =false */;
    public boolean isOkExit ()
    {
        return _okExit;
    }
    /*
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed (ActionEvent e)
    {
        final String    cmd=(null == e) ? null : e.getActionCommand();
        if (OK_CMD.equalsIgnoreCase(cmd)
         && (0 == DBAccessConfig.checkDBAccessConfig(_cfgCopy)))
        {
            updateContent(_cfgCopy, getFieldsMap(false), true);
            // check if anything changed
            if (!_cfg.equals(_cfgCopy))
            {
                _cfg.update(_cfgCopy);
                _changedConfig = true;
            }

            _okExit = true;
            dispose();
        }
    }
    /*
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
    public void stateChanged (ChangeEvent e)
    {
        final Object    src=(null == e) ? null : e.getSource();
        if ((_okBtn != null) && (src instanceof InputTextField))
        {
            updateContent(_cfgCopy, getFieldsMap(false), true);
            _okBtn.setEnabled(DBAccessConfig.checkDBAccessConfig(_cfgCopy) == 0);
        }
    }
}
