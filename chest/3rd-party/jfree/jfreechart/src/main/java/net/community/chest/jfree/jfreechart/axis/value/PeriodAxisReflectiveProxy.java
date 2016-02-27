/*
 * 
 */
package net.community.chest.jfree.jfreechart.axis.value;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.jfree.jfreechart.data.time.RegularTimePeriodValueStringInstantiator;

import org.jfree.chart.axis.PeriodAxis;
import org.jfree.data.time.RegularTimePeriod;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <A> Type of {@link PeriodAxis} being reflected
 * @author Lyor G.
 * @since May 6, 2009 9:55:30 AM
 */
public class PeriodAxisReflectiveProxy<A extends PeriodAxis> extends ValueAxisReflectiveProxy<A> {
	protected PeriodAxisReflectiveProxy (Class<A> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public PeriodAxisReflectiveProxy (Class<A> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}
	/*
	 * @see net.community.chest.jfree.jfreechart.ChartReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
	{
		if ((type != null) && RegularTimePeriod.class.isAssignableFrom(type))
			return (ValueStringInstantiator<C>) RegularTimePeriodValueStringInstantiator.DEFAULT; 

		return super.resolveAttributeInstantiator(name, type);
	}
	
	public static final PeriodAxisReflectiveProxy<PeriodAxis>	PERIOD=
		new PeriodAxisReflectiveProxy<PeriodAxis>(PeriodAxis.class, true);
}
