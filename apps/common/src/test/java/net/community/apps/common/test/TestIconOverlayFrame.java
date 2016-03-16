/*
 *
 */
package net.community.apps.common.test;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import net.community.chest.awt.image.AbstractImageReader;
import net.community.chest.awt.image.BMPReader;
import net.community.chest.awt.image.DefaultImageReader;
import net.community.chest.awt.image.ICOReader;
import net.community.chest.awt.image.ImageUtils;
import net.community.chest.awt.layout.border.BorderLayoutPosition;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.ui.components.combobox.ImagesComboBox;
import net.community.chest.ui.helpers.combobox.EnumComboBox;
import net.community.chest.ui.helpers.combobox.TypedComboBox;
import net.community.chest.ui.helpers.combobox.TypedComboBoxActionListener;
import net.community.chest.ui.helpers.dialog.ButtonsPanel;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 10, 2009 9:44:41 AM
 */
public class TestIconOverlayFrame extends TestMainFrame {
    /**
     *
     */
    private static final long serialVersionUID = -372825643911706591L;
    private JLabel    _lblImage    /* =null */;
    protected void showImage (final Image img)
    {
        if (_lblImage != null)
            _lblImage.setIcon((null == img) ? null : new ImageIcon(img));
    }

    private TypedComboBox<Image>    _imgSel    /* =null */;
    public Image getSelectedImage ()
    {
        return (null == _imgSel) ? null : _imgSel.getSelectedValue();
    }

    public void setImages (final List<? extends Image> il)
    {
        if (_imgSel != null)
        {
            _imgSel.removeAllItems();
            _imgSel.setEnabled(false);
        }

        final int    numImages=(null == il) ? 0 : il.size();
        if ((numImages <= 0) || (null == _imgSel))
            return;

        int    idx1=(-1);
        for (int    i=0; i < numImages; i++)
        {
            final Image    img=il.get(i);
            if (null == img)
                continue;

            _imgSel.addItem(String.valueOf(i), img);
            if (idx1 < 0)
                idx1 = i;
        }

        if (idx1 >= 0)
        {
            _imgSel.setSelectedIndex(idx1);
            _imgSel.setEnabled(true);
        }
    }

    protected static class BorderLayoutPositionChoice extends EnumComboBox<BorderLayoutPosition> {
        /**
         *
         */
        private static final long serialVersionUID = 8727640624469965911L;

        public BorderLayoutPositionChoice ()
        {
            super(BorderLayoutPosition.class);
            setEnumValues(BorderLayoutPosition.VALUES);
            populate();
        }
    }

    private BorderLayoutPositionChoice    _posChoice;
    public BorderLayoutPosition getOverlayPosition ()
    {
        return (null == _posChoice) ? null : _posChoice.getSelectedValue();
    }

    private JSpinner    _wPct, _hPct;
    public int getWidthPercentage ()
    {
        final Object    v=(null == _wPct) ? null : _wPct.getValue();
        return (v instanceof Number) ? ((Number) v).intValue() : (-1);
    }

    public int getHeightPercentage ()
    {
        final Object    v=(null == _hPct) ? null : _hPct.getValue();
        return (v instanceof Number) ? ((Number) v).intValue() : (-1);
    }

    private Icon getOverlayIcon (final Icon iOrig, final Icon iOvrl)
    {
        return ImageUtils.getOverlayIcon(iOrig, iOvrl,
                getOverlayPosition(), getWidthPercentage(), getHeightPercentage(),
                this
            );
    }

    private JLabel    _ovrLabel, _imgLabel;
    protected void createOverlayImage ()
    {
        if (null == _lblImage)
            return;

        final Icon    iOrig=(null == _imgLabel) ? null : _imgLabel.getIcon(),
                    iOvrl=(null == _ovrLabel) ? null : _ovrLabel.getIcon();
        if (null == iOrig)
            _lblImage.setIcon(iOvrl);
        else if (null == iOvrl)
            _lblImage.setIcon(iOrig);
        else
            _lblImage.setIcon(getOverlayIcon(iOrig, iOvrl));
        _lblImage.setText(String.valueOf(getOverlayPosition()) + "[" + getWidthPercentage() + "/" + getHeightPercentage() + "]");
    }

