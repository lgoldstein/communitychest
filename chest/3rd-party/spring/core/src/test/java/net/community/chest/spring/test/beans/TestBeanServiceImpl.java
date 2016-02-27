/*
 * 
 */
package net.community.chest.spring.test.beans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.inject.Inject;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.EOLStyle;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.spring.test.entities.DateTimeEntity;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lyor G.
 * @since Jul 20, 2010 8:56:28 AM
 */
@Service	// see section 3.10 of the Spring documentation
@Transactional
public class TestBeanServiceImpl implements TestBeanService {
	private final DateTimeEntityDao	_dao;
	protected final DateTimeEntityDao getDateTimeEntityDao ()
	{
		return _dao;
	}

	@Inject
	public TestBeanServiceImpl (final DateTimeEntityDao dao)
	{
		_dao = dao;
	}
	/**
	 * The actual {@link Appendable} instance implementing the API
	 */
	private Appendable	_out;
	public Appendable getOutput ()
	{
		return _out;
	}

	/* NOTE: relies on type-based bean resolution - i.e., there
	 * 		is only ONE bean that matches the setter-s argument type
	 */
	@Resource	// see section 3.9.3 tip in Spring documentation 
	@Required
	public void setOutput (Appendable out)
	{
		_out = out;
	}
	/*
	 * @see java.lang.Appendable#append(java.lang.CharSequence, int, int)
	 */
	@Override
	@CoVariantReturn
	public TestBeanService append (CharSequence csq, int start, int end)
			throws IOException
	{
		final Appendable	out=getOutput();
		if (null == out)
			throw new IOException("append(" + csq + ") no " + Appendable.class.getSimpleName() + " instance to append to");

		out.append(csq, start, end);
		return this;
	}
	/*
	 * @see java.lang.Appendable#append(java.lang.CharSequence)
	 */
	@Override
	@CoVariantReturn
	public TestBeanService append (CharSequence csq) throws IOException
	{
		return append(csq, 0, (null == csq) ? 0 : csq.length());
	}
	/*
	 * @see java.lang.Appendable#append(char)
	 */
	@Override
	@CoVariantReturn
	public TestBeanService append (char c) throws IOException
	{
		final Appendable	out=getOutput();
		if (null == out)
			throw new IOException("append(" + String.valueOf(c) + ") no " + Appendable.class.getSimpleName() + " instance to append to");

		out.append(c);
		return this;
	}

	private String	_username;
	@Required
	@Value("#{systemProperties[ 'user.name' ]}")
	public void setUsername (String name)
	{
		_username = name;
	}
	/*
	 * @see net.community.chest.spring.test.TestBeanService#println()
	 */
	@Override
	@CoVariantReturn
	public TestBeanService println () throws IOException
	{
		return append(EOLStyle.LOCAL.getStyleString());
	}

	@PostConstruct
	protected void beanReady () throws IOException
	{
		this.append(_username).append(" - ")
			.append(getClass().getName())
		  	.append(" - bean ready")
		  	;
		this.println();
	}

