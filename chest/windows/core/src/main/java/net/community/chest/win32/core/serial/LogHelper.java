/*
 *
 */
package net.community.chest.win32.core.serial;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Feb 19, 2013 2:50:23 PM
 *
 */
public abstract class LogHelper {
    protected LogHelper ()
    {
        super();
    }

    protected final void logInternal(String msg) {
        System.err.append('\t').append(getClass().getSimpleName()).append(": ++++++ ").println(msg);
    }

}
