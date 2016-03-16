/*
 *
 */
package net.community.chest.beans;

import java.util.Collections;
import java.util.Map;

import net.community.chest.AbstractTestSupport;

import org.junit.Test;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Dec 1, 2011 10:54:47 AM
 */
public class IntrospectionUtilsTest extends AbstractTestSupport {
    public IntrospectionUtilsTest ()
    {
        super();
    }

    @Test
    public void testIntrospect () throws Exception
    {
        final IntroBean        bean=new IntroBean();
        final Map<String,?>    propsMap=IntrospectionUtils.introspect(bean);
        assertFalse("No properties extracted", (propsMap == null) || propsMap.isEmpty());
        assertEquals("Mismatched number of properties", 3 /* the "class" is also a "property" */, propsMap.size());
        assertEquals("Mismatched long value", bean.getLongValue(), ((Number) propsMap.get("longValue")).longValue());
        assertMatches("Mismatched boolean value", bean.isBoolValue(), ((Boolean) propsMap.get("boolValue")).booleanValue());
    }

    @Test
    public void testDetrospect () throws Exception
    {
        final IntroBean        src=new IntroBean();
        final Map<String,?>    propsMap=IntrospectionUtils.introspect(src);
        final IntroBean        dst=new IntroBean(src.getLongValue() + System.nanoTime(), !src.isBoolValue());
        assertFalse("Pre-detrospected equality", src.equals(dst));

        final IntroBean    res=IntrospectionUtils.detrospect(dst, propsMap);
        assertSame("Mismatched detrospected instance", dst, res);
        assertEquals("Mismatched detrospected values", src, dst);
    }

    @Test
    public void testDetrospectNullValue () throws Exception
    {
        assertNull("Mismatched null value detrospect", IntrospectionUtils.detrospect(null, Collections.<String,Object>emptyMap()));
    }

    @Test
    public void testDetrospectEmptyProps () throws Exception
    {
        final IntroBean        src=new IntroBean(),
                            cpy=src.clone(),
                            res=IntrospectionUtils.detrospect(src, Collections.<String,Object>emptyMap());
        assertSame("Mismatched detrospected value", src, res);
        assertEquals("Modified original detrospected", cpy, res);
    }

    static class IntroBean implements Cloneable {
        public IntroBean ()
        {
            this(RANDOMIZER.nextLong(), RANDOMIZER.nextBoolean());
        }

        public IntroBean (long longValue, boolean boolValue)
        {
            _longValue = longValue;
            _boolValue = boolValue;
        }

        private long    _longValue;
        public long getLongValue ()
        {
            return _longValue;
        }

        public void setLongValue (long longValue)
        {
            _longValue = longValue;
        }

        private boolean    _boolValue;
        public boolean isBoolValue ()
        {
            return _boolValue;
        }

        public void setBoolValue (boolean boolValue)
        {
            _boolValue = boolValue;
        }
        /*
         * @see java.lang.Object#clone()
         */
        @Override
        public IntroBean clone () throws CloneNotSupportedException
        {
            return getClass().cast(super.clone());
        }
        /*
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode ()
        {
            return (int) (getLongValue() + (isBoolValue() ? 1 : 0));
        }
        /*
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals (Object obj)
        {
            if (obj == null)
                return false;
            if (this == obj)
                return true;
            if (getClass() != obj.getClass())
                return false;

            final IntroBean    other=(IntroBean) obj;
            if ((getLongValue() != other.getLongValue())
             || (isBoolValue() != other.isBoolValue()))
                return false;

            return true;
        }
        /*
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString ()
        {
            return getLongValue() + ":" + isBoolValue();
        }
    }
}
