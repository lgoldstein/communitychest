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
 * @since Jun 10, 2013 11:31:14 AM
 */
public enum SectionTableCharacter {
    RESERVED_1(SectionTableEntry.IMAGE_SCN_RESERVED_1),
    RESERVED_2(SectionTableEntry.IMAGE_SCN_RESERVED_2),
    RESERVED_4(SectionTableEntry.IMAGE_SCN_RESERVED_4),
    TYPE_NO_PAD(SectionTableEntry.IMAGE_SCN_TYPE_NO_PAD),
    RESERVED_16(SectionTableEntry.IMAGE_SCN_RESERVED_16),
    CNT_CODE(SectionTableEntry.IMAGE_SCN_CNT_CODE),
    CNT_INITIALIZED_DATA(SectionTableEntry.IMAGE_SCN_CNT_INITIALIZED_DATA),
    CNT_UNINITIALIZED_DATA(SectionTableEntry.IMAGE_SCN_CNT_UNINITIALIZED_DATA),
    LNK_OTHER(SectionTableEntry.IMAGE_SCN_LNK_OTHER),
    LNK_INFO(SectionTableEntry.IMAGE_SCN_LNK_INFO),
    RESERVED_256(SectionTableEntry.IMAGE_SCN_RESERVED_256),
    LNK_REMOVE(SectionTableEntry.IMAGE_SCN_LNK_REMOVE),
    LNK_COMDAT(SectionTableEntry.IMAGE_SCN_LNK_COMDAT),
    GPREL(SectionTableEntry.IMAGE_SCN_GPREL),
    MEM_PURGEABLE(SectionTableEntry.IMAGE_SCN_MEM_PURGEABLE),
    MEM_16BIT(SectionTableEntry.IMAGE_SCN_MEM_16BIT),
    MEM_LOCKED(SectionTableEntry.IMAGE_SCN_MEM_LOCKED),
    MEM_PRELOAD(SectionTableEntry.IMAGE_SCN_MEM_PRELOAD),
    ALIGN_1BYTES(SectionTableEntry.IMAGE_SCN_ALIGN_1BYTES),
    ALIGN_2BYTES(SectionTableEntry.IMAGE_SCN_ALIGN_2BYTES),
    ALIGN_4BYTES(SectionTableEntry.IMAGE_SCN_ALIGN_4BYTES),
    ALIGN_8BYTES(SectionTableEntry.IMAGE_SCN_ALIGN_8BYTES),
    ALIGN_16BYTES(SectionTableEntry.IMAGE_SCN_ALIGN_16BYTES),
    ALIGN_32BYTES(SectionTableEntry.IMAGE_SCN_ALIGN_32BYTES),
    ALIGN_64BYTES(SectionTableEntry.IMAGE_SCN_ALIGN_64BYTES),
    ALIGN_128BYTES(SectionTableEntry.IMAGE_SCN_ALIGN_128BYTES),
    ALIGN_256BYTES(SectionTableEntry.IMAGE_SCN_ALIGN_256BYTES),
    ALIGN_512BYTES(SectionTableEntry.IMAGE_SCN_ALIGN_512BYTES),
    ALIGN_1024BYTES(SectionTableEntry.IMAGE_SCN_ALIGN_1024BYTES),
    ALIGN_2048BYTES(SectionTableEntry.IMAGE_SCN_ALIGN_2048BYTES),
    ALIGN_4096BYTES(SectionTableEntry.IMAGE_SCN_ALIGN_4096BYTES),
    ALIGN_8192BYTES(SectionTableEntry.IMAGE_SCN_ALIGN_8192BYTES),
    LNK_NRELOC_OVFL(SectionTableEntry.IMAGE_SCN_LNK_NRELOC_OVFL),
    MEM_DISCARDABLE(SectionTableEntry.IMAGE_SCN_MEM_DISCARDABLE),
    MEM_NOT_CACHED(SectionTableEntry.IMAGE_SCN_MEM_NOT_CACHED),
    MEM_NOT_PAGED(SectionTableEntry.IMAGE_SCN_MEM_NOT_PAGED),
    MEM_SHARED(SectionTableEntry.IMAGE_SCN_MEM_SHARED),
    MEM_EXECUTE(SectionTableEntry.IMAGE_SCN_MEM_EXECUTE),
    MEM_READ(SectionTableEntry.IMAGE_SCN_MEM_READ),
    MEM_WRITE(SectionTableEntry.IMAGE_SCN_MEM_WRITE);

    private final int   _maskValue;
    public final int getMaskValue() {
        return _maskValue;
    }
    
    private final boolean   _reserved;
    public final boolean isReserved() {
        return _reserved;
    }

    SectionTableCharacter(int maskValue) {
        _maskValue = maskValue;
        _reserved = name().startsWith("RESERVED");
    }
    
    public static final Set<SectionTableCharacter>  VALUES=
            Collections.unmodifiableSet(EnumSet.allOf(SectionTableCharacter.class));
    public static final Set<SectionTableCharacter> fromSectionCharacteristics(int mask) {
        Set<SectionTableCharacter>  result=EnumSet.noneOf(SectionTableCharacter.class);
        for (SectionTableCharacter c : VALUES) {
            if ((mask & c.getMaskValue()) != 0) {
                result.add(c);
            }
        }
        
        return result;
    }
}
