/*
 *
 */
package net.community.chest.ui.components.dialog.load.xml;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import net.community.chest.awt.attributes.Textable;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.swing.component.text.BaseTextArea;
import net.community.chest.ui.helpers.combobox.EnumComboBox;
import net.community.chest.ui.helpers.panel.PresetBorderLayoutPanel;
import net.community.chest.ui.helpers.panel.input.FileInputTextPanel;
import net.community.chest.ui.helpers.text.URLInputTextField;
import net.community.chest.util.map.MapEntryImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Presents an import from XML file, text or URL</P>
 * @author Lyor G.
 * @since Mar 31, 2009 12:14:12 PM
 */
public class XmlImportPanel extends PresetBorderLayoutPanel implements Textable {
    /**
     *
     */
    private static final long serialVersionUID = -1337707541621532135L;
    public XmlImportPanel (int hgap, int vgap, Document doc, boolean autoLayout)
    {
        super(hgap, vgap, doc, autoLayout);
    }

    public XmlImportPanel (int hgap, int vgap, boolean autoLayout)
    {
        this(hgap, vgap, (Document) null, autoLayout);
    }

    public XmlImportPanel (int hgap, int vgap)
    {
        this(hgap, vgap, true);
    }

    public XmlImportPanel (int hgap, int vgap, Element elem, boolean autoLayout)
    {
        this(hgap, vgap, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
    }

    public static final int    DEFAULT_HGAP=5, DEFAULT_VGAP=5;
    public XmlImportPanel (Document doc, boolean autoLayout)
    {
        this(DEFAULT_HGAP, DEFAULT_VGAP, doc, autoLayout);
    }

    public XmlImportPanel (Document doc)
    {
        this(doc, true);
    }

    public XmlImportPanel (Element elem, boolean autoLayout)
    {
        this(DEFAULT_HGAP, DEFAULT_VGAP, elem, autoLayout);
    }

    public XmlImportPanel (Element elem)
    {
        this(elem, true);
    }

    public XmlImportPanel (boolean autoLayout)
    {
        this((Document) null, autoLayout);
    }

    public XmlImportPanel ()
    {
        this(true);
    }

    private EnumComboBox<XmlImportSource>    _srcChoice    /* =null */;
    protected EnumComboBox<XmlImportSource> getSourceChoice (final boolean createIfNotExist)
    {
        if ((null == _srcChoice) && createIfNotExist)
            _srcChoice = new XmlImportSourceChoice();
        return _srcChoice;
    }

    public EnumComboBox<XmlImportSource> getSourceChoice ()
    {
        return getSourceChoice(false);
    }

    public void setSourceChoice (EnumComboBox<XmlImportSource> sc)
    {
        _srcChoice = sc;
    }

    public XmlImportSource getSelectedSource ()
    {
        final EnumComboBox<XmlImportSource>    cb=getSourceChoice();
        if (null == cb)
            return null;

        return cb.getSelectedValue();
    }

    private Component    _dataChoice;
    protected Component getDataChoice ()
    {
        return _dataChoice;
    }

    protected void setDataChoice (Component c)
    {
        _dataChoice = c;
    }
    /*
     * @see net.community.chest.awt.attributes.Textable#getText()
     */
    @Override
    public String getText ()
    {
        final Component    c=getDataChoice();
        if (c instanceof Textable)
            return ((Textable) c).getText();
        return null;
    }
    /*
     * @see net.community.chest.awt.attributes.Textable#setText(java.lang.String)
     */
    @Override
    public void setText (String t)
    {
        final Component    c=getDataChoice();
        if (c instanceof Textable)
            ((Textable) c).setText(t);
    }

    private Container    _northContainer;
    protected Container getNorthContainer ()
    {
        return _northContainer;
    }

    protected void setNorthContainer (Container c)
    {
        _northContainer = c;
    }

    protected Component removeDataChoice ()
    {
        final Component    c=getDataChoice();
        if (c != null)
        {
            if (!(c instanceof JTextArea))
            {
                final Container    nc=getNorthContainer();
                if (nc != null)
                    nc.remove(c);
                else
                    remove(c);
            }
            else
                remove(c);
            setDataChoice(null);
        }

        return c;
    }

    protected Component createDataChoice (final XmlImportSource s)
    {
        if (null == s)
            return null;

        switch(s)
        {
            case FILE    :
                return new FileInputTextPanel();

            case TEXT    :
                return new BaseTextArea();

            case URL    :
                return new URLInputTextField();

            default        :
                throw new NoSuchElementException("createDataChoice(" + s + ") unknown source");
        }
    }
    // returns the previous and the new component
    protected Map.Entry<Component,Component> handleSelectedImportSource (final XmlImportSource s)
    {
        if (null == s)
        {
            final Component    c=getDataChoice();
            return new MapEntryImpl<Component,Component>(c, c);
        }

        final Component    prev=removeDataChoice(), cur=createDataChoice(s);
        if (cur != null)
        {
            if (!(cur instanceof JTextArea))
            {
                final Container    nc=getNorthContainer();
                if (nc != null)
                    nc.add(cur);
                else
                    add(cur, BorderLayout.SOUTH);
            }
            else
                add(cur, BorderLayout.CENTER);

            setDataChoice(cur);
        }

        updateUI();

        return new MapEntryImpl<Component,Component>(prev, cur);
    }

    public void setSelectedSource (XmlImportSource s)
    {
        if (null == s)
            return;

        final EnumComboBox<XmlImportSource>    cb=getSourceChoice();
        if (cb != null)    // TODO check if need to invoke "handleSelectedImportSource"
            cb.setSelectedValue(s);
    }

    protected class DefaultSourceChoiceListener extends XmlImportSourceChoiceListener {
        protected DefaultSourceChoiceListener ()
        {
            super();
        }
        /*
         * @see net.community.chest.ui.helpers.combobox.TypedComboBoxActionListener#handleSelectedItem(java.awt.event.ActionEvent, net.community.chest.ui.helpers.combobox.TypedComboBox, java.lang.String, java.lang.Object)
         */
        @Override
        public void handleSelectedItem (ActionEvent e, EnumComboBox<XmlImportSource> cb, String text, XmlImportSource value)
        {
            if ((e != null) && (cb != null) && (value != null))
                handleSelectedImportSource(value);
        }
    }

    private ActionListener    _slc;
    protected ActionListener getSourceChoiceListener (final boolean createIfNotExist)
    {
        if ((null == _slc) && createIfNotExist)
            _slc = new DefaultSourceChoiceListener();
        return _slc;
    }

    public ActionListener getSourceChoiceListener ()
    {
        return getSourceChoiceListener(false);
    }

    public void setSourceChoiceListener (ActionListener l)
    {
        _slc = l;
    }

    protected Container createNorthContainer ()
    {
        final Container        nc=new JPanel();
        nc.setLayout(new BoxLayout(nc, BoxLayout.Y_AXIS));
        return nc;
    }

    protected Component layoutSourceChoice (final JComboBox cb)
    {
        if (null == cb)
            return null;

        final Container    nc=createNorthContainer();
        if (nc != null)
        {
            nc.add(cb);
            return nc;
        }

        return cb;
    }

    protected Component layoutSourceChoice ()
    {
        final JComboBox            cb=getSourceChoice(true);
        final ActionListener    l=getSourceChoiceListener(true);
        if (cb != null)
        {
            cb.setSelectedIndex(0);

            if (l != null)
                cb.addActionListener(l);
        }

        final Component    c=layoutSourceChoice(cb);
        if (c != null)
        {
            add(c, BorderLayout.NORTH);

            if (c instanceof Container)
                setNorthContainer((Container) c);
        }

        return c;
    }

    protected void layoutDataChoice (XmlImportSource s)
    {
        handleSelectedImportSource(s);
    }
     /*
     * @see net.community.chest.ui.helpers.panel.HelperPanel#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        layoutSourceChoice();
        layoutDataChoice(getSelectedSource());
    }
}
