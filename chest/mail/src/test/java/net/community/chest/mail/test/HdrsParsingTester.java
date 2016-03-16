package net.community.chest.mail.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;
import net.community.chest.mail.message.RFCMessageStructure;
import net.community.chest.mail.message.StructureParserOutputStream;
import net.community.chest.test.TestBase;

public class HdrsParsingTester extends TestBase {
    public HdrsParsingTester ()
    {
        // do nothing
    }

    public static final int showMsgStructure (final RFCMessageStructure part, final PrintStream out)
    {
        if ((null == part) || (null == out))
            return (-1);

        out.println("ID=" + part.getPartId() + " " + part.getMIMEType() + "/" + part.getMIMESubType());
        out.println("\tHeaders: " + part.getHeadersStartOffset() + "-" + part.getHeadersEndOffset() + " size=" + part.getHeadersSize());
        {
            final Map<String,String>                                hdrs=part.getPartHeaders();
            final Collection<? extends Map.Entry<String,String>>    hdrsEntries=((null == hdrs) || (hdrs.size() <= 0)) ? null : hdrs.entrySet();
            for (final Iterator<? extends Map.Entry<String,String>>    iter=((null == hdrsEntries) || (hdrsEntries.size() <= 0)) ? null : hdrsEntries.iterator();
                 (iter != null) && iter.hasNext();
                 )
            {
                final Map.Entry<String, String>    eHdr=iter.next();
                if (null == eHdr)
                    continue;    // should not happen

                final String    hdrName=eHdr.getKey(), hdrValue=eHdr.getValue();
                out.println("\t\t" + hdrName + " " + hdrValue);
            }
        }
        out.println("\tData: " + part.getDataStartOffset() + "-" + part.getDataEndOffset() + " size=" + part.getDataSize());

        {
            final Collection<? extends RFCMessageStructure>    subParts=part.getSubParts();
            for (final Iterator<? extends RFCMessageStructure>        iter=((null == subParts) || (subParts.size() <= 0)) ? null : subParts.iterator();
                 (iter != null) && iter.hasNext(); )
            {
                final RFCMessageStructure    rp=iter.next();
                if (null == rp)
                    continue;

                final int    nErr=showMsgStructure(rp, out);
                if (nErr != 0)
                    return nErr;
            }
        }

        return 0;
    }

    /* ------------------------------------------------------------------- */

    public static final int testMessageStructure (final PrintStream out, final BufferedReader in, final File f)
    {
        if (f.isDirectory())
        {
            final File[]    fa=f.listFiles();
            if ((fa != null) && (fa.length > 0))
            {
                for (final File ff : fa)
                {
                    final int    nErr=testMessageStructure(out, in, ff);
                    if (nErr != 0)
                        break;

                }
            }

            return 0;
        }

        for ( ; ; )
        {
            InputStream    i=null;
            try
            {
                i = new FileInputStream(f);

                out.print("Processing " + f);

                final StructureParserOutputStream    msgStruct=new StructureParserOutputStream();
                final long                            psStart=System.currentTimeMillis(),
                                                    cpySize=IOCopier.copyStreams(i, msgStruct),
                                                    psEnd=System.currentTimeMillis(),
                                                    psDuration=(psEnd - psStart);
                if (cpySize <= 0L)
                    throw new StreamCorruptedException("Error (" + cpySize + ") while dumping input file");

                msgStruct.close();    // MUST call it to finalize the structure

                out.println("\tprocessed " + cpySize + " bytes in " + psDuration + " msec.");
                showMsgStructure(msgStruct.getParsedStructure(), out);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
            finally
            {
                try
                {
                    FileUtil.closeAll(i);
                }
                catch(IOException e)
                {
                    // ignore
                }
            }

            final String    ans=getval(out, in, "again [y]/n/q");
            if (isQuit(ans)) return (-1);
            if ((ans != null) && (ans.length() > 0) && (Character.toLowerCase(ans.charAt(0)) != 'y'))
                break;
        }

        return 0;
    }

    // args[i]=file path of RFC822 formatted message or folder containing such files
    public static final int testMessageStructure (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    path=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "RFC822 file path (or Quit)");
            if ((null == path) || (path.length() <= 0))
                continue;
            if (isQuit(path)) break;

            testMessageStructure(out, in, new File(path));
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
        final int                nErr=testMessageStructure(System.out, in, args);
        if (nErr != 0)
            System.err.println("Failed: err=" + nErr);
        else
            System.out.println("Finished");
    }

}
