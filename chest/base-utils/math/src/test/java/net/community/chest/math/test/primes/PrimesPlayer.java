/*
 *
 */
package net.community.chest.math.test.primes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StreamCorruptedException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.community.chest.io.EOLStyle;
import net.community.chest.io.IOCopier;
import net.community.chest.test.TestBase;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jul 16, 2013 12:19:18 PM
 */
public class PrimesPlayer extends TestBase {
    private PrimesPlayer() {
        super();
    }

    public static final byte reducedDigitsSum(long v) {
        long    value=v;
        if (value == Long.MIN_VALUE) {
            value = Long.MAX_VALUE;
        }
        if (value < 0L) {
            value = 0L - value;
        }

        while (value > 9L) {
            long    sum=0L;
            for ( ; value != 0L; value = value / 10L) {
                sum += value % 10L;
            }

            value = sum;
        }

        return (byte) (value & 0x0FL);
    }

    public static final byte reducedDigitsSum(CharSequence cs) throws NumberFormatException {
        if ((cs == null) || (cs.length() <= 0)) {
            return 0;
        }

        long    sum=0L;
        for (int index=0; index < cs.length(); index++) {
            char    digit=cs.charAt(index);
            if ((digit < '0') || (digit > '9')) {
                throw new NumberFormatException("Bad digit (" + digit + ") at offset=" + index);
            }

            sum += digit - '0';
        }

        return reducedDigitsSum(sum);
    }

    //////////////////////////////////////////////////////////////////////////

    private static final long flattenPrimesFiles(PrintStream out, BufferedReader rdr, Appendable writer) throws IOException {
        String  title=null;
        long    count=0L;
        for (String line=rdr.readLine(); line != null; line = rdr.readLine()) {
            line = line.trim();

            if (line.length() <= 0) {
                continue;
            }

            if (title == null) {
                title = line;
                out.append('\t').println(title);
                continue;
            }

            String[]    values=line.split(" ");
            for (String v : values) {
                v = v.trim();
                if (v.length() <= 0) {
                    continue;
                }

                writer.append(v).append(EOLStyle.LOCAL.getStyleString());
                count++;
            }
        }

        return count;
    }

    protected static final long flattenPrimesFiles(PrintStream out, File targetFolder, Collection<String> names) throws IOException {
        File    outFile=new File(targetFolder, "primes-list.txt");
        long    count=0L;
        Writer  w=new BufferedWriter(new FileWriter(outFile));
        try {
            for (String n : names) {
                out.append("\tProcessing ").append(n).append(" ... ");
                BufferedReader  rdr=new BufferedReader(new FileReader(new File(targetFolder, n)), IOCopier.DEFAULT_COPY_SIZE);
                try {
                    long    numValues=flattenPrimesFiles(out, rdr, w);
                    out.println(numValues);
                    count += numValues;
                } finally {
                    rdr.close();
                }
            }
        } finally {
            w.close();
        }

        return count;
    }

