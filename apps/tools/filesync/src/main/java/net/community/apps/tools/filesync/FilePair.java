/*
 *
 */
package net.community.apps.tools.filesync;

import java.awt.Component;
import java.awt.FlowLayout;
import java.io.File;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Icon;

import net.community.chest.awt.attributes.Iconable;
import net.community.chest.awt.attributes.Selectible;
import net.community.chest.awt.attributes.Textable;
import net.community.chest.swing.component.button.BaseCheckBox;
import net.community.chest.swing.component.label.BaseLabel;
import net.community.chest.ui.helpers.panel.HelperPanel;
import net.community.chest.util.BooleanIterator;

import org.w3c.dom.Document;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 15, 2009 3:29:07 PM
 */
public class FilePair extends HelperPanel implements Map.Entry<File,File>, Selectible, Iconable {
    /**
     *
     */
    private static final long serialVersionUID = 5744516500581439319L;
    private Textable    _srcText, _dstText;
    public Textable getSrcText ()
    {
        return _srcText;
    }

    public void setSrcText (Textable srcText)
    {
        _srcText = srcText;
    }

    public Textable getDstText ()
    {
        return _dstText;
    }

    public void setDstText (Textable dstText)
    {
        _dstText = dstText;
    }

    public Textable getPairText (Boolean srcValue)
    {
        if (null == srcValue)
            return null;

        if (srcValue.booleanValue())
            return getSrcText();
        else
            return getDstText();
    }

    public void setPairText (Boolean srcValue, Textable t)
    {
        if (null == srcValue)
            return;

        if (srcValue.booleanValue())
            setSrcText(t);
        else
            setDstText(t);
    }

    protected Textable createPairText (Boolean srcValue)
    {
        if (null == srcValue)
            return null;

        return new BaseLabel();
    }

    private File    _srcFolder, _dstFolder;
    public File getSrcFolder ()
    {
        return _srcFolder;
    }

    public void setSrcFolder (File srcFolder)
    {
        final Textable    t=getSrcText();
        if (t != null)
            t.setText((null == srcFolder) ? "" : srcFolder.getAbsolutePath());

        _srcFolder = srcFolder;
    }

    public File getDstFolder ()
    {
        return _dstFolder;
    }

    public void setDstFolder (File dstFolder)
    {
        final Textable    t=getDstText();
        if (t != null)
            t.setText((null == dstFolder) ? "" : dstFolder.getAbsolutePath());

        _dstFolder = dstFolder;
    }

    public File getFolder (Boolean srcValue)
    {
        if (null == srcValue)
            return null;

        if (srcValue.booleanValue())
            return getSrcFolder();
        else
            return getDstFolder();
    }

    public void setFolder (Boolean srcValue, File f)
    {
        if (null == srcValue)
            return;

        if (srcValue.booleanValue())
            setSrcFolder(f);
        else
            setDstFolder(f);
    }
    /*
     * @see java.util.Map.Entry#getKey()
     */
    @Override
    public File getKey ()
    {
        return getSrcFolder();
    }
    /*
     * @see java.util.Map.Entry#getValue()
     */
    @Override
    public File getValue ()
    {
        return getDstFolder();
    }
    /*
     * @see java.util.Map.Entry#setValue(java.lang.Object)
     */
    @Override
    public File setValue (File value)
    {
        final File    prev=getDstFolder();
        setDstFolder(value);
        return prev;
    }

    private Selectible    _selComp    /* =null */;
    public Selectible getSelectionComponent ()
    {
        return _selComp;
    }

    public void setSelectionComponent (Selectible selComp)
    {
        _selComp = selComp;
    }
    /*
     * @see net.community.chest.awt.attributes.Selectible#isSelected()
     */
    @Override
    public boolean isSelected ()
    {
        final Selectible    sc=getSelectionComponent();
        return (sc != null) && sc.isSelected();
    }
    /*
     * @see net.community.chest.awt.attributes.Selectible#setSelected(boolean)
     */
    @Override
    public void setSelected (boolean sel)
    {
        final Selectible    sc=getSelectionComponent();
        if (sc != null)
            sc.setSelected(sel);
    }

    protected Selectible createSelectionComponent ()
    {
        final Selectible    cb=new BaseCheckBox();
        cb.setSelected(true);
        return cb;
    }

    private Iconable    _sep;
    public Iconable getSeparator ()
    {
        return _sep;
    }

    public void setSeparator (Iconable s)
    {
        _sep = s;
    }
    /*
     * @see net.community.chest.awt.attributes.Iconable#getIcon()
     */
    @Override
    public Icon getIcon ()
    {
        final Iconable    s=getSeparator();
        return (null == s) ? null : s.getIcon();
    }
    /*
     * @see net.community.chest.awt.attributes.Iconable#setIcon(javax.swing.Icon)
     */
    @Override
    public void setIcon (Icon i)
    {
        final Iconable    s=getSeparator();
        if ((i != null) && (s != null))
            s.setIcon(i);
    }

    protected Iconable createSeparator ()
    {
        return new BaseLabel("");
    }
    /*
     * @see net.community.chest.ui.helpers.panel.HelperPanel#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        Selectible    sc=getSelectionComponent();
        if (null == sc)
        {
            if ((sc=createSelectionComponent()) != null)
                setSelectionComponent(sc);
        }

        if (sc instanceof Component)
            add((Component) sc);

         for (final Iterator<Boolean>    iter=new BooleanIterator(false); iter.hasNext(); )
         {
             final Boolean    srcValue=iter.next();
             Textable        t=getPairText(srcValue);
             if (null == t)
             {
                 if ((t=createPairText(srcValue)) != null)
                     setPairText(srcValue, t);
             }

             if (t instanceof Component)
                add((Component) t);
             if (srcValue.booleanValue())
             {
                 Iconable    i=getSeparator();
                 if (null == i)
                 {
                     if ((i=createSeparator()) != null)
                         setSeparator(i);
                 }

                 if (i instanceof Component)
                     add((Component) i);
             }
         }
    }
    /*
     * @see java.awt.Component#toString()
     */
    @Override
    public String toString ()
    {
        return String.valueOf(getSrcFolder()) + " => " + String.valueOf(getDstFolder());
    }

    public FilePair (File srcFolder, File dstFolder, Document doc, boolean autoLayout)
    {
        super(new FlowLayout(FlowLayout.LEFT, 5, 0), doc, autoLayout);

        setSrcFolder(srcFolder);
        setDstFolder(dstFolder);
    }

    public FilePair (File srcFolder, File dstFolder, Document doc)
    {
        this(srcFolder, dstFolder, doc, true);
    }

    public FilePair (File srcFolder, File dstFolder, boolean autoLayout)
    {
        this(srcFolder, dstFolder, null, autoLayout);
    }

    public FilePair (File srcFolder, File dstFolder)
    {
        this(srcFolder, dstFolder, true);
    }

    public FilePair (boolean autoLayout)
    {
        this(null, null, autoLayout);
    }

    public FilePair ()
    {
        this(true);
    }
}
