package net.community.chest.reflect;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.util.map.MapEntryImpl;

/**
 * Copyright 2007 as per GPLv2
 *
 * Useful (static) handlers for
 * @author Lyor G.
 * @since Jun 19, 2007 12:02:05 PM
 */
public final class MethodUtil {
    private MethodUtil ()
    {
        // no instance
    }
    /**
     * @param sc {@link Collection} of {@link Method}-s to be checked
     * @return {@link Collection} of <U>public</U> {@link Method}-s - may be
     * null/empty if none found (or none to begin with)
     */
    public static final Collection<Method> getPubliclyAccessibleMethods (final Collection<? extends Method> sc)
    {
        if ((null == sc) || (sc.size() <= 0))
            return null;

        Collection<Method>    res=null;
        for (final Method m : sc)
        {
            if ((null == m) || (!Modifier.isPublic(m.getModifiers())))
                continue;    // we are interested only in public attributes

            if (null == res)
                res = new LinkedList<Method>();
            res.add(m);
        }

        return res;
    }

    public static final Collection<Method> getPubliclyAccessibleMethods (final Method ... sc)
    {
        return ((null == sc) || (sc.length <= 0)) ? null : getPubliclyAccessibleMethods(Arrays.asList(sc));
    }
    /**
     * Character used to separate the class name from the method
     */
    public static final char    METHOD_SEP_CHAR='#',
    /**
     * Delimiter used to denote start of parameters signature list
     */
                                PARAMS_START_DELIM='(',

    /**
     * Character used to separate signature parameters classes
     */
                                PARAM_SEP_CHAR=',',
    /**
      * Delimiter used to denote end of parameters signature list
      */
                                PARAMS_END_DELIM=')';
    // string equivalents
    public static final String    METHOD_SEP_VAL=String.valueOf(METHOD_SEP_CHAR),
                                 PARAMS_START_VAL=String.valueOf(PARAMS_START_DELIM),
                                 PARAM_SEP_VAL=String.valueOf(PARAM_SEP_CHAR),
                                 PARAMS_END_VAL=String.valueOf(PARAMS_END_DELIM);
    /**
     * Builds a {@link #PARAM_SEP_CHAR} delimited parameters list "(...)"
     * @param <A> The {@link Appendable} generic type
     * @param sb string buffer to append to - may NOT be null
     * @param params A {@link Collection} of parameters classes names
     * {@link String}-s - may be null/empty, and even have "empty" elements
     * (which are ignored when the key is generated)
     * @return same as input {@link Appendable} instance
     * @throws IOException if failed to append data to {@link Appendable} instance
     */
    public static final <A extends Appendable> A appendMethodSignature (final A sb, final Collection<String> params) throws IOException
    {
        if (null == sb)
            return sb;

         sb.append(PARAMS_START_DELIM);

         final int    numParams=(null == params) /* OK */ ? 0 : params.size();
         if (numParams > 0)
         {
             int    nIndex=0;
             for (final String pType : params)
             {
                 if ((null == pType) || (pType.length() <= 0))
                     continue;    // should not happen

                 if (nIndex > 0)
                     sb.append(PARAM_SEP_CHAR);
                 sb.append(pType);

                 nIndex++;
             }
         }

         sb.append(PARAMS_END_DELIM);
         return sb;
    }