    protected static final Set<String> downloadPrimesList(BufferedReader in, PrintStream out, File targetFolder) {
        Integer startIndex=inputIntValue(out, in, "start index", 1, Short.MAX_VALUE, Integer.valueOf(1));
        if (startIndex == null)
            return Collections.emptySet();

        Integer endIndex=inputIntValue(out, in, "end index", 1, Short.MAX_VALUE, Integer.valueOf(50));
        if (endIndex == null)
            return Collections.emptySet();

        if ((!targetFolder.exists()) && (!targetFolder.mkdirs())) {
            throw new IllegalStateException("Cannot create target folder");
        }

        byte[]  workBuf=new byte[IOCopier.DEFAULT_COPY_SIZE];
        Set<String> names=new TreeSet<String>(new Comparator<String>() {
            @Override
            public int compare(String n1, String n2) {
                int i1=indexOf(n1), i2=indexOf(n2);
                return i1 - i2;
            }

            private int indexOf(String s) {
                int pos=s.lastIndexOf('.');
                if (pos <= 0) {
                    return (-1);
                }

                for (int startPos=pos - 1; startPos >= 0; startPos--) {
                    char    ch=s.charAt(startPos);
                    if ((ch < '0') || (ch > '9')) {
                        String  idx=s.substring(startPos + 1, pos);
                        return Integer.parseInt(idx);
                    }
                }

                return (-1);
            }
        });
        for (int    index=startIndex.intValue(); index <= endIndex.intValue(); index++) {
            File   targetFile=new File(targetFolder, "primes" + index + ".txt");
            if (targetFile.exists()) {
                names.add(targetFile.getName());
                continue;
            }

            out.append("Download index=").println(index);
            try {
                URL url=new URL("http://primes.utm.edu/lists/small/millions/primes" + index + ".zip");
                ZipInputStream  zipStream=new ZipInputStream(url.openStream());
                try {
                    for (ZipEntry   ze=zipStream.getNextEntry(); ze != null; ze = zipStream.getNextEntry()) {
                        try {
                            String name=ze.getName();
                            if (!names.add(name)) {
                                throw new FileAlreadyExistsException(name);
                            }

                            out.append('\t').println(targetFile.getAbsolutePath());

                            OutputStream    o=new FileOutputStream(targetFile);
                            try {
                                IOCopier.copyStreams(zipStream, o, workBuf);
                            } finally {
                                o.close();
                            }
                        } finally {
                            zipStream.closeEntry();
                        }
                    }
                } finally {
                    zipStream.close();
                }
            } catch(Exception e) {
                System.err.append("Failed (").append(e.getClass().getSimpleName()).append(')')
                          .append(" to retrieve index=").append(String.valueOf(index))
                          .append(": ").println(e.getMessage());
            }
        }

        return names;
    }

    private static final Map<Byte,Long> countDigits(PrintStream out, InputStream in) throws IOException {
        long[]  counts=new long[1 + ('9' - '0')];
        for (int nRead=in.read(), nCount=0, nLines=0; nRead != (-1); nRead=in.read()) {
            if ((nRead < '0') || (nRead > '9')) {
                if (nRead == '\n') {
                    nLines++;
                }
                continue;
            }

            int idx=nRead - '0';
            counts[idx]++;
            nCount++;
            if (nCount < 0)
                nCount = 1;
            if ((nLines % 1024) == 0) {
                out.append('.');
            }

            if ((nCount % 0xFFFF) == 0) {
                out.println(nCount);
            }
        }
        out.println();

        Map<Byte,Long>  result=new TreeMap<Byte,Long>();
        for (byte index=0; index < counts.length; index++) {
            result.put(Byte.valueOf(index), Long.valueOf(counts[index]));
        }

        return result;
    }

    protected static final Map<Byte,Long> countDigits(PrintStream out, Class<?> anchor, String name) throws IOException {
        Map<Byte,Long>  totals=null;
        ZipInputStream  zs=new ZipInputStream(anchor.getResourceAsStream(name));
        try {
            for (ZipEntry ze=zs.getNextEntry(); ze != null; ze = zs.getNextEntry()) {
                out.println(ze.getName());
                try {
                    Map<Byte,Long>  result=countDigits(out, zs);
                    out.println(ze.getName());
                    for (Map.Entry<Byte,Long> re : result.entrySet()) {
                        out.append('\t').append(String.valueOf(re.getKey())).append('\t').println(re.getValue());
                    }

                    if (totals == null) {
                        totals = result;
                    }
                } finally {
                    zs.closeEntry();
                }
            }
        } finally {
            zs.close();
        }

        return totals;
    }

