/*
 * 
 */
package net.community.chest.jfree.jfreechart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.StandardChartTheme;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <T> The reflected {@link StandardChartTheme} instance
 * @author Lyor G.
 * @since Feb 16, 2009 2:48:20 PM
 */
public class StandardChartThemeReflectiveProxy<T extends StandardChartTheme> extends ChartReflectiveAttributesProxy<T> {

	protected StandardChartThemeReflectiveProxy (Class<T> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public StandardChartThemeReflectiveProxy (Class<T> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final StandardChartThemeReflectiveProxy<StandardChartTheme>	STDTHEME=
		new StandardChartThemeReflectiveProxy<StandardChartTheme>(StandardChartTheme.class, true) {
			/*
			 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
			 */
			@Override
			public StandardChartTheme createInstance (Element elem) throws Exception
			{
				final ChartTheme	t=ChartFactory.getChartTheme();
				if (t instanceof StandardChartTheme)
					return (StandardChartTheme) t;

				return super.createInstance(elem);
			}
	};
}
