package net.community.apps.tools.srcextract;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;
import net.community.chest.lang.StringUtil;
import net.community.chest.util.logging.LoggerWrapper;
import net.community.chest.util.logging.factory.WrapperFactoryManager;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Extracts all non-".class" files from a JAR and creates another one
 * with the extracted files</P>
 *
 * @author Lyor G.
 * @since Nov 25, 2007 11:13:06 AM
 */
final class MainFrame extends JFrame implements FileSpecChangeListener {
    /**
     *
     */
    private static final long serialVersionUID = -1350750460371875633L;

    private static final LoggerWrapper    _logger=WrapperFactoryManager.getLogger(MainFrame.class);

    public static final Dimension DEFAULT_INITIAL_SIZE=new Dimension(480, 200);
    protected Dimension getInitialSize ()
    {
        return DEFAULT_INITIAL_SIZE;
    }

    public static final String findInPackagesList (final String jeName, final Collection<String> inPkgsNames)
    {
        if ((null == jeName) || (jeName.length() <= 0)
         || (null == inPkgsNames) || (inPkgsNames.size() <= 0))
            return null;

        for (final String p : inPkgsNames)
        {
            if (jeName.startsWith(p))
                return p;
        }

        return null;
    }

    public static final Collection<String> adjustPackagesNames (final String ... inPkgsNames)
    {
        final int    numNames=(null == inPkgsNames) ? 0 : inPkgsNames.length;
        if (numNames <= 0)
            return null;

        final Collection<String>    inPkgsPaths=new ArrayList<String>(numNames);
        for (final String p : inPkgsNames)
        {
            final String    pp=(null == p) ? null : p.replace('.', '/').trim();
            if ((null == pp) || (pp.length() <= 0))
                continue;

            inPkgsPaths.add(pp);
        }

        return inPkgsPaths;
    }

    private Collection<? extends JarEntry> transformFiles (
                    final JarInputStream in, final String inPkgsList, final JarOutputStream out) throws IOException
    {
        final String[]                inPkgsNames=inPkgsList.split(",");
        final Collection<String>    inPkgsPaths=adjustPackagesNames(inPkgsNames);
        Collection<JarEntry>    res=null;

        for (JarEntry    je=in.getNextJarEntry(); je != null; je = in.getNextJarEntry())
        {
            final String    jeName=je.getName();
            if ((null == jeName) || (jeName.length() <= 0))
                continue;

            if (_logger.isDebugEnabled())
                _logger.debug("transformEntry(" + jeName + ")");

            final boolean    isMetaInfo=jeName.startsWith("META-INF");
            // NOTE: we copy the META-INF entries as-is
            if ((!isMetaInfo) && (null == findInPackagesList(jeName, inPkgsPaths)))
                  continue;

            boolean    isFile=!je.isDirectory();
            if (isFile && "META-INF".equalsIgnoreCase(jeName))
                isFile = false;

            if (isFile && (!isMetaInfo))
            {
                final int    sfxPos=jeName.lastIndexOf('.');
                if ((sfxPos >= 0) && (sfxPos < jeName.length()))
                {
                    final String    sfx=jeName.substring(sfxPos);
                    if (".class".equalsIgnoreCase(sfx)
                     || ".jar".equalsIgnoreCase(sfx))
                        continue;    // don't want these files
                }
                else
                    isFile = false;
            }

            final long        jeSize=je.getSize();
            final JarEntry    outEntry=new JarEntry(jeName);
            outEntry.setComment(je.getComment());
            outEntry.setTime(je.getTime());
            outEntry.setSize(jeSize);

            out.putNextEntry(outEntry);

            if (isFile)
            {
                if (jeSize > 0L)
                {
                    final long    cpySize=IOCopier.copyStreams(in, out, IOCopier.DEFAULT_COPY_SIZE, jeSize);
                    if (cpySize != jeSize)
                        throw new StreamCorruptedException("Mismatched read(" + jeSize + ")/write(" + cpySize + ") copy size(s)");

                    if (_logger.isDebugEnabled())
                        _logger.debug("transformEntry(" + jeName + ") copied " + cpySize + " bytes");
                }
                else if (_logger.isDebugEnabled())
                    _logger.debug("transformEntry(" + jeName + ") skip empty file");
            }
            else if (jeSize > 0L)
                throw new StreamCorruptedException("Non zero (" + jeSize + ") directory size for entry=" + jeName);
            else if (_logger.isDebugEnabled())
                _logger.debug("transformEntry(" + jeName + ") put directory entry");

               out.closeEntry();

               if (null == res)
                   res = new LinkedList<JarEntry>();
               res.add(outEntry);
        }

        return res;
    }

