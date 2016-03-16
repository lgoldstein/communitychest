/*
 *
 */
package net.community.chest.awt.event;

import java.util.Collection;
import java.util.EventListener;

import net.community.chest.util.compare.InstancesComparator;
import net.community.chest.util.set.UniqueInstanceSet;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <L> Type of {@link EventListener} being contained
 * @author Lyor G.
 * @since May 4, 2009 11:53:13 AM
 */
public class EventListenerSet<L extends EventListener> extends UniqueInstanceSet<L> {
    /**
     *
     */
    private static final long serialVersionUID = -2869064643422683090L;
    public EventListenerSet (InstancesComparator<? super L> c)
            throws IllegalArgumentException
    {
        super(c);
    }

    public EventListenerSet (Class<L> vc, Collection<? extends L> c)
            throws IllegalArgumentException
    {
        super(vc, c);
    }

    public EventListenerSet (Class<L> vc) throws IllegalArgumentException
    {
        super(vc);
    }

    private boolean _useCopyToFireEvents    /* =false */;
    public boolean isUseCopyToFireEvents ()
    {
        return _useCopyToFireEvents;
    }

    public void setUseCopyToFireEvents (boolean useCopyToFireEvents)
    {
        _useCopyToFireEvents = useCopyToFireEvents;
    }
}
