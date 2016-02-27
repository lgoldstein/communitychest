package net.community.chest.test.teasers;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Causes infinite loop after 1st even number since {@link Iterator#next()} is no longer called</O>
 * @author Lyor G.
 * @since Mar 31, 2008 3:30:02 PM
 */
public class OddIteratorBehavior {
	private static boolean isOdd(Integer i)
	{
		return (i != null) && ((i.intValue() & 1) != 0);
	}

	public static void main (String[] args)
	{
		final List<Integer> list = Arrays.asList(
				Integer.valueOf(-2),
				Integer.valueOf(-1),
				Integer.valueOf(0),
				Integer.valueOf(1),
				Integer.valueOf(2)
			);

		boolean foundOdd=false;
		for (Iterator<Integer> it = list.iterator(); it.hasNext(); )
			foundOdd = foundOdd || isOdd(it.next());

		System.out.println(foundOdd);
	}
}
