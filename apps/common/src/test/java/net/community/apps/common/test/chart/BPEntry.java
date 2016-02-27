/*
 * 
 */
package net.community.apps.common.test.chart;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.compare.AbstractComparator;
import net.community.chest.util.datetime.DateUtil;

public class BPEntry implements Comparable<BPEntry>, PubliclyCloneable<BPEntry> {
	public BPEntry ()
	{
		super();
	}

	private int	_diaValue, _sisValue, _hrValue;
	public int getDiaValue ()
	{
		return _diaValue;
	}

	public void setDiaValue (int diaValue)
	{
		_diaValue = diaValue;
	}

	public int getSisValue ()
	{
		return _sisValue;
	}

	public void setSisValue (int sisValue)
	{
		_sisValue = sisValue;
	}

	public int getHrValue ()
	{
		return _hrValue;
	}

	public void setHrValue (int hrValue)
	{
		_hrValue = hrValue;
	}

	private Calendar	_timestamp;
	public Calendar getTimestamp ()
	{
		return _timestamp;
	}

	public void setTimestamp (Calendar timestamp)
	{
		_timestamp = timestamp;
	}
	/*
	 * @see java.lang.Object#clone()
	 */
	@Override
	public BPEntry clone () throws CloneNotSupportedException
	{
		return getClass().cast(super.clone());
	}
	/*
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo (BPEntry o)
	{
		if (null == o)
			return (-1);
		if (this == o)
			return 0;

		int	nRes=AbstractComparator.compareComparables(getTimestamp(), o.getTimestamp());
		if (nRes != 0)
			return nRes;

		if ((nRes=getSisValue() - o.getSisValue()) != 0)
			return nRes;
		if ((nRes=getDiaValue() - o.getDiaValue()) != 0)
			return nRes;
		if ((nRes=getHrValue() - o.getHrValue()) != 0)
			return nRes;

		return 0;
	}
	/*
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (!(obj instanceof BPEntry))
			return false;

		return (0 == compareTo((BPEntry) obj));
	}
	/*
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		return ClassUtil.getObjectHashCode(getTimestamp())
			+ getSisValue()
			+ getDiaValue()
			+ getHrValue()
			;
	}
	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		final Calendar		c=getTimestamp();
		final Date			t=(null == c) ? null : c.getTime();
		final DateFormat	dtf=(null == t) ? null : DateFormat.getDateTimeInstance();
		final String		dts=(null == dtf) ? null : dtf.format(t);
		return dts + ": sis=" + getSisValue() + "/dia=" + getDiaValue() + "/hr=" + getHrValue();  
	}

	public static final BPEntry fromValue (String dts, String tms, String sisv, String diav, String hrv)
	{
		if ((null == dts) || (dts.length() <= 0)
		 || (null == tms) || (tms.length() <= 0))
			throw new IllegalArgumentException("No timestamp specified");

		final int[]		dtv=DateUtil.getDateComponents(dts, '-'),
						ttv=DateUtil.getTimeComponents(tms, true);
		final Calendar	c=DateUtil.createCalendarValue(dtv, ttv, 0);
		final BPEntry	bpe=new BPEntry();
		bpe.setTimestamp(c);

		if ((sisv != null) && (sisv.length() > 0))
			bpe.setSisValue(Integer.parseInt(sisv));
		if ((diav != null) && (diav.length() > 0))
			bpe.setDiaValue(Integer.parseInt(diav));
		if ((hrv != null) && (hrv.length() > 0))
			bpe.setHrValue(Integer.parseInt(hrv));

		return bpe;
	}
}