    public void loadImageOverlayFile (final File f, final boolean refreshOverlay)
    {
        final String filePath=(null == f) ? null : f.getAbsolutePath();
        if ((null == filePath) || (filePath.length() <= 0))
            return;

        try
        {
            final AbstractImageReader    r;
            if (ICOReader.isIconFile(filePath))
                r = ICOReader.DEFAULT;
            else if (BMPReader.isBitmapFile(filePath))
                r = new BMPReader();
            else
                r = DefaultImageReader.DEFAULT;

            final List<? extends Image>    il=r.readImages(f);
            final int                    numImages=(null == il) ? 0 : il.size();
            _logger.info("loadImageOverlayFile(" + filePath + ") loaded " + numImages + " images");
            setImages(il);

            if (_ovrLabel != null)
            {
                final Image    img=(numImages > 0) ? il.get(0) : null;
                final Icon    i=(null == img) ? null : new ImageIcon(img);
                if (i != null)
                    _ovrLabel.setIcon(i);
                _ovrLabel.setText(f.getName());
                _ovrLabel.setToolTipText(filePath);
            }

            if (refreshOverlay)
                createOverlayImage();

            setInitialFileChooserFolder(f, Boolean.FALSE);
        }
        catch(Exception e)
        {
            BaseOptionPane.showMessageDialog(this, e);
        }
    }

    public static final String    OVERLAY_CMD="overlay";
    protected void loadImageOverlayFile ()
    {
        final Element                dlgElement=getLoadDialogElement();
        final List<? extends File>    fl=
            getChosenFiles(dlgElement, OVERLAY_CMD, false);
        final int                    numFiles=
            (null == fl) ? 0 : fl.size();
        if (1 == numFiles)
            loadImageOverlayFile(fl.get(0), true);
    }

