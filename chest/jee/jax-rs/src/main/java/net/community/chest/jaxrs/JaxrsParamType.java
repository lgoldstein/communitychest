/*
 *
 */
package net.community.chest.jaxrs;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * Provides a useful encapsulation for the various type of JAX-RS parameter type annotations
 *
 * @author Lyor G.
 * @since Mar 10, 2011 7:45:28 AM
 */
public enum JaxrsParamType {
    COOKIE(CookieParam.class) {
            /*
             * @see com.springsource.insight.plugin.jaxrs.JaxrsParamType#getValue(java.lang.annotation.Annotation)
             */
            @Override
            public String getValue (Annotation a) throws ClassCastException
            {
                return (a == null) ? null : ((CookieParam) a).value();
            }
        },
    FORM(FormParam.class) {
            /*
             * @see com.springsource.insight.plugin.jaxrs.JaxrsParamType#getValue(java.lang.annotation.Annotation)
             */
            @Override
            public String getValue (Annotation a) throws ClassCastException
            {
                return (a == null) ? null : ((FormParam) a).value();
            }
        },
    HEADER(HeaderParam.class) {
            /*
             * @see com.springsource.insight.plugin.jaxrs.JaxrsParamType#getValue(java.lang.annotation.Annotation)
             */
            @Override
            public String getValue (Annotation a) throws ClassCastException
            {
                return (a == null) ? null : ((HeaderParam) a).value();
            }
        },
    MATRIX(MatrixParam.class) {
            /*
             * @see com.springsource.insight.plugin.jaxrs.JaxrsParamType#getValue(java.lang.annotation.Annotation)
             */
            @Override
            public String getValue (Annotation a) throws ClassCastException
            {
                return (a == null) ? null : ((MatrixParam) a).value();
            }
        },
    PATH(PathParam.class) {
            /*
             * @see com.springsource.insight.plugin.jaxrs.JaxrsParamType#getValue(java.lang.annotation.Annotation)
             */
            @Override
            public String getValue (Annotation a) throws ClassCastException
            {
                return (a == null) ? null : ((PathParam) a).value();
            }
        },
    QUERY(QueryParam.class) {
            /*
             * @see com.springsource.insight.plugin.jaxrs.JaxrsParamType#getValue(java.lang.annotation.Annotation)
             */
            @Override
            public String getValue (Annotation a) throws ClassCastException
            {
                return (a == null) ? null : ((QueryParam) a).value();
            }
        };

    private final Class<? extends Annotation> _annClass;
    public final Class<? extends Annotation> getAnnotationClass ()
    {
        return _annClass;
    }
    /**
     * Extract the annotation's <I>value</I>
     * @param a The {@link Annotation} instance - may be <code>null</code>
     * @return The extracted value - <code>null</code> if <code>null</code> instance
     * @throws ClassCastException If the annotation is not of the expected type
     */
    public abstract String getValue (Annotation a) throws ClassCastException;

    JaxrsParamType (Class<? extends Annotation> annClass)
    {
        _annClass = annClass;
    }

    public static final List<JaxrsParamType> VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final JaxrsParamType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }
    /**
     * Checks if the provided type name matches the {@link Class#getSimpleName()}
     * of any of the annotation classes encapsulated in the enumeration (case <U>insensitive</U>)
     * @param typeName The type name - may be <code>null</code>/empty
     * @return The matching {@link JaxrsParamType} - <code>null</code> if no match found
     */
    public static final JaxrsParamType fromTypeName (final String typeName)
    {
        if ((typeName == null) || (typeName.length() <= 0))
            return null;

        for (final JaxrsParamType val : VALUES)
        {
            final Class<? extends Annotation> annClass = (val == null) ? null : val.getAnnotationClass();
            if ((annClass != null) && typeName.equalsIgnoreCase(annClass.getSimpleName()))
                return val;
        }

        return null; // no match
    }

    public static final JaxrsParamType fromAnnotationClass (final Class<? extends Annotation> annClass)
    {
        if (annClass == null)
            return null;

        for (final JaxrsParamType val : VALUES)
        {
            if ((val != null) && (val.getAnnotationClass() == annClass))
                return val;
        }

        return null; // no match
    }

    public static final JaxrsParamType fromAnnotation (final Annotation a)
    {
        if (a == null)
            return null;

        final Class<?> proxyClass = a.getClass();
        for (final JaxrsParamType val : VALUES)
        {
            final Class<? extends Annotation> annClass = (val == null) ? null : val.getAnnotationClass();
            /*
             * NOTE !!! we cannot use class reference equality since annotation
             * instances are usually represented by Proxy instances of their
             * respective classes
             */
            if ((annClass != null) && annClass.isAssignableFrom(proxyClass))
                return val;
        }

        return null; // no match
    }

}
