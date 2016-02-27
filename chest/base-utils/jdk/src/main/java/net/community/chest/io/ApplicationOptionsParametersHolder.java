/*
 * 
 */
package net.community.chest.io;

import java.util.List;
import java.util.Map;

import net.community.chest.util.MappedParametersHolder;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 24, 2010 10:28:59 AM
 */
public class ApplicationOptionsParametersHolder extends MappedParametersHolder<String,List<String>,Map<String,List<String>>> {
	public ApplicationOptionsParametersHolder (Map<String,List<String>> m)
	{
		super(m);
	}

	public ApplicationOptionsParametersHolder ()
	{
		this(null);
	}
	/*
	 * @see net.community.chest.util.MappedParametersHolder#getParameter(java.lang.String, java.lang.String)
	 */
	@Override
	public String getParameter (String paramName, String defValue)
	{
		final Map<String,List<String>>	m=getParametersMap();
		if ((m == null) || m.isEmpty()
		 || (paramName == null) || (paramName.length() <= 0))
			return defValue;

		final List<String>	vals=m.get(paramName);
		final int			numVals=(vals == null) ? 0 : vals.size();
		if (numVals <= 0)
			return defValue;

		return vals.get(0);
	}
}
