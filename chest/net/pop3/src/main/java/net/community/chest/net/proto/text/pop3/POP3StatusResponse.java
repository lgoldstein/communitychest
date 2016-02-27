package net.community.chest.net.proto.text.pop3;

import net.community.chest.ParsableString;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Special class used for the POP3 STATUS command response</P>
 * @author Lyor G.
 * @since Sep 19, 2007 10:45:36 AM
 */
public class POP3StatusResponse extends POP3Response {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2961475801385609775L;
	public POP3StatusResponse ()
	{
		super();
	}
	/**
	 * Reported number of messages (<0) if error
	 */
	private int	_numMsgs=(-1);
	public int getNumMsgs ()
	{
		return _numMsgs;
	}

	public void setNumMsgs (int numMsgs)
	{
		_numMsgs = numMsgs;
	}
	/**
	 * Total mailbox size (bytes) - or (<0) if error
	 */
	private long	_mboxSize=(-1L);
	public long getMboxSize ()
	{
		return _mboxSize;
	}

	public void setMboxSize (long mboxSize)
	{
		_mboxSize = mboxSize;
	}
    /*
	 * @see net.community.chest.net.proto.text.pop3.POP3Response#reset()
	 */
	@Override
	public void reset ()
	{
		setNumMsgs(-1);
		setMboxSize(-1L);
		super.reset();
	}
	/* @return TRUE if both ERR or both OK and same values
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals (Object obj)
    {
		final Class<?>	oc=(obj == null) ? null : obj.getClass();
		if (oc != getClass())
			return false;
		if (this == obj)
			return true;

        final POP3StatusResponse	other=(POP3StatusResponse) obj;
        if (!isSameResponse(other))
        	return false;

        {
        	final int	numMsgs=getNumMsgs(), otherNum=other.getNumMsgs();
        	if (numMsgs >= 0)
        	{
        		if (otherNum != numMsgs)
        			return false;
        	}
        	else	// make sure other is error
        	{
        		if (otherNum >= 0)
        			return false;
        	}
        }

        {
        	final long	mbxSize=getMboxSize(), otherSize=other.getMboxSize();
        	if (mbxSize >= 0L)
        	{
        		if (otherSize != mbxSize)
        			return false;
        	}
        	else	// make sure other is error
        	{
        		if (otherSize >= 0L)
        			return false;
        	}
        }

        return true;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
	public int hashCode ()
    {
        return super.hashCode()
         	 + Math.max(getNumMsgs(), 0)
         	 + (int) (Math.max(getMboxSize(), 0L) & 0x7FFFFFFF)
         	 ;
    }
	/**
	 * Checks if character sequence is a POP3 STAT(us) response line. If so,
	 * returns a matching object (or null)
	 * @param org original instance to be updated - if valid STAT(us) response
	 * and null then a new one is created. Otherwise, the provided one is
	 * updated (after calling {@link #reset()}).
	 * @param rspLine response line to be checked
	 * @return response object (null if not a POP3 STAT(us) response line)
	 * @throws NumberFormatException if bad/illegal numbers in (+OK) response line
	 */
    public static final POP3StatusResponse getStatusResponse (final POP3StatusResponse	org, final CharSequence rspLine) throws NumberFormatException
    {
		final int	errCode=isOKResponse(rspLine);
		if (errCode < 0)
			return null;

		final POP3StatusResponse	rsp=(null == org) ? new POP3StatusResponse() : org;
		if (rsp == org)
			rsp.reset();
		rsp.setResponseAndLine((0 == errCode), rspLine.toString());

		// if got "+OK" response then get resulting data
		if (0 == errCode)
		{
			final ParsableString	ps=new ParsableString(rspLine, POP3Protocol.POP3_OKChars.length, rspLine.length() - POP3Protocol.POP3_OKChars.length);
			int						curPos=ps.findNonEmptyDataStart(),
									nextPos=ps.findNonEmptyDataEnd(curPos+1);
			rsp.setNumMsgs(ps.getUnsignedInt(curPos, nextPos));
			
			curPos = ps.findNonEmptyDataStart(nextPos+1);
			nextPos = ps.findNonEmptyDataEnd(curPos+1);
			
			rsp.setMboxSize(ps.getUnsignedLong(curPos, nextPos));
		}
		
		return rsp;
    }
	/**
	 * Checks if character sequence is a POP3 STAT(us) response line. If so,
	 * returns a matching object (or null)
	 * @param rspLine response line to be checked
	 * @return response object (null if not a POP3 STAT(us) response line)
	 * @throws NumberFormatException if bad/illegal numbers in (+OK) response line
	 */
	public static final POP3StatusResponse getStatusResponse (final CharSequence rspLine) throws NumberFormatException
	{
		return getStatusResponse(null, rspLine);
	}
}
