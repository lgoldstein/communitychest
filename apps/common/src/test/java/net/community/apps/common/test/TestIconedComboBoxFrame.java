/*
 *
 */
package net.community.apps.common.test;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.io.File;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import net.community.chest.awt.image.ICOReader;
import net.community.chest.ui.components.datetime.DayOfWeekAndMonthPanel;
import net.community.chest.ui.helpers.combobox.IconedTypedComboBoxRenderer;
import net.community.chest.ui.helpers.combobox.TypedComboBox;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 3, 2008 5:44:12 PM
 */
public class TestIconedComboBoxFrame extends TestMainFrame {
    /**
     *
     */
    private static final long serialVersionUID = 7810436080099299134L;
    private TypedComboBox<Image>    _imgSel    /* =null */;
    /*
     * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void loadFile (File ldFile, String cmd, Element dlgElement)
    {
        final String filePath=(null == ldFile) ? null : ldFile.getAbsolutePath();
        if ((null == filePath) || (filePath.length() <= 0))
            return;

        final DefaultComboBoxModel    model=(null == _imgSel) ? null : _imgSel.getModel();
        if (model != null)
            model.removeAllElements();

        final File[]    ff=ldFile.isDirectory() ? ldFile.listFiles() : null;
        if ((null == ff) || (ff.length <= 0))
        {
            JOptionPane.showMessageDialog(this, "No files loaded", "No files", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (final File f : ff)
        {
            final String    iconPath=(null == f) ? null : f.getAbsolutePath();
            if ((null == f)
             || (!f.isFile())
             || (!f.canRead())
             || (!ICOReader.isIconFile(iconPath)))
                continue;

            try
            {
                final List<? extends Image>    il=ICOReader.parseICOImages(f);
                final int                    numImages=(null == il) ? 0 : il.size();
                if (numImages <= 0)
                    throw new NullPointerException("No images loaded");

                final String    n=f.getName();
                for (int    iIndex=0; iIndex < numImages; iIndex++)
                {
                    final Image    img=il.get(iIndex);
                    if (null == img)
                        continue;

                    _imgSel.addItem(n + "[" + iIndex + "]", img);
                }
            }
            catch(Exception e)
            {
                JOptionPane.showMessageDialog(this, iconPath + ": " + e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static class ImagesRenderer extends IconedTypedComboBoxRenderer<Image> {
        /**
         *
         */
        private static final long serialVersionUID = 4367598415473001426L;
        public ImagesRenderer ()
        {
            super(Image.class);
        }
        /*
         * @see net.community.chest.ui.helpers.combobox.IconedTypedComboBoxRenderer#getValueIcon(int, java.lang.Object)
         */
        @Override
        public Icon getValueIcon (int index, Image value)
        {
            if ((index < 0) || (null == value))
                return null;

            return new ImageIcon(value);
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
            _imgSel = new TypedComboBox<Image>(Image.class);
            _imgSel.setRenderer(new ImagesRenderer());
            _imgSel.setOpaque(false);

            ctPane.add(_imgSel, BorderLayout.NORTH);
        }

        ctPane.add(new DayOfWeekAndMonthPanel(), BorderLayout.CENTER);
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
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return fc;
    }

    public TestIconedComboBoxFrame (String... args) throws Exception
    {
        super(args);

        if ((args != null) && (args.length == 1))
            loadFile(new File(args[0]), LOAD_CMD, null);
    }
}
