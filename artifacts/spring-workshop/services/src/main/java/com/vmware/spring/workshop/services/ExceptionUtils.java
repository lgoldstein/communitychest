package com.vmware.spring.workshop.services;

import java.lang.reflect.InvocationTargetException;

/**
 * @author lgoldstein
 */
public final class ExceptionUtils {
    private ExceptionUtils() {
        // no instance
    }
    /**
     * @param t
     *            Original {@link Throwable} instance
     * @return Either the original if already a {@link RuntimeException} or a
     *         new {@link RuntimeException} embedding the <U>resolved</U>
     *         original as its cause
     * @see #toRuntimeException(Throwable, boolean)
     */
    public static final RuntimeException toRuntimeException(final Throwable t) {
        return toRuntimeException(t, true);
    }

    /**
     * @param t
     *            Original {@link Throwable} instance
     * @param resolve
     *            Whether to invoke {@link #resolveEmbeddedThrowable(Throwable)}
     *            before converting to {@link RuntimeException}
     * @return Either the original if already a {@link RuntimeException} or a
     *         new {@link RuntimeException} embedding the original as its cause
     */
    public static final RuntimeException toRuntimeException(final Throwable t, final boolean resolve) {
        final Throwable effective = resolve ? resolveEmbeddedThrowable(t) : t;
        if (effective instanceof RuntimeException)
            return (RuntimeException) effective;
        else
            return new RuntimeException(effective);

    }

    /**
     * @param t
     *            Original {@link Throwable} instance
     * @return The &quot;actual&quot; thrown error/exception or original
     *         instance if no embedded error/exception. E.g., for
     *         {@link InvocationTargetException} this means the
     *         {@link InvocationTargetException#getTargetException()}.
     *         <B>Note:</B> the resolution is <U>recursive</U> until a
     *         non-embedded error/exception is found
     * @see #resolveEmbeddedThrowable(Throwable, boolean)
     */
    public static final Throwable resolveEmbeddedThrowable(final Throwable t) {
        return resolveEmbeddedThrowable(t, true);
    }

    /**
     * @param t
     *            Original {@link Throwable} instance
     * @param recursive
     *            Whether to recursively follow the resolved instance or not
     * @return The &quot;actual&quot; thrown error/exception - e.g., for
     *         {@link InvocationTargetException} this means the
     *         {@link InvocationTargetException#getTargetException()}.
     */
    public static final Throwable resolveEmbeddedThrowable(final Throwable t, final boolean recursive) {
        if (t instanceof InvocationTargetException) {
            final Throwable target = ((InvocationTargetException) t).getTargetException();
            if (recursive)
                return resolveEmbeddedThrowable(target, recursive);
            else
                return target;
        } else
            return t;
    }
}
