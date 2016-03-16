/*
 * @(#)VersionID.java    1.7 05/11/17
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MIDROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

package jnlp.sample.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 *   VersionID contains a JNLP version ID.
 *
 *  The VersionID also contains a prefix indicator that can
 *  be used when stored with a VersionString
 *
 */
public class VersionID implements Comparable<VersionID> {
    private String[] _tuple;   // Array of Integer or String objects
    private boolean  _usePrefixMatch;   // star (*) prefix
    private boolean  _useGreaterThan;  // plus (+) greather-than
    private boolean  _isCompound;       // and (&) operator
    private VersionID _rest;            // remaining part after the &

    /* Creates a VersionID object */
    public VersionID (final String s)
    {
        _usePrefixMatch  = false;
        _useGreaterThan = false;
        _isCompound = false;

        String    str=s;
        if ((str == null) || (str.length() <= 0))
        {
            _tuple = new String[0];
            return;
        }

        // Check for compound
        final int amp=str.indexOf('&');
        if (amp >= 0)
        {
            _isCompound = true;
            VersionID firstPart = new VersionID(str.substring(0, amp));
            _rest = new VersionID(str.substring(amp+1));
            _tuple = firstPart._tuple;
            _usePrefixMatch = firstPart._usePrefixMatch;
            _useGreaterThan = firstPart._useGreaterThan;
        }
        else
        {
            // Check for postfix
            if (str.endsWith("+"))
            {
                _useGreaterThan = true;
                str = str.substring(0, str.length() - 1);
            }
            else if (str.endsWith("*"))
            {
                _usePrefixMatch = true;
                str = str.substring(0, str.length() - 1);
            }
        }

        int start=0;
        final Collection<String> list=new LinkedList<String>();
        for (int i = 0; i < str.length(); i++)
        {
            // Split at each separator character
            if (".-_".indexOf(str.charAt(i)) >= 0)
            {
                if (start < i)
                {
                    String value = str.substring(start, i);
                    list.add(value);
                }
                start = i + 1;
            }
        }

        if (start < str.length())
            list.add(str.substring(start, str.length()));

        _tuple = list.toArray(new String[list.size()]);
    }
    /** @return true if no flags are set */
    public boolean isSimpleVersion ()
    {
        return !_useGreaterThan && !_usePrefixMatch && !_isCompound;
    }

    /* Match 'this' versionID against vid.
     *  The _usePrefixMatch/_useGreaterThan flag is used to determine if a
     *  prefix match of an exact match should be performed
     *  if _isCompound, must match _rest also.
     */
    public boolean match (VersionID vid)
    {
        if (_isCompound)
        {
            if (!_rest.match(vid))
                return false;
        }

        return (_usePrefixMatch) ? isPrefixMatch(vid) :
               (_useGreaterThan) ? vid.isGreaterThanOrEqual(this) : matchTuple(vid);
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object o)
    {
        if (matchTuple(o))
        {
            final VersionID ov=(VersionID) o;
            if ((_rest == null) || _rest.equals(ov._rest))
            {
                if ((_useGreaterThan == ov._useGreaterThan)
                 && (_usePrefixMatch == ov._usePrefixMatch))
                    return true;
            }
        }

        return false;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        final int    numTuples=(null == _tuple) ? 0 : _tuple.length;
        int            curHash=0;
        for (int    tIndex=0; tIndex < numTuples; tIndex++)
        {
            final String    s=_tuple[tIndex];
            final int        sh=
                ((null == s) || (s.length() <= 0)) ? 0 : s.hashCode();
            curHash += sh;
        }

        if (_rest != null)
            curHash += _rest.hashCode();
        if (_useGreaterThan)
            curHash++;
        if (_usePrefixMatch)
            curHash++;
        return curHash;
    }
    /* Compares if two version IDs are equal */
    private boolean matchTuple (Object o)
    {
        // Check for null and type
        if (!(o instanceof VersionID))
            return false;
        if (this == o)
            return true;

        final VersionID vid=(VersionID)o;
        // Normalize arrays
        final String[]    t1=normalize(_tuple, vid._tuple.length),
                        t2=normalize(vid._tuple, _tuple.length);

        // Check contents
        for(int i = 0; i < t1.length; i++)
        {
            final Object o1=getValueAsObject(t1[i]), o2=getValueAsObject(t2[i]);
            if (!ObjectUtil.match(o1, o2))
                return false;
        }

        return true;
    }

    private static Object getValueAsObject (String value)
    {
        if ((value != null) && (value.length() > 0) && (value.charAt(0) != '-'))
        {
            try
            {
                return Integer.valueOf(value);
            }
            catch(NumberFormatException nfe)
            {
                /* fall through */
            }
        }

        return value;
    }

    public boolean isGreaterThan (VersionID vid)
    {
        return isGreaterThanOrEqualHelper(vid, false);
    }

    public boolean isGreaterThanOrEqual (VersionID vid)
    {
        return isGreaterThanOrEqualHelper(vid, true);
    }

    /* Compares if 'this' is greater than vid */
    private boolean isGreaterThanOrEqualHelper (VersionID vid, boolean allowEqual)
    {
        if (_isCompound)
        {
            if (!_rest.isGreaterThanOrEqualHelper(vid, allowEqual))
                return false;
        }

        // Normalize the two strings
        final String[]    t1=normalize(_tuple, vid._tuple.length),
                        t2=normalize(vid._tuple, _tuple.length);
        for(int i = 0; i < t1.length; i++)
        {
            // Compare current element
            final String    o1=t1[i], o2=t2[i];
            final Object    e1=getValueAsObject(o1), e2=getValueAsObject(o2);
            if (ObjectUtil.match(e1, e2))
                continue;    // So far so good

            if ((e1 instanceof Number) && (e2 instanceof Number))
                   return ((Number)e1).intValue() > ((Number)e2).intValue();

            return o1.compareTo(o2) > 0;
        }

        // If we get here, they are equal
        return allowEqual;
    }

    /* Checks if 'this' is a prefix of vid */
    public boolean isPrefixMatch (VersionID vid)
    {
        if (_isCompound)
        {
            if (!_rest.isPrefixMatch(vid))
                return false;
        }

        // Make sure that vid is at least as long as the prefix
        final String[] t2 = normalize(vid._tuple, _tuple.length);
        for (int i = 0; i < _tuple.length; i++)
        {
            final Object e1=_tuple[i], e2=t2[i];
            if (ObjectUtil.match(e1, e2))
                continue;

            // Not a prefix
            return false;
        }

        return true;
    }

    /* Normalize an array to a certain lengrh */
    private static String[] normalize (String[] list, int minlength)
    {
        final int    curLen=(null == list) ? 0 : list.length;
        if (curLen < minlength)
        {
            // Need to do padding
            final String[] newlist=new String[minlength];
            if (curLen > 0)
                System.arraycopy(list, 0, newlist, 0, curLen);
            Arrays.fill(newlist, curLen, newlist.length, "0");
            return newlist;
        }
        else
        {
            return list;
        }
    }
    /*
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo (VersionID vid)
    {
        if (vid == null)
            return (-1);

        return equals(vid) ? 0 : (isGreaterThanOrEqual(vid) ? 1 : -1);
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        final StringBuilder sb=new StringBuilder();
        for(int i = 0; i < _tuple.length -1; i++)
        {
            sb.append(_tuple[i]);
            sb.append('.');
        }
        if (_tuple.length > 0)
            sb.append(_tuple[_tuple.length - 1]);
        if (_usePrefixMatch)
            sb.append('+');
        return sb.toString();
    }
}
