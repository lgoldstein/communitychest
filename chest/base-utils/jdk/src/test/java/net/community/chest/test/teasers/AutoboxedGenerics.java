package net.community.chest.test.teasers;

import java.util.ArrayList;
import java.util.List;

import net.community.chest.test.TestBase;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>The dangers of using autoboxing in generics</P>
 * 
 * @author Lyor G.
 * @since Apr 1, 2008 11:52:10 AM
 */
public final class AutoboxedGenerics extends TestBase {
	@SuppressWarnings("boxing")
	public static void main (String[] args)
	{
		final List<Integer>	l=new ArrayList<Integer>();
		for (int	i=0; i < 10; i++)
			l.add(i);	// autoboxing
		System.out.println("List populated");

		for (int	i=0; i < 10; i++)
			l.remove(i);	// causes exception since there is a "remove(int)" method that is called...
		System.out.println("List cleared");
	}
}
