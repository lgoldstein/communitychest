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
 * @since Jun 10, 2013 10:46:56 AM
 */
public enum OptHeaderSubsystemType {
    UNKNOWN(OptHeaderWin32Fields.IMAGE_SUBSYSTEM_UNKNOWN),
    NATIVE(OptHeaderWin32Fields.IMAGE_SUBSYSTEM_NATIVE),
    WINDOWS_GUI(OptHeaderWin32Fields.IMAGE_SUBSYSTEM_WINDOWS_GUI),
    WINDOWS_CUI(OptHeaderWin32Fields.IMAGE_SUBSYSTEM_WINDOWS_CUI),
    POSIX_CUI(OptHeaderWin32Fields.IMAGE_SUBSYSTEM_POSIX_CUI),
    WINDOWS_CE_GUI(OptHeaderWin32Fields.IMAGE_SUBSYSTEM_WINDOWS_CE_GUI),
    EFI_APPLICATION(OptHeaderWin32Fields.IMAGE_SUBSYSTEM_EFI_APPLICATION),
    EFI_BOOT_SERVICE_DRIVER(OptHeaderWin32Fields.IMAGE_SUBSYSTEM_EFI_BOOT_SERVICE_DRIVER),
    EFI_RUNTIME_DRIVER(OptHeaderWin32Fields.IMAGE_SUBSYSTEM_EFI_RUNTIME_DRIVER),
    EFI_ROM(OptHeaderWin32Fields.IMAGE_SUBSYSTEM_EFI_ROM),
    XBOX(OptHeaderWin32Fields.IMAGE_SUBSYSTEM_XBOX);

    private final short _typeValue;
    public final short getTypeValue() {
        return _typeValue;
    }
    
    OptHeaderSubsystemType(short typeValue) {
        _typeValue = typeValue;
    }
    
    public static final Set<OptHeaderSubsystemType> VALUES=
            Collections.unmodifiableSet(EnumSet.allOf(OptHeaderSubsystemType.class));
    public static final OptHeaderSubsystemType fromTypeValue(short typeValue) {
        for (OptHeaderSubsystemType type : VALUES) {
            if (type.getTypeValue() == typeValue) {
                return type;
            }
        }
        
        return null;
    }
}
