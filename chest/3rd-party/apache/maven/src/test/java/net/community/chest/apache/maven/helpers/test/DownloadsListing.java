package net.community.chest.apache.maven.helpers.test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Goes over the output of a Maven build and extracts all the &quot;
 * Downloading:&quot; lines to extract the download JAR(s)</P>
 *
 * @author Lyor G.
 * @since May 1, 2008 4:10:39 PM
 */
public class DownloadsListing extends MavenTestBase {
    protected DownloadsListing ()
    {
        super();
    }

    private static final String    KEYWORD="Downloading:";
    private static final int    KWLEN=KEYWORD.length();
    private static final int showDownloads (final String path, final String repoPrefix, final PrintStream out, final BufferedReader fin) throws IOException
    {
        int    numEntries=0;

        out.println("================== " + path + " ==========================");
        out.println("Group,Artifact,Version,JAR");
        for (String l=fin.readLine(); l != null; l = fin.readLine())
        {
            final int    ll=l.length();
            if (ll <= KWLEN)
                continue;

            final char    ch=l.charAt(KWLEN);
            if ((ch != ' ') && (ch != '\t'))
                continue;

            final String    kwVal=l.substring(0, KWLEN);
            if (!KEYWORD.equalsIgnoreCase(kwVal))
                continue;

            final String    url=l.substring(KWLEN+1).trim();
            if (!url.startsWith(repoPrefix))
            {
                out.println("==> " + url);
                continue;
            }

            if (showData(out, url.substring(repoPrefix.length()), '/'))
                numEntries++;
        }

        return numEntries;
    }
    // args[i]=the full path containing output of a Maven build
    private static final int showDownloads (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        String        lastPrefix="http://hermes:8081/artifactory/repo/";
        for (int    aIndex=0; ; aIndex++)
        {
            final String    path=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "Maven output path (or Quit)");
            if ((null == path) || (path.length() <= 0))
                continue;
            if (isQuit(path)) break;

            String    repoPrefix=getval(out, in, "file=" + path + " repository URL prefix (ENTER=" + lastPrefix + ")/(Q)uit");
            if (isQuit(repoPrefix)) break;

            if ((null == repoPrefix) || (repoPrefix.length() <= 0))
                repoPrefix = lastPrefix;
            else
                lastPrefix = repoPrefix;

            try(BufferedReader    fin=new BufferedReader(new FileReader(path))) {
                showDownloads(path, repoPrefix, out, fin);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while handling file=" + path + ": " + e.getMessage());
            }
        }

        return 0;
    }

    public static void main (String[] args)
    {
        showDownloads(System.out, getStdin(), args);
    }
}
