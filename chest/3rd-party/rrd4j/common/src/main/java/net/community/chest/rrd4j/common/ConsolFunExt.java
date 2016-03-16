package net.community.chest.rrd4j.common;

import net.community.chest.dom.transform.DOMEnumExt;

import org.rrd4j.ConsolFun;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 8, 2008 1:55:27 PM
 */
public final class ConsolFunExt extends DOMEnumExt<ConsolFun> {
    private ConsolFunExt ()
    {
        super(ConsolFun.class);
    }

    public static final ConsolFunExt    DEFAULT=new ConsolFunExt();
}
