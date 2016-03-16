package net.community.apps.common;

import java.awt.Font;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import net.community.apps.common.resources.BaseAnchor;
import net.community.chest.CoVariantReturn;
import net.community.chest.awt.dnd.DnDUtils;
import net.community.chest.awt.font.FontUtils;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.ElementIndicatorExceptionContainer;
import net.community.chest.dom.impl.EmptyDocumentImpl;
import net.community.chest.dom.proxy.AbstractXmlProxyConverter;
import net.community.chest.dom.proxy.ReflectiveResourceLoaderContext;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.menu.MenuUtil;
import net.community.chest.swing.component.toolbar.JToolBarReflectiveProxy;
import net.community.chest.swing.component.toolbar.ToolBarUtils;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.util.logging.LoggerWrapper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful base class for various applications - provides a status bar,
 * main menu bar, etc.</P>
 *
 * @param <A> The generic {@link BaseAnchor} type
 * @author Lyor G.
 * @since Aug 8, 2007 10:41:36 AM
 */
public abstract class BaseMainFrame<A extends BaseAnchor> extends FilesLoadMainFrame
        implements MainComponent<A>, DropTargetListener {
    /**
     *
     */
    private static final long serialVersionUID = -3539012581945629747L;
    /*
     * @see net.community.apps.common.MainComponent#getMainFrame()
     */
    @Override
    @CoVariantReturn
    public BaseMainFrame<A> getMainFrame ()
    {
        return this;
    }
    /*
     * @see net.community.apps.common.MainComponent#getDefaultResourcesLoader()
     */
    @Override
    public ReflectiveResourceLoaderContext getDefaultResourcesLoader ()
    {
        return getResourcesAnchor();
    }
    /*
     * @see net.community.chest.ui.helpers.frame.HelperFrame#getComponentDocument()
     */
    @Override
    public synchronized Document getComponentDocument () throws RuntimeException
    {
        Document    doc=super.getComponentDocument();
        if ((null == doc) || (doc instanceof EmptyDocumentImpl))
        {
            final BaseAnchor    a=getResourcesAnchor();
            try
            {
                doc = a.getDefaultDocument();
            }
            catch(Exception e)
            {
                if (e instanceof RuntimeException)
                    throw (RuntimeException) e;
                else
                    throw new RuntimeException(e);
            }
        }

        return doc;
    }
    /*
     * @see net.community.apps.common.MainComponent#getMainToolBarElement()
     */
    @Override
    public Element getMainToolBarElement ()
    {
        return getSection(MAIN_TOOLBAR_SECTION_NAME);
    }

    public XmlValueInstantiator<? extends JToolBar> getToolBarConverter (final Element elem) throws Exception
    {
        return (null == elem) ? null : JToolBarReflectiveProxy.TOOLBAR;
    }
    /*
     * @see net.community.apps.common.MainComponent#getMainToolBar()
     */
    @Override
    public JToolBar getMainToolBar ()
    {
        try
        {
            final Element                                    elem=getMainToolBarElement();
            final XmlValueInstantiator<? extends JToolBar>    inst=getToolBarConverter(elem);
            return inst.fromXml(elem);
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }
    /*
     * @see net.community.apps.common.MainComponent#getMainFontsElement()
     */
    @Override
    public Element getMainFontsElement ()
    {
        return getSection(MAIN_FONTS_SECTION_NAME);
    }
    /*
     * @see net.community.apps.common.FileLoadComponent#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void loadFile (File f, String cmd, Element dlgElement)
    {
        if (f != null)
            throw new UnsupportedOperationException("loadFile(" + cmd + " - " + f + ")[" + DOMUtils.toString(dlgElement) + "] N/A");
    }
    /*
     * @see net.community.apps.common.BaseMainComponent#saveFile(java.io.File, org.w3c.dom.Element)
     */
    @Override
    public void saveFile (final File f, final Element dlgElement)
    {
        if (f != null)
            throw new UnsupportedOperationException("saveFile(" + f + ")[" + DOMUtils.toString(dlgElement) + "] N/A");
    }

    private Map<String,Font>    _fontsMap    /* =null */;
    /*
     * @see net.community.apps.common.MainComponent#getMainFontsMap()
     */
    @Override
    public synchronized Map<String,Font> getMainFontsMap ()
    {
        if (null == _fontsMap)
        {
            final Element                        fontsElem=getMainFontsElement();
            final Map<String,? extends Element>    fonts=DOMUtils.getSubsections(fontsElem, FontUtils.DEFAULT_FONT_ELEMNAME, "id");
            _fontsMap = new TreeMap<String, Font>(String.CASE_INSENSITIVE_ORDER);

            final Collection<? extends ElementIndicatorExceptionContainer>    errs=FontUtils.updateFontsMap(fonts, _fontsMap);
            if ((errs != null) && (errs.size() > 0))
            {
                for (final ElementIndicatorExceptionContainer ind : errs)
                {
                    final Element    elem=(null == ind) ? null : ind.getObjectValue();
                    final String    id=(null == elem) ? null : elem.getAttribute("id");
                    final Throwable    t=(null == ind) ? null : ind.getCause();
                    if (t != null)
                        getLogger().error("getFontsMap(" + id + ") " + t.getClass().getName() + ": " + t.getMessage(), t);
                }
            }
        }

        return _fontsMap;
    }
    /*
     * @see net.community.apps.common.MainComponent#getMainFont(java.lang.String)
     */
    @Override
    public final Font getMainFont (final String id)
    {
        return getMainFontsMap().get(id);
    }

    // NOTE !!! if return something other than a JLabel, then should also override "updateStatusBar" method
    protected JComponent createStatusBar ()
    {
        return new JLabel("Ready");
    }

    protected abstract LoggerWrapper getLogger();
    /*
     * @see net.community.apps.common.FilesLoadMainFrame#showManifest()
     */
    @Override
    public void showManifest ()
    {
        try
        {
            super.showManifest();
        }
        catch(Exception e)
        {
            BaseOptionPane.showMessageDialog(this, e);
        }
    }

    protected List<String>    m_args;
    public List<String> getArguments ()
    {
        return m_args;
    }

    private static JFrame    _instance;
    public static final synchronized JFrame getMainFrameInstance ()
    {
        return _instance;
    }

    protected Map<String,AbstractButton> setToolBarHandlers (final JToolBar b)
    {
        final Map<String,AbstractButton>    handlersMap=ToolBarUtils.setToolBarHandlers(b, getActionListenersMap(true));
        MenuUtil.syncFromMenuBar(handlersMap, getJMenuBar());
        return handlersMap;
    }

    public static final String    MAIN_FRAME_ELEM_NAME="main-frame";
    /*
     * @see net.community.chest.ui.helpers.frame.HelperFrame#layoutSection(java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void layoutSection (String name, Element elem) throws RuntimeException
    {
        if ((elem != null) && MAIN_FRAME_ELEM_NAME.equalsIgnoreCase(name))
            layoutComponent(elem);
    }
    /*
     * @see net.community.chest.ui.helpers.frame.HelperFrame#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        setMainMenuActionHandlers(getJMenuBar());
    }
    /*
     * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
     */
    @Override
    public void dragEnter (DropTargetDragEvent dtde)
    {
        // ignored
    }
    /*
     * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
     */
    @Override
    public void dragExit (DropTargetEvent dte)
    {
        // ignored
    }
    /*
     * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
     */
    @Override
    public void dragOver (DropTargetDragEvent dtde)
    {
        // ignored
    }
    /*
     * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
     */
    @Override
    public void dropActionChanged (DropTargetDragEvent dtde)
    {
        // ignored
    }
    /*
     * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
     */
    @Override
    public void drop (final DropTargetDropEvent dtde)
    {
        try
        {
            final List<File>    fl=DnDUtils.getFiles(dtde);
            if ((null == fl) || (fl.size() <= 0))
                throw new UnsupportedOperationException("No valid value found");

            loadFiles(LOAD_CMD, null, fl);
            dtde.dropComplete(true);
        }
        catch (Exception e)
        {
            getLogger().error(e.getClass().getName() + " while handle drop action: " + e.getMessage(), e);
            BaseOptionPane.showMessageDialog(this, e);

            if (dtde != null)
                dtde.rejectDrop();
        }
    }

    protected BaseMainFrame (final boolean autoInit, final String... args) throws Exception
    {
        super(false);    // don't auto-init in super constructor since we will do it here

        synchronized(BaseMainFrame.class)
        {
            if (_instance != null)
                throw new IllegalStateException("Multiple MainFrame(s) registered");
            _instance = this;
        }

        m_args = ((null == args) || (args.length <= 0)) ? null : Arrays.asList(args);

        final ReflectiveResourceLoaderContext    resContext=getDefaultResourcesLoader();
        if (resContext != null)
            AbstractXmlProxyConverter.setDefaultLoader(resContext);

        // make the frame appear in mid-screen by default
        setLocationRelativeTo(null);

        if (autoInit)
            layoutComponent();
    }

    protected BaseMainFrame (final String... args) throws Exception
    {
        this(true, args);
    }
}
