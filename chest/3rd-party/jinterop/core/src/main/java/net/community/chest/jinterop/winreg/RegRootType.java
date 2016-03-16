/*
 *
 */
package net.community.chest.jinterop.winreg;

import net.community.chest.lang.EnumUtil;

import org.jinterop.dcom.common.JIException;
import org.jinterop.winreg.IJIWinReg;
import org.jinterop.winreg.JIPolicyHandle;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Represents the registry top-level root(s) as {@link Enum}-s</P>
 * @author Lyor G.
 * @since May 19, 2009 12:41:21 PM
 */
public enum RegRootType {
    /**
     * HKEY_CLASSES_ROOT key
     */
    CLASSES {
            /*
             * @see net.community.chest.jinterop.winreg.RegRootType#openKey(org.jinterop.winreg.IJIWinReg)
             */
            @Override
            public JIPolicyHandle openKey (IJIWinReg reg) throws JIException
            {
                return (null == reg) ? null : reg.winreg_OpenHKCR();
            }
        },
    /**
     * HKEY_CURRENT_USER key
     */
    CURUSER {
            /*
             * @see net.community.chest.jinterop.winreg.RegRootType#openKey(org.jinterop.winreg.IJIWinReg)
             */
            @Override
            public JIPolicyHandle openKey (IJIWinReg reg) throws JIException
            {
                return (null == reg) ? null : reg.winreg_OpenHKCU();
            }
        },
    /**
     * HKEY_USERS key
     */
    USERS {
            /*
             * @see net.community.chest.jinterop.winreg.RegRootType#openKey(org.jinterop.winreg.IJIWinReg)
             */
            @Override
            public JIPolicyHandle openKey (IJIWinReg reg) throws JIException
            {
                return (null == reg) ? null : reg.winreg_OpenHKU();
            }
        },
    /**
     * HKEY_LOCAL_MACHINE key
     */
    LOCAL {
            /*
             * @see net.community.chest.jinterop.winreg.RegRootType#openKey(org.jinterop.winreg.IJIWinReg)
             */
            @Override
            public JIPolicyHandle openKey (IJIWinReg reg) throws JIException
            {
                return (null == reg) ? null : reg.winreg_OpenHKLM();
            }
        };
    /**
     * @param reg The {@link IJIWinReg} instance to use
     * @return The opened {@link JIPolicyHandle} to the root key (may be
     * <code>null</code> if no {@link IJIWinReg} instance provided)
     * @throws JIException If failed to open
     */
    public abstract JIPolicyHandle openKey (IJIWinReg reg) throws JIException;

    private static RegRootType[]    _values;
    public static final synchronized RegRootType[] getValues ()
    {
        if (null == _values)
            _values = values();
        return _values;
    }

    public static final RegRootType fromString (final String s)
    {
        return EnumUtil.fromString(getValues(), s, false);
    }
}
