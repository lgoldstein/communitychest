/*
 *
 */
package net.community.apps.eclipse.cp2pom;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import net.community.apps.common.BaseMainFrame;
import net.community.apps.eclipse.cp2pom.resources.ResourcesAnchor;
import net.community.chest.apache.maven.helpers.BaseTargetDetails;
import net.community.chest.apache.maven.helpers.BuildProject;
import net.community.chest.apache.maven.helpers.BuildTargetDetails;
import net.community.chest.apache.maven.helpers.BuildTargetFile;
import net.community.chest.awt.attributes.Textable;
import net.community.chest.dom.DOMUtils;
import net.community.chest.eclipse.wst.WstUtils;
import net.community.chest.io.EOLStyle;
import net.community.chest.io.dom.PrettyPrintTransformer;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.swing.component.button.BaseCheckBox;
import net.community.chest.swing.component.scroll.ScrolledComponent;
import net.community.chest.swing.options.BaseOptionPane;
import net.community.chest.ui.helpers.panel.input.LRFieldWithButtonPanel;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 27, 2009 9:30:50 AM
 */
final class MainFrame extends BaseMainFrame<ResourcesAnchor> {
    /**
     *
     */
    private static final long serialVersionUID = 6009715905543540702L;
    private static final LoggerWrapper    _logger=WrapperFactoryManager.getLogger(MainFrame.class);
    /*
     * @see net.community.apps.common.BaseMainFrame#getLogger()
     */
    @Override
    protected LoggerWrapper getLogger ()
    {
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

    private String    _repoPrefix="M2_REPO";
    public String getRepositoryPrefix ()
    {
        return _repoPrefix;
    }

    public void setRepositoryPrefix (final String s)
    {
        _repoPrefix = s;
    }

    private boolean    _verbOutput=true;
    public boolean isVerboseOutput ()
    {
        return _verbOutput;
    }

    public void setVerboseOutput (final boolean f)
    {
        _verbOutput = f;
    }

    private static final File resolveFileInstance (final Textable fld, final File curFile)
    {
        final String    fldPath=(null == fld) ? null : fld.getText(),
                        curPath=(null == curFile) ? null : curFile.getAbsolutePath();
        if (0 == StringUtil.compareDataStrings(fldPath, curPath, true))
            return curFile;

        if ((null == fldPath) || (fldPath.length() <= 0))
            return null;
        return new File(fldPath);
    }

    private LRFieldWithButtonPanel    _outputPanel;
    private File                     _outputFile;
    private File resolveOutputFile ()
    {
        return resolveFileInstance(_outputPanel, _outputFile);
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#saveFile(java.io.File, org.w3c.dom.Element)
     */
    @Override
    public void saveFile (File f, Element dlgElement)
    {
        if (_outputFile != f)
            _outputFile = f;

        if (_outputPanel != null)
            _outputPanel.setText((null == f) ? "" : f.getAbsolutePath());

        setInitialFileChooserFolder(f, Boolean.TRUE);
    }

    public static final String    DEFAULT_NAME_VALUE="cp2pom";
    private String _groupId;
    public String getGroupId ()
    {
        if (null == _groupId)
            return DEFAULT_NAME_VALUE;

        return _groupId;
    }

    public void setGroupId (String groupId)
    {
        _groupId = groupId;
    }

    private String    _artifactId;
    public String getArtifactId ()
    {
        if (null == _artifactId)
            return DEFAULT_NAME_VALUE;
        return _artifactId;
    }

    public void setArtifactId (String artifactId)
    {
        _artifactId = artifactId;
    }

    private String    _projectName;
    public String getProjectName ()
    {
        if (null == _projectName)
            return DEFAULT_NAME_VALUE;
        return _projectName;
    }

    public void setProjectName (String projectName)
    {
        _projectName = projectName;
    }

    private String    _projectVersion;
    public String getProjectVersion ()
    {
        if (null == _projectVersion)
            return DEFAULT_NAME_VALUE;
        return _projectVersion;
    }

    public void setProjectVersion (String projectVersion)
    {
        _projectVersion = projectVersion;
    }

    protected <D extends Document> D createMavenDependencies (
            final D                                     doc,
            final Collection<? extends RepositoryEntry>    rl,
            final File                                     outFile)
        throws Exception
    {
        final Element    proj=doc.createElement(BuildProject.PROJECT_ELEMENT_NAME);
        doc.appendChild(proj);

        {
            final String[]    hdrs={
                    BuildTargetDetails.MODELVERSION_ELEM_NAME, BuildTargetDetails.DEFAULT_MODEL_VERSION,
                    BaseTargetDetails.GROUPID_ELEM_NAME, getGroupId(),
                    BaseTargetDetails.ARTIFACTID_ELEM_NAME, getArtifactId(),
                    BuildTargetDetails.PACKAGING_ELEM_NAME, BuildTargetFile.POM_FILE_TYPE,
                    BuildTargetDetails.NAME_ELEM_NAME, getProjectName(),
                    BaseTargetDetails.VERSION_ELEM_NAME, getProjectVersion()
                };
            for (int hIndex=0; hIndex < hdrs.length; hIndex += 2)
            {
                final Element    elem=
                    DOMUtils.createElementValue(doc, hdrs[hIndex], hdrs[hIndex+1]);
                if (null == elem)
                    continue;
                proj.appendChild(elem);
            }
        }

        {
            final Element    deps=doc.createElement(BuildProject.DEPENDENCIES_ELEM_NAME);
            for (final RepositoryEntry re : rl)
            {
                try
                {
                    final Element    elem=
                        (null == re) ? null : BaseTargetDetails.toXml(re, doc, false);
                    if (null == elem)
                        continue;
                    deps.appendChild(elem);
                }
                catch(Exception e)
                {
                    _logger.error("createMavenDependencies(" + outFile + ")[" + re + " " + e.getClass().getName() + " while create document: " + e.getMessage(), e);
                    throw e;
                }
            }

            proj.appendChild(deps);
        }

        return doc;
    }

    protected <D extends Document> D createAntDependencies (
            final D                                     doc,
            final Collection<? extends RepositoryEntry>    rl,
            final File                                     outFile)
        throws Exception
    {
        final Element    deps=doc.createElement(BuildProject.DEPENDENCIES_ELEM_NAME);
        for (final RepositoryEntry re : rl)
        {
            try
            {
                final Element    elem=
                    (null == re) ? null : BaseTargetDetails.toXml(re, doc, true);
                if (null == elem)
                    continue;
                deps.appendChild(elem);
            }
            catch(Exception e)
            {
                _logger.error("createMavenDependencies(" + outFile + ")[" + re + " " + e.getClass().getName() + " while create document: " + e.getMessage(), e);
                throw e;
            }
        }

        doc.appendChild(deps);
        return doc;
    }

    public RuntimeException createDependencies (
            final Collection<? extends RepositoryEntry>    rl,
            final boolean                                asAntFormat,
            final File                                     outFile)
    {
        if (null == outFile)
            return new IllegalStateException("No output file");
        if ((null == rl) || (rl.size() <= 0))
            return new IllegalArgumentException("No entries to create");

        if (outFile.exists())
        {
            // TODO check if running with no UI
            if (JOptionPane.showConfirmDialog(this, "Output file already exists - override ?", "Confirm output file overwrite", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                return null;
        }

        final Document    doc;
        try
        {
            doc = DOMUtils.createDefaultDocument();
            if (asAntFormat)
                createAntDependencies(doc, rl, outFile);
            else
                createMavenDependencies(doc, rl, outFile);
        }
        catch(Exception e)
        {
            _logger.error("createDependencies(" + outFile + ") " + e.getClass().getName() + " while create document: " + e.getMessage(), e);
            return ExceptionUtil.toRuntimeException(e);
        }

        try
        {
            PrettyPrintTransformer.DEFAULT.transform(doc, outFile);
        }
        catch(Exception e)
        {
            _logger.error("createDependencies(" + outFile + ") " + e.getClass().getName() + " while write document: " + e.getMessage(), e);
            return ExceptionUtil.toRuntimeException(e);
        }

        // TODO check if running with no UI
        {
            final int    nRes=JOptionPane.showConfirmDialog(this, "Open generated file ?", "Conversion complete", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (JOptionPane.YES_OPTION == nRes)
            {
                final Desktop    d=Desktop.getDesktop();
                try
                {
                    d.edit(outFile);
                }
                catch (IOException e)
                {
                    _logger.error("createDependencies(" + outFile + ") " + e.getClass().getName() + " while edit output file: " + e.getMessage(), e);
                    return ExceptionUtil.toRuntimeException(e);
                }
            }
        }

        return null;
    }

    private LRFieldWithButtonPanel    _inputPanel;
    private File                    _inputFile;
    private File resolveInputFile ()
    {
        return resolveFileInstance(_inputPanel, _inputFile);
    }

    private static final boolean isCPFile (final File f)
    {
        final String    n=(null == f) ? null : f.getName();
        if (WstUtils.COMPONENTS_FILENAME.equalsIgnoreCase(n))
            return false;
        else
            return true;
    }

    private RepositoryEntriesTable    _entriesTable;
    private Collection<? extends RepositoryEntry> loadFile (
            final File f, final boolean populateTable)
    {
        try
        {
            final Collection<? extends RepositoryEntry>    el=RepositoryEntry.loadEntries(getRepositoryPrefix(), f, isCPFile(f), isVerboseOutput());
            if (populateTable && (_entriesTable != null))
                _entriesTable.setEntries(el);
            if (_inputFile != f)
                _inputFile = f;
            if (_inputPanel != null)
                _inputPanel.setText((null == f) ? "" : f.getAbsolutePath());

            setInitialFileChooserFolder(f, Boolean.FALSE);
            return el;
        }
        catch(Exception e)
        {
            _logger.error("loadFile(" + f + ") " + e.getClass().getName() + ": " + e.getMessage(), e);
            // TODO check if running with no UI
            BaseOptionPane.showMessageDialog(this, e);
            return null;
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

    private JCheckBox    _asAntFormat;
    public boolean isUseAntFormat ()
    {
        return (_asAntFormat != null) && _asAntFormat.isSelected();
    }

    public void setUseAntFormat (boolean v)
    {
        if (_asAntFormat != null)
            _asAntFormat.setSelected(v);
    }

    protected void createDependencies ()
    {
        final File                                    f=resolveInputFile();
        final Collection<? extends RepositoryEntry>    rl=(f == _inputFile)
            ? ((null == _entriesTable) ? null : _entriesTable.getEntries())
            : loadFile(f, true)
            ;

        final RuntimeException    re=createDependencies(rl, isUseAntFormat(), resolveOutputFile());
        if (re != null)
            BaseOptionPane.showMessageDialog(this, re);
    }

    protected void refreshInputFile ()
    {
        if (_inputFile != null)
            loadFile(_inputFile, true);
    }

    private static final String[]    USAGE={
          "cp2pom [options]",
          "",
          "\tWhere options are:",
          "",
          "\t\t-i,--input <file> - pre-load input file",
          "\t\t-o,--output <file> - pre-load output file",
          "\t\t-g,--group <group> - Maven group name to use (default=" + DEFAULT_NAME_VALUE + ")",
          "\t\t-a,--artifact <artifact> - Maven artifact name to use (default=" + DEFAULT_NAME_VALUE + ")",
          "\t\t-n,--name <name> - Maven project name to use (default=" + DEFAULT_NAME_VALUE + ")",
          "\t\t-v,--version <version> - Maven project version to use (default=" + DEFAULT_NAME_VALUE + ")",
          "\t\t-p,--prefix <prefix> - repository variable prefix",
          "\t\t-f,--format <format> - output format (maven or ant) (default=maven)",
          "\t\t-s,--silent - no UI while working",
          "\t\t-h,--help - show this message"
        };
    protected static final void showUsage (final Component popupParent /* null == use stdout */)
    {
        final StringBuilder    sb=new StringBuilder(USAGE.length * 64);
        final char[]        eolChars=EOLStyle.LOCAL.getStyleChars();
        for (final String s : USAGE)
        {
            if (sb.length() > 0)
                sb.append(eolChars);
            sb.append(s);
        }

        if (popupParent != null)
            JOptionPane.showMessageDialog(popupParent, sb.toString(), "Usage", JOptionPane.INFORMATION_MESSAGE);
        else
            System.out.println(sb.toString());
    }

    private static final String    RUN_CMD="run", REFRESH_CMD="refresh";
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
        lm.put(EXIT_CMD, getExitActionListener());
        lm.put(ABOUT_CMD, getShowManifestActionListener());
        lm.put(RUN_CMD, new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    if (e != null)
                        createDependencies();
                }
            });
        lm.put(REFRESH_CMD, new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    if (e != null)
                        refreshInputFile();
                }
            });
        lm.put("usage", new ActionListener() {
            /*
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @Override
            public void actionPerformed (ActionEvent e)
            {
                if (e != null)
                    showUsage(getMainFrameInstance());
            }
        });

        setActionListenersMap(lm);
        return lm;
    }

    protected LRFieldWithButtonPanel createInputPanel (Element elem)
    {
        // delay auto-layout till after setting the text field
        final LRFieldWithButtonPanel    p=new LRFieldWithButtonPanel(elem, false);
        final ClasspathInputTextField    txtField=new ClasspathInputTextField();
        p.setTextField(txtField);
        p.layoutComponent();
        p.addActionListener(getLoadFileListener());
        return p;
    }

    protected LRFieldWithButtonPanel createOutputPanel (Element elem)
    {
        // delay auto-layout till after setting the text field
        final LRFieldWithButtonPanel    p=new LRFieldWithButtonPanel(elem, false);
        final PomInputTextField            txtField=new PomInputTextField();
        p.setTextField(txtField);
        p.layoutComponent();
        p.addActionListener(getSaveFileListener());
        return p;
    }

    protected RepositoryEntriesTable createEntriesTable (Element elem)
    {
        final RepositoryEntriesTable    t=new RepositoryEntriesTable();
        try
        {
            t.fromXml(elem);
        }
        catch (Exception e)
        {
            throw _logger.errorObject("createEntriesTable(" + DOMUtils.toString(elem) + ") " + e.getClass().getName() + ": " + e.getMessage(), e, ExceptionUtil.toRuntimeException(e));
        }

        t.addKeyListener(new EntriesTableKeyListener(t));
        return t;
    }

    protected JCheckBox createAntFormatSelector (final Element elem)
    {
        try
        {
            return new BaseCheckBox(elem);
        }
        catch(Exception e)
        {
            throw _logger.errorObject("createAntFormatSelector(" + DOMUtils.toString(elem) + ") " + e.getClass().getName() + ": " + e.getMessage(), e, ExceptionUtil.toRuntimeException(e));
        }
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#layoutSection(java.lang.String, org.w3c.dom.Element)
     */
    @Override
    public void layoutSection (String name, Element elem) throws RuntimeException
    {
        if ("input-panel".equalsIgnoreCase(name))
        {
            if (_inputPanel != null)
                throw new IllegalStateException("layoutSection(" + name + ")[" + DOMUtils.toString(elem) + "] already initialized");

            _inputPanel = createInputPanel(elem);
        }
        else if ("output-panel".equalsIgnoreCase(name))
        {
            if (_outputPanel != null)
                throw new IllegalStateException("layoutSection(" + name + ")[" + DOMUtils.toString(elem) + "] already initialized");

            _outputPanel = createOutputPanel(elem);
        }
        else if ("entries-table".equalsIgnoreCase(name))
        {
            if (_entriesTable != null)
                throw new IllegalStateException("layoutSection(" + name + ")[" + DOMUtils.toString(elem) + "] already initialized");

            _entriesTable = createEntriesTable(elem);
        }
        else if ("output-format".equalsIgnoreCase(name))
        {
            if (_asAntFormat != null)
                throw new IllegalStateException("layoutSection(" + name + ")[" + DOMUtils.toString(elem) + "] already initialized");

            _asAntFormat = createAntFormatSelector(elem);
        }
        else
            super.layoutSection(name, elem);
    }
    /*
     * @see net.community.apps.common.BaseMainFrame#layoutComponent()
     */
    @Override
    public void layoutComponent () throws RuntimeException
    {
        super.layoutComponent();

        final Container    ctPane=getContentPane();
        final JPanel    northPanel=new JPanel(new GridLayout(0, 1, 0, 5));
        try
        {
            final JToolBar                        b=getMainToolBar();
            final Map<String,AbstractButton>    hm=setToolBarHandlers(b);
            if ((hm != null) && (hm.size() > 0))
                northPanel.add(b);
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }

        {
            final Component[]    northComps={ _inputPanel, _outputPanel, _asAntFormat };
            for (final Component c : northComps)
            {
                if (null == c)    // should not happen
                    continue;

                northPanel.add(c);
            }
        }

        ctPane.add(northPanel, BorderLayout.NORTH);
        if (_entriesTable != null)
            ctPane.add(new ScrolledComponent<RepositoryEntriesTable>(RepositoryEntriesTable.class, _entriesTable));
    }

    private void processMainArguments (final String ... args)
    {
        if ((null == args) || (args.length <= 0))
            return;

        Boolean    antFormat=null;
        for (int    aIndex=0; aIndex < args.length; aIndex++)
        {
            final String     a=args[aIndex];
            final int        aLen=(null == a) ? 0 : a.length();
            if (aLen <= 0)
                continue;

            if ("-i".equals(a) || "--input".equals(a))
            {
                aIndex++;

                if (aIndex >= args.length)
                    throw new IllegalStateException("processMainArguments(" + a + ") missing argument(s)");

                final String    argVal=args[aIndex];
                if ((null == argVal) || (argVal.length() <= 0))
                    throw new IllegalStateException("processMainArguments(" + a + ") argument value cannot be null/empty");
                if (_inputFile != null)
                    throw new IllegalStateException("processMainArguments(" + a + ") already set");

                _inputFile = new File(argVal);
            }
            else if ("-o".equals(a) || "--output".equals(a))
            {
                aIndex++;

                if (aIndex >= args.length)
                    throw new IllegalStateException("processMainArguments(" + a + ") missing argument(s)");

                final String    argVal=args[aIndex];
                if ((null == argVal) || (argVal.length() <= 0))
                    throw new IllegalStateException("processMainArguments(" + a + ") argument value cannot be null/empty");
                if (_outputFile != null)
                    throw new IllegalStateException("processMainArguments(" + a + ") already set");

                _outputFile = new File(argVal);
            }
            else if ("-g".equals(a) || "--group".equals(a))
            {
                aIndex++;

                if (aIndex >= args.length)
                    throw new IllegalStateException("processMainArguments(" + a + ") missing argument(s)");

                final String    argVal=args[aIndex];
                if ((null == argVal) || (argVal.length() <= 0))
                    throw new IllegalStateException("processMainArguments(" + a + ") argument value cannot be null/empty");
                if (_groupId != null)
                    throw new IllegalStateException("processMainArguments(" + a + ") already set");

                _groupId = argVal;
            }
            else if ("-a".equals(a) || "--artifact".equals(a))
            {
                aIndex++;

                if (aIndex >= args.length)
                    throw new IllegalStateException("processMainArguments(" + a + ") missing argument(s)");

                final String    argVal=args[aIndex];
                if ((null == argVal) || (argVal.length() <= 0))
                    throw new IllegalStateException("processMainArguments(" + a + ") argument value cannot be null/empty");
                if (_artifactId != null)
                    throw new IllegalStateException("processMainArguments(" + a + ") already set");

                _artifactId = argVal;
            }
            else if ("-n".equals(a) || "--name".equals(a))
            {
                aIndex++;

                if (aIndex >= args.length)
                    throw new IllegalStateException("processMainArguments(" + a + ") missing argument(s)");

                final String    argVal=args[aIndex];
                if ((null == argVal) || (argVal.length() <= 0))
                    throw new IllegalStateException("processMainArguments(" + a + ") argument value cannot be null/empty");
                if (_projectName != null)
                    throw new IllegalStateException("processMainArguments(" + a + ") already set");

                _projectName = argVal;
            }
            else if ("-f".equals(a) || "--format".equals(a))
            {
                aIndex++;

                if (aIndex >= args.length)
                    throw new IllegalStateException("processMainArguments(" + a + ") missing argument(s)");

                final String    argVal=args[aIndex];
                if ((null == argVal) || (argVal.length() <= 0))
                    throw new IllegalStateException("processMainArguments(" + a + ") argument value cannot be null/empty");

                if (antFormat != null)
                    throw new IllegalStateException("processMainArguments(" + a + ") already set");

                 if ("ant".equals(argVal))
                     antFormat = Boolean.TRUE;
                 else if ("maven".equals(argVal))
                     antFormat = Boolean.FALSE;
            }
            else
            {
                showUsage(this);
                throw new IllegalStateException("processMainArguments(" + a + ") unknown argument");
            }
        }

        if (_inputFile != null)
            loadFile(_inputFile, true);
        if (_outputFile != null)
            saveFile(_outputFile, null);
        if (antFormat != null)
            setUseAntFormat(antFormat.booleanValue());
    }
    /**
     * @param args original arguments as received by <I>main</I> entry point
     * @throws Exception if unable to start main frame and application
     */
    MainFrame (final String ... args) throws Exception
    {
        super(args);

        processMainArguments(args);
    }
}
