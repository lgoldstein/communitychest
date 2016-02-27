package net.community.chest.jmx.test;

import net.community.chest.jmx.FolderMBeanDescriptorAccessor;
import net.community.chest.jmx.XmlDynamicMBeanEmbedder;
import net.community.chest.jmx.XmlMBeanDescriptorAccessor;
import net.community.chest.lang.SysPropsEnum;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 19, 2007 12:24:00 PM
 */
public class XmlJMXTesterEmbedder extends XmlDynamicMBeanEmbedder<JMXTester> {
	public XmlJMXTesterEmbedder ()
	{
		super(JMXTester.class);
	}

	private XmlMBeanDescriptorAccessor	_acc	/* =null */;
	/*
	 * @see net.community.chest.jmx.XmlDynamicMBeanEmbedder#getDescriptorAccessor()
	 */
	@Override
	public synchronized XmlMBeanDescriptorAccessor getDescriptorAccessor ()
	{
		if (null == _acc)
		{
			final String	path=SysPropsEnum.JAVAIOTMPDIR.getPropertyValue();
			_acc = new FolderMBeanDescriptorAccessor(path);
		}

		return _acc;
	}

}
