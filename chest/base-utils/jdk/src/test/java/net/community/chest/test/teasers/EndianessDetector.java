package net.community.chest.test.teasers;

import java.nio.ByteOrder;

import net.community.chest.test.TestBase;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 17, 2008 8:12:21 AM
 */
public class EndianessDetector extends TestBase {
	
	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
		System.out.println(ByteOrder.nativeOrder());
	}
}
