package net.community.chest.rrd4j.common.jmx;

import java.util.ArrayList;
import java.util.Collection;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.jmx.dom.MBeanAttributeDescriptor;
import net.community.chest.jmx.dom.MBeanEntryDescriptor;
import net.community.chest.rrd4j.common.core.RrdBackendFactoryExt;
import net.community.chest.rrd4j.common.core.RrdDefExt;

import org.rrd4j.core.DsDef;
import org.rrd4j.core.RrdBackendFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used to represent both an {@link MBeanEntryDescriptor} and an {@link org.rrd4j.core.RrdDef}
 * @author Lyor G.
 * @since Jan 9, 2008 1:32:15 PM
 */
public class MBeanRrdDef extends RrdDefExt {
	public MBeanRrdDef (String path)
	{
		super(path);
	}

	public MBeanRrdDef (String path, long step)
	{
		super(path, step);
	}

	public MBeanRrdDef (String path, long startTime, long step)
	{
		super(path, startTime, step);
	}

	public MBeanRrdDef ()
	{
		super();
	}

	public MBeanRrdDef (Element elem) throws Exception
	{
		super(elem);
	}

	private String	_mbeanName	/* =null */;
	public String getMBeanName ()
	{
		return _mbeanName;
	}

	public void setMBeanName (String mbeanName)
	{
		_mbeanName = mbeanName;
	}

	public String setMBeanName (Element elem)
	{
		final String	val=elem.getAttribute(MBeanEntryDescriptor.NAME_ATTR);
		if ((val != null) && (val.length() > 0))
			setMBeanName(val);

		return val;
	}

	public Element addMBeanName (Element elem)
	{
		return DOMUtils.addNonEmptyAttribute(elem, MBeanEntryDescriptor.NAME_ATTR, getMBeanName());
	}

	private RrdBackendFactory	_fac	/* =null */;
	public synchronized RrdBackendFactory getBackendFactory ()
	{
		if (null == _fac)
			_fac = RrdBackendFactory.getDefaultFactory();
		return _fac;
	}

	public synchronized void setBackendFactory (RrdBackendFactory fac)
	{
		_fac = fac;
	}

	public RrdBackendFactory setBackendFactory (Element elem) throws Exception
	{
		final RrdBackendFactory	fac=RrdBackendFactoryExt.resolveFactory(elem, null);
		if (fac != null)
			setBackendFactory(fac);
		return fac;
	}

	public Element addBackendFactory (Element elem)
	{
		return RrdBackendFactoryExt.addNonEmptyFactory(elem, getBackendFactory());
	}
	/*
	 * @see net.community.chest.rrd4j.common.core.RrdDefExt#addDatasource(org.w3c.dom.Element)
	 */
	@Override
	public DsDef addDatasource (Element elem) throws Exception
	{
		final MBeanDsDef	dsDef=(null == elem) ? null : new MBeanDsDef(elem);
		if (dsDef != null)
			addDatasource(dsDef);
		return dsDef;
	}
	/*
	 * @see net.community.chest.rrd4j.common.core.RrdDefExt#fromXml(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	public MBeanRrdDef fromXml (Element elem) throws Exception
	{
		if (this != super.fromXml(elem))
			throw new IllegalStateException("fromXml() mismatched super-fromXml instance");

		setMBeanName(elem);
		setBackendFactory(elem);

		return this;
	}
	/*
	 * @see net.community.chest.rrd4j.common.core.RrdDefExt#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		final Element	elem=super.toXml(doc);
		addMBeanName(elem);
		addBackendFactory(elem);
		return elem;
	}
	/**
	 * @return An {@link MBeanEntryDescriptor} whose attributes are the
	 * names of the {@link DsDef}-s. If no MBean name or {@link DsDef}-s
	 * specified then returns null.
	 */
	public MBeanEntryDescriptor getMBeanDescriptor ()
	{
		final String	name=getMBeanName();
		final DsDef[]	dsa=getDsDefs();
		if (((null == name) || (name.length() <= 0))
		 && ((null == dsa) || (dsa.length <= 0)))
		 	return null;

		 final MBeanEntryDescriptor	d=new MBeanEntryDescriptor(name);
		 if ((dsa != null) && (dsa.length > 0))
		 {
			 for (final DsDef ds : dsa)
			 {
				 final String	dsName=(null == ds) /* should not happen */ ? null : ds.getDsName();
				 if ((null == dsName) || (dsName.length() <= 0))
					 continue;	// should not happen

				 String	attrName=dsName;
				 if (ds instanceof MBeanDsDef)
				 {
					 if ((null == (attrName=((MBeanDsDef) ds).getMBeanAttributeName())) || (attrName.length() <= 0))
						 attrName = dsName;
				 }

				 d.addAttribute(new MBeanAttributeDescriptor(attrName));
			 }
		 }

		 return d;
	}

	public static final Collection<MBeanEntryDescriptor> getDescriptors (final Collection<? extends MBeanRrdDef> defs)
	{
		final int	numDefs=(null == defs) ? 0 : defs.size();
		if (numDefs <= 0)
			return null;

		final Collection<MBeanEntryDescriptor>	mbl=new ArrayList<MBeanEntryDescriptor>(numDefs);
		for (final MBeanRrdDef d : defs)
		{
			final MBeanEntryDescriptor mb=(null == d) ? null : d.getMBeanDescriptor();
			if (null == mb)	// should not happen
				continue;

			mbl.add(mb);
		}

		return mbl;
	}

	public static final XmlValueInstantiator<MBeanRrdDef>	XMLINST=new XmlValueInstantiator<MBeanRrdDef> () {
		/*
		 * @see net.community.chest.dom.transform.XmlValueInstantiator#fromXml(org.w3c.dom.Element)
		 */
		public MBeanRrdDef fromXml (Element elem) throws Exception
		{
			return (null == elem) ? null : new MBeanRrdDef(elem);
		}
	};

	public static final Collection<MBeanRrdDef> readDefinitions (final NodeList nodes) throws Exception
	{
		return DOMUtils.extractValues(nodes, XMLINST);
	}

	public static final Collection<MBeanRrdDef> readDefinitions (final Element root) throws Exception
	{
		return (null == root) ? null : readDefinitions(root.getChildNodes());
	}

	public static final Collection<MBeanRrdDef> readDefinitions (final Document doc) throws Exception
	{
		return (null == doc) ? null : readDefinitions(doc.getDocumentElement());
	}
}
