package net.community.chest.jmx.test;

import net.community.chest.jmx.StaticMBeanClassEmbedder;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 14, 2007 3:47:48 PM
 */
public class StaticEmbedderTester extends StaticMBeanClassEmbedder<JMXTester> {
	public StaticEmbedderTester ()
	{
		super(JMXTester.class);
	}
}
