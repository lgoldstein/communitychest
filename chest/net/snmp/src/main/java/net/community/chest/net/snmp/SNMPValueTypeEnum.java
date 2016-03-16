package net.community.chest.net.snmp;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 18, 2007 12:51:58 PM
 */
public enum SNMPValueTypeEnum {
    GAUGE,
    GAUGE32,
    GAUGE64,
    INTEGER,
    INTEGER32,
    INTEGER64,
    UNSIGNED,
    UNSIGNED32,
    UNSIGNED64,
    COUNTER,
    COUNTER32,
    COUNTER64,
    OCTETSTRING,
    TIMETICKS,
    IPADDRESS;

    public static final List<SNMPValueTypeEnum>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    /**
     * @param s value type name (case insensitive)
     * @return matching type - null if no match found
     */
    public static final SNMPValueTypeEnum fromString (final String s)
    {
        if ((null == s) || (s.length() <= 0))
            return null;

        final SNMPValueTypeEnum    v=CollectionsUtils.fromString(VALUES, s, false);
        if (v != null)
            return v;

        // special case(s) which due to the space in between cannot be valid Enum name(s)
        if (SNMPProtocol.OCTETSTRINGMod.equalsIgnoreCase(s))
            return OCTETSTRING;

        // this point is reached if no match found
        return null;
    }
}
