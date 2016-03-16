/*
 *
 */
package net.community.chest.spring.test;

import org.hibernate.EmptyInterceptor;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jan 20, 2011 11:02:08 AM
 */
public class DBSessionTrackerHelper {
    private final ApplicationContext    _context;
    public final ApplicationContext getApplicationContext ()
    {
        return _context;
    }

    public DBSessionTrackerHelper (ApplicationContext context)
    {
        if ((_context=context) == null)
            throw new IllegalStateException("No context provided");
    }


    private SessionFactory    _sessFactory;
    public SessionFactory getSessionFactory ()
    {
        if (_sessFactory == null)
        {
            final ApplicationContext    ctx=getApplicationContext();
            _sessFactory = (ctx == null) ? null : ctx.getBean(SessionFactory.class);
        }

        return _sessFactory;
    }

    private boolean    _txParticipating;
    // returns true if new session started, false if already in a session
    public boolean startSession ()
    {
        final SessionFactory    sessFac=getSessionFactory();
        if (TransactionSynchronizationManager.hasResource(sessFac))
        {
            // Do not modify the Session: just set the participate flag.
            if (!_txParticipating)
                _txParticipating = true;    // debug breakpoint

            return false;
        }
        // NOTE: the session factory interceptor is overridden by an empty one, because the
        // real interceptor may not function correctly in this test-specific setup.
        final Session session=
            SessionFactoryUtils.getSession(sessFac, EmptyInterceptor.INSTANCE, null);
        session.setFlushMode(FlushMode.AUTO);
        TransactionSynchronizationManager.bindResource(sessFac, new SessionHolder(session));
        return true;
    }

    // returns true if session actually closed
    public boolean endSession ()
    {
        if (_txParticipating)
            return false;

        final SessionFactory    sessFac=getSessionFactory();
        final SessionHolder     sessionHolder=
                (SessionHolder) TransactionSynchronizationManager.unbindResource(sessFac);
        SessionFactoryUtils.releaseSession(sessionHolder.getSession(), sessFac);
        return true;
    }
}
