package net.community.chest.util.logging;

/**
 * Copyright 2007 as per GPLv2
 *
 * @author Lyor G.
 * @since Jun 26, 2007 1:04:29 PM
 */
public interface LoggerWrapperFactory {
    /**
     * @param logClass {@link Class} that is going to use the returned wrapper
     * @param logName A logger name to be used - if null/empty the class name is used
     * @param clsIndex A class "index" discriminator that can be added to the
     * logging {@link Class} ID - may be null/empty
     * @return {@link LoggerWrapper} instance to be used
     */
    LoggerWrapper getLogger (final Class<?> logClass, final String logName, final String clsIndex);
    /**
     * @param logName logger category (may not be null/empty)
     * @param level level to be set for it (and its sub-loggers) - see
     * {@link LogLevelWrapper} for available level names.
     */
    void setLoggerComponentLevel (String logName, LogLevelWrapper level);
}
