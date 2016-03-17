/*
 *
 */
package net.community.chest.javaagent.dumper.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.SwingWorker;
import javax.swing.tree.TreeNode;

import net.community.chest.awt.attributes.Selectible;
import net.community.chest.io.EOLStyle;
import net.community.chest.io.IOCopier;
import net.community.chest.javaagent.dumper.ui.data.SelectibleClassInfo;
import net.community.chest.javaagent.dumper.ui.data.SelectibleMethodInfo;
import net.community.chest.javaagent.dumper.ui.data.SelectiblePackageInfo;
import net.community.chest.javaagent.dumper.ui.tree.AbstractInfoNode;
import net.community.chest.javaagent.dumper.ui.tree.ClassNode;
import net.community.chest.javaagent.dumper.ui.tree.MethodNode;
import net.community.chest.javaagent.dumper.ui.tree.PackageNode;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 21, 2011 10:51:19 AM
 */
class AspectGeneratorThread extends SwingWorker<Object,AbstractInfoNode<?>> {
    private final TreeNode    _root;
    private final File _rootFolder;
    private final AspectGeneratorHandler _handler;
    AspectGeneratorThread (TreeNode root, File rootFolder, AspectGeneratorHandler handler)
    {
        _root = root;
        _rootFolder = rootFolder;
        _handler = handler;
    }

    public static final String    MAIN_SUBFOLDER="src" + File.separator + "main";
    public static final String    RESOURCES_SUBFOLDER=MAIN_SUBFOLDER + File.separator + "resources";

