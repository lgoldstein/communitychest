/**
 *
 */
package net.community.chest.apache.maven.helpers.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.community.chest.apache.maven.helpers.BuildDependencyDetails;
import net.community.chest.apache.maven.helpers.BuildProject;
import net.community.chest.apache.maven.helpers.BuildProjectFile;
import net.community.chest.apache.maven.helpers.BuildTargetFile;
import net.community.chest.apache.maven.helpers.DependenciesList;
import net.community.chest.apache.maven.helpers.ParentTargetDetails;
import net.community.chest.dom.DOMUtils;
import net.community.chest.io.IOCopier;
import net.community.chest.io.output.NullOutputStream;
import net.community.chest.test.TestBase;

import org.w3c.dom.Document;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 13, 2008 3:06:43 PM
 */
public class MavenTestBase extends TestBase {
    public MavenTestBase ()
    {
        super();
    }

    public static final void showDependencies (final PrintStream out, final Collection<? extends BuildDependencyDetails> deps)
    {
        if ((null == deps) || (deps.size() <= 0))
            return;

        for (final BuildDependencyDetails d : deps)
            out.println("\t" + d.getGroupId() + "\t" + d.getArtifactId() + "\t" + d.getVersion());
    }

    /* -------------------------------------------------------------------- */

    public static final void showDependencies (
            final PrintStream                         out,
            final BuildProjectFile                    proj,
            final BuildProject.DepenencyListType    t)
    {
        final DependenciesList    deps=(null == proj) ? null : proj.getDependenciesList(t);
        if ((null == deps) || (deps.size() <= 0))
            return;

        out.println("Dependencies[" + t + "] " + proj.getFilePath());
        showDependencies(out, deps);
    }

    /* -------------------------------------------------------------------- */

    public static final boolean showData (final PrintStream out, final String relPath, final char sepChar)
    {
        if ((null == relPath) || (relPath.length() <= 0))
            return true;

        try
        {
            final BuildTargetFile    tgt=BuildTargetFile.fromRelativeArtifactPath(relPath, sepChar);
            final String            groupId=tgt.getGroupId(),
                                    artifact=tgt.getArtifactId(),
                                    version=tgt.getVersion(),
                                    jarName=tgt.toArtifactFileName();
            out.println(groupId + "," + artifact + "," + version + "," + jarName);
            return true;
        }
        catch(Exception e)
        {
            System.err.println(e.getClass().getName() + " while handling file=" + relPath + ": " + e.getMessage());
            return false;
        }
    }

    //////////////////////////////////////////////////////////////////////////

    // args[i] is a relative artifact file path
    public static final int testRelativeArtifactFilePaths (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    relPath=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "relative artifact file path (or Quit)");
            if ((null == relPath) || (relPath.length() <= 0))
                continue;
            if (isQuit(relPath)) break;

            if (!showData(out, relPath, '/'))
                continue;
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    private static final void testPOMFileLoad (final PrintStream out, final BufferedReader in, final BuildProjectFile proj, final boolean followParent) throws Exception
    {
        showDependencies(out, proj, BuildProject.DepenencyListType.MANAGEMENT);
        showDependencies(out, proj, BuildProject.DepenencyListType.PROJECT);

        final ParentTargetDetails    parent=proj.getParentTarget();
        if ((null == parent) || (!followParent))
            return;

        final String    ans=getval(out, in, "follow parent [y]/n");
        if ((null == ans) || (ans.length() <= 0) || (Character.toLowerCase(ans.charAt(0)) == 'y'))
        {
            final BuildProjectFile    pp=proj.resolveParentProject();
            final String            filePath=pp.getFilePath();    // must exist...
            out.println("Found parent=" + filePath);
            testPOMFileLoad(out, in, pp, true);
        }
    }
    // args[i] is a POM file path
    public static final int testPOMFileLoad (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    relPath=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "POM file path (or Quit)");
            if ((null == relPath) || (relPath.length() <= 0))
                continue;
            if (isQuit(relPath)) break;

            final String    ans=getval(out, in, "load all POM(s) for " + relPath + " [y]/n/q");
            if (isQuit(ans)) break;

            final boolean    loadAll=(null == ans) || (ans.length() <= 0) || (Character.toLowerCase(ans.charAt(0)) == 'y');
            try
            {
                if (loadAll)
                {
                    final Map<String,? extends BuildProjectFile>    pm=BuildProjectFile.loadAllPOMs(relPath);
                    for (BuildProjectFile    curPom=pm.get(relPath); curPom != null; curPom = curPom.getParentProject())
                        testPOMFileLoad(out, in, curPom, false);
                }
                else
                    testPOMFileLoad(out, in, new BuildProjectFile(relPath), true);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while handling file=" + relPath + ": " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    private static final int testMavenDownload (final PrintStream out, final BufferedReader in, final List<? extends BuildTargetFile> fl)
    {
        if ((null == fl) || (fl.size() <= 0))
            return 0;

        String    baseURL="http://hermes:8081/artifactory/repo";
        final OutputStream    os=new NullOutputStream();
        for (BuildTargetFile    tgt=fl.get(0); ; )
        {
            if (null == (tgt=inputListChoice(out, in, "Targets", fl, tgt)))
                break;

            final String    ans=getval(out, in, "base URL (ENTER=" + baseURL + ")/(Q)uit");
            if (isQuit(ans))
                break;
            if ((ans != null) && (ans.length() > 0))
                baseURL = ans;

            final long    cpyStart=System.currentTimeMillis();
            try
            {
                final URL    dldURL=tgt.toURL(new URL(baseURL));
                try(InputStream    ins=dldURL.openStream()) {
                    final long    cpyLen=IOCopier.copyStreams(ins, os),
                                cpyEnd=System.currentTimeMillis(), cpyDuration=cpyEnd - cpyStart;
                    if (cpyLen < 0L)
                        throw new StreamCorruptedException("Error (" + cpyLen + ") while copy contents");
                    out.println("\tCopied " + cpyLen + " bytes in " + cpyDuration + " msec.");
                }
            }
            catch(Exception e)
            {
                final long    cpyEnd=System.currentTimeMillis(), cpyDuration=cpyEnd - cpyStart;
                System.err.println(e.getClass().getName() + " after " + cpyDuration + " msec. while handling target=" + tgt + ": " + e.getMessage());
            }
        }

        return 0;
    }
    // args[i] is a dependencies file path
    private static final int testMavenDownload (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    relPath=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "dependencies file path (or Quit)");
            if ((null == relPath) || (relPath.length() <= 0))
                continue;
            if (isQuit(relPath)) break;

            try
            {
                final Document                            doc=DOMUtils.loadDocument(relPath);
                final List<? extends BuildTargetFile>     fl=BuildTargetFile.fromRootElement(doc);
                testMavenDownload(out, in, fl);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while handling file=" + relPath + ": " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
//        testRelativeArtifactFilePaths(System.out, in, args);
//        testPOMFileLoad(System.out, in, args);
        testMavenDownload(System.out, in, args);
    }
}
