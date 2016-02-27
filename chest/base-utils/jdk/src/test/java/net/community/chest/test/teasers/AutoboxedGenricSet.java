package net.community.chest.test.teasers;

import java.util.HashSet;
import java.util.Set;

import net.community.chest.test.TestBase;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Shows the autoboxing peril of a calculated value</P>
 * @author Lyor G.
 * @since Apr 21, 2008 1:00:22 PM
 */
public class AutoboxedGenricSet extends TestBase {
	@SuppressWarnings("boxing")
	public static void main (String[] args)
	{
		final Set<Short>	s=new HashSet<Short>();
		for (short i=0; i < 100; i++)
			s.add(i);
		for (short i=1; i <= 100; i++)
			s.remove(i - 1);

		System.out.println("There are " + s.size() + " components in the set");
	}
}
