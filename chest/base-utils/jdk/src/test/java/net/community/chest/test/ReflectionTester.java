package net.community.chest.test;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.CoVariantReturn;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.reflect.AttributeMethodType;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.reflect.FieldsAccessor;
import net.community.chest.reflect.MethodUtil;
import net.community.chest.reflect.StringInstantiatorsMap;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Nov 12, 2007 1:26:49 PM
 */
public final class ReflectionTester extends TestBase {
    private ReflectionTester ()
    {
        // no instance
    }

    public void invokeTest ()
    {
        // do nothing
    }

    //////////////////////////////////////////////////////////////////////////

    private static final int testAttributesExtraction (final PrintStream out, final BufferedReader in, final Class<?> c)
    {
        for ( ; ; )
        {
            final Map<String,AttributeAccessor>    attrsMap=
                AttributeMethodType.getAllAccessibleAttributes(c);
            final Collection<? extends AttributeAccessor>    attrs=
                ((null == attrsMap) || (attrsMap.size() <= 0)) ? null : attrsMap.values();
            if ((attrs != null) && (attrs.size() > 0))
            {
                out.println(c.getName() + " attributes:");
                for (final AttributeAccessor a : attrs)
                    out.println("\t" + a);
            }
            else    // unlikely (there is always "getClass")
                out.println(c.getName() + " has no accessible attributes");

            final String    ans=getval(out, in, "again [y]/n");
            if ((ans != null) && (ans.length() > 0) && (Character.toLowerCase(ans.charAt(0)) != 'y'))
                return 0;
        }
    }
    // each argument is the (fully-qualified) class name
    public static final int testAttributesExtraction (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ; aIndex++)
        {
            final String    clsName=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "class name (or Quit)");
            if ((null == clsName) || (clsName.length() <= 0))
                continue;
            if (isQuit(clsName))
                break;

            try
            {
                testAttributesExtraction(out, in, ClassUtil.loadClassByName(clsName));
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while handling class=" + clsName + ": " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static class BaseFoo {
        public BaseFoo ()
        {
            super();
        }

        public Serializable getSeq ()
        {
            return Byte.valueOf((byte) 0);
        }
    }

    public static class Foo extends BaseFoo {
        public Foo ()
        {
            super();
        }
        /*
         * @see net.community.chest.test.ReflectionTester.BaseFoo#getSeq()
         */
        @Override
        public Number /* co-variant return */ getSeq ()
        {
            return Short.valueOf((short) 1);
        }
    }

    private static class Bar extends Foo {
        public Bar ()
        {
            super();
        }
        /*
         * @see net.community.chest.test.ReflectionTester.Foo#getSeq()
         */
        @Override
        public Integer /* co-variant return */ getSeq ()
        {
            return Integer.valueOf(2);
        }
    }

