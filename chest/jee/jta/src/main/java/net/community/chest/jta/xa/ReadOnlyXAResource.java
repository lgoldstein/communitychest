/*
 *
 */
package net.community.chest.jta.xa;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Does nothing and reports {@link javax.transaction.xa.XAResource#XA_RDONLY} as result of
 * {@link #prepare(Xid)} call</P>
 *
 * @author Lyor G.
 * @since Sep 2, 2008 12:08:59 PM
 */
public class ReadOnlyXAResource extends AbstractXAResource {
    /**
     * Default (empty) constructor
     */
    public ReadOnlyXAResource ()
    {
        super();
    }
    /*
     * @see javax.transaction.xa.XAResource#commit(javax.transaction.xa.Xid, boolean)
     */
    @Override
    public void commit (Xid transID, boolean onePhase) throws XAException
    {
        // since we do not manage a real transaction, we do nothing here.
    }
    /*
     * @see javax.transaction.xa.XAResource#end(javax.transaction.xa.Xid, int)
     */
    @Override
    public void end (Xid transId, int flags) throws XAException
    {
        // since we do not manage a real transaction, we do nothing here.
    }
    /*
     * @see javax.transaction.xa.XAResource#forget(javax.transaction.xa.Xid)
     */
    @Override
    public void forget (Xid transId) throws XAException
    {
        // since we do not manage a real transaction, we do nothing here.
    }
    /*
     * @see javax.transaction.xa.XAResource#prepare(javax.transaction.xa.Xid)
     */
    @Override
    public int prepare(Xid transId) throws XAException
    {
        /*
         * Since we do not manage a transaction, we return XA_RDONLY, which
         * indicates that our branch had performed read-only operations and
         * the transaction has been committed.
         */
        return XA_RDONLY;
    }
    // TODO check if we can use a STATIC member for this
    private final Xid[] _xids=new Xid[0];
    /*
     * @see javax.transaction.xa.XAResource#recover(int)
     */
    @Override
    public Xid[] recover (int flags) throws XAException
    {
        return _xids;
    }
    /*
     * @see javax.transaction.xa.XAResource#rollback(javax.transaction.xa.Xid)
     */
    @Override
    public void rollback (Xid transId) throws XAException
    {
        // since we do not manage a real transaction, we do nothing here.
    }
    /*
     * @see javax.transaction.xa.XAResource#start(javax.transaction.xa.Xid, int)
     */
    @Override
    public void start (Xid transId, int flags) throws XAException
    {
        // since we do not manage a real transaction, we do nothing here.
    }
}
