/*
 * 
 */
package net.community.chest.apache.ant.mvnsync;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.w3c.dom.Document;

import net.community.chest.apache.ant.ReplaceProps;
import net.community.chest.apache.ant.mvnsync.helpers.Dependency;
import net.community.chest.apache.maven.helpers.BuildDependencyDetails;
import net.community.chest.apache.maven.helpers.BuildProject;
import net.community.chest.apache.maven.helpers.DependencyComparator;
import net.community.chest.apache.maven.helpers.DependencyFieldValue;
import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.StringUtil;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 9, 2009 1:59:16 PM
 */
public class UpdatePOMDependencies extends ReplaceProps {
	public UpdatePOMDependencies ()
	{
		super();
	}

	private boolean	_autoReplaceProperties=true;
	public boolean isAutoReplaceProperties ()
	{
		return _autoReplaceProperties;
	}

	public void setAutoReplaceProperties (boolean autoReplaceProperties)
	{
		_autoReplaceProperties = autoReplaceProperties;
	}

	private Collection<Dependency>	_deps	/* =null */;
	public Collection<Dependency> getDependenciesList ()
	{
		return _deps;
	}

	public void addConfiguredDependency (Dependency d)
	{
		if (null == _deps)
			_deps = new LinkedList<Dependency>();
		_deps.add(d);
	}

	protected Map.Entry<String,String> updateField (
			final DependencyFieldValue 		fldType,
			final BuildDependencyDetails	curVal,
			final BuildDependencyDetails	newVal)
	{
		if ((null == fldType) || (null == curVal) || (null == newVal))
			return null;

		final String	cv=fldType.getValue(curVal),
						nv=fldType.getValue(newVal);
		if ((null == nv) || (nv.length() <= 0))
			return null;	// OK if no value override supplied

		final boolean	isMatch, isRemove;
		if ("--REMOVE--".equalsIgnoreCase(nv))
		{
			isMatch = ((null == cv) || (cv.length() <= 0));
			isRemove = true;
		}
		else
		{
			isMatch = (0 == StringUtil.compareDataStrings(cv, nv, true));
			isRemove = false;
		}

		if (isMatch)
			return null;

		final String	sv=isRemove ? null : nv;
		fldType.setValue(curVal, sv);
		return new MapEntryImpl<String,String>(cv, sv);
	}

	private static final DependencyFieldValue[]	DEFAULT_UPDATE_FIELDS={
			DependencyFieldValue.VERSION,
			DependencyFieldValue.SCOPE,
			DependencyFieldValue.SYSTEMPATH
		};
	protected BuildDependencyDetails mergeUpdates (final BuildDependencyDetails curVal,
												   final BuildDependencyDetails	newVal)
	{
		if ((null == curVal) || (null == newVal))
			return curVal;

		for (final DependencyFieldValue f : DEFAULT_UPDATE_FIELDS)
			updateField(f, curVal, newVal);
		return curVal;
	}
	/*
	 * @see net.community.chest.apache.ant.ReplaceProps#replaceProperties(java.io.File, java.lang.String)
	 */
	@Override
	protected String replaceProperties (final File inFile, final String inData) throws BuildException
	{
		final String	repData=
			isAutoReplaceProperties() ? super.replaceProperties(inFile, inData) : inData;

		final BuildProject	proj;
		final Document		doc;
		try
		{
			doc = DOMUtils.loadDocumentFromString(repData);
			proj = new BuildProject(doc);
		}
		catch(Exception e)
		{
			throw new BuildException("Failed to load replaced data from file=" + inFile + ": " + e.getMessage(), e, getLocation());
		}

		final Collection<? extends BuildDependencyDetails>	pdl=proj.getProjectDependencies(),
															rdl=getDependenciesList();
		if ((null == pdl) || (pdl.size() <= 0)
		 || (null == rdl) || (rdl.size() <= 0))
			return repData;

		Collection<Map.Entry<BuildDependencyDetails,BuildDependencyDetails>>	cl=null;
		for (final BuildDependencyDetails pd : pdl)
		{
			final BuildDependencyDetails	ovd=
				CollectionsUtils.findElement(rdl, pd, DependencyComparator.ASCENDING);
			if ((null == ovd) || ovd.equals(pd))	// OK if no override or if same
				continue;

			if (isVerboseMode())
				log("\tChange " + pd + " => " + ovd);

			final BuildDependencyDetails	nvd=mergeUpdates(pd, ovd);
			if (null == nvd)
				continue;

			if (null == cl)
				cl = new LinkedList<Map.Entry<BuildDependencyDetails,BuildDependencyDetails>>();
			cl.add(new MapEntryImpl<BuildDependencyDetails,BuildDependencyDetails>(pd, nvd));
		}

		if ((null == cl) || (cl.size() <= 0))	// OK if no further changes
		{
			if (isVerboseMode())
				log("No dependencies updated", getVerbosity());
			return repData;
		}
		// TODO update the Element(s)
		return repData;
	}
	/*
	 * @see net.community.chest.apache.ant.helpers.AbstractFilesInputTask#execute()
	 */
	@Override
	public void execute () throws BuildException
	{
		final Collection<?>	dl=getDependenciesList();
		if ((null == dl) || (dl.size() <= 0))
			throw new BuildException("No dependencies configured", getLocation());

		super.execute();
	}
}