    /*
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected Object doInBackground () throws Exception
    {
        processNode(_root);
        copyResourceFile("pom.xml", "pom.xml");
        copyResourceFile("aop.xml", RESOURCES_SUBFOLDER + File.separator + "META-INF" + File.separator + "aop.xml");
        return _root;
    }
    /*
     * @see javax.swing.SwingWorker#process(java.util.List)
     */
    @Override
    protected void process (List<AbstractInfoNode<?>> chunks)
    {
        if ((_handler == null) || (chunks == null) || chunks.isEmpty())
            return;

        for (final AbstractInfoNode<?> node : chunks)
            _handler.handleProcessedNode(node);
    }
    /*
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done ()
    {
        if (_handler != null)
            _handler.doneGenerating(this);
    }

    private boolean processNode (final TreeNode node) throws IOException
    {
        if (isCancelled())
            return false;

        final boolean    keepProcessing;
        if (node instanceof PackageNode)
            keepProcessing = processPackageNode((PackageNode) node);
        else if (node instanceof ClassNode)
            keepProcessing = processClassNode((ClassNode) node);
        else if (node instanceof MethodNode)
            keepProcessing = processMethodNode((MethodNode) node);
        else
            keepProcessing = true;
        if (!keepProcessing)
            return false;

        // if the parent node is selected then all its children are selected by default
        if ((node instanceof Selectible) && ((Selectible) node).isSelected())
            return !isCancelled();
        else
            return processChildren(node);
    }

    private boolean processPackageNode (final PackageNode node) throws IOException
    {
        if (!node.isSelected())
            return processChildren(node);

        final SelectiblePackageInfo    info=node.getAssignedValue();
        final Writer                w=generateAspectFileWriter(info, "AllClasses");
        try
        {
            EOLStyle.LOCAL.appendEOL(w.append("\t\texecution(public * ").append(info.getName()).append(".*.*(..));"));
        }
        finally
        {
            closeAspectFileWriter(w);
        }

        publish(node);
        return !isCancelled();
    }

    private boolean processClassNode (final ClassNode node) throws IOException
    {
        final boolean                    allNodes=node.isSelected();
        final Collection<MethodNode>    selMethods=allNodes ? null : node.getSelectedNodes();
        if ((!allNodes) && ((selMethods == null) || (selMethods.size() <= 0)))
            return !isCancelled();

        final PackageNode            parent=(PackageNode) node.getParent();
        final SelectiblePackageInfo    pkgInfo=parent.getAssignedValue();
        final SelectibleClassInfo    info=node.getAssignedValue();
        final String                className=info.getSimpleName(), fullName=info.getName();
        final Writer                w=generateAspectFileWriter(pkgInfo, className);
        try
        {
            if (allNodes)
            {
                EOLStyle.LOCAL.appendEOL(w.append("\t\texecution(public * ").append(fullName).append(".*(..));"));
            }
            else
            {
                final Set<String>    names=new TreeSet<String>();
                for (final MethodNode mNode : selMethods)
                {
                    final SelectibleMethodInfo    mInfo=(mNode == null) ? null : mNode.getAssignedValue();
                    if ((mInfo == null) || (!mInfo.isSelected()) || (!mInfo.isPublic()))
                        continue;

                    final String    mName=mInfo.getName();
                    if (names.contains(mName))
                        continue;

                    if (names.size() > 0)
                    {
                        EOLStyle.LOCAL.appendEOL(w);
                        EOLStyle.LOCAL.appendEOL(w.append("\t\t|| execution(public * ").append(fullName).append('.').append(mName).append("(..))"));
                    }
                }

                EOLStyle.LOCAL.appendEOL(w.append("\t\t;"));
            }
        }
        finally
        {
            closeAspectFileWriter(w);
        }

        publish(node);
        return !isCancelled();
    }

    private boolean processMethodNode (MethodNode node)
    {
        return (node != null) && (!isCancelled());
    }

    private boolean processChildren (final TreeNode node) throws IOException
    {
        final int    childCount=(node == null) ? 0 : node.getChildCount();
        for (int    cIndex=0; cIndex < childCount; cIndex++)
        {
            final TreeNode    childNode=node.getChildAt(cIndex);
            if (!processNode(childNode))
                return false;
        }

        return true;
    }

    Writer generateAspectFileWriter (final PackageNode node, final String className) throws IOException
    {
        return generateAspectFileWriter((node == null) ? null : node.getAssignedValue(), className);
    }

    public static final String    COLLECTION_ASPECT_PREFIX="CollectionAspect";
    Writer generateAspectFileWriter (final SelectiblePackageInfo info, final String className) throws IOException
    {
        final String    pkgName=info.getName();
        final File        ajFile=generateAspectFile(info, className);
        final Writer    w=new BufferedWriter(new FileWriter(ajFile), IOCopier.DEFAULT_COPY_SIZE);

        EOLStyle.LOCAL.appendEOL(w.append("// Auto-generated by JavaAgent dumper"));
        EOLStyle.LOCAL.appendEOL(w.append("package ").append(pkgName).append(';'));
        EOLStyle.LOCAL.appendEOL(w);
        EOLStyle.LOCAL.appendEOL(w.append("import org.aspectj.lang.JoinPoint;"));
        EOLStyle.LOCAL.appendEOL(w);
        EOLStyle.LOCAL.appendEOL(w.append("import com.springsource.insight.collection.method.MethodOperationCollectionAspect;"));
        EOLStyle.LOCAL.appendEOL(w.append("import com.springsource.insight.intercept.operation.Operation;"));
        EOLStyle.LOCAL.appendEOL(w.append("import com.springsource.insight.intercept.operation.OperationType;"));
        EOLStyle.LOCAL.appendEOL(w);

        EOLStyle.LOCAL.appendEOL(w.append("public aspect ").append(className).append(COLLECTION_ASPECT_PREFIX).append(" extends MethodOperationCollectionAspect {"));
        EOLStyle.LOCAL.appendEOL(w);

        EOLStyle.LOCAL.appendEOL(w.append('\t').append("@Override"));
        EOLStyle.LOCAL.appendEOL(w.append('\t').append("protected Operation createOperation(JoinPoint jp) {"));
        EOLStyle.LOCAL.appendEOL(w.append("\t\t").append("return super.createOperation(jp).type(OperationType.METHOD);"));
        EOLStyle.LOCAL.appendEOL(w.append("\t}"));
        EOLStyle.LOCAL.appendEOL(w);

        EOLStyle.LOCAL.appendEOL(w.append('\t').append("public pointcut collectionPoint () :"));
        return w;
    }

    void closeAspectFileWriter (Writer w) throws IOException
    {
        EOLStyle.LOCAL.appendEOL(w.append('}'));
        w.close();
    }

    File generateAspectFile (final PackageNode node, final String className)
    {
        return generateAspectFile((node == null) ? null : node.getAssignedValue(), className);
    }

    public static final String    ASPECTJ_SRC_FILE_SUFFIX=".aj";
    File generateAspectFile (final SelectiblePackageInfo info, final String className)
    {
        final File    folder=generateRootFolder(info);
        if (folder == null)
            return null;
        else
            return new File(folder, className + COLLECTION_ASPECT_PREFIX +  ASPECTJ_SRC_FILE_SUFFIX);
    }

    File generateRootFolder (final PackageNode node)
    {
        return generateRootFolder((node == null) ? null : node.getAssignedValue());
    }

    public static final String    SOURCES_SUBFOLDER=MAIN_SUBFOLDER + File.separator + "java";
    File generateRootFolder (final SelectiblePackageInfo info)
    {
        final String    name=(info == null) ? null : info.getName();
        if ((name == null) || (name.length() <= 0))
            return null;

        return ensureTargetFolderExists(new File(_rootFolder, SOURCES_SUBFOLDER + File.separator + name.replace('.', File.separatorChar)));
    }

    File copyResourceFile (final String src, final String dst) throws IOException
    {
        final InputStream    in=getClass().getResourceAsStream(src);
        if (in == null)
            throw new FileNotFoundException("Not found source resource: " + src);

        try
        {
            final File    dstFile=new File(_rootFolder, dst);
            ensureTargetFolderExists(dstFile.getParentFile());
            final OutputStream    out=new FileOutputStream(dstFile);
            try
            {
                final long    cpySize=IOCopier.copyStreams(in, out);
                if (cpySize < 0L)
                    throw new StreamCorruptedException("Failed (" + cpySize + ") to copy to " + dstFile.getAbsolutePath());
                return dstFile;
            }
            finally
            {
                out.close();
            }
        }
        finally
        {
            in.close();
        }
    }

    static File ensureTargetFolderExists (File folder)
    {
        if (folder.exists())
        {
            if (!folder.isDirectory())
                throw new IllegalStateException("Target exists and is not a folder: " + folder.getAbsolutePath());
        }
        else if (!folder.mkdirs())
            throw new IllegalStateException("Failed to created folder: " + folder.getAbsolutePath());
        return folder;
    }
}