    private static Method getSeqMethod (final PrintStream out, final Class<?> c) throws Exception
    {
        final Method[]    ma=c.getMethods();
        for (final Method m : ma)
        {
            if ("getSeq".equals(m.getName()))
                out.println("\t" + c.getName() + "[" + m + "]");

        }
        return c.getMethod("getSeq");
    }
    // attributes are ignored
    public static final int testCovariantReturns (final PrintStream out, final BufferedReader in, final String ... args)
    {
        try
        {
            final Method    gBase=getSeqMethod(out, BaseFoo.class),
                            gFoo=getSeqMethod(out, Foo.class),
                            gBar=getSeqMethod(out, Bar.class);

            {
                final Foo    f=new Foo();
                out.println(gFoo.invoke(f)); /* Obvious */
//                out.println(gBar.invoke(f));    // java.lang.IllegalArgumentException: object is not an instance of declaring class
                out.println(gBase.invoke(f));
            }

            {
                final Bar    b=new Bar();
                out.println(gFoo.invoke(b));
                out.println(gBar.invoke(b)); /* Obvious */
                out.println(gBase.invoke(b));
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

    private static interface Intfc<V> {
        void printValue (V v);
    }

    private abstract static class BaseIntfc<V> extends BaseTypedValuesContainer<V> implements Intfc<V> {
        protected BaseIntfc (Class<V> objClass) throws IllegalArgumentException
        {
            super(objClass);
        }
    }

    private abstract static class NumberIntfc<N extends Number & Comparable<N>> extends BaseIntfc<N> {
        protected NumberIntfc (Class<N> numClass)
        {
            super(numClass);
        }
    }

    private static class IntegerIntfc extends NumberIntfc<Integer> {
        public IntegerIntfc ()
        {
            super(Integer.class);
        }
        /*
         * @see net.community.chest.test.ReflectionTester.Intfc#printValue(java.lang.Object)
         */
        @Override
        public void printValue (Integer v)
        {
            System.out.println(getValuesClass().getSimpleName() + "[" + v + "]");
        }
    }

    private static class FloatIntfc extends NumberIntfc<Float> {
        public FloatIntfc ()
        {
            super(Float.class);
        }
        /*
         * @see net.community.chest.test.ReflectionTester.Intfc#printValue(java.lang.Object)
         */
        @Override
        public void printValue (Float v)
        {
            System.out.println(getValuesClass().getSimpleName() + "[" + v + "]");
        }
    }

    public static void showClassInfo (final PrintStream out, final Class<?> ic) throws Exception
    {
        final TypeVariable<?>[]    gTypes=ic.getTypeParameters();
        if ((gTypes != null) && (gTypes.length > 0))
        {
            for (final TypeVariable<?> tv : gTypes)
            {
                final Type[]    b=tv.getBounds();
                out.print(ic.getSimpleName() + "<" + tv.getName());
                if ((b != null) && (b.length > 0))
                {
                    for (final Type t : b)
                        out.print(" " + t);
                }
                out.println(">");
            }
        }
        else
            out.println(ic.getSimpleName());

    }

    public static void showMethodInfo (final PrintStream out, final Method m)
    {
        out.println(m);

        final Class<?>[]    params=m.getParameterTypes();
        if ((params != null) && (params.length > 0))
        {
            out.print("\t(");
            for (final Class<?> p : params)
                out.print(" " + p.getSimpleName());
            out.println(" )");
        }

        final Type        rt=m.getGenericReturnType();
        final Type[]    ta=m.getGenericParameterTypes();
        if ((ta != null) && (ta.length > 0))
        {
            out.print("\t<");
            for (final Type t : ta)
                out.print(" " + t);
            out.println(" >");
        }
        out.println("\t=> " + rt);
    }
    // arguments are ignored
    public static final int testGenericTypes (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final Intfc<?>[]    vals={
                new FloatIntfc(),
                new IntegerIntfc()
        };

        for (final Intfc<?> i : vals)
        {
            try
            {
                final Class<?>    ic=i.getClass();
                showClassInfo(out, ic);

                {
                    final Method[]    ma=ic.getMethods();
                    final Number[]    nums={ Integer.valueOf(1), Float.valueOf(2.0f) };
                    for (final Method m : ma)
                    {
                        if ("printValue".equals(m.getName()))
                        {
                            showMethodInfo(out, m);
                            for (final Number n : nums)
                            {
                                try
                                {
                                    m.invoke(i, n);
                                }
                                catch(Exception e)
                                {
                                    System.err.println("showIntfcData(" + i + "):  " + e.getClass().getName() + " on invoke " + m + "[" + n + "]: " + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
            catch(Exception e)
            {
                System.err.println("showIntfcData(" + i + "):  " + e.getClass().getName() + ": " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    private static class BaseAttr {
        private Number    _x;
        public BaseAttr (Number x)
        {
            _x = x;
        }

        public BaseAttr ()
        {
            this(Short.valueOf((short) 0));
        }

        public Number getX () { return _x; }
        public void setX (Number x) { _x = x; }
        /*
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString ()
        {
            final Number    x=getX();
            final Class<?>    c=(null == x) ? null : x.getClass();
            return getClass().getSimpleName() + "[" + ((null == c) ? null : c.getSimpleName()) +  "]=" + getX();
        }
    }

    public static class IntAttr extends BaseAttr {
        public IntAttr ()
        {
            super(Integer.valueOf(1));
        }

        @Override
        @CoVariantReturn
        public Integer getX ()
        {
            final Number    n=super.getX();
            if (null == n)
                return null;

            return Integer.valueOf(n.intValue() + 10);
        }

        public void setX (Integer n)
        {
            super.setX((null == n) ? null : Integer.valueOf(n.intValue() - 10));
        }
    }

    private static final void showCovariantAttr (final PrintStream out, final BaseAttr a)
    {
        final Class<?>    c=a.getClass();
        out.println(c.getSimpleName() + ": " + a);
        try
        {
            final Method[]    ma=c.getMethods();
            for (final Method m : ma)
            {
                final String    mName=(null == m) ? null : m.getName();
                if ((null == mName) || (mName.length() <= 0))
                    continue;    // should not happen

                if (mName.equalsIgnoreCase("getX") || mName.equalsIgnoreCase("setX"))
                {
                    final Class<?>        rVal=m.getReturnType();
                    if (Void.class.isAssignableFrom(rVal) || Void.TYPE.isAssignableFrom(rVal))
                    {
                        final Class<?>[]    pars=m.getParameterTypes();
                        final Class<?>        aType=pars[0];
                        out.println("\t" + mName + "[" + aType.getSimpleName() + "]");
                    }
                    else
                        out.println("\t" + mName + "[" + rVal.getSimpleName() + "]");

                }
            }
        }
        catch(Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }
    // arguments are ignored
    public static final int testCovariantAttributes (final PrintStream out, final BufferedReader in, final String ... args)
    {
        showCovariantAttr(out, new BaseAttr());
        showCovariantAttr(out, new IntAttr());
        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    private static final void vShow (final PrintStream out, final String name, final String ... args)
    {
        if ((out != null) && (args != null) && (args.length > 0))
        {
            for (final String s : args)
                out.println(name + ": " + s);
        }
    }
    public static final void v1 (final PrintStream out, final String ... args)
    {
        vShow(out, "v1", args);
    }

    public static final void v2 (final PrintStream out, String[] args)
    {
        vShow(out, "v2", args);
    }

    public static final void testVarargsReflection (final PrintStream out, final BufferedReader in, final String ... args)
    {
        try
        {
            final Class<?>    c=LangTester.class;
            final Method[]    ma=c.getMethods();
            for (final Method m : ma)
            {
                final String    mName=m.getName();
                if ("v1".equalsIgnoreCase(mName)
                ||  "v2".equalsIgnoreCase(mName))
                {
                    out.println(mName + " properties (varargs=" + m.isVarArgs() + "):");

                    final Class<?>[]    params=m.getParameterTypes();
                    for (final Class<?> p : params)
                        out.println("\t" + p.getName());

                    final String    ans=getval(out, in, "invoke " + mName + " method ([y]/n)");
                    if ((null == ans) || (ans.length() <= 0) || ('y' == Character.toLowerCase(ans.charAt(0))))
                        m.invoke(null, out, args);
                }
            }
        }
        catch(Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    //////////////////////////////////////////////////////////////////////////

    // args[i]=number of iterations to run
    public static final void testReflectionPerformance (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final ReflectionTester    inst=new ReflectionTester();
        final Method            iMethod;
        try
        {
            final Class<?>    instClass=inst.getClass();
            if (null == (iMethod=instClass.getMethod("invokeTest")))
                throw new NoSuchElementException("No method found");
        }
        catch(Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return;
        }

        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ;aIndex++)
        {
            final String    ans=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "number of loops (or Quit)");
            if ((null == ans) || (ans.length() <= 0))
                continue;
            if (isQuit(ans)) break;

            try
            {
                final int    numInvokes=Integer.parseInt(ans);
                if (numInvokes <= 0)
                    throw new NumberFormatException("Illegal value requested");

                {
                    final long    refStart=System.currentTimeMillis();
                    for (int    i=0; i < numInvokes; i++)
                        iMethod.invoke(inst);
                    final long    refEnd=System.currentTimeMillis(), refDuration=refEnd - refStart;
                    out.println("\t" + numInvokes + " reflection API calls duration: " + refDuration + " msec.");
                }

                {
                    final long    dirStart=System.currentTimeMillis();
                    for (int    i=0; i < numInvokes; i++)
                        inst.invokeTest();
                    final long    dirEnd=System.currentTimeMillis(), dirDuration=dirEnd - dirStart;
                    out.println("\t" + numInvokes + " direct API calls duration: " + dirDuration + " msec.");
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////

    public static enum Days {
        SUNDAY {
            @SuppressWarnings("unused")
            public String getName ()     { return name(); }
        },
        MONDAY {
            @SuppressWarnings("unused")
            public int getIndex ()     { return 1; }
        },
        TUESDAY {
            @SuppressWarnings("unused")
            public int getValue ()     { return ordinal(); }
        };
    }

    // args[i]=number of iterations to run
    public static final int testEnumOverrides (final PrintStream out, final BufferedReader in, final String ... args)
    {
        checkEnumCompatibility(out, Days.class, Days.SUNDAY, Days.MONDAY);

        final String[]    names={ "Name", "Index", "Value" };
        for ( ; ; )
        {
            final String    ans=getval(out, in, "(S)un/(M)on/(T)ue/(Q)uit");
            if ((null == ans) || (ans.length() <= 0))
                continue;
            if (isQuit(ans)) break;

            final Days    d;
            final char    ch=Character.toUpperCase(ans.charAt(0));
            switch(ch)
            {
                case 'S'    : d = Days.SUNDAY; break;
                case 'M'    : d = Days.MONDAY; break;
                case 'T'    : d = Days.TUESDAY; break;
                default        : d = null;
            }
            if (null == d)
                continue;

            final Class<?>    c=d.getClass();
            for (final String n : names)
            {
                try
                {
                    final Method    m=c.getMethod("get" + n);
                    out.println("\t" + d + "[" + n + "}: " + m);
                }
                catch(Exception e)
                {
                    System.err.println(e.getClass().getName() + " on method=" + n + ": " + e.getMessage());
                }
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unchecked")
    public static final int testFieldsAccess (final PrintStream out, final BufferedReader in, final Object o, final Field f) throws Exception
    {
        if (!Modifier.isPublic(f.getModifiers()))
            f.setAccessible(true);

        final Class<?>                        fType=f.getType();
        final ValueStringInstantiator<?>    vsi=ClassUtil.isAtomicClass(fType)
            ? ClassUtil.getAtomicStringInstantiator(fType)
            : StringInstantiatorsMap.getDefaultInstance().get(fType)
            ;
        for ( ; ; )
        {
            final String    ans=getval(out, in, "[G]et/(S)et/(Q)uit");
            if (isQuit(ans)) break;

            final char    op=((null == ans) || (ans.length() <= 0)) ? '\0' : Character.toUpperCase(ans.charAt(0));
            try
            {
                switch(op)
                {
                    case '\0'    :
                    case 'G'    :
                        {
                            final Object    v=f.get(o);
                            final String    s=(null == vsi)
                                ? String.valueOf(v)
                                : ((ValueStringInstantiator<? super Object>) vsi).convertInstance(v)
                                ;
                            out.println("\t" + s);
                        }
                        break;

                    case 'S'    :
                        {
                            final String    s=getval(out, in, "value (or Quit)");
                            if (isQuit(s)) break;

                            final Object    v=(null == vsi) ? s : vsi.newInstance(s);
                            f.set(o, v);
                        }
                        break;

                    default    :    // do nothing
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }

        return 0;
    }

    @SuppressWarnings("unchecked")
    public static final int testFieldsAccess (final PrintStream out, final BufferedReader in, final Object o)
    {
        final Class<?>             c=o.getClass();
        final FieldsAccessor<?>    fa=new FieldsAccessor<Object>((Class<Object>) c);
        for ( ; ; )
        {
            final String    name=getval(out, in, c.getSimpleName() + " field name (or quit)");
            if ((null == name) || (name.length() <= 0))
                continue;
            if (isQuit(name)) break;

            final String    type=getval(out, in, "(D)eclared/[F]ind/(Q)uit");
            if (isQuit(type)) break;

            final char        ft=((null == type) || (type.length() <= 0)) ? '\0' : Character.toUpperCase(type.charAt(0));
            try
            {
                final Field        f;
                switch(ft)
                {
                    case '\0'    :
                    case 'F'    :
                        f = fa.getAccessible(name);
                        break;

                    case 'D'    :
                        f = c.getDeclaredField(name);
                        break;

                    case 'L'    :

                    default        :
                        f = null;
                }
                if (null == f)
                    throw new NoSuchFieldException("No matching field found");
                out.println("\t" + f);
                testFieldsAccess(out, in, o, f);
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " on field=" + name + ": " + e.getMessage());
            }
        }

        return 0;
    }

    // args[i]=fully qualified class name to test
    public static final int testFieldsAccess (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final int    numArgs=(null == args) ? 0 : args.length;
        for (int    aIndex=0; ;aIndex++)
        {
            final String    ans=(aIndex < numArgs) ? args[aIndex] : getval(out, in,  "fully qualified class name (or Quit)");
            if ((null == ans) || (ans.length() <= 0))
                continue;
            if (isQuit(ans)) break;

            try
            {
                final Class<?>    c=ClassUtil.loadClassByName(ans);
                testFieldsAccess(out, in, c.newInstance());
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " on class=" + ans + ": " + e.getMessage());
            }
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    public static class BaseVar {
        public BaseVar ()
        {
            super();
        }

        public CharSequence getValue ()
        {
            return getClass().getSimpleName();
        }

        public void setValue (String s)
        {
            System.out.println("setValue(" + s + ")");
        }
    }

    public static class DerivedVar extends BaseVar {
        public DerivedVar ()
        {
            super();
        }

        @Override
        @CoVariantReturn
        public String getValue ()
        {
            return "BAD";
        }

        public void setValue (CharSequence s)
        {
            super.setValue(s.toString());
        }
    }
    // arguments are ignored
    public static final int testCovariantJavaBeans (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final Class<?>    c=DerivedVar.class;
        final Method[]    ma=c.getMethods();
        for (final Method m : ma)
        {
            final String    n=m.getName();
            if ("getValue".equalsIgnoreCase(n) || "setValue".equalsIgnoreCase(n))
                out.println(m);
        }

        return 0;
    }

    //////////////////////////////////////////////////////////////////////////

    private static interface Xmpl {
        CharSequence getValue ();
    }

    private static abstract class XmplBase<R extends CharSequence & Appendable> implements Xmpl {
        protected XmplBase ()
        {
            super();
        }

        @Override
        public abstract R getValue ();
    }

    public static class XmplImpl extends XmplBase<StringBuilder> {
        public XmplImpl ()
        {
            super();
        }
        /*
         * @see net.community.chest.test.ReflectionTester.XmplBase#getValue()
         */
        @Override
        public StringBuilder getValue ()
        {
            return new StringBuilder(getClass().getName());
        }

    }
    // arguments are ignored
    public static final int testMixedCovariantReturn (final PrintStream out, final BufferedReader in, final String ... args)
    {
        final Class<?>    c=XmplImpl.class;
        final Method[]    ma=c.getMethods();
        for (final Method m : ma)
        {
            final String    n=m.getName();
            if ("getValue".equalsIgnoreCase(n))
                out.println(m);
        }

        return 0;

    }

    //////////////////////////////////////////////////////////////////////////

    public static class Base1 {
        @CoVariantReturn
        public void setValue (int v)
        {
            System.out.println(getClass().getSimpleName() + "#setValue(" + v + ")");
        }
    }

    public static class Base2 extends Base1 {
        /*
         * @see net.community.chest.test.ReflectionTester.Base1#setValue(int)
         */
        @Override
        public void setValue (int v)
        {
            super.setValue(2 * v);
        }
    }
    // arguments are ignored
    public static final int testAnnotationsLocator (final PrintStream out, final BufferedReader in, final String ... args)
    {
        try
        {
            final Method    m1=Base1.class.getMethod("setValue", Integer.TYPE),
                            m2=Base2.class.getMethod("setValue", Integer.TYPE),
                            m3=Base2.class.getMethod("getClass");

            final Map.Entry<CoVariantReturn,Method>    p1=
                MethodUtil.findClosestAnnotation(CoVariantReturn.class, m1),
                                                    p2=
                MethodUtil.findClosestAnnotation(CoVariantReturn.class, m2),
                                                    p3=
                MethodUtil.findClosestAnnotation(CoVariantReturn.class, m3);
            if (p1 != null)
                out.println("\tP1: " + p1.getValue());
            if (p2 != null)
                out.println("\tP2: " + p2.getValue());
            if (p3 != null)
                out.println("\tP3: " + p3.getValue());

            return 0;
        }
        catch(Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return (-1);
        }
    }

    //////////////////////////////////////////////////////////////////////////

    public static void main (String[] args)
    {
        final BufferedReader    in=getStdin();
//        final int                nErr=testAttributesExtraction(System.out, in, args);
//        final int                nErr=testCovariantReturns(System.out, in, args);
//        final int                nErr=testGenericTypes(System.out, in, args);
//        final int                nErr=testVarargsReflection(System.out, in, args);
//        final int                nErr=testReflectionPerformance(System.out, in, args);
//        final int                nErr=testCovariantAttributes(System.out, in, args);
//        final int                nErr=testEnumOverrides(System.out, in, args);
//        final int                nErr=testFieldsAccess(System.out, in, args);
//        final int                nErr=testCovariantJavaBeans(System.out, in, args);
//        final int                nErr=testMixedCovariantReturn(System.out, in, args);
        final int                nErr=testAnnotationsLocator(System.out, in, args);
        if (nErr != 0)
            System.err.println("test failed (err=" + nErr + ")");
        else
            System.out.println("OK");
    }
}
