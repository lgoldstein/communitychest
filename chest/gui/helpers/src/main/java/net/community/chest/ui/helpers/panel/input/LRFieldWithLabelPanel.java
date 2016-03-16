/*
 *
 */
package net.community.chest.ui.helpers.panel.input;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.community.chest.awt.attributes.Iconable;
import net.community.chest.awt.attributes.Titled;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.swing.component.label.BaseLabel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provides a text field preceded or followed by a text field</P>
 * @author Lyor G.
 * @since Aug 25, 2008 1:23:30 PM
 */
public class LRFieldWithLabelPanel extends AbstractInputTextPanel implements Iconable, Titled {
    /**
     *
     */
    private static final long serialVersionUID = -6363350334013494746L;
    /*
     * @see net.community.chest.swing.component.panel.input.AbstractFieldWithButtonPanel#getPanelConverter(org.w3c.dom.Element)
     */
    @Override
    protected XmlProxyConvertible<?> getPanelConverter (Element elem)
    {
        return (null == elem) ? null : LRFieldWithLabelReflectiveProxy.LRLBLFLDPNL;
    }

    private JLabel    _lbl;
    protected JLabel getLabel (boolean createIfNoExist)
    {
        if ((null == _lbl) && createIfNoExist)
            _lbl = new BaseLabel();
        return _lbl;
    }

    public JLabel getLabel ()
    {
        return getLabel(false);
    }

    public void setLabel (JLabel lbl)
    {
        _lbl = lbl;
    }
    /*
     * @see net.community.chest.awt.attributes.Titled#getTitle()
     */
    @Override
    public String getTitle ()
    {
        final JLabel    l=getLabel();
        return (null == l) ? null : l.getText();
    }
    /*
     * @see net.community.chest.awt.attributes.Titled#setTitle(java.lang.String)
     */
    @Override
    public void setTitle (final String t)
    {
        final JLabel    l=getLabel(true);
        if (l != null)
            l.setText((null == t) ? "" : t);
    }
    /*
     * @see net.community.chest.awt.attributes.Iconable#getIcon()
     */
    @Override
    public Icon getIcon ()
    {
        final JLabel    l=getLabel();
        return (null == l) ? null : l.getIcon();
    }
    /*
     * @see net.community.chest.awt.attributes.Iconable#setIcon(javax.swing.Icon)
     */
    @Override
    public void setIcon (Icon i)
    {
        final JLabel    l=getLabel(true);
        if (l != null)
            l.setIcon(i);
    }

    private boolean    _lblRightPos    /* =false */;
    public boolean isLabelRightPos ()
    {
        return _lblRightPos;
    }

     public void setLabelRightPos (final boolean lblRightPos)
    {
        if (lblRightPos != isLabelRightPos())
            _lblRightPos = lblRightPos;
    }

     public LRFieldWithLabelPanel (final boolean lblRightPos, final Document doc, final boolean autoLayout)
     {
        super(new GridBagLayout(), false /* delay layout till _lblRightPos initialized */);

        _lblRightPos = lblRightPos;

        setComponentDocument(doc);
        if (autoLayout)
            layoutComponent();
     }

    public LRFieldWithLabelPanel (Document doc, boolean autoLayout)
    {
        this(false, doc, autoLayout);
    }

     public LRFieldWithLabelPanel (final boolean autoLayout)
    {
         this((Document) null, autoLayout);
    }

    public LRFieldWithLabelPanel ()
    {
        this(true);
    }

    public LRFieldWithLabelPanel (boolean lblRightPos, Element elem, boolean autoLayout)
    {
        this(lblRightPos, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
    }

    public LRFieldWithLabelPanel (Element elem, boolean autoLayout)
    {
        this(false, elem, autoLayout);
    }

    public LRFieldWithLabelPanel (Element elem)
    {
        this(elem, true);
    }
    /**
     * Default components {@link Insets}
     * @see #getComponentsInsets()
     */
    public static final Insets    COMMON_INSETS=new Insets(5,5,5,5);

    private Insets    _insets=COMMON_INSETS;
    public Insets getComponentsInsets ()
    {
        return _insets;
    }

    public void setComponentsInsets (final Insets i)
    {
        if ((i != null) && (!i.equals(getComponentsInsets())))
            _insets = i;
    }
    /* Allow only GridBagLayout
     * @see java.awt.Container#setLayout(java.awt.LayoutManager)
     */
    @Override
    public void setLayout (final LayoutManager mgr)
    {
        if ((mgr != null) && (!(mgr instanceof GridBagLayout)))
            throw new IllegalArgumentException("setLayout(" + mgr.getClass().getName() + ") non-" + GridBagLayout.class.getSimpleName() + " N/A");

        super.setLayout(mgr);
    }

    protected void layoutComponent (final JLabel l, final JTextField f)
    {
        if ((null == l) && (null == f))
            return;    // should not happen

        final GridBagConstraints    gbc=new GridBagConstraints();
        final boolean                rightPos=isLabelRightPos();
        // initialize common settings
        gbc.gridy = 0;
        gbc.insets = getComponentsInsets();

        if (l != null)
        {
            gbc.gridx = rightPos ? 1 : 0;
            gbc.anchor = rightPos ?  GridBagConstraints.LINE_END : GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.NONE;
            gbc.gridwidth = 1;
            gbc.weightx = 0.0;

            add(l, gbc);
        }

        if (f != null)
        {
            gbc.gridx = rightPos ? 0 : 1;
            gbc.anchor = rightPos ? GridBagConstraints.LINE_START : GridBagConstraints.LINE_END;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = GridBagConstraints.RELATIVE;
            gbc.weightx = 1.0;

            add(f, gbc);
        }
    }
    /*
     * @see net.community.chest.swing.component.panel.input.AbstractInputTextPanel#layoutComponent(javax.swing.JTextField)
     */
    @Override
    protected void layoutComponent (final JTextField f)
    {
        layoutComponent(getLabel(true), f);
    }
}
