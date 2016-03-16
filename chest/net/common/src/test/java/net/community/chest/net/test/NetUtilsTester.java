package net.community.chest.net.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

import net.community.chest.io.FileUtil;
import net.community.chest.net.NetUtil;
import net.community.chest.net.proto.ServiceDefinition;
import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.AttributeMethodType;
import net.community.chest.test.TestBase;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jun 28, 2007 3:07:43 PM
 */
public class NetUtilsTester extends TestBase {

    //////////////////////////////////////////////////////////////////////////

    private static final void showNetInfo (final NetUtil.ComputerNetInfoEnum eInfo, final PrintStream out)
    {
        try
        {
            final String    v=NetUtil.getNetInfo(eInfo);
            out.println("\t" + eInfo + ": " + v);
        }
        catch(Exception e)
        {
            System.err.println("getNetInfo(" + eInfo + ") " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private static final void showAllNetInfo (final PrintStream out)
    {
        final NetUtil.ComputerNetInfoEnum[]    vals=NetUtil.ComputerNetInfoEnum.getValues();
        for (final NetUtil.ComputerNetInfoEnum    eInfo : vals)
            showNetInfo(eInfo, out);
    }

    // args[i] - any command
    public static final int testComputerInfo (final PrintStream out, final BufferedReader in, final String...args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    ans=
                (aIndex < numArgs) ? args[aIndex] : getval(out, in, "[A]ll/(S)hort name/(F)ull name/(I)P address/(D)omain/(Q)uit");
            if (isQuit(ans))
                break;

            final char    op=((null == ans) || (ans.length() <= 0)) ? '\0' : Character.toUpperCase(ans.charAt(0));
            switch(op)
            {
                case '\0'    : // fall through to default (A)ll info
                case 'A'    : showAllNetInfo(out); break;
                case 'S'    : showNetInfo(NetUtil.ComputerNetInfoEnum.SHORT_NAME, out); break;
                case 'F'    : showNetInfo(NetUtil.ComputerNetInfoEnum.FULL_NAME, out); break;
                case 'I'    : showNetInfo(NetUtil.ComputerNetInfoEnum.IP_ADDR, out); break;
                case 'D'    : showNetInfo(NetUtil.ComputerNetInfoEnum.DOMAIN_NAME, out); break;

                default        : /* do nothing */
            }

        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    private static void showLocalAddresses (final PrintStream out, final InetAddress[] la)
    {
        if ((la != null) && (la.length > 0))
        {
            for (final InetAddress a : la)
            {
                if (null == a)    // should not happen
                    continue;

                out.println("\t" + a.getHostName() + "[" + a.getHostAddress() + "]");
            }
        }
        else
            out.println("\tno addresses found");
    }

    private static void showLocalInterfaces (final PrintStream out, final NetworkInterface[] ifs)
    {
        if ((ifs != null) && (ifs.length > 0))
        {
            for (final NetworkInterface nic : ifs)
            {
                if (null == nic)    // should not happen
                    continue;

                out.println("\t" + nic.getName() + "[" + nic.getDisplayName() + "]");

                for (final Enumeration<InetAddress>    nicAddrs=nic.getInetAddresses();
                     (nicAddrs != null) && nicAddrs.hasMoreElements();
                    )
                {
                    final InetAddress    a=nicAddrs.nextElement();
                    if (null == a)    // should not happen
                        continue;

                    out.println("\t\t" + a.getHostName() + "[" + a.getHostAddress() + "]");
                }
            }
        }
        else
            out.println("\tno interfaces detected");
    }
    // args[0]=(C)omputer info/(I)net address, args[1,2,3...]=as per specific test
    public static final int testNetUtils (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final String modePrompt="(C)omputer info/Local (A)ddresses/Local (I)nterfaces";
        for (int    tIndex=0; ; tIndex++)
        {
            String        tstMode=null;
            String[]    realArgs=null;
            if (0 == tIndex)
            {
                if ((args != null) && (args.length > 0))
                {
                    tstMode = args[0];

                    if (args.length > 1)
                    {
                        realArgs = new String[args.length-1];
                        System.arraycopy(args, 1, realArgs, 0, args.length-1);
                    }
                }
            }

            while ((null == tstMode) || (tstMode.length() <= 0))
            {
                tstMode = getval(out, in, modePrompt);
                if (isQuit(tstMode))
                    return 0;
            }

            final char    opChar=Character.toUpperCase(tstMode.charAt(0));
            try
            {
                switch(opChar)
                {
                    case 'C'    : testComputerInfo(out, in, realArgs); break;
                    case 'A'    : showLocalAddresses(out, NetUtil.getAllLocalAddresses()); break;
                    case 'I'    : showLocalInterfaces(out, NetUtil.getAllLocalInterfaces()); break;

                    default        :
                        System.err.println("Unknown option: " + tstMode);
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////
    // each argument is a URI string
    public static final int testURIComponents (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final Map<String,? extends AttributeAccessor>    aMap=
            AttributeMethodType.getAllAccessibleAttributes(URI.class);
        final Collection<? extends Map.Entry<String,? extends AttributeAccessor>> al=aMap.entrySet();

        for (int    aIndex=0, numArgs=(null == args) ? 0 : args.length; ; aIndex++)
        {
            final String    s=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "URI (or Quit)");
            if ((null == s) || (s.length() <= 0))
                continue;
            if (isQuit(s)) break;
            try
            {
                final URI    u=new URI(s);
                out.println(s);
                for (final Map.Entry<String,? extends AttributeAccessor> ae : al)
                {
                    final String            n=(null == ae) ? null : ae.getKey();
                    final AttributeAccessor    aa=(null == ae) ? null : ae.getValue();
                    final Method            gm=(null == aa) ? null : aa.getGetter();
                    if ((null == gm) || "content".equalsIgnoreCase(n))
                        continue;

                    final Object    v=gm.invoke(u, AttributeAccessor.EMPTY_OBJECTS_ARRAY);
                    if (v instanceof Class<?>)
                        continue;

                    out.println("\t" + n + ": " + v);
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static final int testReadServicesFile (
            final PrintStream out, final BufferedReader in, final File f)
    {
        for ( ; ; )
        {
            out.println("Parsing " + f);

            Reader    r=null;
            try
            {
                r = new BufferedReader(new FileReader(f));

                try
                {
                    final long                                        dStart=System.currentTimeMillis();
                    final Collection<? extends ServiceDefinition>    dl=
                        ServiceDefinition.readServices(r);
                    final long                                        dEnd=System.currentTimeMillis(),
                                                                    dDuration=dEnd - dStart;
                    final int                                        dNum=
                        (null == dl) ? 0 : dl.size();
                    out.println("\tRead " + dNum + " entries in " + dDuration + " msec.");
                    if (dNum > 0)
                    {
                        for (final ServiceDefinition d : dl)
                            out.append("\t\t").append(d.toString()).println();
                    }
                }
                finally
                {
                    FileUtil.closeAll(r);
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

        return 0;
    }

    // each argument is a services file path string
    public static final int testReadServicesFile (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        for (int    aIndex=0, numArgs=(null == args) ? 0 : args.length; ; aIndex++)
        {
            final String    s=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "services file path (or Quit)");
            if ((null == s) || (s.length() <= 0))
                continue;
            if (isQuit(s)) break;

            testReadServicesFile(out, in, new File(s));
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
//        final int                nErr=testNetUtils(System.out, in, args);
//        final int                nErr=testURIComponents(System.out, in, args);
        final int                nErr=testReadServicesFile(System.out, in, args);
        if (nErr != 0)
            System.err.println("test failed (err=" + nErr + ")");
        else
            System.out.println("OK");
    }

}
