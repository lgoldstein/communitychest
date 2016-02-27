/*
 * 
 */
package net.community.chest.ui.helpers.panel;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import net.community.chest.awt.layout.gridbag.ExtendedGridBagConstraints;
import net.community.chest.awt.layout.gridbag.ExtendedGridBagConstraintsReflectiveProxy;
import net.community.chest.awt.layout.gridbag.GridBagConstraintsReflectiveFieldsProxy;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.lang.ExceptionUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 11, 2009 1:44:06 PM
 *
 */
public class PresetGridBagLayoutPanel extends PresetLayoutPanel<GridBagLayout> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1833809600210957968L;
	public PresetGridBagLayoutPanel (Document doc, boolean autoLayout)
	{
		super(GridBagLayout.class, new GridBagLayout(), doc, autoLayout);
	}

	public PresetGridBagLayoutPanel (Document doc)
	{
		this(doc, true);
	}

	public PresetGridBagLayoutPanel (Element elem, boolean autoLayout)
	{
		this((null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}

	public PresetGridBagLayoutPanel (Element elem)
	{
		this(elem, true);
	}

	public PresetGridBagLayoutPanel (boolean autoLayout)
	{
		this((Document) null, autoLayout);
	}

	public PresetGridBagLayoutPanel ()
	{
		this(true);
	}

	private GridBagConstraints	_gbc;
	protected void setGridBagConstraints (GridBagConstraints gbc)
	{
		_gbc = gbc;
	}

	protected GridBagConstraints createGridBagConstraints ()
	{
		return new ExtendedGridBagConstraints();
	}

	protected GridBagConstraints getGridBagConstraints (boolean createIfNotExist, boolean reset)
	{
		if (null == _gbc)
		{
			if (createIfNotExist)
			{
				if (null == (_gbc=createGridBagConstraints()))
					return null;
			}
			else
				return null;
		}

		if (reset)
		{
			if (_gbc instanceof ExtendedGridBagConstraints)
				((ExtendedGridBagConstraints) _gbc).reset();
			else
				ExtendedGridBagConstraints.reset(_gbc);
		}

		return _gbc;
	}

	protected GridBagConstraints getGridBagConstraints ()
	{
		return getGridBagConstraints(false, false);
	}

	protected XmlProxyConvertible<?> getConstraintsConverter (GridBagConstraints gbc, Element elem)
	{
		if ((null == gbc) || (null == elem))
			return null;

		if (gbc instanceof ExtendedGridBagConstraints)
			return ExtendedGridBagConstraintsReflectiveProxy.EGBC;
		else
			return GridBagConstraintsReflectiveFieldsProxy.GBC;
	}

	protected GridBagConstraints getGridBagConstraints (Element elem, boolean reset)
	{
		final GridBagConstraints		gbc=getGridBagConstraints(true, reset);
		final XmlProxyConvertible<?>	p=getConstraintsConverter(gbc, elem);
		try
		{
			@SuppressWarnings("unchecked")
			final Object					o=
				(null == p) /* OK - allowed */ ? gbc : ((XmlProxyConvertible<Object>) p).fromXml(gbc, elem);
			if (o != gbc)
				throw new IllegalStateException("getGridBagConstraints(" + DOMUtils.toString(elem) + ") mismatched instances");
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}

		return gbc;
	}

	protected GridBagConstraints add (Component c, Element elem, boolean reset)
	{
		final GridBagConstraints	gbc=getGridBagConstraints(elem, reset);
		if (null == gbc)
			throw new IllegalStateException("add(" + DOMUtils.toString(elem) + ") no constraints");
		add(c, gbc);
		return gbc;
	}

	protected GridBagConstraints add (Component c, String gbcKey, boolean reset)
	{
		return add(c, getSection(gbcKey), reset);
	}

	protected GridBagConstraints add (Component c, Element elem)
	{
		return add(c, elem, true);
	}
	
	protected GridBagConstraints add (Component c, String gbcKey)
	{
		return add(c, getSection(gbcKey));
	}
}
