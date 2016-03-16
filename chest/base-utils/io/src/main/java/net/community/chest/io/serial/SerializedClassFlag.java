/*
 *
 */
package net.community.chest.io.serial;

import java.io.ObjectStreamConstants;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 22, 2012 9:54:30 AM
 */
public enum SerializedClassFlag {
    WRITEMETHOD(ObjectStreamConstants.SC_WRITE_METHOD),         // indicates that class specifies its own "writeMethod"
    BLOCKDATA(ObjectStreamConstants.SC_BLOCK_DATA),             // data written in block mode
    SERIALIZABLE(ObjectStreamConstants.SC_SERIALIZABLE),     // class itself is Serializable
    EXTERNALIZABLE(ObjectStreamConstants.SC_EXTERNALIZABLE), // class is Externalizable
    ENUM(ObjectStreamConstants.SC_ENUM);                     // class is an Enum

    private final byte    _maskValue;
    public final byte getMaskValue () {
        return _maskValue;
    }

    SerializedClassFlag (byte mask) {
        _maskValue = mask;
    }

    public static final Set<SerializedClassFlag>    FLAGS=
            Collections.unmodifiableSet(EnumSet.allOf(SerializedClassFlag.class));
    public static final Set<SerializedClassFlag> fromMask (byte mask) {
        Set<SerializedClassFlag>    result=EnumSet.noneOf(SerializedClassFlag.class);
        for (SerializedClassFlag flag : FLAGS) {
            if ((mask & flag.getMaskValue()) != 0) {
                result.add(flag);
            }
        }

        return result;
    }

    public static final byte fromFlags (Collection<? extends SerializedClassFlag> flags) {
        if ((flags == null) || flags.isEmpty()) {
            return 0;
        }

        byte    result=0;
        for (SerializedClassFlag flag : flags) {
            result |= flag.getMaskValue();
        }

        return result;
    }
}
