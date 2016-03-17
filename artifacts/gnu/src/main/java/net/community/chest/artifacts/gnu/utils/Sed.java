/*
 *
 */
package net.community.chest.artifacts.gnu.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamCorruptedException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.community.chest.artifacts.gnu.options.Option;
import net.community.chest.artifacts.gnu.options.Option.OptionParseResult;
import net.community.chest.io.EOLStyle;
import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 9, 2011 9:37:49 AM
 */
public final class Sed implements Runnable {
    private final Collection<? extends File>    _inputFiles;
    private final EOLStyle    _eolStyle;
    private final Pattern    _pattern;
    private final String    _replacement;
    private final boolean    _inPlace;
    private final String    _bkpSuffix;

    Sed (final OptionParseResult result)
    {
        final Map<String,? extends Option>    optsMap=Option.toMap(result.getParsedOptions());
        final String[]                        args=result.getArguments();
        final int                            startIndex=result.getNumParsed();
        _eolStyle = resolveEOLStyle(optsMap);
        _inPlace = resolveOutputMode(optsMap);
        _bkpSuffix = resolveBackupSuffix(optsMap);
        _pattern = Pattern.compile(extractArgument("Missing patttern to match", startIndex, args));
        _replacement = extractArgument("Missing replacement value", startIndex + 1, args);
        _inputFiles = getProcessedFiles(startIndex /* skip the pattern and the replacement */, args);
    }
    /*
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run ()
    {
        try
        {
            if (_inputFiles.isEmpty())
            {
                processStreams(new BufferedReader(new InputStreamReader(System.in)), System.out);
                return;
            }
        }
        catch(IOException e)
        {
            throw new RuntimeException("Failed (" + e.getClass().getName() + ") to process STDIN/STDOUT: " + e.getMessage(), e);
        }

    }

    private void processInputFiles (final Collection<? extends File> files)
    {
        for (final File file : files)
        {
            if (file.isFile())
            {
                try
                {
                    processFile(file);
                }
                catch(IOException e)
                {
                    throw new RuntimeException("Failed (" + e.getClass().getName() + ")"
                                             + " to process file=" + file.getAbsolutePath()
                                             + ": " + e.getMessage(), e);
                }
            }
            else
                processInputFiles(file.listFiles());
        }
    }

    private void processInputFiles (final File ... files)
    {
        if ((files == null) || (files.length <= 0))
            return;
        processInputFiles(Arrays.asList(files));
    }

    private void processFile (final File orgFile) throws IOException
    {
        File            inFile=orgFile, outFile=resolveOutputFile(inFile);
        final boolean    inPlace=(outFile == inFile), toStdout=(outFile == null);
        if ((!toStdout) && (!inPlace))
        {
            final long    cpySize=IOCopier.copyFile(inFile, outFile);
            if (cpySize < 0L)
                throw new StreamCorruptedException("Failed (" + cpySize + ") to copy " + inFile + " => " + outFile);

            // change the roles since - i.e., read from backup and write back to original
            inFile = outFile;
            outFile = orgFile;
        }

        final BufferedReader    rdr=new BufferedReader(new FileReader(inFile), IOCopier.DEFAULT_COPY_SIZE);
        final Appendable        out;
        try
        {
            out = resolveOutputStream(outFile, inPlace, toStdout);

            try
            {
                processStreams(rdr, out);
            }
            finally
            {
                if ((out instanceof Closeable) && (out != System.out))
                    ((Closeable) out).close();
            }
        }
        finally
        {
            rdr.close();
        }

        if (inPlace)
            replaceOriginalFile(orgFile, (StringBuilder) out);
    }

    Appendable resolveOutputStream (final File outFile, final boolean inPlace, final boolean toStdout) throws IOException
    {
        if (inPlace)
            return getWorkBuf();
        else if (toStdout)
            return System.out;
        else
            return new BufferedWriter(new FileWriter(outFile), IOCopier.DEFAULT_COPY_SIZE);
    }

    private char[]    _cpyBuf;
    // returns number of written characters
    long replaceOriginalFile (final File orgFile, final StringBuilder sb) throws IOException
    {
        if (_cpyBuf == null)
            _cpyBuf = new char[IOCopier.DEFAULT_COPY_SIZE];

        final Writer    w=new FileWriter(orgFile);
        try
        {
            final long    cpySize=IOCopier.copyCharacters(sb, w, _cpyBuf);
            if (cpySize != sb.length())
                throw new StreamCorruptedException("Failed (" + cpySize + ") to copy data to file " + orgFile.getAbsolutePath());
            return cpySize;
        }
        finally
        {
            w.close();
        }
    }

    File resolveOutputFile (final File inFile)
    {
        if (_inPlace)
        {
            if ((_bkpSuffix == null) || (_bkpSuffix.length() <= 0))
                return inFile;

            final String    bkpPath=FileUtil.adjustFileName(inFile.getAbsolutePath(), _bkpSuffix);
            return new File(bkpPath);
        }

        return null;    // signal output to STDOUT
    }

    // returns number of processed lines
    int processStreams (final BufferedReader rdr, final Appendable out) throws IOException
    {
        int    numLines=0;
        for (String    line=rdr.readLine(); line != null; numLines++, line=rdr.readLine())
        {
            if (numLines > 0)    // separate from previous
                _eolStyle.appendEOL(out);

            final Matcher    m=_pattern.matcher(line);
            final String    newLine=m.replaceAll(_replacement);
            if (newLine != line)
                out.append(newLine);    // debug breakpoint when something replaced
            else
                out.append(line);
        }

        return numLines;
    }

    private StringBuilder    _workBuf;
    private StringBuilder getWorkBuf ()
    {
        if (_workBuf == null)
            _workBuf = new StringBuilder(IOCopier.DEFAULT_COPY_SIZE);
        if (_workBuf.length() > 0)
            _workBuf.setLength(0);
        return _workBuf;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return _pattern.pattern() + " => " + _replacement;
    }

    static final String extractArgument (String errorMessage, int index, String ... args)
    {
        final int    numArgs=(args == null) ? 0 : args.length;
        if (index >= numArgs)
            throw new IllegalArgumentException(errorMessage);
        return args[index];
    }

    static final List<File> getProcessedFiles (final int startIndex, final String ... args)
    {
        List<File>     files=null;
        for (int    aIndex=startIndex; aIndex < args.length; aIndex++)
        {
            final String    fileName=args[aIndex];
            if (files == null)
                files = new ArrayList<File>(1 + args.length - aIndex);
            files.add(new File(fileName));
        }

        if (files == null)
            return Collections.emptyList();

        return files;
    }

    static final EOLStyle resolveEOLStyle (final Map<String,? extends Option>    optsMap)
    {
        final Option    option=(optsMap == null) ? null : optsMap.get("eol");
        final EOLStyle    eolStyle=(option == null) ? null : EOLStyle.fromName(option.getValue());
        if (eolStyle != null)
            return eolStyle;

        if (option != null)
            throw new IllegalArgumentException("Unknown EOL style value: " + option.getValue());
        return EOLStyle.LOCAL;
    }

    static final boolean resolveOutputMode (final Map<String,? extends Option>    optsMap)
    {
        final Option    option=Option.findFirstMatchingOption(optsMap, "i", "in-place");
        if (option == null)
            return false;
        else
            return true;
    }

    static final String resolveBackupSuffix (final Map<String,? extends Option>    optsMap)
    {
        final Option    option=Option.findFirstMatchingOption(optsMap, "i", "in-place");
        if (option == null)
            return null;
        else
            return option.getValue();
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        new Sed(Option.parseArguments(args)).run();
    }
}
