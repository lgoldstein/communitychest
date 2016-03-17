/*
 *
 */
package net.community.chest.artifacts.gnu.options;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.artifacts.gnu.EqualsAndHashCode;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 9, 2011 9:42:40 AM
 */
public class Option implements Serializable, Cloneable, Map.Entry<String,String>, Comparable<Option> {
    private static final long serialVersionUID = 6406027260542452893L;

    private String    _key, _value;
    public Option ()
    {
        this((String) null);
    }

    public Option (String key)
    {
        this(key, null);
    }

    public Option (Map.Entry<String,String> entry)
    {
        this(entry.getKey(), entry.getValue());
    }

    public Option (String key, String value)
    {
        _key = key;
        _value = value;
    }
    /*
     * @see java.util.Map.Entry#getKey()
     */
    @Override
    public String getKey ()
    {
        return _key;
    }

    // returns previous value
    public String setKey (String key)
    {
        final String    prev=getKey();
        _key = key;
        return prev;
    }
    /*
     * @see java.util.Map.Entry#getValue()
     */
    @Override
    public String getValue ()
    {
        return _value;
    }
    /*
     * @see java.util.Map.Entry#setValue(java.lang.Object)
     */
    @Override
    public String setValue (String value)
    {
        final String    prev=getValue();
        _value = value;
        return prev;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return EqualsAndHashCode.hashCode(getKey())
             + EqualsAndHashCode.hashCode(getValue())
             ;
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        if (!(obj instanceof Option))
            return false;
        if (compareTo((Option) obj) != 0)
            return false;

        return true;
    }
    /* Compares the key followed by the value (case sensitive)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo (Option o)
    {
        if (o == null)    // push null(s) to end
            return (-1);
        else if (this == o)
            return 0;

        final int    nRes=EqualsAndHashCode.compare(getKey(), o.getKey());
        if (nRes == 0)
            return EqualsAndHashCode.compare(getValue(), o.getValue());
        else
            return nRes;
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    public Option clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        final String    value=getValue();
        if ((value == null) || (value.length() <= 0))
            return getKey();

        return getKey() + "=" + value;
    }

    public static final class OptionParseResult {
        private final String[]    _args;
        private final int    _numParsed;
        private final List<Option>    _options;

        protected OptionParseResult (final int numParsed, final List<Option> options, final String ...args)
        {
            _numParsed = numParsed;
            _options = options;
            _args = args;
        }
        /**
         * @return The original arguments passed for parsing
         */
        public String[] getArguments ()
        {
            return _args;
        }
        /**
         * @return Number of arguments parsed - also 1st index of next non-option
         * argument in the arguments
         */
        public int getNumParsed ()
        {
            return _numParsed;
        }

        /**
         * @return The parsed {@link Option}-s
         */
        public List<Option> getParsedOptions ()
        {
            return _options;
        }
    }

    public static final Map<String,Option> toMap (final OptionParseResult result) throws IllegalStateException
    {
        return toMap((result == null) ? null : result.getParsedOptions());
    }

    public static final Map<String,Option> toMap (final Option ... options) throws IllegalStateException
    {
        if ((options == null) || (options.length <= 0))
            return Collections.emptyMap();

        return toMap(Arrays.asList(options));
    }

    public static final Map<String,Option> toMap (final Collection<? extends Option> options) throws IllegalStateException
    {
        if ((options == null) || options.isEmpty())
            return Collections.emptyMap();

        final Map<String,Option>    optsMap=new TreeMap<String,Option>();
        for (final Option opt : options)
        {
            final String    key=opt.getKey();
            final Option    prev=optsMap.put(key, opt);
            if (prev != null)
                throw new IllegalStateException("Multiple options for key=" + key);
        }
        return optsMap;
    }

    public static final Option findFirstMatchingOption (final Map<String,? extends Option>    optsMap, final String ... keys)
    {
        if ((optsMap == null) || optsMap.isEmpty())
            return null;
        if ((keys == null) || (keys.length <= 0))
            return null;

        for (final String key : keys)
        {
            final Option    option=optsMap.get(key);
            if (option != null)
                return option;
        }

        return null;
    }

    public static final String[]    EMPTY_ARGS=new String[0];
    /**
     * @param args Command line arguments
     * @return The {@link OptionParseResult}
     * @throws IllegalArgumentException if arguments format violates conventions
     */
    public static final OptionParseResult parseArguments (final String ... args) throws IllegalArgumentException
    {
        final int        numArgs=(args == null) ? 0 : args.length;
        List<Option>    optsList=null;
        int                aIndex=0;
        for ( ; aIndex < numArgs; aIndex++)
        {
            final String    argVal=args[aIndex];
            if (argVal.charAt(0) != '-')
                break;    // stop at first non-option

            final String    key, value;
            final int        valPos=argVal.lastIndexOf('=');
            if (argVal.charAt(1) == '-')    // check if double --
            {
                key = (valPos > 2) ? argVal.substring(2, valPos) : argVal.substring(2);
                value = (valPos > 2) ? argVal.substring(valPos + 1) : null;
            }
            else    // single - option
            {
                if (valPos >= 0)
                    throw new IllegalArgumentException("Single option arguments not allowed to contain '=': " + argVal);

                key = String.valueOf(argVal.charAt(1));
                // anything after the single letter is a value
                value = (argVal.length() > 2) ? argVal.substring(2) : null;
            }

            if (optsList == null)
                optsList = new ArrayList<Option>(numArgs);
            optsList.add(new Option(key, value));
        }

        if (optsList == null)
            optsList = Collections.emptyList();
        else
            optsList = Collections.unmodifiableList(optsList);

        return new OptionParseResult(aIndex, optsList, (args == null) ? EMPTY_ARGS : args);
    }
}
