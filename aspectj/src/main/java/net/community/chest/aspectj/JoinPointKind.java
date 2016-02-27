/*
 * 
 */
package net.community.chest.aspectj;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.aspectj.lang.JoinPoint;

/**
 * <P>Copyright as per GPLv2</P>
 * Encapsulates the available kind strings defined in {@link JoinPoint} as {@link Enum}-s
 * @author Lyor G.
 * @since Mar 31, 2011 10:04:08 AM
 */
public enum JoinPointKind {
	METHOD_EXECUTION(JoinPoint.METHOD_EXECUTION),
	METHOD_CALL(JoinPoint.METHOD_CALL),
	CONSTRUCTOR_EXECUTION(JoinPoint.CONSTRUCTOR_EXECUTION),
	CONSTRUCTOR_CALL(JoinPoint.CONSTRUCTOR_CALL),
	FIELD_GET(JoinPoint.FIELD_GET),
	FIELD_SET(JoinPoint.FIELD_SET),
	STATICINITIALIZATION(JoinPoint.STATICINITIALIZATION),
	PREINITIALIZATION(JoinPoint.PREINITIALIZATION),
	INITIALIZATION(JoinPoint.INITIALIZATION),
	EXCEPTION_HANDLER(JoinPoint.EXCEPTION_HANDLER),
	SYNCHRONIZATION_LOCK(JoinPoint.SYNCHRONIZATION_LOCK),
	SYNCHRONIZATION_UNLOCK(JoinPoint.SYNCHRONIZATION_UNLOCK),
	ADVICE_EXECUTION(JoinPoint.ADVICE_EXECUTION);

    private final String	_kind;
    public final String getKind ()
    {
    	return _kind;
    }
    
    JoinPointKind (String kind)
    {
    	_kind = kind;
    }
	/*
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString ()
	{
		return getKind();
	}

	public static final List<JoinPointKind>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final JoinPointKind fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}
	
	public static final JoinPointKind fromKind (final String kind)
	{
		if ((kind == null) || (kind.length() <= 0))
			return null;
		
		for (final JoinPointKind v : VALUES)
		{
			final String	vk=(v == null) /* should not happen */ ? null : v.getKind();
			if (kind.equalsIgnoreCase(vk))
				return v;
		}

		return null;
	}

	public static final JoinPointKind fromJoinPoint (final JoinPoint jp)
	{
		return (jp == null) ? null : fromKind(jp.getKind());
	}
}
