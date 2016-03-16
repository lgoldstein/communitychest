/*
 *
 */
package net.community.chest.resources;

import java.util.Map;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 9, 2009 11:45:28 AM
 */
public abstract class AbstractPropertiesResolver implements PropertyAccessor<String,String> {
    protected AbstractPropertiesResolver ()
    {
        super();
    }
    /**
     * Traverses the input {@link String} and looks for property patterns
     * encoded as <code>${propname}</code>. Once such a pattern is encountered
     * it is replaced with its value from the associated {@link PropertyAccessor}
     * instance. If no value is found then the pattern is echoed to the output
     * as-is.
     * @param s Input string - may be null/empty (in which case nothing is
     * translated)
     * @param acc The {@link PropertyAccessor} to use to resolve referenced
     * properties - may be null (in which case nothing is translated)
     * @return Translation result - same as input if no translation occurred
     */
    public static final String format (final String s, final PropertyAccessor<String,String> acc)
    {
        final int    sLen=(null == s) ? 0 : s.length();
        if ((sLen <= 0) || (null == acc))
            return s;

        StringBuilder    sb=null;
        int                curPos=0;
        for (int    nextPos=s.indexOf('$'); (nextPos >= curPos) && (nextPos < sLen); )
        {
            if (nextPos >= (sLen-1))
                break;    // if '$' at end then nothing can follow it anyway

            if (s.charAt(nextPos+1) != '{')
            {
                nextPos = s.indexOf('$', nextPos + 1);
                continue;    // if not followed by '{' then assume not start of a property
            }

            final int    endPos=s.indexOf('}', nextPos + 2);
            if (endPos <= nextPos)
                break;    // if no ending '}' then no more properties can exist

            if (endPos <= (nextPos+2))
            {
                if (endPos >= (sLen-1))
                    break;

                nextPos = s.indexOf('$', endPos + 1);
                continue;    // if empty property name assume clear text
            }

            final String    propName=s.substring(nextPos+2, endPos),
                            propVal=acc.getProperty(propName);
            if (null == propVal)
            {
                nextPos = s.indexOf('$', endPos + 1);
                continue;    // if empty property value assume clear text
            }

            final String    repVal=format(propVal, acc);    // do recursive resolution
            if (null == sb)
                sb = new StringBuilder(sLen + repVal.length());

            // append clear text
            if (nextPos > curPos)
            {
                final String    t=s.substring(curPos, nextPos);
                sb.append(t);
            }
            sb.append(repVal);

            if ((curPos=(endPos+1)) >= sLen)
                break;    // stop if gone beyond string length

            nextPos = s.indexOf('$', curPos);    // keep looking
        }

        // check if any leftovers
        if ((curPos > 0) && (curPos < sLen))
        {
            final String    t=s.substring(curPos);
            sb.append(t);    // NOTE: sb cannot be null since we appended something to it
        }

        if (null == sb)    // means no replacement took place
            return s;

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static final String format (final String s, final Map<String,String> pm)
    {
        if ((null == pm) || (pm.size() <= 0))
            return s;

        if (pm instanceof PropertyAccessor<?,?>)
            return format(s, (PropertyAccessor<String,String>) pm);

        if ((null == s) || (s.length() <= 0))
            return s;

        return format(s, new PropertyAccessor<String,String>() {
                    @Override
                    public String getProperty (String key)
                    {
                        if ((null == key) || (key.length() <= 0))
                            return null;
                        return pm.get(key);
                    }
                }
            );
    }
    /**
     * Traverses the input {@link String} and looks for property patterns
     * encoded as <code>${propname}</code>. Once such a pattern is encountered
     * it is replaced with its value from the associated {@link PropertyAccessor}
     * instance. If no value is found then the pattern is echoed to the output
     * as-is.
     * @param s Input string - may be null/empty
     * @return Translation result - same as input if no translation occurred
     */
    public String format (final String s)
    {
        return format(s, this);
    }
}
