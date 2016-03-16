/*
 *
 */
package net.community.chest.awt.event;

import java.awt.event.KeyListener;

import net.community.chest.util.compare.InstancesComparator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 6, 2009 11:09:39 AM
 */
public class KeyListenerInstanceComparator extends InstancesComparator<KeyListener> {
    /**
     *
     */
    private static final long serialVersionUID = 9120978789318406033L;

    public KeyListenerInstanceComparator ()
    {
        super(KeyListener.class);
    }

    public static final KeyListenerInstanceComparator    DEFAULT=new KeyListenerInstanceComparator();
}
