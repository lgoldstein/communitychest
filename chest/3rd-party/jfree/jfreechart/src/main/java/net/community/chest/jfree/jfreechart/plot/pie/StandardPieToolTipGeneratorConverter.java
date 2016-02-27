/*
 * 
 */
package net.community.chest.jfree.jfreechart.plot.pie;

import java.text.NumberFormat;
import java.util.Locale;

import net.community.chest.dom.DOMUtils;

import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 26, 2009 3:01:29 PM
 */
public class StandardPieToolTipGeneratorConverter extends
		AbstractPieItemLabelGeneratorConverter<StandardPieToolTipGenerator> {
	public StandardPieToolTipGeneratorConverter ()
	{
		super(StandardPieToolTipGenerator.class);
	}
	/*
	 * @see net.community.chest.dom.transform.XmlValueInstantiator#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public StandardPieToolTipGenerator fromXml (Element elem) throws Exception
	{
		final Locale		l=getLocale(elem);
		final String		f=getLabelFormat(elem);
		final NumberFormat	nf=getNumberFormat(elem),
							pf=getPercentFormat(elem);
		if (l != null)
		{
			if ((nf != null) || (pf != null))
				throw new NoSuchMethodException("fromXml(" + DOMUtils.toString(elem) + ") over specified number format(s): number=" + nf + "/pecent=" + pf);

			if ((null == f) || (f.length() <= 0))
				return new StandardPieToolTipGenerator(l);
			else
				return new StandardPieToolTipGenerator(f, l);
		}
		else
		{
			if ((null == nf) && (null == pf))
			{
				if ((null == f) || (f.length() <= 0))
					return new StandardPieToolTipGenerator();
				else
					return new StandardPieToolTipGenerator(f);
			}

			if ((null == nf) || (null == pf))
				throw new NoSuchMethodException("fromXml(" + DOMUtils.toString(elem) + ") missing number format(s): number=" + nf + "/pecent=" + pf);
			if ((null == f) || (f.length() <= 0))
				throw new NoSuchMethodException("fromXml(" + DOMUtils.toString(elem) + ") missing label format");

			return new StandardPieToolTipGenerator(f, nf, pf); 
		}
	}

	public static final StandardPieToolTipGeneratorConverter	DEFAULT=
		new StandardPieToolTipGeneratorConverter();
}
