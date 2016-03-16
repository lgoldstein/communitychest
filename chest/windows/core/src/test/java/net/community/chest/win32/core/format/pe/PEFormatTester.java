/*
 *
 */
package net.community.chest.win32.core.format.pe;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;
import net.community.chest.io.file.FileIOUtils;
import net.community.chest.io.input.TrackingInputStream;
import net.community.chest.test.TestBase;
import net.community.chest.win32.core.format.pe.rsrc.ResourceDirectoryEntry;
import net.community.chest.win32.core.format.pe.rsrc.ResourceDirectoryTableHeader;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 15, 2009 2:54:31 PM
 */
public class PEFormatTester extends TestBase {
    public static final List<SectionTableEntry> testPEFormatAnalyzer (
            final PrintStream out, final InputStream inStream)
        throws IOException
    {
        final DosHeaderStub dosStub=new DosHeaderStub(inStream);
        {
            final long hdrOffset=dosStub.getExtendedHeaderOffset(), skipOffset=hdrOffset - DosHeaderStub.HEADER_OFFSET_VALUE - 4L;
            if (skipOffset < 0L) {
                throw new StreamCorruptedException("Bad extended header skip offset: " + skipOffset);
            }

            FileIOUtils.skipFully(inStream, skipOffset);
        }

        final PEHeaderData            hdr=new PEHeaderData(inStream);
        final COFFFileHeader        ch=hdr.getCoffHeader();
        out.println("\tCOFF Header: " + ch);

        final OptHeaderStdFields    oh=hdr.getStdHeaderFields();
        out.println("\tOpt (std) Header: " + oh);

        final OptHeaderWin32Fields    wh=hdr.getWinHeaderFields();
        out.println("\tOpt (win) Header" + wh);

        {
            final List<? extends ImageDataDirectoryEntry>                                         el=
                wh.getDirEntries();
            final Map<ImageDataDirectoryEntry.DirEntryType,? extends ImageDataDirectoryEntry>    em=
                ImageDataDirectoryEntry.buildEntriesMap(el, true);
            final Collection<? extends Map.Entry<ImageDataDirectoryEntry.DirEntryType,? extends ImageDataDirectoryEntry>>    dl=
                ((null == em) || (em.size() <= 0)) ? null : em.entrySet();
            if ((dl != null) && (dl.size() > 0))
            {
                for (final Map.Entry<ImageDataDirectoryEntry.DirEntryType,? extends ImageDataDirectoryEntry> de : dl)
                    out.println("\t\t[" + de.getKey() + "] " + de.getValue());
            }
        }

        final List<SectionTableEntry>    sl=hdr.getSections();
        Collections.sort(sl, SectionTableEntry.BY_RAW_DATA_POINTER);
        if ((sl != null) && (sl.size() > 0))
        {
            out.println("\tSections");
            for (final SectionTableEntry se : sl)
                out.println("\t\t" + se);
        }

        return sl;
    }

    /* -------------------------------------------------------------------- */

    private static final void showResourceSection(final PrintStream out, final InputStream inStream) throws IOException {
        final ResourceDirectoryTableHeader  th=new ResourceDirectoryTableHeader(inStream);
        out.println("\t\t\t\t" + th);

        {
            final Collection<? extends ResourceDirectoryEntry> el=th.getNameEntries();
            if ((el != null) && (el.size() > 0))
            {
                out.println("\t\t\t\tNames");
                for (final ResourceDirectoryEntry de : el)
                    out.println("\t\t\t\t\t" + de);
            }
        }

        {
            final Collection<? extends ResourceDirectoryEntry> el=th.getIdEntries();
            if ((el != null) && (el.size() > 0))
            {
                out.println("\t\t\t\tID(s)");
                for (final ResourceDirectoryEntry de : el)
                    out.println("\t\t\t\t\t" + de);
            }
        }
/*
        for (final ResourceDirectoryString  rds=new  ResourceDirectoryString(); ; )
        {
            final ResourceDirectoryString   s=rds.read(inStream);
            out.println("\t\t\t\t\t[len=" + s.getLength() + "] " + s);

            final String    ans=getval(out, in, "read next dir string [y]/n");
            if ((ans != null) && (ans.length() > 0) && (Character.toLowerCase(ans.charAt(0)) != 'y'))
                break;
        }
*/
    }

    /* -------------------------------------------------------------------- */

    public static final void showSectionTableEntries (
                final PrintStream out, final Collection<? extends SectionTableEntry> sl, final TrackingInputStream inStream)
                        throws IOException {
       for (final SectionTableEntry se : sl) {
            out.println("\t\t\tProcessing " + se);

            final long  reOffset=se.getPointerToRawData();
            if (reOffset <= 0L)
                continue;

            final long  inOffset=inStream.getPos(), skOffset=reOffset - inOffset;
            if (skOffset < 0L) {
                System.err.println("Requested offset (" + reOffset + ") below current (" + inOffset + ")");
                continue;
            }

            FileIOUtils.skipFully(inStream, skOffset);

            final String    sectionName=se.getName();
            if (".rsrc".equals(sectionName)) {
                showResourceSection(out, inStream);
            } else {
                System.err.println("Unknown section type: " + sectionName);
            }
        }
    }

    /* -------------------------------------------------------------------- */

    private static final void runPEFormatAnalyzer(final PrintStream out, final BufferedReader in, final String filePath)
    {
        for ( ; ; )
        {
            out.println("Processing " + filePath);

            try
                {
                TrackingInputStream    inStream=null;

                try
                {
                    inStream = new TrackingInputStream(
                            new BufferedInputStream(
                                    new FileInputStream(filePath), IOCopier.DEFAULT_COPY_SIZE));

                    Collection<? extends SectionTableEntry>    sl=testPEFormatAnalyzer(out, inStream);
                    showSectionTableEntries(out, sl, inStream);
                }
                finally
                {
                    FileUtil.closeAll(inStream);
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }

            final String    ans=getval(out, in, "again [y]/n");
            if ((ans != null) && (ans.length() > 0) && (Character.toLowerCase(ans.charAt(0)) != 'y'))
                break;
        }
    }

    /* -------------------------------------------------------------------- */

    // args[i] an EXE/DLL File path
    public static final int testPEFormatAnalyzer (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int aIndex=0; ; aIndex++)
        {
            final String    s=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "file path (or Quit)");
            final int        sLen=(null == s) ? 0 : s.length();
            if (sLen <= 0)
                continue;
            if (isQuit(s))
                break;

            runPEFormatAnalyzer(out, in, s);
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
        final int                nErr=testPEFormatAnalyzer(System.out, in, args);
        if (nErr != 0)
            System.err.println("test failed (err=" + nErr + ")");
        else
            System.out.println("OK");
    }
}
