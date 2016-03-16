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
 * @since Jun 10, 2013 10:07:09 AM
 */
public enum COFFMachineType {
    UNKNOWN(COFFFileHeader.IMAGE_FILE_MACHINE_UNKNOWN),
    AM33(COFFFileHeader.IMAGE_FILE_MACHINE_AM33),
    AMD64(COFFFileHeader.IMAGE_FILE_MACHINE_AMD64),
    ARM(COFFFileHeader.IMAGE_FILE_MACHINE_ARM),
    ARMNT(COFFFileHeader.IMAGE_FILE_MACHINE_ARMNT),
    ARM64(COFFFileHeader.IMAGE_FILE_MACHINE_ARM64),
    EBC(COFFFileHeader.IMAGE_FILE_MACHINE_EBC),
    I386(COFFFileHeader.IMAGE_FILE_MACHINE_I386),
    IA64(COFFFileHeader.IMAGE_FILE_MACHINE_IA64),
    M32R(COFFFileHeader.IMAGE_FILE_MACHINE_M32R),
    MIPS16(COFFFileHeader.IMAGE_FILE_MACHINE_MIPS16),
    MIPSFPU(COFFFileHeader.IMAGE_FILE_MACHINE_MIPSFPU),
    MIPSFPU16(COFFFileHeader.IMAGE_FILE_MACHINE_MIPSFPU16),
    POWERPC(COFFFileHeader.IMAGE_FILE_MACHINE_POWERPC),
    POWERPCFP(COFFFileHeader.IMAGE_FILE_MACHINE_POWERPCFP),
    R4000(COFFFileHeader.IMAGE_FILE_MACHINE_R4000),
    SH3(COFFFileHeader.IMAGE_FILE_MACHINE_SH3),
    SH3DSP(COFFFileHeader.IMAGE_FILE_MACHINE_SH3DSP),
    SH4(COFFFileHeader.IMAGE_FILE_MACHINE_SH4),
    SH5(COFFFileHeader.IMAGE_FILE_MACHINE_SH5),
    THUMB(COFFFileHeader.IMAGE_FILE_MACHINE_THUMB),
    WCEMIPSV2(COFFFileHeader.IMAGE_FILE_MACHINE_WCEMIPSV2);

    private final short _typeValue;
    public final short getTypeValue() {
        return _typeValue;
    }

    COFFMachineType(short typeValue) {
        _typeValue = typeValue;
    }

    public static final Set<COFFMachineType>    VALUES=
            Collections.unmodifiableSet(EnumSet.allOf(COFFMachineType.class));

    public static final COFFMachineType fromTypeValue(short typeValue) {
        for (COFFMachineType type : VALUES) {
            if (typeValue == type.getTypeValue()) {
                return type;
            }
        }

        return null;
    }
}
