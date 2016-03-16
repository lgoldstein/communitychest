/*
 *
 */
package net.community.chest.jta.xa;

import java.util.LinkedList;
import java.util.List;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>The adaptor implements the {@link javax.transaction.xa.XAResource} methods
 * without really managing a transaction. It just returns the values to the JTA
 * so it will think the transaction is managed. It differs from {@link ReadOnlyXAResource}
 * by the fact that it actually keeps track of the registered {@link Xid}-s</P>
 *
 * @author Lyor G.
 * @since Sep 2, 2008 12:11:39 PM
 */
public class EmptyXAResource extends ReadOnlyXAResource {
    /**
     * {@link Xid}-s registered via call to {@link #prepare(Xid)}
     */
    private final List<Xid> _preparedXids=new LinkedList<Xid>();
    /**
     * Default (empty) constructor
     */
    public EmptyXAResource ()
    {
        super();
    }
    /*
     * @see net.community.chest.jta.xa.ReadOnlyXAResource#commit(javax.transaction.xa.Xid, boolean)
     */
    @Override
    public void commit (Xid transID, boolean onePhase) throws XAException
    {
        if (transID != null)
            _preparedXids.remove(transID);
    }
    /*
     * @see net.community.chest.jta.xa.ReadOnlyXAResource#prepare(javax.transaction.xa.Xid)
     */
    @Override
    public int prepare (Xid transId) throws XAException
    {
        if (transId != null)
            _preparedXids.add(transId);
        return XA_OK;
    }
    /*
     * @see net.community.chest.jta.xa.ReadOnlyXAResource#recover(int)
     */
    @Override
    public Xid[] recover (int flags) throws XAException
    {
        final int    numXids=_preparedXids.size();
        if (numXids <= 0)    // we know this returns an empty array
            return super.recover(flags);

        return _preparedXids.toArray(new Xid[numXids]);
    }
    /*
     * @see net.community.chest.jta.xa.ReadOnlyXAResource#rollback(javax.transaction.xa.Xid)
     */
    @Override
    public void rollback (Xid transId) throws XAException
    {
        if (transId != null)
            _preparedXids.remove(transId);
    }
}
