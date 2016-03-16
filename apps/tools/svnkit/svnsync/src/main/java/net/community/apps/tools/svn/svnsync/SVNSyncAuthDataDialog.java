/*
 *
 */
package net.community.apps.tools.svn.svnsync;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.util.Map;

import javax.swing.text.JTextComponent;

import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.AttributeMethodType;
import net.community.chest.svnkit.SVNAccessor;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.ui.components.dialog.TextFieldsOptionsDialog;
import net.community.chest.ui.helpers.dialog.ButtonsPanel;
import net.community.chest.ui.helpers.panel.input.LRFieldWithLabelInput;
import net.community.chest.ui.helpers.text.InputPasswordField;
import net.community.chest.ui.helpers.window.WindowDisposeKeyListener;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 10, 2010 1:12:47 PM
 */
public class SVNSyncAuthDataDialog extends TextFieldsOptionsDialog<SVNAccessor> implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -5857256201222953093L;
    public SVNSyncAuthDataDialog (final SVNSyncMainFrame f, final SVNAccessor acc)
    {
        super(f, acc);
    }
    /*
     * @see net.community.chest.ui.components.dialog.TextFieldsOptionsDialog#createTextField(java.lang.String, boolean)
     */
    @Override
    protected LRFieldWithLabelInput createTextField (String name, boolean readOnly)
    {
        if ("password".equalsIgnoreCase(name))
        {
            // delay auto-layout so we can replace the usual text fields with a password one
            final LRFieldWithLabelInput    fld=new LRFieldWithLabelInput(false);
            fld.setTextField(new InputPasswordField());
            fld.layoutComponent();    // call it specifically since delayed auto-layout
            fld.setName(name);
            fld.setTitle("Password");
            return fld;
        }

        return super.createTextField(name, readOnly);
    }

    private static Map<String,AttributeAccessor>    _accsMap;
    private static final synchronized Map<String,AttributeAccessor> getDefaultAccessors ()
    {
        if (_accsMap == null)
            _accsMap = AttributeMethodType.getAllAccessibleAttributes(SVNAccessor.class);
        return _accsMap;
    }
    /*
     * @see net.community.chest.ui.components.dialog.TextFieldsOptionsDialog#getAccessorsMap(boolean)
     */
    @Override
    protected Map<String,AttributeAccessor> getAccessorsMap (boolean createIfNotExist)
    {
        return getDefaultAccessors();
    }
    /*
     * @see net.community.chest.ui.components.dialog.TextFieldsOptionsDialog#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        final ButtonsPanel    btnPanel=getButtonsPanel();
        if (btnPanel != null)
            btnPanel.setActionListener(this);

        final Map<String,? extends LRFieldWithLabelInput>    fieldsMap=getFieldsMap(false);
        if ((fieldsMap != null) && (fieldsMap.size() > 0))
        {
            final KeyListener    kl=new WindowDisposeKeyListener(this);
            for (final LRFieldWithLabelInput fld : fieldsMap.values())
            {
                final JTextComponent    c=(fld == null) ? null : fld.getTextField();
                if (c == null)
                    continue;
                c.addKeyListener(kl);
            }
        }
    }

    private boolean    _okExit;
    public boolean isOkExit ()
    {
        return _okExit;
    }
    /*
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed (ActionEvent event)
    {
        final String    cmd=(event == null) ? null : event.getActionCommand();
        if (!"ok".equalsIgnoreCase(cmd))
            return;

        final SVNAccessor    acc=getAssignedValue();
        if (acc == null)
            return;

        try
        {
            toCurrentSettings(acc);
        }
        catch(RuntimeException e)
        {
            BaseOptionPane.showMessageDialog(this, e);
            return;
        }

        _okExit = true;
        dispose();
    }
}
