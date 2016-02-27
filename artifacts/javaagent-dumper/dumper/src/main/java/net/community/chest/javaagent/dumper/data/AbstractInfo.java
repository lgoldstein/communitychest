/*
 * 
 */
package net.community.chest.javaagent.dumper.data;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Comparator;

import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 14, 2011 3:09:13 PM
 */
public abstract class AbstractInfo implements Serializable {
	private static final long serialVersionUID = 103144398742360748L;

	protected AbstractInfo ()
	{
		super();
	}

	private String	_name;
	public String getName ()
	{
		return _name;
	}

	public void setName (String name)
	{
		_name = name;
	}

	public static final Comparator<AbstractInfo>	BY_NAME_COMP=new Comparator<AbstractInfo>() {
			/*
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare (AbstractInfo o1, AbstractInfo o2)
			{
				final String	n1=(o1 == null) ? null : o1.getName(),
								n2=(o2 == null) ? null : o2.getName();
				return StringUtil.compareDataStrings(n1, n2, true);
			}
		};
	/**
	 * @see {@link Modifier} for available values
	 */
	private int	_modifiers;
	public int getModifiers ()
	{
		return _modifiers;
	}

	public void setModifiers (int modifiers)
	{
		_modifiers = modifiers;
	}

	public final boolean isPublic ()
	{
		return Modifier.isPublic(getModifiers());
	}

	public final boolean isStatic ()
	{
		return Modifier.isStatic(getModifiers());
	}
	
	public final boolean isAbstract ()
	{
		return Modifier.isAbstract(getModifiers());
	}
}
