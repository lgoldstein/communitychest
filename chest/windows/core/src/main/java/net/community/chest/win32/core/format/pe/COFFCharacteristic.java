/*
 *
 */
package net.community.chest.win32.core.format.pe;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 10, 2013 10:23:25 AM
 */
public enum COFFCharacteristic {
    RELOCS_STRIPPED(COFFFileHeader.IMAGE_FILE_RELOCS_STRIPPED),
    EXECUTABLE_IMAGE(COFFFileHeader.IMAGE_FILE_EXECUTABLE_IMAGE),
    LINE_NUMS_STRIPPED(COFFFileHeader.IMAGE_FILE_LINE_NUMS_STRIPPED),
    LOCAL_SYMS_STRIPPED(COFFFileHeader.IMAGE_FILE_LOCAL_SYMS_STRIPPED),
    AGGRESSIVE_WS_TRIM(COFFFileHeader.IMAGE_FILE_AGGRESSIVE_WS_TRIM),
    LARGE_ADDRESS_AWARE(COFFFileHeader.IMAGE_FILE_LARGE_ADDRESS_AWARE),
    RESERVED(COFFFileHeader.IMAGE_FILE_RESERVED),
    BYTES_REVERSED_LO(COFFFileHeader.IMAGE_FILE_BYTES_REVERSED_LO),
    BIT32_MACHINE(COFFFileHeader.IMAGE_FILE_32BIT_MACHINE),
    DEBUG_STRIPPED(COFFFileHeader.IMAGE_FILE_DEBUG_STRIPPED),
    REMOVABLE_RUN_FROM_SWAP(COFFFileHeader.IMAGE_FILE_REMOVABLE_RUN_FROM_SWAP),
    NET_RUN_FROM_SWAP(COFFFileHeader.IMAGE_FILE_NET_RUN_FROM_SWAP),
    SYSTEM(COFFFileHeader.IMAGE_FILE_SYSTEM),
    DLL(COFFFileHeader.IMAGE_FILE_DLL),
    UP_SYSTEM_ONLY(COFFFileHeader.IMAGE_FILE_UP_SYSTEM_ONLY),
    BYTES_REVERSED_HI(COFFFileHeader.IMAGE_FILE_BYTES_REVERSED_HI);

    private final short _maskValue;
    public final short getMaskValue() {
        return _maskValue;
    }

    COFFCharacteristic(short maskValue) {
        _maskValue = maskValue;
    }

    public static final Set<COFFCharacteristic> VALUES=
            Collections.unmodifiableSet(EnumSet.allOf(COFFCharacteristic.class));
    public static final Set<COFFCharacteristic> fromCharacteristics(short mask) {
        Set<COFFCharacteristic> result=EnumSet.noneOf(COFFCharacteristic.class);
        for (COFFCharacteristic c : VALUES) {
            if ((mask & c.getMaskValue()) != 0) {
                result.add(c);
            }
        }

        return result;
    }
}
