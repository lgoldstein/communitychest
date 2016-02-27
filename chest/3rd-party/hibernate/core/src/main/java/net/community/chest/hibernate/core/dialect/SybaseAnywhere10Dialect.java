/*
 * 
 */
package net.community.chest.hibernate.core.dialect;

import net.community.chest.db.sql.SQLKeyword;
import net.community.chest.lang.StringUtil;
import net.community.chest.lang.math.NumberTables;

import org.hibernate.dialect.SybaseAnywhereDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Special override to allow partial results set fetching</P>
 * 
 * @author Lyor G.
 * @since Jun 21, 2009 1:30:59 PM
 */
public class SybaseAnywhere10Dialect extends SybaseAnywhereDialect {
	private static final Logger _logger=LoggerFactory.getLogger(SybaseAnywhere10Dialect.class );
	public SybaseAnywhere10Dialect ()
	{
		super();
	}
	/*
	 * @see org.hibernate.dialect.Dialect#supportsLimit()
	 */
	@Override
	public boolean supportsLimit ()
	{
		return true;
	}
	/*
	 * @see org.hibernate.dialect.Dialect#supportsLimitOffset()
	 */
	@Override
	public boolean supportsLimitOffset ()
	{
		return true;
	}
	/*
	 * @see org.hibernate.dialect.Dialect#supportsVariableLimit()
	 */
	@Override
	public boolean supportsVariableLimit ()
	{
		return false;
	}
	/*
	 * @see org.hibernate.dialect.Dialect#getLimitString(java.lang.String, boolean)
	 */
	@Override
	protected String getLimitString (String query, boolean hasOffset)
	{
		// don't care if no offset requested - which should be handled by the public "getLimitString" method
		if (hasOffset)
			_logger.error("getLimitString(" + query + ") unexpected call");
		return query;
	}

	public static final String	TOP_KWD="TOP", START_KWD="START AT";
	/*
	 * @see org.hibernate.dialect.Dialect#getLimitString(java.lang.String, int, int)
	 */
	@Override
	public String getLimitString (final String query, final int offset, final int limit)
	{
		if ((offset <= 0) && (limit < 0))
			return query;	// ignore if no limits

		final String	q=StringUtil.getCleanStringValue(query);
		final int		qLen=(null == q) ? 0 : q.length(),
						sPos=(qLen <= SQLKeyword.SELECT.getKeyword().length()) ? (-1) : q.indexOf(' ');
		final String	k=(sPos < SQLKeyword.SELECT.getKeyword().length()) ? null : q.substring(0, sPos);
		if (!SQLKeyword.SELECT.getKeyword().equalsIgnoreCase(k))	// unexpected but OK
		{
			_logger.warn("getLimitString(" + offset + "/" + limit + ") not a selection string: " + query);
			return query;
		}

		final StringBuilder	sb=
			new StringBuilder(qLen + TOP_KWD.length() + START_KWD.length() + 4 + 2 * NumberTables.MAX_UNSIGNED_INT_DIGITS_NUM)
				.append(k)
		/*
		 *  NOTE: see http://www.ianywhere.com/developer/product_manuals/sqlanywhere/0902/en/html/dbugen9/00000276.htm
		 *  
		 *  		FIRST and TOP should be used only in conjunction with an ORDER BY
		 *  		clause to ensure consistent results. Use of FIRST or TOP without
		 *  		an ORDER BY triggers a syntax warning, and will likely yield
		 *  		unpredictable results.
		 *
		 *  	The 'start at' value must be greater than 0.
		 *  	The 'top' value must be greater than 0.
		 */
		  .append(' ')
		  .append(TOP_KWD)
		  .append(' ')
		  .append((limit > 0) ? limit : Integer.MAX_VALUE /* placeholder for "all the rest" */)
		  ;
		if (offset > 0)
			sb.append(' ')
			  .append(START_KWD)
			  .append(' ')
			  .append(offset)
			  ;

		final String	remQ=q.substring(sPos);
		sb.append(remQ);

		final String	retQuery=sb.toString();
		if (_logger.isDebugEnabled())
			_logger.debug("getLimitString(" + offset + "/" + limit + ")[" + q + "] => [" + retQuery + "]");
		return retQuery;
	}
}
