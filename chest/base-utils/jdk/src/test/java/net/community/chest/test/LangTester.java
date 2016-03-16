package net.community.chest.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.spec.DHParameterSpec;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.resources.PropertiesResolver;
import net.community.chest.resources.SystemPropertiesResolver;
import net.community.chest.util.compare.VersionComparator;
import net.community.chest.util.datetime.TimeUnits;

/**
 * Copyright 2007 as per GPLv2
 *
 * Test various aspects of the Java language
 * @author Lyor G.
 * @since Jun 28, 2007 11:51:00 AM
 */
public final class LangTester extends TestBase {
    private LangTester ()
    {
        // do nothing
    }

    //////////////////////////////////////////////////////////////////////////

    private static void showItems (final PrintStream out, final int ... values)
    {
        out.println("showItems(out,int... values)");
        if ((values != null) && (values.length > 0))
        {
            for (final int v : values)
                out.println("\t" + v);
        }
        else
            out.println("\tNo values supplied");
    }

    private static void showItems (final PrintStream out, final int vStart, final int vEnd)
    {
        out.println("showItems(out," + vStart + "-" + vEnd);
        for (int v=vStart; v <= vEnd; v++)
            out.println("\t" + v);
    }

    public static final void testVarargsCall (final PrintStream out)
    {
        showItems(out);
        showItems(out, 1);
        showItems(out, 1, 10);
        showItems(out, new int[] { 1, 10});
    }

    //////////////////////////////////////////////////////////////////////////

    private static final int    ZERO=0;
    public static final void testZeroDoubleValues (final PrintStream out)
    {
        final String[]    values={ "0", "+0.0d", "-0.0d" };
        for (final String v : values) {
            final double    d=Double.parseDouble(v);
            final long        dBits=Double.doubleToRawLongBits(d);
            out.append('\t')
               .append(v)
               .append(" => ")
               .append(String.valueOf(d))
               .append(" [0x")
               .append(Long.toHexString(dBits))
               .append("] zero=")
                .append(String.valueOf(Double.valueOf(d).intValue() == ZERO))
             .println()
             ;
        }
    }

    //////////////////////////////////////////////////////////////////////////

    public static final void testStringInternBehavior (final PrintStream out, final BufferedReader in, final String ... args)
    {
        {
            String    s1="abc", s2="abc";
            out.println("Direct check: " + (s1 == s2));
        }

        {
            String    s1="abc" + "123";
            out.println("Concatenated check: " + (s1 == "abc123"));
        }

        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    orgStr=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "string to be compared to 'abc' (or Quit)");
            if ((null == orgStr) || (orgStr.length() <= 0))
                continue;
            if (isQuit(orgStr))
                break;

