/*
 *
 */
package net.community.chest.jinterop.test;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.jinterop.dcom.common.IJIAuthInfo;
import org.jinterop.winreg.IJIWinReg;
import org.jinterop.winreg.JIPolicyHandle;
import org.jinterop.winreg.JIWinRegFactory;

import net.community.chest.jinterop.winreg.RegRootType;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 19, 2009 12:51:51 PM
 */
public class WinRegTest extends JITestBase {
    protected WinRegTest ()
    {
        super();
    }

    /* -------------------------------------------------------------------- */

    // returns null if Quit
    public static final RegRootType getSelectedRegistryRoot (
            final PrintStream out, final BufferedReader in, final String orgPrompt, final RegRootType defValue)
    {
        final String    prompt=
                ((null == orgPrompt) || (orgPrompt.length() <= 0)) ? "" : (orgPrompt + " ")
            + "(C)lasses/(U)sers/(L)ocal Machine/Curren(t) user"
            + ((null == defValue) ? "" : "[ENTER=" + defValue + "]")
            ;
        for ( ; ; )
        {
            final String    ans=getval(out, in, prompt);
            if ((null == ans) || (ans.length() <= 0))
            {
                if (defValue != null)
                    return defValue;
                continue;
            }
            if (isQuit(ans)) return null;

            final char    rc=Character.toLowerCase(ans.charAt(0));
            switch(rc)
            {
                case 'c'    :    return RegRootType.CLASSES;
                case 'u'    :    return RegRootType.USERS;
                case 'l'    :    return RegRootType.LOCAL;
                case 't'    :    return RegRootType.CURUSER;
                default        :    // do nothing
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////

    public static final void testRegistryAccess (
            final PrintStream out, final BufferedReader in,
            final IJIWinReg registry, final String keyPath, final JIPolicyHandle keyHandle)
    {
        out.println(keyPath);

        for ( ; ; )
        {
            final String    ans=getval(out, in, "[V]alues/Sub-(K)eys/(G)et value/(S)et value/(Q)uit");
            if (isQuit(ans)) break;

            final char    op=
                ((null == ans) || (ans.length() <= 0)) ? '\0' : Character.toUpperCase(ans.charAt(0));
            try
            {
                switch(op)
                {
                    case '\0'    :
                    case 'V'    :
                        for (int    vIndex=0; ; vIndex++)
                        {
                            final Object[]    vpa=registry.winreg_EnumValue(keyHandle, vIndex);
                            if ((null == vpa) || (vpa.length <= 0))
                                break;

                            for (final Object o : vpa)
                            {
                                final Class<?>    oc=(null == o) ? null : o.getClass();
                                out.print("\t[" +  ((null == oc) ? null : oc.getName()) + "]=" + o);
                            }
                            out.println();
                        }
                        break;

                    case 'K'    :
                        for (int    vIndex=0; ; vIndex++)
                        {
                            final Object[]    vpa=registry.winreg_EnumKey(keyHandle, vIndex);
                            if ((null == vpa) || (vpa.length <= 0))
                                break;

                            for (final Object o : vpa)
                            {
                                final Class<?>    oc=(null == o) ? null : o.getClass();
                                out.print("\t[" +  ((null == oc) ? null : oc.getName()) + "]=" + o);
                            }
                            out.println();
                        }
                        break;

                    case 'G'    :
                        break;
                    case 'S'    :
                        break;
                    default        : // ignored
                }
            }
            catch(Exception e)
            {
                System.err.println(e.getClass().getName() + " while access key=" + keyPath + ": " + e.getMessage());
            }
        }
    }
    /* -------------------------------------------------------------------- */

    // args: [0]=domain, [1]=username, [2]=password, [3]=root key [4,...] sub-key paths
    public static final void testRegistryAccess (
            final PrintStream out, final BufferedReader in, final String ... args)
    {
        final Map.Entry<IJIAuthInfo,List<String>>    ap=getAuthenticationParams(out, in, args);
        final IJIAuthInfo                            ai=(null == ap) ? null : ap.getKey();
        if (null == ai)
            return;

        final List<String>    al=ap.getValue();
        final int            numArgs=(null == al) ? 0 : al.size();
        try
        {
            final RegRootType    rootKey;
            if (numArgs > 0)
            {
                final String    n=al.remove(0);
                if (null == (rootKey=RegRootType.fromString(n)))
                    throw new NoSuchElementException("Unknown root key: " + n);
            }
            else
            {
                if (null == (rootKey=getSelectedRegistryRoot(out, in, "root key", null)))
                    return;
            }

            final JIWinRegFactory    fac=JIWinRegFactory.getSingleTon();
            final IJIWinReg         registry=fac.getWinreg(ai, "localhost", true);
            final JIPolicyHandle    rootHandle=rootKey.openKey(registry);
            try
            {
                for (int aIndex=0; ; aIndex++)
                {
                    final String    keyPath=
                        (aIndex < numArgs) ? al.get(aIndex) : getval(out, in, "sub-key path (or Quit)");
                    if ((null == keyPath) || (keyPath.length() <= 0))
                        continue;
                    if (isQuit(keyPath))
                        break;

                    try
                    {
                        final JIPolicyHandle    keyHandle=
                            registry.winreg_OpenKey(rootHandle, keyPath, IJIWinReg.KEY_ALL_ACCESS);
                        try
                        {
                            testRegistryAccess(out, in, registry, keyPath, keyHandle);
                        }
                        finally
                        {
                            registry.winreg_CloseKey(keyHandle);
                        }
                    }
                    catch(Exception e)
                    {
                        System.err.println(e.getClass().getName() + " while access key=" + keyPath + ": " + e.getMessage());
                    }
                }
            }
            finally
            {
                registry.winreg_CloseKey(rootHandle);
            }
        }
        catch(Exception e)
        {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    //////////////////////////////////////////////////////////////////////////

    public static final void main (String[] args)
    {
        testRegistryAccess(System.out, getStdin(), args);
    }
}
