/*
 *
 */
package net.community.apps.common.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.community.chest.awt.layout.border.BorderLayoutPosition;
import net.community.chest.ui.components.icon.ColorIcon;
import net.community.chest.ui.components.icon.IconShape;
import net.community.chest.ui.components.spinner.margin.MarginSpinner;
import net.community.chest.ui.components.spinner.margin.icon.IconMarginPanel;
import net.community.chest.ui.helpers.combobox.EnumComboBox;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 28, 2009 3:31:51 PM
 */
public class TestColorIconFrame extends TestMainFrame {
    /**
     *
     */
    private static final long serialVersionUID = 7569775950848167047L;
    public TestColorIconFrame (String... args) throws Exception
    {
        super(args);
    }

    private JLabel    _imgLabel    /* =null */;
    private ColorIcon getImageIcon (boolean cloneIt)
    {
        final Icon    i=(null == _imgLabel) ? null : _imgLabel.getIcon();
        if (i instanceof ColorIcon)
            return cloneIt ? ((ColorIcon) i).clone() : (ColorIcon) i;

        return new ColorIcon();
    }

    private JColorChooser    _tcc;
    protected void updateLegendColor ()
    {
        final Color    c=(null == _tcc) ? null : _tcc.getColor();
        if ((null == c) || (_imgLabel == null))
            return;

        final ColorIcon    icon=getImageIcon(true);
        icon.setIconColor(c);
        _imgLabel.setIcon(icon);
    }

    protected void updateLegendShape (IconShape s)
    {
        if ((null == s) || (_imgLabel == null))
            return;

        final ColorIcon    icon=getImageIcon(true);
        icon.setIconShape(s);
        _imgLabel.setIcon(icon);
    }

    protected void updateIconMargin (BorderLayoutPosition p, Number v)
    {
        if ((null == p) || (null == v) || (_imgLabel == null))
            return;

        final ColorIcon    icon=getImageIcon(true);
        final Insets    i=icon.getMargin(), m=(null == i) ? new Insets(0, 0, 0, 0) : i;
        switch(p)
        {
            case NORTH    : m.top = v.intValue(); break;
            case SOUTH    : m.bottom = v.intValue(); break;
            case EAST    : m.right = v.intValue(); break;
            case WEST    : m.left = v.intValue(); break;
            default        : return;
        }

        icon.setMargin(m);
        _imgLabel.setIcon(icon);
    }
    /**
     * Icon shape choice
     * @author Lyor G.
     * @since Jan 29, 2009 12:12:26 PM
     */
    private static class ColorIconShapeChoice extends EnumComboBox<IconShape> {
        /**
         *
         */
        private static final long serialVersionUID = -3177868560082813359L;

        public ColorIconShapeChoice ()
        {
            super(IconShape.class, false);
            setEnumValues(IconShape.VALUES);
            populate();
        }
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        final Container    ct=getContentPane();

        if (null == _imgLabel)
            _imgLabel = new JLabel("Legend", new ColorIcon(), SwingConstants.CENTER);

        final Border    b=BorderFactory.createLineBorder(Color.black, 1);

        {
            final JPanel    northPanel=new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            northPanel.add(_imgLabel);

            final ColorIconShapeChoice    sc=new ColorIconShapeChoice();
            final ColorIcon                icon=getImageIcon(false);
            final IconShape    s=(null == icon) ? null : icon.getIconShape();
            if (s != null)
                sc.setSelectedValue(s);
            sc.setBorder(BorderFactory.createTitledBorder(b, "Shape"));
            sc.addActionListener(new ActionListener() {
                    /*
                     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                     */
                    @Override
                    public void actionPerformed (ActionEvent e)
                    {
                        final Object    src=(null == e) ? null : e.getSource();
                        if (src instanceof ColorIconShapeChoice)
                            updateLegendShape(((ColorIconShapeChoice) src).getSelectedValue());
                    }
                });
            northPanel.add(sc);

            ct.add(northPanel, BorderLayout.NORTH);
        }

        {
            final IconMarginPanel    marginPanel=new IconMarginPanel();
            marginPanel.setBorder(BorderFactory.createTitledBorder(b, "Margin"));
            marginPanel.addChangeListener(new ChangeListener() {
                    /*
                     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
                     */
                    @Override
                    public void stateChanged (ChangeEvent e)
                    {
                        final Object    src=(null == e) ? null : e.getSource();
                        if (src instanceof MarginSpinner)
                        {
                            final MarginSpinner    ms=(MarginSpinner) src;
                            final SpinnerModel    m=ms.getModel();
                            if (m instanceof SpinnerNumberModel)
                                updateIconMargin(ms.getPosition(), ((SpinnerNumberModel) m).getNumber());
                        }
                    }
                });

            ct.add(marginPanel, BorderLayout.EAST);
        }

        if (null == _tcc)
        {
            _tcc = new JColorChooser();
            _tcc.setBorder(BorderFactory.createTitledBorder("Choose Text Color"));
            _tcc.getSelectionModel().addChangeListener(new ChangeListener() {
                    /*
                     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
                     */
                    @Override
                    public void stateChanged (ChangeEvent e)
                    {
                        final Object    src=(null == e) ? null : e.getSource();
                        if (src != null)
                            updateLegendColor();
                    }
                });
            ct.add(_tcc, BorderLayout.CENTER);
        }
    }
}