            out.println("\t" + orgStr + " => " + ("abc" == orgStr));
        }
    }

    //////////////////////////////////////////////////////////////////////////

    private static final void testStringSplitter (final PrintStream out, final BufferedReader in, final String orgStr)
    {
        for ( ; ; )
        {
            final String    exp=getval(out, in, orgStr + " split expresssion (or Quit)");
            if ((null == exp) || (exp.length() <= 0))
                continue;
            if (isQuit(exp))
                break;

            final String[]    res=orgStr.split(exp);
            if ((res != null) && (res.length > 0))
            {
                for (final String c : res)
                    out.println("\t" + c);
            }
            else
                out.println("\tNo split results");
        }
    }

    // args[i] string(s) to be split
    public static final void testStringSplitting (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    orgStr=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "string to be split (or Quit)");
            if ((null == orgStr) || (orgStr.length() <= 0))
                continue;
            if (isQuit(orgStr))
                break;
            testStringSplitter(out, in, orgStr);
        }
    }

    //////////////////////////////////////////////////////////////////////////

    // args[i] DecimalFormat(s) to be tested
    public static final void testNumberFormatting (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    fmt=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "Decimal format (or Quit)");
            if ((null == fmt) || (fmt.length() <= 0))
                continue;
            if (isQuit(fmt))
                break;

            final NumberFormat    nf;
            try
            {
                nf = new DecimalFormat(fmt);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while build formatter: " + e.getMessage());
                continue;
            }

            for ( ; ; )
            {
                final String    ans=getval(out, in, "(P)arse/[F]ormat/(Q)uit");
                if (isQuit(ans)) break;
                final char    op=((null == ans) || (ans.length() <= 0)) ? '\0' : Character.toLowerCase(ans.charAt(0));

                final String    num=getval(out, in, "Value[" + fmt + "] to parse/format (or Quit)");
                if ((null == num) || (num.length() <= 0))
                    continue;
                if (isQuit(num)) break;

                try
                {
                    switch(op)
                    {
                        case 'p'    :
                            {
                                final Number    n=nf.parse(num);
                                if (null == n)
                                    throw new NoSuchElementException("No instance returned");

                                System.out.println("\t" + n.getClass().getSimpleName() + "[" + n + "]");
                            }
                            break;

                        case 'f'    :
                        case '\0'    :
                            {
                                final Number    n;
                                if (num.indexOf('.') < 0)
                                    n = Integer.valueOf(num);
                                else
                                    n = Float.valueOf(num);

                                final String    s=nf.format(n);
                                System.out.println("\t" + n.getClass().getSimpleName() + "[" + s + "]");
                            }
                            break;

                        default        :
                    }
                }
                catch(Exception e)
                {
                    System.err.println(e.getClass().getName() + " while handling: " + e.getMessage());
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////

    private static final void testPropertiesResolver (final PrintStream out, final BufferedReader in, final PropertiesResolver pres, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    fmt=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "Properties format (or Quit)");
            if ((null == fmt) || (fmt.length() <= 0))
                continue;
            if (isQuit(fmt))
                break;

            try
            {
                final String    res=pres.format(fmt);
                out.println("\t" + fmt + " => " + res);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while handling fmt=" + fmt + ": " + e.getMessage());
            }
        }
    }
    // args[i] a properties expression
    public static final void testPropertiesResolver (final PrintStream out, final BufferedReader in, final String ... args)
    {
        testPropertiesResolver(out, in, SystemPropertiesResolver.SYSTEM, args);
    }

    //////////////////////////////////////////////////////////////////////////

    // each argument is a timestamp value-e.g., 1023654125S (for seconds) or L (for msec)
    public static final int testTimestampConversion (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final DateFormat    f=DateFormat.getDateTimeInstance();
        final int            numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    ans=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "timestamp value (use 'S' for seconds and L for msec. - or Quit)");
            final int        ansLen=(null == ans) ? 0 : ans.length();
            if (ansLen <= 0)
                continue;
            if (isQuit(ans)) break;

            String    val=ans;
            char    uChar=ans.charAt(ansLen-1);
            if ((uChar != 's') && (uChar != 'S')
             && (uChar != 'l') && (uChar != 'L'))
            {
                final String    uVal=getval(out, in, "[L]ong(msec.)/(S)econds/(Q)uit");
                uChar = ((null == uVal) || (uVal.length() <= 0)) ? 'L' : uVal.charAt(0);
                if (isQuit(uVal)) break;
            }
            else
                val = ans.substring(0, ansLen-1);

            try
            {
                final long    v=Long.parseLong(val), t;
                if (('s' == uChar) || ('S' == uChar))
                    t = TimeUnits.SECOND.getMilisecondValue(v);
                else if (('l' == uChar) || ('L' == uChar))
                    t = v;
                else
                    throw new UnsupportedOperationException("Unknown time unit: " + String.valueOf(uChar));

                final Date        d=new Date(t);
                final String    s=f.format(d);
                out.println("\t" + val + "[" + String.valueOf(uChar) + "]: " + s);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    private static enum Coin {
        PENNY   { @Override int value() { return  1; } },
        NICKEL  { @Override int value() { return  5; } },
        DIME    { @Override int value() { return 10; } },
        QUARTER { @Override int value() { return 25; } };

        abstract int value();
        /*
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString ()
        {
            return super.toString() + "[" + value() + "]";
        }

    }

    private static enum Egg {
        LARGE,    // uses default value
        EXTRALARGE {
            @Override public double recipeEquivalent() { return 24.0 / 27.0; }
        },
        JUMBO {
            @Override public double recipeEquivalent(){ return 24.0 / 30.0; }
        };
        // default value
        public double recipeEquivalent() { return 1.0; }
        /*
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString ()
        {
            return super.toString() + "[" + recipeEquivalent() + "]";
        }
    }

    // arguments are ignored
    public static final int testEnumsCompatibility (final PrintStream out, final BufferedReader in, final String ... args)
    {
        if ((null == in) || (null == out))
            return (-1);

        if ((null == args) || (args.length >= 0))
        {
            checkEnumCompatibility(out, Coin.class, Coin.PENNY, Coin.DIME);
            checkEnumCompatibility(out, Egg.class, Egg.LARGE, Egg.EXTRALARGE);
            checkEnumCompatibility(out, Egg.class, Egg.JUMBO, Egg.EXTRALARGE);
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void addObject (PrintStream out, Collection c, Class<?> objType, String value)
    {
        try
        {
            final ValueStringInstantiator<? super Object>     vsi=
                (ValueStringInstantiator<? super Object>) ClassUtil.getAtomicStringInstantiator(objType);
            final Object                                    o=vsi.newInstance(value);
            c.add(o);
            out.println("add(" + objType.getSimpleName() + ")[" + value + "] done");
        }
        catch(Exception e)
        {
            out.println("add(" + objType.getSimpleName() + ")[" + value + "] " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private static class Strings2 extends AbstractCollection<String> {
        private final Collection<Object>    _c=new LinkedList<Object>();
        public Strings2 ()
        {
            super();
        }
        /*
         * @see java.util.AbstractCollection#add(java.lang.Object)
         */
        @Override
        public boolean add (String e)
        {
            return _c.add(e);
        }
        /*
         * @see java.util.AbstractCollection#iterator()
         */
        @Override
        public Iterator<String> iterator ()
        {
            return null;
        }
        /*
         * @see java.util.AbstractCollection#size()
         */
        @Override
        public int size ()
        {
            return _c.size();
        }
    }

    private static class Strings1 extends LinkedList<String> {
        /**
         *
         */
        private static final long serialVersionUID = 1925603783196637653L;

        public Strings1 ()
        {
            super();
        }
    }
    // arguments are ignored
    public static final int testGenericsCompatibility (final PrintStream out, final BufferedReader in, final String ... args)
    {
        if ((null == out) || (null == in))
            return (-1);

        if ((args != null) && (args.length < 0))
            return (-2);

        final Strings1                l1=new Strings1();
        final Strings2                l2=new Strings2();
        final Collection<String>    l3=new LinkedList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = 3485888376108085687L;

            /*
             * @see java.util.LinkedList#add(java.lang.Object)
             */
            @Override
            public boolean add (String e)
            {
                return super.add(e);
            }

        };
        final Collection<?>[]    cl={ l1, l2, l3 };
        for (final Collection<?> l : cl)
        {
            /*
            addObject(out, l, String.class, "hello");
            addObject(out, l, Integer.class, "3");
            */

            final Class<?>    c=l.getClass();
            out.println(c.getName());

            final Method[]    ma=c.getMethods();
            for (final Method m : ma)
            {
                final String    n=(null == m) ? null : m.getName();
                if ("add".equalsIgnoreCase(n))
                    out.println("\t" + m);
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    // each argument is appended to the StringBuilder and then checked against the backing value
    public static final int testStringBuilderBackingArray (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int            numArgs=(null == args) ? 0 : args.length;
        final StringBuilder    sb=new StringBuilder(16);
        for (int    aIndex=0; ; aIndex++)
        {
            final String    ans=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "test string value (or Quit)");
            final int        ansLen=(null == ans) ? 0 : ans.length();
            if (ansLen <= 0)
                continue;
            if (isQuit(ans)) break;

            sb.setLength(0);
            sb.append(ans);

            final char[]    a=StringUtil.getBackingArray(sb);
            final int        aLen=(null == a) ? 0 : a.length;
            out.println("\t" + ans + ": length=" + ansLen + ";sbLen=" + sb.length() + ";valsLen=" + aLen);
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    // each argument is appended to the StringBuilder and then checked against the backing value
    public static final int testShowArguments (final PrintStream out, final BufferedReader in, final String ... args)
    {
        if ((null == args) || (args.length <= 0) || (null == in))
            return 0;

        for (final String a : args)
            out.println("\t\"" + a + "\"");
        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    // each 2 arguments are considered versions to be compared
    public static final int testVersionComparison (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    v1=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "version #1 (or Quit)");
            if ((null == v1) || (v1.length() <= 0))
                continue;
            if (isQuit(v1)) break;

            aIndex++;
            final String    v2=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "version #2 (or Quit)");
            if ((null == v2) || (v2.length() <= 0))
                continue;
            if (isQuit(v2)) break;

            try
            {
                final int    nRes=VersionComparator.ASCENDING.compare(v1, v2);
                out.println("\t" + v1 + " (" + nRes + ") " + v2);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    // each argument is a ';' separate list of StackTraceElement(s) strings
    public static final int testStackTraceElementsParsing (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    s=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "trace elements list (or Quit)");
            if ((null == s) || (s.length() <= 0))
                continue;
            if (isQuit(s)) break;

            try
            {
                final Collection<? extends StackTraceElement>    el=ExceptionUtil.fromString(s, ';');
                out.println(s);
                if ((null == el) || (el.size() <= 0))
                {
                    out.println("\tNo elements recovered");
                    continue;
                }

                for (final StackTraceElement ste : el)
                    out.println("\t" + ste);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    // args[0]=expression, args[1,2,...]=versions to compare
    public static final int testVersionCompatibility (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final String[]    tstArgs=resolveTestParameters(out, in, args, "requirement");
        if ((null == tstArgs) || (tstArgs.length < 0))
            return 0;

        final String    req=tstArgs[0];
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=1; ; aIndex++)
        {
            final String    ver=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "version (or Quit)");
            if ((null == ver) || (ver.length() <= 0))
                continue;
            if (isQuit(ver)) break;

            try
            {
                final int    nRes=VersionComparator.compareVersionCompatibility(ver, req);
                out.println("\t" + ver + " (" + nRes + ") " + req);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static final int testRegexPatterns (
            final PrintStream out, final BufferedReader in,
            final Pattern p, final int sIndex, final String ... args)
    {
        final int        numArgs=(null == args) ? 0 : args.length;
        final String    ps=p.pattern();
        for (int    aIndex=sIndex; ; aIndex++)
        {
            final String    v=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "value to match (or Quit)");
            if ((null == v) || (v.length() <= 0))
                continue;
            if (isQuit(v)) break;

            final Matcher    m=p.matcher(v);
            final boolean    mv=(m != null) && m.matches();
            out.append('\t')
               .append(ps)
               .append('[').append(v).append(']')
               .append(" match=")
               .append(String.valueOf(mv))
               .println()
               ;
        }

        return 0;
    }

    // args[0]=pattern, args[1,2,...]=strings to compare
    public static final int testRegexPatterns (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            String    pat=null;
            for ( ; (null == pat) || (pat.length() <= 0) ; aIndex++)
                pat = (aIndex < numArgs) ? args[aIndex] : getval(out, in, "pattern (or Quit)");
            if (isQuit(pat)) break;

            try
            {
                testRegexPatterns(out, in, Pattern.compile(pat), aIndex, args);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    // args[i]=comma separated value(s)
    public static final int testLabeledContinue (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    v=
                (aIndex < numArgs) ? args[aIndex] : getval(out, in, "values list (or Quit)");
            if (isQuit(v)) break;

            final Collection<String>    vl=StringUtil.splitString(v, ',');
            if ((null == vl) || (vl.size() <= 0))
                continue;

            LBL:
            for (final String vs : vl)
            {
                final String    ans=getval(out, in, "jump at value=" + vs + " - y/[n]/q");
                if (isQuit(ans)) break;

                if ((ans != null) && (ans.length() > 0) && ('y' == Character.toLowerCase(ans.charAt(0))))
                    continue LBL;
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static final int testGetterInvocation (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        final Collection<String>    vl;
        if ((null == args) || (args.length <= 0))
            vl = Collections.emptyList();
        else
            vl = Arrays.asList(args);

        try
        {
            final Method    m=Collection.class.getDeclaredMethod("size");
            for (Integer    numChecks=Integer.valueOf(100000000); ; )
            {
                final String    ans=getval(out, in, "[D]irect/(E)mpty array/No (V)arargs/(N)ull varargs/(Q)uit");
                if (isQuit(ans)) break;
                final char        op=
                    ((null == ans) || (ans.length() <= 0)) ? '\0' : Character.toUpperCase(ans.charAt(0));

                if (null == (numChecks=inputIntValue(out, in, "Number of checks", Byte.MAX_VALUE, Integer.MAX_VALUE, numChecks)))
                    break;

                final long    tStart=System.currentTimeMillis();
                switch(op)
                {
                    case '\0'    :
                    case 'D'    :
                        for (int    i=0; i < numChecks.intValue(); i++)
                            vl.size();
                        break;
                    case 'E'    :
                        for (int    i=0; i < numChecks.intValue(); i++)
                            m.invoke(vl, AttributeAccessor.EMPTY_OBJECTS_ARRAY);
                        break;
                    case 'V'    :
                        for (int    i=0; i < numChecks.intValue(); i++)
                            m.invoke(vl);
                        break;
                    case 'N'    :
                        for (int    i=0; i < numChecks.intValue(); i++)
                            m.invoke(vl, (Object[]) null);
                        break;
                    default        :    // do nothing
                }

                final long    tEnd=System.currentTimeMillis(), tDuration=tEnd - tStart;
                out.println("\tExecuted " + numChecks + " iterations in " + tDuration + " msec. (" + ((double) tDuration / (double) numChecks.longValue()) + " msec/iteration)");
            }

            return 0;
        }
        catch(Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return (-1);
        }
    }

    //////////////////////////////////////////////////////////////////////////
    // args[i] name(s) of variables whose value to display
    public static final int testShowEnvironment (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        final Comparator<Map.Entry<String,String>>    evComp=
            new Comparator<Map.Entry<String,String>> () {
                @Override
                public int compare (Map.Entry<String,String> p1, Map.Entry<String,String> p2)
                {
                    final String    n1=(null == p1) ? null : p1.getKey(),
                                    n2=(null == p2) ? null : p2.getKey();
                    return StringUtil.compareDataStrings(n1, n2, false);
                }
        };
        for (int    aIndex=0; ; aIndex++)
        {
            final String    varName=
                (aIndex < numArgs) ? args[aIndex] : getval(out, in, "variable name/(Q)uit/ENTER=list");
            if (isQuit(varName))
                break;

            if ((null == varName) || (varName.length() <= 0))
            {
                final Map<String,String>                                eMap=
                    System.getenv();
                final Collection<? extends Map.Entry<String,String>>    epl=
                    ((null == eMap) || (eMap.size() <= 0)) ? null : eMap.entrySet();
                final int                                                numPairs=
                    (null == epl) ? 0 : epl.size();
                final List<Map.Entry<String,String>>                    spl=
                    (numPairs <= 0) ? null : new ArrayList<Map.Entry<String,String>>(epl);
                if (numPairs > 1)
                    Collections.sort(spl, evComp);
                if ((spl != null) && (spl.size() > 0))
                {
                    for (final Map.Entry<String,String>    evp : spl)
                    {
                        final String    vName=(null == evp) ? null : evp.getKey(),
                                        varVal=(null == evp) ? null : evp.getValue();
                        out.append('\t').append(vName).append('=').append(varVal).println();
                    }
                }
                else
                    out.println("No environment variables found");
            }
            else
            {
                final String    varVal=System.getenv(varName);
                out.append('\t').append(varName).append('=').append(varVal).println();
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////
    // args[i] values to be tested
    public static final int testReplaceAll (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    orgValue=
                (aIndex < numArgs) ? args[aIndex] : getval(out, in, "value/(Q)uit");
            if ((null == orgValue) || (orgValue.length() <= 0))
                continue;
            if (isQuit(orgValue))
                break;

            final String    repValue=orgValue.replace("'", "''");
            out.append(orgValue)
               .append(" => ")
               .append(repValue)
               .println()
               ;
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static final <A extends Annotation> void testPackageLevelAnnotations (
            final PrintStream out, final BufferedReader in,
            final Class<A> ac, final Class<?> ... classes)
    {
        for (int    aIndex=0; aIndex <= Short.MAX_VALUE; aIndex++)
        {
            for (final Class<?> c : classes)
            {
                out.append(c.getName()).println();

                final Package    p=c.getPackage();
                final A            pv=p.getAnnotation(ac);
                out.append('\t')
                   .append(p.getName())
                   .append((pv != null) ? " contains " : " does NOT contain ")
                   .append(ac.getName())
                   .append(" annotation")
                   .println()
                   ;
                final A av=c.getAnnotation(ac);
                out.append('\t')
                   .append(c.getSimpleName())
                   .append((av != null) ? " contains " : " does NOT contain ")
                   .append(ac.getName())
                   .append(" annotation")
                   .println()
                   ;
            }

            final String    ans=getval(out, in, "again [y]/n");
            if ((ans != null) && (ans.length() > 0)
             && ('y' != Character.toLowerCase(ans.charAt(0))))
                break;
        }
    }

    //////////////////////////////////////////////////////////////////////////

    // args[i] initial population values
    public static final void testBlockingQueueBehavior (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        final BlockingQueue<String>    q=new LinkedBlockingQueue<String>(10);
        final int                    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; aIndex < numArgs; aIndex++)
        {
            final String    v=args[aIndex];
            if (!q.offer(v))
                System.err.append("Failed to offer value=").append(v).println();
        }

        for (int    aIndex=0; aIndex <= Short.MAX_VALUE ; aIndex++)
        {
            out.append("Q size=")
               .append(String.valueOf(q.size()))
               .append(" remaining=")
               .append(String.valueOf(q.remainingCapacity()))
               .println()
               ;

            final String    opValue=getval(out, in, "[O]ffer/(T)ake/(Q)uit");
            if (isQuit(opValue))
                break;

            final char    opChar=
                ((null == opValue) || (opValue.length() <= 0)) ? '\0' : opValue.charAt(0);
            switch(opChar)
            {
                case '\0'    :
                case 'O'    :
                case 'o'    :
                    if (!q.offer(String.valueOf(System.currentTimeMillis())))
                        System.err.println("Failed to offer value");
                    break;

                case 'T'    :
                case 't'    :
                    try
                    {
                        final String    v=q.take();
                        out.append('\t').append(v).println();
                    }
                    catch(InterruptedException e)
                    {
                        System.err.println("Interruped");
                    }
                    break;
                default        : // do nothing
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////

    // see SprintUtilsTester for test with same name but different implementation
    public static final void testInterfacesInheritance (
            final PrintStream out, final BufferedReader in, final Object o)
    {
        final Class<?>        c=o.getClass();
        final Class<?>[]    ifca=c.getInterfaces();
        for ( ; ; )
        {
            out.append(c.getName()).append(" interfaces:").println();
            out.println("Direct:");
            for (final Class<?> ifc : ifca)
                out.append('\t').append(ifc.getSimpleName()).println();

            final String    ans=getval(out, in, "again [y]/n");
            if ((ans != null) && (ans.length() > 0) && (Character.toLowerCase(ans.charAt(0)) != 'y'))
                break;
        }
    }

    /* -------------------------------------------------------------------- */

    private static interface Ifc1 extends CharSequence, Serializable {
        void helloWorld ();
    }

    private static class Ifc1Impl implements Ifc1 {
        /**
         *
         */
        private static final long serialVersionUID = -2155464245766429848L;
        public Ifc1Impl ()
        {
            super();
        }
        /*
         * @see java.lang.CharSequence#length()
         */
        @Override
        public int length ()
        {
            return 0;
        }
        /*
         * @see java.lang.CharSequence#charAt(int)
         */
        @Override
        public char charAt (int index)
        {
            return '\0';
        }
        /*
         * @see java.lang.CharSequence#subSequence(int, int)
         */
        @Override
        public CharSequence subSequence (int start, int end)
        {
            return this;
        }
        /*
         * @see net.community.chest.test.LangTester.Ifc1#helloWorld()
         */
        @Override
        public void helloWorld ()
        {
            // do nothing
        }
    }

    /* -------------------------------------------------------------------- */

    // see SprintUtilsTester for test with same name but different implementation
    public static final void testInterfacesInheritance (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        testInterfacesInheritance(out, in, new Ifc1Impl());
    }

    //////////////////////////////////////////////////////////////////////////

    public static final void testDecimalFormatRounder (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        final DecimalFormat    formatter=new DecimalFormat("#.#");
        final int                    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    numVal=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "Double value (or Quit)");
            if ((numVal == null) || (numVal.length() <= 0))
                continue;
            if (isQuit(numVal)) break;

            try
            {
                final double    orgValue=Double.parseDouble(numVal);
                final String    fmtString=formatter.format(orgValue);
                final double    fmtValue=Double.parseDouble(fmtString);
                out.append("\t[").append(numVal)
                    .append("] ").append(String.valueOf(orgValue))
                    .append(" => [").append(fmtString)
                    .append("] ").append(String.valueOf(fmtValue))
                    .println()
                    ;
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////

    public static final void testNumericalValueOverflow (final PrintStream out)
    {
        for (byte    value=Byte.MAX_VALUE - 5; value >= 0 ; value++)
            out.println("\tByte: " + value);
        for (short    value=Short.MAX_VALUE - 5; value >= 0 ; value++)
            out.println("\tShort: " + value);
        for (int    value=Integer.MAX_VALUE - 5; value >= 0 ; value++)
            out.println("\tInteger: " + value);
        for (long    value=Long.MAX_VALUE - 5L; value >= 0L ; value++)
            out.println("\tLong: " + value);
    }

    //////////////////////////////////////////////////////////////////////////

    public static final void testURIData (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    argVal=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "URI (or Quit)");
            if ((argVal == null) || (argVal.length() <= 0))
                continue;
            if (isQuit(argVal)) break;
            try
            {
                final URI    uri=new URI(argVal);
                out.append('\t')
                   .append(argVal)
                   .append(" => ")
                   .append(uri.toString())
                   .println();
            }
            catch(Exception e)
            {
                System.err.println(argVal + " - failed (" + e.getClass().getName() + "): " + e.getMessage());
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////

    protected static final void testPreInitializedMap (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final Map<String,String>    TEST_MAP=Collections.unmodifiableMap(new TreeMap<String,String>() {
            private static final long serialVersionUID = -4924436669659053161L;

            {
                put("a", "b");
                put("1", "2");
            }
        });
        for (final Map.Entry<String,String> e : TEST_MAP.entrySet())
            out.append('\t').append(e.getKey()).append(" => ").append(e.getValue()).println();
    }

    //////////////////////////////////////////////////////////////////////////

    private static class NonStaticInitExample {
        {
            System.out.println("Constructing");
        }
        private final long    timestamp=System.nanoTime();
        private String    _value="1234";
        {
            System.out.append("Pre-initialized value=").append(_value).append(" at ").println(timestamp);
        }

        NonStaticInitExample (String val)
        {
            _value = val;
            System.out.append("Post-initialized value=").append(_value).append(" at ").println(timestamp);
        }

        NonStaticInitExample (long value)
        {
            this(String.valueOf(value));
        }

        NonStaticInitExample ()
        {
            this(System.currentTimeMillis());
        }
    }

    protected static final void testNonStaticInitializer (final PrintStream out, final BufferedReader in, final String ... args)
    {
        new NonStaticInitExample();
        new NonStaticInitExample(System.nanoTime());
        new NonStaticInitExample("Hello world");
    }

    //////////////////////////////////////////////////////////////////////////

    protected static final void testArraysCovariance (final PrintStream out, final BufferedReader in, final String ... args)
    {
        String[]        sa=new String[3];
        CharSequence[]    ca=sa;
        ca[0] = "abcdefg";    // should succeed
        System.out.println("ca[0]: " + sa[0]);

        Object[]        oa=sa;
        oa[0] = Integer.valueOf(1);    // should cause an exception
        System.out.println("oa[0]: " + sa[0]);
    }

    //////////////////////////////////////////////////////////////////////////

    protected static final void testInheritableThreadLocal (final PrintStream out, final BufferedReader in, final String ... args) throws InterruptedException
    {
        final InheritableThreadLocal<String>    thl=new InheritableThreadLocal<String>();
        Runnable    runner=new Runnable() {
                @Override
                public void run() {
                    Thread  t=Thread.currentThread();
                    synchronized(out) {
                        out.append(t.getName()).append(": start=").println(thl.get());
                    }

                    thl.set(UUID.randomUUID().toString());

                    for (int index=0; index < (Byte.SIZE / 2); index++) {
                        synchronized(out) {
                            out.append('\t').append(t.getName()).append(": ").println(thl.get());
                        }

                        try {
                            Thread.sleep(Byte.MAX_VALUE);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    synchronized(out) {
                        out.append(t.getName()).println(": end");
                    }
                }
            };
        thl.set(UUID.randomUUID().toString());

        Thread  t=new Thread(runner, "child");
        t.start();
        runner.run();

        t.join(TimeUnit.SECONDS.toMillis(5L));
        if (t.isAlive()) {
            throw new IllegalStateException("Child thread still running");
        }
    }

    //////////////////////////////////////////////////////////////////////////

    protected static final void testSpecialFilePath(PrintStream stdout, BufferedReader stdin, String filePath) throws IOException {
        File    file=new File(filePath);
        {
            System.out.append(File.class.getSimpleName()).append(' ').println(file.getAbsolutePath());
            System.out.append('\t').append("Exists: ").println(file.exists());
            System.out.append('\t').append("Directory: ").println(file.isDirectory());
            System.out.append('\t').append("File: ").println(file.isFile());
            System.out.append('\t').append("Hidden: ").println(file.isHidden());
        }

        Path    path=file.toPath();
        {
            LinkOption[]    options={ LinkOption.NOFOLLOW_LINKS };
            System.out.append(Path.class.getSimpleName()).append(' ').println(path.toString());
            System.out.append('\t').append("Exists: ").println(Files.exists(path, options));
            System.out.append('\t').append("Not Exists: ").println(Files.notExists(path, options));
            System.out.append('\t').append("Directory: ").println(Files.isDirectory(path, options));
            System.out.append('\t').append("Regular: ").println(Files.isRegularFile(path, options));
        }
    }

    protected static final void testSpecialFile(PrintStream stdout, BufferedReader stdin, String ... args) throws Exception {
        testSpecialFilePath(stdout, stdin, "C:" + File.separator + "Wanova Volume Information");
    }

    //////////////////////////////////////////////////////////////////////////

    static interface DiamondA {
        default void foo(PrintStream stdout) {
            stdout.println("DiamondA#foo()");
        }
    }

    static interface DiamondB extends DiamondA {
        @Override default void foo(PrintStream stdout) {
            stdout.println("DiamondB#foo()");
        }
    }

    static interface DiamondC extends DiamondA {
        default void bar(PrintStream stdout) {
            stdout.println("DiamondC#bar()");
        }
    }

    static class DiamondImpl implements DiamondC, DiamondB {
        DiamondImpl() {
            super();
        }
    }

    protected static final void testDiamondDefaultInterfaceMethods(PrintStream stdout, BufferedReader stdin, String ... args) throws Exception {
        DiamondA impl = new DiamondImpl();
        impl.foo(stdout);   // we expected DiamonB to be printed
    }

    //////////////////////////////////////////////////////////////////////////

    static enum TestEnum {
        Value0, Value1, Value2;

        private final String name;
        TestEnum() {
            name = name();
        }

        TestEnum(String name) {
            this.name = name;
        }

        @Override public String toString() { return name; }
    }

    protected static final void testCreateEnumValues(PrintStream stdout, BufferedReader stdin, String ... args) throws Exception {
        Constructor<?> ctor = null;
        for (Constructor<?> c : TestEnum.class.getDeclaredConstructors()) {
            Class<?>[] params = c.getParameterTypes();
            if ((params != null) && (params.length == 2)
             && (params[0] == String.class) && (params[1] == Integer.TYPE)) {
                ctor = c;
                break;
            }
        }

        if (ctor == null) {
            throw new IllegalStateException("Cannot find constructor");
        }
        ctor.setAccessible(true);

        int numArgs = (args == null) ? 0 : args.length;
        for (int index = 0; ; index++) {
            String argVal = (index < numArgs) ? args[index] : getval(stdout, stdin, "value (or Quit)");
            if (argVal.length() <= 0) {
                continue;
            }
            if (isQuit(argVal)) {
                break;
            }

            try {
                TestEnum[] values = TestEnum.values();
                Object v = ctor.newInstance(argVal, values.length + index);
                System.out.append('\t').append(String.valueOf(((Enum<?>) v).ordinal())).append(": ").println(v);
            } catch(Exception e) {
                System.err.append(e.getClass().getSimpleName()).append(": ").println(e.getMessage());
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////
    // where key size is assumed to be power of 2 - 512, 1024, etc...
    public static boolean isDHGroupExchangeSupported(int maxKeySize) {
            BigInteger r = new BigInteger("0").setBit(maxKeySize - 1);
            DHParameterSpec dhSkipParamSpec = new DHParameterSpec(r, r);
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
                kpg.initialize(dhSkipParamSpec);
                return true;
            } catch(Throwable t) {
                return false;
            }
    }

    protected static final void testDHGEX(PrintStream stdout, BufferedReader stdin, String ... args) throws Exception {
        // now let's test it
        for (int keySize : new int[]{512, 1024, 2048, 3072, 4096, 8192}) {
           boolean supported = isDHGroupExchangeSupported(keySize);
           stdout.println("Key size " + keySize + " supported=" + supported);
        }
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args) throws Exception
    {
        final BufferedReader    in=getStdin();
//        final CharSequence        cs=(System.currentTimeMillis() > 1234L) ? "abcd" : new StringBuilder(10);
//        final CharSequence        cs=(System.currentTimeMillis() > 1234L) ? String.valueOf(3) : new StringBuilder(10);
//        final List<String>        l=(System.currentTimeMillis() > 1234L) ?  Collections.emptyList() : new ArrayList<String>();
//        final List<String>        l=(System.currentTimeMillis() > 1234L) ? new ArrayList<String>() : Collections.emptyList();
//        final List<String>        l;
//        if (System.currentTimeMillis() > 1234L)
//            l = Collections.emptyList();
//        else
//            l = new ArrayList<String>();
//        final List<String>        l=(System.currentTimeMillis() > 1234L) ? new ArrayList<String>() : null;
//        final Number    n=(System.currentTimeMillis() > 1234L) ? Double.valueOf(args[0]) : Integer.valueOf(args[0]);
//        final Number    n=(System.currentTimeMillis() > 1234L) ? Integer.valueOf(args[0]) : Double.valueOf(args[0]);
//        testVarargsCall(System.out);
//        testStringSplitting(System.out, in, args);
//        testNumberFormatting(System.out, in, args);
//        testPropertiesResolver(System.out, in, args);
//        testTimestampConversion(System.out, in, args);
//        testEnumsCompatibility(System.out, in, args);
//        testGenericsCompatibility(System.out, in, args);
//        testStringBuilderBackingArray(System.out, in, args);
//        testShowArguments(System.out, in, args);
//        testVersionComparison(System.out, in, args);
//        testStackTraceElementsParsing(System.out, in, args);
//        testVersionCompatibility(System.out, in, args);
//        testRegexPatterns(System.out, in, args);
//        testLabeledContinue(System.out, in, args);
//        testGetterInvocation(System.out, in, args);
//        testShowEnvironment(System.out, in, args);
//        testReplaceAll(System.out, in, args);
//        testPackageLevelAnnotations(System.out, in, TestAnnotation.class, LangTester.class, InsecureAccess.class);
//        testBlockingQueueBehavior(System.out, in, args);
//        testInterfacesInheritance(System.out, in, args);
//        testDecimalFormatRounder(System.out, in, args);
//        testZeroDoubleValues(System.out);
//        testStringInternBehavior(System.out, in, args);
//        testNumericalValueOverflow(System.out);
//        testURIData(System.out, in, args);
//        testPreInitializedMap(System.out, in, args);
//        testNonStaticInitializer(System.out, in, args);
//        testArraysCovariance(System.out, in, args);
//        testInheritableThreadLocal(System.out, in, args);
//        testSpecialFile(System.out, in, args);
//        testDiamondDefaultInterfaceMethods(System.out, in, args);
        testDHGEX(System.out, in, args);
//      testCreateEnumValues(System.out, in, args);
    }
}