    public static final <A extends Appendable> A appendMethodSignature (final A sb, final String... params) throws IOException
    {
        return appendMethodSignature(sb, ((null == params) || (params.length <= 0)) ? null : Arrays.asList(params));
    }
    /**
     * Builds a {@link #PARAM_SEP_CHAR} delimited parameters list "(...)"
     * @param params A {@link Collection} of parameters classes names
     * {@link String}-s - may be null/empty, and even have "empty" elements
     * (which are ignored when the key is generated)
     * @return built string - null/empty if error
     */
    public static final String getMethodSignature (final Collection<String> params)
    {
        try
        {
            final int            numParams=(null == params) /* OK */ ? 0 : params.size();
            final Appendable    sb=appendMethodSignature(new StringBuilder(2 + Math.max(0, numParams) * 64), params);
            return sb.toString();
        }
        catch(IOException e)    // should not happen
        {
            return null;
        }
    }
    public static final String getMethodSignature (final String... params)
    {
        return getMethodSignature(((null == params) || (params.length <= 0)) ? null : Arrays.asList(params));
    }
    /**
     * Appends the method part "name(...params...)"
     * @param <A> The {@link Appendable} generic type
     * @param sb string buffer to append to - may NOT be null
     * @param mName method name - may NOT be null/empty
     * @param params A {@link Collection} of parameters classes names
     * {@link String}-s - may be null/empty, and even have "empty" elements
     * (which are ignored when the key is generated)
     * @return same as input {@link Appendable} instance
     * @throws IOException if cannot append required information
     */
    public static final <A extends Appendable> A appendMethodKeyPart (final A sb, final String mName, final Collection<String> params) throws IOException
    {
        final int    nLen=(null == mName) ? 0 : mName.length();
        if ((null == sb) || (nLen <= 0))
            return sb;

        sb.append(mName);
        appendMethodSignature(sb, params);
        return sb;
    }

    public static final <A extends Appendable> A appendMethodKeyPart (final A sb, final String mName, final String... params) throws IOException
    {
        return appendMethodKeyPart(sb, mName, ((null == params) || (params.length <= 0)) ? null : Arrays.asList(params));
    }
    /**
     * Builds a method key part "name(...params...)"
     * @param mName method name - may NOT be null/empty
     * @param params A {@link Collection} of parameters classes names
     * {@link String}-s - may be null/empty, and even have "empty" elements
     * (which are ignored when the key is generated)
     * @return built string - null/empty if error
     */
    public static final String getMethodKeyPart (final String mName, final Collection<String> params)
    {
         final int    nLen=(null == mName) ? 0 : mName.length(),
                     numParams=(null == params) /* OK */ ? 0 : params.size();
         if (nLen <= 0)
             return null;

         try
         {
             final Appendable    sb=appendMethodKeyPart(new StringBuilder(nLen + 2 + Math.max(0, numParams) * 64), mName, params);
             return sb.toString();
         }
         catch(IOException e)    // should not happen
         {
             return null;
         }
    }
    public static final String getMethodKeyPart (final String mName, final String... params)
    {
        return getMethodKeyPart(mName, ((null == params) || (params.length <= 0)) ? null : Arrays.asList(params));
    }
    /**
     * Converts method/constructor parameters signature into a matching array
     * of strings representing the parameters classes names
     * @param params parameters classes - may be null/empty, and even have
     * "empty" elements (which are ignored if a key is generated)
     * @return matching strings {@link List} - <B>Note:</B> may be null/empty (if no
     * classes to begin with) and even contain null/empty elements (matching
     * the "empty" elements in the signature array)
     */
    public static final List<String> getMethodSignatureStrings (final Class<?>... params)
    {
        final int            numParams=(null == params) /* OK */ ? 0 : params.length;
        final List<String>    names=(numParams <= 0) ? null : new ArrayList<String>(numParams);
        for (int    pIndex=0; pIndex < numParams; pIndex++)
        {
            final Class<?>    pClass=params[pIndex];
            final String    pName=(null == pClass) /* should not happen */ ? null : pClass.getName();
            if ((null == pName) || (pName.length() <= 0))
                continue;    // should not happen

            names.add(pName);
        }

        return names;
    }
    /**
     * Builds a method key part "name(...params...)"
     * @param mName method name - may NOT be null/empty
     * @param params parameters classes - may be null/empty, and even have
     * "empty" elements (which are ignored if a key is generated)
     * @return built string - null/empty if error
     */
    public static final String getMethodKeyPart (final String mName, final Class<?>... params)
    {
        return getMethodKeyPart(mName, getMethodSignatureStrings(params));
    }
    /**
     * Builds a method key part "name(...params...)"
     * @param m method - may NOT be null
     * @return built string - null/empty if error
     */
    public static final String getMethodKeyPart (final Method m)
    {
        return (null == m) ? null : getMethodKeyPart(m.getName(), m.getParameterTypes());
    }
    /**
     * Builds a constructor key part "name(...params...)"
     * @param c constructor - may NOT be null
     * @return built string - null/empty if error
     */
    public static final String getConstructorKeyPart (final Constructor<?> c)
    {
        return (null == c) ? null : getMethodKeyPart(c.getName(), c.getParameterTypes());
    }
    /**
     * Appends the method part "name(...params...)"
     * @param <A> The {@link Appendable} generic type
     * @param sb string buffer to append to - may NOT be null
     * @param mName method name - may NOT be null/empty
     * @param params parameters classes - may be null/empty, and even have
     * "empty" elements (which are ignored if a key is generated)
     * @return same as input {@link Appendable}
     * @throws IOException if cannot append requested information
     */
    public static final <A extends Appendable> A appendMethodKeyPart (final A sb, final String mName, final Class<?>... params) throws IOException
    {
        return appendMethodKeyPart(sb, mName, getMethodSignatureStrings(params));
    }
    /**
     * Builds a {@link #PARAM_SEP_CHAR} delimited parameters list "(...)"
     * @param params parameters classes - may be null/empty, and even have
     * "empty" elements (which are ignored if a key is generated)
     * @return built string - null/empty if error
     */
    public static final String getMethodSignature (final Class<?>... params)
    {
        return getMethodSignature(getMethodSignatureStrings(params));
    }
    /**
     * Builds a full method/constructor key - "class#method(params list)"
     * @param <A> The {@link Appendable} generic type
     * @param sb string buffer to append to - may NOT be null
     * @param cName declaring method class name - may NOT be null/empty
     * @param mName method name - may NOT be null/empty
     * @param params A {@link Collection} of parameters classes names
     * {@link String}-s - may be null/empty, and even have "empty" elements
     * (which are ignored when the key is generated)
     * @return same as input {@link Appendable} instance
     * @throws IOException if cannot append requested information
     */
    public static final <A extends Appendable> A appendMethodKey (final A sb, final String cName, final String mName, final Collection<String> params) throws IOException
    {
        final int    cLen=(null == cName) ? 0 : cName.length();
        if ((null == sb) || (cLen <= 0))
            return sb;

        sb.append(cName).append(METHOD_SEP_CHAR);
        appendMethodKeyPart(sb, mName, params);
        return sb;
    }

