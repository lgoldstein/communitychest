/*
 * 
 */
package net.community.chest.hibernate.core;

import net.community.chest.db.sql.SQLKeyword;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Order;
import org.hibernate.util.StringHelper;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Used to provide a fixed SQL fragment as its "ORDER BY" clause</P>
 * @author Lyor G.
 * @since Jun 21, 2009 1:10:22 PM
 */
public class ClauseOrder extends Order {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4758184791306604748L;
	private final String	_propertyName	/* =null */;
	public final String getPropertyName ()
	{
		return _propertyName;
	}

	private final boolean	_ascending;
	public final boolean isAscending ()
	{
		return _ascending;
	}

	private String	_sqlClause	/* =null */;
	public String getSqlClause ()
	{
		return _sqlClause;
	}

	public void setSqlClause (String sqlClause)
	{
		_sqlClause = sqlClause;
	}

	public ClauseOrder (String propertyName, boolean ascending, String sqlClause)
	{
		super(propertyName, ascending);

		if ((null == (_propertyName=propertyName)) || (propertyName.length() <= 0))
			throw new IllegalArgumentException("No property name provided");

		_ascending = ascending;
		_sqlClause = sqlClause;
	}

	public ClauseOrder (String propertyName, Boolean ascending, String sqlClause)
	{
		this(propertyName, (null == ascending) ? true : ascending.booleanValue(), sqlClause);
	}

	public ClauseOrder (String propertyName, boolean ascending)
	{
		this(propertyName, ascending, null);
	}
	/*
	 * @see org.hibernate.criterion.Order#toSqlString(org.hibernate.Criteria, org.hibernate.criterion.CriteriaQuery)
	 */
	@Override
	public String toSqlString (Criteria criteria, CriteriaQuery criteriaQuery)
		throws HibernateException
	{
		final String	frag=toString(),
						alias=criteriaQuery.getSQLAlias(criteria),
						ordr=StringHelper.replace(frag, CriterionUtils.ALIAS_PATTERN, alias); 
		return ordr;
	}
	/*
	 * @see org.hibernate.criterion.Order#toString()
	 */
	@Override
	public String toString ()
	{
		final String		c=getSqlClause();
		final int			l=(null == c) ? 0 : c.length();
		final SQLKeyword	kw=isAscending() ? SQLKeyword.ASC : SQLKeyword.DESC;
		final StringBuilder	sb=new StringBuilder(Math.max(l, 0) + 8)
								.append((l <= 0) ? "" : c)
								.append(' ').append(kw.getKeyword())
							;
		return sb.toString();
	}
}
