/*
 * 
 */
package net.community.chest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.nio.ByteOrder;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 10, 2013 1:45:19 PM
 */
public class JvmTester {
    private static BufferedReader   _stdin  /* =null */;
    public static final synchronized BufferedReader getStdin ()
    {
        if (null == _stdin)
            _stdin = new BufferedReader(new InputStreamReader(System.in));
        return _stdin;
    }

    public static final boolean isQuit (final String s)
    {
        return "q".equalsIgnoreCase(s) || "quit".equalsIgnoreCase(s);
    }

    public static final String getval (final PrintStream out, final BufferedReader in, final String prompt)
    {
        if (out != null)
            out.print("Enter " + prompt + ": ");
        try
        {
            return in.readLine();
        }
        catch(IOException e)
        {
            return e.getClass().getName() + ": " + e.getMessage();
        }
    }

    private static void showMap(PrintStream out, Map<?,?> m) {
        for (Map.Entry<?,?> e : m.entrySet()) {
            out.append('\t').append(String.valueOf(e.getKey())).append('=').println(e.getValue());
        }
    }

    private static void showEnvironment(PrintStream out, Map<String,String> env) {
        if ((env == null) || env.isEmpty()) {
            out.println("Empty environment");
        } else {
            showMap(out, env);
        }
    }

    private static void showProperties(PrintStream out, Properties props) {
        if ((props == null) || props.isEmpty()) {
            out.println("Empty properties");
        } else {
            showMap(out, props);
        }
    }

    private static void showNetwork(PrintStream out, Enumeration<? extends NetworkInterface> ifs) throws IOException {
        while((ifs != null) && ifs.hasMoreElements()) {
            NetworkInterface    netIf=ifs.nextElement();
            out.append('\t').append(netIf.getName())
               .append(": up=").append(String.valueOf(netIf.isUp()))
               .append(", loopback=").append(String.valueOf(netIf.isLoopback()))
               .append(", virtual=").append(String.valueOf(netIf.isVirtual()))
               .append(", PTP=").append(String.valueOf(netIf.isPointToPoint()))
               .println();
            for (InterfaceAddress addr : netIf.getInterfaceAddresses()) {
                out.append("\t\t").println(addr);
            }
        }
    }

    private static void showDetails(PrintStream out) {
        out.append("File separator: ").println(File.separator);
        out.append("Byte order: ").println(ByteOrder.nativeOrder());
        
        File[]  roots=File.listRoots();
        if ((roots != null) && (roots.length > 0)) {
            out.append("Roots:");
            for (File r : roots) {
                out.append(' ').append(r.getAbsolutePath());
            }
            out.println();
        }
    }

    private static void generateKeys(BufferedReader stdin, PrintStream stdout) throws GeneralSecurityException {
        for ( ; ; ) {
            String  algorithm=getval(stdout, stdin, "algorithm (or Quit)");
            if ((algorithm == null) || (algorithm.length() <= 0)) continue;
            if (isQuit(algorithm)) break;
            
            String  keySize=getval(stdout, stdin, algorithm + " key size");
            if ((keySize == null) || (keySize.length() <= 0)) continue;
            if (isQuit(keySize)) break;
            
            stdout.append("\tGenerate ").append(algorithm).append(" key size=").println(keySize);
            KeyPairGenerator    kg=KeyPairGenerator.getInstance(algorithm.toUpperCase());
            kg.initialize(Integer.parseInt(keySize));
            
            KeyPair kp=kg.generateKeyPair();
            Key     key=kp.getPrivate();
            stdout.append("\tGenerated ").append(key.getAlgorithm()).append(" key size=").println(keySize);
        }
    }

    private static void showFilesystem(PrintStream stdout) {
        stdout.append('\t').append("Name")
              .append('\t').append("Total")
              .append('\t').append("Usable")
              .append('\t').append("Free")
              .println();

        for (File   rootFolder : File.listRoots()) {
            long    totalSpace=rootFolder.getTotalSpace(), freeSpace=rootFolder.getFreeSpace();
            stdout.append('\t').append(rootFolder.getAbsolutePath())
                  .append('\t').append(String.valueOf(rootFolder.getTotalSpace()))
                  .append('\t').append(String.valueOf(rootFolder.getUsableSpace()))
                  .append('\t').append(String.valueOf(rootFolder.getFreeSpace()))
                  .append('\t').append((totalSpace > 0L) ? String.valueOf(((totalSpace - freeSpace) * 100L) / totalSpace) : "???").append('%')
                  .println();
        }
    }

    private static void showMemory(PrintStream stdout) {
        MemoryMXBean mxBean=ManagementFactory.getMemoryMXBean();
        showMemoryUsage("Heap", mxBean.getHeapMemoryUsage(), stdout);
        showMemoryUsage("Non-heap", mxBean.getNonHeapMemoryUsage(), stdout);
    }
    
    private static void showMemoryUsage(String type, MemoryUsage usage, PrintStream stdout) {
        stdout.append('\t').println(type);
        stdout.append("\t\t").append("Initial: ").println(usage.getInit());
        stdout.append("\t\t").append("Committed: ").println(usage.getCommitted());
        stdout.append("\t\t").append("Max: ").println(usage.getMax());
        stdout.append("\t\t").append("Used: ").println(usage.getUsed());
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        BufferedReader  in=getStdin();
        PrintStream     out=System.out;
        for ( ; ; ) {
            String  ans=getval(out, in, "(e)nvironment/(p)roperties/(n)etwork/(d)etails/(k)eys/(f)ile-system/(m)emory/(q)uit");
            if ((ans == null) || (ans.length() <= 0)) continue;
            out.println("===> executing " + ans);
            if (isQuit(ans)) break;
            
            final char  op=Character.toLowerCase(ans.charAt(0));
            try {
                switch(op) {
                    case 'e' : showEnvironment(out, System.getenv()); break;
                    case 'f' : showFilesystem(out); break;
                    case 'p' : showProperties(out, System.getProperties()); break;
                    case 'n' : showNetwork(out, NetworkInterface.getNetworkInterfaces()); break;
                    case 'd' : showDetails(out); break;
                    case 'm' : showMemory(out); break;
                    case 'k' : generateKeys(in, out); break;
                    default  : // ignored
                }
            } catch(Exception e) {
                System.err.append(e.getClass().getSimpleName()).append(": ").println(e.getMessage());
            }
        }
    }
}
