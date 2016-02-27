/*
 * 
 */
package net.community.chest.jms;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jms.DeliveryMode;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * Encapsulates the available {@link DeliveryMode} values as {@link Enum}
 * @author Lyor G.
 * @since Jun 16, 2010 11:24:10 AM
 */
public enum MessageDeliveryMode {
	PERSISTENT(DeliveryMode.PERSISTENT),
	NON_PERSISTENT(DeliveryMode.NON_PERSISTENT);
	
	private final int	_mode;
	public final int getMode ()
	{
		return _mode;
	}
	
	MessageDeliveryMode (final int mode)
	{
		_mode = mode;
	}
	
	public static final List<MessageDeliveryMode>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final MessageDeliveryMode fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
	
	public static final MessageDeliveryMode fromMode (final int m)
	{
		for (final MessageDeliveryMode v : VALUES)
		{
			if ((v != null) && (v.getMode() == m))
				return v;
		}

		return null;
	}
}
