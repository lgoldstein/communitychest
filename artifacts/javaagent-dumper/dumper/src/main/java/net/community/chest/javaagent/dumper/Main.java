/*
 *
 */
package net.community.chest.javaagent.dumper;

import java.lang.instrument.Instrumentation;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jul 25, 2011 1:13:28 PM
 */
public final class Main {
    private static final Logger    _logger=Logger.getLogger(Main.class.getSimpleName());
    /**
     * The {@link Instrumentation} instance
     */
    private static Instrumentation _instrumentation;
    /**
     * A {@link Map} of the options passed to the {@link #premain(String, Instrumentation)} call
     */
    private static Map<String,String>    _optsMap;
    /**
     * JSR-163 preMain Agent entry method
     * @param options The options passed to the agent - may be <code>null</code>/empty
     * @param instrumentation The {@link Instrumentation} instance used by the JVM
     */
    public static void premain (final String options, final Instrumentation instrumentation)
    {
        // Handle duplicate agents
        if (_instrumentation != null)
        {
            if (_logger.isLoggable(Level.FINE))
                _logger.fine("premain(" + options + ") ignored - re-invoked");
            return;
        }

        if ((_instrumentation=instrumentation) == null)
            throw new IllegalStateException("No " + Instrumentation.class.getSimpleName() + " instance provided");

        _optsMap = Collections.unmodifiableMap(parseOptions(options));
        _instrumentation.addTransformer(new DumperClassFileTransformer(_optsMap));

        if (_logger.isLoggable(Level.FINE))
            _logger.fine("premain(" + options + ") initialized");
    }
    /**
     * @return the {@link Instrumentation} system level instance
     */
    public static Instrumentation getInstrumentation ()
    {
        if (_instrumentation == null)
            throw new UnsupportedOperationException("JVM was not started with preMain -javaagent for dumper");

        return _instrumentation;
    }

    public static final char    OPTIONS_SEP='&', VALUES_SEP='=';
    static Map<String,String> parseOptions (final String options)
    {
        if ((options == null) || (options.length() <= 0))
            return Collections.emptyMap();

        final Map<String,String>    optsMap=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
        final String[]                optValues=options.split(String.valueOf(OPTIONS_SEP));
        for (final String optValue : optValues)
        {
            final int        valSep=optValue.indexOf(VALUES_SEP);
            final String    prev;
            if (valSep > 0)
            {
                final String    key=optValue.substring(0, valSep),
                                val=optValue.substring(valSep + 1);
                prev = optsMap.put(key, val);
            }
            else
                prev = optsMap.put(optValue, optValue);

            if (prev != null)
                throw new IllegalStateException("Multiple values specified for option=" + optValue);
        }

        return optsMap;
    }
}
