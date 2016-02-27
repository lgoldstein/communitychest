/*
 * 
 */
package net.community.chest.jfree.jfreechart.title;

import java.lang.reflect.Constructor;

import net.community.chest.lang.ExceptionUtil;

import org.jfree.chart.title.DateTitle;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <T> The reflected {@link DateTitle} type
 * @author Lyor G.
 * @since Jan 27, 2009 4:02:24 PM
 */
public class DateTitleReflectiveProxy<T extends DateTitle> extends TextTitleReflectiveProxy<T> {
	protected DateTitleReflectiveProxy (Class<T> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public DateTitleReflectiveProxy (Class<T> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final DateTitleReflectiveProxy<BaseDateTitle>	DATETTL=
		new DateTitleReflectiveProxy<BaseDateTitle>(BaseDateTitle.class, true) {
			/*
			 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#getElementConstructor()
			 */
			@Override
			protected Constructor<BaseDateTitle> getElementConstructor ()
			{
				Constructor<BaseDateTitle>	ctor=super.getElementConstructor();
				if (null == ctor)
				{
					try
					{
						ctor = BaseDateTitle.class.getConstructor(Element.class);
					}
					catch(NoSuchMethodException e) // should not happen
					{
						throw ExceptionUtil.toRuntimeException(e);
					}
				}
				return ctor;
			}
	};
}
