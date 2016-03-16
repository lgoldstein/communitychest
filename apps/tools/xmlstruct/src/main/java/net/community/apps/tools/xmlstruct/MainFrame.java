/*
 *
 */
package net.community.apps.tools.xmlstruct;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.community.apps.common.BaseMainFrame;
import net.community.apps.tools.xmlstruct.resources.ResourcesAnchor;
import net.community.chest.awt.dom.UIReflectiveAttributesProxy;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.NodeTypeEnum;
import net.community.chest.io.FileUtil;
import net.community.chest.io.dom.PrettyPrintTransformer;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.label.JLabelReflectiveProxy;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 5, 2009 2:56:09 PM
 */
final class MainFrame extends BaseMainFrame<ResourcesAnchor> {
    /**
     *
     */
    private static final long serialVersionUID = -4882335689608555199L;
    private static LoggerWrapper    _logger    /* =null */;
    /*
     * @see net.community.apps.common.BaseMainFrame#getLogger()
     */
    @Override
    protected synchronized LoggerWrapper getLogger ()
    {
        if (null == _logger)
            _logger = WrapperFactoryManager.getLogger(getClass());
        return _logger;
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#getResourcesAnchor()
     */
    @Override
    public ResourcesAnchor getResourcesAnchor ()
    {
        return ResourcesAnchor.getInstance();
    }

    public static final MainFrame getContainerFrameInstance ()
    {
        return MainFrame.class.cast(getMainFrameInstance());
    }

    private DocumentBuilderFactory    _defFactory;
    private synchronized DocumentBuilderFactory getDocumentBuilderFactory ()
    {
        if (null == _defFactory)
            _defFactory = DocumentBuilderFactory.newInstance();
        return _defFactory;
    }

    private Element    _optsDlgElem    /* =null */;
    /*
     * @see net.community.apps.common.BaseMainFrame#layoutSection(java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void layoutSection (String name, Element elem) throws RuntimeException
    {
        if ("doc-builder-options-dialog".equalsIgnoreCase(name))
        {
            if (_optsDlgElem != null)
                throw new IllegalStateException("layoutSection(" + name + ") already set");
            if (null == (_optsDlgElem=elem))
                throw new IllegalStateException("layoutSection(" + name + ") no data");
            return;
        }

        super.layoutSection(name, elem);
    }

    protected void showOptions ()
    {
        new DocBuilderOptionsDialog(this, getDocumentBuilderFactory(), _optsDlgElem, true)
                .setVisible(true);
    }

    private Map<NodeTypeEnum,Icon> getIconsMap (final Element iconsElem)
    {
        final Collection<? extends Element>    il=
            DOMUtils.extractAllNodes(Element.class, iconsElem, Node.ELEMENT_NODE);
        if ((null == il) || (il.size() <= 0))
            return null;

        final ResourcesAnchor    a=getResourcesAnchor();
        Map<NodeTypeEnum,Icon>    im=null;
        for (final Element elem : il)
        {
            final String        n=
                (null == elem) ? null : elem.getAttribute(UIReflectiveAttributesProxy.NAME_ATTR),
                                p=
                (null == elem) ? null : elem.getAttribute(JLabelReflectiveProxy.ICON_ATTR);
            final NodeTypeEnum    t=NodeTypeEnum.fromString(n);
            if ((null == t) || (null == p) || (p.length() <= 0))
                continue;

            final Icon    i;
            try
            {
                if (null == (i=a.getIcon(p)))
                    continue;
            }
            catch(Exception e)
            {
                getLogger().error("getIconsMap(" + t + ")[" + p + "] " + e.getClass().getName() + ": " + e.getMessage(), e);
                continue;
            }

            if (null == im)
                im = new EnumMap<NodeTypeEnum,Icon>(NodeTypeEnum.class);
            im.put(t, i);
        }

        return im;
    }

    private Map<NodeTypeEnum,Icon>    _iconsMap    /* =null */;
    Map<NodeTypeEnum,Icon> getIconsMap ()
    {
        if (null == _iconsMap)
            _iconsMap = getIconsMap(getSection("icons-map"));
        return _iconsMap;
    }

    private Document loadDocument (final String filePath) throws Exception
    {
        InputStream    in=null;
        try
        {
            in = new FileInputStream(filePath);

            final DocumentBuilderFactory    fac=getDocumentBuilderFactory();
            final DocumentBuilder            docBuilder;
            synchronized(fac)
            {
                docBuilder = fac.newDocumentBuilder();
            }

            return docBuilder.parse(in);
        }
        finally
        {
            FileUtil.closeAll(in);
        }
    }

    private DocStructPanel    _docTree    /* =null */;
    /*
     * @see net.community.apps.common.BaseMainFrame#loadFile(java.io.File, java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void loadFile (File f, String cmd, Element dlgElement)
    {
        final String    filePath=(null == f) ? null : f.getAbsolutePath();
        try
        {
            _docTree.setFilePath(filePath);
            _docTree.setDocument(loadDocument(filePath));
        }
        catch(Exception e)
        {
            getLogger().error("loadFile(" + filePath + ") " + e.getClass().getName() + ": " + e.getMessage());
            BaseOptionPane.showMessageDialog(this, e);
        }
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#saveFile(java.io.File, org.w3c.dom.Element)
     */
    @Override
    public void saveFile (File f, Element dlgElement)
    {
        if (null == f)
            return;

        if (f.exists())
        {
            final int    nRes=JOptionPane.showConfirmDialog(
                    this, "File already exists - overwrite ?", "Confirm file override",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (nRes != JOptionPane.YES_OPTION)
                return;
        }

        try
        {
            PrettyPrintTransformer.DEFAULT.transform(_docTree.getDocument(), f);
        }
        catch (Exception e)
        {
            getLogger().error("saveFile(" + f + ") " + e.getClass().getName() + ": " + e.getMessage());
            BaseOptionPane.showMessageDialog(this, e);
        }
    }

    private ActionListener    _optsListener;
    private synchronized ActionListener getOptionsActionListener ()
    {
        if (null == _optsListener)
            _optsListener = new ActionListener() {
                    /*
                     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                     */
                    @Override
                    public void actionPerformed (ActionEvent e)
                    {
                        if (e != null)
                            showOptions();
                    }
                };
        return _optsListener;
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#getActionListenersMap(boolean)
     */
    @Override
    protected Map<String,? extends ActionListener> getActionListenersMap (boolean createIfNotExist)
    {
        final Map<String,? extends ActionListener>    org=super.getActionListenersMap(createIfNotExist);
        if (((org != null) && (org.size() > 0)) || (!createIfNotExist))
            return org;

        final Map<String,ActionListener>    lm=new TreeMap<String,ActionListener>(String.CASE_INSENSITIVE_ORDER);
        lm.put(LOAD_CMD, getLoadFileListener());
        lm.put(SAVE_CMD, getSaveFileListener());
        lm.put("options", getOptionsActionListener());
        lm.put(EXIT_CMD, getExitActionListener());
        lm.put(ABOUT_CMD, getShowManifestActionListener());

        setActionListenersMap(lm);
        return lm;
    }

    private JLabel    _statusBar    /* =null */;
    public void updateStatusBar (final String text)
    {
        if (_statusBar != null)
            _statusBar.setText((null == text) ? "" : text);
    }

    protected void updateStatusBar (final DocStructNode<? extends Node> selNode)
    {
        final Node        n=(null == selNode) ? null : selNode.getAssignedValue();
        final String    t;
        if (n instanceof Element)
            t = DOMUtils.toString((Element) n);
        else if (n instanceof Attr)
            t = DOMUtils.toString((Attr) n);
        else
            t = (null == n) ? null : n.getNodeValue();

        updateStatusBar(t);
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        final Container        ctPane=getContentPane();
        try
        {
            final JToolBar    b=getMainToolBar();
            setToolBarHandlers(b);
            ctPane.add(b, BorderLayout.NORTH);
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }

        if (null == _docTree)
        {
            _docTree = new DocStructPanel();

            final JTree    dt=_docTree.getDocumentTree();
            dt.addTreeSelectionListener(new TreeSelectionListener() {
                /*
                 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
                 */
                @Override
                @SuppressWarnings("unchecked")
                public void valueChanged (final TreeSelectionEvent e)
                {
                    final TreePath    selPath=(null == e) /* should not happen */ ? null : e.getNewLeadSelectionPath();
                    final Object    selNode=(null == selPath) ? null : selPath.getLastPathComponent();
                    if (selNode instanceof DocStructNode)
                        updateStatusBar((DocStructNode<? extends Node>) selNode);
                }
            });
            setDropTarget(new DropTarget(_docTree, this));

            ctPane.add(_docTree, BorderLayout.CENTER);
        }

        if (null == _statusBar)
        {
            _statusBar = new JLabel("");
            ctPane.add(_statusBar, BorderLayout.SOUTH);
        }
    }

    private static final File getFileArgument (final String argType, final int aIndex, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        if (aIndex >= numArgs)
            throw new IllegalArgumentException("Missing " + argType + " argument");

        return new File(args[aIndex+1]);
    }

    private void processMainArguments (final String ... args) throws Exception
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        File        inputFile=null, saveFile=null;
        for (int    aIndex=0; aIndex < numArgs; aIndex++)
        {
            final String    av=args[aIndex];
            if ((null == av) || (av.length() <= 0))
                continue;

            if ("-f".equalsIgnoreCase(av) || "-u".equalsIgnoreCase(av))
            {
                if (inputFile != null)
                    throw new IllegalArgumentException("Re-specified input file argument");

                inputFile = getFileArgument("input file/URL", aIndex, args);
                aIndex++;
            }
            else if ("-o".equalsIgnoreCase(av))
            {
                if (saveFile != null)
                    throw new IllegalArgumentException("Re-specified output file argument");

                saveFile = getFileArgument("output file", aIndex, args);
                aIndex++;
            }
            else if (av.startsWith("-D"))
            {
                DocBuilderOptionsDialog.setOptionValue(av.substring(2), getDocumentBuilderFactory());
            }
        }

        if (inputFile != null)
            loadFile(inputFile, LOAD_CMD, null);
        if (saveFile != null)
            saveFile(saveFile, null);
    }
    /**
     * @param args original arguments as received by <I>main</I> entry point
     * @throws Exception if unable to start main frame and application
     */
    MainFrame (final String ... args) throws Exception
    {
        super(args);

        try
        {
            processMainArguments(args);
        }
        catch(Exception e)
        {
            getLogger().error(e.getClass().getName() + " while process arguments: " + e.getMessage());
            BaseOptionPane.showMessageDialog(this, e);
        }
    }
}
