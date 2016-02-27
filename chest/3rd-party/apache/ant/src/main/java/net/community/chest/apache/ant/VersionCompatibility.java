/*
 * 
 */
package net.community.chest.apache.ant;

import org.apache.tools.ant.BuildException;

import net.community.chest.apache.ant.helpers.AbstractPropConditionTask;
import net.community.chest.util.compare.VersionComparator;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Can be used to check if a version matches some requirement - e.g.,
 * required="1.3+" matches all the following values: 1.3, 1.4, 2.1, etc.</P>
 * 
 * @author Lyor G.
 * @since Jul 12, 2009 9:42:13 AM
 */
public class VersionCompatibility extends AbstractPropConditionTask {
	public VersionCompatibility ()
	{
		super();
	}
	/**
	 * Required version expression
	 */
	private String	_requiredVersion;
	public String getRequiredVersion ()
	{
		return _requiredVersion;
	}

	public void setRequiredVersion (String v)
	{
		_requiredVersion = v;
	}
	/**
	 * The actual version value
	 */
	private String	_versionValue;
	public String getVersionValue ()
	{
		return _versionValue;
	}

	public void setVersionValue (String v)
	{
		_versionValue = v;
	}
	/*
	 * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
	 */
	@Override
	public boolean eval () throws BuildException
	{
		final String	req=getRequiredVersion(),
						ver=getVersionValue();
		if ((null == req) || (req.length() <= 0)
		 || (null == ver) || (ver.length() <= 0))
			throw new BuildException("Incomplete specification", getLocation());

		try
		{
			final int	nRes=VersionComparator.compareVersionCompatibility(ver, req);
			if (isVerboseMode())
				log("eval(" + ver + ")[" + req + "] - result=" + nRes);
			return (0 == nRes);
		}
		catch(NumberFormatException e)
		{
			throw new BuildException(e.getMessage(), e, getLocation());
		}
	}

}
