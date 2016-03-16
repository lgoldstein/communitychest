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
 * @since Jun 10, 2013 11:05:58 AM
 *
 */
public enum OptHeaderDllCharacter {
    RESERVED_1(OptHeaderWin32Fields.IMAGE_DLL_CHARACTERISTICS_RESERVED_1),
    RESERVED_2(OptHeaderWin32Fields.IMAGE_DLL_CHARACTERISTICS_RESERVED_2),
    RESERVED_4(OptHeaderWin32Fields.IMAGE_DLL_CHARACTERISTICS_RESERVED_4),
    RESERVED_8(OptHeaderWin32Fields.IMAGE_DLL_CHARACTERISTICS_RESERVED_8),
    RESERVED_16(OptHeaderWin32Fields.IMAGE_DLL_CHARACTERISTICS_RESERVED_16),
    RESERVED_32(OptHeaderWin32Fields.IMAGE_DLL_CHARACTERISTICS_RESERVED_32),
    DYNAMIC_BASE(OptHeaderWin32Fields.IMAGE_DLL_CHARACTERISTICS_DYNAMIC_BASE),
    FORCE_INTEGRITY(OptHeaderWin32Fields.IMAGE_DLL_CHARACTERISTICS_FORCE_INTEGRITY),
    NX_COMPAT(OptHeaderWin32Fields.IMAGE_DLL_CHARACTERISTICS_NX_COMPAT),
    NO_ISOLATION(OptHeaderWin32Fields.IMAGE_DLL_CHARACTERISTICS_NO_ISOLATION),
    NO_SEH(OptHeaderWin32Fields.IMAGE_DLL_CHARACTERISTICS_NO_SEH),
    NO_BIND(OptHeaderWin32Fields.IMAGE_DLL_CHARACTERISTICS_NO_BIND),
    RESERVED_1024(OptHeaderWin32Fields.IMAGE_DLL_CHARACTERISTICS_RESERVED_1024),
    WDM_DRIVER(OptHeaderWin32Fields.IMAGE_DLL_CHARACTERISTICS_WDM_DRIVER),
    RESERVED_4096(OptHeaderWin32Fields.IMAGE_DLL_CHARACTERISTICS_RESERVED_4096),
    TERMINAL_SERVER_AWARE(OptHeaderWin32Fields.IMAGE_DLL_CHARACTERISTICS_TERMINAL_SERVER_AWARE);

    private final short _maskValue;
    public final short getMaskValue() {
        return _maskValue;
    }

    private final boolean   _reserved;
    public final boolean isReserved() {
        return _reserved;
    }

    OptHeaderDllCharacter(short maskValue) {
        _maskValue = maskValue;
        _reserved = name().startsWith("RESERVED");
    }

    public static final Set<OptHeaderDllCharacter>  VALUES=
            Collections.unmodifiableSet(EnumSet.allOf(OptHeaderDllCharacter.class));
    public static final Set<OptHeaderDllCharacter> fromCharacteristics(short maskValue) {
        Set<OptHeaderDllCharacter>  result=EnumSet.noneOf(OptHeaderDllCharacter.class);
        for (OptHeaderDllCharacter c : VALUES) {
            if ((c.getMaskValue() & maskValue) != 0) {
                result.add(c);
            }
        }

        return result;
    }
}
