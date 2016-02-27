/*
 * 
 */
package net.community.apps.tools.svn.resources;

import java.util.Map;

import javax.swing.Icon;

import net.community.apps.common.resources.BaseAnchor;
import net.community.chest.awt.attributes.AttrUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.svnkit.core.wc.SVNStatusTypeEnum;
import net.community.chest.util.map.MapsUtils;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 10:05:01 AM
 */
public class DefaultResourcesAnchor extends BaseAnchor {
	private DefaultResourcesAnchor ()
	{
		super();
	}

	private Map<String,Element>	_statusValuesMap;
	public synchronized Map<String,Element> getStatusValuesMap ()
	{
		if (null == _statusValuesMap)
			_statusValuesMap = getClassValuesMap(SVNStatusTypeEnum.class);
		return _statusValuesMap;
	}

	private Map<SVNStatusTypeEnum,Icon>	_statusIconsMap;
	public synchronized Map<SVNStatusTypeEnum,Icon> getStatusIconsMap ()
	{
		if (null == _statusIconsMap)
		{
			try
			{
				final Map<String,? extends Element>	em=getStatusValuesMap();
				final Map<String,? extends Icon>	im=
					AttrUtils.getMappedIcons(em, this);
				_statusIconsMap = MapsUtils.toEnumStringMap(im, SVNStatusTypeEnum.class);
			}
			catch(Exception e)
			{
				throw ExceptionUtil.toRuntimeException(e);
			}
		}

		return _statusIconsMap;
	}

	private static DefaultResourcesAnchor	_instance;
	public static final synchronized DefaultResourcesAnchor getInstance ()
	{
		if (_instance == null)
			_instance = new DefaultResourcesAnchor();
		return _instance;
	}
}