    public static final <A extends Appendable> A appendMethodKey (final A sb, final String cName, final String mName, final String... params) throws IOException
    {
        return appendMethodKey(sb, cName, mName, ((null == params) || (params.length <= 0)) ? null : Arrays.asList(params));
    }
    /**
     * Builds a full method/constructor key - "class#method(params list)"
     * @param <A> The {@link Appendable} generic type
     * @param sb string buffer to append to - may NOT be null
     * @param cName declaring method class name - may NOT be null/empty
     * @param mName method name - may NOT be null/empty
     * @param params parameters classes names - may be null/empty, and even have
     * "empty" elements (which are ignored when the key is generated)
     * @return same as input {@link Appendable} instance
     * @throws IOException if cannot append requested information
     */
    public static final <A extends Appendable> A  appendMethodKey (final A sb, final String cName, final String mName, final Class<?>... params) throws IOException
    {
        return appendMethodKey(sb, cName, mName, getMethodSignatureStrings(params));
    }
    /**
     * @param cName declaring method class name - may NOT be null/empty
     * @param mName method name - may NOT be null/empty
     * @param params A {@link Collection} of parameters classes names
     * {@link String}-s - may be null/empty, and even have "empty" elements
     * (which are ignored when the key is generated)
     * @return unique "key" for the method (uses class+method name(s)
     * +signature) - null if error
     */
    public static final String getMethodKey (final String cName, final String mName, final Collection<String> params)
    {
        final int    nLen=(null == mName) ? 0 : mName.length(),
                    cLen=(null == cName) ? 0 : cName.length(),
                    numParams=(null == params) /* OK */ ? 0 : params.size();
        if ((nLen <= 0) || (cLen <= 0))
            return null;

        try
        {
            final Appendable    sb=appendMethodKey(new StringBuilder(cLen + 1 + nLen + 2 + Math.max(numParams, 0) * 64), cName, mName, params);
            return sb.toString();
        }
        catch(IOException e)    // should not happen
        {
            return null;
        }
    }

