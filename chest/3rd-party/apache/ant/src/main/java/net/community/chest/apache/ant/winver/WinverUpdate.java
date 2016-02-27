/*
 * 
 */
package net.community.chest.apache.ant.winver;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;

import net.community.chest.apache.ant.helpers.InMemoryFileContentsReplacer;

import org.apache.tools.ant.BuildException;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 7, 2009 11:15:04 AM
 */
public class WinverUpdate extends InMemoryFileContentsReplacer {
	public WinverUpdate ()
	{
		super();
	}

	protected String updateVersionValue (
			final File inFile, final String inData, final String verValue)
		throws IOException, BuildException
	{
		final int	inLen=(null == inData) ? 0 : inData.length();
		if ((inLen <= 0L) || (null == verValue) || (verValue.length() <= 0))
			return inData;

		Appendable	sb=null;
		int			lastPos=0;
		for (int	curPos=0; (curPos >= 0) && (curPos < inLen); )
		{
			final int	nextPos=inData.indexOf('\n', curPos);
			if (nextPos > curPos)
			{
				final String	lineData, eolValue;
				if ((nextPos > 0) && ('\r' == inData.charAt(nextPos - 1)))
				{
					lineData = inData.substring(curPos, nextPos - 1);
					eolValue = inData.substring(nextPos-1, nextPos + 1);
				}
				else
				{
					lineData = inData.substring(curPos, nextPos);
					eolValue = inData.substring(nextPos-1, nextPos);
				}

				final VersionResourceKeyword	kw=VersionResourceKeyword.fromLineData(lineData);
				if (kw != null)
				{
					final String	vv=kw.extractVersion(lineData);
					if (!verValue.equals(vv))
					{
						if (null == sb)
							sb = new StringBuilder(inLen + 32);

						if (lastPos < curPos)
						{
							final String	cpyVal=inData.substring(lastPos, curPos);
							sb.append(cpyVal);
						}

						final String	nl=kw.rebuildValue(lineData, verValue);
						sb.append(nl).append(eolValue);
						lastPos = nextPos + 1;
					}
					else
					{
		    			if (isVerboseMode())
		    				log("updateVersionValue(" + inFile + ")[" + kw + "] skipped " + vv + " (same value)", getVerbosity());
					}
				}
			}

			if (nextPos >= (inLen-1))
				break;

			curPos = nextPos + 1;
		}

		if (null == sb)
			return inData;	// nothing changed

		if (lastPos < inLen)
		{
			final String	cpyVal=inData.substring(lastPos);
			sb.append(cpyVal);
		}

		return sb.toString();
	}

	private VersionValue	_verValue;
	protected VersionValue getVersionValueInstance (final boolean createIfNotExist)
	{
		if ((null == _verValue) && createIfNotExist)
			_verValue = new VersionValue();
		return _verValue;
	}

	protected void setVersionValue (final VersionComponent vk, final int n)
	{
		if (!VersionComponent.isValidComponentNumber(n))
			throw new BuildException("setVersionValue(" + vk + ")[" + n + "] bad value", getLocation());

		final VersionValue	vv=getVersionValueInstance(true);
		final Number		pv=vv.put(vk, Integer.valueOf(n));
		if (pv != null)
			throw new BuildException("setVersionValue(" + vk + ")[" + n + "] value already set: " + pv, getLocation());
	}

	protected int getVersionValue (final VersionComponent vk)
	{
		final EnumMap<VersionComponent,? extends Number>	vm=
			(null == vk) ? null : getVersionValueInstance(false);
		final Number										n=
			((null == vm) || (vm.size() <= 0)) ? null : vm.get(vk);
		return (null == n) ? 0 : n.intValue();
	}

	public int getMajorValue ()
	{
		return getVersionValue(VersionComponent.MAJOR);
	}

	public void setMajorValue (int v)
	{
		setVersionValue(VersionComponent.MAJOR, v);
	}

	public int getMinorValue ()
	{
		return getVersionValue(VersionComponent.MINOR);
	}

	public void setMinorValue (int v)
	{
		setVersionValue(VersionComponent.MINOR, v);
	}

	public int getReleaseValue ()
	{
		return getVersionValue(VersionComponent.RELEASE);
	}

	public void setReleaseValue (int v)
	{
		setVersionValue(VersionComponent.RELEASE, v);
	}

	public int getBuildValue ()
	{
		return getVersionValue(VersionComponent.BUILD);
	}

	public void setBuildValue (int v)
	{
		setVersionValue(VersionComponent.BUILD, v);
	}

	public void setVersionValue (final String s)
	{
		final VersionValue			vv=new VersionValue(s);
		for (final VersionComponent vk : VersionComponent.VALUES)
		{
			final Number	n=vv.getResolvedValue(vk);
			setVersionValue(vk, n.intValue());
		}
	}
	/*
	 * @see net.community.chest.apache.ant.helpers.InMemoryFileContentsReplacer#replaceProperties(java.io.File, java.lang.String)
	 */
	@Override
	protected String replaceProperties (final File inFile, final String inData)
		throws IOException, BuildException
	{
		final VersionValue	vv=getVersionValueInstance(false);
		final String		vs=(null == vv) ? null : vv.toVersionString();
		if ((null == vs) || (vs.length() <= 0))
			throw new BuildException("No version value set", getLocation());

		return updateVersionValue(inFile, inData, vs);
	}
}