	private static final List<Order> createSortOrder (final Collection<DateTimeEntitySortOrder> listOrder)
	{
		final int	numSorts=(null == listOrder) ? 0 : listOrder.size();
		if (numSorts <= 0)
			return null;

		final List<Order>		ol=new ArrayList<Order>(numSorts);
		final Set<DateTimeEntitySortOrder>	os=EnumSet.noneOf(DateTimeEntitySortOrder.class);
		for (final DateTimeEntitySortOrder lo : listOrder)
		{
			final String	aName=(null == lo) ? null : lo.getAttributeName();
			final Order		o=((null == aName) || (aName.length() <= 0)) ? null : Order.asc(aName);
			if (null == o)
				continue;
			if (os.contains(lo))
				continue;
			if (!os.add(lo))
				continue;
			if (!ol.add(o))
				continue;
		}

		return ol;
	}
	/*
	 * @see net.community.chest.spring.test.beans.TestBeanService#listDateTimeEntities(java.util.Collection)
	 */
	@Override
	public List<DateTimeEntity> listDateTimeEntities (Collection<DateTimeEntitySortOrder> listOrder)
	{
		final DateTimeEntityDao				dao=getDateTimeEntityDao();
		final Collection<? extends Order>	ol=createSortOrder(listOrder);
		final List<DateTimeEntity>			res=dao.findByCriteria(true, null, ol);
		if ((null == res) || (res.size() <= 0))
			return null;

		return res;
	}
	/*
	 * @see net.community.chest.spring.test.beans.TestBeanService#listDateTimeEntities(net.community.chest.spring.test.beans.TestBeanService.SortOrder[])
	 */
	@Override
	public List<DateTimeEntity> listDateTimeEntities (DateTimeEntitySortOrder ... listOrder)
	{
		return listDateTimeEntities(((null == listOrder) || (listOrder.length <= 0)) ? null : Arrays.asList(listOrder));
	}
	/*
	 * @see net.community.chest.spring.test.beans.TestBeanService#findDateTimeEntity(java.util.Set, java.util.Collection)
	 */
	@Override
	public List<DateTimeEntity> findDateTimeEntity (
			final Collection<DateTimeEntitySortOrder> listOrder, final Collection<Long> ids)
	{
		final int							numIds=(null == ids) ? 0 : ids.size();
		final Criterion						c=
			(numIds <= 0) ? null : Restrictions.in("id", ids);
		final Collection<? extends Order>	ol=createSortOrder(listOrder);
		final DateTimeEntityDao				dao=getDateTimeEntityDao();
		final List<DateTimeEntity>			res=
			dao.findByCriteria(true, (null == c) ? null : Arrays.asList(c), ol);
		if ((null == res) || (res.size() <= 0))
			return null;

		return res;
	}
	/*
	 * @see net.community.chest.spring.test.beans.TestBeanService#findDateTimeEntity(java.util.Set, java.lang.Long[])
	 */
	@Override
	public List<DateTimeEntity> findDateTimeEntity (
			final Collection<DateTimeEntitySortOrder> listOrder, final Long... ids)
	{
		return findDateTimeEntity(listOrder, ((null == ids) || (ids.length <= 0)) ? null : Arrays.asList(ids));
	}
	/*
	 * @see net.community.chest.spring.test.beans.TestBeanService#createDateTimeEntity(java.lang.String, long)
	 */
	@Override
	public DateTimeEntity createDateTimeEntity (String name, long timestampValue)
	{
		final DateTimeEntity	dte=new DateTimeEntity(new Date(timestampValue), name);
		final DateTimeEntityDao	dao=getDateTimeEntityDao();
		dao.create(dte);
		final Long				id=dte.getId();
		try
		{
			this.append("==> createDateTimeEntity(")
				.append(dte.toString())
				.append(") => ID=")
				.append(String.valueOf(id))
				;
			this.println();
		}
		catch(IOException e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}

		return dte;
	}
	/*
	 * @see net.community.chest.spring.test.beans.TestBeanService#updateDateTimeEntity(net.community.chest.spring.test.entities.DateTimeEntity)
	 */
	@Override
	public void updateDateTimeEntity (DateTimeEntity dte)
	{
		final DateTimeEntityDao	dao=getDateTimeEntityDao();
		dao.update(dte);

		try
		{
			this.append("==> updateDateTimeEntity(")
				.append(dte.toString())
				.append(") - updated")
				;
			this.println();
		}
		catch(IOException e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}
	}
	/*
	 * @see net.community.chest.spring.test.beans.TestBeanService#deleteDateTimeEntity(java.lang.Long)
	 */
	@Override
	public DateTimeEntity deleteDateTimeEntity (Long id)
	{
		final DateTimeEntityDao	dao=getDateTimeEntityDao();
		final DateTimeEntity	dte=dao.findById(id);
		if (dte != null)
		{
			dao.delete(dte);

			try
			{
				this.append("==> deleteDateTimeEntity(")
					.append(dte.toString())
					.append(") - deleted ID=")
					.append(String.valueOf(id))
					;
				this.println();
			}
			catch(IOException e)
			{
				throw ExceptionUtil.toRuntimeException(e);
			}
		}

		return dte;
	}
	/*
	 * @see net.community.chest.spring.test.beans.TestBeanService#deleteDateTimeEntity(net.community.chest.spring.test.entities.DateTimeEntity)
	 */
	@Override
	public void deleteDateTimeEntity (DateTimeEntity dte)
	{
		if (dte != null)
		{
			final DateTimeEntityDao	dao=getDateTimeEntityDao();
			dao.delete(dte);

			try
			{
				this.append("==> deleteDateTimeEntity(")
					.append(dte.toString())
					.append(") - deleted")
					;
				this.println();
			}
			catch(IOException e)
			{
				throw ExceptionUtil.toRuntimeException(e);
			}
		}		
	}
}
