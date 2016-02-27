/*
 * 
 */
package net.community.chest.awt.event;

import java.awt.AWTEvent;
import java.awt.Component;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2009 8:26:17 AM
 */
public class UserDefinedEvent extends AWTEvent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4509401053302167013L;
	private List<?>	_args;
	public List<?> getArguments ()
	{
		return _args;
	}

	public void setArguments (List<?> args)
	{
		_args = args;
	}

	public void setArguments (Object ... args)
	{
		setArguments(((null == args) || (args.length <= 0)) ? null : Arrays.asList(args));
	}

	public Component getTarget ()
	{
		return (Component) getSource();
	}

	public void setTarget (Component target)
	{
		setSource(target);
	}

	public UserDefinedEvent (Component target /* not null */, int eventId, List<?> args) throws IllegalArgumentException
	{
		super(target, eventId);
		_args = args;
	}

	public UserDefinedEvent (Component target /* not null */, int eventId, Object ... args) throws IllegalArgumentException
	{
		this(target, eventId, ((null == args) || (args.length <= 0)) ? null : Arrays.asList(args));
	}
	
	public UserDefinedEvent (Component target /* not null */, int eventId) throws IllegalArgumentException
	{
		this(target, eventId, (List<?>) null);
	}
	/*
	 * @see java.awt.AWTEvent#paramString()
	 */
	@Override
	public String paramString ()
	{
		final Collection<?>	al=getArguments();
		final int			numArgs=(null == al) ? 0 : al.size();
		final StringBuilder	sb=(numArgs <= 0) ? null : new StringBuilder(numArgs * 32);
		if (numArgs > 0)
		{
			for (final Object o : al)
			{
				if (sb.length() > 0)
					sb.append(',');
				sb.append(o);
			}
		}

		return ((null == sb) || (sb.length() <= 0)) ? "" : sb.toString();
	}
}
