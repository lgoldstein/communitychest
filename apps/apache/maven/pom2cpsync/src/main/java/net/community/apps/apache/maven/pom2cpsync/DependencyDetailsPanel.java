/*
 * 
 */
package net.community.apps.apache.maven.pom2cpsync;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLabel;

import net.community.chest.apache.maven.helpers.BaseTargetDetails;
import net.community.chest.awt.attributes.Iconable;
import net.community.chest.awt.attributes.Textable;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.swing.component.panel.BasePanel;
import net.community.chest.swing.component.table.DefaultTableScroll;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 14, 2008 2:26:40 PM
 */
public abstract class DependencyDetailsPanel extends BasePanel implements Iconable, Textable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8610128912355597614L;
	private final DependencyDetailsTable	_depsTbl;
	public final DependencyDetailsTable getDependencyDetailsTable ()
	{
		return _depsTbl;
	}

	public void setDependencies (final Collection<? extends DependencyTargetEntry> deps)
	{
		final DependencyDetailsTable	tbl=getDependencyDetailsTable();
		tbl.setDependencies(deps);
	}

	public final List<DependencyTargetEntry> getDependencies ()
	{
		final DependencyDetailsTable	tbl=getDependencyDetailsTable();
		return (null == tbl) ? null : tbl.getTypedModel();
	}

	private final JLabel	_filePath;
	public String getFilePath ()
	{
		return (null == _filePath) ? null : _filePath.getText();
	}

	public void setFilePath (String filePath)
	{
		if (_filePath != null)
			_filePath.setText((null == filePath) ? "" : filePath);
	}
	/*
	 * @see net.community.chest.awt.attributes.Textable#getText()
	 */
	@Override
	public String getText ()
	{
		return getFilePath();
	}
	/*
	 * @see net.community.chest.awt.attributes.Textable#setText(java.lang.String)
	 */
	@Override
	public void setText (String t)
	{
		setFilePath(t);
	}
	/*
	 * @see net.community.chest.awt.attributes.Iconable#getIcon()
	 */
	@Override
	public Icon getIcon ()
	{
		return (null == _filePath) ? null : _filePath.getIcon();
	}
	/*
	 * @see net.community.chest.awt.attributes.Iconable#setIcon(javax.swing.Icon)
	 */
	@Override
	public void setIcon (Icon i)
	{
		if (_filePath != null)
			_filePath.setIcon(i);
	}
	/*
	 * @see net.community.chest.swing.component.panel.BasePanel#getPanelConverter(org.w3c.dom.Element)
	 */
	@Override
	protected XmlProxyConvertible<?> getPanelConverter (Element elem)
	{
		return (null == elem) ? null : DependencyDetailsPanelReflectiveProxy.DDPNL;
	}

	public DependencyDetailsPanel (final Collection<? extends DependencyTargetEntry> deps)
	{
		super(new BorderLayout());
		setName(getClass().getSimpleName());

		_filePath = new JLabel("");
		add(_filePath, BorderLayout.NORTH);

		_depsTbl = new DependencyDetailsTable();
		_depsTbl.setFillsViewportHeight(true);
		_depsTbl.setDependencies(deps);
		add(new DefaultTableScroll(_depsTbl), BorderLayout.CENTER);
	}

	public DependencyDetailsPanel ()
	{
		this(null);
	}
	/**
	 * Looks for 1st entry whose group/artifact/version matches the specified one
	 * (case <U>insensitive</U>).
	 * @param groupId Group ID
	 * @param artifactId Artifact name
	 * @param version Version name - if null/empty then version is ignored
	 * @return Index of 1st match - negative if not found
	 */
	public int indexOf (final String groupId, final String artifactId, final String version /* may be null/empty */)
	{
		final DependencyDetailsTable	tbl=getDependencyDetailsTable();
		return (null == tbl) ? (-1) : tbl.indexOf(groupId, artifactId, version);
	}
	/**
	 * Looks for 1st entry whose group/artifact matches the specified one
	 * (case <U>insensitive</U>).
	 * @param groupId Group ID
	 * @param artifactId Artifact name
	 * @return Index of 1st match - negative if not found
	 * @see #indexOf(String, String, String) for specifying a version as well
	 */
	public int indexOf (final String groupId, final String artifactId)
	{
		return indexOf(groupId, artifactId, null);
	}
	/**
	 * Called by default implementation of {@link #setDependencies(String)} in order
	 * to load the dependencies
	 * @param path The file path from which to load
	 * @return A {@link Collection} of loaded {@link BaseTargetDetails} instances 
	 * @throws Exception If failed to load the dependencies
	 */
	public abstract Collection<? extends BaseTargetDetails> loadDependencies (final String path) throws Exception;

	public Collection<? extends DependencyTargetEntry> setDependencies (final String path) throws Exception
	{
		final Collection<? extends BaseTargetDetails>	deps=loadDependencies(path);
		setFilePath(path);

		final int	numDeps=(null == deps) ? 0 : deps.size();
		if (numDeps <= 0)
			return null;

		final Collection<DependencyTargetEntry>	dl=new ArrayList<DependencyTargetEntry>(numDeps);
		for (final BaseTargetDetails t : deps)
		{
			final DependencyTargetEntry	e=(null == t) ? null : new DependencyTargetEntry(t);
			if (null == e)
				continue;

			dl.add(e);
		}

		setDependencies(dl);
		return dl;
	}

	public void markMismatchedDependencies (final Collection<? extends Map.Entry<Integer,Color>> cl)
	{
		final DependencyDetailsTable	tbl=getDependencyDetailsTable();
		if (tbl != null)	// should not be otherwise
			tbl.markMismatchedDependencies(cl);
	}

	public void setFilterMode (final DependencyMismatchType	mode)
	{
		final DependencyDetailsTable	tbl=getDependencyDetailsTable();
		if (tbl != null)	// should not be otherwise
			tbl.setFilterMode(mode);
	}
}
