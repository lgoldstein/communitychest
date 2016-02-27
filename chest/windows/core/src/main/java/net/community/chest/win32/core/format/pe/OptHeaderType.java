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
 * @since Jun 10, 2013 10:36:29 AM
 */
public enum OptHeaderType {
    PE32(PEFormatDetails.PE32_MAGIC_NUMBER),
    PE64(PEFormatDetails.PE32PLUS_MAGIC_NUMBER);

    private final short _magicValue;
    public final short getMagicValue() {
        return _magicValue;
    }
    
    OptHeaderType(short magicValue) {
        _magicValue = magicValue;
    }
    
    public static final Set<OptHeaderType>  VALUES=
            Collections.unmodifiableSet(EnumSet.allOf(OptHeaderType.class));
    public static final OptHeaderType fromMagicNumber(short magicValue) {
        for (OptHeaderType type : VALUES) {
            if (type.getMagicValue() == magicValue) {
                return type;
            }
        }
        
        return null;
    }
}
