/**
 * 
 */
package net.community.apps.apache.maven.pom2cpsync;

import java.awt.Color;

import net.community.chest.apache.maven.helpers.BaseTargetDetails;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 17, 2008 3:23:06 PM
 */
public class DependencyTargetEntry extends BaseTargetDetails {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4907641112588252748L;
	public DependencyTargetEntry ()
	{
		super();
	}

	public DependencyTargetEntry (Element elem) throws Exception
	{
		super(elem);
	}
	// 							not found in target 		version mismatch
	public static final Color	NO_ENTRY_COLOR=Color.RED, BAD_VERSION_COLOR=Color.BLUE;
	// null == match OK
	private Color	_matchColor	/* =null */;
	public Color getMatchColor ()
	{
		return _matchColor;
	}

	public void setMatchColor (Color c)
	{
		_matchColor = c;
	}

	public DependencyTargetEntry (BaseTargetDetails d, Color c)
	{
		setGroupId((null == d) ? null : d.getGroupId());
		setArtifactId((null == d) ? null : d.getArtifactId());
		setVersion((null == d) ? null : d.getVersion());
		_matchColor = c;
	}
	
	public DependencyTargetEntry (BaseTargetDetails d)
	{
		this(d, null);
	}
}