    protected static final Map<Byte,Long> loadDigitsCounts(PrintStream out, Class<?> anchor, String name) throws IOException {
        Properties  props=new Properties();
        InputStream ps=anchor.getResourceAsStream(name);
        try {
            props.load(ps);
        } finally {
            ps.close();
        }

        Map<Byte,Long>  result=new TreeMap<Byte,Long>();
        long            total=0L;
        for (char digit='0'; digit <= '9'; digit++) {
            Byte    key=Byte.valueOf((byte) (digit - '0'));
            String  count=props.getProperty("prime.digit." + String.valueOf(digit) + ".count");
            Long    value=(count == null) ? Long.valueOf(0L) : Long.valueOf(count.trim());
            Long    prev=result.put(key, value);
            if (prev != null) {
                throw new StreamCorruptedException("loadDigitsCounts(" + anchor.getSimpleName() + ")[" + name + "] multiple values for digit=" + digit);
            }

            total += value.longValue();
        }

        for (Map.Entry<Byte,Long> re : result.entrySet()) {
            Long    value=re.getValue();
            out.append('\t').append(String.valueOf(re.getKey()))
               .append('\t').append(value.toString())
               .append('\t').append(String.valueOf((value.longValue() * 100L) / total)).println('%');
        }

        return result;
    }

    protected static final Map<Byte,Long> countReducedDigitsSums(PrintStream out, InputStream in) throws IOException {
        long[]  counts=new long[1 + ('9' - '0')];
        long    sum=0L, procIndex=0L, lineIndex=0L;
        boolean counting=false;
        for (int nRead=in.read(); nRead != (-1); nRead=in.read()) {
            if ((nRead < '0') || (nRead > '9')) {
                if (counting) {
                    procIndex++;
                    if ((procIndex % 1024) == 0L) {
                        lineIndex++;
                        if ((lineIndex % 64L) == 0L) {
                            out.println(procIndex);
                        } else {
                            out.append('.');
                        }
                    }

                    byte    reducedSum=reducedDigitsSum(sum);
                    counts[reducedSum]++;
                    counting = false;
                    sum = 0L;
                }

                continue;
            }

            if (!counting) {
                counting = true;    // debug breakpoint
            }

            sum += ((nRead & 0xFF) - '0');
        }

        if (counting) { // check if any leftovers
            byte    reducedSum=reducedDigitsSum(sum);
            counts[reducedSum]++;
        }

        Map<Byte,Long>  result=new TreeMap<Byte,Long>();
        for (byte index=0; index < counts.length; index++) {
            result.put(Byte.valueOf(index), Long.valueOf(counts[index]));
        }

        return result;
    }

    protected static final Map<Byte,Long> countReducedDigitsSums(PrintStream out, Class<?> anchor, String name) throws IOException {
        Map<Byte,Long>  totals=null;
        ZipInputStream  zs=new ZipInputStream(anchor.getResourceAsStream(name));
        try {
            for (ZipEntry ze=zs.getNextEntry(); ze != null; ze = zs.getNextEntry()) {
                out.println(ze.getName());
                try {
                    Map<Byte,Long>  result=countReducedDigitsSums(out, zs);
                    out.println(ze.getName());
                    for (Map.Entry<Byte,Long> re : result.entrySet()) {
                        out.append('\t').append(String.valueOf(re.getKey())).append('\t').println(re.getValue());
                    }

                    if (totals == null) {
                        totals = result;
                    }
                } finally {
                    zs.closeEntry();
                }
            }
        } finally {
            zs.close();
        }

        return totals;
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) throws Exception {
//        File                targetFolder=new File(new File(System.getProperty("java.io.tmpdir")), "primes");
//        Collection<String>  names=downloadPrimesList(getStdin(), System.out, targetFolder);
//        flattenPrimesFiles(System.out, targetFolder, names);
//        countDigits(System.out, PrimesPlayer.class, "primes-list.zip");
        countReducedDigitsSums(System.out, PrimesPlayer.class, "primes-list.zip");
//        loadDigitsCounts(System.out, PrimesPlayer.class, "primes-list.properties");
    }
}