    private final FileSpecPanel    _srcFileSpec, _dstFileSpec;
    protected void transformFiles ()
    {
        JarInputStream    in=null;
        JarOutputStream    out=null;

        setEnabled(false);

        final long    txfStart=System.currentTimeMillis();
        try
        {
            final String    srcName=_srcFileSpec.getFileName(), dstName=_dstFileSpec.getFileName();
            if (srcName.equalsIgnoreCase(dstName))
                throw new IllegalStateException("Must use different input/output file(s)");

            _logger.info("transformFiles(" + srcName + ")=>(" + dstName + ") start");
            try
            {
                in = new JarInputStream(new BufferedInputStream(new FileInputStream(srcName), 4096), true);
            }
            catch(Exception e)
            {
                JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getName() + " while open input", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try
            {
                final Manifest    inManifest=in.getManifest();
                if (null == inManifest)
                    out = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(dstName), 4096));
                else
                    out = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(dstName), 4096), inManifest);
            }
            catch(Exception e)
            {
                JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getName() + " while open output", JOptionPane.ERROR_MESSAGE);
                return;
            }

            final Collection<? extends JarEntry>    entries=
                    transformFiles(in, _srcFileSpec.getPackageName(), out);
            final long                                txfEnd=System.currentTimeMillis(), txfDuration=txfEnd - txfStart;
            final int                                numEntries=(null == entries) ? 0 : entries.size();
            if (numEntries > 0)
                JOptionPane.showMessageDialog(this, "Created " + numEntries + " entries in " + txfDuration + " msec.", "Extraction successful", JOptionPane.INFORMATION_MESSAGE);
            else
                JOptionPane.showMessageDialog(this, "No entries created", "Extraction done", JOptionPane.WARNING_MESSAGE);

            _logger.info("transformFiles(" + srcName + ")=>(" + dstName + ") done (" + numEntries + " in " + txfDuration + " msec.)");
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(this, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
        }
        finally
        {
            setEnabled(true);

            try
            {
                FileUtil.closeAll(in, out);
            }
               catch(IOException ce)
               {
                   JOptionPane.showMessageDialog(this, ce.getMessage(), ce.getClass().getName() + " while closing input/output", JOptionPane.ERROR_MESSAGE);
               }
        }
    }

    private JButton    _execButton    /* =null */;
    private final JPanel getExecutionPanel ()
    {
        _execButton = new JButton("Execute");
        _execButton.addActionListener(new ActionListener() {
                /*
                 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                 */
                @Override
                public void actionPerformed (ActionEvent e)
                {
                    transformFiles();
                }
            });

        final JPanel    pnl=new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        pnl.add(_execButton);
        return pnl;
    }

    private boolean isOkToExecute ()
    {
        if (null == _execButton)
            return false;

        final String    srcFile=(null == _srcFileSpec) ? null : _srcFileSpec.getFileName();
        {
            if ((null == srcFile) || (srcFile.length() <= 0))
                return false;

            final File    sf=new File(srcFile);
            if ((!sf.exists()) || (!sf.isFile()))
                return false;
        }

        {
            final String    srcPkg=(null == _srcFileSpec) ? null : _srcFileSpec.getPackageName();
            if ((null == srcPkg) || (srcPkg.length() <= 0))
                return false;
        }

        final String    dstFile=(null == _dstFileSpec) ? null : _dstFileSpec.getFileName();
        {
            if ((null == dstFile) || (dstFile.length() <= 0))
                return false;

            final File    df=new File(dstFile);
            if (df.exists() && (!df.isFile()))
                return false;
        }
        // do not allow same input/output file
        if (0 == StringUtil.compareDataStrings(srcFile, dstFile, false))
            return false;

        {
            final String    dstPkg=(null == _dstFileSpec) ? null : _dstFileSpec.getPackageName();
            if ((null == dstPkg) || (dstPkg.length() <= 0))
                return false;
        }

        return true;
    }

    private boolean updateExecutionButtonState ()
    {
        if (_execButton != null)
        {
            final boolean    enabled=isOkToExecute();
            _execButton.setEnabled(enabled);
            return enabled;
        }

        return false;
    }

    /*
     * @see net.community.apps.tools.srcextract.FileSpecChangeListener#handleSelectionChanged(net.community.apps.tools.srcextract.FileSpecPanel)
     */
    @Override
    public void handleSelectionChanged (FileSpecPanel fsp)
    {
        updateExecutionButtonState();
    }

    public static final String    DEFINE_SWITCH="-D";
    private void processArguments (final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; aIndex < numArgs; aIndex++)
        {
            final String    a=args[aIndex];
            if (!a.startsWith(DEFINE_SWITCH))    // ignored
            {
                _logger.warn("processArguments(" + a + ") not an argument format - must start with '" + DEFINE_SWITCH + "'");
                continue;
            }

            final int    valPos=a.indexOf('=');
            if ((valPos <= DEFINE_SWITCH.length()) || (valPos >= (a.length() - 1)))
            {
                _logger.warn("processArguments(" + a + ") missing argument definition");
                continue;
            }

            final String    aName=a.substring(DEFINE_SWITCH.length() /* skip -D */, valPos),
                            aValue=StringUtil.stripDelims(a.substring(valPos + 1));
            if ("src.file.name".equalsIgnoreCase(aName))
                _srcFileSpec.setFileName(aValue);
            else if ("src.pkg.name".equalsIgnoreCase(aName))
                _srcFileSpec.setPackageName(aValue);
            else if ("dst.file.name".equalsIgnoreCase(aName))
                _dstFileSpec.setFileName(aValue);
            else if ("dst.pkg.name".equalsIgnoreCase(aName))
                _dstFileSpec.setPackageName(aValue);
            else
                _logger.warn("processArguments(" + a + ") unknown argument");
        }
    }
    /**
     * Default title of the application
     */
    public static final String    DEFAULT_TITLE="Source files extractor";

    MainFrame (final String ... args) throws Exception
    {
        super(DEFAULT_TITLE);

        final Container    ctPane=getContentPane();
        ctPane.setLayout(new GridLayout(3, 1, 5, 5));
        // close the application if frame closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // make the frame appear in mid-screen by default
        setLocationRelativeTo(null);

        // give some initial size information
        final Dimension    dim=getInitialSize();
        setPreferredSize(dim);
        setSize(dim);

        _srcFileSpec = new FileSpecPanel("Source JAR", "Source package");
        _srcFileSpec.addChangeListener(this);
        ctPane.add(_srcFileSpec);

        _dstFileSpec = new FileSpecPanel("Dest. JAR", "Dest. package");
        _dstFileSpec.addChangeListener(this);
        ctPane.add(_dstFileSpec);

        ctPane.add(getExecutionPanel());

        processArguments(args);
        updateExecutionButtonState();
    }
}