    protected void loadFile (final File f, final boolean refreshOverlay)
    {
        final String filePath=(null == f) ? null : f.getAbsolutePath();
        if ((null == filePath) || (filePath.length() <= 0))
            return;

        try
        {
            final FileSystemView    v=FileSystemView.getFileSystemView();
            _imgLabel.setText(f.getAbsolutePath());

            final Icon    icon=v.getSystemIcon(f);
            if (icon != null)
                _imgLabel.setIcon(icon);
            if (refreshOverlay)
                createOverlayImage();
            setInitialFileChooserFolder(f, Boolean.FALSE);
        }
        catch(Exception e)
        {
            BaseOptionPane.showMessageDialog(this, e);
        }
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void loadFile (File f, String cmd, Element dlgElement)
    {
        loadFile(f, true);
    }

    protected void resetDefaultImage ()
    {
        final String filePath=(null == _imgLabel) ? null : _imgLabel.getText();
        if ((null == filePath) || (filePath.length() <= 0))
            return;
        loadFile(new File(filePath), false);
    }
    /*
     * @see net.community.apps.common.FilesLoadMainFrame#getFileChooser(org.w3c.dom.Element, java.lang.String, java.lang.Boolean)
     */
    @Override
    protected JFileChooser getFileChooser (
            final Element dlgElement, final String cmd, final Boolean isSaveDialog)
    {
        final JFileChooser    fc=super.getFileChooser(dlgElement, cmd, isSaveDialog);
        if (fc != null)
        {
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (OVERLAY_CMD.equalsIgnoreCase(cmd))
                fc.setFileFilter(new FileNameExtensionFilter("Image files", AbstractImageReader.GIF_SUFFIX, AbstractImageReader.JPG_SUFFIX, AbstractImageReader.PNG_SUFFIX, BMPReader.BMP_SUFFIX, ICOReader.ICO_SUFFIX));
        }

        return fc;
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        final JPanel    northPanel=new JPanel(new GridLayout(0, 1));
        if (null == _imgLabel)
            _imgLabel = new JLabel("");
        northPanel.add(_imgLabel);

        if (null == _imgSel)
            _imgSel = new ImagesComboBox.SimpleImagesComboBox();
        _imgSel.setOpaque(false);
        _imgSel.addActionListener(new TypedComboBoxActionListener<Image,ImagesComboBox<Image>>() {
                /*
                 * @see net.community.chest.ui.helpers.combobox.TypedComboBoxActionListener#handleSelectedItem(java.awt.event.ActionEvent, net.community.chest.ui.helpers.combobox.TypedComboBox, java.lang.String, java.lang.Object)
                 */
                @Override
                public void handleSelectedItem (ActionEvent e, ImagesComboBox<Image> cb, String text, Image value)
                {
                    createOverlayImage();
                }
            });
        northPanel.add(_imgSel);

        if (null == _posChoice)
            _posChoice = new BorderLayoutPositionChoice();
        _posChoice.addActionListener(new TypedComboBoxActionListener<BorderLayoutPosition,BorderLayoutPositionChoice>() {
            /*
             * @see net.community.chest.ui.helpers.combobox.TypedComboBoxActionListener#handleSelectedItem(java.awt.event.ActionEvent, net.community.chest.ui.helpers.combobox.TypedComboBox, java.lang.String, java.lang.Object)
             */
            @Override
            public void handleSelectedItem (ActionEvent e, BorderLayoutPositionChoice cb, String text, BorderLayoutPosition value)
            {
                createOverlayImage();
            }
        });
        northPanel.add(_posChoice);

        final Container    ctPane=getContentPane();
        ctPane.add(northPanel, BorderLayout.NORTH);

        final JPanel    southPanel=new JPanel(new GridLayout(0, 1));
        if (null == _lblImage)
            _lblImage = new JLabel("");
        ctPane.add(_lblImage, BorderLayout.CENTER);

        if (null == _ovrLabel)
            _ovrLabel = new JLabel("");
        southPanel.add(_ovrLabel);

        {
            final JPanel    pctPanel=new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            if (null == _wPct)
                _wPct = new JSpinner(new SpinnerNumberModel(60, 5, 100, 1));
            _wPct.addChangeListener(new ChangeListener() {
                    /*
                     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
                     */
                    @Override
                    public void stateChanged (ChangeEvent e)
                    {
                        if (e != null)
                            createOverlayImage();
                    }
                });
            pctPanel.add(_wPct);

            if (null == _hPct)
                _hPct = new JSpinner(new SpinnerNumberModel(60, 5, 100, 1));
            _hPct.addChangeListener(new ChangeListener() {
                    /*
                     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
                     */
                    @Override
                    public void stateChanged (ChangeEvent e)
                    {
                        if (e != null)
                            createOverlayImage();
                    }
                });
            pctPanel.add(_hPct);

            southPanel.add(pctPanel);
        }

        {
            final JPanel    btnsPanel=new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            ButtonsPanel.add(btnsPanel, null, JButton.class, "Load image", OVERLAY_CMD, new ActionListener() {
                    /*
                     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                     */
                    @Override
                    public void actionPerformed (ActionEvent e)
                    {
                        if (e != null)
                            loadImageOverlayFile();
                    }
                });
            ButtonsPanel.add(btnsPanel, null, JButton.class, "Reset", "reset", new ActionListener() {
                    /*
                     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                     */
                    @Override
                    public void actionPerformed (ActionEvent e)
                    {
                        if (e != null)
                            resetDefaultImage();
                    }
                });
            ButtonsPanel.add(btnsPanel, null, JButton.class, "Apply", "apply", new ActionListener() {
                    /*
                     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                     */
                    @Override
                    public void actionPerformed (ActionEvent e)
                    {
                        if (e != null)
                            createOverlayImage();
                    }
                });
            southPanel.add(btnsPanel);
        }
        ctPane.add(southPanel, BorderLayout.SOUTH);
    }
    private void processMainArgs (final String... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; aIndex < numArgs; aIndex++)
        {
            final String    a=args[aIndex];
            if ("-f".equals(a) || "--file".equals(a))
                loadFile(new File(args[++aIndex]), true);
            else if ("-o".equals(a) || "--overlay".equals(a))
                loadImageOverlayFile(new File(args[++aIndex]), true);
        }
    }

    public TestIconOverlayFrame (String... args) throws Exception
    {
        super(args);

        processMainArgs(args);
    }
}
