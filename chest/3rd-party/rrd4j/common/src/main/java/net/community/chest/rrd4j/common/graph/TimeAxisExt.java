package net.community.chest.rrd4j.common.graph;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.rrd4j.common.graph.helpers.TimeAxisReflectiveProxy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 24, 2008 5:20:26 PM
 */
public class TimeAxisExt implements XmlConvertible<TimeAxisExt>, PubliclyCloneable<TimeAxisExt> {
	private long _secPerPix;
	private int _minorUnit, _minorUnitCount, _majorUnit, _majorUnitCount;
	private int _labelUnit, _labelUnitCount, _labelSpan;
	private String _format;

	public long getSecondsPerPixel ()
	{
		return _secPerPix;
	}

	public void setSecondsPerPixel (long secPerPix)
	{
		_secPerPix = secPerPix;
	}

	public int getMinorUnit ()
	{
		return _minorUnit;
	}

	public void setMinorUnit (int minorUnit)
	{
		_minorUnit = minorUnit;
	}

	public int getMinorUnitCount ()
	{
		return _minorUnitCount;
	}

	public void setMinorUnitCount (int minorUnitCount)
	{
		_minorUnitCount = minorUnitCount;
	}

	public int getMajorUnit ()
	{
		return _majorUnit;
	}

	public void setMajorUnit (int majorUnit)
	{
		_majorUnit = majorUnit;
	}

	public int getMajorUnitCount ()
	{
		return _majorUnitCount;
	}

	public void setMajorUnitCount (int majorUnitCount)
	{
		_majorUnitCount = majorUnitCount;
	}

	public int getLabelUnit ()
	{
		return _labelUnit;
	}

	public void setLabelUnit (int labelUnit)
	{
		_labelUnit = labelUnit;
	}

	public int getLabelUnitCount ()
	{
		return _labelUnitCount;
	}

	public void setLabelUnitCount (int labelUnitCount)
	{
		_labelUnitCount = labelUnitCount;
	}

	public int getLabelSpan ()
	{
		return _labelSpan;
	}

	public void setLabelSpan (int labelSpan)
	{
		_labelSpan = labelSpan;
	}

	public String getFormat ()
	{
		return _format;
	}

	public void setFormat (String format)
	{
		_format = format;
	}

	public TimeAxisExt ()
	{
		super();
	}

	public TimeAxisExt (long secPerPix, int minorUnit, int minorUnitCount, int majorUnit, int majorUnitCount, int labelUnit, int labelUnitCount, int labelSpan, String format)
	{
		_secPerPix = secPerPix;
		_minorUnit = minorUnit;
		_minorUnitCount = minorUnitCount;
		_majorUnit = majorUnit;
		_majorUnitCount = majorUnitCount;
		_labelUnit = labelUnit;
		_labelUnitCount = labelUnitCount;
		_labelSpan = labelSpan;
		_format = format;
	}

	public TimeAxisExt (int minorUnit, int minorUnitCount, int majorUnit, int majorUnitCount, int labelUnit, int labelUnitCount, int labelSpan, String format)
	{
		this(0L, minorUnit, minorUnitCount, majorUnit, majorUnitCount, labelUnit, labelUnitCount, labelSpan, format);
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	@CoVariantReturn
	public TimeAxisExt clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	public TimeAxisExt fromXml (Element elem) throws Exception
	{
		if (TimeAxisReflectiveProxy.DEFAULT.fromXml(this, elem) != this)
			throw new IllegalStateException("Mismatched recovered instances");

		return this;
	}

	public TimeAxisExt (Element elem) throws Exception
	{
		if (this != fromXml(elem))
			throw new IllegalStateException("Mismatched re-constructed instances");
	}

	public String getRootElementName ()
	{
		return RrdGraphDefExt.TIME_AXIS_ATTR;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	public Element toXml (Document doc) throws Exception
	{
		// TODO implement toXml
		throw new UnsupportedOperationException("toXml() N/A");
	}
}
