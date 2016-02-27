package net.community.chest.db.sql;

import java.io.Serializable;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 30, 2008 1:32:34 PM
 */
public class ResultSetRowColumnData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6131894389248857753L;
	private transient ResultSetMetaData	_md	/* =null */;
	public ResultSetMetaData getMetaData ()
	{
		return _md;
	}

	public void setMetaData (ResultSetMetaData md)
	{
		_md = md;
	}

	private int	_columnIndex	/* =0 */;
	public int getColumnIndex ()
	{
		return _columnIndex;
	}

	public void setColumnIndex (int columnIndex)
	{
		_columnIndex = columnIndex;
	}

	public String getCatalogName () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		return (null == md) ? null : md.getCatalogName(getColumnIndex());
	}

	public String getColumnClassName () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		return (null == md) ? null : md.getColumnClassName(getColumnIndex());
	}

	public int getColumnDisplaySize () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		return (null == md) ? Integer.MIN_VALUE : md.getColumnDisplaySize(getColumnIndex());
	}

	public String getColumnLabel () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		return (null == md) ? null : md.getColumnLabel(getColumnIndex());
	}

	public String getColumnName () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		return (null == md) ? null : md.getColumnName(getColumnIndex());
	}

	public int getColumnType () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		return (null == md) ? Integer.MIN_VALUE : md.getColumnType(getColumnIndex());
	}

	public TypesEnum getColumnTypeValue () throws SQLException
	{
		return TypesEnum.fromTypeValue(getColumnType());
	}

	public String getColumnTypeName () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		return (null == md) ? null : md.getColumnTypeName(getColumnIndex());
	}

	public int getPrecision () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		return (null == md) ? Integer.MIN_VALUE : md.getPrecision(getColumnIndex());
	}

	public int getScale () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		return (null == md) ? Integer.MIN_VALUE : md.getScale(getColumnIndex());
	}

	public String getSchemaName () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		return (null == md) ? null : md.getSchemaName(getColumnIndex());
	}

	public String getTableName () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		return (null == md) ? null : md.getTableName(getColumnIndex());
	}

	public boolean isAutoIncrement () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		if (null == md)
			throw new SQLException("isAutoIncrement(" + getColumnIndex() + ") no " + ResultSetMetaData.class.getSimpleName() + " instance");

		return md.isAutoIncrement(getColumnIndex());
	}

	public boolean isCaseSensitive () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		if (null == md)
			throw new SQLException("isCaseSensitive(" + getColumnIndex() + ") no " + ResultSetMetaData.class.getSimpleName() + " instance");

		return md.isCaseSensitive(getColumnIndex());
	}

	public boolean isCurrency () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		if (null == md)
			throw new SQLException("isCurrency((" + getColumnIndex() + ") no " + ResultSetMetaData.class.getSimpleName() + " instance");

		return md.isCurrency(getColumnIndex());
	}

	public boolean isDefinitelyWritable () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		if (null == md)
			throw new SQLException("isDefinitelyWritable(" + getColumnIndex() + ") no " + ResultSetMetaData.class.getSimpleName() + " instance");

		return md.isDefinitelyWritable(getColumnIndex());
	}

	public int isNullable () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		return (null == md) ? Integer.MIN_VALUE : md.isNullable(getColumnIndex());
	}

	public boolean isReadOnly () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		if (null == md)
			throw new SQLException("isReadOnly(" + getColumnIndex() + ") no " + ResultSetMetaData.class.getSimpleName() + " instance");

		return md.isReadOnly(getColumnIndex());
	}

	public boolean isSearchable () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		if (null == md)
			throw new SQLException("isSearchable(" + getColumnIndex() + ") no " + ResultSetMetaData.class.getSimpleName() + " instance");

		return md.isSearchable(getColumnIndex());
	}

	public boolean isSigned () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		if (null == md)
			throw new SQLException("isSigned(" + getColumnIndex() + ") no " + ResultSetMetaData.class.getSimpleName() + " instance");

		return md.isSigned(getColumnIndex());
	}

	public boolean isWritable () throws SQLException
	{
		final ResultSetMetaData	md=getMetaData();
		if (null == md)
			throw new SQLException("isWritable(" + getColumnIndex() + ") no " + ResultSetMetaData.class.getSimpleName() + " instance");

		return md.isWritable(getColumnIndex());
	}

	private Object	_colValue	/* =null */;
	public Object getColumnValue ()
	{
		return _colValue;
	}

	public void setColumnValue (Object colValue)
	{
		_colValue = colValue;
	}

	public ResultSetRowColumnData (ResultSetMetaData md, int columnIndex, Object colValue)
	{
		_md = md;
		_columnIndex = columnIndex;
		_colValue = colValue;
	}

	public ResultSetRowColumnData (ResultSetMetaData md, int columnIndex)
	{
		this(md, columnIndex, null);
	}

	public ResultSetRowColumnData (ResultSetMetaData md)
	{
		this(md, 0);
	}

	public ResultSetRowColumnData ()
	{
		this(null);
	}

	public static final List<ResultSetRowColumnData> buildColumnsDataList (final ResultSetMetaData md) throws SQLException
	{
		final int	cc=(null == md) ? 0 : md.getColumnCount();
		if (cc <= 0)
			return null;

		// prepare a results set row data columns list
		final List<ResultSetRowColumnData>	cl=new ArrayList<ResultSetRowColumnData>(cc);
		for (int	cIndex=1; cIndex <= cc; cIndex++)
			cl.add(new ResultSetRowColumnData(md, cIndex));

		return cl;
	}
}
