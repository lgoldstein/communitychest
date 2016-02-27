/*
 * 
 */
package net.community.apps.tools.xmlstruct;

import java.lang.reflect.Method;
import java.util.Map;

import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilderFactory;

import net.community.chest.reflect.AttributeAccessor;
import net.community.chest.ui.components.dialog.BooleanOptionsDialog;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 5, 2009 3:15:28 PM
 */
public class DocBuilderOptionsDialog extends BooleanOptionsDialog<DocumentBuilderFactory> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3335118080650913744L;
	public DocBuilderOptionsDialog (JFrame owner, DocumentBuilderFactory fac, Element elem, boolean autoInit)
	{
		super(owner, elem, autoInit);
		setContent(fac);
	}

	public DocBuilderOptionsDialog (JFrame owner, DocumentBuilderFactory fac, boolean autoInit)
	{
		this(owner, fac, null, autoInit);
	}

	public DocBuilderOptionsDialog (JFrame owner, DocumentBuilderFactory fac)
	{
		this(owner, fac, true);
	}

	public DocBuilderOptionsDialog (JFrame owner, boolean autoInit)
	{
		this(owner, null, autoInit);
	}

	public DocBuilderOptionsDialog (JFrame owner)
	{
		this(owner, true);
	}

	private static Map<String,AttributeAccessor>	_optsAccMap	/* =null */;
	private static final synchronized Map<String,AttributeAccessor> getDefaultOptionsMap ()
	{
		if (null == _optsAccMap)
			_optsAccMap = getOptionsAccessMap(DocumentBuilderFactory.class);

		return _optsAccMap;
	}
	/*
	 * @see net.community.chest.ui.components.dialog.BooleanOptionsDialog#getOptionsAccessMap()
	 */
	@Override
	protected Map<String,AttributeAccessor> getOptionsAccessMap ()
	{
		return getDefaultOptionsMap();
	}

	public static final AttributeAccessor setOptionValue (final DocumentBuilderFactory fac, final String n, final boolean optVal) throws Exception
	{
		final Boolean									f=Boolean.valueOf(optVal);
		final Map<String,? extends AttributeAccessor>	am=getDefaultOptionsMap();
		final AttributeAccessor							a=
			((null == am) || (am.size() <= 0)) ? null : am.get(n);
		final Method									sm=(null == a) ? null : a.getSetter();
		if (sm == null)
			return a;	// debug breakpoint

		sm.invoke(fac, f);
		return a;
	}
	// definition is assumed to be 'name=true/false'
	public static final AttributeAccessor setOptionValue (final String optDef, final DocumentBuilderFactory fac) throws Exception
	{
		final int	dLen=(null == optDef) ? 0 : optDef.length(),
					sPos=(dLen <= 2) /* must be at least a=b */ ? (-1) : optDef.lastIndexOf('=');
		if ((sPos <= 0) || (sPos >= (dLen-1)))
		{
			if (dLen <= 0)
				return null;
			throw new IllegalArgumentException("setOptionValue(" + optDef + ") bad format");
		}

		final String	n=optDef.substring(0, sPos), v=optDef.substring(sPos+1);
		return setOptionValue(fac, n, Boolean.parseBoolean(v.toLowerCase()));
	}
}
