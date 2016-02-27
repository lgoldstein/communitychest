/*
 * 
 */
package net.community.chest.jmx.dom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 8, 2009 9:56:11 AM
 *
 */
public class MBeanOperationDescriptor
		extends MBeanFeatureDescriptor<MBeanOperationInfo>
		implements PubliclyCloneable<MBeanOperationDescriptor> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6698960231028315599L;
	/**
	 * Default (empty) constructor
	 */
	public MBeanOperationDescriptor ()
	{
		super(MBeanOperationInfo.class);
	}

	private Collection<MBeanParameterDescriptor>	_params;
	public Collection<MBeanParameterDescriptor> getParameters ()
	{
		return _params;
	}

	public void setParameters (Collection<MBeanParameterDescriptor> pl)
	{
		_params = pl;
	}
	/*
	 * @see net.community.chest.jmx.dom.MBeanFeatureDescriptor#clear()
	 */
	@Override
	public void clear ()
	{
		super.clear();

		final Collection<? extends MBeanParameterDescriptor>	pl=getParameters();
		if ((pl != null) && (pl.size() > 0))
			pl.clear();
	}

	public Collection<MBeanParameterDescriptor> addParameter (MBeanParameterDescriptor d)
	{
		Collection<MBeanParameterDescriptor>	pl=getParameters();
		if (null == d)
			return pl;

		if (null == pl)
		{
			setParameters(new LinkedList<MBeanParameterDescriptor>());
			if (null == (pl=getParameters()))
				throw new IllegalStateException("No parameters collection created");
		}

		pl.add(d);
		return pl;
	}

	public MBeanParameterDescriptor addParameter (MBeanParameterInfo aInfo)
	{
		if (null == aInfo)
			return null;

		final MBeanParameterDescriptor	d=new MBeanParameterDescriptor(aInfo);
		addParameter(d);
		return d;
	}
	// returns only the added parameters
	public Collection<MBeanParameterDescriptor> addParameters (MBeanParameterInfo ... params)
	{
		if ((null == params) || (params.length <= 0))
			return null;

		Collection<MBeanParameterDescriptor>	pl=null;
		for (final MBeanParameterInfo p : params)
		{
			final MBeanParameterDescriptor	d=addParameter(p);
			if (null == d)
				continue;
			if (null == pl)
				pl = new ArrayList<MBeanParameterDescriptor>(params.length);
			pl.add(d);
		}

		return pl;
	}

	public Collection<MBeanParameterDescriptor> setParameters (MBeanParameterInfo ... params)
	{
		final Collection<? extends MBeanParameterDescriptor>	pl=getParameters();
		if ((pl != null) && (pl.size() > 0))
			pl.clear();

		return addParameters(params);
	}

	public MBeanOperationDescriptor (String name, String type, String description, Object value, MBeanParameterInfo ... params)
	{
		super(MBeanOperationInfo.class, name, type, description, value);
		setParameters(params);
	}

	public MBeanOperationDescriptor (MBeanOperationInfo o, Object value)
	{
		this((null == o) ? null : o.getName(),
			 (null == o) ? null : o.getReturnType(),
			 (null == o) ? null : o.getDescription(),
			 value,
			 (null == o) ? null : o.getSignature());
	}

	public MBeanOperationDescriptor (MBeanOperationInfo o)
	{
		this(o, null);
	}

	public static final String	OPER_ELEM_NAME="operation";
	/*
	 * @see net.community.chest.jmx.dom.MBeanFeatureDescriptor#getMBeanFeatureElementName()
	 */
	@Override
	public String getMBeanFeatureElementName ()
	{
		return OPER_ELEM_NAME;
	}
	/*
	 * @see net.community.chest.jmx.dom.MBeanFeatureDescriptor#clone()
	 */
	@Override
	@CoVariantReturn
	public MBeanOperationDescriptor clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see net.community.chest.jmx.dom.MBeanFeatureDescriptor#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		final Class<?>	oc=(obj == null) ? null : obj.getClass();
		if (oc != getClass())
			return false;
		if (this == obj)
			return true;

		final MBeanOperationDescriptor	o=(MBeanOperationDescriptor) obj;
		if (!isSameDescriptor(o))
			return false;

		return CollectionsUtils.isSameMembers(getParameters(), o.getParameters());
	}
	/*
	 * @see net.community.chest.jmx.dom.MBeanFeatureDescriptor#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return super.hashCode()
			+ CollectionsUtils.getMembersHashCode(getParameters())
			;
	}

	public Collection<MBeanParameterDescriptor> addParameter (Element elem) throws Exception
	{
		final MBeanParameterDescriptor	d=(null == elem) ? null : new MBeanParameterDescriptor(elem);
		return addParameter(d);
	}

	public Collection<MBeanParameterDescriptor> setParameters (Element elem) throws Exception
	{
		final Collection<? extends Element>		el=DOMUtils.extractAllNodes(Element.class, elem, Node.ELEMENT_NODE);
		final int								numParams=(null == el) ? 0 : el.size();
		Collection<MBeanParameterDescriptor>	pl=getParameters();
		if (numParams <= 0)
			return pl;

		for (final Element de : el)
			pl = addParameter(de);
		return pl;
	}
	/*
	 * @see net.community.chest.jmx.dom.MBeanFeatureDescriptor#fromXml(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	public MBeanOperationDescriptor fromXml (Element elem) throws Exception
	{
		final Object	o=super.fromXml(elem);
		if (o != this)
			throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched re-constructed instances");

		setParameters(elem);
		return this;
	}

	public MBeanOperationDescriptor (Element elem) throws Exception
	{
		super(MBeanOperationInfo.class);

		if (fromXml(elem) != this)
			throw new IllegalStateException("<init>(" + DOMUtils.toString(elem) + ") mismatched re-constructed instances");
	}

	public List<Element> createParameters (Document doc) throws Exception
	{
		final Collection<? extends MBeanParameterDescriptor>	pl=getParameters();
		final int												numParams=(null == pl) ? 0 : pl.size();
		final List<Element>										el=(numParams <= 0) ? null : new ArrayList<Element>(numParams);
		if (el == null)
			return null;

		for (final MBeanParameterDescriptor d : pl)
		{
			final Element	elem=(null == d) ? null : d.toXml(doc);
			if (null == elem)	// should not happen
				continue;
			el.add(elem);
		}

		return el;
	}

	public List<Element> addParameters (Document doc, Element root) throws Exception
	{
		final List<Element>	el=createParameters(doc);
		DOMUtils.appendChildren(root, el);
		return el;
	}
	
	public Element toXml (final Document doc, final boolean fetchParams) throws Exception
	{
		final Element	elem=super.toXml(doc);
		if (fetchParams)
			addParameters(doc, elem);

		return elem;
	}
	/*
	 * @see net.community.chest.jmx.dom.MBeanFeatureDescriptor#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		return toXml(doc, true);
	}
}