    public static final String getMethodKey (final String cName, final String mName, final String... params)
    {
        return getMethodKey(cName, mName, ((null == params) || (params.length <= 0)) ? null : Arrays.asList(params));
    }
    /**
     * @param cName declaring method class name - may NOT be null/empty
     * @param mName method name - may NOT be null/empty
     * @param params parameters classes - may be null/empty, and even have
     * "empty" elements (which are ignored when the key is generated)
     * @return unique "key" for the method (uses class+method name(s)
     * +signature) - null if error
     */
    public static final String getMethodKey (final String cName, final String mName, final Class<?>... params)
    {
        return getMethodKey(cName, mName, getMethodSignatureStrings(params));
    }
    /**
     * @param cName declaring method class name - may NOT be null/empty
     * @param m method whose "key" is required - may NOT be null
     * @return unique "key" for the method (uses class+method name(s)
     * +signature) - null if error
     */
    public static final String getMethodKey (final String cName, final Method m)
    {
        return (null == m) ? null : getMethodKey(cName, m.getName(), m.getParameterTypes());
    }
    /**
     * @param c class to which this method belongs (not checked) - may NOT be null
     * @param m method whose "key" is required - may NOT be null
     * @return unique "key" for the method (uses class+method name(s)
     * +signature) - null if error
     */
    public static final String getMethodKey (final Class<?> c, final Method m)
    {
        return (null == c) ? null : getMethodKey(c.getName(), m);
    }
    /**
     * @param m method whose "key" is required - may NOT be null
     * @return unique "key" for the method (uses class+method name(s)
     * +signature) - null if error
     */
    public static final String getMethodKey (final Method m)
    {
        return (null == m) ? null : getMethodKey(m.getDeclaringClass(), m);
    }
    /**
     * @param cName declaring constructor class name - may NOT be null/empty
     * @param c constructor whose "key" is required - may NOT be null
     * @return unique "key" for the constructor (uses class+constructor name(s)
     * +signature) - null if error
     */
    public static final String getConstructorKey (final String cName, final Constructor<?> c)
    {
        return (null == c) ? null : getMethodKey(cName,  c.getName(), c.getParameterTypes());
    }
    /**
     * @param c class to which this constructor belongs (not checked) - may NOT be null
     * @param m constructor whose "key" is required - may NOT be null
     * @return unique "key" for the constructor (uses class+constructor name(s)
     * +signature) - null if error
     */
    public static final String getConstructorKey (final Class<?> c, final Constructor<?> m)
    {
        return (null == c) ? null : getConstructorKey(c.getName(), m);
    }
    /**
     * @param c constructor whose "key" is required - may NOT be null
     * @return unique "key" for the constructor (uses class+constructor name(s)
     * +signature) - null if error
     */
    public static final String getConstructorKey (final Constructor<?> c)
    {
        return (null == c) ? null : getConstructorKey(c.getDeclaringClass(), c);
    }

    public static final <A extends Appendable> A appendFieldKey (final A sb, final Field f) throws IOException
    {
        if ((null == f) || (null == sb))
            return sb;

        final Class<?>    c=f.getDeclaringClass(), t=f.getType();
        sb.append(c.getName())
          .append(METHOD_SEP_CHAR)
          .append(f.getName())
          .append('[')
          .append(t.getName())
          .append(']');
        return sb;
    }

