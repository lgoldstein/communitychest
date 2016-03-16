/*
 *
 */
package net.community.apps.common.test;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.community.chest.awt.image.AbstractImageReader;
import net.community.chest.awt.image.BMPReader;
import net.community.chest.awt.image.DefaultImageReader;
import net.community.chest.awt.image.ICOReader;
import net.community.chest.swing.component.label.DefaultLabelScroll;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.ui.components.combobox.ImagesComboBox;
import net.community.chest.ui.helpers.combobox.TypedComboBox;
import net.community.chest.ui.helpers.combobox.TypedComboBoxActionListener;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Displays loaded ICO files</P>
 *
 * @author Lyor G.
 * @since Nov 13, 2008 1:41:49 PM
 */
public class TestFilesReaderFrame extends TestMainFrame {
    /**
     *
     */
    private static final long serialVersionUID = 5742449879087735812L;
    private JLabel    _lblImage    /* =null */;
    protected void showImage (final Image img)
    {
        if (_lblImage != null)
            _lblImage.setIcon(new ImageIcon(img));
    }

    private TypedComboBox<Image>    _imgSel    /* =null */;
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
            showImage(il.get(idx1));
            _imgSel.setEnabled(true);
        }
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void loadFile (File f, String cmd, Element dlgElement)
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

            final List<? extends Image>    il=r.readImages(filePath);
            final int                    numImages=(null == il) ? 0 : il.size();
            JOptionPane.showMessageDialog(this, "Loaded " + numImages + " images", "Images loaded", JOptionPane.INFORMATION_MESSAGE);
            setTitle(filePath);
            setImages(il);
        }
        catch(Exception e)
        {
            BaseOptionPane.showMessageDialog(this, e);
        }
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        final Container    ctPane=getContentPane();
        if (null == _imgSel)
        {
            _imgSel = new ImagesComboBox.SimpleImagesComboBox();
            _imgSel.setOpaque(false);
            _imgSel.addActionListener(new TypedComboBoxActionListener<Image,ImagesComboBox<Image>>() {
                /*
                 * @see net.community.chest.ui.helpers.combobox.TypedComboBoxActionListener#handleSelectedItem(java.awt.event.ActionEvent, net.community.chest.ui.helpers.combobox.TypedComboBox, java.lang.String, java.lang.Object)
                 */
                @Override
                public void handleSelectedItem (ActionEvent e, ImagesComboBox<Image> cb, String text, Image value)
                {
                    showImage(value);
                }
            });
            ctPane.add(_imgSel, BorderLayout.NORTH);
        }

        if (null == _lblImage)
        {
            _lblImage = new JLabel();
            _lblImage.setPreferredSize(new Dimension(300, 300));

            final JScrollPane sp=new DefaultLabelScroll(_lblImage);
            sp.setOpaque(false);
            sp.getViewport().setOpaque(false);
            ctPane.add(sp, BorderLayout.CENTER);
        }
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
            fc.setFileFilter(new FileNameExtensionFilter("Image files", ICOReader.ICO_SUFFIX, BMPReader.BMP_SUFFIX));
        return fc;
    }

    public TestFilesReaderFrame (String... args) throws Exception
    {
        super(args);

        if ((args != null) && (args.length == 1))
            loadFile(new File(args[0]), LOAD_CMD, null);
    }
}