    public static final String getFieldKey (final Field f)
    {
        if (null == f)
            return null;

        try
        {
            return appendFieldKey(new StringBuilder(64), f).toString();
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }
    /**
     * @param params A {@link String} of method parameters encoded the same
     * way as {@link #getMethodSignature(Collection)}
     * @return A {@link List} of the {@link Class}-es of the parameters
     * @throws Exception If failed to instantiate the {@link Class}-es
     */
    public static final List<Class<?>> fromParamsKeyPart (final String params) throws Exception
    {
        final Collection<String>    pl=StringUtil.splitString(params, PARAM_SEP_CHAR);
        final int                    numParams=(null == pl) ? 0 : pl.size();
        if (numParams <= 0)
            return null;

        final List<Class<?>>    l=new ArrayList<Class<?>>(numParams);
        for (final String p : pl)
        {
            if ((null == p) || (p.length() <= 0))
                continue;    // should not happen

            final Class<?>    c=ClassUtil.loadClassByName(p);
            l.add(c);
        }

        return l;
    }
    /**
     * @param c The {@link Class} where the method is to be located
     * @param mPart The method (and parameters) {@link String} encoded
     * in the same format as {@link #getMethodKeyPart(Method)}
     * @return The located {@link Method} - may be null if null/empty
     * encoding to begin with
     * @throws Exception Failed to parse the &quot;key&quot; or locate
     * the {@link Method}
     */
    public static final Method fromMethodKeyPart (final Class<?> c, final String mPart) throws Exception
    {
        final int    pLen=(null == mPart) ? 0 : mPart.length();
        if (pLen <= 0)
            return null;

        final int    pPos=mPart.indexOf(PARAMS_START_DELIM),
                    pEnd=mPart.lastIndexOf(PARAMS_END_DELIM);
        if ((pPos <= 0) || (pPos >= (pLen-1))
         || (pEnd <= pPos) || (pEnd >= pLen))
            throw new IllegalArgumentException("fromMethodKeyPart(" + mPart + ") bad/illegal format");

        final String                            mName=mPart.substring(0, pPos),
                                                pPart=mPart.substring(pPos+1, pEnd);
        final Collection<? extends Class<?>>    params=fromParamsKeyPart(pPart);
        final int                                numParams=
            (null == params) ? 0 : params.size();
        final Class<?>[]                        pa=
            (numParams <= 0) ? null : params.toArray(new Class[numParams]);
        return ((null == pa) || (pa.length <= 0)) ? c.getMethod(mName) : c.getMethod(mName, pa);
    }
    /**
     * @param key A &quot;key&quot; generated according to the same rules
     * as implemented in {@link #getMethodKey(Method)}
     * @return The extracted {@link Method} - null if no key to begin with
     * @throws Exception If failed to locate method
     */
    public static final Method fromMethodKey (final String key) throws Exception
    {
        final int    kLen=(null == key) ? 0 : key.length();
        if (kLen <= 0)
            return null;

        final int    mPos=key.indexOf(METHOD_SEP_CHAR);
        if ((mPos <= 0) || (mPos >= (kLen-1)))
            throw new IllegalArgumentException("fromMethodKey(" + key + ") no class separator");

        final String    clsName=key.substring(0, mPos);
        final Class<?>    c=ClassUtil.loadClassByName(clsName);
        final String    mPart=key.substring(mPos+1);
        return fromMethodKeyPart(c, mPart);
    }
    /**
     * @param <A> The {@link Appendable} generic type
     * @param aClass An {@link Annotation} {@link Class} to look for
     * @param ma The {@link Method}-s to look for the annotations
     * @return A &quot;pair&quot; represented as a {@link java.util.Map.Entry} whose
     * key=the {@link Annotation} instance, value=the <U>first</U> {@link Method}
     * annotated with this annotation. Null if no match found (or null/empty
     * class/methods to begin with)
     */
    public static final <A extends Annotation> Map.Entry<A,Method> findAnnotation (final Class<A> aClass, final Collection<? extends Method> ma)
    {
        if ((null == aClass) || (null == ma) || (ma.size() <= 0))
            return null;

        for (final Method m : ma)
        {
            final A    ann=(null == m) ? null : m.getAnnotation(aClass);
            if (ann != null)
                return new MapEntryImpl<A,Method>(ann, m);
        }

        return null;
    }
    /**
     * @param <A> The {@link Appendable} generic type
     * @param aClass An {@link Annotation} {@link Class} to look for
     * @param ma The {@link Method}-s to look for the annotations
     * @return A &quot;pair&quot; represented as a {@link java.util.Map.Entry} whose
     * key=the {@link Annotation} instance, value=the <U>first</U> {@link Method}
     * annotated with this annotation. Null if no match found (or null/empty
     * class/methods to begin with)
     */
    public static final <A extends Annotation> Map.Entry<A,Method> findAnnotation (final Class<A> aClass, final Method ... ma)
    {
        if ((null == aClass) || (null == ma) || (ma.length <= 0))
            return null;

        return findAnnotation(aClass, Arrays.asList(ma));
    }
    /**
     * @param <A> The {@link Appendable} generic type
     * @param aClass An {@link Annotation} {@link Class} to look for
     * @param m The &quot;base&quot; {@link Method} to start looking for
     * the annotation. If it is not found for the method, then the parent
     * class is scanned for a method with the same signature. If found, then
     * that method is checked - and so on, until no more parent class method
     * found or annotation located
     * @return A &quot;pair&quot; represented as a {@link java.util.Map.Entry} whose
     * key=the {@link Annotation} instance, value=the <U>first</U> {@link Method}
     * annotated with this annotation. Null if no match found (or null/empty
     * class/methods to begin with)
     */
    public static final <A extends Annotation> Map.Entry<A,Method> findClosestAnnotation (final Class<A> aClass, final Method m)
    {
        if ((null == aClass) || (null == m))
            return null;

        final A    ann=m.getAnnotation(aClass);
        if (ann != null)
            return new MapEntryImpl<A,Method>(ann, m);

        final Class<?>    mClass=m.getDeclaringClass(),
                        pClass=mClass.getSuperclass();
        if (null == pClass)    // OK if reached top-level class (Object)
            return null;

        try
        {
            final Method    superMethod=pClass.getDeclaredMethod(m.getName(), m.getParameterTypes());
            if (null == superMethod)    // should not happen
                return null;

            return findClosestAnnotation(aClass, superMethod);
        }
        catch(NoSuchMethodException e)
        {
            return null;
        }
    }
    /**
     * Default &quot;name&quot; of a {@link Constructor} method
     */
    public static final String    CONSTRUCTOR_METHOD_NAME="<init>";
    /**
     * Goes <U>backwards</U> over an array of {@link StackTraceElement}-s
     * looking for the 1st one that is the name of a constructor.
     * This method is very useful for detecting call sequence in a constructor
     * by using the following pseudo-code:</BR>
     * @param sa The array of {@link StackTraceElement}-s to be scanned
     * @return Index of 1st element in array whose {@link StackTraceElement#getMethodName()}
     * matches {@link #CONSTRUCTOR_METHOD_NAME} - negative if not found
     */
    public static final int getFirstConstructorElement (final StackTraceElement ...    sa)
    {
        final int    numElems=(null == sa) ? 0 : sa.length;
        for (int    sIndex=numElems-1; sIndex >= 0; sIndex--)
        {
            final StackTraceElement    elem=sa[sIndex];
            final String            eMethod=(null == elem) ? null : elem.getMethodName();
            if (0 == StringUtil.compareDataStrings(eMethod, CONSTRUCTOR_METHOD_NAME, true))
                return sIndex;
        }

        return (-1);
    }
}
